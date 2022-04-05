/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.client;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.Configurable;
import org.apache.http.client.methods.HttpDeleteHC4;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.client.methods.HttpHeadHC4;
import org.apache.http.client.methods.HttpOptionsHC4;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.client.methods.HttpPutHC4;
import org.apache.http.client.methods.HttpTraceHC4;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.http.client.ClientHttpRequestFactory} implementation that
 * uses <a href="https://hc.apache.org/httpcomponents-client-4.3.x/android-port.html">Apache
 * HttpComponents Android HttpClient</a> to create requests.
 *
 * <p>Allows to use a pre-configured {@link HttpClient} instance - potentially with
 * authentication, HTTP connection pooling, etc.
 *
 * <p><b>NOTE:</b> Requires Apache HttpComponents Android HttpClient 4.3 or higher.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 * @since 1.0
 */
public class HttpComponentsClientHttpRequestFactory implements
		ClientHttpRequestFactory, DisposableBean {

	private CloseableHttpClient httpClient;

	private int connectTimeout;

	private int socketTimeout;

	private boolean bufferRequestBody = true;

	/**
	 * Create a new instance of the {@code HttpComponentsClientHttpRequestFactory} with a
	 * default {@link HttpClient}.
	 */
	public HttpComponentsClientHttpRequestFactory() {
		this(HttpClients.createSystem());
	}

	/**
	 * Create a new instance of the {@code HttpComponentsClientHttpRequestFactory} with
	 * the given {@link HttpClient} instance.
	 * <p>
	 * As of Spring for Android 2.0, the given client is expected to be of type
	 * {@link CloseableHttpClient} (requiring Android HttpClient 4.3+).
	 * @param httpClient the HttpClient instance to use for this request factory
	 */
	public HttpComponentsClientHttpRequestFactory(HttpClient httpClient) {
		Assert.notNull(httpClient, "'httpClient' must not be null");
		Assert.isInstanceOf(CloseableHttpClient.class, httpClient, "'httpClient' is not of type CloseableHttpClient");
		this.httpClient = (CloseableHttpClient) httpClient;
	}

	/**
	 * Set the {@code HttpClient} used for
	 * <p>
	 * As of Spring Framework 4.0, the given client is expected to be of type
	 * {@link CloseableHttpClient} (requiring HttpClient 4.3+).
	 */
	public void setHttpClient(HttpClient httpClient) {
		Assert.isInstanceOf(CloseableHttpClient.class, httpClient, "'httpClient' is not of type CloseableHttpClient");
		this.httpClient = (CloseableHttpClient) httpClient;
	}

	/**
	 * Return the {@code HttpClient} used for {@linkplain #createRequest(URI, HttpMethod)
	 * synchronous execution}.
	 */
	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	/**
	 * Set the connection timeout for the underlying HttpClient. A timeout value of 0
	 * specifies an infinite timeout.
	 * @param timeout the timeout value in milliseconds
	 */
	public void setConnectTimeout(int timeout) {
		Assert.isTrue(timeout >= 0, "Timeout must be a non-negative value");
		this.connectTimeout = timeout;
		setLegacyConnectionTimeout(getHttpClient(), timeout);
	}

	/**
	 * Apply the specified connection timeout to deprecated {@link HttpClient}
	 * implementations.
	 * <p>
	 * As of HttpClient 4.3, default parameters have to be exposed through a
	 * {@link RequestConfig} instance instead of setting the parameters on the client.
	 * Unfortunately, this behavior is not backward-compatible and older
	 * {@link HttpClient} implementations will ignore the {@link RequestConfig} object set
	 * in the context.
	 * <p>
	 * If the specified client is an older implementation, we set the custom connection
	 * timeout through the deprecated API. Otherwise, we just return as it is set through
	 * {@link RequestConfig} with newer clients.
	 * @param client the client to configure
	 * @param timeout the custom connection timeout
	 */
	@SuppressWarnings("deprecation")
	private void setLegacyConnectionTimeout(HttpClient client, int timeout) {
		if (org.apache.http.impl.client.AbstractHttpClient.class.isInstance(client)) {
			client.getParams().setIntParameter(
					org.apache.http.params.CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
		}
	}

	/**
	 * Set the socket read timeout for the underlying HttpClient. A timeout value of 0
	 * specifies an infinite timeout.
	 * @param timeout the timeout value in milliseconds
	 */
	public void setReadTimeout(int timeout) {
		Assert.isTrue(timeout >= 0, "Timeout must be a non-negative value");
		this.socketTimeout = timeout;
		setLegacySocketTimeout(getHttpClient(), timeout);
	}

	/**
	 * Apply the specified socket timeout to deprecated {@link HttpClient}
	 * implementations. See {@link #setLegacyConnectionTimeout}.
	 * @param client the client to configure
	 * @param timeout the custom socket timeout
	 * @see #setLegacyConnectionTimeout
	 */
	@SuppressWarnings("deprecation")
	private void setLegacySocketTimeout(HttpClient client, int timeout) {
		if (org.apache.http.impl.client.AbstractHttpClient.class.isInstance(client)) {
			client.getParams().setIntParameter(
					org.apache.http.params.CoreConnectionPNames.SO_TIMEOUT, timeout);
		}
	}

	/**
	 * Indicates whether this request factory should buffer the request body internally.
	 * <p>
	 * Default is {@code true}. When sending large amounts of data via POST or PUT, it is
	 * recommended to change this property to {@code false}, so as not to run out of
	 * memory.
	 */
	public void setBufferRequestBody(boolean bufferRequestBody) {
		this.bufferRequestBody = bufferRequestBody;
	}

	@Override
	public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
		CloseableHttpClient client = (CloseableHttpClient) getHttpClient();
		Assert.state(client != null, "Synchronous execution requires an HttpClient to be set");
		HttpUriRequest httpRequest = createHttpUriRequest(httpMethod, uri);
		postProcessHttpRequest(httpRequest);
		HttpContext context = createHttpContext(httpMethod, uri);
		if (context == null) {
			context = HttpClientContext.create();
		}
		// Request configuration not set in the context
		if (context.getAttribute(HttpClientContext.REQUEST_CONFIG) == null) {
			// Use request configuration given by the user, when available
			RequestConfig config = null;
			if (httpRequest instanceof Configurable) {
				config = ((Configurable) httpRequest).getConfig();
			}
			if (config == null) {
				if (this.socketTimeout > 0 || this.connectTimeout > 0) {
					config = RequestConfig.custom()
							.setConnectTimeout(this.connectTimeout)
							.setSocketTimeout(this.socketTimeout).build();
				}
				else {
					config = RequestConfig.DEFAULT;
				}
			}
			context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
		}
		if (this.bufferRequestBody) {
			return new HttpComponentsClientHttpRequest(client, httpRequest, context);
		}
		else {
			return new HttpComponentsStreamingClientHttpRequest(client, httpRequest, context);
		}
	}

	/**
	 * Create a Commons HttpMethodBase object for the given HTTP method and URI
	 * specification.
	 * @param httpMethod the HTTP method
	 * @param uri the URI
	 * @return the Commons HttpMethodBase object
	 */
	protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
		switch (httpMethod) {
		case GET:
			return new HttpGetHC4(uri);
		case DELETE:
			return new HttpDeleteHC4(uri);
		case HEAD:
			return new HttpHeadHC4(uri);
		case OPTIONS:
			return new HttpOptionsHC4(uri);
		case POST:
			return new HttpPostHC4(uri);
		case PUT:
			return new HttpPutHC4(uri);
		case TRACE:
			return new HttpTraceHC4(uri);
		case PATCH:
			return new HttpPatch(uri);
		default:
			throw new IllegalArgumentException("Invalid HTTP method: " + httpMethod);
		}
	}

	/**
	 * Template method that allows for manipulating the {@link HttpUriRequest} before it
	 * is returned as part of a {@link HttpComponentsClientHttpRequest}.
	 * <p>
	 * The default implementation is empty.
	 * @param request the request to process
	 */
	protected void postProcessHttpRequest(HttpUriRequest request) {
	}

	/**
	 * Template methods that creates a {@link HttpContext} for the given HTTP method and
	 * URI.
	 * <p>
	 * The default implementation returns {@code null}.
	 * @param httpMethod the HTTP method
	 * @param uri the URI
	 * @return the http context
	 */
	protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
		return null;
	}

	/**
	 * Shutdown hook that closes the underlying
	 * {@link org.apache.http.conn.HttpClientConnectionManager ClientConnectionManager}'s
	 * connection pool, if any.
	 */
	@Override
	public void destroy() throws Exception {
		this.httpClient.close();
	}

}
