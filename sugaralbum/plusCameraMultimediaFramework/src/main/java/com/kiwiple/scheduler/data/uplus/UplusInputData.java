package com.kiwiple.scheduler.data.uplus;

import java.util.ArrayList;
import java.util.List;

import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.scheduler.data.InputData;

/**
 * Uplus 입력 데이터 클래스.  
 *
 */
public class UplusInputData extends InputData {
	private List<ImageData> mImageDataList;
	private List<ImageData> mVideoDataList;

	/**
	 * UplusInputData 생성자.<br>
	 */
	public UplusInputData() {
		super();
		mImageDataList = new ArrayList<ImageData>();
		mVideoDataList = new ArrayList<ImageData>();
	}

	/**
	 * input data의 사진 data list 설정. 
	 * @param imageDataList : 사진 data list. 
	 */
	public void setImageDataList(ArrayList<ImageData> imageDataList) {
		this.mImageDataList = imageDataList;
	}

	/**
	 * input data의 사진 data list 반환.
	 * @return : input data의 사진 파일 list
	 */
	public List<ImageData> getImageDataList() {
		return mImageDataList;
	}

	/**
	 * input data의 사진 data list에 추가. 
	 * @param imageData : 사진 data. 
	 */
	public void imageDataAddToImageDataList(ImageData imageData) {
		mImageDataList.add(imageData);
	}

	/**
	 * input data의 사진 data list 초기화. 
	 */
	public void clearImageDataList() {
		mImageDataList.clear();
	}

	/**
	 * input data의 비디오 data list 설정. 
	 * @param videoDataList : video data list. 
	 */
	public void setVideoDataList(ArrayList<ImageData> videoDataList) {
		this.mVideoDataList = videoDataList;
	}

	/**
	 * input data의 비디오 data list 반환.  
	 * @return input data의 비디오 data list
	 */
	public List<ImageData> getVideoDataList() {
		return mVideoDataList;
	}
}