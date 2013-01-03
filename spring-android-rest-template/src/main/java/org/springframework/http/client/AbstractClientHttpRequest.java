/*
 * Copyright 2002-2013 the original author or authors.
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
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

/**
 * Abstract base for {@link ClientHttpRequest} that makes sure that headers and body are not written multiple times.
 * 
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @since 1.0
 */
public abstract class AbstractClientHttpRequest implements ClientHttpRequest {

	private final HttpHeaders headers = new HttpHeaders();

	private boolean executed = false;

	private OutputStream compressedBody;


	public final HttpHeaders getHeaders() {
		return (this.executed ? HttpHeaders.readOnlyHttpHeaders(this.headers) : this.headers);
	}

	public final OutputStream getBody() throws IOException {
		checkExecuted();
		OutputStream body = getBodyInternal(this.headers);
		if (shouldCompress()) {
			return getCompressedBody(body);
		} else {
			return body;
		}
	}

	public final ClientHttpResponse execute() throws IOException {
		checkExecuted();
		if (this.compressedBody != null) {
			this.compressedBody.close();
		}
		ClientHttpResponse result = executeInternal(this.headers);
		this.executed = true;
		return result;
	}

	/**
	 * Abstract template method that returns the body.
	 * @param headers the HTTP headers
	 * @return the body output stream
	 */
	protected abstract OutputStream getBodyInternal(HttpHeaders headers) throws IOException;

	/**
	 * Abstract template method that writes the given headers and content to the HTTP request.
	 * @param headers the HTTP headers
	 * @return the response object for the executed request
	 */
	protected abstract ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException;


	private void checkExecuted() {
		Assert.state(!this.executed, "ClientHttpRequest already executed");
	}

	private boolean shouldCompress() {
		List<ContentCodingType> contentCodingTypes = headers.getContentEncoding();
		for (ContentCodingType contentCodingType : contentCodingTypes) {
			if (contentCodingType.equals(ContentCodingType.GZIP)) {
				return true;
			}
		}
		return false;
	}

	private OutputStream getCompressedBody(OutputStream body) throws IOException {
		if (this.compressedBody == null) {
			this.compressedBody = new GZIPOutputStream(body);
		}
		return this.compressedBody;
	}

}
