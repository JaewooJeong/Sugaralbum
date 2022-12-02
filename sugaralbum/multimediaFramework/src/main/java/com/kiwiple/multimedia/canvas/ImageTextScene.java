package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import com.kiwiple.debug.Precondition;
import com.kiwiple.imageframework.filter.Filter;
import com.kiwiple.imageframework.filter.FilterManagerWrapper;
import com.kiwiple.imageframework.filter.live.LiveFilterController;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.canvas.data.TextElement;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.CollectionUtils;
import com.kiwiple.multimedia.util.ImageUtils;
import com.kiwiple.multimedia.util.Size;

/**
 * ImageTextScene.
 * 
 */
public final class ImageTextScene extends Scene {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "image_text_scene";

	public static final String JSON_NAME_TEXT_ELEMENTS = "text_elements";
	public static final String JSON_NAME_LINE_SPACE = "line_space";
	public static final String JSON_NAME_BACKGROUND_IMAGE_FILE_PATH = "background_image_file_path";
	public static final String JSON_NAME_BACKGROUND_VIEWPORT = "background_viewport";

	public static final String JSON_NAME_VIDEO_FRAME_POSITION = "video_frame_position";

	// // // // // Enumeration.
	// // // // //
	public static enum Alignment {
		CENTER;
	}

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private List<TextElement> mTextElements;

	private Alignment mHorizontalAlignment = Alignment.CENTER;
	private Alignment mVerticalAlignment = Alignment.CENTER;

	@CacheCode
	private String mBackgroundImageFilePath;
	@CacheCode
	private Viewport mBackgroundViewport;
	private int mLineSpace;

	@CacheCode
	private long mVideoFramePositionMs;

	// // // // // Constructor.
	// // // // //
	ImageTextScene(Region parent) {
		super(parent);
	}

	ImageTextScene(MultiLayerScene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas dstCanvas) {
		getCanvas(0).copy(dstCanvas);
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_LINE_SPACE, mLineSpace);
		jsonObject.putOpt(JSON_NAME_BACKGROUND_IMAGE_FILE_PATH, mBackgroundImageFilePath);
		jsonObject.putOpt(JSON_NAME_BACKGROUND_VIEWPORT, mBackgroundViewport);
		jsonObject.putOpt(JSON_NAME_TEXT_ELEMENTS, mTextElements);
		jsonObject.putOpt(JSON_NAME_VIDEO_FRAME_POSITION, mVideoFramePositionMs, 0);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setLineSpace(jsonObject.getInt(JSON_NAME_LINE_SPACE));
		setBackgroundFilePath(jsonObject.optString(JSON_NAME_BACKGROUND_IMAGE_FILE_PATH));
		setVideoFramePosition(jsonObject.optInt(JSON_NAME_VIDEO_FRAME_POSITION, 0));

		JsonObject viewportJsonObject = jsonObject.optJSONObject(JSON_NAME_BACKGROUND_VIEWPORT);
		if (viewportJsonObject != null) {
			setBackgroundViewport(new Viewport(viewportJsonObject));
		}

