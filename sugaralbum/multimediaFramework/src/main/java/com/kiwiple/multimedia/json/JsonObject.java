package com.kiwiple.multimedia.json;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kiwiple.debug.Precondition;

/**
 * JsonObject.
 * 
 * @see org.json.JSONObject
 */
@SuppressWarnings("unchecked")
public final class JsonObject extends JSONObject {

	// // // // // Static method.
	// // // // //
	private static String[] extractNames(JSONObject jsonObject) throws JSONException {

		JSONArray jsonArray = jsonObject.names();
		int length = (jsonArray == null ? 0 : jsonArray.length());

		String[] names = new String[length];
		for (int i = 0; i != length; ++i) {
			names[i] = jsonArray.getString(i);
		}
		return names;
	}

	// // // // // Constructor.
	// // // // //
	/**
	 * @see JSONObject#JSONObject()
	 */
	public JsonObject() {
		super();
	}

	/**
	 * @see JSONObject#JSONObject(String)
	 */
	public JsonObject(String json) throws JSONException {
		super(new JsonTokener(json));
	}

	/**
	 * @see JSONObject#JSONObject(Map)
	 */
	public JsonObject(Map<?, ?> copyFrom) throws JSONException {
		super(copyFrom);
	}

	/**
	 * @see JSONObject#JSONObject(JSONObject, String[])
	 */
	public JsonObject(JSONObject jsonObject) throws JSONException {
		super(jsonObject, extractNames(jsonObject));
	}

	// // // // // Method.
	// // // // //
	@Override
	public JsonObject put(String name, int value) throws JSONException {
		return (JsonObject) super.put(name, value);
	}

	@Override
	public JsonObject put(String name, long value) throws JSONException {
		return (JsonObject) super.put(name, value);
	}

	public JsonObject put(String name, float value) throws JSONException {
		return (JsonObject) super.put(name, (double) value);
	}

	@Override
	public JsonObject put(String name, double value) throws JSONException {
		return (JsonObject) super.put(name, value);
	}

	@Override
	public JsonObject put(String name, boolean value) throws JSONException {
		return (JsonObject) super.put(name, value);
	}

	@Override
	public JsonObject put(String name, Object value) throws JSONException {

		if (value instanceof IJsonConvertible)
			return this.put(name, (IJsonConvertible) value);
		else if (value instanceof Enum<?>)
			return this.put(name, (Enum<?>) value);
		return (JsonObject) super.put(name, value);
	}

	public JsonObject put(String name, Enum<?> value) throws JSONException {
		return (JsonObject) super.put(name, JsonUtils.toJsonString(value));
	}

	public JsonObject put(String name, IJsonConvertible jsonConvertible) throws JSONException {
		Precondition.checkString(name).trim().checkNotEmpty();
		Precondition.checkNotNull(jsonConvertible);

		return put(name, jsonConvertible.toJsonObject());
	}

	public <T> JsonObject put(String name, Collection<T> collection) throws JSONException {
		Precondition.checkNotNull(collection);

		JsonArray jsonArray = new JsonArray();
		for (T element : collection)
			jsonArray.put(element);
		return put(name, jsonArray);
	}

	public <T> JsonObject put(String name, T[] array) throws JSONException {
		Precondition.checkNotNull(array);

		JsonArray jsonArray = new JsonArray();
		for (int i = 0; i != array.length; ++i)
			jsonArray.put(array[i]);
		return put(name, jsonArray);
	}

	@Override
	public JsonObject putOpt(String name, Object value) throws JSONException {
		return (JsonObject) (value instanceof IJsonConvertible ? this.putOpt(name, (IJsonConvertible) value) : super.putOpt(name, value));
	}

	public JsonObject putOpt(String name, Enum<?> value) throws JSONException {
		return (value == null ? this : put(name, value));
	}

	public JsonObject putOpt(String name, IJsonConvertible jsonConvertible) throws JSONException {

		if (jsonConvertible == null)
			return this;
		return putOpt(name, jsonConvertible.toJsonObject());
	}

	public <T> JsonObject putOpt(String name, T[] array) throws JSONException {

		if (array == null || array.length == 0)
			return this;
		return put(name, array);
	}

	public <T> JsonObject putOpt(String name, Collection<T> collection) throws JSONException {

		if (collection == null || collection.isEmpty())
			return this;
		return put(name, collection);
	}

	/**
	 * 주어진 {@code value}가 {@code equivalentToNull}과 같은 값을 가진다면 아무론 동작도 취하지 않습니다.
	 * 
	 * @see JSONObject#putOpt(String, Object)
	 */
	public JsonObject putOpt(String name, int value, int equivalentToNull) throws JSONException {
		return (value != equivalentToNull ? (JsonObject) super.put(name, value) : this);
	}

	/**
	 * 주어진 {@code value}가 {@code equivalentToNull}과 같은 값을 가진다면 아무론 동작도 취하지 않습니다.
	 * 
	 * @see JSONObject#putOpt(String, Object)
	 */
	public JsonObject putOpt(String name, long value, long equivalentToNull) throws JSONException {
		return (value != equivalentToNull ? (JsonObject) super.put(name, value) : this);
	}

	/**
	 * 주어진 {@code value}가 {@code equivalentToNull}과 같은 값을 가진다면 아무론 동작도 취하지 않습니다.
	 * 
	 * @see JSONObject#putOpt(String, Object)
	 */
	public JsonObject putOpt(String name, float value, float equivalentToNull) throws JSONException {
		return (value != equivalentToNull ? (JsonObject) super.put(name, value) : this);
	}

