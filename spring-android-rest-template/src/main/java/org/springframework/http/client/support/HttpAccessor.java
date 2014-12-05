/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.http.client.support;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import android.os.Build;
import android.util.Log;

/**
 * Base class for {@link org.springframework.web.client.RestTemplate}
 * and other HTTP accessing gateway helpers, defining common properties
 * such as the {@link ClientHttpRequestFactory} to operate on.
 *
 * <p>Not intended to be used directly. See {@link org.springframework.web.client.RestTemplate}.
 * 
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @since 1.0
 * @see org.springframework.web.client.RestTemplate
 */
public abstract class HttpAccessor {

	private static final String TAG = HttpAccessor.class.getSimpleName();

	private static final boolean httpClient43Present =
			ClassUtils.isPresent("org.apache.http.impl.client.CloseableHttpClient", HttpAccessor.class.getClassLoader());

	private ClientHttpRequestFactory requestFactory;


	@SuppressWarnings("deprecation")
	protected HttpAccessor() {
		if (httpClient43Present) {
			this.requestFactory = new HttpComponentsClientHttpRequestFactory();
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			this.requestFactory = new SimpleClientHttpRequestFactory();
		}
		else {
			this.requestFactory = new org.springframework.http.client.HttpComponentsAndroidClientHttpRequestFactory();
		}
	}


	/**
	 * Set the request factory that this accessor uses for obtaining {@link ClientHttpRequest HttpRequests}.
	 */
	public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
		Assert.notNull(requestFactory, "'requestFactory' must not be null");
		this.requestFactory = requestFactory;
	}

	/**
	 * Return the request factory that this accessor uses for obtaining {@link ClientHttpRequest HttpRequests}.
	 */
	public ClientHttpRequestFactory getRequestFactory() {
		return this.requestFactory;
	}


	/**
	 * Create a new {@link ClientHttpRequest} via this template's {@link ClientHttpRequestFactory}.
	 * @param url the URL to connect to
	 * @param method the HTTP method to exectute (GET, POST, etc.)
	 * @return the created request
	 * @throws IOException in case of I/O errors
	 */
	protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
		ClientHttpRequest request = getRequestFactory().createRequest(url, method);
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Created " + method.name() + " request for \"" + url + "\"");
		}
		return request;
	}

}
