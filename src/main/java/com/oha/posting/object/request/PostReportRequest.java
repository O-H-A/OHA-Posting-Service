package com.oha.posting.object.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostReportRequest {

    @Schema(description = "게시글 ID", example = "1")
    @NotNull(message = "게시글 ID는 필수 항목입니다.")
    private Long postId;

    @Schema(description = "신고 사유", example = "신고합니다")
    @NotBlank(message = "신고 사유를 입력해주세요.")
    private String content;
}
