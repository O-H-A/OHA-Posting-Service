package com.oha.posting.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PostReportIngActionRequest {

    @Schema(description = "신고 ID", example = "1")
    @NotNull(message = "신고 ID는 필수 항목입니다.")
    private Long reportId;

    @Schema(description = "신고 조치 코드", example = "[\"REP_ACT_001\", \"REP_ACT_003\"]")
    @NotEmpty(message = "신고 조치 코드를 입력해주세요.")
    private List<String> actionCodes;
}
