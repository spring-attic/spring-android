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
package org.springframework.social.linkedin.connect;

import java.util.List;

import org.springframework.social.connect.UserProfile;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.linkedin.api.LinkedInProfile;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class LinkedInAdapterTest extends AndroidTestCase {

	private LinkedInAdapter apiAdapter = new LinkedInAdapter();
	
	private LinkedIn linkedIn = new LinkedInMock();
	
	@SmallTest
	public void testFetchProfile() {
		UserProfile profile = apiAdapter.fetchUserProfile(linkedIn);
		assertEquals("Craig Walls", profile.getName());
		assertEquals("Craig", profile.getFirstName());
		assertEquals("Walls", profile.getLastName());
		assertNull(profile.getEmail());
		assertNull(profile.getUsername());
	}
	
	private class LinkedInMock implements LinkedIn {

		@Override
		public String getProfileId() {
			return null;
		}

		@Override
		public String getProfileUrl() {
			return null;
		}

		@Override
		public LinkedInProfile getUserProfile() {
			return new LinkedInProfile("50A3nOf73z", "Craig", "Walls", "Spring Guy", "Software", "http://www.linkedin.com/in/habuma", "http://www.linkedin.com/profile?viewProfile=&key=3630172&authToken=0IpZ&authType=name&trk=api*a121026*s129482*", "http://media.linkedin.com/mpr/mprx/0_9-Hjc8b0ViE1gGElNtdCcGh0s3pjxbRlNzpCciT05XHD8i2Asq4AM_zAN7yGp8VgcAoi4k1faewD");
		}

		@Override
		public List<LinkedInProfile> getConnections() {
			return null;
		}
		
	}

}
