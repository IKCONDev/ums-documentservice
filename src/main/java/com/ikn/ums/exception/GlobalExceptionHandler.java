package com.ikn.ums.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private ResponseEntity<ErrorResponse> buildResponse(String message, String code, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(message, code, status);
        return new ResponseEntity<>(errorResponse, status);
    }
    

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
    	log.error("GlobalExceptionHandler.handleGeneralException() ENTERED: " + ex.getMessage());
        return buildResponse(ex.getErrorMessage(), ex.getErrorCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    	log.error("GlobalExceptionHandler.handleGeneralException() ENTERED: " + ex.getMessage());
        return buildResponse("An unexpected error occurred", "GENERAL-EXCEPTION", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(ObjectIsNullException.class)
    public ResponseEntity<ErrorResponse> handleObjectIsNullException(ObjectIsNullException ex) {
    	log.error("GlobalExceptionHandler.handleObjectIsNullException() ENTERED: " + ex.getMessage());
        return buildResponse(ex.getErrorMessage(), ex.getErrorCode(), HttpStatus.BAD_REQUEST); // Changed to BAD_REQUEST
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    	log.error("GlobalExceptionHandler.handleBusinessException() ENTERED: " + ex.getMessage());
        return buildResponse(ex.getErrorMessage(), ex.getErrorCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
	@ExceptionHandler(EmptyInputException.class)
	public ResponseEntity<ErrorResponse> handleEmptyInput(EmptyInputException ex) {
		log.error("GlobalExceptionHandler.handleEmptyInput() ENTERED" + ex.getMessage());
		return buildResponse(ex.getErrorMessage(), ex.getErrorCode(), HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handling the Controller Exceptions global to reduce boiler plate code
	 * @param noSuchElementException
	 * @return
	 */
	@ExceptionHandler(ControllerException.class)
	public ResponseEntity<ErrorResponse> handleControllerException(ControllerException ex) {
		log.error("GlobalExceptionHandler.handleControllerException() ENTERED: " + ex.getMessage());
		return buildResponse(ex.getErrorMessage(), ex.getErrorCode(),  HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
	    log.error("GlobalExceptionHandler.handleBadRequestException() ENTERED: {}", ex.getMessage());
	    return buildResponse(ex.getErrorMessage(), ex.getErrorCode(), HttpStatus.BAD_REQUEST); // Returning 400 BAD_REQUEST
	}
	
	@ExceptionHandler(EmailSendException.class)
	public ResponseEntity<ErrorResponse> handleEmailSendExceptionn(EmailSendException ex) {
	    log.error("GlobalExceptionHandler.handleEmailSendExceptionn() ENTERED: {}", ex.getMessage());
	    return buildResponse(ex.getErrorMessage(), ex.getErrorCode(), HttpStatus.INTERNAL_SERVER_ERROR); //
	}
	
	@ExceptionHandler(DatabaseException.class)
	public ResponseEntity<ErrorResponse> handleDatabaseException(DatabaseException ex) {
	    log.error("GlobalExceptionHandler.handleDatabaseException() ENTERED: {}", ex.getMessage());
	    return buildResponse(ex.getErrorMessage(), ex.getErrorCode(), HttpStatus.SERVICE_UNAVAILABLE); 
	}

	@ExceptionHandler(INServiceException.class)
	public ResponseEntity<ErrorResponse> handleServiceException(INServiceException ex) {
	    log.error("GlobalExceptionHandler.handleServiceException() ENTERED: {}", ex.getMessage());
	    return buildResponse(ex.getErrorMessage(), ex.getErrorCode(), HttpStatus.SERVICE_UNAVAILABLE); 
	}


}
