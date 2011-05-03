/*
 * Copyright 2011 the original author or authors.
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
package org.springframework.social.facebook.api;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.test.client.MockRestServiceServer;

import android.test.AndroidTestCase;

public class AbstractFacebookApiTest extends AndroidTestCase {
	protected static final String ACCESS_TOKEN = "someAccessToken";

	protected FacebookTemplate facebook;
	protected MockRestServiceServer mockServer;
	protected HttpHeaders responseHeaders;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		facebook = new FacebookTemplate(ACCESS_TOKEN);
		mockServer = MockRestServiceServer.createServer(facebook.getRestTemplate());
		responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(new MediaType("text", "javascript"));
	}
	
	@Override
	public void tearDown() {
		facebook = null;
		mockServer = null;
		responseHeaders = null;
	}


	private static final DateFormat FB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

	protected Date toDate(String dateString) {
		try {
			return FB_DATE_FORMAT.parse(dateString);
		} catch (ParseException e) {
			System.out.println(e);
			return null;
		}
	}

}
