package com.oha.posting.config.exception;

import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.config.response.StatusCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseObject<?> handleValidationException(MethodArgumentNotValidException ex, HttpServletResponse httpServletResponse) {
        log.warn("Bad Request", ex);
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errorMessages = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        String message = "Validation failed: " + String.join(", ", errorMessages);
        httpServletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return new ResponseObject<>(StatusCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseObject<?> handleInvalidDataException(InvalidDataException e, HttpServletResponse httpServletResponse) {
        httpServletResponse.setStatus(e.getHttpStatus().value());
        return new ResponseObject<>(e.getHttpStatus().value(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseObject<?> handleException(Exception e, HttpServletResponse httpServletResponse) {
        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseObject<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }
}

