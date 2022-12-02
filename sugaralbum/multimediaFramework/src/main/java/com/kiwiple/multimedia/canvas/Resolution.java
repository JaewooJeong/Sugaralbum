package com.kiwiple.multimedia.canvas;

import java.io.Serializable;

import org.json.JSONException;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.multimedia.util.StringUtils;

/**
 * 해상도 정보를 담기 위한 클래스.
 * 
 * @see #NHD
 * @see #HD
 * @see #FHD
 */
public final class Resolution implements Serializable, IJsonConvertible {

	// // // // // Static variable.
	// // // // //
	private static final long serialVersionUID = -157586681509155289L;

	public static final String DEFAULT_JSON_NAME = "resolution";
	public static final String JSON_NAME_NAME = "name";
	public static final String JSON_NAME_WIDTH = "width";
	public static final String JSON_NAME_HEIGHT = "height";
	public static final String JSON_NAME_MAGNIFICATION = "magnification";
	public static final String JSON_NAME_ASPECT_RATIO = "aspect_ratio";

	/**
	 * 서로 다른 두 객체의 호환 가능 여부를 판단하기 위한 기준값.
	 */
	public static final float ASPECT_RATIO_ERROR_TOLERANCE = 0.0000005f;

	/**
	 * 16:9 비율의 High-definition 표준 해상도의 일종. 640x360 크기.
	 */
	public static final Resolution NHD = new Resolution("nHD", new Size(640, 360));

	/**
	 * 16:9 비율의 High-definition 표준 해상도의 일종. 1280x720 크기.
	 */
	public static final Resolution HD = new Resolution("HD", NHD, 2);

	/**
	 * 16:9 비율의 High-definition 표준 해상도의 일종. 1920x1080 크기.
	 */
	public static final Resolution FHD = new Resolution("FHD", NHD, 3);

	// // // // // Member variable.
	// // // // //
	/**
	 * 해상도의 영문 이름.
	 */
	public final String name;

	/**
	 * 해상도의 가로 픽셀 크기.
	 */
	public final int width;

	/**
	 * 해상도의 세로 픽셀 크기.
	 */
	public final int height;

	/**
	 * 객체의 가로 및 세로 크기가 기반 해상도({@link #baseResolution})의 몇 배율에 해당하는지 정의한 값.
	 * <p />
	 * 객체 스스로가 기반 해상도라면 이 값은 {@code 1.0f}으로 고정됩니다.
	 */
	public final float magnification;

	/**
	 * 해상도의 화면 비율. {@code width}를 {@code height}로 나눈 값으로 정의.
	 */
	public final float aspectRatio;

	/**
	 * 기반 해상도 객체. 해상도가 다른 해상도 객체를 기반으로 생성되었을 때에는 기반 해상도를 가리키며, 그렇지 않은 경우에는 자기 자신을 가리킵니다.
	 */
	public final Resolution baseResolution;

	/**
	 * Do not use if you don't know what it means.
	 */
	boolean bypassCompatibility;

	// // // // // Static method.
	// // // // //
	static Resolution createFrom(JsonObject jsonObject) throws JSONException {

		if (jsonObject == null) {
			return null;
		}
		String name = jsonObject.getString(JSON_NAME_NAME);
		int width = jsonObject.getInt(JSON_NAME_WIDTH);
		int height = jsonObject.getInt(JSON_NAME_HEIGHT);
		int magnification = jsonObject.getInt(JSON_NAME_MAGNIFICATION);

		return new Resolution(name, null, new Size(width, height), magnification);
	}

	// // // // // Constructor.
	// // // // //
	Resolution(String name, Resolution baseResolution, Size size, int magnification) {
		Precondition.checkNotNull(name, size);
		Precondition.checkArgument(size.product() != 0, "size must be valid.");
		Precondition.checkOnlyPositive(magnification);

		this.name = name;
		this.width = size.width;
		this.height = size.height;
		this.magnification = magnification;
		this.aspectRatio = (float) width / (float) height;
		this.baseResolution = baseResolution == null ? this : baseResolution;
	}

	/**
	 * 기반 해상도 크기의 특정 배율에 해당하는 {@code Resolution}을 생성합니다.
	 * 
	 * @param name
	 *            해상도의 이름.
	 * @param baseResolution
	 *            기반 해상도.
	 * @param magnification
	 *            크기 배율.
	 */
	public Resolution(String name, Resolution baseResolution, int magnification) {
		this(name, baseResolution, new Size(baseResolution.width * magnification, baseResolution.height * magnification), magnification);
	}

	/**
	 * 스스로를 기반 해상도로 삼는 {@code Resolution}을 지정한 크기로 생성합니다.
	 * 
	 * @param name
	 *            해상도의 이름.
	 * @param size
	 *            크기 정보.
	 */
	public Resolution(String name, Size size) {
		this(name, null, size, 1);
	}

	// // // // // Method.
	// // // // //
	/**
	 * 해상도의 크기 정보를 반환합니다.
	 */
	public Size getSize() {
		return new Size(width, height);
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = new JsonObject();

		jsonObject.put(JSON_NAME_NAME, name);
		jsonObject.put(JSON_NAME_WIDTH, width);
		jsonObject.put(JSON_NAME_HEIGHT, height);
		jsonObject.put(JSON_NAME_MAGNIFICATION, (int) magnification);
		jsonObject.put(JSON_NAME_ASPECT_RATIO, aspectRatio);

		return jsonObject;
	}

	/**
	 * 주어진 {@code Resolution}과의 호환 가능 여부를 반환합니다.
	 * <p />
	 * 단, 여기에서 호환 가능하다는 것은 대상이 되는 두 객체의 {@link #aspectRatio}의 차이가
	 * {@value #ASPECT_RATIO_ERROR_TOLERANCE}보다 작다는 것을 의미하며, 그렇지 않은 경우에는 호환이 불가능한 상태라고 정의합니다.
	 * 
	 * @param other
	 *            대상 {@code Resolution} 객체.
	 * @see #ASPECT_RATIO_ERROR_TOLERANCE
	 */
	public boolean isCompatibleWith(Resolution other) {
		Precondition.checkNotNull(other);

		if (bypassCompatibility || other.bypassCompatibility) {
			return true;
		}
		return isCompatibleWith(other.aspectRatio);
	}

	/**
	 * 주어진 화면 비율과의 호환 가능 여부를 반환합니다.
	 * <p />
	 * 단, 여기에서 호환 가능하다는 것은 {@code this.aspectRatio}와 인자로 전달된 aspectRatio의 차이가
	 * {@value #ASPECT_RATIO_ERROR_TOLERANCE}보다 작다는 것을 의미하며, 그렇지 않은 경우에는 호환이 불가능한 상태라고 정의합니다.
	 * 
	 * @param aspectRatio
	 *            대상 화면 비율 값.
	 * @see #ASPECT_RATIO_ERROR_TOLERANCE
	 */
	public boolean isCompatibleWith(float aspectRatio) {
		Precondition.checkOnlyPositive(aspectRatio);

		if (bypassCompatibility) {
			return true;
		}
		return Math.abs(this.aspectRatio - aspectRatio) < ASPECT_RATIO_ERROR_TOLERANCE;
	}

	public boolean isLogicallyEquals(Resolution other) {
		return getSize().equals(other) && magnification == other.magnification;
	}

	@Override
	public String toString() {
		return StringUtils.format("%s(%dx%d:%f)", name, width, height, magnification);
	}
}