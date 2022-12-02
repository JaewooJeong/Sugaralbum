package com.kiwiple.scheduler.util;

import java.io.IOException;

import android.content.Context;
import android.text.TextUtils;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageframework.util.BitmapUtils;
import com.kiwiple.multimedia.canvas.BurstShotScene;
import com.kiwiple.multimedia.canvas.CollageScene;
import com.kiwiple.multimedia.canvas.DummyScene;
import com.kiwiple.multimedia.canvas.FileImageResource;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageResource;
import com.kiwiple.multimedia.canvas.ImageResource.ScaleType;
import com.kiwiple.multimedia.canvas.ImageTextScene;
import com.kiwiple.multimedia.canvas.LayerScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.VideoFileImageResource;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.scheduler.coordinate.scaler.KenBurnsScalerCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusOutroSceneCoordinator;
import com.kiwiple.scheduler.tag.UserTag;
import com.kiwiple.scheduler.theme.Theme;

public class IntroOutroUtils {

	public static final int VIDEO_POSITION_0 = 0;

	/**
	 * Scene에 들어 있는 file의 path를 반환.
	 * 
	 * @param sceneClass
	 *            : Scene
	 * @return : file path.
	 */
	public static String getImage(Scene sceneClass) {
		String imagePath = null;
		if (sceneClass.getClass().equals(CollageScene.class)) {
			CollageScene scene = (CollageScene) sceneClass;
			imagePath = scene.getCollageElements().get(0).path;
		} else if (sceneClass.getClass().equals(ImageFileScene.class)) {
			ImageFileScene scene = (ImageFileScene) sceneClass;
			ImageResource imageResource = scene.getImageResource();
			if (imageResource instanceof FileImageResource) {
				imagePath = ((FileImageResource) imageResource).getFilePath();
			} else if (imageResource instanceof VideoFileImageResource) {
				imagePath = ((VideoFileImageResource) imageResource).getFilePath();
			}
		} else if (sceneClass.getClass().equals(MultiLayerScene.class)) {
			MultiLayerScene scene = (MultiLayerScene) sceneClass;
			if (scene.getLayer(0).getClass().equals(LayerScene.class)) {
				imagePath = ((LayerScene) scene.getLayer(0)).getImageFilePath();
			} else if (scene.getLayer(0).getClass().equals(VideoFileScene.class)) {
				imagePath = ((VideoFileScene) scene.getLayer(0)).getVideoFilePath();
			}
		} else if (sceneClass.getClass().equals(VideoFileScene.class)) {
			VideoFileScene scene = (VideoFileScene) sceneClass;
			imagePath = scene.getVideoFilePath();
		} else if (sceneClass.getClass().equals(BurstShotScene.class)) {
			BurstShotScene scene = (BurstShotScene) sceneClass;
			imagePath = scene.getImageFilePath(0);
		}
		return imagePath;
	}
	
	public static ImageData getImageData(Context context,Scene scene){
		ImageData imageData = null;
		if (scene.getClass().equals(ImageFileScene.class)) {

			ImageFileScene imageFileScene = (ImageFileScene) scene;
			FileImageResource imageFileResource = (FileImageResource) imageFileScene.getImageResource();
			int imageId = imageFileScene.getImageId();
			String filePath = imageFileResource.getFilePath();
			imageData = ImageUtil.getImageData(context, imageId, filePath);
		} else if (scene.getClass().equals(MultiLayerScene.class)) {
			MultiLayerScene multiLayerScene = (MultiLayerScene) scene;
			Scene firstScene = multiLayerScene.getLayer(0);
			if (firstScene.getClass().equals(LayerScene.class)) {
				LayerScene multiLayerImageScene = (LayerScene) firstScene;
				int imageId = multiLayerImageScene.getImageId();
				String filePath = multiLayerImageScene.getImageFilePath();
				imageData = ImageUtil.getImageData(context, imageId, filePath);

			} else if (firstScene.getClass().equals(VideoFileScene.class)) {
				VideoFileScene videoFileScene = (VideoFileScene) firstScene;
				int imageId = videoFileScene.getVideoId();
				imageData = ImageUtil.getVideoData(context, imageId, videoFileScene.getVideoFilePath());
			}
		} else if (scene.getClass().equals(BurstShotScene.class)) {
			BurstShotScene burstShotScene = (BurstShotScene) scene;
			int imageId = burstShotScene.getImageId(0);
			String filePath = burstShotScene.getImageFilePath(0);
			imageData = ImageUtil.getImageData(context, imageId, filePath);

		} else if (scene.getClass().equals(CollageScene.class)) {
			CollageScene collageScene = (CollageScene) scene;
			imageData = (ImageData) collageScene.getCollageElements().get(0);

		} else if (scene.getClass().equals(VideoFileScene.class)) {
			VideoFileScene videoFileScene = (VideoFileScene) scene;
			int imageId = videoFileScene.getVideoId();
			imageData = ImageUtil.getVideoData(context, imageId, videoFileScene.getVideoFilePath());
		}
		return imageData;
	}


