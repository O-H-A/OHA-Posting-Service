package com.oha.posting.config.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseObject<T> {

    @Schema(description = "상태코드", example = "200")
    private int statusCode;
    @Schema(description = "결과메시지", example = "성공")
    private String message;
    @Schema(description = "데이터")
    private T data;

    public ResponseObject(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public void setResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public void setResponse(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }
}
