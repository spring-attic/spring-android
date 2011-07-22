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
package org.springframework.social.facebook.connect;

import java.util.List;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.facebook.api.CommentOperations;
import org.springframework.social.facebook.api.EventOperations;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.social.facebook.api.FeedOperations;
import org.springframework.social.facebook.api.FriendOperations;
import org.springframework.social.facebook.api.GroupOperations;
import org.springframework.social.facebook.api.ImageType;
import org.springframework.social.facebook.api.LikeOperations;
import org.springframework.social.facebook.api.MediaOperations;
import org.springframework.social.facebook.api.PageOperations;
import org.springframework.social.facebook.api.PlacesOperations;
import org.springframework.social.facebook.api.Reference;
import org.springframework.social.facebook.api.UserOperations;
import org.springframework.util.MultiValueMap;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class FacebookAdapterTest extends AndroidTestCase {

	private FacebookAdapter apiAdapter;
	
	private Facebook facebook;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.apiAdapter = new FacebookAdapter();
		this.facebook = new FacebookMock();
	}
	
	@Override
	public void tearDown() {
		apiAdapter = null;
		facebook = null;
	}
	
	@SmallTest
	public void testFetchProfile() {
		UserProfile profile = apiAdapter.fetchUserProfile(facebook);
		assertEquals("Craig Walls", profile.getName());
		assertEquals("Craig", profile.getFirstName());
		assertEquals("Walls", profile.getLastName());
		assertNull(profile.getEmail());
		assertEquals("habuma", profile.getUsername());
	}
	
	
	private class FacebookMock implements Facebook {
		
		UserOperationsMock userOperationsMock = new UserOperationsMock();

		@Override
		public <T> T fetchObject(String objectId, Class<T> type) {
			return null;
		}

		@Override
		public <T> T fetchObject(String objectId, Class<T> type,
				MultiValueMap<String, String> queryParameters) {
			return null;
		}
		
		@Override
		public <T> List<T> fetchConnections(String objectId, String connectionType, Class<T> type, String... fields) {
			return null;
		}

		@Override
		public <T> List<T> fetchConnections(String objectId, String connectionType, Class<T> type,
				MultiValueMap<String, String> queryParameters) {
			return null;
		}

		@Override
		public byte[] fetchImage(String objectId, String connectionType,
				ImageType imageType) {
			return null;
		}

		@Override
		public String publish(String objectId, String connectionType,
				MultiValueMap<String, Object> data) {
			return null;
		}

		@Override
		public void post(String objectId, String connectionType,
				MultiValueMap<String, String> data) {
			
		}

		@Override
		public void delete(String objectId) {
			
		}

		@Override
		public void delete(String objectId, String connectionType) {
			
		}

		@Override
		public CommentOperations commentOperations() {
			return null;
		}

		@Override
		public EventOperations eventOperations() {
			return null;
		}

		@Override
		public FeedOperations feedOperations() {
			return null;
		}

		@Override
		public FriendOperations friendOperations() {
			return null;
		}

		@Override
		public GroupOperations groupOperations() {
			return null;
		}

		@Override
		public LikeOperations likeOperations() {
			return null;
		}

		@Override
		public MediaOperations mediaOperations() {
			return null;
		}

		@Override
		public PageOperations pageOperations() {
			return null;
		}

		@Override
		public PlacesOperations placesOperations() {
			return null;
		}

		@Override
		public UserOperations userOperations() {
			return userOperationsMock;
		}

		@Override
		public boolean isAuthorized() {
			return false;
		}
		
	}
	
	private class UserOperationsMock implements UserOperations {

		@Override
		public FacebookProfile getUserProfile() {
			return new FacebookProfile("12345678", "habuma", "Craig Walls", "Craig", "Walls", null, null);
		}

		@Override
		public FacebookProfile getUserProfile(String userId) {
			return null;
		}

		@Override
		public byte[] getUserProfileImage() {
			return null;
		}

		@Override
		public byte[] getUserProfileImage(String userId) {
			return null;
		}

		@Override
		public byte[] getUserProfileImage(ImageType imageType) {
			return null;
		}

		@Override
		public byte[] getUserProfileImage(String userId, ImageType imageType) {
			return null;
		}

		@Override
		public List<String> getUserPermissions() {
			return null;
		}

		@Override
		public List<Reference> search(String query) {
			return null;
		}
		
	}
}
