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

package org.springframework.web.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Source;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.InterceptingHttpAccessor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.util.UriTemplate;

import android.util.Log;

/**
 * <strong>Spring's central class for client-side HTTP access.</strong>
 * It simplifies communication with HTTP servers, and enforces RESTful principles.
 * It handles HTTP connections, leaving application code to provide URLs
 * (with possible template variables) and extract results.
 *
 * <p>The main entry points of this template are the methods named after the six main HTTP methods:
 * <table>
 * <tr><th>HTTP method</th><th>RestTemplate methods</th></tr>
 * <tr><td>DELETE</td><td>{@link #delete}</td></tr>
 * <tr><td>GET</td><td>{@link #getForObject}</td></tr>
 * <tr><td></td><td>{@link #getForEntity}</td></tr>
 * <tr><td>HEAD</td><td>{@link #headForHeaders}</td></tr>
 * <tr><td>OPTIONS</td><td>{@link #optionsForAllow}</td></tr>
 * <tr><td>POST</td><td>{@link #postForLocation}</td></tr>
 * <tr><td></td><td>{@link #postForObject}</td></tr>
 * <tr><td>PUT</td><td>{@link #put}</td></tr>
 * <tr><td>any</td><td>{@link #exchange}</td></tr>
 * <tr><td></td><td>{@link #execute}</td></tr> </table>
 *
 * <p>The {@code exchange} and {@code execute} methods are generalized versions of the more specific methods listed
 * above them. They support additional, less frequently used combinations including support for requests using the
 * HTTP PATCH method. However, note that the underlying HTTP library must also support the desired combination.
 *
 * <p>For each of these HTTP methods, there are three corresponding Java methods in the {@code RestTemplate}.
 * Two variants take a {@code String} URI as first argument (eg. {@link #getForObject(String, Class, Object[])},
 * {@link #getForObject(String, Class, Map)}), and are capable of substituting any {@linkplain UriTemplate URI templates}
 * in that URL using either a {@code String} variable arguments array, or a {@code Map<String, String>}.
 * The string varargs variant expands the given template variables in order, so that
 * <pre class="code">
 * String result = restTemplate.getForObject("http://example.com/hotels/{hotel}/bookings/{booking}", String.class, "42",
 * "21");
 * </pre>
 * will perform a GET on {@code http://example.com/hotels/42/bookings/21}. The map variant expands the template based
 * on variable name, and is therefore more useful when using many variables, or when a single variable is used multiple
 * times. For example:
 * <pre class="code">
 * Map&lt;String, String&gt; vars = Collections.singletonMap("hotel", "42");
 * String result = restTemplate.getForObject("http://example.com/hotels/{hotel}/rooms/{hotel}", String.class, vars);
 * </pre>
 * will perform a GET on {@code http://example.com/hotels/42/rooms/42}. Alternatively, there are {@link URI} variant
 * methods ({@link #getForObject(URI, Class)}), which do not allow for URI templates, but allow you to reuse a single,
 * expanded URI multiple times.
 *
 * <p>Furthermore, the {@code String}-argument methods assume that the URL String is unencoded. This means that
 * <pre class="code">
 * restTemplate.getForObject("http://example.com/hotel list");
 * </pre>
 * will perform a GET on {@code http://example.com/hotel%20list}. As a result, any URL passed that is already encoded
 * will be encoded twice (i.e. {@code http://example.com/hotel%20list} will become {@code
 * http://example.com/hotel%2520list}). If this behavior is undesirable, use the {@code URI}-argument methods, which
 * will not perform any URL encoding.
 *
 * <p>Objects passed to and returned from these methods are converted to and from HTTP messages by
 * {@link HttpMessageConverter} instances. Converters for the main mime types are registered by default,
 * but you can also write your own converter and register it via the {@link #setMessageConverters messageConverters}
 * bean property.
 * 
 * <p>The default set of converters are listed in the following table, and are registered based on the corresponding rule.
 * 
 * <p><table border=1 cellpadding=2 cellspacing=0>
 * <tr><th>Message Body Converter</th><th>Rule</th></tr>
 * <tr><td>{@link ByteArrayHttpMessageConverter}</td><td rowspan=5 valign=top>Always included</td></tr>
 * <tr><td>{@link StringHttpMessageConverter}</td></tr>
 * <tr><td>{@link ResourceHttpMessageConverter}</td></tr>
 * <tr><td>{@link SourceHttpMessageConverter}</td></tr>
 * <tr><td>{@link AllEncompassingFormHttpMessageConverter}</td></tr>
 * <tr><td>{@link SimpleXmlHttpMessageConverter}</td><td>Included if the Simple XML serializer is present.</td></tr>
 * <tr><td>{@link MappingJackson2HttpMessageConverter}</td><td>Included if the Jackson 2.x JSON processor is present.</td></tr>
 * <tr><td>{@link GsonHttpMessageConverter}</td><td>Included if Gson is present, and only included if Jackson is not available.</td></tr>
 * </table><br />  
 * 
 * <p>This template uses a {@link org.springframework.http.client.SimpleClientHttpRequestFactory} and a
 * {@link DefaultResponseErrorHandler} as default strategies for creating HTTP connections or handling HTTP errors,
 * respectively. These defaults can be overridden through the {@link #setRequestFactory(ClientHttpRequestFactory)
 * requestFactory} and {@link #setErrorHandler(ResponseErrorHandler) errorHandler} properties.
 *
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @see HttpMessageConverter
 * @see RequestCallback
 * @see ResponseExtractor
 * @see ResponseErrorHandler
 * @since 1.0
 */
