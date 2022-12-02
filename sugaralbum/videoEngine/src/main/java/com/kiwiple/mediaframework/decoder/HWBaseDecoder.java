package com.kiwiple.mediaframework.decoder;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.CryptoException;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.MediaType;
import com.kiwiple.mediaframework.demuxer.DemuxerApi;
import com.kiwiple.debug.L;

/**
 * Base Decoder 실제 decoding을 처리하는 내용을 가지고 있음
 */
@SuppressLint("InlinedApi")
public abstract class HWBaseDecoder implements DecoderApi {

	private static int INVALID_INPUT_BUFFER_INDEX = -1;

	/** Decoder Engin */
	protected MediaCodec mCodec;

	/** Decoder InputBuffer */
	ByteBuffer[] mInputBuffer;

	/** Decoder OutputBuffer */
	ByteBuffer[] mOutputBuffer;

	/** Decoder Track number */
	int mTrackIdx;

	/** Decoder Format */
	MediaFormat mFormat;

	/** Decoder Listenr.. */
	protected DecoderListener mDecoderListener;

	/** true: eos false: not eos */
	boolean mIsEos;

	/** Decoder Eos */
	long mInputDataPTS;

	/** MediaType enum 참조 */
	MediaType mType;

	/** media의 시작 시간을 저장함(sync 맞추기 위하여 필요함) */
	long mStartTime;

	/** Dump 할 때 필요함 */
//	FileReadWrite mFile;

	long mEndTime;

	long mOutpueTime;

	/** Decoding 된 data를 Audio, Video에서 각각 처리 되도록 가상 함수로 선언함 */
	protected abstract void do_releaseOutputBuffer(int outputBufferIdx, BufferInfo outputInfo, ByteBuffer buffer);

	/** Decoder destory */
	protected abstract void do_destory();

	/**
	 * Decoder에서 해당 함수로 생성함
	 * 
	 * @param format
	 *            압축 format 내용을 가짐
	 * @param tracIdx
	 *            Parser에서 Track number
	 * @param l
	 *            Decoder listener
	 */
	public HWBaseDecoder(MediaFormat format, int tracIdx, DecoderListener l) {
		mFormat = format;
		mTrackIdx = tracIdx;
		mDecoderListener = l;
		mIsEos = false;
		mInputDataPTS = 0;
		mStartTime = -1;
		mEndTime = -1;
		mCodec = null;
	}

	/**
	 * Decoder에서 해당 함수로 생성함
	 * 
	 * @param format
	 *            압축 format 내용을 가짐
	 * @param tracIdx
	 *            Parser에서 Track number
	 * @param l
	 *            Decoder listener
	 * @param endTime 해당 PTS까지만 디코딩 처리           
	 */
	public HWBaseDecoder(MediaFormat format, int tracIdx, DecoderListener l, long endTime) {

		if (endTime < 0) {
			throw new IllegalArgumentException("endTime must be greater than or equal to 0.");
		}

		mFormat = format;
		mTrackIdx = tracIdx;
		mDecoderListener = l;
		mIsEos = false;
		mInputDataPTS = 0;
		mStartTime = -1;
		mEndTime = endTime;
		mCodec = null;
	}

	/**
	 * 처음 초기화 함수
	 * <p>
	 * 하드웨어 Decoder가 지원되는지 check하는 부분 추가해야됨.
	 * 
	 * @return Decoding 가능한 format인지에 따른 return 값 true: 가능 false: 지원안되는 format
	 */
	@Override
	public boolean init() {

		boolean result = true;
		String mime = mFormat.getString(MediaFormat.KEY_MIME);
		try {
            mCodec = MediaCodec.createDecoderByType(mime);
        } catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
//            VL.e("Decoder init fail : createDecoderByType");
            return false;
        }

		// 화면에 출력되는 부분을 Decoder에서 처리 하지 않아 Surface를 null 변경함
		mCodec.configure(mFormat, null, null, 0);
		mCodec.start();
		mInputBuffer = mCodec.getInputBuffers();
		mOutputBuffer = mCodec.getOutputBuffers();

		L.d("Decoder Input format:" + mFormat.toString());

		for (int i = 0; i < 4; i++) {
			ByteBuffer b = mFormat.getByteBuffer("csd-" + i);
			if (b != null) {
				StringBuffer str = new StringBuffer();
				for (int j = 0; j < b.array().length; j++) {
					str.append(Integer.toHexString(0xFF & b.get(j)) + " ");
				}
				L.w("Decoder csd-" + i + ": " + str);
			} else
				break;
		}

