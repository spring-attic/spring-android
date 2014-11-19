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

package org.springframework.http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Roy Clarkson
 */
public class MediaTypeTests extends TestCase {

	@SmallTest
	public void testIncludes() throws Exception {
		MediaType textPlain = MediaType.TEXT_PLAIN;
		assertTrue("Equal types is not inclusive", textPlain.includes(textPlain));
		MediaType allText = new MediaType("text");

		assertTrue("All subtypes is not inclusive", allText.includes(textPlain));
		assertFalse("All subtypes is inclusive", textPlain.includes(allText));

		assertTrue("All types is not inclusive", MediaType.ALL.includes(textPlain));
		assertFalse("All types is inclusive", textPlain.includes(MediaType.ALL));

		assertTrue("All types is not inclusive", MediaType.ALL.includes(textPlain));
		assertFalse("All types is inclusive", textPlain.includes(MediaType.ALL));

		MediaType applicationSoapXml = new MediaType("application", "soap+xml");
		MediaType applicationWildcardXml = new MediaType("application", "*+xml");

		assertTrue(applicationSoapXml.includes(applicationSoapXml));
		assertTrue(applicationWildcardXml.includes(applicationWildcardXml));

		assertTrue(applicationWildcardXml.includes(applicationSoapXml));
		assertFalse(applicationSoapXml.includes(applicationWildcardXml));
	}

	@SmallTest
	public void testIsCompatible() throws Exception {
		MediaType textPlain = MediaType.TEXT_PLAIN;
		assertTrue("Equal types is not compatible", textPlain.isCompatibleWith(textPlain));
		MediaType allText = new MediaType("text");

		assertTrue("All subtypes is not compatible", allText.isCompatibleWith(textPlain));
		assertTrue("All subtypes is not compatible", textPlain.isCompatibleWith(allText));

		assertTrue("All types is not compatible", MediaType.ALL.isCompatibleWith(textPlain));
		assertTrue("All types is not compatible", textPlain.isCompatibleWith(MediaType.ALL));

		assertTrue("All types is not compatible", MediaType.ALL.isCompatibleWith(textPlain));
		assertTrue("All types is compatible", textPlain.isCompatibleWith(MediaType.ALL));

		MediaType applicationSoapXml = new MediaType("application", "soap+xml");
		MediaType applicationWildcardXml = new MediaType("application", "*+xml");

		assertTrue(applicationSoapXml.isCompatibleWith(applicationSoapXml));
		assertTrue(applicationWildcardXml.isCompatibleWith(applicationWildcardXml));

		assertTrue(applicationWildcardXml.isCompatibleWith(applicationSoapXml));
		assertTrue(applicationSoapXml.isCompatibleWith(applicationWildcardXml));
	}

	@SmallTest
	public void testToString() throws Exception {
		MediaType mediaType = new MediaType("text", "plain", 0.7);
		String result = mediaType.toString();
		assertEquals("Invalid toString() returned", "text/plain;q=0.7", result);
	}

