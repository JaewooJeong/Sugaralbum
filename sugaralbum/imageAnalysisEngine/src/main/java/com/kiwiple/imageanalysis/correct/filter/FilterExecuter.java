
package com.kiwiple.imageanalysis.correct.filter;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.RemoteException;

import com.kiwiple.imageanalysis.Global;
import com.kiwiple.imageframework.filter.FilterData;
import com.kiwiple.imageframework.filter.FilterManager.FilterProcessListener;
import com.kiwiple.imageframework.filter.FilterManagerWrapper;
import com.kiwiple.imageframework.util.FileUtils;

/**
 * 필터를 적용하기 위한 클래스.<br>
 * 받은 보정데이터 (ImageCorrectData)를 통해 해당 이미지에 필터를 적용하여 반환토록한다.
 */
public class FilterExecuter {

    // 필터 전체 목록 파일 이름
    private static final String ASSET_DEFAULT_FILTER = "defaultFilter.json";

    private Context mApplicationContext;

    private ArrayList<FilterData> mFilterList = null;
    private FilterExecuteListener mFilterExecuteListener;

    /**
     * 필터링 결과를 callBack
     */
    public interface FilterExecuteListener {
        /**
         * 이미지에 필터를 적용한 Bitmap을 반환
         * 
         * @param filteredImage 필터가 적용된 이미지 비트맵 (null일 경우 실패)
         * @param filterId 적용된 필터 고유번호.
         * @param userInfo {@link FilterExecuter#applyFilterFromImageCorrectData} 호출 시 전달한 사용자 정의
         *            class
         */
        public void onCompleteFilteredImage(Bitmap filteredImage, int filterId, Object userInfo);

        /**
         * 이미지에 필터 적용이 실패했을 경우
         * 
         * @param filterId 적용하려던 필터 고유번호.
         * @param userInfo {@link FilterExecuter#applyFilterFromImageCorrectData} 호출 시 전달한 사용자 정의
         *            class
         */
        public void onFailFilteredImage(int filterId, Object userInfo);
    }

