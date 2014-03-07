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

package org.springframework.social.connect.sqlite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UsersConnectionRepository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * {@link UsersConnectionRepository} that uses SQLite to persist connection data to a relational database.
 * @author Roy Clarkson
 * @since 1.0
 */
public class SQLiteUsersConnectionRepository implements UsersConnectionRepository {

	private final SQLiteOpenHelper repositoryHelper;
	
	private final ConnectionFactoryLocator connectionFactoryLocator;

	private final TextEncryptor textEncryptor;
	
	private ConnectionSignUp connectionSignUp;

	public SQLiteUsersConnectionRepository(SQLiteOpenHelper repositoryHelper, ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.repositoryHelper = repositoryHelper;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}
	
	/**
	 * The command to execute to create a new local user profile in the event no user id could be mapped to a connection.
	 * Allows for implicitly creating a user profile from connection data during a provider sign-in attempt.
	 * Defaults to null, indicating explicit sign-up will be required to complete the provider sign-in attempt.
	 * @see #findUserIdsWithConnection(Connection)
	 */
	public void setConnectionSignUp(ConnectionSignUp connectionSignUp) {
		this.connectionSignUp = connectionSignUp;
	}
	
	public List<String> findUserIdsWithConnection(Connection<?> connection) {
		ConnectionKey key = connection.getKey();
		final String sql = "select userId from UserConnection where providerId = ? and providerUserId = ?";
		final String[] selectionArgs = {key.getProviderId(), key.getProviderUserId()};
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		Cursor c = db.rawQuery(sql, selectionArgs);
		List<String> localUserIds = new ArrayList<String>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			localUserIds.add(c.getString(c.getColumnIndex("userId")));
			c.moveToNext();
		}
		c.close();
		db.close();

		if (localUserIds.size() == 0 && connectionSignUp != null) {
		    String newUserId = connectionSignUp.execute(connection);
		    if (newUserId != null) {
				createConnectionRepository(newUserId).addConnection(connection);
				return Arrays.asList(newUserId);
			}
		}
		return localUserIds;
	}

	public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
		StringBuilder providerUserIdsCriteriaSql = new StringBuilder();
		providerUserIdsCriteriaSql.append("(");
		List<String> args = new ArrayList<String>(1 + providerUserIds.size());
		args.add(providerId);
		for (Iterator<String> ids = providerUserIds.iterator(); ids.hasNext();) {
			args.add(ids.next());
			providerUserIdsCriteriaSql.append("?");
			if (ids.hasNext()) {
				providerUserIdsCriteriaSql.append(", ");
			}
		}
		providerUserIdsCriteriaSql.append(")");
		
		final String sql = "select userId from UserConnection where providerId = ? and providerUserId in " + providerUserIdsCriteriaSql;
		final String[] selectionArgs = args.toArray(new String[0]);
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		Cursor c = db.rawQuery(sql, selectionArgs);
		final Set<String> userIds = new HashSet<String>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			userIds.add(c.getString(c.getColumnIndex("userId")));
			c.moveToNext();
		}
		c.close();
		db.close();
		return userIds;
	}

	public ConnectionRepository createConnectionRepository(String userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId cannot be null");
		}
		return new SQLiteConnectionRepository(userId, repositoryHelper, connectionFactoryLocator, textEncryptor);
	}

}
