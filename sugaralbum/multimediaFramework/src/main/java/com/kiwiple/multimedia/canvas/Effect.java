package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.multimedia.util.TimeRange;
import com.kiwiple.multimedia.util.TimeRange.Unit;

/**
 * {@link Scene}이 생성한 이미지에 다양한 시각 효과를 적용하기 위한 추상 클래스.
 */
@SuppressWarnings("unchecked")
public abstract class Effect extends RegionChild {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_NAME_ACTIVE_TIME_RANGE = "active_time_range";
	public static final String JSON_NAME_DRAW_ONLY_WHILE_ACTIVE_TIME = "draw_only_while_active_time";

	// // // // // Member variable.
	// // // // //
	private final Scene mParent;

	private TimeRange mActiveTimeRange;
	private boolean mDrawOnlyWhileActiveTime;

	// // // // // Constructor.
	// // // // //
	Effect(Scene parent) {
		super(parent);
		mParent = parent;
	}

	// // // // // Method.
	// // // // //
	abstract void onDraw(PixelCanvas dstCanvas);

	final void draw(PixelCanvas dstCanvas) {

		if (!isValidated()) {
			drawOnInvalid(dstCanvas);
		} else {

			if (mActiveTimeRange != null && mDrawOnlyWhileActiveTime) {
				float progressRatio = getProgressRatio();
				if (progressRatio > 0.0f && progressRatio < 1.0f)
					onDraw(dstCanvas);
			} else {
				onDraw(dstCanvas);
			}
		}
	}

	@Override
	final void drawOnInvalid(PixelCanvas dstCanvas) {
		dstCanvas.clear(COLOR_SYMBOL_INVALID_EFFECT, 20, 0, 4, getHeight());
		dstCanvas.clear(COLOR_SYMBOL_INVALID_EFFECT, 25, 0, 2, getHeight());
	}

	@Override
	public Editor<? extends Effect, ? extends Editor<?, ?>> getEditor() {
		return (Editor<? extends Effect, ? extends Editor<?, ?>>) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.putOpt(JSON_NAME_ACTIVE_TIME_RANGE, mActiveTimeRange);
		jsonObject.putOpt(JSON_NAME_DRAW_ONLY_WHILE_ACTIVE_TIME, mDrawOnlyWhileActiveTime, false);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		if (!jsonObject.isNull(JSON_NAME_ACTIVE_TIME_RANGE))
			setActiveTimeRange(TimeRange.create(jsonObject.getJSONObject(JSON_NAME_ACTIVE_TIME_RANGE)));
		setDrawOnlyWhileActiveTime(jsonObject.optBoolean(JSON_NAME_DRAW_ONLY_WHILE_ACTIVE_TIME, false));
	}

	@Override
	public final Size getSize() {
		return mParent.getSize();
	}

	@Override
	public final int getDuration() {
		return mParent.getDuration();
	}

	@Override
	public final int getPosition() {
		return mParent.getPosition();
	}

	@Override
	public final float getProgressRatio() {

		if (mActiveTimeRange == null) {
			return super.getProgressRatio();

		} else {
			int position = getPosition();
			float start = mActiveTimeRange.start.floatValue();
			float end = mActiveTimeRange.end == null ? getDuration() : mActiveTimeRange.end.floatValue();

			if (mActiveTimeRange.unit == Unit.RATIO) {
				int duration = getDuration();
				start *= duration;
				end *= duration;
			}

			if (position <= start)
				return 0.0f;
			else if (position >= end)
				return 1.0f;
			else
				return (position - start) / (end - start);
		}
	}

	void setActiveTimeRange(int startPositionMs) {
		setActiveTimeRange(TimeRange.create(startPositionMs));
	}

	void setActiveTimeRange(int startPositionMs, int endPositionMs) {
		setActiveTimeRange(TimeRange.create(startPositionMs, endPositionMs));
	}

	void setActiveTimeRange(float startPositionRatio, float endPositionRatio) {
		setActiveTimeRange(TimeRange.create(startPositionRatio, endPositionRatio));
	}

	void setActiveTimeRange(TimeRange timeRange) {
		mActiveTimeRange = timeRange;
	}

	public TimeRange getActiveTimeRange() {
		return mActiveTimeRange;
	}

	void setDrawOnlyWhileActiveTime(boolean drawOnlyWhileActiveTime) {
		mDrawOnlyWhileActiveTime = drawOnlyWhileActiveTime;
	}

	public boolean getDrawOnlyWhileActiveTime() {
		return mDrawOnlyWhileActiveTime;
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link Effect}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static abstract class Editor<C extends Effect, E extends Editor<C, E>> extends RegionChild.Editor<C, E> {

		Editor(C effect) {
			super(effect);
		}

		public E setActiveTimeRange(int startPositionMs) {
			getObject().setActiveTimeRange(startPositionMs);
			return (E) this;
		}

		public E setActiveTimeRange(int startPositionMs, int endPositionMs) {
			getObject().setActiveTimeRange(startPositionMs, endPositionMs);
			return (E) this;
		}

		public E setActiveTimeRange(float startPositionRatio, float endPositionRatio) {
			getObject().setActiveTimeRange(startPositionRatio, endPositionRatio);
			return (E) this;
		}

		public E setActiveTimeRange(TimeRange timeRange) {
			getObject().setActiveTimeRange(timeRange);
			return (E) this;
		}

		public E setDrawOnlyWhileActiveTime(boolean drawOnlyWhileActiveTime) {
			getObject().setDrawOnlyWhileActiveTime(drawOnlyWhileActiveTime);
			return (E) this;
		}
	}
}