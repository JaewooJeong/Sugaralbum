package com.kiwiple.mediaframework.preview;

import android.media.MediaCodecInfo;

/**
 * MediaCodecColorFormat.
 */
public final class MediaCodecColorFormat {

	/**
	 * NV21
	 */
	public static final int YUV420SemiPlanar = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;

	/**
	 * used in OMX.QCOM
	 */
	public static final int YUV420PackedSemiPlanar16m2ka = 0x7FA30C02;

	/**
	 * used in OMX.QCOM
	 */
	public static final int YUV420PackedSemiPlanar32m = 0x7FA30C04;

	/**
	 *
	 */
	public static final int YUV420Planar = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;

	// Unused: OMX.QCOM
	public static final int YVU420SemiPlanar = 0x7FA30C00;
	
	public static final int YVU420PackedSemiPlanar32m4ka = 0x7FA30C01;
	public static final int YUV420PackedSemiPlanar64x32Tile2m8ka = 0x7FA30C03;

	private MediaCodecColorFormat() {
		// Hide constructor and do nothing.
	}
}