	@SmallTest
	public void testSlashInType() {
		try {
			new MediaType("text/plain");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testSlashInSubtype() {
		try {
			new MediaType("text", "/");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testGetDefaultQualityValue() {
		MediaType mediaType = new MediaType("text", "plain");
		assertEquals("Invalid quality value", 1, mediaType.getQualityValue(), 0D);
	}

	@SmallTest
	public void testParseMediaType() throws Exception {
		String s = "audio/*; q=0.2";
		MediaType mediaType = MediaType.parseMediaType(s);
		assertEquals("Invalid type", "audio", mediaType.getType());
		assertEquals("Invalid subtype", "*", mediaType.getSubtype());
		assertEquals("Invalid quality factor", 0.2D, mediaType.getQualityValue(), 0D);
	}

	@SmallTest
	public void testParseMediaTypeNoSubtype() {
		try {
			MediaType.parseMediaType("audio");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeNoSubtypeSlash() {
		try {
			MediaType.parseMediaType("audio/");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeTypeRange() {
		try {
			MediaType.parseMediaType("*/json");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeIllegalType() {
		try {
			MediaType.parseMediaType("audio(/basic");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeIllegalSubtype() {
		try {
			MediaType.parseMediaType("audio/basic)");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeEmptyParameterAttribute() {
		try {
			MediaType.parseMediaType("audio/*;=value");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeEmptyParameterValue() {
		try {
			MediaType.parseMediaType("audio/*;attr=");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeIllegalParameterAttribute() {
		try {
			MediaType.parseMediaType("audio/*;attr<=value");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeIllegalParameterValue() {
		try {
			MediaType.parseMediaType("audio/*;attr=v>alue");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeIllegalQualityFactor() {
		try {
			MediaType.parseMediaType("audio/basic;q=1.1");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeIllegalCharset() {
		try {
			MediaType.parseMediaType("text/html; charset=foo-bar");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseMediaTypeQuotedParameterValue() {
		MediaType.parseMediaType("audio/*;attr=\"v>alue\"");
	}

	@SmallTest
	public void testParseMediaTypeIllegalQuotedParameterValue() {
		try {
			MediaType.parseMediaType("audio/*;attr=\"");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	@SmallTest
	public void testParseCharset() throws Exception {
		String s = "text/html; charset=iso-8859-1";
		MediaType mediaType = MediaType.parseMediaType(s);
		assertEquals("Invalid type", "text", mediaType.getType());
		assertEquals("Invalid subtype", "html", mediaType.getSubtype());
		assertEquals("Invalid charset", Charset.forName("ISO-8859-1"), mediaType.getCharSet());
	}

	@SmallTest
	public void testParseQuotedCharset() {
		String s = "application/xml;charset=\"utf-8\"";
		MediaType mediaType = MediaType.parseMediaType(s);
		assertEquals("Invalid type", "application", mediaType.getType());
		assertEquals("Invalid subtype", "xml", mediaType.getSubtype());
		assertEquals("Invalid charset", Charset.forName("UTF-8"), mediaType.getCharSet());
	}

	@SmallTest
	public void testParseURLConnectionMediaType() throws Exception {
		String s = "*; q=.2";
		MediaType mediaType = MediaType.parseMediaType(s);
		assertEquals("Invalid type", "*", mediaType.getType());
		assertEquals("Invalid subtype", "*", mediaType.getSubtype());
		assertEquals("Invalid quality factor", 0.2D, mediaType.getQualityValue(), 0D);
	}

	@SmallTest
	public void testParseMediaTypes() throws Exception {
		String s = "text/plain; q=0.5, text/html, text/x-dvi; q=0.8, text/x-c";
		List<MediaType> mediaTypes = MediaType.parseMediaTypes(s);
		assertNotNull("No media types returned", mediaTypes);
		assertEquals("Invalid amount of media types", 4, mediaTypes.size());

		mediaTypes = MediaType.parseMediaTypes(null);
		assertNotNull("No media types returned", mediaTypes);
		assertEquals("Invalid amount of media types", 0, mediaTypes.size());
	}

	@SmallTest
	public void testCompareTo() {
		MediaType audioBasic = new MediaType("audio", "basic");
		MediaType audio = new MediaType("audio");
		MediaType audioWave = new MediaType("audio", "wave");
		MediaType audioBasicLevel = new MediaType("audio", "basic", Collections.singletonMap("level", "1"));
		MediaType audioBasic07 = new MediaType("audio", "basic", 0.7);

		// equal
		assertEquals("Invalid comparison result", 0, audioBasic.compareTo(audioBasic));
		assertEquals("Invalid comparison result", 0, audio.compareTo(audio));
		assertEquals("Invalid comparison result", 0, audioBasicLevel.compareTo(audioBasicLevel));

		assertTrue("Invalid comparison result", audioBasicLevel.compareTo(audio) > 0);

		List<MediaType> expected = new ArrayList<MediaType>();
		expected.add(audio);
		expected.add(audioBasic);
		expected.add(audioBasicLevel);
		expected.add(audioBasic07);
		expected.add(audioWave);

		List<MediaType> result = new ArrayList<MediaType>(expected);
		Random rnd = new Random();
		// shuffle & sort 10 times
		for (int i = 0; i < 10; i++) {
			Collections.shuffle(result, rnd);
			Collections.sort(result);

			for (int j = 0; j < result.size(); j++) {
				assertSame("Invalid media type at " + j + ", run " + i, expected.get(j), result.get(j));
			}
		}
	}

	@SmallTest
	public void testCompareToConsistentWithEquals() {
		MediaType m1 = MediaType.parseMediaType("text/html; q=0.7; charset=iso-8859-1");
		MediaType m2 = MediaType.parseMediaType("text/html; charset=iso-8859-1; q=0.7");

		assertEquals("Media types not equal", m1, m2);
		assertEquals("compareTo() not consistent with equals", 0, m1.compareTo(m2));
		assertEquals("compareTo() not consistent with equals", 0, m2.compareTo(m1));

		m1 = MediaType.parseMediaType("text/html; q=0.7; charset=iso-8859-1");
		m2 = MediaType.parseMediaType("text/html; Q=0.7; charset=iso-8859-1");
		assertEquals("Media types not equal", m1, m2);
		assertEquals("compareTo() not consistent with equals", 0, m1.compareTo(m2));
		assertEquals("compareTo() not consistent with equals", 0, m2.compareTo(m1));
	}

	@SmallTest
	public void testCompareToCaseSensitivity() {
		MediaType m1 = new MediaType("audio", "basic");
		MediaType m2 = new MediaType("Audio", "Basic");
		assertEquals("Invalid comparison result", 0, m1.compareTo(m2));
		assertEquals("Invalid comparison result", 0, m2.compareTo(m1));

		m1 = new MediaType("audio", "basic", Collections.singletonMap("foo", "bar"));
		m2 = new MediaType("audio", "basic", Collections.singletonMap("Foo", "bar"));
		assertEquals("Invalid comparison result", 0, m1.compareTo(m2));
		assertEquals("Invalid comparison result", 0, m2.compareTo(m1));

		m1 = new MediaType("audio", "basic", Collections.singletonMap("foo", "bar"));
		m2 = new MediaType("audio", "basic", Collections.singletonMap("foo", "Bar"));
		assertTrue("Invalid comparison result", m1.compareTo(m2) != 0);
		assertTrue("Invalid comparison result", m2.compareTo(m1) != 0);


	}

	@SmallTest
	public void testSpecificityComparator() throws Exception {
		MediaType audioBasic = new MediaType("audio", "basic");
		MediaType audioWave = new MediaType("audio", "wave");
		MediaType audio = new MediaType("audio");
		MediaType audio03 = new MediaType("audio", "*", 0.3);
		MediaType audio07 = new MediaType("audio", "*", 0.7);
		MediaType audioBasicLevel = new MediaType("audio", "basic", Collections.singletonMap("level", "1"));
		MediaType textHtml = new MediaType("text", "html");
		MediaType allXml = new MediaType("application", "*+xml");
		MediaType all = MediaType.ALL;

		Comparator<MediaType> comp = MediaType.SPECIFICITY_COMPARATOR;

		// equal
		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic,audioBasic));
		assertEquals("Invalid comparison result", 0, comp.compare(audio, audio));
		assertEquals("Invalid comparison result", 0, comp.compare(audio07, audio07));
		assertEquals("Invalid comparison result", 0, comp.compare(audio03, audio03));
		assertEquals("Invalid comparison result", 0, comp.compare(audioBasicLevel, audioBasicLevel));

		// specific to unspecific
		assertTrue("Invalid comparison result", comp.compare(audioBasic, audio) < 0);
		assertTrue("Invalid comparison result", comp.compare(audioBasic, all) < 0);
		assertTrue("Invalid comparison result", comp.compare(audio, all) < 0);
		assertTrue("Invalid comparison result", comp.compare(MediaType.APPLICATION_XHTML_XML, allXml) < 0);

		// unspecific to specific
		assertTrue("Invalid comparison result", comp.compare(audio, audioBasic) > 0);
		assertTrue("Invalid comparison result", comp.compare(allXml, MediaType.APPLICATION_XHTML_XML) > 0);
		assertTrue("Invalid comparison result", comp.compare(all, audioBasic) > 0);
		assertTrue("Invalid comparison result", comp.compare(all, audio) > 0);

		// qualifiers
		assertTrue("Invalid comparison result", comp.compare(audio, audio07) < 0);
		assertTrue("Invalid comparison result", comp.compare(audio07, audio) > 0);
		assertTrue("Invalid comparison result", comp.compare(audio07, audio03) < 0);
		assertTrue("Invalid comparison result", comp.compare(audio03, audio07) > 0);
		assertTrue("Invalid comparison result", comp.compare(audio03, all) < 0);
		assertTrue("Invalid comparison result", comp.compare(all, audio03) > 0);

		// other parameters
		assertTrue("Invalid comparison result", comp.compare(audioBasic, audioBasicLevel) > 0);
		assertTrue("Invalid comparison result", comp.compare(audioBasicLevel, audioBasic) < 0);

		// different types
		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic, textHtml));
		assertEquals("Invalid comparison result", 0, comp.compare(textHtml, audioBasic));

		// different subtypes
		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic, audioWave));
		assertEquals("Invalid comparison result", 0, comp.compare(audioWave, audioBasic));
	}

	@SmallTest
	public void testSortBySpecificityRelated() {
		MediaType audioBasic = new MediaType("audio", "basic");
		MediaType audio = new MediaType("audio");
		MediaType audio03 = new MediaType("audio", "*", 0.3);
		MediaType audio07 = new MediaType("audio", "*", 0.7);
		MediaType audioBasicLevel = new MediaType("audio", "basic", Collections.singletonMap("level", "1"));
		MediaType all = MediaType.ALL;

		List<MediaType> expected = new ArrayList<MediaType>();
		expected.add(audioBasicLevel);
		expected.add(audioBasic);
		expected.add(audio);
		expected.add(audio07);
		expected.add(audio03);
		expected.add(all);

		List<MediaType> result = new ArrayList<MediaType>(expected);
		Random rnd = new Random();
		// shuffle & sort 10 times
		for (int i = 0; i < 10; i++) {
			Collections.shuffle(result, rnd);
			MediaType.sortBySpecificity(result);

			for (int j = 0; j < result.size(); j++) {
				assertSame("Invalid media type at " + j, expected.get(j), result.get(j));
			}
		}
	}

	@SmallTest
	public void testSortBySpecificityUnrelated() {
		MediaType audioBasic = new MediaType("audio", "basic");
		MediaType audioWave = new MediaType("audio", "wave");
		MediaType textHtml = new MediaType("text", "html");

		List<MediaType> expected = new ArrayList<MediaType>();
		expected.add(textHtml);
		expected.add(audioBasic);
		expected.add(audioWave);

		List<MediaType> result = new ArrayList<MediaType>(expected);
		MediaType.sortBySpecificity(result);

		for (int i = 0; i < result.size(); i++) {
			assertSame("Invalid media type at " + i, expected.get(i), result.get(i));
		}

	}

	@SmallTest
	public void testQualityComparator() throws Exception {
		MediaType audioBasic = new MediaType("audio", "basic");
		MediaType audioWave = new MediaType("audio", "wave");
		MediaType audio = new MediaType("audio");
		MediaType audio03 = new MediaType("audio", "*", 0.3);
		MediaType audio07 = new MediaType("audio", "*", 0.7);
		MediaType audioBasicLevel = new MediaType("audio", "basic", Collections.singletonMap("level", "1"));
		MediaType textHtml = new MediaType("text", "html");
		MediaType allXml = new MediaType("application", "*+xml");
		MediaType all = MediaType.ALL;

		Comparator<MediaType> comp = MediaType.QUALITY_VALUE_COMPARATOR;

		// equal
		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic,audioBasic));
		assertEquals("Invalid comparison result", 0, comp.compare(audio, audio));
		assertEquals("Invalid comparison result", 0, comp.compare(audio07, audio07));
		assertEquals("Invalid comparison result", 0, comp.compare(audio03, audio03));
		assertEquals("Invalid comparison result", 0, comp.compare(audioBasicLevel, audioBasicLevel));

		// specific to unspecific
		assertTrue("Invalid comparison result", comp.compare(audioBasic, audio) < 0);
		assertTrue("Invalid comparison result", comp.compare(audioBasic, all) < 0);
		assertTrue("Invalid comparison result", comp.compare(audio, all) < 0);
		assertTrue("Invalid comparison result", comp.compare(MediaType.APPLICATION_XHTML_XML, allXml) < 0);

		// unspecific to specific
		assertTrue("Invalid comparison result", comp.compare(audio, audioBasic) > 0);
		assertTrue("Invalid comparison result", comp.compare(all, audioBasic) > 0);
		assertTrue("Invalid comparison result", comp.compare(all, audio) > 0);
		assertTrue("Invalid comparison result", comp.compare(allXml, MediaType.APPLICATION_XHTML_XML) > 0);

		// qualifiers
		assertTrue("Invalid comparison result", comp.compare(audio, audio07) < 0);
		assertTrue("Invalid comparison result", comp.compare(audio07, audio) > 0);
		assertTrue("Invalid comparison result", comp.compare(audio07, audio03) < 0);
		assertTrue("Invalid comparison result", comp.compare(audio03, audio07) > 0);
		assertTrue("Invalid comparison result", comp.compare(audio03, all) > 0);
		assertTrue("Invalid comparison result", comp.compare(all, audio03) < 0);

		// other parameters
		assertTrue("Invalid comparison result", comp.compare(audioBasic, audioBasicLevel) > 0);
		assertTrue("Invalid comparison result", comp.compare(audioBasicLevel, audioBasic) < 0);

		// different types
		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic, textHtml));
		assertEquals("Invalid comparison result", 0, comp.compare(textHtml, audioBasic));

		// different subtypes
		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic, audioWave));
		assertEquals("Invalid comparison result", 0, comp.compare(audioWave, audioBasic));
	}

	@SmallTest
	public void testSortByQualityRelated() {
		MediaType audioBasic = new MediaType("audio", "basic");
		MediaType audio = new MediaType("audio");
		MediaType audio03 = new MediaType("audio", "*", 0.3);
		MediaType audio07 = new MediaType("audio", "*", 0.7);
		MediaType audioBasicLevel = new MediaType("audio", "basic", Collections.singletonMap("level", "1"));
		MediaType all = MediaType.ALL;

		List<MediaType> expected = new ArrayList<MediaType>();
		expected.add(audioBasicLevel);
		expected.add(audioBasic);
		expected.add(audio);
		expected.add(all);
		expected.add(audio07);
		expected.add(audio03);

		List<MediaType> result = new ArrayList<MediaType>(expected);
		Random rnd = new Random();
		// shuffle & sort 10 times
		for (int i = 0; i < 10; i++) {
			Collections.shuffle(result, rnd);
			MediaType.sortByQualityValue(result);

			for (int j = 0; j < result.size(); j++) {
				assertSame("Invalid media type at " + j, expected.get(j), result.get(j));
			}
		}
	}

	@SmallTest
	public void testSortByQualityUnrelated() {
		MediaType audioBasic = new MediaType("audio", "basic");
		MediaType audioWave = new MediaType("audio", "wave");
		MediaType textHtml = new MediaType("text", "html");

		List<MediaType> expected = new ArrayList<MediaType>();
		expected.add(textHtml);
		expected.add(audioBasic);
		expected.add(audioWave);

		List<MediaType> result = new ArrayList<MediaType>(expected);
		MediaType.sortBySpecificity(result);

		for (int i = 0; i < result.size(); i++) {
			assertSame("Invalid media type at " + i, expected.get(i), result.get(i));
		}
	}

	@SmallTest
	public void testWithConversionService() {
		ConversionService conversionService = new DefaultConversionService();
		assertTrue(conversionService.canConvert(String.class, MediaType.class));
		MediaType mediaType = MediaType.parseMediaType("application/xml");
		assertEquals(mediaType, conversionService.convert("application/xml", MediaType.class));
	}

	@SmallTest
	public void testIsConcrete() {
		assertTrue("text/plain not concrete", MediaType.TEXT_PLAIN.isConcrete());
		assertFalse("*/* concrete", MediaType.ALL.isConcrete());
		assertFalse("text/* concrete", new MediaType("text", "*").isConcrete());
	}

	@SmallTest
	public void testConstants() {
		MediaType mediaType = MediaType.ALL;
		assertEquals(mediaType.toString(), MediaType.ALL_VALUE);

		mediaType = MediaType.APPLICATION_ATOM_XML;
		assertEquals(mediaType.toString(), MediaType.APPLICATION_ATOM_XML_VALUE);

		mediaType = MediaType.APPLICATION_RSS_XML;
		assertEquals(mediaType.toString(), MediaType.APPLICATION_RSS_XML_VALUE);

		mediaType = MediaType.APPLICATION_FORM_URLENCODED;
		assertEquals(mediaType.toString(), MediaType.APPLICATION_FORM_URLENCODED_VALUE);

		mediaType = MediaType.APPLICATION_OCTET_STREAM;
		assertEquals(mediaType.toString(), MediaType.APPLICATION_OCTET_STREAM_VALUE);

		mediaType = MediaType.APPLICATION_XHTML_XML;
		assertEquals(mediaType.toString(), MediaType.APPLICATION_XHTML_XML_VALUE);

		mediaType = MediaType.APPLICATION_XML;
		assertEquals(mediaType.toString(), MediaType.APPLICATION_XML_VALUE);

		mediaType = MediaType.APPLICATION_WILDCARD_XML;
		assertEquals(mediaType.toString(), MediaType.APPLICATION_WILDCARD_XML_VALUE);

		mediaType = MediaType.IMAGE_GIF;
		assertEquals(mediaType.toString(), MediaType.IMAGE_GIF_VALUE);

		mediaType = MediaType.IMAGE_JPEG;
		assertEquals(mediaType.toString(), MediaType.IMAGE_JPEG_VALUE);

		mediaType = MediaType.IMAGE_PNG;
		assertEquals(mediaType.toString(), MediaType.IMAGE_PNG_VALUE);

		mediaType = MediaType.MULTIPART_FORM_DATA;
		assertEquals(mediaType.toString(), MediaType.MULTIPART_FORM_DATA_VALUE);

		mediaType = MediaType.TEXT_HTML;
		assertEquals(mediaType.toString(), MediaType.TEXT_HTML_VALUE);

		mediaType = MediaType.TEXT_PLAIN;
		assertEquals(mediaType.toString(), MediaType.TEXT_PLAIN_VALUE);

		mediaType = MediaType.TEXT_XML;
		assertEquals(mediaType.toString(), MediaType.TEXT_XML_VALUE);
	}

}
