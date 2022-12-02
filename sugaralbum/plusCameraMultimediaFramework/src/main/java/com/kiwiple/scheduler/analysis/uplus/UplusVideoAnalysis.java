package com.kiwiple.scheduler.analysis.uplus;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.util.ImageUtils;
import com.kiwiple.multimedia.util.VideoUtils;
import com.kiwiple.debug.L;
import com.kiwiple.scheduler.analysis.VideoAnalysis;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusImageFileSceneCoordinator;
import com.kiwiple.scheduler.data.AnalyzedInputData;
import com.kiwiple.scheduler.data.InputData;
import com.kiwiple.scheduler.data.MultiLayerData;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.SelectedOutputData;
import com.kiwiple.scheduler.data.uplus.UplusInputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.util.DateUtil;

public class UplusVideoAnalysis extends VideoAnalysis {

	private static final long ONE_DAY = 1000 * 60 * 60 * 24;
	private static final long VIDEO_MINIMUM_DURATION = 1000 * 10;
	private static final long VIDEO_MAXIMUM_DURATION = 1000 * 120;

	private UplusInputData mUplusInputData;
	private UplusOutputData mUplusOutputData;
	private JSONObject mTransitionJsonObject; 

	/**
	 * UplusVideoAnalysis 생성자. 
	 * @param context : Context. 
	 * @param maxVideoCnt : 최대 비디오 갯수. 
	 * @param requestAnalysis : 비디오 분석 여부. 
	 */
	public UplusVideoAnalysis(Context context, int maxVideoCnt, boolean requestAnalysis) {
		super(context);
		setMaxVideoCount(maxVideoCnt);
		setRequestAnalysis(requestAnalysis);
	}

	@Override
	public void startInputDataAnalysis(InputData inputData, OutputData outputData) {
		mUplusInputData = (UplusInputData) inputData;
		mUplusOutputData = (UplusOutputData) outputData;

		if (mRequestAnalysis) {
/*			Comparator<SelectedOutputData> comparatorDate = new Comparator<SelectedOutputData>() {

				@Override
				public int compare(SelectedOutputData lhs, SelectedOutputData rhs) {
					return lhs.getDate() <= rhs.getDate() ? -1 : 1;
				}
			};
			Collections.sort(mUplusOutputData.getOutputDataList(), comparatorDate);
*/
			long beforeData = 0;
			for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {
				if (selectedOutputData.getSceneType().equals(ImageFileScene.JSON_VALUE_TYPE)) {
					long newDate = DateUtil.getDayMillisecond(selectedOutputData.getDate());
					if (beforeData != newDate) {
						beforeData = newDate;
						findVideoData(mContext, selectedOutputData.getDate());
					}
				}
			}
		}
	}

	@Override
	public void selectVideoFileScene() {
		if (mUplusInputData.getVideoDataList().size() > 0) {
			int maxVideoIndex;
			ImageData videoData;
			
			if(mUplusInputData.getVideoDataList().size() == 2){
				videoData = mUplusInputData.getVideoDataList().get(0);
				selectVideoFile(new AnalyzedInputData(String.valueOf(videoData.id), videoData.date));
				maxVideoIndex = 1;
			}else{
				maxVideoIndex = 0;
			}

			videoData = mUplusInputData.getVideoDataList().get(maxVideoIndex);
			 // 비디오 비율에 맞는 위치를 선택
			 MatchedVideoMultiScene matchedVideoMultiscene = selectVideoMultiSceneForRatio(videoData);
			 // 선택 여부
			 if (matchedVideoMultiscene != null && matchedVideoMultiscene.outputData != null) {
			     SelectedOutputData selectedOutputData = matchedVideoMultiscene.outputData;
                 ArrayList<ImageData> imageDatas = new ArrayList<ImageData>(matchedVideoMultiscene.outputData.getImageDatas());
                 if (!imageDatas.isEmpty()) {
                     // 이미지와 동영상을 교체
                     ImageData remainImageData = imageDatas.get(matchedVideoMultiscene.frameIndex);
                     imageDatas.remove(matchedVideoMultiscene.frameIndex);
                     imageDatas.add(matchedVideoMultiscene.frameIndex, videoData);
                     selectedOutputData.setImageDatas(new ArrayList<ImageData>(imageDatas));
                     
                     // 교체된 이미지는 새로운 이미지 씬으로 생성
                     createNewImageFileScene(remainImageData, selectedOutputData.getFilterId(), matchedVideoMultiscene.sceneIndex);
                 }
			 } else {
                 selectVideoFile(new AnalyzedInputData(String.valueOf(videoData.id), videoData.date));
			 }
		}
	}

