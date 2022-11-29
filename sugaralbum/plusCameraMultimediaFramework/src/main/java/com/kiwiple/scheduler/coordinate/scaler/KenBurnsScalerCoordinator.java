package com.kiwiple.scheduler.coordinate.scaler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;

import com.kiwiple.imageanalysis.correct.collage.FaceInfomation;
import com.kiwiple.imageanalysis.database.FacePointF;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.database.ImageFaceData;
import com.kiwiple.imageframework.util.BitmapUtils;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.debug.L;
import com.kiwiple.scheduler.data.MultiLayerData;

public abstract class KenBurnsScalerCoordinator {
	
	public static final float SAME_RATIO = 1.0f;
	protected static final float ZOOM_IN_OUT_VALUE = 0.7f;
	protected static final float ZOOM_IN_OUT_MULTI_VALUE = 0.75f;
	protected static final float ASPECT_RATIO_MAX_VALUE = 0.9f;
	public static final float PREVIEW_RATIO_WIDTH = 16.0f;
	public static final float PREVIEW_RATIO_HEIGHT = 9.0f;
	protected static final int MAX_PERSON_NUM = 1;
	protected static final float INVALID_VALUE = -1.0f;
	
	protected static final float MOVE_TOP_DOWN = 0.1f;
	protected static final float MOVE_LEFT_RIGHT = 0.1f;
	protected static final float MAX_KENBURN_SIZE = 1.0f;
	protected static final float MIN_KENBURN_SIZE = 0.0f;
	
	public static final float SCALE_RATIO = 0.8f;
	public static final float SCALE_RATIO_SIZE_UP = 1.25f;
	public static final float CHANGE_DIRECTION = 0.85f; 
	public static final float ENABLE_SCALE = 0.9f; 
	
	
    public static enum KenburnDirection {
        LEFT, RIGHT, UP, DOWN, IN, OUT,RANDOM, NONE;
    }
        
    public static enum KenburnPosition{
    	LEFT, CENTER, RIGHT, LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM, SMALL_LEFT_TOP, LARGE_LEFT_BOTTOM, LARGE_RIGHT_TOP, SMALL_RIGHT_BOTTOM; 
    }
    
    public static enum KenburnShape{
    	HORIZONTAL, VERTICAL; 
    }
    
    
    public KenburnDirection measureKenburnDirection(Viewport from, Viewport to){
		
    	if(from.top == to.top && from.bottom == to.bottom && from.left == to.left && from.right == to.right){
    		return KenburnDirection.NONE; 
    	}else if (from.top == to.top && from.bottom == to.bottom) {
			return (from.left > to.left && from.right > to.right) ? KenburnDirection.LEFT : KenburnDirection.RIGHT;
		} else if (from.left == to.left && from.right == to.right) {
			return (from.top > to.top && from.bottom > to.bottom) ? KenburnDirection.UP : KenburnDirection.DOWN;
		} else {
			return from.contains(to) ? KenburnDirection.IN : to.contains(from) ? KenburnDirection.OUT : KenburnDirection.NONE;
		}
    }
    
    public boolean isSameViewPort(Viewport from, Viewport to){
    	if(from.top == to.top && from.left == to.left && from.right == to.right && from.bottom == to.bottom){
    		return true; 
    	}else{
    		return false; 
    	}
    }
    
	public Viewport getTemplateLayerViewport(int templateId, int index){
		
		float left = 0, top = 0, right = 0, bottom = 0; 
		
		if(templateId == MultiLayerData.MULTI_LAYER_COLUMN_TWO_PICTURES_ID){
			if(index == 0){
				left = 0.0f; top = 0.0f; right = 0.5f; bottom = 1.0f;
			}else if(index == 1){
				left = 0.5f; top = 0.0f; right = 1.0f; bottom = 1.0f;
			}
		}else if(templateId == MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID){
			if(index == 0){
				left = 0.0f; top = 0.0f; right = 0.33f; bottom = 1.0f;
			}else if(index == 1){
				left = 0.33f; top = 0.0f; right = 0.67f; bottom = 1.0f;
			}else if(index == 2){
				left = 0.67f; top = 0.0f; right = 1.0f; bottom = 1.0f;
			}
		}else if(templateId == MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID){
			if(index == 0){
				left = 0.0f; top = 0.0f; right = 0.5f; bottom = 1.0f;
			}else if(index == 1){
				left = 0.5f; top = 0.0f; right = 1.0f; bottom = 0.5f;
			}else if(index == 2){
				left = 0.5f; top = 0.5f; right = 1.0f; bottom = 1.0f;
			}
		}else if(templateId == MultiLayerData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID){
			if(index == 0){
				left = 0.0f; top = 0.0f; right = 0.5f; bottom = 0.5f;
			}else if(index == 1){
				left = 0.0f; top = 0.5f; right = 0.5f; bottom = 1.0f;
			}else if(index == 2){
				left = 0.5f; top = 0.0f; right = 1.0f; bottom = 0.5f;
			}else if(index == 3){
				left = 0.5f; top = 0.5f; right = 1.0f; bottom = 1.0f;
			}
		}else if(templateId == MultiLayerData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID){
			if(index == 0){
				left = 0.0f; top = 0.0f; right = 0.35f; bottom = 0.5f;
			}else if(index == 1){
				left = 0.0f; top = 0.5f; right = 0.65f; bottom = 1.0f;
			}else if(index == 2){
				left = 0.35f; top = 0.0f; right = 1.0f; bottom = 0.5f;
			}else if(index == 3){
				left = 0.65f; top = 0.5f; right = 1.0f; bottom = 1.0f;
			}
		}else{
			left = 0.0f; top = 0.0f; right = 1.0f; bottom = 1.0f;
		}
		return new Viewport(left, top, right, bottom); 
	}
	
	public Viewport[] applyKenBurnEffectFromStartViewPort(ImageData imageData, Viewport startViewPort, KenburnDirection direction){
		
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		Random random = new Random();
		viewportList.add(startViewPort);
		Viewport viewport = null;
		float startViewPortWidth = startViewPort.right - startViewPort.left; 
		float startViewPortHeight = startViewPort.bottom - startViewPort.top; 
		
		L.d("Start view port ("+startViewPort.left +", " + startViewPort.top +")("+startViewPort.right +", " + startViewPort.bottom+") direction : " + direction ); 
		
		if(startViewPort.left == 0 && startViewPort.top == 0 && startViewPort.right == 1.0f && startViewPort.bottom == 1.0f){
			direction = KenburnDirection.IN; 
		}else{
			if(direction == KenburnDirection.NONE){
				if(startViewPortHeight == 1.0f && startViewPortWidth == 1.0f){
					direction = KenburnDirection.IN; 
				}else if(startViewPortHeight == 1.0f && startViewPortWidth != 1.0f){
					if(random.nextBoolean()){
						direction = KenburnDirection.LEFT; 
					}else{
						direction = KenburnDirection.RIGHT; 
					}
				}else if(startViewPortHeight != 1.0f && startViewPortWidth == 1.0f){
					if(random.nextBoolean()){
						direction = KenburnDirection.UP; 
					}else{
						direction = KenburnDirection.DOWN; 
					}
				}else if(startViewPortHeight != 1.0f && startViewPortWidth != 1.0f){
					direction = KenburnDirection.OUT; 
				}
			}
		}
		
		if(direction == KenburnDirection.UP || direction == KenburnDirection.DOWN){
			if(startViewPortHeight == 1.0f){
				if(random.nextBoolean()){
					direction = KenburnDirection.LEFT; 
				}else{
					direction = KenburnDirection.RIGHT; 
				}
			}
		}
		
		if(direction == KenburnDirection.RIGHT || direction == KenburnDirection.LEFT){
			if(startViewPortWidth == 1.0f){
				if(random.nextBoolean()){
					direction = KenburnDirection.UP; 
				}else{
					direction = KenburnDirection.DOWN; 
				}
			}
		}
		
		if(direction == KenburnDirection.RIGHT || direction == KenburnDirection.LEFT){
			if(startViewPortWidth > KenBurnsScalerCoordinator.ASPECT_RATIO_MAX_VALUE){
				direction = KenburnDirection.IN;
			}
		}
		
		if(direction == KenburnDirection.UP || direction == KenburnDirection.DOWN){
			if(startViewPortHeight > KenBurnsScalerCoordinator.ASPECT_RATIO_MAX_VALUE){
				direction = KenburnDirection.IN;
			}
		}
		
		if(direction == KenburnDirection.UP){
			viewport = getTopViewPortByBottomViewPort(startViewPort); 
		}else if(direction == KenburnDirection.DOWN){
			viewport = getBottomViewPortByTopViewPort(startViewPort);  
		}else if(direction == KenburnDirection.LEFT){
			viewport = getLeftViewPortByRightViewPort(startViewPort); 
		}else if(direction == KenburnDirection.RIGHT){
			viewport = getRightViewPortByLeftViewPort(startViewPort); 
		}else if(direction == KenburnDirection.IN){
			viewport = getZoomInViewPortByZoomOutViewPort(startViewPort,imageData); 
		}else if(direction == KenburnDirection.OUT){
			viewport = getZoomOutViewPortByZoomInViewPort(startViewPort,imageData); 
		}
		
		L.d("End view port ("+viewport.left +", " + viewport.top +")("+viewport.right +", " + viewport.bottom+") direction : " + direction );
		
		viewportList.add(viewport); 
		
		Viewport[] viewportArray = new Viewport[viewportList.size()];
		for (int i = 0; i < viewportList.size(); i++) {
			viewportArray[i] = viewportList.get(i);
		}
		return viewportArray;
		
	}
	
