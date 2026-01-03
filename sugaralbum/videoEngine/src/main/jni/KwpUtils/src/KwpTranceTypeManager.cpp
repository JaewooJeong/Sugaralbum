#include <KwpTranceTypeManager.h>

#define TAG		"KwpTranceTypeManager_CPP"

#include <jni.h>
#include <string.h>

#include <libavformat/avformat.h>
#include <libavutil/avutil.h>

#include <MediaFormatJNI.h>
#include <KwpLogs.h>

/**
 * java에서 정의한 codec id값을 ffmpeg에서 사용되는 AVCoecID를 얻어옴
 * @param javaid	java에서 사용되는 codec id [ CFFmpegCodecID 참조 ]
 * @return	ffmpeg Codec Id
 */
AVCodecID getAVCodecID(int javaid) {
	switch (javaid) {
	default:
	case 0:		//UnKnown
		return AV_CODEC_ID_NONE;
	case 1:		//H264
		return AV_CODEC_ID_H264;
	case 100:	//MP3
		return AV_CODEC_ID_MP3;
	case 101:	//AAC
		return AV_CODEC_ID_AAC;
	}
}

/**
 * android에서 사용되는 track data와 data info를 ffmpeg에서 사용되는 packat으로 변환
 * packt에 들어가는 pts, dts값은 android에서 사용되는 origenal값이 들어가므로 외부에서 한번더 변환해줌
 * ffmpeg 함수들은 XXXjni.cpp에서 처리하는 방향으로 작성되어 이렇게 처리함
 * @param 	*env	java class 접근을 위한 객체
 * @param 	bytedata	java의 byate배열 (실제 track data)
 * @param 	bufferinfo		buffer의 offset, size, pts등을 가지고 있음
 * @param 	*pkt			ffmpeg에서 사용할 AVPacket
 */
void	setPacket(JNIEnv *env, jbyteArray bytedata, jobject bufferinfo, AVPacket *pkt){

//	Logw("Call getPacket..");
	// Buffer Info
	jclass clazz = env->FindClass("android/media/MediaCodec$BufferInfo");
	jfieldID sizeId = env->GetFieldID(clazz, "size", "I");
	jfieldID ptsId = env->GetFieldID(clazz, "presentationTimeUs", "J");
	jfieldID offsetId = env->GetFieldID(clazz, "offset", "I");

	pkt->size = env->GetIntField(bufferinfo, sizeId);
	long	pts = env->GetLongField(bufferinfo, ptsId);

	pkt->dts = pkt->pts = pts;

	int offset = env->GetIntField(bufferinfo, offsetId);
	pkt->pos = 0;

//	Logw("get field size : %d   offset: %d  dts pts : %lld  ",
//			pkt->size, offset, pkt->dts);

//	Logw("Data copy");

//	uint8_t* data = ;
	pkt->data = (uint8_t*)malloc((pkt->size)*sizeof(uint8_t));
//	memcpy(pkt->data,data,(pkt->size)*sizeof(uint8_t));

	env->GetByteArrayRegion (bytedata, 0, pkt->size, reinterpret_cast<jbyte*>(pkt->data));

//	free(data);

//	Logw("start data: %0X %0X %0X %0X %0X ",
//			pkt->data[0],
//			pkt->data[1],
//			pkt->data[2],
//			pkt->data[3],
//			pkt->data[4]);
//
//	Logw("last data: %0X %0X %0X %0X %0X ",
//			pkt->data[pkt->size -5],
//			pkt->data[pkt->size -4],
//			pkt->data[pkt->size -3],
//			pkt->data[pkt->size -2],
//			pkt->data[pkt->size -1]);

	if (pkt->size > 0)
		pkt->duration = 0;
//	else
//		pkt->duration = 33;
}

/**
 * android MediaFormat를 cpp에서 사용되도록 변경해주는 함수
 * @param	*env		  java class에 접근을 위한 객체
 * @param	mediaformat	[IN] android mediaformat을 cpp와 호환되어 사용할 수있도록 변경한 객체(MediaFormatJNI class)
 * @param	formatjni	[OUT] cpp에서 사용되는 mediaformat
 */
