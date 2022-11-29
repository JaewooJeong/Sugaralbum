package com.kiwiple.mediaframework.encoder;

import java.nio.ByteBuffer;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.MediaType;

public interface EncoderApi {
	/**
	 * encoding 준비 단계
	 * @return true: init Success false: init fail
	 */
	public boolean init();

	/**
	 * Encoder destory
	 */
	public void	destory();
	
	/**
	 * Encoding...
	 * @param data   row data
	 * @param info   data information
	 */
	public void sampleEncoding(ByteBuffer data, BufferInfo info);
	
	/**
	 * get Track number
	 * @return track number
	 */
	public int getTrackNum();
	
	/**
	 * Track의 Number를 성정 
	 * @param tracknum track number
	 */
	public void setTrackNum(int tracknum);
	
	/**
	 * 컨텐츠의 타입을 얻어오기 위한 메서드 
	 * @return Media Type
	 */
	public MediaType getMediaType();
	
	/**
	 * 입력 버퍼의 MediaFormat을 얻어오기 위한 메서드
	 * @return Media Format
	 */
	public MediaFormat getMediaFormat();
	
	/**
	 * 출력 버퍼의 MediaFormat을 얻어오기 위한 메서드
	 * @return Media Format
	 */	
	public MediaFormat getOutputMediaFormat();
}
