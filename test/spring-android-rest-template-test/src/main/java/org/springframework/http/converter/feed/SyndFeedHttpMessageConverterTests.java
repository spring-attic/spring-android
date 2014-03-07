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
import java.nio.charset.Charset;
import java.util.List;

import org.springframework.core.io.AssetResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;

/** 
 * @author Roy Clarkson 
 */
public class SyndFeedHttpMessageConverterTests extends AndroidTestCase {

	private static final Charset UTF_8 = Charset.forName("UTF-8");
	
	private SyndFeedHttpMessageConverter converter;	

	@Override
	public void setUp() throws Exception{
		super.setUp();
		converter = new SyndFeedHttpMessageConverter();
	}
	
	@Override
	public void tearDown() {
		converter = null;
	}

	@SmallTest
	public void estCanRead() {
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "rss+xml")));
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "rss+xml", UTF_8)));
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "atom+xml")));
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "atom+xml", UTF_8)));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "rss+xml")));
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "rss+xml", UTF_8)));
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "atom+xml")));
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "atom+xml", UTF_8)));
	}

	@MediumTest
	public void testReadRss() throws IOException {
	    Resource asset = new AssetResource(getContext().getAssets(), "rss.xml");
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(asset.getInputStream());
		inputMessage.getHeaders().setContentType(new MediaType("application", "rss+xml", UTF_8));
		SyndFeed feed = converter.read(SyndFeed.class, inputMessage);
		assertEquals("title", feed.getTitle());
		assertEquals("http://example.com", feed.getLink());
		assertEquals("description", feed.getDescription());

		List<?> items = feed.getEntries();
		assertEquals(2, items.size());

		SyndEntry entry1 = (SyndEntry) items.get(0);
		assertEquals("title1", entry1.getTitle());

		SyndEntry entry2 = (SyndEntry) items.get(1);
		assertEquals("title2", entry2.getTitle());
	}
	
	@MediumTest
	public void testReadAtom() throws IOException {
		Resource asset = new AssetResource(getContext().getAssets(), "atom.xml");
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(asset.getInputStream());
		inputMessage.getHeaders().setContentType(new MediaType("application", "atom+xml", UTF_8));
		SyndFeed feed = converter.read(SyndFeed.class, inputMessage);
		assertEquals("title", feed.getTitle());
		List<?> entries = feed.getEntries();
		assertEquals(2, entries.size());

		SyndEntry entry1 = (SyndEntry) entries.get(0);
		assertEquals("title1", entry1.getTitle());

		SyndEntry entry2 = (SyndEntry) entries.get(1);
		assertEquals("title2", entry2.getTitle());
	}
}