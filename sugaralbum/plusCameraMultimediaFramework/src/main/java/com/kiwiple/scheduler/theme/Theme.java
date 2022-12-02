package com.kiwiple.scheduler.theme;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.scheduler.util.ImmutablePoint;

/**
 * 무비 다이어리의 테마 객체
 */
public class Theme implements Serializable {
	private static final long serialVersionUID = -6757740682317372587L;
	private static final String TAG = Theme.class.getSimpleName();
	public static final String OUTRO_ASSET_FILE_NAME = "outro1_2_modi.jpg";
	public static final String DOWN_THEME_ASSET_MAIN_JSON = "theme/down_theme";

	public static final String THEME_NAME_DAILY = "Daily";
	public static final String THEME_NAME_TRAVEL = "Travel";
	public static final String THEME_NAME_BABY = "Baby";
	public static final String THEME_NAME_BIRTHDAY = "Birthday";
	public static final String THEME_NAME_LOVE = "Love";
	public static final String THEME_NAME_CHRISTMAS = "Christmas";
	public static final String THEME_NAME_CLEAN = "Clean";
	public static final String THEME_NAME_OLDMOVIE = "Old Movie";
	public static final String THEME_NAME_SUNNY = "Sunny";

	public static enum ThemeType {
		FRAME, FILTER, MULTI;
	}

	public static enum ResourceType {
		ASSET, DOWNLOAD;
	}

	public static enum MotionType {
		NOTHING, SCALE, ROTATE, REPLACE;
	}

	public static enum FrameType {
		INTRO, CONTENT, OUTRO
	}

	public static enum FontAlign {
		LEFT, CENTER, RIGHT
	}

	public String name = null;
	public ThemeType themeType;
	public ResourceType resourceType;
	public int mainColor = -1;
	public String buttonImageName = null;
	public int filterId = -1;
	public int frameMotionCount = 0;
	public String audioFileName = null;
	public String endingLogo = null;
	public int fileSize = -1;
	public List<Frame> frameData;
	public int fileCount = -1;
	public String collageJsonName = null;
	public String filterJsonName = null;
	public String coverTransitionName = null;
	public String coverTransitionMaskName = null;
	public String verticalImageName = null;

	private boolean mValidate = false;

	public int contentDuration = -1;
	public int frontBackDuration = -1;

	public String effectJsonName = null;
	public String transitionJsonName = null;

	public boolean isUseAutoMovieDiary = false;
	public boolean isEnterEffect = false;
	public boolean isUseColloectionScene = false;
	public boolean isUseStepAppearEffect = false;
	public List<ArrayList<String>> coverTransitionResList;
	public String dynamicIntroString = null;
	public String dynamicOutroString = null;

	public ThemeVersion version = null;
	
	public String analyticsThemeCategory = null; 
	public String analyticsThemeAction = null;
	
	public String musicTitle = null; 
	public String analyticsMusicCategory = null; 
	public String analyticsMusicAction = null;

