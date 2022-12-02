package com.kiwiple.scheduler.coordinate.scaler.uplus;

import java.util.LinkedList;

import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.debug.L;
import com.kiwiple.scheduler.coordinate.scaler.KenBurnsScalerCoordinator;
import com.kiwiple.scheduler.data.MultiLayerData;

public class UplusMultiLayerKenBurnScalerCoordinator extends KenBurnsScalerCoordinator {

	@Override
	public Viewport[] applyKenBurnEffectFromStartViewPort(ImageData imageData, Viewport startViewPort, KenburnDirection direction) {

		return super.applyKenBurnEffectFromStartViewPort(imageData, startViewPort, direction);
	}

	public Viewport[] applyKenBurnEffectPicture(ImageData imageData, int templateId, int index) {
		float previewRatioWidth = 0.0f;
		float previewRatioHeight = 0.0f;
		KenburnDirection direction = null;

		if (templateId == MultiLayerData.MULTI_LAYER_COLUMN_TWO_PICTURES_ID) {
			previewRatioWidth = 8.0f;
			previewRatioHeight = 9.0f;
			if (index == 0) {
				direction = KenburnDirection.UP;
			} else if (index == 1) {
				direction = KenburnDirection.DOWN;
			}
		} else if (templateId == MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID) {
			previewRatioHeight = 9.0f;
			if (index == 0) {
				previewRatioWidth = 5.2f;
				direction = KenburnDirection.UP;
			} else if (index == 1) {
				previewRatioWidth = 5.6f;
				direction = KenburnDirection.DOWN;
			} else if (index == 2) {
				previewRatioWidth = 5.2f;
				direction = KenburnDirection.UP;
			}
		} else if (templateId == MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID) {
			previewRatioWidth = 8.0f;
			if (index == 0) {
				previewRatioHeight = 9.0f;
				direction = KenburnDirection.UP;
			} else if (index == 1) {
				previewRatioHeight = 4.5f;
				direction = KenburnDirection.RIGHT;
			} else if (index == 2) {
				previewRatioHeight = 4.5f;
				direction = KenburnDirection.LEFT;
			}
		} else if (templateId == MultiLayerData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID) {
			previewRatioWidth = 8.0f;
			previewRatioHeight = 4.5f;
			if (index == 0) {
				direction = KenburnDirection.RIGHT;
			} else if (index == 1) {
				direction = KenburnDirection.UP;
			} else if (index == 2) {
				direction = KenburnDirection.DOWN;
			} else if (index == 3) {
				direction = KenburnDirection.LEFT;
			}
		} else if (templateId == MultiLayerData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID) {
			previewRatioHeight = 4.5f;
			if (index == 0) {
				previewRatioWidth = 6.0f;
				direction = KenburnDirection.RIGHT;
			} else if (index == 1) {
				previewRatioWidth = 10.0f;
				direction = KenburnDirection.LEFT;
			} else if (index == 2) {
				previewRatioWidth = 10.0f;
				direction = KenburnDirection.RIGHT;
			} else if (index == 3) {
				previewRatioWidth = 6.0f;
				direction = KenburnDirection.LEFT;
			}
		}

		return applyKenBurnEffectPicture(imageData, previewRatioWidth, previewRatioHeight, direction);
	}