	public Viewport makeViewportFromCollagePosition(ImageData imageData, float previewRatioWidth, float previewRatioHeight, Viewport layerViewPort){
		float scale = imageData.imageCorrectData.collageScale;
		FacePointF point = imageData.imageCorrectData.collageCoordinate;
		Viewport centerViewport = null;
		float collageMoveX, collageMoveY; 
		float realMoveX, realMoveY;
		float left, right, top, bottom; 
		
		float imageWidth, imageHeight; 

        int rotation = 0;
        try {
             rotation = BitmapUtils.getImageRotation(imageData.path);
        } catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(rotation == 90 || rotation == 270){
			//세로 사진.
			imageWidth = imageData.height; 
			imageHeight = imageData.width; 
		}else{
			//가로 사진.
			imageWidth = imageData.width; 
			imageHeight = imageData.height;
		}
		
		if(scale == 1.0f){
			centerViewport = getCenterFullSizeViewPort(imageData, previewRatioWidth, previewRatioHeight);
		}else{
			centerViewport = getCenterScaledViewPort(imageData, previewRatioWidth, previewRatioHeight);
		}
		collageMoveX = point.x; 
		collageMoveY = point.y; 
		L.d("center (" + centerViewport.left +", "+centerViewport.top +")("+ centerViewport.right+","+centerViewport.bottom+")");
		L.d("realMoveX : " + collageMoveX +" y : " + collageMoveY ); 
		
		float kenburnCenterWidth = centerViewport.right - centerViewport.left; 
		float kenburnCenterHeight = centerViewport.bottom - centerViewport.top;
		float kenburnCenterX = (kenburnCenterWidth)*0.5f + centerViewport.left;
		float kenburnCenterY = (kenburnCenterHeight)*0.5f + centerViewport.top;
		float collageWidht = imageData.imageCorrectData.collageWidth * (layerViewPort.right - layerViewPort.left); 
		float collageHeight = imageData.imageCorrectData.collageHeight * (layerViewPort.bottom - layerViewPort.top);
        float widthScale = collageWidht / imageWidth * scale;
        float heightScale = collageHeight / imageHeight * scale;
        float imageScale = widthScale > heightScale ? widthScale : heightScale;
		float kenburnMoveX = getKenburnX(collageMoveX, collageWidht , imageWidth * imageScale) * -1; 
		float kenburnMoveY = getKenburnY(collageMoveY, collageHeight, imageHeight * imageScale) * -1;
		L.d("move x : " + kenburnMoveX +", y : " + kenburnMoveY); 
		 
		
		left = (kenburnCenterX + kenburnMoveX) - (kenburnCenterWidth *0.5f);
		if(left < 0.0f){
			L.d("left is under zero : " + left); 
			left = 0.0f; 
		}
		top = (kenburnCenterY + kenburnMoveY) - (kenburnCenterHeight *0.5f);
		if(top < 0.0f){
			L.d("top is under zero : " + top); 
			top = 0.0f; 
		}
		right = left + kenburnCenterWidth;
		if(right > 1.0f){
			L.d("right is over 1 : " + right);
			right = 1.0f; 
			left = right - kenburnCenterWidth; 
		}
		bottom = top + kenburnCenterHeight;
		if(bottom > 1.0f){
			L.d("bottom is over 1 : " + bottom);
			bottom = 1.0f; 
			top = bottom - kenburnCenterHeight; 
		}
		
		L.d("(" + left +", "+top +")("+ right+","+bottom+")"); 
				
		return new Viewport(left, top, right, bottom); 
	}
	
    private Viewport getZoomInViewPortByZoomOutViewPort(Viewport startViewPort, ImageData imageData) {
    	//start view port는 최대 사이즈이다. 
		Viewport centerZoomInViewPort = getCenterScaledViewPort(imageData, 16.0f, 9.0f);
		float width = startViewPort.right - startViewPort.left;
		float height = startViewPort.bottom - startViewPort.top; 
		
		float centerX = startViewPort.left + (width*0.5f); 
		float centerY = startViewPort.top + (height*0.5f);
		
		float zoomInViewPortWidth = centerZoomInViewPort.right - centerZoomInViewPort.left; 
		float zoomInViewPortHeight = centerZoomInViewPort.bottom - centerZoomInViewPort.top; 
		float left = centerX - (zoomInViewPortWidth)*0.5f; 
		float top = centerY - (zoomInViewPortHeight)*0.5f;
		float right = left + zoomInViewPortWidth; 
		float bottom = top + zoomInViewPortHeight; 
		
		L.d("(" + left +", "+top +")("+ right+","+bottom+")"); 
		return new Viewport(left, top, right, bottom);
	}

	protected Viewport getCenterScaledViewPort(ImageData imageData, float previewRatioWidth, float previewRatioHeight) {
		float left = 0, top = 0, right = 0, bottom = 0;
		Viewport fullSizeViewport = getCenterFullSizeViewPort(imageData, previewRatioWidth, previewRatioHeight); 
		float scaleWidth = (float) ((fullSizeViewport.right - fullSizeViewport.left) * Math.sqrt(SCALE_RATIO));
		float scaleHeight = (float) ((fullSizeViewport.bottom - fullSizeViewport.top) * Math.sqrt(SCALE_RATIO));
		
		left = (1.0f - scaleWidth)*0.5f; 
		top = (1.0f - scaleHeight)*0.5f; 
		right = left + scaleWidth; 
		bottom = top + scaleHeight; 
		
		return new Viewport(left, top, right, bottom);
	}

