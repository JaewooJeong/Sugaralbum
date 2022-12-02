package com.kiwiple.multimedia.canvas.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;

import android.graphics.Color;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.canvas.ICacheCode;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * TextElement.
 * 
 */
public final class TextElement implements ICacheCode, IJsonConvertible, Cloneable {

	// // // // // Static variable.
	// // // // //
	/**
	 * 어떠한 요소도 포함하지 않으며, 변경 불가능한 {@link List} 객체입니다.
	 * 
	 * @see Collections#unmodifiableList(List)
	 */
	public static final List<TextElement> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<TextElement>(0));

	public static final String JSON_NAME_TEXT = "text";
	public static final String JSON_NAME_SIZE = "size";
	public static final String JSON_NAME_COLOR = "color";

	// // // // // Member variable.
	// // // // //
	private String mText;
	private float mSize;
	private int mColor;

	// // // // // Constructor.
	// // // // //
	public TextElement(String text, float size, int color) {

		setText(text);
		setSize(size);
		setColor(color);
	}

	public TextElement(TextElement other) {
		this(other.mText, other.mSize, other.mColor);
	}

	public TextElement(JsonObject jsonObject) throws JSONException {

		setText(jsonObject.getString(JSON_NAME_TEXT));
		setSize(jsonObject.getFloat(JSON_NAME_SIZE));
		setColor(jsonObject.getInt(JSON_NAME_COLOR));
	}

	// // // // // Method.
	// // // // //
	public void setText(String text) {
		Precondition.checkNotNull(text);
		mText = text;
	}

	public String getText() {
		return mText;
	}

	public void setSize(float size) {
		Precondition.checkOnlyPositive(size);
		mSize = size;
	}

	public float getSize() {
		return mSize;
	}

	public void setColor(int color) {
		Precondition.checkArgument(color != Color.TRANSPARENT, "color must not be transparent.");
		mColor = color;
	}

	public int getColor() {
		return mColor;
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = new JsonObject();
		jsonObject.put(JSON_NAME_TEXT, mText);
		jsonObject.put(JSON_NAME_SIZE, mSize);
		jsonObject.put(JSON_NAME_COLOR, mColor);

		return jsonObject;
	}

	@Override
	public int createCacheCode() {
		return (mText + mSize + mColor).hashCode();
	}

	@Override
	public Object clone() {
		return new TextElement(this);
	}
}
