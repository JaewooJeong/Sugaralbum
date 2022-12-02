package com.kiwiple.mediaframework.ffmpeg;

/**
 * IFFmpegProcessor.
 */
public interface IFFmpegProcessor {

	public abstract boolean isRunning();

	public abstract boolean isSupportedInputFile(String fileName);
	
	public static enum TaskPriority {

		HIGH(3),

		NORMAL(2),

		LOW(1);

		final int level;

		private TaskPriority(int level) {
			this.level = level;
		}
	}
}
