package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import android.content.Context;
import android.content.res.Resources;

import com.kiwiple.debug.Precondition;
import com.kiwiple.imageanalysis.correct.collage.CollageExecuter;
import com.kiwiple.imageanalysis.correct.sticker.StickerExecuter;
import com.kiwiple.imageframework.filter.FilterManagerWrapper;
import com.kiwiple.imageframework.filter.live.LiveFilterController;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.exception.MultimediaException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.StringUtils;

/**
 * {@link Visualizer}의 제어를 받으며 작동하는 {@link AbstractCanvasUser}에 대한 추상 클래스.
 */
@SuppressWarnings("unchecked")
abstract class VisualizerChild extends AbstractCanvasUser {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_NAME_USER_TAGS = "user_tags";

	// // // // // Member variable.
	// // // // //
	private final Visualizer mParent;

	private final JsonObject mTagContainer;

	private boolean mIsValidated;

	// // // // // Constructor.
	// // // // //
	{
		mTagContainer = new JsonObject();
	}

	VisualizerChild(Visualizer visualizer) {
		Precondition.checkNotNull(visualizer);
		mParent = visualizer;
	}

	VisualizerChild(VisualizerChild visualizerChild) {
		Precondition.checkNotNull(visualizerChild);
		mParent = visualizerChild.mParent;
	}

	// // // // // Method.
	// // // // //
	abstract void drawOnInvalid(PixelCanvas dstCanvas);

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {

		if (!jsonObject.isNull(JSON_NAME_USER_TAGS)) {
			JsonObject tag = jsonObject.getJSONObject(JSON_NAME_USER_TAGS);
			for (String name : tag.names().asList(String.class)) {
				mTagContainer.put(name, tag.get(name));
			}
		}
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = new JsonObject();
		if (!mTagContainer.isEmpty())
			return jsonObject.put(JSON_NAME_USER_TAGS, mTagContainer);
		return jsonObject;
	}

	@Override
	public Editor<? extends VisualizerChild, ? extends Editor<?, ?>> getEditor() {
		return (Editor<? extends VisualizerChild, ? extends Editor<?, ?>>) super.getEditor();
	}

	final <T extends Editor<? extends VisualizerChild, ? extends Editor<?, ?>>> void injectPreset(IPreset<T> preset) {
		Precondition.checkNotNull(preset);

		try {
			T editor = (T) getEditor();
			preset.inject(editor, getResolution().magnification);
		} catch (ClassCastException exception) {
			String properGenericType = getEditor().getClass().getName();
			properGenericType = properGenericType.substring(properGenericType.lastIndexOf('.') + 1, properGenericType.length());
			throw new MultimediaException(StringUtils.format("You must use a Preset<%s>", properGenericType));
		}
	}

	final Context getContext() {
		return mParent.getContext();
	}

	final Resources getResources() {
		return mParent.getResources();
	}

	final CacheManager getCacheManager() {
		return mParent.getCacheManager();
	}

	final LiveFilterController getLiveFilterController() {
		return mParent.getLiveFilterController();
	}

	final FilterManagerWrapper getFilterManager() {
		return mParent.getFilterManager();
	}

	final StickerExecuter getStickerExecuter() {
		return mParent.getStickerExecuter();
	}

	final CollageExecuter getCollageExecuter() {
		return mParent.getCollageExecuter();
	}

	final Resolution getResolution() {
		return mParent.getResolution();
	}

	final FilterApplier createFilterApplier(int filterId) {
		return mParent.createFilterApplier(filterId);
	}

	/**
	 * @see Visualizer#isOnPreviewMode()
	 */
	final boolean isOnPreviewMode() {
		return mParent.isOnPreviewMode();
	}

	@Override
	public final boolean isOnEditMode() {
		return mParent.isOnEditMode();
	}

	/**
	 * 사용자 임의 정보를 담기 위한 {@link JsonObject}를 반환합니다.<br />
	 * <br />
	 * 반환된 {@code JsonObject}에 임의의 정보를 담아두면 {@link #toJsonObject()} 사용 시에 함께 저장되며, 마찬가지로
	 * {@link #injectJsonObject(JsonObject)}를 통해 복원됩니다.
	 */
	public final JsonObject getTagContainer() {
		return mTagContainer;
	}

	final void setValidated(boolean validated) {
		mIsValidated = validated;
	}

	/**
	 * 객체가 정상적으로 작동할 수 있는 상태인지의 여부를 반환합니다.
	 * 
	 * @return 정상적으로 작동할 수 있는 상태일 때 {@code true}.
	 */
	final boolean isValidated() {
		return mIsValidated;
	}

	abstract void validate(Changes changes);

	abstract void release();

	/**
	 * 객체의 유효성을 검사하기 위한 메서드. 논리 자료형으로 귀결되는 표현식을 {@code condition}으로서 전달하여, 해당 표현식이 {@code false}로
	 * 평가된 경우에는 객체를 사용 불가능한 상태인 것으로 판단하여 구현체에서 정의한 그리기 기능을 수행하지 않도록 처리합니다.
	 * 
	 * @param condition
	 *            유효성 검증을 위한 표현식.
	 * @param message
	 *            표현식이 {@code false}로 평가된 경우, 부모 객체로 전달할 메시지.
	 * @throws InvalidCanvasUserException
	 *             {@code condition}이 {@code false}일 때.
	 */
	final void checkValidity(boolean condition, String message) throws InvalidCanvasUserException {

		if (!condition) {
			mParent.reportVaidationError(this, message);
			mIsValidated = false;

			throw new InvalidCanvasUserException();
		}
	}

	// // // // // Inner Class.
	// // // // //
	static abstract class Editor<C extends VisualizerChild, E extends Editor<C, E>> extends AbstractCanvasUser.Editor<C, E> {

		Editor(C visualizerChild) {
			super(visualizerChild);
		}

		@Override
		public final C getObject() {

			C object = super.getObject();
			object.setValidated(false);
			return object;
		}

		public final E injectJsonObject(JsonObject jsonObject) throws JSONException {
			getObject().injectJsonObject(jsonObject);
			return (E) this;
		}
	}
}