    /**
     * 생성자
     * 
     * @param applicationContext ApplicationContext
     */
    public FilterExecuter(Context applicationContext) {

        mApplicationContext = applicationContext;

        // 필터 목록이 초기화 되지 안은 경우 초기화
        if(!FilterManagerWrapper.getInstance(applicationContext).isInitialized()) {
            try {
                FilterManagerWrapper.getInstance(applicationContext)
                                    .setFilterAsset(ASSET_DEFAULT_FILTER);

            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        // 해당 필터를 구조체로 변경해둔다
        mFilterList = FilterManagerWrapper.getInstance(applicationContext).getFilterData();
    }

    /**
     * 전체 필터 리스트를 반환한다.
     * 
     * @return ArrayList 등록된 필터 리스트
     */
    public ArrayList<FilterData> getDefaultFilterList() {
        return mFilterList;
    }

    /**
     * 이미지 보정 데이터를 가지고 필터를 적용한 이미지를 반환
     * 
     * @param filePath 파일 경로
     * @param filterId 고유 필터 번호
     * @param targetSize 필터를 적용한 이미지의 사이즈 (원본이 이보다 작다면 원본 사이즈로 반환됨)
     * @param filterExecuteListener 필터 적용 리스너
     * @param userInfo 유저 정보 (리스너의 userInfo를 통해 반환된다.)
     */
    public void applyFilterFromImageCorrectData(String filePath, int filterId, int targetSize,
            FilterExecuteListener filterExecuteListener, Object userInfo) {
        mFilterExecuteListener = filterExecuteListener;
        // 이미지 데이터가 없는 경우엔 실패 처리를 하도록 한다.
        if(Global.isNullString(filePath)) {
            failApplyFilter(filterExecuteListener);
            return;
        }

        // 필터 아이디가 있는지 체크 해야함
        if(filterId > 0) {
            Bitmap imageBitmap = null;
            try {
                imageBitmap = FileUtils.decodingImage(filePath, targetSize, Bitmap.Config.ARGB_8888);
            } catch(IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            applyFilterFromImageCorrectData(imageBitmap, filterId, filterExecuteListener, userInfo);
        }
    }

    /**
     * 이미지 보정 데이터를 가지고 필터를 적용한 이미지를 반환
     * 
     * @param bitmap 필터를 적용할 이미지
     * @param filterId 고유 필터 번호
     * @param filterExecuteListener 필터 적용 리스너
     * @param userInfo 유저 정보 (리스너의 userInfo를 통해 반환된다.)
     */
    public void applyFilterFromImageCorrectData(Bitmap bitmap, int filterId,
            FilterExecuteListener filterExecuteListener, Object userInfo) {
        mFilterExecuteListener = filterExecuteListener;
        // 이미지 데이터가 없는 경우엔 실패 처리를 하도록 한다.
        if(bitmap == null) {
            failApplyFilter(filterExecuteListener);
            return;
        }

        // 필터 아이디가 있는지 체크 해야함
        if(filterId > 0) {
            FilterManagerWrapper.getInstance(mApplicationContext)
                                .applyFilterImage(bitmap, filterId, mFilterProcessListener,
                                                  userInfo);
        }
    }

    /**
     * 이미지 보정 데이터를 가지고 필터를 적용한 이미지를 반환
     * 
     * @param imageBitmap 필터를 적용할 이미지 비트맵
     * @param filterId 고유 필터 번호
     * @return 필터가 적용된 비트맵
     */
    public Bitmap applyFilterFromImageCorrectData(Bitmap imageBitmap, int filterId) {
        // 이미지 데이터가 없는 경우엔 실패 처리를 하도록 한다.
        if(imageBitmap == null) {
            return null;
        }

        // 필터 아이디가 있는지 체크 해야함
        if(filterId > 0) {
            Bitmap bitmap = null;
            try {
                bitmap = FilterManagerWrapper.getInstance(mApplicationContext)
                                             .applyFilterImage(imageBitmap, filterId);
            } catch(RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return bitmap;
        }

        return null;
    }

    /**
     * 이미지 보정 데이터를 가지고 필터를 적용한 이미지를 반환
     * 
     * @param filePath 파일 경로
     * @param filterId 고유 필터 번호
     * @param targetSize 필터를 적용한 이미지의 사이즈 (원본이 이보다 작다면 원본 사이즈로 반환됨)
     * @param filterExecuteListener 필터 적용 리스너
     * @param userInfo 유저 정보 (리스너의 userInfo를 통해 반환된다.)
     */
    public Bitmap applyFilterFromImageCorrectData(String filePath, int filterId, int targetSize) {
        // 이미지 데이터가 없는 경우엔 실패 처리를 하도록 한다.
        if(Global.isNullString(filePath)) {
            return null;
        }

        // 필터 아이디가 있는지 체크 해야함
        if(filterId > 0) {
            Bitmap imageBitmap = null;
            try {
                imageBitmap = FileUtils.decodingImage(filePath, targetSize, Bitmap.Config.ARGB_8888);
            } catch(IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Bitmap filteredBitmap = null;
            if(imageBitmap == null) {
                return null;
            }

            filteredBitmap = applyFilterFromImageCorrectData(imageBitmap, filterId);
            return filteredBitmap;
        }

        return null;
    }

    private void failApplyFilter(FilterExecuteListener filterExecuteListener) {
        if(filterExecuteListener != null) {
            filterExecuteListener.onFailFilteredImage(-1, null);
        }
    }

    private FilterProcessListener mFilterProcessListener = new FilterProcessListener() {

        @Override
        public void onFailureFilterProcess(int filterId, Object userInfo) {
            failApplyFilter(mFilterExecuteListener);
        }

        @Override
        public void onCompleteFilterProcess(Bitmap image, String filePath, int filterId,
                Object userInfo) {
            // 현재는 그대로 반환해준다.
            // 사용자에게 반환하기 전에 사전 처리가 필요하다면 이곳에서 처리한다.
            if(mFilterExecuteListener != null) {
                mFilterExecuteListener.onCompleteFilteredImage(image, filterId, userInfo);
            }
        }
    };
}
