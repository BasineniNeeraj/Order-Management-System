package com.neeraj.restapis.order_management_system_project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code=HttpStatus.CONFLICT)
public class ProductAlreadyExistsException extends RuntimeException {
	
	public ProductAlreadyExistsException(String message) {
		super(message);
	}

}
