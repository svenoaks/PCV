package com.smp.sip;

import com.smp.sip.SipAudioCall.Listener;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipSession;
import android.os.RemoteException;

public class SipAudioManager
{
	private Context context;
	private SipManager manager;
	public SipAudioManager(Context context)
	{
		manager = SipManager.newInstance(context);
		this.context = context;
	}
	
	public void open(SipProfile localProfile) throws SipException
	{
		manager.open(localProfile);
	}

	public SipAudioCall makeAudioCall(SipProfile localProfile, SipProfile peerProfile,
			Listener listener, int timeout) throws SipException
	{
		//return manager.makeAudioCall(localProfile, peerProfile, listener, timeout);
		
		if (!isVoipSupported(context)) {
            throw new SipException("VOIP API is not supported");
        }
        SipAudioCall call = new SipAudioCall(context, localProfile);
        call.setListener(listener);
        SipSession s = manager.createSipSession(localProfile, null);
        call.makeCall(peerProfile, s, timeout);
        return call;
        
	}

	public void close(String uriString) throws SipException
	{
		manager.close(uriString);
	}
	
	public static boolean isVoipSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_SIP_VOIP) && isApiSupported(context);
    }
	public static boolean isApiSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_SIP);
    }
	
}
