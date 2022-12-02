package com.kiwiple.scheduler.coordinate.scaler.uplus;

import java.util.LinkedList;
import java.util.Random;

import android.graphics.Rect;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.database.ImageFaceData;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.scheduler.coordinate.scaler.KenBurnsScalerCoordinator;

public class UplusKenBurnsScalerCoordinator extends KenBurnsScalerCoordinator {
	
	private static boolean isZoomIn = true; 
	

	@Override
	public Viewport[] applyKenBurnEffectFromStartViewPort(ImageData imageData, Viewport startViewPort, KenburnDirection direction) {
		// TODO Auto-generated method stub
		return super.applyKenBurnEffectFromStartViewPort(imageData, startViewPort, direction);
	}

	/**
	 * 싱글씬의 사진 데이터, preview의 비율에 따라서 kenburn의 시작과 끝 좌표를 설정한다. 
	 * @param imageData : 사진 데이터. 
	 * @param previewRatioWidth : preview의 가로 비율. 
	 * @param previewRatioHeight : preview의 세로 비율. 
	 * @return : view port.
	 */
	public Viewport[] applyKenBurnEffectPicture(ImageData imageData, float previewRatioWidth, float previewRatioHeight,  boolean isAccelerationZoom, KenburnDirection kenburnDirection) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		int orientation = Integer.parseInt(imageData.orientation);

		L.d("Kenburn image name : " + imageData.fileName);
		L.d("Kenburn image width, height : " + imageData.width + ", " + imageData.height);
		L.d("Kenburn image orientation : " + orientation + "");
		float height, aspectHeight = SAME_RATIO;
		float width, aspectWidth = SAME_RATIO;

		float templetMultiplyWidth;
		float realMultiplyWidth;

		int imageWidth, imageHeight;

		// 이미지의 가로, 세로 여부를 판단한다.
		if ((orientation != 90) && (orientation != 270)) {
			// 명확하지 않는 orientation 정보로 인해서, 이미지 데이터의 width/height값을 체그해 가로 세로를
			// 결정 한다.
			if (imageData.width > imageData.height) {
				// 가로 사진
				imageWidth = imageData.width;
				imageHeight = imageData.height;
				L.d("orientation 정보가 정확한 가로 사진의 파일 이름 : " + imageData.fileName + "widht : " + imageWidth + ", height : " + imageHeight);
			} else {
				// 세로 사진
				// LG, Samsung폰의 스크린 샷의 경우 가로 모드와 세로 모드가 적용되서 width, height값이
				// 적용된다.
				imageWidth = imageData.width;
				imageHeight = imageData.height;
				L.d("orientation 정보가 정확하지 않는 세로 사진의 파일 이름 : " + imageData.fileName + "widht : " + imageWidth + ", height : " + imageHeight);
			}
		} else {
			// 세로 사진.
			// orientation정보가 90, 270일 경우 일반적으로 widht, height값은 변경되지 않는다.
			imageWidth = imageData.height;
			imageHeight = imageData.width;
			L.d("orientation 정보가 정확한 세로 사진의 파일 이름 : " + imageData.fileName + "widht : " + imageWidth + ", height : " + imageHeight);
		}

		templetMultiplyWidth = previewRatioWidth * imageHeight;
		realMultiplyWidth = imageWidth * previewRatioHeight;
		
		
		KenburnDirection zoomInOutdirection;
		if(isZoomIn){
			zoomInOutdirection = KenburnDirection.IN; 
		}else{
			zoomInOutdirection = KenburnDirection.OUT;
		}

