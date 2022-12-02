package com.kiwiple.mediaframework.preview;

/**
 * UnsupportedMediaTypeException.
 */
public class UnsupportedMediaTypeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnsupportedMediaTypeException() {
		super();
	}

	public UnsupportedMediaTypeException(String detailMessage) {
		super(detailMessage);
	}

	public UnsupportedMediaTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedMediaTypeException(Throwable cause) {
		super((cause == null ? null : cause.toString()), cause);
	}
}
