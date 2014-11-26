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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpHeaders;

/**
 * {@link org.springframework.http.client.ClientHttpResponse} implementation that uses
 * Android native Apache HttpClient 4.0 to execute requests.
 * 
 * <p>Created via the {@link HttpComponentsAndroidClientHttpRequest}.
 * 
 * @author Oleg Kalnichevski
 * @author Roy Clarkson
 * @since 2.0
 * @see HttpComponentsAndroidClientHttpRequest#execute()
 * @deprecated
 */
@Deprecated
final class HttpComponentsAndroidClientHttpResponse extends AbstractClientHttpResponse {

	private final HttpResponse httpResponse;

	private HttpHeaders headers;

	HttpComponentsAndroidClientHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public int getRawStatusCode() throws IOException {
		return this.httpResponse.getStatusLine().getStatusCode();
	}

	public String getStatusText() throws IOException {
		return this.httpResponse.getStatusLine().getReasonPhrase();
	}

	public HttpHeaders getHeaders() {
		if (this.headers == null) {
			this.headers = new HttpHeaders();
			for (Header header : this.httpResponse.getAllHeaders()) {
				headers.add(header.getName(), header.getValue());
			}
		}
		return headers;
	}

	@Override
	protected InputStream getBodyInternal() throws IOException {
		HttpEntity entity = this.httpResponse.getEntity();
		return entity != null ? entity.getContent() : null;
	}

	@Override
	protected void closeInternal() {
		HttpEntity entity = this.httpResponse.getEntity();
		if (entity != null) {
			try {
				// This will cause the underlying connection
				// to be released back to the connection manager
				entity.consumeContent();
			} catch (IOException ignore) {
				// Connection will be released automatically
			}
		}
	}

}
