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

package org.springframework.http.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;

import android.test.suitebuilder.annotation.MediumTest;

/** 
 * @author Arjen Poutsma
 * @author Roy Clarkson 
 */
public abstract class AbstractHttpRequestFactoryTestCase extends TestCase {

	protected static final String TAG = AbstractHttpRequestFactoryTestCase.class.getSimpleName();

	protected static String baseUrl;

	private static Server jettyServer;

	protected ClientHttpRequestFactory factory;

	protected abstract ClientHttpRequestFactory createRequestFactory();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.factory = createRequestFactory();
		setUpJetty();
	}

	private static void setUpJetty() throws Exception {
		if (jettyServer == null) {
			int port = 8080;
			jettyServer = new Server(port);
			baseUrl = "http://localhost:" + port;
			Context jettyContext = new Context(jettyServer, "/");
			jettyContext.addServlet(new ServletHolder(new EchoServlet()), "/echo");
			jettyContext.addServlet(new ServletHolder(new GzipServlet()), "/gzip");
			jettyContext.addServlet(new ServletHolder(new IdentityServlet()), "/identity");
			jettyContext.addServlet(new ServletHolder(new NoEncodingServlet()), "/noencoding");
			jettyContext.addServlet(new ServletHolder(new StatusServlet(200)), "/status/ok");
			jettyContext.addServlet(new ServletHolder(new StatusServlet(404)), "/status/notfound");
			jettyContext.addServlet(new ServletHolder(new MethodServlet("DELETE")), "/methods/delete");
			jettyContext.addServlet(new ServletHolder(new MethodServlet("GET")), "/methods/get");
			jettyContext.addServlet(new ServletHolder(new MethodServlet("HEAD")), "/methods/head");
			jettyContext.addServlet(new ServletHolder(new MethodServlet("OPTIONS")), "/methods/options");
			jettyContext.addServlet(new ServletHolder(new PostServlet()), "/methods/post");
			jettyContext.addServlet(new ServletHolder(new MethodServlet("PUT")), "/methods/put");
			jettyContext.addServlet(new ServletHolder(new MethodServlet("PATCH")), "/methods/patch");
			jettyServer.start();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		this.factory = null;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (jettyServer != null) {
			jettyServer.stop();
		}
	}

	@MediumTest
	public void testStatus() throws Exception {
		URI uri = new URI(baseUrl + "/status/notfound");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.GET);
		assertEquals("Invalid HTTP method", HttpMethod.GET, request.getMethod());
		assertEquals("Invalid HTTP URI", uri, request.getURI());
		ClientHttpResponse response = request.execute();
		try {
			assertEquals("Invalid status code", HttpStatus.NOT_FOUND, response.getStatusCode());
		}
		finally {
			response.close();
		}
	}

	@MediumTest
	public void testEchoWithContentLength() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/echo"), HttpMethod.PUT);
		assertEquals("Invalid HTTP method", HttpMethod.PUT, request.getMethod());
		String headerName = "MyHeader";
		String headerValue1 = "value1";
		request.getHeaders().add(headerName, headerValue1);
		String headerValue2 = "value2";
		request.getHeaders().add(headerName, headerValue2);
		final byte[] body = "Hello World".getBytes("UTF-8");
		request.getHeaders().setContentLength(body.length);
		if (request instanceof StreamingHttpOutputMessage) {
			StreamingHttpOutputMessage streamingRequest =
					(StreamingHttpOutputMessage) request;
			streamingRequest.setBody(new StreamingHttpOutputMessage.Body() {
				@Override
				public void writeTo(OutputStream outputStream) throws IOException {
					StreamUtils.copy(body, outputStream);
				}
			});
		}
		else {
			StreamUtils.copy(body, request.getBody());
		}
		ClientHttpResponse response = request.execute();
		try {
			assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
			assertTrue("Header not found", response.getHeaders().containsKey(headerName));
			assertEquals("Header value not found", Arrays.asList(headerValue1, headerValue2),
					response.getHeaders().get(headerName));
			byte[] result = FileCopyUtils.copyToByteArray(response.getBody());
			assertTrue("Invalid body", Arrays.equals(body, result));
		}
		finally {
			response.close();
		}
	}

	@MediumTest
	public void testEchoNoContentLength() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/echo"), HttpMethod.PUT);
		assertEquals("Invalid HTTP method", HttpMethod.PUT, request.getMethod());
		String headerName = "MyHeader";
		String headerValue1 = "value1";
		request.getHeaders().add(headerName, headerValue1);
		String headerValue2 = "value2";
		request.getHeaders().add(headerName, headerValue2);
		final byte[] body = "Hello World".getBytes("UTF-8");
		if (request instanceof StreamingHttpOutputMessage) {
			StreamingHttpOutputMessage streamingRequest =
					(StreamingHttpOutputMessage) request;
			streamingRequest.setBody(new StreamingHttpOutputMessage.Body() {
				@Override
				public void writeTo(OutputStream outputStream) throws IOException {
					StreamUtils.copy(body, outputStream);
				}
			});
		}
		else {
			StreamUtils.copy(body, request.getBody());
		}
		ClientHttpResponse response = request.execute();
		try {
			assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
			assertTrue("Header not found", response.getHeaders().containsKey(headerName));
			assertEquals("Header value not found", Arrays.asList(headerValue1, headerValue2),
					response.getHeaders().get(headerName));
			byte[] result = FileCopyUtils.copyToByteArray(response.getBody());
			assertTrue("Invalid body", Arrays.equals(body, result));
		}
		finally {
			response.close();
		}
	}

	@MediumTest
	public void testMultipleWrites() throws Exception {
		try {
			ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/echo"), HttpMethod.POST);
			final byte[] body = "Hello World".getBytes("UTF-8");
			if (request instanceof StreamingHttpOutputMessage) {
				StreamingHttpOutputMessage streamingRequest =
						(StreamingHttpOutputMessage) request;
				streamingRequest.setBody(new StreamingHttpOutputMessage.Body() {
					@Override
					public void writeTo(OutputStream outputStream) throws IOException {
						StreamUtils.copy(body, outputStream);
					}
				});
			}
			else {
				StreamUtils.copy(body, request.getBody());
			}

			ClientHttpResponse response = request.execute();
			try {
				FileCopyUtils.copy(body, request.getBody());
			}
			finally {
				response.close();
			}
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
		}
	}

	@MediumTest
	public void testMultipleWritesContentEncodingGzip() throws Exception {
		try {
			ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/echo"), HttpMethod.POST);
			request.getHeaders().add("Content-Encoding", "gzip");
			final byte[] body = "Hello World".getBytes("UTF-8");
			if (request instanceof StreamingHttpOutputMessage) {
				StreamingHttpOutputMessage streamingRequest =
						(StreamingHttpOutputMessage) request;
				streamingRequest.setBody(new StreamingHttpOutputMessage.Body() {
					@Override
					public void writeTo(OutputStream outputStream) throws IOException {
						StreamUtils.copy(body, outputStream);
					}
				});
			}
			else {
				StreamUtils.copy(body, request.getBody());
			}

			ClientHttpResponse response = request.execute();
			try {
				FileCopyUtils.copy(body, request.getBody());
			}
			finally {
				response.close();
			}
			fail("Expected IllegalStateException");
		} catch (IllegalStateException e) {
		}
	}

	@MediumTest
	public void testHeadersAfterExecute() throws Exception {
		try {
			ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/echo"), HttpMethod.POST);
			request.getHeaders().add("MyHeader", "value");
			byte[] body = "Hello World".getBytes("UTF-8");
			FileCopyUtils.copy(body, request.getBody());
			ClientHttpResponse response = request.execute();
			try {
				request.getHeaders().add("MyHeader", "value");
			}
			finally {
				response.close();
			}
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
	}

	@MediumTest
	public void testHttpMethods() throws Exception {
		assertHttpMethod("get", HttpMethod.GET);
		assertHttpMethod("head", HttpMethod.HEAD);
		assertHttpMethod("post", HttpMethod.POST);
		assertHttpMethod("put", HttpMethod.PUT);
		assertHttpMethod("options", HttpMethod.OPTIONS);
		assertHttpMethod("delete", HttpMethod.DELETE);
	}

	@MediumTest
	public void testGetAcceptEncodingGzip() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/gzip"), HttpMethod.GET);
		assertEquals("Invalid HTTP method", HttpMethod.GET, request.getMethod());
		request.getHeaders().add("Accept-Encoding", "gzip");
		ClientHttpResponse response = request.execute();
		try {
			assertNotNull(response.getStatusText());
			assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
			assertTrue("Header not found", response.getHeaders().containsKey("Content-Encoding"));
			assertEquals("Header value not found", Arrays.asList("gzip"), response.getHeaders().get("Content-Encoding"));
			final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			byte[] result = FileCopyUtils.copyToByteArray(response.getBody());
			assertTrue("Invalid body", Arrays.equals(body, result));
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(body.length);
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
			FileCopyUtils.copy(body, gzipOutputStream);
			byte[] compressedBody = byteArrayOutputStream.toByteArray();
			assertEquals("Invalid content-length", response.getHeaders().getContentLength(), compressedBody.length);
		} finally {
			response.close();
		}
	}

	@MediumTest
	public void testGetAcceptEncodingIdentity() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/identity"), HttpMethod.GET);
		assertEquals("Invalid HTTP method", HttpMethod.GET, request.getMethod());
		// setting the following header in Gingerbread and newer disables automatic gzip compression
		request.getHeaders().add("Accept-Encoding", "identity");
		ClientHttpResponse response = request.execute();
		try {
			assertNotNull(response.getStatusText());
			assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
			assertFalse("Header found", response.getHeaders().containsKey("Content-Encoding"));
			final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			byte[] result = FileCopyUtils.copyToByteArray(response.getBody());
			assertTrue("Invalid body", Arrays.equals(body, result));
			assertEquals("Invalid content-length", body.length, response.getHeaders().getContentLength());
		} finally {
			response.close();
		}
	}

	@MediumTest
	public void testGetAcceptEncodingNone() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/noencoding"), HttpMethod.GET);
		assertEquals("Invalid HTTP method", HttpMethod.GET, request.getMethod());
		ClientHttpResponse response = request.execute();
		try {
			assertNotNull(response.getStatusText());
			assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
			assertFalse("Header found", response.getHeaders().containsKey("Content-Encoding"));
			final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			byte[] result = FileCopyUtils.copyToByteArray(response.getBody());
			assertTrue("Invalid body", Arrays.equals(body, result));
			assertEquals("Invalid content-length", body.length, response.getHeaders().getContentLength());
		}
		finally {
			response.close();
		}
	}

	@MediumTest
	public void testPostContentEncodingGzip() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/gzip"), HttpMethod.POST);
		assertEquals("Invalid HTTP method", HttpMethod.POST, request.getMethod());
		request.getHeaders().add("Content-Encoding", "gzip");
		final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
				.getBytes("UTF-8");
		if (request instanceof StreamingHttpOutputMessage) {
			StreamingHttpOutputMessage streamingRequest =
					(StreamingHttpOutputMessage) request;
			streamingRequest.setBody(new StreamingHttpOutputMessage.Body() {
				@Override
				public void writeTo(OutputStream outputStream) throws IOException {
					GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
					StreamUtils.copy(body, gzipOutputStream);
					gzipOutputStream.close();
				}
			});
		}
		else {
			StreamUtils.copy(body, request.getBody());
		}

		ClientHttpResponse response = request.execute();
		try {
			assertNotNull(response.getStatusText());
			assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
		} finally {
			response.close();
		}
	}

	// SPR-8809
	@MediumTest
	public void testInterceptor() throws Exception {
		final String headerName = "MyHeader";
		final String headerValue = "MyValue";
		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
				request.getHeaders().add(headerName, headerValue);
				return execution.execute(request, body);
			}
		};
		InterceptingClientHttpRequestFactory factory = new InterceptingClientHttpRequestFactory(createRequestFactory(), Collections.singletonList(interceptor));

		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/echo"), HttpMethod.GET);
			response = request.execute();
			assertEquals("Invalid response status", HttpStatus.OK, response.getStatusCode());
			HttpHeaders responseHeaders = response.getHeaders();
			assertEquals("Custom header invalid", headerValue, responseHeaders.getFirst(headerName));
		}
		finally {
			if (response != null) {
				response.close();
			}
		}
	}

	@MediumTest
	public void testPostContentEncodingIdentity() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/identity"), HttpMethod.POST);
		assertEquals("Invalid HTTP method", HttpMethod.POST, request.getMethod());
		request.getHeaders().add("Content-Encoding", "identity");
		final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
				.getBytes("UTF-8");
		if (request instanceof StreamingHttpOutputMessage) {
			StreamingHttpOutputMessage streamingRequest =
					(StreamingHttpOutputMessage) request;
			streamingRequest.setBody(new StreamingHttpOutputMessage.Body() {
				@Override
				public void writeTo(OutputStream outputStream) throws IOException {
					StreamUtils.copy(body, outputStream);
				}
			});
		}
		else {
			StreamUtils.copy(body, request.getBody());
		}

		ClientHttpResponse response = request.execute();
		try {
			assertNotNull(response.getStatusText());
			assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
		} finally {
			response.close();
		}
	}

	protected void assertHttpMethod(String path, HttpMethod method) throws Exception {
		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = factory.createRequest(new URI(baseUrl + "/methods/" + path), method);
			response = request.execute();
			assertEquals("Invalid response status", HttpStatus.OK, response.getStatusCode());
			assertEquals("Invalid method", path.toUpperCase(Locale.ENGLISH), request.getMethod().name());
		}
		finally {
			if (response != null) {
				response.close();
			}
		}
	}

	/**
	 * Servlet that sets a given status code.
	 */
	@SuppressWarnings("serial")
	private static class StatusServlet extends GenericServlet {

		private final int sc;

		private StatusServlet(int sc) {
			this.sc = sc;
		}

		@Override
		public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
			((HttpServletResponse) response).setStatus(sc);
		}
	}

	@SuppressWarnings("serial")
	private static class MethodServlet extends GenericServlet {

		private final String method;

		private MethodServlet(String method) {
			this.method = method;
		}

		@Override
		public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
			HttpServletRequest httpReq = (HttpServletRequest) req;
			assertEquals("Invalid HTTP method", method, httpReq.getMethod());
			res.setContentLength(0);
			((HttpServletResponse) res).setStatus(200);
		}
	}

	@SuppressWarnings("serial")
	private static class PostServlet extends MethodServlet {

		private PostServlet() {
			super("POST");
		}

		@Override
		public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
			super.service(req, res);
			long contentLength = req.getContentLength();
			if (contentLength != -1) {
				InputStream in = req.getInputStream();
				long byteCount = 0;
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = in.read(buffer)) != -1) {
					byteCount += bytesRead;
				}
				assertEquals("Invalid content-length", contentLength, byteCount);
			}
		}
	}

	@SuppressWarnings("serial")
	private static class EchoServlet extends HttpServlet {

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			echo(req, res);
		}

		private void echo(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.setStatus(HttpServletResponse.SC_OK);
			for (Enumeration e1 = request.getHeaderNames(); e1.hasMoreElements();) {
				String headerName = (String) e1.nextElement();
				for (Enumeration e2 = request.getHeaders(headerName); e2.hasMoreElements();) {
					String headerValue = (String) e2.nextElement();
					response.addHeader(headerName, headerValue);
				}
			}
			FileCopyUtils.copy(request.getInputStream(), response.getOutputStream());
		}
	}

	@SuppressWarnings("serial")
	private static class GzipServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			assertTrue(containsHeaderValue(req, "Accept-Encoding", "gzip"));
			res.setStatus(HttpServletResponse.SC_OK);
			res.addHeader("Content-Encoding", "gzip");
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(body.length);
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
			FileCopyUtils.copy(body, gzipOutputStream);
			gzipOutputStream.close();
			byte[] compressedBody = byteArrayOutputStream.toByteArray();
			FileCopyUtils.copy(compressedBody, res.getOutputStream());
			res.setContentLength(compressedBody.length);
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			assertTrue(containsHeaderValue(req, "Content-Encoding", "gzip"));
			res.setStatus(HttpServletResponse.SC_OK);
			res.setContentLength(0);
			GZIPInputStream gzipInputStream = new GZIPInputStream(req.getInputStream());
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			int byteCount = 0;
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			byteArrayOutputStream.flush();
			gzipInputStream.close();
			assertEquals("Content length does not match", body.length, byteCount);
			assertTrue("Invalid body", Arrays.equals(byteArrayOutputStream.toByteArray(), body));
		}

	}

	@SuppressWarnings("serial")
	private static class IdentityServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			assertTrue(containsHeaderValue(req, "Accept-Encoding", "identity"));
			res.setStatus(HttpServletResponse.SC_OK);
			FileCopyUtils.copy(body, res.getOutputStream());
			res.setContentLength(body.length);
		}

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			assertTrue(containsHeaderValue(req, "Content-Encoding", "identity"));
			res.setStatus(HttpServletResponse.SC_OK);
			res.setContentLength(0);
			byte[] decompressedBody = FileCopyUtils.copyToByteArray(req.getInputStream());
			assertTrue("Invalid body", Arrays.equals(decompressedBody, body));
		}

	}

	@SuppressWarnings("serial")
	private static class NoEncodingServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			final byte[] body = "gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip gzip "
					.getBytes("UTF-8");
			res.setStatus(HttpServletResponse.SC_OK);
			if (containsHeaderValue(req, "Accept-Encoding", "gzip")) {
				res.addHeader("Content-Encoding", "gzip");
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(body.length);
				GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
				FileCopyUtils.copy(body, gzipOutputStream);
				byte[] compressedBody = byteArrayOutputStream.toByteArray();
				FileCopyUtils.copy(compressedBody, res.getOutputStream());
				res.setContentLength(compressedBody.length);
			}
			else {
				FileCopyUtils.copy(body, res.getOutputStream());
				res.setContentLength(body.length);
			}
		}

	}

	private static boolean containsHeaderValue(HttpServletRequest req, String name, String value) {
		for (Enumeration<?> e1 = req.getHeaderNames(); e1.hasMoreElements();) {
			String headerName = (String) e1.nextElement();
			for (Enumeration<?> e2 = req.getHeaders(headerName); e2.hasMoreElements();) {
				String headerValue = (String) e2.nextElement();
				if (headerName.equals(name) && headerValue.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

}