	private Viewport getZoomOutViewPortByZoomInViewPort(Viewport startViewPort, ImageData imageData) {
		Viewport centerZoomOutViewPort = getCenterFullSizeViewPort(imageData, 16.0f, 9.0f);
		float startViewPortWidth = startViewPort.right - startViewPort.left;
		float startViewPortHeight = startViewPort.bottom - startViewPort.top;
		float startViewPortCenterX = startViewPort.left + (startViewPortWidth)*0.5f; 
		float startViewPortCenterY = startViewPort.top + (startViewPortHeight)*0.5f;
		float centerZoomOutViewPortWidth = centerZoomOutViewPort.right - centerZoomOutViewPort.left; 
		float centerZoomOutViewPortHeight = centerZoomOutViewPort.bottom - centerZoomOutViewPort.top;
		float left = startViewPortCenterX - (centerZoomOutViewPortWidth*0.5f); 
		if(left < 0.0f){
			left = 0.0f; 
		}
		float top = startViewPortCenterY - (centerZoomOutViewPortHeight*0.5f); 
		if(top < 0.0f){
			top = 0.0f; 
		}
		float right = left + centerZoomOutViewPortWidth;
		if(right > 1.0f){
			right = 1.0f; 
			left = right - centerZoomOutViewPortWidth; 
		}
		float bottom = top + centerZoomOutViewPortHeight;
		if(bottom > 1.0f){
			bottom = 1.0f; 
			top = bottom - centerZoomOutViewPortHeight; 
		}
		
		L.d("(" + left +", "+top +")("+ right+","+bottom+")");
		return new Viewport(left, top, right, bottom);
	}

	protected Viewport getCenterFullSizeViewPort(ImageData imageData, float previewRatioWidth, float previewRatioHeight) {
		float left = 0, top = 0, right = 0, bottom = 0; 
		int imageWidth, imageHeight; 
		float templetMultiplyWidth;
		float realMultiplyWidth;
		float height, aspectHeight = SAME_RATIO;
		float width, aspectWidth = SAME_RATIO;

        int rotation = 0;
        try {
             rotation = BitmapUtils.getImageRotation(imageData.path);
        } catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rotation = Math.abs(rotation); 
        if(rotation == 90 || rotation == 270){
			//세로 사진.
			imageWidth = imageData.height; 
			imageHeight = imageData.width; 
		}else{
			//가로 사진.
			imageWidth = imageData.width; 
			imageHeight = imageData.height;
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
			width = previewRatioWidth * imageHeight / previewRatioHeight;
			aspectWidth = (float) (width / imageWidth);
			left = (1.0f - aspectWidth) * 0.5f; 
			top = 0.0f; 
			right = left + aspectWidth; 
			bottom = 1.0f; 
		} else if (templetMultiplyWidth == realMultiplyWidth) {
			left = 0.0f; top = 0.0f; right = 1.0f; bottom = 1.0f;   
		}
		L.d("center full size view port (" + left +", " + top+")("+right+", "+bottom+")"); 
		return new Viewport(left, top, right, bottom);
	}

	public KenburnDirection getSingleLayerViewPortDirection(ImageData imageData, float previewRatioWidth, float previewRatioHeight, Viewport startViewPort) {
		int imageWidth, imageHeight; 
		float templetMultiplyWidth;
		float realMultiplyWidth;
		float height, aspectHeight = SAME_RATIO;
		float width, aspectWidth = SAME_RATIO;
		float viewportWidth = startViewPort.right - startViewPort.left; 
		float viewportHeight = startViewPort.bottom - startViewPort.top;
		Random random = new Random(); 
		
		Viewport centerViewPort = getCenterFullSizeViewPort(imageData, previewRatioWidth, previewRatioHeight);
		float centerViewPortWidth = centerViewPort.right - centerViewPort.left; 
		float centerViewPortHeight = centerViewPort.bottom - centerViewPort.top; 

        int rotation = 0;
        try {
             rotation = BitmapUtils.getImageRotation(imageData.path);
        } catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rotation = Math.abs(rotation); 
        if(rotation == 90 || rotation == 270){
			//세로 사진.
			imageWidth = imageData.height; 
			imageHeight = imageData.width; 
		}else{
			//가로 사진.
			imageWidth = imageData.width; 
			imageHeight = imageData.height;
		}
		
		templetMultiplyWidth = previewRatioWidth * imageHeight;
		realMultiplyWidth = imageWidth * previewRatioHeight;
		
		if (templetMultiplyWidth > realMultiplyWidth) {
			// 레이아웃 템플릿의 width값의 비율이 데이터의 widht값의 비율보다 클 경우 최대 높이를 구한다.
			height = previewRatioHeight * imageWidth / previewRatioWidth;
			aspectHeight = (float) (height / imageHeight);
			if(aspectHeight > ZOOM_IN_OUT_VALUE){
				if(viewportHeight == centerViewPortHeight || viewportWidth == centerViewPortWidth){
					return KenburnDirection.IN; 
				}else{
					return KenburnDirection.OUT; 
				}
			}else{
				if(random.nextBoolean()){
					return KenburnDirection.UP; 
				}else{
					return KenburnDirection.DOWN;
				}
			}
		}else if (templetMultiplyWidth < realMultiplyWidth){
			// 레이아웃 템플릿의 width값의 비율이 데이터의 widht값의 비율보다 작을 경우 최대 넓이를 구한다.
			width = previewRatioWidth * imageHeight / previewRatioHeight;
			aspectWidth = (float) (width / imageWidth);
			
			if(aspectHeight > ZOOM_IN_OUT_VALUE){
				if(viewportHeight == centerViewPortHeight || viewportWidth == centerViewPortWidth){
					return KenburnDirection.IN; 
				}else{
					return KenburnDirection.OUT; 
				}
			}else{
				if(random.nextBoolean()){
					return KenburnDirection.LEFT; 
				}else{
					return KenburnDirection.RIGHT;
				} 
			}
			
		} else if (templetMultiplyWidth == realMultiplyWidth) {
			if(viewportHeight == centerViewPortHeight || viewportWidth == centerViewPortWidth){
				return KenburnDirection.IN; 
			}else{
				return KenburnDirection.OUT; 
			}
		}
		return null;
	}

	/**
     * 이미지 데이터의 center view port를 구한다. 
     * @param imageData 이미지 데이터. 
     * @param viewport 이미지 데이터의 시작 뷰포트. 
     * @return center view port. 
     */
    public Viewport getCenterViewPort(Viewport viewPort){
    	
		float viewPortwidth = (viewPort.right - viewPort.left);
		float viewPortheight = (viewPort.bottom - viewPort.top);
		float centerLeft = 0.0f, centerTop = 0.0f, centerRight = 0.0f, centerBottom = 0.0f;
		
		if((viewPortwidth != 1.0f) && (viewPortheight != 1.0f)){
			
			centerLeft = (1.0f - viewPortwidth)* 0.5f;
			centerRight = centerLeft + viewPortwidth; 
			centerTop = (1.0f - viewPortheight)*0.5f; 
			centerBottom = centerTop + viewPortheight;
		}else{
			if(viewPortheight != 1.0f){
				//최대 높이를 구한 경우.
				centerLeft = 0.0f;
				centerRight = 1.0f; 
				centerTop = (1.0f - viewPortheight)*0.5f; 
				centerBottom = centerTop + viewPortheight; 
				
			}else{
				//최대 넓이를 구한 경우.
				centerLeft = (1.0f - viewPortwidth)* 0.5f;
				centerRight = centerLeft + viewPortwidth; 
				centerTop = 0.0f;
				centerBottom = 1.0f; 
			}
		}
		return new Viewport(centerLeft, centerTop, centerRight, centerBottom); 
    }
    
    /**
     * kenburn의 view port를 보고 스케일 값을 구한다. 
     * @param viewport
     * @return
     */
    public float getScaleRatio(Viewport viewPort){
    	
		float viewPortwidth = (viewPort.right - viewPort.left);
		float viewPortheight = (viewPort.bottom - viewPort.top);
		float scaleRatio = 0.0f; 
		
		if((viewPortwidth != 1.0f) && (viewPortheight != 1.0f)){
			scaleRatio = (float) Math.sqrt(SCALE_RATIO_SIZE_UP); 
		}else{
			scaleRatio = 1.0f; 
		}
		
		return scaleRatio; 
		 
    }
    
