package com.kiwiple.multimedia.canvas;

import java.io.IOException;

import org.json.JSONException;

import android.graphics.Bitmap;

import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.annotation.RiValue;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * {@link Scene}이 생성한 이미지에 지정된 이미지를 덧붙이는 효과를 적용하는 클래스.
 */
public final class OverlayEffect extends Effect {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "overlay_effect";

	public static final String JSON_NAME_IMAGE_RESOURCE = ImageResource.DEFAULT_JSON_NAME;
	public static final String JSON_NAME_COORDINATE_X = "coordinate_x";
	public static final String JSON_NAME_COORDINATE_Y = "coordinate_y";

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private ImageResource mImageResource;

	@RiValue
	private float mCoordinateX;
	@RiValue
	private float mCoordinateY;

	// // // // // Constructor.
	// // // // //
	OverlayEffect(Scene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_COORDINATE_X, mCoordinateX);
		jsonObject.put(JSON_NAME_COORDINATE_Y, mCoordinateY);
		jsonObject.putOpt(JSON_NAME_IMAGE_RESOURCE, mImageResource);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		if (!jsonObject.isNull(JSON_NAME_IMAGE_RESOURCE)) {
			mImageResource = ImageResource.createFromJsonObject(getResources(), jsonObject.getJSONObject(JSON_NAME_IMAGE_RESOURCE));
		}
		setCoordinate(jsonObject.getInt(JSON_NAME_COORDINATE_X), jsonObject.getInt(JSON_NAME_COORDINATE_Y));
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		checkValidity(mImageResource != null, "You must invoke setImage[\"File\"|\"AssetFile\"|\"Drawable\"|\"InternalDrawable\"]()");
		checkValidity(mImageResource.validate(), String.format("ImageResource(%s) is invalid.", mImageResource));
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {
		PixelCanvas srcCanvas = getCanvas(0);
		srcCanvas.blend(dstCanvas, Math.round(mCoordinateX), Math.round(mCoordinateY));
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
		Resolution resolution = getResolution();
		return mImageResource.createBitmap(resolution);
	}

	@Override
	public int getCacheCount() {
		return (mImageResource != null) ? 1 : 0;
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { mImageResource.measureSize(getResolution()) };
	}

	void setImageFile(String filePath, Resolution resolution) {
		mImageResource = ImageResource.createFromFile(filePath, resolution);
	}

	void setImageAssetFile(String assetFilePath, Resolution resolution) {
		mImageResource = ImageResource.createFromAsset(assetFilePath, getResources(), resolution);
	}

	void setImageDrawable(int drawableId, Resolution resolution) {
		mImageResource = ImageResource.createFromDrawable(drawableId, getResources(), resolution);
	}

	/**
	 * 효과 적용에 사용하는 이미지 자원의 형태를 반환합니다.
	 * 
	 * @see ResourceType
	 */
	public ResourceType getImageResourceType() {
		return mImageResource.getResourceType();
	}

	/**
	 * 효과 적용에 사용하는 이미지 파일의 경로를 반환합니다.
	 * 
	 * @return 이미지 파일 경로 혹은 파일 경로로써 이미지를 설정하지 않은 경우에는 {@code null}.
	 */
	public String getFilePath() {

		if (mImageResource instanceof FileImageResource) {
			return ((FileImageResource) mImageResource).getFilePath();
		} else if (mImageResource instanceof AssetImageResource) {
			return ((AssetImageResource) mImageResource).getFilePath();
		} else {
			return null;
		}
	}

	/**
	 * 효과 적용에 사용하는 이미지 파일의 아이디를 반환합니다.
	 * 
	 * @return 이미지 파일의 아이디 혹은 아이디로써 이미지를 설정하지 않은 경우에는 {@code 0}.
	 */
	public int getDrawaableId() {

		if (mImageResource instanceof DrawableImageResource) {
			return ((DrawableImageResource) mImageResource).getDrawableId();
		} else {
			return 0;
		}
	}

	void setCoordinate(float x, float y) {
		mCoordinateX = x;
		mCoordinateY = y;
	}

	/**
	 * 이미지를 출력하는 가로축 좌표를 반환합니다.
	 */
	public float getCoordinateX() {
		return mCoordinateX;
	}

	/**
	 * 이미지를 출력하는 세로축 좌표를 반환합니다.
	 */
	public float getCoordinateY() {
		return mCoordinateY;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link OverlayEffect}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Effect.Editor<OverlayEffect, Editor> {

		private Editor(OverlayEffect OverlayEffect) {
			super(OverlayEffect);
		}

		/**
		 * 이미지 파일의 절대 경로로써 효과 적용에 사용할 이미지를 설정합니다.
		 * 
		 * @param imageFilePath
		 *            이미지 파일의 절대 경로.
		 * @param resolution
		 *            이미지 파일의 기준 해상도.
		 * @see Resolution
		 */
		public Editor setImageFile(String imageFilePath, Resolution resolution) {
			getObject().setImageFile(imageFilePath, resolution);
			return this;
		}

		/**
		 * 안드로이드 응용 프로그램 패키지에 포함된 {@code assets/}의 이미지 파일 경로로써 효과 적용에 사용할 이미지를 설정합니다.
		 * 
		 * @param imageFilePath
		 *            Asset 이미지 파일의 경로.
		 * @param resolution
		 *            이미지 파일의 기준 해상도.
		 * @see Resolution
		 */
		public Editor setImageAssetFile(String imageFilePath, Resolution resolution) {
			getObject().setImageAssetFile(imageFilePath, resolution);
			return this;
		}

		/**
		 * 안드로이드 응용 프로그램 패키지에 포함된 {@code res/drawable}의 이미지 파일 아이디로써 효과 적용에 사용할 이미지를 설정합니다.
		 * 
		 * @param drawableId
		 *            Drawable 이미지 파일의 아이디.
		 * @param resolution
		 *            이미지 파일의 기준 해상도.
		 * @see Resolution
		 */
		public Editor setImageDrawable(int drawableId, Resolution resolution) {
			getObject().setImageDrawable(drawableId, resolution);
			return this;
		}

		/**
		 * 이미지를 출력할 좌상단 기준의 좌표를 설정합니다.
		 * 
		 * @param x
		 *            가로축 좌표.
		 * @param y
		 *            세로축 좌표.
		 */
		public Editor setCoordinate(float x, float y) {
			getObject().setCoordinate(x, y);
			return this;
		}
	}
}
