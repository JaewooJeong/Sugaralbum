package com.kiwiple.mediaframework.preview;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaExtractor;
import android.media.MediaFormat;

public class PreviewVideoDemuxer {

	private MediaExtractor mMediaExtractor;
	private MediaFormat mVideoMediaFormat;

	public PreviewVideoDemuxer(String videoFilePath) throws IOException {

		mMediaExtractor = new MediaExtractor();
		mMediaExtractor.setDataSource(videoFilePath);

		boolean hasVideoTrack = false;
		int trackSize = mMediaExtractor.getTrackCount();

		for (int i = 0; i != trackSize; ++i) {
			try {
				MediaFormat format = mMediaExtractor.getTrackFormat(i);
				String mimeType = format.getString(MediaFormat.KEY_MIME);

				if (mimeType.contains("video")) {
					hasVideoTrack = true;
					mVideoMediaFormat = format;
					mMediaExtractor.selectTrack(i);
				}
			} catch (IllegalArgumentException exception) {
				continue;
			}
		}

		if (!hasVideoTrack) {
			throw new IOException("There is no video track: " + videoFilePath);
		}
	}

	public int readSampleData(ByteBuffer buffer) {
		return mMediaExtractor.readSampleData(buffer, 0);
	}

	public long getSampleTime() {
		return mMediaExtractor.getSampleTime();
	}

	public MediaFormat getMediaFormat() {
		return mVideoMediaFormat;
	}

	public boolean advance() {
		return mMediaExtractor.advance();
	}

	public void release() {
		mMediaExtractor.release();
	}

	public int getSampleFlags() {
		return mMediaExtractor.getSampleFlags();
	}

	public void seekTo(long timeUs) {
		mMediaExtractor.seekTo(timeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
	}
}