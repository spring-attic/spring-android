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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.AssetResource;
import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.xml.sax.SAXException;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.atom.Entry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.atom.Feed;

/** 
 * @author Roy Clarkson 
 */
public class AtomFeedHttpMessageConverterTests extends AndroidTestCase {

	private static final Charset UTF_8 = Charset.forName("UTF-8");
	
	private AtomFeedHttpMessageConverter converter;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		converter = new AtomFeedHttpMessageConverter();
	}
	
	@Override
	public void tearDown() {
		converter = null;
	}

	@SmallTest
	public void testCanRead() {
		assertTrue(converter.canRead(Feed.class, new MediaType("application", "atom+xml")));
		assertTrue(converter.canRead(Feed.class, new MediaType("application", "atom+xml", UTF_8)));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(converter.canWrite(Feed.class, new MediaType("application", "atom+xml")));
		assertTrue(converter.canWrite(Feed.class, new MediaType("application", "atom+xml", UTF_8)));
	}

	@MediumTest
	public void testRead() throws IOException {
		AssetResource resource = new AssetResource(getContext().getAssets(), "atom.xml");
		InputStream inputStream = resource.getInputStream();
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(inputStream);
		inputMessage.getHeaders().setContentType(new MediaType("application", "atom+xml", UTF_8));
		Feed result = converter.read(Feed.class, inputMessage);
		assertEquals("title", result.getTitle());
		assertEquals("subtitle", result.getSubtitle().getValue());
		List<?> entries = result.getEntries();
		assertEquals(2, entries.size());

		Entry entry1 = (Entry) entries.get(0);
		assertEquals("id1", entry1.getId());
		assertEquals("title1", entry1.getTitle());

		Entry entry2 = (Entry) entries.get(1);
		assertEquals("id2", entry2.getId());
		assertEquals("title2", entry2.getTitle());
	}

	@SmallTest
	public void testWrite() throws IOException, SAXException {
		Feed feed = new Feed("atom_1.0");
		feed.setTitle("title");

		Entry entry1 = new Entry();
		entry1.setId("id1");
		entry1.setTitle("title1");

		Entry entry2 = new Entry();
		entry2.setId("id2");
		entry2.setTitle("title2");

		List<Entry> entries = new ArrayList<Entry>(2);
		entries.add(entry1);
		entries.add(entry2);
		feed.setEntries(entries);

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(feed, null, outputMessage);

		//TODO: verify output
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<feed xmlns=\"http://www.w3.org/2005/Atom\">" + "<title>title</title>" +
				"<entry><id>id1</id><title>title1</title></entry>" +
				"<entry><id>id2</id><title>title2</title></entry></feed>";
		
		assertEquals("Invalid content-type", new MediaType("application", "atom+xml", UTF_8), 
				outputMessage.getHeaders().getContentType());
	}

	@SmallTest
	public void testWriteOtherCharset() throws IOException, SAXException {
		Feed feed = new Feed("atom_1.0");
		feed.setTitle("title");
		String encoding = "ISO-8859-1";
		feed.setEncoding(encoding);

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(feed, null, outputMessage);

		assertEquals("Invalid content-type", new MediaType("application", "atom+xml", Charset.forName(encoding)),
				outputMessage.getHeaders().getContentType());
	}

}