	/**
	 * video file scene의 경우에 비디오의 시작 위치를 반환.
	 * 
	 * @param sceneClass
	 *            : Scene.
	 * @return : video file의 시작 위치.
	 */
	public static long getVideoStartPosition(Scene sceneClass) {
		long position = VIDEO_POSITION_0;
		if (sceneClass.getClass().equals(VideoFileScene.class)) {
			VideoFileScene scene = (VideoFileScene) sceneClass;
			position = scene.getVideoStartPosition();
		}else if(sceneClass.getClass().equals(MultiLayerScene.class)){
			if(((MultiLayerScene)sceneClass).getLayer(0).getClass().equals(VideoFileScene.class)){
				VideoFileScene scene = (VideoFileScene) ((MultiLayerScene)sceneClass).getLayer(0);
				position = scene.getVideoStartPosition();
			}
		}
		return position;
	}

	/**
	 * video file scene의 경우에 비디오의 끝 위치를 반환.
	 * 
	 * @param sceneClass
	 *            : Scene.
	 * @return : video file의 끝 위치.
	 */
	public static long getVideoEndPosition(Scene sceneClass) {
		long position = VIDEO_POSITION_0;
		if (sceneClass.getClass().equals(VideoFileScene.class)) {
			VideoFileScene scene = (VideoFileScene) sceneClass;
			position = scene.getVideoEndPosition();
		}else if(sceneClass.getClass().equals(MultiLayerScene.class)){
			if(((MultiLayerScene)sceneClass).getLayer(0).getClass().equals(VideoFileScene.class)){
				VideoFileScene scene = (VideoFileScene) ((MultiLayerScene)sceneClass).getLayer(0);
				position = scene.getVideoEndPosition();
			}
		}
		return position;
	}

	/**
	 * 전달 받은 scene의 file path와 현재 설정되어 있는 file path가 다를 경우,<br>
	 * 배경 이미지를 변경 한다.<br>
	 * 
	 * @param visualizer
	 *            : Visualizer
	 * @param extraScene
	 *            : intro scene
	 * @param firstContentScene
	 *            : first content scene
	 * @param path
	 *            : first content scene resource file path.
	 * @param videoPosition
	 *            : video scene의 시작 포인트.
	 * @return : 배경 화면 전환 유무.
	 */
	public static boolean chageImage(Visualizer visualizer, Scene extraScene, Scene firstContentScene, String path, long videoPosition, Theme theme) {
		if (extraScene.getClass().equals(ImageTextScene.class)) {
			ImageTextScene scene = (ImageTextScene) extraScene;
			String previous = scene.getBackgroundFilePath();
			long previousPosition = scene.getVideoFramePosition();
			if (!path.equals(previous) || videoPosition != previousPosition) {

				Visualizer.Editor vEditor = null;
				if (!visualizer.isOnEditMode()) {
					vEditor = visualizer.getEditor().start();
				}

				ImageTextScene.Editor editor = scene.getEditor();
				editor.setBackgroundFilePath(path).setVideoFramePosition(videoPosition);

				if (vEditor != null) {
					vEditor.finish();
				}
				return true;
			}
		} else if (extraScene.getClass().equals(DummyScene.class)) {
			DummyScene scene = (DummyScene) extraScene;
			String previous = scene.getBackgroundFilePath();
			long previousPosition = scene.getVideoFramePosition();
			if (!TextUtils.isEmpty(previous) && (!path.equals(previous) || videoPosition != previousPosition)) {

				Visualizer.Editor vEditor = null;
				if (!visualizer.isOnEditMode()) {
					vEditor = visualizer.getEditor().start();
				}

				DummyScene.Editor editor = scene.getEditor();
				editor.setBackgroundFilePath(path).setVideoFramePosition(videoPosition);

				if (vEditor != null) {
					vEditor.finish();
				}
				return true;
			}
		} else if (extraScene.getClass().equals(ImageFileScene.class)) {
			ImageFileScene scene = (ImageFileScene) extraScene;

			String introSceneResoucePath = null;
			int introSceneVideoPosition = VIDEO_POSITION_0;

			if (scene.getImageResource() instanceof VideoFileImageResource) {
				VideoFileImageResource videoFileResource = (VideoFileImageResource) scene.getImageResource();
				introSceneResoucePath = videoFileResource.getFilePath();
				introSceneVideoPosition = videoFileResource.getFramePosition();
			} else {
				FileImageResource imageFileResource = (FileImageResource) scene.getImageResource();
				introSceneResoucePath = imageFileResource.getFilePath();
			}

			boolean isUserOutro = false;
			if(UserTag.getTagSceneType(extraScene).equalsIgnoreCase(UplusOutroSceneCoordinator.TAG_JSON_VALE_SCENE_OUTRO)){
				if(theme.isDynamicOutroDefaultImage()){
					isUserOutro = false;
				}else{
					isUserOutro = true;
				}
			}else{
				isUserOutro = true;
			}
			
			if (!TextUtils.isEmpty(introSceneResoucePath) && (!introSceneResoucePath.equals(path) || introSceneVideoPosition != videoPosition) && isUserOutro) {
				Visualizer.Editor vEditor = null;
				if (!visualizer.isOnEditMode()) {
					vEditor = visualizer.getEditor().start();
				}

				ImageFileScene.Editor editor = scene.getEditor();
				if (firstContentScene.getClass().equals(VideoFileScene.class)) {
					editor.setImageResource(ImageResource.createFromVideoFile(path, ScaleType.BUFFER, (int) videoPosition));
				}else if(firstContentScene.getClass().equals(MultiLayerScene.class)){
					MultiLayerScene mLayerScene = (MultiLayerScene) firstContentScene;
					if (mLayerScene.getLayer(0).getClass().equals(LayerScene.class)) {
						path = ((LayerScene) mLayerScene.getLayer(0)).getImageFilePath();
						editor.setImageResource(ImageResource.createFromFile(path, ScaleType.BUFFER));
					} else if (mLayerScene.getLayer(0).getClass().equals(VideoFileScene.class)) {
						path = ((VideoFileScene) mLayerScene.getLayer(0)).getVideoFilePath();
						editor.setImageResource(ImageResource.createFromVideoFile(path, ScaleType.BUFFER, (int) videoPosition));
					}
				}else {
					editor.setImageResource(ImageResource.createFromFile(path, ScaleType.BUFFER));
				}

				if (vEditor != null) {
					vEditor.finish();
				}
				return true; 
			}

		}
		return false;
	}
	
