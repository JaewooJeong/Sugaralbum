package com.kiwiple.scheduler.coordinate.transition;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.CoverTransition;
import com.kiwiple.multimedia.canvas.FileImageResource;
import com.kiwiple.multimedia.canvas.ImageResource;
import com.kiwiple.multimedia.canvas.CoverTransition.Direction;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.scheduler.R;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusCoverTransitionData;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.ResourceType;

public class CoverTransitionCoordinator extends TransitionCoordinator {
	
	
	public static final String COVER_TRANSITION_LOVE_DOWNLOAD = "cover_transition_love.png"; 
	public static final String MASK_TRANSITION_LOVE_DOWNLOAD= "cover_transition_love_mask.png"; 

	public static final String COVER_TRANSITION_TRAVEL_DOWNLOAD = "cover_transition_travel.png"; 
	public static final String MASK_TRANSITION_TRAVEL_DOWNLOAD= "cover_transition_travel_mask.png"; 
	
	public static final String COVER_TRANSITION_CHRISTMAS_DOWNLOAD = "cover_transition_christmas.png"; 
	public static final String MASK_TRANSITION_CHRISTMAS_DOWNLOAD= "cover_transition_christmas_mask.png"; 

	private static Direction[] COVER_TRANSITION_DIRECTION = { CoverTransition.Direction.LEFT, CoverTransition.Direction.RIGHT,
			CoverTransition.Direction.UP, CoverTransition.Direction.DOWN };

	private Context mContext;
	
	public CoverTransitionCoordinator(Context context) {
		mContext = context;
	}
	
	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		CoverTransition.Editor transitionEditor = regionEditor.replaceTransition(CoverTransition.class, transitionIndex).getEditor();
		transitionEditor.setDuration(TRANSITION_DEFAULT_DURATION);
		transitionEditor.setDirection(COVER_TRANSITION_DIRECTION[(int) (Math.random() * COVER_TRANSITION_DIRECTION.length)]);
	}

	@Override
	public void applyTransition(Editor regionEditor, int index, TransitionData transitionData) {
		CoverTransition.Editor transitionEditor = regionEditor.replaceTransition(CoverTransition.class, index).getEditor();
		UplusCoverTransitionData coverTransitionData = (UplusCoverTransitionData)transitionData; 
		Theme theme = coverTransitionData.getTheme();
		String themeName = theme.name;
		
		Resolution maskResolution = Resolution.NHD; 
		if(theme.name.equals(Theme.THEME_NAME_CHRISTMAS)){
			maskResolution = Resolution.FHD; 
		}else{
			maskResolution = Resolution.NHD;
		}
		//20151028  : 테마별로 다른 CoverTransition Resource를 사용해야 한다.
		if(theme != null && !TextUtils.isEmpty(themeName)){
			
			L.i("resourceType : "+ theme.resourceType);
			List<ArrayList<String>> coverTransitionList = theme.coverTransitionResList;
			
			if(theme.resourceType == ResourceType.DOWNLOAD && coverTransitionList != null && coverTransitionList.size() > 0){
				Context context = coverTransitionData.getContext();
				String coverFilePath = null;
				String maskFilePath = null;
				
				int resSize = coverTransitionList.size();
				L.e("theme : "+ themeName +", download C/T Res size : "+ coverTransitionList.size());
				// Case1  다운로드 테마가 가지는 CoverTransition resource를 적용 
				index = index % resSize;
				coverFilePath = theme.combineDowloadImageFilePath(context, coverTransitionList.get(index).get(0));
				maskFilePath = theme.combineDowloadImageFilePath(context, coverTransitionList.get(index).get(1));

				if(!TextUtils.isEmpty(coverFilePath) && !TextUtils.isEmpty(maskFilePath)){
					ImageResource cover = ImageResource.createFromFile(coverFilePath, Resolution.FHD);
					ImageResource mask = ImageResource.createFromFile(maskFilePath, maskResolution);
					transitionEditor.setImageResource(cover, mask);
				}
				
			}else {
				//다운로드 테마에 리소스가 없을경우 asset이 가지는 default cover resource를 적용
				//asset
				ImageResource cover = ImageResource.createFromDrawable(R.drawable.cover_transition_baby, mContext.getResources(), Resolution.FHD);
				ImageResource mask = ImageResource.createFromDrawable(R.drawable.cover_transition_baby_mask, mContext.getResources(), maskResolution);
				transitionEditor.setImageResource(cover, mask);
			}
		}else{
			//만약 테마 정보가 없다면 asset이 가지는 default cover resource를 적용
			ImageResource cover = ImageResource.createFromDrawable(R.drawable.cover_transition_baby, mContext.getResources(), Resolution.FHD);
			ImageResource mask = ImageResource.createFromDrawable(R.drawable.cover_transition_baby_mask, mContext.getResources(), maskResolution);
			transitionEditor.setImageResource(cover, mask);
		}
		
		transitionEditor.setDuration(coverTransitionData.getDuration());
		transitionEditor.setDirection(coverTransitionData.getDirection());
	}
}
