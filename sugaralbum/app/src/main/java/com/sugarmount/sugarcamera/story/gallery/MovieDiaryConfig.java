package com.sugarmount.sugarcamera.story.gallery;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import com.sugarmount.sugaralbum.R;
import com.sugarmount.sugarcamera.story.utils.Utils;

import java.text.SimpleDateFormat;

public class MovieDiaryConfig {
	
	private static final String TAG = "Config";
	
	// for header of DynamicList
	public static final long TIME_WEEK = 1000 * 60 * 60 * 24* 7;
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM.dd");
	
	// dymamic hover view animation duration
	public static final int HOVER_DURATION_SHORT = 240;
	public static final int HOVER_DURATION_LONG = 300;	// 6열 상태인 경우 썸네일이 작아서 좀더 시간을 길게 준다.
	
	// 리스트에서 날짜 및 사진, 비디오의 개수를 표시하는 헤더 높이
	public static float HEIGHT_OF_LIST_HEADER;
	
	// 선택 리스트의 가로 사이즈
	public static int WIDTH_OF_SELECTED_LIST_LAND;
	
	// for list
	public static int LEFT_OF_LIST;
	public static int TOP_OF_LIST;
	public static int WIDTH_OF_LIST;
	public static int HEIGHT_OF_LIST;
	
	public static final int COLUMN_MODE_TWO		= 0;
	public static final int COLUMN_MODE_THREE	= 1;
	public static final int COLUMN_MODE_SIX		= 2;
	public static final int COLUMN_MODE_FOLDER_TWO = 3; 
	public static final int COLUMN_MODE_FOLDER_THREE = 4; 
	
	// row의 형태에 따른 1개 아이템의 사이즈
	public static int ITEM_SIZE_OF_TWO;
	public static int ITEM_SIZE_OF_THREE;
	public static int ITEM_SIZE_OF_THREE_BIG;
	public static int ITEM_SIZE_OF_SIX;
	public static int ITEM_SIZE_OF_FOLDER_TWO;
	public static int ITEM_SIZE_OF_FOLDER_THREE;
	
	// row의 형태에 따른 각 아이템의 위치와 사이즈
	public static Rect[] RECT_TWO = new Rect[6];
	public static Rect[] RECT_THREE = new Rect[6];
	public static Rect[] RECT_THREE_BIG = new Rect[6];
	public static Rect[] RECT_SIX = new Rect[6];
	public static Rect[] RECT_FOLDER_TWO = new Rect[6]; 
	public static Rect[] RECT_FOLDER_THREE = new Rect[6]; 
	public static Rect RECT_LIST_FOLDER = new Rect(); 
	public static int mFolderListWidth; 
	
	private static int mOrientation;
	
	public static final int REQUEST_CODE_COLLAGE 					= 5;
	public static final int RESULT_CODE_UPLOAD_CANCELED_NETWORK_ERR	= 6;
	public static final int REQUEST_CODE_UPLOAD 					= 7;
	public static final int REQUEST_CODE_ACTION_DIRECT_FILE_SHARE	= 8;
	public static final int REQUEST_CODE_MAGISTO_GUIDE    			= 9;
	
	/**
	 * app 시작시에 값을 설정함.
	 * @param context
	 */
	public static void init(Context context, int orientation) {
		mOrientation = orientation;
		
		HEIGHT_OF_LIST_HEADER = context.getResources().getDimension(R.dimen.gallery_list_header_height);
		WIDTH_OF_SELECTED_LIST_LAND = (int)context.getResources().getDimension(R.dimen.gallery_landscape_selected_list_width);
		
		initRowItemProperties(context);
	}
	
	/**
	 * 리스트의 화면(layout) 표시가 완료되면 호출하여 값을 설정함.
	 * @param context
	 * @param list
	 */
	public static void initDynamicList(Context context, View list, int width, int height) {
		int[] loc = new int[2];
		list.getLocationOnScreen(loc);
		LEFT_OF_LIST = loc[0];
		TOP_OF_LIST = loc[1];
		WIDTH_OF_LIST = width;
		HEIGHT_OF_LIST = height;
	}
	
	public static void initRowItemProperties(Context context) {
		initRowItemProperties(context, 0);
	}
	
