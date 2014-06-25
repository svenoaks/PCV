#include <jni.h>
#include <util.h>
extern "C"
{
#include <re.h>
#include <baresip.h>
}





void fromThread()
{
	//LOGV("hellllooooo");
}


extern "C" DLL_PUBLIC void Java_com_smp_rtp_JniTest_testJNI(JNIEnv *env, jobject thiz)
{
	int err;

	err = libre_init();
		if (err)
			goto out;

	err = ua_init("baresip v" BARESIP_VERSION " (" ARCH "/" OS ")",
				 true, true, true, true);

	RE_LOGI("ERROR: %d", err);
	if (err)
		 	goto out;


	out:
	;
}





