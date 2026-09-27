package com.neeraj.restapis.order_management_system_project.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
		ErrorResponse response = new ErrorResponse(LocalDateTime.now(), status.value(), message,
				request.getRequestURI());
		return ResponseEntity.status(status).body(response);
	}

	// Handles not-found exceptions and returns HTTP 404.
	@ExceptionHandler({ UserNotFoundException.class, OrderNotFoundException.class, ProductNotFoundException.class,
			PaymentNotFoundException.class })
	public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	// Handles duplicate resource errors and returns HTTP 409.
	@ExceptionHandler({ EmailAlreadyExistsException.class, ProductAlreadyExistsException.class })
	public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
	}

	// Handles handle Optimistic Locking errors and returns HTTP 409.
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ErrorResponse> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex,
			HttpServletRequest request) {
		return buildResponse(HttpStatus.CONFLICT,
				"The resource was modified by another concurrent request. Please retry.", request);
	}

	// Handles Forbidden Access errors and returns HTTP 403.
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to access or modify this resource.",
				request);
	}

	// Handles DTO validation errors and returns field-level validation messages.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		Map<String, String> validationErrors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

		ErrorResponse response = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				"Validation failed for one or more fields", request.getRequestURI(), validationErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// Handles invalid password errors and returns HTTP 400.
	@ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class, InvalidPasswordException.class,
			PaymentVerificationException.class })
	public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
	}

	// Handles malformed JSON and invalid request body errors with HTTP 400.
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex,
			HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request body or invalid field value.", request);
	}

	// Handles unexpected exceptions and returns HTTP 500.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.",
				request);
	}

}