#ifndef VIDEO_ENGINE_LOGNDK
#define VIDEO_ENGINE_LOGNDK

#include <android/log.h>

#define VIDEO_ENGINE_LOG_TAG "kiwi_ndk"
#define LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE, VIDEO_ENGINE_LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, VIDEO_ENGINE_LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, VIDEO_ENGINE_LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN, VIDEO_ENGINE_LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, VIDEO_ENGINE_LOG_TAG, __VA_ARGS__)

#endif /* VIDEO_ENGINE_LOGNDK */
