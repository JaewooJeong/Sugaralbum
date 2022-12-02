
package com.kiwiple.imageanalysis.correct.sticker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;

/**
 * 스티커를 관리하는 매니저 클래스
 */
public class StickerManager {

    private static final String STICKER_LIST = "stickers";

    private Context mContext;
    private static StickerManager sInstance;

    private StickerManager(Context applicationContext) {
        mContext = applicationContext;
    }

    /**
     * 싱글톤 생성자
     * 
     * @param applicationContext Context
     * @return StickerManager
     */
    public static StickerManager getInstance(Context applicationContext) {
        if(sInstance == null) {
            sInstance = new StickerManager(applicationContext);
        }

        if(sInstance.mContext == null) {
            sInstance.mContext = applicationContext;
        }
        return sInstance;
    }

    /**
     * asset에 있는 Sticker 관련 Json 파일을 배열로 파싱하여 반환
     * 
     * @param assetPath Sticker 관련 Json
     * @return StickerInfo의 배열
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<StickerInfo> getStickerListAsset(String assetPath) throws IOException {
        InputStream in = mContext.getResources().getAssets().open(assetPath);
        ArrayList<StickerInfo> stickerInfoArr = new ArrayList<StickerInfo>();
        try {
            HashMap<String, Object> mData = new ObjectMapper().readValue(in, HashMap.class);
            for(HashMap<String, Object> stickerInfo : (ArrayList<HashMap<String, Object>>)mData.get(STICKER_LIST)) {
                StickerInfo data = new StickerInfo();
                data.parse(stickerInfo);
                if(data.isValid()) {
                    stickerInfoArr.add(data);
                }
            }
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException e) {
                }
            }
        }
        return stickerInfoArr;
    }
}
