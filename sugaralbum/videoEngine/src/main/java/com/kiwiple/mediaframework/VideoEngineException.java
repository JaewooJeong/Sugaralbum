package com.kiwiple.mediaframework;

public class VideoEngineException extends RuntimeException {

	private static final long serialVersionUID = 6814098035193661314L;

	public VideoEngineException() {
		super();
	}

	public VideoEngineException(String detailMessage) {
		super(detailMessage);
	}

	public VideoEngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public VideoEngineException(Throwable cause) {
		super((cause == null ? null : cause.toString()), cause);
	}
}