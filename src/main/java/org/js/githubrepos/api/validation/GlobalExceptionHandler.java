package org.js.githubrepos.api.validation;

import lombok.extern.apachecommons.CommonsLog;
import org.js.githubrepos.api.model.Error;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@CommonsLog
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceGeneralException.class)
    public ResponseEntity<Error> handleServiceGeneralException(ServiceGeneralException ex, WebRequest request) {
        Error response = Error.of("ERROR", ex.getMessage());
        log.warn("Handled ServiceGeneralException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Error> handleBadRequestException(BadRequestException ex, WebRequest request) {
        Error response = Error.of("BAD_REQUEST", ex.getMessage());
        log.warn("Handled BadRequestException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }
}
