LOCAL_PATH := $(call my-dir)

### Prebuilt Library
include $(CLEAR_VARS)
LOCAL_MODULE:= libavcodec
LOCAL_SRC_FILES:= prebuilt-lib/$(TARGET_ARCH_ABI)/lib/libavcodec-56.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libavformat
LOCAL_SRC_FILES:= prebuilt-lib/$(TARGET_ARCH_ABI)/lib/libavformat-56.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libswscale
LOCAL_SRC_FILES:= prebuilt-lib/$(TARGET_ARCH_ABI)/lib/libswscale-3.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libavutil
LOCAL_SRC_FILES:= prebuilt-lib/$(TARGET_ARCH_ABI)/lib/libavutil-54.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libavfilter
LOCAL_SRC_FILES:= prebuilt-lib/$(TARGET_ARCH_ABI)/lib/libavfilter-5.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libswresample
LOCAL_SRC_FILES:= prebuilt-lib/$(TARGET_ARCH_ABI)/lib/libswresample-1.so
include $(PREBUILT_SHARED_LIBRARY)


### FFmpegProcessor
include $(CLEAR_VARS)

LOCAL_MODULE    := FFmpegProcessor
LOCAL_CFLAGS := -Wdeprecated-declarations -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS
LOCAL_LDLIBS := -llog
LOCAL_ARM_MODE := arm

LOCAL_SHARED_LIBRARIES := libavformat libavfilter libavcodec libavutil libswresample libswscale
LOCAL_SRC_FILES :=	FFmpegProcessor.cpp \
					ffmpeg.c \
					ffmpeg_filter.c \
					ffmpeg_opt.c \
					cmdutils.c
LOCAL_C_INCLUDES := $(LOCAL_PATH) \
					$(LOCAL_PATH)/../Utils

include $(BUILD_SHARED_LIBRARY)