    /**
     * kenburn 의 x축 이동 좌표를 collage view의 x축 이동 좌표로 변환. 
     * @param moveX kenburn의 x축 이동 거리. 
     * @param scaledImageWidth 스케일된 이미지의 넓이.
     * @param collageWidth 타겟이 되는 콜라쥬의 넓이.
     * @return 콜라쥬의 x좌표 이동 값. 
     */
    public float getCoordinateX(float moveX, float scaledImageWidth, float collageWidth){
    	float realMoveX =  moveX * scaledImageWidth;
    	L.d("moveX : " + moveX + ", realMoveX : " + realMoveX  +", collageWidth : " + collageWidth + ", collage move : " + (( collageWidth)*(realMoveX / scaledImageWidth)) ); 
    	return ( collageWidth )* (realMoveX / scaledImageWidth);  
    }
    
    /**
     * kenburn 의 y축 이동 좌표를 collage view의 y축 이동 좌표로 변환. 
     * @param moveY kenburn의 y축 이동 거리. 
     * @param scaledImageHeight 스케일된 이미지의 넓이.
     * @param collageHeight 타겟이 되는 콜라쥬의 넓이.
     * @return 콜라쥬의 y좌표 이동 값. 
     */
    public float getCoordinateY(float moveY, float scaledImageHeight, float collageHeight){
    	float realMoveY =  moveY * scaledImageHeight;
    	L.d("moveY : " + moveY + ", realMoveY : " + realMoveY + ", collageHeight : " + collageHeight + ", collage move : " + (( collageHeight)*(realMoveY / scaledImageHeight)));
    	return (collageHeight) * (realMoveY / scaledImageHeight); 
    }
    
    public float getKenburnX(float collageMoveX, float collageWidth, float scaledWidth){
//		float realMoveX = (collageMoveX  * scaledWidth) / collageWidth;
//		float kenburnX = realMoveX / scaledWidth;  
//		L.d("real move x : " + realMoveX + "kenburn X : " + kenburnX); 
		return collageMoveX / scaledWidth; 
	}
	public float getKenburnY(float collageMoveY, float collageHeight, float scaledHeight){
//		float realMoveY = (collageMoveY  * scaledHeight) / collageHeight;
//		float kenburnY = realMoveY / scaledHeight;  
//		L.d("real move y : " + realMoveY + "kenburn Y : " + kenburnY); 
		return collageMoveY / scaledHeight;   
	}
    
    
    /**
     * 얼굴 좌표가 인식된 사진으로, 좌우로 움직이는 kenburn 설정. 
     * @param imageData : 얼굴 좌표가 인식된 image data. 
     * @param imageWidth : 사진 data의 width. 
     * @param imageHeight : 사진 data의 height.
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
     */
    protected LinkedList<Viewport> kenburnPortraitLeftRight(ImageData imageData, int imageWidth, int imageHeight, float previewRatioWidth, float previewRatioHeight, KenburnDirection direction) {
    	//최대 넓이를 구한 경우이고 스케일 할 필요가 없다. 
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		float left = 0.0f, top = 0.0f, right = 0.0f, bottom = 0.0f;
		float facePosition = 0.0f; 
		float centerHeight = 0.0f, centerWidth = 0.0f;
		
		Viewport centerFullViewPort = getCenterFullSizeViewPort(imageData, previewRatioWidth, previewRatioHeight);
		float faceRectWith = centerFullViewPort.right - centerFullViewPort.left;
		float faceRectHeight = centerFullViewPort.bottom - centerFullViewPort.top;
		
		float leftRightPadding = faceRectWith * 0.5f;
		float topBottomPadding = faceRectHeight * 0.5f;
		Random random = new Random();
		
		L.d("direction : " + direction);
		
		Viewport viewPort = null; 

		for (int i = 0; i < MAX_PERSON_NUM; i++) {
			Rect faceRect = new Rect();
			float faceScaleValue = imageData.faceBitmapScale; 
			RectF faceRectF = FaceInfomation.getFaceRect((int)(imageWidth / faceScaleValue), 
									   (int)(imageHeight/faceScaleValue), 
										imageData.faceDataItems.get(i).leftEyePoint, 
										imageData.faceDataItems.get(i).rightEyePoint, 
										imageData.faceDataItems.get(i).mouthPoint);
			L.d("Face rectF : (" + faceRectF.left +", " +faceRectF.top +")(" +faceRectF.right+", "+faceRectF.bottom+")" );
			float temp; 
			if(faceRectF.left > faceRectF.right){
				temp = faceRectF.right; 
				faceRectF.right = faceRectF.left; 
				faceRectF.left = temp; 
			}
			if(faceRectF.top > faceRectF.bottom){
				temp = faceRectF.bottom; 
				faceRectF.bottom = faceRectF.top; 
				faceRectF.top = temp; 
			}
			L.d("Face rectF : (" + faceRectF.left +", " +faceRectF.top +")(" +faceRectF.right+", "+faceRectF.bottom+")" );
			
			faceRect.left = (int)(faceRectF.left * faceScaleValue); 
			faceRect.right = (int)(faceRectF.right * faceScaleValue); 
			faceRect.bottom = (int)(faceRectF.bottom * faceScaleValue); 
			faceRect.top = (int)(faceRectF.top * faceScaleValue); 
			
			L.d("Face rect : (" + faceRect.left +", " +faceRect.top +")(" +faceRect.right+", "+faceRect.bottom+")" );

			left = (float) faceRect.left / imageWidth;
			top = (float) faceRect.top / imageHeight;
			right = (float) faceRect.right / imageWidth;
			bottom = (float) faceRect.bottom / imageHeight;
			centerHeight = (bottom - top) * 0.5f + top;
			centerWidth = (right - left) * 0.5f + left;

			left = centerWidth - leftRightPadding; 
			if(left < 0){
				left = 0; 
			}
			
			top = 0.0f; 
			right = left + faceRectWith;
			if(right > 1.0f){
				right = 1.0f; 
				left = right - faceRectWith; 
			}
			bottom = top + faceRectHeight;

			facePosition = (right - left) *0.5f + left; 

			L.d(i + " : 번째 face 영역 top : " + top + ", left : " + left + ", right : " + right + ", bottom : " + bottom + ", facePosition : " + facePosition);

			viewPort = makeViewport(left, top, right, bottom, faceRectWith, faceRectHeight);
			viewportList.add(viewPort);
		}

		if(direction != KenburnDirection.LEFT || direction != KenburnDirection.RIGHT ){
			if(random.nextBoolean()){
				direction = KenburnDirection.LEFT; 
			}else{
				direction = KenburnDirection.RIGHT;
			}
		}

		if (direction == KenburnDirection.LEFT) {
	
			if(facePosition < 0.5f){		
				//첫번째 얼굴 영역은 왼쪽에 있다. 왼쪽으로 가려면 0번에 오른쪽 영역을 잡아야 한다. 
				viewportList.add(0, getRightViewPortByLeftViewPort(viewPort));
			}else{
				//첫번째 얼굴 영역은 오른쪽에 있다. 왼쪽으로 가려면 1번에 왼쪽 영역을 잡아야 한다.
				viewportList.add(getLeftViewPortByRightViewPort(viewPort));
			}
		} else if (direction == KenburnDirection.RIGHT){
			if(facePosition < 0.5f){		
				//첫번째 얼굴 영역은 왼쪽에 있다. 오른쪽으로 가려면 1번에 왼쪽 영역을 잡아야 한다. 
				viewportList.add(getRightViewPortByLeftViewPort(viewPort));
			}else{
				//첫번째 얼굴 영역은 오른쪽에 있다. 왼쪽으로 가려면 0번에 왼쪽 영역을 잡아야 한다.
				viewportList.add(0, getLeftViewPortByRightViewPort(viewPort));
			}
		}

		return viewportList;
	}

