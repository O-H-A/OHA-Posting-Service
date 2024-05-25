package com.oha.posting.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostReportRequest {

    @Schema(description = "게시물 ID", example = "1")
    @NotNull(message = "게시물 ID는 필수 항목입니다.")
    private Long postId;

    @Schema(description = "신고 사유 코드", example = "REP_RSN_001")
    @NotBlank(message = "신고 사유 코드를 입력해주세요.")
    private String reasonCode;
}