public class RestTemplate extends InterceptingHttpAccessor implements RestOperations {

	private static final String TAG = "RestTemplate";

	private final List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

	private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

	private final ResponseExtractor<HttpHeaders> headersExtractor = new HeadersExtractor();


	/**
	 * Create a new instance of the {@link RestTemplate} using default settings.
	 * Default {@link HttpMessageConverter}s are initialized.
	 * @since 2.0 registers a default set of {@link HttpMessageConverter}s
	 */
	public RestTemplate() {
		DefaultMessageConverters.init(this.messageConverters);
	}

	/**
	 * Create a new instance of {@link RestTemplate}, specifying whether to include a
	 * default set of {@link HttpMessageConverter}s. Setting to {@code true} is equivalent
	 * to using the default constructor.
	 * @param registerDefaultConverters true to add the default set of
	 * {@link HttpMessageConverter}s
	 * @see HttpMessageConverter
	 * @deprecated in favor of {@link #RestTemplate(List)}
	 */
	@Deprecated
	public RestTemplate(boolean registerDefaultConverters) {
		if (registerDefaultConverters) {
			DefaultMessageConverters.init(this.messageConverters);
		}
	}

	/**
	 * Create a new instance of the {@link RestTemplate} based on the given {@link ClientHttpRequestFactory}.
	 * @param requestFactory HTTP request factory to use
	 * @see org.springframework.http.client.SimpleClientHttpRequestFactory
	 * @see org.springframework.http.client.HttpComponentsClientHttpRequestFactory
	 * @see org.springframework.http.client.OkHttpClientHttpRequestFactory
	 * @see org.springframework.http.client.HttpComponentsAndroidClientHttpRequestFactory
	 */
	public RestTemplate(ClientHttpRequestFactory requestFactory) {
		this();
		setRequestFactory(requestFactory);
	}

	/**
	 * Create a new instance of {@link RestTemplate} based on the given
	 * {@link ClientHttpRequestFactory}, specifying whether to include a default set of
	 * {@link HttpMessageConverter}s. Setting to {@code true} is equivalent to using the
	 * default constructor.
	 * @param registerDefaultConverters true to add the default set of
	 * {@link HttpMessageConverter}s
	 * @param requestFactory HTTP request factory to use
	 * @see HttpMessageConverter
	 * @see org.springframework.http.client.SimpleClientHttpRequestFactory
	 * @see org.springframework.http.client.HttpComponentsClientHttpRequestFactory
	 * @see org.springframework.http.client.OkHttpClientHttpRequestFactory
	 * @see org.springframework.http.client.HttpComponentsAndroidClientHttpRequestFactory
	 * @deprecated in favor of {@link #RestTemplate(List)} and {@link #setRequestFactory(ClientHttpRequestFactory)}
	 */
	@Deprecated
	public RestTemplate(boolean registerDefaultConverters, ClientHttpRequestFactory requestFactory) {
		this(registerDefaultConverters);
		setRequestFactory(requestFactory);
	}

	/**
	 * Create a new instance of the {@link RestTemplate} using the given list of
	 * {@link HttpMessageConverter} to use
	 * @param messageConverters the list of {@link HttpMessageConverter} to use
	 * @since 2.0
	 */
	public RestTemplate(List<HttpMessageConverter<?>> messageConverters) {
		Assert.notEmpty(messageConverters, "'messageConverters' must not be empty");
		this.messageConverters.addAll(messageConverters);
	}