    /**
     * 얼굴 좌표가 인식된 사진으로, 위아래로 움직이는 kenburn 설정. 
     * @param imageData : 얼굴 좌표가 인식된 image data. 
     * @param imageWidth : 사진 data의 width. 
     * @param imageHeight : 사진 data의 height.
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
     */
    protected LinkedList<Viewport> kenburnPortraitUpDown(ImageData imageData, int imageWidth, int imageHeight, float previewRatioWidth, float previewRatioHeight, KenburnDirection direction) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		float left = 0.0f, top = 0.0f, right = 0.0f, bottom = 0.0f;
		float facePosition = 0.0f; 
		float centerHeight = 0.0f, centerWidth = 0.0f;
		
		Viewport centerFullViewPort = getCenterFullSizeViewPort(imageData, previewRatioWidth, previewRatioHeight); 
		
		float faceRectWith = centerFullViewPort.right - centerFullViewPort.left;
		float faceRectHeight = centerFullViewPort.bottom - centerFullViewPort.top;
		
		float leftRightPadding = faceRectWith * 0.5f;
		float topBottomPadding = faceRectHeight * 0.5f;
		Random random = new Random();
		Viewport viewPort = null; 
		
		L.d("direction : " + direction); 

		for (int i = 0; i < MAX_PERSON_NUM; i++) {
			Rect faceRect = new Rect();
			float faceScaleValue = imageData.faceBitmapScale; 
			RectF faceRectF = FaceInfomation.getFaceRect((int)(imageWidth / faceScaleValue), 
									   (int)(imageHeight/faceScaleValue), 
										imageData.faceDataItems.get(i).leftEyePoint, 
										imageData.faceDataItems.get(i).rightEyePoint, 
										imageData.faceDataItems.get(i).mouthPoint);
			L.d("Face rectF : (" + faceRectF.left +", " +faceRectF.top +")(" +faceRectF.right+", "+faceRectF.bottom+")" );
			float temp; 
			if(faceRectF.left > faceRectF.right){
				temp = faceRectF.right; 
				faceRectF.right = faceRectF.left; 
				faceRectF.left = temp; 
			}
			if(faceRectF.top > faceRectF.bottom){
				temp = faceRectF.bottom; 
				faceRectF.bottom = faceRectF.top; 
				faceRectF.top = temp; 
			}
			L.d("Face rectF : (" + faceRectF.left +", " +faceRectF.top +")(" +faceRectF.right+", "+faceRectF.bottom+")" );
			
			faceRect.left = (int)(faceRectF.left * faceScaleValue); 
			faceRect.right = (int)(faceRectF.right * faceScaleValue); 
			faceRect.bottom = (int)(faceRectF.bottom * faceScaleValue); 
			faceRect.top = (int)(faceRectF.top * faceScaleValue); 
			
			L.d("Face rect : (" + faceRect.left +", " +faceRect.top +")(" +faceRect.right+", "+faceRect.bottom+")" );

			left = (float) faceRect.left / imageWidth;
			top = (float) faceRect.top / imageHeight;
			right = (float) faceRect.right / imageWidth;
			bottom = (float) faceRect.bottom / imageHeight;
			centerHeight = (bottom - top) * 0.5f + top;
			centerWidth = (right - left) * 0.5f + left;

			left = 0.0f; 
			top = centerHeight - topBottomPadding; 
			if(top < 0){
				top = 0; 
			}

			right = left + faceRectWith;
			bottom = top + faceRectHeight;
			if(bottom > 1.0f){
				bottom = 1.0f; 
				top = bottom - faceRectHeight; 
			}

			facePosition = (bottom - top) *0.5f + top; 
			L.d(i + " : 번째 face 영역 top : " + top + ", left : " + left + ", right : " + right + ", bottom : " + bottom +", facePosition : " + facePosition);
			viewPort = makeViewport(left, top, right, bottom, faceRectWith, faceRectHeight); 
			viewportList.add(viewPort);

		}
		
		if(direction != KenburnDirection.UP || direction != KenburnDirection.DOWN ){
			if(random.nextBoolean()){
				direction = KenburnDirection.UP; 
			}else{
				direction = KenburnDirection.DOWN;
			}
		}