		if (!jsonObject.isNull(JSON_NAME_TEXT_ELEMENTS)) {
			ArrayList<TextElement> textElements = new ArrayList<TextElement>();
			for (JsonObject textElementJsonObject : jsonObject.getJSONArray(JSON_NAME_TEXT_ELEMENTS).asList(JsonObject.class)) {
				textElements.add(new TextElement(textElementJsonObject));
			}
			setTextElements(textElements);
		}
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		checkValidity(mTextElements != null, "You must invoke setTextElements()");
	}

	@Override
	void prepareCanvasWithCache() throws IOException {
		getCacheManager().decodeImageCache(getCacheCodeChunk(0), getCanvas(0));
	}

	@Override
	void prepareCanvasWithoutCache() throws IOException {
		PixelExtractUtils.extractARGB(createCacheAsBitmap(0), getCanvas(0), true);
	}

	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {

		if (index == 0) {
			int targetWidth = getWidth();
			int targetHeight = getHeight();
			float sizeMultiplier = getResolution().magnification;

			Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, BITMAP_CONFIG);

			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.BLACK);

			if (mBackgroundImageFilePath != null && !mBackgroundImageFilePath.isEmpty()) {

				Bitmap backgroundBitmap = ImageUtils.getImage(mBackgroundImageFilePath, getResolution(), mVideoFramePositionMs);
				if (backgroundBitmap != null) {

					Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
					paint.setAlpha(255);

					if (mBackgroundViewport == null) {
						float left = (targetWidth - backgroundBitmap.getWidth()) / 2.0f;
						float top = (targetHeight - backgroundBitmap.getHeight()) / 2.0f;
						canvas.drawBitmap(backgroundBitmap, left, top, paint);

					} else {
						int imageWidth = backgroundBitmap.getWidth();
						int imageHeight = backgroundBitmap.getHeight();
						float left = imageWidth * mBackgroundViewport.left;
						float top = imageHeight * mBackgroundViewport.top;
						float scale = targetWidth / (imageWidth * mBackgroundViewport.width());

						Matrix matrix = new Matrix();
						matrix.postTranslate(-left, -top);
						matrix.postScale(scale, scale);

						canvas.drawBitmap(backgroundBitmap, matrix, paint);
					}
					backgroundBitmap.recycle();

					LiveFilterController liveFilterController = getLiveFilterController();
					FilterManagerWrapper filterManager = getFilterManager();
					Filter filter = new Filter();
					filter.mArtFilter.mFilterName = ArtFilterUtils.sImageGaussianBlurFilter.getFilterInfo().filterName;
					filter.mArtFilter.mParams.add("1");
					filter.mArtFilter.mParamCount = 1;
					liveFilterController.applyFilter(filterManager, filter, bitmap, bitmap);
				}
			}

			if (mTextElements != null && mTextElements.size() > 0) {

				Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
				paint.setTextAlign(Paint.Align.CENTER);
				paint.setColor(Color.WHITE); // FIXME: at sometime.

				Rect bounds = new Rect();
				int lineSpace = Math.round(mLineSpace * sizeMultiplier);
				int textAreaHeight = lineSpace * (mTextElements.size() - 1);

				for (TextElement textElement : mTextElements) {

					String text = textElement.getText();
					paint.setTextSize(textElement.getSize() * sizeMultiplier);
					paint.getTextBounds(text, 0, Math.min(1, text.length()), bounds);
					textAreaHeight += bounds.height();
				}

				float posX = targetWidth / 2.0f;
				float posY = targetHeight / 2.0f - textAreaHeight / 2.0f;

				for (TextElement textElement : mTextElements) {

					String text = textElement.getText();
					paint.setTextSize(textElement.getSize() * sizeMultiplier);
					paint.getTextBounds(text, 0, Math.min(1, text.length()), bounds);

					posY += bounds.height() / 2.0f + paint.descent();
					canvas.drawText(text, posX, posY, paint);
					posY += bounds.height() / 2.0f + lineSpace;
				}
			}
			return bitmap;
		}
		return Precondition.assureUnreachable();
	}

	@Override
	public int getCacheCount() {
		return 1;
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { getSize() };
	}

	void setHorizontalAlignment(Alignment alignment) {
		Precondition.checkNotNull(alignment);
		mHorizontalAlignment = alignment;
	}

	public Alignment getHorizontalAlignment() {
		return mHorizontalAlignment;
	}

	void setVerticalAlignment(Alignment alignment) {
		Precondition.checkNotNull(alignment);
		mVerticalAlignment = alignment;
	}

	public Alignment getVerticalAlignment() {
		return mVerticalAlignment;
	}

	void setBackgroundFilePath(String imageFilePath) {
		mBackgroundImageFilePath = imageFilePath;
	}

	public String getBackgroundFilePath() {
		return mBackgroundImageFilePath;
	}

	void setBackgroundViewport(Viewport viewport) {
		mBackgroundViewport = viewport;
	}

	public Viewport getBackgroundViewport() {
		return mBackgroundViewport;
	}

	void setLineSpace(int lineSpacePx) {
		Precondition.checkNotNegative(lineSpacePx);
		mLineSpace = lineSpacePx;
	}

	public int getLineSpace() {
		return mLineSpace;
	}

	void setTextElements(List<TextElement> textElements) {
		Precondition.checkCollection(textElements).checkNotEmpty().checkNotContainsNull();
		mTextElements = CollectionUtils.deepClone(textElements);
	}

	public List<TextElement> getTextElements() {
		return CollectionUtils.deepClone(mTextElements);
	}

	void setVideoFramePosition(long framePositionMs) {
		Precondition.checkNotNegative(framePositionMs);
		mVideoFramePositionMs = framePositionMs;
	}

	public long getVideoFramePosition() {
		return mVideoFramePositionMs;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link ImageTextScene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<ImageTextScene, Editor> {

		private Editor(ImageTextScene textScene) {
			super(textScene);
		}

		public Editor setTextElements(List<TextElement> textElements) {
			getObject().setTextElements(textElements);
			return this;
		}

		public Editor setBackgroundFilePath(String imageFilePath) {
			getObject().setBackgroundFilePath(imageFilePath);
			return this;
		}

		public Editor setBackgroundViewport(Viewport viewport) {
			getObject().setBackgroundViewport(viewport);
			return this;
		}

		public Editor setLineSpace(int lineSpacePx) {
			getObject().setLineSpace(lineSpacePx);
			return this;
		}

		public Editor setVideoFramePosition(long framePositionMs) {
			getObject().setVideoFramePosition(framePositionMs);
			return this;
		}
	}
}
