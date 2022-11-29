package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import android.graphics.Bitmap;

import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.annotation.Child;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * 이미지 파일을 출력하는 클래스.
 * 
 * @see Scene
 */
public final class BurstShotScene extends Scene {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "burst_shot_scene";
	public static final String JSON_NAME_FILE_PATH = "file_path";
	public static final String JSON_NAME_OBJECT = "burst_shot_object";
	public static final String JSON_NAME_FRAME_DURATION = "frame_duration";
	private static final float FRAME_DURATION = 0.05f;

	// // // // // Member variable.
	// // // // //
	@CacheCode(indexed = true)
	private ArrayList<ImageResource> mImageResources = new ArrayList<ImageResource>();

	@CacheCode
	private FilterApplier mFilterApplier = FilterApplier.INVALID_OBJECT;
	private float mDuration = FRAME_DURATION;
	private int mCurrentIndex = 0;

	private ArrayList<Integer> mImageIds = new ArrayList<Integer>();
	
	@Child
	private Scaler mScaler ;
	private static final Class<? extends Scaler> CENTER_SCALER = CenterCropScaler.class;

	// // // // // Constructor.
	// // // // //
	BurstShotScene(Region parent) {
		super(parent);
		mScaler = CanvasUserFactory.createScaler(CENTER_SCALER, this);
	}


	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas dstCanvas) {
		
		int currentIndex = Math.round(getProgressRatio() / mDuration) % mImageResources.size();
		if(mCurrentIndex != currentIndex) mCurrentIndex = currentIndex;
			
		mScaler.draw(getCanvas(mCurrentIndex), dstCanvas);
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		JsonArray jsonArray = new JsonArray();

		for(int i = 0;i<mImageResources.size();i++){
			JsonObject imageObject = new JsonObject();
			imageObject.put(JSON_NAME_FILE_PATH, getImageFilePath(i));
			imageObject.put(JSON_NAME_IMAGE_ID, mImageIds.get(i));
			jsonArray.put(imageObject);
		}
		
		jsonObject.put(JSON_NAME_OBJECT,jsonArray);
		jsonObject.put(JSON_NAME_FRAME_DURATION, getFrameDuration());
		jsonObject.putOpt(JSON_NAME_FILTER_ID, mFilterApplier);
		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		
		ArrayList<String> filePaths = new ArrayList<String>();
		ArrayList<Integer> fileIds = new ArrayList<Integer>();
		JsonArray jsonArray = jsonObject.getJSONArray(JSON_NAME_OBJECT);
		for(int i = 0;i<jsonArray.length();i++){
			JsonObject subObject = jsonArray.getJSONObject(i);
			String filePath = subObject.getString(JSON_NAME_FILE_PATH);
			int imageId = subObject.optInt(JSON_NAME_IMAGE_ID);
			fileIds.add(imageId);
			filePaths.add(filePath);
		}
		
		setImageFilePath(filePaths);
		setImageId(fileIds);
		if(!jsonObject.isNull(JSON_NAME_FRAME_DURATION)) setFrameDuration(jsonObject.getInt(JSON_NAME_FRAME_DURATION));
		setFilterId(jsonObject.optInt(JSON_NAME_FILTER_ID, FilterApplier.INVALID_FILTER_ID));
	}

	@Override
	void prepareCanvasWithCache() throws IOException {
		for(int i = 0;i<getCacheCount();i++){
			getCacheManager().decodeImageCache(getCacheCodeChunk(i), getCanvas(i));
		}
	}

	@Override
	void prepareCanvasWithoutCache() throws IOException {
		for(int i = 0;i<getCacheCount();i++){
			PixelExtractUtils.extractARGB(createCacheAsBitmap(i), getCanvas(i), true);
		}
	}
	
	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {
		Resolution resolution = getResolution();
			
		Bitmap bitmap = mImageResources.get(index).createBitmap(resolution);

		if (mFilterApplier.isVaild()) {
			mFilterApplier.apply(bitmap);
		}
		return bitmap;
	}

	@Override
	public int getCacheCount() {
		return mImageResources.size();
	}

	@Override
	Size[] getCanvasRequirement(){
		Size[] size = new Size[mImageResources.size()];
		
		for(int index = 0;index < mImageResources.size();index++){
			size[index] = mImageResources.get(index).measureSize(getResolution());
		}
		return  size;
	}

	void setImageFilePath(ArrayList<String> imageFilePaths) {
		mImageResources.clear();
		for(int i = 0;i<imageFilePaths.size();i++) {
			mImageResources.add(ImageResource.createFromFile(imageFilePaths.get(i), ImageResource.ScaleType.BUFFER));
		}
	}

	/**
	 * 이미지를 그릴 때 사용하는 원본 이미지 파일의 절대 경로를 문자열로써 반환합니다.
	 */
	public String getImageFilePath(int index) {
		
		return ((FileImageResource) mImageResources.get(index)).getFilePath();
	}

	void setFilterId(int filterId) {
		mFilterApplier = createFilterApplier(filterId);
	}
	
	void setFrameDuration(int duration){
		mDuration = ((float)duration) * 0.001f;
	}
	
	public int getFrameDuration(){
		return (int) (mDuration * 1000f);
	}
	

	/**
	 * 이미지를 그릴 때 적용하는 필터의 식별자를 반환합니다.
	 */
	public int getFilterId() {
		return mFilterApplier.getFilterId();
	}

	/**
	 * 사용자 임의 이미지 식별자를 등록합니다.
	 * <p />
	 * 본 메서드는 {@code ImageFileScene}의 기능에 아무런 영향을 미치지 않습니다.
	 * 
	 * @param imageId
	 *            등록할 이미지 식별자.
	 */
	public void setImageId(ArrayList<Integer> imageIds) {
		mImageIds.clear();
		for(int i = 0;i<imageIds.size();i++)  mImageIds.add(imageIds.get(i));
	}

	/**
	 * 사용자 임의 이미지 식별자를 반환합니다.
	 */
	public int getImageId(int index) {
		return mImageIds.get(index);
	}
	
	public int getImageSize(){
		return mImageResources.size();
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link ImageFileScene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<BurstShotScene, Editor> {

		private Editor(BurstShotScene imageFileScene) {
			super(imageFileScene);
		}

		/**
		 * 이미지를 그릴 때 사용할 원본 이미지 파일의 절대 경로를 설정합니다.
		 * 
		 * @param imageFilePath
		 *            원본 이미지 파일의 절대 경로.
		 */
		public Editor setImageFilePath(ArrayList<String> imageFilePaths) {
			getObject().setImageFilePath(imageFilePaths);
			return this;
		}
		
		public Editor setImageId(ArrayList<Integer> imageIds){
			getObject().setImageId(imageIds);
			return this;
		}
		
		public Editor setFrameDuration(int duration){
			getObject().setFrameDuration(duration);
			return this;
		}

		/**
		 * 이미지를 그릴 때 적용할 필터의 식별자를 설정합니다.
		 * 
		 * @param filterId
		 *            적용할 필터의 식별자.
		 */
		public Editor setFilterId(int filterId) {
			getObject().setFilterId(filterId);
			return this;
		}
	}
}
