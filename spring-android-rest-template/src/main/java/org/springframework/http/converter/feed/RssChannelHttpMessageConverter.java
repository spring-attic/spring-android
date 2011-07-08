/*
 * Copyright 2002-2011 the original author or authors.
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

import org.springframework.http.MediaType;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.rss.Channel;

/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter} that can read and write RSS feeds.
 * Specifically, this converter can handle {@link Channel} objects, from the <a href="https://rome.dev.java.net/">ROME</a>
 * project.
 *
 * <p>By default, this converter reads and writes the media type ({@code application/rss+xml}). This can
 * be overridden by setting the {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes} property.
 *
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @see Channel
 * @since 1.0.0
 */
public class RssChannelHttpMessageConverter extends AbstractWireFeedHttpMessageConverter<Channel> {

	public RssChannelHttpMessageConverter() {
		super(MediaType.APPLICATION_RSS_XML);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return Channel.class.isAssignableFrom(clazz);
	}


}