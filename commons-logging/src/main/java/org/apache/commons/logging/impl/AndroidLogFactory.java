package org.apache.commons.logging.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Concrete subclass of {@link LogFactory}.
 * </p>
 * 
 * <p>
 * This factory generates instances of {@link AndroidLog}. It will remember
 * previously created instances for the same name, and will return them on
 * repeated requests to the <code>getInstance()</code> method.
 * </p>
 * 
 * <p>
 * This implementation ignores any configured attributes.
 * </p>
 * 
 * <p>
 * This implementation is largely based on the jcl-over-slf4j library
 * </p>
 * 
 * @author Roy Clarkson
 */

public class AndroidLogFactory extends LogFactory {
	
	/**
	 * The {@link org.apache.commons.logging.Log}instances that have already been
	 * created, keyed by logger name.
	 */
	private Map<String, Log> logMap;
	
	/**
	 * The name of the system property identifying our {@link Log}implementation
	 * class.
	 */
	public static final String LOG_PROPERTY = "org.apache.commons.logging.Log";

	
	/**
	 * Configuration attributes.
	 */
	protected Hashtable<String, Object> attributes = new Hashtable<String, Object>();
	
	/**
	 * Public no-arguments constructor required by the lookup mechanism.
	 */
	public AndroidLogFactory() {
		this.logMap = new HashMap<String, Log>();
	}

	/**
	 * Return the configuration attribute with the specified name (if any), or
	 * <code>null</code> if there is no such attribute.
	 * 
	 * @param name
	 *          Name of the attribute to return
	 */
	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	/**
	 * Return an array containing the names of all currently defined configuration
	 * attributes. If there are no such attributes, a zero length array is
	 * returned.
	 */
	@Override
	public String[] getAttributeNames() {
		List<String> names = new ArrayList<String>();
	    Enumeration<String> keys = attributes.keys();
	    
	    while (keys.hasMoreElements()) {
	      names.add(keys.nextElement());
	    }
	    
	    String results[] = new String[names.size()];
	    
	    for (int i = 0; i < results.length; i++) {
	      results[i] = names.get(i);
	    }
	    
	    return results;
	}

	/**
	 * Convenience method to derive a name from the specified class and call
	 * <code>getInstance(String)</code> with it.
	 * 
	 * @param clazz
	 *          Class for which a suitable Log name will be derived
	 * 
	 * @exception LogConfigurationException
	 *              if a suitable <code>Log</code> instance cannot be returned
	 */

	@Override
	public Log getInstance(Class clazz) throws LogConfigurationException {
		return (getInstance(clazz.getName()));
	}

	/**
	 * <p>
	 * Construct (if necessary) and return a <code>Log</code> instance, using
	 * the factory's current set of configuration attributes.
	 * </p>
	 * 
	 * @param name
	 *          Logical name of the <code>Log</code> instance to be returned
	 *          (the meaning of this name is only known to the underlying logging
	 *          implementation that is being wrapped)
	 * 
	 * @exception LogConfigurationException
	 *              if a suitable <code>Log</code> instance cannot be returned
	 */
	@Override
	public Log getInstance(String name) throws LogConfigurationException {
		Log log = null;

	    synchronized (logMap) {
	    	log = logMap.get(name);
	    	
	    	if (log == null) {
	    		log = new AndroidLog(name);
	    		logMap.put(name, log);
	    	}
	    }
	    
	    return log;
	}

	/**
	 * Release any internal references to previously created
	 * {@link org.apache.commons.logging.Log}instances returned by this factory.
	 * This is useful in environments like servlet containers, which implement
	 * application reloading by throwing away a ClassLoader. Dangling references
	 * to objects in that class loader would prevent garbage collection.
	 */
	@Override
	public void release() {
		// This method is never called by this library.
	    System.out.println("WARN: The method " + AndroidLogFactory.class
	        + "#release() was invoked.");
	    System.out.flush();
	}

	/**
	 * Remove any configuration attribute associated with the specified name. If
	 * there is no such attribute, no action is taken.
	 * 
	 * @param name
	 *          Name of the attribute to remove
	 */
	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	/**
	 * Set the configuration attribute with the specified name. Calling this with
	 * a <code>null</code> value is equivalent to calling
	 * <code>removeAttribute(name)</code>.
	 * 
	 * @param name
	 *          Name of the attribute to set
	 * @param value
	 *          Value of the attribute to set, or <code>null</code> to remove
	 *          any setting for this attribute
	 */
	@Override
	public void setAttribute(String name, Object value) {
		if (value == null) {
			attributes.remove(name);
		} else {
			attributes.put(name, value);
		}
	}

}
