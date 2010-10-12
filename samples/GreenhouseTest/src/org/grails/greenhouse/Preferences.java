package org.grails.greenhouse;

import org.grails.greenhouse.util.ErrorReporter;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Burt Beckwith
 */
public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ErrorReporter.INSTANCE.init(this);

		addPreferencesFromResource(R.xml.preferences);

//		// Get the custom preference
//		Preference customPref = findPreference("customPref");
//		customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//			public boolean onPreferenceClick(Preference preference) {
//				Toast.makeText(getBaseContext(), "The custom preference has been clicked", Toast.LENGTH_LONG).show();
//				SharedPreferences customSharedPreference = getSharedPreferences("myCustomSharedPrefs",
//						Activity.MODE_PRIVATE);
//				SharedPreferences.Editor editor = customSharedPreference.edit();
//				editor.putString("myCustomPref", "The preference has been clicked");
//				editor.commit();
//				return true;
//			}
//		});
	}
}
