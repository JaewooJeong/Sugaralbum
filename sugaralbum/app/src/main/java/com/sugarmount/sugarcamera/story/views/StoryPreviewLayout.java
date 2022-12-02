package com.sugarmount.sugarcamera.story.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

//import com.kiwiple.imageframework.util.SmartLog;
import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.exception.InvalidFileException;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.kiwiple.multimedia.preview.PreviewManager.OnPrepareListener;
import com.kiwiple.multimedia.preview.PreviewManager.OnProgressUpdateListener;
import com.kiwiple.multimedia.preview.PreviewManager.OnUpdateListener;
import com.kiwiple.multimedia.preview.PreviewManager.PreviewController;
import com.kiwiple.multimedia.preview.PreviewSurfaceView;
import com.sugarmount.sugaralbum.R;
import com.sugarmount.sugarcamera.story.movie.MovieEditMainActivity;
import com.sugarmount.sugarcamera.story.utils.StringUtils;

import org.json.JSONException;

import java.util.Locale;

/**
 * StoryPreviewLayout.
 * 
 */
public class StoryPreviewLayout extends FrameLayout {

	private static final long ARRANGE_SCREEN_DELAY_MS = 3L * 1000L;
	private static final long CLEAR_FLAG_KEEP_SCREEN_ON_DELAY = 30L * 1000L;
	private static final int HANDLER_MESSAGE_UPDATE_SEEKBAR_PROGRESS = 0;
	private static final int HANDLER_MESSAGE_UPDATE_SEEKBAR_SECONDARY_PROGRESS = 1;
	private static final int HANDLER_MESSAGE_PAUSE_PREVIEW = 2;

	private Window mWindow;
	private InputMethodManager mInputMethodManager;

	private PreviewManager mPreviewManager;
	private Visualizer mVisualizer;
	private Visualizer.Editor mVisualizerEditor;
	private PreviewController mPreviewController;

	private PreviewSurfaceView mSurfaceView;
	private PreviewSeekBar mSeekBar;
	private ImageButton mImageButton;
	private ImageView mImageBg; 
	private TextView mTextViewDuration;
	private TextView mTextViewPosition;

	private Runnable mArrangeScreenRunnable;
	private Runnable mClearFlagKeepScreenRunnable;

	private boolean mIsPlaying = false;
	private boolean mIsFirstFrame = false;
	private int mControlViewVisibility = View.VISIBLE;

	private int mLastProgress;
	private final static int INTERVAL_TIME_MS = 33;
	private Handler mSeekHandler = new Handler();
	private int mTempProgress = -1;

	private int nSeek = 0;
	private int nRemain = 0;
	private String sDuration = "";
	
	private boolean mIsTouchEnable= true;
	
	private MovieEditMainActivity mMovieEditMainActivity;

	public StoryPreviewLayout(Context context) {
		super(context);
		initialize(context);
	}

	public StoryPreviewLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public StoryPreviewLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		Resources resources = getResources();
		int width = resources.getDisplayMetrics().widthPixels;
		int height = Math.round(width / PreviewManager.DEFAULT_PREVIEW_RESOLUTION.aspectRatio);

