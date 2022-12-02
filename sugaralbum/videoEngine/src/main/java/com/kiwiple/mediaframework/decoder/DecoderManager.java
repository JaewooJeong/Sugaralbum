package com.kiwiple.mediaframework.decoder;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import com.kiwiple.debug.L;

public class DecoderManager {

	/**
	 * VideoDecoder를 생성하는 메서드
	 * @param format	디코딩할 영상의 정보  
	 * @param tracIdx   디코딩할 Track의 index 
	 * @param l 		디코딩 시 처리할 동작에 대한 리스너
	 * 
	 * @return DecoderApi
	 */
	static public DecoderApi CreateVideoDecoer(MediaFormat format, int tracIdx, DecoderListener l) {
		if (!SupportHWDecoder(format)) {
			L.w("Not Support Decoder:" + format);
			return null;
		}

		DecoderApi result = new VideoDecoder(format, tracIdx, l);
		if (result.init()) {
			return result;
		} else {
			result.destory();
			// S/W Decoder 추가 필요
			return null;
		}
	}

	/**
	 * VideoDecoder를 생성하는 메서드
	 * @param format	디코딩할 영상의 정보  
	 * @param tracIdx   디코딩할 Track의 index 
	 * @param l 		디코딩 시 처리할 동작에 대한 리스너
	 * @param endTime 	영상 중 전달받은 PTS까지만 디코딩
	 * 
	 * @return DecoderApi
	 */
	static public DecoderApi CreateVideoDecoer(MediaFormat format, int tracIdx,
			DecoderListener l, long endTime) {
		if (!SupportHWDecoder(format)) {
			L.w("Not Support Decoder:" + format);
			return null;
		}

		DecoderApi result = new VideoDecoder(format,  tracIdx, l, endTime);
		if (result.init()) {
			return result;
		} else {
			result.destory();
			// S/W Decoder 추가 필요
			return null;
		}
	}

	/**
	 * AudioDecoder를 생성하는 메서드
	 * @param format	디코딩할 오디의 정보  
	 * @param tracIdx   디코딩할 Track의 index 
	 * @param l 		디코딩 시 처리할 동작에 대한 리스너
	 * 
	 * @return DecoderApi
	 */
	static public DecoderApi CreateAudioDecoer(MediaFormat format,
			int trackIdx, DecoderListener l) {
		if (!SupportHWDecoder(format)) {
			L.w("Not Support Decoder:" + format);
			return null;
		}

		DecoderApi result = new AudioDecoder(format, trackIdx, l);
		if (result.init()) {
			return result;
		} else {
			result.destory();
			// S/W Decoder 추가 필요
			return null;
		}
	}

	/**
	 * 해당 Mime 타입을 기기에서 지원하는지 확인
	 * @param format	디코딩할 컨텐츠의 정보
	 * 
	 * @return 지원하는 Mime type - true 
	 */
	static boolean SupportHWDecoder(MediaFormat format) {
		boolean result = false;
		String mimeType = format.getString(MediaFormat.KEY_MIME);
		for (int i = 0, size = MediaCodecList.getCodecCount(); i < size; i++) {
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			if (info.isEncoder()) {
				continue;
			}
			String[] types = info.getSupportedTypes();
			for (int j = 0; j < types.length; j++) {
				if (types[j].equalsIgnoreCase(mimeType)) {
					result = true;
					break;
				}
			}
			if(result)
				break;
		}
		return result;
	}

}
