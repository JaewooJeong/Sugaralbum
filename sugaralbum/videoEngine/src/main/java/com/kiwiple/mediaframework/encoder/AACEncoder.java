package com.kiwiple.mediaframework.encoder;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build;

import com.kiwiple.mediaframework.MediaType;
import com.kiwiple.mediaframework.data.AudioInfo;
import com.kiwiple.mediaframework.util.ADTSManager;
import com.kiwiple.mediaframework.util.ADTSManager.AAC_PROFILE;

/**
 * AAC Encoder Calss
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("InlinedApi")
public class AACEncoder extends HWBaseEncoder {

	/** AAC Information */
	AACINFO mInfo;
	/** Adts Data */
	byte[] mAdtsData;
	
	private		int		BIT_RATE = 128000;
	private		int		SAMPLE_RATE = 44100;

	/**
	 * Constructure
	 * @param id Encoder가 여러개 인경우 구분하기 위한 id
	 * @param l  Encoding listner(결과 내용은 pipeline에서 사용)
	 */	
	public AACEncoder(EncoderListener l, int id) {
		super( l , id);
		mInfo = new AACINFO();
        mInfo.mBitRate = BIT_RATE;
        mInfo.mSampleRate = SAMPLE_RATE;
        mInfo.mChannel = 2;
		mInfo.mFreqIdx = ADTSManager.getFrequencyIdx(SAMPLE_RATE);
        mInfo.mProfile = AAC_PROFILE.OMX_AUDIO_AACObjectLC;
		mType = MediaType.Audio;
	}

	/**
	 * encoding 준비 단계
	 * @return true: init Success false: init fail
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public boolean do_init() {

		if (mInfo.mProfile == AAC_PROFILE.OMX_AUDIO_AACObjectHE
				&& mInfo.mSampleRate < 22050)
			return false;

		mFormat = new MediaFormat();
		mFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
		mFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, mInfo.mProfile.getKey());
		mFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, mInfo.mSampleRate);
		mFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mInfo.mChannel);
		mFormat.setInteger(MediaFormat.KEY_BIT_RATE, mInfo.mBitRate);
		try {
			mCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
			} catch(IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
//	            VL.e("Encoder init fail : createEncoderByType");
	            return false;
	        }

		mCodec.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

//		mAdtsData = new byte[7];

		return true;
	}

	/**
	 * encoding 종료 시 호출
	 */
	@Override
	public void do_destory() {

	}

	/**
	 * encoding 된  data를 처리 할 때 사용
	 * @param buffer   Encoding 된 data
	 * @param info     buffer info
	 */
	@Override
	public void do_releaseOutputBuffer(ByteBuffer buffer, BufferInfo info) {

//		byte[] data = new byte[info.size];
//		buffer.get(data);

//		info.size += 7;
//		ADTSManager.setADTSByte(mInfo, mAdtsData, info.size);
//		setADTSByte(info.size);
//		buffer.clear();
//		buffer.put(mAdtsData);
//		buffer.put(data);

		buffer.position(info.offset);
//		buffer.limit(info.offset + info.size);
		mEncoderListener.onEncodedOutput(buffer, info, mId, false);
	}

	
	
	/**
	 * AAC Information
	 */
	static public class AACINFO extends AudioInfo{
		/**	AAC_PROFILE 참조 	*/
		public AAC_PROFILE mProfile;
		public int			mFreqIdx;
	}	
}