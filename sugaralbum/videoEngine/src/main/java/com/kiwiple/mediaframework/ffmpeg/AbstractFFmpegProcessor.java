package com.kiwiple.mediaframework.ffmpeg;

import com.kiwiple.debug.Precondition;

/**
 * AbstractFFmpegProcessor.
 */
abstract class AbstractFFmpegProcessor implements IFFmpegProcessor {

	// // // // // Static variable.
	// // // // //
	static final int INVALID_INTEGER_VALUE = -1;
	static final float INVALID_FLOAT_VALUE = -1.0f;

	static final String SPLIT_DELIMITER = "\n";

	private TaskPriority mPriority = TaskPriority.NORMAL;

	// // // // // Static method.
	// // // // //
	static final String adaptCommandFormat(String format) {
		Precondition.checkNotNull(format, "format must not be null.");
		return format.replace(" ", SPLIT_DELIMITER);
	}

	public void setPriority(TaskPriority priority) {
		Precondition.checkNotNull(priority, "priority must not be null.");
		mPriority = priority;
	}

	public TaskPriority getPriority() {
		return mPriority;
	}

	// // // // // Inner Class.
	// // // // //
	static final class CommandBuilder {

		private final StringBuilder builder;

		CommandBuilder() {
			builder = new StringBuilder();
		}

		private void appendDelimiter() {
			if (builder.length() != 0) {
				builder.append(SPLIT_DELIMITER);
			}
		}

		public CommandBuilder append(int value) {
			appendDelimiter();
			builder.append(value);
			return this;
		}

		public CommandBuilder append(long value) {
			appendDelimiter();
			builder.append(value);
			return this;
		}

		public CommandBuilder append(float value) {
			appendDelimiter();
			builder.append(value);
			return this;
		}

		public CommandBuilder append(double value) {
			appendDelimiter();
			builder.append(value);
			return this;
		}

		public CommandBuilder append(String value) {
			appendDelimiter();
			builder.append(value);
			return this;
		}

		@Override
		public String toString() {
			return builder.toString();
		}
	}
}