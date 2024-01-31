package com.oha.posting.controller;

import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.dto.request.PostInsertRequest;
import com.oha.posting.dto.request.PostLikeRequest;
import com.oha.posting.dto.request.PostReportRequest;
import com.oha.posting.dto.request.PostUpdateRequest;
import com.oha.posting.dto.response.PostInsertResponse;
import com.oha.posting.dto.response.PostSearchResponse;
import com.oha.posting.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Tag(name="업로드", description = "업로드 API")
@RequestMapping("/api/posting")
@RestController
public class PostController {

    private final PostService postService;

    @GetMapping("/post/{postId}")
    @Operation(summary = "게시글 단건 조회", description = """
                                                        **statusCode:**
                                                        - 200: 성공
                                                        - 400: 데이터 오류
                                                        - 404: 게시글 없음
                                                        - 500: 서버 오류
                                                        """)
    public ResponseObject<PostSearchResponse> getPost(@Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
                                                    , @Parameter(description = "게시글 ID", required = true, example = "1") @PathVariable Long postId) {
        return postService.getPost(token, postId);
    }

    @GetMapping("/posts")
    @Operation(summary = "게시글 리스트 조회", description = """
                                                          **statusCode:**
                                                          - 200: 성공
                                                          - 400: 데이터 오류
                                                          - 404: 게시글 없음
                                                          - 500: 서버 오류\n
                                                          **`offset: 결과 집합 시작 위치(0부터 시작), 기본값 0`**\n
                                                          **`size: 반환할 행 최대 수, 기본값 10(최대 100)`**
                                                          """)
    public ResponseObject<List<PostSearchResponse>> getPostList(@Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
                                                              , @RequestParam(name = "regionCode") Long regionCode
                                                              , @RequestParam(name = "popular", required = false) Boolean popular
                                                              , @RequestParam(name = "categoryCode", required = false) String categoryCode
                                                              , @RequestParam(name = "offset", defaultValue = "0") Integer offset
                                                              , @RequestParam(name = "size", defaultValue = "10") @Max(100) Integer size) {
        return postService.getPostList(token, regionCode, popular, categoryCode, offset, size);
    }

    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 작성", description = """ 
                                                    **Status Code:**
                                                    - `201`: 성공
                                                    - 400: 데이터 오류
                                                    - 404: 게시글 없음
                                                    - 500: 서버 오류\n
                                                    **필수 값:**
                                                    - category
                                                    - regionCode\n
                                                    **현재 파일은 1개만 가능하지만 변경을 고려하여 리스트로 저장/조회하도록 하였습니다.**
                                                    """)
    public ResponseObject<PostInsertResponse> insertPost(@RequestPart(name = "dto") @Validated PostInsertRequest dto
                                                       , @RequestPart(name = "files") @Size(max = 1, message = "파일은 1개까지 가능합니다.") List<MultipartFile> files
                                                       , @Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
                                                       , @Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId) {
        return postService.insertPost(dto, files, token, userId);
    }

    @PatchMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 수정", description = """
                                                    **statusCode:**
                                                    - 200: 성공
                                                    - 400: 데이터 오류
                                                    - 403: 권한
                                                    - 404: 게시글 없음
                                                    - 500: 서버 오류\n
                                                    **`updateItem에 해당되는 속성만 업데이트 됩니다. ","로 구분해서 정확히 입력해주세요. (파일 속성명 files)`**\n
                                                    **`keywords 수정하는 경우 keywords가 없거나 빈 리스트면 삭제만 됩니다.`**\n
                                                    """)
    public ResponseObject<?> updatePost(@RequestPart(name = "dto") @Validated PostUpdateRequest dto
                                      , @RequestPart(name = "files", required = false) @Size(max = 1, message = "파일은 1개까지 가능합니다.") List<MultipartFile> files
                                      , @Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
                                      , @Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId) {
        return postService.updatePost(dto, files, token, userId);
    }

    @DeleteMapping("/post/{postId}")
    @Operation(summary = "게시글 삭제", description = """
                                                    **statusCode:**
                                                    - 200: 성공
                                                    - 400: 데이터 오류
                                                    - 403: 권한
                                                    - 404: 게시글 없음
                                                    - 500: 서버 오류
                                                    """)
    public ResponseObject<?> deletePost(@Parameter(description = "게시글 ID", required = true, example = "1") @PathVariable Long postId
                                      , @Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId) {
        return postService.deletePost(postId, userId);
    }

    @PostMapping("/post/like")
    @Operation(summary = "게시글 좋아요", description = """
                                                **statusCode:**
                                                - 200: 성공(좋아요 취소)
                                                - `201`: 성공(좋아요)
                                                - 400: 데이터 오류
                                                - 404: 게시글 없음
                                                - 500: 서버 오류\n
                                                **`type: L(Like) 또는 U(Unlike) 중 한 글자만 입력해주세요`**
                                                """)
    public ResponseObject<?> like(@Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId
                                , @RequestBody @Validated PostLikeRequest dto) {
        return postService.like(userId, dto);
    }

    @PostMapping("/post/report")
    @Operation(summary = "게시글 신고하기", description = """
                                                **statusCode:**
                                                - `201`: 성공
                                                - 400: 데이터 오류
                                                - 404: 게시글 없음
                                                - 500: 서버 오류
                                                """)
    public ResponseObject<?> reportPost(@Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId
                                      , @RequestBody @Validated PostReportRequest dto) {
        return postService.reportPost(userId, dto);
    }

}