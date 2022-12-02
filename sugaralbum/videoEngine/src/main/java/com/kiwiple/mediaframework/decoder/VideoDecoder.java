package com.kiwiple.mediaframework.decoder;

import java.nio.ByteBuffer;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.MediaType;
import com.kiwiple.debug.L;

public class VideoDecoder extends HWBaseDecoder {

	/**
	 * Video Decoder 생성자
	 * @param format    영상 format
	 * @param trackIdx  Demuxer의 Track number
	 * @param l         Decoder listener
	 * @param endTime   디코딩 할 마지막 시간 
	 */
	public VideoDecoder(MediaFormat format, int tracIdx, DecoderListener l, long endTime) {
		super(format, tracIdx, l, endTime);
		mType = MediaType.Video;
	}

	
	/**
	 * Video Decoder 생성자
	 * @param format    영상 format
	 * @param trackIdx  Demuxer의 Track number
	 * @param l         Decoder listener 
	 */
	public VideoDecoder(MediaFormat format, int tracIdx, DecoderListener l) {
		super(format, tracIdx, l);
		mType = MediaType.Video;
	}

	/**
	 * Video Decoder의 데이터를 전달 받기 위한 메서드
	 * @param outputBufferIdx    출력 버퍼의 인덱스 번호
	 * @param outputInfo  		 출력된 데이터의 정보
	 * @param buffer			 출력된 바이트 데이터
	 */		
	@Override
	protected void do_releaseOutputBuffer(int outputBufferIdx, BufferInfo outputInfo, ByteBuffer buffer) {

		if (buffer != null) {
			buffer.position(outputInfo.offset);
			buffer.limit(outputInfo.size);
		} else {
			L.w("Video Outbuffer is null...........");
		}

		mDecoderListener.onDecodedOutputAvailable(mTrackIdx, buffer, outputInfo);
		try {
			mCodec.releaseOutputBuffer(outputBufferIdx, false);
		} catch (IllegalStateException exception) {
			L.e("IllegalStateException.");
		}
	}

	/**
	 * Video Decoding 종료 시 호출
	 */	
	@Override
	protected void do_destory() {

	}
}
