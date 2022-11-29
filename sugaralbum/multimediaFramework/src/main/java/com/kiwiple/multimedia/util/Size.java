package com.kiwiple.multimedia.util;

import android.graphics.Rect;

import com.kiwiple.debug.Precondition;

/**
 * 픽셀의 개수로 크기 산정이 가능한 2차원 시각 매체의 가로 및 세로의 크기 정보를 담기 위한 클래스.
 */
public class Size {

	// // // // // Static variable.
	// // // // //
	/**
	 * {@link #width}와 {@link #height}가 0으로 지정되어 있는 유일한 객체로, 유효하지 않은 크기 정보를 표현하기 위해 사용할 수 있습니다.
	 */
	public static final Size INVALID_SIZE = new Size();

	// // // // // Member variable.
	// // // // //
	/**
	 * 가로 크기 즉, 가로 픽셀의 개수.
	 */
	public final int width;

	/**
	 * 세로 크기 즉, 세로 픽셀의 개수.
	 */
	public final int height;

	// // // // // Constructor.
	// // // // //
	/**
	 * {@link #INVALID_SIZE}를 생성하기 위한 내부 생성자.
	 */
	private Size() {
		width = 0;
		height = 0;
	}

	/**
	 * 지정된 크기를 지니는 객체를 생성합니다.
	 * 
	 * @param width
	 *            1보다 작지 않은 가로 크기.
	 * @param height
	 *            1보다 작지 않은 세로 크기.
	 * @throws IllegalArgumentException
	 *             width 혹은 height가 1보다 작은 경우.
	 * @see #INVALID_SIZE
	 */
	public Size(int width, int height) {
		Precondition.checkOnlyPositive(width);
		Precondition.checkOnlyPositive(height);

		this.width = width;
		this.height = height;
	}

	public Size(Rect rect) {
		Precondition.checkNotNull(rect);

		this.width = rect.width();
		this.height = rect.height();
	}

	// // // // // Method.
	// // // // //
	@Override
	public boolean equals(Object other) {

		if (this == other) {
			return true;
		}
		if (other == null || !(other instanceof Size)) {
			return false;
		}

		Size otherSize = (Size) other;
		return this.width == otherSize.width && this.height == otherSize.height;
	}

	@Override
	public int hashCode() {
		long forHashing = (long) width << 32 | height;
		return (int) (forHashing ^ forHashing >>> 32);
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}

	/**
	 * 가로 크기 수치와 세로 크기 수치가 반전된 새로운 {@code Size}를 반환합니다.
	 * 
	 * @return {@code new Size(this.height, this.width)}
	 */
	public Size reverse() {
		return new Size(height, width);
	}

	/**
	 * 가로 크기 수치와 세로 크기 수치의 곱을 반환합니다.
	 * 
	 * @return {@code (width * height)}의 결과값.
	 */
	public int product() {
		return width * height;
	}

	/**
	 * 객체가 유효한 크기를 나타내는지의 여부를 반환합니다.<br />
	 * <br />
	 * 크기가 유효하다는 것은 가로 및 세로가 양의 정수임을 의미하며, {@link #INVALID_SIZE}만이 유일하게 {@code false}를 반환합니다.
	 * 
	 * @return 유효한 크기를 나타낼 때 {@code true}.
	 */
	public boolean isValid() {
		return !equals(INVALID_SIZE);
	}
}