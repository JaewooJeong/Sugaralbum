package com.sugarmount.sugarcamera.ui.gallery2;

import android.content.Context;
import android.graphics.Rect;

import com.sugarmount.sugaralbum.R;
import com.sugarmount.sugarcamera.utils.Utils;

public class Config {

	// 리스트에서 날짜 및 사진, 비디오의 개수를 표시하는 헤더 높이
	public static float HEIGHT_OF_LIST_HEADER;
	
	// 선택 리스트의 가로 사이즈
	public static int WIDTH_OF_SELECTED_LIST_LAND;

	public static final int COLUMN_MODE_THREE	= 1;

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

	/**
	 * app 시작시에 값을 설정함.
	 * @param context
	 */
	public static void init(Context context, int orientation) {
		mOrientation = orientation;

		HEIGHT_OF_LIST_HEADER = context.getResources().getDimension(R.dimen.gallery2_list_header_height);
		WIDTH_OF_SELECTED_LIST_LAND = (int)context.getResources().getDimension(R.dimen.gallery2_landscape_selected_list_width);
		
		initRowItemProperties(context);
	}

	
	public static void initRowItemProperties(Context context) {
		initRowItemProperties(context, 0);
	}
	
	public static void initRowItemProperties(Context context, int offset) {
		int widthOfRow = (Utils.getDisplayWidth(context) + offset) - Utils.dp(context, 8.f);	// list의 좌우 패딩 제외(좌우 합친값)
		int widthOfFolderRow = ((Utils.getDisplayWidth(context) * 70 / 100) + offset) - Utils.dp(context, 10.4f);	// list의 좌우 패딩 제외(좌우 합친값)
		int widthOfFolderList = (Utils.getDisplayWidth(context) * 30 / 100)  - Utils.dp(context, 10.4f);	// list의 좌우 패딩 제외(좌우 합친값)
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
	
	public static void setOrientation(int orientation) {
		mOrientation = orientation;
	}



}