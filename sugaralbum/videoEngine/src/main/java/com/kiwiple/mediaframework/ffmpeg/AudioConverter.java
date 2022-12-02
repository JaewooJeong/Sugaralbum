package com.kiwiple.mediaframework.ffmpeg;

import com.kiwiple.debug.Precondition;
import com.kiwiple.mediaframework.ffmpeg.FileFormat.AudioOutput;

/**
 * AudioConverter.
 */
public final class AudioConverter extends AbstractFFmpegProcessor {

	// // // // // Member variable.
	// // // // //
	private final FFmpegProcessor mProcessor;

	private AudioOutput mOutputFileFormat;
	private String mInputFilePath;
	private String mOutputFilePath;

	private int mSamplingRate = INVALID_INTEGER_VALUE;
	private int mChannelCount = INVALID_INTEGER_VALUE;
	private float mStartTimeS = INVALID_FLOAT_VALUE;
	private float mDurationS = INVALID_FLOAT_VALUE;

	// // // // // Constructor.
	// // // // //
	AudioConverter(FFmpegProcessor processor) {
		Precondition.checkNotNull(processor, "processor must not be null.");
		mProcessor = processor;
	}

	// // // // // Method.
	// // // // //
	public AudioConverter setFilePath(String input, String output) {
		Precondition.checkFile(input).checkExist();
		Precondition.checkFile(output).checkParent(true);

		mInputFilePath = input;
		mOutputFilePath = output;
		return this;
	}

	public AudioConverter setOutputFormat(AudioOutput format) {
		Precondition.checkNotNull(format, "format must not be null.");

		mOutputFileFormat = format;
		return this;
	}

	public AudioConverter setSamplingRate(int samplingRate) {
		Precondition.checkArgument(samplingRate > 0, "samplingRate must be greater than 0.");

		mSamplingRate = samplingRate;
		return this;
	}

	public AudioConverter setChannelCount(int channelCount) {
		Precondition.checkArgument(channelCount > 0, "channelCount must be greater than 0.");

		mChannelCount = channelCount;
		return this;
	}

	public AudioConverter setStartTime(float startTimeS) {
		Precondition.checkArgument(startTimeS >= 0.0f, "startTimeS must be greater than or equal to 0.");

		mStartTimeS = startTimeS;
		return this;
	}

	public AudioConverter setDuration(float durationS) {
		Precondition.checkArgument(durationS > 0.0f, "durationS must be greater than 0.");

		mDurationS = durationS;
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
		Precondition.checkState(mOutputFilePath != null && mInputFilePath != null, "You must invoke setFilePath()");
		Precondition.checkState(mOutputFileFormat != null, "You must invoke setOutputFormat()");

		CommandBuilder builder = new CommandBuilder();
		builder.append("-i").append(mInputFilePath);

		if (mSamplingRate != INVALID_INTEGER_VALUE) {
			builder.append("-ar").append(mSamplingRate);
		}
		if (mChannelCount != INVALID_INTEGER_VALUE) {
			builder.append("-ac").append(mChannelCount);
		}
		if (mStartTimeS != INVALID_FLOAT_VALUE) {
			builder.append("-ss").append(mStartTimeS);
		}
		if (mDurationS != INVALID_FLOAT_VALUE) {
			builder.append("-t").append(mDurationS);
		}

		builder.append("-f").append(mOutputFileFormat.extension);
		builder.append(mOutputFilePath);
		builder.append("-y");

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