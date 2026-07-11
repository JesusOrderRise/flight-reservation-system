package com.frsystem.globalexceptionhandler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        String customMessage = "";

        if (message.contains("airport_iata_code_key")) {
            customMessage = "There is an already existing airport with the same IATA code.";
        } else if (message.contains("airplane_tail_number_key")) {
            customMessage = "There is an already existing airplane with the same tail number.";
        }

        return new ResponseEntity<>(customMessage, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .getFirst()
                .getDefaultMessage();

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);

    }
}