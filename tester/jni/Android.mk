LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := tester
LOCAL_SRC_FILES := tester.cpp

include $(BUILD_SHARED_LIBRARY)
