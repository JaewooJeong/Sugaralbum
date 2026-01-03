#include "KwpMuxer.h"

#define TAG  "KwpMuxerJni_CPP"

#include <stdio.h>
#include <jni.h>
#include <KwpLogs.h>
#include <KwpTranceTypeManager.h>
#include <map>

using namespace std;

/**	 Java와 매칭하는 Class name 	*/
static const char *classPathName = "com/kiwiple/mediaframework/muxer/KwpMuxerJni";

/**	Muxer를 여러개 관리 하기 위한 Map 	*/
static map<long, KwpMuxer*>	mmpMuxer;

/**	Muxer를 여러개 관리 하기 위한 map Key 	*/
static long		mapkey = 0;

/**
 * key값에 해당하는 KwpMuxer instence를 가져온다.
 * @param	key		 Map에서 해당 Muxer를 가져오기위한 key 값
 * @return	key에 해당하는 muxer 객체
 */
KwpMuxer* getMuxer(long key)
{
	KwpMuxer *muxer = mmpMuxer[key];

	if(muxer == NULL)
		Loge("key %ld  Muxer is null", key);

	return muxer;
}

/**
 * KwpMuxer Create
 * @param 	*env	java class 접근을 위한 객체
 * @param	thiz	java class object
 * @return	Map에서 해당 Muxer를 가져오기위한 key 값
 */
jlong CreateMuxer(JNIEnv *env, jobject thiz) {
	Logw("CreateClassTest");
	Logw("Map size: %d" , mmpMuxer.size() );

	if(mmpMuxer.size() < 1)
		mapkey = 0;

	mmpMuxer[mapkey] = new KwpMuxer();
	mapkey ++;
	return (mapkey - 1);
}

/**
 * KwpMuxer Destory
 * @param 	*env	java class 접근을 위한 객체
 * @param	thiz	java class object
 * @return	Map에서 해당 Muxer를 가져오기위한 key 값
 */
void DestoryMuxer(JNIEnv *env, jobject thiz, jlong key) {
	Logw("Map size: %d" , mmpMuxer.size() );

	delete getMuxer(key);
	mmpMuxer.erase(key);

	Logw("by delete Map size: %d" , mmpMuxer.size() );
}

/**
 * KwpMuxre Init
 * @param 	*env	java class 접근을 위한 객체
 * @param	thiz	java class object
 * @param	key		Map에서 해당 Muxer를 가져오기위한 key 값
 * @param	patheObj	java String type의 Muxing 결과 file pull path
 * @return	MuxerInit 성공 실패 (성공 : true      실패 : false)
 */
jboolean Init(JNIEnv *env, jobject thiz, jlong key, jstring pathObj) {
	KwpMuxer *muxer = getMuxer(key );
	Logw("Init");

	const char *path = env->GetStringUTFChars(pathObj, NULL);
	if(muxer != NULL)
	{
		long ckey = key;
		muxer->init(path);
		return true;
	}
	else
	{
		Logw("muxer is null");
		return false;
	}
}


/**
 * Muxing 할 Track 추가
 * @param 	*env	java class 접근을 위한 객체
 * @param	thiz	java class object
 * @param	key		Map에서 해당 Muxer를 가져오기위한 key 값
 * @param	format	android MediaFormat
 * @param	bytedata	extra data
 * @param	size		extra data size
 * @reutrn  Muxer TrackNumber
 */
jint addTrack(JNIEnv *env, jobject thiz, jlong key, jobject format, jbyteArray bytedata, jint size) {
	Logw("Add Track");
	KwpMuxer *muxer = getMuxer(key);
	MediaFormatJNI	mediaformat;
	getMediaFormatJNI(env, format, mediaformat);

	uint8_t 	*extradata = NULL;
	if(size >0)
	{
		extradata = (uint8_t*)malloc(size*sizeof(uint8_t));
		env->GetByteArrayRegion (bytedata, 0, size, reinterpret_cast<jbyte*>(extradata));
	}

	Logw("Call Muxer AddTrack...");
	int tracknum = muxer->addTrack(mediaformat, extradata, size);
	if(mediaformat.mMimeName != NULL)
		delete mediaformat.mMimeName;

	muxer = NULL;

	Logw("============ End Add Track : %d ============", tracknum);
	return tracknum;
}

