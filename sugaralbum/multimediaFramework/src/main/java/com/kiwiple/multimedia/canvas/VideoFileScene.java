package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import android.media.MediaFormat;
import android.os.Handler;
import android.text.TextUtils;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.mediaframework.VideoEngineEnvironment;
import com.kiwiple.mediaframework.preview.MediaCodecColorFormat;
import com.kiwiple.mediaframework.preview.PreviewVideoDecoder;
import com.kiwiple.mediaframework.preview.PreviewVideoDecoderListener;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.multimedia.util.VideoUtils;

/**
 * 비디오 파일을 출력하는 클래스.
 * 
 * @see Scene
 */
public final class VideoFileScene extends Scene {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "video_file_scene";

	public static final String JSON_NAME_FILE_PATH = "file_path";
	public static final String JSON_NAME_VIDEO_ID = "video_id";
	public static final String JSON_NAME_VIDEO_START_POSITION = "video_start_position";
	public static final String JSON_NAME_VIDEO_END_POSITION = "video_end_position";
	public static final String JSON_NAME_SCALE_TYPE = "scale_type";
	public static final String JSON_NAME_SLOW_MOTION_RATIO = "slow_motion_ratio";
	public static final String JSON_NAME_IS_VIDEO_MULTILAYER = "is_video_multilayer";
	public static final String JSON_NAME_SLOW_MOTION_CONVERSION_POSITION = "slow_motion_conversion_position";
	public static final String JSON_NAME_SLOW_MOTION_VELOCITY = "slow_motion_velocity";
	public static final String JSON_NAME_IS_FAST_PLAY_MODE = "is_fast_play_mode";

	public static final Change CHANGE_VIDEO_FILE_PATH = new Change();

	public static final int REQ_PREVIEW_REWIND = 1000;
	public static final int REQ_PREVIEW_INVALIDATE = 2000;

	public static final float DEFAULT_SLOW_MOTION_RATIO = 1.0f;
	
	private boolean mIsSlowMotionEnable = false;
	private int mSpeedConversionPosition = Integer.MAX_VALUE;
	private float mSlowMotionRatio = DEFAULT_SLOW_MOTION_RATIO;
	private int mSlowMotionRatioDuration;
	private int mSlowMotionVelocity = 1;
	private boolean mIsFastPlayMode = false;

	// // // // // Member variable.
	// // // // //
	private PreviewVideoDecoder mVideoDecoder;

	@CacheCode
	private FilterApplier mFilterApplier = FilterApplier.INVALID_OBJECT;

	@CacheCode
	private String mFilePath;
	private int mRotation;

	private long mVideoStartPositionMs;
	private long mVideoEndPositionMs;
	private CountDownLatch mCountDownLatch = null;
	private boolean mIsFirstFocusOn = true;
	private Handler mPrepareHandler = null;
	private boolean mPrepareDone = false;

	private int mVideoId;
	private boolean mIsVideoMultiLayer = false;

	private long mLastPositionUS = Long.MAX_VALUE;

	private BufferHolder mBufferHolder = new BufferHolder();

	// // // // // Constructor.
	// // // // //
	VideoFileScene(Region parent) {
		super(parent);
	}

