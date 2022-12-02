
package com.kiwiple.imageanalysis.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;

import com.kiwiple.imageanalysis.Constants;
import com.kiwiple.imageanalysis.Global;
import com.kiwiple.imageanalysis.analysis.operator.ColorOperator;
import com.kiwiple.imageanalysis.analysis.operator.FaceOperator;
import com.kiwiple.imageanalysis.analysis.operator.LocationOperator;
import com.kiwiple.imageanalysis.database.GalleryDBManager;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.utils.ContentResolverUtil;
import com.kiwiple.imageanalysis.utils.DateUtil;
import com.kiwiple.imageanalysis.utils.SmartLog;

/**
 * 갤러리의 이미지 분석을 위한 클래스 시간, 위치, 인물, 선명도, ColorSet등을 분석한다.
 */
public class ImageAnalysis {

    private static final String TAG = ImageAnalysis.class.getSimpleName();

    public static final String INTENT_ACTION_ANALYSIS = "IntentActionAnalysis";
    public static final String EXTRA_KEY_ANALYSIS_COUNT = "AnalysisCount";
    public static final String EXTRA_KEY_ANALYSIS_TOTAL_COUNT = "AnalysisTotalCount";

    private static final String SCREENSHOT_FOLDER = "Screenshots";

    private static ImageAnalysis mInstance;
    private ImageAnalysisCondition mImageAnalysisCondition;
    private Context mContext;

    // 앨범 리스트 조회 항목
    private static final String[] ALBUM_PROJECTION = {
            ImageColumns.BUCKET_ID, ImageColumns.BUCKET_DISPLAY_NAME
    };
    // 기본 갤러리 커서 항목
    private static final String[] IMAGE_PROJECTION = {
            BaseColumns._ID, MediaColumns.DATA, ImageColumns.DATE_TAKEN, ImageColumns.DATE_ADDED,
            ImageColumns.ORIENTATION, ImageColumns.LATITUDE, ImageColumns.LONGITUDE,
            ImageColumns.BUCKET_ID, ImageColumns.BUCKET_DISPLAY_NAME, MediaColumns.WIDTH,
            MediaColumns.HEIGHT, MediaColumns.MIME_TYPE, ImageColumns.TITLE, ImageColumns.SIZE
    };
    // 위치 관련 처리
    private LocationOperator mLocationOperator;
    // 인물 관련 처리
    private FaceOperator mFaceOperator;
    // 퀄리티, 선명도, 색상추출 관련 처리
//    private QualityOperator mQualityOperator;
    // 색상 관련 처리
    private ColorOperator mColorOperator;

    // DB 관련 처리
    private GalleryDBManager mGalleryDBManager;
    // Task
    private static ImageAnalysisAsyncTask mImageAnalysisTask;
    // 분석 시간 체크용도
    private long mTimeCheck = 0L;
    // 분석 취소 플래그
    private boolean mImageAnalysisCancel = false;
    private ImageAutoAnalysisListener mAutoAnalysisListener;

    /**
     * ImageAnalysisListener
     */
    public interface ImageAnalysisListener {
        /**
         * 각 이미지의 분석이 끝날때마다 호출됨.
         * 
         * @param finishCount 현재까지 분석이 완료된 이미지 갯수
         * @param totalCount 전체 이미지 갯수
         */
        public abstract void onImageAnalysisFinish(int finishCount, int totalCount);

        /**
         * 이미지 분석이 완전히 종료될때 호출된다.
         */
        public abstract void onImageAnalysisTotalFinish(boolean isSuccess);
    }

    public interface ImageAutoAnalysisListener {
        /**
         * 이미지 분석이 완전히 종료될때 호출된다.
         */
        public abstract void onImageAnalysisTotalFinish(boolean isSuccess);
    }

