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

package org.springframework.http.converter.feed;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StringUtils;

import android.os.Build;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedOutput;

/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter} that can read and 
 * write RSS and ATOM feeds.  Specifically, this converter can handle {@link SyndFeed} objects, from the 
 * <a href="https://rome.dev.java.net/">ROME</a> project.
 *
 * <p>By default, this converter reads and writes the media types ({@code application/rss+xml} and 
 * {@code application/atom+xml}). This can be overridden by setting the 
 * {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes} property.
 *
 * @author Roy Clarkson
 * @since 1.0.0
 * @see SyndFeed
 */
public class SyndFeedHttpMessageConverter extends AbstractHttpMessageConverter<SyndFeed> {

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	/**
	 * Protected constructor that sets the {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes}
	 * to {@code text/xml} and {@code application/xml}, and {@code application/*-xml}.
	 */
	public SyndFeedHttpMessageConverter() {
		super(MediaType.APPLICATION_RSS_XML, MediaType.APPLICATION_ATOM_XML);
		
		// Workaround to get ROME working with Android 2.1 and earlier
        if (Build.VERSION.SDK != null && Integer.parseInt(Build.VERSION.SDK) < 8) {
        	Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        }
	}
	
	@Override
	protected boolean supports(Class<?> clazz) {
		return SyndFeed.class.isAssignableFrom(clazz);
	}

	@Override
	protected SyndFeed readInternal(Class<? extends SyndFeed> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		SyndFeedInput feedInput = new SyndFeedInput();
		MediaType contentType = inputMessage.getHeaders().getContentType();
		Charset charset;
		if (contentType != null && contentType.getCharSet() != null) {
			charset = contentType.getCharSet();
		} else {
			charset = DEFAULT_CHARSET;
		}
		try {
			Reader reader = new InputStreamReader(inputMessage.getBody(), charset);
			return feedInput.build(reader);
		}
		catch (FeedException ex) {
			throw new HttpMessageNotReadableException("Could not read SyndFeed: " + ex.getMessage(), ex);
		}
	}

	@Override
	protected void writeInternal(SyndFeed syndFeed, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		String syndFeedEncoding = syndFeed.getEncoding();
		if (!StringUtils.hasLength(syndFeedEncoding)) {
			syndFeedEncoding = DEFAULT_CHARSET.name();
		}
		MediaType contentType = outputMessage.getHeaders().getContentType();
		if (contentType != null) {
			Charset syndFeedCharset = Charset.forName(syndFeedEncoding);
			contentType = new MediaType(contentType.getType(), contentType.getSubtype(), syndFeedCharset);
			outputMessage.getHeaders().setContentType(contentType);
		}

		SyndFeedOutput feedOutput = new SyndFeedOutput();

		try {
			Writer writer = new OutputStreamWriter(outputMessage.getBody(), syndFeedEncoding);
			feedOutput.output(syndFeed, writer);
		}
		catch (FeedException ex) {
			throw new HttpMessageNotWritableException("Could not write SyndFeed: " + ex.getMessage(), ex);
		}
	}
}
