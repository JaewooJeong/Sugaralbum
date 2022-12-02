package com.kiwiple.mediaframework.data;

/**
 * Video Encoding 시 필요한 Data
 */
public class VideoInfo {

	/** Video Width */
	public int mWidth;
	/** Video Height */
	public int mHeight;
	/** Video BiteRate Height 값에 따라 설정함 */
	public int mBitRate;
	/** Video FrameRate default : 24 */
	public int mFrameRate;

	public VideoInfo() {

		mWidth = 1280;
		mHeight = 720;
		mFrameRate = 24;
	}
}
