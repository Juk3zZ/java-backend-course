package ru.tinkoff.edu.java.scrapper;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.tinkoff.edu.java.common.exception.IncorrectRequestParamsException;
import ru.tinkoff.edu.java.common.model.ApiErrorResponse;
import ru.tinkoff.edu.java.scrapper.exception.ResourceNotFoundException;

import java.util.Arrays;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleResourceNotFoundException(ResourceNotFoundException exception) {
        return handleInternal("Requested resource not found", exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncorrectRequestParamsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleIncorrectRequestParamsException(IncorrectRequestParamsException exception) {
        return handleInternal("There are incorrect query parameters/body", exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        return handleInternal("There are missing query parameters!", exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMissingRequestHeaderException(MissingServletRequestParameterException exception) {
        return handleInternal("There are missing query parameters", exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleException(Exception exception) {
        return handleInternal("Something went wrong while your request", exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiErrorResponse handleInternal(String message, Exception exception, HttpStatus httpStatus) {
        return new ApiErrorResponse(
                message,
                String.valueOf(httpStatus.value()),
                exception.getClass().getName(),
                exception.getMessage(),
                Arrays.stream(exception.getStackTrace()).map(String::valueOf).toList()
        );
    }
}
