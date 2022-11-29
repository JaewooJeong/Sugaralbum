package com.kiwiple.mediaframework.muxer;

import java.nio.ByteBuffer;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

public class SWMuxer extends MuxerApi {
	
	KwpMuxerJni		mMuxer;

	/**
	 * Muxer 생성자 
	 */
	public SWMuxer()
	{
		mMuxer = new KwpMuxerJni();
		mIsReady = false;
		mIsHW = false;
	}

	/**
	 * Muxer 초기화 
	 * @param path		생성될 file path
	 * @return			true : Muxer init 성공    false: 실패 
	 */	
	@Override
	public boolean init(String path) {
		mIsReady = false;
		return mMuxer.init(path);
	}

	/**
	 * Muxer Start 
	 * init후 add track이 완료 되면 Muxing 할 준비 함 
	 */	
	@Override
	public void start() {
		mIsReady = true;
		mMuxer.Start();
	}

	/**
	 * Muxing이 완료 되면 해당 함수 호출함 
	 */	
	@Override
	public void finish() {
		mIsReady = false;
		mMuxer.End();
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
		mMuxer.destoryMuxer();
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
			mMuxer.Muxing(trackNum, data, info);
	}

}
