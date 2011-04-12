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
		if (c.getCount() > 0) {
			c.moveToFirst();
			localUserId = c.getString(c.getColumnIndex("localUserId"));
		}
		c.close();
		db.close();
		return localUserId;
	}

	public Set<String> findLocalUserIdsConnectedTo(String providerId, List<String> providerUserIds) {
//		MapSqlParameterSource parameters = new MapSqlParameterSource();
//		parameters.addValue("providerId", providerId);
//		parameters.addValue("providerUserIds", providerUserIds);
//		final Set<String> localUserIds = new HashSet<String>();
//		return new NamedParameterJdbcTemplate(jdbcTemplate).query("select localUserId from ServiceProviderConnection where providerId = :providerId and providerUserId in (:providerUserIds)", parameters,
//			new ResultSetExtractor<Set<String>>() {
//				public Set<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
//					while (rs.next()) {
//						localUserIds.add(rs.getString("localUserId"));
//					}
//					return localUserIds;
//				}
//			});
		
		// TODO: finish
		return null;
	}

	public ServiceProviderConnectionRepository createConnectionRepository(String localUserId) {
		return new SqliteServiceProviderConnectionRepository(localUserId, repositoryHelper, connectionFactoryLocator, textEncryptor);
	}

}
