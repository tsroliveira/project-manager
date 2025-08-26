package com.portfolio.web.error;

import com.portfolio.exception.BusinessException;
import com.portfolio.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice // GLOBAL: sem basePackages
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // 400 - Bean Validation no corpo (@RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        var violations = new ArrayList<ErrorResponse.Violation>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
            violations.add(new ErrorResponse.Violation(err.getField(), err.getDefaultMessage()))
        );
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Dados inválidos. Corrija os campos indicados.",
                request.getRequestURI(), violations);
    }

    // 400 - Validação/binding em query/path/form
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex, HttpServletRequest request) {
        var violations = new ArrayList<ErrorResponse.Violation>();
        ex.getFieldErrors().forEach(err ->
            violations.add(new ErrorResponse.Violation(err.getField(), err.getDefaultMessage()))
        );
        return build(HttpStatus.BAD_REQUEST, "BIND_ERROR",
                "Parâmetros inválidos.", request.getRequestURI(), violations);
    }

    // 400 - Validação em @RequestParam/@PathVariable com @Validated
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        var violations = ex.getConstraintViolations().stream()
                .map(v -> new ErrorResponse.Violation(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION",
                "Parâmetros inválidos.", request.getRequestURI(), violations);
    }

    // 400 - Tipos/enum inválidos em parâmetros
    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, TypeMismatchException.class, ConversionFailedException.class })
    public ResponseEntity<ErrorResponse> handleTypeMismatch(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH",
                "Parâmetro inválido na requisição.", request.getRequestURI(), List.of());
    }

    // 400 - Parâmetro obrigatório ausente
    @ExceptionHandler({ MissingServletRequestParameterException.class, MissingPathVariableException.class })
    public ResponseEntity<ErrorResponse> handleMissingParam(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                "Parâmetro obrigatório ausente.", request.getRequestURI(), List.of());
    }

    // 400 - JSON malformado / tipo errado no body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON",
                "Corpo da requisição inválido ou malformado.", request.getRequestURI(), List.of());
    }

    // 405 - Método HTTP não suportado
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        String allowed = ex.getSupportedMethods() == null ? "" : String.join(", ", ex.getSupportedMethods());
        String msg = allowed.isBlank()
                ? "Método HTTP não permitido para este endpoint."
                : "Método HTTP não permitido. Permitidos: " + allowed + ".";
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                msg, request.getRequestURI(), List.of());
    }

    // 404 - Rotas inexistentes (Boot 3+: NoResourceFoundException; compat: NoHandlerFoundException)
    @ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
    public ResponseEntity<ErrorResponse> handleNoResource(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "Recurso não encontrado.", request.getRequestURI(), List.of());
    }

    // 404 - Domínio
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request.getRequestURI(), List.of());
    }

    // 422 - Regra de negócio
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION",
                ex.getMessage(), request.getRequestURI(), List.of());
    }

    // 409 - Integridade de dados
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY",
                "Operação conflitou com restrições do banco de dados.", request.getRequestURI(), List.of());
    }

    // Erros com ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String reason = (ex.getReason() != null && !ex.getReason().isBlank())
                ? ex.getReason() : status.getReasonPhrase();
        return build(status, "SPRING_ERROR", reason, request.getRequestURI(), List.of());
    }

    // Outros erros mapeados pelo Spring
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleSpringErrorResponse(ErrorResponseException ex, HttpServletRequest request) {
        var status = ex.getStatusCode();
        return build(HttpStatus.valueOf(status.value()), "SPRING_ERROR",
                ex.getMessage(), request.getRequestURI(), List.of());
    }

    // 500 - Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Erro não tratado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR",
                "Ocorreu um erro interno. Tente novamente mais tarde.",
                request.getRequestURI(), List.of());
    }

    // helper
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                String path, List<ErrorResponse.Violation> violations) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(
                        OffsetDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        code,
                        message,
                        path,
                        violations
                )
        );
    }
}
