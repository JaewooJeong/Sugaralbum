package com.kiwiple.multimedia.preview;

import java.util.ArrayDeque;
import java.util.Arrays;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.canvas.PixelCanvas;
import com.kiwiple.multimedia.util.Size;

/**
 * BufferPool.
 */
class BufferPool {

	// // // // // Static variable.
	// // // // //
	public static final int DEFAULT_BUFFER_POOL_SIZE = 2;
	public static final int MAX_BUFFER_POOL_SIZE = 2;

	/**
	 * NULL_OBJECT is to avoid null checks.
	 */
	static final BufferPool NULL_OBJECT = new BufferPool();

	// // // // // Member variable.
	// // // // //
	private final int mPoolSize;
	private final Size mBufferSize;
	private final PixelCanvas[] mBufferArrays;

	private final ArrayDeque<Integer> mReadableQueue;
	private final ArrayDeque<Integer> mWritableQueue;

	private Integer mCurrentReadBufferIndex;
	private Integer mCurrentWriteBufferIndex;

	private boolean mIsWriting = false;
	private boolean mIsReading = false;

	// // // // // Constructor.
	// // // // //
	/**
	 * To construct {@link #NULL_OBJECT}.
	 */
	private BufferPool() {
		mPoolSize = 0;
		mBufferSize = Size.INVALID_SIZE;
		mBufferArrays = null;
		mReadableQueue = mWritableQueue = new ArrayDeque<Integer>(0);
	}

	BufferPool(Size bufferSize) {
		this(DEFAULT_BUFFER_POOL_SIZE, bufferSize);
	}

	BufferPool(int poolSize, Size bufferSize) {

		Precondition.checkArgument(poolSize > 0 && bufferSize.product() > 0, "poolSize and bufferSize.product() must be greater than 0.");
		if (poolSize > MAX_BUFFER_POOL_SIZE) {
			poolSize = MAX_BUFFER_POOL_SIZE;
		}

		mPoolSize = poolSize;
		mBufferSize = bufferSize;
		mBufferArrays = new PixelCanvas[mPoolSize];

		mReadableQueue = new ArrayDeque<Integer>(poolSize);
		mWritableQueue = new ArrayDeque<Integer>(poolSize);

		for (int i = 0; i != mPoolSize; ++i) {
			PixelCanvas pixelCanvas = new PixelCanvas(bufferSize, true);

			mBufferArrays[i] = pixelCanvas;
			mWritableQueue.add(i);
		}
	}

	// // // // // Method.
	// // // // //
	void initializeBuffer() {

		mReadableQueue.clear();
		mWritableQueue.clear();

		for (int i = 0; i != mPoolSize; ++i) {
			Arrays.fill(mBufferArrays[i].intArray, 0);
			mWritableQueue.add(i);
		}
	}

	boolean isReadBufferReady() {
		return !mReadableQueue.isEmpty();
	}

	boolean isWriteBufferReady() {
		return !mWritableQueue.isEmpty();
	}

	synchronized PixelCanvas getLastReadBufferWithLock() {

		if (mIsReading || mReadableQueue.isEmpty())
			return null;

		mIsReading = true;
		mCurrentReadBufferIndex = mReadableQueue.poll();

		return mBufferArrays[mCurrentReadBufferIndex];
	}

	synchronized void unlockReadBuffer() {

		if (mIsReading) {

			mWritableQueue.add(mCurrentReadBufferIndex);
			mCurrentReadBufferIndex = null;
			mIsReading = false;
		}
	}

	synchronized PixelCanvas getWriteBufferWithLock() {

		if (mIsWriting || mWritableQueue.isEmpty()) {
			return null;
		}

		mIsWriting = true;
		mCurrentWriteBufferIndex = mWritableQueue.poll();

		return mBufferArrays[mCurrentWriteBufferIndex];
	}

	synchronized void unlockWrittenBuffer() {

		if (mIsWriting) {

			mReadableQueue.add(mCurrentWriteBufferIndex);

			mCurrentWriteBufferIndex = null;
			mIsWriting = false;
		}
	}

	int getPoolSize() {
		return mPoolSize;
	}

	Size getBufferSize() {
		return mBufferSize;
	}
}