package com.kiwiple.multimedia.exception;

/**
 * InvalidFileException.
 */
public class InvalidFileException extends MultimediaException {

	private static final long serialVersionUID = 2473411001331742342L;

	public InvalidFileException() {
		super();
	}

	public InvalidFileException(String detailMessage) {
		super(detailMessage);
	}

	public InvalidFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidFileException(Throwable cause) {
		super((cause == null ? null : cause.toString()), cause);
	}
}
