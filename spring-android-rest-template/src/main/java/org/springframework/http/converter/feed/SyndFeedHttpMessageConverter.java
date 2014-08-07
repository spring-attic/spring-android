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

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;

/**
/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter}
 * that can read and write RSS and ATOM feeds. Specifically, this converter can 
 * handle {@link SyndFeed} objects from the 
 * <a href="https://github.com/rometools/rome">ROME</a> project.
 * 
 *  <p>><b>NOTE: As of Spring for Android 2.0, this is based on the {@code com.rometools}
 * variant of ROME, version 1.5. Please upgrade your build dependency.</b>
 * 
 * <p>
 * By default, this converter reads and writes the media types ({@code application/rss+xml} and
 * {@code application/atom+xml}). This can be overridden by setting the 
 * {@link #setSupportedMediaTypes supportedMediaTypes} property.
 * 
 * @author Roy Clarkson
 * @since 1.0
 * @see SyndFeed
 * @see RssChannelHttpMessageConverter
 * @see AtomFeedHttpMessageConverter
 */
public class SyndFeedHttpMessageConverter extends AbstractHttpMessageConverter<SyndFeed> {

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	/**
	 * Constructor that sets the {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes} to
	 * {@code application/rss+xml} and {@code application/atom+xml}.
	 */
	public SyndFeedHttpMessageConverter() {
		super(MediaType.APPLICATION_RSS_XML, MediaType.APPLICATION_ATOM_XML);
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
		Charset charset =
				(contentType != null && contentType.getCharSet() != null? contentType.getCharSet() : DEFAULT_CHARSET);
		try {
			Reader reader = new InputStreamReader(inputMessage.getBody(), charset);
			return feedInput.build(reader);
		} catch (FeedException ex) {
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
		} catch (FeedException ex) {
			throw new HttpMessageNotWritableException("Could not write SyndFeed: " + ex.getMessage(), ex);
		}
	}
}
