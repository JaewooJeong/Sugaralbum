
package com.sugarmount.sugarcamera.story.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.BurstShotScene;
import com.kiwiple.multimedia.canvas.CollageScene;
import com.kiwiple.multimedia.canvas.DummyScene;
import com.kiwiple.multimedia.canvas.Effect;
import com.kiwiple.multimedia.canvas.FileImageResource;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageResource;
import com.kiwiple.multimedia.canvas.ImageTextScene;
import com.kiwiple.multimedia.canvas.KenBurnsScaler;
import com.kiwiple.multimedia.canvas.LayerScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.PixelCanvas;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.Scene.Editor;
import com.kiwiple.multimedia.canvas.TextEffect;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.canvas.data.CollageElement;
import com.kiwiple.multimedia.canvas.data.TextElement;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.kiwiple.multimedia.util.ImageUtils;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.scheduler.analysis.ImageAnalysis;
import com.kiwiple.scheduler.theme.Theme;
import com.sugarmount.sugarcamera.story.theme.ThemeManager;
import com.sugarmount.sugarcamera.story.utils.StoryUtils;
import com.sugarmount.sugarcamera.story.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StoryJsonPersister {
    private static StoryJsonPersister sPersister;

    private static final long AUDIO_MINIMUM_DURATION = 1000 * 60;
    private static final String CAMERA_DIR = Environment.getExternalStorageDirectory()
            + "/DCIM/Camera/";

    private Context mContext;
    private ContentResolver mContentResolver;
    private PreviewManager mPreviewManager;
    private Visualizer mVisualizer;
    
	private OnStoryJsonDataChangeListener mStoryJsonDataChangeListener; 
	
	public static interface OnStoryJsonDataChangeListener{
		public void onDataChanged(); 
	}

    private StoryJsonPersister(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mPreviewManager = PreviewManager.getInstance(context);
        mVisualizer = mPreviewManager.getVisualizer();
    }
    
    /** Get or create if not exist an instance of StoryJsonPersister */
    public static StoryJsonPersister getStoryJsonPersister(Context context) {
        if((sPersister == null) || !context.equals(sPersister.mContext)) {
            sPersister = new StoryJsonPersister(context);
        }

        return sPersister;
    }
    
    public void release() {
        sPersister = null;
    }
    

    public void setOnStoryJsonDataChangeListener(OnStoryJsonDataChangeListener listener){
    	mStoryJsonDataChangeListener = listener;
    }
    
    private void storyJsonDataChanged(){
    	 if(mStoryJsonDataChangeListener != null){
         	mStoryJsonDataChangeListener.onDataChanged(); 
         }
    }

    public void verifyJsonData(Uri jsonUri) {
        // TODO
        // String jsonString = getJsonString(jsonUri);
        //
        // BatchDataBuilder batchDataBuilder = new BatchDataBuilder();
        // ParsedSlide parsedSlide =
        // batchDataBuilder.makeSlideFromJsonString(jsonString);
        // Set<Integer> imageIds = new HashSet<Integer>();
        // Set<Integer> videoIds = new HashSet<Integer>();
        // makeSetIdsInParsedSlide(parsedSlide, imageIds, videoIds);
        // L.d("imageIds size = " + imageIds.size());
        // L.d("videoIds size = " + videoIds.size());
        //
        // if (imageIds.isEmpty() && videoIds.isEmpty()) {
        // return;
        // }
        //
        // int imageCursorSize = getCursorSizeInImageGallery(imageIds);
        // int videoCursorSize = getCursorSizeInVideoGallery(videoIds);
        // L.d("imageCursorSize  = " + imageCursorSize);
        // L.d("videoCursorSize  = " + videoCursorSize);
        //
        // if ((imageIds.size() == imageCursorSize) && (videoIds.size() ==
        // videoCursorSize)) {
        // return;
        // }
        //
        // Set<Integer> deletedImageIds = new HashSet<Integer>();
        // Set<Integer> deletedVideoIds = new HashSet<Integer>();
        //
        // makeDeletedSetIds(imageIds, videoIds, deletedImageIds,
        // deletedVideoIds);
        // L.d("deletedImageIds size = " + deletedImageIds.size());
        // L.d("deletedVideoIds size = " + deletedVideoIds.size());
        //
        // if (deletedImageIds.isEmpty() && deletedVideoIds.isEmpty()) {
        // return;
        // }
        //
        // verifyParsedSlide(parsedSlide, deletedImageIds, deletedVideoIds);
        // batchDataBuilder.setParsedSlide(parsedSlide);
        // batchDataBuilder.setCategoryTextBackgroundImage(mContext);
        // batchDataBuilder.countImageAndVideoData();
        //
        // ContentValues values = new ContentValues(2);
        // values.put(StoryJSONDatabaseConstants.JSONDatabaseField.JSON_STRING,
        // batchDataBuilder.processBatchJson());
        // values.put(StoryJSONDatabaseConstants.JSONDatabaseField.DURAION,
        // batchDataBuilder.getBatchJsonTotalDuration());
        // mContentResolver.update(jsonUri, values, null, null);
    }

    public Uri insertJsonData(String jsonString, String themeName, String schedulerVersion, boolean isFromUplusBox, String title) {
        String audioPath = "";
        boolean isAsset = true;

        if(jsonString != null) {
            JSONObject jsonObj;
            try {
                jsonObj = new JSONObject(jsonString);

                if(!jsonObj.isNull(PreviewManager.JSON_NAME_AUDIO)) {
                    JSONObject jsonAudio = jsonObj.getJSONObject(PreviewManager.JSON_NAME_AUDIO);
                    if(!jsonAudio.isNull(PreviewManager.JSON_NAME_AUDIO_IS_ASSET)) {
                        isAsset = jsonAudio.getBoolean(PreviewManager.JSON_NAME_AUDIO_IS_ASSET);
                    }

                    if(!jsonAudio.isNull(PreviewManager.JSON_NAME_AUDIO_FILE_PATH)) {
                        audioPath = jsonAudio.getString(PreviewManager.JSON_NAME_AUDIO_FILE_PATH);
                    }
                }
            } catch(JSONException e1) {
                e1.printStackTrace();
            }
        }

        ContentValues values = new ContentValues(8);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING_TITLE, title);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING, jsonString);
        int duration = mPreviewManager.getVisualizer().getDuration();
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.DURAION, duration);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.BG_MUSIC,
                   audioPath.substring(audioPath.indexOf('/') + 1, audioPath.lastIndexOf('.')));
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.IS_INTERNAL,
                   isAsset ? StoryJsonDatabaseConstants.BG_MUSIC_IS_INTERNAL
                           : StoryJsonDatabaseConstants.BG_MUSIC_IS_EXTERNAL);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.THEME, themeName);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.SCHEDULER_VERSION, schedulerVersion);
        if(isFromUplusBox){
        	values.put(StoryJsonDatabaseConstants.JsonDatabaseField.IS_FROM_UBOX, 1); 
        }else{
        	values.put(StoryJsonDatabaseConstants.JsonDatabaseField.IS_FROM_UBOX, 0); 
        }
        
        storyJsonDataChanged(); 
        
        return mContentResolver.insert(StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI,
                                       values);
    }
    
    public void saveJsonThumbnailImageTextDummyScene(Uri jsonUri){
    	Visualizer.Editor vEditor = null;
    	if (!mVisualizer.isOnEditMode()) {
    		vEditor = mVisualizer.getEditor().start();
    	}
    	
        List<Scene> scenes = mVisualizer.getRegion().getScenes();
        if(scenes != null && scenes.size() > 0) {
            for(Scene scene : scenes) {
                if(scene != null) {
                    Class sceneClass = null;
                    String path = null;

                    String orientation = String.valueOf(ImageAnalysis.ORIENTATION_DEGREE_0);
                    int mediaId = -1;
                    boolean makeSuccess = false;

                    sceneClass = scene.getClass();
                    L.e("sceneClass = "+ sceneClass);
                    
                    if(sceneClass.equals(DummyScene.class)){
                    	DummyScene dummyScene = (DummyScene) scene;
                    	path = dummyScene.getBackgroundFilePath();
                    	if(path == null || path.equals("")){
	                    	
                    		Size size = PreviewManager.DEFAULT_PREVIEW_RESOLUTION.getSize();
                    		PixelCanvas canvas = new PixelCanvas(size, false);
                    		dummyScene.getEditor().draw(canvas, 0);
                			Bitmap bitmap = com.sugarmount.sugarcamera.kiwiple.util.BitmapUtils.getBitmapImageFromPixelBuffer(size, canvas);
                			L.e("bitmap = "+ bitmap +", "+ bitmap.getWidth() +", "+ bitmap.getHeight());
                			if(bitmap != null) {
                				try {
                                    OutputStream output = mContentResolver.openOutputStream(jsonUri);
                                    bitmap.compress(CompressFormat.JPEG, 100, output);
                                    output.flush();
                                    output.close();
                                    bitmap.recycle();
                                    bitmap = null;
                				} catch (Exception e) {
                					e.printStackTrace();
                				}
                            }
                    		makeSuccess = mediaScanJson(getJsonDataPath(jsonUri));
                    		L.e("makeSuccess = "+ makeSuccess);
                    		if(makeSuccess) {
                    			break;
                    		}
                    	}
                    }else{
                    	/**
                    	 * dummy/ multilayer를 제외한 나머지 scene은 Intro다음 Scene으로 구분 
                    	 */
//	                    if(sceneClass.equals(ImageTextScene.class)){
//	                    	path = ((ImageTextScene.Editor)editor).getBackgroundFilePath();
//	                    }else 
                    	if(sceneClass.equals(VideoFileScene.class)) {
	                        path = ((VideoFileScene) scene).getVideoFilePath();
	                    }else if(sceneClass.equals(ImageFileScene.class)) {
	                    	ImageResource imageResource = ((ImageFileScene) scene).getImageResource();
	                    	if (imageResource instanceof FileImageResource) {
	                    		path = ((FileImageResource) imageResource).getFilePath();
	                    	} else {
	                    		// FIXME: do something for path.
	                    	}
	                    } else if(sceneClass.equals(CollageScene.class)) {
	                        path = ((CollageScene) scene).getCollageElements().get(0).path;
	                    } else if(sceneClass.equals(MultiLayerScene.class)) {
	                        Scene layerScene = ((MultiLayerScene) scene).getLayer(0);
	                        if (layerScene.getClass().equals(LayerScene.class)) {
	                            LayerScene layerImageScene = (LayerScene)((MultiLayerScene) scene).getLayer(0); 
	                            path = layerImageScene.getImageFilePath();    
	                        } else if (layerScene.getClass().equals(VideoFileScene.class)) {
	                            VideoFileScene layerVideoScene = (VideoFileScene)((MultiLayerScene) scene).getLayer(0); 
                                path = layerVideoScene.getVideoFilePath();
	                        }
	                    } else if(sceneClass.equals(BurstShotScene.class)) {
	                        path = ((BurstShotScene) scene).getImageFilePath(0);
	                    }
	
	                    if(sceneClass != null && path != null) {
	                        if(sceneClass.equals(VideoFileScene.class)) {
                            mediaId = getVideoIdInGallery(path);
                        } else {
                            mediaId = getImageIdInGallery(path);
                            orientation = String.valueOf(getImageOrientationInGallery(path));
                        }
	                    Scene.Editor sEditor = scene.getEditor();
	                    
                        saveThumbnail(mediaId, sceneClass, path, Integer.parseInt(orientation),jsonUri, sEditor);

                        // TODO copy CameraDir
                        // copyThumbnail(jsonUri);

                        ContentValues values = new ContentValues(1);
                        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.THUMB_ORIENTATION,
                                   Integer.parseInt(orientation));
	                        mContentResolver.update(jsonUri, values, null, null);
	
	                        makeSuccess = mediaScanJson(getJsonDataPath(jsonUri));
	                        L.e("makeSuccess = "+ makeSuccess);
	                        if(makeSuccess) {
	                            break;
	                        }
                        }
                    }
                }
            }
        }
        if (vEditor != null) {
        	vEditor.finish();
        }
        
        storyJsonDataChanged(); 

    	
    }
    
    public void saveJsonThumbnail(Uri jsonUri) {

		Visualizer.Editor vEditor = null;
    	if (!mVisualizer.isOnEditMode()) {
    		vEditor = mVisualizer.getEditor().start();
    	}
    	
    	List<Scene> scenes = mVisualizer.getRegion().getScenes();
        if( scenes== null || scenes.isEmpty()){
            return;
        }

        Scene introScene = scenes.get(0);
        boolean makeSuccess = false;
    		
		Size size = PreviewManager.DEFAULT_PREVIEW_RESOLUTION.getSize();
		PixelCanvas canvas = new PixelCanvas(size, false);
		introScene.getEditor().draw(canvas, 0);
		Bitmap bitmap = com.sugarmount.sugarcamera.kiwiple.util.BitmapUtils.getBitmapImageFromPixelBuffer(size, canvas);
		L.e("bitmap = "+ bitmap +", "+ bitmap.getWidth() +", "+ bitmap.getHeight());
		if(bitmap != null) {
			try {
                OutputStream output = mContentResolver.openOutputStream(jsonUri);
                bitmap.compress(CompressFormat.JPEG, 100, output);
                output.flush();
                output.close();
                bitmap.recycle();
                bitmap = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
		makeSuccess = mediaScanJson(getJsonDataPath(jsonUri));
		L.e("makeSuccess = "+ makeSuccess);
   
        if (vEditor != null) {
        	vEditor.finish();
        }
        storyJsonDataChanged(); 
    }

    public int updateJsonData(Uri jsonUri) {
        ContentValues values = new ContentValues(2);
        try {
            values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING,
                       mPreviewManager.toJsonObject().toString());
        } catch(JSONException e) {
            e.printStackTrace();
        }
        int duration = mPreviewManager.getVisualizer().getDuration();
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.DURAION, duration);
        
        storyJsonDataChanged();
        
        return mContentResolver.update(jsonUri, values, null, null);
    }
    public int updateJsonScriptData(Uri jsonUri, String jsonScript){
    	ContentValues values = new ContentValues(1);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING,jsonScript);
        return mContentResolver.update(jsonUri, values, null, null);
    }
    public int updateJsonDataScheculderVersion(Uri jsonUri, JSONObject JsonObject, String schedulerVersion) {
        ContentValues values = new ContentValues(2);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING,JsonObject.toString());
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.SCHEDULER_VERSION, schedulerVersion); 
        return mContentResolver.update(jsonUri, values, null, null);
    }
    
    public int updateJsonDataScheculderVersion(Uri jsonUri, String schedulerVersion) {
        ContentValues values = new ContentValues(1);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.SCHEDULER_VERSION, schedulerVersion); 
        return mContentResolver.update(jsonUri, values, null, null);
    }

    public void autoRemake(Uri jsonUri, String themeName) {
        Theme theme;
        boolean isInteranl = false;
        List<Theme> themeList = ThemeManager.getInstance(mContext).getThemeList();
        do {
            theme = themeList.get((int)(Math.random() * themeList.size()));
        } while(theme.name.equals(themeName));
        updateJsonTheme(jsonUri, theme);

        MusicInfo musicInfo = getMusicInfo();
        if(musicInfo == null) {
            musicInfo = new MusicInfo();
            String audioPath = theme.audioFileName;
            musicInfo.setTitle(audioPath.substring(0, audioPath.lastIndexOf('.')));
            musicInfo.setPath(audioPath);
            isInteranl = true;
        }
//        updateJsonBgMusic(jsonUri, musicInfo.getTitle(), musicInfo.getPath(), isInteranl, themeName);
    }

    public int updateJsonTitle(Uri jsonUri, String jsonTitle) {
    	
        List<Scene> scenes = mVisualizer.getRegion().getScenes();
        if(scenes != null && scenes.size() > 0) {
            for(int i = scenes.size() - 1; i >= 0; i--) {
                Scene scene = scenes.get(i);
                
                if(scene.getClass().equals(ImageTextScene.class)) {
                    List<TextElement> textElements = ((ImageTextScene) scene).getTextElements();
                    textElements.get(textElements.size() - 1).setText(jsonTitle); // This is meaningless.
                    break;
                } else if(scene.getClass().equals(DummyScene.class)) {
                    for(Effect effect : scene.getEffects()) {
                        if(effect.getClass().equals(TextEffect.class)) {
                        	if (mVisualizer.isOnEditMode()) {
                        		((TextEffect) effect).getEditor().setResourceText(jsonTitle);
                        	} else {
                        		Visualizer.Editor visualizerEditor = mVisualizer.getEditor().start();
                        		((TextEffect) effect).getEditor().setResourceText(jsonTitle);
                        		visualizerEditor.finish();
                        	}
                        }
                    }
                }
            }
        }
        
        ContentValues values = new ContentValues(2);
        try {
            values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING,
                       mPreviewManager.toJsonObject().toString());
        } catch(JSONException e) {
            e.printStackTrace();
        }
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING_TITLE, jsonTitle);
        return mContentResolver.update(jsonUri, values, null, null);
    }

    public int updateJsonTitleOnly(Uri jsonUri, String jsonTitle) {
        ContentValues values = new ContentValues(2);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING_TITLE, jsonTitle);
        return mContentResolver.update(jsonUri, values, null, null);
    }

    public int updateJsonTheme(Uri jsonUri, Theme theme) {
        adjustTheme(theme);
        adjustFilterTheme(theme);
        
        if(mPreviewManager.getVisualizer().isOnEditMode()){
        	L.d("have to visualizer edit finish before get json from preview manager"); 
        	mPreviewManager.getVisualizer().getEditor().finish(); 
        }else{
        	L.d("visualizer is not edit mode");
        }

        ContentValues values = new ContentValues(3);
        try {
            values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING,
                       mPreviewManager.toJsonObject().toString());
        } catch(JSONException e) {
            e.printStackTrace();
        }
        int duration = mPreviewManager.getVisualizer().getDuration();
        L.d("update duration : " + duration); 
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.DURAION, duration);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.THEME, theme.name);
        
        storyJsonDataChanged();
        
        return mContentResolver.update(jsonUri, values, null, null);
    }
  
    //분석/ 분석안된 음원 선택시 duration update
	public void updateBgMusicDuration(Uri mJsonDataUri, int duration) {
		ContentValues values = new ContentValues(1);
		int result ;
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.DURAION, duration);
        result  = mContentResolver.update(mJsonDataUri, values, null, null);
        L.d("result = "+ result +", duration = "+ duration);
	}
    
    /**
     * 
     * 
     * @param jsonUri
     * @param bgTitle
     * @param bgPath
     * @param inInternal : asset과 Theme Manager로 다운 받은 음악은 internal이다. 
     * @param isChangedTheme : 테마 변경 유무 
     * @param themeName
     * @return
     */
    public int updateJsonBgMusic(Uri jsonUri, String bgTitle, String bgPath, boolean inInternal, boolean isChangedTheme, String themeName) {
    	
    	boolean isInternalAudio= false;
    	
    	L.i("inInternal = "+ inInternal +", bgTitle = "+ bgTitle);
    	
    	if(Utils.isAssetDefaultMusic(bgTitle, mContext)){
    		isInternalAudio = true;
    	}else{
    		isInternalAudio = false;
    	}
    	
    	L.i("isInternalAudio = "+ isInternalAudio);
    	if(!isChangedTheme)
    		mPreviewManager.setAudioFile(bgPath, isInternalAudio);
    	
        ContentValues values = new ContentValues(3);
        try {
            values.put(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING,
                       mPreviewManager.toJsonObject().toString());
        } catch(JSONException e) {
            e.printStackTrace();
        }
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.BG_MUSIC, bgTitle);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.IS_INTERNAL,
        		inInternal ? StoryJsonDatabaseConstants.BG_MUSIC_IS_INTERNAL
                           : StoryJsonDatabaseConstants.BG_MUSIC_IS_EXTERNAL);
        return mContentResolver.update(jsonUri, values, null, null);
    }
    
    public int updateJsonBgMusic(Uri jsonUri, String bgTitle, String bgPath, boolean inInternal) {
    	
    	L.i("inInternal = "+ inInternal +", bgTitle = "+ bgTitle);
        ContentValues values = new ContentValues(2);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.BG_MUSIC, bgTitle);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.IS_INTERNAL,
        		inInternal ? StoryJsonDatabaseConstants.BG_MUSIC_IS_INTERNAL
                           : StoryJsonDatabaseConstants.BG_MUSIC_IS_EXTERNAL);
        return mContentResolver.update(jsonUri, values, null, null);
    }

    public void deleteJsonData(Uri uri) {
        mContentResolver.delete(uri, null, null);
        sendBroadcastForSmartBulletin(); 
        storyJsonDataChanged(); 
    }
    
    private void sendBroadcastForSmartBulletin() {
        /*
		Intent intent = new Intent(mContext, MovieDiaryCardProvider.class); 
		intent.setAction(MovieDiaryCardProvider.RELEASED_MOVIE_DIARY_STATUS_CARD); 
		mContext.sendBroadcast(intent);
        */
	}

    public void deleteAllJsonData() {
        mContentResolver.delete(StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI, null,
                                null);
        sendBroadcastForSmartBulletin(); 
        storyJsonDataChanged(); 
    }

    public String getJsonBgMusicPath(Uri jsonUri) {
        String bgMusicPath = "";
        String jsonString = getJsonString(jsonUri);
        JSONObject jsonObj = null;
        if(jsonString != null) {
            try {
                jsonObj = new JSONObject(jsonString);
                if(!jsonObj.isNull(PreviewManager.JSON_NAME_AUDIO)) {
                    JSONObject jsonBgMusic = jsonObj.getJSONObject(PreviewManager.JSON_NAME_AUDIO);
                    if(!jsonBgMusic.isNull(PreviewManager.JSON_NAME_AUDIO_FILE_PATH)) {
                        bgMusicPath = jsonBgMusic.getString(PreviewManager.JSON_NAME_AUDIO_FILE_PATH);
                    }
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
        return bgMusicPath;
    }

    public int getImageCount() {
        int imageCnt = 0;
        List<Scene> scenes = mVisualizer.getRegion().getScenes();
        if(scenes != null && scenes.size() > 0) {
            for(Scene scene : scenes) {
                Class sceneClass = scene.getClass();
                if(StoryUtils.isValidContentImageFileScene(scene)){
                	imageCnt++;
                }else if(sceneClass.equals(CollageScene.class) || sceneClass.equals(MultiLayerScene.class)|| sceneClass.equals(BurstShotScene.class)) {
                    imageCnt++;
                }
            }
        }
        return imageCnt;
    }

    public int getVideoCount() {
        int videoCnt = 0;
        List<Scene> scenes = mVisualizer.getRegion().getScenes();
        if(scenes != null && scenes.size() > 0) {
            for(Scene scene : scenes) {
                if(scene.getClass().equals(VideoFileScene.class)) {
                    videoCnt++;
                } else if (scene.getClass().equals(MultiLayerScene.class)) {
                    for (Scene layer : ((MultiLayerScene)scene).getLayers()) {
                        if (layer.getClass().equals(VideoFileScene.class)) {
                            videoCnt++;
                        }
                    }
                }
            }
        }
        return videoCnt;
    }
    
    public int getDuration(Uri jsonUri) {
        Cursor cursor = mContentResolver.query(jsonUri, new String[] {
            StoryJsonDatabaseConstants.JsonDatabaseField.DURAION
        }, null, null, null);

        int duration = 0;

        if(cursor == null) {
            return duration;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return duration;
        }

        cursor.moveToNext();
        duration = (int)cursor.getLong(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.DURAION));
        cursor.close();
        return duration;
    }
    
    public String getJsonSchedulerVersion(Uri jsonUri) {
        String versionString = null;
        Cursor cursor = mContentResolver.query(jsonUri, new String[] {
            StoryJsonDatabaseConstants.JsonDatabaseField.SCHEDULER_VERSION
        }, null, null, null);

        if(cursor == null) {
            return versionString;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return versionString;
        }

        cursor.moveToNext();
        if(cursor != null) {
        	versionString = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.SCHEDULER_VERSION));
            cursor.close();
        }
        return versionString;
    }

    public String getJsonString(Uri jsonUri) {
        String jsonString = null;
        Cursor cursor = mContentResolver.query(jsonUri, new String[] {
            StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING
        }, null, null, null);

        if(cursor == null) {
            return jsonString;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return jsonString;
        }

        cursor.moveToNext();
        if(cursor != null) {
            jsonString = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING));
            cursor.close();
        }
        return jsonString;
    }
    
    public String getThemeName(Uri jsonUri) {
        String themeName = null;
        Cursor cursor = mContentResolver.query(jsonUri, new String[] {
            StoryJsonDatabaseConstants.JsonDatabaseField.THEME
        }, null, null, null);

        if(cursor == null) {
            return themeName;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return themeName;
        }

        cursor.moveToNext();
        if(cursor != null) {
        	themeName = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.THEME));
            cursor.close();
        }
        return themeName;
    }

    public int getJsonUnReadCount() {
        int unread = 0;
        Cursor cursor = mContentResolver.query(StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI,
                                               new String[] {
                                                   StoryJsonDatabaseConstants.JsonDatabaseField._ID
                                               }, getJsonUnreadSelection(), null, null);

        if(cursor == null) {
            unread = 0;
        }

        unread = cursor.getCount();
        cursor.close();
        return unread;
    }

    public int updateJsonRead(Uri jsonUri) {
        ContentValues values = new ContentValues(1);
        values.put(StoryJsonDatabaseConstants.JsonDatabaseField.READ,
                   StoryJsonDatabaseConstants.JSON_READ);
        return mContentResolver.update(jsonUri, values, null, null);
    }

	//수동 생성한 무비다이어리를 미독으로 update
	public int addUnreadJsonRead(Uri jsonUri) {
		ContentValues values = new ContentValues(1);
		values.put(StoryJsonDatabaseConstants.JsonDatabaseField.READ, StoryJsonDatabaseConstants.JSON_UNREAD);
		return mContentResolver.update(jsonUri, values, null, null);
	}
	
    // TODO
    // private void makeSetIdsInParsedSlide(ParsedSlide parsedSlide,
    // Set<Integer> imageIds, Set<Integer> videoIds) {
    // for (int i = 0; i < parsedSlide.getSceneList().size() -
    // ImageAnalysis.TITLE_AND_LOGO_COUNT; i++) {
    // ParsedScene scene = parsedSlide.getSceneList().get(i);
    // if
    // (scene.getType().equals(ThemeJsonNamespace.VALUE_SCENE_TYPE_IMAGE_FILE))
    // {
    // imageIds.add(scene.getId());
    // } else if
    // (scene.getType().equals(ThemeJsonNamespace.VALUE_SCENE_TYPE_COLLAGE_IMAGE))
    // {
    // ParsedCollageData collageData = scene.getCollage();
    // if (collageData != null) {
    // for (int j = 0; j < collageData.getCollageDataSize(); j++) {
    // imageIds.add(collageData.getIdList().get(j));
    // }
    // }
    // } else if
    // (scene.getType().equals(ThemeJsonNamespace.VALUE_SCENE_TYPE_VIDEO)) {
    // videoIds.add(scene.getId());
    // }
    // }
    // }

    private int getCursorSizeInImageGallery(Set<Integer> imageIds) {
        int retVal = 0;
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                               new String[] {
                                                   MediaStore.Images.ImageColumns._ID
                                               },
                                               getVerifyDataSelection(MediaStore.Images.Media._ID,
                                                                      imageIds), null, null);

        if(cursor == null) {
            return retVal;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return retVal;
        }

        cursor.moveToNext();
        retVal = cursor.getCount();
        cursor.close();
        return retVal;
    }

    private int getCursorSizeInVideoGallery(Set<Integer> videoIds) {
        int retVal = 0;
        Cursor cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                               new String[] {
                                                   MediaStore.Video.VideoColumns._ID
                                               },
                                               getVerifyDataSelection(MediaStore.Video.Media._ID,
                                                                      videoIds), null, null);

        if(cursor == null) {
            return retVal;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return retVal;
        }

        cursor.moveToNext();
        retVal = cursor.getCount();
        cursor.close();
        return retVal;
    }

    private String getVerifyDataSelection(String id, Set<Integer> ids) {
        StringBuilder builder = new StringBuilder();
        Iterator<Integer> itr = ids.iterator();
        builder.append(id);
        builder.append(" IN ");
        builder.append(" ( ");
        for(int i = 0; i < ids.size(); i++) {
            builder.append(itr.next());
            if(i < ids.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(" ) ");
        return builder.toString();
    }

    private void makeDeletedSetIds(Set<Integer> imageIds, Set<Integer> videoIds,
            Set<Integer> deletedImageIds, Set<Integer> deletedVideoIds) {
        Iterator<Integer> itr = imageIds.iterator();
        while(itr.hasNext()) {
            int id = itr.next();
            if(!isExistImageDataInGallery(id)) {
                deletedImageIds.add(id);
            }
        }

        itr = videoIds.iterator();
        while(itr.hasNext()) {
            int id = itr.next();
            if(!isExistVideoDataInGallery(id)) {
                deletedVideoIds.add(id);
            }
        }
    }

    private boolean isExistImageDataInGallery(int id) {
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                               new String[] {
                                                   MediaStore.Images.ImageColumns._ID
                                               }, getExistImageDataInGallerySelection(id), null,
                                               null);

        if(cursor == null) {
            return false;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return false;
        }

        if(cursor != null) {
            cursor.close();
        }
        return true;
    }

    private String getExistImageDataInGallerySelection(int id) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(" ( ");
        buffer.append(MediaStore.Images.ImageColumns._ID);
        buffer.append(" = ");
        buffer.append(id);
        buffer.append(" ) ");
        return buffer.toString();
    }

    private boolean isExistVideoDataInGallery(int id) {
        Cursor cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                               new String[] {
                                                   MediaStore.Video.VideoColumns._ID
                                               }, getExistVideoDataInGallerySelection(id), null,
                                               null);

        if(cursor == null) {
            return false;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return false;
        }

        if(cursor != null) {
            cursor.close();
        }

        return true;
    }

    private String getExistVideoDataInGallerySelection(int id) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(" ( ");
        buffer.append(MediaStore.Video.VideoColumns._ID);
        buffer.append(" = ");
        buffer.append(id);
        buffer.append(" ) ");
        return buffer.toString();
    }

    // TODO
    // private void verifyParsedSlide(ParsedSlide parsedSlide, Set<Integer>
    // deletedImageIds, Set<Integer> deletedVideoIds) {
    // Iterator<Integer> itr = deletedImageIds.iterator();
    // Set<ParsedScene> deleteSceneList = new HashSet<ParsedScene>();
    // while (itr.hasNext()) {
    // int id = itr.next();
    // for (int i = 0; i < parsedSlide.getSceneList().size() -
    // ImageAnalysis.TITLE_AND_LOGO_COUNT; i++) {
    // ParsedScene scene = parsedSlide.getSceneList().get(i);
    // if
    // (scene.getType().equals(ThemeJsonNamespace.VALUE_SCENE_TYPE_IMAGE_FILE))
    // {
    // if (id == scene.getId()) {
    // deleteSceneList.add(scene);
    // }
    // } else if
    // (scene.getType().equals(ThemeJsonNamespace.VALUE_SCENE_TYPE_COLLAGE_IMAGE))
    // {
    // ParsedCollageData collageData = scene.getCollage();
    // boolean isFind = false;
    // if (collageData != null) {
    // for (int j = 0; j < collageData.getCollageDataSize(); j++) {
    // if (id == collageData.getIdList().get(j)) {
    // isFind = true;
    // break;
    // }
    // }
    // }
    // if (isFind) {
    // deleteSceneList.add(scene);
    // }
    // }
    // }
    // }
    //
    // itr = deletedVideoIds.iterator();
    // while (itr.hasNext()) {
    // int id = itr.next();
    // for (int i = 0; i < parsedSlide.getSceneList().size() -
    // ImageAnalysis.TITLE_AND_LOGO_COUNT; i++) {
    // ParsedScene scene = parsedSlide.getSceneList().get(i);
    // if (scene.getType().equals(ThemeJsonNamespace.VALUE_SCENE_TYPE_VIDEO)) {
    // if (id == scene.getId()) {
    // deleteSceneList.add(scene);
    // }
    // }
    // }
    // }
    //
    // for (ParsedScene scene : deleteSceneList) {
    // int index = parsedSlide.getSceneList().indexOf(scene);
    // parsedSlide.getSceneList().remove(index);
    // if (index < parsedSlide.getTransitionList().size()) {
    // parsedSlide.getTransitionList().remove(index);
    // }
    // }
    //
    // int position = parsedSlide.getSceneList().size() -
    // ImageAnalysis.TITLE_AND_LOGO_COUNT - 1;
    // if
    // (parsedSlide.getSceneList().get(position).getType().equals(ThemeJsonNamespace.VALUE_SCENE_TYPE_IMAGE_TEXT))
    // {
    // L.d("Last Text Calculate = " +
    // parsedSlide.getSceneList().get(position).getCategory().getCategoryTextList().get(0));
    // parsedSlide.getSceneList().remove(position);
    // if (position < parsedSlide.getTransitionList().size()) {
    // parsedSlide.getTransitionList().remove(position);
    // }
    // }
    // }

    private String getJsonTitle() {
        String jsonTitle = "";

        List<Scene> scenes = mVisualizer.getRegion().getScenes();
        if(scenes != null && scenes.size() > 0) {
            for(int i = scenes.size() - 1; i >= 0; i--) {
                Scene scene = scenes.get(i);
                if(scene.getClass().equals(ImageTextScene.class)) {
                    List<TextElement> textElements = ((ImageTextScene) scene).getTextElements();
                    jsonTitle = textElements.get(textElements.size() - 1).getText();
                    break;
                } else if(scene.getClass().equals(DummyScene.class)) {
                    for(Effect effect : scene.getEffects()) {
                        if(effect.getClass().equals(TextEffect.class)) {
                        	jsonTitle = ((TextEffect) effect).getResourceText();  
                        }
                    }
                }
            }
        }
        return jsonTitle;
    }

    private int getImageIdInGallery(String path) {
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                               new String[] {
                                                   MediaStore.Images.ImageColumns._ID
                                               }, getMediaStoreImageSelection(path), null, null);

        if(cursor == null) {
            return -1;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return -1;
        }

        cursor.moveToNext();
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));

        if(cursor != null) {
            cursor.close();
        }
        return id;
    }

    private int getImageOrientationInGallery(String path) {
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                               new String[] {
                                                   MediaStore.Images.ImageColumns.ORIENTATION
                                               }, getMediaStoreImageSelection(path), null, null);

        if(cursor == null) {
            return -1;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return -1;
        }

        cursor.moveToNext();
        int orientation = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));

        if(cursor != null) {
            cursor.close();
        }
        return orientation;
    }

    private String getMediaStoreImageSelection(String path) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(" ( ");
        buffer.append(MediaStore.Images.ImageColumns.DATA);
        buffer.append(" = ");
        buffer.append("'");
        buffer.append(path);
        buffer.append("'");
        buffer.append(" ) ");
        return buffer.toString();
    }

    private int getVideoIdInGallery(String path) {
        Cursor cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                               new String[] {
                                                   MediaStore.Video.VideoColumns._ID
                                               }, getMediaStoreVideoSelection(path), null, null);

        if(cursor == null) {
            return -1;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return -1;
        }

        cursor.moveToNext();
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID));

        if(cursor != null) {
            cursor.close();
        }
        return id;
    }

    private String getMediaStoreVideoSelection(String path) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(" ( ");
        buffer.append(MediaStore.Video.VideoColumns.DATA);
        buffer.append(" = ");
        buffer.append("'");
        buffer.append(path);
        buffer.append("'");
        buffer.append(" ) ");
        return buffer.toString();
    }

    private void saveThumbnail(int mediaId, Class sceneClass, String path,
            int orientation, Uri jsonUri, Editor editor) {
        final Uri baseUri = sceneClass.equals(VideoFileScene.class) ? Video.Thumbnails.EXTERNAL_CONTENT_URI
                : Images.Thumbnails.EXTERNAL_CONTENT_URI;
        final String column = sceneClass.equals(VideoFileScene.class) ? Video.Thumbnails.VIDEO_ID : Images.Thumbnails.IMAGE_ID;

        Cursor cursor = null;
        Uri thumbUri = null;
        /**
         * 프리뷰 첫 Image 사이즈와 동일하게 Thumbnail 생성 후 저장 /story/thumb 폴더 
         */
        Resolution resolution = Resolution.NHD;
        int targetWidth = resolution.width;
        int targetHeight = resolution.height;
        
    	Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
    	Canvas canvas = new Canvas(bitmap);
    	canvas.drawColor(Color.BLACK);
    	Bitmap backgroundBitmap = null;
    	
        /**
         * 비디오 Scene
         */
    	try {
	        backgroundBitmap= getStoryThumbnail(path, resolution, orientation, editor);
	        
        if(backgroundBitmap != null) {
        	
        		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        		paint.setAlpha(255);
        		
	        	/*
	        	 * aubergine : 20150527 storylist thumnail만들때 centercrop -> kenburn view port영역적용으로 변경됨에 따라
	        	 * 관련 소스 수정처리함.
	        	 */
				if (sceneClass.equals(ImageFileScene.class)) {
					// TODO sest viewport
					ImageFileScene.Editor imageFileSceneEditor = (ImageFileScene.Editor) editor;
					KenBurnsScaler.Editor kenburnScaler = (KenBurnsScaler.Editor) imageFileSceneEditor.getObject().getScaler().getEditor();
					Viewport mBackgroundViewport = kenburnScaler.getObject().getViewports()[0];
						
					int imageWidth = backgroundBitmap.getWidth();
					int imageHeight = backgroundBitmap.getHeight();
					float left = imageWidth * mBackgroundViewport.left;
					float top = imageHeight * mBackgroundViewport.top;
					float scale = targetWidth / (imageWidth * mBackgroundViewport.width());
					Matrix matrix = new Matrix();
					matrix.postTranslate(-left, -top);
					matrix.postScale(scale, scale);
					canvas.drawBitmap(backgroundBitmap, matrix, paint);
				} else{
					canvas.drawBitmap(backgroundBitmap, (targetWidth - backgroundBitmap.getWidth()) / 2, (targetHeight - backgroundBitmap.getHeight()) / 2, paint);
				}
					backgroundBitmap.recycle();
			}
        
        if(bitmap != null) {
        	OutputStream output = mContentResolver.openOutputStream(jsonUri);
            bitmap.compress(CompressFormat.JPEG, 100, output);
            output.flush();
            output.close();
            bitmap.recycle();
            bitmap = null;
        } 
        
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
	 * 무비다이어리 Story 파일 저장
	 */
	private static Bitmap getStoryThumbnail(String path, Resolution resolution, int orientation, Scene.Editor editor) {

		Bitmap bitmap = null;
		if (ImageUtils.getMimeType(path).startsWith("image/")) {
			try {
				
				Size originalSize = ImageUtils.measureImageSize(path);
				int targetWidth = resolution.width;
				int targetHeight = resolution.height;

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = ImageUtils.measureSampleSize(originalSize, targetWidth, targetHeight, orientation);
				options.inPreferredConfig = Config.ARGB_8888;
				options.inMutable = true;

				bitmap = BitmapFactory.decodeFile(path, options);
				L.w("Thumb :"+bitmap.getWidth() +", "+ bitmap.getHeight());
				bitmap = ImageUtils.createScaledBitmapAspectRatioMaintained(bitmap, targetWidth, targetHeight, true);

				if (bitmap != null) {
					if (orientation != 0) {
						bitmap = ImageUtils.createRotatedBitmap(bitmap, orientation, true);
					}
					bitmap = ImageUtils.createScaledBitmapForCenterCrop(bitmap, resolution.width, resolution.height, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (ImageUtils.getMimeType(path).startsWith("video/")) {
			/**
			 * Editor를 이용해 start position을 가져오기 때문에 Video Scene에 대해서만 처리
			 */
			try {
				MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
				mRetriever.setDataSource(path);
				VideoFileScene.Editor vEditor = (VideoFileScene.Editor) editor;
				VideoFileScene videoFileScene = vEditor.getObject();
				long startPosition = videoFileScene.getVideoStartPosition();
				bitmap = mRetriever.getFrameAtTime(startPosition * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

				L.w("Thumb : "+bitmap.getWidth() +", "+ bitmap.getHeight());
				if (bitmap != null) {
					if (!Config.ARGB_8888.equals(bitmap.getConfig())) {
						Bitmap newConfigBitmap = bitmap.copy(Config.ARGB_8888, true);
						bitmap.recycle();
						bitmap = newConfigBitmap;
					}

					bitmap = ImageUtils.createScaledBitmapForCenterCrop(bitmap, resolution.width, resolution.height, true);
				}
				mRetriever.release();
				mRetriever = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (bitmap != null)
			L.i("w = " + bitmap.getWidth() + ", h =" + bitmap.getHeight());
		else
			L.i("bitmap is null");

		return bitmap;
	}
    
	private BitmapFactory.Options getBitmapOption() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.ARGB_8888; //<-- 요걸로 설정하는 경우, 갤럭시S3/6열/상세보기시 애니메이션 끊겨서 보임.
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inDither = true;
		return options;
	}
    
    /**
     * uri to file path 
     */
    public String getPathFromUri(Uri uri){
    	String path = null;
    	try {
	    	Cursor cursor = mContentResolver.query(uri, null, null, null, null );
	    	cursor.moveToNext(); 
	    	path = cursor.getString( cursor.getColumnIndex( "_data" ) );
	    	cursor.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return path;
    }
    

    private void copy(InputStream input, OutputStream output) throws Exception, IOException {
        byte[] buffer = new byte[1024 * 2];

        BufferedInputStream in = new BufferedInputStream(input, 1024 * 2);
        BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 2);
        int n = 0;
        try {
            while((n = in.read(buffer, 0, 1024 * 2)) != -1) {
                out.write(buffer, 0, n);
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap createBitmap(Uri thumbUri, Class sceneClass, String path,
            int orientation) {
        Bitmap bitmap = null;
        InputStream input = null;
        if(thumbUri == null && sceneClass.equals(VideoFileScene.class)) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            bitmap = retriever.getFrameAtTime(0);
            if(bitmap == null) {
                return null;
            }
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(orientation > 0) {
            try {
                bitmap = createBitmap(mContentResolver.openInputStream(thumbUri), orientation);
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if(input != null) {
                    try {
                        input.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bitmap;
    }

    private Bitmap createBitmap(InputStream input, int orientation) {
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        bitmap = ImageUtils.createRotatedBitmap(bitmap, orientation, true);
        return bitmap;
    }

    private boolean mediaScanJson(String path) {
    	// 해당 json 삭제후 null check
    	if(path == null || path.equals("")) return true;
    	
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
        return true;
    }

    private void copyThumbnail(Uri jsonUri) {
        String storyPath = getJsonDataPath(jsonUri);

        if(storyPath == null) {
            return;
        }

        File story = new File(storyPath);
        File cameraStory = new File(CAMERA_DIR
                + storyPath.substring(storyPath.lastIndexOf('/') + 1, storyPath.length()));

        FileChannel src = null;
        FileChannel dst = null;
        try {
            if(!cameraStory.exists()) {
                cameraStory.createNewFile();
            }

            src = new FileInputStream(story).getChannel();
            dst = new FileOutputStream(cameraStory).getChannel();
            dst.transferFrom(src, 0, src.size());

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(src != null) {
                    src.close();
                }

                if(dst != null) {
                    dst.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void adjustTheme(Theme theme) {
        // slide.getTheme().setThemeName(themeName);
        // for (ParsedScene scene : slide.getSceneList()) {
        // if
        // (scene.getType().equals(ThemeJsonNamespace.VALUE_SCENE_TYPE_IMAGE_FILE))
        // {
        // scene.setFilterId(filterId);
        // } else if
        // (scene.getType().equals(ThemeJsonNamespace.VALUE_SCENE_TYPE_COLLAGE_IMAGE))
        // {
        // ParsedCollageData collageData = changeCollageData(themeName,
        // scene.getCollage());
        // if (collageData.getCollageDataSize() > 0) {
        // scene.setCollage(collageData);
        // }
        // scene.setFilterId(filterId);
        // }
        // }
    }

    private void adjustFilterTheme(Theme theme) {
        // Object filterId = Theme
        //
        // if (filterId != null) {
        // L.d("FilterId = " + (int) filterId);
        // } else {
        // filterId = ImageAnalysis.DEFAULT_FILTER_ID;
        // }
        // return (int) filterId;
    }

    //
    // private ParsedCollageData changeCollageData(String themeName,
    // ParsedCollageData collageData) {
    // String tempThemeName;
    // if (isDesignTheme(themeName)) {
    // tempThemeName = themeName;
    // } else {
    // tempThemeName = Theme.DEFAULT.name;
    // }
    //
    // ParsedCollageData retCollageData = new ParsedCollageData();
    // ArrayList<ImageResData> imageDataList = makeImageDataList(collageData);
    // if (imageDataList != null && imageDataList.size() > 0) {
    // CollageCorrectAdviser collageCorrectAdviser = new
    // CollageCorrectAdviser(mContext,
    // MultimediaEnvironment.ASSET_DEFAULT_COLLAGE_PATH, true);
    // ArrayList<TemplateInfo> templateInfoList =
    // collageCorrectAdviser.getTemplateArrayForThemeAndFrameCount(tempThemeName,
    // imageDataList.size());
    // int templetId = -1;
    // if (templateInfoList != null && templateInfoList.size() > 0) {
    // templetId = templateInfoList.get(0).getId();
    // }
    //
    // if (templetId != -1) {
    // imageDataList =
    // collageCorrectAdviser.getCollageImageDatasWithTemplateId(imageDataList,
    // templetId,
    // ImageAnalysis.COLLAGE_DEFAULT_IMAGE_SIZE);
    // collageData = makeCollageData(imageDataList);
    // if (collageData.getCollageDataSize() > 0) {
    // retCollageData = collageData;
    // }
    // }
    // }
    // return retCollageData;
    // }

    // TODO
    // private ArrayList<ImageResData> makeImageDataList(ParsedCollageData
    // collageData) {
    // ArrayList<ImageResData> imageDataList = new ArrayList<ImageResData>();
    // if (collageData != null) {
    // for (int i = 0; i < collageData.getCollageDataSize(); i++) {
    // ImageSearch imageSearch = new ImageSearch(mContext, null);
    // ImageResData imageData =
    // imageSearch.getImagaeDataForImageId(collageData.getIdList().get(i));
    //
    // if (imageData == null) {
    // imageData = new ImageResData();
    // imageData.id = collageData.getIdList().get(i);
    // imageData.path = collageData.getPathList().get(i);
    // imageData.orientation = collageData.getOrientationList().get(i);
    // }
    //
    // if (i < collageData.getCollageTempletIdList().size()) {
    // imageData.imageCorrectData.collageTempletId =
    // collageData.getCollageTempletIdList().get(i);
    // }
    // if (i < collageData.getCollageCoordinateList().size()) {
    // imageData.imageCorrectData.collageCoordinate = new
    // FacePointF(collageData.getCollageCoordinateList().get(i).x, collageData
    // .getCollageCoordinateList().get(i).y);
    // }
    // if (i < collageData.getCollageScaleList().size()) {
    // imageData.imageCorrectData.collageScale =
    // collageData.getCollageScaleList().get(i);
    // }
    // if (i < collageData.getCollageRotateList().size()) {
    // imageData.imageCorrectData.collageRotate =
    // collageData.getCollageRotateList().get(i);
    // }
    // if (i < collageData.getCollageWidthList().size()) {
    // imageData.imageCorrectData.collageWidth =
    // collageData.getCollageWidthList().get(i);
    // }
    // if (i < collageData.getCollageHeightList().size()) {
    // imageData.imageCorrectData.collageHeight =
    // collageData.getCollageHeightList().get(i);
    // }
    // if (i < collageData.getCollageFrameBorderWidthList().size()) {
    // imageData.imageCorrectData.collageFrameBorderWidth =
    // collageData.getCollageFrameBorderWidthList().get(i);
    // }
    // if (i < collageData.getCollageFrameCornerRadiusList().size()) {
    // imageData.imageCorrectData.collageFrameCornerRadius =
    // collageData.getCollageFrameCornerRadiusList().get(i);
    // }
    // if (i < collageData.getCollageBackgroudColorList().size()) {
    // imageData.imageCorrectData.collageBackgroundColor =
    // collageData.getCollageBackgroudColorList().get(i);
    // }
    // if (i < collageData.getCollageBackgroundColorTagList().size()) {
    // imageData.imageCorrectData.collageBackgroundColorTag =
    // collageData.getCollageBackgroundColorTagList().get(i);
    // }
    // if (i < collageData.getCollageBackgroudImageFileNameList().size()) {
    // imageData.imageCorrectData.collageBackgroundImageFileName =
    // collageData.getCollageBackgroudImageFileNameList().get(i);
    // }
    // imageDataList.add(imageData);
    // }
    // }
    // return imageDataList;
    // }
    //
    // private ParsedCollageData makeCollageData(ArrayList<ImageResData>
    // collageCorrectImageDatas) {
    // ParsedCollageData collageData = new ParsedCollageData();
    // for (ImageResData correctCollageData : collageCorrectImageDatas) {
    // collageData.addId(correctCollageData.id);
    // collageData.addPath(correctCollageData.path);
    // collageData.addOrientation(correctCollageData.orientation);
    // collageData.addCollageTempletId(correctCollageData.imageCorrectData.collageTempletId);
    // collageData.addCollageCoordinate(new
    // PointF(correctCollageData.imageCorrectData.collageCoordinate.x,
    // correctCollageData.imageCorrectData.collageCoordinate.y));
    // collageData.addCollageScale(correctCollageData.imageCorrectData.collageScale);
    // collageData.addCollageRotate(correctCollageData.imageCorrectData.collageRotate);
    // collageData.addCollageWidth(correctCollageData.imageCorrectData.collageWidth);
    // collageData.addCollageHeight(correctCollageData.imageCorrectData.collageHeight);
    // collageData.addCollageFrameBorderWidth(correctCollageData.imageCorrectData.collageFrameBorderWidth);
    // collageData.addCollageFrameCornerRaidus(correctCollageData.imageCorrectData.collageFrameCornerRadius);
    // collageData.addCollageBackgroudColor(correctCollageData.imageCorrectData.collageBackgroundColor);
    // collageData.addCollageBackgroundColorTag(correctCollageData.imageCorrectData.collageBackgroundColorTag);
    // collageData.addCollageBackgroudImageFileName(correctCollageData.imageCorrectData.collageBackgroundImageFileName);
    // }
    // return collageData;
    // }
    //
    // private void adjustSplitLineColor(ParsedSlide slide, String themeName) {
    // for (ParsedTransition transition : slide.getTransitionList()) {
    // String type = transition.getType();
    // if (type.equals(ThemeJsonNamespace.VALUE_TRANSITION_TYPE_SPLIT)) {
    // transition.setLineColor(ThemeFrame.getThemeSplitLineColorHashMap().get(themeName));
    // }
    // }
    // }

    public String getJsonDataPath(Uri jsonUri) {
        Cursor cursor = mContentResolver.query(jsonUri, new String[] {
            StoryJsonDatabaseConstants.JsonDatabaseField._DATA
        }, null, null, null);

        if(cursor == null) {
            return null;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField._DATA));

        if(cursor != null) {
            cursor.close();
        }
        return path;
    }

    private String getJsonUnreadSelection() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(" ( ");
        buffer.append(StoryJsonDatabaseConstants.JsonDatabaseField.READ);
        buffer.append(" = ");
        buffer.append(StoryJsonDatabaseConstants.JSON_UNREAD);
        buffer.append(" ) ");
        return buffer.toString();
    }

    private MusicInfo getMusicInfo() {
        Cursor cursor = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                               new String[] {
                                                       MediaStore.Audio.Media.TITLE,
                                                       MediaStore.Audio.Media.DATA
                                               }, getMusicSelection(), null, null);

        if(cursor == null) {
            return null;
        }

        if(cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        ArrayList<MusicInfo> musicInfoArray = new ArrayList<MusicInfo>(cursor.getCount());

        while(cursor.moveToNext()) {
            MusicInfo musciInfo = new MusicInfo();
            musciInfo.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
            musciInfo.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
            musicInfoArray.add(musciInfo);
        }

        int index = (int)(Math.random() * cursor.getCount());

        if(cursor != null) {
            cursor.close();
        }
        return musicInfoArray.get(index);
    }

    private String getMusicSelection() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(MediaStore.Audio.AudioColumns.DURATION);
        buffer.append(" >= ");
        buffer.append(AUDIO_MINIMUM_DURATION);
        return buffer.toString();
    }

    private class MusicInfo {
        private String mTitle;
        private String mPath;

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            this.mTitle = title;
        }

        public String getPath() {
            return mPath;
        }

        public void setPath(String mPath) {
            this.mPath = mPath;
        }
    }
    
    public static final String JSON_NAME_FILE_PATH = "file_path"; 
    public ArrayList<String> getUriDatas(Uri jsonUri){
    	ArrayList<String> items = new ArrayList<String>(); 
    	
    	String jsonScript = getJsonString(jsonUri); 

		try {
			JSONObject jsonObject = new JSONObject(jsonScript);
			String path = null; 
			JSONObject jsonRegionObject = jsonObject.getJSONObject(Visualizer.JSON_NAME_REGIONS);
			JSONArray jsonSceneArray = jsonRegionObject.getJSONArray(Region.JSON_NAME_SCENES);
			for (int i = 0; i < jsonSceneArray.length(); i++) {
				
				JSONObject jsonSceneObject = jsonSceneArray.getJSONObject(i);
				
				if (!jsonSceneObject.isNull(JSON_NAME_FILE_PATH)) {
					path = jsonSceneObject.getString(JSON_NAME_FILE_PATH); 
					items.add(path); 

				}
				
				if (!jsonSceneObject.isNull(CollageScene.JSON_NAME_COLLAGE_ELEMENTS)) {
		
					JSONArray collageArray = jsonSceneObject.getJSONArray(CollageScene.JSON_NAME_COLLAGE_ELEMENTS); 
					for(int j = 0; j<collageArray.length(); j++){
						JSONObject collageObject = collageArray.getJSONObject(j); 
						path = collageObject.getString(JSON_NAME_FILE_PATH);
						items.add(path); 
					}
				}
				
				if (!jsonSceneObject.isNull(MultiLayerScene.JSON_NAME_LAYERS)) {
		
					JSONArray multiArray = jsonSceneObject.getJSONArray(MultiLayerScene.JSON_NAME_LAYERS); 
					for(int j = 0; j<multiArray.length(); j++){
						JSONObject collageObject = multiArray.getJSONObject(j); 
						path = collageObject.getString(JSON_NAME_FILE_PATH);
						items.add(path); 
					}
				}
			}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return items; 
    }
    
    
    /**
     * 무비다이어리에 포함된 전체 이미지의 path를 string array 로 리턴한다  
     * 
     */
	public String[] getImagesPathInMovieDiary(boolean isAllowDuplicate) {

		ArrayList<String> filePathList = new ArrayList<>();
		Region region = PreviewManager.getInstance(mContext).getVisualizer().getRegion();
		
		for (Scene scene : region.getScenes()) {

			if (StoryUtils.isValidContentImageFileScene(scene))
			{
				ImageResource resource = ((ImageFileScene) scene).getImageResource();
				if (resource instanceof FileImageResource) {
					filePathList.add(((FileImageResource) resource).getFilePath());
				}
			} else if (scene instanceof CollageScene) {
				for (CollageElement element : ((CollageScene) scene).getCollageElements()) {
					filePathList.add(element.path);
				}
			} else if (scene instanceof MultiLayerScene) {
				for (Scene layerScene : ((MultiLayerScene) scene).getLayers()) {
					if(layerScene.getClass().equals(LayerScene.class)){
						LayerScene multiLayerImageScene = (LayerScene)layerScene; 
						filePathList.add(multiLayerImageScene.getImageFilePath());
					}
				}
			} else {
				continue;
			}
		}
		if (!isAllowDuplicate) {
			HashSet<String> filePathSet = new HashSet<String>(filePathList);
			filePathList.removeAll(filePathList);
			filePathList.addAll(filePathSet);
		}
		return filePathList.toArray(new String[filePathList.size()]);
	
	}
}
