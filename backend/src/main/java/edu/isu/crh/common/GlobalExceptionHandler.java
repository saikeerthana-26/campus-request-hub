package edu.isu.crh.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(e -> e.getField() + " " + e.getDefaultMessage())
        .orElse("Validation error");
    return new ApiError(Instant.now(), 400, "Bad Request", msg, req.getRequestURI());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
    return new ApiError(Instant.now(), 400, "Bad Request", ex.getMessage(), req.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiError handleAny(Exception ex, HttpServletRequest req) {
    return new ApiError(Instant.now(), 500, "Internal Server Error", ex.getMessage(), req.getRequestURI());
  }
}