		if (templetMultiplyWidth > realMultiplyWidth) {
			// 레이아웃 템블릿의 width값의 비율이 데이터의 widht값의 비율보다 클 경우 최대 높이를 구한다.
			// height = (imageWidth * ratioHight)/ratioWidth;
			// 최대 높이 값을 구하므로 사진은 위아래로 움직이거나
			// 사진이 레이아웃에 70프로 이상 노출 되면 zoom in/out으로 움직인다.
			height = previewRatioHeight * imageWidth / previewRatioWidth;
			aspectHeight = (float) (height / imageHeight);
			L.d("최대 높이 구하기, up down, aspect height : " + aspectHeight);

			if (aspectHeight > ZOOM_IN_OUT_VALUE) {
				isZoomIn = !isZoomIn;	
				if ((imageData.faceDataItems != null) && (imageData.faceDataItems.size() > 0)) {
					if(isAccelerationZoom){
						viewportList = kenburnPortraitAccelerationZoomInOut(imageData, imageWidth, imageHeight, kenburnDirection);
					}else{
						viewportList = kenburnPortraitZoomInOut(imageData, imageWidth, imageHeight, zoomInOutdirection);
					}
				} else {
					viewportList = kenburnLandScapeZoomInOut(imageData, zoomInOutdirection);
				}

			} else {
				if ((imageData.faceDataItems != null) && (imageData.faceDataItems.size() > 0)) {
					viewportList = kenburnPortraitUpDown(imageData, imageWidth, imageHeight, previewRatioWidth, previewRatioHeight, KenburnDirection.RANDOM);
				} else {
					viewportList = kenburnLandScapeUpDown(aspectWidth, aspectHeight, KenburnDirection.RANDOM);
				}
			}
		} else if (templetMultiplyWidth < realMultiplyWidth) {
			// 레이아웃 템블릿의 width값의 비율이 데이터의 widht값의 비율보다 작을 경우 최대 넓이를 구한다.
			// width = (imageHeight * ratioWidth)/ratioHeight;
			// 최대 넓이 값을 구하므로 사진은 좌우로 움직인다.
			// 사진이 레이아웃에 70프로 이상 노출되면 zoom in/out으로 움직인다.
			width = previewRatioWidth * imageHeight / previewRatioHeight;
			aspectWidth = (float) (width / imageWidth);

			L.d("최대 넓이, move left right, aspect width : " + aspectWidth);

			if (aspectWidth > ZOOM_IN_OUT_VALUE) {
				isZoomIn = !isZoomIn;	
				if ((imageData.faceDataItems != null) && (imageData.faceDataItems.size() > 0)) {
					if(isAccelerationZoom){
						viewportList = kenburnPortraitAccelerationZoomInOut(imageData, imageWidth, imageHeight, kenburnDirection);
					}else{
						viewportList = kenburnPortraitZoomInOut(imageData, imageWidth, imageHeight, zoomInOutdirection);
					}
				} else {
					viewportList = kenburnLandScapeZoomInOut(imageData, zoomInOutdirection);
				}
			} else {
				if (imageData.faceDataItems != null)  {
					viewportList = kenburnPortraitLeftRight(imageData, imageWidth, imageHeight, aspectWidth, aspectHeight,KenburnDirection.RANDOM);
				} else {
					viewportList = kenburnLandScapeLeftRight(aspectWidth, aspectHeight, KenburnDirection.RANDOM);
				}
			}
		} else if (templetMultiplyWidth == realMultiplyWidth) {
			// 레이아웃 템블릿의 width값의 비율이 데이터의 widht값의 비율이 동일 할 경우
			// zoom in/out으로 움직인다.
			isZoomIn = !isZoomIn;	
			if ((imageData.faceDataItems != null) && (imageData.faceDataItems.size() > 0)) {
				if(isAccelerationZoom){
					viewportList = kenburnPortraitAccelerationZoomInOut(imageData, imageWidth, imageHeight, kenburnDirection);
				}else{
					viewportList = kenburnPortraitZoomInOut(imageData, imageWidth, imageHeight, zoomInOutdirection);
				}
			} else {
				viewportList = kenburnLandScapeZoomInOut(imageData, zoomInOutdirection);
			}
		}
		Viewport[] viewportArray = new Viewport[viewportList.size()];
		for (int i = 0; i < viewportList.size(); i++) {
			viewportArray[i] = viewportList.get(i);
		}
		return viewportArray;
	}

	/**
	 * 싱글씬에서 얼굴 데이터가 있는 사진일때 최대 높이나 최대 넓이가 ZOOM_IN_OUT_VALUE 보다 클 경우<br>
	 * kenburn은 첫번째 얼굴 영역과 사진 최대 영역으로 view port를 작성한다. <br>
     * @param imageData : 얼굴 좌표가 인식된 image data. 
     * @param imageWidth : 사진 data의 width. 
     * @param imageHeight : 사진 data의 height.
     * @param aspectWidth : preview layout이 가지는 가로 비율. 
     * @param aspectHeight : preview layout이 가지는 세로 비율.
     * @param direction : kenburn의 방향. 
     * @param speed : : kenburn의 속도. 
     * @return : 시작과 끝의 view port. 
	 */
	private LinkedList<Viewport> kenburnPortraitZoomInOut(ImageData imageData, int imageWidth, int imageHeight, KenburnDirection direction) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		float realFaceWidth  = 0.0f, realFaceHeight = 0.0f; 
		float left = 0.0f, top = 0.0f, right = 0.0f, bottom = 0.0f;
		float centerHeight = 0.0f, centerWidth = 0.0f;
		Random random = new Random();
		Viewport faceViewPort = null; 
		
		float faceImageViewPortWidth = 0.0f, faceImageViewPortHeight = 0.0f;
		Viewport centerScaledViewPort = getCenterScaledViewPort(imageData, PREVIEW_RATIO_WIDTH, PREVIEW_RATIO_HEIGHT); 
		Viewport centerFullViewPort = getCenterFullSizeViewPort(imageData, PREVIEW_RATIO_WIDTH, PREVIEW_RATIO_HEIGHT);
		
		for (int i = 0; i < MAX_PERSON_NUM; i++) {
			ImageFaceData faceData = imageData.faceDataItems.get(i); 
			Rect faceRect = getFaceRect(imageData, faceData); 			
			L.d("Face rect : (" + faceRect.left +", " +faceRect.top +")(" +faceRect.right+", "+faceRect.bottom+")" );

			left = (float) faceRect.left / imageWidth;
			top = (float) faceRect.top / imageHeight;
			right = (float) faceRect.right / imageWidth;
			bottom = (float) faceRect.bottom / imageHeight;
			
			realFaceHeight = faceRect.bottom - faceRect.top; 
			realFaceWidth = (realFaceHeight / 9)*16; 
			if(realFaceWidth > imageWidth){
				realFaceWidth = faceRect.right - faceRect.left; 
				realFaceHeight = (realFaceWidth*9)/16; 
			}
			
			centerHeight = (bottom - top) * 0.5f + top;
			centerWidth = (right - left) * 0.5f + left;

			faceImageViewPortHeight = centerScaledViewPort.bottom - centerScaledViewPort.top; 
			faceImageViewPortWidth = centerScaledViewPort.right - centerScaledViewPort.left;

			top = centerHeight - (faceImageViewPortHeight/2); 
			if(top < 0){
				top = 0; 
			}
			left = centerWidth - (faceImageViewPortWidth/2); 
			if(left < 0){
				left = 0; 
			}

			right = left + faceImageViewPortWidth;
			if(right > 1.0f){
				right = 1.0f; 
				left = right - faceImageViewPortWidth; 
			}
			bottom = top + faceImageViewPortHeight;
			if(bottom > 1.0f){
				bottom = 1.0f; 
				top = bottom - faceImageViewPortHeight; 
			}
			
			L.d("face zoom in (" + left +", "+top +")("+ right+","+bottom+")"); 

			faceViewPort = makeViewport(left, top, right, bottom, faceImageViewPortWidth, faceImageViewPortHeight); 
			viewportList.add(faceViewPort);

		}
		float fullWidth = centerFullViewPort.right - centerFullViewPort.left; 
		float fullHeight = centerFullViewPort.bottom - centerFullViewPort.top;
		
		left = centerWidth - (fullWidth * 0.5f); 
		if(left < 0.0f){
			left = 0.0f; 
		}
		top = centerHeight - (fullHeight * 0.5f); 
		if(top < 0.0f){
			top = 0.0f; 
		}
		right = left + fullWidth; 
		if(right > 1.0f){
			right = 1.0f; 
			left = right - fullWidth; 
		}
		bottom = top + fullHeight;
		if(bottom > 1.0f){
			bottom = 1.0f; 
			top = bottom - fullHeight;  
		}

		L.d("face zoom out (" + left +", "+top +")("+ right+","+bottom+")"); 
		Viewport zoomOutViewPort = makeViewport(left, top, right, bottom, fullWidth, fullHeight); 

		if(direction == KenburnDirection.IN){
			viewportList.add(zoomOutViewPort);
		}else if(direction == KenburnDirection.OUT){
			viewportList.add(0, zoomOutViewPort);
		}else{
			if (random.nextBoolean()) {
				viewportList.add(0, zoomOutViewPort);
			} else {
				viewportList.add(zoomOutViewPort);
			}
		}
		return viewportList;
	}
	
	private LinkedList<Viewport> kenburnPortraitAccelerationZoomInOut(ImageData imageData, int imageWidth, int imageHeight, KenburnDirection direction) {
		LinkedList<Viewport> viewportList = new LinkedList<Viewport>();
		float realFaceWidth  = 0.0f, realFaceHeight = 0.0f; 
		float left = 0.0f, top = 0.0f, right = 0.0f, bottom = 0.0f;
		float centerHeight = 0.0f, centerWidth = 0.0f;
		Random random = new Random();
		Viewport faceViewPort = null; 
		
		float faceImageViewPortWidth = 0.0f, faceImageViewPortHeight = 0.0f;
		Viewport centerFullViewPort = getCenterFullSizeViewPort(imageData, PREVIEW_RATIO_WIDTH, PREVIEW_RATIO_HEIGHT);
		L.d("Acceleration image path : " + imageData.path +", person num : " + imageData.faceDataItems.size()); 
		for (int i = 0; i < imageData.faceDataItems.size(); i++) {
			ImageFaceData faceData = imageData.faceDataItems.get(i); 
			Rect faceRect = getFaceRect(imageData, faceData); 

			left = (float) faceRect.left / imageWidth;
			top = (float) faceRect.top / imageHeight;
			right = (float) faceRect.right / imageWidth;
			bottom = (float) faceRect.bottom / imageHeight;
			
			realFaceHeight = faceRect.bottom - faceRect.top;
			realFaceWidth = (realFaceHeight / 9)*16; 
			if(realFaceWidth > imageWidth){
				realFaceWidth = faceRect.right - faceRect.left; 
				realFaceHeight = (realFaceWidth*9)/16; 
			}
			
			centerHeight = (bottom - top) * 0.5f + top;
			centerWidth = (right - left) * 0.5f + left;
			
			faceImageViewPortHeight = realFaceHeight / imageHeight; 
			faceImageViewPortWidth = realFaceWidth / imageWidth;
			
			L.d("realFaceHeight : " + realFaceHeight +", realFaceWidth : " + realFaceWidth +", faceImageViewPortHeight : " + faceImageViewPortHeight +", faceImageViewPortWidth : " + faceImageViewPortWidth); 

			top = centerHeight - (faceImageViewPortHeight/2); 
			if(top < 0){
				top = 0; 
			}
			left = centerWidth - (faceImageViewPortWidth/2); 
			if(left < 0){
				left = 0; 
			}

			right = left + faceImageViewPortWidth;
			if(right > 1.0f){
				right = 1.0f; 
				left = right - faceImageViewPortWidth; 
			}
			bottom = top + faceImageViewPortHeight;
			if(bottom > 1.0f){
				bottom = 1.0f; 
				top = bottom - faceImageViewPortHeight; 
			}
			
			L.d("face viewport num : "+ i+", (" + left +", "+top +")("+ right+","+bottom+")"); 

			faceViewPort = makeViewport(left, top, right, bottom, faceImageViewPortWidth, faceImageViewPortHeight); 
			viewportList.add(faceViewPort);

		}
		float fullWidth = centerFullViewPort.right - centerFullViewPort.left; 
		float fullHeight = centerFullViewPort.bottom - centerFullViewPort.top;
		
		left = centerWidth - (fullWidth * 0.5f); 
		if(left < 0.0f){
			left = 0.0f; 
		}
		top = centerHeight - (fullHeight * 0.5f); 
		if(top < 0.0f){
			top = 0.0f; 
		}
		right = left + fullWidth; 
		if(right > 1.0f){
			right = 1.0f; 
			left = right - fullWidth; 
		}
		bottom = top + fullHeight;
		if(bottom > 1.0f){
			bottom = 1.0f; 
			top = bottom - fullHeight;  
		}

		L.d("face zoom out (" + left +", "+top +")("+ right+","+bottom+")"); 
		Viewport zoomOutViewPort = makeViewport(left, top, right, bottom, fullWidth, fullHeight);
		
		if(viewportList.size() == 1){

			if(direction == KenburnDirection.OUT){
				viewportList.add(zoomOutViewPort);
				viewportList.add(getScaledDownViewport(zoomOutViewPort));
			}else if(direction == KenburnDirection.IN){
				viewportList.add(getScaledUpViewport(faceViewPort)); 
				viewportList.add(0, zoomOutViewPort);
			}
		}else{
			Viewport tempViewport = new Viewport(viewportList.get(0).left, viewportList.get(0).top, viewportList.get(0).right, viewportList.get(0).bottom); 
			if(tempViewport.left > viewportList.get(1).left){
				viewportList.add(tempViewport); 
				viewportList.remove(0); 
			}
			viewportList.add(getScaledUpViewport(viewportList.getLast())); 
			viewportList.add(0, zoomOutViewPort);
		}
		return viewportList;
	}

	private Viewport getScaledDownViewport(Viewport viewport) {
		float left = 0, top = 0, right = 0, bottom = 0;
		float centerHeight = (viewport.bottom - viewport.top) * 0.5f + viewport.top;
		float centerWidth = (viewport.right - viewport.left) * 0.5f + viewport.left;
		float scaleWidth = (float) ((viewport.right - viewport.left) * Math.sqrt(0.8f));
		float scaleHeight = (float) ((viewport.bottom - viewport.top) * Math.sqrt(0.8f));
		
		top = centerHeight - (scaleHeight/2); 
		if(top < 0){
			top = 0; 
		}
		left = centerWidth - (scaleWidth/2); 
		if(left < 0){
			left = 0; 
		}

		right = left + scaleWidth;
		if(right > 1.0f){
			right = 1.0f; 
			left = right - scaleWidth; 
		}
		bottom = top + scaleHeight;
		if(bottom > 1.0f){
			bottom = 1.0f; 
			top = bottom - scaleHeight; 
		}
		
		return new Viewport(left, top, right, bottom);
	}
	
	private float  getScaleWidth(float scaleValue, Viewport viewport){
		return (float) ((viewport.right - viewport.left) * Math.sqrt(scaleValue));
	}
	
	private float  getScaleHeight(float scaleValue, Viewport viewport){
		return (float) ((viewport.bottom - viewport.top) * Math.sqrt(scaleValue));
	}
	
	private Viewport getScaledUpViewport(Viewport viewport) {
		float left = 0, top = 0, right = 0, bottom = 0;
		float scaleWidth = 0.0f; 
		float scaleHeight = 0.0f; 
		float centerHeight = (viewport.bottom - viewport.top) * 0.5f + viewport.top;
		float centerWidth = (viewport.right - viewport.left) * 0.5f + viewport.left;
		
		for(float scaleValue = 1.8f; scaleValue >= 1.0f; scaleValue -= 0.1f){
			scaleWidth = getScaleWidth(scaleValue, viewport); 
			scaleHeight = getScaleHeight(scaleValue, viewport);
			if(!(scaleWidth > 1.0f || scaleHeight > 1.0f)){
				break; 
			}
		}
		
		top = centerHeight - (scaleHeight/2); 
		if(top < 0){
			top = 0; 
		}
		left = centerWidth - (scaleWidth/2); 
		if(left < 0){
			left = 0; 
		}

		right = left + scaleWidth;
		if(right > 1.0f){
			right = 1.0f; 
			left = right - scaleWidth; 
		}
		bottom = top + scaleHeight;
		if(bottom > 1.0f){
			bottom = 1.0f; 
			top = bottom - scaleHeight; 
		}
		return new Viewport(left, top, right, bottom);
	}

	private Viewport getLastFaceMoveViewport(Viewport faceViewPort) {
		float left, top, right, bottom;
		float move = 0.1f;
    	float width = faceViewPort.right - faceViewPort.left;
    	float height = faceViewPort.bottom - faceViewPort.top; 

    	if(width > 0.9f){
    		L.d("Face size too big so can not move");
    		return faceViewPort;
    	}else{
    		if(faceViewPort.right <= 0.9f){
        		left = faceViewPort.left + move; 
        		right = faceViewPort.right + move; 
        	}else if(faceViewPort.left >= 0.1f){
        		left = faceViewPort.left - move; 
        		right = faceViewPort.right - move;
        	}else{
        		left = faceViewPort.left; 
        		right = faceViewPort.right;
        	}
        	
    		top = faceViewPort.top; 
    		bottom = faceViewPort.bottom;
    		
    		L.d("kenburn  view point(" + left + ", " + top + ", " + right + ", " + bottom + ") move : " + move);
    		return makeViewport(left, top, right, bottom, width, height);
    	}
	}
	

}
