package org.springframework.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public abstract class AbstractClientHttpResponse implements ClientHttpResponse {

	public InputStream getBody() throws IOException {
		InputStream body = getBodyInternal();
        if (body == null) {
        	return null;
        }
        
		List<ContentCodingType> contentCodingTypes = this.getHeaders().getContentEncoding();
		for (ContentCodingType contentCodingType : contentCodingTypes) {
			if (contentCodingType.equals(ContentCodingType.GZIP)) {
				return new GZIPInputStream(body);
			}
		}
		return body;
	}
	
	/**
	 * Abstract template method that returns the body.
	 *
	 * @param headers the HTTP headers
	 * @return the body output stream
	 */
	protected abstract InputStream getBodyInternal() throws IOException;

}
