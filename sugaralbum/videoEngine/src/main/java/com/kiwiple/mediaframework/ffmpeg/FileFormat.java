package com.kiwiple.mediaframework.ffmpeg;

import java.util.Locale;

import com.kiwiple.debug.Precondition;

/**
 * FileFormat
 */
public interface FileFormat {

	public abstract String extension();

	public static final class Utils {

		private Utils() {
			// Utility classes should have a private constructor.
		}

		static boolean isSupportedFileFormat(String fileName, FileFormat[] formats) {
			Precondition.checkNotNull(formats, "formats must not be null.");

			fileName = fileName.toLowerCase(Locale.ENGLISH);
			for (FileFormat format : formats) {
				if (fileName.endsWith("." + format.extension())) {
					return true;
				}
			}
			return false;
		}
	}

	public static enum AudioInput implements FileFormat {

		WAV("wav"),

		MP3("mp3"),

		OGG("ogg"),

		AAC("m4a"),

		FLAC("flac");

		public static boolean isSupported(String fileName) {
			return Utils.isSupportedFileFormat(fileName, values());
		}

		public final String extension;

		private AudioInput(String extension) {
			this.extension = extension;
		}

		@Override
		public String extension() {
			return extension;
		}
	}

	public static enum AudioOutput implements FileFormat {

		WAV("wav");

		public static boolean isSupported(String fileName) {
			return Utils.isSupportedFileFormat(fileName, values());
		}

		public final String extension;

		private AudioOutput(String extension) {
			this.extension = extension;
		}

		@Override
		public String extension() {
			return extension;
		}
	}

	public static enum VideoInput implements FileFormat {

		MPEG4("mp4");

		public static boolean isSupported(String fileName) {
			return Utils.isSupportedFileFormat(fileName, values());
		}

		public final String extension;

		private VideoInput(String extension) {
			this.extension = extension;
		}

		@Override
		public String extension() {
			return extension;
		}
	}

	public static enum VideoOutput implements FileFormat {

		MPEG4("mp4");

		public static boolean isSupported(String fileName) {
			return Utils.isSupportedFileFormat(fileName, values());
		}

		public final String extension;

		private VideoOutput(String extension) {
			this.extension = extension;
		}

		@Override
		public String extension() {
			return extension;
		}
	}
}