package com.kiwiple.multimedia.canvas.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;

import android.graphics.Rect;
import android.graphics.RectF;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.canvas.ICacheCode;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * Viewport.
 * 
 */
public class Viewport implements Serializable, ICacheCode, IJsonConvertible {

	private static final long serialVersionUID = -189534657255874557L;

	// // // // // Static variable.
	// // // // //
	/**
	 * 어떠한 요소도 포함하지 않으며, 변경 불가능한 {@link List} 객체입니다.
	 * 
	 * @see Collections#unmodifiableList(List)
	 */
	public static final List<Viewport> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<Viewport>(0));

	public static final Viewport FULL_VIEWPORT = new Viewport(0.0f, 0.0f, 1.0f, 1.0f);

	public static final String JSON_NAME_LEFT = "left";
	public static final String JSON_NAME_TOP = "top";
	public static final String JSON_NAME_RIGHT = "right";
	public static final String JSON_NAME_BOTTOM = "bottom";

	// // // // // Member variable.
	// // // // //
	public final float left;
	public final float top;
	public final float right;
	public final float bottom;

	// // // // // Constructor.
	// // // // //
	public Viewport(float left, float top, float right, float bottom) {
		Precondition.checkArgument(left <= right, "left must be less than or equal to right.");
		Precondition.checkArgument(top <= bottom, "top must be less than or equal to bottom.");

		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public Viewport(Viewport other) {
		Precondition.checkNotNull(other);

		this.left = other.left;
		this.top = other.top;
		this.right = other.right;
		this.bottom = other.bottom;
	}

	public Viewport(JsonObject jsonObject) throws JSONException {
		Precondition.checkNotNull(jsonObject);

		left = jsonObject.getFloat(JSON_NAME_LEFT);
		top = jsonObject.getFloat(JSON_NAME_TOP);
		right = jsonObject.getFloat(JSON_NAME_RIGHT);
		bottom = jsonObject.getFloat(JSON_NAME_BOTTOM);
	}

	// // // // // Method.
	// // // // //
	@Override
	public boolean equals(Object o) {

		if (o == null || !(o instanceof Viewport)) {
			return false;
		}
		if (this == o) {
			return true;
		}
		Viewport other = (Viewport) o;
		return left == other.left && top == other.top && right == other.right && bottom == other.bottom;
	}

	@Override
	public int hashCode() {

		int left = Float.floatToIntBits(this.left);
		int top = Integer.reverse(Float.floatToIntBits(this.top));
		int right = Integer.rotateLeft(Float.floatToIntBits(this.left), 7);
		int bottom = Integer.rotateRight(Float.floatToIntBits(this.left), 17);

		return left ^ top ^ right ^ bottom;
	}

	@Override
	public int createCacheCode() {
		return hashCode();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = new JsonObject();

		jsonObject.put(JSON_NAME_LEFT, left);
		jsonObject.put(JSON_NAME_TOP, top);
		jsonObject.put(JSON_NAME_RIGHT, right);
		jsonObject.put(JSON_NAME_BOTTOM, bottom);

		return jsonObject;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder(64);
		builder.append('[').append(left).append(',').append(top).append(']');
		builder.append('-');
		builder.append('[').append(right).append(',').append(bottom).append(']');

		return builder.toString();
	}

	public float width() {
		return right - left;
	}

	public float height() {
		return bottom - top;
	}

	public boolean contains(Viewport other) {
		return (left <= other.left && top <= other.top && right >= other.right && bottom >= other.bottom);
	}

	public Rect asActualSizeRect(Size size) {
		return new Rect(Math.round(left * size.width), Math.round(top * size.height), Math.round(right * size.width), Math.round(bottom * size.height));
	}

	public RectF asActualSizeRectF(Size size) {
		return new RectF(left * size.width, top * size.height, right * size.width, bottom * size.height);
	}
}
