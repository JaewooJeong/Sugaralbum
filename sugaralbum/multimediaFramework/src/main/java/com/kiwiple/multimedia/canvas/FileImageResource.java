package com.kiwiple.multimedia.canvas;

import java.io.IOException;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.ImageUtils;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.multimedia.util.StringUtils;

/**
 * FileImageResource.
 */
public class FileImageResource extends ImageResource {

	// // // // // Static variable.
	// // // // //
	private static final String MIME_TYPE_JPEG = "image/jpeg";

	// // // // // Member variable.
	// // // // //
	private final String mFilePath;
	private final Size mSize;
	private final int mOrientation;

	// // // // // Static method.
	// // // // //
	private static Size measureImageSize(String filePath) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		return new Size(options.outWidth, options.outHeight);
	}

	// // // // // Constructor.
	// // // // //
	FileImageResource(String filePath, ScaleType scaleType, Resolution resolution) {
		super(ResourceType.FILE, scaleType, resolution);
		Precondition.checkFile(filePath).checkExist();

		mFilePath = filePath;
		mSize = measureImageSize(filePath);

		String mimeType = getMimeType(filePath);
		mOrientation = (mimeType != null && mimeType.equals(MIME_TYPE_JPEG) ? ImageUtils.getOrientation(filePath) : 0);
	}

	// // // // // Method.
	// // // // //
	@Override
	Bitmap decodeResource(Options options) throws IOException {
		return BitmapFactory.decodeFile(mFilePath, options);
	}

	@Override
	boolean validate() {
		try {
			measureImageSize(mFilePath);
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
