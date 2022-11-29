package com.kiwiple.mediaframework.preview;

import static com.kiwiple.mediaframework.VideoEngineEnvironment.INVALID_INTEGER_VALUE;
import static com.kiwiple.mediaframework.VideoEngineEnvironment.INVALID_LONG_VALUE;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.VideoEngineEnvironment;
import com.kiwiple.mediaframework.VideoEngineException;
import com.kiwiple.debug.L;

public class PreviewVideoDecoder {

	// // // // // Static variable.
	// // // // //
	public static final int[] sPrioritizedColorFormats;

	private static final long TIMEOUT_INDEFINITE = 100;
	private static final long TIMEOUT_IMMEDIATE = 10000;

	// // // // // Member variable.
	// // // // //
	private final PreviewVideoDemuxer mPreviewVideoDemuxer;

	private DecoderThread mDecoderThread;

	private MediaCodec mMediaCodec;
	private MediaFormat mMediaFormat;

	private ByteBuffer[] mInputBuffers;
	private ByteBuffer[] mOutputBuffers;

	private PreviewVideoDecoderListener mDecoderListener;

	private String mCodecName;

	private long mInputTimeUs = INVALID_LONG_VALUE;
	private long mOutputTimeUs = INVALID_LONG_VALUE;
	private long mBlockTimeUs = INVALID_LONG_VALUE;
	private long mSeekTimeUs = INVALID_LONG_VALUE;
	private long mTempBlockTimeUs = INVALID_LONG_VALUE;

	private long mStartTimeUs;
	private long mEndTimeUs;

	private boolean mIsRunning = false;
	private boolean mIsSeeking = false;
	
	private boolean mIsSeekTo = false;	
	private boolean mFocusStateOn = true;
	private boolean mIsRewind =false;
	private long mUpdateIntervalMicro = VideoEngineEnvironment.DEFAULT_UPDATE_INTERVAL_MICROS;
	// // // // // Static method.
	// // // // //
	static {
		sPrioritizedColorFormats = new int[4];
		sPrioritizedColorFormats[0] = MediaCodecColorFormat.YUV420SemiPlanar;
		sPrioritizedColorFormats[1] = MediaCodecColorFormat.YUV420PackedSemiPlanar16m2ka;
		sPrioritizedColorFormats[2] = MediaCodecColorFormat.YUV420PackedSemiPlanar32m;
		sPrioritizedColorFormats[3] = MediaCodecColorFormat.YUV420Planar;
	}

	// // // // // Constructor.
	// // // // //
	public PreviewVideoDecoder(String videoFilePath, PreviewVideoDecoderListener decoderListener) throws IOException {

		try {
			mPreviewVideoDemuxer = new PreviewVideoDemuxer(videoFilePath);
		} catch (IOException exception) {
			throw exception;
		}

		if (decoderListener == null) {
			throw new IllegalArgumentException("decoderListener must not be null.");
		}

		mDecoderListener = decoderListener;
		mMediaFormat = mPreviewVideoDemuxer.getMediaFormat();

		initialize();
	}

