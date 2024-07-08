package com.oha.posting.service;

import com.oha.posting.config.exception.InvalidDataException;
import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.dto.comment.*;
import com.oha.posting.dto.external.ExternalUser;
import com.oha.posting.dto.kafka.PostCommentEvent;
import com.oha.posting.entity.*;
import com.oha.posting.repository.CommentLikeRepository;
import com.oha.posting.repository.CommentRepository;
import com.oha.posting.repository.PostRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;
    private final ExternalApiService externalApiService;
    private final CommentLikeRepository commentLikeRepository;
    private final KafkaProducer kafkaProducer;
    private final PostService postService;


    @Transactional(readOnly = true)
    public ResponseObject<List<CommentSearchResponse>> getCommentList(String token, Long postId, Long parentId, Integer offset, Integer size, Long userId) throws Exception {
        if((postId == null) == (parentId == null)) {
            throw new InvalidDataException(HttpStatus.BAD_REQUEST, "postId, parentId 둘 중 하나만 입력해주세요");
        }

        ResponseObject<List<CommentSearchResponse>> response = new ResponseObject<>();

        try {
            QComment qComment = QComment.comment;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qComment.isDel.eq(false));
            if(parentId != null) {
                builder.and(qComment.parent.commentId.eq(parentId));
                builder.and(qComment.type.ne("C"));
            }
            else {
                builder.and(qComment.post.postId.eq(postId));
                builder.and(qComment.type.eq("C"));
            }

            List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
            orderSpecifiers.add(qComment.regDtm.desc());

            List<CommentSearchResponse> commentList = commentRepository.searchCommentList(builder, orderSpecifiers, offset, size);

            if(commentList.isEmpty()) {
                throw new InvalidDataException(HttpStatus.NOT_FOUND, "댓글이 없습니다.");
            }
            else {
                List<Long> commentIds = commentList.stream().map(CommentSearchResponse::getCommentId).toList();
                Map<Long, List<Long>> likeUsers = commentRepository.getCommentLikeUsers(commentIds);
                for (CommentSearchResponse comment : commentList) {
                    List<Long> commentLikes = likeUsers.get(comment.getCommentId());
                    if (commentLikes != null) {
                        comment.setIsLike(commentLikes.stream().anyMatch(likeUserId -> likeUserId.equals(userId)));
                        comment.setLikeCount(commentLikes.size());
                    } else {
                        comment.setIsLike(false);
                        comment.setLikeCount(0);
                    }
                }

                Set<Long> userIds = new HashSet<>();
                for(CommentSearchResponse c : commentList) {
                    userIds.add(c.getUserId());
                    if(c.getReplyUserId() != null) {
                        userIds.add(c.getReplyUserId());
                    }
                }

                Map<Long, ExternalUser> userMap = externalApiService.getUserMap(token, userIds);

                Iterator<CommentSearchResponse> iterator = commentList.iterator();
                while(iterator.hasNext()) {
                    CommentSearchResponse c = iterator.next();
                    // user 정보
                    ExternalUser user = userMap.get(c.getUserId());

                    if(user == null) {
                        iterator.remove();
                        continue;
                    }

                    c.setUserName(user.getName());
                    c.setProfileUrl(user.getProfileUrl());

                    // tagged user 정보
                    if(c.getReplyUserId() != null) {
                        ExternalUser replyUser = userMap.get(c.getReplyUserId());
                        if(replyUser != null) {
                            c.setReplyUserName(replyUser.getName());
                        }
                    }
                }

                if(commentList.isEmpty()) {
                    throw new InvalidDataException(HttpStatus.NOT_FOUND, "댓글이 없습니다.");
                }

                response.setResponse(HttpStatus.OK.value(), "Success", commentList);
            }
        } catch (InvalidDataException e) {
            log.warn("Exception during comment search", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during comment search", e);
            throw new Exception("댓글 조회에 실패하였습니다.");
        }

        return response;
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<CommentInsertResponse> insertComment(CommentInsertRequest dto, String token, Long userId, HttpServletResponse httpServletResponse) throws Exception {
        ResponseObject<CommentInsertResponse> response = new ResponseObject<>();
        CommentInsertResponse data = CommentInsertResponse.toResponse(dto);
        Comment comment = Comment.toEntity(dto);
        comment.setUserId(userId);

        int typeCnt = 0;
        if (dto.getPostId() != null) typeCnt++;
        if (dto.getParentId() != null) typeCnt++;
        if (dto.getReplyId() != null) typeCnt++;
        if (typeCnt != 1) {
            throw new InvalidDataException(HttpStatus.BAD_REQUEST, "postId, parentId, replyId 중 하나만 입력해주세요");
        }

        try {
            Post post;
            Comment reply = null;

            // 댓글 (Comment)
            if(dto.getParentId() == null && dto.getReplyId() == null) {
                // 게시물 확인
                post = postRepository.findByPostIdAndIsDel(dto.getPostId(), false)
                        .orElseThrow(() -> new InvalidDataException(HttpStatus.BAD_REQUEST, "게시물이 없습니다."));
                comment.setPost(post);
                comment.setType("C");
            }

            else {
                Comment parentComment;
                // 답글 (Reply)
                if (dto.getParentId() != null) {
                    // 부모 댓글 확인
                    parentComment = commentRepository.findByCommentIdAndIsDelAndType(dto.getParentId(), false, "C")
                            .orElseThrow(() -> new InvalidDataException(HttpStatus.BAD_REQUEST, "댓글이 없습니다."));

                    post = parentComment.getPost();
                    if (post == null || post.getIsDel()) {
                        throw new InvalidDataException(HttpStatus.BAD_REQUEST, "게시물이 없습니다.");
                    }

                    comment.setType("R");
                }

                // 답글의 댓글 (CommentInReply)
                else {
                    // 답글 확인
                    reply = commentRepository.findByCommentIdAndIsDelAndTypeIn(dto.getReplyId(), false, List.of("R", "RC"))
                            .orElseThrow(() -> new InvalidDataException(HttpStatus.BAD_REQUEST, "답글이 없습니다."));

                    parentComment = reply.getParent();
                    if (reply.getParent() == null || reply.getParent().getIsDel()) {
                        throw new InvalidDataException(HttpStatus.BAD_REQUEST, "댓글이 없습니다.");
                    }

                    post = reply.getParent().getPost();
                    if (reply.getParent().getPost() == null || reply.getParent().getPost().getIsDel()) {
                        throw new InvalidDataException(HttpStatus.BAD_REQUEST, "게시물이 없습니다.");
                    }

                    comment.setType("RC");
                    comment.setReply(reply);
                }

                comment.setPost(post);
                comment.setParent(parentComment);
            }

            Set<Long> userIds = new HashSet<>();
            userIds.add(userId);
            if(reply != null) {
                userIds.add(reply.getUserId());
            }

            // 유저 확인
            Map<Long, ExternalUser> userMap = externalApiService.getUserMap(token, userIds);
            if (userMap.size() != userIds.size()) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "사용자 정보를 찾을 수 없습니다.");
            }

            ExternalUser user = userMap.get(userId);
            data.setUserId(user.getUserId());
            data.setUserName(user.getName());
            data.setProfileUrl(user.getProfileUrl());

            if(reply != null) {
                ExternalUser replyUser = userMap.get(reply.getUserId());
                data.setReplyUserId(replyUser.getUserId());
                data.setReplyUserName(replyUser.getName());
            }

            Comment savedComment = commentRepository.save(comment);
            data.setCommentId(savedComment.getCommentId());
            data.setRegDtm(savedComment.getRegDtm());

            if(!post.getUserId().equals(userId)) { // 본인 게시물 댓글 알림 x
                kafkaProducer.sendPostCommentEvent(new PostCommentEvent(
                        post.getPostId()
                        , post.getUserId()
                        , userId
                        , comment.getContent()
                        , postService.getThumbnailUrl(post)));
            }

            response.setResponse(HttpStatus.CREATED.value(), "Success", data);
            httpServletResponse.setStatus(HttpStatus.CREATED.value());

        } catch (InvalidDataException e) {
            log.warn("Exception during comment insert", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during comment insert", e);
            throw new Exception("댓글 작성에 실패하였습니다.");
        }

        return response;
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<CommentUpdateResponse> updateComment(CommentUpdateRequest dto, String token, Long userId) throws Exception {
        ResponseObject<CommentUpdateResponse> response = new ResponseObject<>();

        try{
            Comment comment = commentRepository.findByCommentIdAndIsDel(dto.getCommentId(), false)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.NOT_FOUND, "댓글이 없습니다."));



            if(comment.getPost().getIsDel()) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "삭제된 게시물입니다.");
            }

            if(!userId.equals(comment.getUserId())) {
                throw new InvalidDataException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
            }

            comment.setContent(dto.getContent());
            comment.setUpdDtm(new Timestamp(System.currentTimeMillis()));

            CommentUpdateResponse data = CommentUpdateResponse.toResponse(comment);
            response.setResponse(HttpStatus.OK.value(), "Success", data);

        } catch (InvalidDataException e) {
            log.warn("Exception during comment update", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during comment update", e);
            throw new Exception("댓글 수정에 실패하였습니다.");
        }

        return response;
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<?> deleteComment(Long commentId, Long userId) throws Exception {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            Comment comment = commentRepository.findByCommentIdAndIsDel(commentId, false)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.NOT_FOUND, "댓글이 없습니다."));

            if(!userId.equals(comment.getUserId())) {
                throw new InvalidDataException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
            }
            comment.setIsDel(true);
            response.setResponse(HttpStatus.OK.value(), "Success");

        } catch (InvalidDataException e) {
            log.warn("Exception during comment delete", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during comment delete", e);
            throw new Exception("댓글 삭제에 실패하였습니다.");
        }

        return response;
    }

    @Transactional
    public ResponseObject<?> likeComment(CommentLikeRequest dto, Long userId, HttpServletResponse httpServletResponse) throws Exception {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            Comment comment = commentRepository.findByCommentIdAndIsDel(dto.getCommentId(), false)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.NOT_FOUND, "댓글이 없습니다."));

            if(comment.getPost().getIsDel()) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "삭제된 게시물입니다.");
            }

            CommentLikeId commentLikeId = new CommentLikeId(dto.getCommentId(), userId);
            Optional<CommentLike> existingLike = commentLikeRepository.findById(commentLikeId);

            if("L".equals(dto.getType())) {
                if(existingLike.isPresent()) {
                    throw new InvalidDataException(HttpStatus.CONFLICT, "이미 좋아요 상태입니다.");
                }

                comment.getLikes().add(new CommentLike(commentLikeId, comment));
                response.setResponse(HttpStatus.CREATED.value(), "Success");
                httpServletResponse.setStatus(HttpStatus.CREATED.value());
            } else {
                if (existingLike.isEmpty()) {
                    throw new InvalidDataException(HttpStatus.BAD_REQUEST, "좋아요 상태가 아닙니다.");
                }

                commentLikeRepository.delete(existingLike.get());
                response.setResponse(HttpStatus.OK.value(), "Success");
            }

        } catch (InvalidDataException e) {
            log.warn("Exception during comment like", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during comment like", e);
            throw new Exception("댓글 좋아요에 실패하였습니다.");
        }

        return response;
    }
}
