LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

NDK_TOOLCHAIN_VERSION := clang
APP_CPPFLAGS += -std=c++11
LOCAL_MODULE    := smp_rtp_jni
LOCAL_SRC_FILES :=  \
	rtp_jni.cpp \
	jrtplib-3.9.1/rtcpapppacket.cpp \
	jrtplib-3.9.1/rtcpbyepacket.cpp \
	jrtplib-3.9.1/rtcpcompoundpacket.cpp \
	jrtplib-3.9.1/rtcpcompoundpacketbuilder.cpp \
	jrtplib-3.9.1/rtcppacket.cpp \
	jrtplib-3.9.1/rtcppacketbuilder.cpp \
	jrtplib-3.9.1/rtcprrpacket.cpp \
	jrtplib-3.9.1/rtcpscheduler.cpp \
	jrtplib-3.9.1/rtcpsdesinfo.cpp \
	jrtplib-3.9.1/rtcpsdespacket.cpp \
	jrtplib-3.9.1/rtcpsrpacket.cpp \
	jrtplib-3.9.1/rtpcollisionlist.cpp \
	jrtplib-3.9.1/rtpdebug.cpp \
	jrtplib-3.9.1/rtperrors.cpp \
	jrtplib-3.9.1/rtpinternalsourcedata.cpp \
	jrtplib-3.9.1/rtpipv4address.cpp \
	jrtplib-3.9.1/rtpipv6address.cpp \
	jrtplib-3.9.1/rtplibraryversion.cpp \
	jrtplib-3.9.1/rtppacket.cpp \
	jrtplib-3.9.1/rtppacketbuilder.cpp \
	jrtplib-3.9.1/rtppollthread.cpp \
	jrtplib-3.9.1/rtprandom.cpp \
	jrtplib-3.9.1/rtprandomrand48.cpp \
	jrtplib-3.9.1/rtprandomrands.cpp \
	jrtplib-3.9.1/rtprandomurandom.cpp \
	jrtplib-3.9.1/rtpsession.cpp \
	jrtplib-3.9.1/rtpsessionparams.cpp \
	jrtplib-3.9.1/rtpsessionsources.cpp \
	jrtplib-3.9.1/rtpsourcedata.cpp \
	jrtplib-3.9.1/rtpsources.cpp \
	jrtplib-3.9.1/rtptimeutilities.cpp \
	jrtplib-3.9.1/rtpudpv4transmitter.cpp \
	jrtplib-3.9.1/rtpudpv6transmitter.cpp \
	jrtplib-3.9.1/rtpbyteaddress.cpp \
	jrtplib-3.9.1/rtpexternaltransmitter.cpp
	
LOCAL_C_INCLUDES := $(LOCAL_PATH)/jrtplib-3.9.1/
LOCAL_C_INCLUDES += $(LOCAL_PATH)


include $(BUILD_SHARED_LIBRARY)
