
package com.kiwiple.imageframework.cosmetic;

import com.qualcomm.snapdragon.sdk.face.FaceData;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

/**
 * 구글 FaceDetector 클래스 또는 퀄컴 SDK를 이용해 사진의 얼굴정보를 얻어와 눈, 입, 턱, 볼의 위치와 영역을 계산해 주는 클래스
 * 
 * @version 2.0
 */
public class Face {
    /**
     * Android API를 사용하여 얼굴인식. Use with {@link #mSourceData}
     */
    private static final int SOURCE_DATA_ANDROID_API = 0;
    /**
     * Qualcomm SDK를 사용하여 얼굴인식. Use with {@link #mSourceData}
     */
    private static final int SOURCE_DATA_QUALCOMM_API = 1;

    public static final float CONFIDENCE_THRESHOLD = 0.4f;
    public static final int EULER_X = 0;
    public static final int EULER_Y = 1;
    public static final int EULER_Z = 2;
    
    private boolean DEBUG = false;

    /**
     * 세팅된 값의 출처
     * 
     * @see {@link #SOURCE_DATA_ANDROID_API}<br>
     *      {@link #SOURCE_DATA_QUALCOMM_API}
     */
    private int mSourceData = SOURCE_DATA_ANDROID_API;

    /**
     * 퀄컴 SDK로 detecting한 얼굴 정보.
     */
    public FaceData mFaceData;

    public float confidence() {
        return mConfidence;
    }

    public void getMidPoint(PointF point) {
        // don't return a PointF to avoid allocations
        point.set(mMidPointX, mMidPointY);
    }

    public float eyesDistance() {
        return mEyesDist;
    }

    public float pose(int euler) {
        // don't use an array to avoid allocations
		if (euler == EULER_X)
			return mPoseEulerX;
		else if (euler == EULER_Y)
			return mPoseEulerY;
		else if (euler == EULER_Z)
			return mPoseEulerZ;
        throw new IllegalArgumentException();
    }

    public Face() {
    }

    /**
     * 구글 FaceDetector 클래스를 이용해 얻어온 사진의 얼굴정보를 설정한다.
     * 
     * @param face {@link android.media.FaceDetector#Face}
     * @version 2.0
     */
    public void copy(android.media.FaceDetector.Face face) {
        if(face == null) {
            return;
        }
        mSourceData = SOURCE_DATA_ANDROID_API;
        mConfidence = face.confidence();
        PointF point = new PointF();
        face.getMidPoint(point);
        mMidPointX = point.x;
        mMidPointY = point.y;
        mEyesDist = face.eyesDistance();
        
        // x,y,z 축 회전 각도인 듯 하나 아직까지 값이 넘어온 적은 없음... 
        mPoseEulerX = face.pose(android.media.FaceDetector.Face.EULER_X);
        mPoseEulerY = face.pose(android.media.FaceDetector.Face.EULER_Y);
        mPoseEulerZ = face.pose(android.media.FaceDetector.Face.EULER_Z);
        
        if(DEBUG){
            Log.e("Face","copy, mEyesDist:"+mEyesDist);
    		Log.e("Face","copy, mMidPointX:"+mMidPointX+",mMidPointY:"+mMidPointY);
        }
    }

    /**
     * Qualcomm SDK를 이용해 얻어온 사진의 얼굴정보를 설정한다.
     * 
     * @param face {@link com.qualcomm.snapdragon.sdk.face.FaceData}
     */
    public void copy(FaceData face) {
        if(face==null) {
           return;
        }
        mSourceData = SOURCE_DATA_QUALCOMM_API;
        mFaceData = face;
        
        mConfidence = face.getRecognitionConfidence();
		mEyesDist = (float) Math.hypot(face.rightEye.x - face.leftEye.x, face.rightEye.y - face.leftEye.y);
        
        mMidPointX = face.leftEye.x + Math.abs((face.rightEye.x - face.leftEye.x) / 2);//face.leftEye.x + mEyesDist/2;/
        mMidPointY = face.leftEye.y + (face.rightEye.y - face.leftEye.y) / 2;//face.leftEye.y ;//
        
        mPoseEulerX = face.getPitch();
        mPoseEulerY = face.getYaw();
        mPoseEulerZ = face.getRoll();
        mRotation = mPoseEulerZ;
        if(DEBUG){
        	Log.e("Face","copy, mPoseEulerX:"+mPoseEulerX+", mPoseEulerY:"+mPoseEulerY+", mPoseEulerZ:"+mPoseEulerZ);
        	Log.e("Face","copy, face.leftEye.x:"+face.leftEye.x+", face.rightEye.x:"+face.rightEye.x);
            Log.e("Face","copy, mEyesDist:"+mEyesDist+", mMidPointX:"+mMidPointX+", mMidPointY:"+mMidPointY);
        }
    }