	public static void initRowItemProperties(Context context, int offset) {
		int widthOfRow = (Utils.getDisplayWidth(context) + offset) - Utils.dp(context, 12.4f);	// list의 좌우 패딩 제외(좌우 합친값)
		
		int widthOfFolderRow = ((Utils.getDisplayWidth(context) * 70 / 100)) - Utils.dp(context, 10.4f);	// list의 좌우 패딩 제외(좌우 합친값)
		int widthOfFolderList = ((Utils.getDisplayWidth(context) * 30 / 100)) - Utils.dp(context, 10.4f);	// list의 좌우 패딩 제외(좌우 합친값)
		mFolderListWidth = (Utils.getDisplayWidth(context) * 30 / 100); 
		
		ITEM_SIZE_OF_TWO = widthOfRow / 2;
		ITEM_SIZE_OF_THREE = widthOfRow / 3;
		ITEM_SIZE_OF_THREE_BIG = ITEM_SIZE_OF_THREE * 2;
		ITEM_SIZE_OF_SIX = widthOfRow / 6;
		ITEM_SIZE_OF_FOLDER_TWO = widthOfFolderRow /2; 
		ITEM_SIZE_OF_FOLDER_THREE = widthOfFolderRow / 3; 
		
		// rect of column Two
		int sizeOfImage = ITEM_SIZE_OF_TWO;
		RECT_TWO[0] = new Rect(0, 0, sizeOfImage, sizeOfImage);
		RECT_TWO[1] = new Rect(ITEM_SIZE_OF_TWO, 0, ITEM_SIZE_OF_TWO + sizeOfImage, sizeOfImage);
		RECT_TWO[2] = new Rect(0, ITEM_SIZE_OF_TWO, sizeOfImage, ITEM_SIZE_OF_TWO + sizeOfImage);
		RECT_TWO[3] = new Rect(ITEM_SIZE_OF_TWO, ITEM_SIZE_OF_TWO, ITEM_SIZE_OF_TWO + sizeOfImage, ITEM_SIZE_OF_TWO + sizeOfImage);
		RECT_TWO[4] = new Rect(0, ITEM_SIZE_OF_TWO * 2, sizeOfImage, (ITEM_SIZE_OF_TWO * 2) + sizeOfImage);
		RECT_TWO[5] = new Rect(ITEM_SIZE_OF_TWO, ITEM_SIZE_OF_TWO * 2, ITEM_SIZE_OF_TWO + sizeOfImage, (ITEM_SIZE_OF_TWO * 2) + sizeOfImage);
		
		// rect of column Three
		sizeOfImage = ITEM_SIZE_OF_THREE;
		RECT_THREE[0] = new Rect(0, 0, sizeOfImage, sizeOfImage);
		RECT_THREE[1] = new Rect(ITEM_SIZE_OF_THREE, 0, ITEM_SIZE_OF_THREE + sizeOfImage, sizeOfImage);
		RECT_THREE[2] = new Rect(ITEM_SIZE_OF_THREE * 2, 0, (ITEM_SIZE_OF_THREE * 2) + sizeOfImage, sizeOfImage);
		RECT_THREE[3] = new Rect(0, ITEM_SIZE_OF_THREE, sizeOfImage, ITEM_SIZE_OF_THREE + sizeOfImage);
		RECT_THREE[4] = new Rect(ITEM_SIZE_OF_THREE, ITEM_SIZE_OF_THREE, ITEM_SIZE_OF_THREE + sizeOfImage, ITEM_SIZE_OF_THREE + sizeOfImage);
		RECT_THREE[5] = new Rect(ITEM_SIZE_OF_THREE * 2, ITEM_SIZE_OF_THREE, (ITEM_SIZE_OF_THREE * 2) + sizeOfImage, ITEM_SIZE_OF_THREE + sizeOfImage);
		
		// rect of column Three big
		sizeOfImage = ITEM_SIZE_OF_THREE;
		RECT_THREE_BIG[0] = new Rect(0, 0, (sizeOfImage * 2), (sizeOfImage * 2));
		RECT_THREE_BIG[1] = new Rect(ITEM_SIZE_OF_THREE * 2, 0, (ITEM_SIZE_OF_THREE * 2) + sizeOfImage, sizeOfImage);
		RECT_THREE_BIG[2] = new Rect(ITEM_SIZE_OF_THREE * 2, ITEM_SIZE_OF_THREE, (ITEM_SIZE_OF_THREE * 2) + sizeOfImage, ITEM_SIZE_OF_THREE + sizeOfImage);
		RECT_THREE_BIG[3] = new Rect(0, ITEM_SIZE_OF_THREE * 2, sizeOfImage, (ITEM_SIZE_OF_THREE * 2) + sizeOfImage);
		RECT_THREE_BIG[4] = new Rect(ITEM_SIZE_OF_THREE, ITEM_SIZE_OF_THREE * 2, ITEM_SIZE_OF_THREE + sizeOfImage, (ITEM_SIZE_OF_THREE * 2) + sizeOfImage);
		RECT_THREE_BIG[5] = new Rect(ITEM_SIZE_OF_THREE * 2, ITEM_SIZE_OF_THREE * 2, (ITEM_SIZE_OF_THREE * 2) + sizeOfImage, (ITEM_SIZE_OF_THREE * 2) + sizeOfImage);
		
		// rect of column Three big
		sizeOfImage = ITEM_SIZE_OF_SIX;
		RECT_SIX[0] = new Rect(0, 0, sizeOfImage, sizeOfImage);
		RECT_SIX[1] = new Rect(ITEM_SIZE_OF_SIX, 0, ITEM_SIZE_OF_SIX + sizeOfImage, sizeOfImage);
		RECT_SIX[2] = new Rect(ITEM_SIZE_OF_SIX * 2, 0, (ITEM_SIZE_OF_SIX * 2) + sizeOfImage, sizeOfImage);
		RECT_SIX[3] = new Rect(ITEM_SIZE_OF_SIX * 3, 0, (ITEM_SIZE_OF_SIX * 3) + sizeOfImage, sizeOfImage);
		RECT_SIX[4] = new Rect(ITEM_SIZE_OF_SIX * 4, 0, (ITEM_SIZE_OF_SIX * 4) + sizeOfImage, sizeOfImage);
		RECT_SIX[5] = new Rect(ITEM_SIZE_OF_SIX * 5, 0, (ITEM_SIZE_OF_SIX * 5) + sizeOfImage, sizeOfImage);
		
		sizeOfImage = ITEM_SIZE_OF_FOLDER_TWO;
		RECT_FOLDER_TWO[0] = new Rect(0, 0, sizeOfImage, sizeOfImage);
		RECT_FOLDER_TWO[1] = new Rect(ITEM_SIZE_OF_FOLDER_TWO, 0, ITEM_SIZE_OF_FOLDER_TWO + sizeOfImage, sizeOfImage);
		RECT_FOLDER_TWO[2] = new Rect(0, ITEM_SIZE_OF_FOLDER_TWO, sizeOfImage, ITEM_SIZE_OF_FOLDER_TWO + sizeOfImage);
		RECT_FOLDER_TWO[3] = new Rect(ITEM_SIZE_OF_FOLDER_TWO, ITEM_SIZE_OF_FOLDER_TWO, ITEM_SIZE_OF_FOLDER_TWO + sizeOfImage, ITEM_SIZE_OF_FOLDER_TWO + sizeOfImage);
		RECT_FOLDER_TWO[4] = new Rect(0, ITEM_SIZE_OF_FOLDER_TWO * 2, sizeOfImage, (ITEM_SIZE_OF_FOLDER_TWO * 2) + sizeOfImage);
		RECT_FOLDER_TWO[5] = new Rect(ITEM_SIZE_OF_FOLDER_TWO, ITEM_SIZE_OF_FOLDER_TWO * 2, ITEM_SIZE_OF_FOLDER_TWO + sizeOfImage, (ITEM_SIZE_OF_FOLDER_TWO * 2) + sizeOfImage);
		
		sizeOfImage = ITEM_SIZE_OF_FOLDER_THREE;
		RECT_FOLDER_THREE[0] = new Rect(0, 0, sizeOfImage, sizeOfImage);
		RECT_FOLDER_THREE[1] = new Rect(ITEM_SIZE_OF_FOLDER_THREE, 0, ITEM_SIZE_OF_FOLDER_THREE + sizeOfImage, sizeOfImage);
		RECT_FOLDER_THREE[2] = new Rect(ITEM_SIZE_OF_FOLDER_THREE * 2, 0, (ITEM_SIZE_OF_FOLDER_THREE * 2) + sizeOfImage, sizeOfImage);
		RECT_FOLDER_THREE[3] = new Rect(0, ITEM_SIZE_OF_FOLDER_THREE, sizeOfImage, ITEM_SIZE_OF_FOLDER_THREE + sizeOfImage);
		RECT_FOLDER_THREE[4] = new Rect(ITEM_SIZE_OF_FOLDER_THREE, ITEM_SIZE_OF_FOLDER_THREE, ITEM_SIZE_OF_FOLDER_THREE + sizeOfImage, ITEM_SIZE_OF_FOLDER_THREE + sizeOfImage);
		RECT_FOLDER_THREE[5] = new Rect(ITEM_SIZE_OF_FOLDER_THREE * 2, ITEM_SIZE_OF_FOLDER_THREE, (ITEM_SIZE_OF_FOLDER_THREE * 2) + sizeOfImage, ITEM_SIZE_OF_FOLDER_THREE + sizeOfImage);
		
		RECT_LIST_FOLDER.set(0, 0, widthOfFolderList, widthOfFolderList); 
	}
	
	public static int COLUMN_VIEW_SIZE(int columnMode) {
		switch(columnMode) {
		case COLUMN_MODE_TWO:
			return ITEM_SIZE_OF_TWO;
		case COLUMN_MODE_THREE:
			return ITEM_SIZE_OF_THREE;				
		case COLUMN_MODE_SIX:
			return ITEM_SIZE_OF_SIX;
		case COLUMN_MODE_FOLDER_TWO:
			return ITEM_SIZE_OF_FOLDER_TWO; 
		case COLUMN_MODE_FOLDER_THREE:
			return ITEM_SIZE_OF_FOLDER_THREE; 
		}
		return 0;
	}
	
	public static void setOrientation(int orientation) {
		mOrientation = orientation;
	}
	
	public static boolean isLandscape() {
    	return mOrientation == Configuration.ORIENTATION_LANDSCAPE;
    }
	
	private static Point mSelectedListPosition = new Point(-1, 0);
	public static void setSelectedListPosition(int position, int offset) {
		mSelectedListPosition.set(position, offset);
	}
	
	public static Point getSelectedListPosition() {
		return mSelectedListPosition;
	}
	
	public static void resetSelectedListPosition() {
		mSelectedListPosition.set(-1, 0);
	}
	
}