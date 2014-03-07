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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;

/**
 * Represents a Compression Type, as defined in the HTTP specification.
 * 
 * @author Roy Clarkson
 * @since 1.0
 */
public class ContentCodingType implements Comparable<ContentCodingType> {

	/**
	 * Public constant encoding type for {@code *}.
	 */
	public final static ContentCodingType ALL;

	/**
	 * A String equivalent of {@link ContentCodingType#ALL}.
	 */
	public static final String ALL_VALUE = "*";

	/**
	 * Public constant encoding type for {@code identity}.
	 */
	public final static ContentCodingType IDENTITY;

	/**
	 * A String equivalent of {@link ContentCodingType#IDENTITY}.
	 */
	public final static String IDENTITY_VALUE = "identity";

	/**
	 * Public constant encoding type for {@code gzip}.
	 */
	public final static ContentCodingType GZIP;

	/**
	 * A String equivalent of {@link ContentCodingType#GZIP}.
	 */
	public final static String GZIP_VALUE = "gzip";


	private static final BitSet TOKEN;

	private static final String WILDCARD_TYPE = "*";

	private static final String PARAM_QUALITY_FACTOR = "q";

	private final String type;

	private final Map<String, String> parameters;

	static {

		// variable names refer to RFC 2616, section 2.2
		BitSet ctl = new BitSet(128);
		for (int i = 0; i <= 31; i++) {
			ctl.set(i);
		}
		ctl.set(127);

		BitSet separators = new BitSet(128);
		separators.set('(');
		separators.set(')');
		separators.set('<');
		separators.set('>');
		separators.set('@');
		separators.set(',');
		separators.set(';');
		separators.set(':');
		separators.set('\\');
		separators.set('\"');
		separators.set('/');
		separators.set('[');
		separators.set(']');
		separators.set('?');
		separators.set('=');
		separators.set('{');
		separators.set('}');
		separators.set(' ');
		separators.set('\t');

		TOKEN = new BitSet(128);
		TOKEN.set(0, 128);
		TOKEN.andNot(ctl);
		TOKEN.andNot(separators);

		ALL = ContentCodingType.valueOf(ALL_VALUE);
		IDENTITY = ContentCodingType.valueOf(IDENTITY_VALUE);
		GZIP = ContentCodingType.valueOf(GZIP_VALUE);
	}


	/**
	 * Create a new {@code ContentCodingType} for the given type.
	 * @param type the type
	 */
	public ContentCodingType(String type) {
		this(type, Collections.<String, String> emptyMap());
	}

	/**
	 * Create a new {@code ContentCodingType} for the given type and quality value.
	 * 
	 * @param type the primary type
	 * @param qualityValue the quality value
	 * @throws IllegalArgumentException if any of the parameters contain illegal characters
	 */
	public ContentCodingType(String type, double qualityValue) {
		this(type, Collections.singletonMap(PARAM_QUALITY_FACTOR, Double.toString(qualityValue)));
	}

