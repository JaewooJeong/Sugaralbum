package com.kiwiple.multimedia.canvas;

/**
 * VideoFileFactoryListener.
 * 
 */
public interface VideoFileFactoryListener {

	public abstract void onComplete();

	public abstract void onError(Exception exception);

	public abstract void onProgressUpdate(int totalFrameCount, int renderedFrameCount);
}