		super.onMeasure(width | MeasureSpec.EXACTLY, height | MeasureSpec.EXACTLY);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mIsPlaying) {
			if (mControlViewVisibility == View.GONE) {
				setControlViewVisibility(View.VISIBLE);
				arrangeScreenDelayed(ARRANGE_SCREEN_DELAY_MS);
			} else if (mControlViewVisibility == View.VISIBLE) {
				arrangeScreenDelayed(0L);
			}
		}
		
		return super.onTouchEvent(event);
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {

		if (visibility == View.VISIBLE) {
			mPreviewManager.bindPreview(mSurfaceView, mOnUpdateListener, mOnPrepareListener, mOnProgressUpdateListener);
			updateView();
		} else {
			mIsFirstFrame = true;
			mTempProgress = -1;
		}

		super.onVisibilityChanged(changedView, visibility);
	}

	private void initialize(Context context) {

		mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (context instanceof Activity) {
			mWindow = ((Activity) context).getWindow();
		}

		Context applicationContext = context.getApplicationContext();
		View.inflate(applicationContext, R.layout.activity_story_preview_layout, this);

		mPreviewManager = PreviewManager.getInstance(applicationContext);
		mVisualizer = mPreviewManager.getVisualizer();
		mVisualizerEditor = mVisualizer.getEditor();
		mPreviewController = mPreviewManager.getPreviewController();

		mSurfaceView = (PreviewSurfaceView) findViewById(R.id.kiwiple_story_preview_surface_view);
		mSeekBar = (PreviewSeekBar) findViewById(R.id.kiwiple_story_preview_seek_bar);
		mImageBg = (ImageView) findViewById(R.id.kiwiple_story_preview_bg_img);
		mImageButton = (ImageButton) findViewById(R.id.kiwiple_story_preview_button);
		mTextViewPosition = (TextView) findViewById(R.id.kiwiple_story_preview_position);
		mTextViewPosition.setTypeface(MovieEditMainActivity.typeface);
//		mTextViewPosition.setVisibility(INVISIBLE);

		mTextViewDuration = (TextView) findViewById(R.id.kiwiple_story_preview_duration);
		mTextViewDuration.setTypeface(MovieEditMainActivity.typeface);
//		mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_movie_play_time_nor, 0, 0, 0);
		mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

		if (context instanceof MovieEditMainActivity) {
		    mMovieEditMainActivity = (MovieEditMainActivity)context;
		}
		
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// 마지막 frame 한번더 갱신(video)
				mSeekHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						mPreviewController.setPosition(mLastProgress);
						if(mLastProgress <= 0){
							if(mTextViewPosition.getVisibility() == VISIBLE) {
//								mTextViewPosition.setVisibility(INVISIBLE);
//								mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_movie_play_time_nor, 0, 0, 0);
							}
						}else{
							if(mTextViewPosition.getVisibility() == INVISIBLE) {
//								mTextViewPosition.setVisibility(VISIBLE);
//								mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
							}
						}

						nSeek = StringUtils.getMs2Time(mLastProgress);
						nRemain = StringUtils.MAX_DURATION_SEC - nSeek;
						if(nRemain >= 0) {
							mTextViewPosition.setText(String.format(Locale.getDefault(), "%02d:%02d", nSeek / 60, nSeek % 60));
							sDuration = String.format(Locale.getDefault(), "%02d:%02d", nRemain / 60, nRemain % 60);
							if (!sDuration.equals("00:00")) {
//								if (StringUtils.MAX_DURATION_SEC != nRemain)
//									sDuration = "-" + sDuration;
							} else {
								sDuration = " " + sDuration;
							}
							mTextViewDuration.setText(sDuration);
						}
						mIsTouchEnable = true;
					}
				}, 100);

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if(!mIsTouchEnable)
					return;
				
				mIsTouchEnable =false;
				pausePreview();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					// 33ms 기준으로 progress 변경(play 동작과 같이)
					if (progress != 0) {
						if (progress % INTERVAL_TIME_MS > INTERVAL_TIME_MS / 2) {
							progress += INTERVAL_TIME_MS - (progress % INTERVAL_TIME_MS);
						} else {
							progress -= progress % INTERVAL_TIME_MS;
						}
					}
					if (mTempProgress == -1) {
						mTempProgress = progress;
					} else {
						if (mTempProgress == progress)
							return;
						else
							mTempProgress = progress;
					}

					seekBar.setProgress(progress);
					mLastProgress = progress;
					try {
					    mPreviewController.setPosition(progress);    
					} catch (InvalidFileException e) {
					    if (mMovieEditMainActivity != null) {
					        mMovieEditMainActivity.showInvalidDataPopup();
					    }
					}

					if(mLastProgress <= 0){
						if(mTextViewPosition.getVisibility() == VISIBLE) {
//							mTextViewPosition.setVisibility(INVISIBLE);
//							mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_movie_play_time_nor, 0, 0, 0);
						}
					}else{
						if(mTextViewPosition.getVisibility() == INVISIBLE) {
//							mTextViewPosition.setVisibility(VISIBLE);
//							mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
						}
					}

