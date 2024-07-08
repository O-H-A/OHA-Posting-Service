package com.oha.posting.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentInsertRequest {

    @Schema(description = "게시물 ID", example = "1")
    private Long postId;

    @Schema(description = "부모 댓글 ID", example = "1")
    private Long parentId;

    @Schema(description = "답글 ID", example = "1")
    private Long replyId;

    @Schema(description = "댓글 내용", example = "댓글 내용")
    @Size(min = 1, max = 300, message = "댓글은 최대 300자까지 입력 가능합니다.")
    @NotBlank
    private String content;

}
