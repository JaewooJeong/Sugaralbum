package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import android.animation.TimeInterpolator;
import android.graphics.Rect;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.math.interpolator.InterpolatorType;
import com.kiwiple.multimedia.util.Size;

/**
 * 원본 영역을 시간의 흐름에 따라 목적 영역으로 서서히 이행하여 출력하는 {@link Effect}.
 */
public final class ScaleEffect extends Effect {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "scale_effect";

	public static final String JSON_NAME_SOURCE_VIEWPORT = "source_viewport";
	public static final String JSON_NAME_DESTINATION_VIEWPORT = "destination_viewport";
	public static final String JSON_NAME_INTERPOLATOR_TYPE = InterpolatorType.DEFAULT_JSON_NAME;

	private static final Viewport DEFAULT_VIEWPORT = Viewport.FULL_VIEWPORT;
	private static final InterpolatorType DEFAULT_INTERPOLATOR_TYPE = InterpolatorType.LINEAR;

	// // // // // Member variable.
	// // // // //
	private InterpolatorType mInterpolatorType;
	private TimeInterpolator mInterpolator;

	private Viewport mSrcViewport;
	private Viewport mDstViewport;
	private Rect mSrcRect;
	private BaseLine mBaseLine;

	// // // // // Constructor.
	// // // // //
	{
		setViewport(DEFAULT_VIEWPORT, DEFAULT_VIEWPORT);
		setInterpolator(DEFAULT_INTERPOLATOR_TYPE);
	}

	ScaleEffect(Scene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	Change[] getSensitivities() {
		return new Change[] { Change.SIZE };
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {

		Size size = getSize();
		Rect rectSrc = mSrcViewport.asActualSizeRect(size);
		Rect rectDst = mDstViewport.asActualSizeRect(size);

		float srcAspectRatio = rectSrc.width() / rectSrc.height();
		float dstAspectRatio = rectDst.width() / rectDst.height();

		mBaseLine = (srcAspectRatio > dstAspectRatio) ? BaseLine.HEIGHT : BaseLine.WIDTH;
		mSrcRect = rectSrc;
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {

		float progressRatio = mInterpolator.getInterpolation(getProgressRatio());

		float left = mSrcViewport.left + (mDstViewport.left - mSrcViewport.left) * progressRatio;
		float top = mSrcViewport.top + (mDstViewport.top - mSrcViewport.top) * progressRatio;
		float scale = 0.0f;

		switch (mBaseLine) {
			case WIDTH:
				float right = mSrcViewport.right + (mDstViewport.right - mSrcViewport.right) * progressRatio;
				scale = (right - left) / mSrcViewport.width();
				break;
			case HEIGHT:
				float bottom = mSrcViewport.bottom + (mDstViewport.bottom - mSrcViewport.bottom) * progressRatio;
				scale = (bottom - top) / mSrcViewport.height();
				break;
			default:
				Precondition.assureUnreachable();
		}

		int srcWidth = mSrcRect.width();
		int srcHeight = mSrcRect.height();
		PixelCanvas buffer = getCanvas(0);

		dstCanvas.copy(buffer, mSrcRect.left, mSrcRect.top, 0, 0, srcWidth, srcHeight);
		buffer.setImageSize(srcWidth, srcHeight);
		buffer.copyWithScale(dstCanvas, left * getWidth(), top * getHeight(), scale);
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_SOURCE_VIEWPORT, mSrcViewport);
		jsonObject.put(JSON_NAME_DESTINATION_VIEWPORT, mDstViewport);
		jsonObject.put(JSON_NAME_INTERPOLATOR_TYPE, mInterpolatorType);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		Viewport viewportSrc = new Viewport(jsonObject.getJSONObject(JSON_NAME_SOURCE_VIEWPORT));
		Viewport viewportDst = new Viewport(jsonObject.getJSONObject(JSON_NAME_DESTINATION_VIEWPORT));
		setViewport(viewportSrc, viewportDst);
		setInterpolator(jsonObject.getEnum(JSON_NAME_INTERPOLATOR_TYPE, InterpolatorType.class));
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { new Size(mSrcRect) };
	}

	/**
	 * @see Editor#setViewport(Viewport, Viewport)
	 */
	void setViewport(Viewport source, Viewport destination) {
		Precondition.checkNotNull(source, destination);

		mSrcViewport = source;
		mDstViewport = destination;
	}

	/**
	 * 출력에 사용할 원본에 해당하는 영역을 반환합니다.
	 */
	public Viewport getSourceViewport() {
		return mSrcViewport;
	}

	/**
	 * 출력이 이루어질 목적지에 해당하는 영역을 반환합니다.
	 */
	public Viewport getDestinationViewport() {
		return mDstViewport;
	}

	/**
	 * @see Editor#setInterpolator(InterpolatorType)
	 */
	void setInterpolator(InterpolatorType interpolatorType) {

		if (interpolatorType == null) {
			interpolatorType = InterpolatorType.LINEAR;
		}
		mInterpolatorType = interpolatorType;
		mInterpolator = InterpolatorType.createInterpolator(interpolatorType);
	}

	/**
	 * 사용할 {@link InterpolatorType}을 반환합니다.
	 */
	public InterpolatorType getInterpolator() {
		return mInterpolatorType;
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link ScaleEffect}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Effect.Editor<ScaleEffect, Editor> {

		private Editor(ScaleEffect scaleEffect) {
			super(scaleEffect);
		}

		/**
		 * 출력할 영역을 설정합니다.
		 * 
		 * @param source
		 *            출력에 사용할 원본에 해당하는 영역.
		 * @param destination
		 *            출력이 이루어질 목적지에 해당하는 영역.
		 */
		public Editor setViewport(Viewport source, Viewport destination) {
			getObject().setViewport(source, destination);
			return this;
		}

		/**
		 * 시간 축의 흐름에 따른 출력 영역을 조정에 사용할 {@code TimeInterpolator}를 {@code InterpolatorType}으로써 설정합니다.
		 * 만약 인자로 {@code null}을 전달한다면 자동적으로 {@code InterpolatorType#LINEAR}가 사용됩니다.
		 * 
		 * @param interpolatorType
		 *            사용할 {@code TimeInterpolator}에 해당하는 {@code InterpolatorType}.
		 */
		public Editor setInterpolator(InterpolatorType interpolatorType) {
			getObject().setInterpolator(interpolatorType);
			return this;
		}
	}

	// // // // // Enumeration.
	// // // // //
	private static enum BaseLine {

		WIDTH, HEIGHT;
	}
}