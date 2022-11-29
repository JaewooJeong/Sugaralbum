package com.kiwiple.mediaframework.data;

import java.nio.ByteBuffer;

import android.media.MediaFormat;

/**
 * JNI에서 MediaFormat전달 class
 */
public class MediaFormatJNI {
	private MediaFormat mFormat;

	/** 0: Audio 1: Video */
	private int mMediaType;

	/** CFFmpegCodeID enum id값 */
	private int mCodecId;

	/**
	 * 생성자
	 * 
	 * @param f
	 *            Muxing 할 컨텐츠의 foramt
	 * @param type
	 *            컨텐츠의 타입
	 * @param id
	 *            코덱의 id
	 */
	public MediaFormatJNI(MediaFormat f, int type, int id) {
		mFormat = f;
		mMediaType = type;
		mCodecId = id;
	}

	/**
	 * 컨텐츠의 타입을 설정
	 * 
	 * @param type
	 *            컨텐츠의 타입
	 */
	public void setMediaType(int type) {
		mMediaType = type;
	}

	/**
	 * 컨텐츠의 타입을 반환
	 * 
	 * @return 컨텐츠 타입의 아이디
	 */
	public int getMediaType() {
		return mMediaType;
	}

	/**
	 * 코덱의 아이디를 설정
	 * 
	 * @param id
	 *            코덱 아이디
	 */
	public void setCodecID(int id) {
		mCodecId = id;
	}

	/**
	 * 코덱의 아이디를 반환
	 * 
	 * @return 코덱의 아이디
	 */
	public int getCodecID() {
		return mCodecId;
	}

	/**
	 * mime type을 설정
	 * 
	 * @param value
	 *            mime type
	 */
	public void setMime(char[] value) {
		String string = String.copyValueOf(value);
		mFormat.setString(MediaFormat.KEY_MIME, string);
	}

	/**
	 * mime type을 반
	 * 
	 * @return mime type
	 */
	public String getMime() {
		if (mFormat.containsKey(MediaFormat.KEY_MIME)) {
			return mFormat.getString(MediaFormat.KEY_MIME);
		} else {
			return null;
		}
	}

