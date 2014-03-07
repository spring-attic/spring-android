/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.core.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.Assert;

import android.content.res.AssetManager;

/**
 * {@link Resource} implementation for Android asset resources.
 * 
 * @author Roy Clarkson
 * @since 1.0
 */
public class AssetResource extends AbstractResource {

	private final AssetManager assetManager;

	private final String fileName;


	/**
	 * Create a new AssetResource.
	 * <p>
	 * The Android AssetManager will be used for loading the resource.
	 * @param fileName the name of the asset to open.
	 * @see android.content.res.AssetManager#open(String)
	 */
	public AssetResource(AssetManager assetManager, String fileName) {
		Assert.notNull(assetManager, "assetManager must not be null");
		Assert.notNull(fileName, "fileName must not be null");
		this.assetManager = assetManager;
		this.fileName = fileName;
	}

	/**
	 * This implementation returns whether the underlying asset exists.
	 */
	@Override
	public boolean exists() {
		try {
			InputStream inputStream = this.assetManager.open(this.fileName);
			if (inputStream != null) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * This implementation checks the length of the underlying AssetFileDescriptor, if available.
	 * @see android.content.res.AssetFileDescriptor#getLength()
	 */
	public long contentLength() throws IOException {
		return assetManager.openFd(this.fileName).getLength();
	}

	public String getDescription() {
		return "asset [" + this.fileName + "]";
	}

	public InputStream getInputStream() throws IOException {
		InputStream inputStream = this.assetManager.open(this.fileName);
		if (inputStream == null) {
			throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
		}
		return inputStream;
	}

	/**
	 * This implementation compares the file names of the resources.
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj == this || (obj instanceof AssetResource && this.fileName.equals(((AssetResource) obj).fileName)));
	}

	/**
	 * This implementation returns the hash code of the file name.
	 */
	@Override
	public int hashCode() {
		return this.fileName.hashCode();
	}
}
