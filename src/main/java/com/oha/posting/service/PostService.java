package com.oha.posting.service;

import com.oha.posting.config.exception.InvalidDataException;
import com.oha.posting.config.file.FileConfig;
import com.oha.posting.config.file.FileUtil;
import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.config.response.StatusCode;
import com.oha.posting.entity.*;
import com.oha.posting.object.request.PostInsertRequest;
import com.oha.posting.object.request.PostLikeRequest;
import com.oha.posting.object.request.PostUpdateRequest;
import com.oha.posting.object.response.PostInsertResponse;
import com.oha.posting.object.response.PostSearchResponse;
import com.oha.posting.repository.CommonCodeRepository;
import com.oha.posting.repository.KeywordRepository;
import com.oha.posting.repository.LikeRepository;
import com.oha.posting.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;
    private final ExternalApiService externalApiService;
    private final CommonCodeRepository commonCodeRepository;
    private final KeywordRepository keywordRepository;
    private final LikeRepository likeRepository;
    private final FileConfig fileConfig;

    @Value("${file.base-url}")
    private String FILE_BASE_URL;

    @Value("${file.save-path}")
    private String SAVE_PATH;

    @Transactional(readOnly = true)
    public ResponseObject<PostSearchResponse> getPost(String token, Long postId) {
        ResponseObject<PostSearchResponse> response = new ResponseObject<>();

        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new InvalidDataException(StatusCode.NOT_FOUND, "게시글이 없습니다."));

            PostSearchResponse data = PostSearchResponse.toDto(post);
            List<String> urls = new ArrayList<>();
            for(PostFile file : post.getFiles()) {
                urls.add(FILE_BASE_URL+file.getUrl());
            }
            data.setUrls(urls);

            // 행정구역코드로 위치 조회 API 호출
            Map<String, Object> params = new HashMap<>();
            params.put("hcode", post.getHcode());
            // Map<String, Object> responseBody = externalApiService.get(token, "/api/common/weather", params);
            // data >> 위치 정보 set
            response.setResponse(StatusCode.OK, "Success", data);
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post search", e);
            response.setResponse(e.getStatusCode(), e.getMessage());
        }
        catch (Exception e) {
            log.warn("Exception during post search", e);
            response.setResponse(StatusCode.SERVER_ERROR, "조회에 실패하였습니다");
        }

        return response;
    }

    @Transactional(readOnly = true)
    public ResponseObject<List<PostSearchResponse>> getPostList(Boolean likeOrder, Long hcode) {
        ResponseObject<List<PostSearchResponse>> response = new ResponseObject<>();

        return response;
    }


    @Transactional
    public ResponseObject<PostInsertResponse> insertPost(PostInsertRequest dto, List<MultipartFile> files, String token, Long userId) {
        ResponseObject<PostInsertResponse> response = new ResponseObject<>();
        PostInsertResponse data = new PostInsertResponse();
        Post post = Post.toEntity(dto);
        List<PostFile> fileList = new ArrayList<>();

        try {
            // db 유저 확인 (user 서비스)
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
//            Map<String, Object> responseBody = externalApiService.get(token, "/api/user", params);
//            if (!"200".equals((String)responseBody.get("statusCode"))) {
//                  throw new DatabaseException(StatusCode.BAD_REQUEST,"사용자가 존재하지 않습니다.");
//            }
            post.setUserId(userId);

            // db 행정동 확인 (common 서비스)
            params = new HashMap<>();
            params.put("hcode", post.getHcode());
//            responseBody = externalApiService.get(token, "/api/common/weather", params);
//            if (!"200".equals((String)responseBody.get("statusCode"))) {
//                  throw new DatabaseException(StatusCode.BAD_REQUEST, "위치가 존재하지 않습니다.");
//            }

            // category 조회
            CommonCode commonCode = commonCodeRepository.findByCode(dto.getCategoryCode())
                    .orElseThrow(() ->  new InvalidDataException(StatusCode.BAD_REQUEST, "카테고리가 없습니다."));

            post.setCategory(commonCode);

            // keyword 생성
            if (dto.getKeywords() != null) {
                List<Keyword> keywords = (dto.getKeywords()).stream().map(keywordName -> new Keyword(post, keywordName)).toList();
                post.setKeywords(keywords);
            }

            /* file */
            for(MultipartFile file : files) {
                if(!fileConfig.isAllowedFile(file)) {
                    throw new InvalidDataException(StatusCode.BAD_REQUEST, "지원되지 않는 확장자입니다.");
                }

                String extension = fileConfig.getFileExtension(file.getOriginalFilename());
                String url = "/images/post/";

                // 파일 db 저장
                String uuidFileName = UUID.randomUUID() + "." +extension;
                PostFile postFile = new PostFile(post, SAVE_PATH + "post/" + uuidFileName, url+uuidFileName);
                fileList.add(postFile);

                // 파일 저장
                file.transferTo(new File(SAVE_PATH + "post/" + uuidFileName));
            }
            post.setFiles(fileList);

            Post savedPost = postRepository.save(post);
            data.setPostId(savedPost.getPostId());
            response.setResponse(StatusCode.CREATED, "Success", data);
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post create", e);
            rollbackFile(fileList);
            response.setResponse(e.getStatusCode(), e.getMessage());
        }
        catch (Exception e) {
            log.warn("Exception during post create", e);
            rollbackFile(fileList);
            response.setResponse(StatusCode.SERVER_ERROR, "저장에 실패하였습니다");
        }

        return response;
    }

    @Async
    public void rollbackFile(List<PostFile> fileList) {
        for (PostFile file : fileList) {
            try {
                FileUtil.deleteFile(file.getSavePath());
            } catch (IOException e) {}
        }
    }

    @Transactional
    public ResponseObject<?> updatePost(PostUpdateRequest dto, List<MultipartFile> files, String token, Long userId) {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            Post post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new InvalidDataException(StatusCode.NOT_FOUND, "게시글이 없습니다."));

            if (!userId.equals(post.getUserId())) {
                throw new InvalidDataException(StatusCode.FORBIDDEN, "권한이 없습니다.");
            }

            for(String item : dto.getUpdateItem().split(",")) {
                switch (item) {
                    case "content":
                        post.setContent(dto.getContent());
                        break;
                    case "categoryCode":
                        CommonCode commonCode = commonCodeRepository.findByCode(dto.getCategoryCode())
                                .orElseThrow(() ->  new InvalidDataException(StatusCode.BAD_REQUEST, "카테고리가 없습니다."));

                        post.setCategory(commonCode);
                        break;
                    case "keywords":
                        post.getKeywords().clear();
                        if(dto.getKeywords() != null) {
                            (dto.getKeywords()).forEach(keywordName -> post.getKeywords().add(new Keyword(post, keywordName)));
                        }
                        break;
                    case "hcode":
                        // Map<String, Object> params = new HashMap<>();
                        // params.put("hcode", post.getHcode());
            //            responseBody = externalApiService.get(token, "/api/common/weather", params);
            //            if (!"200".equals((String)responseBody.get("statusCode"))) {
                        // throw new DatabaseUpdateException(StatusCode.BAD_REQUEST,"위치가 존재하지 않습니다.");
            //            }
                        // post.setHcode( Long.parseLong((String)responseBody.get("hcode")));
                        break;
                    case "locationDetail":
                        post.setLocationDetail(dto.getLocationDetail());
                        break;
                    default:
                        throw new InvalidDataException(StatusCode.BAD_REQUEST, "일치하는 수정 항목이 없습니다.");
                }
            }

            response.setResponse(StatusCode.OK, "Success");
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post update", e);
            response.setResponse(e.getStatusCode(), e.getMessage());
        }
        catch (Exception e) {
            log.warn("Exception during post update", e);
            response.setResponse(StatusCode.SERVER_ERROR, "저장에 실패하였습니다");
        }

        return response;
    }

    @Transactional
    public ResponseObject<?> deletePost(Long postId, Long userId) {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new InvalidDataException(StatusCode.NOT_FOUND, "게시글이 없습니다."));

            if (!userId.equals(post.getUserId())) {
                throw new InvalidDataException(StatusCode.FORBIDDEN, "권한이 없습니다.");
            }

            postRepository.delete(post);
            rollbackFile(post.getFiles());

            response.setResponse(StatusCode.OK, "Success");
        }
        catch (InvalidDataException e) {
            log.warn("Exception during post delete", e);
            response.setResponse(e.getStatusCode(), e.getMessage());
        }
        catch (Exception e) {
            log.warn("Exception during post delete", e);
            response.setResponse(StatusCode.SERVER_ERROR, "삭제에 실패하였습니다");
        }

        return response;
    }

    @Transactional
    public ResponseObject<?> like(Long userId, PostLikeRequest dto) {
        ResponseObject<?> response = new ResponseObject<>();

        try {
            Post post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new InvalidDataException(StatusCode.NOT_FOUND, "게시글이 없습니다."));

            LikeId likeId = new LikeId(post.getPostId(), userId);
            Optional<Like> existingLike = likeRepository.findById(likeId);

            if ("L".equals(dto.getType())) {
                if (existingLike.isPresent()) {
                    throw new InvalidDataException(StatusCode.BAD_REQUEST, "이미 좋아요 상태입니다.");
                }
                // 좋아요 등록
                post.getLikes().add(new Like(new LikeId(dto.getPostId(), userId), post));
                response.setResponse(StatusCode.CREATED, "Success");
            } else {
                if (existingLike.isEmpty()) {
                    throw new InvalidDataException(StatusCode.BAD_REQUEST, "좋아요 상태가 아닙니다.");
                }
                // 좋아요 삭제
                likeRepository.delete(existingLike.get());
                response.setResponse(StatusCode.OK, "Success");
            }
        } catch (InvalidDataException e) {
            log.warn("Exception during post like", e);
            response.setResponse(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.warn("Exception during post like", e);
            response.setResponse(StatusCode.SERVER_ERROR, "좋아요에 실패하였습니다");
        }

        return response;
    }
}
