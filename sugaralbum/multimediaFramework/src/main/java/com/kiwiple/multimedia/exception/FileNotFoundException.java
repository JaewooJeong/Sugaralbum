package com.kiwiple.multimedia.exception;

/**
 * FileNotFoundException.
 */
public class FileNotFoundException extends MultimediaException {

	private static final long serialVersionUID = -622840530207189767L;

	public FileNotFoundException() {
		super();
	}

	public FileNotFoundException(String detailMessage) {
		super(detailMessage);
	}

	public FileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileNotFoundException(Throwable cause) {
		super((cause == null ? null : cause.toString()), cause);
	}
}
