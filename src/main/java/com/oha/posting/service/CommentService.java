package com.oha.posting.service;

import com.oha.posting.config.exception.InvalidDataException;
import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.dto.comment.CommentInsertRequest;
import com.oha.posting.dto.comment.CommentInsertResponse;
import com.oha.posting.dto.comment.CommentSearchResponse;
import com.oha.posting.dto.external.ExternalUser;
import com.oha.posting.entity.Comment;
import com.oha.posting.entity.Post;
import com.oha.posting.entity.QComment;
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

import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;
    private final ExternalApiService externalApiService;


    @Transactional(readOnly = true)
    public ResponseObject<List<CommentSearchResponse>> getCommentList(String token, Long postId, Long parentId, Integer offset, Integer size) throws Exception {
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
                builder.and(qComment.isParent.eq(false));
            }
            else {
                builder.and(qComment.post.postId.eq(postId));
                builder.and(qComment.isParent.eq(true));
            }

            List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
            orderSpecifiers.add(qComment.regDtm.desc());

            List<CommentSearchResponse> commentList = commentRepository.searchCommentList(builder, orderSpecifiers, offset, size);

            if(commentList.isEmpty()) {
                throw new InvalidDataException(HttpStatus.NOT_FOUND, "댓글이 없습니다.");
            }
            else {
                Set<Long> userIds = new HashSet<>();
                for(CommentSearchResponse c : commentList) {
                    userIds.add(c.getUserId());
                    if(c.getTaggedUserId() != null) {
                        userIds.add(c.getTaggedUserId());
                    }
                }

                Map<Long, ExternalUser> userMap = externalApiService.getUserMap(token, userIds);
                if (userMap.size() != userIds.size()) {
                    throw new InvalidDataException(HttpStatus.BAD_REQUEST, "사용자 정보를 찾을 수 없습니다.");
                }

                for(CommentSearchResponse c : commentList) {
                    // user 정보
                    ExternalUser user = userMap.get(c.getUserId());
                    c.setUserNickname(user.getName());
                    c.setProfileUrl(user.getProfileUrl());

                    // tagged user 정보
                    if(c.getTaggedUserId() != null) {
                        ExternalUser taggedUser = userMap.get(c.getTaggedUserId());
                        c.setTaggedUserNickname(taggedUser.getName());
                    }
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

        try {
            // 게시물 확인
            Post post = postRepository.findByPostIdAndIsDel(dto.getPostId(), false)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.BAD_REQUEST, "게시물이 없습니다."));
            comment.setPost(post);

            Set<Long> userIds = new HashSet<>();
            userIds.add(userId);
            if(dto.getTaggedUserId() != null) {
                userIds.add(dto.getTaggedUserId());
            }

            // 유저 확인
            Map<Long, ExternalUser> userMap = externalApiService.getUserMap(token, userIds);
            if (userMap.size() != userIds.size()) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "사용자 정보를 찾을 수 없습니다.");
            }

            ExternalUser user = userMap.get(userId);
            data.setUserId(user.getUserId());
            data.setUserNickname(user.getName());
            data.setProfileUrl(user.getProfileUrl());

            if(dto.getTaggedUserId() != null) {
                ExternalUser taggedUser = userMap.get(dto.getTaggedUserId());
                comment.setTaggedUserId(taggedUser.getUserId());
                data.setTaggedUserId(taggedUser.getUserId());
                data.setTaggedUserNickname(taggedUser.getName());
            }

            if(dto.getParentId() != null) {
                // 부모 댓글 확인
                Comment parentComment = commentRepository.findByCommentIdAndIsDelAndIsParent(dto.getParentId(), false, true)
                        .orElseThrow(() -> new InvalidDataException(HttpStatus.BAD_REQUEST, "부모 댓글이 없습니다."));

                comment.setParent(parentComment);
                comment.setIsParent(false);
            }
            else {
                comment.setIsParent(true);
            }

            Comment savedComment = commentRepository.save(comment);
            data.setCommentId(savedComment.getCommentId());
            data.setRegDtm(savedComment.getRegDtm());

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

}
