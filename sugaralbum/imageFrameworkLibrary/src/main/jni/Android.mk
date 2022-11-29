LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
LOCAL_MODULE    := native_filter
LOCAL_SRC_FILES := ImageFilter.c \
				   Spline.c

#추가 라이브러리를 사용할경우 아래 작성 필요!
#where : NDK/platforms/<level>/arch-arm/usr/include
#math.h : m
#log.h : log
#jnigraphics : bitmap.h
LOCAL_LDLIBS	:= -llog

include $(BUILD_SHARED_LIBRARY)