package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.util.Locale;

import org.json.JSONException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.webkit.MimeTypeMap;

import com.kiwiple.debug.Precondition;
import com.kiwiple.debug.PreconditionException;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.exception.InvalidFileException;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.ImageUtils;
import com.kiwiple.multimedia.util.Size;

/**
 * 다양한 형태의 이미지 자원을 통합 관리하기 위한 클래스. 이미지 자원에 대한 무결성을 객체 생성 중에 자체적으로 검사하며, {@link Bitmap}을 생성하여 제공할 수
 * 있습니다.
 */
public abstract class ImageResource implements ICacheCode, IJsonConvertible {

	// // // // // Static variable.
	// // // // //
	public static final String DEFAULT_JSON_NAME = "image_resource";

	public static final String JSON_NAME_DRAWABLE_ID = "drawable_id";
	public static final String JSON_NAME_DRAWABLE_FULL_NAME = "drawable_full_name";
	public static final String JSON_NAME_FILE_PATH = "file_path";
	public static final String JSON_NAME_BASE_RESOLUTION = "base_resolution";
	public static final String JSON_NAME_SCALE_TYPE = "scale_type";
	public static final String JSON_NAME_RESOURCE_TYPE = ResourceType.DEFAULT_JSON_NAME;
	public static final String JSON_NAME_VIDEO_FRAME_POSITION = "video_frame_position";

	private static final String RESOURCE_TYPE_DRAWABLE = "drawable";

	static final String MIME_TYPE_IMAGE_PREFIX = "image/";
	static final String MIME_TYPE_VIDEO_PREFIX = "video/";

	// // // // // Member variable.
	// // // // //
	private final ResourceType mResourceType;
	private final ScaleType mScaleType;
	private final Resolution mResolution;

	// // // // // Static method.
	// // // // //
	public static FileImageResource createFromFile(String filePath, ScaleType scaleType) {
		return new FileImageResource(filePath, scaleType, null);
	}

	public static FileImageResource createFromFile(String filePath, Resolution resolution) {
		return new FileImageResource(filePath, null, resolution);
	}

	public static VideoFileImageResource createFromVideoFile(String videoFilePath, ScaleType scaleType, int framePositionMs) {
		return new VideoFileImageResource(videoFilePath, scaleType, null, framePositionMs);
	}

	public static VideoFileImageResource createFromVideoFile(String videoFilePath, Resolution resolution, int framePositionMs) {
		return new VideoFileImageResource(videoFilePath, null, resolution, framePositionMs);
	}

	public static AssetImageResource createFromAsset(String filePath, Resources resources, ScaleType scaleType) {
		return new AssetImageResource(filePath, resources, scaleType, null);
	}

	public static AssetImageResource createFromAsset(String filePath, Resources resources, Resolution resolution) {
		return new AssetImageResource(filePath, resources, null, resolution);
	}

	public static DrawableImageResource createFromDrawable(int drawableId, Resources resources, ScaleType scaleType) {
		return new DrawableImageResource(drawableId, resources, scaleType, null);
	}

	public static DrawableImageResource createFromDrawable(int drawableId, Resources resources, Resolution resolution) {
		return new DrawableImageResource(drawableId, resources, null, resolution);
	}

	public static DrawableImageResource createFromDrawable(String drawableName, Context context, ScaleType scaleType) {
		String drawableFullName = String.format("%s:%s/%s", context.getPackageName(), RESOURCE_TYPE_DRAWABLE, drawableName);
		return new DrawableImageResource(drawableFullName, context.getResources(), scaleType, null);
	}

	public static DrawableImageResource createFromDrawable(String drawableName, Context context, Resolution resolution) {
		String drawableFullName = String.format("%s:%s/%s", context.getPackageName(), RESOURCE_TYPE_DRAWABLE, drawableName);
		return new DrawableImageResource(drawableFullName, context.getResources(), null, resolution);
	}

