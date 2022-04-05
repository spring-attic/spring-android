/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.crypto.encrypt;

import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.AndroidKeyGenerators;

/**
 * Factory for commonly used encryptors. Defines the public API for constructing {@link BytesEncryptor} and
 * {@link TextEncryptor} implementations.
 * 
 * @author Keith Donald
 * @since 1.0
 */
public class AndroidEncryptors {

	/**
	 * Creates a standard password-based bytes encryptor using 256 bit AES encryption. Derives the secret key using PKCS
	 * #5's PBKDF2 (Password-Based Key Derivation Function #2). Salts the password to prevent dictionary attacks against
	 * the key. The provided salt is expected to be hex-encoded; it should be random and at least 8 bytes in length.
	 * Also applies a random 16 byte initialization vector to ensure each encrypted message will be unique. Requires
	 * Java 6.
	 * 
	 * @param password the password used to generate the encryptor's secret key; should not be shared
	 * @param salt a hex-encoded, random, site-global salt value to use to generate the key
	 */
	public static BytesEncryptor standard(CharSequence password, CharSequence salt) {
		return new AndroidAesBytesEncryptor(password.toString(), salt, AndroidKeyGenerators.secureRandom(16));
	}

	/**
	 * Creates a text encryptor that uses standard password-based encryption. Encrypted text is hex-encoded.
	 * 
	 * @param password the password used to generate the encryptor's secret key; should not be shared
	 */
	public static TextEncryptor text(CharSequence password, CharSequence salt) {
		return new HexEncodingTextEncryptor(standard(password, salt));
	}

	/**
	 * Creates an encryptor for queryable text strings that uses standard password-based encryption. Uses a shared, or
	 * constant 16 byte initialization vector so encrypting the same data results in the same encryption result. This is
	 * done to allow encrypted data to be queried against. Encrypted text is hex-encoded.
	 * 
	 * @param password the password used to generate the encryptor's secret key; should not be shared
	 * @param salt a hex-encoded, random, site-global salt value to use to generate the secret key
	 */
	public static TextEncryptor queryableText(CharSequence password, CharSequence salt) {
		return new HexEncodingTextEncryptor(new AndroidAesBytesEncryptor(password.toString(), salt, AndroidKeyGenerators.shared(16)));
	}

	/**
	 * Creates a text encryptor that performs no encryption. Useful for developer testing environments where working
	 * with plain text strings is desired for simplicity.
	 */
	public static TextEncryptor noOpText() {
		return NO_OP_TEXT_INSTANCE;
	}

	private AndroidEncryptors() {
	}

	private static final TextEncryptor NO_OP_TEXT_INSTANCE = new NoOpTextEncryptor();

	private static final class NoOpTextEncryptor implements TextEncryptor {

		public String encrypt(String text) {
			return text;
		}

		public String decrypt(String encryptedText) {
			return encryptedText;
		}

	}

}
