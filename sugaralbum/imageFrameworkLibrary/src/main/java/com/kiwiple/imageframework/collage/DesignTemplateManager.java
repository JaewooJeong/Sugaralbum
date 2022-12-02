
package com.kiwiple.imageframework.collage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.kiwiple.imageframework.network.util.Base64Coder;

import android.content.Context;
import android.text.TextUtils;

/**
 * 템플릿 목록을 관리하는 class. <br>
 * {@link #getInstance} 함수로 instance를 생성하고 singleton으로 동작한다.
 * 
 * @version 2.0
 */
public class DesignTemplateManager {
    private static final String TEMPLATE_LIST = "TemplateList";

    private Context mContext;
    private static DesignTemplateManager sInstance;

    private ArrayList<DesignTemplate> mDesignTemplates = new ArrayList<DesignTemplate>();
    private boolean mIsAssetTemplateInitailzed = false;
    private ArrayList<DesignTemplate> mThemeDesignTemplates = new ArrayList<DesignTemplate>();

    private String mBasePath = "template";

    private DesignTemplateManager(Context applicationContext) {
        mContext = applicationContext;
    }

    /**
     * @param applicationContext
     * @return {@link #DesignTemplateManager}의 인스턴스 반환
     * @version 1.0
     */
    public static DesignTemplateManager getInstance(Context applicationContext) {
        if(sInstance == null) {
            sInstance = new DesignTemplateManager(applicationContext);
        }
        if(sInstance.mContext == null) {
            sInstance.mContext = applicationContext;
        }
        return sInstance;
    }

    /**
     * @param dataPath 템플릿 목록 파일 경로
     * @throws IOException 템플릿 목록 파일을 불러오지 못했을 경우
     * @version 1.0
     */
    public void setTemplateFile(String filePath, boolean isAsset) throws IOException {
        InputStream in = null;
        if(isAsset) {
            in = mContext.getResources().getAssets().open(filePath);
        } else {
            in = new FileInputStream(filePath);
        }
        setTemplate(in);
    }

    /**
     * 테마와 관련된 콜라주 json경로를 입력
     * 
     * @param filePath Json 경로
     * @throws IOException
     * @version 2.0
     */
    public boolean addThemeTemplate(String filePath) throws IOException {
        if(mThemeDesignTemplates == null) {
            mThemeDesignTemplates = new ArrayList<DesignTemplate>();
        }

        InputStream in = new FileInputStream(filePath);
        return addThemeTemplate(in);
    }

    public boolean isInitialized() {
        return mIsAssetTemplateInitailzed;
    }

    /**
     * @return 템플릿 전체 목록
     * @version 1.0
     */
    public ArrayList<TemplateInfo> getTemplateArray() {
        ArrayList<TemplateInfo> templateInfos = new ArrayList<TemplateInfo>();
        for(DesignTemplate template : mDesignTemplates) {
            templateInfos.add(new TemplateInfo(template));
        }
        for(DesignTemplate template : mThemeDesignTemplates) {
            templateInfos.add(new TemplateInfo(template));
        }
        return templateInfos;
    }

    /**
     * @return Asset에 셋팅된 템플릿 전체 목록
     */
    public ArrayList<TemplateInfo> getAssetTemplateArray() {
        ArrayList<TemplateInfo> templateInfos = new ArrayList<TemplateInfo>();
        for(DesignTemplate template : mDesignTemplates) {
            templateInfos.add(new TemplateInfo(template));
        }
        return templateInfos;
    }

    /**
     * @return 다운로드된 테마 템플릿 전체 목록
     */
    public ArrayList<TemplateInfo> getThemeTemplateArray() {
        ArrayList<TemplateInfo> templateInfos = new ArrayList<TemplateInfo>();
        for(DesignTemplate template : mThemeDesignTemplates) {
            templateInfos.add(new TemplateInfo(template));
        }
        return templateInfos;
    }

    /**
     * @param themeName 테마 이름
     * @return 특정 테마의 템플릿 목록
     */
    public ArrayList<TemplateInfo> getThemeTemplateArrayByName(String themeName) {
        ArrayList<TemplateInfo> templateInfos = new ArrayList<TemplateInfo>();
        if(!TextUtils.isEmpty(themeName)) {
            for(DesignTemplate template : mDesignTemplates) {
                if(themeName.equals(template.mTheme)) {
                    templateInfos.add(new TemplateInfo(template));
                }
            }

            for(DesignTemplate template : mThemeDesignTemplates) {
                if(themeName.equals(template.mTheme)) {
                    templateInfos.add(new TemplateInfo(template));
                }
            }
        }
        return templateInfos;
    }

