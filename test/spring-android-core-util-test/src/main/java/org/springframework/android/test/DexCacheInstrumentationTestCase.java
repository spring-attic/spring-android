/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.android.test;

import android.os.Build;
import android.test.InstrumentationTestCase;

/**
 * There is an <a href="https://code.google.com/p/dexmaker/issues/detail?id=2">issue with
 * dexmaker</a> on Android 4.3 and newer.
 * 
 * <p>
 * This workaround forces Android to create the cache directory so dexmaker can use it.
 * 
 * @author Roy Clarkson
 */
public class DexCacheInstrumentationTestCase extends InstrumentationTestCase {

	@Override
	public void setUp() throws Exception {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
		}
	}

}
