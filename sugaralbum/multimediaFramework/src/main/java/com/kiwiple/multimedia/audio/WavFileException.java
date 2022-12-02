package com.kiwiple.multimedia.audio;

/**
 * WavFileException.
 * 
 * http://www.labbookpages.co.uk/
 */
class WavFileException extends Exception {

	private static final long serialVersionUID = -2106322774466656406L;

	public WavFileException() {
		super();
	}

	public WavFileException(String message) {
		super(message);
	}

	public WavFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public WavFileException(Throwable cause) {
		super(cause);
	}
}
