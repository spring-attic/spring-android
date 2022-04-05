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

package org.springframework.http.converter;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

/**
 * Implementation of {@link HttpMessageConverter} that can read and write {@link Resource Resources}.
 *
 * <p>By default, this converter can read all media types. Written resources use
 * {@code application/octet-stream} for the {@code Content-Type}.
 *
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @since 1.0
 */
public class ResourceHttpMessageConverter extends AbstractHttpMessageConverter<Resource> {

	public ResourceHttpMessageConverter() {
		super(MediaType.ALL);
	}


	@Override
	protected boolean supports(Class<?> clazz) {
		return Resource.class.isAssignableFrom(clazz);
	}

	@Override
	protected Resource readInternal(Class<? extends Resource> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {

		byte[] body = StreamUtils.copyToByteArray(inputMessage.getBody());
		return new ByteArrayResource(body);
	}

	@Override
	protected MediaType getDefaultContentType(Resource resource) {
			return MediaType.APPLICATION_OCTET_STREAM;
		}

	@Override
	protected Long getContentLength(Resource resource, MediaType contentType) throws IOException {
		// Don't try to determine contentLength on InputStreamResource - cannot be read afterwards...
		// Note: custom InputStreamResource subclasses could provide a pre-calculated content length!
		return (InputStreamResource.class.equals(resource.getClass()) ? null : resource.contentLength());
	}

	@Override
	protected void writeInternal(Resource resource, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		InputStream in = resource.getInputStream();
		try {
			StreamUtils.copy(in, outputMessage.getBody());
		}
		finally {
			try {
				in.close();
			}
			catch (IOException ex) {
			}
		}
		outputMessage.getBody().flush();
	}

}
