package com.kiwiple.multimedia.canvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;

import android.animation.TimeInterpolator;
import android.graphics.Color;
import android.graphics.Rect;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.RiValue;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.math.interpolator.ExponentialInOutInterpolator;
import com.kiwiple.multimedia.util.Range;
import com.kiwiple.multimedia.util.Size;

public final class ExtendBoxTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "extend_box_transition";

	public static final String JSON_NAME_BOXES = "boxes";
	public static final String JSON_NAME_INTERVAL = "interval";
	public static final String JSON_NAME_BOX_COLOR = "box_color";
	public static final String JSON_NAME_BOX_THICKNESS = "box_thickness";
	public static final String JSON_NAME_USE_FADE_IN = "use_fade_in";

	private static final Viewport DEFAULT_BOX = Viewport.FULL_VIEWPORT;
	private static final int DEFAULT_BOX_COLOR = Color.BLACK;
	private static final float DEFAULT_INTERVAL = 0.0f;
	private static final float DEFAULT_BOX_THICKNESS = 1.0f;
	private static final boolean DEFAULT_USE_FADE_IN = false;

	private static final float START_FADE_OUT_BOX_POSITION = 0.9f;

	private static final int TINT_COLOR = 0x00ffffff;
	private static final float START_TINT_ALPHA = 0.8f;
	private static final float END_TINT_ALPHA = 0.0f;

	// // // // // Member variable.
	// // // // //
	private final ArrayList<Viewport> mBoxes;
	private final ArrayList<Rect> mManagedBoxes;
	private final ArrayList<Range> mBoxProgressRanges;

	private final TimeInterpolator mInterpolator;
	private final Rect mBufferRect;

	private float mInterval;

	private int mBoxColor;
	@RiValue
	private float mBoxThickness;

	private boolean mUseFadeIn;

	// // // // // Static method.
	// // // // //
	private static void inset(Rect src, Rect dst, float ratio) {

		float centerX = src.exactCenterX();
		float centerY = src.exactCenterY();
		dst.left = Math.round(makeProgress(centerX, src.left, ratio));
		dst.top = Math.round(makeProgress(centerY, src.top, ratio));
		dst.right = Math.round(makeProgress(centerX, src.right, ratio));
		dst.bottom = Math.round(makeProgress(centerY, src.bottom, ratio));
	}

	// // // // // Constructor.
	// // // // //
	{
		mBoxes = new ArrayList<>();
		mManagedBoxes = new ArrayList<>();
		mBoxProgressRanges = new ArrayList<>();

		mInterpolator = new ExponentialInOutInterpolator();
		mBufferRect = new Rect();

		injectPreset(Preset.DEFAULT);
	}

	ExtendBoxTransition(Region parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		float position = getPosition();
		int boxCount = mBoxes.size();
		int boxAlpha = mBoxColor >>> 24;

		srcCanvasFormer.copy(dstCanvas);

		for (int i = 0; i != boxCount; ++i) {
			Range boxRange = mBoxProgressRanges.get(i);
			if (boxRange.start > position) {
				break;
			}

			Rect box = mManagedBoxes.get(i);
			float boxProgressRatio = mInterpolator.getInterpolation(Math.min(1.0f, position / boxRange.end));

			inset(box, mBufferRect, boxProgressRatio);
			int x = mBufferRect.left;
			int y = mBufferRect.top;
			int width = mBufferRect.width();
			int height = mBufferRect.height();
			srcCanvasLatter.copy(dstCanvas, x, y, x, y, width, height);

			if (mUseFadeIn) {
				int tintAlpha = Math.round(0xff * makeProgress(START_TINT_ALPHA, END_TINT_ALPHA, boxProgressRatio)) << 24;
				dstCanvas.tint(tintAlpha | TINT_COLOR, x, y, width, height);
			}

			if (boxProgressRatio > START_FADE_OUT_BOX_POSITION) {
				float alphaRatio = 1.0f - (boxProgressRatio - START_FADE_OUT_BOX_POSITION) / (1.0f - START_FADE_OUT_BOX_POSITION);
				dstCanvas.drawRect(Math.round(boxAlpha * alphaRatio) << 24 | (mBoxColor & 0x00ffffff), mBufferRect, mBoxThickness);
			} else {
				dstCanvas.drawRect(mBoxColor, mBufferRect, mBoxThickness);
			}
		}
	}

	@Override
	Change[] getSensitivities() {
		return new Change[] { Change.DURATION, Change.SIZE };
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {

		checkValidity(!mBoxes.isEmpty(), "You must invoke addBox()");

		int duration = getDuration();
		int boxCount = mBoxes.size();

		int boxDuration = (boxCount == 1) ? duration : makeProgress(duration / boxCount, duration, 1.0f - mInterval);
		int boxStartPositionOffset = (boxCount == 1) ? 0 : (duration - boxDuration) / (boxCount - 1);

		mManagedBoxes.clear();
		mBoxProgressRanges.clear();

		Size size = new Size(getWidth() - 1, getHeight() - 1);
		for (int i = 0; i != boxCount; ++i) {
			Viewport box = mBoxes.get(i);

			int startPosition = boxStartPositionOffset * i;
			mManagedBoxes.add(box.asActualSizeRect(size));
			mBoxProgressRanges.add(Range.closed(startPosition, startPosition + boxDuration));
		}
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_BOXES, mBoxes);
		jsonObject.put(JSON_NAME_BOX_COLOR, mBoxColor);
		jsonObject.put(JSON_NAME_BOX_THICKNESS, mBoxThickness);
		jsonObject.put(JSON_NAME_USE_FADE_IN, mUseFadeIn);
		jsonObject.put(JSON_NAME_INTERVAL, mInterval);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setBox(jsonObject.getJSONArrayAsList(JSON_NAME_BOXES, Viewport.class));
		setBoxColor(jsonObject.getInt(JSON_NAME_BOX_COLOR));
		setBoxThickness(jsonObject.getFloat(JSON_NAME_BOX_THICKNESS));
		setFadeIn(jsonObject.getBoolean(JSON_NAME_USE_FADE_IN));
		setInterval(jsonObject.getFloat(JSON_NAME_INTERVAL));
	}

	void addBox(Viewport viewport) {
		Precondition.checkNotNull(viewport);

		if (!mBoxes.contains(viewport))
			mBoxes.add(viewport);
	}

	void setBox(Viewport... viewports) {
		Precondition.checkArray(viewports).checkNotEmpty().checkNotContainsNull();
		setBox(Arrays.asList(viewports));
	}

	void setBox(List<Viewport> viewports) {
		Precondition.checkCollection(viewports).checkNotEmpty().checkNotContainsNull();

		removeAllBox();
		for (Viewport viewport : viewports)
			if (!mBoxes.contains(viewport))
				mBoxes.add(viewport);
	}

	void removeAllBox() {
		mBoxes.clear();
	}

	public List<Viewport> getBoxes() {
		return new ArrayList<Viewport>(mBoxes);
	}

	void setInterval(float interval) {
		Precondition.checkNotNegative(interval);
		mInterval = (interval >= 1.0f ? 1.0f : interval);
	}

	public float getInterval() {
		return mInterval;
	}

	void setBoxColor(int color) {
		mBoxColor = color;
	}

	public int getBoxColor() {
		return mBoxColor;
	}

	void setBoxThickness(float thickness) {
		Precondition.checkNotNegative(thickness);
		mBoxThickness = thickness;
	}

	public float getBoxThickness() {
		return mBoxThickness;
	}

	void setFadeIn(boolean useFadeIn) {
		mUseFadeIn = useFadeIn;
	}

	public boolean isUsingFadeIn() {
		return mUseFadeIn;
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link ExtendBoxTransition}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Transition.Editor<ExtendBoxTransition, Editor> {

		private Editor(ExtendBoxTransition extendBoxTransition) {
			super(extendBoxTransition);
		}

		public Editor addBox(Viewport viewport) {
			getObject().addBox(viewport);
			return this;
		}

		public Editor setBox(Viewport... viewports) {
			getObject().setBox(viewports);
			return this;
		}

		public Editor setBox(List<Viewport> viewports) {
			getObject().setBox(viewports);
			return this;
		}

		public Editor removeAllBox() {
			getObject().removeAllBox();
			return this;
		}

		public Editor setInterval(float interval) {
			getObject().setInterval(interval);
			return this;
		}

		public Editor setBoxColor(int color) {
			getObject().setBoxColor(color);
			return this;
		}

		public Editor setBoxThickness(float thickness) {
			getObject().setBoxThickness(thickness);
			return this;
		}

		public Editor setFadeIn(boolean useFadeIn) {
			getObject().setFadeIn(useFadeIn);
			return this;
		}
	}

	// // // // // Enumeration.
	// // // // //
	public static enum Preset implements IPreset<Editor> {

		DEFAULT;

		@Override
		public void inject(Editor editor, float magnification) {

			switch (this) {
				case DEFAULT:
				default:
					editor.setBox(DEFAULT_BOX);
					editor.setBoxColor(DEFAULT_BOX_COLOR);
					editor.setBoxThickness(DEFAULT_BOX_THICKNESS * magnification);
					editor.setFadeIn(DEFAULT_USE_FADE_IN);
					editor.setInterval(DEFAULT_INTERVAL);
					break;
			}
		}
	}
}