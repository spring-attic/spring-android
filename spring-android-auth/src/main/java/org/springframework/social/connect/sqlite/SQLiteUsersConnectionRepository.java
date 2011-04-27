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
package org.springframework.social.connect.sqlite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * {@link MultiUserServiceProviderConnectionRepository} that uses the SQLite to persist connection data to a relational database.
 * 
 * @author Roy Clarkson
 */
public class SQLiteUsersConnectionRepository implements UsersConnectionRepository {

	private final SQLiteOpenHelper repositoryHelper;
	
	private final ConnectionFactoryLocator connectionFactoryLocator;

	private final TextEncryptor textEncryptor;

	public SQLiteUsersConnectionRepository(SQLiteOpenHelper repositoryHelper, ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.repositoryHelper = repositoryHelper;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}

	public String findUserIdWithConnection(Connection<?> connection) {
		final String sql = "select userId from UserConnection where providerId = ? and providerUserId = ?";
		ConnectionKey key = connection.getKey();
		final String[] selectionArgs = {key.getProviderId(), key.getProviderUserId()};		
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		Cursor c = db.rawQuery(sql, selectionArgs);		
		String userId = null;
		if (c.getCount() == 1) {
			c.moveToFirst();
			userId = c.getString(c.getColumnIndex("userId"));
		} 
		c.close();
		db.close();
		return userId;
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
		return new SQLiteConnectionRepository(userId, repositoryHelper, connectionFactoryLocator, textEncryptor);
	}
}
