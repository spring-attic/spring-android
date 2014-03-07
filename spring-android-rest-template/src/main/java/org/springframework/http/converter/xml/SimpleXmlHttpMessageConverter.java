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

package org.springframework.http.converter.xml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter HttpMessageConverter}
 * that can read and write XML using Simple's {@link Serializer} abstraction.
 * *
 * <p>By default, this converter supports {@code text/xml} and {@code application/xml}. This can be
 * overridden by setting the {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes} property.
 *
 * @author Roy Clarkson
 * @since 1.0
 */
public class SimpleXmlHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
	
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	
	private Serializer serializer;
	
	/**
	 * Construct a new {@code SimpleXmlHttpMessageConverter} with a default {@link Serializer}.
	 * Sets the {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes}
	 * to {@code text/xml} and {@code application/xml}, and {@code application/*+xml}.
	 */
	public SimpleXmlHttpMessageConverter() {
		this(new Persister());
	}

	/**
	 * Construct a new {@code SimpleXmlHttpMessageConverter} with a customized {@link Serializer}.
	 * Sets the {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes}
	 * to {@code text/xml} and {@code application/xml}, and {@code application/*-xml}.
	 */
	public SimpleXmlHttpMessageConverter(Serializer serializer) {
		super(MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_WILDCARD_XML);
		setSerializer(serializer);
	}
	
	/**
	 * Sets the {@code Serializer} for this view. If not set, a default
	 * {@link Serializer} is used.
	 * <p>Setting a custom-configured {@code Serializer} is one way to take further control of the XML serialization
	 * process.
	 * @throws IllegalArgumentException if serializer is null
	 */
	public void setSerializer(Serializer serializer) {
		Assert.notNull(serializer, "'serializer' must not be null");
		this.serializer = serializer;
	}
	
	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return canRead(mediaType);
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return clazz.isAnnotationPresent(Root.class) && canWrite(mediaType);
	}
	
	@Override
	protected boolean supports(Class<?> clazz) {
		// should not be called, since we override canRead/Write
		throw new UnsupportedOperationException();
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) 
			throws IOException, HttpMessageNotReadableException {
		
		Reader source = new InputStreamReader(inputMessage.getBody(), getCharset(inputMessage.getHeaders()));
		
		try {
			Object result = this.serializer.read(clazz, source);
			if (!clazz.isInstance(result)) {
				throw new TypeMismatchException(result, clazz);
			}
			return result;
		} catch (Exception ex) {
			throw new HttpMessageNotReadableException("Could not read [" + clazz + "]", ex);
		}
	}

	@Override
	protected void writeInternal(Object o, HttpOutputMessage outputMessage) 
			throws IOException, HttpMessageNotWritableException {
		
		Writer out = new OutputStreamWriter(outputMessage.getBody(), getCharset(outputMessage.getHeaders()));
		
		try {			
			this.serializer.write(o, out);
			out.close();
		} catch (Exception ex) {
			throw new HttpMessageNotWritableException("Could not write [" + o + "]", ex);
		}
	}
	
	
	// helpers
	
	private Charset getCharset(HttpHeaders headers) {
		if (headers != null && headers.getContentType() != null
				&& headers.getContentType().getCharSet() != null) {
			return headers.getContentType().getCharSet();
		}
		return DEFAULT_CHARSET;
	}
	
}
