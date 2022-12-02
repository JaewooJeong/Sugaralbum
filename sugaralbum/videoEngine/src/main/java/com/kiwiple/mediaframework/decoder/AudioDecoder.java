package com.kiwiple.mediaframework.decoder;

import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.MediaType;

/**
 * Audio Decoder
 */
@SuppressLint("InlinedApi")
public class AudioDecoder extends HWBaseDecoder {

	/**
	 * Audio Decoder 생성자
	 * @param format    압축 format
	 * @param trackIdx  Demuxer의 Track number
	 * @param l         Deocoder listener
	 */
	public AudioDecoder(MediaFormat format, int trackIdx, DecoderListener l) {
		super(format, trackIdx, l);
		mType = MediaType.Audio;
	}

	/**
	 * Audio Decoder의 데이터를 전달 받기 위한 메서드
	 * @param outputBufferIdx    출력 버퍼의 인덱스 번호
	 * @param outputInfo  		 출력된 데이터의 정보
	 * @param buffer			 출력된 바이트 데이터
	 */	
	protected void do_releaseOutputBuffer(int outputBufferIdx,
			BufferInfo outputInfo, ByteBuffer buffer) {

		buffer.position(outputInfo.offset);
		buffer.limit(outputInfo.size);

		mDecoderListener.onDecodedOutputAvailable(mTrackIdx, buffer, outputInfo);

		mCodec.releaseOutputBuffer(outputBufferIdx, false); 
	};

	
	/**
	 * Audio Decoding 종료 시 호출
	 */
	@Override
	protected void do_destory() {
	}

}
