package com.sugarmount.sugarcamera.api;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.search.ImageSearch;
import com.kiwiple.mediaframework.preview.PreviewVideoDecoder;
import com.kiwiple.multimedia.util.ImageUtils;
import com.sugarmount.sugarcamera.ImageResData;
import com.sugarmount.sugarcamera.kiwiple.util.BitmapUtils;
import com.sugarmount.sugarcamera.story.gallery.ItemMediaData;
import com.sugarmount.sugarcamera.story.gallery.RULES;
import com.sugarmount.sugarcamera.story.movie.MovieEditMainActivity.ERROR_HANDLER;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * 선택된 이미지, 비디오의 무비다이어리 적합성 정보를 제공 하기 위한 API 클래스
 */
public class MovieContentApi {
    private static final int INVALID_VALUE = -1;
    private static final int ONE_VIDEO_SCENE=  1;
    private static final int TWO_VIDEO_SCENE = 2;

    private static Context mContext;

    public static ArrayList<ImageData> photoData = new ArrayList<>();
    public static ArrayList<ImageData> videoData = new ArrayList<>();

    public static boolean call_api = false;

    /**
     * 선택된 이미지, 비디오 정보를 전달하여 적합성을 확인하고 ERROR_HANDLER 를 리턴한다.
     * @param context
     * @param avItems
     * @return ERROR_HANDLER
     */
    public static ERROR_HANDLER checkDataValidate(Context context, ArrayList<ImageResData> avItems){
        RULES.init();
        mContext = context;
        call_api = true;

        if(mContext != null) {
            try {
                photoData = new ArrayList<>();
                videoData = new ArrayList<>();

                if (avItems.size() == 0)
                    return ERROR_HANDLER.NOT_FOUND;

                ArrayList<ItemMediaData> imageData = getImageDataForUser(avItems);
                for (int i = 0; i < imageData.size(); i++) {
                    if (imageData.get(i).isVideo) {
                        if (imageData.get(i).duration >= RULES.MAX_DURATION_PER_VIDEO)
                            return ERROR_HANDLER.VIDEO_MAX_DURATION;
                        if (imageData.get(i).duration <= RULES.MIN_DURATION_PER_VIDEO)
                            return ERROR_HANDLER.VIDEO_MIN_DURATION;
                    }
                }

                photoData = getImageDataForEdit(imageData);
                videoData = getVideoDataForEdit(imageData);

                if (videoData.size() > RULES.getMakeClipMaxVideoCount()) {
                    return ERROR_HANDLER.VIDEO_COUNT;
                }

                if (photoData.size() <= 4 || photoData.size() > RULES.getMakeClipMaxPhotoCount()) {
                    return ERROR_HANDLER.IMAGE_COUNT;
                }

                CodecInspectorTask CodecInspectorTask = new CodecInspectorTask(MovieContentApi.videoData);
                if(CodecInspectorTask.doWork() == false)
                    return ERROR_HANDLER.CODEC_ERROR;

                return ERROR_HANDLER.SUCCESS;
            }catch(Exception ex){
                return ERROR_HANDLER.UNKNOWN_ERROR;
            }
        }else{
            return ERROR_HANDLER.UNKNOWN_ERROR;
        }
    }

