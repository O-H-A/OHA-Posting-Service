package com.oha.posting.controller;

import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.dto.post.PostReportIngActionRequest;
import com.oha.posting.dto.post.PostReportingSearchResponse;
import com.oha.posting.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Tag(name="업로드 (관리자)", description = "업로드 API (관리자)")
@RequestMapping("/api/posting/admin")
@RestController
public class AdminPostController {

    private final PostService postService;

    @GetMapping("/post/reports")
    @Operation(summary = "게시물 신고 조회", description = """
                                                **statusCode:**
                                                - 200: 성공
                                                - 400: 데이터 오류
                                                - 404: 신고 정보 없음
                                                - 500: 서버 오류
                                                """)
    public ResponseObject<List<PostReportingSearchResponse>> searchPostReporting(@Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
                                                                               , @RequestParam(name = "isDone", required = false) Boolean isDone) throws Exception {
        return postService.searchPostReporting(token, isDone);
    }

    @PatchMapping("/post/report/action")
    @Operation(summary = "게시물 신고 조치", description = """
                                                **statusCode:**
                                                - 201: 성공
                                                - 400: 데이터 오류
                                                - 404: 신고 정보 없음
                                                - 409: 조치 완료된 신고 건
                                                - 500: 서버 오류
                                                """)
    public ResponseObject<?> updatePostReportingAction(@RequestBody PostReportIngActionRequest dto) throws Exception {
        return postService.updatePostReportingAction(dto);
    }

}