    /**
     * @param numOfFrame 프레임 개수
     * @return 프레임 개수에 해당하는 템플릿 목록 반환
     * @version 1.0
     */
    public ArrayList<TemplateInfo> getTemplateArray(int numOfFrame, String themeName) {
        ArrayList<TemplateInfo> templateInfos = new ArrayList<TemplateInfo>();
        if(!TextUtils.isEmpty(themeName)) {
            for(DesignTemplate template : mDesignTemplates) {
                if(themeName.equals(template.mTheme) && template.mFrameInfos.size() == numOfFrame) {
                    templateInfos.add(new TemplateInfo(template));
                }
            }
            for(DesignTemplate template : mThemeDesignTemplates) {
                if(themeName.equals(template.mTheme) && template.mFrameInfos.size() == numOfFrame) {
                    templateInfos.add(new TemplateInfo(template));
                }
            }
        } else {
            for(DesignTemplate template : mDesignTemplates) {
                if(TextUtils.isEmpty(template.mTheme) && template.mFrameInfos.size() == numOfFrame) {
                    templateInfos.add(new TemplateInfo(template));
                }
            }
        }
        return templateInfos;
    }

    /**
     * @param id 템플릿 고유 식별자
     * @return 템플릿 반환
     * @version 1.0
     */
    public TemplateInfo getTemplateInfo(int id) {
        for(DesignTemplate template : mDesignTemplates) {
            if(template.mId == id) {
                return new TemplateInfo(template);
            }
        }
        for(DesignTemplate template : mThemeDesignTemplates) {
            if(template.mId == id) {
                return new TemplateInfo(template);
            }
        }
        return null;
    }

    /**
     * Template 관련 파일(SVG, Thumbnail image)들이 저장된 path를 설정한다.<br>
     * File System에 저장되어 있으면 absolute path, Assets이면 relative path를 설정한다.
     * 
     * @param path Template 파일이 저자된 directory path.
     * @param isAssets File System에 저장되어 있으면 false, Assets이면 true.
     * @version 1.0
     */
    public void setAssetBasePath(String path) {
        if(!TextUtils.isEmpty(path)) {
            mBasePath = path;
        }
    }

    /**
     * Template 관련 파일(SVG, Thumbnail image)들이 저장된 path를 반환한다.<br>
     * File일 경우 absolute path, Assets 일 경우 relative path가 반환된다.
     * 
     * @return directory path
     * @version 1.0
     */
    public String getAssetBasePath() {
        if(mBasePath.charAt(mBasePath.length() - 1) != File.separatorChar) {
            mBasePath += File.separatorChar;
        }
        return mBasePath;
    }

    /**
     * U+ 무비 다이어리 테마 전용 경로
     * 
     * @param template 템플릿 정보
     * @return
     */
    public String getThemeBasePath(String themeName) {
        StringBuilder pathBuilder = new StringBuilder().append(mContext.getFilesDir().toString())
                                                       .append(File.separator)
                                                       .append("Theme")
                                                       .append(File.separator)
                                                       .append(Base64Coder.getMD5HashString(themeName));
        return pathBuilder.toString();
    }

    /**
     * {@link DesignTemplateManager} 리소스 반환
     * 
     * @version 1.0
     */
    public void release() {
        mDesignTemplates.clear();
        mDesignTemplates = null;
        mThemeDesignTemplates.clear();
        mThemeDesignTemplates = null;
        mIsAssetTemplateInitailzed = false;
        mContext = null;
        if(sInstance != null) {
            sInstance = null;
        }
    }

    /**
     * 파라메타로 받은 아이디에 해당하는 DesignTemplate 을 반환한다.
     * 
     * @param id 콜라주 템플릿의 고유 번호
     * @return {@link DesignTemplate}
     * @version 2.0
     */
    public DesignTemplate getDesignTemplate(int id) {

        for(DesignTemplate template : mDesignTemplates) {
            if(template.mId == id) {
                return template;
            }
        }

        for(DesignTemplate template : mThemeDesignTemplates) {
            if(template.mId == id) {
                return template;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void setTemplate(InputStream in) throws IOException {
        try {
            mDesignTemplates = new ArrayList<DesignTemplate>();
            HashMap<String, Object> mData = new ObjectMapper().readValue(in, HashMap.class);
            for(HashMap<String, Object> templateInfo : (ArrayList<HashMap<String, Object>>)mData.get(TEMPLATE_LIST)) {
                DesignTemplate data = new DesignTemplate();
                data.parse(templateInfo);
                if(data.isValid()) {
                    mDesignTemplates.add(data);
                }
            }
            mIsAssetTemplateInitailzed = true;
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException e) {
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean addThemeTemplate(InputStream in) throws IOException {
        boolean result = false;
        try {
            HashMap<String, Object> mData = new ObjectMapper().readValue(in, HashMap.class);
            
            ArrayList <Integer> designTemplateIdList = new ArrayList<Integer>(); 
            for(DesignTemplate designtemplate : mThemeDesignTemplates){
            	designTemplateIdList.add(designtemplate.mId); 
            }
            
            for(HashMap<String, Object> templateInfo : (ArrayList<HashMap<String, Object>>)mData.get(TEMPLATE_LIST)) {
                DesignTemplate data = new DesignTemplate();
                data.parse(templateInfo);
                data.mIsThemeTemplate = true;
                if(data.isValid()) {
                	if(!designTemplateIdList.contains(data.mId)){
                		mThemeDesignTemplates.add(data);
                	}
                }
            }
            result = true;
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException e) {
                }
            }
        }

        return result;
    }
}
