package com.sugarmount.sugarcamera.story.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import com.kiwiple.debug.L;

import android.content.Context;
import android.text.format.DateUtils;

public class DateUtil {
	
    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
    public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
    
    public static final int FORMAT_SHOW_YEAR = 0x00001;
    public static final int FORMAT_SHOW_PEROID = 0x00002;
    public static final int FORMAT_SHOW_YEAR2 = 0x00004;
	
	/**
	 * 예 > 한글 : 3 일전
	 *      영어 : 3 days ago
	 * @param dateInMilliseconds
	 * @return
	 */
	public static String getPeriodTime(long dateInMilliseconds) {
		return DateUtils.getRelativeTimeSpanString(dateInMilliseconds, System.currentTimeMillis(), 0, DateUtils.FORMAT_SHOW_DATE).toString();
    }
	
    public static String getPeriodMillisecondTime(long time, int flags, String lang){
    	
    	boolean format = (flags & FORMAT_SHOW_YEAR) != 0;
    	boolean format2 = (flags & FORMAT_SHOW_YEAR2) != 0;
    	
    	long now = System.currentTimeMillis();
    	Calendar currentCalendar = Calendar.getInstance();  
    	currentCalendar.setTimeInMillis(now);		
		int currentYear = currentCalendar.get(Calendar.YEAR);
		int currentMonth = currentCalendar.get(Calendar.MONTH)+1; 
		int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH); 
		
    	Calendar calendar = Calendar.getInstance();  
    	calendar.setTimeInMillis(time);	
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        long duration = Math.abs(now - time);
        long count; 
        
        StringBuilder timePeriod = new StringBuilder();
        
