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

import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.core.io.AssetResource;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.xml.sax.SAXException;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.rss.Channel;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.rss.Item;

/** 
 * @author Arjen Poutsma
 * @author Roy Clarkson 
 * */
public class RssChannelHttpMessageConverterTests extends AndroidTestCase {

	private static final Charset utf8 = Charset.forName("UTF-8");

	private RssChannelHttpMessageConverter converter;

	@Override
	public void setUp() throws Exception{
		super.setUp();
		converter = new RssChannelHttpMessageConverter();
		XMLUnit.setIgnoreWhitespace(true);
	}

	@SmallTest
	public void testCanRead() {
		assertTrue(converter.canRead(Channel.class, new MediaType("application", "rss+xml")));
		assertTrue(converter.canRead(Channel.class, new MediaType("application", "rss+xml", utf8)));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(converter.canWrite(Channel.class, new MediaType("application", "rss+xml")));
		assertTrue(converter.canWrite(Channel.class, new MediaType("application", "rss+xml", utf8)));
	}

	@SuppressWarnings("unchecked")
	@MediumTest
	public void testRead() throws IOException {
		AssetResource resource = new AssetResource(getContext().getAssets(), "rss.xml");
		InputStream inputStream = resource.getInputStream();
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(inputStream);
		inputMessage.getHeaders().setContentType(new MediaType("application", "rss+xml", utf8));
		Channel result = converter.read(Channel.class, inputMessage);
		assertEquals("title", result.getTitle());
		assertEquals("http://example.com", result.getLink());
		assertEquals("description", result.getDescription());

		List<Item> items = result.getItems();
		assertEquals(2, items.size());

		Item item1 = items.get(0);
		assertEquals("title1", item1.getTitle());

		Item item2 = items.get(1);
		assertEquals("title2", item2.getTitle());
	}

	@SuppressWarnings("unchecked")
	@MediumTest
	public void testReadComplex() throws IOException {
		AssetResource resource = new AssetResource(getContext().getAssets(), "complex-rss.xml");
		InputStream inputStream = resource.getInputStream();
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(inputStream);
		inputMessage.getHeaders().setContentType(new MediaType("application", "rss+xml", utf8));
		Channel result = converter.read(Channel.class, inputMessage);
		assertEquals("Pivotal P.O.V.", result.getTitle());
		assertEquals("http://blog.pivotal.io", result.getLink());
		assertEquals("The Pivotal blog explores how people are harnessing sophisticated data fabrics and the cloud to build applications that achieve extraordinary things.", result.getDescription());

		List<Item> items = result.getItems();
		assertEquals(10, items.size());

		Item item1 = items.get(0);
		assertEquals("Field Report: Hack Midwest Highlights How Developers Are Innovating on the Internet of Things and Big Data in Real-time", item1.getTitle());
		assertEquals("http://blog.pivotal.io/pivotal/features/field-report-hack-midwest-highlights-how-developers-are-innovating-on-the-internet-of-things-and-big-data-in-real-time?utm_source=rss&utm_medium=rss&utm_campaign=field-report-hack-midwest-highlights-how-developers-are-innovating-on-the-internet-of-things-and-big-data-in-real-time", item1.getLink());
		assertEquals("http://blog.pivotal.io/?p=10209", item1.getGuid().getValue());

		Item item2 = items.get(1);
		assertEquals("How to Deploy Drupal to Pivotal CF Within Seconds", item2.getTitle());
		assertEquals("http://blog.pivotal.io/cloud-foundry-pivotal/products/how-to-deploy-drupal-to-pivotal-cf-within-seconds?utm_source=rss&utm_medium=rss&utm_campaign=how-to-deploy-drupal-to-pivotal-cf-within-seconds", item2.getLink());
		assertEquals("http://blog.gopivotal.com/?p=9983", item2.getGuid().getValue());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SmallTest
	public void testWrite() throws IOException, SAXException {
		Channel channel = new Channel("rss_2.0");
		channel.setTitle("title");
		channel.setLink("http://example.com");
		channel.setDescription("description");

		Item item1 = new Item();
		item1.setTitle("title1");

		Item item2 = new Item();
		item2.setTitle("title2");

		List items = new ArrayList(2);
		items.add(item1);
		items.add(item2);
		channel.setItems(items);

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(channel, null, outputMessage);

		assertEquals("Invalid content-type", new MediaType("application", "rss+xml", utf8),
				outputMessage.getHeaders().getContentType());
		String expected = "<rss version=\"2.0\">" +
				"<channel><title>title</title><link>http://example.com</link><description>description</description>" +
				"<item><title>title1</title></item>" +
				"<item><title>title2</title></item>" +
				"</channel></rss>";
		assertXMLEqual(expected, outputMessage.getBodyAsString(utf8));
	}

	@SmallTest
	public void testWriteOtherCharset() throws IOException, SAXException {
		Channel channel = new Channel("rss_2.0");
		channel.setTitle("title");
		channel.setLink("http://example.com");
		channel.setDescription("description");

		String encoding = "ISO-8859-1";
		channel.setEncoding(encoding);

		Item item1 = new Item();
		item1.setTitle("title1");

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(channel, null, outputMessage);

		assertEquals("Invalid content-type", new MediaType("application", "rss+xml", Charset.forName(encoding)),
				outputMessage.getHeaders().getContentType());
	}

}