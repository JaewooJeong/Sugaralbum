
package com.kiwiple.scheduler.theme;

import java.io.File;

import android.content.Context;

import com.kiwiple.imageframework.network.util.Base64Coder;
import com.kiwiple.imageframework.util.FileUtils;

/**
 * 테마 유틸 객체. 
 *
 */
public class ThemeUtils {
    private static final String THEME_FOLDER_NAME = "Theme";

    /**
     * 테마별 디렉토리 경로를 반환. 
     * @param context Context
     * @return 디렉토리 경로.
     */
    public static String getThemeDirectoryPath(Context context) {
        String path = new StringBuilder().append(context.getFilesDir().toString())
                                         .append(File.separator).append(THEME_FOLDER_NAME)
                                         .toString();
        // 디렉토리가 없을 경우에 생성
        if(!FileUtils.isExist(path)) {
            new File(path).mkdirs();
        }
        return path;
    }

    /**
     * 테마별 파일의 전체 경로 반환. 
     * @param context Context
     * @param name 파일 이름.
     * @return 파일의 전체 경로. 
     */
    public static String getThemeDirectoryPathWithName(Context context, String name) {
        return getThemeDirectoryPath(context) + File.separator + Base64Coder.getMD5HashString(name);
    }
}
