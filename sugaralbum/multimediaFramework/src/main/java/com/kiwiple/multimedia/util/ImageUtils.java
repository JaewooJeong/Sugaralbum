package com.kiwiple.multimedia.util;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.exception.FileNotFoundException;

/**
 * 라이브러리 개발 목적으로 사용하는 클래스입니다. 라이브러리 외부에서의 사용에 대해서는 그 유효성을 보장하지 않습니다.
 */
public final class ImageUtils {

	public static Size measureImageSize(String imageFilePath) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imageFilePath, options);

		return new Size(options.outWidth, options.outHeight);
	}

	public static int measureSampleSize(Size imageSize, int targetWidth, int targetHeight, int orientation) {

		int srcWidth = imageSize.width;
		int srcHeight = imageSize.height;

		if (orientation == 90 || orientation == 270) {
			int forSwap = srcWidth;
			srcWidth = srcHeight;
			srcHeight = forSwap;
		}
		int sampleSize = 1;
		while ((srcWidth / (sampleSize * 2) >= targetWidth) && (srcHeight / (sampleSize * 2) >= targetHeight)) {
			sampleSize *= 2;
		}

		return sampleSize;
	}

	public static int getOrientation(String imageFilePath) {

		int orientationTag;
		try {
			ExifInterface exifInterface = new ExifInterface(imageFilePath);
			orientationTag = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		} catch (IOException exception) {
			orientationTag = ExifInterface.ORIENTATION_NORMAL;
		}

		switch (orientationTag) {
			case ExifInterface.ORIENTATION_NORMAL:
				return 0;
			case ExifInterface.ORIENTATION_ROTATE_90:
				return 90;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return 180;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return 270;
			default:
				return 0;
		}
	}

	public static Bitmap createScaledBitmapDouble(Bitmap bitmap, Resolution srcResolution, Resolution dstResolution, boolean doRecycle) {

		if (!srcResolution.equals(dstResolution)) {

			float scale = dstResolution.magnification / srcResolution.magnification;
			float interScale = (1.0f + scale) / 2.0f;

			int scaledWidth = Math.round(bitmap.getWidth() * interScale);
			int scaledHeight = Math.round(bitmap.getHeight() * interScale);
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

			scaledWidth = Math.round(bitmap.getWidth() * scale);
			scaledHeight = Math.round(bitmap.getHeight() * scale);
			scaledBitmap = Bitmap.createScaledBitmap(scaledBitmap, scaledWidth, scaledHeight, true);

			if (doRecycle && scaledBitmap != bitmap) {
				bitmap.recycle();
			}
			bitmap = scaledBitmap;
		}
		return bitmap;
	}

	public static Bitmap createScaledBitmap(Bitmap bitmap, Resolution srcResolution, Resolution dstResolution, boolean doRecycle) {

		if (!srcResolution.equals(dstResolution)) {

			float scale = dstResolution.magnification / srcResolution.magnification;
			int scaledWidth = Math.round(bitmap.getWidth() * scale);
			int scaledHeight = Math.round(bitmap.getHeight() * scale);

			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

			if (doRecycle && scaledBitmap != bitmap) {
				bitmap.recycle();
			}
			bitmap = scaledBitmap;
		}
		return bitmap;
	}

	public static Bitmap createScaledBitmap(Bitmap bitmap, int width, int height, boolean doRecycle) {

		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

		if (doRecycle && scaledBitmap != bitmap) {
			bitmap.recycle();
		}
		return scaledBitmap;
	}

	public static Bitmap createScaledBitmapDouble(Bitmap bitmap, int width, int height, boolean doRecycle) {

		int interWidth = Math.round((bitmap.getWidth() + width) / 2.0f);
		int interHeight = Math.round((bitmap.getHeight() + height) / 2.0f);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, interWidth, interHeight, true);
		scaledBitmap = Bitmap.createScaledBitmap(scaledBitmap, width, height, true);

		if (doRecycle && scaledBitmap != bitmap) {
			bitmap.recycle();
		}
		return scaledBitmap;
	}

	public static Bitmap createScaledBitmapForCenterCrop(Bitmap bitmap, int targetWidth, int targetHeight, boolean doRecycle) {

		float widthScale = ((float) targetWidth / bitmap.getWidth());
		float heightScale = ((float) targetHeight / bitmap.getHeight());
		float scale = Math.max(widthScale, heightScale);
		int scaledWidth = Math.round(bitmap.getWidth() * scale);
		int scaledHeight = Math.round(bitmap.getHeight() * scale);

		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
		if (doRecycle && scaledBitmap != bitmap) {
			bitmap.recycle();
		}

		return scaledBitmap;
	}

	public static Bitmap createScaledBitmapAspectRatioMaintained(Bitmap bitmap, int targetWidth, int targetHeight, boolean doRecycle) {

		float widthScale = (float) targetWidth / bitmap.getWidth();
		float heightScale = (float) targetHeight / bitmap.getHeight();
		float scale = Math.min(widthScale, heightScale);
		int scaledWidth = Math.round(bitmap.getWidth() * scale);
		int scaledHeight = Math.round(bitmap.getHeight() * scale);

		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
		if (doRecycle && scaledBitmap != bitmap) {
			bitmap.recycle();
		}

		return scaledBitmap;
	}

	public static Bitmap createRotatedBitmap(Bitmap bitmap, int orientation, boolean doRecycle) {

		Matrix matrix = new Matrix();
		matrix.postRotate(orientation);

		Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		if (doRecycle) {
			bitmap.recycle();
		}

		return rotatedBitmap;
	}

	public static Bitmap getImage(String path, Resolution resolution, long videoFramePosition) {
		Bitmap bitmap = null;
		if (getMimeType(path).startsWith("image/")) {

			Size originalSize = null;
			try {
				originalSize = measureImageSize(path);
			} catch (Exception exception) {
				throw new FileNotFoundException(exception.getMessage());
			}
			int targetWidth = resolution.width;
			int targetHeight = resolution.height;
			int orientation = getOrientation(path);

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = ImageUtils.measureSampleSize(originalSize, targetWidth, targetHeight, orientation);
			options.inPreferredConfig = Config.ARGB_8888;
			options.inMutable = true;

			bitmap = BitmapFactory.decodeFile(path, options);
			bitmap = ImageUtils.createScaledBitmapAspectRatioMaintained(bitmap, targetWidth, targetHeight, true);

			if (bitmap != null) {
				if (orientation != 0) {
					bitmap = ImageUtils.createRotatedBitmap(bitmap, orientation, true);
				}
				bitmap = ImageUtils.createScaledBitmapForCenterCrop(bitmap, resolution.width, resolution.height, true);
			}
		} else if (getMimeType(path).startsWith("video/")) {

			MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
			try {
				mRetriever.setDataSource(path);
			} catch (IllegalArgumentException exception) {
				throw new FileNotFoundException(exception.getMessage());
			}

			bitmap = mRetriever.getFrameAtTime(videoFramePosition * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

			if (bitmap != null) {
				if (!Config.ARGB_8888.equals(bitmap.getConfig())) {
					Bitmap newConfigBitmap = bitmap.copy(Config.ARGB_8888, true);
					bitmap.recycle();
					bitmap = newConfigBitmap;
				}

				bitmap = ImageUtils.createScaledBitmapForCenterCrop(bitmap, resolution.width, resolution.height, true);
			}
			if (mRetriever != null) {
				try {
					mRetriever.release();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);

		if (!TextUtils.isEmpty(extension)) {
			extension = extension.toLowerCase();
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		} else {
			int dotPos = url.lastIndexOf('.');
			if (0 <= dotPos) {
				extension = url.substring(dotPos + 1);
				extension = extension.toLowerCase();
				MimeTypeMap mime = MimeTypeMap.getSingleton();
				type = mime.getMimeTypeFromExtension(extension);
			}
		}
		if (type == null) {
			type = "";
		}
		return type;
	}

	private ImageUtils() {
		// Utility classes should have a private constructor.
	}
}
