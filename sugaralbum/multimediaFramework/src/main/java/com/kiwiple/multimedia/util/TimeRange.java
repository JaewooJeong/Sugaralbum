package com.kiwiple.multimedia.util;

import org.json.JSONException;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * 시간 범위를 나타내기 위한 클래스.<br />
 * <br />
 * 시작 시간과 끝 시간에 해당하는 수치를 가지고 있으며, 이 수치는 생성 시에 결정되는 시간 단위에 따라 그 해석이 달라집니다.
 * 
 * @see TimeRange.Unit
 */
public final class TimeRange implements IJsonConvertible {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_NAME_UNIT = "unit";
	public static final String JSON_NAME_START = "start";
	public static final String JSON_NAME_END = "end";

	// // // // // Member variable.
	// // // // //
	public final Unit unit;
	public final Number start;
	public final Number end;

	// // // // // Constructor.
	// // // // //
	TimeRange(Unit unit, Number start, Number end) {
		Precondition.checkNotNull(unit, start);

		if (end != null) {
			double startDoubleValue = start.doubleValue();
			double endDoubleValue = end.doubleValue();
			Precondition.checkNotNegative(startDoubleValue, endDoubleValue);
			Precondition.checkArgument(startDoubleValue < endDoubleValue, "start must be less than end.");
		}
		this.unit = unit;
		this.start = start;
		this.end = end;
	}

	// // // // // Static method.
	// // // // //
	public static TimeRange create(int startMs) {
		return new TimeRange(Unit.MILLISECONDS, startMs, null);
	}

	public static TimeRange create(int startMs, int endMs) {
		return new TimeRange(Unit.MILLISECONDS, startMs, endMs);
	}

	public static TimeRange create(float startRatio, float endRatio) {
		return new TimeRange(Unit.RATIO, startRatio, endRatio);
	}

	public static TimeRange create(JsonObject jsonObject) throws JSONException {

		switch (jsonObject.getEnum(JSON_NAME_UNIT, Unit.class)) {
			case MILLISECONDS:
				if (jsonObject.isNull(JSON_NAME_END))
					return create(jsonObject.getInt(JSON_NAME_START));
				return create(jsonObject.getInt(JSON_NAME_START), jsonObject.getInt(JSON_NAME_END));
			case RATIO:
				return create(jsonObject.getFloat(JSON_NAME_START), jsonObject.getFloat(JSON_NAME_END));
			default:
				return Precondition.assureUnreachable();
		}
	}

	// // // // // Method.
	// // // // //
	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = new JsonObject();
		jsonObject.put(JSON_NAME_UNIT, unit);
		jsonObject.put(JSON_NAME_START, start);
		jsonObject.putOpt(JSON_NAME_END, end);

		return jsonObject;
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * 시간 단위를 구분하기 위한 열거형.
	 * 
	 * @see #MILLISECONDS
	 * @see #RATIO
	 */
	public static enum Unit {

		/**
		 * 주어진 수치가 밀리초 단위임을 의미합니다.
		 */
		MILLISECONDS,

		/**
		 * 주어진 수치가 전체 시간 길이에 대한 비율이라는 것을 의미합니다.<br />
		 * <br />
		 * 즉, 전체 시간 길이가 10초, 주어진 수치가 0.2일 때에 실제 시간은 2초로 결정됩니다.
		 */
		RATIO;
	}
}