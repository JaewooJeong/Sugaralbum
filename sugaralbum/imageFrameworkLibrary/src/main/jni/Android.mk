LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
LOCAL_MODULE    := native_filter
LOCAL_SRC_FILES := ImageFilter.c \
				   Spline.c

#�߰� ���̺귯���� ����Ұ�� �Ʒ� �ۼ� �ʿ�!
#where : NDK/platforms/<level>/arch-arm/usr/include
#math.h : m
#log.h : log
#jnigraphics : bitmap.h
LOCAL_LDLIBS	:= -llog
LOCAL_LDFLAGS += -Wl,-z,max-page-size=16384

include $(BUILD_SHARED_LIBRARY)