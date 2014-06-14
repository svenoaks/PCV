/*
 * This file is part of TinySip. 
 * http://code.google.com/p/de-tiny-sip/
 * 
 * Created 2011 by Sebastian Rösch <flowfire@sebastianroesch.de>
 * 
 * This software is licensed under the Apache License 2.0.
 */

package de.tinysip.sipdemo;

/**
 * The SettingsProvider holds all your SIP details. Change these to suit your SIP provider's details.
 * 
 * @author Sebastian
 * 
 */
public class SettingsProvider {
	/**
	 * The SIP user name.
	 */
	public static String sipUserName = "svenoaks";
	/**
	 * The SIP domain.
	 */
	public static String sipDomain = "smptestsip.sip.twilio.com";
	/**
	 * The SIP password.
	 */
	public static String sipPassword = "cantTouchthis666";
	/**
	 * The SIP server port.
	 */
	public static int sipPort = 5060;
	/**
	 * The local users display name.
	 */
	public static String displayName = "Steve";

	/**
	 * The SIP contact to call.
	 */
	public static String callContact = "svenoaks";
	/**
	 * The SIP contact's domain.
	 */
	public static String callDomain = "smptestsip.sip.twilio.com";
}
