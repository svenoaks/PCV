LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libre
LOCAL_SRC_FILES := re/libre.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/re/include
LOCAL_STATIC_LIBRARIES := libssl libcrypto
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libssl
LOCAL_SRC_FILES := openssl/libssl.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/openssl/include/openssl
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libcrypto
LOCAL_SRC_FILES := openssl/libcrypto.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/openssl/include/openssl
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := librem
LOCAL_SRC_FILES := rem/librem.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/rem/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libsip
LOCAL_SRC_FILES := \
	sip/sip_ua.c \
	sip/alog.c \
	sip/log.c \
	sip/audio.c \
	sip/call.c \
	sip/stream.c \
	sip/realtime.c \
	sip/metric.c \
	sip/rtpkeep.c \
	sip/sdp.c \
	sip/aufilt.c \
	sip/auplay.c \
	sip/ausrc.c \
	sip/ua.c
	
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/sip/include
LOCAL_C_INCLUDES := $(LOCAL_PATH)/sip/include
LOCAL_STATIC_LIBRARIES := libre librem
include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := smp_sip_jni
LOCAL_SRC_FILES :=  \
	sip_jni.cpp \
    
LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_STATIC_LIBRARIES := libsip
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)


