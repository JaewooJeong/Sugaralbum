package com.kiwiple.scheduler.select;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.kiwiple.debug.L;
import com.kiwiple.scheduler.data.OutputData;

/**
 * 출력 데이터 매니저 객체. 
 *
 */
public abstract class OutputDataManager {
	
	protected OutputData mOutputData;
	protected PreviewManager mPreviewManager;
	protected Visualizer mVisualizer;

	/**
	 * 생성자
	 * @param context Context
	 * @param outputData 출력 데이터. 
	 */
	public OutputDataManager(Context context, OutputData outputData) {
		mOutputData = outputData;
		mPreviewManager = PreviewManager.getInstance(context);
		mVisualizer = mPreviewManager.getVisualizer();
//		mPreviewManager.edit().editRegion().removeAll();
//		mPreviewManager.setAudioFile(mOutputData.getAudioPath(), mOutputData.isAudioInAsset());
	}
	
	/**
	 * 출력 데이터를 json object로 반환.  
	 * @return json object. 
	 */
	public JSONObject makeJSONObject() {
		try {
			L.d("output data size : " + mVisualizer.getRegion().getScenes().size()); 
			if (mVisualizer.getRegion().getScenes().size() > 0) {
				return mPreviewManager.toJsonObject();
			} else {
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 출력 데이터에 transition 설정. 
	 * @param transitionJsonObject transition json object
	 */
	public abstract void setOutputDataTransition(JSONObject transitionJsonObject);
	
	
	/**
	 * 출력 데이터에 effect 설정. 
	 * @param jsonEffectObject effect json object
	 */
	public abstract void setOutputDataEffect(JSONObject jsonEffectObject, boolean isChangeTheme, boolean isChangeOrder); 
	
	
	/**
	 * 출력 데이터를 이용해서 각 scene을 구성한다.
	 *  
	 */
	public abstract void setOutputDataScene();
}
