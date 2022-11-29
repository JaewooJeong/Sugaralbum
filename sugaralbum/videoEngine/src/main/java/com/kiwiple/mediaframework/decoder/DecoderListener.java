package com.kiwiple.mediaframework.decoder;

import java.nio.ByteBuffer;

import android.media.MediaCodec.BufferInfo;

public interface DecoderListener {
	/**
	 * Decoding 데이터 전달을 위한 메서드
	 * @param trackIdx  디코딩 된 Track의 Index
	 * @param buffer    디코딩된 데이터 
	 * @param info 		디코딩된 데이터에 대한 정보
	 */
	public abstract  void onDecodedOutputAvailable(int trackIdx, ByteBuffer buffer, BufferInfo info);
	/**
	 * Decoding 종료 시 호출되는 메서드
	 * @param trackIdx  종료 Track의 Index
	 */	
    public abstract  void onEndOfStream(int trackIdx);
}
