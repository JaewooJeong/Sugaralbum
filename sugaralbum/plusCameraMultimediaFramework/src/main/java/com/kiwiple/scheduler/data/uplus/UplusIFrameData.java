package com.kiwiple.scheduler.data.uplus;

import com.kiwiple.imageanalysis.analysis.ImageAnalysis.IFrameData;

/**
 * frame 데이터 클래스. 
 */
public class UplusIFrameData {
	private IFrameData mUplusIFrameData;
	private String mVideoPath;
	private long mStartPosition;
	private long mEndPosition;
	private String mOrientation;

	/**
	 * 생성자.  
	 * @param uplusIFrameData i frame data.
	 * @param videopath 비디오 경로.
	 * @param startPosition 시작 위치.
	 * @param endPosition 끝 위치.
	 * @param orientation 비디오 orientation.
	 */
	public UplusIFrameData(IFrameData uplusIFrameData, String videopath, long startPosition, long endPosition, String orientation) {
		setUplusIFrameData(uplusIFrameData);
		setVideoPath(videopath);
		setStartPosition(startPosition);
		setEndPosition(endPosition);
		setOrientation(orientation);
	}

	/**
	 * i frame data 반환. 
	 * @return i frame data.
	 */
	public IFrameData getUplusIFrameData() {
		return mUplusIFrameData;
	}

	/**
	 * i frame data 섲어. 
	 * @param uplusIFrameData i frame data. 
	 */
	public void setUplusIFrameData(IFrameData uplusIFrameData) {
		this.mUplusIFrameData = uplusIFrameData;
	}

	/**
	 * 비디오 경로 반환.
	 * @return 비디오 경로.
	 */
	public String getVideoPath() {
		return mVideoPath;
	}

	/**
	 * 비디오 경로 설정. 
	 * @param videoPath 비디오 경로.
	 */
	public void setVideoPath(String videoPath) {
		this.mVideoPath = videoPath;
	}

	/**
	 * 시작 위치 반환. 
	 * @return 시작 위치. 
	 */
	public long getStartPosition() {
		return mStartPosition;
	}

	/**
	 * 시작 위치 설정. 
	 * @param startPosition 시작 위치.
	 */
	public void setStartPosition(long startPosition) {
		this.mStartPosition = startPosition;
	}

	/**
	 * 끝 위치 반환. 
	 * @return 끝 위치. 
	 */
	public long getEndPosition() {
		return mEndPosition;
	}

	/**
	 * 끝 위치 설정. 
	 * @param endPosition 끝 위치
	 */
	public void setEndPosition(long endPosition) {
		this.mEndPosition = endPosition;
	}

	/**
	 * orientation 반환. 
	 * @return orientation. 
	 */
	public String getOrientation() {
		return mOrientation;
	}

	/**
	 * orientation 설정. 
	 * @param orientation 비디오 orientation. 
	 */
	public void setOrientation(String orientation) {
		this.mOrientation = orientation;
	}
}
