package com.kiwiple.multimedia.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;

import com.kiwiple.debug.Precondition;

/**
 * JsonSearcher.
 */
public final class JsonSearcher {

	// // // // // Member variable.
	// // // // //
	private final JsonObject mJsonObject;

	// // // // // Constructor.
	// // // // //
	public JsonSearcher(JsonObject jsonObject) {
		Precondition.checkNotNull(jsonObject);
		mJsonObject = jsonObject;
	}

	// // // // // Static method.
	// // // // //
	private static <T> T searchRecursively(JsonObject jsonObject, List<T> dstList, Class<T> type, String name) throws JSONException {

		Iterator<String> iterator = jsonObject.keys();
		while (iterator.hasNext()) {

			String key = iterator.next();
			Object value = jsonObject.get(key);

			if (key.equals(name) && (type.isAssignableFrom(value.getClass()))) {
				if (dstList == null)
					return type.cast(value);
				dstList.add(type.cast(value));
			}

			T found = null;
			if (value instanceof JsonObject)
				found = searchRecursively((JsonObject) value, dstList, type, name);
			else if (value instanceof JsonArray)
				found = searchRecursively((JsonArray) value, dstList, type, name);

			if (found != null && dstList == null)
				return found;
		}
		return null;
	}

	private static <T> T searchRecursively(JsonArray jsonArray, List<T> dstList, Class<T> type, String name) throws JSONException {

		int length = jsonArray.length();
		for (int i = 0; i != length; ++i) {

			Object value = jsonArray.get(i);
			T found = null;

			if (value instanceof JsonObject)
				found = searchRecursively((JsonObject) value, dstList, type, name);
			else if (value instanceof JsonArray)
				found = searchRecursively((JsonArray) value, dstList, type, name);

			if (found != null && dstList == null)
				return found;
		}
		return null;
	}

	// // // // // Method.
	// // // // //
	public Object get(String name) throws JSONException {
		return get(Object.class, name);
	}

	/**
	 * @see SupportedType
	 */
	public <T> T get(Class<T> type, String name) throws JSONException {
		Precondition.checkNotNull(type);
		Precondition.checkString(name).trim().checkNotEmpty();
		Precondition.checkArgument(SupportedType.isSupported(type), "not supported type: " + type.getSimpleName());

		return (T) searchRecursively(mJsonObject, null, type, name);
	}

	public List<Object> getAll(String name) throws JSONException {
		return getAll(Object.class, name);
	}

	/**
	 * @see SupportedType
	 */
	public <T> List<T> getAll(Class<T> type, String name) throws JSONException {
		Precondition.checkNotNull(type);
		Precondition.checkString(name).trim().checkNotEmpty();
		Precondition.checkArgument(SupportedType.isSupported(type), "not supported type: " + type.getSimpleName());

		ArrayList<T> list = new ArrayList<>();
		searchRecursively(mJsonObject, list, type, name);
		return list;
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * SupportedType.
	 */
	public static enum SupportedType {

		OBJECT(Object.class),

		NUMBER(Number.class),

		BOOLEAN(Boolean.class),

		STRING(String.class),

		JSON_OBJECT(JsonObject.class),

		JSON_ARRAY(JsonArray.class);

		final Class<?> type;

		private SupportedType(Class<?> type) {
			this.type = type;
		}

		public static boolean isSupported(Class<?> type) {

			for (SupportedType value : values())
				if (value.type == type)
					return true;
			return false;
		}
	}
}