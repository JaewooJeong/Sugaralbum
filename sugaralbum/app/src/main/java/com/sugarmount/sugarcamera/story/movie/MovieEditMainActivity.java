
package com.sugarmount.sugarcamera.story.movie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageframework.util.SmartLog;
import com.kiwiple.multimedia.canvas.DummyScene;
import com.kiwiple.multimedia.canvas.Effect;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageTextScene;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.TextEffect;
import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.canvas.data.TextElement;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.kiwiple.scheduler.SchedulerEnvironment;
import com.kiwiple.scheduler.SchedulerVersion;
import com.kiwiple.scheduler.data.uplus.effect.UplusTextEffectData;
import com.kiwiple.scheduler.tag.UserTag;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.ResourceType;
import com.sugarmount.common.ads.GoogleAds;
import com.sugarmount.common.env.MvConfig;
import com.sugarmount.common.utils.log;
import com.sugarmount.sugaralbum.R;
import com.sugarmount.sugarcamera.camera.Storage;
import com.sugarmount.sugarcamera.story.StoryCommonIntent;
import com.sugarmount.sugarcamera.story.database.StoryJsonDatabaseConstants;
import com.sugarmount.sugarcamera.story.database.StoryJsonPersister;
import com.sugarmount.sugarcamera.story.gallery.ConstantsGallery;
import com.sugarmount.sugarcamera.story.gallery.RULES;
import com.sugarmount.sugarcamera.story.gallery.SelectManager;
import com.sugarmount.sugarcamera.story.noti.StoryNotification;
import com.sugarmount.sugarcamera.story.scenario.PriorityAsyncTask;
import com.sugarmount.sugarcamera.story.scheduler.SchedulerManager;
import com.sugarmount.sugarcamera.story.service.VideoCreationService;
import com.sugarmount.sugarcamera.story.story.StoryLatestDataManager;
import com.sugarmount.sugarcamera.story.story.imageloader.StoryImageLoader;
import com.sugarmount.sugarcamera.story.theme.ThemeManager;
import com.sugarmount.sugarcamera.story.utils.StoryUtils;
import com.sugarmount.sugarcamera.story.utils.Utils;
import com.sugarmount.sugarcamera.story.utils.WaveProgress;
import com.sugarmount.sugarcamera.story.views.StoryEditText;
import com.sugarmount.sugarcamera.story.views.StoryPreviewLayout;
import com.sugarmount.sugarcamera.story.views.TwoButtonTextDialog;
import com.sugarmount.sugarcamera.story.views.TwoButtonTextDialog.OnBtnClickListener;
import com.sugarmount.sugarcamera.ui.gallery2.GalleryDialogActivity;
import com.sugarmount.sugarcamera.utils.FileCopyUtil;
import com.sugarmount.sugarcamera.utils.PermissionUtil;

import org.json.JSONException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MovieEditMainActivity extends GalleryDialogActivity {

    private final static int LOADING_ANIMATION_TRANSITION_POSITION = 90;
    private final static int LOADING_ANIMATION_TRANSITION_MAX = 100;
    private final static int REQUEST_HIDE_PROGRESS = 10;
    public enum ERROR_HANDLER {
        SUCCESS, UNKNOWN_ERROR, CODEC_ERROR, ITEM_COUNT, VIDEO_COUNT, IMAGE_COUNT, VIDEO_MIN_DURATION, VIDEO_MAX_DURATION, NOT_FOUND
    }

    private boolean isMakeJson = false;
    private boolean isProgressState = true;

    private boolean mIsServiceBound = false;
    private boolean mIsMusicInternal = true;
    private boolean mIsStart = true;
    private boolean mIsShowingProgress = false;
    private boolean isChangedEndingLogo = false;
    private boolean mIsOverOffSet = false;
    private boolean mSaveRequested = false;
    private Bundle mSaveMessageData = null;

    private int mIsInternal;

    private String mJsonScript;
    private String mTitle;
    private String sTheme;
    private String mBasicMusicStr;
    private String mOriginMusicStr = mBasicMusicStr;
    private String mBgMusicTitle;
    private String currentSaveFileName;

    private Uri mJsonDataUri;
    private List<Scene> mScenes;

    private static MovieEditMainActivity sLastInstance;
    private StoryPreviewLayout mStoryPreviewLayout;
    private StoryJsonPersister mStoryJsonPersister;
    private SchedulerManager mSchedulerManager;
    private StoryFileName storyFileName;
    private TwoButtonTextDialog mInValidDialog;
    private StoryLatestDataManager mLatestDataManager;

    private Context mContext;
    private ImageView mSendLayout;
    private ConstraintLayout mHeaderLayout;
    private ViewGroup mFrame;
    private View mPreviewLayout;
    private View appBarLayout;
    private View mSaveLayout;
    private Button mTitleChangeButton;
    private TextView mProgressPercent;
    private WaveProgress mWaveProgress;
    private StoryEditText mSubjectEText;
    private InputMethodManager mImm;
    private Messenger mMessenger;
    private Messenger mService1;
    private Cursor mCursor = null;
    private Resolution mResolution = Resolution.FHD;

    public static Typeface typeface;

    private AdView adView;
    private FrameLayout adContainerView;
    private boolean initialLayoutComplete = false;

    private NativeAd currentNativeAd;
    private NativeAdView adView2;

    private Activity activity;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setNeedPermission(PermissionUtil.STORAGE_PERMISSION_INDEX);
        super.onCreate(savedInstanceState);
        if (!super.hasPermission()) {
            finish();
            return;
        }
//        SmartLog.d("MovieEditMainActivity", "#onCreate");

        if (!FileCopyUtil.isFileExits(getApplicationContext(), Theme.OUTRO_ASSET_FILE_NAME)) {
            FileCopyUtil.copyFile(getApplicationContext(), Theme.OUTRO_ASSET_FILE_NAME);
        }

        sLastInstance = this;
        mContext = this;

        typeface = ResourcesCompat.getFont(this, R.font.nanumsquarel);

        mStoryJsonPersister = StoryJsonPersister.getStoryJsonPersister(mContext.getApplicationContext());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(Window.FEATURE_NO_TITLE, Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();

        setContentView(R.layout.activity_movie_edit_main);
        setInsetView(findViewById(R.id.kiwipleStoryEditMainLayout));

        activity = this;

        initComboBox();

//        initAds();

        mImm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        storyFileName = new StoryFileName(this.getString(R.string.story_file_name_format));
        mMessenger = new Messenger(mIncomingHandler);

        mStoryPreviewLayout = findViewById(R.id.kiwipleStoryEditPreview);

        if (!VideoCreationService.getIsSavingMovieDiary())
            StoryUtils.checkMovieDiaryTmpFile(this);

        mStoryPreviewLayout.setOnTouchListener((view, event) -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mStoryPreviewLayout.getWindowToken(), 0);
            return false;
        });

        mHeaderLayout = findViewById(R.id.MovieEditMainTitleArea);

        ImageView imageView1 = findViewById(R.id.imageView1);
        imageView1.setOnClickListener(mBtnClickListener);

        mSendLayout = findViewById(R.id.send);
        mSendLayout.setOnClickListener(mBtnClickListener);

        mTitleChangeButton = findViewById(R.id.button2);
        mTitleChangeButton.setOnClickListener(mBtnClickListener);

        appBarLayout = findViewById(R.id.appBarLayout);
        mPreviewLayout = findViewById(R.id.kiwiple_story_movie_edit_layout);

        mSaveLayout = findViewById(R.id.kiwiple_story_movie_save_layout);
        mSaveLayout.setVisibility(View.INVISIBLE);

        mSubjectEText = findViewById(R.id.kiwipleStoryEditMainMainFragmentTitleEditTextEt);

        mProgressPercent = findViewById(R.id.kiwiple_story_save_wave_percent1);

        mWaveProgress = findViewById(R.id.kiwiple_story_save_wave_progress);

        if (VideoCreationService.getIsSavingMovieDiary()) {
            hideTitleButtons();
        }

        mLatestDataManager = StoryLatestDataManager.getStoryDataManager(getApplicationContext());
        mLatestDataManager.update();

        RULES.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        StoryCommonIntent.getInstance(getApplicationContext()).cancelSavingMDService();
