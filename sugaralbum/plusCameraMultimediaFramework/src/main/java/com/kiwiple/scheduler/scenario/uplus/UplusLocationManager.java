package com.kiwiple.scheduler.scenario.uplus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kiwiple.imageanalysis.analysis.operator.LocationOperator;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.search.ImageSearch;
import com.kiwiple.imageanalysis.search.ImageSearchCondition;
import com.kiwiple.imageanalysis.search.ImageSearchDetailCondition;
import com.kiwiple.scheduler.R;

import android.content.Context;
import android.content.res.Resources;

public class UplusLocationManager {

	private static UplusLocationManager sInstance;
	private Context mContext;
	private List<String> mLocationNameList;
	private List<String> mCountryExceptionList;
	private List<String> mCityExceptionList;
	private static final int IMAGE_SEARCH_COUNT = 0;

	private UplusLocationManager(Context context) {
		mContext = context;
		mLocationNameList = new ArrayList<String>();
		mCountryExceptionList = new ArrayList<String>();
		mCityExceptionList = new ArrayList<String>();
	}

	/** Get or create if not exist an instance of UplusLocationManager */
	public static UplusLocationManager getLocationManager(Context context) {
		if ((sInstance == null) || !context.equals(sInstance.mContext)) {
			sInstance = new UplusLocationManager(context);
		}

		return sInstance;
	}

	public List<UplusLocationAndCount> makeLocationAndCountList(JSONArray jsonArray) {
		mLocationNameList.clear();
		mCountryExceptionList.clear();
		mCityExceptionList.clear();

		makeExceptionList();
		mLocationNameList = makeLocationNameList();
		return makeLocationAndCountList(jsonArray, mLocationNameList);
	}

	private void makeExceptionList() {
		Resources res = mContext.getResources();
		String[] country = res.getStringArray(R.array.country_exception);
		if (country != null) {
			for (int i = 0; i < country.length; i++) {
				mCountryExceptionList.add(country[i]);
			}
		}
		String[] city = res.getStringArray(R.array.city_exception);
		if (city != null) {
			for (int i = 0; i < city.length; i++) {
				mCityExceptionList.add(city[i]);
			}
		}
	}

	private List<String> makeLocationNameList() {
		List<String> tempLocationList;

		tempLocationList = LocationOperator.getAddressNameWithCategory(mContext, LocationOperator.ADDRESS_INDEX_OF_COUNTRY);
		if (tempLocationList != null) {
			for (String location : tempLocationList) {
				if (!mCountryExceptionList.contains(location)) {
					mLocationNameList.add(location);
				}
			}
		}

		tempLocationList = LocationOperator.getAddressNameWithCategory(mContext, LocationOperator.ADDRESS_INDEX_OF_CITY);
		if (tempLocationList != null) {
			for (String location : tempLocationList) {
				if (!mCityExceptionList.contains(location)) {
					mLocationNameList.add(location);
				}
			}
		}

		tempLocationList = LocationOperator.getAddressNameWithCategory(mContext, LocationOperator.ADDRESS_INDEX_OF_DISTRICT);
		if (tempLocationList != null) {
			for (String location : tempLocationList) {
				mLocationNameList.add(location);
			}
		}
		return mLocationNameList;
	}

	private List<UplusLocationAndCount> makeLocationAndCountList(JSONArray jsonArray, List<String> locationNameList) {
		List<UplusLocationAndCount> tempList = new ArrayList<UplusLocationAndCount>();
		for (String locationName : locationNameList) {
			List<ImageData> imageData = null;
			try {
				replaceAddressJSON(jsonArray, locationName);
				imageData = getImageData(mContext, jsonArray.get(0).toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (imageData != null && imageData.size() > 0) {
				UplusLocationAndCount locationAndCnt = new UplusLocationAndCount();
				locationAndCnt.setLocationName(locationName);
				locationAndCnt.setCount(imageData.size());
				tempList.add(locationAndCnt);
			}
		}
		Comparator<UplusLocationAndCount> comparator = new Comparator<UplusLocationAndCount>() {

			@Override
			public int compare(UplusLocationAndCount lhs, UplusLocationAndCount rhs) {
				return lhs.getCount() >= rhs.getCount() ? -1 : 1;
			}
		};

		Collections.sort(tempList, comparator);
		return tempList;
	}

	private void replaceAddressJSON(JSONArray jsonArray, String locationName) {
		JSONArray addressList = new JSONArray();

		try {
			JSONObject address = new JSONObject();
			address.put(UplusScenarioJsonNamespace.ADDRESS_NAME, locationName);
			addressList.put(address);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				((JSONObject) jsonArray.get(i)).put(UplusScenarioJsonNamespace.ADDRESS_NAMES, addressList);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private List<ImageData> getImageData(Context context, String jsonString) {
		ImageSearchCondition imageSearchCondition = getImageSearchCondition(jsonString);
		ImageSearch imageSearch = new ImageSearch(context, imageSearchCondition);
		return imageSearch.getImageDatasFromCondition();
	}

	private ImageSearchCondition getImageSearchCondition(String jsonString) {
		ImageSearchCondition condition = new ImageSearchCondition(IMAGE_SEARCH_COUNT, 0);
		ImageSearchDetailCondition detailCondition = new ImageSearchDetailCondition();
		detailCondition.setJsonStringCondition(jsonString);
		condition.setImageSearchDetailCondition(detailCondition);
		return condition;
	}
}
