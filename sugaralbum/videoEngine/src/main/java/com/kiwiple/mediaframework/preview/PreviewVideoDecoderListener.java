package com.kiwiple.mediaframework.preview;

import java.nio.ByteBuffer;

import android.media.MediaFormat;

public interface PreviewVideoDecoderListener {

	public abstract void onDecoderInitialized(MediaFormat mediaFormat);

	public abstract void onOutputFormatChanged(MediaFormat mediaFormat);

	public abstract void onMeasureBufferSize(int bufferSize);

	public abstract void onDecode(ByteBuffer yuvPixels, long timeUs);

	public abstract void onSeek();

	public abstract void onWait();
	
	public abstract void onPrepareDone();
}