/**
 * Muxer에 Track이 추가 되면 Muxing할 준비를 하는 함수
 * @param 	*env	java class 접근을 위한 객체
 * @param	thiz	java class object
 * @param	key		Map에서 해당 Muxer를 가져오기위한 key 값
 */
void Start(JNIEnv *env, jobject thiz, jlong key)
{
	KwpMuxer *muxer = getMuxer(key);
	if(muxer != NULL){
		muxer->start();
	}
}

/**
 * Muxing이 모두 끝나면 호출 되는 함수
 * @param 	*env	java class 접근을 위한 객체
 * @param	thiz	java class object
 * @param	key		Map에서 해당 Muxer를 가져오기위한 key 값
 */
void End(JNIEnv *env, jobject thiz, jlong key)
{
	KwpMuxer *muxer = getMuxer(key);
	if(muxer != NULL){
		muxer->end();
	}

}


/**
 * 실제 Muxing 하는 작업을 하는 함수
 * @param 	*env	java class 접근을 위한 객체
 * @param	thiz	java class object
 * @param	key		Map에서 해당 Muxer를 가져오기위한 key 값
 * @param	tracknum	Track Number
 * @param	bytedata	Muxing할 Data
 * @param	bufferinfo	bytedata의 정보 (offset, size, pts 등)
 */
void Muxing(JNIEnv *env, jobject thiz, jlong key, jint tracknum, jbyteArray bytedata, jobject bufferinfo, jint flag ) {
//	Logw("Muxing..");
	KwpMuxer *muxer = getMuxer(key);

	AVPacket pkt;

	av_init_packet(&pkt);
	pkt.stream_index = tracknum;
	setPacket(env, bytedata, bufferinfo, &pkt);

	AVRational basetime = muxer->getBaseTime(tracknum);
	AVRational		bq = {1, 1000000};
	pkt.pts = pkt.dts = av_rescale_q(pkt.pts, bq, basetime);
//	Logw("Muxing..flag =%d ", flag);
	if(flag == 1){
		pkt.flags |= AV_PKT_FLAG_KEY;
	}

	muxer->Muxing(&pkt);
	free(pkt.data);
	av_free_packet(&pkt);

}

/**
 * Test용
 * @param 	*env	java class 접근을 위한 객체
 * @param	thiz	java class object
 * @param	key		Map에서 해당 Muxer를 가져오기위한 key 값
 */
void displayPath(JNIEnv *env, jobject thiz, jlong key) {
	KwpMuxer *muxer = getMuxer(key);
	Logw("jni cpp displayPath() ..");
	muxer->displayPaht();
}


/**
 * Java에서 사용되는 함수명과 해당 file에 있는 함수와 연결해주기 위한 변수
 * ex> { "Java에서 사용되는 함수명", 함수 형태, 실제 cpp 함수  }
 */
static JNINativeMethod gMethods[] = {
		{ "NativeCreateMuxer", "()J", (void*) CreateMuxer },
		{ "NativeDestoryMuxer", "(J)V", (void*) DestoryMuxer },
		{ "NativeMuxerInit", "(JLjava/lang/String;)Z", (void*) Init },
		{ "NativeAddTrack", "(JLcom/kiwiple/mediaframework/data/MediaFormatJNI;[BI)I", (void*) addTrack },
		{ "NativeMuxing", "(JI[BLandroid/media/MediaCodec$BufferInfo;I)V", (void*) Muxing},
		{ "NativeMuxerStart", "(J)V", (void*) Start },
		{ "NativeMuxerEnd", "(J)V", (void*) End},
		{"NativeDisplayPath", "(J)V", (void*)displayPath}
};


// 아래 함수들은 JNI를 사용하기 위한 기본 함수 내용들임.

static int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods, int numMethods) {
	jclass clazz;
	clazz = env->FindClass(classPathName);
	if (clazz == NULL) {
		return JNI_FALSE;
	}
	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

static int registerNatives(JNIEnv* env) {
	if (!registerNativeMethods(env, classPathName, gMethods, sizeof(gMethods) / sizeof(gMethods[0]))) {
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

typedef union {
	JNIEnv* env;
	void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	Logd("JNI_OnLoad()....");
	UnionJNIEnvToVoid uenv;
	uenv.venv = NULL;
	jint result = -1;
	JNIEnv* env = NULL;

	if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
		goto bail;
	}
	env = uenv.env;

	if (registerNatives(env) != JNI_TRUE) {
		goto bail;
	}
	result = JNI_VERSION_1_4;
	bail: return result;
}
