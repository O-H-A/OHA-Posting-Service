package com.oha.posting.object.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PostInsertRequest {

    @Schema(description = "내용", example = "무지개 떴다")
    @Size(max = 300, message = "내용은 최대 300자까지 입력 가능합니다.")
    private String content;

    @Schema(description = "카테고리 코드", example = "CTGR_RAINBOW")
    @NotBlank(message = "카테고리를 선택해주세요.")
    private String categoryCode;

    @Schema(description = "키워드", example = "[\"무지개\", \"구름\", \"가을 하늘\"]")
    @Size(max = 3, message = "키워드는 최대 3개까지 선택 가능합니다.")
    private List<String> keywords;

    @Schema(description = "행정구역코드", example = "1111065000")
    @NotNull(message = "위치를 선택해주세요.")
    private Long hcode;

    @Schema(description = "위치 추가 정보", example = "영종도 다리")
    @Size(max = 50, message = "위치 상세 정보는 50자 이하로 입력해주세요")
    private String locationDetail;

}