void	getMediaFormatJNI(JNIEnv *env, jobject mediaformat, MediaFormatJNI &formatjni)
{
	jclass clazz = env->FindClass("com/kiwiple/mediaframework/data/MediaFormatJNI");
	int value =-1;
	jmethodID	f_mediaType = env->GetMethodID(clazz, "getMediaType", "()I");
	value = env->CallIntMethod(mediaformat, f_mediaType);
	switch(value)
	{
		case 0:
			formatjni.mMediaType = AVMEDIA_TYPE_AUDIO;
			Logw("Media Type : Audio...");
			break;
		case 1:
			formatjni.mMediaType = AVMEDIA_TYPE_VIDEO;
			Logw("Media Type : Video...");
			break;
		default:
			formatjni.mMediaType = AVMEDIA_TYPE_UNKNOWN;
			Logw("Media Type : Unknown...");
			break;
	}
	jmethodID	f_codecID = env->GetMethodID(clazz, "getCodecID", "()I");
	value = env->CallIntMethod(mediaformat, f_codecID);
	formatjni.mCodecId = getAVCodecID(value);

	jmethodID	f_mime = env->GetMethodID(clazz, "getMime", "()Ljava/lang/String;");
	jstring str =  (jstring)env->CallObjectMethod(mediaformat, f_mime);
	const char* mime = env->GetStringUTFChars(str, NULL);
	if(formatjni.mMimeName != NULL)
		delete formatjni.mMimeName;

	if(mime != NULL)
	{
		formatjni.mMimeName = (char*)malloc(strlen(mime)+1);
		strcpy(formatjni.mMimeName, mime);
		Logw("Call  getMime :%s",formatjni.mMimeName);
	}
	else
	{
		Loge("Mime type is null");
	}

	jmethodID	f_maxInputSize = env->GetMethodID(clazz, "getMaxInputSize", "()I");
	formatjni.mMaxInputSize =  env->CallIntMethod(mediaformat, f_maxInputSize);

	jmethodID	f_bitrate = env->GetMethodID(clazz, "getBitrate", "()I");
	formatjni.mBitRate =  env->CallIntMethod(mediaformat, f_bitrate);

	jmethodID	f_width = env->GetMethodID(clazz, "getWidth", "()I");
	formatjni.mWidth = env->CallIntMethod(mediaformat, f_width);

	jmethodID	f_height = env->GetMethodID(clazz, "getHeight", "()I");
	formatjni.mHeight = env->CallIntMethod(mediaformat, f_height);

	jmethodID	f_colorFormat = env->GetMethodID(clazz, "getColorFormat", "()I");
	formatjni.mColorFormat = env->CallIntMethod(mediaformat, f_colorFormat);

	jmethodID	f_frameRate = env->GetMethodID(clazz, "getFrameRate", "()I");
	formatjni.mFrameRate = env->CallIntMethod(mediaformat, f_frameRate);

	jmethodID	f_frameInterval = env->GetMethodID(clazz, "getIFrameInterval", "()I");
	formatjni.mIFrameInterval = env->CallIntMethod(mediaformat, f_frameInterval);

	jmethodID	f_channel = env->GetMethodID(clazz, "getChannel", "()I");
	formatjni.mChannel = env->CallIntMethod(mediaformat, f_channel);

	jmethodID	f_sampleRate = env->GetMethodID(clazz, "getSampleRate", "()I");
	formatjni.mSampleRate = env->CallIntMethod(mediaformat, f_sampleRate);

	jmethodID	f_isAdts = env->GetMethodID(clazz, "getISADTS", "()I");
	formatjni.mIsAdts = env->CallIntMethod(mediaformat, f_isAdts);

	jmethodID	f_AACProfile = env->GetMethodID(clazz, "getAACProfile", "()I");
	formatjni.mAACProfile = env->CallIntMethod(mediaformat, f_AACProfile);

	jmethodID	f_channelMask = env->GetMethodID(clazz, "getChannelMask", "()I");
	formatjni.mChannelMask = env->CallIntMethod(mediaformat, f_channelMask);

	jmethodID	f_flacCompressionLevel = env->GetMethodID(clazz, "getFlacCompressionLevel", "()I");
	formatjni.mFlacCompressionLevel = env->CallIntMethod(mediaformat, f_flacCompressionLevel);


	// Buffer copy를 줄이기위하여 별도 함수에서 처리하도록 함
//	getExtraData

}

/**
 *	media format에서 extradata를 가져오기 위해서 제작하였으나 h264 encoder 이후에 data가 extradata에서 나오지 않아
 *	addtrack시 extra data parameter로 받아 처리 하는 방법으로 처리함
 *	즉 muxer에서 사용하지 않고 있음
 * @param	*env		java class에 접근을 위한 객체
 * @param	mediaformat	android mediaformat을 cpp에서 사용할 수있도록 변경한 객체(MediaFormatJNI class)
 * @param	size		buffer size
 */
uint8_t* 	getExtraData( JNIEnv *env, jobject mediaformat, int &size )
{
	jclass clazz = env->FindClass("com/kiwiple/mediaframework/data/MediaFormatJNI");
	jmethodID	f_ExtraData = env->GetMethodID(clazz, "getExtraData", "(I)[B");
	jmethodID	f_ExtraDataSize = env->GetMethodID(clazz, "getExtraDataSize", "()I");
	size = env->CallIntMethod(mediaformat, f_ExtraDataSize);

	uint8_t* data  = (uint8_t*)malloc(size);
	int idx = 0;
	for(int i =0 ; i < 10 ; i++)
	{
		jbyteArray array = (jbyteArray)env->CallObjectMethod(mediaformat, f_ExtraData, i);
		if(array == NULL)
			break;
		else
		{
			int len = env->GetArrayLength (array);
			env->GetByteArrayRegion (array, idx, len, reinterpret_cast<jbyte*>(data));
			idx += len;
		}
	}

	return data;

}

