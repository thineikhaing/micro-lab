package org.tek.microlab.exception;


public class DMSServiceException extends DomainException {

	private static final long serialVersionUID = -1L;

	private static final String CODE = "00102";
	private static final String MESSAGE = "Unable to complete the DMS transfer";
	
	public DMSServiceException() {
		super(MESSAGE, new ErrorResponse<String>(CODE, MESSAGE, null));
	}

}
