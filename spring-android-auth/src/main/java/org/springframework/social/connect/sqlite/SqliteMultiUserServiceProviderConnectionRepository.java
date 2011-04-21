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
import org.springframework.social.connect.MultiUserServiceProviderConnectionRepository;
import org.springframework.social.connect.ServiceProviderConnectionFactoryLocator;
import org.springframework.social.connect.ServiceProviderConnectionKey;
import org.springframework.social.connect.ServiceProviderConnectionRepository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Roy Clarkson
 */
public class SqliteMultiUserServiceProviderConnectionRepository implements MultiUserServiceProviderConnectionRepository {

	private final SQLiteOpenHelper repositoryHelper;
	
	private final ServiceProviderConnectionFactoryLocator connectionFactoryLocator;

	private final TextEncryptor textEncryptor;

	public SqliteMultiUserServiceProviderConnectionRepository(SQLiteOpenHelper repositoryHelper, ServiceProviderConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
		this.repositoryHelper = repositoryHelper;
		this.connectionFactoryLocator = connectionFactoryLocator;
		this.textEncryptor = textEncryptor;
	}

	public String findLocalUserIdConnectedTo(ServiceProviderConnectionKey connectionKey) {
		final String sql = "select localUserId from ServiceProviderConnection where providerId = ? and providerUserId = ?";
		final String[] selectionArgs = {connectionKey.getProviderId(), connectionKey.getProviderUserId()};		
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		Cursor c = db.rawQuery(sql, selectionArgs);		
		String localUserId = null;
		if (c.getCount() == 1) {
			c.moveToFirst();
			localUserId = c.getString(c.getColumnIndex("localUserId"));
		} 
		c.close();
		db.close();
		return localUserId;
	}

	public Set<String> findLocalUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
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
		
		final String sql = "select localUserId from ServiceProviderConnection where providerId = ? and providerUserId in " + providerUserIdsCriteriaSql;
		final String[] selectionArgs = args.toArray(new String[0]);
		SQLiteDatabase db = repositoryHelper.getReadableDatabase();
		Cursor c = db.rawQuery(sql, selectionArgs);
		final Set<String> localUserIds = new HashSet<String>();
		c.moveToFirst();
		for (int i = 0; i < c.getCount(); i++) {
			localUserIds.add(c.getString(c.getColumnIndex("localUserId")));
			c.moveToNext();
		}
		c.close();
		db.close();
		return localUserIds;
	}

	public ServiceProviderConnectionRepository createConnectionRepository(String localUserId) {
		return new SqliteServiceProviderConnectionRepository(localUserId, repositoryHelper, connectionFactoryLocator, textEncryptor);
	}
}
