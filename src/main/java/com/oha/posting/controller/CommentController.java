package com.oha.posting.controller;

import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.dto.comment.*;
import com.oha.posting.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Tag(name="댓글", description = "댓글 API")
@RequestMapping("/api/posting")
@RestController
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/comments")
    @Operation(summary = "게시물 댓글 리스트 조회", description = """
                                                          **statusCode:**
                                                          - 200: 성공
                                                          - 400: 데이터 오류
                                                          - 404: 댓글 없음
                                                          - 500: 서버 오류\n
                                                          **`offset: 결과 집합 시작 위치(0부터 시작), 기본값 0`**\n
                                                          **`size: 반환할 행 최대 수, 기본값 10(최대 100)`**
                                                          """)
    public ResponseObject<List<CommentSearchResponse>> getCommentList(@Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
            , @RequestParam(name = "postId", required = false) Long postId
            , @RequestParam(name = "parentId", required = false) Long parentId
            , @RequestParam(name = "offset", defaultValue = "0") Integer offset
            , @RequestParam(name = "size", defaultValue = "10") @Max(100) Integer size) throws Exception {
        return commentService.getCommentList(token, postId, parentId, offset, size);
    }


    @PostMapping(value = "/post/comment")
    @Operation(summary = "게시물 댓글 작성", description = """ 
                                                    **Status Code:**
                                                    - `201`: 성공
                                                    - 400: 데이터 오류
                                                    - 500: 서버 오류
                                                    """)
    public ResponseObject<CommentInsertResponse> insertComment(@RequestBody @Validated CommentInsertRequest dto
                                                          , @Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
                                                          , @Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId
                                                          , HttpServletResponse httpServletResponse) throws Exception {
        return commentService.insertComment(dto, token, userId, httpServletResponse);
    }

    @PutMapping(value = "/post/comment")
    @Operation(summary = "게시물 댓글 수정", description = """ 
                                                    **Status Code:**
                                                    - 200: 성공
                                                    - 400: 데이터 오류
                                                    - 403: 권한 오류
                                                    - 500: 서버 오류
                                                    """)
    public ResponseObject<CommentUpdateResponse> updateComment(@RequestBody @Validated CommentUpdateRequest dto
                                                             , @Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
                                                             , @Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId) throws Exception {
        return commentService.updateComment(dto, token, userId);
    }

    @DeleteMapping(value = "/post/comment/{commentId}")
    @Operation(summary = "게시물 댓글 삭제", description = """ 
                                                    **Status Code:**
                                                    - 200: 성공
                                                    - 400: 데이터 오류
                                                    - 403: 권한 오류
                                                    - 500: 서버 오류
                                                    """)
    public ResponseObject<?> deleteComment(@PathVariable(value = "commentId") Long commentId
                                         , @Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId) throws Exception {
        return commentService.deleteComment(commentId, userId);
    }
}
