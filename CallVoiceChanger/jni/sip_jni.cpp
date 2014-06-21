#include <jni.h>
#include <util.h>
extern "C"
{
#include "sip_ua.h"
}





void fromThread()
{
	//LOGV("hellllooooo");
}


extern "C" DLL_PUBLIC void Java_com_smp_rtp_JniTest_testJNI(JNIEnv *env, jobject thiz)
{
	make_call();
}





