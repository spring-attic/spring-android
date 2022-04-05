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

import java.nio.charset.Charset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an HTTP 5xx is received.
 *
 * @author Arjen Poutsma
 * @see DefaultResponseErrorHandler
 * @since 1.0
 */
public class HttpServerErrorException extends HttpStatusCodeException {

	private static final long serialVersionUID = -2915754006618138282L;


	/**
	 * Construct a new instance of {@code HttpServerErrorException} based on an
	 * {@link HttpStatus}.
	 * @param statusCode the status code
	 */
	public HttpServerErrorException(HttpStatus statusCode) {
		super(statusCode);
	}

	/**
	 * Construct a new instance of {@code HttpServerErrorException} based on an
	 * {@link HttpStatus} and status text.
	 * @param statusCode the status code
	 * @param statusText the status text
	 */
	public HttpServerErrorException(HttpStatus statusCode, String statusText) {
		super(statusCode, statusText);
	}

	/**
	 * Construct a new instance of {@code HttpServerErrorException} based on an
	 * {@link HttpStatus}, status text, and response body content.
	 * @param statusCode	  the status code
	 * @param statusText	  the status text
	 * @param responseBody	the response body content, may be {@code null}
	 * @param responseCharset the response body charset, may be {@code null}
	 */
	public HttpServerErrorException(HttpStatus statusCode, String statusText,
									byte[] responseBody, Charset responseCharset) {
		super(statusCode, statusText, responseBody, responseCharset);
	}

	/**
	 * Construct a new instance of {@code HttpServerErrorException} based on a
	 * {@link HttpStatus}, status text, and response body content.
	 * @param statusCode the status code
	 * @param statusText the status text
	 * @param responseHeaders the response headers, may be {@code null}
	 * @param responseBody the response body content, may be {@code null}
	 * @param responseCharset the response body charset, may be {@code null}
	 * @since 2.0
	 */
	public HttpServerErrorException(HttpStatus statusCode, String statusText,
									HttpHeaders responseHeaders, byte[] responseBody, Charset responseCharset) {
		super(statusCode, statusText, responseHeaders, responseBody, responseCharset);
	}
}
