package com.kiwiple.multimedia.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * JsonTokener.
 *
 * @see org.json.JSONTokener
 */
class JsonTokener extends JSONTokener {

	// // // // // Constructor.
	// // // // //
	public JsonTokener(String in) {
		super(in);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Object nextValue() throws JSONException {

		Object object = super.nextValue();

		if (object instanceof JSONObject) {
			return new JsonObject((JSONObject) object);
		} else if (object instanceof JSONArray) {
			return new JsonArray((JSONArray) object);
		} else {
			return object;
		}
	}
}
