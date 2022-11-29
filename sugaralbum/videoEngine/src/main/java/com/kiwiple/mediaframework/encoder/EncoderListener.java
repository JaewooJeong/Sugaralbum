package com.kiwiple.mediaframework.encoder;

import java.nio.ByteBuffer;

import android.media.MediaCodec.BufferInfo;

public interface EncoderListener {
	/**
	 * encoding 데이터 전달을 위한 메서드
	 * @param id  		인코더의 id
	 * @param buffer    인코딩 된 데이터 
	 * @param info 		인코딩 된 데이터에 대한 정보
	 * @param IsStart	인코딩 시작 여부 파악을 위함
	 */
	public abstract  void onEncodedOutput(ByteBuffer buffer, BufferInfo info, int id, boolean IsStart);
	
	/**
	 * encoding 종료 시 호출되는 메서드
	 * @param id  종료 encoder의 Index
	 */	
	public abstract  void onEndOfStream(int id);
}