        if(lang.equals("ko"))
        {
        	if(format){
        		return timePeriod.append(year +"년 " + month + "월 " + day + "일").toString(); 
        	}
        
        	if(format2){
        		return timePeriod.append(year +". " + String.format("%02d",month) + ". " + String.format("%02d",day)).toString();
        	}
        
        	if(duration < MINUTE_IN_MILLIS){
        		count = duration / SECOND_IN_MILLIS; 
        		timePeriod.append(count+"초 전");
        		timePeriod.append("/"+month+"."+day); 
        		return timePeriod.toString(); 
        	}else if(duration < HOUR_IN_MILLIS){
        		count = duration /MINUTE_IN_MILLIS; 
        		timePeriod.append(count+"분 전");
        		timePeriod.append("/"+month+"."+day);
        		return timePeriod.toString(); 
        	}else if(duration < DAY_IN_MILLIS){
        		count = duration / HOUR_IN_MILLIS; 
        		timePeriod.append(count+"시간 전");
        		timePeriod.append("/"+month+"."+day);
        		return timePeriod.toString(); 
        	}else if(duration < WEEK_IN_MILLIS){
        		if(currentMonth != month){
        			int lastDayOfMonth = calendar.getActualMaximum(Calendar.DATE);
        			count = lastDayOfMonth - day + currentDay; 
        		}else{
        			count = currentDay - day;
        		}
        		timePeriod.append(count+"일 전");
        		timePeriod.append("/"+month+"."+day);
        		return timePeriod.toString();
        	}else{
        		if(currentYear != year){
        			return timePeriod.append(year +"년 " + month + "월 " + day + "일").toString(); 
        		}else{
        			return timePeriod.append(month + "월 " + day + "일").toString();  
        		}
        	}
        }
        else
        {
        	return DateUtil.changeEngDayString(day) + ", " + DateUtil.changeEngMonthString(month) + ", " + year;
        }
    }
        
    public static long getDayMillisecond(long time){
    	
		Calendar calendar = Calendar.getInstance();  
		calendar.setTimeInMillis(time); 
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        Date date = new Date(year, month, day); 
        Calendar dayCalender = Calendar.getInstance(); 
        dayCalender.setTime(date); 
        
        return dayCalender.getTimeInMillis();  
    }
    
    public static long getTimeMillisecond(int year, int month, int day){
        Date date = new Date(year, month, day);  
        return date.getTime(); 
    }
    
    public static int getMonth(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.get(Calendar.MONTH)+1;
		 
    }
    public static int getDay(long time){
    	Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time); 
    	return calendar.get(Calendar.DAY_OF_MONTH); 
    }
    public static Calendar getCalendar(long time){
    	Calendar calendar = Calendar.getInstance();  
    	calendar.setTimeInMillis(time); 
    	return calendar; 
    }
    
	public static String getPeriodTime(long dateInMilliseconds, int format) {
		return DateUtils.getRelativeTimeSpanString(dateInMilliseconds, System.currentTimeMillis(), 0, format).toString();
    }
	
	/**
	 * 예 > 한글 : 2012년 10월 13일
	 *      영어 : October 13, 2012
	 * @param context
	 * @param dateInMilliseconds
	 * @return
	 */
	public static String getDisplayDate(Context context, long dateInMilliseconds) {
		DateFormat df= android.text.format.DateFormat.getLongDateFormat(context);
		Date date = new Date(dateInMilliseconds);
		return df.format(date);
	}
	
	/**
	 * milliseconds를 시:분:초로 리턴
	 * @param milliseconds
	 * @return
	 */
	public static String getDuration(long milliseconds) {
		if (milliseconds == 0) {
			return "00:00";
		}
		StringBuilder time = new StringBuilder();
		int millis = (int) (milliseconds % 1000);  // UBox 에서 milliseconds 단위는 반올림하여 초까지만 표시함. 
		if (millis >= 500) {
			milliseconds += 1000;
		}
		int seconds = (int) (milliseconds / 1000) % 60 ;
		int minutes = (int) ((milliseconds / (1000*60)) % 60);
		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

		if (hours > 0) {
			if (hours < 10) {
				time.append("0" + hours + ":");
			} else {
				time.append(hours + ":");
			}
		}
		if (minutes <= 0) {
			time.append("00:");
		} else if (minutes < 10) {
			time.append("0" + minutes + ":");
		} else {
			time.append(minutes + ":");
		}
		
		if (seconds < 10) {
			time.append("0" + seconds);
		} else {
			time.append(seconds);			
		}
		
		return time.toString();
	}
	
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
	
	public static int changeDigitMonth(String month)
	{

		int intMonth = 1;
		if(month.equals("Jan")){
			intMonth = 1;
		}else if(month.equals("Feb")){
			intMonth = 2;
		}else if(month.equals("Mar")){
			intMonth = 3;
		}else if(month.equals("Apr")){
			intMonth = 4;
		}else if(month.equals("May")){
			intMonth = 5;
		}else if(month.equals("Jun")){
			intMonth = 6;
		}else if(month.equals("Jul")){
			intMonth = 7;
		}else if(month.equals("Aug")){
			intMonth = 8;
		}else if(month.equals("Sep")){
			intMonth = 9;
		}else if(month.equals("Oct")){
			intMonth = 10;
		}else if(month.equals("Nov")){
			intMonth = 11;
		}else if(month.equals("Dec")){
			intMonth = 12;
		}
			
		return intMonth;
	}
	
	public static String changeDigitDay(String day)
	{
		String strDay =day.substring(0, day.length()-2); 
		
		return strDay;
	}
	
	public static String getThisYear(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		int year = calendar.get(Calendar.YEAR);
		return year +"";
	}
	public static Calendar getLunaCalendar(long time) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);		
		calendar = LunaDateUtil.getLunal(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
		return calendar; 
	}
	
	public static Calendar getSolarCalendar(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar;
	}
	
    public static String getMonthEnglish(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		String month = ""; 
		switch(calendar.get(Calendar.MONTH)){
		case Calendar.JANUARY:
			month = "Jan"; 
			break; 
		case Calendar.FEBRUARY:
			month = "Feb"; 
			break; 
		case Calendar.MARCH:
			month = "Mar"; 
			break; 
		case Calendar.APRIL:
			month = "Apr"; 
			break; 
		case Calendar.MAY:
			month = "May"; 
			break; 
		case Calendar.JUNE:
			month = "Jun"; 
			break; 
		case Calendar.JULY:
			month = "Jul"; 
			break; 
		case Calendar.AUGUST:
			month = "Aug"; 
			break; 
		case Calendar.SEPTEMBER:
			month = "Sep"; 
			break; 
		case Calendar.OCTOBER:
			month = "Oct"; 
			break; 
		case Calendar.NOVEMBER:
			month = "Nov"; 
			break; 
		case Calendar.DECEMBER:
			month = "Dec"; 
			break; 
		}
		return month; 
    }
    
    public static String getDayString(Context context, long time){
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
	 *  1) 오늘(X월 X일) / 어제(X월 X일)
	 *  2) 이 외의 경우 : X월 X일, 동일 년도가 아닌 경우 '년도' 표시
	 *  
	 * @param createTime 이미지 생성 시간
	 * @param lang 		 사용 언어
	 * @return
	 */
	public static String getDateString(long createTime,String lang) {
		String resultDate = null;
		
		
		try {
			Calendar currentCalendar = DateUtil.getCalendar(System.currentTimeMillis()); 
			int currentYear  = currentCalendar.get(Calendar.YEAR);
			int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;
			int currentDay   = currentCalendar.get(Calendar.DAY_OF_MONTH);
			
			Calendar createCalendar = DateUtil.getCalendar(createTime);
			int createYear  = createCalendar.get(Calendar.YEAR);
			int createMonth = createCalendar.get(Calendar.MONTH) + 1;
			int createDay   = createCalendar.get(Calendar.DAY_OF_MONTH);
						
			if(lang.equals("ko"))
			{
				if(createYear < currentYear) {
					resultDate = DateUtil.getPeriodTime(createTime, DateUtils.FORMAT_SHOW_YEAR);
				}
				else {
					if(createMonth == currentMonth) {
					// 오늘
						if(createDay == currentDay) {
							if(lang.equals("ko")) resultDate = "오늘 (" + Integer.toString(createMonth) + "월 " + Integer.toString(createDay) + "일)";						
						}
					// 어제
						else if((createDay + 1) == currentDay) {
							resultDate = "어제 (" + Integer.toString(createMonth) + "월 " + Integer.toString(createDay) + "일)";		
						}
						else {
							resultDate = Integer.toString(createMonth) + "월 " + Integer.toString(createDay) + "일";	
						}
					}
					else {
						resultDate = Integer.toString(createMonth) + "월 " + Integer.toString(createDay) + "일";
					}
				}
			}
			else				  resultDate = DateUtil.changeEngDayString(createDay) + ", " + DateUtil.changeEngMonthString(createMonth) + ", " + createYear;
		}
		catch(NumberFormatException e) {
			e.printStackTrace();
		}
		catch(IllegalArgumentException  e1) {
			e1.printStackTrace();
		}
		catch(ArrayIndexOutOfBoundsException e2) {
			e2.printStackTrace();
		}

		return resultDate;
	}
	
	public static long getMilliSceondDateFromExifString(String time){
		int year = 0, month = 0, day = 0, hour = 0, min = 0, sec = 0; 
		
		if(time == null){
			return System.currentTimeMillis(); 
		}else{
			L.d("time : " + time); 
			StringTokenizer st = new StringTokenizer(time, ":");
			if(st.countTokens() <= 1){
				return System.currentTimeMillis(); 
			}
			for(int i = 0; st.hasMoreTokens(); i++){
				if(i == 0){
					year = Integer.parseInt(st.nextToken()); 
				}else if(i == 1){
					month = Integer.parseInt(st.nextToken()); 
				}if(i == 2){
					String dayHour = st.nextToken();
					day = Integer.parseInt((String) dayHour.substring(0, 1));
					hour = Integer.parseInt((String) dayHour.substring(3, 4));
				}if(i == 3){
					min = Integer.parseInt(st.nextToken()); 
				}if(i == 4){
					sec = Integer.parseInt(st.nextToken()); 
				}
			}
			Calendar calendar = Calendar.getInstance(); 
			calendar.set(year, month, day, hour, min, sec); 
			return calendar.getTimeInMillis();
		}
	}

}
