package com.kiwiple.mediaframework.encoder;

import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;

import com.kiwiple.mediaframework.MediaType;
import com.kiwiple.mediaframework.VideoEngineEnvironment;
import com.kiwiple.mediaframework.VideoEngineException;
import com.kiwiple.mediaframework.data.VideoInfo;
import com.kiwiple.mediaframework.preview.PreviewVideoDecoder;
import com.kiwiple.debug.L;

public class AVCEncoder extends HWBaseEncoder {

	private static final int IFRAME_INTERVAL = 1;
	private VideoInfo mInfo;
	private final	int	FRAME_RATE = 30;
	
	private	byte[]	mSps;
	private	byte[]	mPps;
	private boolean		mSetExtraData;

	/*
	 * 20150326 Baby테마 열화현상으로 4배 
	 * 20151118 : Ubox로 업로드 된 무비다이어리 재생시 끊기는 문제로 인해 bitrate 1/2 수정
	 */
	private final static int AVC_FULL_HD_BITRATE = 4194304 * 2;  // 16mb to 8mb
	private final static int AVC_HD_BITRATE = 2097152 * 2;  //8mb to 4mb
	private final static int AVC_SD_BITRATE = 1258291 / 2;  //1.2mb to 0.6mb

	private final static int AVC_FULL_HD_WIDTH = 1920;
	private final static int AVC_HD_WIDTH = 1280;
	private final static String mMimeType = "video/avc";
	private String mCodecName;
	private int mBufferOffSet;
	private final static int AVC_DEFAULT_OFFSET = 0;

	/**
	 * Constructure
	 * @param id     Encoder가 여러개 인경우 구분하기 위한 id
	 * @param l      Encoding listner(결과 내용은 pipeline에서 사용)
	 * @param info   Encoding 할 영상 정보 
	 * @param offset 초기 설정 할 Offset 
	 */	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public AVCEncoder(EncoderListener l, VideoInfo info, int id, int offset) {
		super( l, id);

		mInfo = new VideoInfo();
		mBufferOffSet = offset;
		if(info.mWidth < 1 )
			mInfo.mWidth = 0;
		else
			mInfo.mWidth = info.mWidth;

		if(info.mHeight < 1)
			mInfo.mHeight = 0;
		else
			mInfo.mHeight = info.mHeight;
		
		mInputDataSize = (int)(mInfo.mWidth * mInfo.mHeight * 1.5);

		if(mInfo.mWidth == AVC_FULL_HD_WIDTH){
			mInfo.mBitRate = AVC_FULL_HD_BITRATE;
			
		}else if(mInfo.mWidth == AVC_HD_WIDTH){
			mInfo.mBitRate = AVC_HD_BITRATE;
			
		}else{
			mInfo.mBitRate = AVC_SD_BITRATE;
		}
		
        mInfo.mFrameRate = FRAME_RATE;

		mType = MediaType.Video;
		
		mSps = null;
		mPps = null;
		mSetExtraData = false;
	}
	
