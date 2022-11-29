package com.kiwiple.scheduler.coordinate.transition.uplus;

import android.content.Context;

import com.kiwiple.multimedia.canvas.CircleTransition;
import com.kiwiple.multimedia.canvas.CoverTransition;
import com.kiwiple.multimedia.canvas.EnterTransition;
import com.kiwiple.multimedia.canvas.ExtendBoxTransition;
import com.kiwiple.multimedia.canvas.FadeTransition;
import com.kiwiple.multimedia.canvas.FlashTransition;
import com.kiwiple.multimedia.canvas.GrandUnionTransition;
import com.kiwiple.multimedia.canvas.MetroTransition;
import com.kiwiple.multimedia.canvas.OverlayTransition;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.SpinTransition;
import com.kiwiple.multimedia.canvas.SplitTransition;
import com.kiwiple.scheduler.coordinate.transition.CircleTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.CoverTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.CutTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.EnterTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.ExtendBoxTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.FadeTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.FlashTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.GrandUnionTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.MetrotransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.OverlayTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.SpinTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.SplitTransitionCoordinator;
import com.kiwiple.scheduler.coordinate.transition.TransitionApplyManager;
import com.kiwiple.scheduler.data.TransitionData;

public class UplusTransitionApplyManager extends TransitionApplyManager {
//	private static String[] TRANSITION_TYPE = { SplitTransition.JSON_VALUE_TYPE, FadeTransition.JSON_VALUE_TYPE, SpinTransition.JSON_VALUE_TYPE };

	private CircleTransitionCoordinator mCircleTransitioinCoordinator;
	private SplitTransitionCoordinator mSplitTransitionCoordinator;
	private CoverTransitionCoordinator mCoverTransitionCoordinator;
	private FadeTransitionCoordinator mFadeTransitionCoordinator;
	private FlashTransitionCoordinator mFlashTransitionCoordinator;
	private SpinTransitionCoordinator mSpinTransitionCoordinator;
	private CutTransitionCoordinator mCutTransitionCoordinator; 
	private OverlayTransitionCoordinator mOverlayTransitionCoordinator; 
	private MetrotransitionCoordinator mMetroTransitionCoordinator; 
	private ExtendBoxTransitionCoordinator mExtendBoxTransiionCoordinator;
	private GrandUnionTransitionCoordinator mGrandUnionTransitionCoordinatior;
	private EnterTransitionCoordinator mEnterTransitionCoordinator; 

	public UplusTransitionApplyManager(Context context) {
		mSplitTransitionCoordinator = new SplitTransitionCoordinator();
		mCoverTransitionCoordinator = new CoverTransitionCoordinator(context);
		mFadeTransitionCoordinator = new FadeTransitionCoordinator();
		mFlashTransitionCoordinator = new FlashTransitionCoordinator();
		mSpinTransitionCoordinator = new SpinTransitionCoordinator();
		mCutTransitionCoordinator = new CutTransitionCoordinator(); 
		mOverlayTransitionCoordinator = new OverlayTransitionCoordinator();
		mMetroTransitionCoordinator = new MetrotransitionCoordinator(); 
		mCircleTransitioinCoordinator = new CircleTransitionCoordinator();
		mExtendBoxTransiionCoordinator = new ExtendBoxTransitionCoordinator();
		mGrandUnionTransitionCoordinatior = new GrandUnionTransitionCoordinator();
		mEnterTransitionCoordinator = new EnterTransitionCoordinator(); 
	}

	/**
	 * transitionData를 통해서 transition을 구성.<br>
	 * @param regionEditor : 현재 편집되고 있는 regionEditor.<br>
	 * @param i : 삽입될 위치. <br>
	 * @param transitionData : transition data. <br>
	 * @param type : transition type. <br>
	 *//*
	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		String transitionType = TRANSITION_TYPE[(int) (Math.random() * TRANSITION_TYPE.length)];
		if (transitionType.equals(SplitTransition.JSON_VALUE_TYPE)) {
			mSplitTransitionCoordinator.applyTransition(regionEditor, transitionIndex);
		} else if (transitionType.equals(CoverTransition.JSON_VALUE_TYPE)) {
			mCoverTransitionCoordinator.applyTransition(regionEditor, transitionIndex);
		} else if (transitionType.equals(FadeTransition.JSON_VALUE_TYPE)) {
			mFadeTransitionCoordinator.applyTransition(regionEditor, transitionIndex);
		} else if (transitionType.equals(FlashTransition.JSON_VALUE_TYPE)) {
			mFlashTransitionCoordinator.applyTransition(regionEditor, transitionIndex);
		} else if (transitionType.equals(SpinTransition.JSON_VALUE_TYPE)) {
			mSpinTransitionCoordinator.applyTransition(regionEditor, transitionIndex);
		}else if (transitionType.equals("cut")) {
			mCutTransitionCoordinator.applyTransition(regionEditor, transitionIndex);
		}		
	}*/
	/**
	 * transitionData를 통해서 transition을 구성.<br>
	 * @param regionEditor : 현재 편집되고 있는 regionEditor.<br>
	 * @param i : 삽입될 위치. <br>
	 * @param transitionData : transition data. <br>
	 * @param type : transition type. <br>
	 */
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData, String type) {
		if(type.equals(FadeTransition.JSON_VALUE_TYPE)){
			mFadeTransitionCoordinator.applyTransition(regionEditor, i, transitionData); 
		}else if(type.equals(SplitTransition.JSON_VALUE_TYPE)){
			mSplitTransitionCoordinator.applyTransition(regionEditor, i, transitionData); 
		}else if(type.equals(SpinTransition.JSON_VALUE_TYPE)){
			mSpinTransitionCoordinator.applyTransition(regionEditor, i, transitionData); 
		}else if(type.equals(CoverTransition.JSON_VALUE_TYPE)){
			mCoverTransitionCoordinator.applyTransition(regionEditor, i, transitionData); 
		}else if(type.equals(ExtendBoxTransition.JSON_VALUE_TYPE)){
			mExtendBoxTransiionCoordinator.applyTransition(regionEditor, i, transitionData);
		}else if(type.equals(GrandUnionTransition.JSON_VALUE_TYPE)){
			mGrandUnionTransitionCoordinatior.applyTransition(regionEditor, i, transitionData);
		}else if (type.equals(FlashTransition.JSON_VALUE_TYPE)){
			mFlashTransitionCoordinator.applyTransition(regionEditor, i, transitionData); 
		}else if (type.equals("cut")) {
			mCutTransitionCoordinator.applyTransition(regionEditor, i, transitionData);
		}else if(type.equals(OverlayTransition.JSON_VALUE_TYPE)){
			mOverlayTransitionCoordinator.applyTransition(regionEditor, i, transitionData); 
		}else if(type.equals(MetroTransition.JSON_VALUE_TYPE)){
			mMetroTransitionCoordinator.applyTransition(regionEditor, i, transitionData); 
		}else if(type.equals(CircleTransition.JSON_VALUE_TYPE)){
			mCircleTransitioinCoordinator.applyTransition(regionEditor, i, transitionData); 
		}else if(type.equals(EnterTransition.JSON_VALUE_TYPE)){
			mEnterTransitionCoordinator.applyTransition(regionEditor, i, transitionData); 
		}
	}
}
