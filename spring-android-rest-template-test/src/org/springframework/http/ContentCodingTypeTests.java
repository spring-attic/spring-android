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

package org.springframework.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Roy Clarkson
 */
public class ContentCodingTypeTests extends TestCase {

	@SmallTest
	public void testIncludes() throws Exception {
		ContentCodingType gzip = ContentCodingType.GZIP;
		assertTrue("Equal types is not inclusive", gzip.includes(gzip));
		
		ContentCodingType gzip2 = new ContentCodingType("gzip");
		assertTrue("Equal types is not inclusive", gzip2.includes(gzip));
		assertTrue("Equal types is not inclusive", gzip.includes(gzip2));

		assertTrue("All types is not inclusive", ContentCodingType.ALL.includes(gzip));
		assertFalse("All types is inclusive", gzip.includes(ContentCodingType.ALL));

		assertTrue("All types is not inclusive", ContentCodingType.ALL.includes(gzip));
		assertFalse("All types is inclusive", gzip.includes(ContentCodingType.ALL));
	}
	
	@SmallTest
	public void testIsCompatible() throws Exception {
		ContentCodingType gzip = ContentCodingType.GZIP;
		assertTrue("Equal types is not compatible", gzip.isCompatibleWith(gzip));
		ContentCodingType gzip2 = new ContentCodingType("gzip");

		assertTrue("Equal types is not compatible", gzip.isCompatibleWith(gzip2));
		assertTrue("Equal types is not compatible", gzip2.isCompatibleWith(gzip));

		assertTrue("All types is not compatible", ContentCodingType.ALL.isCompatibleWith(gzip));
		assertTrue("All types is not compatible", gzip.isCompatibleWith(ContentCodingType.ALL));

		assertTrue("All types is not compatible", ContentCodingType.ALL.isCompatibleWith(gzip));
		assertTrue("All types is compatible", gzip.isCompatibleWith(ContentCodingType.ALL));
	}

	@SmallTest
	public void testToString() throws Exception {
		ContentCodingType codingType = new ContentCodingType("gzip", 0.7);
		String result = codingType.toString();
		assertEquals("Invalid toString() returned", "gzip;q=0.7", result);
	}

//	@SmallTest
//	public void testSlashInType() {
//		boolean success = false;
//		try {
//			new MediaType("text/plain");
//		} catch (IllegalArgumentException e) {
//			success = true;
//		}
//		assertTrue("Expected IllegalArgumentException", success);
//	}
//
//	@SmallTest
//	public void testSlashInSubtype() {
//		boolean success = false;
//		try {
//			new MediaType("text", "/");
//		} catch (IllegalArgumentException e) {
//			success = true;
//		}
//		assertTrue("Expected IllegalArgumentException", success);
//	}
	
	@SmallTest
	public void testGetDefaultQualityValue() {
		ContentCodingType type = new ContentCodingType("gzip");
		assertEquals("Invalid quality value", 1, type.getQualityValue(), 0D);
	}

	@SmallTest
	public void testParseCodingType() throws Exception {
		String s = "deflate; q=0.2";
		ContentCodingType codingType = ContentCodingType.parseCodingType(s);
		assertEquals("Invalid type", "deflate", codingType.getType());
		assertEquals("Invalid quality factor", 0.2D, codingType.getQualityValue(), 0D);
	}

//	@SmallTest
//	public void testParseMediaTypeNoSubtype() {
//		boolean success = false;
//		try {
//			MediaType.parseMediaType("audio");
//		} catch (IllegalArgumentException e) {
//			success = true;
//		}
//		assertTrue("Expected IllegalArgumentException", success);
//	}
//
//	@SmallTest
//	public void testParseMediaTypeNoSubtypeSlash() {
//		boolean success = false;
//		try {
//			MediaType.parseMediaType("audio/");
//		} catch (IllegalArgumentException e) {
//			success = true;
//		}
//		assertTrue("Expected IllegalArgumentException", success);
//	}

