package com.discussion.forum.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ApiErrorException {
    private long timestamp = new Date().getTime();
    private int status;
    private String message;
    private String url;
    private Map<String, String> validationErrors;

    public ApiErrorException(int status, String message, String url) {
        this.status = status;
        this.message = message;
        this.url = url;
    }
}
