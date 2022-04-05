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

package org.springframework.web.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;

import android.util.Log;

/**
 * Response extractor that uses the given {@linkplain HttpMessageConverter entity
 * converters} to convert the response into a type {@code T}.
 *
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @see RestTemplate
 * @since 1.0
 */
public class HttpMessageConverterExtractor<T> implements ResponseExtractor<T> {

	private static final String TAG = "RestTemplate";

	private final Type responseType;

	private final Class<T> responseClass;

	private final List<HttpMessageConverter<?>> messageConverters;

	/**
	 * Creates a new instance of the {@code HttpMessageConverterExtractor} with the given
	 * response type and message converters. The given converters must support the response
	 * type.
	 */
	public HttpMessageConverterExtractor(Class<T> responseType, List<HttpMessageConverter<?>> messageConverters) {
		this((Type) responseType, messageConverters);
	}

	/**
	 * Creates a new instance of the {@code HttpMessageConverterExtractor} with the given
	 * response type and message converters. The given converters must support the response
	 * type.
	 */
	@SuppressWarnings("unchecked")
	public HttpMessageConverterExtractor(Type responseType, List<HttpMessageConverter<?>> messageConverters) {
		Assert.notNull(responseType, "'responseType' must not be null");
		Assert.notEmpty(messageConverters, "'messageConverters' must not be empty");
		this.responseType = responseType;
		this.responseClass = (responseType instanceof Class) ? (Class<T>) responseType : null;
		this.messageConverters = messageConverters;
	}

	@SuppressWarnings("unchecked")
	public T extractData(ClientHttpResponse response) throws IOException {
		if (!hasMessageBody(response)) {
			return null;
		}
		MediaType contentType = getContentType(response);

		for (HttpMessageConverter messageConverter : this.messageConverters) {
			if (messageConverter instanceof GenericHttpMessageConverter) {
				GenericHttpMessageConverter genericMessageConverter = (GenericHttpMessageConverter) messageConverter;
				if (genericMessageConverter.canRead(this.responseType, null, contentType)) {
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "Reading [" + this.responseType + "] as \"" +
								contentType + "\" using [" + messageConverter + "]");
					}
					return (T) genericMessageConverter.read(this.responseType, null, response);
				}
			}
			if (this.responseClass != null) {
				if (messageConverter.canRead(this.responseClass, contentType)) {
					if (Log.isLoggable(TAG, Log.DEBUG)) {
						Log.d(TAG, "Reading [" + this.responseClass.getName() + "] as \"" +
								contentType + "\" using [" + messageConverter + "]");
					}
					return (T) messageConverter.read(this.responseClass, response);
				}
			}
		}
		throw new RestClientException(
				"Could not extract response: no suitable HttpMessageConverter found for response type [" +
						this.responseType + "] and content type [" + contentType + "]");
	}

	private MediaType getContentType(ClientHttpResponse response) {
		MediaType contentType = response.getHeaders().getContentType();
		if (contentType == null) {
			if (Log.isLoggable(TAG, Log.VERBOSE)) {
				Log.v(TAG, "No Content-Type header found, defaulting to application/octet-stream");
			}
			contentType = MediaType.APPLICATION_OCTET_STREAM;
		}
		return contentType;
	}

	/**
	 * Indicates whether the given response has a message body. <p>Default implementation
	 * returns {@code false} for a response status of {@code 204} or {@code 304}, or a {@code
	 * Content-Length} of {@code 0}.
	 *
	 * @param response the response to check for a message body
	 * @return {@code true} if the response has a body, {@code false} otherwise
	 * @throws IOException in case of I/O errors
	 */
	protected boolean hasMessageBody(ClientHttpResponse response) throws IOException {
		HttpStatus responseStatus = response.getStatusCode();
		if (responseStatus == HttpStatus.NO_CONTENT ||
				responseStatus == HttpStatus.NOT_MODIFIED) {
			return false;
		}
		long contentLength = response.getHeaders().getContentLength();
		return contentLength != 0;
	}

}
