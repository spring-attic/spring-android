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
package org.springframework.social.github.connect;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.github.api.GitHubApi;
import org.springframework.social.github.api.GitHubUserProfile;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

public class GitHubServiceApiAdapterTest extends AndroidTestCase {

	private GitHubApiAdapter apiAdapter;
	
	private GitHubApi serviceApi;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		apiAdapter = new GitHubApiAdapter();
		serviceApi = new GitHubApiMock();
	}
	
	@Override
	public void tearDown() {
		apiAdapter = null;
		serviceApi = null;
	}
	
	@MediumTest
	public void testFetchProfile() {		
		((GitHubApiMock) serviceApi).setUserProfile(new GitHubUserProfile(123456L, "habuma", "Craig Walls", "Plano, TX", "SpringSource", null, "cwalls@vmware.com", null, null));
		UserProfile profile = apiAdapter.fetchUserProfile(serviceApi);
		assertEquals("Craig Walls", profile.getName());
		assertEquals("Craig", profile.getFirstName());
		assertEquals("Walls", profile.getLastName());
		assertEquals("cwalls@vmware.com", profile.getEmail());
		assertEquals("habuma", profile.getUsername());
	}
	
	@MediumTest
	public void testFetchProfileFirstNameOnly() {
		((GitHubApiMock) serviceApi).setUserProfile(new GitHubUserProfile(123456L, "habuma", "Craig", "Plano, TX", "SpringSource", null, "cwalls@vmware.com", null, null));
		UserProfile profile = apiAdapter.fetchUserProfile(serviceApi);
		assertEquals("Craig", profile.getName());
		assertEquals("Craig", profile.getFirstName());
		assertNull(profile.getLastName());
		assertEquals("cwalls@vmware.com", profile.getEmail());
		assertEquals("habuma", profile.getUsername());
	}

	@MediumTest
	public void testFetchProfileMiddleName() {
		((GitHubApiMock) serviceApi).setUserProfile(new GitHubUserProfile(123456L, "habuma", "Michael Craig Walls", "Plano, TX", "SpringSource", null, "cwalls@vmware.com", null, null));
		UserProfile profile = apiAdapter.fetchUserProfile(serviceApi);
		assertEquals("Michael Craig Walls", profile.getName());
		assertEquals("Michael", profile.getFirstName());
		assertEquals("Walls", profile.getLastName());
		assertEquals("cwalls@vmware.com", profile.getEmail());
		assertEquals("habuma", profile.getUsername());
	}
	
	@MediumTest
	public void testFetchProfileExtraWhitespace() {
		((GitHubApiMock) serviceApi).setUserProfile(new GitHubUserProfile(123456L, "habuma", "Michael    Craig Walls", "Plano, TX", "SpringSource", null, "cwalls@vmware.com", null, null));
		UserProfile profile = apiAdapter.fetchUserProfile(serviceApi);
		assertEquals("Michael    Craig Walls", profile.getName());
		assertEquals("Michael", profile.getFirstName());
		assertEquals("Walls", profile.getLastName());
		assertEquals("cwalls@vmware.com", profile.getEmail());
		assertEquals("habuma", profile.getUsername());
	}

	
	private class GitHubApiMock implements GitHubApi {
		
		private GitHubUserProfile userProfileMock;

		@Override
		public String getProfileId() {
			return null;
		}
		
		public void setUserProfile(GitHubUserProfile userProfile) {
			this.userProfileMock = userProfile;
		}

		@Override
		public GitHubUserProfile getUserProfile() {
			return userProfileMock;
		}

		@Override
		public String getProfileUrl() {
			return null;
		}
		
	}
}
