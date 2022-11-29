package com.kiwiple.multimedia.canvas;

import java.io.IOException;

import org.json.JSONException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.multimedia.util.StringUtils;

/**
 * DrawableImageResource.
 */
public class DrawableImageResource extends ImageResource {

	// // // // // Member variable.
	// // // // //
	private final Resources mResources;

	private final String mDrawableFullName;
	private final int mDrawableId;

	private final Size mSize;
	private final int mOrientation;

	// // // // // Static method.
	// // // // //
	private static Size measureImageSize(Resources resources, int resourceId) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resourceId, options);

		return new Size(options.outWidth, options.outHeight);
	}

	// // // // // Constructor.
	// // // // //
	{
		mOrientation = 0;
	}

	DrawableImageResource(int drawableId, Resources resources, ScaleType scaleType, Resolution resolution) {
		super(ResourceType.ANDROID_RESOURCE, scaleType, resolution);
		Precondition.checkNotNull(resources);

		mResources = resources;
		mDrawableFullName = null;
		mDrawableId = drawableId;
		mSize = measureImageSize(resources, drawableId);
	}

	DrawableImageResource(String drawableFullName, Resources resources, ScaleType scaleType, Resolution resolution) {
		super(ResourceType.ANDROID_RESOURCE, scaleType, resolution);
		Precondition.checkNotNull(resources);

		mResources = resources;
		mDrawableFullName = drawableFullName;
		mDrawableId = resources.getIdentifier(drawableFullName, null, null);
		mSize = measureImageSize(resources, mDrawableId);
	}

	// // // // // Method.
	// // // // //
	@Override
	Bitmap decodeResource(Options options) throws IOException {
		return BitmapFactory.decodeResource(mResources, mDrawableId, options);
	}

	@Override
	boolean validate() {
		try {
			measureImageSize(mResources, mDrawableId);
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		if (mDrawableFullName == null) {
			jsonObject.put(JSON_NAME_DRAWABLE_ID, mDrawableId);
		} else {
			jsonObject.put(JSON_NAME_DRAWABLE_FULL_NAME, mDrawableFullName);
		}
		return jsonObject;
	}

	@Override
	public int createCacheCode() {
		return super.createCacheCode() ^ mDrawableId;
	}

	@Override
	Size getSize() {
		return mSize;
	}

	@Override
	int getOrientation() {
		return mOrientation;
	}

	public int getDrawableId() {
		return mDrawableId;
	}

	public String getDrawableFullName() {
		return mDrawableFullName;
	}

	@Override
	public String toString() {
		boolean useId = (mDrawableFullName == null);
		return StringUtils.format("[%s] %s : %s", getClass().getSimpleName(), useId ? "drawable id" : "drawable full name", useId ? mDrawableId : mDrawableFullName);
	}
}