    public float mConfidence;
    public float mMidPointX;
    public float mMidPointY;
    public float mEyesDist;
    public float mPoseEulerX;
    public float mPoseEulerY;
    public float mPoseEulerZ;
    public float mWidth;
    public float mHeight;
    
    private RectF mImageBound = new RectF();

    /**
     * 왼쪽 눈의 영역 정보
     * 
     * @version 2.0
     */
    public RectF mLeftEyeBound = new RectF();
    /**
     * 오른쪽 눈의 영역 정보
     * 
     * @version 2.0
     */
    public RectF mRightEyeBound = new RectF();
    /**
     * 볼의 영역 정보
     * 
     * @version 2.0
     */
    public RectF mCheekBound = new RectF();
    /**
     * 얼굴 전체 영역 정보
     * 
     * @version 2.0
     */
    public RectF mFaceBound = new RectF();
    /**
     * 턱의 영역 정보
     * 
     * @version 2.0
     */
    public RectF mChinBound = new RectF();
    /**
     * 입의 영역 정보
     * 
     * @version 2.0
     */
    public RectF mMouseBound = new RectF();
    /**
     * 콧등 영역 정보 - midpoint ~ 입영역 위쪽 까지 부분의 2/3 까지의 영역 
     * 
     * @version 2.0
     */
    public RectF mNoseBound = new RectF();
    /**
     * 왼쪽 빰 영역, 콧등 영역의 1/2 부분에서 입영역위쪽 까지의 영역 
     * 
     * @version 2.0
     */
    public RectF mLeftCheekBound = new RectF();
    /**
     * 오른쪽 빰 영역, 콧등 영역의 1/2 부분에서 입 영역 위쪽 까지의 영역 
     * 
     * @version 2.0
     */
    public RectF  mRightCheekBound = new RectF();

    public float mRotation = 0f;

    /**
     * 구글 FaceDetector 클래스를 이용해 얻어온 사진의 얼굴정보를 사람 얼굴의 황금비율 기반으로하여 눈, 입, 턱 위치를 계산한다.
     * 
     * @remark <a href="http://www.intmath.com/blog/is-she-beautiful-the-new-golden-ratio/4149">Face
     *         Golden Ratio</a>
     * @remark <a href="http://ocean.kisti.re.kr/downfile/volume/kips/JBCRFU/2009/v16Bn4/JBCRFU_2009_v16Bn4_299.pdf">AAM  algorithm
     * @version 2.0
     */
    public void prepare() {
        if(mSourceData == SOURCE_DATA_QUALCOMM_API){
        	if(mFaceData==null) {
        		return;
        	}
        	mEyesDist = Math.abs(mEyesDist);
        	mLeftEyeCenterX = mFaceData.leftEye.x;
            mLeftEyeCenterY = mFaceData.leftEye.y;
            
            mRightEyeCenterX = mFaceData.rightEye.x;
            mRightEyeCenterY = mFaceData.rightEye.y;
            
            mLeftEyeSize = mEyesDist / 4f;
            mRightEyeSize = mEyesDist / 4f;
            
            mMouseCenterX = mFaceData.mouth.x;
            mMouseCenterY = mFaceData.mouth.y;
        }
        else {
        	 mLeftEyeCenterX = mMidPointX;
             mLeftEyeCenterY = mMidPointY;
             // centerX - eyesDistance / 2, centerY, eyesDistance / 4
             mLeftEyeCenterX -= mEyesDist / 2f;
             
             mRightEyeCenterX = mMidPointX;
             mRightEyeCenterX += mEyesDist / 2f;
             mRightEyeCenterY = mLeftEyeCenterY;
             
             mLeftEyeSize = mEyesDist / 4f;
             mRightEyeSize = mEyesDist / 4f;
             
             mMouseCenterX = mMidPointX;
             // reference: http://www.intmath.com/blog/is-she-beautiful-the-new-golden-ratio/4149
             float distanceEyeToMouse = mEyesDist * (148.f / 138.f);
             mMouseCenterY = mRightEyeCenterY + distanceEyeToMouse;
        	
        }
    }

