APP_ABI := armeabi armeabi-v7a
APP_CPPFLAGS := -std=c++11
NDK_TOOLCHAIN_VERSION := 4.8
APP_STL := gnustl_static
APP_PLATFORM := android-12
APP_OPTIM := debug
APP_CFLAGS := \
	-D HAVE_INTTYPES_H \
	-D HAVE_PTHREAD \
	-D HAVE_UNISTD_H