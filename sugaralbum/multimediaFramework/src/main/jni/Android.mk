LOCAL_PATH := $(call my-dir)
rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

# libYUV
include $(CLEAR_VARS)

LOCAL_MODULE := libYUV
LOCAL_CPPFLAGS := -std=c++11 -O3

LIBYUV_SOURCE_PATH := $(LOCAL_PATH)/libyuv/source
LIBYUV_INCLUDE_PATH := $(LOCAL_PATH)/libyuv/include

LOCAL_SRC_FILES := $(wildcard $(LIBYUV_SOURCE_PATH)/*.cc)
LOCAL_SRC_FILES := $(filter-out $(wildcard $(LIBYUV_SOURCE_PATH)/*neon*.cc),$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out $(wildcard $(LIBYUV_SOURCE_PATH)/*mips.cc),$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(filter-out $(wildcard $(LIBYUV_SOURCE_PATH)/*win.cc),$(LOCAL_SRC_FILES))

# ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
	LOCAL_CFLAGS += -DLIBYUV_NEON -Werror=shorten-64-to-32 -Wdeprecated-declarations 
	LOCAL_SRC_FILES += $(addsuffix .neon,$(wildcard $(LIBYUV_SOURCE_PATH)/*neon*.cc))
# endif

LOCAL_C_INCLUDES := $(LIBYUV_INCLUDE_PATH)
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

include $(BUILD_STATIC_LIBRARY)


# MASP
include $(CLEAR_VARS)

LOCAL_MODULE := libMasp
LOCAL_CPPFLAGS := -std=c++11 -O3
LOCAL_CPP_FEATURES := exceptions

LOCAL_SRC_FILES := 	$(call rwildcard, $(LOCAL_PATH)/masp, *.c) \
					$(call rwildcard, $(LOCAL_PATH)/masp, *.cpp)
LOCAL_C_INCLUDES := $(shell find $(LOCAL_PATH)/armadillo -type d) \
					$(shell find $(LOCAL_PATH)/masp -type d) \
					$(LOCAL_PATH)/utils
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

include $(BUILD_STATIC_LIBRARY)


# PixelCanvas
include $(CLEAR_VARS)

LOCAL_MODULE := PixelCanvas
LOCAL_CPPFLAGS := -std=c++11 -O3
LOCAL_LDLIBS := -llog

LOCAL_SRC_FILES := $(call rwildcard, $(LOCAL_PATH)/canvas, *.cpp)
LOCAL_C_INCLUDES := $(LOCAL_PATH)/canvas \
					$(LOCAL_PATH)/utils

include $(BUILD_SHARED_LIBRARY)


# PixelUtils
include $(CLEAR_VARS)

LOCAL_MODULE := PixelUtils
LOCAL_CPPFLAGS := -std=c++11 -O3
LOCAL_LDLIBS := -llog

LOCAL_SHARED_LIBRARIES := libYUV PixelCanvas
LOCAL_SRC_FILES := PixelUtils.cpp
LOCAL_C_INCLUDES := $(LOCAL_PATH) \
					$(LOCAL_PATH)/canvas

include $(BUILD_SHARED_LIBRARY)


# BeatTracker
include $(CLEAR_VARS)

LOCAL_MODULE := BeatTracker
LOCAL_CPPFLAGS := -std=c++11 -O3
LOCAL_LDLIBS := -latomic -llog

LOCAL_SHARED_LIBRARIES := libMasp
LOCAL_SRC_FILES := BeatTracker.cpp

include $(BUILD_SHARED_LIBRARY)