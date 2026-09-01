package com.gym.gym_management_system.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarClienteNoEncontrado(
            ClienteNoEncontradoException exception,
            HttpServletRequest request) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(DniDuplicadoException.class)
    public ResponseEntity<ApiError> manejarDniDuplicado(
            DniDuplicadoException exception,
            HttpServletRequest request) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(PagoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarPagoNoEncontrado(
            PagoNoEncontradoException exception,
            HttpServletRequest request) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(PagoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarPagoDuplicado(
            PagoDuplicadoException exception,
            HttpServletRequest request) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(ClienteInactivoException.class)
    public ResponseEntity<ApiError> manejarClienteInactivo(
            ClienteInactivoException exception,
            HttpServletRequest request) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarCuerpoInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud contiene datos inválidos",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidaciones(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> validaciones = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                validaciones.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos",
                request.getRequestURI(),
                validaciones
        );
    }

    private ResponseEntity<ApiError> crearRespuesta(
            HttpStatus estado,
            String mensaje,
            String ruta,
            Map<String, String> validaciones) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                ruta,
                validaciones
        );
        return ResponseEntity.status(estado).body(error);
    }
}
