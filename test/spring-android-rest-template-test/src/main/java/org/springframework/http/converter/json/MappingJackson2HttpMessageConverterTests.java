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

package org.springframework.http.converter.json;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MockHttpOutputMessage;

import android.os.Build;
import android.util.Log;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roy Clarkson
 */
public class MappingJackson2HttpMessageConverterTests extends AbstractMappingJacksonHttpMessageConverterTests<MappingJackson2HttpMessageConverter> {

	private static final String TAG = MappingJackson2HttpMessageConverterTests.class.getSimpleName();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.converter = new MappingJackson2HttpMessageConverter();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			Log.w(TAG, "Jackson 2.4 is not supported on Android 2.2 and older");
		}
	}

	protected void prepareReadGenericsTest() {
		this.converter = new MappingJackson2HttpMessageConverter() {

			@Override
			protected JavaType getJavaType(Type type, Class<?> contextClass) {
				if (type instanceof Class && List.class.isAssignableFrom((Class<?>)type)) {
					return new ObjectMapper().getTypeFactory().constructCollectionType(ArrayList.class, MyBean.class);
				}
				else {
					return super.getJavaType(type, contextClass);
				}
			}
		};
	}

	public void testPrettyPrint() throws Exception {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		PrettyPrintBean bean = new PrettyPrintBean();
		bean.setName("Jason");

		getConverter().setPrettyPrint(true);
		getConverter().writeInternal(bean, outputMessage);
		String result = outputMessage.getBodyAsString(Charset.forName("UTF-8"));

		assertEquals("{" + NEWLINE_SYSTEM_PROPERTY + "  \"name\" : \"Jason\"" + NEWLINE_SYSTEM_PROPERTY + "}", result);
	}

	public void testPrefixJson() throws Exception {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		getConverter().setPrefixJson(true);
		getConverter().writeInternal("foo", outputMessage);

		assertEquals("{} && \"foo\"", outputMessage.getBodyAsString(Charset.forName("UTF-8")));
	}

	public void testPrefixJsonCustom() throws Exception {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		getConverter().setJsonPrefix(")]}',");
		getConverter().writeInternal("foo", outputMessage);

		assertEquals(")]}',\"foo\"", outputMessage.getBodyAsString(Charset.forName("UTF-8")));
	}


	public static class PrettyPrintBean {

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}
