
package com.kiwiple.imageframework.cosmetic;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.kiwiple.imageframework.gpuimage.ArtFilterManager;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;

/**
 * 성형기능 지원을 위해 이미지 확대, 축소, 잡티제거, 색온도 조절, 밝기 조절 등 필터 효과를 부여하는 클래스<br>
 * {@link #getInstance} 함수로 인스턴스를 생성하고, 싱글톤으로 동작한다
 * 
 * @version 2.0
 */
public class FilterManager {
    private FilterManagerListener mListener;
    private FilterProcess mFilterProcess;
    private Context mContext;
    private Face[] mFaces;
    private Face mFace;

    /**
     * 필터 효과가 부여될 이미지의 얼굴 정보를 설정한다.
     * 
     * @param face 이미지의 얼굴 정보
     * @version 2.0
     */
    public void setFace(Face face) {
        mFace = face;
    }
    
    public void setFaces(Face[] face) {
    	mFaces = face;
    }

    /**
     * 필터 효과가 부여될 이미지의 얼굴 정보를 반환한다.
     * 
     * @return 이미지의 얼굴 정보
     * @version 2.0
     */
    public Face getFace() {
        return mFace;
    }
    

    public Face[] getFaces(){
    	return mFaces;
    }

    

    /**
     * 부여할 필터 효과의 정보를 관리하는 클래스
     * 
     * @version 2.0
     */
    public static class FilterInfo {
        /**
         * 부여할 필터의 이름
         * 
         * @version 2.0
         */
        public String mFilterName;
        /**
         * 필터 효과를 부여할 이미지
         * 
         * @version 2.0
         */
        public Bitmap mTargetImage;
        /**
         * 부여할 필터의 상세 값
         * 
         * @version 2.0
         */
        public float[] mValue;
        public Object mObject;

        public FilterInfo(String filterName, Bitmap targetImage, float[] values, Object object) {
            mFilterName = filterName;
            mTargetImage = targetImage;
            mValue = values;
            mObject = object;
        }
    }

    /**
     * 원본 이미지에 필터 효과 적용이 완료되면 호출되는 콜백 인터페이스
     * 
     * @version 2.0
     */
    public interface FilterManagerListener {

        /**
         * 필터 효과 적용이 완료되면 호출된다.
         * 
         * @param data 필터 효과가 적용된 이미지
         * @param object
         * @version 2.0
         */
        public void onImageFilteringComplete(Bitmap data, Object object);
    }

    private static FilterManager sInstance;

    /**
     * @param context
     * @return {@link #FilterManager}의 인스턴스 반환
     * @version 2.0
     */
    public static synchronized FilterManager getInstance(Context context) {
        if(sInstance == null) {
            sInstance = new FilterManager(context);
        }
        return sInstance;
    }

    /**
     * {@link #FilterManagerListener}<br>
     * 필터 효과 적용 결과를 반환해주는 리스너를 등록한다.
     * 
     * @param listener 필터 효과 적용 결과 리스너
     * @version 2.0
     */
    public void setFilterManagerListener(FilterManagerListener listener) {
        mListener = listener;
    }

    private FilterManager(Context context) {
        mContext = context;

        mFilterProcess = new FilterProcess();
        mFilterProcess.start();
    }

    /**
     * {@link FilterManager} 리소스 반환
     * 
     * @version 2.0
     */
    public void release() {
        mFilterProcess.cancelFiltering();
        mFilterProcess.clearThread();
    }

    /**
     * 필터 효과를 부여할 이미지 및 필터 정보 추가
     * 
     * @param data 이미지 및 필터 정보
     * @version 2.0
     */
    public void addFilterData(FilterInfo data) {
        mFilterProcess.addFilterData(data);
    }

    /**
     * 필터 효과를 적용할 이미지가 남아 있는지 확인한다.
     * 
     * @return 필터 효과를 적용할 이미지 개수
     * @version 2.0
     */
    public boolean isEmpty() {
        return mFilterProcess.isEmpty();
    }

    /**
     * 진행중인 필터 효과 적용 이미지 목록을 삭제한다.
     * 
     * @version 2.0
     */
    public void clear() {
        mFilterProcess.cancelFiltering();
    }


