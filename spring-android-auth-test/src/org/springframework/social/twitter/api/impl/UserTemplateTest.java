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

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.twitter.api.ImageSize;
import org.springframework.social.twitter.api.SuggestionCategory;
import org.springframework.social.twitter.api.TwitterProfile;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Craig Walls
 */
public class UserTemplateTest extends AbstractTwitterApiTest {

	@MediumTest
	public void testGetProfileId() {
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		mockServer.expect(requestTo("https://api.twitter.com/1/account/verify_credentials.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(jsonResource("twitter-profile"), responseHeaders));
		assertEquals(161064614, twitter.userOperations().getProfileId());
	}
	
	@SmallTest
	public void testGetProfileId_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.userOperations().getProfileId();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetScreenName() {
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		mockServer.expect(requestTo("https://api.twitter.com/1/account/verify_credentials.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(jsonResource("twitter-profile"), responseHeaders));
		assertEquals("artnames", twitter.userOperations().getScreenName());
	}
	
	@SmallTest
	public void testGetScreenName_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.userOperations().getScreenName();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}

	@MediumTest
	public void testGetUserProfile() throws Exception {
		mockServer.expect(requestTo("https://api.twitter.com/1/account/verify_credentials.json"))
				.andExpect(method(GET))
				.andRespond(withResponse(jsonResource("twitter-profile"), responseHeaders));

		TwitterProfile profile = twitter.userOperations().getUserProfile();
		assertEquals(161064614, profile.getId());
		assertEquals("artnames", profile.getScreenName());
		assertEquals("Art Names", profile.getName());
		assertEquals("I'm just a normal kinda guy", profile.getDescription());
		assertEquals("Denton, TX", profile.getLocation());
		assertEquals("http://www.springsource.org", profile.getUrl());
		assertEquals("http://a1.twimg.com/sticky/default_profile_images/default_profile_4_normal.png", profile.getProfileImageUrl());
		assertTrue(profile.isNotificationsEnabled());
		assertFalse(profile.isVerified());
		assertTrue(profile.isGeoEnabled());
		assertTrue(profile.isContributorsEnabled());
		assertTrue(profile.isTranslator());
		assertTrue(profile.isFollowing());
		assertTrue(profile.isFollowRequestSent());
		assertTrue(profile.isProtected());
		assertEquals("en", profile.getLanguage());
		assertEquals(125, profile.getStatusesCount());
		assertEquals(1001, profile.getListedCount());
		assertEquals(14, profile.getFollowersCount());
		assertEquals(194, profile.getFriendsCount());
		assertEquals(4, profile.getFavoritesCount());
		assertEquals("Mountain Time (US & Canada)", profile.getTimeZone());
		assertEquals(-25200, profile.getUtcOffset());
		assertTrue(profile.useBackgroundImage());
		assertEquals("C0DEED", profile.getSidebarBorderColor());
		assertEquals("DDEEF6", profile.getSidebarFillColor());
		assertEquals("C0DEED", profile.getBackgroundColor());
		assertEquals("http://a3.twimg.com/a/1301419075/images/themes/theme1/bg.png", profile.getBackgroundImageUrl());
		assertFalse(profile.isBackgroundImageTiled());
		assertEquals("333333", profile.getTextColor());
		assertEquals("0084B4", profile.getLinkColor());
	}
	
	@SmallTest
	public void testGetUserProfile_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.userOperations().getUserProfile();
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
	}
	
	@MediumTest
	public void testGetUserProfile_userId() throws Exception {
		mockServer.expect(requestTo("https://api.twitter.com/1/users/show.json?user_id=12345"))
				.andExpect(method(GET))
				.andRespond(withResponse(jsonResource("twitter-profile"), responseHeaders));
		
		TwitterProfile profile = twitter.userOperations().getUserProfile(12345);
		assertEquals(161064614, profile.getId());
		assertEquals("artnames", profile.getScreenName());
		assertEquals("Art Names", profile.getName());
		assertEquals("I'm just a normal kinda guy", profile.getDescription());
		assertEquals("Denton, TX", profile.getLocation());
		assertEquals("http://www.springsource.org", profile.getUrl());
		assertEquals("http://a1.twimg.com/sticky/default_profile_images/default_profile_4_normal.png", profile.getProfileImageUrl());
	}
	
	@MediumTest
	public void testGetUserProfileImage() throws Exception {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.IMAGE_JPEG);
		mockServer.expect(requestTo("https://api.twitter.com/1/users/profile_image/tinyrod?size=normal"))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("tinyrod.jpg", getClass()), responseHeaders));
		
		twitter.userOperations().getUserProfileImage("tinyrod");
		// TODO: Fix ResponseCreators to handle binary data so that we can assert the contents/size of the image bytes. 
	}

	@MediumTest
	public void testGetUserProfileImage_mini() throws Exception {
		getUserProfileImageBySize(ImageSize.MINI);
	}

	@MediumTest
	public void testGetUserProfileImage_normal() throws Exception {
		getUserProfileImageBySize(ImageSize.NORMAL);
	}

	@MediumTest
	public void testGetUserProfileImage_original() throws Exception {
		getUserProfileImageBySize(ImageSize.ORIGINAL);
	}

	@MediumTest
	public void testGetUserProfileImage_bigger() throws Exception {
		getUserProfileImageBySize(ImageSize.BIGGER);
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
		mockServer.expect(requestTo("https://api.twitter.com/1/users/search.json?page=1&per_page=20&q=some+query"))
			.andExpect(method(GET))
			.andRespond(withResponse(jsonResource("list-of-profiles"), responseHeaders));
		List<TwitterProfile> users = twitter.userOperations().searchForUsers("some query");
		assertEquals(2, users.size());
		assertEquals("royclarkson", users.get(0).getScreenName());
		assertEquals("kdonald", users.get(1).getScreenName());
	}
	
	@MediumTest
	public void testSearchForUsers_paged() {
		mockServer.expect(requestTo("https://api.twitter.com/1/users/search.json?page=3&per_page=35&q=some+query"))
			.andExpect(method(GET))
			.andRespond(withResponse(jsonResource("list-of-profiles"), responseHeaders));
		List<TwitterProfile> users = twitter.userOperations().searchForUsers("some query", 3, 35);
		assertEquals(2, users.size());
		assertEquals("royclarkson", users.get(0).getScreenName());
		assertEquals("kdonald", users.get(1).getScreenName());
	}
	
	@SmallTest
	public void testSearchForUsers_unauthorized() {
		boolean success = false;
		try {
			unauthorizedTwitter.userOperations().searchForUsers("some query");
		} catch (NotAuthorizedException e) {
			success = true;
		}
		assertTrue("Expected NotAuthorizedException", success);
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
	
	private void getUserProfileImageBySize(ImageSize imageSize) throws IOException {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.IMAGE_JPEG);
		mockServer.expect(requestTo("https://api.twitter.com/1/users/profile_image/habuma?size=" + imageSize.name().toLowerCase()))
			.andExpect(method(GET))
			.andRespond(withResponse(new ClassPathResource("tinyrod.jpg", getClass()), responseHeaders));
		twitter.userOperations().getUserProfileImage("habuma", imageSize);
		// TODO: Fix ResponseCreators to handle binary data so that we can assert the contents/size of the image bytes. 
	}
}
