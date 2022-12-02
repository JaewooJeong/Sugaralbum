package com.kiwiple.mediaframework.muxer;

import java.nio.ByteBuffer;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

public abstract class MuxerApi {

	/**	 true: H/W Muxer  false: S/W Muxer(ffmpeg)	*/
	boolean			mIsHW;
	
	/**	Muxing할 준비 여부 true: Muxing 가능   false: Muxing 안됨 	*/
	boolean			mIsReady;

	/**
	 * Muxer 초기화 
	 * @param path		생성될 file path
	 * @return			true : Muxer init 성공    false: 실패 
	 */
	public abstract boolean init(String path);
	
	/**
	 * Muxer Start 
	 * init후 add track이 완료 되면 Muxing 할 준비 함 
	 */
	public abstract void start();
	/**
	 * Muxing이 완료 되면 해당 함수 호출함 
	 */
	public abstract void finish();
	/**
	 * Muxing 할 Track의 정보를 전달함 
	 * <p>  ffmpeg에서 extradata가 있는 경우 MediaFormat에 Key값으로 extradatasize와 extradata가 포함됨
	 * <p> extradatasize[int] : extradata size, extradata[bytebuffer]: extradata
	 * @param f
	 * @return
	 */
	public abstract int addTrack(MediaFormat f);
	
	/**
	 * Muxing end후 Muxer destory
	 */
	public abstract void destoryMuxer();
	/**
	 * Data Muxing
	 * @param trackNum	Muxer기준의 Track number
	 * @param data		Muxing 할 data
	 * @param info		data info
	 */
	public abstract void mux(int trackNum, ByteBuffer data, BufferInfo info);
	
	/**
	 * H/W Muxer 인지 확인 
	 * @return		true : H/W Muxer   false: S/W Muxer [ffmpeg]
	 */
	public boolean	isHW()
	{
		return mIsHW;
	}
	
	/**
	 * Muxing 할 준비가 되어있는지 확인 
	 * @return		true: Muxing 준비 완료   false: Muxing 준비 안됨 
	 */
	public boolean isReady()
	{
		return mIsReady;
	}
}
