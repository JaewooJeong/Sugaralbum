package com.kiwiple.mediaframework.encoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.data.VideoInfo;
import com.kiwiple.mediaframework.demuxer.DemuxerApi;
import com.kiwiple.mediaframework.demuxer.DemuxerManager;
import com.kiwiple.mediaframework.muxer.MuxerApi;
import com.kiwiple.mediaframework.muxer.MuxerManager;
import com.kiwiple.debug.L;

public class VideoEncoderBin {

	private ArrayList<EncoderApi> mEncoder;
	private MuxerApi mMuxer;

	private DemuxerApi mParser;
	private ByteBuffer mAudioBuffer;
	private long mBeforePts = 0;

	/** Encoder Buffer에 남아있는 data를 다 처리 하기위하여 Eos 설정함 */
	private boolean mEos = false;

	/** Audio Muxer Track number */
	private int mAuTracknum;

	/**
	 * VideoEncoderBin class의 생성자
	 */
	public VideoEncoderBin() {
		mAuTracknum = -1;
	}

	/**
	 * VideoEncoderBin class에 사용되는 변수 및 데이터를 설정
	 * 
	 * @param path
	 *            Encoding 경로
	 * @param width
	 *            Encoding 영상 가로 길이
	 * @param height
	 *            Encoding 영상 세로 길이
	 * @param offset
	 *            Encoding 위치
	 */
	public void initialize(String path, int width, int height, int offset) {
		mEncoder = new ArrayList<EncoderApi>();
		VideoInfo vInfo = new VideoInfo();
		vInfo.mWidth = width;
		vInfo.mHeight = height;

		mEos = false;
		mEncoder.add(EncoderManager.CreateVideo_AVC_Encoder(vInfo, mEncoderListener, 0, offset));

		mMuxer = MuxerManager.CreateMuxer(path);
	}

	/**
	 * Encoding할 Audio를 설정하기 위한 메서드
	 * 
	 * @param audioPath
	 *            Encoding 할 mp3 파일 경로
	 */
	public void initializeAudio(String audioPath) {
		L.w("audio path:" + audioPath);

		mParser = DemuxerManager.CreateDemuxer(audioPath, 0, 0);

		if (mParser != null) {
			mParser.selectTrack(0);
			MediaFormat format = mParser.getTrackFormat(0);
			int capacity = format.getInteger(MediaFormat.KEY_SAMPLE_RATE) * 2 * format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
			mAudioBuffer = ByteBuffer.allocate(capacity);

			for (int i = 0; i < mParser.getTrackSize(); i++) {
				L.d(mParser.getTrackFormat(i).toString());
			}
		} else {
			L.d("could not found audio file.");
		}

	}

	/**
	 * Encoding할 Audio를 설정하기 위한 메서드
	 * 
	 * @param aFd
	 *            Encoding 할 asset 내 mp3 파일
	 */
	public void initializeAudio(AssetFileDescriptor aFd) {
		mParser = DemuxerManager.CreateDemuxer(aFd, 0, 0);
		// mVideoStartTime = VideoStartTime;

		if (mParser != null) {
			mParser.selectTrack(0);
			MediaFormat format = mParser.getTrackFormat(0);
			int capacity = format.getInteger(MediaFormat.KEY_SAMPLE_RATE) * 2 * format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
			mAudioBuffer = ByteBuffer.allocate(capacity);

		} else {
			L.d("could not found audio file.");
		}
	}

	/**
	 * Encoding 종료 시 사용된 encoder와 muxer 해제
	 * 
	 */
	public void destory() {
		mEncoder.get(0).destory();
		mMuxer.destoryMuxer();
	}

	/**
	 * Muxer 마지막 마무리 해주는 부분
	 */
	public void finish() {
		mMuxer.finish();
	}

	/**
	 * 실제 Encoding 데이터를 받아서 Encoding을 수행하는 메서드
	 * 
	 * @param buffer
	 *            전달된 byte 데이터
	 * @param info
	 *            전달된 데이터에 해당하는 정보
	 */
	public void sampleEncoding(ByteBuffer buffer, BufferInfo info) {
		if (mEncoder == null || mEncoder.isEmpty() || mEncoder.get(0) == null) {
			L.e("VideoEncoderBin: Encoder not initialized or null");
			throw new RuntimeException("Encoder not initialized");
		}
		
		if (buffer == null || info == null) {
			L.e("VideoEncoderBin: Buffer or BufferInfo is null");
			throw new IllegalArgumentException("Buffer and BufferInfo cannot be null");
		}
		
		try {
			if (info.size > 0) {
				buffer.position(info.offset);
			}
			mEncoder.get(0).sampleEncoding(buffer, info);
		} catch (Exception e) {
			L.e("VideoEncoderBin: Error during sample encoding", e);
			throw new RuntimeException("Sample encoding failed", e);
		}
	}

	EncoderListener mEncoderListener = new EncoderListener() {

		@Override
		public void onEncodedOutput(ByteBuffer buffer, BufferInfo info, int id, boolean isStart) {

			if (isStart) { // H264 초기 데이터 인경우(sps, pps)
				L.d("Call addTrack");

				// Buffer를 Media format의 csd 값으로 통일하여 extradata값을 없애려 하였으나
				// 특정 단말(갤럭시 팝)에서 OutputFormat이 호출될때 죽는 현상으로 SwMuxer 사용시 기존 방법으로 처리
				if (mMuxer.isHW()) {
					mEncoder.get(0).setTrackNum(mMuxer.addTrack(mEncoder.get(0).getOutputMediaFormat()));
				} else {
					mEncoder.get(0).setTrackNum(mMuxer.addTrack(mEncoder.get(0).getMediaFormat()));
				}
				if (mParser != null) {
					mAuTracknum = mMuxer.addTrack(mParser.getTrackFormat(0));
				}
				mMuxer.start();

				if (!mMuxer.isHW()) { // ffmpeg muxer 사용인 경우만 처리
					return;
				}
			}

			if (mEncoder.get(0).getTrackNum() > -1) {
				if (mMuxer.isReady()) {
					if (mParser == null) { // Video만 Muxing
						mMuxer.mux(mEncoder.get(0).getTrackNum(), buffer, info);
					} else { // Audio Video Muxing
						if (!mParser.isEos()) { // Audio Eos가 아니면 Audio Muxing
							long p = mParser.getSampleTime();
							long v = info.presentationTimeUs;

							MediaFormat format = mParser.getTrackFormat(0);

							long duration = format.getLong(MediaFormat.KEY_DURATION);

							if (p >= duration - 1000000) {
								mBeforePts += p;
								mParser.seek(0);
							}

							while (p + mBeforePts < v) {

								mAudioBuffer.position(0);
								int size = mParser.readSampleData(mAudioBuffer, 0);

								BufferInfo auInfo = new BufferInfo();
								auInfo.offset = 0;

								mAudioBuffer.position(auInfo.offset);
								auInfo.set(auInfo.offset, size, p + mBeforePts, auInfo.flags);

								mMuxer.mux(mAuTracknum, mAudioBuffer, auInfo);

								mParser.next();
								p = mParser.getSampleTime();
							}
						}
						mMuxer.mux(mEncoder.get(0).getTrackNum(), buffer, info);
					}
				}
			}
		}

		@Override
		public void onEndOfStream(int tracknum) {
			L.d("Encoder Eos.....");
			mParser.clear();
			mEos = true;
		}
	};

	/**
	 * Encoder의 Data를 모두 사용 한경우
	 * 
	 * @return true : 모두 사용 false : Muxing 되지 않은 data가 남아있음
	 */
	public boolean isEos() {
		return mEos;
	}
}
