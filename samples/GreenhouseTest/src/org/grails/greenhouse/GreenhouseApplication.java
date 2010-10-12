package org.grails.greenhouse;

import java.util.Date;

import android.app.Application;
import android.util.Log;

/**
 * @author Burt Beckwith
 */
public class GreenhouseApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("GreenhouseApplication", "Started at " + new Date());
	}
}
//android.os.Debug
//Debug.startMethodTracing()
//Debug.stopMethodTracing()
//Window ➤ Show View ➤ Other ➤ Android