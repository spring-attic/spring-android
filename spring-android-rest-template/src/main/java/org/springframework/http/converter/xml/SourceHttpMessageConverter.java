/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.converter.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import android.os.Build;


/**
 * Implementation of {@link org.springframework.http.converter.HttpMessageConverter}
 * that can read and write {@link Source} objects.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * Roy Clarkson
 * @since 1.0
 */
public class SourceHttpMessageConverter<T extends Source> extends AbstractHttpMessageConverter<T> {

	private static final Set<Class<?>> SUPPORTED_CLASSES = new HashSet<Class<?>>(4);

	static {
		SUPPORTED_CLASSES.add(DOMSource.class);
		SUPPORTED_CLASSES.add(SAXSource.class);
		SUPPORTED_CLASSES.add(StreamSource.class);
		SUPPORTED_CLASSES.add(Source.class);
	}


	private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

	private boolean processExternalEntities = false;


	/**
	 * Sets the {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes}
	 * to {@code text/xml} and {@code application/xml}, and {@code application/*-xml}.
	 */
	public SourceHttpMessageConverter() {
		super(MediaType.APPLICATION_XML, MediaType.TEXT_XML, new MediaType("application", "*+xml"));
	}


	/**
	 * Indicates whether external XML entities are processed when converting to a Source.
	 * <p>Default is {@code false}, meaning that external entities are not resolved.
	 *
	 * <p>Note: This feature is not supported when reading {@code DOMSource}
	 * entities and is only supported on Android 4.0 and newer when reading
	 * {@code SAXSource}.
	 */
	public void setProcessExternalEntities(boolean processExternalEntities) {
		this.processExternalEntities = processExternalEntities;
	}

	/**
	 * Returns the configured value for whether XML external entities are allowed.
	 */
	public boolean isProcessExternalEntities() {
		return this.processExternalEntities;
	}


	@Override
	public boolean supports(Class<?> clazz) {
		return SUPPORTED_CLASSES.contains(clazz);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {

		InputStream body = inputMessage.getBody();
		if (DOMSource.class.equals(clazz)) {
			return (T) readDOMSource(body);
		}
		else if (SAXSource.class.equals(clazz)) {
			return (T) readSAXSource(body);
		}
		else if (StreamSource.class.equals(clazz) || Source.class.equals(clazz)) {
			return (T) readStreamSource(body);
		}
		else {
			throw new HttpMessageConversionException("Could not read class [" + clazz +
					"]. Only DOMSource, SAXSource, and StreamSource are supported.");
		}
	}

	private DOMSource readDOMSource(InputStream body) throws IOException {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			if (!isProcessExternalEntities()) {
				documentBuilder.setEntityResolver(NO_OP_ENTITY_RESOLVER);
			}
			Document document = documentBuilder.parse(body);
			return new DOMSource(document);
		}
		catch (ParserConfigurationException ex) {
			throw new HttpMessageNotReadableException("Could not set feature: " + ex.getMessage(), ex);
		}
		catch (SAXException ex) {
			throw new HttpMessageNotReadableException("Could not parse document: " + ex.getMessage(), ex);
		}
	}

	private SAXSource readSAXSource(InputStream body) throws IOException {
		try {
			// XMLReaderFactory.createXMLReader() is not working on Android
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser newSAXParser = saxParserFactory.newSAXParser();
			XMLReader reader = newSAXParser.getXMLReader();
			// older versions of the Android parser do not support this feature
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				reader.setFeature("http://www.xml.org/sax/features/external-general-entities", isProcessExternalEntities());
			}
			byte[] bytes = StreamUtils.copyToByteArray(body);
			if (!isProcessExternalEntities()) {
				reader.setEntityResolver(NO_OP_ENTITY_RESOLVER);
			}
			return new SAXSource(reader, new InputSource(new ByteArrayInputStream(bytes)));
		}
		catch (ParserConfigurationException ex) {
			throw new HttpMessageNotReadableException("Could not parse document: " + ex.getMessage(), ex);
		}
		catch (SAXException ex) {
			throw new HttpMessageNotReadableException("Could not parse document: " + ex.getMessage(), ex);
		}
	}

	private StreamSource readStreamSource(InputStream body) throws IOException {
		byte[] bytes = StreamUtils.copyToByteArray(body);
		return new StreamSource(new ByteArrayInputStream(bytes));
	}

	@Override
	protected Long getContentLength(T t, MediaType contentType) {
		if (t instanceof DOMSource) {
			try {
				CountingOutputStream os = new CountingOutputStream();
				transform(t, new StreamResult(os));
				return os.count;
			}
			catch (TransformerException ex) {
				// ignore
			}
		}
		return null;
	}

	@Override
	protected void writeInternal(T t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		try {
			Result result = new StreamResult(outputMessage.getBody());
			transform(t, result);
		}
		catch (TransformerException ex) {
			throw new HttpMessageNotWritableException("Could not transform [" + t + "] to output message", ex);
		}
	}

	private void transform(Source source, Result result) throws TransformerException {
		this.transformerFactory.newTransformer().transform(source, result);
	}


	private static class CountingOutputStream extends OutputStream {

		long count = 0;

		@Override
		public void write(int b) throws IOException {
			this.count++;
		}

		@Override
		public void write(byte[] b) throws IOException {
			this.count += b.length;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.count += len;
		}
	}


	private static final EntityResolver NO_OP_ENTITY_RESOLVER = new EntityResolver() {
		@Override
		public InputSource resolveEntity(String publicId, String systemId) {
			return new InputSource(new StringReader(""));
		}
	};

}