	// // // // // Method.
	// // // // //
	private void initialize() {

		String mimeType = mMediaFormat.getString(MediaFormat.KEY_MIME);

		int currentColorFormatPriority = sPrioritizedColorFormats.length;
		int selectedColorFormat = INVALID_INTEGER_VALUE;

		for (int i = 0; i != MediaCodecList.getCodecCount(); ++i) {

			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
			if (codecInfo.isEncoder() || codecInfo.getName().contains("audio")) {
				continue;
			}

			try { // FIXME: YOU must have to FIGURE OUT how to SIMPLIFY thiS.
				for (String type : codecInfo.getSupportedTypes()) {
					if (type.equalsIgnoreCase(mimeType)) {
						for (int supportedColorFormat : codecInfo.getCapabilitiesForType(mimeType).colorFormats) {
							for (int j = 0; j != currentColorFormatPriority; ++j) {
								if (supportedColorFormat == sPrioritizedColorFormats[j]) {
									mCodecName = codecInfo.getName();
									selectedColorFormat = sPrioritizedColorFormats[j];
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

		if (mCodecName == null) {
			throw new UnsupportedMediaTypeException(mimeType + " is unsupported.");
		}

		L.i("mCodecName : " + mCodecName + " : selectedColorFormat : " + selectedColorFormat);
		mDecoderListener.onDecoderInitialized(mMediaFormat);
	}

	public void setVideoClip(long startTimeUs, long endTimeUs) {

		if (startTimeUs < 0 || endTimeUs < 0) {
			throw new IllegalArgumentException("startTimeUs and endTimeUs must be greater than or equal to 0.");
		}
		if (startTimeUs >= endTimeUs) {
			throw new IllegalArgumentException("endTimeUs must be greater than startTimeUs");
		}

		mStartTimeUs = startTimeUs;
		mEndTimeUs = endTimeUs;
	}
	
	public void setPosition(long positionUs) {
		if(!mFocusStateOn) return; 
		
		mIsRewind = false;
		positionUs += mStartTimeUs;
		
		// seek backward or fast forward
		if (positionUs < mBlockTimeUs  
				|| Math.abs(positionUs - mBlockTimeUs) > mUpdateIntervalMicro *20 /*20 frame*/ ) {
			mTempBlockTimeUs = mBlockTimeUs;
			mBlockTimeUs = positionUs;
			seekTo(positionUs);
			//forward, backward 1초 이상 딜레이 체크
			if(mIsSeeking && Math.abs(positionUs - mTempBlockTimeUs)> mUpdateIntervalMicro/* 1frame*/ *30/*1sec*/){
				mIsSeekTo = true;
			}
		} else {   // seek forward 
			mBlockTimeUs = positionUs;
			notifyThreads();
		}
	}

	public void release() {

		stop();
		mPreviewVideoDemuxer.release();

		if (mMediaCodec != null) {
			mMediaCodec.stop();
			mMediaCodec.release();
			mMediaCodec = null;
		}
	}
	
	public void onRewind(){
		if(!mIsRewind){
			mBlockTimeUs = mStartTimeUs;
			seekTo(mStartTimeUs);
			mIsRewind = true;
		}
	}

	public synchronized void prepare() {
		
		if (!mIsRunning) {

			try {
				mMediaCodec = MediaCodec.createByCodecName(mCodecName);
				mMediaCodec.configure(mMediaFormat, null, null, 0);
				mMediaCodec.start();
	
				mInputBuffers = mMediaCodec.getInputBuffers();
				mOutputBuffers = mMediaCodec.getOutputBuffers();
	
				mDecoderThread = new DecoderThread();
				mDecoderThread.start();
			} catch (Exception exception) {
				throw new VideoEngineException(exception.getMessage());
			}
			onRewind();
			//To do  입출력 버퍼 못받아오면 display 안됨<nexus4 known issue>
//			synchronized (this) {
//				try {
//					L.w("start wait.......................................................");
//					long time = System.currentTimeMillis();
//					this.wait();
//					L.w("end wait.......................................................lost time = " + (System.currentTimeMillis() - time));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
		}
	}

	public synchronized void play() {

	}

	public synchronized void stop() {

		if (mIsRunning) {

			mIsSeeking = false;
			mIsRunning = false;

			mBlockTimeUs = mStartTimeUs;
			
			try {
				if (mDecoderThread != null) {
					mDecoderThread.interrupt();
					mDecoderThread = null;
				}
	
				mMediaCodec.stop();
				mMediaCodec.release();
			} catch (IllegalStateException ise) {
				L.w(ise.getMessage());
				mMediaCodec.release();
			} catch (Exception e) {
				L.e(e.getMessage());
			}
			mMediaCodec = null;
		}
	}

	public void onResume(){
		mFocusStateOn = true;
	}
	public void onPause() {
		mFocusStateOn = false;
	}

	private void seekTo(long timeUs) {	
		mSeekTimeUs = timeUs;
		mIsSeeking = true;
		notifyThreads();
	}
	
	public void setUpdateInterval(long timeUs) {
	    mUpdateIntervalMicro = timeUs;
	}

	private void notifyThreads() {
		// null check
	    if (mDecoderThread != null) {
	    	if (mDecoderThread.getState().equals(Thread.State.WAITING)) {
	    		synchronized (mDecoderThread) {
	    			mDecoderThread.notify();
        		}
    	    }
	    }
	}

	// // // // // Inner class.
	// // // // //
	private class DecoderThread extends Thread {

		private final BufferInfo mBufferInfo = new BufferInfo();
		private boolean mIsFirstOut = true;
		private boolean mIsException = false;

		@Override
		public synchronized void start() {
			mIsRunning = true;
			super.start();
		}

		@Override
		public synchronized void run() {

			try {
				while (mIsRunning) {
					try {
						if (mIsSeeking) {

							mOutputTimeUs = INVALID_LONG_VALUE;
							if(!mIsException)
								mMediaCodec.flush();
							if(mIsException) mIsException = false;
							
							mPreviewVideoDemuxer.seekTo(mSeekTimeUs);
							while (mIsSeeking && mOutputTimeUs < mSeekTimeUs) {
								queueSampleData(TIMEOUT_INDEFINITE);
								dequeueDecodedData(TIMEOUT_IMMEDIATE);
								

								if(mIsSeekTo ){
									mIsSeekTo = false;
									mMediaCodec.flush();
									mPreviewVideoDemuxer.seekTo(mSeekTimeUs);
								}
							}
							mSeekTimeUs = 0L;
							mIsSeeking = false;
							mDecoderListener.onSeek();
						}
						queueSampleData(TIMEOUT_IMMEDIATE);
						dequeueDecodedData(TIMEOUT_IMMEDIATE);
					} catch (IllegalStateException exception) {
						exception.printStackTrace();
						L.e("IllegalStateException.");
						sleep(VideoEngineEnvironment.DEFAULT_THREAD_SLEEP_TIME);
						mIsException = true;
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (InterruptedException exception) {
				exception.printStackTrace();
				L.e("InterruptedException");
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void queueSampleData(long timeout) throws InterruptedException, IllegalStateException {

			int inputBufferIndex = mMediaCodec.dequeueInputBuffer(timeout);

			if (inputBufferIndex >= 0) {

				ByteBuffer inputBuffer = mInputBuffers[inputBufferIndex];

				int sampleSize = mPreviewVideoDemuxer.readSampleData(inputBuffer);
				mInputTimeUs = mPreviewVideoDemuxer.getSampleTime();
				
				/**
				 * 샘플 사이즈가 -1이 나올경우 예외처리 파일 끝이거나 
				 * 보통 정상적이지 않은 mp4 마지막 1~2초 전 단에서 발생됨 > 노트 3 Rev 시료 
				 */
				if(sampleSize > 0 ){
					mMediaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mInputTimeUs, 0);
					mPreviewVideoDemuxer.advance();
				}else{
					wait();
				}
			} 
		}

		private void dequeueDecodedData(long timeout) throws InterruptedException, IllegalStateException {

			int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, timeout);
			if (outputBufferIndex >= 0) {

				if (mIsFirstOut) {
					mIsFirstOut = false;
					mDecoderListener.onMeasureBufferSize(mBufferInfo.size);
				}

				mOutputTimeUs = mBufferInfo.presentationTimeUs;
				while (!mIsSeeking && mOutputTimeUs > mBlockTimeUs) {
					mDecoderListener.onDecode(mOutputBuffers[outputBufferIndex], mBufferInfo.presentationTimeUs);
					if(!mFocusStateOn){
						if( (mOutputTimeUs - mBlockTimeUs) > 1000 * 1000){   // 1초 이상이면 onRewind시 seekto 안된걸로 판단 
							mOutputTimeUs = mStartTimeUs;
							mIsRewind = false;
						}
					}
					mDecoderListener.onPrepareDone();
                    mDecoderListener.onWait();
	                wait();
				}
				
				if (!mIsSeeking || (Math.abs(mBlockTimeUs - mOutputTimeUs) < mUpdateIntervalMicro)) {
					mDecoderListener.onDecode(mOutputBuffers[outputBufferIndex], mBufferInfo.presentationTimeUs);
//					mDecoderListener.onPrepareDone();
				}

				mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
			} else {
				handleExceptionOnDequeue(outputBufferIndex);
			}
		}

		private void handleExceptionOnDequeue(int outputBufferInfo) throws InterruptedException {

			switch (outputBufferInfo) {
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					L.d("MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED");
					mOutputBuffers = mMediaCodec.getOutputBuffers();
					//To do  입출력 버퍼 안전하게 받기 
//					synchronized (PreviewVideoDecoder.this) {
//						PreviewVideoDecoder.this.notify();
//					}
					return;

				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					L.d("MediaCodec.INFO_OUTPUT_FORMAT_CHANGED : " + mMediaCodec.getOutputFormat());
					MediaFormat mediaFormat = mMediaCodec.getOutputFormat();
					mDecoderListener.onOutputFormatChanged(mediaFormat);
					return;

				case MediaCodec.INFO_TRY_AGAIN_LATER:
//					L.d("MediaCodec.INFO_TRY_AGAIN_LATER");
//					sleep(VideoEngineEnvironment.DEFAULT_THREAD_SLEEP_TIME);
					return;

				default:
					return;
			}
		}
	}
}
