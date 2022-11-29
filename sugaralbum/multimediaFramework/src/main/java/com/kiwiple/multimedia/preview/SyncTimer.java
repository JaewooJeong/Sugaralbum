package com.kiwiple.multimedia.preview;

import android.os.AsyncTask;

import com.kiwiple.debug.Precondition;

/**
 * SyncTimer.
 * 
 */
final class SyncTimer {

	// // // // // Static variable.
	// // // // //
	private static final long MILLION = 1000L * 1000L;

	private static final long THREAD_DELAY = 2L;

	// // // // // Member variable.
	// // // // //
	private OnUpdateListener mUpdateListener;
	private InternalTimer mInternalTimer;

	private final long mUpdateIntervalNanos;
	private long mNextUpdateTimeNanos;
	private boolean mIsRunning = false;

	// // // // // Constructor.
	// // // // //
	SyncTimer(int updateIntervalMillis) {
		Precondition.checkOnlyPositive(updateIntervalMillis);

		mUpdateListener = OnUpdateListener.NULL_OBJECT;
		mUpdateIntervalNanos = updateIntervalMillis * MILLION;
	}

	// // // // // Method.
	// // // // //
	void start() {

		if (!mIsRunning) {
			mIsRunning = true;
			mInternalTimer = new InternalTimer();
			mInternalTimer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	void stop() {

		if (mIsRunning) {
			mInternalTimer.cancel(true);
			mInternalTimer = null;
			mIsRunning = false;
		}
	}

	long getUpdateInterval() {
		return mUpdateIntervalNanos / MILLION;
	}

	void setOnUpdateListener(OnUpdateListener updateListener) {
		Precondition.checkNotNull(updateListener);
		mUpdateListener = updateListener;
	}

	boolean isRunning() {
		return mIsRunning;
	}

	// // // // // Inner class.
	// // // // //
	private class InternalTimer extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			mNextUpdateTimeNanos = System.nanoTime() + mUpdateIntervalNanos;

			try {
				while (mIsRunning) {
					Thread.sleep(THREAD_DELAY, 0);

					if (System.nanoTime() > mNextUpdateTimeNanos) {

						mUpdateListener.onUpdate();
						mNextUpdateTimeNanos += mUpdateIntervalNanos;
					}
				}
			} catch (InterruptedException exception) {
				// Do nothing.
			}
			return null;
		}
	}

	// // // // // Interface.
	// // // // //
	interface OnUpdateListener {

		/**
		 * NULL_OBJECT is to avoid null checks.
		 */
		static final OnUpdateListener NULL_OBJECT = new OnUpdateListener() {

			@Override
			public void onUpdate() {
				// Do nothing.
			}
		};

		public abstract void onUpdate();
	}
}