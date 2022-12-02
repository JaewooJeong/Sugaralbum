package com.kiwiple.multimedia.exception;

/**
 * InvalidCanvasUserException.
 */
public class InvalidCanvasUserException extends MultimediaException {

	private static final long serialVersionUID = 7005689093345585452L;

	public InvalidCanvasUserException() {
		super();
	}

	public InvalidCanvasUserException(String detailMessage) {
		super(detailMessage);
	}

	public InvalidCanvasUserException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCanvasUserException(Throwable cause) {
		super((cause == null ? null : cause.toString()), cause);
	}
}