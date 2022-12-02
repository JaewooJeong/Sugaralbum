
package com.sugarmount.sugarcamera.story.theme;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.kiwiple.debug.L;
import com.kiwiple.imageframework.collage.DesignTemplateManager;
import com.kiwiple.scheduler.SchedulerEnvironment;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.ThemeType;
import com.kiwiple.scheduler.theme.ThemeUtils;
import com.kiwiple.scheduler.theme.ThemeVersion;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 무비 다이어리에 쓰이는 테마를 관리하는 클래스<br>
 * Embedded Theme & Download Theme를 관리한다.
 */
public class ThemeManager {
    public static final String DEFAULT_THEME_NAME = "Clean";
    private static final String THEME_LIST = "ThemeList";
    private static final String ASSET_THEME_FILE_NAME = "theme/theme.json";
    private static final String ASSET_THEME_SUPPORT_FILE = "theme_support";

    public static final String ASSET_COLLAGE_FILE_NAME = "template/defaultTemplates.json";

    private static ThemeManager sInstance;

    private Context mContext;
    private boolean mInitialized = false;

    // 임베디드 테마 목록
    private ArrayList<Theme> mAssetThemeList = new ArrayList<Theme>();
    // 다운로드 테마 목록
    private ArrayList<Theme> mDownloadThemeList = new ArrayList<Theme>();

    /*
    private static final String[] mTotalthemeNameList = new String[] {"Mix up", "Travel2", "Dynamic","Clean","Love", "Sunny", "Rose beds", "Baby", "Travel", "Lite blue", 
			"Being Alone", "Old Movie","Joyful day", "Week Lomo", "Daily", "Vivid toon", "Birthday", "Little baby", 
			"Snap Shot SX-70","Emo","Cozy room","Slide film","Vintage", "Old book", "T-Max Film", "Faded Memory",
			"Under the Cafe", "Mint sugar", "cherry blossom", "Cute", "Documentary", "Pet", "Christmas"};
    */
    //private static final String[] mTotalthemeNameList = new String[] {"Morning Kiss", "Gloomy", "Hide and Seek", "We Wish You a Merry Christmas", "Chuncheon and Autumn"};
    private static final String[] mTotalthemeNameList = new String[] {"Morning Kiss"};

    public static ThemeManager getInstance(Context applicationContext) {
        if(sInstance == null) {
            sInstance = new ThemeManager(applicationContext);
        }
        if(sInstance != null && sInstance.mContext == null) {
            sInstance.mContext = applicationContext;
        }
        return sInstance;
    }

    private ThemeManager(Context applicationContext) {
        mContext = applicationContext;

        // setAssetTheme
        if(!mInitialized) {
            try {
                setThemeAsset(ASSET_THEME_FILE_NAME);
//                setThemeDownload();
//                setCollage();
                mInitialized = true;
                L.d("initialized value : " + mInitialized); 
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        Log.e("themeManager", "" + getAvailableList());
    }

    public static void release() {
        sInstance = null;
    }

    @SuppressWarnings("unchecked")
    private void setThemeAsset(String assetPath) throws IOException {
        InputStream in = mContext.getResources().getAssets().open(assetPath);
        try {
            mAssetThemeList = new ArrayList<Theme>();
            HashMap<String, Object> mData = new ObjectMapper().readValue(in, HashMap.class);
            for(HashMap<String, Object> themeInfo : (ArrayList<HashMap<String, Object>>)mData.get(THEME_LIST)) {
                Theme theme = new Theme();
                theme.parse(themeInfo);
                mAssetThemeList.add(theme);
            }
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException e) {
                }
            }
        }
    }

    private void setThemeDownload() throws IOException {
        String[] downloadThemeNameList = new String[] {
                "Baby", "Love", "Birthday", "Travel", "Travel2", "Daily" , "Christmas", "Cute", "Documentary", "Pet"
        };

        mDownloadThemeList = new ArrayList<Theme>();
        AssetManager assetManager = mContext.getAssets();

        for(int i = 0; i < downloadThemeNameList.length; i++) {
            String name = downloadThemeNameList[i];
            File directory = new File(getThemeDirectoryPathWithName(name));
            if(directory.exists() && directory.isDirectory()) {
            	InputStream in = assetManager.open(Theme.DOWN_THEME_ASSET_MAIN_JSON + File.separator + name +".json");
            	checkThemeVersion(directory, name);
            	
	            @SuppressWarnings("unchecked")
            	 HashMap<String, Object> mData = new ObjectMapper().readValue(in, HashMap.class);
            	 Theme theme = new Theme();
            	 theme.parse(mData);
            	 if(new File(directory.getPath() + File.separator + name + ".json").exists()){
            		 L.d(name + " json downloaded");
     	             theme.version = new ThemeVersion(1,0,0); 
     	             theme.fileSize = -1; 
     	             theme.resourceType = Theme.ResourceType.DOWNLOAD; 
     	             mDownloadThemeList.add(theme);
	 	         }else{
	 	        	 mDownloadThemeList.add(theme);
	 	         }
            } else {
                Theme theme = new Theme();
                theme.name = name;
                theme.resourceType = Theme.ResourceType.DOWNLOAD; 
                theme.themeType = ThemeType.FRAME;
                mDownloadThemeList.add(theme);
            }
        }
    }