	/**
	 * 멀티씬의 사진 데이터, preview의 비율, 방향성에 따라서 kenburn의 시작과 끝 좌표를 설정한다.
	 * 
	 * @param imageData
	 *            : 사진 데이터.
	 * @param previewRatioWidth
	 *            : preview의 가로 비율.
	 * @param previewRatioHeight
	 *            : preview의 세로 비율.
	 * @param direction
	 *            : kenburn의 방향성.
	 * @param speed
	 *            : kenburn의 속도.
	 * @return : view port.
	 */
	public Viewport[] applyKenBurnEffectPicture(ImageData imageData, float previewRatioWidth, float previewRatioHeight, KenburnDirection direction) {
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

		KenburnShape shape = previewRatioHeight > previewRatioWidth ? KenburnShape.VERTICAL : KenburnShape.HORIZONTAL;

		// 이미지의 가로, 세로 여부를 판단한다.
		if ((orientation != 90) && (orientation != 270)) {
			// 명확하지 않는 orientation 정보로 인해서, 이미지 데이터의 width/height값을 체그해 가로 세로를
			// 결정 한다.
			if (imageData.width > imageData.height) {
				// 가로 사진
				L.d("orientation 정보가 정확한 가로 사진의 파일 이름 : " + imageData.fileName + ", widht : " + imageData.width + ", height : " + imageData.height);
				imageWidth = imageData.width;
				imageHeight = imageData.height;
			} else {
				// 세로 사진
				// LG, Samsung폰의 스크린 샷의 경우 가로 모드와 세로 모드가 적용되서 width, height값이
				// 적용된다.
				L.d("orientation 정보가 정확하지 않는 세로 사진의 파일 이름 : " + imageData.fileName + ", widht : " + imageData.width + ", height : " + imageData.height);
				imageWidth = imageData.width;
				imageHeight = imageData.height;
			}
		} else {
			// 세로 사진.
			// orientation정보가 90, 270일 경우 일반적으로 widht, height값은 변경되지 않는다.
			imageWidth = imageData.height;
			imageHeight = imageData.width;
			L.d("orientation 정보가 정확한 세로 사진의 파일 이름 : " + imageData.fileName + ", widht : " + imageData.width + ", height : " + imageData.height);
		}

		templetMultiplyWidth = previewRatioWidth * imageHeight;
		realMultiplyWidth = imageWidth * previewRatioHeight;

		if (templetMultiplyWidth > realMultiplyWidth) {
			// 레이아웃 템블릿의 width값의 비율이 데이터의 widht값의 비율보다 클 경우 최대 높이를 구한다.
			// height = (imageWidth * ratioHight)/ratioWidth;
			// 최대 높이 값을 구하므로 사진은 위아래로 움직이거나
			// 사진이 레이아웃에 70프로 이상 노출 되면 zoom in/out으로 움직인다.
			height = previewRatioHeight * imageWidth / previewRatioWidth;
			aspectHeight = (float) (height / imageHeight);
			L.d("최대 높이 구하기, up down, aspect height : " + aspectHeight + " direction : " + direction);

			// 최대 높이를 구한 경우. 위아래 움직일수 있다.
			if (direction == KenburnDirection.UP || direction == KenburnDirection.DOWN) {
				if (aspectHeight > KenBurnsScalerCoordinator.ASPECT_RATIO_MAX_VALUE && aspectHeight < 1.0f) {
					viewportList = kenburnLandScapeCropUpDown(aspectWidth, aspectHeight, direction);
				} else {
					viewportList = kenburnLandScapeUpDown(aspectWidth, aspectHeight, direction);
				}
			} else {
				// 최대 높이를 구한 경우 옆으로 움직일수 없다. 하지만 최대 높이가 ZOOM_IN_OUT_MULTI_VALUE 이상이라면 옆으로 움직이게 한다.
				if (aspectHeight >= ZOOM_IN_OUT_MULTI_VALUE) {
					// 사진이 보여줄수 있는 영역이 70%라면 줌인을 한다.
					viewportList = kenburnLandScapeCropLeftRight(aspectWidth, aspectHeight, direction);
				} else {
					// 사진이 보여줄수 있는 영역이 70% 이하라면 up down을 한다.
					if (imageData.faceDataItems != null) {
						viewportList = kenburnPortraitUpDown(imageData, imageWidth, imageHeight, aspectWidth, aspectHeight, direction);
					} else {
						viewportList = kenburnLandScapeUpDown(aspectWidth, aspectHeight, direction);
					}
				}

			}

		} else if (templetMultiplyWidth < realMultiplyWidth) {
			// 레이아웃 템블릿의 width값의 비율이 데이터의 widht값의 비율보다 작을 경우 최대 넓이를 구한다.
			// width = (imageHeight * ratioWidth)/ratioHeight;
			// 최대 넓이 값을 구하므로 사진은 좌우로 움직인다.
			// 사진이 레이아웃에 70프로 이상 노출되면 zoom in/out으로 움직인다.
			width = previewRatioWidth * imageHeight / previewRatioHeight;
			aspectWidth = (float) (width / imageWidth);

			L.d("최대 넓이, move left right, aspect width : " + aspectWidth + " direction : " + direction);
			// 최대 넓이를 구한 경우. 양 옆으로 움직일수 있다.
			if (direction == KenburnDirection.LEFT || direction == KenburnDirection.RIGHT) {
				if(aspectWidth > KenBurnsScalerCoordinator.ASPECT_RATIO_MAX_VALUE && aspectWidth < 1.0f){
					viewportList = kenburnLandScapeCropLeftRight(aspectWidth, aspectHeight, direction);
				}else{
					viewportList = kenburnLandScapeLeftRight(aspectWidth, aspectHeight, direction);
				}
			} else {
				// 최대 넓이를 구한 경우, 위아래로 움직 일 수 없다. 하지만 최대 넓이가 ZOOM_IN_OUT_MULTI_VALUE 보다 크다면 view
				// port를 설정한다.
				if (aspectWidth > ZOOM_IN_OUT_MULTI_VALUE) {
					viewportList = kenburnLandScapeCropUpDown(aspectWidth, aspectHeight, direction);

				} else {
					if (imageData.faceDataItems != null) {
						viewportList = kenburnPortraitLeftRight(imageData, imageWidth, imageHeight, previewRatioWidth, previewRatioHeight, direction);
					} else {
						viewportList = kenburnLandScapeLeftRight(aspectWidth, aspectHeight, direction);
					}
				}
			}

		} else if (templetMultiplyWidth == realMultiplyWidth) {
			L.d("조각 레이아웃과 사진의 비율이 일치. direction : " + direction);
			// 가로 사진으로 조각 레이아웃과 비율이 일치(16:9) 할 경우. 위아래 양 옆으로 움직일수 있다.
			if (direction == KenburnDirection.UP || direction == KenburnDirection.DOWN) {
				viewportList = kenburnLandScapeSameRatioUpDown(SAME_RATIO, SAME_RATIO, direction);
			} else {
				viewportList = kenburnLandScapeSameRatioLeftRight(SAME_RATIO, SAME_RATIO, direction);
			}
		}
		Viewport[] viewportArray = new Viewport[viewportList.size()];
		for (int i = 0; i < viewportList.size(); i++) {
			viewportArray[i] = viewportList.get(i);
		}
		return viewportArray;
	}
}
