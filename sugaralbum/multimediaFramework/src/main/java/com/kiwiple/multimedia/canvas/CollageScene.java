package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.graphics.Bitmap;

import com.kiwiple.imageanalysis.correct.collage.CollageExecuter;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.canvas.data.CollageElement;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.CollectionUtils;
import com.kiwiple.multimedia.util.Size;

/**
 * 여러 이미지 파일을 하나의 콜라주 이미지로 구성하여 출력하는 클래스.
 * 
 * @see Scene
 */
public final class CollageScene extends Scene {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "collage_scene";

	public static final String JSON_NAME_COLLAGE_ELEMENTS = "collage_elements";
	public static final String JSON_NAME_TEMPLATE_ID = "template_id";

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private List<CollageElement> mCollageElements;

	@CacheCode
	private FilterApplier mFilterApplier = FilterApplier.INVALID_OBJECT;

	private int mTemplateId;

	// // // // // Constructor.
	// // // // //
	CollageScene(Region parent) {
		super(parent);
	}

	CollageScene(MultiLayerScene parent) {
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

		jsonObject.putOpt(JSON_NAME_TEMPLATE_ID, mTemplateId, 0);
		jsonObject.putOpt(JSON_NAME_COLLAGE_ELEMENTS, mCollageElements);
		jsonObject.putOpt(JSON_NAME_FILTER_ID, mFilterApplier);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setCollageElements(jsonObject.optJSONArrayAsList(JSON_NAME_COLLAGE_ELEMENTS, CollageElement.class));
		setTemplateId(jsonObject.optInt(JSON_NAME_TEMPLATE_ID, 0));
		setFilterId(jsonObject.optInt(JSON_NAME_FILTER_ID, FilterApplier.INVALID_FILTER_ID));
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		checkValidity(mCollageElements != null && !mCollageElements.isEmpty(), "You must invoke setCollageElements().");
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
			CollageExecuter collageExecuter = getCollageExecuter();
			ArrayList<ImageData> imageData = new ArrayList<ImageData>(mCollageElements);
			bitmap = collageExecuter.getCollageImage(imageData, getWidth());

			if (mFilterApplier.isVaild()) {
				mFilterApplier.apply(bitmap);
			}
		}
		return bitmap;
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { getSize() };
	}

	@Override
	public int getCacheCount() {
		return 1;
	}

	void setCollageElements(List<CollageElement> collageElements) {
		mCollageElements = CollectionUtils.deepClone(collageElements);
	}

	/**
	 * 콜라주를 구성할 때 사용하는 콜라주 구성 요소 목록의 사본을 반환합니다.
	 * 
	 * @see CollageElement
	 */
	public List<CollageElement> getCollageElements() {
		return CollectionUtils.deepClone(mCollageElements);
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
	 * 사용자 임의 CollageScene 식별자를 등록합니다.
	 * <p />
	 * 본 메서드는 {@code CollageScene}의 기능에 아무런 영향을 미치지 않습니다.
	 * 
	 * @param templateId
	 *            등록할 CollageScene 식별자.
	 */
	public void setTemplateId(int templateId) {
		mTemplateId = templateId;
	}

	/**
	 * 사용자 임의 CollageScene 식별자를 반환합니다.
	 */
	public int getTemplateId() {
		return mTemplateId;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link CollageScene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<CollageScene, Editor> {

		private Editor(CollageScene collageScene) {
			super(collageScene);
		}

		/**
		 * 콜라주를 구성할 때 사용할 콜라주 구성 요소 목록을 전달합니다.
		 * 
		 * @param collageElements
		 *            콜라주 구성 요소 목록.
		 * @throws IllegalArgumentException
		 *             주어진 콜라주 구성 요소 목록이 비어있을 때.
		 * @see CollageElement
		 */
		public Editor setCollageElements(List<CollageElement> collageElements) {
			getObject().setCollageElements(collageElements);
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