	/**
	 * 주어진 {@code value}가 {@code equivalentToNull}과 같은 값을 가진다면 아무론 동작도 취하지 않습니다.
	 * 
	 * @see JSONObject#putOpt(String, Object)
	 */
	public JsonObject putOpt(String name, double value, double equivalentToNull) throws JSONException {
		return (value != equivalentToNull ? (JsonObject) super.put(name, value) : this);
	}

	/**
	 * 주어진 {@code value}가 {@code equivalentToNull}과 같은 값을 가진다면 아무론 동작도 취하지 않습니다.
	 * 
	 * @see JSONObject#putOpt(String, Object)
	 */
	public JsonObject putOpt(String name, boolean value, boolean equivalentToNull) throws JSONException {
		return (value != equivalentToNull ? (JsonObject) super.put(name, value) : this);
	}

	public Object opt(String name, Object fallback) throws JSONException {
		Object result = super.opt(name);
		return (result != null ? result : fallback);
	}

	public <T extends Enum<?>> T getEnum(String name, Class<? extends T> type) throws JSONException {
		return JsonUtils.getEnumByJsonString(getString(name), type);
	}

	public <T extends Enum<?>> T optEnum(String name, Class<? extends T> type) {
		return optEnum(name, type, null);
	}

	public <T extends Enum<?>> T optEnum(String name, Class<? extends T> type, T fallback) {

		try {
			return (isNull(name) ? fallback : getEnum(name, type));
		} catch (IllegalArgumentException exception) {
			return fallback;
		} catch (JSONException exception) {
			return Precondition.assureUnreachable();
		}
	}

	/**
	 * @see JSONObject#getDouble(String)
	 */
	public float getFloat(String name) throws JSONException {
		return (float) getDouble(name);
	}

	/**
	 * @see JSONObject#optDouble(String)
	 */
	public float optFloat(String name) {
		double result = optDouble(name);
		return Double.isNaN(result) ? Float.NaN : (float) result;
	}

	/**
	 * @see JSONObject#optDouble(String, double)
	 */
	public float optFloat(String name, float fallback) {
		return (float) optDouble(name, fallback);
	}

	@Override
	public JsonObject getJSONObject(String name) throws JSONException {
		return (JsonObject) super.getJSONObject(name);
	}

	@Override
	public JsonObject optJSONObject(String name) {
		return (JsonObject) super.optJSONObject(name);
	}

	public JsonObject optJSONObject(String name, JsonObject fallback) {
		JsonObject result = (JsonObject) super.optJSONObject(name);
		return (result != null ? result : fallback);
	}

	public <T> T getJSONObjectAsConcrete(String name, Class<T> type) throws JSONException {

		try {
			JsonObject jsonObject = getJSONObject(name);
			Constructor<?> constructor = type.getConstructor(JsonObject.class);
			return (T) constructor.newInstance(jsonObject);
		} catch (Exception e) {
			return Precondition.assureUnreachable();
		}
	}

	public <T> T optJSONObjectAsConcrete(String name, Class<T> type) throws JSONException {
		return optJSONObjectAsConcrete(name, type, null);
	}

	public <T> T optJSONObjectAsConcrete(String name, Class<T> type, T fallback) throws JSONException {
		return isNull(name) ? fallback : getJSONObjectAsConcrete(name, type);
	}

	@Override
	public JsonArray getJSONArray(String name) throws JSONException {
		return (JsonArray) super.getJSONArray(name);
	}

	@Override
	public JsonArray optJSONArray(String name) {
		return (JsonArray) super.optJSONArray(name);
	}

	public JsonArray optJSONArray(String name, JsonArray fallback) {
		JsonArray result = (JsonArray) super.optJSONArray(name);
		return (result != null ? result : fallback);
	}

	public List<Object> getJSONArrayAsList(String name) throws JSONException {
		return getJSONArray(name).asList();
	}

	public <T> List<T> getJSONArrayAsList(String name, Class<T> type) throws JSONException {
		return getJSONArray(name).asList(type);
	}

	public List<Object> optJSONArrayAsList(String name) throws JSONException {
		return isNull(name) ? new ArrayList<Object>(0) : getJSONArray(name).asList();
	}

	public <T> List<T> optJSONArrayAsList(String name, Class<T> type) throws JSONException {
		return isNull(name) ? new ArrayList<T>(0) : getJSONArray(name).asList(type);
	}

	@Override
	public JsonArray names() {

		try {
			return new JsonArray(super.names());
		} catch (JSONException exception) {
			return Precondition.assureUnreachable(exception.getMessage());
		}
	}

	public boolean isEmpty() {
		return length() == 0;
	}

	public void clear() {

		Iterator<String> iterator = keys();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	public Object find(String name) {
		return find(Object.class, name);
	}

	public <T> T find(Class<T> type, String name) {

		try {
			return new JsonSearcher(this).get(type, name);
		} catch (JSONException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public List<Object> findAll(String name) {
		return findAll(Object.class, name);
	}

	public <T> List<T> findAll(Class<T> type, String name) {

		try {
			return new JsonSearcher(this).getAll(type, name);
		} catch (JSONException exception) {
			exception.printStackTrace();
			return null;
		}
	}
}