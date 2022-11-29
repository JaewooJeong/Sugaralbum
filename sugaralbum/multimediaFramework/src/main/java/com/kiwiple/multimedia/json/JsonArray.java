package com.kiwiple.multimedia.json;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kiwiple.debug.Precondition;

/**
 * JsonArray.
 * 
 * @see org.json.JSONArray
 */
@SuppressWarnings("unchecked")
public class JsonArray extends JSONArray {

	// // // // // Static variable.
	// // // // //
	private static final HashSet<Class<?>> sAsListSupportedTypes;

	// // // // // Constructor.
	// // // // //
	static {
		sAsListSupportedTypes = new HashSet<>();

		sAsListSupportedTypes.add(Object.class);
		sAsListSupportedTypes.add(Number.class);
		sAsListSupportedTypes.add(Integer.class);
		sAsListSupportedTypes.add(Long.class);
		sAsListSupportedTypes.add(Float.class);
		sAsListSupportedTypes.add(Double.class);
		sAsListSupportedTypes.add(JsonObject.class);
		sAsListSupportedTypes.add(JSONObject.class);
		sAsListSupportedTypes.add(JsonArray.class);
		sAsListSupportedTypes.add(JSONArray.class);
		sAsListSupportedTypes.add(String.class);
		sAsListSupportedTypes.add(Boolean.class);
	}

	public JsonArray() {
		super();
	}

	public JsonArray(String in) throws JSONException {
		super(new JsonTokener(in));
	}

	public JsonArray(JSONArray jsonArray) throws JSONException {

		int length = jsonArray.length();
		for (int i = 0; i != length; ++i) {
			put(jsonArray.get(i));
		}
	}

	public JsonArray(Collection<?> copyFrom) throws JSONException {
		super(copyFrom);
	}

	public JsonArray(Object array) {
		super();
		Precondition.checkArray(array);

		int length = Array.getLength(array);
		for (int i = 0; i != length; ++i) {
			Object element = Array.get(array, i);
			put(element != null ? element.getClass().isArray() ? new JsonArray(element) : element : JsonObject.NULL);
		}
	}

	public JsonArray(Object... values) {
		super();
		Precondition.checkNotNull((Object) values);

		for (Object value : values) {
			put(value != null ? value.getClass().isArray() ? new JsonArray(value) : value : JsonObject.NULL);
		}
	}

	// // // // // Static method.
	// // // // //
	private static boolean isAsListSupportedType(Class<?> type) {

		if (sAsListSupportedTypes.contains(type))
			return true;
		else if (Enum.class.isAssignableFrom(type))
			return true;
		else if (isConstructibleFromJsonObject(type))
			return true;
		return false;
	}

	private static boolean isConstructibleFromJsonObject(Class<?> type) {

		for (Constructor<?> consturctor : type.getConstructors()) {
			Class<?>[] parameterTypes = consturctor.getParameterTypes();
			if (parameterTypes.length == 1 && parameterTypes[0] == JsonObject.class)
				return true;
		}
		return false;
	}

	// // // // // Method.
	// // // // //

	@Override
	public JsonArray put(int value) {
		return (JsonArray) super.put(value);
	}

	@Override
	public JsonArray put(long value) {
		return (JsonArray) super.put(value);
	}

	@Override
	public JsonArray put(double value) throws JSONException {
		return (JsonArray) super.put(value);
	}

	@Override
	public JsonArray put(boolean value) {
		return (JsonArray) super.put(value);
	}

	@Override
	public JsonArray put(Object value) {

		try {
			if (value instanceof IJsonConvertible)
				return put((IJsonConvertible) value);
			else if (value instanceof Enum<?>)
				return put((Enum<?>) value);
			return (JsonArray) super.put(value);
		} catch (JSONException exception) {
			exception.printStackTrace();
			return Precondition.assureUnreachable(exception.getMessage());
		}
	}

	public JsonArray put(Enum<?> value) {
		return (JsonArray) super.put(value != null ? JsonUtils.toJsonString(value) : null);
	}

	public JsonArray put(IJsonConvertible jsonConvertible) throws JSONException {
		return (JsonArray) super.put(jsonConvertible != null ? jsonConvertible.toJsonObject() : null);
	}

	@Override
	public JsonArray put(int index, int value) throws JSONException {
		return (JsonArray) super.put(index, value);
	}

