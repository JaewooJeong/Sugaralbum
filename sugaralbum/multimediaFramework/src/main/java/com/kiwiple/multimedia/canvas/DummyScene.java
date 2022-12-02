package com.kiwiple.multimedia.canvas;

import java.io.IOException;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.ImageUtils;
import com.kiwiple.multimedia.util.Size;

/**
 * DummyScene.
 * 
 */
public final class DummyScene extends Scene {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "dummy_scene";

	public static final String JSON_NAME_BACKGROUND_IMAGE_FILE_PATH = "background_image_file_path";
	public static final String JSON_NAME_BACKGROUND_VIEWPORT = "background_viewport";

	public static final String JSON_NAME_VIDEO_FRAME_POSITION = "video_frame_position";

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private String mBackgroundImageFilePath;
	@CacheCode
	private Viewport mBackgroundViewport;

	@CacheCode
	private long mVideoFramePositionMs = 0;

	// // // // // Constructor.
	// // // // //
	DummyScene(Region parent) {
		super(parent);
	}

	DummyScene(MultiLayerScene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas dstCanvas) {
		getCanvas(0).copy(dstCanvas);
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.putOpt(JSON_NAME_BACKGROUND_IMAGE_FILE_PATH, mBackgroundImageFilePath);
		if (mBackgroundViewport != null) {
			jsonObject.putOpt(JSON_NAME_BACKGROUND_VIEWPORT, mBackgroundViewport.toJsonObject());
		}

		jsonObject.put(JSON_NAME_VIDEO_FRAME_POSITION, mVideoFramePositionMs);
		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		String backgroundFilePath = jsonObject.optString(JSON_NAME_BACKGROUND_IMAGE_FILE_PATH);
		setBackgroundFilePath(backgroundFilePath);

		JsonObject viewportJsonObject = jsonObject.optJSONObject(JSON_NAME_BACKGROUND_VIEWPORT);
		if (viewportJsonObject != null) {
			setBackgroundViewport(new Viewport(viewportJsonObject));
		}

		int videoFramePositionMs = jsonObject.getInt(JSON_NAME_VIDEO_FRAME_POSITION);
		setVideoFramePosition(videoFramePositionMs);
	}

	@Override
	void prepareCanvasWithCache() throws IOException {
		getCacheManager().decodeImageCache(getCacheCodeChunk(0), getCanvas(0));
	}

	@Override
	void prepareCanvasWithoutCache() throws IOException {
		PixelExtractUtils.extractARGB(createCacheAsBitmap(0), getCanvas(0), true);
	}

	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {

		Bitmap bitmap = null;

		if (index == 0) {
			int targetWidth = getWidth();
			int targetHeight = getHeight();

			bitmap = Bitmap.createBitmap(targetWidth, targetHeight, BITMAP_CONFIG);

			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.BLACK);

			if (mBackgroundImageFilePath != null && !mBackgroundImageFilePath.isEmpty()) {
				Bitmap backgroundBitmap = ImageUtils.getImage(mBackgroundImageFilePath, getResolution(), mVideoFramePositionMs);

				if (backgroundBitmap != null) {
					Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
					paint.setAlpha(255);

					if (mBackgroundViewport == null) {
						float left = (targetWidth - backgroundBitmap.getWidth()) / 2.0f;
						float top = (targetHeight - backgroundBitmap.getHeight()) / 2.0f;
						canvas.drawBitmap(backgroundBitmap, left, top, paint);

					} else {
						int imageWidth = backgroundBitmap.getWidth();
						int imageHeight = backgroundBitmap.getHeight();
						float left = imageWidth * mBackgroundViewport.left;
						float top = imageHeight * mBackgroundViewport.top;
						float scale = targetWidth / (imageWidth * mBackgroundViewport.width());

						Matrix matrix = new Matrix();
						matrix.postTranslate(-left, -top);
						matrix.postScale(scale, scale);

						canvas.drawBitmap(backgroundBitmap, matrix, paint);
					}
					backgroundBitmap.recycle();
				}
			}
		}
		return bitmap;
	}

	@Override
	public int getCacheCount() {
		return 1;
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { getSize() }; // FIXME: need to optimize.
	}

	void setBackgroundFilePath(String imageFilePath) {
		mBackgroundImageFilePath = imageFilePath;
	}

	public String getBackgroundFilePath() {
		return mBackgroundImageFilePath;
	}

	void setBackgroundViewport(Viewport viewport) {
		mBackgroundViewport = viewport;
	}

	public Viewport getBackgroundViewport() {
		return mBackgroundViewport;
	}

	void setVideoFramePosition(long framePositionMs) {
		Precondition.checkNotNegative(framePositionMs);
		mVideoFramePositionMs = framePositionMs;
	}

	public long getVideoFramePosition() {
		return mVideoFramePositionMs;
	}

	// // // // // Inner class.
	// // // // //
	public static final class Editor extends Scene.Editor<DummyScene, Editor> {

		private Editor(DummyScene dummyScene) {
			super(dummyScene);
		}

		public Editor setBackgroundFilePath(String imageFilePath) {
			getObject().setBackgroundFilePath(imageFilePath);
			return this;
		}

		public Editor setBackgroundViewport(Viewport viewport) {
			getObject().setBackgroundViewport(viewport);
			return this;
		}

		public Editor setVideoFramePosition(long framePositionMs) {
			getObject().setVideoFramePosition(framePositionMs);
			return this;
		}
	}
}