	/**
	 * theme json data를 파싱하는 함수.
	 * 
	 * @param data
	 *            테마 json data.
	 */
	@SuppressWarnings("unchecked")
	public void parse(HashMap<String, Object> data) {
		Object value = data.get("Name");
		if (value instanceof String) {
			name = (String) value;
		}

		value = data.get("Type");
		if (value instanceof String) {
			String type = (String) value;
			if ("Frame".equalsIgnoreCase(type)) {
				themeType = ThemeType.FRAME;
			} else if ("Multi".equalsIgnoreCase(type)) {
				themeType = ThemeType.MULTI;
			} else {
				themeType = ThemeType.FILTER;
				mValidate = true;
			}
		}

		value = data.get("MainColor");
		if (value instanceof String) {
			String color = (String) value;
			if (color != null) {
				color = "#" + color;
				mainColor = Color.parseColor(color);
			}
		}

		value = data.get("ResourceType");
		if (value instanceof String) {
			String resource = (String) value;
			if ("Asset".equalsIgnoreCase(resource)) {
				resourceType = ResourceType.ASSET;
				mValidate = true;
			} else if ("Download".equalsIgnoreCase(resource)) {
				resourceType = ResourceType.DOWNLOAD;
			}
		}

		value = data.get("BtnImageName");
		if (value instanceof String) {
			buttonImageName = (String) value;
		}

		value = data.get("AudioFileName");
		if (value instanceof String) {
			audioFileName = (String) value;
		}

		value = data.get("EndingLogo");
		if (value instanceof String) {
			endingLogo = (String) value;
		}

		value = data.get("MotionCount");
		if (value instanceof Integer) {
			frameMotionCount = (Integer) value;
		}

		value = data.get("IsUseCollectionScene");
		if (value instanceof Boolean) {
			isUseColloectionScene = (Boolean) value;
		}
		if (data.containsKey("DownloadCoverResourceList")) {
			L.i("has download cover resource : " + data.containsKey("DownloadCoverResourceList"));
			coverTransitionResList = new ArrayList<ArrayList<String>>();
			for (HashMap<String, Object> coverInfo : ((ArrayList<HashMap<String, Object>>) data.get("DownloadCoverResourceList"))) {
				if (coverInfo != null && coverInfo.size() > 0) {
					ArrayList<String> coverTransition = new ArrayList<String>();
					coverTransition.add((String) coverInfo.get("CoverResource"));
					coverTransition.add((String) coverInfo.get("MaskResource"));
					coverTransitionResList.add(coverTransition);
				}
			}
		}
		
		if(data.containsKey("GoogleThemeAnalytics")){
			
			HashMap<String, Object> analytics = (HashMap<String, Object>) data.get("GoogleThemeAnalytics");

			value = analytics.get("Category");
			if (value instanceof String) {
				analyticsThemeCategory = (String) analytics.get("Category");
			}
			
			value = analytics.get("Action");
			if (value instanceof String) {
				analyticsThemeAction = (String) analytics.get("Action");
			}
		}
				
		if(data.containsKey("GoogleMusicAnalytics")){
			
			HashMap<String, Object> musicAnalytics = (HashMap<String, Object>) data.get("GoogleMusicAnalytics");

			value = musicAnalytics.get("title");
			if (value instanceof String) {
				musicTitle = (String) musicAnalytics.get("title");
			}
			
			value = musicAnalytics.get("Category");
			if (value instanceof String) {
				analyticsMusicCategory = (String) musicAnalytics.get("Category");
			}
			
			value = musicAnalytics.get("Action");
			if (value instanceof String) {
				analyticsMusicAction = (String) musicAnalytics.get("Action");
			}			
		}

		value = data.get("IsUseStepAppearEffect");
		if (value instanceof Boolean) {
			isUseStepAppearEffect = (Boolean) value;
		}

		value = data.get("FilterId");
		if (value instanceof Integer) {
			filterId = (Integer) value;
		}

		frameData = new ArrayList<Frame>();
		for (HashMap<String, Object> frameInfo : (ArrayList<HashMap<String, Object>>) data.get("FrameList")) {
			frameData.add(new Frame(frameInfo));
		}

		value = data.get("FileSize");
		if (value instanceof Integer) {
			fileSize = (Integer) value;
		}

		value = data.get("FileCount");
		if (value instanceof Integer) {
			fileCount = (Integer) value;
		}

		value = data.get("CollageJsonName");
		if (value instanceof String) {
			collageJsonName = (String) value;
		}

		value = data.get("FilterJsonName");
		if (value instanceof String) {
			filterJsonName = (String) value;
		}

		value = data.get("CoverTransitionName");
		if (value instanceof String) {
			coverTransitionName = (String) value;
		}

		value = data.get("CoverTransitionMaskName");
		if (value instanceof String) {
			coverTransitionMaskName = (String) value;
		}

		value = data.get("VerticalImageName");
		if (value instanceof String) {
			verticalImageName = (String) value;
		}

		value = data.get("IsUseAutoMovieDiary");
		if (value instanceof Boolean) {
			isUseAutoMovieDiary = (Boolean) value;
		}

		value = data.get("IsUseCollectionScene");
		if (value instanceof Boolean) {
			isUseColloectionScene = (Boolean) value;
		}

		value = data.get("IsEnterEffect");
		if (value instanceof Boolean) {
			isEnterEffect = (Boolean) value;
		}

		value = data.get("ContentDuration");
		if (value instanceof Integer) {
			contentDuration = (Integer) value;
		}

		value = data.get("FrontBackDuration");
		if (value instanceof Integer) {
			frontBackDuration = (Integer) value;
		}
		value = data.get("effectJsonName");
		if (value instanceof String) {
			effectJsonName = (String) value;
		}

		value = data.get("transitionJsonName");
		if (value instanceof String) {
			transitionJsonName = (String) value;
		}
		value = data.get("dynamic_intro_object");
		if (value != null) {
			dynamicIntroString = value.toString();
		}

		value = data.get("dynamic_outro_object");
		if (value != null) {
			dynamicOutroString = value.toString();
		}

		int major = 0;
		int minor = 0;
		int patch = 0;

		value = data.get("version_major");
		if (value instanceof Integer) {
			major = (Integer) value;
		}
		value = data.get("version_minor");
		if (value instanceof Integer) {
			minor = (Integer) value;
		}
		value = data.get("version_patch");
		if (value instanceof Integer) {
			patch = (Integer) value;
		}

		version = new ThemeVersion(major, minor, patch);
	}

