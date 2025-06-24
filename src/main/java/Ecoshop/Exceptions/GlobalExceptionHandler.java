package Ecoshop.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleProductExists(ProductAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(ProductDoesNotExistsException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductDoesNotExistsException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }
}
