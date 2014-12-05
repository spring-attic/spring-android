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
import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.http.client.ClientHttpRequestFactory} implementation that
 * uses Android native
 * <a href="http://hc.apache.org/httpcomponents-client-ga/httpclient/">Apache
 * HttpClient 4.0</a> to create requests.
 * 
 * <p>Allows to use a pre-configured {@link HttpClient} instance - potentially with authentication, HTTP connection
 * pooling, etc.
 * 
 * @author Oleg Kalnichevski
 * @author Roy Clarkson
 * @since 2.0
 * @deprecated in favor of HttpClient 4.3 and {@link org.springframework.http.client.HttpComponentsClientHttpRequestFactory}
 */
@Deprecated
public class HttpComponentsAndroidClientHttpRequestFactory implements ClientHttpRequestFactory, DisposableBean {

	private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;

	private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 5;

	private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (60 * 1000);

	private HttpClient httpClient;

	/**
	 * Create a new instance of the {@code HttpComponentsAndroidClientHttpRequestFactory} with a default {@link HttpClient}
	 * that uses a default {@link ThreadSafeClientConnManager}.
	 */
	public HttpComponentsAndroidClientHttpRequestFactory() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		HttpParams params = new BasicHttpParams();
		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
		ConnManagerParams.setMaxTotalConnections(params, DEFAULT_MAX_TOTAL_CONNECTIONS);
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(DEFAULT_MAX_CONNECTIONS_PER_ROUTE));

		this.httpClient = new DefaultHttpClient(connectionManager, null);
		setReadTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS);
	}

	/**
	 * Create a new instance of the HttpComponentsAndroidClientHttpRequestFactory with the given {@link HttpClient} instance.
	 * @param httpClient the HttpClient instance to use for this factory
	 */
	public HttpComponentsAndroidClientHttpRequestFactory(HttpClient httpClient) {
		Assert.notNull(httpClient, "HttpClient must not be null");
		this.httpClient = httpClient;
	}


	/**
	 * Set the {@code HttpClient} used by this factory.
	 */
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Return the {@code HttpClient} used by this factory.
	 */
	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	/**
	 * Set the connection timeout for the underlying HttpClient. A timeout value of 0 specifies an infinite timeout.
	 * @param timeout the timeout value in milliseconds
	 */
	public void setConnectTimeout(int timeout) {
		Assert.isTrue(timeout >= 0, "Timeout must be a non-negative value");
		getHttpClient().getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
	}

	/**
	 * Set the socket read timeout for the underlying HttpClient. A timeout value of 0 specifies an infinite timeout.
	 * @param timeout the timeout value in milliseconds
	 */
	public void setReadTimeout(int timeout) {
		Assert.isTrue(timeout >= 0, "Timeout must be a non-negative value");
		getHttpClient().getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
	}

	public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
		HttpUriRequest httpRequest = createHttpRequest(httpMethod, uri);
		postProcessHttpRequest(httpRequest);
		return new HttpComponentsAndroidClientHttpRequest(getHttpClient(), httpRequest, createHttpContext(httpMethod, uri));
	}

	/**
	 * Create a HttpComponents HttpUriRequest object for the given HTTP method and URI specification.
	 * 
	 * @param httpMethod the HTTP method
	 * @param uri the URI
	 * @return the HttpComponents HttpUriRequest object
	 */
	protected HttpUriRequest createHttpRequest(HttpMethod httpMethod, URI uri) {
		switch (httpMethod) {
			case GET:
				return new HttpGet(uri);
			case DELETE:
				return new HttpDelete(uri);
			case HEAD:
				return new HttpHead(uri);
			case OPTIONS:
				return new HttpOptions(uri);
			case POST:
				return new HttpPost(uri);
			case PUT:
				return new HttpPut(uri);
			case TRACE:
				return new HttpTrace(uri);
			default:
				throw new IllegalArgumentException("Invalid HTTP method: " + httpMethod);
		}
	}

	/**
	 * Template method that allows for manipulating the {@link HttpUriRequest} before it is returned as part of a
	 * {@link HttpComponentsAndroidClientHttpRequest}.
	 * <p>
	 * The default implementation is empty.
	 * 
	 * @param httpRequest the HTTP request object to process
	 */
	protected void postProcessHttpRequest(HttpUriRequest httpRequest) {
		HttpParams params = httpRequest.getParams();
		HttpProtocolParams.setUseExpectContinue(params, false);
	}

	/**
	 * Template methods that creates a {@link HttpContext} for the given HTTP method and URI.
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
	 * Shutdown hook that closes the underlying {@link ClientConnectionManager}'s connection pool, if any.
	 */
	public void destroy() {
		getHttpClient().getConnectionManager().shutdown();
	}

}
