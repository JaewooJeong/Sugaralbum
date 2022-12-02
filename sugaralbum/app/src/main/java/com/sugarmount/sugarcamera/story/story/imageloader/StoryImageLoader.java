package com.sugarmount.sugarcamera.story.story.imageloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;

import com.sugarmount.sugaralbum.R;

public class StoryImageLoader {

	private static final int THUMBNAIL_SIZE = 300;

	private static final float STORY_THUMB_HEIGHT_RATIO = 0.2f;

	private MemoryCache mMemoryCache = new MemoryCache();
	private Map<ImageView, String> mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService mExecutorService;
	private Context mContext;
	private Handler mHandler = new Handler();

	private int mDefaultImage = R.drawable.bg_img_1;
	
	private static StoryImageLoader sInstance;
	
	public static synchronized StoryImageLoader getInstance(Context context) {
	    if(sInstance == null) {
	        sInstance = new StoryImageLoader(context.getApplicationContext());
	    }
	    return sInstance;
	}

	private StoryImageLoader(Context context) {
		mContext = context;
		mExecutorService = Executors.newFixedThreadPool(5);
	}

	public void displayImage(String url, int orientation, ImageView imageView) {
		mImageViews.put(imageView, url);
		Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null) {
//			imageView.setImageBitmap(getCroppedBitmap(orientation, bitmap));
			imageView.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, orientation, imageView);
			imageView.setImageResource(mDefaultImage);
		}
	}
	
	public void deleteCache(String url) {
	    mMemoryCache.remove(url);
	}

	public void clear() {
		mImageViews.clear();
		clearCache();
	}

	public void clearCache() {
		mMemoryCache.clear();
	}

	private void queuePhoto(String url, int orientation, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, orientation, imageView);
		mExecutorService.submit(new PhotosLoader(p));
	}

	private Bitmap getCroppedBitmap(int orientation, Bitmap bitmap) {
		Bitmap croppedBitmap;
		if (orientation == 0 || orientation == 180) {
			croppedBitmap = bitmap;
		} else {
			int startY = (int) (bitmap.getHeight() * STORY_THUMB_HEIGHT_RATIO);
			croppedBitmap = Bitmap.createBitmap(bitmap, 0, startY, bitmap.getWidth(), (int) (9.0f / 16.0f * bitmap.getWidth() + startY));
		}
		return croppedBitmap;
	}

	private Bitmap getBitmap(String url) {
		try {
			Bitmap bitmap = null;
			bitmap = decodeFromUri(Uri.parse(url));
			return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError) {
				mMemoryCache.clear();
			}
			return null;
		}
	}

	private Bitmap decodeFromUri(Uri uri) {
		InputStream input;
		Bitmap bitmap = null;
		try {
			input = mContext.getContentResolver().openInputStream(uri);

			BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
			onlyBoundsOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
			input.close();
			if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
				return null;
			}

			int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

			double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
			input = mContext.getContentResolver().openInputStream(uri);
			bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
			input.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0) {
			return 1;
		} else {
			return k;
		}
	}

	private boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = mImageViews.get(photoToLoad.mImageView);
		if (tag == null || !tag.equals(photoToLoad.mUrl)) {
			return true;
		}
		return false;
	}

	private class PhotoToLoad {
		public String mUrl;
		public int mOrientation;
		public ImageView mImageView;

		public PhotoToLoad(String u, int orientation, ImageView i) {
			mUrl = u;
			mOrientation = orientation;
			mImageView = i;
		}
	}

	private class PhotosLoader implements Runnable {
		PhotoToLoad mPhotoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.mPhotoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			try {
				if (imageViewReused(mPhotoToLoad)) {
					return;
				}

				Bitmap bmp = getBitmap(mPhotoToLoad.mUrl);
				mMemoryCache.put(mPhotoToLoad.mUrl, bmp);

				if (imageViewReused(mPhotoToLoad)) {
					return;
				}
				BitmapDisplayer bd = new BitmapDisplayer(bmp, mPhotoToLoad);
				mHandler.post(bd);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	private class BitmapDisplayer implements Runnable {
		Bitmap mBitmap;
		PhotoToLoad mPhotoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			mBitmap = b;
			mPhotoToLoad = p;
		}

		@Override
		public void run() {
			if (imageViewReused(mPhotoToLoad)) {
				return;
			}
			if (mBitmap != null) {
				//jhshin
//				mPhotoToLoad.mImageView.setImageBitmap(getCroppedBitmap(mPhotoToLoad.mOrientation, mBitmap));
				mPhotoToLoad.mImageView.setImageBitmap(mBitmap);
			} else {
				mPhotoToLoad.mImageView.setImageResource(mDefaultImage);
			}

		}
	}
}
