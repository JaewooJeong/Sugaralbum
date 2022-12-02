package com.sugarmount.sugarcamera.story.gallery;

import java.util.ArrayList;

public class RowMediaData {
	
	// header
	public long dateAdded = -1;	// default is -1
	public int photoCount;
	public int videoCount;
	//haed and tail 
	public boolean bHead = false; 
	public boolean bTail = false; // default is false
	//anniversayDay tab title; 
	public String anniversaryTitle = "";
	//person tab name
	public String personName = ""; 
	
	public String folderName = ""; 
	// checked
	public boolean checked;
	public int headerRowPosition;	// header의 위치를 저장해놓고 헤더의 체크나 상태를 변경해야 하는 경우에 쉽게 찾을 수 있게 함.
	public boolean invalidVideo = false; 
	// content validate
	public int invalidCount;
	// media 
	public ItemMediaData[] medias;
	
	public boolean invalid() {
		return (photoCount + videoCount) == invalidCount;
	}
	public boolean bClicked = false;  
	
}
