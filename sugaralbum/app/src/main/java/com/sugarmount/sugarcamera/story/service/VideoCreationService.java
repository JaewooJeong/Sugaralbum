
package com.sugarmount.sugarcamera.story.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kiwiple.imageanalysis.utils.SmartLog;
import com.kiwiple.mediaframework.VideoEngineException;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.VideoFileFactory;
import com.kiwiple.multimedia.canvas.VideoFileFactoryListener;
import com.kiwiple.multimedia.exception.FileNotFoundException;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.sugarmount.sugarcamera.story.noti.StoryNotification;

import java.io.File;

/**
 * VideoCreationService.
 */
public class VideoCreationService extends Service {

    public static final int LOADING_ANIMATION_TRANSITION_MAX= 100;
    public static final String EXTRAS_KEY_MESSENGER = "com.kiwiple.story.extras.Messenger";
    public static final String EXTRAS_KEY_VIDEO_FILE_NAME = "com.kiwiple.story.extras.VideoFileName";
    public static final String EXTRAS_KEY_RESOLUTION = "com.kiwiple.story.extras.Resolution";
    public static final String EXTRAS_KEY_JSONDATA_URI = "com.kiwiple.story.extras.jsonUri";
    public static final String EXTRAS_KEY_VIDEO_SHARE_RESERVE = "com.kiwiple.story.extras.VideoShareReserve";
    public static final String EXTRAS_KEY_DIRECTORY = "com.kiwiple.story.extras.Directory";

    public static final String INTENT_ACTION_VIDEO_CREATION = "IntentActionVideoCreation";
    public static final String EXTRA_KEY_VIDEO_CREATE_RENDERED_FRAME_COUNT = "VideoCreateRenderedFrameCount";
    public static final String EXTRA_KEY_VIDEO_CREATE_TOTAL_FRAME_COUNT = "VideoCreateTotalFrameCount";
    public static final String EXTRA_KEY_VIDEO_CREATE_FINISH = "VideoCreateFinish";
    
    public static final int MESSAGE_ON_COMPLETE = 0;
    public static final int MESSAGE_ON_PROGRESS = 1;
    public static final int MESSAGE_ON_START = 2;
    public static final int MESSAGE_ON_COMPLETE_SHARE_NEXT = 3;
    public static final int MESSAGE_ON_SHARE_LIST_POP_UP = 4;

    public static final int MESSAGE_ON_ERROR_UNKNOWN = 5;
    public static final int MESSAGE_ON_ERROR_FILE_NOT_FOUND = 6;
    public static final int MESSAGE_ON_ERROR_VIDEO_ENGINE = 7;
    
    public static final int MESSAGE_START_CREATION = 100;
    public static final int MESSAGE_CANCEL_CREATION = 101;
    public static final int MESSAGE_IS_RENDERING = 102;

    public static String DUMP_FOLDER_PATH;
    private static final String VIDEO_FILE_EXTENSION = ".tmp";
    public static boolean isGuageOverOffSet =false;

    private Context mContext;
    private Messenger mClient;
    private Messenger mMessenger;

    private static boolean sIsRendering = false;
    
    private static Uri mJsonDataUri; 
    
//    PowerManager pm;
//    PowerManager.WakeLock wl;