	/**
	 * encoding 준비 단계
	 * @return true: init Success false: init fail
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public boolean do_init() {
		mFormat = MediaFormat.createVideoFormat(mMimeType, mInfo.mWidth, mInfo.mHeight);

		mFormat.setInteger(MediaFormat.KEY_WIDTH, mInfo.mWidth);
		mFormat.setInteger(MediaFormat.KEY_HEIGHT, mInfo.mHeight);

		mFormat.setInteger(MediaFormat.KEY_BIT_RATE, mInfo.mBitRate);
		mFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mInfo.mFrameRate);
		
		if(mBufferOffSet != AVC_DEFAULT_OFFSET)
			mFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, (int)(mInfo.mWidth * (mInfo.mHeight + mBufferOffSet) * 1.5));
		
		int currentColorFormatPriority = PreviewVideoDecoder.sPrioritizedColorFormats.length;
		int selectedColorFormat = VideoEngineEnvironment.INVALID_INTEGER_VALUE;
		
		for (int i = 0; i != MediaCodecList.getCodecCount(); ++i) {

			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
			if (!codecInfo.isEncoder() || codecInfo.getName().contains("audio")) {
				continue;
			}

			try { // FIXME: YOU must have to FIGURE OUT how to SIMPLIFY thiS.
				for (String type : codecInfo.getSupportedTypes()) {
					if (type.equalsIgnoreCase(mMimeType)) {
						for (int supportedColorFormat : codecInfo.getCapabilitiesForType(mMimeType).colorFormats) {
							for (int j = 0; j != currentColorFormatPriority; ++j) {
								if (supportedColorFormat == PreviewVideoDecoder.sPrioritizedColorFormats[j]) {
									mCodecName = codecInfo.getName();
									selectedColorFormat = PreviewVideoDecoder.sPrioritizedColorFormats[j];
									currentColorFormatPriority = j;
									break;
								}
							}
						}
					}
				}
			
			} catch (IllegalArgumentException exception) { // cause is unknown.
				L.i(codecInfo.getName() + " causes IllegalArgumentException at getCapabilitiesForType()!");
				continue;
			}
		}
		
		L.i("mCodecName : " + mCodecName + " : selectedColorFormat : " + selectedColorFormat);
		
		try {
			mFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, selectedColorFormat);
			mFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
			mCodec = MediaCodec.createEncoderByType(mMimeType);
			mCodec.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		} catch (Exception exception) {
			throw new VideoEngineException(exception.getMessage());
		}

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
		
		boolean	IsStart = false;
		if(!mSetExtraData && (info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
		{
			int idx = info.offset;
        		
			if(mSps == null)
			{
				idx = CheckSps(buffer, idx, info.size);
				L.d("sps return idx:" + idx + "  buffer size:" + info.size);
			}
		
			if(mPps == null)
			{
				idx = CheckPps(buffer, idx, info.size);
				L.d("pps return idx:" + idx + "  buffer size:" + info.size);
			}
			
	        if(mSps != null && mPps != null)
	        {
				L.d("Set Extradata");
	        	IsStart = true;
	        	setExtradata();
	        }
	        else
	        {
				L.d("not Extradata");
	        }
		} 
		
		buffer.position(info.offset);
		
        mEncoderListener.onEncodedOutput(buffer, info, mId, IsStart);
	}
	
	/**
	 * Sps 데이터를 만들기 위한 메서드
	 * @param buf   		encoding할 데이터 
	 * @param buffoffset    buffer 내 데이터의 오프셋
	 * @param bufsize		buffer의 크기
	 */
	private int CheckSps(ByteBuffer buf, int buffoffset, int bufsize) {
		int offset = 0;
		int size = 0;
		int searchsize = bufsize - 5;
		byte[] startcode =  new byte[4];
		for (int i = buffoffset; i < searchsize; i++) {
			buf.position(i);
			buf.get(startcode);

			if(startcode[0] == 0x00 && startcode[1] == 0x00 &&
					startcode[2] == 0x00 &&  startcode[3] == 0x01 )
			{
				if(offset > 0)
				{
					size = i - offset;
					break;
				}

				byte nal = buf.get(i+4);
				L.d("Nal:" + Integer.toHexString(0xFF&(nal)));
				//20150528 칩 제조사 마다 sps, pps값이 다르기에  SPS/PPS n/u 이 0아닐 경우 extradata 설정 
//				if(nal == 0x67)
//				{
//					offset = i+4;
//					i = offset +1;
//				}
				if(nal != 0)
				{
					offset = i+4;
					i = offset +1;
				}
			}
		}
		if(offset > 0 && size < 1)
			size = bufsize - offset;
		
		if(size > 0)
		{
			mSps = new byte[size + 2];
			buf.position(offset);
			buf.get(mSps, 2, size);
			mSps[0] = (byte)((size&0xFF00)>>8);   
			mSps[1] = (byte)(size&0xFF);
		}
		
		return offset+size;
	}
	
	/**
	 * PPS 데이터를 만들기 위한 메서드
	 * @param buf   		encoding할 데이터 
	 * @param buffoffset    buffer 내 데이터의 오프셋
	 * @param bufsize		buffer의 크기
	 */	
	private int CheckPps(ByteBuffer buf, int buffoffset, int bufsize) {
		int offset = 0;
		int size = 0;
		int searchsize = bufsize - 5;
		byte[] startcode =  new byte[4];
		for (int i = buffoffset; i < searchsize; i++) {
			buf.position(i);
			buf.get(startcode);

			if(startcode[0] == 0x00 && startcode[1] == 0x00 &&
					startcode[2] == 0x00 &&  startcode[3] == 0x01 )
			{
				if(offset > 0)
				{
					size = i - offset;
					break;
				}

				byte nal = buf.get(i+4);
				L.d("Nal:" + Integer.toHexString(0xFF&(nal)));
				L.d("nal == 40 >>>"+ (nal == 40));
				L.d("nal == 0x28 >>>"+ (nal == 0x28));
				//20150528 칩 제조사 마다 sps, pps값이 다를 수 있기에 SPS/PPS n/u 이 0아닐 경우 extradata 설정 
//				if(nal == 0x68 || nal == 40 || nal == 0x28)
//				{
//					offset = i+4;
//					i = offset +1;
//				}
				if(nal != 0)
				{
					offset = i+4;
					i = offset +1;
				}
			}
		}
		if(offset > 0 && size < 1)
			size = bufsize - offset;
		
		if(size > 0)
		{
			mPps = new byte[size + 3];
			buf.position(offset);
			buf.get(mPps, 3, size);
			mPps[0] = 0x01;
			mPps[1] = (byte)((size&0xFF00)>>8);   
			mPps[2] = (byte)(size&0xFF);
		}
		
		return offset+size;
	}

	/**
	 * 헤더 정보를 만들기 위한 메서
	 */
	private void setExtradata() {
		mSetExtraData = true;

        byte[]	data = new byte[6];
        data[0] = (byte)0x01;
        data[1] = mSps[3];
        data[2] = mSps[4];
        data[3] = mSps[5];
        data[4] = (byte)0xff;
        data[5] = (byte)0xE1;
        
        
        int size = data.length + mSps.length + mPps.length;
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);

        buffer.put(data);
        
        buffer.put(mSps);
        buffer.put(mPps);
       
        // Android에서 사용되지 않는 key임 ffmpeg에서 사용함 
        mFormat.setByteBuffer("extradata", buffer);
        mFormat.setInteger("extradatasize", size);
	}
	
}