		if (direction == KenburnDirection.UP) {
	
			if(facePosition < 0.5f){		
				//첫번째 얼굴 영역은 위쪽에 있다. 위로 갈려면 0번에 아래 영역을 잡아야 한다. 
				viewportList.add(0, getBottomViewPortByTopViewPort(viewPort));
			}else{
				//첫번째 얼굴 영역은 아래에 있다. 위로 갈려면 1번에 윗 영역을 잡아야 한다.  
				viewportList.add(getTopViewPortByBottomViewPort(viewPort));
			}
		} else if (direction == KenburnDirection.DOWN){
			if(facePosition < 0.5f){		
				//첫번째 얼굴 영역은 위쪽에 있다. 아래로 갈려면 1번에 아래 영역을 잡아야 한다. 
				viewportList.add(getBottomViewPortByTopViewPort(viewPort));
			}else{
				//첫번째 얼굴 영역은 아래에 있다. 위로 갈려면 0번에 윗 영역을 잡아야 한다.  
				viewportList.add(0, getTopViewPortByBottomViewPort(viewPort));
			}
		}
		return viewportList;
	}
    
    /**
     * 풍경사진으로, 위아래로 움직이는 kenburn 설정. 
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
     */
    protected LinkedList<Viewport> kenburnLandScapeUpDown(float aspectWidth, float aspectHeight, KenburnDirection direction) {
    	//최대 높이 구하기, 사진은 스케일 하지 않고 위아래로 움직인다.  
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		Random random = new Random();
		L.d("aspect with : " + aspectWidth +", height : " + aspectHeight);
		Viewport topViewPort = getTopViewPortByAspectHeight(aspectHeight);
		Viewport bottomViewPort = getBottomViewPortByTopViewPort(topViewPort); 
		
		if (direction == KenburnDirection.UP) {
			viewportList.add(topViewPort);
			viewportList.add(bottomViewPort);
		} else if(direction == KenburnDirection.DOWN){
			viewportList.add(bottomViewPort);
			viewportList.add(topViewPort);
		}else{
			if(random.nextBoolean()){
				viewportList.add(topViewPort);
				viewportList.add(bottomViewPort);
			}else{
				viewportList.add(bottomViewPort);
				viewportList.add(topViewPort); 
			}
		}
		return viewportList;
	}

	/**
     * 풍경 사진으로, zoom in/out 움직이는 kenburn 설정. 
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
     */
    protected LinkedList<Viewport> kenburnLandScapeZoomInOut(ImageData imageData, KenburnDirection direction) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		Random random = new Random();

		Viewport centerFullViewPort = getCenterFullSizeViewPort(imageData, PREVIEW_RATIO_WIDTH, PREVIEW_RATIO_HEIGHT);		
		viewportList.add(centerFullViewPort);
		Viewport centerScaleViewPort = getCenterScaledViewPort(imageData, PREVIEW_RATIO_WIDTH, PREVIEW_RATIO_HEIGHT); 

		L.d("direction : " + direction);
		L.d("zoom out (" + centerFullViewPort.left +", "+centerFullViewPort.top +")("+ centerFullViewPort.right+","+centerFullViewPort.bottom+")"); 
		L.d("zoom in (" + centerScaleViewPort.left +", "+centerScaleViewPort.top +")("+ centerScaleViewPort.right+","+centerScaleViewPort.bottom+")");

		if (direction == KenburnDirection.OUT) {
			viewportList.add(0, centerScaleViewPort);
		} else if(direction == KenburnDirection.IN) {
			viewportList.add(centerScaleViewPort);
		}else {
			if(random.nextBoolean())
			{
				viewportList.add(0, centerScaleViewPort);
			}else{
				viewportList.add(centerScaleViewPort);
			}
		}
		return viewportList;
	}
    
    
    

	/**
     * 사진의 비율과 preview layout의 비율의 동일 한 경우. <br>
     * zoom in/out으로 kenburn 설정. 
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
     */
    //done
    protected LinkedList<Viewport> kenburnLandScapeSameRatioUpDown(float aspectWidth, float aspectHeight, KenburnDirection direction) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		float left, top, right, bottom;
		Random random = new Random();
		
		float scaleValue = (float) Math.sqrt(SCALE_RATIO);
		 
		L.d("aspect with : " + aspectWidth +", height : " + aspectHeight +", scale value : " + scaleValue);
		left = (1.0f - scaleValue) * 0.5f; top = 0.0f; right = left + scaleValue; bottom = top + scaleValue; 
		L.d("kenburn up view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		Viewport topViewPort = makeViewport(left, top, right, bottom, scaleValue, scaleValue);

		left = (1.0f - scaleValue) * 0.5f; top = 1.0f - scaleValue; right = left + scaleValue; bottom = 1.0f;
		L.d("kenburn down view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		Viewport bottomViewPort = makeViewport(left, top, right, bottom, scaleValue, scaleValue); 

		if (direction == KenburnDirection.UP) {
			viewportList.add(topViewPort);
			viewportList.add(bottomViewPort);
		} else if(direction == KenburnDirection.DOWN){
			viewportList.add(bottomViewPort);
			viewportList.add(topViewPort);
		}else{
			if(random.nextBoolean()){
				viewportList.add(bottomViewPort);
				viewportList.add(topViewPort);	
			}else{
				viewportList.add(topViewPort);
				viewportList.add(bottomViewPort);
			}
		}
		return viewportList;
	}

	/**
     * 최대 높이를 우선으로 구한 경우는 좌우로 움직일수 없지만, <br>
     * 최대 높이가 ZOOM_IN_OUT_MULTI_VALUE 보다 크다면 좌우로 움직이게 view port를 구성한다. <br>
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
     */
    protected LinkedList<Viewport> kenburnLandScapeCropLeftRight(float aspectWidth, float aspectHeight, KenburnDirection direction) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		float left, top, right, bottom;
		Random random = new Random();
		
		float scaledValue = (float) Math.sqrt(SCALE_RATIO); 
		L.d("before aspect width : " + aspectWidth +", height : " + aspectHeight+", scaled : " + scaledValue);
		aspectWidth = aspectWidth * scaledValue; 
		aspectHeight = aspectHeight * scaledValue; 
		L.d("aspect width : " + aspectWidth +", height : " + aspectHeight+", scaled : " + scaledValue);
		
		left = 0.0f; 
		top = (1.0f - aspectHeight) * 0.3f;
		right = left + aspectWidth;
		bottom = top + aspectHeight;
		L.d("kenburn left view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		Viewport leftViewPort = makeViewport(left, top, right, bottom, aspectWidth, aspectHeight); 

		left = 1.0f - aspectWidth; 
		top = (1.0f - aspectHeight) * 0.3f;
		right = left + aspectWidth;
		bottom = top + aspectHeight;
		L.d("kenburn right view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		Viewport rightViewPort = makeViewport(left, top, right, bottom, aspectWidth, aspectHeight);

		if (direction == KenburnDirection.LEFT) {
			viewportList.add(leftViewPort);
			viewportList.add(rightViewPort);
		} else if(direction == KenburnDirection.RIGHT) {
			viewportList.add(rightViewPort);
			viewportList.add(leftViewPort);
		}else{
			if(random.nextBoolean()){
				viewportList.add(rightViewPort);
				viewportList.add(leftViewPort);	
			}else{
				viewportList.add(leftViewPort);
				viewportList.add(rightViewPort);
			}
		}
		return viewportList;
	}
 	
    /**
     * 최대 넓이를 우선으로 구한 경우는 좌우로 움직일수 없지만, <br>
     * 최대 넓이가 ZOOM_IN_OUT_MULTI_VALUE 보다 크다면 위아래로 움직이게 view port를 구성한다. <br>
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
     */
    protected LinkedList<Viewport> kenburnLandScapeCropUpDown(float aspectWidth, float aspectHeight, KenburnDirection direction) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		float left, top, right, bottom;
		Random random = new Random();
		
		float scaledValue = (float) Math.sqrt(SCALE_RATIO); 
		aspectWidth = aspectWidth * scaledValue; 
		aspectHeight = aspectHeight * scaledValue; 
				
		L.d("aspect width : " + aspectWidth +", height : " + aspectHeight+", scaled : " + scaledValue); 

		left = (1.0f - aspectWidth) * 0.5f; 
		top = 0.0f;
		right = left + aspectWidth;
		bottom = top + aspectHeight;
		L.d("kenburn top view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		Viewport topViewPort = makeViewport(left, top, right, bottom, aspectWidth, aspectHeight); 

		left = (1.0f - aspectWidth) * 0.5f; 
		top = 1.0f - aspectHeight;
		right = left + aspectWidth;
		bottom = top + aspectHeight;		
		L.d("kenburn bottom view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		Viewport bottomViewPort = makeViewport(left, top, right, bottom, aspectWidth, aspectHeight);

		if (direction == KenburnDirection.UP) {
			viewportList.add(topViewPort); 
			viewportList.add(bottomViewPort); 
		} else if(direction == KenburnDirection.DOWN) {
			viewportList.add(bottomViewPort);
			viewportList.add(topViewPort);
		}else{
			if(random.nextBoolean()){
				viewportList.add(bottomViewPort);
				viewportList.add(topViewPort);	
			}else{
				viewportList.add(topViewPort); 
				viewportList.add(bottomViewPort);
			}
		}
		return viewportList;
	}
    
    /**
     * 풍경 사진으로, 옆으로 움직이는 kenburn 설정. 
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
     */
    protected LinkedList<Viewport> kenburnLandScapeLeftRight(float aspectWidth, float aspectHeight, KenburnDirection direction) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		Random random = new Random();		
		L.d("aspect width : " + aspectWidth +", height : " + aspectHeight);
		Viewport leftViewPort = getLeftViewPortByAspectWidth(aspectWidth);
		Viewport rightViewPort = getRightViewPortByLeftViewPort(leftViewPort); 

		if (direction == KenburnDirection.RIGHT) {
			viewportList.add(rightViewPort);
			viewportList.add(leftViewPort);
		} else if(direction == KenburnDirection.LEFT) {
			viewportList.add(rightViewPort);
			viewportList.add(leftViewPort);
		}else{
			if(random.nextBoolean()){
				viewportList.add(rightViewPort);
				viewportList.add(leftViewPort);
			}else{
				viewportList.add(rightViewPort);
				viewportList.add(leftViewPort); 
			}
		}
		return viewportList;
	}
	
    /**
     * 풍경 사진으로 사진의 비율과 preview의 비율이 동일한 경우, 옆으로 움직이는 kenburn 설정. 
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
     */
	public LinkedList<Viewport> kenburnLandScapeSameRatioLeftRight(float aspectWidth, float aspectHeight, KenburnDirection direction) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		float left = 0, top = 0, right = 0, bottom = 0;
		Random random = new Random();
		
		float scaledValue = (float) Math.sqrt(SCALE_RATIO); 
		aspectHeight *= scaledValue; 
		aspectWidth *= scaledValue;
		
		L.d("aspect width : " + aspectWidth +", height : " + aspectHeight + " scale value : " + scaledValue); 
		left = 0.00f; top = (1.0f - aspectHeight)*0.5f; right = left + aspectWidth; bottom = top+ aspectHeight;
		L.d("kenburn left view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		Viewport leftViewPort = makeViewport(left, top, right, bottom, aspectWidth, aspectHeight); 

		left = 1.0f - aspectWidth; top = (1.0f - aspectHeight)*0.5f; right = 1.0f; bottom = top+ aspectHeight;
		L.d("kenburn right view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		Viewport rightViewPort = makeViewport(left, top, right, bottom, aspectWidth, aspectHeight);
		
		if (direction == KenburnDirection.LEFT) {
			viewportList.add(leftViewPort);
			viewportList.add(rightViewPort);
		} else if(direction == KenburnDirection.RIGHT) {
			viewportList.add(rightViewPort);
			viewportList.add(leftViewPort);
		}else{
			if(random.nextBoolean()){
				viewportList.add(leftViewPort);
				viewportList.add(rightViewPort);	
			}else{
				viewportList.add(rightViewPort);
				viewportList.add(leftViewPort);
			}
		}
		return viewportList;
	}
	
	/**
	 * 최대 높이를 구한 경우, 최대 높이에 따라서 사진의 하단 view port를 설정한다. 
	 * @param aspectHeight : 최대 높이 비율. 
	 * @return : 사진 하단의 view port. 
	 */
    private Viewport getBottomViewPortByAspectHeight(float aspectHeight){
    	
    	float left, top, right, bottom;
    	left = 0.0f;
		if (aspectHeight > 0.9f) {
			top = 1.0f - aspectHeight - 0.00f;
		}else if (aspectHeight > 0.8f) {
			top = 1.0f - aspectHeight - 0.00f;
		}else if (aspectHeight > 0.7f) {
			top = 1.0f - aspectHeight - 0.05f;
		}else if (aspectHeight > 0.6f) {
			top = 1.0f - aspectHeight - 0.1f;
		} else if (aspectHeight > 0.5f) {
			top = 1.0f - aspectHeight - 0.15f;
		} else if (aspectHeight > 0.4f) {
			top = 1.0f - aspectHeight - 0.2f;
		} else if (aspectHeight > 0.3f) {
			top = 1.0f - aspectHeight - 0.25f;
		} else {
			top = 1.0f - aspectHeight - 0.27f;
		}
		right = 1.0f;
		bottom = top + aspectHeight;
		L.d("kenburn  view point(" + left + ", " + top + ", " + right + ", " + bottom + "), aspectHeight : " + aspectHeight);
		return makeViewport(left, top, right, bottom, 1.0f, aspectHeight); 
    }
    
    /**
	 * 최대 높이를 구한 경우, 최대 높이에 따라서 사진의 상단 view port를 설정한다. 
	 * @param aspectHeight : 최대 높이 비율. 
	 * @return : 사진 상단의 view port. 
	 */
    private Viewport getTopViewPortByAspectHeight(float aspectHeight){
    	
    	float left, top, right, bottom;
		if (aspectHeight > 0.9f) {
			top = 0.00f;
		}else if (aspectHeight > 0.8f) {
			top = 0.00f;
		}else if (aspectHeight > 0.7f) {
			top = 0.05f;
		}else if (aspectHeight > 0.6f) {
			top = 0.1f;
		} else if (aspectHeight > 0.5f) {
			top = 0.15f;
		} else if (aspectHeight > 0.4f) {
			top = 0.2f;
		} else if (aspectHeight > 0.3f) {
			top = 0.25f;
		} else {
			top = 0.27f;
		}

		left = 0.0f;
		right = 1.0f;
		bottom = top + aspectHeight;
		L.d("kenburn  view point(" + left + ", " + top + ", " + right + ", " + bottom + ") aspectHeight : " + aspectHeight);
		return makeViewport(left, top, right, bottom, 1.0f, aspectHeight); 
    }
    
    /**
	 * 최대 높이를 구한 경우, top view port에서 아래로 0.1씩만 내려간 bottom view port를 구한다.  
	 * @param topViewPort : top kenburn 좌표. 
	 * @return : 사진 상단의 view port. 
	 */
    private Viewport getBottomViewPortByTopViewPort(Viewport topViewPort){
    	
    	float left, top, right, bottom;
    	float move = 0;
    	float height = topViewPort.bottom - topViewPort.top;
    	float width = topViewPort.right - topViewPort.left; 
    	
    	if(topViewPort.bottom == 1.0f){
    		return getTopViewPortByBottomViewPort(topViewPort); 
    	}else{
    		
	    	if(topViewPort.bottom + MOVE_TOP_DOWN > 1.0f){
	    		move = MOVE_TOP_DOWN * -1; 
	    	}else{
	    		move = MOVE_TOP_DOWN; 
	    	}
		
			left = topViewPort.left;
			top = topViewPort.top + move; 
			right = topViewPort.right;
			bottom = topViewPort.bottom + move;
    	}
		L.d("kenburn  view point(" + left + ", " + top + ", " + right + ", " + bottom + ") move : " + move);
		return makeViewport(left, top, right, bottom, width, height); 
    }
    
    /**
	 * 최대 높이를 구한 경우, top view port에서 아래로 0.1씩만 내려간 bottom view port를 구한다.  
	 * @param bottomViewPort : bottom kenburn 좌표. 
	 * @return : 사진 하단의 view port. 
	 */
    private Viewport getTopViewPortByBottomViewPort(Viewport bottomViewPort){
    	
    	float left, top, right, bottom;
    	float move = 0;
    	float height = bottomViewPort.bottom - bottomViewPort.top;
    	float width = bottomViewPort.left - bottomViewPort.right; 
    	
    	L.d("kenburn bottom view point(" + bottomViewPort.left + ", " + bottomViewPort.top + ", " + bottomViewPort.right + ", " + bottomViewPort.bottom + ")");
    	
    	if(bottomViewPort.top == 0.0f){
    		return getBottomViewPortByTopViewPort(bottomViewPort); 
    	}else{
    		
	    	if(bottomViewPort.top - MOVE_TOP_DOWN < 0.0f){
	    		move = MOVE_TOP_DOWN * -1;  
	    	}else{
	    		move = MOVE_TOP_DOWN; 
	    	}
		    	
			left = bottomViewPort.left;
			top = bottomViewPort.top - move; 
			right = bottomViewPort.right;
			bottom = bottomViewPort.bottom - move;
			
			if(top < 0.0f){
    			top = 0.0f; 
    			bottom = top + height;  
    		}
    		if(bottom > 1.0f){
    			bottom = 1.0f; 
    			top = bottom - height;  
    		}
    	}
		L.d("kenburn top view point(" + left + ", " + top + ", " + right + ", " + bottom + ") move : " + move);
		return makeViewport(left, top, right, bottom, width, height); 
    }
    
	
    /**
	 * 최대 넓이를 구한 경우, 최대 넓이에 따라서 사진의 왼쪽 view port를 설정한다. 
	 * @param aspectWidth : 최대 넓이 비율. 
	 * @return : 사진 왼쪽의 view port. 
	 */
   private Viewport getLeftViewPortByAspectWidth(float aspectWidth){
    	
    	float left, top, right, bottom;
    	if(aspectWidth > 0.9f){
    		left = 0.0f;
    	}else if(aspectWidth > 0.8f){
    		left = 0.1f;
    	}else if(aspectWidth > 0.7f){
    		left = 0.125f;
    	}else if (aspectWidth > 0.6f) {
			left = 0.175f;
		} else if (aspectWidth > 0.5f) {
			left = 0.225f;
		} else if (aspectWidth > 0.4f) {
			left = 0.275f;
		} else if (aspectWidth > 0.3f) {
			left = 0.325f;
		} else {
			left = 0.375f;
		}

		top = 0.0f;
		right = left + aspectWidth;
		bottom = 1.0f;
		L.d("kenburn  view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		return makeViewport(left, top, right, bottom, aspectWidth, 1.0f); 
    }
   /**
	 * 최대 넓이를 구한 경우, right view port에 따라서 left view port를 구한다.  
	 * @param rightViewPort : 오른쪽 kenburn 좌표. 
	 * @return : 사진 왼쪽의 view port. 
	 */
 private Viewport getLeftViewPortByRightViewPort(Viewport rightViewPort){
  	
		float left, top, right, bottom;
		float move = 0;
    	float width = rightViewPort.right - rightViewPort.left;
    	float height = rightViewPort.bottom - rightViewPort.top; 
    	
    	//rightViewport가 left에 붙어 있다면 leftViewport를 구한다.
    	if(rightViewPort.left == 0.0f){
    		return getRightViewPortByLeftViewPort(rightViewPort); 
    	}else{
    		
	    	if(rightViewPort.left - MOVE_LEFT_RIGHT < 0.0f){
	    		// 왼쪽에 좌표가 0.1보다 작다. 오른쪽으로 간다. 
	    		move = MOVE_LEFT_RIGHT * -1;   
	    	}else{
	    		move = MOVE_LEFT_RIGHT; 
	    	}
    		
    		left = rightViewPort.left - move; 
    		top = rightViewPort.top;
    		right = rightViewPort.right - move;
    		bottom = rightViewPort.bottom;
    		
    		if(left < 0.0f){
    			left = 0.0f; 
    			right = left + width; 
    		}
    		if(right > 1.0f){
    			right = 1.0f; 
    			left = right - width; 
    		}
    		
    	}
    	
    	L.d("kenburn  view point(" + left + ", " + top + ", " + right + ", " + bottom + ") move : " + move);
		
		return makeViewport(left, top, right, bottom, width, height); 
  }
  
   /**
	 * 최대 넓이를 구한 경우, left view port에 따라서 right view port를 구한다.  
	 * @param aspectWidth : 최대 넓이 비율. 
	 * @return : 사진 왼쪽의 view port. 
	 */
  private Viewport getRightViewPortByLeftViewPort(Viewport leftViewPort){
   	
		float left, top, right, bottom;
		float move = 0;
		float width = leftViewPort.right - leftViewPort.left;
		float height = leftViewPort.bottom - leftViewPort.top; 
		
		if(leftViewPort.right == 1.0f){
			return getLeftViewPortByRightViewPort(leftViewPort); 
		}else{
			
	    	if(leftViewPort.right + MOVE_LEFT_RIGHT > 1.0f){
	    		move = MOVE_LEFT_RIGHT * -1;   
	    	}else{
	    		move = MOVE_LEFT_RIGHT; 
	    	}
	    	
			left = leftViewPort.left + move; 
			top = leftViewPort.top;
			right = leftViewPort.right + move; 
			bottom = leftViewPort.bottom;
			
			if(left < 0.0f){
    			left = 0.0f; 
    			right = left + width; 
    		}
    		if(right > 1.0f){
    			right = 1.0f; 
    			left = right - width; 
    		}
		}
		L.d("kenburn  view point(" + left + ", " + top + ", " + right + ", " + bottom + ") move : " + move);
		return makeViewport(left, top, right, bottom, width, height); 
   }
   
   /**
	 * 최대 넓이를 구한 경우, 최대 넓이에 따라서 사진의 오른쪽 view port를 설정한다. 
	 * @param aspectWidth : 최대 넓이 비율. 
	 * @return : 사진 오른쪽의 view port. 
	 */
   private Viewport getRightViewPortByAspectWidth(float aspectWidth){
   	
		float left, top, right, bottom;
		left = 0.0f;
		if (aspectWidth > 0.6f) {
			left = 1.0f - aspectWidth - 0.1f;
		} else if (aspectWidth > 0.5f) {
			left = 1.0f - aspectWidth - 0.15f;
		} else if (aspectWidth > 0.4f) {
			left = 1.0f - aspectWidth - 0.2f;
		} else if (aspectWidth > 0.3f) {
			left = 1.0f - aspectWidth - 0.25f;
		} else {
			left = 1.0f - aspectWidth - 0.3f;
		}
		top = 0.0f;
		right = left + aspectWidth;
		bottom = 1.0f;
		L.d("kenburn  view point(" + left + ", " + top + ", " + right + ", " + bottom + ")");
		return makeViewport(left, top, right, bottom, aspectWidth, 1.0f); 
   }
	
   /**
    * left, top, right, bottom 좌표로 viewport 생성. 
    * @param left : left 좌표. 
    * @param top : top 좌표. 
    * @param right : right 좌표. 
    * @param bottom : bottom 좌표. 
    * @return : viewport. 
    */
	protected Viewport makeViewport(float left, float top, float right, float bottom, float width, float height) {
		Viewport viewport = checkNanValue(left, top, right, bottom, width, height);
		return viewport;
	}

	/**
	 * view port의 유효성 검사. 
    * @param left : left 좌표. 
    * @param top : top 좌표. 
    * @param right : right 좌표. 
    * @param bottom : bottom 좌표. 
    * @return : viewport. 
	 */
	protected Viewport checkNanValue(float left, float top, float right, float bottom, float width, float height) {
		
		if (Float.isNaN(left)) {
			L.d("Kenburn view point left NaN");
			left = 0.0f;
		}

		if (Float.isNaN(top)) {
			L.d("Kenburn view point top NaN");
			top = 0.0f;
		}

		if (Float.isNaN(right)) {
			L.d("Kenburn view point right NaN");
			right = 1.0f;
		}

		if (Float.isNaN(bottom)) {
			L.d("Kenburn view point bottom NaN");
			bottom = 1.0f;
		}
		
		if(left < 0.0f){
			left = 0.0f;
			right = left + width; 
		}
		
		if(top < 0.0f){
			top = 0.0f; 
			bottom = top + height; 
		}
		
		if(right > 1.0f){
			right = 1.0f; 
			left = right - width; 
		}
		
		if(bottom > 1.0f){
			bottom = 1.0f; 
			top = bottom - height; 
		}
		
		if(left < 0.0f || top < 0.0f || right >1.0f || bottom > 1.0f){
			L.e("Kenburn position error left : " + left +", top : " + top +", right : " + right +", bottom : " + bottom); 
		}
		
		return new Viewport(left, top, right, bottom);
	}
	
	/**
	 * kenburn port가 최대값을 넘지 못하게 체크 하는 함수. 
	 * @param num 최대값을 체크 해야 하는 함수. 
	 * @return 최대값을 넘지 않는 수. 
	 */
	protected float checkMaxSize(float num){
		if(num > MAX_KENBURN_SIZE){
			return MAX_KENBURN_SIZE; 
		}else{
			return num; 
		}
	}
	
	/**
	 * kenburn port가 최소값을 넘지 못하게 체크 하는 함수. 
	 * @param num 최소값을 체크 해야 하는 함수. 
	 * @return 최소값을 넘지 않는 수. 
	 */
	protected float checkMinSize(float num){
		if(num <= MIN_KENBURN_SIZE){
			return MIN_KENBURN_SIZE; 
		}else{
			return num; 
		}
	}
	
	protected Rect getFaceRect(ImageData imageData, ImageFaceData faceData){
		
		float faceScaleValue = imageData.faceBitmapScale; 
		RectF faceRectF = FaceInfomation.getFaceRect((int)(imageData.width / faceScaleValue), 
				   (int)(imageData.height/faceScaleValue), 
					faceData.leftEyePoint, 
					faceData.rightEyePoint, 
					faceData.mouthPoint);
		float temp; 
		if(faceRectF.left > faceRectF.right){
		temp = faceRectF.right; 
		faceRectF.right = faceRectF.left; 
		faceRectF.left = temp; 
		}
		if(faceRectF.top > faceRectF.bottom){
		temp = faceRectF.bottom; 
		faceRectF.bottom = faceRectF.top; 
		faceRectF.top = temp; 
		}
		
		Rect faceRect = new Rect();
		faceRect.left = (int)(faceRectF.left * faceScaleValue); 
		faceRect.right = (int)(faceRectF.right * faceScaleValue); 
		faceRect.bottom = (int)(faceRectF.bottom * faceScaleValue); 
		faceRect.top = (int)(faceRectF.top * faceScaleValue);
		
		return faceRect; 
	}
}