    @Override
    public void onCreate() {
        mContext = VideoCreationService.this;
        mJsonDataUri = null; 
        mMessenger = new Messenger(mIncomingHandler);
//        DUMP_FOLDER_PATH = Storage.getDirectory();
        
//        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SavingDaily");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        mClient = intent.getParcelableExtra(EXTRAS_KEY_MESSENGER);

        Message message = new Message();
        message.replyTo = mMessenger;
        message.what = MESSAGE_ON_START;
        message.obj = sIsRendering;
//        DUMP_FOLDER_PATH = Storage.getDirectory();

        try {
            mClient.send(message);
        } catch(RemoteException exception) {
            exception.printStackTrace(); // Do nothing.
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mClient = intent.getParcelableExtra(EXTRAS_KEY_MESSENGER);
        return mMessenger.getBinder();
    }

    private String mVideoFilePath;
    
    private final Handler mIncomingHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

        	VideoFileFactory videoFileFactory = VideoFileFactory.getInstance(mContext);

            switch(msg.what) {
                case MESSAGE_START_CREATION:
//                	wl.acquire();
                    sIsRendering = true;

                    Bundle data = msg.getData();
                    String videoFileName = data.getString(EXTRAS_KEY_VIDEO_FILE_NAME);
                    Resolution resolution = (Resolution)data.getSerializable(EXTRAS_KEY_RESOLUTION);
                    mJsonDataUri = Uri.parse(data.getString(EXTRAS_KEY_JSONDATA_URI));
                    final boolean bShareNext = data.getBoolean(EXTRAS_KEY_VIDEO_SHARE_RESERVE);

                    DUMP_FOLDER_PATH = data.getString(EXTRAS_KEY_DIRECTORY);

                    SmartLog.i("MovieEditMainActivity", "DUMP_FOLDER_PATH:" + DUMP_FOLDER_PATH);

                    File folder = new File(DUMP_FOLDER_PATH);
                    if(!folder.exists()) {
                        folder.mkdirs();
                    }

                    mVideoFilePath = DUMP_FOLDER_PATH + File.separator + videoFileName + VIDEO_FILE_EXTENSION;

                    VideoFileFactoryListener videoFileFactoryListener = new VideoFileFactoryListener() {
                        @Override
                        public void onComplete() {
                        	isGuageOverOffSet = false;
                            sIsRendering = false;

                            if(mClient != null) {
                                Message message = new Message();
                                
                                
                                if(bShareNext) {
                                	message.what = MESSAGE_ON_COMPLETE_SHARE_NEXT;
                                    message.obj = mVideoFilePath;
                                }
                                else {
                                	message.what = MESSAGE_ON_COMPLETE;
                                }
                                
                                try {
                                    mClient.send(message);
                                } catch(RemoteException exception) {
                                    exception.printStackTrace(); // Do nothing.
                                }
                            }
                            
                            Intent i = new Intent(INTENT_ACTION_VIDEO_CREATION);
                            i.putExtra(EXTRA_KEY_VIDEO_CREATE_FINISH, true);
                            i.putExtra(EXTRA_KEY_VIDEO_CREATE_TOTAL_FRAME_COUNT, 0);
                            i.putExtra(EXTRA_KEY_VIDEO_CREATE_RENDERED_FRAME_COUNT, 0);
                            mContext.sendBroadcast(i);
//                            wl.release();
                        }

                        @Override
                        public void onError(Exception exception) {
                        	isGuageOverOffSet = false;
                            sIsRendering = false;

                            // TODO how to do
//                            StoryNotification.updateCreateStoryNotification(mContext, videoFilePath);
//                            Toast.makeText(VideoCreationService.this, "onError()",Toast.LENGTH_LONG).show();

                            if (mClient != null) {

                            	Message message = new Message();
                               
                                if (exception instanceof FileNotFoundException) {
                                	message.what = MESSAGE_ON_ERROR_FILE_NOT_FOUND;
                                } else if (exception instanceof VideoEngineException) {
                                	message.what = MESSAGE_ON_ERROR_VIDEO_ENGINE;
                                } else {
                                	message.what = MESSAGE_ON_ERROR_UNKNOWN;
                                }
                                message.obj = mVideoFilePath;

                                try {
                                    mClient.send(message);
                                } catch(RemoteException remoteException) {
                                    remoteException.printStackTrace(); // Do nothing.
                                }
                            }
//                            wl.release();

                        }

                        @Override
                        public void onProgressUpdate(int totalFrameCount, int renderedFrameCount) {
                            int progress = (int)100 * renderedFrameCount / totalFrameCount;

                            // keylime : fake progress rate display
                            int realValue = 1, fakeValue = 3, fakeProgress = progress;

                            if(fakeProgress >= 0 && fakeProgress <= realValue) {
                                fakeProgress = fakeProgress * fakeValue / realValue;
                            } else {
                                fakeProgress = fakeValue + (fakeProgress - realValue) / ((100 - realValue) / (100 - fakeValue));
                            }
                            
                            if(fakeProgress > 90)
                            	isGuageOverOffSet = true;
                            if(fakeProgress <= 100)
                            	StoryNotification.updateCreateStoryNotification(mContext, fakeProgress);
                            totalFrameCount = LOADING_ANIMATION_TRANSITION_MAX;

                            if(mClient != null) {
                                Message message = new Message();
                                message.what = MESSAGE_ON_PROGRESS;
                                message.arg1 = totalFrameCount;
                                message.arg2 = fakeProgress;
                                try {
                                    mClient.send(message);
                                } catch(RemoteException exception) {
                                    exception.printStackTrace(); // Do nothing.
                                }
                            }
                            /**
                             * 저장 Ani 종료후 재진입시 루틴 
                             */
                            /*
                            Intent i = new Intent(INTENT_ACTION_VIDEO_CREATION);
                            i.putExtra(EXTRA_KEY_VIDEO_CREATE_TOTAL_FRAME_COUNT, totalFrameCount);
                            i.putExtra(EXTRA_KEY_VIDEO_CREATE_RENDERED_FRAME_COUNT, fakeProgress);
                            mContext.sendBroadcast(i);
                            */
                        }
                    };

                    PreviewManager previewManager = PreviewManager.getInstance(mContext);
                    videoFileFactory.setListener(videoFileFactoryListener);
                    videoFileFactory.create(previewManager, resolution, mVideoFilePath);
                    
                    break;
                case MESSAGE_CANCEL_CREATION:
                	videoFileFactory.cancel();
//                    wl.release();
                    break;
                default:
                    // Do nothing.
                    break;
                    
            }
        }
    };
    
    public static boolean getIsSavingMovieDiary() {
        return sIsRendering;
    }

}
