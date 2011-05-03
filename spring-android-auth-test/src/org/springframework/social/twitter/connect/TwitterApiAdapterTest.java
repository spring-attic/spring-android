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
package org.springframework.social.twitter.connect;

import java.util.Date;
import java.util.List;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.twitter.api.DirectMessageOperations;
import org.springframework.social.twitter.api.FriendOperations;
import org.springframework.social.twitter.api.ImageSize;
import org.springframework.social.twitter.api.ListOperations;
import org.springframework.social.twitter.api.SearchOperations;
import org.springframework.social.twitter.api.SuggestionCategory;
import org.springframework.social.twitter.api.TimelineOperations;
import org.springframework.social.twitter.api.TwitterApi;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.social.twitter.api.UserOperations;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class TwitterApiAdapterTest extends AndroidTestCase {

	private TwitterApiAdapter apiAdapter;
	
	private TwitterApi api;
	
	private UserOperationsMock userOperations;
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        
        apiAdapter = new TwitterApiAdapter();
        userOperations = new UserOperationsMock();
        api = new TwitterApiMock(userOperations);
	}
	
    @Override
    public void tearDown() {
    	apiAdapter = null;
    	api = null;
    }
	
	@SmallTest
	public void testFetchProfile() {
		userOperations.setUserProfile(new TwitterProfile(123L, "kdonald", "Keith Donald", "http://twitter.com/kdonald", "http://twitter.com/kdonald/picture", "me", "melbourne, fl", new Date()));
		UserProfile profile = apiAdapter.fetchUserProfile(api);
		assertEquals("Keith Donald", profile.getName());
		assertEquals("Keith", profile.getFirstName());
		assertEquals("Donald", profile.getLastName());
		assertNull(profile.getEmail());
		assertEquals("kdonald", profile.getUsername());
	}

	@SmallTest
	public void testFetchProfileFirstNameOnly() {
		userOperations.setUserProfile(new TwitterProfile(123L, "kdonald", "Keith", "http://twitter.com/kdonald", "http://twitter.com/kdonald/picture", "me", "melbourne, fl", new Date()));
		UserProfile profile = apiAdapter.fetchUserProfile(api);
		assertEquals("Keith", profile.getName());
		assertEquals("Keith", profile.getFirstName());
		assertNull(profile.getLastName());
		assertNull(profile.getEmail());
		assertEquals("kdonald", profile.getUsername());
	}

	@SmallTest
	public void testFetchProfileMiddleName() {
		userOperations.setUserProfile(new TwitterProfile(123L, "kdonald", "Keith Preston Donald", "http://twitter.com/kdonald", "http://twitter.com/kdonald/picture", "me", "melbourne, fl", new Date()));
		UserProfile profile = apiAdapter.fetchUserProfile(api);
		assertEquals("Keith Preston Donald", profile.getName());
		assertEquals("Keith", profile.getFirstName());
		assertEquals("Donald", profile.getLastName());
		assertNull(profile.getEmail());
		assertEquals("kdonald", profile.getUsername());
	}
	
	@SmallTest
	public void testFetchProfileExtraWhitespace() {
		userOperations.setUserProfile(new TwitterProfile(123L, "kdonald", "Keith 	Preston  Donald", "http://twitter.com/kdonald", "http://twitter.com/kdonald/picture", "me", "melbourne, fl", new Date()));
		UserProfile profile = apiAdapter.fetchUserProfile(api);
		assertEquals("Keith 	Preston  Donald", profile.getName());
		assertEquals("Keith", profile.getFirstName());
		assertEquals("Donald", profile.getLastName());
		assertNull(profile.getEmail());
		assertEquals("kdonald", profile.getUsername());
	}
	
	
	private class TwitterApiMock implements TwitterApi {
		
		private UserOperations userOperationsMock;
		
		public TwitterApiMock(UserOperations userOperations) {
			this.userOperationsMock = userOperations;
		}

		@Override
		public boolean isAuthorizedForUser() {
			return false;
		}

		@Override
		public DirectMessageOperations directMessageOperations() {
			return null;
		}

		@Override
		public FriendOperations friendOperations() {
			return null;
		}

		@Override
		public ListOperations listOperations() {
			return null;
		}

		@Override
		public SearchOperations searchOperations() {
			return null;
		}

		@Override
		public TimelineOperations timelineOperations() {
			return null;
		}
		
		@Override
		public UserOperations userOperations() {
			return userOperationsMock;
		}
		
	}
	
	private class UserOperationsMock implements UserOperations {
		
		private TwitterProfile userProfileMock;

		@Override
		public long getProfileId() {
			return 0;
		}

		@Override
		public String getScreenName() {
			return null;
		}

		public void setUserProfile(TwitterProfile twitterProfile) {
			this.userProfileMock = twitterProfile;
		}
		
		@Override
		public TwitterProfile getUserProfile() {
			return userProfileMock;
		}
		
		@Override
		public TwitterProfile getUserProfile(String screenName) {
			return null;
		}

		@Override
		public TwitterProfile getUserProfile(long userId) {
			return null;
		}

		@Override
		public byte[] getUserProfileImage(String screenName) {
			return null;
		}

		@Override
		public byte[] getUserProfileImage(String screenName, ImageSize size) {
			return null;
		}

		@Override
		public List<TwitterProfile> getUsers(long... userIds) {
			return null;
		}

		@Override
		public List<TwitterProfile> getUsers(String... screenNames) {
			return null;
		}

		@Override
		public List<TwitterProfile> searchForUsers(String query) {
			return null;
		}

		@Override
		public List<SuggestionCategory> getSuggestionCategories() {
			return null;
		}

		@Override
		public List<TwitterProfile> getSuggestions(String slug) {
			return null;
		}
		
	}
	
}
