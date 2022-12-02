package com.kiwiple.mediaframework.encoder;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

import com.kiwiple.mediaframework.data.VideoInfo;
import com.kiwiple.debug.L;

public class EncoderManager {
	
	/**
	 * Create AVC Encoder 
	 * @param info	Video Info	
	 * @param l		Encoder listener
	 * @param id	Encoder Id (Encoder가 여러개 있는 경우 구분하는 변수)
	 * @param offset 
	 * @return		AVCEncoder가 생성되지 않으면 null
	 */
	static public EncoderApi CreateVideo_AVC_Encoder(VideoInfo info,  EncoderListener l, int id, int offset) {
		if(!SupportHWEncoder("video/avc"))
		{
			L.w("Not Support Encoder: AVC");
			return null;
		}

		EncoderApi result = new AVCEncoder(l, info, id, offset);
		if (result.init()) {
			return result;
		} else {
			result.destory();
			// S/W Decoder 추가 필요
			return null;
		}
	}


	/**
	 * Create	AAC Encoder
	 * @param l		Encoder Listener
	 * @param id	Encoder Id (Encoder가 여러개 있는 경우 구분하는 변수)
	 * @return	AAC Encoder가 생성되지 않으면 null
	 */
	static public EncoderApi CreateAudio_AAC_Encoder( EncoderListener l, int id) {
		if(!SupportHWEncoder("audio/mp4a-latm"))
		{
			L.w("Not Support Encoder: AAC");
			return null;
		}


		EncoderApi result = new AACEncoder(l, id);
		if (result.init()) {
			return result;
		} else {
			result.destory();
			// S/W Enecoder 추가 필요
			return null;
		}
	}

	static boolean SupportHWEncoder(String mimeType) {
		boolean result = false;
		for (int i = 0, size = MediaCodecList.getCodecCount(); i < size; i++) {
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			if (!info.isEncoder()) {
				continue;
			}
			String[] types = info.getSupportedTypes();
			for (int j = 0; j < types.length; j++) {
				if (types[j].equalsIgnoreCase(mimeType)) {
					return true;
				}
			}
		}
		return result;
	}

}
