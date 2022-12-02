package com.kiwiple.multimedia.canvas;

import java.io.IOException;

import org.json.JSONException;

import android.graphics.Bitmap;

import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.annotation.Child;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * {@link MultiLayerScene}이 사용하는 공간의 일부분에 대한 그리기 기능을 대행하는 클래스.
 * <p />
 * {@code LayerScene} 객체를 제어하며 공간을 제공하는 {@code MultiLayerScene}를 부모라 지칭합니다.
 */
public final class LayerScene extends Scene implements IScalable {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "layer_scene";

	public static final String JSON_NAME_FILE_PATH = "file_path";

	public static final String JSON_NAME_SCALER = "scaler";

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private ImageResource mImageResource;

	@Child
	private KenBurnsScaler mScaler;

	@CacheCode
	private FilterApplier mFilterApplier = FilterApplier.INVALID_OBJECT;

	private int mImageId;

	// // // // // Constructor.
	// // // // //
	LayerScene(Region parent) {
		super(parent);
		throw new UnsupportedOperationException();
	}

	LayerScene(MultiLayerScene parent) {
		super(parent);
		mScaler = new KenBurnsScaler(this);
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas dstCanvas) {
		mScaler.draw(getCanvas(0), dstCanvas);
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		checkValidity(mImageResource != null, "You must invoke setImageFilePath().");
		checkValidity(mImageResource.validate(), String.format("ImageResource(%s) is invalid.", mImageResource));
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.remove(JSON_NAME_DURATION);

		jsonObject.putOpt(JSON_NAME_FILTER_ID, mFilterApplier);
		jsonObject.putOpt(JSON_NAME_IMAGE_ID, mImageId, 0);
		jsonObject.putOpt(JSON_NAME_SCALER, mScaler);
		if (mImageResource != null) {
			jsonObject.put(JSON_NAME_FILE_PATH, ((FileImageResource) mImageResource).getFilePath());
		}
		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setImageId(jsonObject.optInt(JSON_NAME_IMAGE_ID, 0));
		setFilterId(jsonObject.optInt(JSON_NAME_FILTER_ID, FilterApplier.INVALID_FILTER_ID));

		if (!jsonObject.isNull(JSON_NAME_FILE_PATH)) {
			setImageFilePath(jsonObject.getString(JSON_NAME_FILE_PATH));
		}
		if (!jsonObject.isNull(JSON_NAME_SCALER)) {
			JsonObject kenBurnsJsonObject = jsonObject.getJSONObject(JSON_NAME_SCALER);
			mScaler = (KenBurnsScaler) CanvasUserFactory.createScaler(kenBurnsJsonObject, this);
		}
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
		Bitmap bitmap = mImageResource.createBitmap(resolution);

		if (mFilterApplier.isVaild()) {
			mFilterApplier.apply(bitmap);
		}
		return bitmap;
	}

	@Override
	public int getCacheCount() {
		return (mImageResource != null ? 1 : 0);
	}

	@Override
	Size[] getCanvasRequirement() {
		Size size = mImageResource.measureSize(getResolution());
		return new Size[] { size };
	}

	void setImageFilePath(String imageFilePath) {
		mImageResource = ImageResource.createFromFile(imageFilePath, ImageResource.ScaleType.BUFFER);
	}

	/**
	 * 이미지를 그릴 때 사용하는 원본 이미지 파일의 절대 경로를 문자열로써 반환합니다.
	 */
	public String getImageFilePath() {
		return ((FileImageResource) mImageResource).getFilePath();
	}

	/**
	 * 이미지를 그릴 때 적용하는 {@link Scaler}를 반환합니다.
	 */
	@Override
	public Scaler getScaler() {
		return mScaler;
	}

	void setFilterId(int filterId) {
		mFilterApplier = createFilterApplier(filterId);
	}

	/**
	 * 이미지를 그릴 때 적용하는 필터의 식별자를 반환합니다.
	 */
	public int getFilterId() {
		return mFilterApplier.getFilterId();
	}

	/**
	 * 사용자 임의 이미지 식별자를 등록합니다.
	 * <p />
	 * 본 메서드는 {@code LayerScene}의 기능에 아무런 영향을 미치지 않습니다.
	 * 
	 * @param imageId
	 *            등록할 이미지 식별자.
	 */
	public void setImageId(int imageId) {
		mImageId = imageId;
	}

	/**
	 * 사용자 임의 이미지 식별자를 반환합니다.
	 */
	public int getImageId() {
		return mImageId;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link LayerScene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<LayerScene, Editor> {

		private Editor(LayerScene layerScene) {
			super(layerScene);
		}

		/**
		 * 이미지를 그릴 때 사용할 원본 이미지 파일의 절대 경로를 설정합니다.
		 * 
		 * @param imageFilePath
		 *            원본 이미지 파일의 절대 경로.
		 */
		public Editor setImageFilePath(String imageFilePath) {
			getObject().setImageFilePath(imageFilePath);
			return this;
		}

		/**
		 * 이미지를 그릴 때 적용할 필터의 식별자를 설정합니다.
		 * 
		 * @param filterId
		 *            적용할 필터의 식별자.
		 */
		public Editor setFilterId(int filterId) {
			getObject().setFilterId(filterId);
			return this;
		}
	}
}
