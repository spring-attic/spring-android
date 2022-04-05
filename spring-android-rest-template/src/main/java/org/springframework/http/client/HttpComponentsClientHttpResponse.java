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
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtilsHC4;

import org.springframework.http.HttpHeaders;

/**
 * {@link org.springframework.http.client.ClientHttpResponse} implementation that uses
 * Apache HttpComponents Android HttpClient to execute requests.
 *
 * <p>Created via the {@link HttpComponentsClientHttpRequest}.
 *
 * <p><b>NOTE:</b> Requires Apache HttpComponents Android HttpClient 4.3 or higher.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @since 1.0
 * @see HttpComponentsClientHttpRequest#execute()
 */
final class HttpComponentsClientHttpResponse extends AbstractClientHttpResponse {

	private final CloseableHttpResponse httpResponse;

	private HttpHeaders headers;


	HttpComponentsClientHttpResponse(CloseableHttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}


	@Override
	public int getRawStatusCode() throws IOException {
		return this.httpResponse.getStatusLine().getStatusCode();
	}

	@Override
	public String getStatusText() throws IOException {
		return this.httpResponse.getStatusLine().getReasonPhrase();
	}

	@Override
	public HttpHeaders getHeaders() {
		if (this.headers == null) {
			this.headers = new HttpHeaders();
			for (Header header : this.httpResponse.getAllHeaders()) {
				this.headers.add(header.getName(), header.getValue());
			}
		}
		return this.headers;
	}

	@Override
	public InputStream getBodyInternal() throws IOException {
		HttpEntity entity = this.httpResponse.getEntity();
		return (entity != null ? entity.getContent() : null);
	}

	@Override
	public void closeInternal() {
		// Release underlying connection back to the connection manager
		try {
			try {
				// Attempt to keep connection alive by consuming its remaining content
				EntityUtilsHC4.consume(this.httpResponse.getEntity());
			}
			finally {
				this.httpResponse.close();
			}
		}
		catch (IOException ex) {
			// Ignore exception on close...
		}
	}

}