//					SmartLog.e("onProgressChanged", "progress:" + progress + ", nSeek:" + nSeek);

					nSeek = StringUtils.getMs2Time(progress);
					nRemain = StringUtils.MAX_DURATION_SEC - nSeek;
					if(nRemain >= 0) {
						mTextViewPosition.setText(String.format(Locale.getDefault(), "%02d:%02d", nSeek / 60, nSeek % 60));
						sDuration = String.format(Locale.getDefault(), "%02d:%02d", nRemain / 60, nRemain % 60);

//						SmartLog.e("onProgressChanged", "getDuration:" + mVisualizer.getDuration() +
//								", progress:" + progress + ", nSeek:" + nSeek + ", nRemain:" + nRemain);

						if (!sDuration.equals("00:00")) {
//							if (StringUtils.MAX_DURATION_SEC != nRemain)
//								sDuration = "-" + sDuration;
						} else {
							sDuration = " " + sDuration;
						}
						mTextViewDuration.setText(sDuration);
					}
				}
			}

		});
		mImageButton.setOnClickListener(mOnImageButtonClickListener);
	}

	private void updateView() {

		int duration = mVisualizer.getDuration();
		StringUtils.setMaxDurationSec(duration);

		mSeekBar.setMax(duration);
//		mSeekBar.setMax(StringUtils.MAX_DURATION_SEC*1000);

//		SmartLog.e("updateView", "getDuration:" + StringUtils.getTimeStringFromMillis(duration));

		mTextViewPosition.setText("00:00");
		if(duration > 0)
			mTextViewDuration.setText(StringUtils.getTimeStringFromMillis(duration));

		int position = mVisualizer.getPosition();
		mSeekBar.setProgress(position);
	}

	private void arrangeScreenDelayed(long delayMillis) {

		if (mArrangeScreenRunnable != null) {
			removeCallbacks(mArrangeScreenRunnable);
		}

		mArrangeScreenRunnable = new Runnable() {

			@Override
			public void run() {
				setControlViewVisibility(View.GONE);
			}
		};
		postDelayed(mArrangeScreenRunnable, delayMillis);
	}

	private void setControlViewVisibility(int visibility) {

		if (mControlViewVisibility != visibility) {
			mImageButton.setVisibility(visibility);
			mSeekBar.setVisibility(visibility);
			mTextViewDuration.setVisibility(visibility);
			mTextViewPosition.setVisibility(visibility);
			mImageBg.setVisibility(visibility);
			mControlViewVisibility = visibility;
		}
	}

	public void setPreviewScript(String jsonScript) throws JSONException, RuntimeException {

		if (mIsPlaying) {
			pausePreview();
		}

		mTextViewDuration.setText("00:00");
		mTextViewPosition.setText("00:00");
		mSeekBar.setProgress(0);
		mSeekBar.setSecondaryProgress(0);
		mSeekBar.setMax(0);

		mIsFirstFrame = true;

		try {
			mVisualizerEditor.start();
			mPreviewManager.injectJsonScript(jsonScript);
		} catch (JSONException | RuntimeException exception) {
			mVisualizer.clear();
			throw exception;
		} finally {
			mVisualizerEditor.finish();
		}
		updateView();
	}

	public void setPreviewScriptWithoutUpdateView(String jsonScript) throws JSONException, RuntimeException {

		try {
			mVisualizerEditor.start();
			mPreviewManager.injectJsonScript(jsonScript);
		} catch (JSONException | RuntimeException exception) {
			mVisualizer.clear();
			throw exception;
		} finally {   
			mVisualizerEditor.finish();
		}
	}


	public void setPreviewMusic(String audioFilePath, boolean isInternalAudio) {
		mPreviewManager.setAudioFile(audioFilePath, isInternalAudio);
	}

	public synchronized void startPreview() {
		if (!mIsPlaying) {
			removeCallbacks(mClearFlagKeepScreenRunnable);
			mClearFlagKeepScreenRunnable = null;

			mWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			mWindow.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			mWindow.clearFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
			mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);

			int nMax = mVisualizer.getDuration();
			int nCurrent = mVisualizer.getPosition();

			if (nMax == nCurrent || nMax-nCurrent <= 33) {
				mPreviewController.setPosition(0);
			}

			mImageButton.setImageResource(R.drawable.kiwiple_story_preview_button_pause);
			mPreviewController.startPreview();
			mIsPlaying = true;

			arrangeScreenDelayed(ARRANGE_SCREEN_DELAY_MS);
		}

		if(mTextViewPosition.getText().equals("00:00")) {
//			mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_movie_play_time_nor, 0, 0, 0);
		}else{
//			mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		mTextViewPosition.setVisibility(VISIBLE);
	}

	public synchronized void pausePreview() {

		if (mIsPlaying) {
			mClearFlagKeepScreenRunnable = new Runnable() {

				@Override
				public void run() {
					mWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					mWindow.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				}
			};
			postDelayed(mClearFlagKeepScreenRunnable, CLEAR_FLAG_KEEP_SCREEN_ON_DELAY);

			if (mArrangeScreenRunnable != null) {
				removeCallbacks(mArrangeScreenRunnable);
				mArrangeScreenRunnable = null;
			}

			setControlViewVisibility(View.VISIBLE);

			mImageButton.setImageResource(R.drawable.kiwiple_story_preview_button_play);
			mPreviewController.pausePreview();
			mIsPlaying = false;
		}

		if(mTextViewPosition.getText().equals("00:00")) {
//			mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_movie_play_time_nor, 0, 0, 0);
//			mTextViewPosition.setVisibility(INVISIBLE);
		}else{
//			mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//			mTextViewPosition.setVisibility(VISIBLE);
		}
	}

	public synchronized void rewind() {
		if (mIsPlaying) {
			pausePreview();
		}

		mPreviewController.setPosition(0);
		mHandler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_SEEKBAR_PROGRESS);
	}

	private final View.OnClickListener mOnImageButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			
			if(!mIsTouchEnable)
				return;
			mIsTouchEnable = false;

			if (mIsPlaying) {
				pausePreview();
			} else {
				startPreview();
			}
			mIsTouchEnable = true;
		}
	};

	private  final OnProgressUpdateListener mOnProgressUpdateListener = new OnProgressUpdateListener() {
		@Override
		public void onFinish() {
			if(mMovieEditMainActivity != null){

				mMovieEditMainActivity.hideProgressAbstract();
			}
		}
	};

	private final OnUpdateListener mOnUpdateListener = new OnUpdateListener() {

		@Override
		public void onUpdate() {
			mHandler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_SEEKBAR_PROGRESS);
		}

		@Override
		public void onEnd() {
			mHandler.sendEmptyMessage(HANDLER_MESSAGE_PAUSE_PREVIEW);
		}
	};

	private final OnPrepareListener mOnPrepareListener = new OnPrepareListener() {

		@Override
		public void onPrepare(int index, int durationPreparedMs) {

			Message message = new Message();
			message.what = HANDLER_MESSAGE_UPDATE_SEEKBAR_SECONDARY_PROGRESS;
			message.arg1 = durationPreparedMs;

			mHandler.sendMessage(message);
		}

		@Override
		public void onComplete() {

			Message message = new Message();
			message.what = HANDLER_MESSAGE_UPDATE_SEEKBAR_SECONDARY_PROGRESS;
			message.arg1 = mVisualizer.getDuration();

			mHandler.sendMessage(message);
		}
	};

	private final Handler mHandler = new Handler() {

		private int mProgress;

		@Override
		public void handleMessage(Message message) {
			
			try {
			
				switch (message.what) {
	
					case HANDLER_MESSAGE_UPDATE_SEEKBAR_PROGRESS:
						mProgress = mVisualizer.getPosition();
						mSeekBar.setProgress(mProgress);

//						if(mProgress <= 0) {
//							mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_movie_play_time_nor, 0, 0, 0);
//						}else{
//							mTextViewDuration.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//						}

						nSeek = StringUtils.getMs2Time(mProgress);
						nRemain = StringUtils.MAX_DURATION_SEC - nSeek;
						if(nRemain >= 0) {
							mTextViewPosition.setText(String.format(Locale.getDefault(), "%02d:%02d", nSeek / 60, nSeek % 60));
							sDuration = String.format(Locale.getDefault(), "%02d:%02d", nRemain / 60, nRemain % 60);
							if (!sDuration.equals("00:00")) {
//								if (StringUtils.MAX_DURATION_SEC != nRemain)
//									sDuration = "-" + sDuration;
							} else {
								sDuration = " " + sDuration;
							}
							mTextViewDuration.setText(sDuration);
						}

						break;
	
					case HANDLER_MESSAGE_PAUSE_PREVIEW:
						pausePreview();
						break;
	
					case HANDLER_MESSAGE_UPDATE_SEEKBAR_SECONDARY_PROGRESS:
	
						if (mIsFirstFrame) {
							mIsFirstFrame = false;
							mPreviewController.setPosition(0);
						}
						mSeekBar.setSecondaryProgress(message.arg1);
						break;
	
					default:
						break;
				}
			} catch(NullPointerException npe){
				npe.getMessage();
			} catch (Exception e) {
				e.getMessage();
			}
		}
	};

}