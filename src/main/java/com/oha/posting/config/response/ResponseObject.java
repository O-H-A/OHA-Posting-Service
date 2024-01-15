package com.oha.posting.config.response;

import io.swagger.v3.oas.annotations.media.Schema;

public class ResponseObject<T> {

    @Schema(description = "상태코드")
    private int statusCode;
    @Schema(description = "결과메시지")
    private String message;
    @Schema(description = "데이터")
    private T data;
}
