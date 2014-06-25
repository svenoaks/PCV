#ifdef __ANDROID__
#include <android/log.h>

#define LOG_TAG "re_log"

int log_write(int, const char *, const char *, ...);

#define  RE_LOGV(...)  log_write((int) ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define  RE_LOGE(...)  log_write((int) ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  RE_LOGW(...)  log_write((int) ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define  RE_LOGD(...)  log_write((int) ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  RE_LOGI(...)  log_write((int) ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  RE_LOGWTF(...)  log_write((int) ANDROID_LOG_WTF, LOG_TAG, __VA_ARGS__)

#endif

