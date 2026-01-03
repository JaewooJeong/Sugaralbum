#ifndef _CLASS_KWP_LOG_h
#define _CLASS_KWP_LOG_h

#ifndef TAG
#define TAG  "LOGTAG"
#endif

#include <jni.h>
#include <android/log.h>
#define Logi(...) ((void)__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__))
#define Logd(...) ((void)__android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__))
#define Logw(...) ((void)__android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__))
#define Loge(...) ((void)__android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__))
#define Logf(...) ((void)__android_log_print(ANDROID_LOG_FATAL,TAG,__VA_ARGS__))

#endif