	static ImageResource createFromJsonObject(Resources resources, JsonObject jsonObject) throws JSONException {

		ResourceType type = jsonObject.getEnum(JSON_NAME_RESOURCE_TYPE, ResourceType.class);
		ScaleType scaleType = jsonObject.optEnum(JSON_NAME_SCALE_TYPE, ScaleType.class);
		Resolution resolution = Resolution.createFrom(jsonObject.optJSONObject(JSON_NAME_BASE_RESOLUTION));

		if (type == ResourceType.FILE) {
			String filePath = jsonObject.getString(JSON_NAME_FILE_PATH);
			String mimeType = getMimeType(filePath);

			if (mimeType.startsWith(MIME_TYPE_IMAGE_PREFIX))
				return new FileImageResource(filePath, scaleType, resolution);
			else if (mimeType.startsWith(MIME_TYPE_VIDEO_PREFIX))
				return new VideoFileImageResource(filePath, scaleType, resolution, jsonObject.getInt(JSON_NAME_VIDEO_FRAME_POSITION));

		} else if (type == ResourceType.ANDROID_ASSET) {
			String assetFilePath = jsonObject.getString(JSON_NAME_FILE_PATH);
			return new AssetImageResource(assetFilePath, resources, scaleType, resolution);

		} else if (type == ResourceType.ANDROID_RESOURCE) {
			if (jsonObject.isNull(JSON_NAME_DRAWABLE_FULL_NAME)) {
				int drawableId = jsonObject.getInt(JSON_NAME_DRAWABLE_ID);
				return new DrawableImageResource(drawableId, resources, scaleType, resolution);
			} else {
				String drawableFullName = jsonObject.optString(JSON_NAME_DRAWABLE_FULL_NAME);
				return new DrawableImageResource(drawableFullName, resources, scaleType, resolution);
			}
		}
		return Precondition.assureUnreachable();
	}

	private static Size measureImageSizeWithScaleType(Size srcSize, Size dstSize, ScaleType scaleType) {

		double scale = 0.0;
		if (scaleType == ScaleType.BUFFER) {
			scale = Math.sqrt((double) dstSize.product() / srcSize.product());

		} else if (scaleType == ScaleType.FIT_CENTER) {
			double widthScale = (double) dstSize.width / srcSize.width;
			double heightScale = (double) dstSize.height / srcSize.height;
			scale = Math.min(widthScale, heightScale);

		} else {
			Precondition.assureUnreachable();
		}

		int scaledWidth = (int) Math.floor(srcSize.width * scale);
		int scaledHeight = (int) Math.floor(srcSize.height * scale);
		return new Size(scaledWidth, scaledHeight);
	}

	private static int measureSampleSize(Size imageSize, Size targetSize) {

		int imagePixelCount = imageSize.product();
		int targetPixelCount = targetSize.product();

		int sampleSize = 1;
		while (true) {
			int doubleSampleSize = sampleSize * 2;
			int divisor = doubleSampleSize * doubleSampleSize;
			if (imagePixelCount / divisor >= targetPixelCount) {
				sampleSize *= 2;
			} else {
				return sampleSize;
			}
		}
	}

	static String getMimeType(String fileName) {
		Precondition.checkString(fileName).checkNotEmpty();

		int lastIndexOfDot = fileName.lastIndexOf(".");
		if (lastIndexOfDot == -1)
			return null;

		String extension = fileName.substring(lastIndexOfDot + 1, fileName.length()).toLowerCase(Locale.ENGLISH);
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	}

	// // // // // Constructor.
	// // // // //
	ImageResource(ResourceType type, ScaleType scaleType, Resolution resolution) {
		Precondition.checkArgument(scaleType != null || resolution != null, "either resolution or scaleType must not be null.");

		mResourceType = type;
		mScaleType = scaleType;
		mResolution = resolution;
	}

	// // // // // Method.
	// // // // //
	abstract Bitmap decodeResource(Options options) throws IOException;

	abstract boolean validate();

