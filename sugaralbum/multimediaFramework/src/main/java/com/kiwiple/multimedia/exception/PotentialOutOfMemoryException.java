package com.kiwiple.multimedia.exception;

/**
 * PotentialOutOfMemoryException.
 */
public class PotentialOutOfMemoryException extends RuntimeException {

	private static final long serialVersionUID = -5497615489817976942L;

	public PotentialOutOfMemoryException() {
		super();
	}

	public PotentialOutOfMemoryException(String detailMessage) {
		super(detailMessage);
	}

	public PotentialOutOfMemoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public PotentialOutOfMemoryException(Throwable cause) {
		super((cause == null ? null : cause.toString()), cause);
	}
}