    /**
     * 잡티 제거를 위한 Median filter 적용
     * 
     * @param image 적용할 이미
     * @param mSize 매트릭스 크
     */
	public void processMedianfilter2(Bitmap image, int mSize ) {
		int MEDIAN_SIZE = mSize;
		int width = image.getWidth();
		int height = image.getHeight();
		int index = 0;
		int bound = mSize/2;
		
		int[] argb = new int[MEDIAN_SIZE*MEDIAN_SIZE];
		int[] r = new int[MEDIAN_SIZE*MEDIAN_SIZE];
		int[] g = new int[MEDIAN_SIZE*MEDIAN_SIZE];
		int[] b = new int[MEDIAN_SIZE*MEDIAN_SIZE];
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		
		image.getPixels(inPixels,0, width,0, 0, width, height);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int k = 0;
				for (int dy = -bound; dy < bound; dy++) {
					int iy = y+dy;
					if (0 <= iy && iy < height) {
						int ioffset = iy*width;
						for (int dx = -bound; dx < bound; dx++) {
							int ix = x+dx;
							if (0 <= ix && ix < width) {
								int rgb = inPixels[ioffset+ix];
								argb[k] = rgb;
								r[k] = (rgb >> 16) & 0xff;
								g[k] = (rgb >> 8) & 0xff;
								b[k] = rgb & 0xff;
								k++;
							}
						}
					}
				}
				
				outPixels[index++] = argb[rgbMedian(r, g, b, mSize*mSize)];
			}
		}
		
		image.setPixels(outPixels, 0,  width, 0,0, width, height);
	}
	
	 private int rgbMedian(int[] r, int[] g, int[] b, int size) {
			int sum, index = 0, min = Integer.MAX_VALUE;
			
			for (int i = 0; i < size; i++) {
				sum = 0;
				for (int j = 0; j < size; j++) {
					sum += Math.abs(r[i]-r[j]);
					sum += Math.abs(g[i]-g[j]);
					sum += Math.abs(b[i]-b[j]);
				}
				if (sum < min) {
					min = sum;
					index = i;
				}
			}
			return index;
		}
    

    private class FilterProcess extends Thread {
        private ArrayList<FilterInfo> mThreadQueue;
        private boolean mFlag = false;
        ArtFilterManager artFilterManager;

        public FilterProcess() {
            mThreadQueue = new ArrayList<FilterInfo>();
        }

        public void addFilterData(FilterInfo data) {
            synchronized(mThreadQueue) {
                mThreadQueue.add(data);
                mThreadQueue.notify();
            }
        }

        public void cancelFiltering() {
            synchronized(mThreadQueue) {
                mThreadQueue.clear();
            }
        }

        public void clearThread() {
            synchronized(mThreadQueue) {
                mFlag = true;
                mThreadQueue.notify();
            }
        }

        public boolean isEmpty() {
            boolean empty;
            synchronized(mThreadQueue) {
                empty = mThreadQueue.size() == 0;
            }
            return empty;
        }

        @Override
        public void run() {
            artFilterManager = ArtFilterManager.getInstance();
            artFilterManager.initGL(1, 1);
            while(true) {
                if(mFlag) {
                    break;
                }

                final FilterInfo data;

                synchronized(mThreadQueue) {
                    if(mThreadQueue.size() != 0) {
                        data = mThreadQueue.get(0);
                        mThreadQueue.remove(0);
                    } else {
                        data = null;
                    }
                }
                if(data != null) {
                    
                	if("Median2".equals(data.mFilterName)){
                    	processMedianfilter2(data.mTargetImage, (int)data.mValue[0]);
                    }else {
                    
						ArtFilterUtils.initFilter(mContext, data.mFilterName, data.mValue, data.mTargetImage.getWidth(), data.mTargetImage.getHeight());
						ArtFilterUtils.processArtFilter(mContext, data.mTargetImage);
					}
                    
                    if(mListener != null) {
                        mListener.onImageFilteringComplete(data.mTargetImage, data.mObject);
                    }
                }

                synchronized(mThreadQueue) {
                    try {
                        if(mThreadQueue.size() == 0) {
                            mThreadQueue.wait();
                        }
                    } catch(InterruptedException e) {
                    }
                }
            }
            artFilterManager.deinitGL();
        }
    }
}
