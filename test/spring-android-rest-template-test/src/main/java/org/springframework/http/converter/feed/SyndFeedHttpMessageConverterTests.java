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
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndPerson;

/** 
 * @author Roy Clarkson 
 */
public class SyndFeedHttpMessageConverterTests extends AndroidTestCase {

	private static final Charset utf8 = Charset.forName("UTF-8");

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
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "rss+xml", utf8)));
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "atom+xml")));
		assertTrue(converter.canRead(SyndFeed.class, new MediaType("application", "atom+xml", utf8)));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "rss+xml")));
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "rss+xml", utf8)));
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "atom+xml")));
		assertTrue(converter.canWrite(SyndFeed.class, new MediaType("application", "atom+xml", utf8)));
	}

	@SuppressWarnings("unchecked")
	@MediumTest
	public void testReadRss() throws IOException {
		Resource asset = new AssetResource(getContext().getAssets(), "rss.xml");
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(asset.getInputStream());
		inputMessage.getHeaders().setContentType(new MediaType("application", "rss+xml", utf8));
		SyndFeed feed = converter.read(SyndFeed.class, inputMessage);
		assertEquals("title", feed.getTitle());
		assertEquals("http://example.com", feed.getLink());
		assertEquals("description", feed.getDescription());

		List<SyndEntry> entries = feed.getEntries();
		assertEquals(2, entries.size());

		SyndEntry entry1 = entries.get(0);
		assertEquals("title1", entry1.getTitle());

		SyndEntry entry2 = entries.get(1);
		assertEquals("title2", entry2.getTitle());
	}

	@SuppressWarnings("unchecked")
	@MediumTest
	public void testReadRssComplex() throws IOException {
		AssetResource resource = new AssetResource(getContext().getAssets(), "complex-rss.xml");
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(resource.getInputStream());
		inputMessage.getHeaders().setContentType(new MediaType("application", "rss+xml", utf8));
		SyndFeed feed = converter.read(SyndFeed.class, inputMessage);
		assertEquals("Pivotal P.O.V.", feed.getTitle());
		assertEquals("http://blog.pivotal.io", feed.getLink());
		assertEquals("The Pivotal blog explores how people are harnessing sophisticated data fabrics and the cloud to build applications that achieve extraordinary things.", feed.getDescription());

		List<SyndEntry> entries = feed.getEntries();
		assertEquals(10, entries.size());

		SyndEntry entry1 = entries.get(0);
		assertEquals("Field Report: Hack Midwest Highlights How Developers Are Innovating on the Internet of Things and Big Data in Real-time", entry1.getTitle());
		assertEquals("http://blog.pivotal.io/pivotal/features/field-report-hack-midwest-highlights-how-developers-are-innovating-on-the-internet-of-things-and-big-data-in-real-time?utm_source=rss&utm_medium=rss&utm_campaign=field-report-hack-midwest-highlights-how-developers-are-innovating-on-the-internet-of-things-and-big-data-in-real-time", entry1.getLink());

		SyndEntry entry2 = entries.get(1);
		assertEquals("How to Deploy Drupal to Pivotal CF Within Seconds", entry2.getTitle());
		assertEquals("http://blog.pivotal.io/cloud-foundry-pivotal/products/how-to-deploy-drupal-to-pivotal-cf-within-seconds?utm_source=rss&utm_medium=rss&utm_campaign=how-to-deploy-drupal-to-pivotal-cf-within-seconds", entry2.getLink());
	}

	@SuppressWarnings("unchecked")
	@MediumTest
	public void testReadAtom() throws IOException {
		Resource asset = new AssetResource(getContext().getAssets(), "atom.xml");
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(asset.getInputStream());
		inputMessage.getHeaders().setContentType(new MediaType("application", "atom+xml", utf8));
		SyndFeed feed = converter.read(SyndFeed.class, inputMessage);
		assertEquals("title", feed.getTitle());

		List<SyndEntry> entries = feed.getEntries();
		assertEquals(2, entries.size());

		SyndEntry entry1 = entries.get(0);
		assertEquals("title1", entry1.getTitle());

		SyndEntry entry2 = entries.get(1);
		assertEquals("title2", entry2.getTitle());
	}

	@SuppressWarnings("unchecked")
	@MediumTest
	public void testReadAtomComplex() throws IOException {
		AssetResource resource = new AssetResource(getContext().getAssets(), "complex-atom.xml");
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(resource.getInputStream());
		inputMessage.getHeaders().setContentType(new MediaType("application", "atom+xml", utf8));
		SyndFeed feed = converter.read(SyndFeed.class, inputMessage);
		assertEquals("Spring", feed.getTitle());

		List<SyndEntry> entries = feed.getEntries();
		assertEquals(20, entries.size());

		SyndEntry entry1 = entries.get(0);
		assertEquals("Spring Boot 1.1.5 released", entry1.getTitle());
		SyndPerson author1 = (SyndPerson) entry1.getAuthors().get(0);
		assertEquals("Phil Webb", author1.getName());

		SyndEntry entry2 = entries.get(1);
		assertEquals("Spring MVC Test HtmlUnit 1.0.0.M2 Released", entry2.getTitle());
		SyndPerson author2 = (SyndPerson) entry2.getAuthors().get(0);
		assertEquals("Rob Winch", author2.getName());
	}

}
