package com.oha.posting.config.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseObject<T> {

    @Schema(description = "상태코드", example = "200")
    private int statusCode;
    @Schema(description = "결과메시지", example = "성공")
    private String message;
    @Schema(description = "데이터")
    private T data;
}
