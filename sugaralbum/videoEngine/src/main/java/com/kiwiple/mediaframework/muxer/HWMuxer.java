package com.kiwiple.mediaframework.muxer;

import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaMuxer.OutputFormat;
import android.os.Build;

import com.kiwiple.debug.L;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class HWMuxer extends MuxerApi {

	MediaMuxer		mMuxer;
	
	/**
	 * Muxer 생성자 
	 */
	public HWMuxer()
	{
		mMuxer = null;
		mIsReady = false;
		mIsHW = true;
	}
	
	/**
	 * Muxer 초기화 
	 * @param path		생성될 file path
	 * @return			true : Muxer init 성공    false: 실패 
	 */	
	@Override
	public boolean init(String path) {
		if(mMuxer != null)
			destoryMuxer();
		
		
		mIsReady = false;
		try {
			mMuxer = new MediaMuxer(path,  OutputFormat.MUXER_OUTPUT_MPEG_4);
		} catch (Exception e) {
			L.e("H/W Muxer init error:" + e.getMessage());
			return false;
		}
		
		return true;
	}

	/**
	 * Muxer Start 
	 * init후 add track이 완료 되면 Muxing 할 준비 함 
	 */	
	@Override
	public void start() {
		mIsReady = true;
		mMuxer.start();
	}

	/**
	 * Muxing이 완료 되면 해당 함수 호출함 
	 */	
	@Override
	public void finish() {
		mIsReady = false;
		mMuxer.stop();
	}

	/**
	 * Muxing 할 Track의 정보를 전달함 
	 * <p>  ffmpeg에서 extradata가 있는 경우 MediaFormat에 Key값으로 extradatasize와 extradata가 포함됨
	 * <p> extradatasize[int] : extradata size, extradata[bytebuffer]: extradata
	 * @param f
	 * @return
	 */	
	@Override
	public int addTrack(MediaFormat f) {
		return mMuxer.addTrack(f);
	}

	/**
	 * Muxing end후 Muxer destory
	 */
	@Override
	public void destoryMuxer() {
		mMuxer.release();
	}

	/**
	 * Muxing 할 준비가 되어있는지 확인 
	 * @return		true: Muxing 준비 완료   false: Muxing 준비 안됨 
	 */	
	@Override
	public boolean isReady() {
		return mIsReady;
	}

	/**
	 * Data Muxing
	 * @param trackNum	Muxer기준의 Track number
	 * @param data		Muxing 할 data
	 * @param info		data info
	 */	
	@Override
	public void mux(int trackNum, ByteBuffer data, BufferInfo info) {
		if(info.size > 0) 
			mMuxer.writeSampleData(trackNum, data, info);
	}

}
