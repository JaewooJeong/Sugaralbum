package com.kiwiple.mediaframework.demuxer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.content.res.AssetFileDescriptor;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.MediaType;
import com.kiwiple.mediaframework.decoder.DecoderApi;
import com.kiwiple.mediaframework.decoder.DecoderListener;

public interface DemuxerApi {

	/**
	 * Parser 초기화 
	 * @param path 		File Path
	 * @return	Parser 생성 여부 
	 */
	public boolean	init(String path,  long starttime, long endtitme);
	
	/**
	 * Parser 초기화 
	 * @param fd Source file descriptor
	 * @return	Parser 생성 여부 
	 */
	public boolean	init(AssetFileDescriptor afd,  long starttime, long endtitme);
	
	/**
	 * Demuxer의 각 해당하는 track의 decoder를 매칭시킴 
	 * @param artrack	array List Decoder
	 * @param l			Decoder Listener
	 */
	public void addDecoder( ArrayList<DecoderApi> artrack,  DecoderListener l);
	
	/**
	 * Demuxer의 Video Track의 decoder를 매칭 시킴
	 * @param artrack	result Video Track
	 * @param l			Listenr 
	 */
	public void addVideoDecoder(ArrayList<DecoderApi> artrack, DecoderListener l);
	
	/**
	 * Demuxer의 Audio Track의 decoder를 매칭시
	 * @param artrack	result Audio Track
	 * @param l			Listener
	 */
	
	public void addAudioDecoder(ArrayList<DecoderApi> artrack, DecoderListener l);
	
	/**
	 * readSampleData 함수로 얻오올 data의 Track index 
	 * @return		해당 data의 Track index
	 */
	public int 		getSampleTrackIndex();
	
	/**
	 * Data 가져오기 
	 * @param buffer		가져올 Data(Out)
	 * @param offset		해당 buffer에 이어쓸 경우 offset위치를 지정해주어야됨 
	 * @return		data 길이 
	 */
	public int		readSampleData(ByteBuffer buffer, int offset);
	
	/**
	 * get Track size
	 * @return  	Track size
	 */
	public int 		getTrackSize();
	
	/**
	 * index에 해당하는 Track Type 가져오기 
	 * @param idx	index
	 * @return			index에 해당하는 Trak Type
	 */
	public MediaType	getTrackType(int idx);
	
	/**
	 * index에 해당하는 Track 정보 가져오기 
	 * @param idx	index
	 * @return		index에 해당하는 Track 정보 
	 */
	public MediaFormat	getTrackFormat(int idx);
	
	/**
	 * Parsing되는 track index
	 * @param idx		track number
	 */
	public void		selectTrack(int idx);
	
	/**
	 * Demuxer에서 나온 data의 PTs
	 * @return		data의  PTS
	 */
	public long		getSampleTime();
	
	/**
	 * Demuxer에서 나온 data의 flag
	 * @return
	 */
	public int		getSampleFlags();
	
	/**
	 * 다음 Data를 가져오기 위함  
	 * @return		
	 */
	public boolean	next(); 
	
	/**
	 * 해당 시간에 맞는 위치부터 parsing
	 * @param time	Seek time (1/1000000초)
	 */
	public void seek(long time);
	
	/**
	 * demuxer 초기화 
	 */
	public void clear();
	
	/**
	 * Eos 
	 * @return 	true : eos 		false : not eos
	 */
	public boolean		isEos();
}
