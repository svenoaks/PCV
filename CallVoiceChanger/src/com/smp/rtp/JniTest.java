package com.smp.rtp;

public class JniTest
{
	static
	{
		System.loadLibrary("smp_sip_jni");
	}
	private native void testJNI();
	
	public void test()
	{
		testJNI();
	}
	
	
}