	/**
	 * Create a new {@code ContentCodingType} for the given type, and parameters.
	 * @param type the primary type
	 * @param parameters the parameters, may be <code>null</code>
	 * @throws IllegalArgumentException if any of the parameters contain illegal characters
	 */
	public ContentCodingType(String type, Map<String, String> parameters) {
		Assert.hasLength(type, "'type' must not be empty");
		checkToken(type);
		this.type = type.toLowerCase(Locale.ENGLISH);
		if (!CollectionUtils.isEmpty(parameters)) {
			Map<String, String> m = new LinkedCaseInsensitiveMap<String>(parameters.size(), Locale.ENGLISH);
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				String attribute = entry.getKey();
				String value = entry.getValue();
				checkParameters(attribute, value);
				m.put(attribute, unquote(value));
			}
			this.parameters = Collections.unmodifiableMap(m);
		} else {
			this.parameters = Collections.emptyMap();
		}
	}

	/**
	 * Checks the given token string for illegal characters, as defined in RFC 2616, section 2.2.
	 * @throws IllegalArgumentException in case of illegal characters
	 * @see <a href="http://tools.ietf.org/html/rfc2616#section-2.2">HTTP 1.1, section 2.2</a>
	 */
	private void checkToken(String s) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (!TOKEN.get(ch)) {
				throw new IllegalArgumentException("Invalid token character '" + ch + "' in token \"" + s + "\"");
			}
		}
	}

	private void checkParameters(String attribute, String value) {
		Assert.hasLength(attribute, "parameter attribute must not be empty");
		Assert.hasLength(value, "parameter value must not be empty");
		checkToken(attribute);
		if (PARAM_QUALITY_FACTOR.equals(attribute)) {
			value = unquote(value);
			double d = Double.parseDouble(value);
			Assert.isTrue(d >= 0D && d <= 1D, "Invalid quality value \"" + value + "\": should be between 0.0 and 1.0");
		} else if (!isQuotedString(value)) {
			checkToken(value);
		}
	}

	private boolean isQuotedString(String s) {
		return s.length() > 1 && s.startsWith("\"") && s.endsWith("\"");
	}

	private String unquote(String s) {
		if (s == null) {
			return null;
		}
		return isQuotedString(s) ? s.substring(1, s.length() - 1) : s;
	}

	/**
	 * Return the primary type.
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Indicates whether the {@linkplain #getType() type} is the wildcard character <code>&#42;</code> or not.
	 */
	public boolean isWildcardType() {
		return WILDCARD_TYPE.equals(type);
	}

	/**
	 * Return the quality value, as indicated by a <code>q</code> parameter, if any. Defaults to <code>1.0</code>.
	 * @return the quality factory
	 */
	public double getQualityValue() {
		String qualityFactory = getParameter(PARAM_QUALITY_FACTOR);
		return (qualityFactory != null ? Double.parseDouble(qualityFactory) : 1D);
	}

	/**
	 * Return a generic parameter value, given a parameter name.
	 * @param name the parameter name
	 * @return the parameter value; or <code>null</code> if not present
	 */
	public String getParameter(String name) {
		return this.parameters.get(name);
	}

	/**
	 * Indicate whether this {@code ContentCodingType} includes the given coding type.
	 * <p>
	 * For instance, {@code *} includes {@code gzip} and {@code deflate}
	 * @param other the reference coding type with which to compare
	 * @return <code>true</code> if this coding type includes the given coding type; <code>false</code> otherwise
	 */
	public boolean includes(ContentCodingType other) {
		if (other == null) {
			return false;
		}
		if (this.isWildcardType()) {
			// * includes anything
			return true;
		} else if (this.type.equals(other.type)) {
			return true;
		}
		return false;
	}

	/**
	 * Indicate whether this {@code ContentCodingType} is compatible with the given coding type.
	 * <p>
	 * For instance, {@code *} is compatible with {@code gzip}, {@code deflate}, and vice versa. In effect, this method
	 * is similar to {@link #includes(ContentCodingType)}, except that it <b>is</b> symmetric.
	 * @param other the reference coding type with which to compare
	 * @return <code>true</code> if this coding type is compatible with the given coding type; <code>false</code>
	 *         otherwise
	 */
	public boolean isCompatibleWith(ContentCodingType other) {
		if (other == null) {
			return false;
		}
		if (isWildcardType() || other.isWildcardType()) {
			return true;
		} else if (this.type.equals(other.type)) {
			return true;
		}
		return false;
	}

	/**
	 * Compares this {@code ContentCodingType} to another alphabetically.
	 * @param other content coding type to compare to
	 */
	public int compareTo(ContentCodingType other) {
		int comp = this.type.compareToIgnoreCase(other.type);
		if (comp != 0) {
			return comp;
		}
		comp = this.parameters.size() - other.parameters.size();
		if (comp != 0) {
			return comp;
		}
		TreeSet<String> thisAttributes = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		thisAttributes.addAll(this.parameters.keySet());
		TreeSet<String> otherAttributes = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		otherAttributes.addAll(other.parameters.keySet());
		Iterator<String> thisAttributesIterator = thisAttributes.iterator();
		Iterator<String> otherAttributesIterator = otherAttributes.iterator();
		while (thisAttributesIterator.hasNext()) {
			String thisAttribute = thisAttributesIterator.next();
			String otherAttribute = otherAttributesIterator.next();
			comp = thisAttribute.compareToIgnoreCase(otherAttribute);
			if (comp != 0) {
				return comp;
			}
			String thisValue = this.parameters.get(thisAttribute);
			String otherValue = other.parameters.get(otherAttribute);
			if (otherValue == null) {
				otherValue = "";
			}
			comp = thisValue.compareTo(otherValue);
			if (comp != 0) {
				return comp;
			}
		}
		return 0;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ContentCodingType)) {
			return false;
		}
		ContentCodingType otherType = (ContentCodingType) other;
		return (this.type.equalsIgnoreCase(otherType.type) && this.parameters.equals(otherType.parameters));
	}

	@Override
	public int hashCode() {
		int result = this.type.hashCode();
		result = 31 * result + this.parameters.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		appendTo(builder);
		return builder.toString();
	}

	private void appendTo(StringBuilder builder) {
		builder.append(this.type);
		appendTo(this.parameters, builder);
	}

	private void appendTo(Map<String, String> map, StringBuilder builder) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			builder.append(';');
			builder.append(entry.getKey());
			builder.append('=');
			builder.append(entry.getValue());
		}
	}

	/**
	 * Parse the given String value into a {@code ContentCodingType} object.
	 * @see #parseCodingType(String)
	 */
	public static ContentCodingType valueOf(String value) {
		return parseCodingType(value);
	}

	/**
	 * Parse the given String into a single {@code ContentCodingType}.
	 * @param codingType the string to parse
	 * @return the content coding type
	 * @throws IllegalArgumentException if the string cannot be parsed
	 */
	public static ContentCodingType parseCodingType(String codingType) {
		Assert.hasLength(codingType, "'codingType' must not be empty");
		String[] parts = StringUtils.tokenizeToStringArray(codingType, ";");
		String type = parts[0].trim();

		Map<String, String> parameters = null;
		if (parts.length > 1) {
			parameters = new LinkedHashMap<String, String>(parts.length - 1);
			for (int i = 1; i < parts.length; i++) {
				String parameter = parts[i];
				int eqIndex = parameter.indexOf('=');
				if (eqIndex != -1) {
					String attribute = parameter.substring(0, eqIndex);
					String value = parameter.substring(eqIndex + 1, parameter.length());
					parameters.put(attribute, value);
				}
			}
		}

		return new ContentCodingType(type, parameters);
	}


	/**
	 * Parse the given, comma-separated string into a list of {@code ContentCodingType} objects.
	 * <p>
	 * This method can be used to parse an Accept-Encoding.
	 * @param codingTypes the string to parse
	 * @return the list of content coding types
	 * @throws IllegalArgumentException if the string cannot be parsed
	 */
	public static List<ContentCodingType> parseCodingTypes(String codingTypes) {
		if (!StringUtils.hasLength(codingTypes)) {
			return Collections.emptyList();
		}
		String[] tokens = codingTypes.split(",");
		List<ContentCodingType> result = new ArrayList<ContentCodingType>(tokens.length);
		for (String token : tokens) {
			result.add(parseCodingType(token));
		}
		return result;
	}

	/**
	 * Return a string representation of the given list of {@code ContentCodingType} objects.
	 * @param codingTypes the string to parse
	 * @return the list of content coding types
	 * @throws IllegalArgumentException if the String cannot be parsed
	 */
	public static String toString(Collection<ContentCodingType> codingTypes) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<ContentCodingType> iterator = codingTypes.iterator(); iterator.hasNext();) {
			ContentCodingType codingType = iterator.next();
			codingType.appendTo(builder);
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}


	/**
	 * Sorts the given list of {@code ContentCodingType} objects by quality value.
	 * <p>
	 * Given two content coding types:
	 * <ol>
	 * <li>if the two coding types have different {@linkplain #getQualityValue() quality value}, then the coding type
	 * with the highest quality value is ordered before the other.</li>
	 * <li>if either coding type has a {@linkplain #isWildcardType() wildcard type}, then the coding type without the
	 * wildcard is ordered before the other.</li>
	 * <li>if the two coding types have different {@linkplain #getType() types}, then they are considered equal and
	 * remain their current order.</li>
	 * </ol>
	 * @param codingTypes the list of coding types to be sorted
	 * @see #getQualityValue()
	 */
	public static void sortByQualityValue(List<ContentCodingType> codingTypes) {
		Assert.notNull(codingTypes, "'codingTypes' must not be null");
		if (codingTypes.size() > 1) {
			Collections.sort(codingTypes, QUALITY_VALUE_COMPARATOR);
		}
	}


	/**
	 * Comparator used by {@link #sortByQualityValue(List)}.
	 */
	public static final Comparator<ContentCodingType> QUALITY_VALUE_COMPARATOR = new Comparator<ContentCodingType>() {

		public int compare(ContentCodingType codingType1, ContentCodingType codingType2) {
			double quality1 = codingType1.getQualityValue();
			double quality2 = codingType2.getQualityValue();
			int qualityComparison = Double.compare(quality2, quality1);
			if (qualityComparison != 0) {
				return qualityComparison; // deflate;q=0.7 < deflate;q=0.3
			} else if (codingType1.isWildcardType() && !codingType2.isWildcardType()) { // * < deflate
				return 1;
			} else if (codingType2.isWildcardType() && !codingType1.isWildcardType()) { // deflate > *
				return -1;
			} else if (!codingType1.getType().equals(codingType2.getType())) { // gzip == deflate
				return 0;
			}
			return 0;
		}
	};

}
