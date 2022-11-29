package com.kiwiple.multimedia.canvas;

import java.io.IOException;

import org.json.JSONException;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * CoverTransition.
 */
public final class CoverTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "cover_transition";

	public static final String JSON_NAME_DIRECTION = "direction";
	public static final String JSON_NAME_COVER_IMAGE_RESOURCE = "cover_image_resource";
	public static final String JSON_NAME_MASK_IMAGE_RESOURCE = "mask_image_resource";

	private static final int INDEX_COVER = 0;
	private static final int INDEX_MASK = 1;

	private static final Direction DEFAULT_DIRECTION = Direction.LEFT;

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private Direction mDirection;

	@CacheCode(indexed = true)
	private final ImageResource mImageResources[];

	// // // // // Constructor.
	// // // // //
	{
		mImageResources = new ImageResource[] { null, null };
		setDirection(DEFAULT_DIRECTION);
	}

	CoverTransition(Region parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	Size[] getCanvasRequirement() {

		Resolution resolution = getResolution();
		Size coverSize = mImageResources[INDEX_COVER].measureSize(resolution);
		Size maskSize = mImageResources[INDEX_MASK].measureSize(resolution);

		return new Size[] { coverSize, maskSize };
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_DIRECTION, mDirection);
		jsonObject.putOpt(JSON_NAME_COVER_IMAGE_RESOURCE, mImageResources[INDEX_COVER]);
		jsonObject.putOpt(JSON_NAME_MASK_IMAGE_RESOURCE, mImageResources[INDEX_MASK]);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setDirection(jsonObject.getEnum(JSON_NAME_DIRECTION, Direction.class));

		if (!jsonObject.isNull(JSON_NAME_COVER_IMAGE_RESOURCE) && !jsonObject.isNull(JSON_NAME_MASK_IMAGE_RESOURCE)) {
			Resources resources = getResources();
			ImageResource cover = ImageResource.createFromJsonObject(resources, jsonObject.getJSONObject(JSON_NAME_COVER_IMAGE_RESOURCE));
			ImageResource mask = ImageResource.createFromJsonObject(resources, jsonObject.getJSONObject(JSON_NAME_MASK_IMAGE_RESOURCE));

			setImageResource(cover, mask);
		}
	}

	@Override
	void prepareCanvasWithCache() throws IOException {

		CacheManager cacheManager = getCacheManager();
		cacheManager.decodeImageCache(getCacheCodeChunk(0), getCanvas(0));
		cacheManager.decodeImageCache(getCacheCodeChunk(1), getCanvas(1));
	}

	@Override
	void prepareCanvasWithoutCache() throws IOException {
		PixelExtractUtils.extractARGB(createCacheAsBitmap(0), getCanvas(0), true);
		PixelExtractUtils.extractARGB(createCacheAsBitmap(1), getCanvas(1), true);
	}

	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		float progressRatio = getProgressRatio();
		int width = getWidth();
		int height = getHeight();

		boolean isHorizontal = mDirection.isHorizontal();
		boolean isNegative = mDirection.isNegative();

		PixelCanvas coverCanvas = getCanvas(0);
		PixelCanvas maskCanvas = getCanvas(1);

		srcCanvasFormer.copy(dstCanvas);
		if (isHorizontal) {
			int coverStartX;
			int coverWidth = coverCanvas.getImageWidth();
			int progressWidth = Math.round((coverWidth + width) * progressRatio);

			if (isNegative) {
				coverStartX = width - progressWidth;
				int coverEndX = coverStartX + coverWidth;
				srcCanvasLatter.copy(dstCanvas, coverEndX, 0, coverEndX, 0, width - coverEndX, height);
			} else {
				coverStartX = progressWidth - coverWidth;
				srcCanvasLatter.copy(dstCanvas, 0, 0, 0, 0, coverStartX, height);
			}
			srcCanvasLatter.blendWithMask(dstCanvas, maskCanvas, coverStartX, 0);
			coverCanvas.blend(dstCanvas, coverStartX, 0);

		} else {
			int coverStartY;
			int coverHeight = coverCanvas.getImageHeight();
			int progressHeight = Math.round((coverHeight + height) * progressRatio);

			if (isNegative) {
				coverStartY = height - progressHeight;
				int coverEndY = coverStartY + coverHeight;
				srcCanvasLatter.copy(dstCanvas, 0, coverEndY, 0, coverEndY, width, height - coverEndY);
			} else {
				coverStartY = progressHeight - coverHeight;
				srcCanvasLatter.copy(dstCanvas, 0, 0, 0, 0, width, coverStartY);
			}
			srcCanvasLatter.blendWithMask(dstCanvas, maskCanvas, 0, coverStartY);
			coverCanvas.blend(dstCanvas, 0, coverStartY);
		}
	}

	@Override
	public int getCacheCount() {
		return isValidated() ? 2 : 0;
	}

	void setImageResource(ImageResource coverImageResource, ImageResource maskImageResource) {
		Precondition.checkNotNull(coverImageResource, maskImageResource);

		mImageResources[INDEX_COVER] = coverImageResource;
		mImageResources[INDEX_MASK] = maskImageResource;
	}

	public ImageResource getCoverImageResource() {
		return mImageResources[INDEX_COVER];
	}

	public ImageResource getMaskImageResource() {
		return mImageResources[INDEX_MASK];
	}

	void setDirection(Direction direction) {
		Precondition.checkNotNull(direction);
		mDirection = direction;
	}

	public Direction getDirection() {
		return mDirection;
	}

	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {

		ImageResource imageResource = mImageResources[index];

		Resolution targetResolution = getResolution();
		Bitmap bitmap = imageResource.createBitmap(targetResolution, null, (float) mDirection.degree);

		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		int targetWidth = Math.min(bitmapWidth, targetResolution.width);
		int targetHeight = Math.min(bitmapHeight, targetResolution.height);

		if (mDirection.isHorizontal()) {
			int y = Math.round(bitmapHeight / 2.0f - targetHeight / 2.0f);
			bitmap = Bitmap.createBitmap(bitmap, 0, y, targetWidth, targetHeight);
		}
		return bitmap;
	}

	// // // // // Inner Class.
	// // // // //
	public static final class Editor extends Transition.Editor<CoverTransition, Editor> {

		private Editor(CoverTransition coverTransition) {
			super(coverTransition);
		}

		public Editor setImageResource(ImageResource coverImageResource, ImageResource maskImageResource) {
			getObject().setImageResource(coverImageResource, maskImageResource);
			return this;
		}

		public Editor setDirection(Direction direction) {
			getObject().setDirection(direction);
			return this;
		}
	}

	// // // // // Enumeration.
	// // // // //
	public static enum Direction {

		LEFT(180),

		UP(270),

		RIGHT(0),

		DOWN(90);

		final int degree;

		private Direction(int degree) {
			this.degree = degree;
		}

		boolean isHorizontal() {
			return equals(LEFT) || equals(RIGHT);
		}

		boolean isNegative() {
			return equals(LEFT) || equals(UP);
		}
	}
}