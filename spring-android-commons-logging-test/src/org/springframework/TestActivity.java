package org.springframework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		this.runLogTests();
	}
	
	public void runLogTests() {
		testLogging(LogFactory.getLog(TestActivity.class));
		testLogging(LogFactory.getLog("test log tag"));
	}
	  
	private void testLogging(Log log) {
	    Exception e = new Exception("just testing");
	  
	    android.util.Log.v(null, "isTraceEnabled: " + log.isTraceEnabled());
	    android.util.Log.v(null, "isDebugEnabled: " + log.isDebugEnabled());
	    android.util.Log.v(null, "isInfoEnabled: " + log.isInfoEnabled());
	    android.util.Log.v(null, "isWarnEnabled: " + log.isWarnEnabled());
	    android.util.Log.v(null, "isErrorEnabled: " + log.isErrorEnabled());
	    android.util.Log.v(null, "isFatalEnabled: " + log.isFatalEnabled());
	    
	    log.trace(null);
	    log.trace("trace message");
	    
	    log.debug(null);
	    log.debug("debug message");
	    
	    log.info(null);
	    log.info("info message");
	    
	    log.warn(null);
	    log.warn("warn message");

	    log.error(null);
	    log.error("error message");

	    log.fatal(null);
	    log.fatal("fatal message");
	    
	    log.trace(null, e);
	    log.trace("trace message", e);
	    
	    log.debug(null, e);
	    log.debug("debug message", e);
	    
	    log.info(null, e);    
	    log.info("info  message", e);
	    
	    log.warn(null, e);
	    log.warn("warn message", e);
	    
	    log.error(null, e);
	    log.error("error message", e);
	    
	    log.fatal(null, e);
	    log.fatal("fatal message", e);
	}
	
}