//        SmartLog.d("MovieEditMainActivity", "#onResume");

        PreviewManager previewManager = PreviewManager.getInstance(getApplicationContext());
        Visualizer visualizer = previewManager.getVisualizer();
        if (!visualizer.isOnPreviewMode()) {
            Visualizer.Editor editor = visualizer.getEditor();
            if (visualizer.isOnEditMode()) {
                editor.setPreviewMode(true);
            } else {
                editor.start().setPreviewMode(true).finish();
            }
        }

        if (isProgressState) {
            showProgress();
        } else {
            hideProgress();
        }

        if (!mIsServiceBound) {
            Intent serviceIntent = new Intent(this, VideoCreationService.class);
            serviceIntent.putExtra(VideoCreationService.EXTRAS_KEY_MESSENGER, mMessenger);
            
            // Start as foreground service first, then bind
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            
            // Then bind to get the messenger interface
            bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            mIsServiceBound = true;
        }

        if (mIsShowingProgress) {
            showProgress();
        }

        if (getIntent() != null) {
            new MakeJsonTask().execute();
        }

    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
//        SmartLog.d("MovieEditMainActivity", "#onPause");

        if (mIsServiceBound) {
            unbindService(mServiceConnection);
            mIsServiceBound = false;
        }

        mStoryPreviewLayout.pausePreview();

        hideProgress();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        SmartLog.d("MovieEditMainActivity", "onDestroy");
        if (adView != null) {
            adView.destroy();
        }
        if (adView2 != null) {
            adView2.destroy();
        }
        if (currentNativeAd != null) {
            currentNativeAd.destroy();
        }
        NotificationManager mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        // If the service is still bound when the activity is destroyed, unbind it.
        if (mIsServiceBound) {
            unbindService(mServiceConnection);
            mIsServiceBound = false;
        }

        if(mLatestDataManager != null) {
            mLatestDataManager.clear();
        }

//        L.e("mJsonDataUri = " + mJsonDataUri);
        if (mJsonDataUri != null) {
            mStoryJsonPersister.deleteJsonData(mJsonDataUri);
            StoryUtils.deleteImageFilesFromUBox(this);
        }

        PreviewManager.getInstance(getApplicationContext()).release();
        StoryJsonPersister.getStoryJsonPersister(getApplicationContext()).release();

