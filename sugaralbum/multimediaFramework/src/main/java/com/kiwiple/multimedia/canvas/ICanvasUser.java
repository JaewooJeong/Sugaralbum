package com.kiwiple.multimedia.canvas;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.kiwiple.multimedia.util.Size;

/**
 * 주어진 환경에 따라 {@link PixelCanvas}에 그리기 기능을 수행하는 객체를 구현하기 위한 기초 인터페이스.
 */
public interface ICanvasUser {

	public static final CanvasUserEnvironment environment = CanvasUserEnvironment.create();

	static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

	static final int COLOR_SYMBOL_INVALID = Color.RED;
	static final int COLOR_SYMBOL_INVALID_SCENE = Color.BLACK;
	static final int COLOR_SYMBOL_INVALID_TRANSITION = Color.WHITE;
	static final int COLOR_SYMBOL_INVALID_EFFECT = Color.GREEN;
	static final int COLOR_SYMBOL_INVALID_SCALER = Color.BLUE;

	/**
	 * 출력 기능이 유지되는 시간 축의 전체 길이를 ms 단위로써 반환합니다.
	 */
	public abstract int getDuration();

	/**
	 * 출력할 이미지의 시간 위치를 ms 단위로써 반환합니다.
	 */
	public abstract int getPosition();

	/**
	 * 가로 크기 즉, 가로 픽셀의 개수를 반환합니다.
	 */
	public abstract int getWidth();

	/**
	 * 세로 크기 즉, 세로 픽셀의 개수를 반환합니다.
	 */
	public abstract int getHeight();

	/**
	 * 시간 축이 진행된 정도를 비율로써 반환합니다.
	 * <p />
	 * 비율은 현재 시간 위치를 전체 시간 길이로 나눈 값으로 계산하며, 출력할 이미지의 시간 위치가 시작점일 때에는 {@code 0.0f}, 끝점일 때에는
	 * {@code 1.0f}를 반환합니다.
	 * 
	 * @return {@code 0.0f}에서 {@code 1.0f}까지의 값을 가지는 비율.
	 */
	public abstract float getProgressRatio();

	/**
	 * 크기 정보를 {@link Size}로써 반환합니다.
	 */
	public abstract Size getSize();
}
