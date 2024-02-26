package com.oha.posting.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentInsertRequest {

    @Schema(description = "게시물 ID", example = "1")
    @NotNull
    private Long postId;

    @Schema(description = "부모 댓글 ID", example = "1")
    private Long parentId;

    @Schema(description = "댓글 내용", example = "댓글 내용")
    @Size(max = 300, message = "댓글은 최대 300자까지 입력 가능합니다.")
    @NotBlank
    private String content;

    @Schema(description = "태그 유저 ID", example = "17")
    private Long taggedUserId;

}
