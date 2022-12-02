package com.kiwiple.multimedia.canvas;

import java.io.IOException;

import org.json.JSONException;

import android.graphics.Bitmap;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.annotation.Child;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * 이미지 파일을 출력하는 클래스.
 * 
 * @see Scene
 * @see Scaler
 */
public final class ImageFileScene extends Scene implements IScalable {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "image_file_scene";

	public static final String JSON_NAME_SCALER = "scaler";
	public static final String JSON_NAME_IMAGE_RESOURCE = ImageResource.DEFAULT_JSON_NAME;

	private static final Class<? extends Scaler> DEFAULT_SCALER = FitCenterScaler.class;

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private ImageResource mImageResource;

	@Child
	private Scaler mScaler;

	@CacheCode
	private FilterApplier mFilterApplier;

	private int mImageId;

	// // // // // Constructor.
	// // // // //
	{
		setFilterId(FilterApplier.INVALID_FILTER_ID);
		setScaler(CanvasUserFactory.createScaler(DEFAULT_SCALER, this));
	}

	ImageFileScene(Region parent) {
		super(parent);
	}

	ImageFileScene(MultiLayerScene parent) {
		super(parent);
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
		checkValidity(mImageResource != null, "You must invoke setImageResource().");
		checkValidity(mImageResource.validate(), String.format("ImageResource(%s) is invalid.", mImageResource));
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.putOpt(JSON_NAME_IMAGE_RESOURCE, mImageResource);
		jsonObject.putOpt(JSON_NAME_SCALER, mScaler);
		jsonObject.putOpt(JSON_NAME_IMAGE_ID, mImageId, 0);
		jsonObject.putOpt(JSON_NAME_FILTER_ID, mFilterApplier);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		JsonObject imageResourceJsonObject = jsonObject.optJSONObject(JSON_NAME_IMAGE_RESOURCE);
		if (imageResourceJsonObject != null) {
			setImageResource(ImageResource.createFromJsonObject(getResources(), imageResourceJsonObject));
		}
		JsonObject scalerJsonObject = jsonObject.optJSONObject(JSON_NAME_SCALER);
		if (scalerJsonObject != null) {
			setScaler(CanvasUserFactory.createScaler(scalerJsonObject, this));
		}
		setImageId(jsonObject.optInt(JSON_NAME_IMAGE_ID, 0));
		setFilterId(jsonObject.optInt(JSON_NAME_FILTER_ID, FilterApplier.INVALID_FILTER_ID));
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

	void setImageResource(ImageResource imageResource) {
		Precondition.checkNotNull(imageResource);
		mImageResource = imageResource;
	}

	public ImageResource getImageResource() {
		return mImageResource;
	}

	void setScaler(Scaler scaler) {
		Precondition.checkNotNull(scaler);
		mScaler = scaler;
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
	 * 본 메서드는 {@code ImageFileScene}의 기능에 아무런 영향을 미치지 않습니다.
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
	 * {@link ImageFileScene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<ImageFileScene, Editor> {

		private Editor(ImageFileScene imageFileScene) {
			super(imageFileScene);
		}

		public Editor setImageResource(ImageResource imageResource) {
			getObject().setImageResource(imageResource);
			return this;
		}

		/**
		 * 이미지를 그릴 때 적용할 {@link Scaler}를 생성하여 추가합니다.
		 * 
		 * @param type
		 *            추가할 {@code Scaler}의 {@link Class} 객체.
		 * @return 추가된 {@code Scaler} 객체.
		 */
		public <T extends Scaler> T setScaler(Class<T> type) {

			T scaler = (T) CanvasUserFactory.createScaler(type, getObject());
			getObject().setScaler(scaler);
			return scaler;
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
