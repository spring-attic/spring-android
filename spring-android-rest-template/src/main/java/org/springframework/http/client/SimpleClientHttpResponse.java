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

package org.springframework.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * {@link ClientHttpResponse} implementation that uses standard J2SE facilities.
 * Obtained via {@link SimpleBufferingClientHttpRequest#execute()} and
 * {@link SimpleStreamingClientHttpRequest#execute()}.
 *
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @since 1.0
 */
final class SimpleClientHttpResponse extends AbstractClientHttpResponse {

	private static final String AUTH_ERROR = "Received authentication challenge is null";

	private static final String AUTH_ERROR_JELLY_BEAN = "No authentication challenges found";

	private static final String PROXY_AUTH_ERROR = "Received HTTP_PROXY_AUTH (407) code while not using proxy";

	private final HttpURLConnection connection;

	private HttpHeaders headers;


	SimpleClientHttpResponse(HttpURLConnection connection) {
		this.connection = connection;
	}


	public int getRawStatusCode() throws IOException {
		try {
		return this.connection.getResponseCode();
		} catch (IOException ex) {
			return handleIOException(ex);
		}
	}

	/**
	 * If credentials are incorrect or not provided for Basic Auth, then Android
	 * may throw this exception when an HTTP 401 is received. A separate exception
	 * is thrown for proxy authentication errors. Checking for this response and
	 * returning the proper status.
	 * @param ex the exception raised from Android
	 * @return HTTP Status Code
	 */
	private int handleIOException(IOException ex) throws IOException {
		if (AUTH_ERROR.equals(ex.getMessage()) || AUTH_ERROR_JELLY_BEAN.equals(ex.getMessage())) {
			return HttpStatus.UNAUTHORIZED.value();
		} else if (PROXY_AUTH_ERROR.equals(ex.getMessage())) {
			return HttpStatus.PROXY_AUTHENTICATION_REQUIRED.value();
		} else {
			throw ex;
		}
	}

	public String getStatusText() throws IOException {
		try {
		return this.connection.getResponseMessage();
		} catch (IOException ex) {
			return HttpStatus.valueOf(handleIOException(ex)).getReasonPhrase();
		}
	}

	public HttpHeaders getHeaders() {
		if (this.headers == null) {
			this.headers = new HttpHeaders();
			// Header field 0 is the status line for most HttpURLConnections, but not on GAE
			String name = this.connection.getHeaderFieldKey(0);
			if (StringUtils.hasLength(name)) {
				this.headers.add(name, this.connection.getHeaderField(0));
			}
			int i = 1;
			while (true) {
				name = this.connection.getHeaderFieldKey(i);
				if (!StringUtils.hasLength(name)) {
					break;
				}
				this.headers.add(name, this.connection.getHeaderField(i));
				i++;
			}
		}
		return this.headers;
	}

	@Override
	protected InputStream getBodyInternal() throws IOException {
		InputStream errorStream = this.connection.getErrorStream();
		return (errorStream != null ? errorStream : this.connection.getInputStream());
	}

	@Override
	protected void closeInternal() {
		this.connection.disconnect();
	}

}
