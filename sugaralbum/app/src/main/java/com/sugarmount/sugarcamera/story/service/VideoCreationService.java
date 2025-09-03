
package com.sugarmount.sugarcamera.story.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
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
    private boolean mServiceStopped = false;

    private static boolean sIsRendering = false;
    
    private static Uri mJsonDataUri; 
    private static Uri mVideoUri;
    
    PowerManager pm;
    PowerManager.WakeLock wl;
    
    private int mCurrentStartId = -1;
    private boolean mVideoCreationInProgress = false;

    @Override
    public void onCreate() {
        mContext = VideoCreationService.this;
        mJsonDataUri = null;
        mMessenger = new Messenger(mIncomingHandler);
        // DUMP_FOLDER_PATH will be set from app-specific directory

        try {
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SugarAlbum:VideoCreation");
        } catch (Exception e) {
            SmartLog.e("VideoCreationService", "Failed to create WakeLock", e);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        SmartLog.i("VideoCreationService", "onStartCommand called with startId: " + startId);
        mCurrentStartId = startId;  // Store startId to control service lifecycle
        mServiceStopped = false;    // Reset flag when service starts

        if (intent == null) {
            // Service restarted unexpectedly - let system redeliver the original intent
            SmartLog.w("VideoCreationService", "Intent is null, waiting for system to redeliver original intent");
            return START_REDELIVER_INTENT;
        }

        mClient = intent.getParcelableExtra(EXTRAS_KEY_MESSENGER);

        // Start foreground service using centralized notification management
        try {
            SmartLog.i("VideoCreationService", "Starting foreground service in onStartCommand");
            // Ensure notification channel is created before creating notification
            StoryNotification.createNotificationChannel(this);
            Notification notification = StoryNotification.createVideoCreationNotification(this, 0);
            
            if (notification == null) {
                SmartLog.e("VideoCreationService", "Failed to create notification - stopping service");
                stopSelf();
                return START_NOT_STICKY;
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                startForeground(StoryNotification.NOTIFICATION_ID_CREATE_STORY, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE);
                SmartLog.i("VideoCreationService", "Foreground service started with SHORT_SERVICE type");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(StoryNotification.NOTIFICATION_ID_CREATE_STORY, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING);
                SmartLog.i("VideoCreationService", "Foreground service started with MEDIA_PROCESSING type");
            } else {
                startForeground(StoryNotification.NOTIFICATION_ID_CREATE_STORY, notification);
                SmartLog.i("VideoCreationService", "Foreground service started");
            }
        } catch (Exception e) {
            SmartLog.e("VideoCreationService", "Failed to start foreground service in onStartCommand", e);
            return START_NOT_STICKY;
        }

        if (mClient != null) {
            Message message = new Message();
            message.replyTo = mMessenger;
            message.what = MESSAGE_ON_START;
            message.obj = sIsRendering;

            try {
                mClient.send(message);
            } catch (RemoteException exception) {
                exception.printStackTrace(); // Do nothing.
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mClient = intent.getParcelableExtra(EXTRAS_KEY_MESSENGER);
        return mMessenger.getBinder();
    }
    
    @Override
    public void onDestroy() {
        SmartLog.i("VideoCreationService", "onDestroy called - Video creation in progress: " + mVideoCreationInProgress);
        mServiceStopped = true;  // Mark service as stopped
        
        // If video creation is still in progress, try to prevent destruction
        if (mVideoCreationInProgress) {
            SmartLog.w("VideoCreationService", "Service being destroyed while video creation is in progress!");
            // Force restart the service
            Intent restartIntent = new Intent(this, VideoCreationService.class);
            startService(restartIntent);
        }
        
        super.onDestroy();
    }
    
    // Android 15: Handle foreground service timeout
    @Override
    public void onTimeout(int startId, int fgsType) {
        SmartLog.w("VideoCreationService", "Service timeout reached - startId: " + startId + ", fgsType: " + fgsType);
        
        // If video creation is still in progress, let user know
        if (mVideoCreationInProgress) {
            SmartLog.w("VideoCreationService", "Video creation still in progress during timeout");
            
            // Use centralized notification management for timeout
            StoryNotification.removeVideoCreationNotification(this);
            
            // Send error message to client
            if (mClient != null) {
                Message errorMessage = new Message();
                errorMessage.what = MESSAGE_ON_ERROR_UNKNOWN;
                errorMessage.obj = "Service timeout - video creation taking too long";
                try {
                    mClient.send(errorMessage);
                } catch (RemoteException e) {
                    SmartLog.e("VideoCreationService", "Failed to send timeout error message", e);
                }
            }
        }
        
        // Clean up and stop
        mVideoCreationInProgress = false;
        releaseWakeLockAndStopService();
        super.onTimeout(startId, fgsType);
        
        // Release WakeLock when service is destroyed
        try {
            if (wl != null && wl.isHeld()) {
                wl.release();
                SmartLog.i("VideoCreationService", "WakeLock released on service destroy");
            }
        } catch (Exception e) {
            SmartLog.e("VideoCreationService", "Failed to release WakeLock on destroy", e);
        }
        
        SmartLog.i("VideoCreationService", "Service destroyed");
    }

    private String mVideoFilePath;
    
    @SuppressLint("HandlerLeak")
    private final Handler mIncomingHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            // Early exit if service has been stopped - prevent race conditions
            if (mServiceStopped) {
                SmartLog.w("VideoCreationService", "Handler message received but service is stopped - ignoring message: " + msg.what);
                return;
            }

        	VideoFileFactory videoFileFactory = VideoFileFactory.getInstance(mContext);

            switch(msg.what) {
                case MESSAGE_START_CREATION:
                    SmartLog.i("VideoCreationService", "=== VIDEO CREATION START ===");
                    SmartLog.i("VideoCreationService", "MESSAGE_START_CREATION received");
                    SmartLog.i("VideoCreationService", "Service state - stopped: " + mServiceStopped + ", rendering: " + sIsRendering);
                    
                    // Double-check service state after acquiring message
                    if (mServiceStopped) {
                        SmartLog.w("VideoCreationService", "Service stopped during message processing - aborting");
                        return;
                    }
                    
//                	wl.acquire();
                    sIsRendering = true;

                    Bundle data = msg.getData();
                    String videoFileName = data.getString(EXTRAS_KEY_VIDEO_FILE_NAME);
                    Resolution resolution = (Resolution)data.getSerializable(EXTRAS_KEY_RESOLUTION);
                    mJsonDataUri = Uri.parse(data.getString(EXTRAS_KEY_JSONDATA_URI));
                    final boolean bShareNext = data.getBoolean(EXTRAS_KEY_VIDEO_SHARE_RESERVE);

                    DUMP_FOLDER_PATH = data.getString(EXTRAS_KEY_DIRECTORY);
                    
                    SmartLog.i("VideoCreationService", "Video creation parameters:");
                    SmartLog.i("VideoCreationService", "  - videoFileName: " + videoFileName);
                    SmartLog.i("VideoCreationService", "  - resolution: " + resolution);
                    SmartLog.i("VideoCreationService", "  - jsonDataUri: " + mJsonDataUri);
                    SmartLog.i("VideoCreationService", "  - directory: " + DUMP_FOLDER_PATH);
                    SmartLog.i("VideoCreationService", "  - shareNext: " + bShareNext);

                    SmartLog.i("MovieEditMainActivity", "DUMP_FOLDER_PATH:" + DUMP_FOLDER_PATH);

                    // Use app-specific directory passed from MovieEditMainActivity
                    String fileName = videoFileName + VIDEO_FILE_EXTENSION;
                    
                    SmartLog.i("VideoCreationService", "DUMP_FOLDER_PATH: " + DUMP_FOLDER_PATH);
                    
                    if (DUMP_FOLDER_PATH != null && !DUMP_FOLDER_PATH.isEmpty()) {
                        // Create the output directory if it doesn't exist
                        java.io.File outputDir = new java.io.File(DUMP_FOLDER_PATH);
                        if (!outputDir.exists()) {
                            boolean created = outputDir.mkdirs();
                            SmartLog.i("VideoCreationService", "Directory created: " + created + " at " + DUMP_FOLDER_PATH);
                        }
                        
                        mVideoFilePath = DUMP_FOLDER_PATH + "/" + fileName;
                        SmartLog.i("VideoCreationService", "Using direct file path: " + mVideoFilePath);
                        
                        // Clear MediaStore URI since we're using direct file path
                        mVideoUri = null;
                    } else {
                        SmartLog.e("VideoCreationService", "DUMP_FOLDER_PATH is null or empty, using fallback");
                        // Fallback to app-specific directory
                        mVideoFilePath = com.sugarmount.sugarcamera.MediaStoreHelper.createFallbackVideoPath(
                            VideoCreationService.this, fileName
                        );
                        mVideoUri = null;
                        SmartLog.i("VideoCreationService", "Using fallback path: " + mVideoFilePath);
                    }

                    // Acquire WakeLock to prevent system from sleeping during video creation
                    try {
                        if (wl != null && !wl.isHeld()) {
                            wl.acquire(30*60*1000L /*30 minutes*/);
                            SmartLog.i("VideoCreationService", "WakeLock acquired for video creation (30 min timeout)");
                        }
                    } catch (Exception e) {
                        SmartLog.e("VideoCreationService", "Failed to acquire WakeLock", e);
                    }


                    VideoFileFactoryListener videoFileFactoryListener = new VideoFileFactoryListener() {
                        @Override
                        public void onComplete() {
                            SmartLog.i("VideoCreationService", "=== VIDEO CREATION COMPLETE ===");
                            SmartLog.i("VideoCreationService", "VideoFileFactoryListener.onComplete() called");
                            SmartLog.i("VideoCreationService", "Final video path: " + mVideoFilePath);
                            isGuageOverOffSet = false;
                            sIsRendering = false;

                            // The Activity will handle the file finalization. Just send a simple completion message.
                            if(mClient != null) {
                                Message message = new Message();
                                message.what = MESSAGE_ON_COMPLETE;
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

                            PreviewManager.getInstance(mContext).release();

                            // Remove the video creation notification on completion
                            StoryNotification.removeVideoCreationNotification(VideoCreationService.this);
                            
                            // Release WakeLock on completion
                            try {
                                if (wl != null && wl.isHeld()) {
                                    wl.release();
                                    SmartLog.i("VideoCreationService", "WakeLock released on completion");
                                }
                            } catch (Exception e) {
                                SmartLog.e("VideoCreationService", "Failed to release WakeLock on completion", e);
                            }
                            
                            mVideoCreationInProgress = false;  // Mark completion
                            stopForeground(false);
                            if (mCurrentStartId != -1) {
                                SmartLog.i("VideoCreationService", "Stopping service with startId: " + mCurrentStartId);
                                stopSelf(mCurrentStartId);
                            } else {
                                stopSelf();
                            }
                        }

                        @Override
                        public void onError(Exception exception) {
                            SmartLog.e("VideoCreationService", "=== VIDEO CREATION ERROR ===");
                            SmartLog.e("VideoCreationService", "VideoFileFactoryListener.onError() called", exception);
                            SmartLog.e("VideoCreationService", "Error type: " + exception.getClass().getSimpleName());
                            SmartLog.e("VideoCreationService", "Error message: " + exception.getMessage());
                            isGuageOverOffSet = false;
                            sIsRendering = false;

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

                            PreviewManager.getInstance(mContext).release();

                            // Remove the video creation notification on error
                            StoryNotification.removeVideoCreationNotification(VideoCreationService.this);
                            
                            // Release WakeLock on error
                            try {
                                if (wl != null && wl.isHeld()) {
                                    wl.release();
                                    SmartLog.i("VideoCreationService", "WakeLock released on error");
                                }
                            } catch (Exception e) {
                                SmartLog.e("VideoCreationService", "Failed to release WakeLock on error", e);
                            }
                            
                            mVideoCreationInProgress = false;  // Mark completion even on error
                            stopForeground(false);
                            if (mCurrentStartId != -1) {
                                SmartLog.i("VideoCreationService", "Stopping service with startId after error: " + mCurrentStartId);
                                stopSelf(mCurrentStartId);
                            } else {
                                stopSelf();
                            }
                        }

                        @Override
                        public void onProgressUpdate(int totalFrameCount, int renderedFrameCount) {
//                            SmartLog.i("VideoCreationService", "=== PROGRESS UPDATE ===");
//                            SmartLog.i("VideoCreationService", "Rendered frames: " + renderedFrameCount);
                            SmartLog.i("VideoCreationService", "Total frames: " + totalFrameCount);
                            int progress = totalFrameCount > 0 ? (int)(100L * renderedFrameCount / totalFrameCount) : 0;
                            SmartLog.i("VideoCreationService", "Real progress: " + progress + "%");

                            // keylime : fake progress rate display
                            int realValue = 1, fakeValue = 3, fakeProgress = progress;

                            if(fakeProgress >= 0 && fakeProgress <= realValue) {
                                fakeProgress = fakeProgress * fakeValue / realValue;
                            } else {
                                fakeProgress = fakeValue + (fakeProgress - realValue) / ((100 - realValue) / (100 - fakeValue));
                            }
                            
//                            SmartLog.i("VideoCreationService", "Fake progress: " + fakeProgress + "% (real: " + progress + "%)");
                            
                            if(fakeProgress > 90)
                            	isGuageOverOffSet = true;

                            // Update progress using centralized notification management
                            Notification progressNotification = StoryNotification.createVideoCreationNotification(mContext, fakeProgress);
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            if (notificationManager != null) {
                                notificationManager.notify(StoryNotification.NOTIFICATION_ID_CREATE_STORY, progressNotification);
                            }

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

                    try {
                        String jsonScript = previewManager.toJsonObject().toString(2);
//                        SmartLog.i("VideoCreationService", "JSON Script to be processed:\n" + jsonScript);
                        com.kiwiple.multimedia.canvas.Visualizer visualizer = previewManager.getVisualizer();
                        if (visualizer != null) {
                            int sceneCount = visualizer.getRegion().getScenes().size();
                            SmartLog.i("VideoCreationService", "Number of scenes: " + sceneCount);
                            if (sceneCount == 0) {
                                SmartLog.e("VideoCreationService", "CRITICAL: No scenes found in PreviewManager. Video creation will likely fail.");
                            }
                        } else {
                            SmartLog.e("VideoCreationService", "CRITICAL: Visualizer is null.");
                        }
                    } catch (Exception e) {
                        SmartLog.e("VideoCreationService", "Could not get debug info from PreviewManager", e);
                    }

                    SmartLog.i("VideoCreationService", "PreviewManager instance: " + previewManager);
                    SmartLog.i("VideoCreationService", "PreviewManager instance: " + previewManager);
                    SmartLog.i("VideoCreationService", "Resolution: " + resolution);
                    SmartLog.i("VideoCreationService", "Final video path: " + mVideoFilePath);
                    SmartLog.i("VideoCreationService", "JSON data URI: " + mJsonDataUri);
                    
                    videoFileFactory.setListener(videoFileFactoryListener);
                    SmartLog.i("VideoCreationService", "=== STARTING VIDEO FILE FACTORY ===");
                    SmartLog.i("VideoCreationService", "Starting video creation with videoFileFactory.create()");
                    SmartLog.i("VideoCreationService", "Thread: " + Thread.currentThread().getName() + " (ID: " + Thread.currentThread().getId() + ")");
                    
                    try {
                        SmartLog.i("VideoCreationService", "About to call videoFileFactory.create() with parameters:");
                        SmartLog.i("VideoCreationService", "  - PreviewManager: " + (previewManager != null ? "OK" : "NULL"));
                        SmartLog.i("VideoCreationService", "  - Resolution: " + resolution);
                        SmartLog.i("VideoCreationService", "  - Output path: " + mVideoFilePath);
                        
                        boolean success = videoFileFactory.create(previewManager, resolution, mVideoFilePath);
                        SmartLog.i("VideoCreationService", "videoFileFactory.create() returned: " + success);
                        
                        if (success) {
                            mVideoCreationInProgress = true;  // Mark video creation as in progress
                            SmartLog.i("VideoCreationService", "videoFileFactory.create() called successfully - AsyncTask started");
                            SmartLog.i("VideoCreationService", "Service will remain alive until AsyncTask completes (startId: " + mCurrentStartId + ")");
                            // CRITICAL: Don't call stopSelf() here - let the AsyncTask callbacks handle service lifecycle
                        } else {
                            SmartLog.e("VideoCreationService", "CRITICAL: videoFileFactory.create() returned false");
                            // Send error message to client
                            if (mClient != null) {
                                Message errorMessage = new Message();
                                errorMessage.what = MESSAGE_ON_ERROR_UNKNOWN;
                                errorMessage.obj = "Failed to start video creation - factory returned false";
                                try {
                                    mClient.send(errorMessage);
                                } catch (RemoteException re) {
                                    SmartLog.e("VideoCreationService", "Failed to send error message", re);
                                }
                            }
                            // Only stop service on failure, not on success
                            releaseWakeLockAndStopService();
                        }
                    } catch (Exception e) {
                        SmartLog.e("VideoCreationService", "Error calling videoFileFactory.create(): " + e.getMessage(), e);
                        // Send error message to client
                        if (mClient != null) {
                            Message errorMessage = new Message();
                            errorMessage.what = MESSAGE_ON_ERROR_UNKNOWN;
                            errorMessage.obj = "Failed to start video creation: " + e.getMessage();
                            try {
                                mClient.send(errorMessage);
                            } catch (RemoteException re) {
                                SmartLog.e("VideoCreationService", "Failed to send error message", re);
                            }
                        }
                        // Only stop service on exception, not on success
                        releaseWakeLockAndStopService();
                    }
                    
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
    
    private void releaseWakeLockAndStopService() {
        try {
            if (wl != null && wl.isHeld()) {
                wl.release();
                SmartLog.i("VideoCreationService", "WakeLock released before stopping service");
            }
        } catch (Exception e) {
            SmartLog.e("VideoCreationService", "Failed to release WakeLock before stopping service", e);
        }
        
        mVideoCreationInProgress = false;
        stopForeground(false);
        if (mCurrentStartId != -1) {
            SmartLog.i("VideoCreationService", "Stopping service with startId in helper method: " + mCurrentStartId);
            stopSelf(mCurrentStartId);
        } else {
            stopSelf();
        }
    }

}
