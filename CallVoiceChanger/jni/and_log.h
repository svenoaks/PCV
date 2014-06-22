#include <android/log.h>

#define ALOG_TAG "re_alog"

#define  RE_LOGV(...)  re_alog_write((int) ANDROID_LOG_VERBOSE, ALOG_TAG, __VA_ARGS__)
#define  RE_LOGE(...)  re_alog_write((int) ANDROID_LOG_ERROR, ALOG_TAG, __VA_ARGS__)
#define  RE_LOGW(...)  re_alog_write((int) ANDROID_LOG_WARN, ALOG_TAG, __VA_ARGS__)
#define  RE_LOGD(...)  re_alog_write((int) ANDROID_LOG_DEBUG, ALOG_TAG, __VA_ARGS__)
#define  RE_LOGI(...)  re_alog_write((int) ANDROID_LOG_INFO, ALOG_TAG, __VA_ARGS__)


