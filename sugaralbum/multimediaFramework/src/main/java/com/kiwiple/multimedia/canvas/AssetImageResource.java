package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.io.InputStream;

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
 * AssetImageResource.
 */
public class AssetImageResource extends ImageResource {

	// // // // // Member variable.
	// // // // //
	private final Resources mResources;

	private final String mFilePath;
	private final Size mSize;
	private final int mOrientation;

	// // // // // Static method.
	// // // // //
	private static Size measureImageSize(Resources resources, String filePath) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		try {
			InputStream iStream = resources.getAssets().open(filePath);
			BitmapFactory.decodeStream(iStream, null, options);
			iStream.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return new Size(options.outWidth, options.outHeight);
	}

	// // // // // Constructor.
	// // // // //
	AssetImageResource(String filePath, Resources resources, ScaleType scaleType, Resolution resolution) {
		super(ResourceType.ANDROID_ASSET, scaleType, resolution);
		Precondition.checkNotNull(resources);

		mResources = resources;
		mFilePath = filePath;
		mSize = measureImageSize(resources, filePath);
		mOrientation = 0;
	}

	// // // // // Method.
	// // // // //
	@Override
	Bitmap decodeResource(Options options) throws IOException {

		InputStream iStream = mResources.getAssets().open(mFilePath);
		Bitmap bitmap = BitmapFactory.decodeStream(iStream, null, options);
		iStream.close();

		return bitmap;
	}

	@Override
	boolean validate() {
		try {
			measureImageSize(mResources, mFilePath);
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_FILE_PATH, mFilePath);

		return jsonObject;
	}

	@Override
	public int createCacheCode() {
		return super.createCacheCode() ^ mFilePath.hashCode();
	}

	@Override
	Size getSize() {
		return mSize;
	}

	@Override
	int getOrientation() {
		return mOrientation;
	}

	public String getFilePath() {
		return mFilePath;
	}

	@Override
	public String toString() {
		return StringUtils.format("[%s] file path : %s", getClass().getSimpleName(), mFilePath);
	}
}
