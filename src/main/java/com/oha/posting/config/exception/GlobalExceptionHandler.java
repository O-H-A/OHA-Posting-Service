package com.oha.posting.config.exception;

import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.config.response.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseObject<?> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Bad Request", ex);
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errorMessages = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        String message = "Validation failed: " + String.join(", ", errorMessages);
        return new ResponseObject<>(StatusCode.BAD_REQUEST, message);
    }
}
