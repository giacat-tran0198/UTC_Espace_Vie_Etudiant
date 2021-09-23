package com.discussion.forum.handler;

import com.discussion.forum.exception.ApiErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.discussion.forum.utils.Constant.API_1_0_LOGIN;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorException handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ApiErrorException apiError = new ApiErrorException(HttpStatus.BAD_REQUEST.value(), "Validator error", request.getServletPath());
        BindingResult result = exception.getBindingResult();
        Map<String, String> validatioinErrors = new HashMap<>();
        for (FieldError fieldError : result.getFieldErrors()) {
            validatioinErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        apiError.setValidationErrors(validatioinErrors);
        return apiError;
    }

//    @ExceptionHandler({AccessDeniedException.class})
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    ApiErrorException handleAccessDeniedException() {
//        return new ApiErrorException(HttpStatus.UNAUTHORIZED.value(), "Access error", API_1_0_LOGIN);
//    }
}
