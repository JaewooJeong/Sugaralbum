package com.kiwiple.multimedia.canvas;

import java.io.IOException;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * 안드로이드 응용 프로그램 패키지에 포함된 {@code res/drawable}의 이미지 파일을 출력하는 클래스.
 */
public final class DrawableScene extends Scene {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "drawable_scene";

	public static final String JSON_NAME_IMAGE_RESOURCE = ImageResource.DEFAULT_JSON_NAME;
	public static final String JSON_NAME_BACKGROUND_COLOR = "background_color";

	public static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK;

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private DrawableImageResource mImageResource;

	@CacheCode
	private int mBackgroundColor;

	// // // // // Constructor.
	// // // // //
	{
		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
	}

	DrawableScene(Region parent) {
		super(parent);
	}

	DrawableScene(MultiLayerScene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas dstCanvas) {

		dstCanvas.clear(mBackgroundColor);

		Size imageSize = mImageResource.measureSize(getResolution());
		int width = getWidth();
		int height = getHeight();
		int dstX = Math.round(width / 2.0f - imageSize.width / 2.0f);
		int dstY = Math.round(height / 2.0f - imageSize.height / 2.0f);

		getCanvas(0).blend(dstCanvas, dstX, dstY);
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_BACKGROUND_COLOR, mBackgroundColor);
		jsonObject.putOpt(JSON_NAME_IMAGE_RESOURCE, mImageResource);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		JsonObject imageResourceJsonObject = jsonObject.optJSONObject(JSON_NAME_IMAGE_RESOURCE);
		if (imageResourceJsonObject != null) {
			setDrawable((DrawableImageResource) ImageResource.createFromJsonObject(getResources(), imageResourceJsonObject));
		}
		setBackgroundColor(jsonObject.getInt(JSON_NAME_BACKGROUND_COLOR));
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		checkValidity(mImageResource != null, "You must invoke setDrawableId().");
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
			Resolution resolution = getResolution();
			bitmap = mImageResource.createBitmap(resolution);
		} else {
			Precondition.assureUnreachable();
		}
		return bitmap;
	}

	@Override
	public int getCacheCount() {
		return 1;
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { mImageResource.measureSize(getResolution()) };
	}

	void setDrawable(DrawableImageResource drawable) {
		mImageResource = drawable;
	}

	public DrawableImageResource getDrawable() {
		return mImageResource;
	}

	void setBackgroundColor(int backgroundColor) {
		mBackgroundColor = backgroundColor;
	}

	/**
	 * 배경 색상을 반환합니다.
	 * 
	 * @return {@code #AARRGGBB} 형식의 출력 색상.
	 */
	public int getBackgroundColor() {
		return mBackgroundColor;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link DrawableScene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<DrawableScene, Editor> {

		private Editor(DrawableScene drawableScene) {
			super(drawableScene);
		}

		public Editor setDrawable(DrawableImageResource drawable) {
			getObject().setDrawable(drawable);
			return this;
		}

		/**
		 * 배경 색상을 설정합니다.
		 * 
		 * @param backgroundColor
		 *            {@code #AARRGGBB} 형식의 출력 색상.
		 */
		public Editor setBackgroundColor(int backgroundColor) {
			getObject().setBackgroundColor(backgroundColor);
			return this;
		}
	}
}
