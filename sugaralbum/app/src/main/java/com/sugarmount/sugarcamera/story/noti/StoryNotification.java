
package com.sugarmount.sugarcamera.story.noti;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.kiwiple.debug.L;
import com.kiwiple.imageframework.util.SmartLog;
import com.sugarmount.sugaralbum.R;
import com.sugarmount.sugarcamera.story.service.UplusStorySavingService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class StoryNotification {
    private static final String CHANNEL_ID = "ForegroundServiceChannelkiki";
    private static final int NOTIFICATION_ID_MANUAL_CREATE_STORY = 2;

    //음악 분석용 
    public static final int NOTIFICATION_ID_ON_GOING_MUSIC_ANALYSIS = 1;
    
    //동영상 생성용  
    public static final int NOTIFICATION_ID_CREATE_STORY = 1001;

    private static final int THUMBNAIL_SIZE = 300;

    private static NotificationCompat.Builder mBuilder;

    private static Uri mCurrentJsonUri;

    public static void removeManualStoryNotification(Context context) {
        createNotificationChannel(context).cancel(NOTIFICATION_ID_MANUAL_CREATE_STORY);
    }

    public static void removeAnalysisMusicNotification(Context context) {
        createNotificationChannel(context).cancel(NOTIFICATION_ID_ON_GOING_MUSIC_ANALYSIS);
    }

    public static void removeVideoCreationNotification(Context context) {
        createNotificationChannel(context).cancel(NOTIFICATION_ID_CREATE_STORY);
    }

    private static void setSmallIcon(NotificationCompat.Builder builder){
    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
    		builder.setSmallIcon(R.drawable.noti_icon_white);
    	}else{
    		builder.setSmallIcon(R.drawable.ico_utv_jikcam);
    	}
    }

    private static void setSmallIcon(Notification.Builder builder){
    	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
    		builder.setSmallIcon(R.drawable.noti_icon_white);
    	}else{
    		builder.setSmallIcon(R.drawable.ico_utv_jikcam);
    	}
    }

    public static void setCurrentJsonDataUri(Uri uri) {
    	mCurrentJsonUri = uri;
    }

    // 수동 생성 시작
    @RequiresApi(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
    public static void getCreateStoryNotification(Context context) {
        removeManualStoryNotification(context);


        Intent dummyIntent = new Intent();
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, dummyIntent, PendingIntent.FLAG_IMMUTABLE);

        mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        setSmallIcon(mBuilder);
        Bitmap banner = decodeFromUri(context, mCurrentJsonUri);
        // banner = getCroppedBitmap(getJsonOrientation(context,uri), banner, context);
        banner = getScaledForScreenDimension(banner, context);
        mBuilder.setLargeIcon(banner);
        mBuilder.setTicker(context.getString(R.string.kiwiple_story_story_saving_progressbar_text));
        mBuilder.setContentTitle(context.getString(R.string.kiwiple_story_story_movie_diary));
        mBuilder.setContentText(context.getString(R.string.kiwiple_story_story_saving_progressbar_text));
        mBuilder.setProgress(100, 0, false);
        // 20150409 olive : 퀵 커버 미니뷰에서 Notification이 반복적이로 호출되면서 animation이 발생하는 문제가 있어서 설정.
        mBuilder.setOngoing(true);
        mBuilder.setContentIntent(pIntent);
        mBuilder.setPriority(PRIORITY_MIN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ((UplusStorySavingService)context).startForeground(NOTIFICATION_ID_MANUAL_CREATE_STORY, mBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING);
        }else {
            ((UplusStorySavingService)context).startForeground(NOTIFICATION_ID_MANUAL_CREATE_STORY, mBuilder.build());
        }
    }

    // 동영상 생성 서비스용 알림 생성
    public static Notification createVideoCreationNotification(Context context, int progress) {
        Intent dummyIntent = new Intent();
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, dummyIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        setSmallIcon(builder);
        builder.setTicker(context.getString(R.string.kiwiple_story_story_saving_progressbar_text));
        builder.setContentTitle(context.getString(R.string.kiwiple_story_story_movie_diary));
        builder.setContentText(context.getString(R.string.kiwiple_story_story_saving_progressbar_text));
        builder.setProgress(100, progress, false);
        builder.setOngoing(true);
        builder.setContentIntent(pIntent);
        builder.setPriority(PRIORITY_MIN);
        
        return builder.build();
    }

    static int tmpProgress = -1;

    // 수동 생성 완료
    public static void completeCreateStoryNotification(Context context, String filePath,
                                                       Uri jsonUri, int storyOrientation) {

        if(mCurrentJsonUri == null){
            mCurrentJsonUri = jsonUri;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(filePath);

        /*
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setDataAndType(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file), "video/mp4");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "video/mp4");
        }
        */
        intent.setDataAndType(Uri.fromFile(file), "video/mp4");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        mBuilder.setContentTitle(context.getString(R.string.kiwiple_story_noti_manually_creation_title));
        mBuilder.setContentText(context.getString(R.string.kiwiple_story_noti_manually_creation_summary)); // fake
        mBuilder.setTicker(context.getString(R.string.kiwiple_story_noti_manually_creation_ticker));
        setSmallIcon(mBuilder);
        Bitmap banner = decodeFromUri(context, mCurrentJsonUri);

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
        style.setBigContentTitle(context.getString(R.string.kiwiple_story_noti_manually_creation_title));
        style.setSummaryText(context.getString(R.string.kiwiple_story_noti_manually_creation_summary));
        style.bigPicture(banner);
        mBuilder.setStyle(style);

        banner = getScaledForScreenDimension(banner, context);
        mBuilder.setLargeIcon(banner);

        try {
            /**
             * PendingIntent.FLAG_ONE_SHOT 속성 사용후 이벤트 처리 없이 중복 생성되면 이전 extra value가 남는다.
             */
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE);
            mBuilder.setContentIntent(pIntent);
            mBuilder.setAutoCancel(true);

            createNotificationChannel(context).notify(NOTIFICATION_ID_MANUAL_CREATE_STORY, mBuilder.build());

            mCurrentJsonUri = null;

        }catch(Exception e){
            SmartLog.e("##########" , "ex:"+e);
        }

    }

    // 수동 생성 중
    public static void updateCreateStoryNotification(Context context, int progress) {

        if(mBuilder == null) return;

        if(tmpProgress == -1) {
            tmpProgress = progress;
        } else {
            if(tmpProgress == progress)
                return;
            else
                tmpProgress = progress;
        }
        mBuilder.setProgress(100, progress, false);
        mBuilder.setContentInfo(progress + "%");

        createNotificationChannel(context).notify(NOTIFICATION_ID_MANUAL_CREATE_STORY, mBuilder.build());

    }

    private static Bitmap decodeFromUri(Context mContext, Uri uri) {
        InputStream input;
        Bitmap bitmap = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);

            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();
            if((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
                return null;
            }

            int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
                    : onlyBoundsOptions.outWidth;
            double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
            input = mContext.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            L.i("bitmap  = " + bitmap.getWidth() + ", bitmap = " + bitmap.getHeight());
            input.close();

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k == 0) {
            return 1;
        } else {
            return k;
        }
    }

    private static Bitmap getScaledForScreenDimension(Bitmap bitmap, Context context) {
        Bitmap croppedBitmap = null;
        Resources res = context.getResources();
        try {

            int width = (int)res.getDimension(android.R.dimen.notification_large_icon_width);
            int height = (int)res.getDimension(android.R.dimen.notification_large_icon_height);

            if(bitmap.getWidth() >= bitmap.getHeight()) {

                croppedBitmap = Bitmap.createBitmap(bitmap,
                                                    bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                                                    0, bitmap.getHeight(), bitmap.getHeight());

            } else {
                croppedBitmap = Bitmap.createBitmap(bitmap, 0,
                                                    bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                                                    bitmap.getWidth(), bitmap.getWidth());
            }
            // 단말 large icon 사이즈로 스케 일
            // croppedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return croppedBitmap;
    }


    public static NotificationManager createNotificationChannel(Context context) {
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "SugarAlbum foreground service",
                    NotificationManager.IMPORTANCE_MIN
            );
            manager.createNotificationChannel(serviceChannel);
        }
        return manager;
    }
	
}
