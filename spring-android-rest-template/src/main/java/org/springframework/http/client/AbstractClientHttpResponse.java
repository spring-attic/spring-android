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
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpStatus;

/**
 * Abstract base for {@link ClientHttpResponse}.
 * 
 * @author Roy Clarkson
 * @since 1.0
 */
public abstract class AbstractClientHttpResponse implements ClientHttpResponse {

	private InputStream compressedBody;


	public HttpStatus getStatusCode() throws IOException {
		return HttpStatus.valueOf(getRawStatusCode());
	}

	public InputStream getBody() throws IOException {
		InputStream body = getBodyInternal();
		if (isCompressed()) {
			return getCompressedBody(body);
		}
		return body;
	}
	
	public void close() {
		if (this.compressedBody != null) {
			try {
				this.compressedBody.close();
			} catch (IOException e) {
				// ignore
			}
		}
		closeInternal();
	}

	private boolean isCompressed() {
		List<ContentCodingType> contentCodingTypes = this.getHeaders().getContentEncoding();
		for (ContentCodingType contentCodingType : contentCodingTypes) {
			if (contentCodingType.equals(ContentCodingType.GZIP)) {
				return true;
			}
		}
		return false;
	}

	private InputStream getCompressedBody(InputStream body) throws IOException {
		if (this.compressedBody == null) {
			this.compressedBody = new GZIPInputStream(body);
		}
		return this.compressedBody;
	}


	/**
	 * Abstract template method that returns the body.
	 * @return the body input stream
	 */
	protected abstract InputStream getBodyInternal() throws IOException;

	/**
	 * Abstract template method that closes the response.
	 */
	protected abstract void closeInternal();


}
