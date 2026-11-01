package com.dev.payment.config;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.PessimisticLockException;
import org.hibernate.exception.LockTimeoutException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dev.payment.exception.AuthenticationException;
import com.dev.payment.exception.ResourceNotFoundException;
import com.dev.payment.model.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(
                    false,
                    HttpStatus.NOT_FOUND.value(),
                    ex.getMessage(),
                    null
            ));
    }
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidArguments(MethodArgumentNotValidException ex) {
    	Map<String, String> errorMap = new HashMap<>();
    	ex.getBindingResult().getFieldErrors()
    		.forEach(error -> errorMap.put(error.getField(), error.getDefaultMessage()));
    	return errorMap;
    }

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
		
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(new ApiResponse<>(
						false,
						HttpStatus.UNAUTHORIZED.value(),
						ex.getMessage(),
						null
						));
	}
	
	@ExceptionHandler({
	    PessimisticLockException.class,
	    LockTimeoutException.class,
	    CannotAcquireLockException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleLockException(Exception ex) {

	    return ResponseEntity
	            .status(HttpStatus.CONFLICT)
	            .body(new ApiResponse<>(
	                    false,
	                    HttpStatus.CONFLICT.value(),
	                    "Unable to process request due to concurrent operation. Please retry.",
	                    null
	            ));
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {

		log.error(ex.getMessage(), ex);
	    return ResponseEntity
	        .status(HttpStatus.INTERNAL_SERVER_ERROR)
	        .body(new ApiResponse<>(
	            false,
	            HttpStatus.INTERNAL_SERVER_ERROR.value(),
	            "An unexpected error occurred",
	            null
	        ));
	}

}
