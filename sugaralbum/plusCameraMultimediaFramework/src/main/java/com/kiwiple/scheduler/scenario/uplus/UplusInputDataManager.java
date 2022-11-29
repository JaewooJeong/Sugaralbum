package com.kiwiple.scheduler.scenario.uplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.kiwiple.imageanalysis.database.GalleryDBManager.DateAndCount;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.search.ImageSearch;
import com.kiwiple.multimedia.util.ImageUtils;
import com.kiwiple.debug.L;
import com.kiwiple.scheduler.R;
import com.kiwiple.scheduler.analysis.uplus.UplusImageAnalysis;
import com.kiwiple.scheduler.data.uplus.UplusInputData;
import com.kiwiple.scheduler.database.uplus.UplusAnalysisConstants;
import com.kiwiple.scheduler.database.uplus.UplusAnalysisPersister;
import com.kiwiple.scheduler.scenario.InputDataManager;
import com.kiwiple.scheduler.util.JsonReader;

/**
 * uplus 입력 데이터 매니저 객체. 
 *
 */
public class UplusInputDataManager extends InputDataManager {

	private Context mContext;
	private JsonReader mJsonReader;
	private int mScenarioNumber;
	private String mLocation;
	private String mMainDate;
	private UplusAnalysisPersister mUplusAnalysisPersister;
	private static final String SCREENSHOT_FOLDER_NAME = "Screenshots";
	private int mMaxImageCount; 
	
	/**
	 * 입력된 데이터의 첫번째 날짜. 
	 */
	private String mStartDate;  

	/**
	 * 생성자 
	 * @param context Context
	 * @param int 특정 타겟이 지원하는 image 최대 갯수
	 */
	public UplusInputDataManager(Context context, int maxImageCount) {
		mContext = context;
		mJsonReader = new JsonReader();
		mUplusAnalysisPersister = UplusAnalysisPersister.getAnalysisPersister(context.getApplicationContext());
		mMaxImageCount = maxImageCount;
	}
	

	
	/**
	 * 입력된 사진에서 스크린샷 폴더에 있는 사진은 제외 시킨다. 
	 * @param imageDataList 입력 사진 리스트. 
	 * @return 스크린샷 폴더에 있는 사진은 제외된 사진 리스트. 
	 */
	private List<ImageData> selecteWithoutScreenShoot(List<ImageData> imageDataList){
		List<ImageData> selectedImageDatas = new ArrayList<ImageData>();
		
		for(ImageData imageData : imageDataList){
			if(!imageData.path.contains(SCREENSHOT_FOLDER_NAME)){
				selectedImageDatas.add(imageData); 
			}
		}
		return selectedImageDatas; 
	}



	public UplusInputData setUplusInputData(ArrayList<ImageData> imageDataList, ArrayList<ImageData> videoDataList) {
		UplusInputData uplusInputData = new UplusInputData();

		for (int i = 0; i < imageDataList.size(); i++) {
			ImageSearch imageSearch = new ImageSearch(mContext, null);
			int imageId = (int) Math.max(Math.min(Integer.MAX_VALUE, imageDataList.get(i).id), Integer.MIN_VALUE);
			ImageData imageData = imageSearch.getImagaeDataForImageId(imageId);
			
			if(imageData != null){
				if(imageData.width == 0 || imageData.height == 0){
					imageData.width = ImageUtils.measureImageSize(imageData.path).width; 
					imageData.height = ImageUtils.measureImageSize(imageData.path).height; 
				}
			}

			if (imageData != null) {
				uplusInputData.imageDataAddToImageDataList(imageData);
			} else {
				uplusInputData.imageDataAddToImageDataList(imageDataList.get(i));
			}
		}

		for (ImageData videoData : videoDataList) {
			uplusInputData.getVideoDataList().add(videoData);
		}
		return uplusInputData;
	}

	public String getLocation() {
		return mLocation;
	}

	public String getMainDate() {
		return mMainDate;
	}