	public static Viewport getCenterFullViewPort(String mediaPath, int srcWidth, int srcHeight){
		float left = 0.0f, top = 0.0f, right = 0.0f, bottom = 0.0f; 
		float templetMultiplyWidth;
		float realMultiplyWidth;
		float height, aspectHeight = KenBurnsScalerCoordinator.SAME_RATIO;
		float width, aspectWidth = KenBurnsScalerCoordinator.SAME_RATIO;
		float previewRatioWidth = KenBurnsScalerCoordinator.PREVIEW_RATIO_WIDTH; 
		float previewRatioHeight = KenBurnsScalerCoordinator.PREVIEW_RATIO_HEIGHT;
		int rotation = 0;
		float imageWidth, imageHeight;
		
		if(ImageUtil.isVideoFile(mediaPath)){
			imageWidth = srcWidth; 
			imageHeight = srcHeight; 
		}else{
	        try {
	             rotation = BitmapUtils.getImageRotation(mediaPath);
	        } catch(IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        rotation = Math.abs(rotation); 
	        if(rotation == 90 || rotation == 270){
				//세로 사진.
				imageWidth = srcHeight; 
				imageHeight = srcWidth; 
			}else{
				//가로 사진.
				imageWidth = srcWidth; 
				imageHeight = srcHeight;
			}
		}
		
		templetMultiplyWidth = previewRatioWidth * imageHeight; 
		realMultiplyWidth = imageWidth * previewRatioHeight;
		
		if (templetMultiplyWidth > realMultiplyWidth) {
			// 레이아웃 템플릿의 width값의 비율이 데이터의 widht값의 비율보다 클 경우 최대 높이를 구한다.
			height = previewRatioHeight * imageWidth / previewRatioWidth;
			aspectHeight = (float) (height / imageHeight);
			
			left = 0.0f; 
			top = (1.0f - aspectHeight)*0.5f; 
			right = 1.0f; 
			bottom = top + aspectHeight; 
		}else if (templetMultiplyWidth < realMultiplyWidth){
			// 레이아웃 템플릿의 width값의 비율이 데이터의 widht값의 비율보다 작을 경우 최대 넓이를 구한다.
			width = (previewRatioWidth * imageHeight) / previewRatioHeight;
			aspectWidth = (float) (width / imageWidth);
			left = (1.0f - aspectWidth) * 0.5f; 
			top = 0.0f; 
			right = left + aspectWidth; 
			bottom = 1.0f; 
		} else if (templetMultiplyWidth == realMultiplyWidth) {
			left = 0.0f; top = 0.0f; right = 1.0f; bottom = 1.0f;   
		}
		L.d("view port (" + left +", "+ top+")("+right +", "+bottom+")"); 
		return new Viewport(left, top, right, bottom); 
	}

	public static Viewport getZoomInViewPort(Viewport org){
		float width = org.width()*0.2f;
		float height = org.height()*0.2f;
		return new Viewport(org.left+width, org.top+height,org.right-width, org.bottom-height);
	}
	
	public static Viewport getZoomOutViewPort(Viewport org){
		return new Viewport(org.left - 0.2f, org.top - 0.2f, org.right + 0.2f, org.bottom + 0.2f );
	}
}
