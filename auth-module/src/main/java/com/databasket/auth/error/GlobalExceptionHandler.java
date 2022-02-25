package com.databasket.auth.error;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<?> resourceNotFoundHandling(ResourceNotFoundException exception, WebRequest request){
		String message = exception.getMessage();
		if(message == null || message.isEmpty()) {
			message = "Internal server error. Please contact admin...";
		}
		
		log.error("resourceNotFoundHandling > {}", message);
		log.error("resourceNotFoundHandling > {}", exception);
		//exception.printStackTrace();
		
		ErrorDetails errorDetails = ErrorDetails.builder().timestamp(new Date()).message(message).details(null).build();
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> globalExceptionHandling(Exception exception, WebRequest request){

		String message = exception.getMessage();
		if(message == null || message.isEmpty()) {
			message = "Internal server error. Please contact admin...";
		}
		
		log.error("globalExceptionHandling > {}", exception.getMessage());
		log.error("globalExceptionHandling > {}", exception);
		//exception.printStackTrace();
		
		ErrorDetails errorDetails = ErrorDetails.builder().timestamp(new Date()).message(message).details(null).build();
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
