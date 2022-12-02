package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.util.Random;

import org.json.JSONException;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.kiwiple.multimedia.R;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Randomizer;
import com.kiwiple.multimedia.util.Size;

/**
 * {@link Scene}이 생성한 이미지에 먼지나 흠이 쌓인 오래된 필름이 영사될 때 나올 법한 노이즈를 덧붙이는 클래스.
 */
public final class NoiseEffect extends Effect {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "noise_effect";

	private static final String JSON_NAME_INTERNAL_SEED = "internal_seed";

	private static final int[] sNoiseResourceIds = { //
			/**/ R.drawable.film_noise_01, /**/
			/**/ R.drawable.film_noise_02, /**/
			/**/ R.drawable.film_noise_03, /**/
			/**/ R.drawable.film_noise_04, /**/
			/**/ R.drawable.film_noise_05, /**/
			/**/ R.drawable.film_noise_06, /**/
			/**/ R.drawable.film_noise_07, /**/
			/**/ R.drawable.film_noise_08, /**/
			/**/ R.drawable.film_noise_09, /**/
			/**/ R.drawable.film_noise_10, /**/
			/**/ R.drawable.film_noise_11, /**/
			/**/ R.drawable.film_noise_12, /**/
			/**/ R.drawable.film_noise_13, /**/
			/**/ R.drawable.film_noise_14 /* */
	};

	// // // // // Member variable.
	// // // // //
	private final Randomizer mRandomizer;

	private ImageResource[] mImageResources;
	private ImageResourceInfo[] mImageResourceInfos;

	private Size mCanvasSize;

	// // // // // Constructor.
	// // // // //
	NoiseEffect(Scene parent) {
		super(parent);
		mRandomizer = new Randomizer((0x0000800 + new Random().nextInt(0x00000800)) | 0x00000001);

		int arrayLength = sNoiseResourceIds.length;
		mImageResources = new ImageResource[arrayLength];
		mImageResourceInfos = new ImageResourceInfo[arrayLength];

		Resources resources = getResources();
		Resolution resolution = getResolution();
		int sizeAmount = 0;

		for (int i = 0; i != sNoiseResourceIds.length; ++i) {

			mImageResources[i] = ImageResource.createFromDrawable(sNoiseResourceIds[i], resources, Resolution.FHD);
			Size size = mImageResources[i].measureSize(resolution);

			mImageResourceInfos[i] = new ImageResourceInfo(size, sizeAmount);
			sizeAmount += size.product();
		}
		mCanvasSize = new Size(sizeAmount, 1);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_INTERNAL_SEED, mRandomizer.getSeed());
		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		mRandomizer.setSeed(jsonObject.getInt(JSON_NAME_INTERNAL_SEED));
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {

		int randomNumber = mRandomizer.randomizeAbs(getPosition());

		int noiseCount = randomNumber % 4 + 1;
		int width = getWidth();
		int height = getHeight();

		for (int i = 0; i != noiseCount; ++i) {

			int infoIndex = randomNumber % sNoiseResourceIds.length;
			ImageResourceInfo info = mImageResourceInfos[infoIndex];
			randomNumber >>>= 1;

			int dstX = randomNumber % (width - info.size.width);
			int dstY = randomNumber % (height - info.size.height);

			PixelCanvas canvas = getCanvas(0);
			canvas.setImageSize(info.size);
			canvas.setOffset(info.bufferOffset);
			canvas.blend(dstCanvas, dstX, dstY);
		}
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { mCanvasSize };
	}

	@Override
	public int getCacheCount() {
		return 1;
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

		Resolution resolution = getResolution();
		int[] buffer = new int[mCanvasSize.product()];

		for (int i = 0, bufferOffset = 0; i != mImageResources.length; ++i) {
			Size size = mImageResourceInfos[i].size;
			Bitmap bitmap = mImageResources[i].createBitmap(resolution);
			bitmap.getPixels(buffer, bufferOffset, size.width, 0, 0, size.width, size.height);
			bufferOffset += size.product();
		}
		return Bitmap.createBitmap(buffer, mCanvasSize.width, mCanvasSize.height, BITMAP_CONFIG);
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link NoiseEffect}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Effect.Editor<NoiseEffect, Editor> {

		private Editor(NoiseEffect noiseEffect) {
			super(noiseEffect);
		}
	}

	private static class ImageResourceInfo {

		final Size size;
		final int bufferOffset;

		ImageResourceInfo(Size size, int bufferOffset) {

			this.size = size;
			this.bufferOffset = bufferOffset;
		}
	}
}