	@Override
	public JsonArray put(int index, long value) throws JSONException {
		return (JsonArray) super.put(index, value);
	}

	@Override
	public JsonArray put(int index, double value) throws JSONException {
		return (JsonArray) super.put(index, value);
	}

	@Override
	public JsonArray put(int index, boolean value) throws JSONException {
		return (JsonArray) super.put(index, value);
	}

	@Override
	public JsonArray put(int index, Object value) throws JSONException {

		if (value instanceof IJsonConvertible)
			return put(index, (IJsonConvertible) value);
		else if (value instanceof Enum<?>)
			return put(index, (Enum<?>) value);
		return (JsonArray) super.put(index, value);
	}

	public JsonArray put(int index, Enum<?> value) throws JSONException {
		return (JsonArray) super.put(index, (value != null ? JsonUtils.toJsonString(value) : null));
	}

	public JsonArray put(int index, IJsonConvertible jsonConvertible) throws JSONException {
		return (JsonArray) super.put(index, (jsonConvertible != null ? jsonConvertible.toJsonObject() : null));
	}

	@Override
	public JsonObject getJSONObject(int index) throws JSONException {
		return (JsonObject) super.getJSONObject(index);
	}

	@Override
	public JsonObject optJSONObject(int index) {
		return (JsonObject) super.optJSONObject(index);
	}

	public JsonObject optJSONObject(int index, JsonObject fallback) {
		JsonObject result = (JsonObject) super.optJSONObject(index);
		return (result != null ? result : fallback);
	}

	@Override
	public JsonArray getJSONArray(int index) throws JSONException {
		return (JsonArray) super.getJSONArray(index);
	}

	@Override
	public JsonArray optJSONArray(int index) {
		return (JsonArray) super.optJSONArray(index);
	}

	public JsonArray optJSONArray(int index, JsonArray fallback) {
		JsonArray result = (JsonArray) super.optJSONArray(index);
		return (result != null ? result : fallback);
	}

	public boolean isEmpty() {
		return length() == 0;
	}

	public List<Object> asList() throws JSONException {
		return asList(Object.class);
	}

	public <T> List<T> asList(Class<T> type) throws JSONException {
		Precondition.checkNotNull(type);
		Precondition.checkArgument(isAsListSupportedType(type), "not supported type: " + type.getSimpleName());

		if (Enum.class.isAssignableFrom(type))
			return (List<T>) asEnumList((Class<Enum<?>>) type);
		else if (Number.class.isAssignableFrom(type) && type != Number.class)
			return (List<T>) asNumberList((Class<? extends Number>) type);
		else if (isConstructibleFromJsonObject(type))
			return (List<T>) asConstructibleObjectList(type);

		int length = length();
		ArrayList<T> list = new ArrayList<>(length);

		try {
			for (int i = 0; i != length; ++i) {
				T value = (T) get(i);
				list.add(value.equals(JsonObject.NULL) ? null : value);
			}
		} catch (JSONException exception) {
			Precondition.assureUnreachable();
		} catch (Exception exception) {
			throw new JSONException(exception.getMessage());
		}
		return list;
	}

	private <T extends Enum<?>> List<T> asEnumList(Class<T> type) throws JSONException {

		ArrayList<T> list = new ArrayList<>(length());
		for (String jsonName : asList(String.class))
			list.add(JsonUtils.getEnumByJsonString(jsonName, type));
		return list;
	}

	private <T extends Number> List<T> asNumberList(Class<T> type) throws JSONException {

		ArrayList<T> list = new ArrayList<>(length());
		for (Number number : asList(Number.class)) {
			if (type == Integer.class)
				list.add((T) (Integer) number.intValue());
			else if (type == Long.class)
				list.add((T) (Long) number.longValue());
			else if (type == Float.class)
				list.add((T) (Float) number.floatValue());
			else if (type == Double.class)
				list.add((T) (Double) number.doubleValue());
		}
		return list;
	}

	private <T> List<T> asConstructibleObjectList(Class<T> type) throws JSONException {

		ArrayList<T> list = new ArrayList<>(length());
		try {
			Constructor<?> constructor = type.getConstructor(JsonObject.class);
			for (JsonObject jsonObject : asList(JsonObject.class))
				list.add((T) constructor.newInstance(jsonObject));
		} catch (Exception exception) {
			return Precondition.assureUnreachable();
		}
		return list;
	}
}
