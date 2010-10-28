/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.commons.logging.impl;

import java.io.Serializable;

import android.util.Log;

/**
 * Implementation of {@link Log org.apache.commons.logging.Log} interface which
 * delegates all processing to {@link Log android.util.Log}
 * instance.
 * <p>
 * The logging levels specified for Commons Logging can be almost directly mapped to
 * the levels that exist in the Google Android platform. The following table
 * shows the mapping implemented by this logger.
 * <p>
 * <table border="1">
 * 	<tr><th><b>JCL<b></th><th><b>Android</b></th></tr>
 *  <tr><td>TRACE</td><td>VERBOSE</td></tr>
 * 	<tr><td>DEBUG</td><td>DEBUG</td></tr> 
 * 	<tr><td>INFO</td><td>INFO</td></tr>
 * 	<tr><td>WARN</td><td>WARN</td></tr>
 * 	<tr><td>ERROR</td><td>ERROR</td></tr>
 *  <tr><td>FATAL</td><td>ERROR</td></tr>
 * </table>
 *  
 * @author Roy Clarkson
 */
public class AndroidLog implements org.apache.commons.logging.Log, Serializable {

	private static final long serialVersionUID = -6608881763908916154L;
	
	protected String name;

	public AndroidLog() {
		
	}
  
	public AndroidLog(String name) {
		this.name = name;
	}
	
	/**
	 * Delegates to the <code>isLoggable<code> method of the  
	 * <code>android.util.Log</code>.
	 */
	@Override
	public boolean isTraceEnabled() {
		return Log.isLoggable(name, Log.VERBOSE);
	}

	/**
	 * Converts the input parameter to String and then delegates to the 
	 * <code>v</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 */
	@Override
	public void trace(Object message) {
		Log.v(name, String.valueOf(message));
	}

	/**
	 * Converts the first input parameter to String and then delegates to the
	 * <code>v</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 * @param t
	 *          the exception to log
	 */
	@Override
	public void trace(Object message, Throwable t) {
		Log.v(name, String.valueOf(message), t);
	}

	/**
	 * Delegates to the <code>isLoggable<code> method of   
	 * <code>android.util.Log</code>.
	 */
	@Override
	public boolean isDebugEnabled() {
		return Log.isLoggable(name, Log.DEBUG);
	}
	
	/**
	 * Converts the input parameter to String and then delegates to the 
	 * <code>d</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 */
	@Override
	public void debug(Object message) {
		Log.d(name, String.valueOf(message));
	}

	/**
	 * Converts the first input parameter to String and then delegates to the 
	 * <code>d</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 * @param t
	 *          the exception to log
	 */
	@Override
	public void debug(Object message, Throwable t) {
		Log.d(name, String.valueOf(message), t);
	}

	/**
	 * Delegates to the <code>isLoggable<code> method of the  
	 * <code>android.util.Log</code>.
	 */
	@Override
	public boolean isInfoEnabled() {
		return Log.isLoggable(name, Log.INFO);
	}

	/**
	 * Converts the input parameter to String and then delegates to the 
	 * <code>i</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 */
	@Override
	public void info(Object message) {
		Log.i(name, String.valueOf(message));
	}

	/**
	 * Converts the first input parameter to String and then delegates to the 
	 * <code>i</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 * @param t
	 *          the exception to log
	 */
	@Override
	public void info(Object message, Throwable t) {
		Log.i(name, String.valueOf(message), t);
	}

	/**
	 * Delegates to the <code>isLoggable<code> method of the  
	 * <code>android.util.Log</code>.
	 */
	@Override
	public boolean isWarnEnabled() {
		return Log.isLoggable(name, Log.WARN);
	}

	/**
	 * Converts the input parameter to String and then delegates to the 
	 * <code>w</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 */
	@Override
	public void warn(Object message) {
		Log.w(name, String.valueOf(message));
	}

	/**
	 * Converts the first input parameter to String and then delegates to the 
	 * <code>w</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 * @param t
	 *          the exception to log
	 */
	@Override
	public void warn(Object message, Throwable t) {
		Log.w(name, String.valueOf(message), t);
	}
			
	/**
	 * Delegates to the <code>isLoggable<code> method of   
	 * <code>android.util.Log</code>.
	 */
	@Override
	public boolean isErrorEnabled() {
		return Log.isLoggable(name, Log.ERROR);
	}
	
	/**
	 * Converts the input parameter to String and then delegates to the 
	 * <code>e</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 */
	@Override
	public void error(Object message) {
		Log.e(name, String.valueOf(message));
	}

	/**
	 * Converts the first input parameter to String and then delegates to the 
	 * <code>e</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 * @param t
	 *          the exception to log
	 */
	@Override
	public void error(Object message, Throwable t) {
		Log.e(name, String.valueOf(message), t);
	}
	
	/**
	 * Delegates to the <code>isLoggable<code> method of   
	 * <code>android.util.Log</code>.
	 */
	@Override
	public boolean isFatalEnabled() {
		return Log.isLoggable(name, Log.ERROR);
	}

	/**
	 * Converts the input parameter to String and then delegates to the 
	 * <code>e</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 */
	@Override
	public void fatal(Object message) {
		Log.e(name, String.valueOf(message));
	}

	/**
	 * Converts the first input parameter to String and then delegates to the 
	 * <code>e</code> method of <code>android.util.Log</code>.
	 * 
	 * @param message
	 *          the message to log. Converted to {@link String}
	 * @param t
	 *          the exception to log
	 */
	@Override
	public void fatal(Object message, Throwable t) {
		Log.e(name, String.valueOf(message), t);
	}
}