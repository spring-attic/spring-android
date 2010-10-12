package org.grails.greenhouse.util;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

/**
 * @author Burt Beckwith
 */
public class Prefs {

	private Prefs() {
		// static only
	}

	private static final String TAG = "Prefs";

	public static final String USER_TOKEN = "user_token";
	public static final String USER_SECRET = "user_secret";
	public static final String REQUEST_TOKEN = "request_token";
	public static final String REQUEST_SECRET = "request_secret";

	public static final String PREFS = "GreenhousePreferences";

	private static final String CALLBACK_URI_STRING = "x-com-springsource-greenhouse://oauth-response";

	public static void saveRequestInformation(final SharedPreferences settings, final String token, final String secret) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(REQUEST_TOKEN, token);
		editor.putString(REQUEST_SECRET, secret);
		debug("Saving Request Token: " + token + " and Secret: " + secret);
		editor.commit();
	}

	public static void resetRequestInformation(final SharedPreferences settings) {
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(REQUEST_TOKEN);
		editor.remove(REQUEST_SECRET);
		debug("Clearing Request Token and Secret");
		editor.commit();
	}

	public static void resetAuthInformation(final SharedPreferences settings) {
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(USER_TOKEN);
		editor.remove(USER_SECRET);
		debug("Clearing OAuth Token and Secret");
		editor.commit();
	}

	public static void saveAuthInformation(final SharedPreferences settings, final String token, final String secret) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(USER_TOKEN, token);
		editor.putString(USER_SECRET, secret);
		debug("Saving OAuth Token: " + token + " and Secret: " + secret);
		editor.commit();
	}

	public static String[] getRequestTokenAndSecret(final SharedPreferences settings) {
		 String token = settings.getString(Prefs.REQUEST_TOKEN, null);
		 String secret = settings.getString(Prefs.REQUEST_SECRET, null);
		 return new String[] { token, secret };
	}

	public static String[] getUserTokenAndSecret(final SharedPreferences settings) {
		String token = null;
		String tokenSecret = null;
		if (settings.contains(USER_TOKEN) && settings.contains(USER_SECRET)) {
			token = settings.getString(USER_TOKEN, null);
			tokenSecret = settings.getString(USER_SECRET, null);
		}
		return new String[] { token, tokenSecret };
	}

	public static boolean isLoggedIn(final SharedPreferences settings) {
		String[] tokenAndSecret = getUserTokenAndSecret(settings);
		return tokenAndSecret[0] != null && tokenAndSecret[1] != null;
	}

	public static String getConsumerKey() {
//		return "QLM4vX68dBRLG1sE7zSJiA"; // twitter
		return "a08318eb478a1ee31f69a55276f3af64"; // greenhouse
	}

	public static String getConsumerSecret() {
//		return "4Ah41ojlwi249GCI1UU5oKolmNDpjLLXgPKwGfVs0"; // twitter
		return "80e7f8f7ba724aae9103f297e5fb9bdf"; // greenhouse
	}

	public static String getRequestTokenUrl() {
		return getUrlBase() + "/oauth/request_token";
	}

	public static String getAccessTokenUrl() {
		return getUrlBase() + "/oauth/access_token";
	}

	public static String getAuthorizeUrl() {
//		return getUrlBase() + "/oauth/authorize"; // twitter
		return getUrlBase() + "/oauth/confirm_access";
	}

	private static String getUrlBase() {
//		return "http://twitter.com";
//		return "http://192.168.0.4:9090/grailshouse";
//		return "http://192.168.0.4:8080/greenhouse";
		return "http://192.168.0.8:8080/grailshouse";
	}

	public static Uri getCallbackUri() {
		return Uri.parse(CALLBACK_URI_STRING);
	}

	public static int getSocketBufferSize() {
		return 8192;
	}

	public static boolean isTcpNoDelay() {
		return true;
	}

	public static int getPort() {
		try {
			return new URL(getUrlBase()).getPort();
		}
		catch (MalformedURLException e) {
			error(e);
			return 80;
		}
	}

	public static String getProfileUrl() {
//		return getUrlBase() + "/account/verify_credentials.json";
		return getUrlBase() + "/members/@self";
	}

//	public static String getProfileContentCharset() {
//		return HTTP.DEFAULT_CONTENT_CHARSET;
//	}

	public static String getScheme() {
		try {
			return new URL(getUrlBase()).getProtocol();
		}
		catch (MalformedURLException e) {
			error(e);
			return "http";
		}
	}

	private static void debug(final String message) {
		Log.d(TAG, message);
	}

	private static void error(final Throwable t) {
		Log.e(TAG, t.getMessage(), t);
	}
}