	/**
	 * muxing 시 최대 입력 크기를 결정
	 * 
	 * @param value
	 *            최대 입력 크기
	 */
	public void setMaxInputSize(int value) {
		mFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, value);
	}

	/**
	 * muxing 시 최대 입력 크기를 반환
	 * 
	 * @return 최대 입력 크기
	 */
	public int getMaxInputSize() {
		if (mFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
			return mFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
		} else {
			return 0;
		}
	}

	/**
	 * 컨텐츠의 Bitrate 설정
	 * 
	 * @param value
	 *            bitrate
	 */
	public void setBitrate(int value) {
		mFormat.setInteger(MediaFormat.KEY_BIT_RATE, value);
	}

	/**
	 * 컨텐츠의 Bitrate를 반환
	 * 
	 * @return Bitrate
	 */
	public int getBitrate() {
		if (mFormat.containsKey(MediaFormat.KEY_BIT_RATE)) {
			return mFormat.getInteger(MediaFormat.KEY_BIT_RATE);
		} else {
			return 0;
		}
	}

	// Video Format ..
	/**
	 * 컨텐츠의 width 설정
	 * 
	 * @param value
	 *            width
	 */
	public void setWidth(int value) {
		mFormat.setInteger(MediaFormat.KEY_WIDTH, value);
	}

	/**
	 * 컨텐츠의 width 반환
	 * 
	 * @return width
	 */
	public int getWidth() {
		if (mFormat.containsKey(MediaFormat.KEY_WIDTH)) {
			return mFormat.getInteger(MediaFormat.KEY_WIDTH);
		} else {
			return 0;
		}
	}

	/**
	 * 컨텐츠의 height 설정
	 * 
	 * @param value
	 *            height
	 */
	public void setHeight(int value) {
		mFormat.setInteger(MediaFormat.KEY_WIDTH, value);
	}

	/**
	 * 컨텐츠의 height를 반환
	 * 
	 * @return height
	 */
	public int getHeight() {
		if (mFormat.containsKey(MediaFormat.KEY_HEIGHT)) {
			return mFormat.getInteger(MediaFormat.KEY_HEIGHT);
		} else {
			return 0;
		}
	}

	// Decoder에서 사용
	/**
	 * 컨텐츠의 Coler Format을 설정
	 * 
	 * @param value
	 *            color format
	 */
	public void setColorFormat(int value) {
		mFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, value);
	}

	/**
	 * 컨텐츠의 Color Format을 반환
	 * 
	 * @return Bitrate
	 */
	public int getColorFormat() {
		if (mFormat.containsKey(MediaFormat.KEY_COLOR_FORMAT)) {
			return mFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
		} else {
			return 0;
		}
	}

	/**
	 * 컨텐츠의 Frame Rate를 설정
	 * 
	 * @param value
	 *            frame rate
	 */
	public void setFrameRate(int value) {
		mFormat.setInteger(MediaFormat.KEY_FRAME_RATE, value);
	}

	/**
	 * 컨텐츠의 framerate를 반환
	 * 
	 * @return framerate
	 */
	public int getFrameRate() {
		if (mFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
			return mFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
		} else {
			return 0;
		}
	}

	/**
	 * 컨텐츠의 I Frame의 interval 간격을 설정
	 * 
	 * @param value
	 *            I Frmae interval
	 */
	public void setIFrameInterval(int value) {
		mFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, value);
	}

	/**
	 * 컨텐츠의 I Frmae Interval 반환
	 * 
	 * @return I Frmae Interval
	 */
	public int getIFrameInterval() {
		if (mFormat.containsKey(MediaFormat.KEY_I_FRAME_INTERVAL)) {
			return mFormat.getInteger(MediaFormat.KEY_I_FRAME_INTERVAL);
		} else {
			return 0;
		}
	}

	// Audio..
	/**
	 * 컨텐츠의 Channel 수 설정
	 * 
	 * @param value
	 *            channel 수
	 */
	public void setChannel(int value) {
		mFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, value);
	}

	/**
	 * 컨텐츠의 channel 수를 반환
	 * 
	 * @return channel 수
	 */
	public int getChannel() {
		if (mFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
			return mFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
		} else {
			return 0;
		}
	}

	/**
	 * 컨텐츠의 sample rate 설정
	 * 
	 * @param value
	 *            sample rate
	 */
	public void setSampleRate(int value) {
		mFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, value);
	}

	/**
	 * 컨텐츠의 sample rate를 반환
	 * 
	 * @return sample rate
	 */
	public int getSampleRate() {
		if (mFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
			return mFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
		} else {
			return 0;
		}
	}

	/**
	 * 컨텐츠의 ADTS 설정
	 * 
	 * @param value
	 *            ADTS
	 */
	public void setISADTS(int value) {
		mFormat.setInteger(MediaFormat.KEY_IS_ADTS, value);
	}

	/**
	 * 컨텐츠의 ADTS를 반환
	 * 
	 * @return ADTS
	 */
	public int getISADTS() {
		if (mFormat.containsKey(MediaFormat.KEY_IS_ADTS)) {
			return mFormat.getInteger(MediaFormat.KEY_IS_ADTS);
		} else {
			return 0;
		}
	}

	/**
	 * 컨텐츠의 AAC profile 설정
	 * 
	 * @param value
	 *            AAC profile
	 */
	public void setAACProfile(int value) {
		mFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, value);
	}

	/**
	 * 컨텐츠의 AAC Profile을 반환
	 * 
	 * @return AAC Profile
	 */
	public int getAACProfile() {
		if (mFormat.containsKey(MediaFormat.KEY_AAC_PROFILE)) {
			return mFormat.getInteger(MediaFormat.KEY_AAC_PROFILE);
		} else {
			return 0;
		}
	}

	/**
	 * 컨텐츠의 Channel Mask 설정
	 * 
	 * @param value
	 *            Channel Mask
	 */
	public void setChannelMask(int value) {
		mFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, value);
	}

	/**
	 * 컨텐츠의 Channel Mask를 반환
	 * 
	 * @return Channel Mask
	 */
	public int getChannelMask() {
		if (mFormat.containsKey(MediaFormat.KEY_CHANNEL_MASK)) {
			return mFormat.getInteger(MediaFormat.KEY_CHANNEL_MASK);
		} else {
			return 0;
		}
	}

	/**
	 * 컨텐츠의 Flac 압축 레벨 설정
	 * 
	 * @param value
	 *            flac 압축 레벨
	 */
	public void setFlacCompressionLevel(int value) {
		mFormat.setInteger(MediaFormat.KEY_FLAC_COMPRESSION_LEVEL, value);
	}

	/**
	 * 컨텐츠의 Flac 압축 레벨 반환
	 * 
	 * @return Flac 압축 레벨
	 */
	public int getFlacCompressionLevel() {
		if (mFormat.containsKey(MediaFormat.KEY_FLAC_COMPRESSION_LEVEL)) {
			return mFormat.getInteger(MediaFormat.KEY_FLAC_COMPRESSION_LEVEL);
		} else {
			return 0;
		}
	}

	// Extra Data
	/**
	 * 컨텐츠의 Extra data 설정
	 * 
	 * @param key
	 *            key
	 * @param data
	 *            extra data
	 */
	public void setExtraData(int key, byte[] data) {
		String str = "csd-" + key;
		ByteBuffer buffer = ByteBuffer.wrap(data);
		mFormat.setByteBuffer(str, buffer);
	}

	/**
	 * 컨텐츠의 key 에 해당하는 Extra Data를 반환
	 * 
	 * @param key
	 *            extradata의 key
	 * @return extra data
	 */
	public byte[] getExtraData(int key) {
		String str = "csd-" + key;
		ByteBuffer buffer = mFormat.getByteBuffer(str);
		if (buffer != null) {
			return buffer.array();
		} else {
			return null;
		}
	}

	/**
	 * 컨텐츠의 ExtraData의 크기를 반환
	 * 
	 * @param value
	 *            extradata 크기
	 */
	public int getExtraDataSize() {
		int size = 0;
		for (int i = 0; i < 10; i++) {
			String str = "csd-" + i;
			if (!mFormat.containsKey(str)) {
				break;
			}

			ByteBuffer buffer = mFormat.getByteBuffer(str);
			if (buffer == null) {
				break;
			} else {
				size += buffer.capacity();
			}
		}
		return size;
	}

	// video raw data
	/**
	 * 영상의 왼쪽부분을 얼마나 자를것인지 반환
	 * 
	 * @return Crop size
	 */
	public int getCropLeft() {
		if (mFormat.containsKey("crop-left")) {
			return mFormat.getInteger("crop-left");
		} else {
			return -1;
		}
	}

	/**
	 * 영상의 오른쪽 부분을 얼마나 자를것인지 반환
	 * 
	 * @return Crop size
	 */
	public int getCropRight() {
		if (mFormat.containsKey("crop-right")) {
			return mFormat.getInteger("crop-right");
		} else {
			return -1;
		}
	}

	/**
	 * 영상의 상단을 얼마나 자를것인지 반환
	 * 
	 * @return Crop size
	 */
	public int getCropTop() {
		if (mFormat.containsKey("crop-top")) {
			return mFormat.getInteger("crop-top");
		} else {
			return -1;
		}
	}

	/**
	 * 영상의 하단을 얼마나 자를것인지 반환
	 * 
	 * @return Crop size
	 */
	public int getCropBottom() {
		if (mFormat.containsKey("crop-botton")) {
			return mFormat.getInteger("crop-botton");
		} else {
			return -1;
		}
	}

	/**
	 * 영상의 stride 값을 반환
	 * 
	 * @return stride 값
	 */
	public int getStride() {
		if (mFormat.containsKey("stride")) {
			return mFormat.getInteger("stride");
		} else {
			return -1;
		}
	}
}