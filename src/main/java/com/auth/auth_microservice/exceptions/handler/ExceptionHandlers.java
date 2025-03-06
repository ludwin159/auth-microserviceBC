package com.auth.auth_microservice.exceptions.handler;

import com.auth.auth_microservice.exceptions.ClientNotFound;
import com.auth.auth_microservice.exceptions.InvalidCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler({
            RuntimeException.class,
            ClientNotFound.class,
            InvalidCredentials.class})
    public Mono<ResponseEntity<Map<String, String>>> handleExceptions(RuntimeException exception) {
        HttpStatus status = getStatus(exception);
        return Mono.just(ResponseEntity.status(status).body(
                Map.of("message", exception.getMessage())
        ));
    }

    public HttpStatus getStatus(RuntimeException exception) {
        if (exception instanceof ClientNotFound) {
            return HttpStatus.NOT_FOUND;
        } else if (exception instanceof InvalidCredentials) {
            return HttpStatus.UNAUTHORIZED;
        } else {
            log.error("Problem in internal Server: " + exception.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<Map<String, String>> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errorsResponse = new HashMap<>();
        log.error("Problem in validation data: " + exception.getMessage());
        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errorsResponse.put(error.getField(), error.getDefaultMessage()));
        return Mono.just(errorsResponse);
    }
}