	private void setScenarioNumber(int scenarioNumber) {
		L.d("scenarioNumber = " + scenarioNumber);

		mScenarioNumber = R.raw.search_ruleset_yesterday;
		if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_YESTERDAY) {
			mScenarioNumber = R.raw.search_ruleset_yesterday;
		} else if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_ONE_TWO_THREE_YEARS_AGO) {
			mScenarioNumber = R.raw.search_ruleset_one_two_three_years_ago;
		} else if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_LOCATION) {
			mScenarioNumber = R.raw.search_ruleset_location;
		} else if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_MAINDATE) {
			mScenarioNumber = R.raw.search_ruleset_main_date;
		} else if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_FIRST){
			mScenarioNumber = R.raw.search_ruleset_first;
		}else if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_USER_ANNIVERSARY){
			mScenarioNumber = R.raw.search_ruleset_user_anniversary;
		}else if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_ANNIVERSARY){
			mScenarioNumber = R.raw.search_ruleset_anniversary;
		}else if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_LAST_3_DAYS){
			mScenarioNumber = R.raw.search_ruleset_last_3_days;
		}else if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_LAST_4_DAYS){
			mScenarioNumber = R.raw.search_ruleset_last_4_days;
		}else if (scenarioNumber == UplusScenarioJsonNamespace.SEARCH_RULESET_LAST_5_DAYS){
			mScenarioNumber = R.raw.search_ruleset_last_5_days;
		}
		
	}
	
    /**
     * 이미지 데이터 날짜와 해당 날짜에 속한 이미지의 갯수 구조 클래스
     */
    public class DateCount {
    	/**
    	 * 특정 날짜 long형; 
    	 */
    	public long date; 
        /**
         * 특정 날짜 String형
         */
        public String dateFormat;
        /**
         * 특정 날짜에 속한 이미지 갯수
         */
        public int count;
        /**
         * 생성자. 
         * @param date long형 날짜. 
         * @param dateFormat String형 날짜 
         * @param count : 데이터 카운트. 
         */
		public DateCount(long date, String dateFormat, int count) {
			super();
			this.date = date;
			this.dateFormat = dateFormat;
			this.count = count;
		}
        
        
    }


	private JSONArray makeJsonArray() {
		JSONArray jsonArrayMain = mJsonReader.readJsonArrayFile(mContext, mScenarioNumber);
		replaceQualityValues(jsonArrayMain); 
		if (mScenarioNumber == R.raw.search_ruleset_location) {
			UplusLocationManager locationManager = UplusLocationManager.getLocationManager(mContext);
			List<UplusLocationAndCount> locationAndCountList = locationManager.makeLocationAndCountList(jsonArrayMain);
			boolean isFind = false;
			if (locationAndCountList != null) {
				for (UplusLocationAndCount locationAndCount : locationAndCountList) {
					if (locationAndCount.getCount() > UplusImageAnalysis.MINIMUM_AUTO_IMAGE_COUNT
							&& !mUplusAnalysisPersister.isExistInfoData(UplusAnalysisConstants.INFO_TYPE_LOCATION, null,
									locationAndCount.getLocationName())) {
						replaceAddressJSON(jsonArrayMain, locationAndCount.getLocationName());
						L.d("location = " + locationAndCount.getLocationName());
						mLocation = locationAndCount.getLocationName();
						isFind = true;
						break;
					}
				}
			}

			if (!isFind) {
				jsonArrayMain = null;
			}
		}else if (mScenarioNumber == R.raw.search_ruleset_main_date) {
		
			ImageSearch imageSearch = new ImageSearch(mContext, null);
			ArrayList<DateAndCount> dateAndCountList = imageSearch.getImageDataCountsInDate();
			ArrayList<DateCount> dateCountList = getDateCountOrderByLatestDay(dateAndCountList); 
			boolean isFind = false;
			if (dateAndCountList != null) {
				for (DateCount dateCount : dateCountList) {
					L.d("date : " + dateCount.dateFormat +", count : " + dateCount.count);  
					if (dateCount.count > UplusImageAnalysis.MINIMUM_AUTO_IMAGE_COUNT){
						boolean isExist = mUplusAnalysisPersister.isExistInfoData(UplusAnalysisConstants.INFO_TYPE_A_DAY_MOVIE_DIARY, dateCount.dateFormat, null);
						L.d("is exist : " + isExist + ", date : " + dateCount.dateFormat); 
						if(!isExist) {
							try {
								SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
								Date date = formatter.parse(dateCount.dateFormat);
								int diffDay = (int) ((System.currentTimeMillis() - date.getTime()) / (1000 * 60 * 60 * 24));
								if(diffDay == 0){
									continue; 
								}
								L.d("date = " + dateCount.dateFormat);
								L.d("diffDay = " + diffDay);
								replaceStartEndDateJSON(jsonArrayMain, diffDay, diffDay);
								mMainDate = dateCount.dateFormat;
								isFind = true;
								break;
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			if (!isFind) {
				jsonArrayMain = null;
			}
		}
		return jsonArrayMain;
	}

	private ArrayList<DateCount> getDateCountOrderByLatestDay(ArrayList<DateAndCount> dateAndCountList) {
		ArrayList<DateCount> dateCountList = new ArrayList<DateCount>();
		for(DateAndCount dateAndCount : dateAndCountList){
			dateCountList.add(new DateCount(getLongDateFromString(dateAndCount.dateFormat), dateAndCount.dateFormat, dateAndCount.count));
		}
		
		// 선택된 사진을 시간순으로 배치 한다.
		Comparator<DateCount> comparatorDate = new Comparator<DateCount>() {

			@Override
			public int compare(DateCount lhs, DateCount rhs) {
				long lhsDate = lhs.date; 
				long rhsDate = rhs.date; 
				if(lhsDate > rhsDate){
					return -1; 
				}else if (lhsDate == rhsDate){
					return 0; 
				}else{
					return 1; 
				}
			}
		};
		Collections.sort(dateCountList, comparatorDate);
		
		return dateCountList;
	}

	private long getLongDateFromString(String dateFormat) {
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = transFormat.parse(dateFormat);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return date.getTime();
	}

	private void replaceStartEndDateJSON(JSONArray jsonArrayMain, int startDate, int endDate) {
		for (int i = 0; i < jsonArrayMain.length(); i++) {
			try {
				((JSONObject) jsonArrayMain.get(i)).put(UplusScenarioJsonNamespace.START_DATE_AMOUNT, startDate);
				((JSONObject) jsonArrayMain.get(i)).put(UplusScenarioJsonNamespace.END_DATE_AMOUNT, endDate);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void replaceAddressJSON(JSONArray jsonArrayMain, String locationName) {
		JSONArray addressList = new JSONArray();

		try {
			JSONObject address = new JSONObject();
			address.put(UplusScenarioJsonNamespace.ADDRESS_NAME, locationName);
			addressList.put(address);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < jsonArrayMain.length(); i++) {
			try {
				((JSONObject) jsonArrayMain.get(i)).put(UplusScenarioJsonNamespace.ADDRESS_NAMES, addressList);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void replaceQualityValues(JSONArray jsonArrayMain){

		for (int i = 0; i < jsonArrayMain.length(); i++) {
			try {
				((JSONObject) jsonArrayMain.get(i)).put(UplusScenarioJsonNamespace.MIN_FOCUS_VALUE, UplusScenarioJsonNamespace.MIN_FOCUS_VALUE_VALUE);
				((JSONObject) jsonArrayMain.get(i)).put(UplusScenarioJsonNamespace.MIN_BRIGHTNESS_VALUE, UplusScenarioJsonNamespace.MIN_BRIGHTNESS_VALUE_VALUE);
				((JSONObject) jsonArrayMain.get(i)).put(UplusScenarioJsonNamespace.MAX_BRIGHTNESS_VALUE, UplusScenarioJsonNamespace.MAX_BRIGHTNESS_VALUE_VALUE);
				((JSONObject) jsonArrayMain.get(i)).put(UplusScenarioJsonNamespace.MIN_QUALITY_VALUE, UplusScenarioJsonNamespace.MIN_QUALITY_VALUE_VALUE);
				((JSONObject) jsonArrayMain.get(i)).put(UplusScenarioJsonNamespace.MIN_TOTAL_SCORE_VALUE, UplusScenarioJsonNamespace.MIN_TOTAL_SCORE_VALUE_VALUE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 입력 데이터의 첫번째 날짜 반환. 
	 * @return 입력 첫번째 데이터의 날짜. 
	 */
	public String getStartDate() {
		return mStartDate;
	}

}