//        SmartLog.d("MovieEditMainActivity", "#onDestroy 2");
        StoryCommonIntent.getInstance(getApplicationContext()).cancelSavingMDService();

        super.onDestroy();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isProgressState) {
                return false;
            } else if (mIsShowingProgress) {
                hideProgressRetainValue();
                return false;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }



    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getMediaContent().getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    super.onVideoEnd();
                }
            });
        }
    }

    private void loadBanner(AdSize adSize) {
        adView = new AdView(this);
        adView.setAdUnitId(mContext.getString(MvConfig.debug ? R.string.banner_ad_unit_id_test : R.string.banner_ad_unit_id));
        adContainerView.addView(adView);
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void loadNative() {
        AdLoader.Builder builder = new AdLoader.Builder(this, mContext.getString(MvConfig.debug ? R.string.native_ad_unit_id_test : R.string.native_ad_unit_id));
        builder.forNativeAd( nativeAd -> {
            currentNativeAd = nativeAd;

            FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
            adView2 = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified, null);
            populateNativeAdView(nativeAd, adView2);
            frameLayout.removeAllViews();
            frameLayout.addView(adView2);
        });

        VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(
                                new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                                        log.e("Fail to load Ad");
                                    }

                                    @Override
                                    public void onAdLoaded() {
                                        log.i("load Ad");
                                    }
                                })
                        .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    public void initAds(){

        /** 전면 광고 */
        GoogleAds.Companion.loadInterstitialAd(this);

        /** 적응형 배너 광고 */
        // 배너 광고 호출
        adContainerView = findViewById(R.id.ad_view_container);
        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainerView.getViewTreeObserver().addOnGlobalLayoutListener(
                () -> {
                    if (!initialLayoutComplete) {
                        initialLayoutComplete = true;
                        loadBanner(getAdSize());
                    }
                });

        /** 네이티브 광고 */
        loadNative();

    }



    public void initComboBox(){
        final AppCompatSpinner resolution = findViewById(R.id.appCompatSpinner);
        resolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 if(position == 0){
                     mResolution = Resolution.FHD;
                 }else if(position == 1){
                     mResolution = Resolution.HD;
                 }else if(position == 2){
                     mResolution = Resolution.NHD;
                 }else {
                     mResolution = Resolution.FHD;
                 }
                 SmartLog.i("", "onItemSelected");
             }

             @Override
             public void onNothingSelected(AdapterView<?> parent) {

             }
         });

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.resolutionSpinner,
                        R.layout.res_spinner);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resolution.setAdapter(adapter);
    }

    public void onBackPressed() {
        if (isMakeJson) return;
        hideProgress();
        mStoryPreviewLayout.pausePreview();

        if (mSaveLayout.getVisibility() == View.INVISIBLE) {
            super.onBackPressed();
        }
    }

    private void onTitleChange(String title) {

        mStoryPreviewLayout.pausePreview();

        PreviewManager previewManager = PreviewManager.getInstance(getApplicationContext());
        Visualizer visualizer = previewManager.getVisualizer();
        Visualizer.Editor visualizerEditor = visualizer.getEditor().start();

        mScenes = visualizer.getRegion().getScenes();

        // 20150303 olive : #10804 모든 ImageTextScene과 TextEffect가 있는 DummyScene의 텍스트를 변경한다.
        // TODO: ImageTextScene 또는 DummyScene을 Intro나 Outro로 사용하지 않을 경우에 대한 분기처리.

        Scene introScene = mScenes.get(0);
        if (introScene.getClass().equals(ImageFileScene.class)) {
            for (Effect effect : introScene.getEffects()) {
                if (effect.getClass().equals(TextEffect.class)) {
                    if (UserTag.getTextEffectTagType((TextEffect) effect).equals(UplusTextEffectData.TAG_JSON_VALUE_TYPE_TITLE)) {
                        ((TextEffect) effect).getEditor().setResourceText(title);
                    }
                }
            }
        } else if (introScene.getClass().equals(ImageTextScene.class)) {
            List<TextElement> textElements = ((ImageTextScene) introScene).getTextElements();
            textElements.get(textElements.size() - 1).setText(title);
            ((ImageTextScene) introScene).getEditor().setTextElements(textElements);
        } else if (introScene.getClass().equals(DummyScene.class)) {
            for (Effect effect : introScene.getEffects()) {
                if (effect.getClass().equals(TextEffect.class)) {
                    ((TextEffect) effect).getEditor().setResourceText(title);
                }
            }
        }

        Scene outroScene = mScenes.get(mScenes.size() - 1);
        if (outroScene.getClass().equals(ImageFileScene.class)) {
            for (Effect effect : outroScene.getEffects()) {
                if (effect.getClass().equals(TextEffect.class)) {
                    if (UserTag.getTextEffectTagType((TextEffect) effect).equals(UplusTextEffectData.TAG_JSON_VALUE_TYPE_TITLE)) {
                        ((TextEffect) effect).getEditor().setResourceText(title);
                    }
                }
            }
        } else if (outroScene.getClass().equals(ImageTextScene.class)) {
            List<TextElement> textElements = ((ImageTextScene) outroScene).getTextElements();
            textElements.get(textElements.size() - 1).setText(title);
            ((ImageTextScene) outroScene).getEditor().setTextElements(textElements);
        } else if (outroScene.getClass().equals(DummyScene.class)) {
            for (Effect effect : outroScene.getEffects()) {
                if (effect.getClass().equals(TextEffect.class)) {
                    ((TextEffect) effect).getEditor().setResourceText(title);
                }
            }
        }

        visualizerEditor.finish();

        mStoryJsonPersister.saveJsonThumbnail(mJsonDataUri);
        StoryImageLoader.getInstance(mContext).deleteCache(mJsonDataUri.toString());
        StoryCommonIntent.getInstance(mContext).sendDeleteThumbnailAction(mJsonDataUri);

        mStoryPreviewLayout.rewind();
        mStoryJsonPersister.updateJsonTitle(mJsonDataUri, mTitle);

    }

    public void handleMakeSuccess(){
        StoryCommonIntent.getInstance(getApplicationContext()).cancelSavingMDService();
    }

    public void handleMakeFail(){
        StoryCommonIntent.getInstance(getApplicationContext()).cancelSavingMDService();
    }

    public void handleMakeResult(ERROR_HANDLER error_handler){
        Intent goodIntent = new Intent();
        goodIntent.putExtra(SelectManager.ERROR_CODE, error_handler);
        setResult(ConstantsGallery.REQ_CODE_CONTENT_DETAIL, goodIntent);
        finish();
    }

    private final View.OnClickListener mBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.send == id) {
                GoogleAds.Companion.showInterstitialAd(activity);
                mTitle = Objects.requireNonNull(mSubjectEText.getText()).toString();
                onTitleChange(mTitle);
                saveVideoWork(false);
            }else if(R.id.imageView1 == id) {
                onBackPressed();
//            }else if(R.id.kiwipleStoryEditMainMainFragmentStyleRandomBtn == id){
//                mTitle = mSubjectTmp = mSubjectEText.getText().toString();
//                onTitleChange(mTitle);
            }else if(R.id.button2 == id){
                mTitle = Objects.requireNonNull(mSubjectEText.getText()).toString();
                onTitleChange(mTitle);
                mImm.hideSoftInputFromWindow(mSubjectEText.getWindowToken(), 0);
            }else if(R.id.kiwiple_story_movie_edit_layout == id){
                if(mImm.isAcceptingText()){
                    mTitle = Objects.requireNonNull(mSubjectEText.getText()).toString();
                    onTitleChange(mTitle);
                    mImm.hideSoftInputFromWindow(mSubjectEText.getWindowToken(), 0);
                }
            }
        }
    };

    private class completeMsgThread extends AsyncTask<Uri, Void, Uri> {
        @Override
        protected Uri doInBackground(Uri... uri) {
            int nPos = 0;
            Uri fileUri = null;

            // Use app-specific directory for Android 15 compatibility
            String appDir = Storage.getAppSpecificDirectory(MovieEditMainActivity.this);
            File fromFile = new File(appDir, currentSaveFileName + ".tmp");
            File toFile = new File(appDir, currentSaveFileName + ".mp4");

            while(!fromFile.exists()) {
                if(nPos++ <= 30){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    SmartLog.e("MovieEditMainActivity", "fromFile exists - fail");
                    // file not found
                    break;
                }
            }

            if(fromFile.exists()) {
                if (fromFile.renameTo(toFile)) {
                    // rename ok
                    SmartLog.d("MovieEditMainActivity", "renameTo OK");
                } else {
                    // rename fail
                    SmartLog.e("MovieEditMainActivity", "renameTo FAIL");
                }

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

                fileUri = Uri.fromFile(toFile);
                intent.setData(fileUri);
                getApplicationContext().sendBroadcast(intent);
                StoryCommonIntent.getInstance(getApplicationContext()).cancelSavingMDService();

                SmartLog.d("MovieEditMainActivity", "fileUri:" + fileUri.toString());

                nPos = 0;
                while (new File(fileUri.getPath()).exists() == false) {
                    if (nPos++ <= 30) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // file not found
                        fileUri = null;
                        break;
                    }
                }
            }else{
                // file not found
                fileUri = null;
            }

            return fileUri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            final Uri u = uri;

            if(u != null) {
                if (new File(u.getPath()).exists() == true) {
                    SmartLog.d("MovieEditMainActivity", "mv diary OK");
                } else {
                    SmartLog.e("MovieEditMainActivity", "mv diary FAIL");
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent goodIntent = new Intent();
                    if(u != null) {
                        SmartLog.d("MovieEditMainActivity", "uri:" + u);
                        handleMakeSuccess();
                        goodIntent.putExtra(SelectManager.ERROR_CODE, ERROR_HANDLER.SUCCESS);
                        goodIntent.putExtra(SelectManager.FILE_URI, u.toString());
                        setResult(ConstantsGallery.REQ_CODE_CONTENT_DETAIL, goodIntent);
                        finish();
                    }else{
                        handleMakeFail();
                        handleMakeResult(ERROR_HANDLER.NOT_FOUND);
                    }
                }
            });
        }

    }

    @SuppressLint("HandlerLeak")
    private final Handler mIncomingHandler = new Handler() {
        private void dummy() {
        }

        @Override
        public void handleMessage(Message message) {

            boolean bPreview = false;
            ViewGroup.LayoutParams params = null;

            switch (message.what) {
                case VideoCreationService.MESSAGE_ON_COMPLETE:
                    SmartLog.d("MovieEditMainActivity", "Finish Movie Diary");
                    SmartLog.d("MovieEditMainActivity", "######## VideoCreationService.MESSAGE_ON_COMPLETE ########");

                    // The Activity will finalize the file.
                    String appDir = Storage.getAppSpecificDirectory(MovieEditMainActivity.this);
                    File fromFile = new File(appDir, currentSaveFileName + ".tmp");
                    String finalFileName = currentSaveFileName.replace("MOV_", "SugarAlbum_");
                    File toFile = new File(appDir, finalFileName + ".mp4");
                    Uri finalUri = null;

                    if (fromFile.exists()) {
                        if (fromFile.renameTo(toFile)) {
                            // Now that we have the final file, add it to the MediaStore.
                            finalUri = com.sugarmount.sugarcamera.MediaStoreHelper.addVideoToMediaStore(
                                MovieEditMainActivity.this, toFile.getAbsolutePath(), toFile.getName());
                        } else {
                            SmartLog.e("MovieEditMainActivity", "Failed to rename temp file.");
                        }
                    } else {
                        SmartLog.e("MovieEditMainActivity", "Temp file not found: " + fromFile.getAbsolutePath());
                    }

                    handleMakeSuccess();
                    Intent goodIntent = new Intent();
                    if (finalUri != null) {
                        goodIntent.putExtra(SelectManager.ERROR_CODE, ERROR_HANDLER.SUCCESS);
                        goodIntent.putExtra(SelectManager.FILE_URI, finalUri.toString());
                    } else {
                        goodIntent.putExtra(SelectManager.ERROR_CODE, ERROR_HANDLER.NOT_FOUND);
                    }
                    setResult(ConstantsGallery.REQ_CODE_CONTENT_DETAIL, goodIntent);
                    finish();
                    break;
                case VideoCreationService.MESSAGE_ON_ERROR_UNKNOWN:
                    handleMakeFail();
                    handleMakeResult(ERROR_HANDLER.UNKNOWN_ERROR);
                    break;
                case VideoCreationService.MESSAGE_ON_ERROR_FILE_NOT_FOUND:
                    handleMakeFail();
                    handleMakeResult(ERROR_HANDLER.NOT_FOUND);
                    break;
                case VideoCreationService.MESSAGE_ON_ERROR_VIDEO_ENGINE:
                    handleMakeFail();
                    handleMakeResult(ERROR_HANDLER.CODEC_ERROR);
                    break;
                case REQUEST_HIDE_PROGRESS:
                    if (message.obj != null && ((Boolean) message.obj) == true) {
                        isProgressState = false;
                    }
                    hideProgress();
                    break;
                case VideoCreationService.MESSAGE_ON_PROGRESS:
                    int totalFrameCount = message.arg1;
                    int renderedFrameCount = message.arg2;
                    mWaveProgress.setProgress(renderedFrameCount);

                    if(renderedFrameCount <= LOADING_ANIMATION_TRANSITION_MAX && !mIsOverOffSet){
                        mProgressPercent.setText(String.format("%02d", renderedFrameCount));
                    }else{
                        mIsOverOffSet = true;
                    }

                    break;
                default:
                    break;
            }

            if (bPreview == true) {
                setPreViewLayout();
            }
        }
    };

    private void handleVideoFileFactoryException(Handler handler, String videoFilePath, int stringResourceId) {
        sLastInstance.showProgress();
        Toast.makeText(this, stringResourceId, Toast.LENGTH_LONG).show();

        if (videoFilePath != null && !videoFilePath.equals("")) {
            File tmpFile = new File(videoFilePath);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }

        StoryNotification.removeManualStoryNotification(MovieEditMainActivity.this);
        handler.sendMessageDelayed(handler.obtainMessage(REQUEST_HIDE_PROGRESS), 1000L);
    }

    private void sendMessageToService() {
        if (!mSaveRequested || mService1 == null || mSaveMessageData == null) {
            return;
        }
        Message saveMessage = new Message();
        saveMessage.what = VideoCreationService.MESSAGE_START_CREATION;
        saveMessage.setData(mSaveMessageData);

        try {
            mService1.send(saveMessage);
            SmartLog.d("MovieEditMainActivity", "Start Movie Diary message sent to service.");
        } catch (RemoteException exception) {
            exception.printStackTrace();
        }

        // Reset the request flag
        mSaveRequested = false;
        mSaveMessageData = null;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            SmartLog.d("MovieEditMain", "onServiceDisconnected." + name);
            mService1 = null;
            mIsServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SmartLog.d("MovieEditMain", "onServiceConnected." + name);
            mService1 = new Messenger(service);
            mIsServiceBound = true;

            // Now that we are connected, send the message if one is pending.
            sendMessageToService();
        }
    };

    public class EditMessageOnKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                mTitle = Objects.requireNonNull(mSubjectEText.getText()).toString();
                onTitleChange(mTitle);

                if (mImm.isAcceptingText()) {
                    mImm.hideSoftInputFromWindow(mSubjectEText.getWindowToken(), 0);
                }
            }
            return false;
        }
    }

    private void setPreViewLayout() {
        try {
            //SmartSmartLog.d(MovieEditMainActivity.class.getSimpleName(), "############# setPreViewLayout ");
            if (mStoryPreviewLayout != null) {
                stopSaveVideoUI();
                mStoryPreviewLayout.setPreviewScript(mStoryJsonPersister.getJsonString(mJsonDataUri));
                hideProgress();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            handleMakeResult(ERROR_HANDLER.UNKNOWN_ERROR);
        }
    }

    private void startSaveVideoUI() {
        //SmartSmartLog.d(MovieEditMainActivity.class.getSimpleName(), "############# startSaveVideoUI ");
        hideTitleButtons();
        mWaveProgress.startProgressAnimation();
    }

    private void stopSaveVideoUI() {
        mPreviewLayout.setVisibility(View.VISIBLE);
        appBarLayout.setVisibility(View.VISIBLE);
        mSaveLayout.setVisibility(View.GONE);
        appBarLayout.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.black));

        if (mSaveLayout.getVisibility() == View.INVISIBLE) {
            mHeaderLayout.setVisibility(View.VISIBLE);
//            mSendStopButton.setVisibility(View.INVISIBLE);
        } else {
            mHeaderLayout.setVisibility(View.INVISIBLE);
//            mSendStopButton.setVisibility(View.VISIBLE);
        }

        mWaveProgress.stopProgressAnimation();

        mIsOverOffSet = false;
    }

    private void hideTitleButtons() {
        mHeaderLayout.setVisibility(View.INVISIBLE);
        mPreviewLayout.setVisibility(View.INVISIBLE);
        appBarLayout.setVisibility(View.GONE);
        mSaveLayout.setVisibility(View.VISIBLE);
        appBarLayout.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.transparent));
    }

    private void stopPreview() {
        mStoryPreviewLayout.pausePreview();
        mStoryPreviewLayout.rewind();
    }

    protected void releaseDialog(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    void showProgress() {
        if (mFrame == null) {
            Window w = getWindow();

            ViewGroup view = (ViewGroup) w.getDecorView();

            mFrame = (ViewGroup) LayoutInflater.from(this)
                    .inflate(R.layout.activity_indicator_spinner_layout,
                            view, false);

             ProgressBar spinner = mFrame.findViewById(R.id.progress);
//            LoadingAniView spinner = (LoadingAniView) mFrame.findViewById(R.id.progress);
//            spinner.startAni();


            mFrame.setClickable(true);

            w.addContentView(mFrame,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    public void hideProgressAbstract(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
            }
        });

    }

    public void hideProgress() {

        if (mFrame != null) {
            ViewGroup parent = ((ViewGroup) mFrame.getParent());
            if (parent != null) {
                parent.removeView(mFrame);
            }

            mFrame = null;
            isProgressState = false;
        }
    }

    private void hideProgressRetainValue() {
        mIsShowingProgress = false;
        hideProgress();

    }

    public void showInvalidDataPopup() {
        releaseDialog(mInValidDialog);
        mInValidDialog = new TwoButtonTextDialog(this);
        mInValidDialog.setDialogMessage1(getString(R.string.kiwiple_story_gallery_data_validate_fail));
        mInValidDialog.setMessage1TextSize(13.0f);
        mInValidDialog.setOnBtnClickListener(new OnBtnClickListener() {

            @Override
            public void onClick(View view, boolean flag) {
                if (flag) {
                    mStoryJsonPersister.deleteJsonData(mJsonDataUri);
                }
                releaseDialog(mInValidDialog);
                finish();
            }
        });

        mInValidDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        mInValidDialog.show();
    }



    private boolean checkDataValidate(Intent intent) {
        ERROR_HANDLER er = makeJsonData(intent);
        boolean bResult = false;
        switch (er){
            case SUCCESS:
                checkSchedulerVersion();
                L.e("json uri : " + mJsonDataUri);
                if(mJsonDataUri != null) {
                    String script = mStoryJsonPersister.getJsonString(mJsonDataUri);
                    if (Utils.isDataValidate(script, getApplicationContext(), mStoryPreviewLayout))
                        bResult = true;
                }else{
                    Intent errIntent = new Intent();
                    errIntent.putExtra(SelectManager.ERROR_CODE, ERROR_HANDLER.UNKNOWN_ERROR);
                    setResult(ConstantsGallery.REQ_CODE_CONTENT_DETAIL, errIntent);
                    onBackPressed();
                }
                break;
            case UNKNOWN_ERROR:
            case ITEM_COUNT:
            case VIDEO_COUNT:
            case IMAGE_COUNT:
            case VIDEO_MIN_DURATION:
            case VIDEO_MAX_DURATION:
            case NOT_FOUND:
                onBackPressed();
                handleMakeResult(er);
                break;
        }

        return bResult;
    }

    private void handleIntent(Intent intent) {
        L.d("json uri : " + mJsonDataUri);
        if (mJsonDataUri == null) {
            return;
        }

        String[] projection = new String[]{
                StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING,
                StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING_TITLE,
                StoryJsonDatabaseConstants.JsonDatabaseField.BG_MUSIC,

                StoryJsonDatabaseConstants.JsonDatabaseField.IS_INTERNAL,
                StoryJsonDatabaseConstants.JsonDatabaseField.THEME
        };
        mCursor = mContext.getContentResolver().query(mJsonDataUri, projection, null, null, null);

        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                mJsonScript = mCursor.getString(mCursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING));

                try {
                    mStoryPreviewLayout.setPreviewScript(mJsonScript);
                } catch (Exception exception) {
                    //20151026 : fixes #12542
                    exception.printStackTrace();
                    if (exception instanceof com.kiwiple.debug.InvalidFileException) {
                        releaseCursor();
                        showInvalidDataPopup();
                        return;
                    }
                }

                mBgMusicTitle = mCursor.getString(mCursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.BG_MUSIC));
                sTheme = mCursor.getString(mCursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.THEME));
                mOriginMusicStr = mStoryJsonPersister.getJsonBgMusicPath(mJsonDataUri);
                mIsInternal = mCursor.getInt(mCursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.IS_INTERNAL));
                //#12272 : fragment 중복 호출 방지

