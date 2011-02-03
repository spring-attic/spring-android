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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;

/** 
 * @author Roy Clarkson 
 * */
public class SyndFeedHttpMessageConverterTests {

	private SyndFeedHttpMessageConverter converter;

	private Charset utf8;

	@Before
	public void setUp() {
		utf8 = Charset.forName("UTF-8");
		converter = new SyndFeedHttpMessageConverter();
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void canRead() {
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "rss+xml")));
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "rss+xml", utf8)));
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "atom+xml")));
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "atom+xml", utf8)));
	}

	@Test
	public void canWrite() {
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "rss+xml")));
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "rss+xml", Charset.forName("UTF-8"))));
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "atom+xml")));
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "atom+xml", Charset.forName("UTF-8"))));
	}

	@Test
	public void readRss() throws IOException {
		InputStream is = getClass().getResourceAsStream("rss.xml");
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(is);
		inputMessage.getHeaders().setContentType(new MediaType("application", "rss+xml", utf8));
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
	
	@Test
	public void readAtom() throws IOException {
		InputStream is = getClass().getResourceAsStream("atom.xml");
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(is);
		inputMessage.getHeaders().setContentType(new MediaType("application", "atom+xml", utf8));
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