	@SmallTest
	public void testParseCodingTypeIllegalType() {
		boolean success = false;
		try {
			ContentCodingType.parseCodingType("foo(bar");
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

//	@SmallTest
//	public void testParseMediaTypeIllegalSubtype() {
//		boolean success = false;
//		try {
//			MediaType.parseMediaType("audio/basic)");
//		} catch (IllegalArgumentException e) {
//			success = true;
//		}
//		assertTrue("Expected IllegalArgumentException", success);
//	}

	@SmallTest
	public void testParseCodingTypeEmptyParameterAttribute() {
		boolean success = false;
		try {
			ContentCodingType.parseCodingType("gzip;=value");
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	@SmallTest
	public void testParseCodingTypeEmptyParameterValue() {
		boolean success = false;
		try {
			ContentCodingType.parseCodingType("gzip;attr=");
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	@SmallTest
	public void testParseCodingTypeIllegalParameterAttribute() {
		boolean success = false;
		try {
			ContentCodingType.parseCodingType("gzip;attr<=value");
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	@SmallTest
	public void testParseCodingTypeIllegalParameterValue() {
		boolean success = false;
		try {
			ContentCodingType.parseCodingType("gzip;attr=v>alue");
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

	@SmallTest
	public void testParseCodingTypeIllegalQualityFactor() {
		boolean success = false;
		try {
			ContentCodingType.parseCodingType("gzip;q=1.1");
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

//	@SmallTest
//	public void testParseCodingTypeIllegalCharset() {
//		boolean success = false;
//		try {
//			ContentCodingType.parseCodingType("gzip; charset=foo-bar");
//		} catch (IllegalArgumentException e) {
//			success = true;
//		}
//		assertTrue("Expected IllegalArgumentException", success);
//	}

	@SmallTest
	public void testPparseCodingTypeQuotedParameterValue() {
		ContentCodingType.parseCodingType("gzip;attr=\"v>alue\"");
	}

	@SmallTest
	public void testParseCodingTypeIllegalQuotedParameterValue() {
		boolean success = false;
		try {
			ContentCodingType.parseCodingType("gzip;attr=\"");
		} catch (IllegalArgumentException e) {
			success = true;
		}
		assertTrue("Expected IllegalArgumentException", success);
	}

//	@SmallTest
//	public void testPparseCharset() throws Exception {
//		String s = "text/html; charset=iso-8859-1";
//		MediaType mediaType = MediaType.parseMediaType(s);
//		assertEquals("Invalid type", "text", mediaType.getType());
//		assertEquals("Invalid subtype", "html", mediaType.getSubtype());
//		assertEquals("Invalid charset", Charset.forName("ISO-8859-1"), mediaType.getCharSet());
//	}
//
//	@SmallTest
//	public void testPparseQuotedCharset() {
//		String s = "application/xml;charset=\"utf-8\"";
//		MediaType mediaType = MediaType.parseMediaType(s);
//		assertEquals("Invalid type", "application", mediaType.getType());
//		assertEquals("Invalid subtype", "xml", mediaType.getSubtype());
//		assertEquals("Invalid charset", Charset.forName("UTF-8"), mediaType.getCharSet());
//	}

	@SmallTest
	public void testPparseURLConnectionMediaType() throws Exception {
		String s = "*; q=.2";
		ContentCodingType codingType = ContentCodingType.parseCodingType(s);
		assertEquals("Invalid type", "*", codingType.getType());
		assertEquals("Invalid quality factor", 0.2D, codingType.getQualityValue(), 0D);
	}

	@SmallTest
	public void testParseCodingTypes() throws Exception {
		String s = "gzip; q=0.5, compress, deflate; q=0.8";
		List<ContentCodingType> codingTypes = ContentCodingType.parseCodingTypes(s);
		assertNotNull("No coding types returned", codingTypes);
		assertEquals("Invalid amount of media types", 3, codingTypes.size());

		codingTypes = ContentCodingType.parseCodingTypes(null);
		assertNotNull("No coding types returned", codingTypes);
		assertEquals("Invalid amount of media types", 0, codingTypes.size());
	}

	@SmallTest
	public void testCompareTo() {
		ContentCodingType gzip = new ContentCodingType("gzip");
		ContentCodingType compress01 = new ContentCodingType("compress", 0.1);
		ContentCodingType identity = new ContentCodingType("identity");
		ContentCodingType deflate02 = new ContentCodingType("deflate", 0.2);

		// equal
		assertEquals("Invalid comparison result", 0, gzip.compareTo(gzip));
		assertEquals("Invalid comparison result", 0, compress01.compareTo(compress01));

		List<ContentCodingType> expected = new ArrayList<ContentCodingType>();
		expected.add(compress01);
		expected.add(deflate02);
		expected.add(gzip);
		expected.add(identity);

		List<ContentCodingType> result = new ArrayList<ContentCodingType>(expected);
		Random rnd = new Random();
		// shuffle & sort 10 times
		for (int i = 0; i < 10; i++) {
			Collections.shuffle(result, rnd);
			Collections.sort(result);

			for (int j = 0; j < result.size(); j++) {
				assertSame("Invalid coding type at " + j + ", run " + i, expected.get(j), result.get(j));
			}
		}
	}

	@SmallTest
	public void testCompareToConsistentWithEquals() {
		ContentCodingType codingType1 = ContentCodingType.parseCodingType("gzip; q=0.7");
		ContentCodingType codingType2 = ContentCodingType.parseCodingType("gzip; Q=0.7");
		assertEquals("Media types not equal", codingType1, codingType2);
		assertEquals("compareTo() not consistent with equals", 0, codingType1.compareTo(codingType2));
		assertEquals("compareTo() not consistent with equals", 0, codingType2.compareTo(codingType1));
	}

	@SmallTest
	public void testCompareToCaseSensitivity() {
		ContentCodingType codingType1 = new ContentCodingType("gzip");
		ContentCodingType codingType2 = new ContentCodingType("GZip");
		assertEquals("Invalid comparison result", 0, codingType1.compareTo(codingType2));
		assertEquals("Invalid comparison result", 0, codingType2.compareTo(codingType1));
	}

//	@SmallTest
//	public void testSpecificityComparator() throws Exception {
//		MediaType audioBasic = new MediaType("audio", "basic");
//		MediaType audioWave = new MediaType("audio", "wave");
//		MediaType audio = new MediaType("audio");
//		MediaType audio03 = new MediaType("audio", "*", 0.3);
//		MediaType audio07 = new MediaType("audio", "*", 0.7);
//		MediaType audioBasicLevel = new MediaType("audio", "basic", Collections.singletonMap("level", "1"));
//		MediaType textHtml = new MediaType("text", "html");
//		MediaType all = MediaType.ALL;
//
//		Comparator<MediaType> comp = MediaType.SPECIFICITY_COMPARATOR;
//
//		// equal
//		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic,audioBasic));
//		assertEquals("Invalid comparison result", 0, comp.compare(audio, audio));
//		assertEquals("Invalid comparison result", 0, comp.compare(audio07, audio07));
//		assertEquals("Invalid comparison result", 0, comp.compare(audio03, audio03));
//		assertEquals("Invalid comparison result", 0, comp.compare(audioBasicLevel, audioBasicLevel));
//
//		// specific to unspecific
//		assertTrue("Invalid comparison result", comp.compare(audioBasic, audio) < 0);
//		assertTrue("Invalid comparison result", comp.compare(audioBasic, all) < 0);
//		assertTrue("Invalid comparison result", comp.compare(audio, all) < 0);
//
//		// unspecific to specific
//		assertTrue("Invalid comparison result", comp.compare(audio, audioBasic) > 0);
//		assertTrue("Invalid comparison result", comp.compare(all, audioBasic) > 0);
//		assertTrue("Invalid comparison result", comp.compare(all, audio) > 0);
//
//		// qualifiers
//		assertTrue("Invalid comparison result", comp.compare(audio, audio07) < 0);
//		assertTrue("Invalid comparison result", comp.compare(audio07, audio) > 0);
//		assertTrue("Invalid comparison result", comp.compare(audio07, audio03) < 0);
//		assertTrue("Invalid comparison result", comp.compare(audio03, audio07) > 0);
//		assertTrue("Invalid comparison result", comp.compare(audio03, all) < 0);
//		assertTrue("Invalid comparison result", comp.compare(all, audio03) > 0);
//
//		// other parameters
//		assertTrue("Invalid comparison result", comp.compare(audioBasic, audioBasicLevel) > 0);
//		assertTrue("Invalid comparison result", comp.compare(audioBasicLevel, audioBasic) < 0);
//
//		// different types
//		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic, textHtml));
//		assertEquals("Invalid comparison result", 0, comp.compare(textHtml, audioBasic));
//
//		// different subtypes
//		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic, audioWave));
//		assertEquals("Invalid comparison result", 0, comp.compare(audioWave, audioBasic));
//	}
//
//	@SmallTest
//	public void testSortBySpecificityRelated() {
//		MediaType audioBasic = new MediaType("audio", "basic");
//		MediaType audio = new MediaType("audio");
//		MediaType audio03 = new MediaType("audio", "*", 0.3);
//		MediaType audio07 = new MediaType("audio", "*", 0.7);
//		MediaType audioBasicLevel = new MediaType("audio", "basic", Collections.singletonMap("level", "1"));
//		MediaType all = MediaType.ALL;
//
//		List<MediaType> expected = new ArrayList<MediaType>();
//		expected.add(audioBasicLevel);
//		expected.add(audioBasic);
//		expected.add(audio);
//		expected.add(audio07);
//		expected.add(audio03);
//		expected.add(all);
//
//		List<MediaType> result = new ArrayList<MediaType>(expected);
//		Random rnd = new Random();
//		// shuffle & sort 10 times
//		for (int i = 0; i < 10; i++) {
//			Collections.shuffle(result, rnd);
//			MediaType.sortBySpecificity(result);
//
//			for (int j = 0; j < result.size(); j++) {
//				assertSame("Invalid media type at " + j, expected.get(j), result.get(j));
//			}
//		}
//	}
//
//	@SmallTest
//	public void testSortBySpecificityUnrelated() {
//		MediaType audioBasic = new MediaType("audio", "basic");
//		MediaType audioWave = new MediaType("audio", "wave");
//		MediaType textHtml = new MediaType("text", "html");
//
//		List<MediaType> expected = new ArrayList<MediaType>();
//		expected.add(textHtml);
//		expected.add(audioBasic);
//		expected.add(audioWave);
//
//		List<MediaType> result = new ArrayList<MediaType>(expected);
//		MediaType.sortBySpecificity(result);
//
//		for (int i = 0; i < result.size(); i++) {
//			assertSame("Invalid media type at " + i, expected.get(i), result.get(i));
//		}
//
//	}
//
//	@SmallTest
//	public void testQualityComparator() throws Exception {
//		MediaType audioBasic = new MediaType("audio", "basic");
//		MediaType audioWave = new MediaType("audio", "wave");
//		MediaType audio = new MediaType("audio");
//		MediaType audio03 = new MediaType("audio", "*", 0.3);
//		MediaType audio07 = new MediaType("audio", "*", 0.7);
//		MediaType audioBasicLevel = new MediaType("audio", "basic", Collections.singletonMap("level", "1"));
//		MediaType textHtml = new MediaType("text", "html");
//		MediaType all = MediaType.ALL;
//
//		Comparator<MediaType> comp = MediaType.QUALITY_VALUE_COMPARATOR;
//
//		// equal
//		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic,audioBasic));
//		assertEquals("Invalid comparison result", 0, comp.compare(audio, audio));
//		assertEquals("Invalid comparison result", 0, comp.compare(audio07, audio07));
//		assertEquals("Invalid comparison result", 0, comp.compare(audio03, audio03));
//		assertEquals("Invalid comparison result", 0, comp.compare(audioBasicLevel, audioBasicLevel));
//
//		// specific to unspecific
//		assertTrue("Invalid comparison result", comp.compare(audioBasic, audio) < 0);
//		assertTrue("Invalid comparison result", comp.compare(audioBasic, all) < 0);
//		assertTrue("Invalid comparison result", comp.compare(audio, all) < 0);
//
//		// unspecific to specific
//		assertTrue("Invalid comparison result", comp.compare(audio, audioBasic) > 0);
//		assertTrue("Invalid comparison result", comp.compare(all, audioBasic) > 0);
//		assertTrue("Invalid comparison result", comp.compare(all, audio) > 0);
//
//		// qualifiers
//		assertTrue("Invalid comparison result", comp.compare(audio, audio07) < 0);
//		assertTrue("Invalid comparison result", comp.compare(audio07, audio) > 0);
//		assertTrue("Invalid comparison result", comp.compare(audio07, audio03) < 0);
//		assertTrue("Invalid comparison result", comp.compare(audio03, audio07) > 0);
//		assertTrue("Invalid comparison result", comp.compare(audio03, all) > 0);
//		assertTrue("Invalid comparison result", comp.compare(all, audio03) < 0);
//
//		// other parameters
//		assertTrue("Invalid comparison result", comp.compare(audioBasic, audioBasicLevel) > 0);
//		assertTrue("Invalid comparison result", comp.compare(audioBasicLevel, audioBasic) < 0);
//
//		// different types
//		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic, textHtml));
//		assertEquals("Invalid comparison result", 0, comp.compare(textHtml, audioBasic));
//
//		// different subtypes
//		assertEquals("Invalid comparison result", 0, comp.compare(audioBasic, audioWave));
//		assertEquals("Invalid comparison result", 0, comp.compare(audioWave, audioBasic));
//	}

	@SmallTest
	public void testSortByQualityRelated() {
		ContentCodingType gzip = new ContentCodingType("gzip");
		ContentCodingType gzip03 = new ContentCodingType("gzip", 0.3);
		ContentCodingType gzip07 = new ContentCodingType("gzip", 0.7);
		ContentCodingType all = ContentCodingType.ALL;

		List<ContentCodingType> expected = new ArrayList<ContentCodingType>();
		expected.add(gzip);
		expected.add(all);
		expected.add(gzip07);
		expected.add(gzip03);

		List<ContentCodingType> result = new ArrayList<ContentCodingType>(expected);
		Random rnd = new Random();
		// shuffle & sort 10 times
		for (int i = 0; i < 10; i++) {
			Collections.shuffle(result, rnd);
			ContentCodingType.sortByQualityValue(result);

			for (int j = 0; j < result.size(); j++) {
				assertSame("Invalid media type at " + j, expected.get(j), result.get(j));
			}
		}
	}
	
//	@SmallTest
//	public void testSortByQualityUnrelated() {
//		ContentCodingType gzip = new ContentCodingType("gzip");
//		ContentCodingType deflate = new ContentCodingType("deflate");
//		ContentCodingType identity = new ContentCodingType("identity");
//
//		List<ContentCodingType> expected = new ArrayList<ContentCodingType>();
//		expected.add(gzip);
//		expected.add(deflate);
//		expected.add(identity);
//
//		List<ContentCodingType> result = new ArrayList<ContentCodingType>(expected);
//		ContentCodingType.sortBySpecificity(result);
//
//		for (int i = 0; i < result.size(); i++) {
//			assertSame("Invalid coding type at " + i, expected.get(i), result.get(i));
//		}
//	}

//	@SmallTest
//	public void testWithConversionService() {
//		ConversionService conversionService = new DefaultConversionService();
//		assertTrue(conversionService.canConvert(String.class, MediaType.class));
//		MediaType mediaType = MediaType.parseMediaType("application/xml");
//		assertEquals(mediaType, conversionService.convert("application/xml", MediaType.class));
//	}
//	
//	@SmallTest
//	public void testIsConcrete() {
//		assertTrue("text/plain not concrete", MediaType.TEXT_PLAIN.isConcrete());
//		assertFalse("*/* concrete", MediaType.ALL.isConcrete());
//		assertFalse("text/* concrete", new MediaType("text", "*").isConcrete());
//	}



}