//                mBgMusicTitle = "Free_And_Lucky";
//                mOriginMusicStr = "audio/Free_And_Lucky.mp3";


                /**
                 * theme정보 확인하도록 추가 aubergine
                 */
                mIsMusicInternal = Utils.isAssetDefaultMusic(mBgMusicTitle, getApplicationContext());

                L.e("OriginMusicStr : " + mOriginMusicStr + ", mBgMusicTitle = " + mBgMusicTitle + ", mBasicMusicStr = " + mBasicMusicStr);
                L.e("mIsMusicInternal : " + mIsMusicInternal + ", mIsInternal = " + mIsInternal);
                mStoryPreviewLayout.setPreviewMusic(mOriginMusicStr, mIsMusicInternal);
                mTitle = mCursor.getString(mCursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING_TITLE));
                L.d("mTitle : " + mTitle);

            }
            releaseCursor();
        }

        if (mIsMusicInternal && mIsStart) {
            mIsStart = false;
            mBasicMusicStr = "";
            // 20150211 olive : #10605 replace com.sugarmount.sugarcamera.story.Theme to
            // com.sugarmount.sugarcamera.story.Theme.ThemeManager
            // Object bgSound = ThemeFrame.getDesignThemeBgSoundHashMap().get(
            // sTheme);
            Object bgSound = ThemeManager.getInstance(this).getThemeByName(sTheme).audioFileName;
            if (bgSound != null) {
                mBasicMusicStr = (String) bgSound;
                L.e("mBgMusicTitle = " + mBgMusicTitle);
            }
        }