    private static ArrayList<ItemMediaData> getImageDataForUser(ArrayList<ImageResData> mMediaDataList){
        LoadMediaDataThread mLoadMediaDataThread = new LoadMediaDataThread();
        mLoadMediaDataThread.mTmpDataList = mMediaDataList;
        mLoadMediaDataThread.start();

        int nPos = 0;
        while(nPos++ < 10000 && !mLoadMediaDataThread.isFinish) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //SmartLog.e(MovieEditMainActivity.class.getSimpleName(), "Wait for single thread (get image)", e);
            }
        }

        if(mLoadMediaDataThread.mMediaDataList.size() <= 0 || !mLoadMediaDataThread.isFinish) {
            return null;
        }else{
            return mLoadMediaDataThread.mMediaDataList;
        }
    }

    private static ArrayList<ImageData> getImageDataForEdit(ArrayList<ItemMediaData> mMediaDataList) {
        if (mMediaDataList.size() <= 0) {
            return null;
        }

        ArrayList<ImageData> imageDatas = new ArrayList<ImageData>();
        ImageData imageData = null;
        ImageSearch imageSearch = new ImageSearch(mContext, null);
        for (int i = 0; i < mMediaDataList.size(); i++) {
            if (!mMediaDataList.get(i).isVideo) {
                imageData = imageSearch.getImagaeDataForImageId(mMediaDataList.get(i)._id.intValue());

                if(imageData == null){
                    imageData = new ImageData();
                    imageData.id = mMediaDataList.get(i)._id.intValue();
                    imageData.path = mMediaDataList.get(i).contentPath;
                    imageData.fileName = mMediaDataList.get(i).displayName;
                    imageData.date = mMediaDataList.get(i).date;
                    imageData.orientation = mMediaDataList.get(i).degrees + "";
                    if(mMediaDataList.get(i).width == 0 || mMediaDataList.get(i).height == 0){
                        imageData.width = ImageUtils.measureImageSize(mMediaDataList.get(i).contentPath).width;
                        imageData.height = ImageUtils.measureImageSize(mMediaDataList.get(i).contentPath).height;
                    }else{
                        imageData.width = mMediaDataList.get(i).width;
                        imageData.height = mMediaDataList.get(i).height;
                    }
                }else{

                    if(imageData.width == 0 || imageData.height == 0){
                        imageData.width = ImageUtils.measureImageSize(mMediaDataList.get(i).contentPath).width;
                        imageData.height = ImageUtils.measureImageSize(mMediaDataList.get(i).contentPath).height;
                    }
                }

                if (imageData != null) {
                    imageDatas.add(imageData);
                }
            }
        }
        return imageDatas;
    }

    private static ArrayList<ImageData> getVideoDataForEdit(ArrayList<ItemMediaData> mMediaDataList) {
        if (mMediaDataList.size() <= 0) {
            return null;
        }

        ArrayList<ImageData> videoDatas = new ArrayList<ImageData>();
        ImageData videoData = null;
        for (int i = 0; i < mMediaDataList.size(); i++) {
            if (mMediaDataList.get(i).isVideo) {
                videoData = new ImageData();
                videoData.id = mMediaDataList.get(i)._id.intValue();
                videoData.path = mMediaDataList.get(i).contentPath;
                videoData.fileName = mMediaDataList.get(i).displayName;
                videoData.date = mMediaDataList.get(i).date;
                videoData.orientation = mMediaDataList.get(i).degrees + "";
                videoData.width = mMediaDataList.get(i).width;
                videoData.height = mMediaDataList.get(i).height;
                if (videoData != null) {
                    videoDatas.add(videoData);
                }
            }
        }
        return videoDatas;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected static class CodecInspectorTask {
        MediaExtractor mediaExtractor;
        //선택된 비디오 갯수 만큼 codec open 갯수
        final int decoderMaxCount;
        final int encoderMaxCount;
        final int selectVideoCount;
        final ArrayList<ImageData> videoList;
        boolean bResult = false;

        public CodecInspectorTask(ArrayList<ImageData> v) {
            videoList = v;
            selectVideoCount = videoList.size();
            decoderMaxCount = selectVideoCount;
            encoderMaxCount = selectVideoCount > 0 ? 1 : 0;
            L.i("선택된 비디오 갯수 :  " + selectVideoCount);
        }

        protected boolean onPostExecute(boolean result) {
            L.i("Result = " + result);
            return result;
        }

        protected boolean doWork() {
            boolean result = true;

            if (selectVideoCount == 0)
                return onPostExecute(result);

            ArrayList<MediaFormat> mediaFormats = new ArrayList<>();

            for (ImageData data : videoList) {
                L.i("data.path = " + data.path);
                mediaExtractor = new MediaExtractor();
                try {
                    mediaExtractor.setDataSource(data.path);
                    int trackSize = mediaExtractor.getTrackCount();
                    for (int i = 0; i != trackSize; ++i) {
                        try {
                            MediaFormat format = mediaExtractor.getTrackFormat(i);
                            String mimeType = format.getString(MediaFormat.KEY_MIME);
                            if (mimeType.contains("video")) {
                                mediaFormats.add(format);
                            }
                        } catch (IllegalArgumentException exception) {
                            continue;
                        }
                    }
                    mediaExtractor.release();
                    mediaExtractor = null;

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            switch (selectVideoCount) {
                case ONE_VIDEO_SCENE:
                    result = isEnableSelectedCodec(mediaFormats, false);
                    L.e("encoder check result = "+ result);
                    result = result & isEnableSelectedCodec(mediaFormats, true);
                    L.e("decoder check result = "+ result);
                    break;

                case TWO_VIDEO_SCENE:
                    result = isEnableSelectedCodec(null, false);
                    L.e("encoder check result = "+ result);
                    result = result & isEnableSelectedCodec(mediaFormats, true);
                    L.e("decoder check result = "+ result);
                    break;
            }
            return onPostExecute(result);
        }

        private boolean isEnableSelectedCodec(ArrayList <MediaFormat> mediaFormats, boolean isDecoder) {
            boolean result = true;
            int enableCodecCount;
            L.i("mediaFormats = " + mediaFormats);

            if (isDecoder) {
                MediaCodec[] decoders = new MediaCodec[decoderMaxCount];
                for (enableCodecCount = 0; enableCodecCount < decoderMaxCount; enableCodecCount++) {
                    try {
                        if(enableCodecCount % 2 == 0){
                            decoders[enableCodecCount] = MediaCodec.createDecoderByType(mediaFormats.get(0).getString(MediaFormat.KEY_MIME));
                            decoders[enableCodecCount].configure(mediaFormats.get(0), null, null, 0);
                        }else{
                            decoders[enableCodecCount] = MediaCodec.createDecoderByType(mediaFormats.get(1).getString(MediaFormat.KEY_MIME));
                            decoders[enableCodecCount].configure(mediaFormats.get(1), null, null, 0);
                        }

                    } catch (Exception ex) {
//						ex.printStackTrace();
                        L.i("Exception occurred while calling codec configure #" + enableCodecCount +"\n"+ex);
                        break;
                    }
                }

                if(mediaFormats.size() == 2){
                    if (enableCodecCount > 1) {
                        result = true;
                    } else {
                        result = false;
                    }
                }else{
                    if (enableCodecCount > 0) {
                        result = true;
                    } else {
                        result = false;
                    }
                }
                releaseCodec(decoders);
                L.i("mediaFormats = "+ mediaFormats +"\nEnable Decoder Count = " + enableCodecCount);

            } else {
                final String MIME_TYPE = "video/avc";
                final int SUPPORT_MAX_WIDTH = RULES.getClipVideoMaxWidth();
                final int SUPPORT_MAX_HEIGHT = RULES.getClipVideoMaxHeight();
                //무비다이어리 생성 최대 사이즈 기준으로 Encoder open check
                final int FRAME_RATE = 30;
                final int IFRAME_INTERVAL = 1;
                final int BIT_RATE ;

                if(SUPPORT_MAX_WIDTH * SUPPORT_MAX_HEIGHT > com.sugarmount.sugarcamera.story.PublicVariable.BASE_RECORDING_HD_RESOLUTION_WIDTH * com.sugarmount.sugarcamera.story.PublicVariable.BASE_RECORDING_HD_RESOLUTION_HEIGHT){
                    BIT_RATE = 4194304 * 4;
                    L.i("Try opening fhd encoder ..");
                }else{
                    BIT_RATE = 2097152 * 4;
                    L.i("Try opening hd encoder ..");
                }

                MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, SUPPORT_MAX_WIDTH, SUPPORT_MAX_HEIGHT);
                if(Build.VERSION.SDK_INT >=18)
                    format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface );
                else
                    format.setInteger(MediaFormat.KEY_COLOR_FORMAT, getSupportedColorFormat());

                format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
                format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
                format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
                L.i("format: " + format);

                MediaCodec[] encoders = new MediaCodec[encoderMaxCount];

                L.i("android.os.Build.VERSION.SDK_INT  = "+ Build.VERSION.SDK_INT );
                for (enableCodecCount = 0; enableCodecCount < encoderMaxCount; enableCodecCount++) {
                    try {
                        encoders[enableCodecCount] = MediaCodec.createEncoderByType(MIME_TYPE);
                        encoders[enableCodecCount].configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                        if(Build.VERSION.SDK_INT >=18)
                            encoders[enableCodecCount].createInputSurface();
                        encoders[enableCodecCount].start();
                    } catch (Exception ex) {
                        L.i("Exception occurred while calling createInputSurface #" + enableCodecCount +"\n"+ex);
                        break;
                    }

                }

                if (enableCodecCount > 0) {
                    result = true;
                } else {
                    result = false;
                }

                releaseCodec(encoders);
                L.i("MediaFormats = "+ mediaFormats +"\nEnable Encoder Count = " + enableCodecCount);
            }
            return result;
        }

        private int getSupportedColorFormat(){
            int selectedColorFormat = -1;
            String mMimeType = "video/avc";
            int currentColorFormatPriority = PreviewVideoDecoder.sPrioritizedColorFormats.length;

            for (int i = 0; i != MediaCodecList.getCodecCount(); ++i) {

                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                if (!codecInfo.isEncoder() || codecInfo.getName().contains("audio")) {
                    continue;
                }

                try {
                    for (String type : codecInfo.getSupportedTypes()) {
                        if (type.equalsIgnoreCase(mMimeType)) {
                            for (int supportedColorFormat : codecInfo.getCapabilitiesForType(mMimeType).colorFormats) {
                                for (int j = 0; j != currentColorFormatPriority; ++j) {
                                    if (supportedColorFormat == PreviewVideoDecoder.sPrioritizedColorFormats[j]) {
                                        selectedColorFormat = PreviewVideoDecoder.sPrioritizedColorFormats[j];
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
            L.i("selectedColorFormat = "+ selectedColorFormat);
            return selectedColorFormat;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        private void releaseCodec(MediaCodec[] codecs) {
            for (MediaCodec codec : codecs) {
                if (codec != null) {
                    L.i("codec.release()");
                    codec.release();
                    codec = null;
                }
            }
        }
    }

    public static String getPathFromUri(Uri uri){
        String[] proj = { MediaStore.Images.Media.DATA, MediaStore.Video.Media.DATA };
        Cursor cursor = mContext.getContentResolver().query(uri, proj, null, null, null );
        cursor.moveToNext();
        @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        cursor.close();
        return path;
    }

    protected static class LoadMediaDataThread extends Thread {
        private boolean isFinish = false;
        private ArrayList<ItemMediaData> mMediaDataList = new ArrayList<>();
        private ArrayList<ImageResData> mTmpDataList = new ArrayList<>();

        @SuppressLint("Recycle")
        @Override
        public void run() {

            mTmpDataList.forEach(it -> {
                ItemMediaData mediaData = new ItemMediaData();
                mediaData._id = it._id;
                mediaData.contentUri = it.contentUri;
                mediaData.contentPath = getPathFromUri(mediaData.contentUri);

                String itemPath = mediaData.contentPath.trim();
                int lastIndex = itemPath.lastIndexOf("/");

                if(lastIndex == INVALID_VALUE){
                    mediaData.folderName = itemPath;
                }else{
                    mediaData.folderName = itemPath.substring(0, lastIndex);
                }

                AssetFileDescriptor fileDescriptor = null;
                try {
                    fileDescriptor = mContext.getContentResolver().openAssetFileDescriptor(mediaData.contentUri , "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                assert fileDescriptor != null;
                mediaData.contentSize = fileDescriptor.getLength();
                mediaData.isVideo = false;

                if(mediaData.height <= 0 || mediaData.width <= 0){
                    BitmapFactory.Options options = BitmapUtils.getImageFileOption(mediaData.contentPath);
                    mediaData.width = options.outWidth;
                    mediaData.height = options.outHeight;
                }

                /**
                 * db를 통해서 얻은 값이 0일 경우 예외처리 :: retriever 를 통한 크기 가져오기
                 * 비디오 일 경우 추가
                 */
                if((mediaData.width <= 0 || mediaData.height <= 0) && mediaData.duration >= 0 && mediaData.isVideo){
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

                    try {
                        mediaMetadataRetriever.setDataSource(mediaData.contentPath);
                        mediaData.width = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                        mediaData.height = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                        mediaData.duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                    } catch (Exception e) {
                        mediaData.width = 0;
                        mediaData.height = 0;
                        mediaData.duration = 0;
                    }

                    try {
                        mediaMetadataRetriever.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                mediaData.degrees = 0;
                mediaData.date = 1111111;
                mediaData.invalid = false;
                mMediaDataList.add(mediaData);
            });

            isFinish = true;
        }
    }

}