	VideoFileScene(MultiLayerScene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {
		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_FILE_PATH, mFilePath);
		jsonObject.put(JSON_NAME_VIDEO_ID, mVideoId);
		jsonObject.put(JSON_NAME_VIDEO_START_POSITION, mVideoStartPositionMs);
		jsonObject.put(JSON_NAME_VIDEO_END_POSITION, mVideoEndPositionMs);
		jsonObject.put(JSON_NAME_SLOW_MOTION_RATIO, mSlowMotionRatio);
		jsonObject.putOpt(JSON_NAME_FILTER_ID, mFilterApplier);
		jsonObject.put(JSON_NAME_IS_VIDEO_MULTILAYER, mIsVideoMultiLayer);
		jsonObject.put(JSON_NAME_SLOW_MOTION_CONVERSION_POSITION, mSpeedConversionPosition);
		jsonObject.put(JSON_NAME_SLOW_MOTION_VELOCITY, mSlowMotionVelocity);
		jsonObject.put(JSON_NAME_IS_FAST_PLAY_MODE, mIsFastPlayMode);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException, IllegalStateException {
		super.injectJsonObject(jsonObject);

		mVideoId = jsonObject.optInt(JSON_NAME_VIDEO_ID);

		String videoFilePath = jsonObject.getString(JSON_NAME_FILE_PATH);
		setVideoFilePath(videoFilePath);

		int videoStartPositionMs = jsonObject.getInt(JSON_NAME_VIDEO_START_POSITION);
		int videoEndPositionMs = jsonObject.getInt(JSON_NAME_VIDEO_END_POSITION);
		setVideoClip(videoStartPositionMs, videoEndPositionMs);

		if (!jsonObject.isNull(JSON_NAME_SLOW_MOTION_RATIO)) {
			mSlowMotionRatio = jsonObject.getFloat(JSON_NAME_SLOW_MOTION_RATIO);
		} 
		
		if (!jsonObject.isNull(JSON_NAME_SLOW_MOTION_VELOCITY)) 
			mSlowMotionVelocity = jsonObject.getInt(JSON_NAME_SLOW_MOTION_VELOCITY);
		
		if(mSlowMotionVelocity != DEFAULT_SLOW_MOTION_RATIO){
			mIsSlowMotionEnable = true;
		}else{
			mIsSlowMotionEnable = false;
		}
		
		if(!jsonObject.isNull(JSON_NAME_IS_VIDEO_MULTILAYER)){
			mIsVideoMultiLayer = jsonObject.getBoolean(JSON_NAME_IS_VIDEO_MULTILAYER);
		}

		if(!jsonObject.isNull(JSON_NAME_IS_FAST_PLAY_MODE)){
			mIsFastPlayMode = jsonObject.getBoolean(JSON_NAME_IS_FAST_PLAY_MODE);
		}

		if(mIsSlowMotionEnable){
			mSlowMotionRatioDuration = (int) (mSlowMotionRatio * 1000);
			if(mIsFastPlayMode){
				setDuration((int) ((videoEndPositionMs - videoStartPositionMs) - (mSlowMotionRatioDuration / mSlowMotionVelocity)) );
			}else{
				setDuration((int) ((videoEndPositionMs - videoStartPositionMs) + mSlowMotionRatioDuration * (mSlowMotionVelocity - 1)));
			}
			mSpeedConversionPosition = jsonObject.getInt(JSON_NAME_SLOW_MOTION_CONVERSION_POSITION);
		}else{
			setDuration((int) (videoEndPositionMs - videoStartPositionMs));
		}
		
		L.i("velocity : "+mSlowMotionVelocity +", motion active : "+ mIsSlowMotionEnable +", speedConversionPosition : "+ mSpeedConversionPosition +", ratio : " + mSlowMotionRatio);
		setFilterId(jsonObject.optInt(JSON_NAME_FILTER_ID, FilterApplier.INVALID_FILTER_ID));
	}

	@Override
	Change[] getSensitivities() {
		return new Change[] { Change.SIZE };
	}
	
	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		checkValidity(mFilePath != null, "You must invoke setVideoFilePath().");
		checkValidity(mVideoStartPositionMs != mVideoEndPositionMs, "You must invoke setVideoClip().");

		if (changes.contains(CHANGE_VIDEO_FILE_PATH)) {

			if (mVideoDecoder != null)
				mVideoDecoder.release();
			try {
				mVideoDecoder = createVideoDecoder();
				mVideoDecoder.prepare();
			} catch (Exception exception) {
				exception.printStackTrace();

				if (mVideoDecoder != null)
					mVideoDecoder.release();
				checkValidity(false, "failed to create and prepare VideoDecoder.");
			}
		}
		if (changes.contains(Change.SIZE)) {
			doChangeCanvasInfo();	
		}
		mVideoDecoder.setVideoClip(mVideoStartPositionMs * 1000L, mVideoEndPositionMs * 1000L);
	}

