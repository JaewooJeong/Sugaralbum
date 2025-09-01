package com.kiwiple.mediaframework.encoder;

import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

import com.kiwiple.mediaframework.MediaType;
import com.kiwiple.debug.L;

/**
 * H/W Encoder
 * <p>
 * H/W Encoding 하는 내용을 가짐
 */
@SuppressLint("InlinedApi")
public abstract class HWBaseEncoder implements EncoderApi{

	/** Encoder Instance */
	protected MediaCodec mCodec;
	/** Encoder InputBuffer */
	private ByteBuffer[] mInputBuffer;
	/** Encoder OutputBuffer */
	private ByteBuffer[] mOutputBuffer;

	/** Encoder Format */
	protected MediaFormat mFormat;
	/** Encoder Listener */
	protected EncoderListener mEncoderListener;
	/** Media Type */
	protected MediaType mType;

	/** Muxer 기준 Track Number */
	protected int mTrackNum;
	
	/** Encoder가 여러개인경우 구분하기 위한 값  */
	protected int mId;
	
	protected	boolean		mIsEos;
	
	/**
	 * 외부에서 들어오는 raw data size
	 */
	protected int mInputDataSize;
	
	/**
	 * BaseEncoder Class를 상속 받은 Classs에서 Destory 될때의 내용을 가짐
	 */
	public abstract void do_destory();

	/**
	 * Init 할 때 사용
	 * @return Init 성공 여부
	 */
	public abstract boolean do_init();

	/**
	 * encoding 된 data를 상속받은 Class에서 data를 처리 할 때 사용
	 * @param buffer   Encoding 된 data
	 * @param info     buffer info
	 */
	public abstract void do_releaseOutputBuffer(ByteBuffer buffer,
			BufferInfo info);

	/**
	 * Constructure
	 * @param tracknum  Track number
	 * @param listener  Encoding listner(결과 내용은 pipeline에서 사용)
	 */
	public HWBaseEncoder(EncoderListener listener, int id) {
		mId = id;
		mTrackNum = -1;
		mFormat = null;
		mEncoderListener = listener;
		mInputDataSize = 0;
	}

	/**
	 * encoding 준비 단계
	 * @return true: init Success false: init fail
	 */
	@Override
	public boolean init() {
		clear();
		mIsEos = false;
		if (!do_init())
			return false;

		mCodec.start();
		mInputBuffer = mCodec.getInputBuffers();
		mOutputBuffer = mCodec.getOutputBuffers();
		if(mType == MediaType.Video && (mInputBuffer[0].capacity() < mInputDataSize))
		{
			L.d("Encoder || InpuBuffer Size:" + mInputBuffer[0].capacity() + " || inputDataSize:" + mInputDataSize);
			L.d("Encoder Input buffer size error");
			return false;
		}

        L.d("Encoder Input format:" + mFormat.toString());
        for (int i = 0; i < 4; i++) {
        	ByteBuffer	b = mFormat.getByteBuffer("csd-"+i);
			if( b != null)
			{
				StringBuffer	str = new StringBuffer();
				for (int j = 0; j < b.array().length; j++) {
					str.append(Integer.toHexString(0xFF&b.get(j)) +" ");
				}
				L.d("Encoder csd-" + i +": " +str);
			}
			else
				break;
		}
        

		return true;
	}

	/**
	 * Encoder 초기화
	 */
	protected void clear() {
		if (mCodec != null) {
			mCodec.stop();
			mCodec.release();
			mCodec = null;
		}
	}

	/**
	 * Encoder destory
	 */
	@Override
	public void destory() {
		clear();
		do_destory();
	}

	/**
	 * Encoding...
	 * @param data   row data
	 * @param info   data information
	 */
	@Override
	public void sampleEncoding(ByteBuffer data, BufferInfo info) {
		
		// queueBuffer에 남아있는 data를 가져오기위한 Source
		if(mIsEos)
		{
			outputData();
			return ;
		}

		// Android 15+ compatibility: Use timeout for dequeueInputBuffer
		int timeout = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM ? 10000 : -1;
		int inIndex = mCodec.dequeueInputBuffer(timeout);

		if (inIndex >= 0) {
			if (info.size > 0) {
				ByteBuffer inputBuffer = mInputBuffer[inIndex];
				inputBuffer.clear();
				inputBuffer.rewind();
				inputBuffer.put(data);
				mCodec.queueInputBuffer(inIndex, 0, info.size,
						info.presentationTimeUs, 0);
			} else {
				mCodec.queueInputBuffer(inIndex, 0, 0, info.presentationTimeUs,
						MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				mIsEos = true;
			}
		}
		else
		{
			L.d("get not InputBuffer idx:" + inIndex);
			
			// Android 15+ recovery mechanism
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM && inIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
				L.w("Android 15: MediaCodec buffer queue full, attempting recovery");
				try {
					Thread.sleep(5); // Brief pause to allow buffer processing
					outputData(); // Force output processing to clear buffers
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		outputData();
	}

	/**
	 * get Track number
	 * @return track number
	 */
	@Override
	public int getTrackNum() {
		return mTrackNum;
	}
	
	/**
	 * Track의 Number를 성정 
	 * @param tracknum track number
	 */
	@Override
	public void setTrackNum(int tracknum)
	{
		mTrackNum = tracknum;
	}

	/**
	 * get Media Type
	 * @return Media Type
	 */
	@Override
	public MediaType getMediaType() {
		return mType;
	}

	/**
	 * encoding 된 data 가져오기
	 * <p>
	 * 결과 data는 do_releaseOutputBuffer()로 전달
	 */
	private void outputData() {
		BufferInfo info = new BufferInfo();
		int outIndex = mCodec.dequeueOutputBuffer(info, 0);

		switch (outIndex) {
		case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
			L.d(mType.getToString()	+ ": INFO_OUTPUT_BUFFERS_CHANGED");
			mOutputBuffer = mCodec.getOutputBuffers();
			break;
		case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
			L.d(mType.getToString() + ": New format "			+ mCodec.getOutputFormat());
			break;
		case MediaCodec.INFO_TRY_AGAIN_LATER:
			L.d(mType.getToString()	+ ": dequeueOutputBuffer timed out!");
			
			try {
				Thread.sleep(5);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			if (outIndex >= 0) {
				ByteBuffer outputBuffer = mOutputBuffer[outIndex];

				outputBuffer.position(info.offset);

				if(info.size > 0)
					do_releaseOutputBuffer(outputBuffer, info);

				if(info.size < 1 || info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM)
					mEncoderListener.onEndOfStream(mId);

				
				mCodec.releaseOutputBuffer(outIndex, false);
			}
			break;
		}

	}
	
	/**
	 * 입력 버퍼의 MediaFormat을 얻어오기 위한 메서드
	 * @return Media Format
	 */
	@Override
	public MediaFormat getMediaFormat() {
		return mFormat;
	}
	
	/**
	 * 출력 버퍼의 MediaFormat을 얻어오기 위한 메서드
	 * @return Media Format
	 */	
	@Override
	public MediaFormat getOutputMediaFormat() {
        return mCodec.getOutputFormat();
	}

}