    private void checkThemeVersion(File directory, String themeName) {
    	Theme theme = checkCurrentTheme(directory, themeName); 
    	if(theme.version == null || theme.version.isBelow(SchedulerEnvironment.THEME_VERSION_1_0_0)){
    		L.d("theme version : " + theme.version.toString());
    		upgradeTo_1_0_0(themeName, directory); 
    	}
	}

	private void upgradeTo_1_0_0(String themeName, File directory) {
		
		InputStream mainInput = null; 
		InputStream effectInput = null;
		InputStream transitionInput = null;
		
		OutputStream mainOutput = null; 
		OutputStream effectOutput = null;
		OutputStream transitionOutput = null;
		
		L.d("upgrade to version 1_0_0"); 
		
		String assetPath = ASSET_THEME_SUPPORT_FILE +File.separator + SchedulerEnvironment.THEME_VERSION_1_0_0.toString()+ File.separator + themeName; 
		L.d("asset path : " + assetPath); 
		
		try {			
			mainOutput = new FileOutputStream(directory + File.separator + themeName +".json"); 
			effectOutput = new FileOutputStream(directory+File.separator + "effect" +".json");
			transitionOutput = new FileOutputStream(directory+File.separator + "transition" +".json");
			
			mainInput = mContext.getResources().getAssets().open(assetPath+File.separator+themeName+".json");
			effectInput = mContext.getResources().getAssets().open(assetPath+File.separator+"effect"+".json");
			transitionInput = mContext.getResources().getAssets().open(assetPath+ File.separator+"transition"+".json");
			
			copyFile(mainOutput, mainInput); 
			copyFile(effectOutput, effectInput); 
			copyFile(transitionOutput, transitionInput); 
			
			mainInput.close(); 
			mainOutput.close();
			effectInput.close(); 
			effectOutput.close(); 
			transitionInput.close(); 
			transitionOutput.close();
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private Theme checkCurrentTheme(File directory, String themeName){
		AssetManager assetManager = mContext.getAssets();
        InputStream in;
        Theme theme = null; 
		try {
			in = assetManager.open(Theme.DOWN_THEME_ASSET_MAIN_JSON + File.separator + themeName +".json");
			@SuppressWarnings("unchecked")
			HashMap<String, Object> mData = new ObjectMapper().readValue(in, HashMap.class);
			theme = new Theme();
	        theme.parse(mData);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
        return theme; 
	}

	private OutputStream copyFile(OutputStream output, InputStream input) {
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize]; 
		int len = 0; 
		try {
			while((len = input.read(buffer)) != -1){
				output.write(buffer, 0, len); 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output; 
	}

	private void setCollage() {
        if(!DesignTemplateManager.getInstance(mContext.getApplicationContext()).isInitialized()) {
            try {
                DesignTemplateManager.getInstance(mContext.getApplicationContext())
                                     .setTemplateFile(ASSET_COLLAGE_FILE_NAME, true);
            } catch(IOException e) {
                // asset file error
            }
        }
        // Asset의 SVG 파일 및 템플릿 Thumbnail 이미지가 저장된 경로 설정
        DesignTemplateManager.getInstance(mContext.getApplicationContext())
                             .setAssetBasePath("template");

        // 다운로드 된 테마 셋팅.
        for(Theme theme : mDownloadThemeList) {
            if(!TextUtils.isEmpty(theme.collageJsonName)
                    || !"null".equalsIgnoreCase(theme.collageJsonName)) {
                String collageJsonPath = getThemeDirectoryPathWithName(theme.name) + File.separator
                        + theme.collageJsonName + ".json";
                if(new File(collageJsonPath).exists()) {
                    try {
                        DesignTemplateManager.getInstance(mContext)
                                             .addThemeTemplate(collageJsonPath);
                    } catch(IOException e) {
                        Log.e("ThemeManger", "Error Add DownloadTemplate : " + theme.name
                                + "," + collageJsonPath);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 테마 리스트를 반환한다.
     * 
     * @return 테마 리스트
     */
    public ArrayList<Theme> getThemeList() {
    	return getSpecificThemeList(mTotalthemeNameList); 
    }
    
    private ArrayList<Theme> getSpecificThemeList(String[] themeNameListOrder){
    	
		HashMap<String, Theme> themeMap = new HashMap<String, Theme>();
		
		for(Theme theme : mAssetThemeList){
			themeMap.put(theme.name, theme); 
		}
		
		for(Theme theme : mDownloadThemeList){
			themeMap.put(theme.name, theme);
		}
		
		ArrayList<Theme> themeList = new ArrayList<Theme>();
		for(int i = 0; i<themeNameListOrder.length; i++){
			Theme theme = themeMap.get(themeNameListOrder[i]); 
			if(theme.resourceType.equals(Theme.ResourceType.DOWNLOAD)){
				if(theme.fileSize >= 0){
					themeList.add(theme); 
				}
			}else{
				themeList.add(theme);
			}
		}   
		return themeList;
		    	
    }

    
    /**
     * 현재 사용할 수 있는 테마 리스트를 반환한다.
     * 
     * @return 사용 가능한 테마 리스트
     */
    public ArrayList<Theme> getAvailableList() {
        ArrayList<Theme> themeList = getThemeList();
        ArrayList<Theme> availableThemeList = new ArrayList<Theme>();
        if (themeList != null) {
            for(int i = 0; i < themeList.size(); i++) {
                Theme theme = themeList.get(i);
                if (theme.themeType == ThemeType.FILTER) {
                    availableThemeList.add(theme);
                } else if (theme.themeType == ThemeType.FRAME && theme.fileSize >= 0) {
                    availableThemeList.add(theme);
                } else if (theme.themeType == ThemeType.MULTI && theme.fileSize >= 0) {
                    availableThemeList.add(theme);
                }
            }    
        }
        return availableThemeList;
    }
    
    /**
     * movie diary 생성시, 사용할 수 있는 테마 리스트를 반환한다.
     * 
     * @return movie diary 생성시, 사용 가능한 테마 리스트
     */
    public ArrayList<Theme> getAvailableThemeListForMaking() {
        ArrayList<Theme> themeList = getThemeList();
        ArrayList<Theme> availableThemeList = new ArrayList<Theme>();
        if (themeList != null) {
            for(int i = 0; i < themeList.size(); i++) {
                Theme theme = themeList.get(i);
                if(theme.isUseAutoMovieDiary){
	                if (theme.themeType == ThemeType.FILTER) {
	                    availableThemeList.add(theme);
	                } else if (theme.themeType == ThemeType.FRAME && theme.fileSize >= 0) {
	                    availableThemeList.add(theme);
	                } else if (theme.themeType == ThemeType.MULTI && theme.fileSize >= 0) {
	                    availableThemeList.add(theme);
	                }
                }
            }    
        }
        return availableThemeList;
    }
    
    
    // 테마를 셋팅
    public void setTheme(Theme theme) {
        if (theme != null) {
            for(int i = 0; i < mAssetThemeList.size(); i++) {
                Theme targetTheme = mAssetThemeList.get(i);
                if (targetTheme.name.equals(theme.name)) {
                    mAssetThemeList.remove(i);
                    mAssetThemeList.add(i, theme);
                    break;
                }
            }
            
            for(int i = 0; i < mDownloadThemeList.size(); i++) {
                Theme targetTheme = mDownloadThemeList.get(i);
                if (targetTheme.name.equals(theme.name)) {
                    mDownloadThemeList.remove(i);
                    mDownloadThemeList.add(i, theme);
                    break;
                }
            }
        }
    }

    public String getThemeDirectoryPathWithName(String name) {
        return ThemeUtils.getThemeDirectoryPathWithName(mContext, name);
    }

    /**
     * 테마 이름으로 테마 객체를 반환.
     * 
     * @param name 테마 이름
     * @return 테마 이름에 맞는 객체
     */
    public Theme getThemeByName(String name) {
        for(Theme theme : mAssetThemeList) {
            if(theme.name.equalsIgnoreCase(name)) {
                return theme;
            }
        }

        for(Theme theme : mDownloadThemeList) {
            if(theme.name.equalsIgnoreCase(name)) {
                return theme;
            }
        }
        return null;
    }

}
