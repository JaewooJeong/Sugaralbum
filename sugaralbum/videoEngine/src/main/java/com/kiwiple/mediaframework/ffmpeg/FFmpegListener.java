package com.kiwiple.mediaframework.ffmpeg;

/**
 * FFmpegListener.
 */
public interface FFmpegListener {

	public abstract void onCompletion();

	public abstract void onError();
}
