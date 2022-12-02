package com.sugarmount.sugarcamera.story.gallery;

public class ConstantsGallery {
	
	public static final int MAKE_CLIP_SELECT = 0; 
	public static final int COLLAGE_IMAGE_SELECT = 1;
	public static final int COLLAGE_VIDEO_SELECT = 2;
	public static final int SCENARIO_IMAGE_SELECT = 3;
	public static final int SCENARIO_VIDEO_SELECT = 4;
	public static final int SCENARIO_COLLAGE_IMAGE_SELECT = 5; 

	public static final int FRAGMENT_GALLERY = 0;
	public static final int FRAGMENT_STORY = 1;
	
	public static final int GALLERY_TAB_DATE = 0; 
	public static final int GALLERY_TAB_PERSON = 1; 
	public static final int GALLERY_TAB_ANNIVERSARY = 2; 
	
    // ------------ Intent 통신을 위한 RequestCode -------------------------
    public static final int REQ_CODE_EDIT_ANNIVERSARY = 1000;
    public static final int REQ_CODE_EDIT_PERSON = 1001;
    public static final int REQ_CODE_CONTENT_DETAIL = 1002; 
    public static final int REQ_CODE_SETTING = 1003;
    public static final int REQ_CODE_MOVIEDIARY_GALLERY = 1004;
    public static final int REQ_CODE_EDITCOLLAGE = 1005; 
    
    // ------------ Intent 통신을 위한 Extra Key Value -------------------------
	public static final String EXTRA_KEY_GALLERY_ADD_ANNIVERSARY_FROM_GALLERY = "addAnniversaryFromGallery";

}
