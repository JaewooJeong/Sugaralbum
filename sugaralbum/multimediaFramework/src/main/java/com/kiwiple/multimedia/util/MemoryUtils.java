package com.kiwiple.multimedia.util;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.exception.PotentialOutOfMemoryException;

public final class MemoryUtils {

	private static final int MB = 1024 * 1024;

	private static final long BYTES_OF_STANDBY_MEMORY = Math.round(0.1 * MB);

	public static synchronized void checkPotentialOOM(long bytesToAllocate) throws PotentialOutOfMemoryException {
		Precondition.checkNotNegative(bytesToAllocate);

		System.gc();
		Runtime runtime = Runtime.getRuntime();

		long bytesAvailable = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();
		bytesAvailable = Math.max(0L, bytesAvailable - BYTES_OF_STANDBY_MEMORY);

		boolean potentialOOM = bytesAvailable < bytesToAllocate;
		if (potentialOOM) {
			float mbytesToAllocate = (float) bytesToAllocate / MB;
			float mbytesAvailable = (float) bytesAvailable / MB;

			L.fw("Potential OutOfMemoryError detected: %.2fMB/%.2fMB(allocate/available)", mbytesToAllocate, mbytesAvailable);
			throw new PotentialOutOfMemoryException("Potential OutOfMemoryError detected.");
		}
	}

	private MemoryUtils() {
		// Do not instantiate.
	}
}
