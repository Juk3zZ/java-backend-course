package ru.tinkoff.edu.java.bot;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.tinkoff.edu.java.common.exception.IncorrectRequestParamsException;
import ru.tinkoff.edu.java.common.model.ApiErrorResponse;

import java.util.Arrays;

@RestControllerAdvice
public class WebErrorsHandler {

    @ExceptionHandler(IncorrectRequestParamsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleIncorrectRequest(IncorrectRequestParamsException exception) {
        return new ApiErrorResponse(
                "There are incorrect parameters in your request!",
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                exception.getClass().getName(),
                exception.getMessage(),
                Arrays.stream(exception.getStackTrace()).map(String::valueOf).toList()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleException(Exception exception) {
        return new ApiErrorResponse(
                "Something went wrong while your request",
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                Exception.class.getName(),
                exception.getMessage(),
                Arrays.stream(exception.getStackTrace()).map(String::valueOf).toList()
        );
    }
}
