/*
 * Copyright 2002-2016 the original author or authors.
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
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

/**
 * {@link ClientHttpRequest} implementation that uses OkHttp 3.x to execute requests.
 *
 * <p>Created via the {@link OkHttp3ClientHttpRequestFactory}.
 *
 * @author Luciano Leggieri
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @since 2.0
 */
class OkHttp3ClientHttpRequest extends AbstractBufferingClientHttpRequest
		implements ClientHttpRequest {

	private static final String PROXY_AUTH_ERROR = "Received HTTP_PROXY_AUTH (407) code while not using proxy";

	private static final byte[] NO_BODY = new byte[0];

	private final OkHttpClient client;

	private final URI uri;

	private final HttpMethod method;


	public OkHttp3ClientHttpRequest(OkHttpClient client, URI uri, HttpMethod method) {
		this.client = client;
		this.uri = uri;
		this.method = method;
	}


	@Override
	public HttpMethod getMethod() {
		return this.method;
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	protected ClientHttpResponse executeInternal(HttpHeaders headers, byte[] content) throws IOException {

		boolean requiresBody = method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;

		RequestBody body;

		if (requiresBody && content.length == 0) {
			body = RequestBody.create(null, NO_BODY);
		} else {
			MediaType contentType = getContentType(headers);
			body = (content.length > 0 ? RequestBody.create(contentType, content) : null);
		}

		URL url = this.uri.toURL();
		String methodName = this.method.name();
		Request.Builder builder = new Request.Builder().url(url).method(methodName, body);

		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String headerName = entry.getKey();
			for (String headerValue : entry.getValue()) {
				builder.addHeader(headerName, headerValue);
			}
		}
		Request request = builder.build();
		Response response = null;
		try {
			response = client.newCall(request).execute();
		}
		catch (ProtocolException e) {
			if (PROXY_AUTH_ERROR.equals(e.getMessage())) {
				throw new HttpClientErrorException(HttpStatus.PROXY_AUTHENTICATION_REQUIRED,
						HttpStatus.PROXY_AUTHENTICATION_REQUIRED.getReasonPhrase());
			} else {
				throw e;
			}
		}
		return new OkHttp3ClientHttpResponse(response);
	}

	private MediaType getContentType(HttpHeaders headers) {
		String rawContentType = headers.getFirst("Content-Type");
		return (StringUtils.hasText(rawContentType) ? MediaType.parse(rawContentType) : null);
	}

}
