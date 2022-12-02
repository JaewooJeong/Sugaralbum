package com.kiwiple.mediaframework;

/**
 * ffmpgeg에서 사용되는 Codec Id와 호환하기 위한 enum 값 
 */
public enum CFFmpegCodecID {
	CODEC_UNKNOWN(0),
	// Video
	CODEC_H264(1),

	// AUDIO
	CODEC_MP3(100), CODEC_AAC(101);
	
	private int mValue;
	private int mType;
	private String mStr;
	private String mMime;

	/**
	 * 생성자  
	 * 
	 * @param v 코덱 id 
	 * 
	 */			
	private CFFmpegCodecID(int v) {
		mValue = v;
		switch (mValue) {
		default:
		case 0:		//UnKnown
			mStr =  "Un_Known";
			mType = -1;
			mMime = "Un_Known";
			break;
		case 1:		//H264
			mStr =  "Codec_H264";
			mType = 1;
			mMime = "video/avc";
			break;
		case 100:	//MP3
			mStr =  "Codec_MP3";
			mType = 0;
			mMime = "audio/mpeg";
			break;
		case 101:	//AAC
			mStr =  "Codec_AAC";
			mType = 0;
			mMime = "audio/mp4a-latm";
			break;
		}
	}

	/**
	 * 코덱 이름 반환  
	 * 
	 * @return codec name
	 */				
	public String getToString() {
		return mStr;
	}
	
	/**
	 * 코덱의 id를 반환  
	 * 
	 * @return 코덱 id
	 */			
	public int getId()
	{
		return mValue;
	}
	
	/**
	 * 코덱의 타입을 반환  
	 * 
	 * @return Codec type
	 */			
	public int getType()
	{
		return mType;
	}
	
	/**
	 * mime type 별 FFmpeg 코덱과 매칭이 되도록 변환  
	 * 
	 * @param mime mime type
	 * 
	 * @return FFMpeg Codec ID 
	 */			
	public static CFFmpegCodecID getMimeToCodecID(String mime)
	{
		if(mime.equals("video/avc"))
			return CFFmpegCodecID.CODEC_H264;
		else if(mime.equals("audio/mpeg"))
			return CFFmpegCodecID.CODEC_MP3;
		else if(mime.equals("audio/mp4a-latm"))
			return CFFmpegCodecID.CODEC_AAC;
		else
			return CFFmpegCodecID.CODEC_UNKNOWN;
	}
	
}
