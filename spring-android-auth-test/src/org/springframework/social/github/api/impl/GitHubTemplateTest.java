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
package org.springframework.social.github.api.impl;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.social.test.client.RequestMatchers.header;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.social.github.api.GitHubUserProfile;
import org.springframework.social.test.client.MockRestServiceServer;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Craig Walls
 */
public class GitHubTemplateTest extends AndroidTestCase {

	private GitHubTemplate github;
	private MockRestServiceServer mockServer;
	private HttpHeaders responseHeaders;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		github = new GitHubTemplate("ACCESS_TOKEN");
		mockServer = MockRestServiceServer.createServer(github.getRestTemplate());
		responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
	}
	
	@Override
	public void tearDown() {
		github = null;
		mockServer = null;
		responseHeaders = null;
	}

	@MediumTest
	public void testGetUserProfile() throws Exception {
		mockServer.expect(requestTo("https://github.com/api/v2/json/user/show")).andExpect(method(GET))
				.andExpect(header("Authorization", "Token token=\"ACCESS_TOKEN\""))
				.andRespond(withResponse(new ClassPathResource("profile.json", getClass()), responseHeaders));
		GitHubUserProfile profile = github.getUserProfile();
		assertEquals("habuma", profile.getUsername());
		assertEquals("Craig Walls", profile.getName());
		assertEquals("SpringSource", profile.getCompany());
		assertEquals("http://blog.springsource.com/author/cwalls", profile.getBlog());
		assertEquals("cwalls@vmware.com", profile.getEmail());
		assertEquals(123456, profile.getId());
	}

	@MediumTest
	public void testGetProfileId() {
		mockServer.expect(requestTo("https://github.com/api/v2/json/user/show")).andExpect(method(GET))
				.andExpect(header("Authorization", "Token token=\"ACCESS_TOKEN\""))
				.andRespond(withResponse(new ClassPathResource("profile.json", getClass()), responseHeaders));
		assertEquals("habuma", github.getProfileId());
	}

	@MediumTest
	public void testGetProfileUrl() {
		mockServer.expect(requestTo("https://github.com/api/v2/json/user/show")).andExpect(method(GET))
				.andExpect(header("Authorization", "Token token=\"ACCESS_TOKEN\""))
				.andRespond(withResponse(new ClassPathResource("profile.json", getClass()), responseHeaders));
		assertEquals("https://github.com/habuma", github.getProfileUrl());
	}
}
