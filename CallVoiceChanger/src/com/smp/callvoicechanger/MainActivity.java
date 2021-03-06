/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smp.callvoicechanger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.ParseException;

import com.smp.rtp.JniTest;
import com.smp.sip.SipAudioCall;
import com.smp.sip.SipAudioManager;

/**
 * Handles all calling, receiving calls, and UI interaction in the WalkieTalkie app.
 */
public class MainActivity extends Activity implements View.OnTouchListener {

    public String sipAddress = null;

    public SipAudioManager manager = null;
    public SipProfile me = null;
    public SipAudioCall call = null;
    

    private static final int CALL_ADDRESS = 1;
    private static final int SET_AUTH_INFO = 2;
    private static final int UPDATE_SETTINGS_DIALOG = 3;
    private static final int HANG_UP = 4;
    static {
        System.loadLibrary("smp_sip_jni");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeManager();
    }

    @Override
    public void onStart() {
        super.onStart();
        // When we get back from the preference setting Activity, assume
        // settings have changed, and re-login with new auth info.
        //initializeManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.close();
        }

        //closeLocalProfile();

        
    }

    public void initializeManager() {
        if(manager == null) {
          manager = new SipAudioManager(this);
        }
        
        //initializeLocalProfile();
    }

    /**
     * Logs you into your SIP provider, registering this device as the location to
     * send SIP calls to for your SIP address.
     */
    
    /**
     * Closes out your local profile, freeing associated objects into memory
     * and unregistering your device from the server.
     */
    
    /**
     * Make an outgoing call.
     */
    public void onRegister(View view)
    {
	String username = "svenoaks140614131743";
        String domain = "phone.plivo.com";
        String password = "blahe123";
        
        SipProfile.Builder builder = null;
	try {
	    builder = new SipProfile.Builder(username, domain);
	    builder.setPassword(password);
	    me = builder.build();
	} catch (ParseException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
        try {
	    manager.open(me);
	} catch (SipException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }
    public void initiateCall() {
    	
    	
        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(false);
                    call.toggleMute();
                    //updateStatus(call);
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    //updateStatus("Ready.");
                }
            };
            SipProfile.Builder builder2 = new SipProfile.Builder("31581563481044486", "phone.plivo.com");
            //builder2.setPassword("blahe123");
            SipProfile other = builder2.build();
            call = manager.makeAudioCall(me, other, listener, 30);
        }
        catch (Exception e) {
            Log.i("WalkieTalkieActivity/InitiateCall", "Error when trying to close manager.", e);
            if (me != null) {
                try {
                    manager.close(me.getUriString());
                } catch (Exception ee) {
                    Log.i("WalkieTalkieActivity/InitiateCall",
                            "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
        
    }

    public void onClick(View view)
    {
    	new Thread(new Runnable() {

			@Override
			public void run()
			{
				JniTest test = new JniTest();
		    	test.test();	
			}
    		
    	}).start();
    	
    	//initiateCall();
    }
    /**
     * Updates the status box at the top of the UI with a messege of your choice.
     * @param status The String to display in the status box.
     */
    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
                
            }
        });
    }

    /**
     * Updates the status box with the SIP address of the current call.
     * @param call The current, active call.
     */
    public void updateStatus(SipAudioCall call) {
        String useName = call.getPeerProfile().getDisplayName();
        if(useName == null) {
          useName = call.getPeerProfile().getUserName();
        }
        updateStatus(useName + "@" + call.getPeerProfile().getSipDomain());
    }

    /**
     * Updates whether or not the user's voice is muted, depending on whether the button is pressed.
     * @param v The View where the touch event is being fired.
     * @param event The motion to act on.
     * @return boolean Returns false to indicate that the parent view should handle the touch event
     * as it normally would.
     */
    public boolean onTouch(View v, MotionEvent event) {
        if (call == null) {
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN && call != null && call.isMuted()) {
            call.toggleMute();
        } else if (event.getAction() == MotionEvent.ACTION_UP && !call.isMuted()) {
            call.toggleMute();
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, CALL_ADDRESS, 0, "Call someone");
        menu.add(0, SET_AUTH_INFO, 0, "Edit your SIP Info.");
        menu.add(0, HANG_UP, 0, "End Current Call.");

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CALL_ADDRESS:
                showDialog(CALL_ADDRESS);
                break;
            case SET_AUTH_INFO:
                updatePreferences();
                break;
            case HANG_UP:
                if(call != null) {
                    try {
                      call.endCall();
                    } catch (SipException se) {
                        Log.d("WalkieTalkieActivity/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    call.close();
                }
                break;
        }
        return true;
    }

    

    public void updatePreferences() {
        
    }
}

/*
package com.smp.callvoicechanger;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends ActionBarActivity {
    private PlaceholderFragment frag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	frag = new PlaceholderFragment();
	if (savedInstanceState == null) {
	    getSupportFragmentManager().beginTransaction()
		    .add(R.id.container, frag).commit();
	}
    }
    
    public void onClickCall(View view)
    {
	frag.initiateCall();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle action bar item clicks here. The action bar will
	// automatically handle clicks on the Home/Up button, so long
	// as you specify a parent activity in AndroidManifest.xml.
	int id = item.getItemId();
	if (id == R.id.action_settings) {
	    return true;
	}
	return super.onOptionsItemSelected(item);
    }

    
    
     
    public static class PlaceholderFragment extends Fragment {
	@Override
	    public void onDestroy() {
	        super.onDestroy();
	        if (call != null) {
	            call.close();
	        }
	        try {
		    manager.close(me.getUriString());
		} catch (SipException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	SipManager manager;
	SipAudioCall call;
	SipProfile me;
	public void initializeManager() {
	        if(manager == null) {
	          manager = SipManager.newInstance(this.getActivity());
	        }
	    }
	public PlaceholderFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
	    View rootView = inflater.inflate(R.layout.fragment_main, container,
		    false);
	    return rootView;
	}
	public void initiateCall()
	{
	    SipProfile other = null;
	    try {
	            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
	                // Much of the client's interaction with the SIP Stack will
	                // happen via listeners.  Even making an outgoing call, don't
	                // forget to set up a listener to set things up once the call is established.
	                @Override
	                public void onCallEstablished(SipAudioCall call) {
	                    call.startAudio();
	                    call.setSpeakerMode(true);
	                    call.toggleMute();
	             
	                }

	                @Override
	                public void onCallEnded(SipAudioCall call) {
	                    
	                }
	            };
	            SipProfile.Builder builder = new SipProfile.Builder("svenoaks", "smptestsip.sip.twilio.com");
	            builder.setPassword("cantTouchthis666");
	            me = builder.build();
	            SipProfile.Builder builder2 = new SipProfile.Builder("blah", "smptestsip.sip.twilio.com");
	            builder2.setPassword("mehpass");
	            other = builder2.build();
	            call = manager.makeAudioCall(me, other, listener, 30);

	        }
	        catch (Exception e) {
	            e.printStackTrace();
	            Log.i("WalkieTalkieActivity/InitiateCall", "Error when trying to close manager.", e);
	            if (me != null) {
	                try {
	                    manager.close(me.getUriString());
	                } catch (Exception ee) {
	                    Log.i("WalkieTalkieActivity/InitiateCall",
	                            "Error when trying to close manager.", ee);
	                    ee.printStackTrace();
	                }
	            }
	            if (call != null) {
	                call.close();
	            }
	        }
	    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    initializeManager();
	}
	   
	}
	
	

	
    }

*/