	//무비다이어리  저장시 HD/ FHD로 저장할 경우 Canvas size 변경으로 resize W/H, 버퍼 사이즈를 갱신 해야한다
	private void doChangeCanvasInfo() {
		int width = getWidth();
		int height = getHeight();

		mResizeWidth = width;
		mResizeHeight = height;

		//예외처리 단말일 경우만 해당
		if (!TextUtils.isEmpty(mDeviceName) && mDeviceName.contains(VideoEngineEnvironment.G_FLEX2) || mDeviceName.contains(VideoEngineEnvironment.G_500L)) {
	
			if (mRotation == 90 || mRotation == 270) {
				if (mSrcWidth > mSrcHeight) {
					mResizeWidth = (int) (mSrcWidth * (width / (float) mSrcHeight));
					mResizeHeight = width;
				}
			} else {
				if (mSrcHeight > mSrcWidth) {
					mResizeWidth = width;
					mResizeHeight = (int) (mSrcHeight * (width / (float) mSrcWidth));
				}
			}
		}else{
	
			if (mRotation == 90 || mRotation == 270) {
				if (mSrcWidth > mSrcHeight) {
					mResizeWidth = (int) (mSrcWidth * (width / (float) mSrcHeight));
					mResizeHeight = width;
				}
			} else {
				if (mSrcHeight > mSrcWidth) {
					mResizeWidth = width;
					mResizeHeight = (int) (mSrcHeight * (width / (float) mSrcWidth));
				}
			}
		}
		
		L.i("mIsVideoMultiLayer : " + mIsVideoMultiLayer);
		if (mRotation == 0 && mSrcHeight > mSrcWidth && !mIsVideoMultiLayer) {
			mResizeHeight += 16 - (mResizeHeight % 16);
		} else {
			if (mResizeWidth * mResizeHeight > width * height && !mIsVideoMultiLayer) {
				mResizeWidth = width;
				mResizeHeight = height;
			}
			if(mIsVideoMultiLayer){
				mResizeHeight += 16 - (mResizeHeight % 16);
			}
		}

		mBufferSize = mSrcWidth * mSrcHeight > mResizeWidth * mResizeHeight ? mSrcWidth * mSrcHeight : mResizeWidth * mResizeHeight;
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { getSize() };
	}

	
	@Override
	void onDraw(PixelCanvas dstCanvas) {
		int position = getPosition();

		
		if(mIsSlowMotionEnable){
			if(mIsFastPlayMode){
				L.i("mIsFastPlayMode = "+ mIsFastPlayMode +", position = "+ position +", start : "+ mSpeedConversionPosition +",  end : "+ (mSpeedConversionPosition + mSlowMotionRatioDuration / mSlowMotionVelocity ));
				//앞으로 빠르게 재생
				if(position > mSpeedConversionPosition && position <= mSpeedConversionPosition + mSlowMotionRatioDuration / mSlowMotionVelocity){
					position += (position - mSpeedConversionPosition) ;
				}else if(position > mSpeedConversionPosition + mSlowMotionRatioDuration / mSlowMotionVelocity){
					//20151129 : fast mode로  줄어든 시간만큼 늘려준다
					position += mSlowMotionRatioDuration / mSlowMotionVelocity;
				}
				
			}else{
				//느리게 재생  
				L.i("mIsFastPlayMode = "+ mIsFastPlayMode +", position = "+ position +", start : "+ mSpeedConversionPosition +",  end : "+ (mSpeedConversionPosition + mSlowMotionRatioDuration * mSlowMotionVelocity ));
				if(position > mSpeedConversionPosition && position <= mSpeedConversionPosition + mSlowMotionRatioDuration * mSlowMotionVelocity){
					//below 2x
					if(mSlowMotionVelocity < 3){
						position = (int) (position / mSlowMotionVelocity + mSpeedConversionPosition/ mSlowMotionVelocity);
					}else{
						//over 2x
						position = (int) (mSpeedConversionPosition + (position - mSpeedConversionPosition) / mSlowMotionVelocity);
					}
					
				}else if(position > mSpeedConversionPosition + mSlowMotionRatioDuration * mSlowMotionVelocity){
					//20151129 : slowmotion으로 늘어난 시간만큼 제거해 준다
					position -= mSlowMotionRatioDuration * (mSlowMotionVelocity -1);
				}
			}
		}
		
		if (Math.abs(position - mLastPositionUS) > 100) {
			
			mLastPositionUS = position;

			mCountDownLatch = new CountDownLatch(1);

			try {
				mVideoDecoder.setPosition(position * 1000L);
				if (mPrepareHandler == null){

                    mCountDownLatch.await(mIsFirstFocusOn ? 3500 : 150, TimeUnit.MILLISECONDS);

				}
				// 구간 편집일 경우 싱크 보장을 위해 wait
				if (mPrepareHandler != null)
					mCountDownLatch.await(1000, TimeUnit.MILLISECONDS);

			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
		}

		PixelCanvas pixelCanvas = getCanvas(0);
		synchronized (pixelCanvas) {
			System.arraycopy(pixelCanvas.intArray, 0, dstCanvas.intArray, 0, (pixelCanvas.intArray.length > dstCanvas.intArray.length ? dstCanvas.intArray.length : pixelCanvas.intArray.length));
		}

		mIsFirstFocusOn = false;
		// rewind로 해당 position의 이미지 버퍼를 채운 뒤 surfaceview update
		if (mPrepareHandler != null && mPrepareDone) {
			mPrepareDone = false;
			mPrepareHandler.sendEmptyMessage(REQ_PREVIEW_INVALIDATE);
			mPrepareHandler = null;
		}
	}

	/**
	 * 비디오 구간 설정으로 부터 전달받은 핸들러.
	 */
	public void onPrepareVideo(Handler handler) {
		mPrepareHandler = handler;
	}

	void prepare() {
		if (mVideoDecoder != null)
			mVideoDecoder.prepare();
	}

	@Override
	void onPrepare() {

		if (mCountDownLatch == null) {
			mCountDownLatch = new CountDownLatch(1);
		}
		if (getFocusState().equals(FocusState.OFF)) {
			mVideoDecoder.onResume();
			mVideoDecoder.prepare();
		}
		mVideoDecoder.play();
		mVideoDecoder.setUpdateInterval(100 * 1000);
	}

	@Override
	void onUnprepare() {

		mLastPositionUS = Long.MAX_VALUE;
		if (mBufferHolder != null) {
			mBufferHolder.release();
		}
		mCountDownLatch = null;
		mIsFirstFocusOn = true;
		mVideoDecoder.onPause();
		mVideoDecoder.onRewind();
	}

	@Override
	void onRelease() {

		if (mVideoDecoder != null) {
			mVideoDecoder.release();
			mVideoDecoder = null;
		}
		if (mBufferHolder != null) {
			mBufferHolder.release();
		}
	}

	void setVideoFilePath(String videoFilePath) {
		Precondition.checkFile(videoFilePath).checkExist();

		if (!videoFilePath.equalsIgnoreCase(mFilePath)) {
			mFilePath = videoFilePath;
			mRotation = VideoUtils.getVideoRotation(mFilePath);

			notifyChange(CHANGE_VIDEO_FILE_PATH);
		}
	}

	public String getVideoFilePath() {
		return mFilePath;
	}

	public boolean getSlowMotionEnable(){
		return mIsSlowMotionEnable;
	}

	public float getSlowMotionRatio() {
		return mSlowMotionRatio;
	}

	void setVideoClip(long startPositionMs, long endPositionMs) {
		if (startPositionMs < 0 || endPositionMs < 0) {
			throw new IllegalArgumentException("startPosition and endPosition must be greater than 0.");
		}
		if (startPositionMs >= endPositionMs) {
			throw new IllegalArgumentException("startPosition must be less than endPosition.");
		}
		mLastPositionUS = Long.MAX_VALUE;
		mVideoStartPositionMs = startPositionMs;
		mVideoEndPositionMs = endPositionMs;
	}

	public long getVideoStartPosition() {
		return mVideoStartPositionMs;
	}

	public long getVideoEndPosition() {
		return mVideoEndPositionMs;
	}

	void setFilterId(int filterId) {
		mFilterApplier = createFilterApplier(filterId);
	}

	/**
	 * 이미지를 그릴 때 적용하는 필터의 식별자를 반환합니다.
	 */
	public int getFilterId() {
		return mFilterApplier.getFilterId();
	}

	void setVideoId(int videoId) {
		mVideoId = videoId;
	}

	public int getVideoId() {
		return mVideoId;
	}
	
	void setIsVideoMultiLayer(boolean isVideoMultiLayer) {
	    mIsVideoMultiLayer = isVideoMultiLayer;
	}
	
	void setDurationForSlowMotion(int duration){
		setDuration(duration);
		if(mIsSlowMotionEnable){
			mSlowMotionRatioDuration = (int) (mSlowMotionRatio * 1000);
		}
	}
	
	void setFastPlayMode(boolean isFastMode) {
		mIsFastPlayMode = isFastMode;
	}
	
	public boolean getFastPlayMode(){
		return mIsFastPlayMode;
	}

	void setCoversionPosition(int position){
		if(mIsSlowMotionEnable){
			mSpeedConversionPosition = position;
		}
	}
	
	void setSlowMotionVelocity(int velocity) {
		mSlowMotionVelocity = velocity;
	}
	
	void setSlowMotionRatio(float ratio) {
		mSlowMotionRatio = ratio;
		if(mSlowMotionVelocity != DEFAULT_SLOW_MOTION_RATIO){
			mIsSlowMotionEnable = true;
		}else{
			mSpeedConversionPosition = Integer.MAX_VALUE;
			mIsSlowMotionEnable = false;
		}
	}
	
	public boolean getIsVideoMuitiLayer() {
	    return mIsVideoMultiLayer;
	}

	private static class BufferHolder {
		private byte[] mTempBuffer;
		private PixelCanvas mTempCanvas1;
		private PixelCanvas mTempCanvas2;

		public synchronized byte[] getByteBuffer(int size) {
			if (mTempBuffer == null || mTempBuffer.length != size) {
				mTempBuffer = new byte[size];
			}
			return mTempBuffer;
		}

		public synchronized PixelCanvas getCanvas1(int size) {
			if (mTempCanvas1 == null || mTempCanvas1.getCapacity() != size) {
				mTempCanvas1 = new PixelCanvas(size);
			}
			return mTempCanvas1;
		}

		public synchronized PixelCanvas getCanvas2(int size) {
			if (mTempCanvas2 == null || mTempCanvas2.getCapacity() != size) {
				mTempCanvas2 = new PixelCanvas(size);
			}
			return mTempCanvas2;
		}

		public synchronized void release() {
			mTempBuffer = null;
			mTempCanvas1 = null;
			mTempCanvas2 = null;
		}
	}

	private int mSrcWidth;
	private int mSrcHeight;
	private int mResizeWidth;
	private int mResizeHeight;
	private String mDeviceName;
	private int mBufferSize;

	private PreviewVideoDecoder createVideoDecoder() throws IOException {
		PreviewVideoDecoderListener videoDecoderListener = new PreviewVideoDecoderListener() {

			// TODO: 20150213 olive : #10613 buffer 개수, 크기 줄일 수 있는 방안 검토 필요

			private int mSrcSliceHeight;
			private int mSrcStride;

			private int mColorFormat;

			private boolean mIsUsingSliceHeight = false;


			@Override
			public void onDecoderInitialized(MediaFormat mediaFormat) {
				mDeviceName = android.os.Build.MODEL;
				L.i("mediaFormat : " + mediaFormat + ", model : " + mDeviceName);

				int width = getWidth();
				int height = getHeight();

				mSrcWidth = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
				mSrcHeight = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);

				mResizeWidth = width;
				mResizeHeight = height;

				// 세로로 긴 동영상일 경우
				if (mRotation == 90 || mRotation == 270) {
					if (mSrcWidth > mSrcHeight) {
						mResizeWidth = (int) (mSrcWidth * (width / (float) mSrcHeight));
						mResizeHeight = width;
					}
				} else {
					if (mSrcHeight > mSrcWidth) {
						mResizeWidth = width;
						mResizeHeight = (int) (mSrcHeight * (width / (float) mSrcWidth));
					}
				}
				mLastPositionUS = Long.MAX_VALUE;
			}

			@Override
			public void onOutputFormatChanged(MediaFormat mediaFormat) {
				L.i("mediaFormat : " + mediaFormat +", mIsVideoMultiLayer : "+ mIsVideoMultiLayer);

				int width = getWidth();
				int height = getHeight();

				mColorFormat = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
				mSrcStride = mediaFormat.getInteger("stride");
				mSrcSliceHeight = mediaFormat.getInteger("slice-height");

				// G-Flex2 단말에 대해서만 적용
				if (mDeviceName != null && !mDeviceName.equals("")) {
					if (mDeviceName.contains(VideoEngineEnvironment.G_FLEX2) || mDeviceName.contains(VideoEngineEnvironment.G_500L)) {
						mSrcWidth = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
						mSrcHeight = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);

						// jhshin src width와 height는 디코딩시 코덱으로 부터 받은 mediaFormat정보로 설정
						// 세로로 긴 동영상일 경우
						if (mRotation == 90 || mRotation == 270) {
							if (mSrcWidth > mSrcHeight) {
								mResizeWidth = (int) (mSrcWidth * (width / (float) mSrcHeight));
								mResizeHeight = width;
							}
						} else {
							if (mSrcHeight > mSrcWidth) {
								mResizeWidth = width;
								mResizeHeight = (int) (mSrcHeight * (width / (float) mSrcWidth));
							}
						}
					}
				}

				// up scale은 하지 않는다.
				// 세로 동영상이고, Rotate가 0인 영상에 대해서 up scale시 width, height 사이즈가 16배수가 아닌 경우 코덱에 따라서 영상이
				// 변색되어 보이는 문제가 생기기 때문에 특정 동영상에 대한 예외처
				if (mRotation == 0 && mSrcHeight > mSrcWidth && !mIsVideoMultiLayer) {
					mResizeHeight += 16 - (mResizeHeight % 16);
				} else {
					// VideoFileScene이 싱글씬으로 사용될 때에만 up scale은 하지 않는다.
					if (mResizeWidth * mResizeHeight > width * height && !mIsVideoMultiLayer) {
						mResizeWidth = width;
						mResizeHeight = height;
					}
					if(mIsVideoMultiLayer){
						mResizeHeight += 16 - (mResizeHeight % 16);
					}
				}

				mBufferSize = mSrcWidth * mSrcHeight > mResizeWidth * mResizeHeight ? mSrcWidth * mSrcHeight : mResizeWidth * mResizeHeight;
			}

			@Override
			public void onMeasureBufferSize(int bufferSize) {

				if (bufferSize > Math.round(mSrcStride * mSrcHeight * 1.5f)) {
					mIsUsingSliceHeight = true;
				}
				L.d("bufferSize : " + bufferSize + " : mIsUsingSliceHeight : " + mIsUsingSliceHeight);
			}

			@Override
			public void onDecode(ByteBuffer yuvPixels, long positionUs) {

				if (getFocusState().equals(FocusState.OFF)) {
					return;
				}

				byte[] tempBuffer = mBufferHolder.getByteBuffer(mBufferSize * 4);
				PixelCanvas canvas1 = mBufferHolder.getCanvas1(mBufferSize);
				PixelCanvas canvas2 = mBufferHolder.getCanvas2(mBufferSize);

				//fixes #12400 : mSrcSliceHeight가 존재할 경우, mSrcSliceHeight에 맞게 리사이징을 해야함.
				
				PixelUtils.resizeNearestNeighborYUV(yuvPixels, tempBuffer, mSrcWidth, mSrcHeight, mIsUsingSliceHeight ? mSrcSliceHeight : mSrcHeight, mSrcStride, mResizeWidth, mResizeHeight);

				if (getFocusState().equals(FocusState.OFF)) {
					return;
				}
				try {
					PixelCanvas useCanvas = canvas1;
					switch (mColorFormat) {
						case MediaCodecColorFormat.YUV420SemiPlanar:
						case MediaCodecColorFormat.YUV420PackedSemiPlanar16m2ka:
						case MediaCodecColorFormat.YUV420PackedSemiPlanar32m:
							PixelUtils.convertYuv420spToArgb(tempBuffer, useCanvas, mResizeWidth, mResizeHeight);
							break;
						case MediaCodecColorFormat.YUV420Planar:
							PixelUtils.convertYuv420pToArgb(tempBuffer, useCanvas, mResizeWidth, mResizeHeight);
							break;
						default:
							break;
					}
					
					int width = getWidth();
					int height = getHeight();
					int lastWidth = mResizeWidth;
					int lastHeight = mResizeHeight;

					if (mRotation == 90 || mRotation == 270) {
						PixelUtils.rotate(useCanvas, canvas2, mResizeWidth, mResizeHeight, mRotation);
						lastWidth = mResizeHeight;
						lastHeight = mResizeWidth;
						useCanvas = canvas2;
					} else if (mRotation == 180) {
						PixelUtils.rotate(useCanvas, canvas2, mResizeWidth, mResizeHeight, mRotation);
						useCanvas = canvas2;
					}
					

					// 세로로 긴 사진은 crop 한다.
					if (lastHeight > lastWidth) {
						PixelCanvas nextCanvas = useCanvas == canvas1 ? canvas2 : canvas1;
						int croppedHeight = (int) Math.ceil((lastWidth * (height / (float) width)));
						PixelUtils.crop(useCanvas, nextCanvas, lastWidth, lastHeight,
								// left
								0,
								// top
								(lastHeight - croppedHeight) / 2,
								// right
								lastWidth,
								// bottom
								(lastHeight + croppedHeight) / 2);
						lastHeight = croppedHeight;
						useCanvas = nextCanvas;
					} 
					
					if (mFilterApplier.isVaild()) {
						PixelCanvas nextCanvas = useCanvas == canvas1 ? canvas2 : canvas1;
						mFilterApplier.apply(useCanvas.intArray, nextCanvas.intArray, lastWidth, lastHeight);
						useCanvas = nextCanvas;
					}

					if (lastWidth != width || lastHeight != height) {
						PixelCanvas nextCanvas = useCanvas == canvas1 ? canvas2 : canvas1;
						float xScale = width / (float) lastWidth;
						float yScale = height / (float) lastHeight;
						useCanvas.setImageSize(lastWidth, lastHeight);
						nextCanvas.setImageSize(width, height);
						PixelUtils.resizeBilinear(useCanvas, nextCanvas, width, height, 0, 0, xScale < yScale ? yScale : xScale);
						useCanvas = nextCanvas;
					}

					if (getFocusState() != FocusState.OFF) {
						PixelCanvas pixelCanvas = getCanvas(0);
						synchronized (pixelCanvas) {
							System.arraycopy(useCanvas.intArray, 0, pixelCanvas.intArray, 0, (useCanvas.intArray.length > pixelCanvas.intArray.length ? pixelCanvas.intArray.length : useCanvas.intArray.length));
						}
					}
				} catch (Exception e) {
					// timing issue, buffer is null
					e.printStackTrace();
				}
			}

			@Override
			public void onSeek() {
				// Do nothing.
			}

			@Override
			public void onWait() {
				if (mCountDownLatch != null) {
					mCountDownLatch.countDown();
				}
			}

			/**
			 * 비디오 편집 화면에서 해당 비디오의 prepare 상태를 전달하기 위한 메소드
			 */
			@Override
			public void onPrepareDone() {
				if (mPrepareHandler != null && !mPrepareDone) {
					mPrepareHandler.sendEmptyMessageDelayed(REQ_PREVIEW_REWIND, 30);
				}
				mPrepareDone = true;
			}
		};
		return new PreviewVideoDecoder(mFilePath, videoDecoderListener);
	}

	// // // // // Interface.
	// // // // //
	public interface VideoSceneListener {

		public abstract void onStart();

		public abstract void onStop();

		public abstract void onPosition(int positionMs);

		public abstract void onRelease();
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link VideoFileScene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<VideoFileScene, Editor> {

		private Editor(VideoFileScene videoScene) {
			super(videoScene);
		}

		public Editor setVideoFilePath(String videoFilePath) {
			getObject().setVideoFilePath(videoFilePath);
			return this;
		}

		public Editor setVideoClip(long videoStartPositionMs, long videoEndPositionMs) {
			getObject().setVideoClip(videoStartPositionMs, videoEndPositionMs);
			return this;
		}

		/**
		 * 이미지를 그릴 때 적용할 필터의 식별자를 설정합니다.
		 * 
		 * @param filterId
		 *            적용할 필터의 식별자.
		 */
		public Editor setFilterId(int filterId) {
			getObject().setFilterId(filterId);
			return this;
		}

		public Editor setVideoId(int videoId) {
			getObject().setVideoId(videoId);
			return this;
		}

		public Editor setUpdateHandler(Handler handler) {
			getObject().onPrepareVideo(handler);
			return this;
		}

		public Editor prepare() {
			getObject().prepare();
			return this;
		}

		public Editor setSlowMotionRatio(float speed) {
			getObject().setSlowMotionRatio(speed);
			return this;
		}

		public Editor setSlowMotionVelocity(int velocity) {
			getObject().setSlowMotionVelocity(velocity);
			return this;
		}

		public Editor setDuration(int duration) {
			getObject().setDuration(duration);
			return this;
		}
		
		public Editor setIsVideoMultiLayer(boolean isVideoMultiLayer) {
		    getObject().setIsVideoMultiLayer(isVideoMultiLayer);
		    return this;
		}
		
		public Editor setDurationForSlowMotion(int duration){
			getObject().setDurationForSlowMotion(duration);
			return this;
		}

		public Editor setCoversionPosition(int position){
			getObject().setCoversionPosition(position);
			return this;
		}
		public Editor setFastPlayMode(boolean isFastMode){
			getObject().setFastPlayMode(isFastMode);
			return this;
		}
	}


}
