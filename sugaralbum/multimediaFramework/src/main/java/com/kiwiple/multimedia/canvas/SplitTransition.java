package com.kiwiple.multimedia.canvas;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.json.JSONException;

import android.animation.TimeInterpolator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.R;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * 전 {@code Scene}을 후 {@code Scene}으로 덮어버리는 방식의 전환 효과를 연출하는 클래스.
 * <p />
 * 전 {@code Scene}을 바탕에 깔고, 이를 갈라내듯 후 {@code Scene}의 이미지가 중앙으로부터 시작하여 면적을 늘려 나갑니다.
 */
public final class SplitTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "split_transition";

	public static final String JSON_NAME_DIRECTION = "direction";
	public static final String JSON_NAME_LINE_COLOR = "line_color";
	public static final String JSON_NAME_WHITE_LINE_SPLIT = "white_line_split";
	
	private static final float LINE_PROGRESS_RATIO = 0.1f;
	
	//가운데에서 선그리는 시간 (0.075 - 0)
	private static final float CENTER_TO_SIDE_PROGRESS_RATIO = 0.075f;
	// side에서 반원 이동하는 시간 (0.275 - 0.075)
	private static final float SIDE_TO_CENTER_PROGRESS_RATIO = 0.275f;
	// 가운데에서 반원이 갈라지는 시간 (0.5 - 0.275)
	private static final float ON_PAUSE_PROGRESS_RATIO = 0.5f;
	// 가운데에서 반원이 갈라지고 대기하는 시간(0.875 - 0.5)
	private static final float ON_START_PROGRESS_RATIO = 0.875f;
	// 마무리 시간 
	private static final float SPLIT_PROGRESS_RATIO = 1.0f - LINE_PROGRESS_RATIO;

	private static final float LINE_PROGRESS_RATIO_FACTOR = 1.0f / LINE_PROGRESS_RATIO;
	private static final float SPLIT_PROGRESS_RATIO_FACTOR = 1.0f / SPLIT_PROGRESS_RATIO;
	private final int PLUS_LINE_COLOR = 0xFFFFFFFF;

	private static final TimeInterpolator sLineInterpolator = new AccelerateInterpolator();
	private static final TimeInterpolator sSplitInterpolator = new LinearInterpolator();
	//whiteSplitLine  사용시
	private static final TimeInterpolator sDecelerateLineInterpolator = new DecelerateInterpolator(2.5f);
	private static final TimeInterpolator sDefaultLineInterpolator = new DecelerateInterpolator();
	private static final TimeInterpolator sDiagonalLineInterpolator = new DecelerateInterpolator(1.35f);
	private static final TimeInterpolator sEndWhiteSplitLineInterpolator = new DecelerateInterpolator();
	// // // // // Member variable.
	// // // // //
	private Direction mDirection = Direction.HORIZONTAL;

	private float mLineSizeRatio = 0.004f;
	private final float SPLIT_LINE_MAX_SIZE_RATIO = 0.999f;
	private final float  PROGRESS_OFFSET = 0.025f;
	private int mLineSize;
	private int mLineColor;
	
	private boolean mIsWhiteLineSplit = false;
	private Size mSymbolSize;
	private ImageResource mSymbolResource;
	private DecimalFormat mDecimalFormat = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
	
	private final float H_V_PAUSE_PROGRESS_RATIO = sDecelerateLineInterpolator.getInterpolation(((ON_PAUSE_PROGRESS_RATIO+PROGRESS_OFFSET - SIDE_TO_CENTER_PROGRESS_RATIO) * 1 / (1 - SIDE_TO_CENTER_PROGRESS_RATIO)));
	private final float DIAGONAL_PAUSE_PROGRESS_RATIO = sDiagonalLineInterpolator.getInterpolation(((ON_PAUSE_PROGRESS_RATIO+PROGRESS_OFFSET - SIDE_TO_CENTER_PROGRESS_RATIO) * 1 / (1 - SIDE_TO_CENTER_PROGRESS_RATIO)));
	
	// // // // // Constructor.
	// // // // //
	SplitTransition(Region parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	Change[] getSensitivities() {
		return new Change[] { Change.SIZE };
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		
		mLineSize = Math.round((getWidth() + getHeight()) * mLineSizeRatio);
		if(mIsWhiteLineSplit){
			mSymbolResource = ImageResource.createFromDrawable(R.drawable.half_circle, getResources(), Resolution.FHD);
			if(mDirection.degree == 0){
				mSymbolSize = mSymbolResource.measureSize(getResolution(), null, 0f);
			}else if(mDirection.degree == 90){
				mSymbolSize = mSymbolResource.measureSize(getResolution(), null, 90f);
			}else if(mDirection.degree == 45){
				mSymbolSize = mSymbolResource.measureSize(getResolution(), null, 45f);
			}else{
				mSymbolSize = mSymbolResource.measureSize(getResolution(), null, 135f);
			}
		}
	}

	
	@Override
	void onPrepare() {
		// TODO Auto-generated method stub
		super.onPrepare();
		
		if(mIsWhiteLineSplit){
			
			Bitmap symbolBitmap;
			
			try {
				PixelCanvas buffer;
				Matrix matrix = new Matrix();
				
				if(mDirection.degree == 0){
					buffer = getCanvas(1);
					symbolBitmap = mSymbolResource.createBitmap(getResolution(), null, 0.f);
					buffer.copyFrom(symbolBitmap);
					recycleBitmap(symbolBitmap);
					
					symbolBitmap = mSymbolResource.createBitmap(getResolution(), null, 180.f);
					buffer = getCanvas(0);
					buffer.copyFrom(symbolBitmap);
					recycleBitmap(symbolBitmap);
					
				}else if(mDirection.degree == 90){
					
					symbolBitmap = mSymbolResource.createBitmap(getResolution(), null, -90f);
					buffer = getCanvas(0);
					buffer.copyFrom(symbolBitmap);
					recycleBitmap(symbolBitmap);
							
					symbolBitmap = mSymbolResource.createBitmap(getResolution(), null, 90f);
					buffer = getCanvas(1);
					buffer.copyFrom(symbolBitmap);
					recycleBitmap(symbolBitmap);
					
				}else if(mDirection.degree == 45){
					symbolBitmap = mSymbolResource.createBitmap(getResolution(), null, -45f);
					buffer = getCanvas(0);
					buffer.copyFrom(symbolBitmap);
					recycleBitmap(symbolBitmap);
					
					symbolBitmap = mSymbolResource.createBitmap(getResolution(), null, 135f);
					buffer = getCanvas(1);
					buffer.copyFrom(symbolBitmap);
					recycleBitmap(symbolBitmap);
					
				}else{  //135
					
					symbolBitmap = mSymbolResource.createBitmap(getResolution(), null, -135f);
					matrix.postRotate(-135);
					buffer = getCanvas(0);
					buffer.copyFrom(symbolBitmap);
					recycleBitmap(symbolBitmap);
					
					symbolBitmap = mSymbolResource.createBitmap(getResolution(), null, 45f);
					buffer = getCanvas(1);
					buffer.copyFrom(symbolBitmap);
					recycleBitmap(symbolBitmap);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void recycleBitmap(Bitmap symbolBitmap) {
		if(symbolBitmap != null && !symbolBitmap.isRecycled()){
			symbolBitmap.recycle();
			symbolBitmap = null;
		}
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_DIRECTION, mDirection);
		jsonObject.put(JSON_NAME_LINE_COLOR, mLineColor);
		jsonObject.put(JSON_NAME_WHITE_LINE_SPLIT, mIsWhiteLineSplit);
		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setDirection(jsonObject.getEnum(JSON_NAME_DIRECTION, Direction.class));
		setLineColor(jsonObject.getInt(JSON_NAME_LINE_COLOR));

		if (!jsonObject.isNull(JSON_NAME_WHITE_LINE_SPLIT)) {
			boolean whiteLineSplit = jsonObject.getBoolean(JSON_NAME_WHITE_LINE_SPLIT);
			setWhileLineSplit(whiteLineSplit);
		}
	}

	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		float progressRatio = getProgressRatio();
		
		//While line 적용시 
		if(mIsWhiteLineSplit){
			float splitProgressRatio = sDecelerateLineInterpolator.getInterpolation(((progressRatio - SIDE_TO_CENTER_PROGRESS_RATIO)  / (1 - SIDE_TO_CENTER_PROGRESS_RATIO)));
			float splitDiagonalProgressRatio = sDiagonalLineInterpolator.getInterpolation(((progressRatio - SIDE_TO_CENTER_PROGRESS_RATIO)  / (1 - SIDE_TO_CENTER_PROGRESS_RATIO)));
			float endSplitProgressRatio = sDecelerateLineInterpolator.getInterpolation(((progressRatio - ON_PAUSE_PROGRESS_RATIO)  / (1-0.65f)));

			int width = dstCanvas.getImageWidth();
			int height = dstCanvas.getImageHeight();
			int centerX = dstCanvas.getImageWidth() / 2;
			int centerY = dstCanvas.getImageHeight() /2;
			
			float fixedRatio = 0.001f;
			
			int lineXLength = Math.round(centerX * (1 - H_V_PAUSE_PROGRESS_RATIO));
			int lineYLength = Math.round(centerY  * (1 - H_V_PAUSE_PROGRESS_RATIO));
			
			if(lineXLength < lineYLength){
				int swapLength = lineXLength;
				lineXLength = lineYLength;
				lineYLength = swapLength;
			}
			
			PixelCanvas circleLeftUp = getCanvas(0);
			PixelCanvas circleRightDown = getCanvas(1);
			
			// center to side 
			if(progressRatio <CENTER_TO_SIDE_PROGRESS_RATIO  && progressRatio > 0 ){
//				splitProgressRatio = sDefaultLineInterpolator.getInterpolation((float) (progressRatio/* * SPLIT_LINE_MAX_SIZE_RATIO */ * 10));
				splitProgressRatio = sDefaultLineInterpolator.getInterpolation((float) (progressRatio * (1/CENTER_TO_SIDE_PROGRESS_RATIO)));
				srcCanvasFormer.copy(dstCanvas);
				int lineOffSet = Math.round(centerY * splitProgressRatio);
				
				if(mDirection.degree == 0){
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					dstCanvas.drawLine(mLineColor, centerX, centerY, centerX , centerY - lineOffSet, mLineSize);
					dstCanvas.drawLine(mLineColor, centerX, centerY, centerX, centerY + lineOffSet, mLineSize);
				}else if(mDirection.degree == 90){
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					dstCanvas.drawLine(mLineColor, centerX, centerY, centerX + lineOffSet, centerY, mLineSize);
					dstCanvas.drawLine(mLineColor, centerX, centerY, centerX - lineOffSet, centerY, mLineSize);
					// 사선의 경우 교점이 비어있는 것 처럼 보이기에 line을 한좌표 이동해서 그리자. 45' 135'
				}else if(mDirection.degree == 45){
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					dstCanvas.drawLine(mLineColor, centerX, centerY, centerX - lineOffSet, centerY - lineOffSet, mLineSize);
					dstCanvas.drawLine(mLineColor, centerX - 1, centerY - 1, centerX + lineOffSet, centerY + lineOffSet, mLineSize);
				}else{  // 135'
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					dstCanvas.drawLine(mLineColor, centerX, centerY, centerX - lineOffSet, centerY + lineOffSet, mLineSize);
					dstCanvas.drawLine(mLineColor, centerX - 1, centerY + 1, centerX + lineOffSet, centerY - lineOffSet, mLineSize);
				}

				//side to center with half circle
			}else if(progressRatio >= CENTER_TO_SIDE_PROGRESS_RATIO && progressRatio < SIDE_TO_CENTER_PROGRESS_RATIO){
				float tempProgressRatio = progressRatio;
				progressRatio -= CENTER_TO_SIDE_PROGRESS_RATIO;
//				splitProgressRatio = sDefaultLineInterpolator.getInterpolation((float) (progressRatio /** SPLIT_LINE_MAX_SIZE_RATIO*/ * 10));
				splitProgressRatio = sDefaultLineInterpolator.getInterpolation((float) (progressRatio *  (1/ (SIDE_TO_CENTER_PROGRESS_RATIO - CENTER_TO_SIDE_PROGRESS_RATIO))));
				PixelUtils.applySplitTransition(srcCanvasFormer, srcCanvasLatter, dstCanvas, mLineSize, Color.TRANSPARENT, mDirection.degree, fixedRatio);
				
				if(mDirection.degree == 0){
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, tempProgressRatio, splitProgressRatio);
					dstCanvas.drawLine(mLineColor, centerX, 0, centerX , height, mLineSize);
				
				}else if(mDirection.degree == 90){
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, tempProgressRatio, splitProgressRatio);
					dstCanvas.drawLine(mLineColor, mLineColor, centerY, width , centerY, mLineSize);
				
				}else if(mDirection.degree == 45){  // ' \ '
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, tempProgressRatio, splitProgressRatio);
					dstCanvas.drawLine(mLineColor, centerX - centerY, 0, centerX + centerY , height, mLineSize);

				}else{  // 135'     ' / ' 
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, tempProgressRatio, splitProgressRatio);
					dstCanvas.drawLine(mLineColor, centerX - centerY, height, centerX + centerY , 0, mLineSize);
				}
			}else{
				
				if (progressRatio >= SIDE_TO_CENTER_PROGRESS_RATIO && progressRatio < ON_PAUSE_PROGRESS_RATIO) {
					
					if(mDirection.degree == 45 || mDirection.degree == 135){
						PixelUtils.applySplitTransition(srcCanvasFormer, srcCanvasLatter, dstCanvas, mLineSize, mLineColor, mDirection.degree, splitDiagonalProgressRatio);
						drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, progressRatio, splitDiagonalProgressRatio);
					}else{
						PixelUtils.applySplitTransition(srcCanvasFormer, srcCanvasLatter, dstCanvas, mLineSize, mLineColor, mDirection.degree, splitProgressRatio);
						drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, progressRatio, splitProgressRatio);
					}
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					
				//잠시 대기 
				} else if(progressRatio >= ON_PAUSE_PROGRESS_RATIO && progressRatio < ON_START_PROGRESS_RATIO){
					float tmpProgress;
					
					if(mDirection.degree == 45 || mDirection.degree == 135){
						tmpProgress = Float.parseFloat(mDecimalFormat.format(DIAGONAL_PAUSE_PROGRESS_RATIO));
						PixelUtils.applySplitTransition(srcCanvasFormer, srcCanvasLatter, dstCanvas, mLineSize, mLineColor, mDirection.degree, tmpProgress);
						drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, progressRatio, DIAGONAL_PAUSE_PROGRESS_RATIO);
					}else{
						tmpProgress = Float.parseFloat(mDecimalFormat.format(H_V_PAUSE_PROGRESS_RATIO));
						PixelUtils.applySplitTransition(srcCanvasFormer, srcCanvasLatter, dstCanvas, mLineSize, mLineColor, mDirection.degree, tmpProgress);
						drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, progressRatio, H_V_PAUSE_PROGRESS_RATIO);
					}
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
					//마무리
				}else if(progressRatio >= ON_START_PROGRESS_RATIO){
					float tmpProgressRatio = progressRatio;
					progressRatio -= ON_START_PROGRESS_RATIO;
					splitProgressRatio = sEndWhiteSplitLineInterpolator.getInterpolation((float) (progressRatio));
					if(H_V_PAUSE_PROGRESS_RATIO + splitProgressRatio > 1.0f){
						endSplitProgressRatio = 1.0f;
						PixelUtils.applySplitTransition(srcCanvasFormer, srcCanvasLatter, dstCanvas, mLineSize, Color.TRANSPARENT, mDirection.degree, endSplitProgressRatio);
						drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, tmpProgressRatio, endSplitProgressRatio);
					}else{
						if(mDirection.degree == 45 || mDirection.degree == 135){
							endSplitProgressRatio = DIAGONAL_PAUSE_PROGRESS_RATIO + splitProgressRatio;
						}else{
							endSplitProgressRatio = H_V_PAUSE_PROGRESS_RATIO + splitProgressRatio;
						}
						PixelUtils.applySplitTransition(srcCanvasFormer, srcCanvasLatter, dstCanvas, mLineSize, mLineColor, mDirection.degree, endSplitProgressRatio);
						drawHalfCircleSymbol(circleLeftUp, circleRightDown, dstCanvas, width, height, tmpProgressRatio, endSplitProgressRatio);
					}
					drawPlusSymbol(dstCanvas, lineXLength, lineYLength, width, height);
				}else {
					srcCanvasFormer.copy(dstCanvas);
				}
			}
		}else{
			// Original SplitTransition
			float splitProgressRatio = sSplitInterpolator.getInterpolation(((progressRatio - LINE_PROGRESS_RATIO) * SPLIT_PROGRESS_RATIO_FACTOR));
			if (progressRatio < LINE_PROGRESS_RATIO) {
				srcCanvasFormer.copy(dstCanvas);
			} else {
				PixelUtils.applySplitTransition(srcCanvasFormer, srcCanvasLatter, dstCanvas, mLineSize, mLineColor, mDirection.degree, splitProgressRatio);
			}
		}
	}

	//게이지별 circle 그리기
	private void drawHalfCircleSymbol(PixelCanvas circleLeftUp, PixelCanvas circleRightDown, PixelCanvas dstCanvas, int width, int height, float progressRatio, float splitProgressRatio) {
		int centerX = dstCanvas.getImageWidth() / 2;
		int centerY = dstCanvas.getImageHeight() / 2;
		int lineOffSet = Math.round(centerY * splitProgressRatio);
		int diagonalSymbolOffSet = Math.round(height * splitProgressRatio);
		
		int circleWidth  = circleLeftUp.getImageWidth();
		int circleHeight = circleLeftUp.getImageHeight();
		
		int centerCircleX = circleWidth / 2;
		int centerCircleY = circleHeight / 2;
		int aThirdCircleX = circleWidth / 3;

		// use for diagonal line
		int latterImageWidth = (int) (width * splitProgressRatio + 0.5f );
		int addendPerY = (mDirection.degree > 90) ? 1 : -1;

		int latterCenterX = centerX + centerY* addendPerY;
		int latterStartX = latterCenterX - latterImageWidth;
		int latterEndX = latterCenterX + latterImageWidth;

		if (latterStartX < 0) {
			latterStartX = 0;
		}
		if (latterEndX > width) {
			latterEndX = width;
		}
		
		int leftLineX1 = centerX + centerY * addendPerY - latterImageWidth /*- mLineSize/2*/;
		int leftLineX2 = leftLineX1 - height * addendPerY; 
		int rightLineX1 = leftLineX1 + latterImageWidth * 2;
		int rightLineX2 = leftLineX2 + latterImageWidth * 2;

		if(mDirection.degree == 0){
			if(progressRatio >= CENTER_TO_SIDE_PROGRESS_RATIO && progressRatio < SIDE_TO_CENTER_PROGRESS_RATIO){
				circleLeftUp.blend(dstCanvas, centerX - circleWidth, height - Math.round(centerY * splitProgressRatio) - centerCircleY);
				circleRightDown.blend(dstCanvas, centerX, Math.round(centerY * splitProgressRatio) - centerCircleY);
			}else if (progressRatio >= SIDE_TO_CENTER_PROGRESS_RATIO && progressRatio < ON_PAUSE_PROGRESS_RATIO
					|| progressRatio >= ON_PAUSE_PROGRESS_RATIO && progressRatio < ON_START_PROGRESS_RATIO
					|| progressRatio >= ON_START_PROGRESS_RATIO) {    
				circleLeftUp.blend(dstCanvas, Math.round(centerX - (centerX * splitProgressRatio)) - circleWidth, centerY - centerCircleY);
				circleRightDown.blend(dstCanvas, Math.round(centerX + (centerX * splitProgressRatio)) , centerY - centerCircleY);
			}
			
		}else if(mDirection.degree == 90){
			if(progressRatio >= CENTER_TO_SIDE_PROGRESS_RATIO && progressRatio < SIDE_TO_CENTER_PROGRESS_RATIO){
				circleLeftUp.blend(dstCanvas, Math.round(centerX * splitProgressRatio) - centerCircleX, centerY - circleHeight);
				circleRightDown.blend(dstCanvas, width - Math.round(centerX * splitProgressRatio) - centerCircleX, centerY);
			}else if (progressRatio >= SIDE_TO_CENTER_PROGRESS_RATIO && progressRatio < ON_PAUSE_PROGRESS_RATIO
					|| progressRatio >= ON_PAUSE_PROGRESS_RATIO && progressRatio < ON_START_PROGRESS_RATIO
					|| progressRatio >= ON_START_PROGRESS_RATIO) {
				circleLeftUp.blend(dstCanvas, centerX - centerCircleX, Math.round(centerY - (centerY * splitProgressRatio) - circleHeight));
				circleRightDown.blend(dstCanvas, centerX - centerCircleX, Math.round(centerY + (centerY * splitProgressRatio)) );
			}
			
		}else if(mDirection.degree == 45){  // ' \ '
			if(progressRatio >= CENTER_TO_SIDE_PROGRESS_RATIO && progressRatio < SIDE_TO_CENTER_PROGRESS_RATIO){
				circleLeftUp.blend(dstCanvas, centerX - centerY + lineOffSet - aThirdCircleX, lineOffSet - aThirdCircleX*2 );
				circleRightDown.blend(dstCanvas, centerX + centerY - Math.round(centerY * splitProgressRatio) - aThirdCircleX*2, height - lineOffSet - aThirdCircleX);
				
			}else if (progressRatio >= SIDE_TO_CENTER_PROGRESS_RATIO && progressRatio < ON_PAUSE_PROGRESS_RATIO
					|| progressRatio >= ON_PAUSE_PROGRESS_RATIO && progressRatio < ON_START_PROGRESS_RATIO
					|| progressRatio >= ON_START_PROGRESS_RATIO) {

				//사선에 대한 반원 위치 계산 수정 
				diagonalSymbolOffSet  = (rightLineX1 - (centerX - centerY))/2;
				
				circleLeftUp.blend(dstCanvas, centerX + diagonalSymbolOffSet - aThirdCircleX, centerY - diagonalSymbolOffSet - aThirdCircleX*2);
				circleRightDown.blend(dstCanvas, centerX - diagonalSymbolOffSet - aThirdCircleX*2, centerY + diagonalSymbolOffSet - aThirdCircleX);
			}
			
		}else{  // 135'     ' / ' 
			if(progressRatio >= CENTER_TO_SIDE_PROGRESS_RATIO && progressRatio < SIDE_TO_CENTER_PROGRESS_RATIO){
				circleLeftUp.blend(dstCanvas, centerX - centerY+ lineOffSet - aThirdCircleX*2, height - lineOffSet - aThirdCircleX*2);
				circleRightDown.blend(dstCanvas, centerX + centerY - lineOffSet - aThirdCircleX, lineOffSet - aThirdCircleX);
			}else if (progressRatio >= SIDE_TO_CENTER_PROGRESS_RATIO && progressRatio < ON_PAUSE_PROGRESS_RATIO 
					|| progressRatio >= ON_PAUSE_PROGRESS_RATIO && progressRatio < ON_START_PROGRESS_RATIO
					|| progressRatio >= ON_START_PROGRESS_RATIO){
				diagonalSymbolOffSet  = ((centerX + centerY) - leftLineX1)/2;
				
				circleLeftUp.blend(dstCanvas, centerX  - diagonalSymbolOffSet - aThirdCircleX*2, centerY - diagonalSymbolOffSet - aThirdCircleX*2);
				circleRightDown.blend(dstCanvas, centerX + diagonalSymbolOffSet - aThirdCircleX, centerY + diagonalSymbolOffSet - aThirdCircleX );
			}
		}
	}

	// 사방 '+' 그리기
	private void drawPlusSymbol(PixelCanvas dstCanvas, int lineXLength, int lineYLength, int width, int height) {
		
		int lineSize = mLineSize -1;
		int centerLineY = lineYLength / 4;
		// 좌상단 
		dstCanvas.drawLine(PLUS_LINE_COLOR, lineXLength - centerLineY, lineYLength , lineXLength + centerLineY, lineYLength, lineSize);
		dstCanvas.drawLine(PLUS_LINE_COLOR, lineXLength, lineYLength - centerLineY, lineXLength, lineYLength + centerLineY, lineSize);
		// 좌 중간
		dstCanvas.drawLine(PLUS_LINE_COLOR, lineXLength , height/2, lineXLength + centerLineY, height/2, lineSize);
		dstCanvas.drawLine(PLUS_LINE_COLOR, lineXLength, height/2 - centerLineY, lineXLength, height/2 + centerLineY, lineSize);
		// 좌하단
		dstCanvas.drawLine(PLUS_LINE_COLOR, lineXLength - centerLineY, height - lineYLength, lineXLength + centerLineY, height - lineYLength, lineSize);
		dstCanvas.drawLine(PLUS_LINE_COLOR, lineXLength, height- lineYLength - centerLineY, lineXLength, height - lineYLength + centerLineY, lineSize);
		
		// 우상단 
		dstCanvas.drawLine(PLUS_LINE_COLOR, width - lineXLength - centerLineY , lineYLength, width - lineXLength + centerLineY, lineYLength, lineSize);
		dstCanvas.drawLine(PLUS_LINE_COLOR, width - lineXLength, lineYLength - centerLineY, width - lineXLength, lineYLength + centerLineY, lineSize);
		// 우 중간
		dstCanvas.drawLine(PLUS_LINE_COLOR, width - lineXLength - centerLineY, height/2, width - lineXLength, height/2, lineSize);
		dstCanvas.drawLine(PLUS_LINE_COLOR, width - lineXLength, height/2 - centerLineY, width - lineXLength , height/2 + centerLineY, lineSize);
		// 우하단
		dstCanvas.drawLine(PLUS_LINE_COLOR, width - lineXLength - centerLineY, height - lineYLength, width - lineXLength + centerLineY, height - lineYLength, lineSize);
		dstCanvas.drawLine(PLUS_LINE_COLOR, width - lineXLength, height - lineYLength - centerLineY, width - lineXLength , height - lineYLength + centerLineY, lineSize);
	}

	@Override
	Size[] getCanvasRequirement() {
		if(mIsWhiteLineSplit){
			return new Size[]{mSymbolSize, mSymbolSize};
		}else{
			return DO_NOT_NEED_CANVAS;
		}
	}

	void setDirection(Direction direction) {
		Precondition.checkNotNull(direction);
		mDirection = direction;
	}
	
	/**
	 * isWhiteLineSplit : false   >> 기존 split 사용
	 * isWhiteLineSplit : true   >> white line split 사용
	 */
	void setWhileLineSplit(boolean isWhiteLineSplit) {
		mIsWhiteLineSplit = isWhiteLineSplit;
	}
	
	public boolean getWhileLineSplit(){
		return mIsWhiteLineSplit;
	}
	
	
	
	/**
	 * 갈라지는 방향을 반환합니다.
	 */
	public Direction getDirection() {
		return mDirection;
	}

	void setLineColor(int lineColor) {
		mLineColor = lineColor;
	}

	public int getLineColor() {
		return mLineColor;
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link SplitTransition}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Transition.Editor<SplitTransition, Editor> {

		private Editor(SplitTransition splitTransition) {
			super(splitTransition);
		}

		/**
		 * 갈라지는 방향을 설정합니다.
		 * 
		 * @param direction
		 *            설정할 {@code Direction} 객체.
		 */
		public Editor setDirection(Direction direction) {
			getObject().setDirection(direction);
			return this;
		}

		public Editor setLineColor(int lineColor) {
			getObject().setLineColor(lineColor);
			return this;
		}
		
		public void setWhileLineSplit(boolean enableWhileLineSplit){
			getObject().setWhileLineSplit(enableWhileLineSplit);
		}
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * 갈라지는 방향을 분류하기 위한 열거형.
	 * 
	 * @see #HORIZONTAL
	 * @see #VERTICAL
	 * @see #DIAGONAL_LEFT_TOP
	 * @see #DIAGONAL_LEFT_BOTTOM
	 */
	public static enum Direction {

		/**
		 * 전 {@link Scene}의 이미지가 수평으로 갈라짐을 의미합니다.
		 */
		HORIZONTAL(0),

		/**
		 * 전 {@link Scene}의 이미지가 수직으로 갈라짐을 의미합니다.
		 */
		VERTICAL(90),

		/**
		 * 전 {@link Scene}의 이미지가 좌상단에서 우하단으로 이어지는 사선으로 갈라짐을 의미합니다.
		 */
		DIAGONAL_LEFT_TOP(45),

		/**
		 * 전 {@link Scene}의 이미지가 좌하단에서 우상단으로 이어지는 사선으로 갈라짐을 의미합니다.
		 */
		DIAGONAL_LEFT_BOTTOM(135);

		final int degree;

		private Direction(int degree) {
			this.degree = degree;
		}
	}
}