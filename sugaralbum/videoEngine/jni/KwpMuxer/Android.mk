LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := KwpFFmpegMuxer

LOCAL_SRC_FILES :=	KwpMuxer.cpp \
					KwpMuxerJni.cpp \
					../KwpUtils/src/KwpTranceTypeManager.cpp
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../KwpUtils/include \
					$(LOCAL_PATH)/../FFmpeg

LOCAL_LDLIBS := -ljnigraphics -lz -llog -landroid -ldl 
LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil

LOCAL_CPPFLAGS := -std=c++11 -D__STDC_FORMAT_MACROS
LOCAL_CFLAGS := -Wreserved-user-defined-literal -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS

include $(BUILD_SHARED_LIBRARY)
$(call import-module, android/cpufeatures)