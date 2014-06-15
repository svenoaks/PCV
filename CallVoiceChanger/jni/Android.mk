LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

NDK_TOOLCHAIN_VERSION := clang
APP_CPPFLAGS += -std=c++11
LOCAL_MODULE    := smp_rtp_jni
LOCAL_SRC_FILES :=  rtp_jni.cpp \
					AmrCodec.cpp
LOCAL_C_INCLUDES := $(LOCAL_PATH)


include $(BUILD_SHARED_LIBRARY)