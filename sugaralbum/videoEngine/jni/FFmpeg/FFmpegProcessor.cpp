#include "FFmpegProcessor.h"

extern "C" {
#include "ffmpeg.h"
}

JNIEnv *jniEnv;
jobject listener;

JNIEXPORT jboolean JNICALL Java_com_kiwiple_mediaframework_ffmpeg_FFmpegProcessor_nativeExecute
(JNIEnv *env, jclass jobj, jobjectArray args, jobject listener)
{
	::jniEnv = env;
	::listener = listener;

	int i = 0;
	int argc = 0;
	char **argv = NULL;
	jstring *strr = NULL;

	if (args != NULL) {
		argc = env->GetArrayLength(args);
		argv = (char **) malloc(sizeof(char *) * argc);
		strr = (jstring *) malloc(sizeof(jstring) * argc);

		for (i = 0; i < argc; ++i) {
			strr[i] = (jstring) env->GetObjectArrayElement(args, i);
			argv[i] = (char *) env->GetStringUTFChars(strr[i], 0);
		}
	}

	int result = main(argc, argv);

	for (i = 0; i < argc; ++i) {
		env->ReleaseStringUTFChars(strr[i], argv[i]);
	}
	free(argv);
	free(strr);

	return result == 0;
}

void onExit(int state)
{
	jclass clazz = jniEnv->GetObjectClass(listener);
	jmethodID method = jniEnv->GetMethodID(clazz, state ? "onError" : "onCompletion", "()V");
	jniEnv->CallVoidMethod(listener, method);
}
