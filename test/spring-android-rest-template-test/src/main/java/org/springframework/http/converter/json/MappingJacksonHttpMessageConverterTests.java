/*
 * Copyright 2002-2013 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.JavaType;

/**
 * @author Roy Clarkson
 */
public class MappingJacksonHttpMessageConverterTests extends AbstractMappingJacksonHttpMessageConverterTests {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.converter = new MappingJacksonHttpMessageConverter();
	}

	protected void prepareReadGenericsTest() {
		converter = new MappingJacksonHttpMessageConverter() {
			@Override
			protected JavaType getJavaType(Class<?> clazz) {
				if (List.class.isAssignableFrom(clazz)) {
					return getObjectMapper().getTypeFactory().constructCollectionType(ArrayList.class, MyBean.class);
				} else {
					return super.getJavaType(clazz);
				}
			}
		};
	}

}