    public float mLeftEyeCenterX;
    public float mLeftEyeCenterY;
    public float mRightEyeCenterX;
    public float mRightEyeCenterY;

    public float mMouseCenterX;
    public float mMouseCenterY;

    public float mLeftEyeSize;
    public float mRightEyeSize;

    public float mMouseWidth;
    public float mMouseHeight;

    public float mChinCenterX;
    public float mChinCenterY;

    public float mChinWidth;
    public float mChinHeight;

    public float mDistanceEyeToMouse;

    /**
     * {@link #copy(android.media.FaceDetector.Face)}에서 입력 받은 정보를 기반으로 하여 눈, 입, 턱, 볼의 영역 정보를 계산한다.
     * 
     * @version 2.0
     */
    public void calculateFaceBound() {
    	 mImageBound.set(0, 0, mWidth, mHeight);
         

        mLeftEyeBound.set(mLeftEyeCenterX, mLeftEyeCenterY, mLeftEyeCenterX, mLeftEyeCenterY);
       
        mRightEyeBound.set(mRightEyeCenterX, mRightEyeCenterY, mRightEyeCenterX, mRightEyeCenterY);
       
        mMouseBound.set(mMouseCenterX, mMouseCenterY, mMouseCenterX, mMouseCenterY);
        
        /**
         * check poseEulerZ
         */
        Matrix m = new Matrix();
        
        m.setRotate(-mPoseEulerZ, mMidPointX,mMidPointY);
        m.mapRect(mImageBound);
        m.mapRect(mLeftEyeBound);
        m.mapRect(mRightEyeBound);
        m.mapRect(mMouseBound);
        
        mMouseCenterX = mMouseBound.centerX();
        mMouseCenterY = mMouseBound.centerY();
        
        mMouseWidth = mEyesDist / 2f;
        mMouseHeight = mEyesDist / 5f;

        mChinCenterX = mMouseCenterX;
        mChinCenterY = mMouseCenterY + mMouseHeight*2f;
        mChinWidth = mMouseWidth;
        mChinHeight = mMouseHeight*2.0f;

        mDistanceEyeToMouse = mMouseCenterY - mRightEyeCenterY;
        
        if(DEBUG){
            Log.e("Face","calculateFaceBound, mDistanceEyeToMouse:"+mDistanceEyeToMouse);
    		Log.e("Face","calculateFaceBound, mMidPointX:"+mMidPointX+",mMidPointY:"+mMidPointY);
        }
        
        
        mLeftEyeBound.left -= mLeftEyeSize;
        mLeftEyeBound.right += mLeftEyeSize;
        mLeftEyeBound.top -= mLeftEyeSize;
        mLeftEyeBound.bottom += mLeftEyeSize;

        mRightEyeBound.left -= mRightEyeSize;
        mRightEyeBound.right += mRightEyeSize;
        mRightEyeBound.top -= mRightEyeSize;
        mRightEyeBound.bottom += mRightEyeSize;
 
        
        mMouseBound.left -= mMouseWidth;
        mMouseBound.right += mMouseWidth;
        mMouseBound.top -= mMouseHeight;
        mMouseBound.bottom += mMouseHeight;
        
        mChinBound.top = (int)mMouseBound.bottom;
        mChinBound.bottom = (int)Math.min(mImageBound.bottom, mChinCenterY + mChinHeight);
        mChinBound.left = (int)Math.max(0.0f, mChinCenterX - mChinWidth);
        mChinBound.right = (int)Math.min(mImageBound.right, mChinCenterX + mChinWidth);
        
       

        if (mLeftEyeBound.top < mRightEyeBound.top) {
			mCheekBound.top = (int) Math.max(0.0f, mLeftEyeCenterY - mRightEyeSize/4 );
		} else if(mLeftEyeBound.top > mRightEyeBound.top) {
			mCheekBound.top = (int) Math.max(0.0f, mRightEyeCenterY- mRightEyeSize/4 );
		} else {
			mCheekBound.top = mLeftEyeBound.bottom;//mMidPointY-mLeftEyeSize/2; 
		}
        
		mCheekBound.bottom = mMouseBound.bottom;
		mCheekBound.left = (int) Math.max(0.0f, mLeftEyeBound.left - mLeftEyeSize );
		mCheekBound.right = (int) Math.min(mImageBound.right,  mRightEyeBound.right + mRightEyeSize );
		
		
		mLeftCheekBound.left = mCheekBound.left +(mLeftEyeBound.left - mCheekBound.left)/3;
        mLeftCheekBound.top = mLeftEyeBound.bottom;
        mLeftCheekBound.bottom = mMouseBound.top;
        mLeftCheekBound.right = mLeftEyeBound.right - (mLeftEyeBound.right - mLeftEyeBound.left)/6;
        
        mRightCheekBound.left = mRightEyeBound.left + (mLeftEyeBound.right - mLeftEyeBound.left)/6;
        mRightCheekBound.top = mRightEyeBound.bottom;
        mRightCheekBound.bottom = mMouseBound.top;
        mRightCheekBound.right = mCheekBound.right  -  (mCheekBound.right - mRightEyeBound.right)/3;
        
		
        float faceHeight = mDistanceEyeToMouse * (200.f/72.f);
        float faceWidth = mEyesDist * (156.f/ 72.f );
        
        mFaceBound.top = (int)Math.max(0,(mMidPointY - faceHeight*2/3));
        mFaceBound.bottom = (int)Math.min(mImageBound.bottom,(mMidPointY + faceHeight*2/3));
        mFaceBound.left = (int)Math.max(0,(mMidPointX - faceWidth*2/3));
        mFaceBound.right = (int)Math.min(mImageBound.right,(mMidPointX + faceWidth*2/3));
        
        mNoseBound.top = (mLeftEyeCenterY + mRightEyeCenterY)/2;
        mNoseBound.left = mLeftEyeBound.right;
        mNoseBound.right = mRightEyeBound.left;
        mNoseBound.bottom = mNoseBound.top+(mMouseBound.top - mNoseBound.top)*2/3;
                
       //calculate yaw at cheek bound
        
		/*float cheekSize = Math.abs(mCheekBound.left - mLeftEyeBound.left);
		float movingPer = -(mPoseEulerY / 30f * (cheekSize)) ;

		//calculate yaw at face bound
        mFaceBound.left+=movingPer;
        mFaceBound.right+=movingPer;
        
		mCheekBound.left += movingPer;
		mCheekBound.right += movingPer;

		mMouseBound.left += movingPer;
		mMouseBound.right += movingPer;

		mChinBound.left += movingPer;
		mChinBound.right += movingPer;

		*/
        // point is the point about which to rotate.
        m.setRotate(mPoseEulerZ, mMidPointX,mMidPointY);
        m.mapRect(mLeftEyeBound);
        m.mapRect(mRightEyeBound);
        m.mapRect(mMouseBound);
        m.mapRect(mChinBound);
        m.mapRect(mCheekBound);
        m.mapRect(mFaceBound);
        m.mapRect(mNoseBound);
        m.mapRect(mImageBound);
        m.mapRect(mRightCheekBound);
        m.mapRect(mLeftCheekBound);
        mMouseCenterX = mMouseBound.centerX();
        mMouseCenterY = mMouseBound.centerY();
        mChinCenterX = mChinBound.centerX();
        mChinCenterY = mChinBound.centerY();
        mChinWidth = mChinBound.width();
        mChinHeight = mChinBound.height();
        
		if(DEBUG){
		        Log.e("Face","calculate, mFaceBound:"+mFaceBound.toString());
				Log.e("Face","calculate, mLeftEyeBound:"+mLeftEyeBound.toString()+", mRightEyeBound:"+mRightEyeBound.toString());
		        Log.e("Face","calculate, faceHeight:"+faceHeight+", faceWidth:"+faceWidth);
		        Log.e("Face","calculate, mEyesDist:"+mEyesDist+", mMidPointX:"+mMidPointX+", mMidPointY:"+mMidPointY);
	    }
     }
}
