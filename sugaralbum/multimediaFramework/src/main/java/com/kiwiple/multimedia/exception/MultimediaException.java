package com.kiwiple.multimedia.exception;

/**
 * MultimediaException.
 */
public class MultimediaException extends RuntimeException {

	private static final long serialVersionUID = -4052642958119169004L;

	public MultimediaException() {
		super();
	}

	public MultimediaException(String detailMessage) {
		super(detailMessage);
	}

	public MultimediaException(String message, Throwable cause) {
		super(message, cause);
	}

	public MultimediaException(Throwable cause) {
		super((cause == null ? null : cause.toString()), cause);
	}
}
