package com.oha.posting.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CommentLikeRequest {

    @Schema(description = "댓글 ID", example = "1")
    @NotNull(message = "댓글 ID는 필수값입니다.")
    private Long commentId;

    @Schema(description = "좋아요 구분", example = "L")
    @NotBlank
    @Pattern(regexp = "[LU]", message = "L(Like) 또는 U(Unlike) 중 한 글자만 입력해주세요")
    private String type;

}