/**
 * cpp에서 사용되는 Track의 정보를 android에서 사용되는 mediaforamt으로 변경해주기 위한 함수
 * (demuxer에서 사용될것을 예상하여 제작하였으나 현재 muxer만 지원하고 있어 사용되지 않음
 * 사용하게 될 경우 Test 필요함)
 * @param	*env		java class에 접근을 위한 객체
 * @param	mediaformat	[OUT] android mediaformat을 cpp와 호환되어 사용할 수있도록 변경한 객체(MediaFormatJNI class)
 * @param	formatjni	[IN] cpp에서 사용되는 mediaformat
 */
void setMediaFormatJNI(JNIEnv *env, jobject mediaformat, MediaFormatJNI &formatjni) {
	jclass clazz = env->FindClass("com/kiwiple/mediaframework/data/MediaFormatJNI");
	int value =-1;
	jmethodID	f_mediaType = env->GetMethodID(clazz, "setMediaType", "(I)V");
	switch (formatjni.mMediaType) {
		case AVMEDIA_TYPE_AUDIO:
			value = 0;
			break;
		case AVMEDIA_TYPE_VIDEO:
			value = 1;
			break;
		default:
			break;
	}
	env->CallVoidMethod(mediaformat, f_mediaType, value);

	jmethodID	f_codecID = env->GetMethodID(clazz, "setCodecID", "(I)V");
	switch (formatjni.mCodecId) {
		case AV_CODEC_ID_NONE:
			value = 0;
			break;
		case AV_CODEC_ID_H264:
			value = 1;
			break;
		case AV_CODEC_ID_MP3:
			value = 100;
		case AV_CODEC_ID_AAC:
			value = 101;
		default:
			break;
	}
	env->CallVoidMethod(mediaformat, f_codecID, value);

	if(formatjni.mMimeName != NULL) {
		jmethodID	f_mime = env->GetMethodID(clazz, "setMime", "([C)V");
		env->CallVoidMethod(mediaformat, f_mime, formatjni.mMimeName);
	}

	jmethodID	f_maxInputSize = env->GetMethodID(clazz, "setMaxInputSize", "(I)V");
	env->CallVoidMethod(mediaformat, f_maxInputSize, formatjni.mMaxInputSize);

	jmethodID	f_bitrate = env->GetMethodID(clazz, "setBitrate", "(I)V");
	env->CallVoidMethod(mediaformat, f_bitrate, formatjni.mBitRate);

	jmethodID	f_width = env->GetMethodID(clazz, "setWidth", "(I)V");
	env->CallVoidMethod(mediaformat, f_width, formatjni.mWidth);

	jmethodID	f_height = env->GetMethodID(clazz, "setHeight", "(I)V");
	env->CallVoidMethod(mediaformat, f_height, formatjni.mHeight);

	jmethodID	f_colorFormat = env->GetMethodID(clazz, "setColorFormat", "(I)V");
	env->CallVoidMethod(mediaformat, f_colorFormat, formatjni.mColorFormat);

	jmethodID	f_frameRate = env->GetMethodID(clazz, "setFrameRate", "()I");
	env->CallVoidMethod(mediaformat, f_frameRate, formatjni.mFrameRate);

	jmethodID	f_frameInterval = env->GetMethodID(clazz, "setIFrameInterval", "(I)V");
	env->CallVoidMethod(mediaformat, f_frameInterval, formatjni.mIFrameInterval);

	jmethodID	f_channel = env->GetMethodID(clazz, "setChannel", "(I)V");
	env->CallVoidMethod(mediaformat, f_channel, formatjni.mChannel );

	jmethodID	f_sampleRate = env->GetMethodID(clazz, "setSampleRate", "(I)V");
	env->CallVoidMethod(mediaformat, f_sampleRate, formatjni.mSampleRate);

	jmethodID	f_isAdts = env->GetMethodID(clazz, "setISADTS", "(I)V");
	env->CallVoidMethod(mediaformat, f_isAdts, formatjni.mIsAdts);

	jmethodID	f_AACProfile = env->GetMethodID(clazz, "setAACProfile", "(I)V");
	env->CallVoidMethod(mediaformat, f_AACProfile, formatjni.mAACProfile);

	jmethodID	f_channelMask = env->GetMethodID(clazz, "setChannelMask", "(I)V");
	env->CallVoidMethod(mediaformat, f_channelMask, formatjni.mChannelMask);

	jmethodID	f_flacCompressionLevel = env->GetMethodID(clazz, "setFlacCompressionLevel", "(I)V");
	env->CallVoidMethod(mediaformat, f_flacCompressionLevel, formatjni.mFlacCompressionLevel);
}

