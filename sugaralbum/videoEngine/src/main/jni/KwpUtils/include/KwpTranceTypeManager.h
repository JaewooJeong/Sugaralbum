#ifndef _CLASS_KWP_CODEC_TRANCE_TYPE_h
#define _CLASS_KWP_CODEC_TRANCE_TYPE_h

#include <jni.h>
#include <MediaFormatJNI.h>
#include <libavformat/avformat.h>

/**
 * java에서 정의한 codec id값을 ffmpeg에서 사용되는 AVCoecID를 얻어옴
 * @param javaid	java에서 사용되는 codec id [ CFFmpegCodecID 참조 ]
 * @return	ffmpeg Codec Id
 */
AVCodecID	getAVCodecID(int javaid);

/**
 * android에서 사용되는 track data와 data info를 ffmpeg에서 사용되는 packat으로 변환
 * packt에 들어가는 pts, dts값은 android에서 사용되는 origenal값이 들어가므로 외부에서 한번더 변환해줌
 * ffmpeg 함수들은 XXXjni.cpp에서 처리하는 방향으로 작성되어 이렇게 처리함
 * @param 	*env	java class 접근을 위한 객체
 * @param 	bytedata	java의 byate배열 (실제 track data)
 * @param 	bufferinfo		buffer의 offset, size, pts등을 가지고 있음
 * @param 	*pkt			ffmpeg에서 사용할 AVPacket
 */
void	setPacket(JNIEnv *env, jbyteArray bytedata, jobject bufferinfo, AVPacket *pkt);

/**
 * android MediaFormat를 cpp에서 사용되도록 변경해주는 함수
 * @param	*env		  java class에 접근을 위한 객체
 * @param	mediaformat	[IN] android mediaformat을 cpp와 호환되어 사용할 수있도록 변경한 객체(MediaFormatJNI class)
 * @param	formatjni	[OUT] cpp에서 사용되는 mediaformat
 */
void	getMediaFormatJNI(JNIEnv *env, jobject mediaformat, MediaFormatJNI &formatjni);

/**
 * cpp에서 사용되는 Track의 정보를 android에서 사용되는 mediaforamt으로 변경해주기 위한 함수
 * (demuxer에서 사용될것을 예상하여 제작하였으나 현재 muxer만 지원하고 있어 사용되지 않음
 * 사용하게 될 경우 Test 필요함)
 * @param	*env		java class에 접근을 위한 객체
 * @param	mediaformat	[OUT] android mediaformat을 cpp와 호환되어 사용할 수있도록 변경한 객체(MediaFormatJNI class)
 * @param	formatjni	[IN] cpp에서 사용되는 mediaformat
 */
void	setMediaFormatJNI(JNIEnv *env, jobject mediaformat, MediaFormatJNI &formatjni);

#endif