	/**
	 * 해당 date안의 비디오 데이터를 검색한다. 
	 * @param context : Context. 
	 * @param date : 비디오 데이터를 찾아야 하는 날짜 정보. 
	 */
	private void findVideoData(Context context, long date) {
		long startDateMillis = date;
		long endDateMillis = startDateMillis + ONE_DAY;
		L.d("start : " + DateUtil.getDayStringFromDate(mContext, startDateMillis));
		L.d("end : " + DateUtil.getDayStringFromDate(mContext, endDateMillis));
		Cursor videoCursor = getVideoDataCursor(context, startDateMillis, endDateMillis);
		if (videoCursor == null) {
			return;
		}

		while (videoCursor.moveToNext()) {
			boolean isAdd = true;
			int videoId = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID));
			long videoDate = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_TAKEN));
			int width = videoCursor.getInt(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.WIDTH)); 
			int height = videoCursor.getInt(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.HEIGHT)); 
			String contentPath = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));

			if (mInpuDataList.size() + 1 > mMaxVideoCount) {
				break;
			}
			
			/**
			 * db를 통해서 얻은 값이 0일 경우 예외처리 :: retriever 를 통한 크기 가져오기 
			 */
			if(width == 0 || height == 0){
				MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
				mediaMetadataRetriever.setDataSource(contentPath);
				width = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
				height = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
				try {
					mediaMetadataRetriever.release();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(width * height >= (1920*1080)){
				//FHD 비디오는 포함시키지 않는다. 
				continue;  
			}

			for (AnalyzedInputData analyzedInputData : mInpuDataList) {
				if (analyzedInputData.getName().equals(String.valueOf(videoId))) {
					isAdd = false;
					break;
				}
			}

			if (isAdd) {
				L.d("video Id = " + videoId);
				mInpuDataList.add(new AnalyzedInputData(String.valueOf(videoId), videoDate));
			}
		}

		if (videoCursor != null) {
			videoCursor.close();
		}
	}

	/**
	 * 비디오 데이터의 cursor를 얻어 온다. 
	 * @param context : Context. 
	 * @param startDateMillis : 비디오 데이터의 시작 날짜. 
	 * @param endDateMillis : 비디오 데이터의 마지막 날짜. 
	 * @return : 비디오 데이터 cursor. 
	 */
	private Cursor getVideoDataCursor(Context context, long startDateMillis, long endDateMillis) {
		Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, getProjection(),
				getSelection(startDateMillis, endDateMillis), null, getOrder());

		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		return cursor;
	}
	/**
	 * 비디오 데이터 cursor의 projection. 
	 * @return projection.  
	 */
	private String[] getProjection() {
		return new String[] { MediaStore.Video.VideoColumns._ID, MediaStore.Video.VideoColumns.DATE_TAKEN, MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.WIDTH, MediaStore.Video.VideoColumns.HEIGHT  };
	}

	/**
	 * 비디오 데이터 cursor의 selection.
	 * @param startDateMillis : 비디오 데이터의 시작 날짜. 
	 * @param endDateMillis : 비디오 데이터의 마지막 날짜.   
	 * @return : selection.
	 */
	private String getSelection(long startDateMillis, long endDateMillis) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" ( ");
		buffer.append(MediaStore.Video.VideoColumns.DATE_TAKEN);
		buffer.append(" >= ");
		buffer.append(startDateMillis);
		buffer.append(" AND ");
		buffer.append(MediaStore.Video.VideoColumns.DATE_TAKEN);
		buffer.append(" <= ");
		buffer.append(endDateMillis);
		buffer.append(" ) ");
		buffer.append(" AND ");
		buffer.append(" ( ");
		buffer.append(MediaStore.Video.VideoColumns.DURATION);
		buffer.append(" >= ");
		buffer.append(VIDEO_MINIMUM_DURATION); // 10초 이상. 
		buffer.append(" AND ");
		buffer.append(MediaStore.Video.VideoColumns.DURATION);
		buffer.append(" <= ");
		buffer.append(VIDEO_MAXIMUM_DURATION); //2분 이하. 
		buffer.append(" ) ");
		L.d("video cursor slection : " + buffer.toString()); 
		return buffer.toString();
	}
	/**
	 * 비디오 데이터 cursor order. 
	 * @return : order
	 */
	private String getOrder() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(MediaStore.Video.VideoColumns.DATE_TAKEN);
		buffer.append(" DESC ");
		L.d("video cursor order : " + buffer.toString()); 
		return buffer.toString();
	}

	/**
	 * 분석된 데이터를 output data list에 삽입. 
	 * @param analyzedInputData : 분석된 비디오 데이터. 
	 */
	private void selectVideoFile(AnalyzedInputData analyzedInputData) {
		boolean isAdd = true;
		int addPoint =0; 
		for (int i = 0; i < mUplusOutputData.getOutputDataList().size(); i++){
			
			SelectedOutputData selectedOutputData = mUplusOutputData.getOutputDataList().get(i);
			
			if((selectedOutputData.getDate() != -1) && selectedOutputData.getDate() < analyzedInputData.getDate()){
				addPoint = i+1; 
			}
			if (selectedOutputData.getSceneType().equals(VideoFileScene.JSON_VALUE_TYPE)
					&& selectedOutputData.getNameList().contains(analyzedInputData.getName())) {
				isAdd = false;
				break;
			}
			
			if(selectedOutputData.getSceneType().equals(MultiLayerScene.JSON_VALUE_TYPE) 
			        && isContainVideoData(selectedOutputData.getImageDatas()) >= 0) {
		        isAdd = false;
			    break;
			}
		}

		if (isAdd) {
			
			SelectedOutputData selectedOutputData = new SelectedOutputData(VideoFileScene.JSON_VALUE_TYPE, 0, 0);
			selectedOutputData.setDate(analyzedInputData.getDate());
			selectedOutputData.addNameToNameList(analyzedInputData.getName());
			mUplusOutputData.addOutputDataToOutputDataList(addPoint, selectedOutputData); 
		}
	}
	
	/**
	 * 주어진 날짜와 가장 가까운 MultiLayerScene을 찾는다.
	 * @param date 주어진 날짜
	 * @return 가장 가까운 MultiLayerScene
	 */
	private SelectedOutputData getNearMultiLayerOutputData(long date) {
	    long distance = 999999999;
        SelectedOutputData selectedMultiVideoData = null;
        for (int j = 0; j < mUplusOutputData.getOutputDataList().size(); j++) {
            SelectedOutputData selectedOutputData = mUplusOutputData.getOutputDataList().get(j);
            if (selectedOutputData.getSceneType().equals(MultiLayerScene.JSON_VALUE_TYPE)) {
                long newDate = DateUtil.getDayMillisecond(selectedOutputData.getDate());
                if (Math.abs(date - newDate) < distance) {
                    distance = Math.abs(date - newDate);
                    selectedMultiVideoData = selectedOutputData;
                }
            }
        }
        return selectedMultiVideoData;
	}
	
	public static boolean isMovieData(ImageData imageData) {
	    return (ImageUtils.getMimeType(imageData.path).startsWith("video/"));
	}
	
	/**
	 * 이미지 데이터 내에 비디오가 있는지 판단.
	 * 
	 * @param imageDatas 검색할 이미지 데이터 배열
	 * @return 처음 포함된 비디오가 있는 인덱스. 없다면 -1 반환
	 */
	public static int isContainVideoData(ArrayList<ImageData> imageDatas) {
	    if (imageDatas == null) {
	        return -1;
	    }
	    
	    for (int i = 0; i < imageDatas.size(); i++) {
	        if (isMovieData(imageDatas.get(i))) {
	            return i;
	        }
	    }
	    return -1;
	}
	
	private MatchedVideoMultiScene selectVideoMultiSceneForRatio(ImageData videoData) {
	    if (videoData == null || videoData.path == null || videoData.path.isEmpty()) {
	        return null;
	    }
	    
	    int videoOrientation = VideoUtils.getVideoRotation(videoData.path);
	    float videoRatio = (float)videoData.width / videoData.height;
	    if (videoOrientation == 90 || videoOrientation == 270) {
	        videoRatio = (float)videoData.height / videoData.width;
	    }
	    
	    MatchedVideoMultiScene matchedVideoMultiscene = new MatchedVideoMultiScene();
	    for (int i = 0; i < mUplusOutputData.getOutputDataList().size(); i++) {
	        SelectedOutputData selectedOutputData = mUplusOutputData.getOutputDataList().get(i);
	        // 이미 MultiLayerScene에 비디오가 포함되어 있는지 여부를 확인
            int videoIndex = isContainVideoData(selectedOutputData.getImageDatas());
	        if (selectedOutputData.getSceneType().equals(MultiLayerScene.JSON_VALUE_TYPE) 
	                && videoIndex == -1 // 이미 비디오가 포함되어 있다면 적용하지 않음 
	                && !selectedOutputData.getMultiFilter()) { // 멀티 필터씬에는 적용하지 않음
	            if (selectedOutputData.getFrameId() == MultiLayerData.MULTI_LAYER_COLUMN_TWO_PICTURES_ID) {
	                    matchedVideoMultiscene.outputData = selectedOutputData;
                        matchedVideoMultiscene.frameIndex = 0;
	            } 
//	            else if (selectedOutputData.getFrameId() == MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID) {
//	                if (videoRatio <= 1) {
//                        matchedVideoMultiscene.outputData = selectedOutputData;
//                        matchedVideoMultiscene.frameIndex = 0;
//                    }
//	            }
	            else if (selectedOutputData.getFrameId() == MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID) {
	                if (videoRatio >= 1) {
                        matchedVideoMultiscene.outputData = selectedOutputData;
                        matchedVideoMultiscene.frameIndex = 1;
                    } else {
                        matchedVideoMultiscene.outputData = selectedOutputData;
                        matchedVideoMultiscene.frameIndex = 0;
                    }
	            } else if (selectedOutputData.getFrameId() == MultiLayerData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID) {
	                if (videoRatio >= 1) {
                        matchedVideoMultiscene.outputData = selectedOutputData;
                        matchedVideoMultiscene.frameIndex = 0;
                    }
	            } else if (selectedOutputData.getFrameId() == MultiLayerData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID) {
	                matchedVideoMultiscene.outputData = selectedOutputData;
                    matchedVideoMultiscene.frameIndex = 1;
	            } else {
	                matchedVideoMultiscene.outputData = null;
	                matchedVideoMultiscene.frameIndex = -1;
	            }
	            matchedVideoMultiscene.sceneIndex = mUplusOutputData.getOutputDataList().indexOf(selectedOutputData);
	        }
	        
	        if (matchedVideoMultiscene.outputData != null) {
                break;
            }
	    }
	    return matchedVideoMultiscene;
	}
	
	private class MatchedVideoMultiScene {
	    public SelectedOutputData outputData = null;
	    public int frameIndex = -1;
	    public int sceneIndex = -1;
	}
	
	private void createNewImageFileScene(ImageData imageData, int filterId, int sceneIndex) {
	 // 교체된 이미지 데이터를 싱글씬으로 추가해준다.
        SelectedOutputData imageFileSceneSelectedOutputData = new SelectedOutputData(ImageFileScene.JSON_VALUE_TYPE, filterId, UplusImageFileSceneCoordinator.SINGLE_FRAME_ID);
        ArrayList<ImageData> imageFileSceneImages = new ArrayList<ImageData>(); 
        imageFileSceneImages.add(imageData); 
        imageFileSceneSelectedOutputData.setImageDatas(imageFileSceneImages);
        imageFileSceneSelectedOutputData.setDate(imageFileSceneImages.get(0).date); 
        mUplusOutputData.addOutputDataToOutputDataList(sceneIndex, imageFileSceneSelectedOutputData);
	}
	
	private void selectVideoMultiSceneForNearSelection() {
	    if (mUplusInputData.getVideoDataList().size() > 0) {
            for (int i = 0; i < mUplusInputData.getVideoDataList().size(); i++) {
                ImageData videoData = mUplusInputData.getVideoDataList().get(i);
                // 가장 가까운 MultiLayerScene을 찾는다.
                SelectedOutputData selectedMultiVideoData = getNearMultiLayerOutputData(videoData.date);
                // 찾았다면
                if(selectedMultiVideoData != null) {
                    int sceneIndex = mUplusOutputData.getOutputDataList().indexOf(selectedMultiVideoData);
                    ImageData remainImageData = null; 
                    // 이미 MultiLayerScene에 비디오가 포함되어 있는지 여부를 확인
                    int videoIndex = isContainVideoData(selectedMultiVideoData.getImageDatas());
                    if (videoIndex == -1) {
                        // 우선적으로 첫번째 이미지와 동영상을 교체
                        ArrayList<ImageData> imageDatas = new ArrayList<ImageData>(selectedMultiVideoData.getImageDatas());
                        if (imageDatas != null && !imageDatas.isEmpty()) {
                            remainImageData = imageDatas.get(0);
                            selectedMultiVideoData.getImageDatas().remove(0);
                            selectedMultiVideoData.getImageDatas().add(0, videoData);
                            
                            // 교체된 이미지 데이터를 싱글씬으로 추가해준다.
                            SelectedOutputData imageFileSceneSelectedOutputData = new SelectedOutputData(ImageFileScene.JSON_VALUE_TYPE, selectedMultiVideoData.getFilterId(), UplusImageFileSceneCoordinator.SINGLE_FRAME_ID);
                            ArrayList<ImageData> imageFileSceneImages = new ArrayList<ImageData>(); 
                            imageFileSceneImages.add(remainImageData); 
                            imageFileSceneSelectedOutputData.setImageDatas(imageFileSceneImages);
                            imageFileSceneSelectedOutputData.setDate(imageFileSceneImages.get(0).date); 
                            mUplusOutputData.addOutputDataToOutputDataList(sceneIndex, imageFileSceneSelectedOutputData);
                        }
                    }
                } else {
                    // 못찾았다면 이녀석은 비디오씬으로 변경
                    if (mInpuDataList.size() + 1 > mMaxVideoCount) {
                        break;
                    }
                    mInpuDataList.add(new AnalyzedInputData(String.valueOf(videoData.id), videoData.date));                 
                }
            }
        }
	}
}
