package com.oha.posting.service;

import com.oha.posting.config.exception.InvalidDataException;
import com.oha.posting.config.file.FileConfig;
import com.oha.posting.config.file.FileUtil;
import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.dto.external.ExternalLocation;
import com.oha.posting.dto.external.ExternalUser;
import com.oha.posting.dto.kafka.PostLikeEvent;
import com.oha.posting.dto.kafka.PostReportEvent;
import com.oha.posting.dto.post.*;
import com.oha.posting.entity.*;
import com.oha.posting.repository.CommonCodeRepository;
import com.oha.posting.repository.LikeRepository;
import com.oha.posting.repository.PostRepository;
import com.oha.posting.repository.ReportRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;
    private final ExternalApiService externalApiService;
    private final CommonCodeRepository commonCodeRepository;
    private final LikeRepository likeRepository;
    private final FileConfig fileConfig;
    private final FileService fileService;
    private final KafkaProducer kafkaProducer;
    private final ReportRepository reportRepository;

    @Value("${file.base-url}")
    private String FILE_BASE_URL;

    @Value("${file.save-path}")
    private String SAVE_PATH;

    @Transactional(readOnly = true)
    public ResponseObject<PostSearchResponse> getPost(String token, Long postId, Long userId) throws Exception {
        ResponseObject<PostSearchResponse> response = new ResponseObject<>();

        try {
            Post post = postRepository.findByPostIdAndIsDel(postId, false)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다."));

            PostSearchResponse data = PostSearchResponse.toDto(post, userId);
            data.setThumbnailUrl(getThumbnailUrl(post));
            for(PostFile file : post.getFiles()) {
                data.getFiles().add(new PostSearchResponse.PostSearchFile(
                        getFileUrl(file),
                        file.getSeq()
                ));
            }

            // db 유저 정보 (user 서비스)
            Map<Long, ExternalUser> userMap = externalApiService.getUserMap(token, Set.of(post.getUserId()));
            if (userMap.size() != 1) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "사용자 정보를 찾을 수 없습니다.");
            }

            data.setUserName(userMap.get(post.getUserId()).getName());
            data.setProfileUrl(userMap.get(post.getUserId()).getProfileUrl());

            // 행정구역코드로 위치 조회 API 호출
            ExternalLocation location = externalApiService.getLocation(token, post.getRegionCode());
            data.setLocationInfo(location);

            response.setResponse(HttpStatus.OK.value(), "Success", data);
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post search", e);
            throw e;
        }
        catch (Exception e) {
            log.warn("Exception during post search", e);
            throw new Exception("게시물 조회에 실패하였습니다.");
        }

        return response;
    }

    @Transactional(readOnly = true)
    public ResponseObject<List<PostSearchResponse>> getPostList(String token, Long regionCode, Boolean popular, String categoryCode, Integer offset, Integer size, Long userId) throws Exception {
        ResponseObject<List<PostSearchResponse>> response = new ResponseObject<>();
        List<PostSearchResponse> dataList = new ArrayList<>();
        try{
            // 행정구역코드로 위치 조회 API 호출
            Map<String, ExternalLocation> locationMap = externalApiService.getLocationMap(token, regionCode);
            // 주변 동네 포함
            List<Long> regionCodes = (locationMap.keySet()).stream().map(item -> Long.parseLong(item)).toList();

            QPost qPost = QPost.post;

            // where
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qPost.isDel.eq(false));
            builder.and(qPost.regionCode.in(regionCodes)); // 위치 (필수)
            if(categoryCode != null) { // 카테고리
                builder.and(qPost.category.code.eq(categoryCode));
            }
            // order by
            List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
            if (popular != null && popular) { // 인기순
                orderSpecifiers.add(qPost.likes.size().desc());
            }
            orderSpecifiers.add(qPost.regDtm.desc()); // 최신순

            List<Post> postList = postRepository.searchPostList(builder, orderSpecifiers, offset, size);

            if(postList.isEmpty()) {
                throw new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다.");
            }
            else {
                Set<Long> userIds = new HashSet<>();
                for(Post post: postList) {
                    userIds.add(post.getUserId());
                }

//              user 리스트 조회
                Map<Long, ExternalUser> userMap = externalApiService.getUserMap(token, userIds);
                setPostInfo(dataList, postList, userMap, locationMap, userId);
                if(dataList.isEmpty()) {
                    throw new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다.");
                }

                response.setResponse(HttpStatus.OK.value(), "Success", dataList);
            }
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post search", e);
            throw e;
        }
        catch (Exception e) {
            log.warn("Exception during post search", e);
            throw new Exception("게시물 조회에 실패하였습니다.");
        }

        return response;
    }

    @Transactional(readOnly = true)
    public ResponseObject<List<PostSearchResponse>> getPostsByUser(String token, Long userId) throws Exception {
        ResponseObject<List<PostSearchResponse>> response = new ResponseObject<>();
        List<PostSearchResponse> dataList = new ArrayList<>();
        try{
            List<Post> postList = postRepository.searchPostList(userId);

            if(postList.isEmpty()) {
                throw new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다.");
            }
            else {
                Set<Long> userIds = Set.of(userId);

//              user 리스트 조회
                Map<Long, ExternalUser> userMap = externalApiService.getUserMap(token, userIds);

                Set<Long> codes = new HashSet<>();
                postList.forEach(post -> codes.add(post.getRegionCode()));
                Map<String, ExternalLocation> locationMap = externalApiService.getLocationMap(token, codes);

                setPostInfo(dataList, postList, userMap, locationMap, userId);
                if(dataList.isEmpty()) {
                    throw new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다.");
                }

                response.setResponse(HttpStatus.OK.value(), "Success", dataList);
            }
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post search", e);
            throw e;
        }
        catch (Exception e) {
            log.warn("Exception during post search", e);
            throw new Exception("게시물 조회에 실패하였습니다.");
        }

        return response;
    }

    private void setPostInfo(List<PostSearchResponse> dataList, List<Post> postList, Map<Long, ExternalUser> userMap, Map<String, ExternalLocation> locationMap, Long userId) {
        for(Post post: postList) {
            PostSearchResponse data = PostSearchResponse.toDto(post, userId);

            data.setThumbnailUrl(getThumbnailUrl(post));
            for(PostFile file : post.getFiles()) {
                data.getFiles().add(new PostSearchResponse.PostSearchFile(
                        getFileUrl(file),
                        file.getSeq()
                ));
            }

            // user 정보 매핑
            ExternalUser user = userMap.get(post.getUserId());
            if(user == null) {
                continue;
            }

            data.setUserName(user.getName());
            data.setProfileUrl(user.getProfileUrl());

            // 위치 정보 매핑
            ExternalLocation location = locationMap.get(post.getRegionCode().toString());
            if(location == null) {
                continue;
            }

            data.setLocationInfo(location);
            dataList.add(data);
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<PostInsertResponse> insertPost(PostInsertRequest dto, List<MultipartFile> files, String token, Long userId, HttpServletResponse httpServletResponse) throws Exception {
        ResponseObject<PostInsertResponse> response = new ResponseObject<>();
        PostInsertResponse data = new PostInsertResponse();
        Post post = Post.toEntity(dto);

        try {
            post.setUserId(userId);

            // 행정구역코드로 위치 조회 API 호출
            externalApiService.getLocation(token, post.getRegionCode());

            // category 조회
            CommonCode commonCode = commonCodeRepository.findByCode(dto.getCategoryCode())
                    .orElseThrow(() ->  new InvalidDataException(HttpStatus.BAD_REQUEST, "카테고리가 없습니다."));

            post.setCategory(commonCode);

            // keyword 생성
            if (dto.getKeywords() != null) {
                List<Keyword> keywords = (dto.getKeywords()).stream().map(keywordName -> new Keyword(post, keywordName)).toList();
                post.setKeywords(keywords);
            }

            /* file */
            saveFiles(files, post);
            Post savedPost = postRepository.save(post);
            data.setPostId(savedPost.getPostId());

            response.setResponse(HttpStatus.CREATED.value(), "Success", data);
            httpServletResponse.setStatus(HttpStatus.CREATED.value());
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post create", e);
            rollbackFile(post.getFiles());
            throw e;
        }
        catch (Exception e) {
            log.warn("Exception during post create", e);
            rollbackFile(post.getFiles());
            throw new Exception("게시물 저장에 실패하였습니다.");
        }

        return response;
    }

    @Async
    public void rollbackFile(List<PostFile> fileList) {
        for (PostFile file : fileList) {
            try {
                FileUtil.deleteFile(file.getDirectory()+file.getFileName());
            } catch (IOException e) {
                log.warn("Exception during file delete", e);
            }
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<?> updatePost(PostUpdateRequest dto, List<MultipartFile> files, String token, Long userId) throws Exception {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            Post post = postRepository.findByPostIdAndIsDel(dto.getPostId(), false)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다."));

            if (!userId.equals(post.getUserId())) {
                throw new InvalidDataException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
            }

            for(String item : dto.getUpdateItem().split(",")) {
                switch (item.trim()) {
                    case "content":
                        post.setContent(dto.getContent());
                        break;
                    case "categoryCode":
                        CommonCode commonCode = commonCodeRepository.findByCode(dto.getCategoryCode())
                                .orElseThrow(() ->  new InvalidDataException(HttpStatus.BAD_REQUEST, "카테고리가 없습니다."));

                        post.setCategory(commonCode);
                        break;
                    case "keywords":
                        post.getKeywords().clear();
                        if(dto.getKeywords() != null) {
                            (dto.getKeywords()).forEach(keywordName -> post.getKeywords().add(new Keyword(post, keywordName)));
                        }
                        break;
                    case "regionCode":
                        // 행정구역코드로 위치 조회 API 호출
                        externalApiService.getLocation(token, dto.getRegionCode());
                        post.setRegionCode(dto.getRegionCode());
                        break;
                    case "locationDetail":
                        post.setLocationDetail(dto.getLocationDetail());
                        break;
                    case "files":
                        if (files == null) {
                            throw new InvalidDataException(HttpStatus.BAD_REQUEST,"파일을 선택해주세요.");
                        }
                        rollbackFile(post.getFiles());
                        post.getFiles().clear();
                        saveFiles(files, post);
                        break;
                    default:
                        throw new InvalidDataException(HttpStatus.BAD_REQUEST, "일치하는 수정 항목이 없습니다.");
                }
            }

            post.setUpdDtm(new Timestamp(System.currentTimeMillis()));
            response.setResponse(HttpStatus.OK.value(), "Success");
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post update", e);
            throw e;
        }
        catch (Exception e) {
            log.warn("Exception during post update", e);
            throw new Exception("게시물 수정에 실패하였습니다");
        }

        return response;
    }

    private void saveFiles(List<MultipartFile> files, Post post) throws IOException, JCodecException {
        long timestamp = System.currentTimeMillis();
        for(int order=0; order<files.size(); order++) {
            MultipartFile file = files.get(order);

            if(!fileConfig.isAllowedFile(file)) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "지원하지 않는 확장자입니다.");
            }

            String extension = FileUtil.getFileExtension(file.getOriginalFilename());

            // 파일 db 저장
            String fileName = timestamp + "" + post.getUserId() + "" + order;
            String postSavePath = SAVE_PATH + "post/";

            PostFile postFile = new PostFile(post, postSavePath, fileName + "." + extension, order);
            post.getFiles().add(postFile);

            if(order == 0) {
                String thumbnailName = "s_"+fileName;
                post.setThumbnailName(thumbnailName+".jpg");

                // 파일, 썸네일 저장
                fileService.saveFile(file, postSavePath, fileName+ "." + extension, true);
            }
            else {
                // 파일만 저장
                fileService.saveFile(file, postSavePath, fileName+ "." + extension, false);
            }

        }
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<?> deletePost(Long postId, Long userId) throws Exception {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            Post post = postRepository.findByPostIdAndIsDel(postId, false)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다."));

            if (!userId.equals(post.getUserId())) {
                throw new InvalidDataException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
            }

            deletePost(post);

            response.setResponse(HttpStatus.OK.value(), "Success");
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post delete", e);
            throw e;
        }
        catch (Exception e) {
            log.warn("Exception during post delete", e);
            throw new Exception("게시물 삭제에 실패하였습니다");
        }

        return response;
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<?> like(Long userId, PostLikeRequest dto, HttpServletResponse httpServletResponse) throws Exception {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            Post post = postRepository.findByPostIdAndIsDel(dto.getPostId(), false)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다."));

            LikeId likeId = new LikeId(post.getPostId(), userId);
            Optional<Like> existingLike = likeRepository.findById(likeId);

            if ("L".equals(dto.getType())) {
                if (existingLike.isPresent()) {
                    throw new InvalidDataException(HttpStatus.CONFLICT, "이미 좋아요 상태입니다.");
                }
                // 좋아요 등록
                post.getLikes().add(new Like(new LikeId(dto.getPostId(), userId), post));
                response.setResponse(HttpStatus.CREATED.value(), "Success");
                httpServletResponse.setStatus(HttpStatus.CREATED.value());

                if(!post.getUserId().equals(userId)) {
                    String mediaType = getMediaType(post.getFiles());
                    kafkaProducer.sendPostLikeEvent(new PostLikeEvent(post.getPostId(), post.getUserId(), userId, mediaType
                            , getThumbnailUrl(post)));
                }
            } else {
                if (existingLike.isEmpty()) {
                    throw new InvalidDataException(HttpStatus.BAD_REQUEST, "좋아요 상태가 아닙니다.");
                }
                // 좋아요 삭제
                likeRepository.delete(existingLike.get());
                response.setResponse(HttpStatus.OK.value(), "Success");
            }
        } catch (InvalidDataException e) {
            log.warn("Exception during post like", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during post like", e);
            throw new Exception("게시물 좋아요에 실패하였습니다");
        }

        return response;
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<?> reportPost(Long userId, PostReportRequest dto) throws Exception {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            Post post = postRepository.findByPostIdAndIsDel(dto.getPostId(), false)
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다."));

            if(userId.equals(post.getUserId())) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "본인이 작성한 글은 신고하실 수 없습니다.");
            }
            // 중복 신고 막기
            for (Report report : post.getReports()) {
                if(userId.equals(report.getUserId())) {
                    throw new InvalidDataException(HttpStatus.CONFLICT, "신고 내역이 있습니다.");
                }
            }

            CommonCode commonCode = commonCodeRepository.findById(dto.getReasonCode())
                    .orElseThrow(() -> new InvalidDataException(HttpStatus.BAD_REQUEST, "잘못된 신고사유입니다."));

            Report newReport = new Report(userId, commonCode, post);
            post.getReports().add(newReport);
            response.setResponse(HttpStatus.CREATED.value(), "Success");
        } catch (InvalidDataException e) {
            log.warn("Exception during post report", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during post report", e);
            throw new Exception("게시물 신고에 실패하였습니다");
        }

        return response;
    }

    @Transactional(readOnly = true)
    public ResponseObject<List<PostReportingSearchResponse>> searchPostReporting(String token, Boolean isDone) throws Exception {
        ResponseObject<List<PostReportingSearchResponse>> response = new ResponseObject<>();

        try {
            QReport qReport = QReport.report;
            BooleanBuilder builder = new BooleanBuilder();
            if(isDone != null) {
                builder.and(qReport.isDone.eq(isDone));
            }

            List<Report> reportList = reportRepository.getReportList(builder);
            if(reportList.isEmpty()) {
                throw new InvalidDataException(HttpStatus.NOT_FOUND, "신고 내역이 없습니다.");
            } else {
                Set<Long> userIds = new HashSet<>();

                for(Report report: reportList){
                    userIds.add(report.getUserId());
                    userIds.add(report.getPost().getUserId());
                }

                Map<Long, ExternalUser> userMap = externalApiService.getUserMap(token, userIds);

                List<PostReportingSearchResponse> dataList = new ArrayList<>();
                for(Report report: reportList){
                    PostReportingSearchResponse dto = PostReportingSearchResponse.toDto(report);
                    dto.setThumbnailUrl(getThumbnailUrl(report.getPost()));
                    ExternalUser reportingUser = userMap.get(dto.getReportingUserId());
                    dto.setReportingUserName(reportingUser.getName());

                    ExternalUser reportedUser = userMap.get(dto.getReportedUserId());
                    dto.setReportedUserName(reportedUser.getName());
                    dataList.add(dto);
                }

                response.setResponse(HttpStatus.OK.value(), "Success", dataList);
            }
        } catch (InvalidDataException e) {
            log.warn("Exception during post reporting search", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during post reporting search", e);
            throw new Exception("게시물 신고 조회에 실패하였습니다");
        }

        return response;
    }

    @Transactional(rollbackFor = {Exception.class})
    public ResponseObject<?> updatePostReportingAction(PostReportIngActionRequest dto) throws Exception {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            List<CommonCode> actionCodes = new ArrayList<>();
            for (String actionCode: dto.getActionCodes()) {
                CommonCode action = commonCodeRepository.findById(actionCode).orElseThrow(() -> new InvalidDataException(HttpStatus.BAD_REQUEST, "잘못된 신고 조치 코드입니다."));
                actionCodes.add(action);
            }

            if (dto.getActionCodes().contains("REP_ACT_001")) {
                if (dto.getActionCodes().contains("REP_ACT_002")) {
                    throw new InvalidDataException(HttpStatus.BAD_REQUEST, "게시물 삭제/미삭제를 동시에 선택할 수 없습니다.");
                }
            }
            else if (dto.getActionCodes().contains("REP_ACT_003")) {
                throw new InvalidDataException(HttpStatus.BAD_REQUEST, "게시물을 삭제하는 경우에만 알림 발송이 가능합니다.");
            }

            Report report = reportRepository.findById(dto.getReportId()).orElseThrow(() ->
                    new InvalidDataException(HttpStatus.BAD_REQUEST, "신고 정보가 없습니다."));

            if (report.getIsDone()) {
                throw new InvalidDataException(HttpStatus.CONFLICT, "조치 완료된 신고 건입니다.");
            }

            Post post = report.getPost();
            // 게시물 삭제
            if (dto.getActionCodes().contains("REP_ACT_001")) {
                deletePost(post);
            }

            // 알림 발송
            if (dto.getActionCodes().contains("REP_ACT_003")) {
                PostReportEvent event = new PostReportEvent(
                        post.getPostId()
                        , report.getReportId()
                        , report.getUserId()
                        , post.getUserId()
                        , report.getReason().getCodeName()
                        , getThumbnailUrl(post)
                );

                kafkaProducer.sendPostReportEvent(event);
            }

            report.setIsDone(true);
            report.setActionDtm(new Timestamp(System.currentTimeMillis()));

            List<ReportAction> actions = actionCodes.stream().map(action -> new ReportAction(report, action)).toList();
            report.setActions(actions);

            response.setResponse(HttpStatus.OK.value(), "Success");

        } catch (InvalidDataException e) {
            log.warn("Exception during post reporting action", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during post reporting action", e);
            throw new Exception("게시물 신고 조치에 실패하였습니다.");
        }

        return response;
    }

    @Transactional(readOnly = true)
    public ResponseObject<List<PostBatchSearchResponse>> searchPostBatch(PostBatchSearchRequest dto) throws Exception {

        ResponseObject<List<PostBatchSearchResponse>> response = new ResponseObject<>();
        try {
            QPost qPost = QPost.post;

            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qPost.postId.in(dto.getPostIds()));
            List<Post> postList = postRepository.searchPostBatch(builder);

            if(postList.isEmpty()) {
                throw new InvalidDataException(HttpStatus.NOT_FOUND, "게시물이 없습니다.");
            }
            else {
                List<PostBatchSearchResponse> dataList = new ArrayList<>();
                for(Post post: postList) {
                    PostBatchSearchResponse data = PostBatchSearchResponse.toDto(post);
                    data.setThumbnailUrl(getThumbnailUrl(post));
                    data.setMediaType(getMediaType(post.getFiles()));
                    dataList.add(data);
                }
                response.setResponse(HttpStatus.OK.value(), "Success", dataList);
            }
        } catch (InvalidDataException e) {
            log.warn("Exception during post batch search", e);
            throw e;
        } catch (Exception e) {
            log.warn("Exception during post batch search", e);
            throw new Exception("게시물 일괄 조회에 실패하였습니다");
        }

        return response;
    }

    private String getMediaType(List<PostFile> fileList) {
        if (!fileList.isEmpty()) {
            return FileUtil.isVideo(fileList.get(0).getFileName()) ? "동영상" : "사진";
        } else
            return null;
    }

    public String getFileUrl(PostFile file) {
        return FILE_BASE_URL+ "/files/post/"+ file.getFileName();
    }

    public String getThumbnailUrl(Post post) {
        return Optional.ofNullable(post.getThumbnailName())
                .map(thumbnailName -> FILE_BASE_URL + "/files/post/" + thumbnailName)
                .orElse(null);
    }

    public void deletePost(Post post) {
        post.setIsDel(true);
        post.setDelDtm(new Timestamp(System.currentTimeMillis()));
        rollbackFile(post.getFiles());
    }

    @Transactional(rollbackFor = {Exception.class})
    public void deletePostByUserId(Long userId) {
        List<Post> postList = postRepository.findByUserIdAndIsDel(userId, false);
        postList.forEach(post -> {
            if(!post.getIsDel()) {
                deletePost(post);
            }
        });
    }
}
