package com.kiwiple.mediaframework.data;

/**
 * Audio Encoding 시 필요한 Data
 */
public class AudioInfo {
	/**
	 * SampleRate
	 * <p>
	 * 96000 hz, 88200 hz, 64000 hz, 48000 hz, 44100 hz, 32000 hz, 24000 hz, 22050 hz, 16000 hz,
	 * 12000 hz, 11025 hz, 8000 hz, 7350 hz
	 * <p>
	 * default: 44100hz
	 * */
	public int mSampleRate;

	/**
	 * BitRate 64000, 128000, 320000
	 * <p>
	 * default: 64000
	 * */
	public int mBitRate;

	/** Channel Channel 수 default:2 */
	public int mChannel;

	public AudioInfo() {
		mSampleRate = 44100;
		mBitRate = 128000;
		mChannel = 2;
	}
}