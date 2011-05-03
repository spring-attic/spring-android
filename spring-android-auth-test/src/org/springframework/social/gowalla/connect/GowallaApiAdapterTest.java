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
package org.springframework.social.gowalla.connect;

import java.util.List;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.gowalla.api.Checkin;
import org.springframework.social.gowalla.api.GowallaApi;
import org.springframework.social.gowalla.api.GowallaProfile;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class GowallaApiAdapterTest extends AndroidTestCase {

	private GowallaApiAdapter apiAdapter;
	
	private GowallaApi api;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		apiAdapter = new GowallaApiAdapter();
		api = new GowallaApiMock();
	}
	
	@Override
	public void tearDown() {
		apiAdapter = null;
		api = null;
	}
	
	@SmallTest
	public void testFetchProfile() {		
		UserProfile profile = apiAdapter.fetchUserProfile(api);
		assertEquals("Craig Walls", profile.getName());
		assertEquals("Craig", profile.getFirstName());
		assertEquals("Walls", profile.getLastName());
		assertNull(profile.getEmail());
		assertEquals("habuma", profile.getUsername());
	}
	
	private class GowallaApiMock implements GowallaApi {
		
		@Override
		public String getProfileId() {
			return null;
		}

		@Override
		public String getProfileUrl() {
			return null;
		}

		@Override
		public GowallaProfile getUserProfile() {
			return new GowallaProfile("habuma", "Craig", "Walls", "Plano, TX", 1, 2, "http://s3.amazonaws.com/static.gowalla.com/users/362641-standard.jpg?1294162106");
		}

		@Override
		public GowallaProfile getUserProfile(String userId) {
			return null;
		}

		@Override
		public List<Checkin> getTopCheckins(String userId) {
			return null;
		}
		
	}
}
