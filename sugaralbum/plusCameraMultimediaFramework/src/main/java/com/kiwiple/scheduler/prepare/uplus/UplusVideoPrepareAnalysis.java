package com.kiwiple.scheduler.prepare.uplus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.kiwiple.imageanalysis.analysis.ImageAnalysis;
import com.kiwiple.imageanalysis.analysis.ImageAnalysis.IFrameData;
import com.kiwiple.debug.L;
import com.kiwiple.multimedia.util.VideoUtils;
import com.kiwiple.scheduler.data.uplus.UplusIFrameData;
import com.kiwiple.scheduler.database.uplus.UplusAnalysisPersister;
import com.kiwiple.scheduler.prepare.VideoPrepareAnalysis;

public class UplusVideoPrepareAnalysis extends VideoPrepareAnalysis {
	private static final long DURATION_DIVIDER = 10000;
	private static final long VIDEO_MINIMUM_DURATION = 1000 * 10;
	private static final long VIDEO_MAXIMUM_DURATION = 1000 * 120;
	private static final int MAXIMUM_PREPARE_VIDEO_ANALYSIS_COUNT = 5;

	private VideoPrepareAnalysisAsynTask mUplusVideoPrepareAnalysisTask;
	private UplusVideoPrepareAnalysisListener mUplusVideoAnalysisListener;

	public void startAnalysisGallery(Context context, UplusVideoPrepareAnalysisListener listener) {
		mUplusVideoAnalysisListener = listener;
		mUplusVideoPrepareAnalysisTask = new VideoPrepareAnalysisAsynTask(context);

		if (mUplusVideoPrepareAnalysisTask.getStatus() != AsyncTask.Status.RUNNING) {
			mUplusVideoPrepareAnalysisTask.execute();
		} else {
			if (mUplusVideoAnalysisListener != null) {
				mUplusVideoAnalysisListener.onUplusVideoPrepareAnalysisTotalFinish(false);
			}
		}
	}
/**
 * 1. 비디오를 10초 단위로 thumbnail bitmap을 얻는다. <br>
 * 2. thumbnail의 quality를 측정. 
 * 3. quality별로 정렬. 
 * @param context
 * @param videoPath
 * @param duration
 * @return
 */
	private ArrayList<UplusIFrameData> getIFrameDataList(Context context, String videoPath, long duration) {
		ArrayList<UplusIFrameData> mIFrameDataList = new ArrayList<UplusIFrameData>();

		int position = (int) (duration / DURATION_DIVIDER);
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		retriever.setDataSource(videoPath);

		String orientation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

		ImageAnalysis imageAnalysis = ImageAnalysis.getInstance(context);
		for (int i = 0; i < position; i++) {

			Bitmap thumb = retriever.getFrameAtTime(i * DURATION_DIVIDER); //비디오에서 10초 단위로 thumbnail을 가지고 온다. 
			if (thumb == null) {
				return mIFrameDataList;
			}

			if (thumb.getConfig() != Config.ARGB_8888) {
				thumb = thumb.copy(Config.ARGB_8888, true);
			}

			IFrameData iFrameData = imageAnalysis.analyzeIFrame(thumb);  //IFrameData 분석된 데이터의 결과 class

			if (iFrameData == null) {
				return mIFrameDataList;
			}

			//list에 add
			UplusIFrameData mParsedIFrameData = new UplusIFrameData(iFrameData, videoPath, i * DURATION_DIVIDER,
					(i + 1) * DURATION_DIVIDER > duration ? duration : (i + 1) * DURATION_DIVIDER, orientation);
			mIFrameDataList.add(mParsedIFrameData);

			if (thumb != null) {
				thumb.recycle();
			}
		}
		try {
			retriever.release();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//점수별 정렬
		Comparator<UplusIFrameData> comparator = new Comparator<UplusIFrameData>() {

			@Override
			public int compare(UplusIFrameData lhs, UplusIFrameData rhs) {
				return lhs.getUplusIFrameData().totalScore >= rhs.getUplusIFrameData().totalScore ? -1 : 1;
			}
		};

		Collections.sort(mIFrameDataList, comparator);
		return mIFrameDataList;
	}

	private Cursor getVideoData(Context context) {
		Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, getProjection(), getSelection(), null,
				getOrder());

		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		return cursor;
	}

	private String[] getProjection() {
		return new String[] { MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DURATION };
	}

	private String getSelection() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" ( ");
		buffer.append(MediaStore.Video.VideoColumns.DURATION);
		buffer.append(" >= ");
		buffer.append(VIDEO_MINIMUM_DURATION);
		buffer.append(" AND ");
		buffer.append(MediaStore.Video.VideoColumns.DURATION);
		buffer.append(" <= ");
		buffer.append(VIDEO_MAXIMUM_DURATION);
		buffer.append(" ) ");
		return buffer.toString();
	}

	private String getOrder() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(MediaStore.Video.VideoColumns.DATE_TAKEN);
		buffer.append(" DESC ");
		return buffer.toString();
	}

	private class VideoPrepareAnalysisAsynTask extends AsyncTask<Void, Void, Void> {

		private Context mContext;

		VideoPrepareAnalysisAsynTask(Context context) {
			mContext = context;
		}

		@Override
		protected Void doInBackground(Void... params) {
			UplusAnalysisPersister persister = UplusAnalysisPersister.getAnalysisPersister(mContext.getApplicationContext());
			persister.verifyVideoDataInGallery();

			Cursor videoCursor = getVideoData(mContext);

			if (videoCursor == null) {
				return null;
			}

			L.d("videoCount = " + videoCursor.getCount());
			int analysisVideoCnt = 0;
			while (videoCursor.moveToNext()) {
				int id = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID));
				String path = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));

                // 20150227 olive : #10806 세로 동영상 제외
				boolean isPotrait = false;
				try {
				    isPotrait = VideoUtils.isPortraitVideo(path);
                    if(isPotrait) {
                        continue;
                    }
				} catch (Exception e) {
				    continue;   
				}
                
				if (!persister.isExistVideoDataInAnalysis(id)) {
					L.d("video id = " + id);
					boolean isFind = false;
					ArrayList<UplusIFrameData> parsedIFrameDataList = getIFrameDataList(mContext, path,
							videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)));

					if (parsedIFrameDataList != null && !parsedIFrameDataList.isEmpty()) {
						for (UplusIFrameData iFrameData : parsedIFrameDataList) {
							if (iFrameData.getUplusIFrameData().numberOfFace > 0) {
								persister.insertVideoData(id, iFrameData.getVideoPath(), iFrameData.getStartPosition(), iFrameData.getEndPosition(),
										iFrameData.getOrientation());
								isFind = true;
								break;
							}
						}

						if (!isFind) {
							persister.insertVideoData(id, parsedIFrameDataList.get(0).getVideoPath(), parsedIFrameDataList.get(0).getStartPosition(),
									parsedIFrameDataList.get(0).getEndPosition(), parsedIFrameDataList.get(0).getOrientation());
						}
					}

					analysisVideoCnt++;
					if (analysisVideoCnt >= MAXIMUM_PREPARE_VIDEO_ANALYSIS_COUNT) {
						if (mUplusVideoAnalysisListener != null) {
							L.d("Video Analysis Count break");
							mUplusVideoAnalysisListener.onUplusVideoPrepareAnalysisTotalFinish(false);
						}
						break;
					}
				}
			}

			if (videoCursor != null) {
				videoCursor.close();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mUplusVideoAnalysisListener != null) {
				L.d("Video Analysis End");
				mUplusVideoAnalysisListener.onUplusVideoPrepareAnalysisTotalFinish(true);
			}
		}
	}
}
