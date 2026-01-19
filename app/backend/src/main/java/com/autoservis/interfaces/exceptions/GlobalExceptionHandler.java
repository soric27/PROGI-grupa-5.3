package com.autoservis.interfaces.exceptions;

import com.autoservis.interfaces.http.vozilo.VoziloController.Unauthorized;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Unauthorized.class)
  public ResponseEntity<?> handleUnauthorized(Unauthorized ex){
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ex.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<?> handleDenied(AccessDeniedException ex){
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Nemate dopuštenje za ovu akciju."));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex){
    return ResponseEntity.badRequest().body(Map.of("message","Neispravan unos", "details", ex.getBindingResult().toString()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegal(IllegalArgumentException ex){
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleOther(Exception ex){
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Greška na serveru."));
  }
}
