package com.kiwiple.mediaframework.ffmpeg;

import com.kiwiple.debug.Precondition;
import com.kiwiple.mediaframework.ffmpeg.FileFormat.VideoOutput;

/**
 * VideoMuxer.
 */
public final class VideoMuxer extends AbstractFFmpegProcessor {

	// // // // // Member variable.
	// // // // //
	private final FFmpegProcessor mProcessor;

	private VideoOutput mOutputFileFormat;
	private String mVideoInputFilePath;
	private String mAudioInputFilePath;
	private String mOutputFilePath;

	private float mVideoDurationMs;
	private boolean mLoopAudio;

	private int mAudioFadeInDurationMs;
	private int mAudioFadeOutDurationMs;

	// // // // // Constructor.
	// // // // //
	VideoMuxer(FFmpegProcessor processor) {
		Precondition.checkNotNull(processor, "processor must not be null.");
		mProcessor = processor;
	}

	// // // // // Method.
	// // // // //
	public VideoMuxer setFilePath(String videoInput, String audioInput, String output) {
		Precondition.checkFile(videoInput).checkExist();
		Precondition.checkFile(audioInput).checkExist();
		Precondition.checkFile(output).checkParent(true);

		mVideoInputFilePath = videoInput;
		mAudioInputFilePath = audioInput;
		mOutputFilePath = output;
		return this;
	}

	public VideoMuxer setOutputFormat(VideoOutput format) {
		Precondition.checkNotNull(format, "format must not be null.");

		mOutputFileFormat = format;
		return this;
	}

	public VideoMuxer setLoopAudio(boolean loopAudio) {
		mLoopAudio = loopAudio;
		return this;
	}

	public VideoMuxer setFadeAudio(int fadeDurationMs) {
		return setFadeAudio(fadeDurationMs, fadeDurationMs);
	}

	public VideoMuxer setFadeAudio(int fadeInDurationMs, int fadeOutDurationMs) {
		Precondition.checkNotNegative(fadeInDurationMs, fadeOutDurationMs);

		mAudioFadeInDurationMs = fadeInDurationMs;
		mAudioFadeOutDurationMs = fadeOutDurationMs;
		return this;
	}

	public boolean execute() {
		String command = buildCommand();
		return mProcessor.execute(command, getPriority());
	}

	public void execute(FFmpegListener listener) {
		String command = buildCommand();
		mProcessor.execute(command, getPriority(), listener);
	}

	private String buildCommand() {
		Precondition.checkNotNull(mOutputFilePath, "You must invoke setFilePath()");
		Precondition.checkNotNull(mOutputFileFormat, "You must invoke setOutputFormat()");

		mVideoDurationMs = MediaUtils.getDuration(mVideoInputFilePath);

		CommandBuilder builder = new CommandBuilder();
		builder.append("-i").append(mVideoInputFilePath);
		builder.append("-i").append(mLoopAudio ? buildCommandChunkForAudioLoop() : mAudioInputFilePath);

		if (mAudioFadeInDurationMs > 0 || mAudioFadeOutDurationMs > 0) {
			builder.append("-af").append(buildCommandChunkForAudioFade());
		}

		if (mOutputFileFormat.equals(VideoOutput.MPEG4)) {
			builder.append("-c:v").append("copy");
			builder.append("-c:a").append("aac");
			builder.append("-strict").append("experimental");
		} else {
			Precondition.assureUnreachable();
		}

		builder.append("-t").append(mVideoDurationMs / 1000.0f);
		builder.append("-f").append(mOutputFileFormat.extension);
		builder.append(mOutputFilePath);
		builder.append("-y");

		return builder.toString();
	}

	private String buildCommandChunkForAudioLoop() {

		float audioDurationMs = MediaUtils.getDuration(mAudioInputFilePath);
		int loop = (int) Math.ceil(mVideoDurationMs / audioDurationMs);

		if (loop > 1) {

			StringBuilder builder = new StringBuilder();
			builder.append("concat:").append(mAudioInputFilePath);
			loop -= 1;

			for (int i = 0; i != loop; ++i) {
				builder.append("|").append(mAudioInputFilePath);
			}
			return builder.toString();
		}
		return mAudioInputFilePath;
	}

	private String buildCommandChunkForAudioFade() {

		StringBuilder builder = new StringBuilder();
		if (mAudioFadeInDurationMs > 0) {
			builder.append("afade=t=in:d=");
			builder.append(mAudioFadeInDurationMs / 1000.0f);
		}
		if (mAudioFadeOutDurationMs > 0) {
			if (mAudioFadeInDurationMs > 0) {
				builder.append(",");
			}
			float fadeOutDuration = mAudioFadeOutDurationMs / 1000.0f;
			builder.append("afade=t=out:st=");
			builder.append(mVideoDurationMs / 1000.0f - fadeOutDuration);
			builder.append(":d=");
			builder.append(fadeOutDuration);
		}
		return builder.toString();
	}

	@Override
	public boolean isSupportedInputFile(String fileName) {
		return mProcessor.isSupportedInputFile(fileName);
	}

	@Override
	public boolean isRunning() {
		return mProcessor.isRunning();
	}
}