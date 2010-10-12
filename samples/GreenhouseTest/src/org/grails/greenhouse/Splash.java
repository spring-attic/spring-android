package org.grails.greenhouse;

import org.grails.greenhouse.util.ErrorReporter;
import org.grails.greenhouse.util.Prefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @author Burt Beckwith
 */
public class Splash extends Activity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		ErrorReporter.INSTANCE.init(this);

		findViewById(R.id.buttonSignin).setOnClickListener(new OnClickListener() {
			public void onClick(final View view) {
				boolean loggedIn = Prefs.isLoggedIn(getSharedPreferences(Prefs.PREFS, Context.MODE_PRIVATE));
				startActivity(new Intent(Splash.this, loggedIn ? Profile.class : OAuth.class));
				finish();
			}
		});
	}
}
