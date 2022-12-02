package com.kiwiple.scheduler.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

import android.content.Context;
import android.media.ExifInterface;
import android.text.TextUtils;

public class DateUtil {
	/**
	 * 주어진 millisecond time에서 일(DAY) 단위에서 내림.   
	 * @param time : millisecond time. 
	 * @return : 일(DAY) 단위 시간을 반환.  
	 */
	public static long getDayMillisecond(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date date = calendar.getTime();
		return date.getTime();
	}

	/**
	 * millisecond time을 string 형태로 반환. 
	 * @param time : millisecond time. 
	 * @return : string 타입 시간. 
	 */
	public static String getDateString(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy. MM. dd");
		return dateformat.format(calendar.getTime());
	}

	/**
	 * 파일의 경로를 가지고 string 형태의 시간을 반환. 
	 * @param context : Context.
	 * @param imageFilePath : 이미지 파일 경로. 
	 * @return : string 형태의 시간. 
	 */
	public static String getDayStringFromPath(Context context, String imageFilePath) {
		String takenDate = null;
		String _takenDate = null;
		String fullTakenDate = null;
		ExifInterface exifInterface;
		try {
			exifInterface = new ExifInterface(imageFilePath);
			fullTakenDate = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(TextUtils.isEmpty(takenDate)) {
		    File file = new File(imageFilePath);
		    fullTakenDate = getDateString(file.lastModified());
		}
		
		StringTokenizer st = new StringTokenizer(fullTakenDate, " ");
        while (st.hasMoreTokens()) {
        	_takenDate = st.nextToken();
            break;
        }
        takenDate = _takenDate.replaceAll(":", ". ");
        Calendar calendar = DateUtil.getCalendarFromString(takenDate);
        return getDayStringFromDate(context, calendar.getTimeInMillis()); 
	}
	
	/**
	 * 시스템의 언어 설정에 맞게 millisecond time을 string 형태로 반환.  
	 * @param context : Context. 
	 * @param time : millisecond time. 
	 * @return : string 형태의 시간. 
	 */
    public static String getDayStringFromDate(Context context, long time){
    	Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR); 
		int month = calendar.get(Calendar.MONTH)+1; 
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		if(context.getResources().getConfiguration().locale.getLanguage().equals("ko")){
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy. MM. dd");
			return dateformat.format(calendar.getTime()); 
		}else{
			return DateUtil.changeEngDayString(day) + ", " + DateUtil.changeEngMonthString(month) + ", " + year;	
		}
    }
    /**
     * 영문 day 반환. 
     * @param day : day of month 
     * @return : 영문 day. 
     */
	public static String changeEngDayString(int day)
	{
		String strDay = "1st";
		switch(day){
		case 1:
			strDay = "1st";
			break;
		case 2:
			strDay = "2nd";
			break;
		case 3:
			strDay = "3rd";
			break;
		default:
			strDay = day+"th";
			break;
		}
		return strDay;
	}
	
	/**
	 * 영문 month 반환.  
	 * @param month : month. 
	 * @return : 영문 month. 
	 */
	public static String changeEngMonthString(int month)
	{
		String strMon = "Jan";
		
		switch (month) {
		case 1:
			strMon = "Jan";
			break;
		case 2:
			strMon = "Feb";
			break;
		case 3:
			strMon = "Mar";
			break;
		case 4:
			strMon = "Apr";
			break;
		case 5:
			strMon = "May";
			break;
		case 6:
			strMon = "Jun";
			break;
		case 7:
			strMon = "Jul";
			break;
		case 8:
			strMon = "Aug";
			break;
		case 9:
			strMon = "Sep";
			break;
		case 10:
			strMon = "Oct";
			break;
		case 11:
			strMon = "Nov";
			break;
		case 12:
			strMon = "Dec";
			break;
		default:
			break;
		}
		
		return strMon;
	}
	
	/**
	 * millisecond time으로 Calendar 변수 반환. 
	 * @param time : millisecond time. 
	 * @return : Calendar 변수. 
	 */
	public static Calendar getCalendarFromString(String time){
		int year = 0, month = 0, day = 0; 
		StringTokenizer st = new StringTokenizer(time, ".");
		for(int i = 0; st.hasMoreTokens(); i++){
			if(i == 0){
				year = Integer.parseInt(st.nextToken()); 
			}else if(i == 1){
				month = Integer.parseInt(st.nextToken()); 
			}if(i == 2){
				day = Integer.parseInt(st.nextToken()); 
			}
		}
		Calendar calendar = Calendar.getInstance(); 
		calendar.set(year, month, day); 
		return calendar; 
	}
}
