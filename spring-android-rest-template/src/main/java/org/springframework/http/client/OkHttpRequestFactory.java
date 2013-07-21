/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.http.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.util.Assert;

import com.squareup.okhttp.OkHttpClient;

/**
 * {@link ClientHttpRequestFactory} implementation that uses standard J2SE facilities.
 * @author Arjen Poutsma
 * @author Roy Clarkson
 * @see java.net.HttpURLConnection
 * @since 1.0
 */
public class OkHttpRequestFactory extends SimpleClientHttpRequestFactory {

    private OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    protected HttpURLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (proxy != null) {
            okHttpClient.setProxy(proxy);
        }
        URLConnection urlConnection = okHttpClient.open(url);
        Assert.isInstanceOf(HttpURLConnection.class, urlConnection);
        return (HttpURLConnection) urlConnection;
    }
}
