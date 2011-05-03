/*
 * Copyright 2010 the original author or authors.
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
package org.springframework.social.gowalla.api.impl;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.social.test.client.RequestMatchers.header;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.social.gowalla.api.Checkin;
import org.springframework.social.test.client.MockRestServiceServer;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Craig Walls
 */
public class GowallaTemplateTest extends AndroidTestCase {

	private GowallaTemplate gowalla;
	private MockRestServiceServer mockServer;
	private HttpHeaders responseHeaders;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		gowalla = new GowallaTemplate("ACCESS_TOKEN");
		mockServer = MockRestServiceServer.createServer(gowalla.getRestTemplate());
		responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
	}
	
	@Override 
	public void tearDown() {
		gowalla = null;
		mockServer = null;
		responseHeaders = null;
	}

	@MediumTest
	public void testGetProfileId() {
		mockServer.expect(requestTo("https://api.gowalla.com/users/me")).andExpect(method(GET))
				.andExpect(header("Authorization", "Token token=\"ACCESS_TOKEN\""))
				.andRespond(withResponse("{\"username\":\"habuma\",\"pins_count\":17,\"stamps_count\":2}", responseHeaders));
		assertEquals("habuma", gowalla.getProfileId());
	}

	@MediumTest
	public void testGetProfileUrl() {
		mockServer.expect(requestTo("https://api.gowalla.com/users/me")).andExpect(method(GET))
				.andExpect(header("Authorization", "Token token=\"ACCESS_TOKEN\""))
				.andRespond(withResponse("{\"username\":\"habuma\",\"pins_count\":17,\"stamps_count\":2}", responseHeaders));
		assertEquals("http://www.gowalla.com/users/habuma", gowalla.getProfileUrl());
	}

	@MediumTest
	public void testGetTopCheckins() {
		mockServer.expect(requestTo("https://api.gowalla.com/users/habuma/top_spots")).andExpect(method(GET))
				.andExpect(header("Authorization", "Token token=\"ACCESS_TOKEN\""))
				.andRespond(withResponse(new ClassPathResource("top_spots.json", getClass()), responseHeaders));

		List<Checkin> checkins = gowalla.getTopCheckins("habuma");
		assertEquals(3, checkins.size());
		assertEquals("Burrito Shack", checkins.get(0).getName());
		assertEquals(100, checkins.get(0).getCount());
		assertEquals("Chicken Hut", checkins.get(1).getName());
		assertEquals(25, checkins.get(1).getCount());
		assertEquals("Burger Bell", checkins.get(2).getName());
		assertEquals(3, checkins.get(2).getCount());
	}
}
