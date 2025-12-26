package com.ikn.ums.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadRequestException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String errorCode;
	private String errorMessage;
	
    /**
     * Constructs an BadRequestException with a custom error code and message.
     *
     * @param errorCode the error code associated with the exception.
     * @param errorMessage the detailed error message.
     */
    public BadRequestException(String errorCode, String errorMessage) {
        super(String.format("ErrorCode: %s, ErrorMessage: %s", errorCode, errorMessage));
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Default constructor with a generic message.
     */
	public BadRequestException() {
		 super("An unspecified business exception occurred.");
	}
}