	public JSONObject getDynamicIntroJson() {
		if (dynamicIntroString != null) {
			try {
				JSONObject introJsonObject = new JSONObject(dynamicIntroString);
				return introJsonObject; 
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public JSONObject getDynamicOutroJson() {
		if (dynamicOutroString != null) {
			try {
				JSONObject introJsonObject = new JSONObject(dynamicOutroString);
				return introJsonObject; 
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 테마별 파일 전체 경로 설정.
	 * 
	 * @param context
	 *            Context
	 * @param fileName
	 *            파일 이름.
	 * @param extension
	 *            파일 확장자.
	 * @return 파일 전체 경로.
	 */
	public String combineDowloadImageFilePath(Context context, String fileName, String extension) {
		if (fileName == null || fileName.isEmpty()) {
			return null;
		}
		return ThemeUtils.getThemeDirectoryPathWithName(context, name) + File.separator + fileName + "." + extension;
	}

	/**
	 * 테마별 파일 전체 경로 설정.
	 * 
	 * @param context
	 *            Context
	 * @param fileName
	 *            확장자를 포함한 파일 이름.
	 * @return 파일 전체 경로.
	 */
	public String combineDowloadImageFilePath(Context context, String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return null;
		}
		return ThemeUtils.getThemeDirectoryPathWithName(context, name) + File.separator + fileName;
	}

	/**
	 * 다운 받은 테마 파일 유효성 체크.
	 * 
	 * @param context
	 *            Context
	 * @return 유효성.
	 */
	public boolean isDownloadThemeFileValidate(Context context) {

		if (mValidate) {
			return mValidate;
		}

		File directory = new File(ThemeUtils.getThemeDirectoryPathWithName(context, name));
		if (directory.exists() && directory.isDirectory()) {
			long totalSize = 0;
			long totalCount = 0;
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().contains(".png") || files[i].getName().contains(".svg") || files[i].getName().contains(".json") || files[i].getName().contains(".mp3") || files[i].getName().contains(".jpg") || files[i].getName().contains(".JPG")) {
					totalCount++;
					totalSize += files[i].length();
				}
			}

			if (totalCount == fileCount && totalSize == fileSize) {
				mValidate = true;
			} else {
				if (totalCount != fileCount) {
					L.d("Theme count invalid total count : " + totalCount + ", fileCount : " + fileCount);
				}
				if (totalSize != fileSize) {
					L.d("Theme size invalid, name :" + name + ", totalSize : " + totalSize + ", fileSize : " + fileSize);
				}
			}
		}
		return mValidate;
	}

	/**
	 * 각 테마의 Frame(디자인) 객체
	 * 
	 */
	public static class Frame implements Serializable {
		private static final long serialVersionUID = -3910113538684418625L;

		public int id;
		public String frameImageName = null;
		public String frameThumbImageName = null;
		public String frameThumbFocImageName = null;
		public int frameCount = 0;
		public FrameType frameType = FrameType.CONTENT;
		public String fontName = null;
		public int fontSize = 0;
		public int fontColor = -1;
		public FontAlign fontAlign = FontAlign.LEFT;
		public int fontCoordinateLeft = 0;
		public int fontCoordinateTop = 0;
		public int fontCoordinateRight = 0;
		public int fontCoordinateBottom = 0;
		public List<FrameObject> objects;
		public boolean useUserImageBackground = false;

		@SuppressWarnings("unchecked")
		public Frame(HashMap<String, Object> frameInfo) {
			Object value = frameInfo.get("Id");
			if (value instanceof Integer) {
				id = (Integer) value;
			}

			value = frameInfo.get("FrameImageName");
			if (value instanceof String) {
				frameImageName = (String) value;
			}

			value = frameInfo.get("FrameThumbImageName");
			if (value instanceof String) {
				frameThumbImageName = (String) value;
			}

			value = frameInfo.get("FrameThumbFocImageName");
			if (value instanceof String) {
				frameThumbFocImageName = (String) value;
			}

			value = frameInfo.get("FrameCount");
			if (value instanceof Integer) {
				frameCount = (Integer) value;
			}

			value = frameInfo.get("FrameType");
			if (value instanceof String) {
				String frameTypeString = (String) value;
				if ("Intro".equalsIgnoreCase(frameTypeString)) {
					frameType = FrameType.INTRO;
				} else if ("Content".equalsIgnoreCase(frameTypeString)) {
					frameType = FrameType.CONTENT;
				} else if ("Outro".equalsIgnoreCase(frameTypeString)) {
					frameType = FrameType.OUTRO;
				}
			}

			value = frameInfo.get("FontName");
			if (value instanceof String) {
				fontName = (String) value;
			}

			value = frameInfo.get("FontSize");
			if (value instanceof Integer) {
				fontSize = (Integer) value;
			}

			value = frameInfo.get("FontColor");
			if (value instanceof String) {
				String color = (String) value;
				if (color != null) {
					color = "#" + color;
					fontColor = Color.parseColor(color);
				}
			}

			value = frameInfo.get("FontAlign");
			if (value instanceof String) {
				String fontAlignString = (String) value;
				if ("Left".equalsIgnoreCase(fontAlignString)) {
					fontAlign = FontAlign.LEFT;
				} else if ("Center".equalsIgnoreCase(fontAlignString)) {
					fontAlign = FontAlign.CENTER;
				} else if ("Right".equalsIgnoreCase(fontAlignString)) {
					fontAlign = FontAlign.RIGHT;
				}
			}

			value = frameInfo.get("FontCoordinateLeft");
			if (value instanceof Integer) {
				fontCoordinateLeft = (Integer) value;
			}

			value = frameInfo.get("FontCoordinateTop");
			if (value instanceof Integer) {
				fontCoordinateTop = (Integer) value;
			}

			value = frameInfo.get("FontCoordinateRight");
			if (value instanceof Integer) {
				fontCoordinateRight = (Integer) value;
			}

			value = frameInfo.get("FontCoordinateBottom");
			if (value instanceof Integer) {
				fontCoordinateBottom = (Integer) value;
			}

			objects = new ArrayList<FrameObject>();
			for (HashMap<String, Object> objectInfo : (ArrayList<HashMap<String, Object>>) frameInfo.get("ObjectInfo")) {
				objects.add(new FrameObject(objectInfo));
			}

			value = frameInfo.get("useUserImageBackground");
			if (value instanceof Boolean) {
				useUserImageBackground = (Boolean) value;
			}
		}
	}

	/**
	 * 각 테마의 Frame Object 객체
	 * 
	 */
	public static class FrameObject implements Serializable {
		private static final long serialVersionUID = 9094788747562026128L;

		public String imageName;
		public ImmutablePoint coordinate;
		public Motion motion = new Motion();

		public FrameObject(HashMap<String, Object> objectInfo) {
			Object value = objectInfo.get("ImageName");
			if (value instanceof String) {
				imageName = (String) value;
			}

			int coordinateLeft = 0;
			value = objectInfo.get("CoordinateLeft");
			if (value instanceof Integer) {
				coordinateLeft = (Integer) value;
			}
			int coordinateTop = 0;
			value = objectInfo.get("CoordinateTop");
			if (value instanceof Integer) {
				coordinateTop = (Integer) value;
			}
			coordinate = new ImmutablePoint(coordinateLeft, coordinateTop);

			String motionType = null;
			double motionFloatValue = 0;
			String motionStringValue = null;
			value = objectInfo.get("MotionType");
			if (value instanceof String) {
				motionType = (String) value;
			}

			value = objectInfo.get("MotionFloatValue");
			if (value instanceof Double) {
				motionFloatValue = (Double) value;
			} else if (value instanceof Integer) {
				motionFloatValue = Double.valueOf(String.valueOf(value));
			}

			value = objectInfo.get("MotionStringValue");
			if (value instanceof String) {
				motionStringValue = (String) value;
			}
			if ("Rotate".equalsIgnoreCase(motionType)) {
				motion.type = MotionType.ROTATE;
				motion.value = motionFloatValue;
			} else if ("Scale".equalsIgnoreCase(motionType)) {
				motion.type = MotionType.SCALE;
				motion.value = motionFloatValue;
			} else if ("Replace".equalsIgnoreCase(motionType)) {
				motion.type = MotionType.REPLACE;
				motion.value = motionStringValue;
			} else {
				motion.type = MotionType.NOTHING;
			}
		}
	}

	/**
	 * 각 테마의 motion 객체.
	 * 
	 */
	public static class Motion implements Serializable {
		private static final long serialVersionUID = -4421987543520951563L;

		public MotionType type;
		public Object value;
	}

	/**
	 * 디자인 테마 여부를 반환.
	 * 
	 * @return 디자인 테마 여부.
	 */
	public boolean isDesignTheme() {
		return themeType == ThemeType.FRAME;
	}

	/**
	 * 테마별 인트로 씬 여부를 반환.
	 * 
	 * @return 인트로 씬 여부.
	 */
	public boolean hasIntro() {
		if (frameData != null) {
			for (Frame frame : frameData) {
				if (FrameType.INTRO.equals(frame.frameType)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 테마별 아웃트로 씬 여부를 반환.
	 * 
	 * @return 아웃트로 씬 여부.
	 */
	public boolean hasOutro() {
		if (frameData != null) {
			for (Frame frame : frameData) {
				if (FrameType.OUTRO.equals(frame.frameType)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isIntroSceneWithUserImage() {
		if (frameData != null) {
			for (Frame frame : frameData) {
				if (FrameType.INTRO.equals(frame.frameType)) {
					if (frame.useUserImageBackground) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean isDynamicOutroDefaultImage(){
		if (dynamicOutroString != null) {
			try {
				JSONObject introJsonObject = new JSONObject(dynamicOutroString);
				return introJsonObject.getBoolean("use_default_image");  
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
}
