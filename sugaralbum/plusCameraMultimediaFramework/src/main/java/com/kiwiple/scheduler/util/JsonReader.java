package com.kiwiple.scheduler.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;

import com.kiwiple.debug.L;
import com.kiwiple.scheduler.scenario.uplus.UplusScenarioJsonNamespace;

public class JsonReader {
	
	/**
	 * inputStream에 해당하는 json object반환. 
	 * @param context : Context. 
	 * @param inputStream : InputStream. 
	 * @return : JSONObject. 
	 */
	public static JSONObject readJsonFile(Context context, InputStream inputStream) {
		JSONObject jsonObject = null;
		String string = null;
		try {
			string = readJsonFile(inputStream);
			jsonObject = new JSONObject(string);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	/**
	 * assetManger와 file 이름을 통해서 JSONObject를 반환. 
	 * @param context : Context. 
	 * @param assetManager : AssetManager. 
	 * @param fileName : file name. 
	 * @return : JSONObject. 
	 */
	public static JSONObject readJsonFile(Context context, AssetManager assetManager, String fileName) {
		JSONObject jsonObject = null;
		String string = null;
		try {
			try {
				string = readJsonFile(new InputStreamReader(assetManager.open(fileName)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsonObject = new JSONObject(string);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	/**
	 * resource id를 통한 JSONObject를 반환. 
	 * @param context : Context. 
	 * @param jsonResourceId : resource id. 
	 * @return : JSONObject. 
	 */
	public static JSONObject readJsonFile(Context context, int jsonResourceId) {
		JSONObject jsonObject = null;
		try {
			String string = readJsonFile(context.getResources().openRawResource(jsonResourceId));
			jsonObject = new JSONObject(string);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	/**
	 * resource id를 통한 JSONArray를 반환. 
	 * @param context : Context. 
	 * @param ruleSetNumber : rule set resource id. 
	 * @return : JSONArray. 
	 */
	public JSONArray readJsonArrayFile(Context context, int ruleSetNumber) {
		JSONArray jsonArray = null;
		try {
			String string = readJsonFile(context.getResources().openRawResource(ruleSetNumber));

			JSONObject object = new JSONObject(string);
			jsonArray = object.getJSONArray(UplusScenarioJsonNamespace.CONDITION);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonArray;
	}

	/**
	 * InputStream 에서 string을 반환. 
	 * @param inputStream : InputStream. 
	 * @return : String 
	 */
	private static String readJsonFile(InputStream inputStream) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, len);
			}
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputStream.toString();
	}

	/**
	 * InputStreamReader에서 string을 반환. 
	 * @param inputStream : InputStreamReader
	 * @return : string. 
	 */
	private static String readJsonFile(InputStreamReader inputStream) {
		// Reading text file from assets folder
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		try {
			br = new BufferedReader(inputStream);
			String temp;
			while ((temp = br.readLine()) != null)
				sb.append(temp);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close(); // stop reading
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
}