//        mBasicMusicStr = "audio/Free_And_Lucky.mp3";

        mStoryJsonPersister.updateJsonRead(mJsonDataUri);


    }

    private void checkSchedulerVersion() {

        if(mJsonDataUri == null){
            //SmartSmartLog.d(MovieEditMainActivity.class.getSimpleName(), "mJsonDataUri is null");

            Intent errIntent = new Intent();
            errIntent.putExtra(SelectManager.ERROR_CODE, ERROR_HANDLER.UNKNOWN_ERROR);
            setResult(ConstantsGallery.REQ_CODE_CONTENT_DETAIL, errIntent);
            onBackPressed();
            return;
        }

        sTheme = mStoryJsonPersister.getThemeName(mJsonDataUri);
        Theme theme = ThemeManager.getInstance(mContext).getThemeByName(sTheme);

        String versionString = mStoryJsonPersister.getJsonSchedulerVersion(mJsonDataUri);
        SchedulerVersion version = new SchedulerVersion(versionString);

        L.d("scheduler version : " + version);
        L.d("theme : " + sTheme + ", uri : " + mJsonDataUri);
        L.d("theme version : " + theme.version.toString() + ", resource type : " + theme.resourceType);

        try {


            if (version.isBelow(SchedulerEnvironment.VERSION_1_0_3)) {

                String jsonScript = mStoryJsonPersister.getJsonString(mJsonDataUri);
                String updateJsonScript = null;
                if (Utils.isAssetMusic(jsonScript)) {
                    String audioPath = Utils.getAssetDefaultMusicPath(mContext);
                    String audioTitle = audioPath.substring(audioPath.indexOf('/') + 1, audioPath.lastIndexOf('.'));
                    updateJsonScript = Utils.changeMusicPath(jsonScript, Utils.getAssetDefaultMusicPath(mContext), com.kiwiple.multimedia.ResourceType.ANDROID_ASSET);
                    mStoryJsonPersister.updateJsonScriptData(mJsonDataUri, updateJsonScript);
                    mStoryJsonPersister.updateJsonBgMusic(mJsonDataUri, audioTitle, Utils.getAssetDefaultMusicPath(mContext), true);
                }

                if (theme.name.equals(Theme.THEME_NAME_OLDMOVIE)) {
                    String srcScript = null;
                    if (updateJsonScript != null) {
                        srcScript = updateJsonScript;
                    } else {
                        srcScript = jsonScript;
                    }
                    String newScript = Utils.changeOldMovieOverlayEffectDrawableId(srcScript);
                    mStoryJsonPersister.updateJsonScriptData(mJsonDataUri, newScript);
                }

                if (version.isBelow(SchedulerEnvironment.VERSION_1_0_2)) {
                    if (theme.resourceType.equals(ResourceType.DOWNLOAD)) {

                        if (theme.version.isBelow(SchedulerEnvironment.THEME_VERSION_1_0_1)) {

                        } else {
                            changeEndingLogo();
                        }
                    } else if (theme.resourceType.equals(ResourceType.ASSET)) {
                        changeEndingLogo();
                    }
                    if (isChangedEndingLogo) {
                        updateSchedulerVersion(SchedulerEnvironment.VERSION_1_0_3);
                    }
                } else {
                    updateSchedulerVersion(SchedulerEnvironment.VERSION_1_0_3);
                }

                initData();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void initData() {
        handleIntent(getIntent());

        mSubjectEText.setText(mTitle);
        mSubjectEText.setOnKeyListener(new EditMessageOnKeyListener());
//        mSubjectEText.addTextChangedListener(new CustomTextWatcher(mSubjectEText));
        mSubjectEText.setOnBackKeyPressListener(new StoryEditText.OnBackKeyPressListener() {
            @Override
            public void onBackKeyPress() {
                mSubjectEText.setText(mTitle);
                Editable e = mSubjectEText.getText();
                Selection.setSelection(e, mTitle.length());
            }
        });

        String themeName = sTheme;
        if(themeName.equals("Travel")){
            themeName = "Travel1";
        }
    }

    private class MakeJsonTask extends PriorityAsyncTask<Object, Object, Uri> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            showProgress();
            isMakeJson = true;
        }

        @Override
        protected Uri doInBackground(Object... params) {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            if (!VideoCreationService.getIsSavingMovieDiary() && !checkDataValidate(getIntent())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, getString(R.string.kiwiple_story_video_saving_error), Toast.LENGTH_SHORT);
                        onBackPressed();
                    }
                });
                return null;
            }else{
                Looper.myLooper().quit();
                return mJsonDataUri;
            }
        }

        @Override
        protected void onPostExecute(Uri uri) {
            isMakeJson = false;
            if(uri != null) {
                initData();
                setIntent(null);

                //hideProgress();

                /*/////////////////////////////////////
                //xx debug
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                saveVideoWork(false);
                            }
                        });
                    }
                }, 10000);
                /////////////////////////////////////*/
            }else{
                onBackPressed();
            }

        }
    }

    private ERROR_HANDLER makeJsonData(Intent intent) {
        try {
            if (intent == null)
                return ERROR_HANDLER.UNKNOWN_ERROR;

            mTitle = intent.getStringExtra("title");
            if (mTitle == null) {
                final Calendar c = Calendar.getInstance();
                StringBuilder sb = new StringBuilder();
                sb.append(c.get(Calendar.YEAR)).append(".");
                sb.append(c.get(Calendar.MONTH) + 1).append(".");
                sb.append(c.get(Calendar.DAY_OF_MONTH));
                mTitle = sb.toString();
            }

            Resolution res = (Resolution) intent.getSerializableExtra(SelectManager.SELECTED_RESOLUTION);
            mResolution = res == null ? Resolution.FHD : res;

            Storage.setDirectory(intent.getStringExtra(SelectManager.OUTPUT_DIR));

//            ArrayList<ImageResData> avItems = (ArrayList<ImageResData>) intent.getSerializableExtra(SelectManager.SELECTED_ITEMS);

            ArrayList<ImageData> photoData = (ArrayList<ImageData>) intent.getSerializableExtra(SelectManager.SELECTED_IMAGES);
            ArrayList<ImageData> videoData = (ArrayList<ImageData>) intent.getSerializableExtra(SelectManager.SELECTED_VIDEOS);

//            SmartLog.d("MovieEditMainActivity", "all size:" + avItems.size());

//            if(MovieContentApi.call_api == false){
//                // check av items validate
//                ERROR_HANDLER eh = MovieContentApi.checkDataValidate(mContext, avItems);
//                if (eh != ERROR_HANDLER.SUCCESS) {
//                    return eh;
//                }
//            }else{
//                MovieContentApi.call_api = false;
//            }

//            SmartLog.d("MovieEditMainActivity", "api photo size:" + MovieContentApi.photoData.size() + ", api video size:" + MovieContentApi.videoData.size());
//            SmartLog.d("MovieEditMainActivity", "##### finish....." );

            SmartLog.i("MovieEditMainActivity", "intent title:" + mTitle);
            String outputDir = Storage.getAppSpecificDirectory(MovieEditMainActivity.this);
            SmartLog.i("MovieEditMainActivity", "intent SelectManager.OUTPUT_DIR:" + outputDir);
            SmartLog.i("MovieEditMainActivity", "intent photoData size:" + photoData.size());
            SmartLog.i("MovieEditMainActivity", "intent videoData size:" + videoData.size());

            mJsonDataUri = null;
            mSchedulerManager = new SchedulerManager(mContext);
            mJsonDataUri = mSchedulerManager.makeJsonString(photoData, videoData, mTitle, false);

            PreviewManager previewManager = PreviewManager.getInstance(getApplicationContext());

            Visualizer.Editor vEditor = previewManager.getVisualizer().getEditor();
            if (!previewManager.getVisualizer().isOnEditMode())
                vEditor.start();

            if (vEditor != null)
                vEditor.setPreviewMode(true).finish();

        }catch(Exception e){
            e.printStackTrace();
            SmartLog.e("MovieEditMainActivity", "## MAIN Exception ## - " + e);
            return ERROR_HANDLER.UNKNOWN_ERROR;
        }

        return ERROR_HANDLER.SUCCESS;
    }




    private void releaseCursor() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
            mCursor = null;
        }
    }

    private int getJsonOrientation() {
        int orientation = 0;
        String[] projection = new String[]{
                StoryJsonDatabaseConstants.JsonDatabaseField.THUMB_ORIENTATION
        };

        try {
            mCursor = mContext.getContentResolver().query(mJsonDataUri, projection, null, null, null);
            if (mCursor != null && mCursor.moveToFirst()) {
                orientation = mCursor.getInt(mCursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.THUMB_ORIENTATION));
                mCursor.close();
                mCursor = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return orientation;
        }
        return orientation;
    }

    private void updateSchedulerVersion(SchedulerVersion version) {
        mStoryJsonPersister.updateJsonDataScheculderVersion(mJsonDataUri, version.toString());
    }

    private static class StoryFileName {
        private final SimpleDateFormat mFormat;
        // The date (in milliseconds) used to generate the last name.
        private long mLastDate;
        // Number of names generated for the same second.
        private int mSameSecondCount;

        private StoryFileName(String format) {
            mFormat = new SimpleDateFormat(format);
        }

        private String generateName(long dateTaken) {
            Date date = new Date(dateTaken);
            String result = mFormat.format(date);
            // If the last name was generated for the same second,
            // we append _1, _2, etc to the name.
            if (dateTaken / 1000 == mLastDate / 1000) {
                mSameSecondCount++;
                result += "_" + mSameSecondCount;
            } else {
                mLastDate = dateTaken;
                mSameSecondCount = 0;
            }
            return result;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

    }

    private void changeEndingLogo() {
        Theme theme = ThemeManager.getInstance(getApplicationContext()).getThemeByName(sTheme);

        PreviewManager previewManager = PreviewManager.getInstance(getApplicationContext());
        Visualizer visualizer = previewManager.getVisualizer();
        if (visualizer.isEmpty()) {
            try {
                mStoryPreviewLayout.setPreviewScriptWithoutUpdateView(mStoryJsonPersister.getJsonString(mJsonDataUri));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (RuntimeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        Visualizer.Editor visualizerEditor = null;
        if (!visualizer.isOnEditMode()) {
            visualizerEditor = visualizer.getEditor().start();
        }

        Region.Editor regionEditor = visualizer.getRegion().getEditor();

        List<Scene> scenes = visualizer.getRegion().getScenes();

        if (scenes == null || scenes.isEmpty() || scenes.size() == 0) {
            //씬 정보가 없을 경우, 프리뷰 삭제를 유도한다.
            if (visualizerEditor != null) {
                visualizerEditor.finish();
            }
            showInvalidDataPopup();

        } else {
            int duration = scenes.get(scenes.size() - 1).getDuration(); //마지막 scene의 duration을 저장.

            DummyScene.Editor dummySceneEditor = regionEditor.replaceScene(DummyScene.class, scenes.size() - 1).getEditor();
            dummySceneEditor.setDuration(duration);
            if (theme.resourceType.equals(ResourceType.DOWNLOAD)) {
                dummySceneEditor.setBackgroundFilePath(theme.combineDowloadImageFilePath(mContext, theme.endingLogo));
            } else {
                String packagePath = mContext.getFilesDir().getAbsolutePath();
                String endingLogoFilePath = null;
                endingLogoFilePath = new StringBuffer().append(packagePath).append(File.separator).append(theme.endingLogo).toString();
                dummySceneEditor.setBackgroundFilePath(endingLogoFilePath);
            }
            if (visualizerEditor != null) {
                visualizerEditor.finish();
            }

            String newScript = null;
            try {
                newScript = previewManager.toJsonObject().toString();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mStoryJsonPersister.updateJsonScriptData(mJsonDataUri, newScript);
            isChangedEndingLogo = true;
        }

    }



    private void saveVideoWork(final boolean bShareNext) {
        mProgressPercent.setText("00");
        mWaveProgress.setProgress(0);

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                mImm.hideSoftInputFromWindow(mSubjectEText.getWindowToken(), 0);
            }
        }, 100);

        saveVideo(mResolution, bShareNext);
    }

    /**
     #fixed 무비 다이어리 생성 / 팝업
     */
    private void saveVideo(Resolution resolution, boolean bShareNext) {
        StoryNotification.setCurrentJsonDataUri(mJsonDataUri);
        // This ensures the service is started and can outlive the activity.
        StoryCommonIntent.getInstance(getApplicationContext()).startSavingMDService();

        stopPreview();

        StringBuilder sb = new StringBuilder(storyFileName.generateName(System.currentTimeMillis()));
        sb.append("_");
        sb.append(new Random().nextInt(999999));
        currentSaveFileName = sb.toString();

        startSaveVideoUI();

        // Prepare the message data, but don't send it yet.
        // onServiceConnected will send it once the connection is established.
        Bundle data = new Bundle();
        data.putString(VideoCreationService.EXTRAS_KEY_VIDEO_FILE_NAME, currentSaveFileName);
        data.putSerializable(VideoCreationService.EXTRAS_KEY_RESOLUTION, resolution);
        data.putString(VideoCreationService.EXTRAS_KEY_JSONDATA_URI, mJsonDataUri.toString());
        String outputDir = Storage.getAppSpecificDirectory(MovieEditMainActivity.this);
        data.putString(VideoCreationService.EXTRAS_KEY_DIRECTORY, outputDir);
        data.putBoolean(VideoCreationService.EXTRAS_KEY_VIDEO_SHARE_RESERVE, false);
        
        mSaveMessageData = data;
        mSaveRequested = true;

        // Start and bind to the service. If already bound, the message will be sent.
        // If not, onServiceConnected will send it.
        if (!mIsServiceBound) {
            Intent serviceIntent = new Intent(this, VideoCreationService.class);
            serviceIntent.putExtra(VideoCreationService.EXTRAS_KEY_MESSENGER, mMessenger);
            
            // Start as foreground service first
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            
            // Then bind to get the messenger interface
            bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else if (mService1 != null) {
            // If already bound and connected, send the message immediately.
            sendMessageToService();
        }
    }

}