	/**
	 * 이미지 자원의 원본 크기 정보를 반환합니다.
	 * 
	 * @return 이미지 자원의 크기 정보.
	 */
	abstract Size getSize();

	abstract int getOrientation();

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = new JsonObject();

		jsonObject.put(JSON_NAME_RESOURCE_TYPE, mResourceType);
		jsonObject.putOpt(JSON_NAME_SCALE_TYPE, mScaleType);
		jsonObject.putOpt(JSON_NAME_BASE_RESOLUTION, mResolution);

		return jsonObject;
	}

	/**
	 * 주어진 해상도에 맞춰 {@link Bitmap}을 생성하여 반환합니다.<br />
	 * <br />
	 * 만약 이미지 자원에 orientation 정보가 존재한다면 {@code Bitmap} 생성에 이를 자동적으로 반영합니다.
	 * 
	 * @param targetResolution
	 *            목표 해상도. 객체의 상태에 따라 적절한 이미지 크기를 산정하기 위해 사용합니다.
	 * @return 생성된 {@code Bitmap} 혹은 오류가 발생한 경우 {@code null}.
	 * @throws IllegalArgumentException
	 *             {@code targetResolution == null}인 경우.
	 * @see ScaleType
	 */
	Bitmap createBitmap(Resolution targetResolution) throws IOException {
		Precondition.checkNotNull(targetResolution);
		return createBitmap(targetResolution, null, null);
	}

	/**
	 * 주어진 해상도에 맞춰 {@link Bitmap}을 생성한 후, {@code scale} 및 {@code rotation} 값에 따라 변환 행렬을 적용하여 반환합니다.
	 * <br />
	 * <br />
	 * 만약 이미지 자원에 orientation 정보가 존재한다면 {@code Bitmap} 생성에 이를 자동적으로 반영합니다.
	 * 
	 * @param targetResolution
	 *            목표 해상도. 객체의 상태에 따라 적절한 이미지 크기를 산정하기 위해 사용합니다.
	 * @param scale
	 *            null-ok; 크기 행렬에 적용할 수치. {@code null}인 경우에는 크기 행렬을 적용하지 않습니다.
	 * @param rotation
	 *            null-ok; 회전 행렬에 적용할 수치. {@code null}인 경우에는 회전 행렬을 적용하지 않습니다.
	 * 
	 * @see Matrix
	 */
	Bitmap createBitmap(Resolution targetResolution, Float scale, Float rotation) throws IOException {
		Precondition.checkNotNull(targetResolution);

		Matrix matrix = new Matrix();
		matrix.postRotate(getOrientation());

		if (scale != null) {
			matrix.postScale(scale, scale);
		}
		if (rotation != null) {
			matrix.postRotate(rotation);
		}
		return createBitmap(targetResolution, matrix);
	}

	private Bitmap createBitmap(Resolution targetResolution, Matrix matrix) throws IOException {

		Bitmap bitmap = createBasicBitmap(targetResolution);
		Bitmap matrixAppliedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		bitmap.recycle();

		return matrixAppliedBitmap;
	}

	private Bitmap createBasicBitmap(Resolution targetResolution) throws IOException {
		Precondition.checkNotNull(targetResolution);

		if (!validate()) {
			throw new InvalidFileException("Failed to validate resource: " + toString());
		}

		Size srcSize = getSize();
		Size dstSize = measureSize(targetResolution, false);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inScaled = false;
		options.inMutable = true;
		options.inSampleSize = measureSampleSize(srcSize, dstSize);

		Bitmap bitmap = decodeResource(options);
		if (bitmap != null && !options.inPreferredConfig.equals(bitmap.getConfig()))
			bitmap = bitmap.copy(options.inPreferredConfig, true);
		if (bitmap == null)
			throw new IOException("bitmap is null.");
		bitmap.setDensity(Bitmap.DENSITY_NONE);

		if (!srcSize.equals(dstSize))
			bitmap = ImageUtils.createScaledBitmapDouble(bitmap, dstSize.width, dstSize.height, true);
		return bitmap;
	}

	public ResourceType getResourceType() {
		return mResourceType;
	}

	public Resolution getResolution() {
		return mResolution;
	}

	/**
	 * 객체 생성 시에 지정한 {@link ScaleType}을 반환합니다. 지정하지 않은 경우 즉, {@link Resolution}을 매개변수로 사용하는 생성자를 통해
	 * 객체를 생성했다면 {@code null}을 반환합니다.
	 * 
	 * @return 객체 생성 시에 지정한 {@code ScaleType}, 지정하지 않은 경우 {@code null}.
	 */
	public ScaleType getScaleType() {
		return mScaleType;
	}

	/**
	 * {@link #createBitmap(Resolution)}을 통해 생성될 {@link Bitmap}의 크기 정보를 반환합니다.
	 * 
	 * @param targetResolution
	 *            목표 해상도. 객체의 상태에 따라 적절한 이미지 크기를 산정하기 위해 사용합니다.
	 * @throws PreconditionException
	 *             이미지 자원의 해상도가 설정된 상태에서 {@code targetResolution}과 호환되지 않는 경우.
	 */
	Size measureSize(Resolution targetResolution) {
		return measureSize(targetResolution, true);
	}

	Size measureSize(Resolution targetResolution, boolean withOrientation) {
		Precondition.checkNotNull(targetResolution);

		Size srcSize = getSize();
		int orientation = getOrientation();

		Size size = (withOrientation && orientation != 0 && orientation != 180) ? srcSize.reverse() : srcSize;
		if (mScaleType == null) {
			Precondition.checkArgument(mResolution.isCompatibleWith(targetResolution), "targetResolution is not compatible with mResolution.");

			if (targetResolution.equals(mResolution)) {
				return size;
			} else {
				float scale = targetResolution.magnification / mResolution.magnification;
				return new Size(Math.round(size.width * scale), Math.round(size.height * scale));
			}
		} else {
			return measureImageSizeWithScaleType(size, targetResolution.getSize(), mScaleType);
		}
	}

	/**
	 * {@link #createBitmap(Resolution, float, float)}을 통해 생성될 {@link Bitmap}의 크기 정보를 반환합니다.
	 * 
	 * @param targetResolution
	 *            목표 해상도. 객체의 상태에 따라 적절한 이미지 크기를 산정하기 위해 사용합니다.
	 * @param scale
	 *            null-ok; 크기 행렬에 적용할 수치. {@code null}인 경우에는 크기 행렬을 적용하지 않습니다.
	 * @param rotation
	 *            null-ok; 회전 행렬에 적용할 수치. {@code null}인 경우에는 회전 행렬을 적용하지 않습니다.
	 * @throws PreconditionException
	 *             이미지 자원의 해상도가 설정된 상태에서 {@code targetResolution}과 호환되지 않는 경우.
	 * @see Matrix
	 */
	Size measureSize(Resolution targetResolution, Float scale, Float rotation) {

		Matrix matrix = new Matrix();
		if (scale != null) {
			matrix.postScale(scale, scale);
		}
		if (rotation != null) {
			matrix.postRotate(rotation);
		}

		return measureSize(targetResolution, matrix);
	}

	private Size measureSize(Resolution targetResolution, Matrix matrix) {

		Size size = measureSize(targetResolution);
		RectF rect = new RectF(0, 0, size.width, size.height);
		matrix.mapRect(rect);

		return new Size(Math.round(rect.width()), Math.round(rect.height()));
	}

	@Override
	public int createCacheCode() {

		int cacheCode = mResourceType.hashCode();
		if (mScaleType != null)
			cacheCode ^= mScaleType.hashCode();
		if (mResolution != null)
			cacheCode ^= mResolution.getSize().hashCode();
		return cacheCode;
	}

	// // // // // Inner Class.
	// // // // //
	public static final class Builder {

		private ResourceType mResourceType;
		private Object mSource;
		private ScaleType mScaleType;
		private Resolution mResolution;
		private Integer mVideoFramePositionMs;

		public Builder setFilePath(String path) {
			Precondition.checkString(path).checkNotEmpty();
			mResourceType = ResourceType.FILE;
			mSource = path;
			return this;
		}

		public Builder setAssetFilePath(String path) {
			Precondition.checkString(path).checkNotEmpty();
			mResourceType = ResourceType.ANDROID_ASSET;
			mSource = path;
			return this;
		}

		public Builder setDrawableId(int id) {
			Precondition.checkArgument(id != 0, "id must not be zero.");
			mResourceType = ResourceType.ANDROID_RESOURCE;
			mSource = id;
			return this;
		}

		public Builder setDrawableName(String name) {
			Precondition.checkString(name).checkNotEmpty();
			mResourceType = ResourceType.ANDROID_RESOURCE;
			mSource = name;
			return this;
		}

		public Builder setScaleType(ScaleType scaleType) {
			Precondition.checkNotNull(scaleType);
			mScaleType = scaleType;
			return this;
		}

		public Builder setResolution(Resolution resolution) {
			Precondition.checkNotNull(resolution);
			mResolution = resolution;
			return this;
		}

		public Builder setVideoFramePosition(int positionMs) {
			Precondition.checkNotNegative(positionMs);
			mVideoFramePositionMs = positionMs;
			return this;
		}

		ImageResource build(Context context) {
			Precondition.checkState(mResourceType != null, "You must set path or id of the image resource.");
			Precondition.checkState(mScaleType != null || mResolution != null, "You must invoke setScaleType() or setResolution()");

			try {
				JsonObject jsonObject = new JsonObject();

				jsonObject.put(JSON_NAME_RESOURCE_TYPE, mResourceType);
				jsonObject.putOpt(JSON_NAME_SCALE_TYPE, mScaleType);
				jsonObject.putOpt(JSON_NAME_BASE_RESOLUTION, mResolution);
				jsonObject.putOpt(JSON_NAME_VIDEO_FRAME_POSITION, mVideoFramePositionMs);

				switch (mResourceType) {
					case FILE:
					case ANDROID_ASSET:
						jsonObject.put(JSON_NAME_FILE_PATH, mSource);
						break;
					case ANDROID_RESOURCE:
						if (mSource instanceof String) {
							String fullName = String.format("%s:%s/%s", context.getPackageName(), RESOURCE_TYPE_DRAWABLE, mSource);
							jsonObject.put(JSON_NAME_DRAWABLE_FULL_NAME, fullName);
						} else {
							jsonObject.put(JSON_NAME_DRAWABLE_ID, mSource);
						}
						break;
				}
				ImageResource imageResource = createFromJsonObject(context.getResources(), jsonObject);
				if (!imageResource.validate())
					throw new InvalidFileException("Failed to validate resource: " + toString());
				return imageResource;
			} catch (Exception exception) {
				exception.printStackTrace();
				return null;
			}
		}
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * 이미지 자원을 {@link Bitmap}으로 생성할 때 사용할 크기 조정 방식을 구분하기 위한 열거형.
	 * 
	 * @see #BUFFER
	 * @see #FIT_CENTER
	 * @see ImageResource#createBitmap(Resolution)
	 */
	public static enum ScaleType {

		/**
		 * 이미지 자원의 종횡비를 유지하면서, {@code Bitmap} 생성 시 지정한 {@code Resolution}의 {@code width * height}
		 * 크기에 해당하는 1차원의 자료 구조를 상정하여, 이에 저장될 수 있는 최대 크기에 맞춥니다.
		 */
		BUFFER,

		/**
		 * 이미지 자원의 종횡비를 유지하면서, {@code Bitmap} 생성 시 지정한 {@code Resolution}의 허용 범위 내에 있는 최대 크기에 맞춥니다.
		 */
		FIT_CENTER;
	}
}