package com.sugarmount.sugarcamera.story.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.canvas.AssetImageResource;
import com.kiwiple.multimedia.canvas.BurstShotScene;
import com.kiwiple.multimedia.canvas.CollageScene;
import com.kiwiple.multimedia.canvas.DrawableImageResource;
import com.kiwiple.multimedia.canvas.FileImageResource;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageResource;
import com.kiwiple.multimedia.canvas.LayerScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.VideoFileImageResource;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.data.CollageElement;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.kiwiple.scheduler.R;
import com.sugarmount.sugarcamera.story.views.StoryPreviewLayout;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	private static final String DEFAULT_CACHE_DIRECTORY = "/pluscamera/.cache";
	private static final String CACHE_EXTENSION = ".cache";

	@Deprecated
	public static String getExternalStorageDirectory() {
		if (isExternalStorageMounted()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		return null;
	}
	
	/**
	 * Get app-specific external storage directory for Android 15 compatibility
	 * @param context Application context
	 * @return App-specific external directory path
	 */
	public static String getAppSpecificDirectory(Context context) {
		File externalDir = context.getExternalFilesDir(null);
		if (externalDir != null) {
			return externalDir.getAbsolutePath();
		}
		return context.getFilesDir().getAbsolutePath();
	}

	public static String getExternalStoragePublicDirectoryDCIM() {
		if (isExternalStorageMounted()) {
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
		}
		return null;
	}

	@Deprecated
	public static boolean isExternalStorageMounted() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	/**
	 * Check if app-specific external storage is available
	 * @param context Application context
	 * @return true if available
	 */
	public static boolean isAppStorageAvailable(Context context) {
		File externalDir = context.getExternalFilesDir(null);
		return externalDir != null && externalDir.canWrite();
	}

	@Deprecated
	public static long getAvailableSDcardSize() {
		if (isExternalStorageMounted()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		}
		return 0;
	}
	
	/**
	 * Get available storage size in app-specific directory
	 * @param context Application context
	 * @return Available bytes
	 */
	public static long getAvailableAppStorageSize(Context context) {
		File externalDir = context.getExternalFilesDir(null);
		if (externalDir == null) {
			externalDir = context.getFilesDir();
		}
		
		try {
			StatFs stat = new StatFs(externalDir.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} catch (Exception e) {
			return 0;
		}
	}

	public static String getCacheDirectory() {
		return getExternalStorageDirectory(DEFAULT_CACHE_DIRECTORY);
	}

	@Deprecated
	public static String getExternalStorageDirectory(String subDirectory) {
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + subDirectory + "/";
		try {
			return getDirectory(dir);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Get app-specific directory with subdirectory for Android 15 compatibility
	 * @param context Application context
	 * @param subDirectory Subdirectory name
	 * @return App-specific directory path
	 */
	public static String getAppSpecificDirectory(Context context, String subDirectory) {
		File externalDir = context.getExternalFilesDir(null);
		if (externalDir == null) {
			externalDir = context.getFilesDir();
		}
		String dir = externalDir.getAbsolutePath() + subDirectory + "/";
		try {
			return getDirectory(dir);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getCacheFileNameFromURL(String url) {
		return getFileNameFromURL(url) + CACHE_EXTENSION;
	}

	public static String getFileNameFromURL(String url) {
		String fileName = url.substring(url.substring(0, url.lastIndexOf('/')).lastIndexOf('/') + 1, url.length());
		return fileName.replace('/', '_');
	}

	public static String getDirectory(String path) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
				if (!file.exists()) {
					Logger.v("Utils", "NOT CREATE DIRECTORY");
				}
			}
			return path;
		} catch (Exception e) {
			return null;
		}
	}

	public static int valueOfInt(String str, int defalutValue) {
		if (str != null && str.length() > 0) {
			try {
				return Integer.valueOf(str);
			} catch (NumberFormatException e) {
				// TODO: handle exception
			}
		}
		return defalutValue;
	}

	public static String getReadableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	public static long getVideoDuration(String path) {
		if (TextUtils.isEmpty(path)) {
			return 0;
		}
		try {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(path);
			String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			return Long.parseLong(duration);
		} catch (RuntimeException e) {
			// TODO: handle exception
		}
		return 0;
	}

	public static boolean isFlagContain(int sourceFlag, int compareFlag) {
		return (sourceFlag & compareFlag) == compareFlag;
	}

	public static boolean isNull(Object o) {
		return o == null ? true : false;
	}

	public static boolean isNull(List<?> list) {
		return list == null || list.size() == 0 ? true : false;
	}

	public static boolean isNull(String str) {
		return TextUtils.isEmpty(str) ? true : false;
	}

	// ///////////////////////////////////////////
	// for gallery2
	// ///////////////////////////////////////////
	public static int getDisplayWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.x;
	}

	public static int getDisplayHeight(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.y;
	}

	public static int dp(Context context, float dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	// >>>>>>>>>>>>>
	// 자사/ 타사 단말 구분 그런데 자사일때 sim 카드 없어도 Uplus 마켓 진입 되나??
	// 진입 된다면, 다른 property로 체크
	// >>>>>>>>>>>>>
	private final static String CMD_FINGER_PRINT = "getprop ro.build.fingerprint";
	private final static String CMD_SIM_OPERATOR = "getprop gsm.sim.operator.alpha";

	static boolean result = false;
	public static boolean isAvailableUplusMarket(Context context) {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String command = CMD_FINGER_PRINT;
				long time = System.currentTimeMillis();
				boolean isRun = true;
				// TODO Auto-generated method stub
				while (isRun) {
					
					StringBuilder outputLog = new StringBuilder();
					String line = null;
					try {
						Process process = Runtime.getRuntime().exec(command); // 통신사
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
						
						while ((line = bufferedReader.readLine()) != null) {
							outputLog.append(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					L.e("outputLog.toLowerCase().contains(lgu) = " + outputLog.toString().toLowerCase().contains("lgu"));
					
					if (outputLog != null && outputLog.toString().toLowerCase().contains("lgu")) {
						isRun = false;
						result = true;
					} else {
						if(command.equalsIgnoreCase(CMD_SIM_OPERATOR))
							isRun = false;
						command = CMD_SIM_OPERATOR;
						continue;
					}
				}
				L.e("searching time...... "+(System.currentTimeMillis() - time)+"ms");
			}
		}).start();

		return result;
	}
	
	public static boolean isFileExist(String path){
		File file = new File(path); 
		if(file.exists() == true){
			return true; 
		}else{
			return false; 
		}
	}
	
	public static final String JSON_NAME_FILE_PATH = "file_path";
	
	public static String changeMusicPath(String jsonScript, String path, ResourceType resourceType) throws JSONException{
		if(jsonScript == null || jsonScript.isEmpty()){
			return null; 
		}

		JsonObject jsonObject = new JsonObject(jsonScript);
		
		if(!jsonObject.isNull(PreviewManager.JSON_NAME_AUDIO)){
			JsonObject jsonAudioObject = jsonObject.getJSONObject(PreviewManager.JSON_NAME_AUDIO);
			jsonAudioObject.put(PreviewManager.JSON_NAME_AUDIO_FILE_PATH, path); 
			jsonAudioObject.put(ResourceType.DEFAULT_JSON_NAME, resourceType);
		}
		return jsonObject.toString(); 
		
	}
	
	public static boolean isAssetMusic(String jsonScript) throws JSONException{
		if(jsonScript == null || jsonScript.isEmpty()){
			return false; 
		}

		JsonObject jsonObject = new JsonObject(jsonScript);
		
		if(!jsonObject.isNull(PreviewManager.JSON_NAME_AUDIO)){
			JsonObject jsonAudioObject = jsonObject.getJSONObject(PreviewManager.JSON_NAME_AUDIO);
			
			if(!jsonAudioObject.isNull("is_asset")){
				boolean isAsset = jsonAudioObject.getBoolean("is_asset"); 
				L.d("audio is asset  : " + isAsset);
				return isAsset; 
			}else{
				if(!jsonAudioObject.isNull(ResourceType.DEFAULT_JSON_NAME)){
					String audioType = jsonAudioObject.getString(ResourceType.DEFAULT_JSON_NAME);
					if("file".equals(audioType)){
						L.d("audio is not asset ");
						return false; 
					}else{
						L.d("audio is asset ");
						return true; 
					}
				}else{
					L.d("audio can not find audio info");
					return true; 
				}
			}
		}else{
			return false; 
		}
	}
	
	public static String getAssetDefaultMusicPath(Context context){
		AssetManager assetManager = context.getAssets();
		String path = null; 
		try {
			path = assetManager.list("audio")[0]; 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return "audio/" + path; 
	}

	public static boolean isAssetDefaultMusic(String title, Context context){
		AssetManager assetManager = context.getAssets();
		boolean isInternalAudio = false;
		try {
			String [] assetAudioArray = assetManager.list("audio");
			for(String fileName : assetAudioArray){
	    		if(fileName.contains(title)){
	    			isInternalAudio = true;
	    			break;
	    		}else{
	    			isInternalAudio = false;
	    		}
	    	}
			return isInternalAudio;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return isInternalAudio;
	}
	
	public static boolean isMusicFileValidate(Context context, String jsonScript) throws JSONException{
		if(jsonScript == null || jsonScript.isEmpty()){
			return false; 
		}

		boolean returnValue = true; 
		JsonObject jsonObject = new JsonObject(jsonScript);
		AssetManager assetManager = context.getAssets();
	
		if(!jsonObject.isNull(PreviewManager.JSON_NAME_AUDIO)){
			JsonObject jsonAudioObject = jsonObject.getJSONObject(PreviewManager.JSON_NAME_AUDIO);
			
			ResourceType audioType = jsonAudioObject.optEnum(ResourceType.DEFAULT_JSON_NAME, ResourceType.class);
			String path = jsonAudioObject.getString(PreviewManager.JSON_NAME_AUDIO_FILE_PATH);
			if(ResourceType.ANDROID_ASSET.equals(audioType)){
				try {
					assetManager.open(path);
					returnValue = true; 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					L.i("BG audio is not available.....[deleted or moved]");
					returnValue = false;  
				} 
			}else{
				File file = new File(path);
				if(file == null || !file.exists()){
					L.i("BG audio is not available.....[deleted or moved]");
					returnValue =  false;
				}else{
					returnValue = true; 
				}
			}
		}
			
		return returnValue; 
	}

	public static boolean isDataValidate(String jsonScript, Context context, StoryPreviewLayout storyPreviewLayout){
		if (jsonScript == null || jsonScript.isEmpty()) {
			return false;
		}

		if(PreviewManager.getInstance(context).getVisualizer().isEmpty()){
			try {
				storyPreviewLayout.setPreviewScriptWithoutUpdateView(jsonScript);
			} catch (JSONException e) {
				L.e("Error : " +e.getMessage());
			} catch (NullPointerException e){
				L.e("Error : " +e.getMessage());
			} catch (IllegalStateException stateE){
				L.e("Error : " +stateE.getMessage());
			}  catch(RuntimeException RunE){
				L.e("Error : " +RunE.getMessage());
			}
		}

		ArrayList<String> filePathList = new ArrayList<>();
		String DataPath = null;

		PreviewManager previewManager = PreviewManager.getInstance(context);
		String audioFilePath = previewManager.getAudioFilePath();
		L.e("aronia audio file path : " + audioFilePath);
		if(!previewManager.isAssetAudioFile() && (audioFilePath != null)){
			File file = new File(audioFilePath);
			if (file == null || !file.exists()) {
				L.i("BG audio is not available.....[deleted or moved]");
				return false;
			}
		}

		try {

			List<Scene> scenes = previewManager.getVisualizer().getRegion().getScenes();
			if(scenes == null || scenes.isEmpty()){
				return false;
			}

			for (Scene scene : scenes) {

				if (scene instanceof ImageFileScene) {
					ImageResource resource = ((ImageFileScene) scene).getImageResource();
					if (resource instanceof FileImageResource) {
						DataPath = ((FileImageResource) resource).getFilePath();
						filePathList.add(DataPath);
					} else if(resource instanceof VideoFileImageResource){
						DataPath = ((VideoFileImageResource) resource).getFilePath();
						filePathList.add(DataPath);
					} else if(resource instanceof AssetImageResource){
					} else if(resource instanceof DrawableImageResource){
					} else {
						filePathList.add("");
					}
				} else if (scene instanceof CollageScene) {
					for (CollageElement element : ((CollageScene) scene).getCollageElements()) {
						filePathList.add(element.path);
					}
				} else if (scene instanceof MultiLayerScene) {
					for (Scene layerScene : ((MultiLayerScene) scene).getLayers()) {
						if (layerScene.getClass().equals(LayerScene.class)) {
							LayerScene multiLayerImageScene = (LayerScene) layerScene;
							DataPath = multiLayerImageScene.getImageFilePath();
							filePathList.add(DataPath);
						} else if (layerScene.getClass().equals(VideoFileScene.class)) {
							VideoFileScene videoFileScene = (VideoFileScene) layerScene;
							DataPath = videoFileScene.getVideoFilePath();
							filePathList.add(DataPath);
						}
					}
				} else if (scene instanceof BurstShotScene) {
					BurstShotScene burstScene = (BurstShotScene) scene;
					int size = burstScene.getCacheCount();
					if (size > 0) {
						for (int index = 0; index < size; index++) {
							DataPath = burstScene.getImageFilePath(index);
							filePathList.add(DataPath);
						}
					}
				}
			}


		}catch(NullPointerException npe){
			L.e("Error : "  +npe.getMessage());
			return false;
		}

		for(String _path : filePathList){
			if (!_path.startsWith(context.getApplicationInfo().dataDir)) {
				if (!isFileExist(_path)) {
					L.d("path : " + _path + ", return false");
					return false;
				}
			}
		}

		return true;
	}

	public static String getAudioName(String audioPath) {
    	MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		
    	String audioName = null;
		try {
			mmr.setDataSource(audioPath);

			audioName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			
			mmr.release();
			mmr = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return audioName;
    }

    public static String getAssetAudioName(String audioPath, Context context) {
    	
    	MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    	
    	String audioName = null;
    	try {
    		AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd(audioPath);
			FileDescriptor fd = assetFileDescriptor.getFileDescriptor();
			mmr.setDataSource(fd, assetFileDescriptor.getStartOffset(), assetFileDescriptor.getDeclaredLength());
    		
    		audioName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    		
    		mmr.release();
    		mmr = null;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return audioName;
    }



	public static String changeOldMovieOverlayEffectDrawableId(String jsonScript) throws JSONException {
		JsonObject jsonObject = new JsonObject(jsonScript);
		for( JsonObject imageResourceJsonObject : jsonObject.findAll(JsonObject.class, ImageResource.DEFAULT_JSON_NAME)){
			imageResourceJsonObject.put(ImageResource.JSON_NAME_DRAWABLE_ID, R.drawable.vignette_blur); 
		}
		return jsonObject.toString(); 
	}
	
	
}