    /**
     * ImageAnalysis 싱글톤 생성자. <br>
     * 기본 조건으로 갤러리를 분석한다.
     * 
     * @param ctx Context
     * @param listener ImageAnalysisListener
     */
    public static ImageAnalysis getInstance(Context ctx) {
        if(mInstance == null) {
            mInstance = new ImageAnalysis(ctx);
        }
        return mInstance;
    }

    /**
     * ImageAnalysis 객체 해제
     */
    public void release() {
        mLocationOperator = null;
        mInstance = null;
    }

    private ImageAnalysis(Context ctx) {
        mContext = ctx;
        Global.getInstance();
        mLocationOperator = new LocationOperator(mContext);
        mFaceOperator = FaceOperator.getInstance(mContext);
        mGalleryDBManager = new GalleryDBManager(mContext);
//        mQualityOperator = new QualityOperator();
        mColorOperator = new ColorOperator();
    }

    /**
     * 갤러리 앨범의 이름 리스트를 반환한다.
     * 
     * @return ArrayList 앨범 이름 리스트
     */
    public static ArrayList<String> getAlbumNames(Context context) {
        ArrayList<String> albumNames = new ArrayList<String>();
        Cursor imageGroupCursor = null;
        try {
            imageGroupCursor = ContentResolverUtil.getImageCursor(context, ALBUM_PROJECTION,
                                                                  ImageColumns.BUCKET_ID
                                                                          + " IS NOT NULL",
                                                                  ImageColumns.BUCKET_ID,
                                                                  ImageColumns.BUCKET_DISPLAY_NAME
                                                                          + " ASC");
            // 사진이 있는 앨범 정보만 꺼낸다.
            if(imageGroupCursor != null && imageGroupCursor.getCount() > 0) {
                while(imageGroupCursor.moveToNext()) {
                    String albumName = imageGroupCursor.getString(imageGroupCursor.getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME));
                    albumNames.add(albumName);
                }
            }
        } finally {
            if(imageGroupCursor != null) {
                imageGroupCursor.close();
            }
        }
        return albumNames;
    }

    /**
     * 갤러리에 저장되어있는 총 이미지 갯수를 반환한다.
     * 
     * @param ctx Context
     * @return int 갤러리에 저장된 이미지의 총 갯수
     */
    public static int getTotalImageCount(Context ctx) {
        Cursor imageGroupCursor = null;
        try {
            imageGroupCursor = ContentResolverUtil.getImageCursor(ctx, IMAGE_PROJECTION, null,
                                                                  null, null);
            // 사진이 있는 앨범 정보만 꺼낸다.
            if(imageGroupCursor != null) {
                return imageGroupCursor.getCount();
            }
        } finally {
            if(imageGroupCursor != null) {
                imageGroupCursor.close();
            }
        }

        return 0;
    }

    /**
     * 갤러리 이미지 분석 시작. <br>
     * ImageAnalysisCondition의 조건들에 따라 갤러리를 분석한다. <br>
     * 
     * @param listener ImageAnalysisListener
     * @param imageAnalysisCondition 이미지 분석에 관련된 조건.
     */
    public void startAnalysisGallery(ImageAnalysisListener listener,
            ImageAnalysisCondition imageAnalysisCondition) {
        mImageAnalysisCondition = imageAnalysisCondition;

        if(mImageAnalysisTask == null) {
            // 갤러리와의 싱크를 먼저 맞춰야함. (불필요한 DB데이터를 지우는 것이 목적)
            checkDatabaseForImageData();
            mImageAnalysisTask = new ImageAnalysisAsyncTask(listener, mAutoAnalysisListener,
                                                            imageAnalysisCondition);
            // 자동 인물 인식의 경우
            if(imageAnalysisCondition.getIsAutoFaceRecognition()
                    && FaceOperator.IS_SUPPORT_SNAPDRAGON_FACE_PROCESSING) {
                // 기존 앨범을 리셋할지의 여부는 따로 설정값이 필요해보이기도 함..
                mFaceOperator.setIsAutoFaceRecognition(imageAnalysisCondition.getIsAutoFaceRecognition(),
                                                       imageAnalysisCondition.getProtagonistCount());
            }

            // 인물을 서칭할 경우 기존에 저장된 주인공들을 로드함
            if(imageAnalysisCondition.getAnalysisFaceCondition()
                    && FaceOperator.IS_SUPPORT_SNAPDRAGON_FACE_RECOGNITION) {
                mFaceOperator.loadFaceRecognitionAlbumGroup();
            }

            if(mImageAnalysisTask.getStatus() != AsyncTask.Status.RUNNING) {
                mImageAnalysisTask.execute(mContext);
            }
        } else {
            mImageAnalysisTask.setImageAnalysisListener(listener);
        }
    }

    private void checkDatabaseForImageData() {

        // 1.이미지 분석 db에서 전체 이미지 id리스트를 조회한다.
        ArrayList<Integer> imageDataIdArray = mGalleryDBManager.selectAllImageDatasId();
        if(imageDataIdArray == null || imageDataIdArray.isEmpty()) {
            return;
        }

        // 2.갤러리에 있는 전체 이미지의 id를 조회한다.
        ArrayList<Integer> galleryIdArray = new ArrayList<Integer>();
        Cursor imageCursor = ContentResolverUtil.getImageCursor(mContext, IMAGE_PROJECTION, null,
                                                                null, ImageColumns.DATE_TAKEN
                                                                        + " DESC");
        if(imageCursor != null && imageCursor.getCount() > 0) {
            while(imageCursor.moveToNext()) {
                int id = imageCursor.getInt(imageCursor.getColumnIndex(BaseColumns._ID));
                galleryIdArray.add(id);
            }
        }

        if(galleryIdArray.isEmpty()) {
            return;
        }

        // 3. 이미지 분석 db의 아이디가 실제 갤러리에 존재하는지 체크한다. 없다면 db에서 지워준다.
        for(int i = 0; i < imageDataIdArray.size(); i++) {
            // 각 이미지 데이터 id가 갤러리에 존재하는지 체크
            int imageDataId = imageDataIdArray.get(i);
            if(!galleryIdArray.contains(imageDataId)) {
                // 포함되어있지 않은 아이디 값이라면 DB에서 지워준다.
                mGalleryDBManager.deleteImageData(imageDataId);
            }
        }
    }

    /**
     * 갤러리 이미지 분석 종료. <br>
     */
    public void stopAnalysisGallery() {
        if(mImageAnalysisTask != null) {
            mImageAnalysisTask.cancel(false);
            mImageAnalysisTask = null;
        }

        mImageAnalysisCancel = true;
    }

    /**
     * 이미지가 분석 중인지에 대한 여부를 얻을 수 있다.
     */
    public boolean getIsOperatingAnalysis() {
        if(mImageAnalysisTask == null) {
            return false;
        }
        return mImageAnalysisTask.getStatus() == AsyncTask.Status.RUNNING;
        // return mIsAnalysisOperating;
    }

    /**
     * 위치 관련 처리 클래스를 리턴.
     * 
     * @return LocationOperator 분석에 사용된 위치 관련 처리 클래스
     */
    public LocationOperator getLocationOperator() {
        return mLocationOperator;
    }

    /**
     * 인물 관련 처리 클래스를 리턴.
     * 
     * @return FaceOperator 분석에 사용된 인물 관련 처리 클래스
     */
    public FaceOperator getFaceOperator() {
        return mFaceOperator;
    }

    /**
     * 갤러리 DB Manager를 리턴.
     * 
     * @return GalleryDBManager 분석에 사용된 DB Manager
     */
    public GalleryDBManager getGalleryDBManager() {
        return mGalleryDBManager;
    }

    /**
     * 이미지 데이터를 Hash 및 DB에 저장.
     * 
     * @param childItem 이미지 데이터
     */
    public void setCacheGalleryData(ImageData imageData) {
        // Save DB
        mGalleryDBManager.updateImageData(imageData);
        // Face Data 컬럼
        mGalleryDBManager.updateFaceAllDataItem(imageData);
    }

    public void setImageAnalysisListener(ImageAnalysisListener listener) {
        if(mImageAnalysisTask != null) {
            mImageAnalysisTask.setImageAnalysisListener(listener);
        }
    }

    public void setImageAutoAnalysisListener(ImageAutoAnalysisListener listener) {
        if(mImageAnalysisTask != null) {
            mImageAnalysisTask.setImageAutoAnalysisListener(listener);
        }

        mAutoAnalysisListener = listener;
    }

    /**
     * 갤러리 등으로 선택한 앨범에서 ImageData를 생성한다.
     * 
     * @param uri 선택한 앨범 이미지 uri
     * @return ImageData 선택한 이미지의 분석 데이터
     */
    public ImageData getImageData(Uri uri) {
        if(mImageAnalysisCondition == null) {
            mImageAnalysisCondition = new ImageAnalysisCondition(mContext);
            mImageAnalysisCondition.setAnalysisLocationCondition(true);
            mImageAnalysisCondition.setAnalysisFaceCondition(true);
            mImageAnalysisCondition.setIsAutoFaceRecognition(true, 15);
            mImageAnalysisCondition.setAnalysisQualityCondition(true);
        }
        Cursor imageCursor = ContentResolverUtil.getImageCursor(mContext, uri, IMAGE_PROJECTION,
                                                                null, null, null);
        String albumName = null;
        if(imageCursor != null && imageCursor.moveToFirst() && imageCursor.getCount() > 0) {
            albumName = imageCursor.getString(imageCursor.getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME));
        }
        return getImageData(imageCursor, albumName, false);
    }

    private class ImageAnalysisAsyncTask extends AsyncTask<Object, Integer, Void> {

        // 몇개까지 분석할지에 대한 이미지 갯수
        private int mPreviousAnalysisCount = 0;
        private ImageAnalysisListener mImageAnalysisListener;
        private ImageAutoAnalysisListener mImageAutoAnalysisListener;
        private ImageAnalysisCondition mImageAnalysisCondition;

        public ImageAnalysisAsyncTask(ImageAnalysisListener listener,
                ImageAutoAnalysisListener imageAutoAnalysisListener,
                ImageAnalysisCondition condition) {
            mImageAnalysisListener = listener;
            mImageAutoAnalysisListener = imageAutoAnalysisListener;
            mImageAnalysisCondition = condition;
        }

        public void setImageAnalysisListener(ImageAnalysisListener listener) {
            mImageAnalysisListener = listener;
        }

        public void setImageAutoAnalysisListener(ImageAutoAnalysisListener listener) {
            mImageAutoAnalysisListener = listener;
        }

        @Override
        protected Void doInBackground(Object... params) {

            if(!Constants.RELEASE_BUILD) {
                mTimeCheck = System.currentTimeMillis();
            }

            Context context = (Context)params[0];
            Cursor imageCursor = null;

            // 전체 이미지 중 몇개까지 분석되었는가 파악하기 위한 count
            int analysisCount = 0;
            // 현재 작업으로 분석된 이미지 갯수
            int realAnalysisCount = 0;
            // 분석할 갯수를 불러온다.
            mPreviousAnalysisCount = mImageAnalysisCondition.getAnalysisCount();

            // 검색 조건을 Query로 풀어야지..
            String whereString = mImageAnalysisCondition.getGenerateQueryFromCondition();
            imageCursor = ContentResolverUtil.getImageCursor(context, IMAGE_PROJECTION,
                                                             whereString, null,
                                                             ImageColumns.DATE_TAKEN + " DESC");
            if(imageCursor != null && imageCursor.moveToFirst() && imageCursor.getCount() > 0) {
                do {
                    // 취소하였다면 stopAnalysisGallery()를 호출하였다면 멈춤
                    if(mImageAnalysisCancel) {
                        mImageAnalysisCancel = false;
                        break;
                    }

                    // 분석 갯수가 0개라면 전체 분석을 수행한다.
                    if(mPreviousAnalysisCount < 1) {
                        mPreviousAnalysisCount = imageCursor.getCount();
                    }

                    // 분석 갯수가 지정된 갯수와 같다면
                    if(realAnalysisCount >= mPreviousAnalysisCount) {
                        // 종료한다.
                        if(mImageAnalysisListener != null) {
                            mImageAnalysisListener.onImageAnalysisTotalFinish(false);
                        }
                        if(mImageAutoAnalysisListener != null) {
                            mImageAutoAnalysisListener.onImageAnalysisTotalFinish(false);
                        }
                        break;
                    }

                    // fixes #10761 스크린샷 폴더의 컨텐츠들을 제외한다.
                    String path = imageCursor.getString(imageCursor.getColumnIndex(MediaColumns.DATA));
                    if(path == null || path.contains(SCREENSHOT_FOLDER)) {
                        continue;
                    }
                    
                    // 파일이 중간에 삭제되었을 경우 수정
                    File file = new File(path);
                    if(!file.exists()) {
                        analysisCount++;
                        publishProgress(analysisCount, imageCursor.getCount());
                        continue;
                    }
                    file = null;

                    // 분석 갯수만큼 아직 덜 분석되었다면 분석을 시작하자.
                    String albumName = imageCursor.getString(imageCursor.getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME));
                    ImageData imageData = getImageData(imageCursor, albumName, true);
                    setCacheGalleryData(imageData);
                    if(imageData.isAnalysisData) {
                        realAnalysisCount++;
                    }
                    analysisCount++;
                    publishProgress(analysisCount, imageCursor.getCount());
                } while(imageCursor.moveToNext());
            } else {
                if(mImageAnalysisListener != null) {
                    mImageAnalysisListener.onImageAnalysisTotalFinish(false);
                }
                if(mImageAutoAnalysisListener != null) {
                    mImageAutoAnalysisListener.onImageAnalysisTotalFinish(false);
                }
            }
            imageCursor.close();

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int count = values[0];
            int totalCount = values[1];
            SmartLog.e(TAG, "count:" + count + ",totalCount:" + totalCount);
            if(mImageAnalysisListener != null) {
                mImageAnalysisListener.onImageAnalysisFinish(count, totalCount);
            }

            Intent i = new Intent(INTENT_ACTION_ANALYSIS);
            i.putExtra(EXTRA_KEY_ANALYSIS_COUNT, count);
            i.putExtra(EXTRA_KEY_ANALYSIS_TOTAL_COUNT, totalCount);
            mContext.sendBroadcast(i);
        }

        @Override
        protected void onPostExecute(Void result) {
            // 다 사용하고 난 뒤에는 반환하자.
            FaceOperator.release();

            if(!Constants.RELEASE_BUILD) {
                mTimeCheck = System.currentTimeMillis() - mTimeCheck;
                SmartLog.e(TAG, "time : " + mTimeCheck + "m/s");
            }

            if(mImageAnalysisListener != null) {
                mImageAnalysisListener.onImageAnalysisTotalFinish(true);
            }
            if(mImageAutoAnalysisListener != null) {
                mImageAutoAnalysisListener.onImageAnalysisTotalFinish(true);
            }

            mImageAnalysisTask = null;
        }
    }

    private ImageData getImageData(Cursor imageSubCursor, String albumName, boolean isCacheLoaded) {

        // 기존에 저장된 데이터인지 실제 분석이 이루어졌는지에 대한 변수
        boolean isAnalysisImageData = false;

        int childId = imageSubCursor.getInt(imageSubCursor.getColumnIndex(BaseColumns._ID));
        ImageData itemObject = mGalleryDBManager.selectImageDataForImageId(childId);
        if(itemObject == null || !isCacheLoaded) {
            itemObject = new ImageData();
            isAnalysisImageData = true;
        }

        itemObject.albumName = albumName;
        itemObject.id = childId;
        itemObject.date = imageSubCursor.getLong(imageSubCursor.getColumnIndex(ImageColumns.DATE_TAKEN));
        itemObject.dateFormat = DateUtil.sdf.format(new Date(itemObject.date));
        itemObject.dateAdded = imageSubCursor.getLong(imageSubCursor.getColumnIndex(ImageColumns.DATE_ADDED)) * 1000;
        itemObject.fileName = imageSubCursor.getString(imageSubCursor.getColumnIndex(ImageColumns.TITLE));
        itemObject.fileSize = imageSubCursor.getInt(imageSubCursor.getColumnIndex(ImageColumns.SIZE));
        String orientation = imageSubCursor.getString(imageSubCursor.getColumnIndex(ImageColumns.ORIENTATION));
        itemObject.orientation = Global.isNullString(orientation) ? "0" : orientation;
        itemObject.path = imageSubCursor.getString(imageSubCursor.getColumnIndex(MediaColumns.DATA));
        SmartLog.e(TAG, "path : " + itemObject.path);
        itemObject.mimeType = imageSubCursor.getString(imageSubCursor.getColumnIndex(MediaColumns.MIME_TYPE));
        double latitude = imageSubCursor.getDouble(imageSubCursor.getColumnIndex(ImageColumns.LATITUDE));
        if(latitude == 0) {
            itemObject.latitude = null;
        } else {
            itemObject.latitude = latitude + "";
        }
        double longitude = imageSubCursor.getDouble(imageSubCursor.getColumnIndex(ImageColumns.LONGITUDE));
        if(longitude == 0) {
            itemObject.longitude = null;
        } else {
            itemObject.longitude = longitude + "";
        }

        itemObject.width = imageSubCursor.getInt(imageSubCursor.getColumnIndex(MediaColumns.WIDTH));
        itemObject.height = imageSubCursor.getInt(imageSubCursor.getColumnIndex(MediaColumns.HEIGHT));

        // 미디어 DB에서 가로 세로가 0일 수 있음....
        // 비트맵을 가상으로 디코딩하여 사이즈를 알아낸다.
        if(itemObject.width == 0 || itemObject.height == 0) {
            SmartLog.e(TAG, "image width or height == 0");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bm = BitmapFactory.decodeFile(itemObject.path, options);
            itemObject.width = options.outWidth;
            itemObject.height = options.outHeight;
            if(bm != null){
            	bm.recycle();
            	bm = null;
            }
            options = null;
        }

        // 위치 관련 처리
        // 2015.07.23 : 65버전에서 발열 및 배터리 문제가 발생하여 대책의 일환으로 위치 정보 수집을 중단
        mImageAnalysisCondition.setAnalysisLocationCondition(false);
        if(mImageAnalysisCondition.getAnalysisLocationCondition()
                && Global.isNullString(itemObject.addressFullName)
                && LocationOperator.isOnline(mContext)) {
            Address addressInfo = mLocationOperator.getAddress(itemObject.latitude,
                                                               itemObject.longitude);
            if(addressInfo != null) {
                itemObject.addressFullName = addressInfo.getAddressLine(0).toString();
                itemObject.addressCountry = addressInfo.getCountryName();
                itemObject.addressCity = addressInfo.getAdminArea();
                itemObject.addressDistrict = addressInfo.getLocality();
                itemObject.addressTown = addressInfo.getThoroughfare();
            }

            if(!Global.isNullString(itemObject.addressTown)) {
                itemObject.addressShortName = itemObject.addressTown;
            } else {
                if(!Global.isNullString(itemObject.addressDistrict)) {
                    itemObject.addressShortName = itemObject.addressDistrict;
                } else {
                    if(!Global.isNullString(itemObject.addressCity)) {
                        itemObject.addressShortName = itemObject.addressCity;
                    } else {
                        if(!Global.isNullString(itemObject.addressCountry)) {
                            itemObject.addressShortName = itemObject.addressCountry;
                        }
                    }
                }
            }
        }

        // 인물 관련 처리
        if(mImageAnalysisCondition.getAnalysisFaceCondition() && !itemObject.isFinishFaceDetecting) {
            if(mImageAnalysisCondition.getAnalysisCount() != 0) {
                // 분석 갯수가 정해져있다면 새벽시간대 선행 분석으로 판단.
                if(isAnalysisImageData) {
                    // 새벽 시간 선행 분석은 실질 분석하는 녀석만 분석하도록 한다.
                    mFaceOperator.operatingFaceProcessing(itemObject);
                }
            } else {
                // 분석 갯수가 0이면 전체 분석(인물 수동분석)이므로 수행토록 한다
                mFaceOperator.operatingFaceProcessing(itemObject);
            }
        }

        // 이미지 데이터 관련 처리
        if(mImageAnalysisCondition.getAnalysisQualityCondition()
                && !itemObject.isFinishColorsetAnalysis) {
//            mQualityOperator.setChildItemHighlightExData(itemObject);

            // 색상 처리
            if(!Global.isNullString(itemObject.colorSet)) {
                synchronized(mColorOperator) {
                    String representColorName = mColorOperator.getRepresentColorName(itemObject.colorSet);
                    itemObject.representColorName = representColorName;
                    // 색상 갯수가 몇개인지 처리
                    itemObject.numberOfRepresentColor = mColorOperator.getRepresentColorCount();
                    // Brightness 처리
                    itemObject.brightnessValue = mColorOperator.getAvgBrightnessValue();
                }
            }

            itemObject.isFinishColorsetAnalysis = true;
        }

        // 실제 분석이 이루어졌다면
        itemObject.isAnalysisData = isAnalysisImageData;

        return itemObject;
    }

    public IFrameData analyzeIFrame(Bitmap iFrame) {
        IFrameData iFrameData = new IFrameData();
//        mQualityOperator.setChildItemHighlightExData(iFrame, iFrameData);

        synchronized(mColorOperator) {
            mColorOperator.getRepresentColorName(iFrameData.colorSet);
            // Brightness 처리
            iFrameData.brightnessValue = mColorOperator.getAvgBrightnessValue();
        }

        FaceOperator.getInstance(mContext).operatingFaceProcessing(iFrame, iFrameData);
        return iFrameData;
    }

    /**
     * Credicts Library 값 구조체 클래스
     */
    public static class IFrameData {
        /**
         * 이미지 종합 점수
         */
        public int totalScore = 0;
        /**
         * 이미지 퀄리티 점수
         */
        public int qualityScore = 0;
        /**
         * 이미지 선명도 점수
         */
        public int sharpnessScore = 0;
        /**
         * colorSet의 4x4영역의 대표 색상값들의 평균 밝기 값.<br>
         * colorSet이 있을 경우(Jpeg 포멧)에만 값이 있다.
         */
        public int brightnessValue = 0;
        /**
         * 이미지 컬러셋 (이미지를 4x4로 분할하여 각 영역별 대표 색상 문자열)<br>
         * 각 영역의 구분은 " "(띄어쓰기)로 한다. <br>
         * Jpeg 형태의 이미지만이 가능하다. <br>
         * ex)
         * "6D482D 9DAF2D 20D5FF 24EE2A 2672EA 6D482D 9DAF2D 20D5FF 24EE2A 2672EA 6D482D 9DAF2D 20D5FF"
         */
        public String colorSet = "";
        /**
         * 이미지의 얼굴 갯수
         */
        public int numberOfFace = 0;
    }
}
