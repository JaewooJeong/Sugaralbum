package com.kiwiple.mediaframework.decoder;

import android.media.MediaFormat;

import com.kiwiple.mediaframework.MediaType;
import com.kiwiple.mediaframework.demuxer.DemuxerApi;

public interface DecoderApi {
	/**
	 * 처음 초기화 함수 
	 * @return	Decoding 가능한 format인지에 따른 return 값 true: 가능 false: 지원안되는 format
	 */
	public boolean	init();
	
	/**
	 * decoder 사용하지 않을시 해당 함수로 종료시킴
	 */
	public void destory();
	
	/**
	 * Decoder의 Media type을 전달함 
	 * @return		Media type
	 */
	public MediaType	getMediaType();
	
	/**
	 * Data Decoding	
	 * <p>buffer copy 줄이기 위하여 demuxer를 parameter로 받음
	 * @param demuxer	Demuxer 
	 */
	public void sampleDecoding(DemuxerApi demuxer);
	
	
	/**
	 * 마지막 PTS 전달 
	 * @return		Pste
	 */
	public long lastPts();
	
	/**
	 * Demuxer 기준의 Track Index
	 * @return
	 */
	public int getTrackIndex();
	
	/**
	 * Input Stream Format
	 */
	public MediaFormat	getFormat();
	
	/**
	 * Output Stream Format
	 * @return
	 */
	public MediaFormat  getOutFormat();

	/**
	 * Output Data max Size 
	 * <p>(단말에 따라 H/W Encoder input data가 작은 경우가 있어 해당 함수가 필요할듯함)
	 * @return
	 */
	public int outputmaxSize();
	
	/**
	 * Output Buffer Size
	 * 아웃버퍼 크기 리턴
	 * @return 
	 */
	public int getOutputBufferSize();
	
	/**
	 * Decoding Current Time
	 * 디코딩 한 결과 데이터 나오는 프리젠테이션 시간 리턴
	 * @return
	 */
	public long getOutputTime();
	
	/**
	 * 버퍼를 초기화 하기 위한 메서드 
	 */	
	public void flush();
}
