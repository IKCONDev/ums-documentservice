package com.ikn.ums.exception;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileProcessingException extends RuntimeException  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String errorCode;
	private IOException errorMessage;

    /**
     * Constructs an ControllerException with a custom error code and message.
     *
     * @param errorCode the error code associated with the exception.
     * @param e the detailed error message.
     */
    public FileProcessingException(String errorCode, IOException e) {
        super(String.format("ErrorCode: %s, ErrorMessage: %s", errorCode, e));
        this.errorCode = errorCode;
        this.errorMessage = e;
    }
    
    /**
     * Default constructor with a generic message.
     */
	public FileProcessingException() {
		 super("An unspecified error occurred in the file processing layer.");
	}
    
}