		mBufferQueueObserver.start();
		return result;
	}

	/**
	 * decoder 사용하지 않을시 해당 함수로 종료시킴
	 */
	@Override
	public void destory() {

		mBufferQueueObserver.interrupt();

		if (mCodec != null) {
			mCodec.flush();
			mCodec.stop();
			mCodec.release();
		}
		do_destory();
	}

	/**
	 * Decoder의 Media type을 전달함
	 * 
	 * @return media type
	 */
	@Override
	public MediaType getMediaType() {
		return mType;
	}

	/**
	 * parser에서 stream을 가져와 decoding하는 부분
	 * 
	 * @param demuxer
	 *            DemuxerApi
	 */
	@Override
	public void sampleDecoding(DemuxerApi demuxer) {

		if (mIsEos) {
			return;
		}

		int inputBufferIndex = INVALID_INPUT_BUFFER_INDEX;
		try {
			inputBufferIndex = mCodec.dequeueInputBuffer(-1);
		} catch (IllegalStateException exception) {
			return;
		}

		if (inputBufferIndex >= 0) {

			ByteBuffer destinationBuffer = mInputBuffer[inputBufferIndex];
			int sampleSize = demuxer.readSampleData(destinationBuffer, 0);

			mInputDataPTS = demuxer.getSampleTime();
			
			if (mInputDataPTS > mEndTime || sampleSize < 1) {

				mIsEos = true;
				mCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				return;
			}

			mCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mInputDataPTS, 0);
			
			demuxer.next();
		}
	}

	/**
	 * 현재 재생되는 시간
	 * 
	 * @return 현재 재생되는 시간 (1/1000000초)
	 */
	@Override
	public long lastPts() {
		return mInputDataPTS;
	}

	/**
	 * 디코딩 되고있는 Track의 Index 
	 *  
	 *  @return Track index
	 */
	@Override
	public int getTrackIndex() {
		return mTrackIdx;
	}

	/**
	 * 디코딩 되고 있는 컨텐츠의 Format 정보를 반환
	 *
	 * @return Media Format
	 */
	@Override
	public MediaFormat getFormat() {
		return mFormat;
	}

	/**
	 * 디코딩 되고 있는 컨텐츠의 출력 Format 정보를 반환
	 * 
	 * @param Media Format
	 */
	@Override
	public MediaFormat getOutFormat() {
		return mCodec.getOutputFormat();
	}

	/**
	 * 출력 버퍼의 크기를 반환 
	 * 
	 * @return 출력 버퍼 크기
	 */
	@Override
	public int outputmaxSize() {
		return mOutputBuffer[0].capacity();
	}

	/**
	 * 출력 버퍼의 수를 반환 
	 * 
	 * @return 출력 버퍼 수
	 */
	@Override
	public int getOutputBufferSize() {
		return mOutputBuffer.length;
	}

	/**
	 * 현재 디코딩 되고 있는 pts를 반환 
	 * 
	 * @return 현재 데이터의 PTS
	 */
	@Override
	public long getOutputTime() {
		return mOutpueTime;
	}

	/**
	 * MediaCodec을 처음으로 되돌림 
	 * 
	 */
	@Override
	public void flush() {
		mCodec.flush();
	}

	private final Thread mBufferQueueObserver = new Thread() {

		private final BufferInfo mBufferInfo = new BufferInfo();
		private boolean mIsInterrupted = false;

		@Override
		public void interrupt() {
			mIsInterrupted = true;
			super.interrupt();
		}

		@Override
		public void run() {
			while (!mIsInterrupted) {
				try {
					int outputBufferIndex = mCodec.dequeueOutputBuffer(mBufferInfo, -1);

					switch (outputBufferIndex) {
						case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
							L.d(mType.getToString() + ": INFO_OUTPUT_BUFFERS_CHANGED");
							mOutputBuffer = mCodec.getOutputBuffers();
							continue;

						case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
							L.d(mType.getToString() + ": New format " + mCodec.getOutputFormat());
							continue;

						case MediaCodec.INFO_TRY_AGAIN_LATER:
							L.d(mType.getToString() + ": dequeueOutputBuffer timed out!");
							continue;

						default:

							mOutpueTime = mBufferInfo.presentationTimeUs;

							if (mBufferInfo.size > 0) {
								ByteBuffer buffer = mOutputBuffer[outputBufferIndex];
								do_releaseOutputBuffer(outputBufferIndex, mBufferInfo, buffer);
							}
							if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM || mBufferInfo.size <= 0) {
								mDecoderListener.onEndOfStream(mTrackIdx);
							}
							continue;
					}
				} catch (CryptoException exception) {
					L.e(exception.getMessage());
				} catch (IllegalStateException exception) {
					L.e("IllegalStateException.");
				}
			}
		}
	};
}
