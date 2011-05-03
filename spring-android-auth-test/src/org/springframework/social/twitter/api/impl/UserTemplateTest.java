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
package org.springframework.social.twitter.api.impl;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.social.test.client.RequestMatchers.method;
import static org.springframework.social.test.client.RequestMatchers.requestTo;
import static org.springframework.social.test.client.ResponseCreators.withResponse;

import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.social.twitter.api.SuggestionCategory;
import org.springframework.social.twitter.api.TwitterProfile;

import android.test.suitebuilder.annotation.MediumTest;

/**
 * @author Craig Walls
 */
public class UserTemplateTest extends AbstractTwitterApiTest {

	@MediumTest
	public void testGetProfileId() {
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		mockServer.expect(requestTo("https://api.twitter.com/1/account/verify_credentials.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("verify-credentials.json", getClass()), responseHeaders));
		assertEquals(161064614, twitter.userOperations().getProfileId());
	}

	@MediumTest
	public void testGetScreenName() {
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		mockServer.expect(requestTo("https://api.twitter.com/1/account/verify_credentials.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("verify-credentials.json", getClass()), responseHeaders));
		assertEquals("artnames", twitter.userOperations().getScreenName());
	}

	@MediumTest
	public void testGetUserProfile() throws Exception {
		mockServer.expect(requestTo("https://api.twitter.com/1/account/verify_credentials.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("verify-credentials.json", getClass()), responseHeaders));

		TwitterProfile profile = twitter.userOperations().getUserProfile();
		assertEquals(161064614, profile.getId());
		assertEquals("artnames", profile.getScreenName());
		assertEquals("Art Names", profile.getName());
		assertEquals("I'm just a normal kinda guy", profile.getDescription());
		assertEquals("Denton, TX", profile.getLocation());
		assertEquals("http://www.springsource.org", profile.getUrl());
		assertEquals("http://a1.twimg.com/sticky/default_profile_images/default_profile_4_normal.png", profile.getProfileImageUrl());
	}
	
	@MediumTest
	public void testGetUserProfile_userId() throws Exception {
		mockServer.expect(requestTo("https://api.twitter.com/1/users/show.json?user_id=12345"))
				.andExpect(method(GET))
				.andRespond(withResponse(new ClassPathResource("twitter-profile.json", getClass()), responseHeaders));

		TwitterProfile profile = twitter.userOperations().getUserProfile(12345);
		assertEquals(12345, profile.getId());
		assertEquals("habuma", profile.getScreenName());
		assertEquals("Craig Walls", profile.getName());
		assertEquals("Spring Guy", profile.getDescription());
		assertEquals("Plano, TX", profile.getLocation());
		assertEquals("http://www.springsource.org", profile.getUrl());
		assertEquals("http://a3.twimg.com/profile_images/1205746571/me2_300.jpg", profile.getProfileImageUrl());
	}
	
	@MediumTest
	public void testGetUsers_byUserId() {
		mockServer.expect(requestTo("https://api.twitter.com/1/users/lookup.json?user_id=14846645%2C14718006"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-profiles.json", getClass()), responseHeaders));
		List<TwitterProfile> users = twitter.userOperations().getUsers(14846645, 14718006);
		assertEquals(2, users.size());
		assertEquals("royclarkson", users.get(0).getScreenName());
		assertEquals("kdonald", users.get(1).getScreenName());
	}
	
	@MediumTest
	public void testGetUsers_byScreenName() {
		mockServer.expect(requestTo("https://api.twitter.com/1/users/lookup.json?screen_name=royclarkson%2Ckdonald"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-profiles.json", getClass()), responseHeaders));
		List<TwitterProfile> users = twitter.userOperations().getUsers("royclarkson", "kdonald");
		assertEquals(2, users.size());
		assertEquals("royclarkson", users.get(0).getScreenName());
		assertEquals("kdonald", users.get(1).getScreenName());
	}
	
	@MediumTest
	public void testSearchForUsers() {
		mockServer.expect(requestTo("https://api.twitter.com/1/users/search.json?q=some+query"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("list-of-profiles.json", getClass()), responseHeaders));
		List<TwitterProfile> users = twitter.userOperations().searchForUsers("some query");
		assertEquals(2, users.size());
		assertEquals("royclarkson", users.get(0).getScreenName());
		assertEquals("kdonald", users.get(1).getScreenName());
	}
	
	@MediumTest
	public void testGetSuggestionCategories() {
		mockServer.expect(requestTo("https://api.twitter.com/1/users/suggestions.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("suggestion-categories.json", getClass()), responseHeaders));
		List<SuggestionCategory> categories = twitter.userOperations().getSuggestionCategories();
		assertEquals(4, categories.size());
		assertEquals("Art & Design", categories.get(0).getName());
		assertEquals("art-design", categories.get(0).getSlug());
		assertEquals(56, categories.get(0).getSize());
		assertEquals("Books", categories.get(1).getName());
		assertEquals("books", categories.get(1).getSlug());
		assertEquals(72, categories.get(1).getSize());
		assertEquals("Business", categories.get(2).getName());
		assertEquals("business", categories.get(2).getSlug());
		assertEquals(65, categories.get(2).getSize());
		assertEquals("Twitter", categories.get(3).getName());
		assertEquals("twitter", categories.get(3).getSlug());
		assertEquals(16, categories.get(3).getSize());
	}
	
	@MediumTest
	public void testGetSuggestions() {
		mockServer.expect(requestTo("https://api.twitter.com/1/users/suggestions/springsource.json"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("suggestions.json", getClass()), responseHeaders));

		List<TwitterProfile> users = twitter.userOperations().getSuggestions("springsource");
		assertEquals(2, users.size());
		assertEquals("royclarkson", users.get(0).getScreenName());
		assertEquals("kdonald", users.get(1).getScreenName());
	}
}