	/**
	 * Set the message body converters to use.
	 * <p>These converters are used to convert from and to HTTP requests and responses.
	 */
	public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
		Assert.notEmpty(messageConverters, "'messageConverters' must not be empty");
		// Take getMessageConverters() List as-is when passed in here
		if (this.messageConverters != messageConverters) {
			this.messageConverters.clear();
			this.messageConverters.addAll(messageConverters);
		}
	}

	/**
	 * Return the message body converters.
	 */
	public List<HttpMessageConverter<?>> getMessageConverters() {
		return this.messageConverters;
	}

	/**
	 * Set the error handler.
	 * <p>By default, RestTemplate uses a {@link DefaultResponseErrorHandler}.
	 */
	public void setErrorHandler(ResponseErrorHandler errorHandler) {
		Assert.notNull(errorHandler, "'errorHandler' must not be null");
		this.errorHandler = errorHandler;
	}

	/**
	 * Return the error handler.
	 */
	public ResponseErrorHandler getErrorHandler() {
		return this.errorHandler;
	}


	// GET

	public <T> T getForObject(String url, Class<T> responseType, Object... urlVariables) throws RestClientException {
		AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, urlVariables);
	}

	public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> urlVariables) throws RestClientException {
		AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, urlVariables);
	}

	public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
		AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor);
	}

	public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... urlVariables)
			throws RestClientException {

		AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(responseType);
		ResponseEntityResponseExtractor<T> responseExtractor =
				new ResponseEntityResponseExtractor<T>(responseType);
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, urlVariables);
	}

	public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> urlVariables)
			throws RestClientException {

		AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(responseType);
		ResponseEntityResponseExtractor<T> responseExtractor =
				new ResponseEntityResponseExtractor<T>(responseType);
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, urlVariables);
	}

	public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
		AcceptHeaderRequestCallback requestCallback = new AcceptHeaderRequestCallback(responseType);
		ResponseEntityResponseExtractor<T> responseExtractor =
				new ResponseEntityResponseExtractor<T>(responseType);
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor);
	}

	// HEAD

	public HttpHeaders headForHeaders(String url, Object... urlVariables) throws RestClientException {
		return execute(url, HttpMethod.HEAD, null, this.headersExtractor, urlVariables);
	}

	public HttpHeaders headForHeaders(String url, Map<String, ?> urlVariables) throws RestClientException {
		return execute(url, HttpMethod.HEAD, null, this.headersExtractor, urlVariables);
	}

	public HttpHeaders headForHeaders(URI url) throws RestClientException {
		return execute(url, HttpMethod.HEAD, null, this.headersExtractor);
	}

	// POST

	public URI postForLocation(String url, Object request, Object... urlVariables) throws RestClientException {
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request);
		HttpHeaders headers = execute(url, HttpMethod.POST, requestCallback, this.headersExtractor, urlVariables);
		return headers.getLocation();
	}

	public URI postForLocation(String url, Object request, Map<String, ?> urlVariables)
			throws RestClientException {

		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request);
		HttpHeaders headers = execute(url, HttpMethod.POST, requestCallback, this.headersExtractor, urlVariables);
		return headers.getLocation();
	}

	public URI postForLocation(URI url, Object request) throws RestClientException {
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request);
		HttpHeaders headers = execute(url, HttpMethod.POST, requestCallback, this.headersExtractor);
		return headers.getLocation();
	}

	public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables)
			throws RestClientException {

		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request, responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
	}

	public <T> T postForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables)
			throws RestClientException {

		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request, responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
	}

	public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request, responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor);
	}

	public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables)
			throws RestClientException {

		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request, responseType);
		ResponseEntityResponseExtractor<T> responseExtractor =
				new ResponseEntityResponseExtractor<T>(responseType);
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
	}

	public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables)
			throws RestClientException {

		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request, responseType);
		ResponseEntityResponseExtractor<T> responseExtractor =
				new ResponseEntityResponseExtractor<T>(responseType);
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
	}

	public <T> ResponseEntity<T> postForEntity(URI url, Object request, Class<T> responseType) throws RestClientException {
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request, responseType);
		ResponseEntityResponseExtractor<T> responseExtractor =
				new ResponseEntityResponseExtractor<T>(responseType);
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor);
	}

	// PUT

	public void put(String url, Object request, Object... urlVariables) throws RestClientException {
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request);
		execute(url, HttpMethod.PUT, requestCallback, null, urlVariables);
	}

	public void put(String url, Object request, Map<String, ?> urlVariables) throws RestClientException {
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request);
		execute(url, HttpMethod.PUT, requestCallback, null, urlVariables);
	}

	public void put(URI url, Object request) throws RestClientException {
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(request);
		execute(url, HttpMethod.PUT, requestCallback, null);
	}

	// DELETE

	public void delete(String url, Object... urlVariables) throws RestClientException {
		execute(url, HttpMethod.DELETE, null, null, urlVariables);
	}

	public void delete(String url, Map<String, ?> urlVariables) throws RestClientException {
		execute(url, HttpMethod.DELETE, null, null, urlVariables);
	}

	public void delete(URI url) throws RestClientException {
		execute(url, HttpMethod.DELETE, null, null);
	}

	// OPTIONS

	public Set<HttpMethod> optionsForAllow(String url, Object... urlVariables) throws RestClientException {
		HttpHeaders headers = execute(url, HttpMethod.OPTIONS, null, this.headersExtractor, urlVariables);
		return headers.getAllow();
	}

	public Set<HttpMethod> optionsForAllow(String url, Map<String, ?> urlVariables) throws RestClientException {
		HttpHeaders headers = execute(url, HttpMethod.OPTIONS, null, this.headersExtractor, urlVariables);
		return headers.getAllow();
	}

	public Set<HttpMethod> optionsForAllow(URI url) throws RestClientException {
		HttpHeaders headers = execute(url, HttpMethod.OPTIONS, null, this.headersExtractor);
		return headers.getAllow();
	}

	// exchange

	public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
			HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException {

		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(requestEntity, responseType);
		ResponseEntityResponseExtractor<T> responseExtractor = new ResponseEntityResponseExtractor<T>(responseType);
		return execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
			HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {

		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(requestEntity, responseType);
		ResponseEntityResponseExtractor<T> responseExtractor = new ResponseEntityResponseExtractor<T>(responseType);
		return execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
			Class<T> responseType) throws RestClientException {

		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(requestEntity, responseType);
		ResponseEntityResponseExtractor<T> responseExtractor = new ResponseEntityResponseExtractor<T>(responseType);
		return execute(url, method, requestCallback, responseExtractor);
	}

	public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {

		Type type = responseType.getType();
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(requestEntity, type);
		ResponseEntityResponseExtractor<T> responseExtractor = new ResponseEntityResponseExtractor<T>(type);
		return execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) throws RestClientException {

		Type type = responseType.getType();
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(requestEntity, type);
		ResponseEntityResponseExtractor<T> responseExtractor = new ResponseEntityResponseExtractor<T>(type);
		return execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType) throws RestClientException {

		Type type = responseType.getType();
		HttpEntityRequestCallback requestCallback = new HttpEntityRequestCallback(requestEntity, type);
		ResponseEntityResponseExtractor<T> responseExtractor = new ResponseEntityResponseExtractor<T>(type);
		return execute(url, method, requestCallback, responseExtractor);
	}

	// general execution

	public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback,
			ResponseExtractor<T> responseExtractor, Object... urlVariables) throws RestClientException {

		URI expanded = new UriTemplate(url).expand(urlVariables);
		return doExecute(expanded, method, requestCallback, responseExtractor);
	}

	public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback,
			ResponseExtractor<T> responseExtractor, Map<String, ?> urlVariables) throws RestClientException {

		URI expanded = new UriTemplate(url).expand(urlVariables);
		return doExecute(expanded, method, requestCallback, responseExtractor);
	}

	public <T> T execute(URI url, HttpMethod method, RequestCallback requestCallback,
			ResponseExtractor<T> responseExtractor) throws RestClientException {

		return doExecute(url, method, requestCallback, responseExtractor);
	}

	/**
	 * Execute the given method on the provided URI.
	 * <p>The {@link ClientHttpRequest} is processed using the {@link RequestCallback};
	 * the response with the {@link ResponseExtractor}.
	 * @param url the fully-expanded URL to connect to
	 * @param method the HTTP method to execute (GET, POST, etc.)
	 * @param requestCallback object that prepares the request (can be {@code null})
	 * @param responseExtractor object that extracts the return value from the response (can be {@code null})
	 * @return an arbitrary object, as returned by the {@link ResponseExtractor}
	 */
	protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
			ResponseExtractor<T> responseExtractor) throws RestClientException {

		Assert.notNull(url, "'url' must not be null");
		Assert.notNull(method, "'method' must not be null");
		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = createRequest(url, method);
			if (requestCallback != null) {
				requestCallback.doWithRequest(request);
			}
			response = request.execute();
			if (!getErrorHandler().hasError(response)) {
				logResponseStatus(method, url, response);
			}
			else {
				handleResponseError(method, url, response);
			}
			if (responseExtractor != null) {
				return responseExtractor.extractData(response);
			}
			else {
				return null;
			}
		}
		catch (IOException ex) {
			throw new ResourceAccessException("I/O error on " + method.name() +
					" request for \"" + url + "\": " + ex.getMessage(), ex);
		}
		finally {
			if (response != null) {
				response.close();
			}
		}
	}

	private void logResponseStatus(HttpMethod method, URI url, ClientHttpResponse response) {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			try {
				Log.d(TAG, 
						method.name() + " request for \"" + url + "\" resulted in " + response.getStatusCode() + " (" +
								response.getStatusText() + ")");
			}
			catch (IOException e) {
				// ignore
			}
		}
	}

	private void handleResponseError(HttpMethod method, URI url, ClientHttpResponse response) throws IOException {
		if (Log.isLoggable(TAG, Log.WARN)) {
			try {
				Log.w(TAG, 
						method.name() + " request for \"" + url + "\" resulted in " + response.getStatusCode() + " (" +
								response.getStatusText() + "); invoking error handler");
			}
			catch (IOException e) {
				// ignore
			}
		}
		getErrorHandler().handleError(response);
	}

	/**
	 * Request callback implementation that prepares the request's accept headers.
	 */
	private class AcceptHeaderRequestCallback implements RequestCallback {

		private final Type responseType;

		private AcceptHeaderRequestCallback(Type responseType) {
			this.responseType = responseType;
		}

		public void doWithRequest(ClientHttpRequest request) throws IOException {
			if (responseType != null) {
				Class<?> responseClass = null;
				if (responseType instanceof Class) {
					responseClass = (Class<?>) responseType;
				}

				List<MediaType> allSupportedMediaTypes = new ArrayList<MediaType>();
				for (HttpMessageConverter<?> converter : getMessageConverters()) {
					if (responseClass != null) {
						if (converter.canRead(responseClass, null)) {
							allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter));
						}
					}
					else if (converter instanceof GenericHttpMessageConverter) {

						GenericHttpMessageConverter<?> genericConverter = (GenericHttpMessageConverter<?>) converter;
						if (genericConverter.canRead(responseType, null, null)) {
							allSupportedMediaTypes.addAll(getSupportedMediaTypes(converter));
						}
					}

				}
				if (!allSupportedMediaTypes.isEmpty()) {
					MediaType.sortBySpecificity(allSupportedMediaTypes);
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "Setting request Accept header to " +
								allSupportedMediaTypes);
					}
					request.getHeaders().setAccept(allSupportedMediaTypes);
				}
			}
		}

		private List<MediaType> getSupportedMediaTypes(HttpMessageConverter<?> messageConverter) {
			List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();
			List<MediaType> result = new ArrayList<MediaType>(supportedMediaTypes.size());
			for (MediaType supportedMediaType : supportedMediaTypes) {
				if (supportedMediaType.getCharSet() != null) {
					supportedMediaType =
							new MediaType(supportedMediaType.getType(), supportedMediaType.getSubtype());
				}
				result.add(supportedMediaType);
			}
			return result;
		}
	}

	/**
	 * Request callback implementation that writes the given object to the request stream.
	 */
	private class HttpEntityRequestCallback extends AcceptHeaderRequestCallback {

		private final HttpEntity<?> requestEntity;

		private HttpEntityRequestCallback(Object requestBody) {
			this(requestBody, null);
		}

		private HttpEntityRequestCallback(Object requestBody, Type responseType) {
			super(responseType);
			if (requestBody instanceof HttpEntity) {
				this.requestEntity = (HttpEntity<?>) requestBody;
			}
			else if (requestBody != null) {
				this.requestEntity = new HttpEntity<Object>(requestBody);
			}
			else {
				this.requestEntity = HttpEntity.EMPTY;
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
			super.doWithRequest(httpRequest);
			if (!requestEntity.hasBody()) {
				HttpHeaders httpHeaders = httpRequest.getHeaders();
				HttpHeaders requestHeaders = requestEntity.getHeaders();
				if (!requestHeaders.isEmpty()) {
					httpHeaders.putAll(requestHeaders);
				}
				if (httpHeaders.getContentLength() == -1) {
					httpHeaders.setContentLength(0L);
				}
			}
			else {
				Object requestBody = requestEntity.getBody();
				Class<?> requestType = requestBody.getClass();
				HttpHeaders requestHeaders = requestEntity.getHeaders();
				MediaType requestContentType = requestHeaders.getContentType();
				for (HttpMessageConverter<?> messageConverter : getMessageConverters()) {
					if (messageConverter.canWrite(requestType, requestContentType)) {
						if (!requestHeaders.isEmpty()) {
							httpRequest.getHeaders().putAll(requestHeaders);
						}
						if (Log.isLoggable(TAG, Log.DEBUG)) {
							if (requestContentType != null) {
								Log.d(TAG, "Writing [" + requestBody + "] as \"" + requestContentType +
										"\" using [" + messageConverter + "]");
							}
							else {
								Log.d(TAG, "Writing [" + requestBody + "] using [" + messageConverter + "]");
							}

						}
						((HttpMessageConverter<Object>) messageConverter).write(
								requestBody, requestContentType, httpRequest);
						return;
					}
				}
				String message = "Could not write request: no suitable HttpMessageConverter found for request type [" +
						requestType.getName() + "]";
				if (requestContentType != null) {
					message += " and content type [" + requestContentType + "]";
				}
				throw new RestClientException(message);
			}
		}
	}

	/**
	 * Response extractor for {@link HttpEntity}.
	 */
	private class ResponseEntityResponseExtractor<T> implements ResponseExtractor<ResponseEntity<T>> {

		private final HttpMessageConverterExtractor<T> delegate;

		public ResponseEntityResponseExtractor(Type responseType) {
			if (responseType != null && !Void.class.equals(responseType)) {
				this.delegate = new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
			}
			else {
				this.delegate = null;
			}
		}

		public ResponseEntity<T> extractData(ClientHttpResponse response) throws IOException {
			if (this.delegate != null) {
				T body = this.delegate.extractData(response);
				return new ResponseEntity<T>(body, response.getHeaders(), response.getStatusCode());
			}
			else {
				return new ResponseEntity<T>(response.getHeaders(), response.getStatusCode());
			}
		}
	}

	/**
	 * Response extractor that extracts the response {@link HttpHeaders}.
	 */
	private static class HeadersExtractor implements ResponseExtractor<HttpHeaders> {

		public HttpHeaders extractData(ClientHttpResponse response) throws IOException {
			return response.getHeaders();
		}
	}

	/**
	 * Identifies and initializes default {@link HttpMessageConverter} implementations.
	 */
	private static class DefaultMessageConverters {

		private static final boolean javaxXmlTransformPresent = 
				ClassUtils.isPresent("javax.xml.transform.Source", RestTemplate.class.getClassLoader());

		private static final boolean simpleXmlPresent =
				ClassUtils.isPresent("org.simpleframework.xml.Serializer", RestTemplate.class.getClassLoader());

		private static final boolean jackson2Present =
				ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", RestTemplate.class.getClassLoader()) &&
						ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", RestTemplate.class.getClassLoader());

		private static final boolean gsonPresent =
				ClassUtils.isPresent("com.google.gson.Gson", RestTemplate.class.getClassLoader());

		public static void init(List<HttpMessageConverter<?>> messageConverters) {
			messageConverters.add(new ByteArrayHttpMessageConverter());
			messageConverters.add(new StringHttpMessageConverter());
			messageConverters.add(new ResourceHttpMessageConverter());

			// if javax.xml.transform is not available, fall back to standard Form message converter
			if (javaxXmlTransformPresent) {
				messageConverters.add(new SourceHttpMessageConverter<Source>());
				messageConverters.add(new AllEncompassingFormHttpMessageConverter());
			}
			else {
				messageConverters.add(new FormHttpMessageConverter());
			}
			if (simpleXmlPresent) {
				messageConverters.add(new SimpleXmlHttpMessageConverter());
			}
			if (jackson2Present) {
				messageConverters.add(new MappingJackson2HttpMessageConverter());
			} 
			else if (gsonPresent) {
				messageConverters.add(new GsonHttpMessageConverter());
			}
		}
	}

}
