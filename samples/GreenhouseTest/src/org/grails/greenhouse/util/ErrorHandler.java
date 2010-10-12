package org.grails.greenhouse.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

/**
 * @author Burt Beckwith
 */
public class ErrorHandler {

	private ErrorHandler() {
		// static only
	}

	private static final OnClickListener NO_OP_LISTENER = new OnClickListener() {
		public void onClick(final DialogInterface di, int buttonId) { /* do nothing */ }
	};

	public static void handle(final Throwable t, final Activity activity) {
//Prefs.loggedIn = false;
//Prefs.resetUserTokenAndSecret(activity);

		Log.e("ErrorHandler", t.getMessage(), t);

		Writer result = new StringWriter();
		t.printStackTrace(new PrintWriter(result));

//		new AlertDialog.Builder(activity)
//			.setTitle("Exception: " + t.getMessage())
//			.setMessage(result.toString())
//			.setPositiveButton("OK", NO_OP_LISTENER)
//			.create()
//			.show();
	}
}
