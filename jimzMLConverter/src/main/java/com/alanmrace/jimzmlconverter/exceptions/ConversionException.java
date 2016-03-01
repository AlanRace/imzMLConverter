package com.alanmrace.jimzmlconverter.exceptions;


public class ConversionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

        public ConversionException(String message) {
            super(message);
        }
        
	public ConversionException(String message, Exception exception) {
		super(message, exception);
	}

}
