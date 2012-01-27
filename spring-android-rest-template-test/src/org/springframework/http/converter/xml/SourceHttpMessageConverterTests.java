/*
 * Copyright 2002-2012 the original author or authors.
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

import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import android.os.Build;
import android.test.suitebuilder.annotation.SmallTest;

/** 
 * @author Arjen Poutsma
 * @author Roy Clarkson 
 */
public class SourceHttpMessageConverterTests extends TestCase {
	
	private static final boolean javaxXmlTransformPresent = 
		(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO);

	private SourceHttpMessageConverter<Source> converter;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (javaxXmlTransformPresent) {
			converter = new SourceHttpMessageConverter<Source>();
		} else {
			fail("SourceHttpMessageConverter is not compatible with this version of Android");
		}
	}
	
	@Override
	public void tearDown() {
		converter = null;
	}

	@SmallTest
	public void testCanRead() {
		assertTrue(converter.canRead(Source.class, new MediaType("application", "xml")));
		assertTrue(converter.canRead(Source.class, new MediaType("application", "soap+xml")));
	}

	@SmallTest
	public void testCanWrite() {
		assertTrue(converter.canWrite(Source.class, new MediaType("application", "xml")));
		assertTrue(converter.canWrite(Source.class, new MediaType("application", "soap+xml")));
		assertTrue(converter.canWrite(Source.class, MediaType.ALL));
	}

	@SmallTest
	public void testReadDOMSource() throws Exception {
		String body = "<root>Hello World</root>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "xml"));
		DOMSource result = (DOMSource) converter.read(DOMSource.class, inputMessage);
		Document document = (Document) result.getNode();
		assertEquals("Invalid result", "root", document.getDocumentElement().getLocalName());
	}

	@SmallTest
	public void testReadSAXSource() throws Exception {
		String body = "<root>Hello World</root>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "xml"));
		SAXSource result = (SAXSource) converter.read(SAXSource.class, inputMessage);
		InputSource inputSource = result.getInputSource();
		String s = FileCopyUtils.copyToString(new InputStreamReader(inputSource.getByteStream()));
		assertTrue("Invalid result", s.contains(body));
	}

	@SmallTest
	public void testReadStreamSource() throws Exception {
		String body = "<root>Hello World</root>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "xml"));
		StreamSource result = (StreamSource) converter.read(StreamSource.class, inputMessage);
		String s = FileCopyUtils.copyToString(new InputStreamReader(result.getInputStream()));
		assertTrue("Invalid result", s.contains(body));
	}

	@SmallTest
	public void testReadSource() throws Exception {
		String body = "<root>Hello World</root>";
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body.getBytes("UTF-8"));
		inputMessage.getHeaders().setContentType(new MediaType("application", "xml"));
		converter.read(Source.class, inputMessage);
	}

	@SmallTest
	public void testWriteDOMSource() throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
		Element rootElement = document.createElement("root");
		document.appendChild(rootElement);
		Text text = document.createTextNode("Hello World");
		rootElement.appendChild(text);
		DOMSource domSource = new DOMSource(document);

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(domSource, null, outputMessage);
		String s = outputMessage.getBodyAsString(Charset.forName("UTF-8"));
		assertTrue("Invalid result", s.contains("<root>Hello World</root>"));
		assertEquals("Invalid content-type", new MediaType("application", "xml"), outputMessage.getHeaders().getContentType());
		assertEquals("Invalid content-length", outputMessage.getBodyAsBytes().length, outputMessage.getHeaders().getContentLength());
	}
	
	@SmallTest
	public void testWriteSAXSource() throws Exception {
		String xml = "<root>Hello World</root>";
		SAXSource saxSource = new SAXSource(new InputSource(new StringReader(xml)));

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(saxSource, null, outputMessage);
		String s = outputMessage.getBodyAsString(Charset.forName("UTF-8"));
		assertTrue("Invalid result", s.contains("<root>Hello World</root>"));
		assertEquals("Invalid content-type", new MediaType("application", "xml"), outputMessage.getHeaders().getContentType());
	}

	@SmallTest
	public void testWriteStreamSource() throws Exception {
		String xml = "<root>Hello World</root>";
		StreamSource streamSource = new StreamSource(new StringReader(xml));

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		converter.write(streamSource, null, outputMessage);
		String s = outputMessage.getBodyAsString(Charset.forName("UTF-8"));
		assertTrue("Invalid result", s.contains("<root>Hello World</root>"));
		assertEquals("Invalid content-type", new MediaType("application", "xml"), outputMessage.getHeaders().getContentType());
	}

}
