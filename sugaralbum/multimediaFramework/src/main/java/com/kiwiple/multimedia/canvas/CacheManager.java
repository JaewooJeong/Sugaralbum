package com.kiwiple.multimedia.canvas;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Bitmap;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.util.Size;

/**
 * CacheManager.
 * 
 */
public class CacheManager {

	// // // // // Static variable.
	// // // // //
	private static final String THEME_CACHE_SUB_PATH = "theme_image";

	private static final int ARGB_PIXEL_BYTE_SIZE = 4;
	private static final int THEME_CACHE_HEADER_INT_SIZE = 2;
	private static final int THEME_CACHE_HEADER_BYTE_SIZE = THEME_CACHE_HEADER_INT_SIZE * 4;

	private static CacheManager sInstance;

	// // // // // Member variable.
	// // // // //
	private final Context mContext;

	private final File mCacheDirectory;

	private final ConcurrentHashMap<String, ReentrantLock> mImageCacheFileLocks = new ConcurrentHashMap<>();
	private final Object mLockMethodSynchronizer = new Object();
	private final Object mUnlockMethodSynchronizer = new Object();

	// // // // // Static method.
	// // // // //
	public static synchronized CacheManager getInstance(Context context) {

		if (sInstance == null) {
			sInstance = new CacheManager(context);
			sInstance.clearCacheDirectory();
		}
		return sInstance;
	}

	// // // // // Constructor.
	// // // // //
	private CacheManager(Context context) {
		Precondition.checkNotNull(context);

		mContext = context;
		mCacheDirectory = new File(mContext.getCacheDir(), THEME_CACHE_SUB_PATH);
		mCacheDirectory.mkdir();
	}

	// // // // // Method.
	// // // // //
	File getCacheDirectory() {

		File cacheDirectory = new File(mContext.getCacheDir(), THEME_CACHE_SUB_PATH);
		cacheDirectory.mkdir();

		return cacheDirectory;
	}

	void clearCacheDirectory() {

		File[] files = mCacheDirectory.listFiles();
		if (files != null) {
			for (File file : files) {
				file.delete();
			}
		}
	}

	boolean isImageCacheExist(String cacheFileName) {
		return validate(cacheFileName);
	}

	private Size getSize(String cacheFileName) throws IOException {
		return getSize(new File(mCacheDirectory, cacheFileName));
	}

	private Size getSize(File cacheFile) throws IOException {

		RandomAccessFile randomAccessFile = new RandomAccessFile(cacheFile, "r");

		int width = randomAccessFile.readInt();
		int height = randomAccessFile.readInt();
		randomAccessFile.close();

		return new Size(width, height);
	}

	private boolean validate(String cacheFileName) {

		File imageCacheFile = new File(mCacheDirectory, cacheFileName);
		if (!imageCacheFile.isFile() || imageCacheFile.length() <= THEME_CACHE_HEADER_BYTE_SIZE) {
			return false;
		}

		try {
			lockImageCacheFile(cacheFileName);

			Size size = getSize(imageCacheFile);
			int fileSize = size.product() * ARGB_PIXEL_BYTE_SIZE + THEME_CACHE_HEADER_BYTE_SIZE;

			if (imageCacheFile.length() == fileSize) {
				return true;
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		} finally {
			unlockImageCacheFile(cacheFileName);
		}
		return false;
	}

	void createImageCache(String cacheFileName, Bitmap bitmap) {
		createImageCache(cacheFileName, PixelExtractUtils.extractARGB(bitmap, false), bitmap.getWidth(), bitmap.getHeight());
	}

	void createImageCache(String cacheFileName, Bitmap bitmap, boolean doRecycle) {
		createImageCache(cacheFileName, PixelExtractUtils.extractARGB(bitmap, doRecycle), bitmap.getWidth(), bitmap.getHeight());
	}

	void createImageCache(String cacheFileName, int[] pixels, int imageWidth, int imageHeight) {

		if (validate(cacheFileName)) {
			L.i("Image cache already exist.");
		} else {
			try {
				lockImageCacheFile(cacheFileName);
				File imageCache = new File(mCacheDirectory, cacheFileName);

				RandomAccessFile randomAccessFile = new RandomAccessFile(imageCache, "rw");
				FileChannel fileChannel = randomAccessFile.getChannel();

				int cacheSize = pixels.length * ARGB_PIXEL_BYTE_SIZE + THEME_CACHE_HEADER_BYTE_SIZE;
				MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, cacheSize);
				IntBuffer intBuffer = mappedByteBuffer.asIntBuffer();

				intBuffer.put(imageWidth);
				intBuffer.put(imageHeight);
				intBuffer.put(pixels);

				fileChannel.close();
				randomAccessFile.close();

			} catch (IOException exception) {
				exception.printStackTrace();
			} finally {
				unlockImageCacheFile(cacheFileName);
			}
		}
	}

	PixelCanvas decodeImageCache(String cacheFileName) throws IOException {

		Size size = getSize(cacheFileName);
		PixelCanvas canvas = new PixelCanvas(size, false);
		decodeImageCache(cacheFileName, canvas);

		return canvas;
	}

	void decodeImageCache(String cacheFileName, PixelCanvas dstCanvas) throws IOException {

		try {
			lockImageCacheFile(cacheFileName);

			File imageCache = new File(mCacheDirectory, cacheFileName);
			RandomAccessFile randomAccessFile = new RandomAccessFile(imageCache, "r");
			FileChannel fileChannel = randomAccessFile.getChannel();

			MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			IntBuffer intBuffer = mappedByteBuffer.asIntBuffer();

			int width = intBuffer.get();
			int height = intBuffer.get();
			dstCanvas.setImageSize(width, height);
			intBuffer.get(dstCanvas.intArray, 0, width * height);

			fileChannel.close();
			randomAccessFile.close();
		} finally {
			unlockImageCacheFile(cacheFileName);
		}
	}

	private void lockImageCacheFile(String cacheFileName) {
		synchronized (mLockMethodSynchronizer) {

			ReentrantLock reetrantLock = mImageCacheFileLocks.get(cacheFileName);
			if (reetrantLock == null) {
				reetrantLock = new ReentrantLock();
			}
			reetrantLock.lock();
			mImageCacheFileLocks.put(cacheFileName, reetrantLock);
		}
	}

	private void unlockImageCacheFile(String cacheFileName) {
		synchronized (mUnlockMethodSynchronizer) {
			mImageCacheFileLocks.remove(cacheFileName).unlock();
		}
	}

	public void release() {
		clearCacheDirectory();
		sInstance = null;
	}
}