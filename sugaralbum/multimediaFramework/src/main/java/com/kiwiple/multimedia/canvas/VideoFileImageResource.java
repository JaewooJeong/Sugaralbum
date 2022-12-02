package com.kiwiple.multimedia.canvas;

import java.io.IOException;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.media.MediaMetadataRetriever;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.multimedia.util.StringUtils;

/**
 * VideoFileImageResource.
 */
public class VideoFileImageResource extends ImageResource {

	// // // // // Member variable.
	// // // // //
	private final String mFilePath;
	private final Size mSize;
	private final int mFramePositionMs;

	// // // // // Static method.
	// // // // /
	private static Size measureImageSize(String filePath) {

		MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
		mRetriever.setDataSource(filePath);
		Bitmap bitmap = mRetriever.getFrameAtTime();
		try {
			mRetriever.release();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		return new Size(width, height);
	}

	// // // // // Constructor.
	// // // // //
	VideoFileImageResource(String filePath, ScaleType scaleType, Resolution resolution, int framePositionMs) {
		super(ResourceType.FILE, scaleType, resolution);
		Precondition.checkFile(filePath).checkExist();
		Precondition.checkNotNegative(framePositionMs);

		mFilePath = filePath;
		mSize = measureImageSize(filePath);
		mFramePositionMs = framePositionMs;
	}

	// // // // // Method.
	// // // // //
	@Override
	Bitmap decodeResource(Options options) throws IOException {

		MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
		mRetriever.setDataSource(mFilePath);
		Bitmap bitmap = mRetriever.getFrameAtTime(mFramePositionMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
		mRetriever.release();

		return bitmap;
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
		jsonObject.put(JSON_NAME_VIDEO_FRAME_POSITION, mFramePositionMs);

		return jsonObject;
	}

	@Override
	public int createCacheCode() {
		return super.createCacheCode() ^ mFilePath.hashCode() + mFramePositionMs;
	}

	@Override
	Size getSize() {
		return mSize;
	}

	@Override
	int getOrientation() {
		return 0;
	}

	public String getFilePath() {
		return mFilePath;
	}

	public int getFramePosition() {
		return mFramePositionMs;
	}

	@Override
	public String toString() {
		return StringUtils.format("[%s] file path : %s", getClass().getSimpleName(), mFilePath);
	}
}
