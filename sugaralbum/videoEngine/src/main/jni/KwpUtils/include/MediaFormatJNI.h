#ifndef _MEDIA_FORMAT_JNI_h
#define _MEDIA_FORMAT_JNI_h

#include <jni.h>
#include <libavformat/avformat.h>

/**
 * Android Media format과 매칭시켜 cpp에서 사용되는 struct
 */
typedef struct _MediaForamtJNI
{
	public:
		AVMediaType 	mMediaType = AVMEDIA_TYPE_UNKNOWN;
		AVCodecID 	mCodecId;
		char* 	mMimeName = NULL;
		int		mMaxInputSize = 0;
		int		mBitRate = 0;
		int		mWidth = 0;
		int		mHeight = 0;
		int		mColorFormat = 0;
		int		mFrameRate = 0;
		int		mIFrameInterval = 0;
		int		mChannel = 0;
		int		mSampleRate = 0;
		int		mIsAdts = 0;
		int		mAACProfile = 0;
		int		mChannelMask = 0;
		int		mFlacCompressionLevel = 0;

}MediaFormatJNI;


#endif
