package com.kiwiple.mediaframework.demuxer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.MediaType;
import com.kiwiple.mediaframework.decoder.DecoderApi;
import com.kiwiple.mediaframework.decoder.DecoderListener;
import com.kiwiple.mediaframework.decoder.DecoderManager;
import com.kiwiple.debug.L;

public class HWDemuxer implements DemuxerApi {

	MediaExtractor mExtractor;

	long mStartTime;
	long mEndTime;
	ArrayList<MediaType> marTrackType;

	boolean mEos;

	/**
	 * Parser 초기화 
	 * @param path 		File Path
	 * @return	Parser 생성 여부 
	 */
	@Override
	public boolean init(String path, long starttime, long endtitme) {
		clear();
		initData();
		mEos = false;
		try {
			mExtractor.setDataSource(path);
			initTrackInfo();
			mStartTime = starttime;
			mEndTime = endtitme;
			return true;
		} catch (Exception e) {
			L.e("File open error:" + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Parser 초기화 
	 * @param fd Source file descriptor
	 * @return	Parser 생성 여부 
	 */	
	public boolean init(AssetFileDescriptor afd, long starttime, long endtime){
		clear();
		initData();
		mEos = false;
		
		try {
			mExtractor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			initTrackInfo();
			mStartTime = starttime;
			mEndTime = endtime;
			
			return true; 
		} catch (Exception e){
			L.e("Resource load error : " + e.getMessage());
			return false;
		}
	}

	/**
	 * 해당 Track의 정보를 설정 
	 */
	private void initTrackInfo() {
		int trackSize = mExtractor.getTrackCount();
		marTrackType = new ArrayList<MediaType>();
		for (int i = 0; i < trackSize; i++) {
			MediaFormat format = mExtractor.getTrackFormat(i);
			String mime = format.getString(MediaFormat.KEY_MIME);
			L.d(i + " || format:" + format);
			MediaType type = MediaType.Unknown;
			if (mime.contains("audio")) {
				type = MediaType.Audio;

			} else if (mime.contains("video")) {
				type = MediaType.Video;
			}
			marTrackType.add(type);
		}
	}

	/**
	 * Demuxer의 각 해당하는 track의 decoder를 매칭시킴 
	 * @param artrack	array List Decoder
	 * @param l			Decoder Listener
	 */
	@SuppressLint("InlinedApi")
	@Override
	public void addDecoder(ArrayList<DecoderApi> artrack, DecoderListener l) {

		for (int i = 0, size = mExtractor.getTrackCount(); i < size; i++) {
			MediaFormat format = mExtractor.getTrackFormat(i);
			DecoderApi track = null;
			switch (marTrackType.get(i)) {
				case Audio:
					track = DecoderManager.CreateAudioDecoer(format, i, l);
					track.init();
					break;
				case Video:
					track = DecoderManager.CreateVideoDecoer(format, i, l);
					track.init();
					break;
				default:
					break;
			}

			if (track != null) {
				artrack.add(track);
				mExtractor.selectTrack(i);
			}
		}
		if (mStartTime > 0) {
			seek(mStartTime);
		}
	}

	/**
	 * index에 해당하는 Track 정보 가져오기 
	 * @param idx	index
	 * @return		index에 해당하는 Track 정보 
	 */
	@Override
	public MediaFormat getTrackFormat(int idx) {
		return mExtractor.getTrackFormat(idx);
	}

	/**
	 * index에 해당하는 Track Type 가져오기 
	 * @param idx	index
	 * @return			index에 해당하는 Trak Type
	 */
	@Override
	public MediaType getTrackType(int idx) {
		if (marTrackType.size() <= idx)
			return MediaType.Unknown;

		return marTrackType.get(idx);
	}

	/**
	 * get Track size
	 * @return  	Track size
	 */
	@Override
	public int getTrackSize() {
		return mExtractor.getTrackCount();
	}

	/**
	 * Parsing되는 track index
	 * @param idx		track number
	 */	
	@Override
	public void selectTrack(int idx) {
		mExtractor.selectTrack(idx);
	}

	/**
	 * readSampleData 함수로 얻오올 data의 Track index 
	 * @return		해당 data의 Track index
	 */
	@Override
	public int getSampleTrackIndex() {
		return mExtractor.getSampleTrackIndex();
	}

	/**
	 * Data 가져오기 
	 * @param buffer		가져올 Data(Out)
	 * @param offset		해당 buffer에 이어쓸 경우 offset위치를 지정해주어야됨 
	 * @return		data 길이 
	 */
	@Override
	public int readSampleData(ByteBuffer buffer, int offset) {
		int result = mExtractor.readSampleData(buffer, offset);
		if (result < 1)
			mEos = true;
		return result;
	}

	/**
	 * Demuxer에서 나온 data의 PTs
	 * @return		data의  PTS
	 */
	@Override
	public long getSampleTime() {
		return mExtractor.getSampleTime();
	}

	/**
	 * 다음 Data를 가져오기 위함  
	 * @return		
	 */
	@Override
	public boolean next() {
		return mExtractor.advance();
	}

	
	/**
	 * demuxer 초기화 
	 */
	@Override
	public void clear() {
		if (mExtractor != null) {
			mExtractor.release();
			mExtractor = null;
		}
	}

	/**
	 * Demuxer에서 나온 data의 flag
	 * @return
	 */
	@Override
	public int getSampleFlags() {
		return mExtractor.getSampleFlags();
	}

	/**
	 * Demuxer 생성 
	 * 
	 */
	private void initData() {
		mExtractor = new MediaExtractor();
	}

	/**
	 * 해당 시간에 맞는 위치부터 parsing
	 * @param time	Seek time (1/1000000초)
	 */
	@Override
	public void seek(long time) {
		mExtractor.seekTo(time, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
		
	}

	/**
	 * Demuxer의 Video Track의 decoder를 매칭 시킴
	 * @param artrack	result Video Track
	 * @param l			Listenr 
	 */
	@Override
	public void addVideoDecoder(ArrayList<DecoderApi> artrack, DecoderListener l) {
		for (int i = 0, size = mExtractor.getTrackCount(); i < size; i++) {
			MediaFormat format = mExtractor.getTrackFormat(i);

			if (format.getString(MediaFormat.KEY_MIME).startsWith("video")) {
				DecoderApi track = null;
				track = DecoderManager.CreateVideoDecoer(format, i, l, mEndTime);

				if (track != null) {
					artrack.add(track);
					mExtractor.selectTrack(i);
				}
			}
		}
		if (mStartTime > 0) {
			seek(mStartTime);
		}
	}

	/**
	 * Demuxer의 Audio Track의 decoder를 매칭시
	 * @param artrack	result Audio Track
	 * @param l			Listener
	 */
	public void addAudioDecoder(ArrayList<DecoderApi> artrack, DecoderListener l) {
		for (int i = 0, size = mExtractor.getTrackCount(); i < size; i++) {
			MediaFormat format = mExtractor.getTrackFormat(i);

			if (format.getString(MediaFormat.KEY_MIME).startsWith("")) {
				DecoderApi track = null;
				track = DecoderManager.CreateAudioDecoer(format, i, l);

				if (track != null) {
					artrack.add(track);
					mExtractor.selectTrack(i);
				}
			}
		}
		if (mStartTime > 0) {
			seek(mStartTime);
		}
	}

	/**
	 * Eos 
	 * @return 	true : eos 		false : not eos
	 */
	@Override
	public boolean isEos() {
		return mEos;
	}
}