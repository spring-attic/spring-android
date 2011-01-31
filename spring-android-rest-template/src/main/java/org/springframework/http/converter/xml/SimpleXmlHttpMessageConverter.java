/*
 * Copyright 2011 the original author or authors.
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

package org.springframework.http.converter.xml;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter HttpMessageConverter}
 * that can read and write XML using Simple's {@link Persister} abstraction.
 * *
 * <p>By default, this converter supports {@code text/xml} and {@code application/xml}. This can be
 * overridden by setting the {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes} property.
 *
 * @author Roy Clarkson
 * @since 1.0.0
 */
public class SimpleXmlHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
	
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	
	private Serializer serializer;
	

	/**
	 * Protected constructor that sets the {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes}
	 * to {@code text/xml} and {@code application/xml}, and {@code application/*-xml}.
	 */
	public SimpleXmlHttpMessageConverter() {
		super(MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_WILDCARD_XML);
		
		this.serializer = new Persister();
	}
	
	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException {
		Assert.notNull(this.serializer, "Property 'serializer' is required");
		try {
			Object result = this.serializer.read(clazz, inputMessage.getBody());
			if (!clazz.isInstance(result)) {
				throw new TypeMismatchException(result, clazz);
			}
			return result;
		}
		catch (Exception ex) {
			throw new HttpMessageNotReadableException("Could not read [" + clazz + "]", ex);
		}
	}

	@Override
	protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException {
		Assert.notNull(this.serializer, "Property 'serializer' is required");
		try {
			Writer writer = new OutputStreamWriter(outputMessage.getBody(), DEFAULT_CHARSET.name());
			this.serializer.write(o, writer);
		}
		catch (Exception ex) {
			throw new HttpMessageNotWritableException("Could not write [" + o + "]", ex);
		}
	}
}
