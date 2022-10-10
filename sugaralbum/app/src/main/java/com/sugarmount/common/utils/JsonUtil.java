package com.sugarmount.common.utils;

/**
 * Created by Jaewoo on 2016-08-18.
 */
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

    public static JSONObject getJSONObjectFrom(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject;
        } catch (JSONException e) {
//            e.printStackTrace();
            return null;
        }
    }
}
