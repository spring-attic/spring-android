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

package org.springframework.web.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.android.test.Assert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

/** 
 * @author Arjen Poutsma
 * @author Roy Clarkson 
 */
public abstract class AbstractRestTemplateIntegrationTests extends AndroidTestCase {

	protected static final String TAG = AbstractRestTemplateIntegrationTests.class.getSimpleName();

	private static Server jettyServer;

	private static String helloWorld = "H\u00e9llo W\u00f6rld!!";

	private static String baseUrl;

	private static MediaType contentType;

	private RestTemplate restTemplate;

	protected abstract RestTemplate getRestTemplate();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.restTemplate = getRestTemplate();
		setUpJetty();
	}

	private static void setUpJetty() throws Exception {
		if (jettyServer == null) {
			int port = 8181;
			jettyServer = new Server(port);
			baseUrl = "http://localhost:" + port;
			Context jettyContext = new Context(jettyServer, "/");
			byte[] bytes = helloWorld.getBytes("UTF-8");
			contentType = new MediaType("text", "plain", Collections.singletonMap("charset", "utf-8"));
			jettyContext.addServlet(new ServletHolder(new GetServlet(bytes, contentType)), "/get");
			jettyContext.addServlet(new ServletHolder(new GetServlet(new byte[0], contentType)), "/get/nothing");
			jettyContext.addServlet(new ServletHolder(new GetServlet(bytes, null)), "/get/nocontenttype");
			jettyContext.addServlet(new ServletHolder(new ErrorServlet(401)), "/get/notauthorized");
			jettyContext.addServlet(new ServletHolder(new ErrorServlet(407)), "/get/notproxyauthorized");
			jettyContext.addServlet(new ServletHolder(new PostServlet(helloWorld, baseUrl + "/post/1", bytes,
					contentType)), "/post");
			jettyContext.addServlet(new ServletHolder(new StatusCodeServlet(204)), "/status/nocontent");
			jettyContext.addServlet(new ServletHolder(new StatusCodeServlet(304)), "/status/notmodified");
			jettyContext.addServlet(new ServletHolder(new ErrorServlet(401)), "/status/notfound");
			jettyContext.addServlet(new ServletHolder(new ErrorServlet(404)), "/status/notfound");
			jettyContext.addServlet(new ServletHolder(new ErrorServlet(500)), "/status/server");
			jettyContext.addServlet(new ServletHolder(new UriServlet()), "/uri/*");
			jettyContext.addServlet(new ServletHolder(new MultipartServlet()), "/multipart");
			jettyServer.start();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		this.restTemplate = null;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (jettyServer != null) {
			jettyServer.stop();
		}
	}

	@MediumTest
	public void testGetString() {
		String s = restTemplate.getForObject(baseUrl + "/{method}", String.class, "get");
		assertEquals("Invalid content", helloWorld, s);
	}

	@MediumTest
	public void testGetEntity() {
		ResponseEntity<String> entity = restTemplate.getForEntity(baseUrl + "/{method}", String.class, "get");
		assertEquals("Invalid content", helloWorld, entity.getBody());
		assertFalse("No headers", entity.getHeaders().isEmpty());
		assertEquals("Invalid content-type", contentType, entity.getHeaders().getContentType());
		assertEquals("Invalid status code", HttpStatus.OK, entity.getStatusCode());
	}

	@MediumTest
	public void testGetEntityNotAuthorized() {
		try {
			restTemplate.getForEntity(baseUrl + "/get/notauthorized", String.class);
			fail("HttpClientErrorException expected");
		} catch (HttpClientErrorException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getStatusText());
			assertNotNull(ex.getResponseBodyAsString());
		}
	}
	
	@MediumTest
	public void testGetEntityNotAuthorizedBadCredentials() {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setAuthorization(new HttpBasicAuthentication("bob", "password"));
			HttpEntity<Void> requestEntity = new HttpEntity<Void>(headers);
			restTemplate.exchange(baseUrl + "/get/notauthorized", HttpMethod.GET, requestEntity, String.class);
			fail("HttpClientErrorException expected");
		} catch (HttpClientErrorException ex) {
			assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
			assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getStatusText());
			assertNotNull(ex.getResponseBodyAsString());
		}
	}
	
	@MediumTest
	public void testGetEntityNotProxyAuthorized() {
		try {
			restTemplate.getForEntity(baseUrl + "/get/notproxyauthorized", String.class);
			fail("HttpClientErrorException expected");
		} catch (HttpClientErrorException ex) {
			assertEquals(HttpStatus.PROXY_AUTHENTICATION_REQUIRED, ex.getStatusCode());
			assertEquals(HttpStatus.PROXY_AUTHENTICATION_REQUIRED.getReasonPhrase(), ex.getStatusText());
			assertNotNull(ex.getResponseBodyAsString());
		}
	}

	@MediumTest
	public void testGetNoResponse() {
		String s = restTemplate.getForObject(baseUrl + "/get/nothing", String.class);
		assertNull("Invalid content", s);
	}

	@MediumTest
	public void testGetNoContentTypeHeader() throws UnsupportedEncodingException {
		byte[] bytes = restTemplate.getForObject(baseUrl + "/get/nocontenttype", byte[].class);
		Assert.assertArrayEquals("Invalid content", helloWorld.getBytes("UTF-8"), bytes);
	}

	@MediumTest
	public void testGetNoContent() {
		String s = restTemplate.getForObject(baseUrl + "/status/nocontent", String.class);
		assertNull("Invalid content", s);

		ResponseEntity<String> entity = restTemplate.getForEntity(baseUrl + "/status/nocontent", String.class);
		assertEquals("Invalid response code", HttpStatus.NO_CONTENT, entity.getStatusCode());
		assertNull("Invalid content", entity.getBody());
	}

	@MediumTest
	public void testGetNotModified() {
		String s = restTemplate.getForObject(baseUrl + "/status/notmodified", String.class);
		assertNull("Invalid content", s);

		ResponseEntity<String> entity = restTemplate.getForEntity(baseUrl + "/status/notmodified", String.class);
		assertEquals("Invalid response code", HttpStatus.NOT_MODIFIED, entity.getStatusCode());
		assertNull("Invalid content", entity.getBody());
	}

	@MediumTest
	public void testPostForLocation() throws URISyntaxException {
		URI location = restTemplate.postForLocation(baseUrl + "/{method}", helloWorld, "post");
		assertEquals("Invalid location", new URI(baseUrl + "/post/1"), location);
	}

	@MediumTest
	public void testPostForLocationEntity() throws URISyntaxException {
		HttpHeaders entityHeaders = new HttpHeaders();
		entityHeaders.setContentType(new MediaType("text", "plain", Charset.forName("ISO-8859-15")));
		HttpEntity<String> entity = new HttpEntity<String>(helloWorld, entityHeaders);
		URI location = restTemplate.postForLocation(baseUrl + "/{method}", entity, "post");
		assertEquals("Invalid location", new URI(baseUrl + "/post/1"), location);
	}

	@MediumTest
	public void testPostForObject() throws URISyntaxException {
		String s = restTemplate.postForObject(baseUrl + "/{method}", helloWorld, String.class, "post");
		assertEquals("Invalid content", helloWorld, s);
	}

	@MediumTest
	public void testNotFound() {
		try {
			restTemplate.execute(baseUrl + "/status/notfound", HttpMethod.GET, null, null);
			fail("HttpClientErrorException expected");
		} catch (HttpClientErrorException ex) {
			assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
			assertNotNull(ex.getStatusText());
			assertNotNull(ex.getResponseBodyAsString());
		}
	}

	@MediumTest
	public void testServerError() {
		try {
			restTemplate.execute(baseUrl + "/status/server", HttpMethod.GET, null, null);
			fail("HttpServerErrorException expected");
		} catch (HttpServerErrorException ex) {
			assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
			assertNotNull(ex.getStatusText());
			assertNotNull(ex.getResponseBodyAsString());
		}
	}

	@MediumTest
	public void testOptionsForAllow() throws URISyntaxException {
		Set<HttpMethod> allowed = restTemplate.optionsForAllow(new URI(baseUrl + "/get"));
		assertEquals("Invalid response",
				EnumSet.of(HttpMethod.GET, HttpMethod.OPTIONS, HttpMethod.HEAD, HttpMethod.TRACE), allowed);
	}

	@MediumTest
	public void testUri() throws InterruptedException, URISyntaxException {
		String result = restTemplate.getForObject(baseUrl + "/uri/{query}", String.class, "Z\u00fcrich");
		assertEquals("Invalid request URI", "/uri/Z%C3%BCrich", result);

		result = restTemplate.getForObject(baseUrl + "/uri/query={query}", String.class, "foo@bar");
		assertEquals("Invalid request URI", "/uri/query=foo@bar", result);

		result = restTemplate.getForObject(baseUrl + "/uri/query={query}", String.class, "T\u014dky\u014d");
		assertEquals("Invalid request URI", "/uri/query=T%C5%8Dky%C5%8D", result);
	}

	@MediumTest
	public void testMultipart() throws UnsupportedEncodingException {
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("name 1", "value 1");
		parts.add("name 2", "value 2+1");
		parts.add("name 2", "value 2+2");
		Resource logo = new ClassPathResource("res/drawable/icon.png");
		parts.add("logo", logo);
		restTemplate.postForLocation(baseUrl + "/multipart", parts);
	}

	@MediumTest
	public void testExchangeGet() throws Exception {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("MyHeader", "MyValue");
		HttpEntity<Void> requestEntity = new HttpEntity<Void>(requestHeaders);
		ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/{method}", HttpMethod.GET, requestEntity,
				String.class, "get");
		assertEquals("Invalid content", helloWorld, response.getBody());
	}

	@MediumTest
	public void testExchangePost() throws Exception {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("MyHeader", "MyValue");
		requestHeaders.setContentType(MediaType.TEXT_PLAIN);
		HttpEntity<String> requestEntity = new HttpEntity<String>(helloWorld, requestHeaders);
		HttpEntity<Void> result = restTemplate.exchange(baseUrl + "/{method}", HttpMethod.POST, requestEntity,
				Void.class, "post");
		assertEquals("Invalid location", new URI(baseUrl + "/post/1"), result.getHeaders().getLocation());
		assertFalse(result.hasBody());
	}

	// ANDROID-81
	// ignoring since this test makes a request to an external address
//	public void testMultipleHttpsRequests() throws Exception {
//		HttpHeaders requestHeaders = new HttpHeaders();
//		requestHeaders.setAcceptEncoding(ContentCodingType.GZIP);
//		requestHeaders.set("Connection", "Close");
//		requestHeaders.set("Cache-Control", "no-cache");
//		requestHeaders.set("Pragma", "no-cache");
//		HttpEntity<Void> requestEntity = new HttpEntity<Void>(requestHeaders);
//		String url = "https://github.com/spring-guides/gs-consuming-rest-android";
//		try {
//			restTemplate.exchange(url, HttpMethod.GET, requestEntity, byte[].class);
//		} catch (HttpClientErrorException e) {
//			if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
//				throw e;
//			}
//		}
//		url = "https://github.com/spring-guides/gs-consuming-rest-xml-android";
//		restTemplate.exchange(url, HttpMethod.GET, requestEntity, byte[].class);
//	}

	/** Servlet that sets the given status code. */
	private static class StatusCodeServlet extends GenericServlet {

		private static final long serialVersionUID = 1L;

		private final int sc;

		private StatusCodeServlet(int sc) {
			this.sc = sc;
		}

		@Override
		public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
			((HttpServletResponse) response).setStatus(sc);
		}
	}

	/** Servlet that returns an error message for a given status code. */
	private static class ErrorServlet extends GenericServlet {

		private static final long serialVersionUID = 1L;

		private final int sc;

		private ErrorServlet(int sc) {
			this.sc = sc;
		}

		@Override
		public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
			((HttpServletResponse) response).sendError(sc);
		}
	}

	private static class GetServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		private final byte[] buf;

		private final MediaType contentType;

		private GetServlet(byte[] buf, MediaType contentType) {
			this.buf = buf;
			this.contentType = contentType;
		}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
				IOException {
			if (contentType != null) {
				response.setContentType(contentType.toString());
			}
			response.setContentLength(buf.length);
			FileCopyUtils.copy(buf, response.getOutputStream());
		}
	}

	private static class PostServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		private final String s;

		private final String location;

		private final byte[] buf;

		private final MediaType contentType;

		private PostServlet(String s, String location, byte[] buf, MediaType contentType) {
			this.s = s;
			this.location = location;
			this.buf = buf;
			this.contentType = contentType;
		}

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
				IOException {
			assertTrue("Invalid request content-length", request.getContentLength() > 0);
			assertNotNull("No content-type", request.getContentType());
			String body = FileCopyUtils.copyToString(request.getReader());
			assertEquals("Invalid request body", s, body);
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader("Location", location);
			response.setContentLength(buf.length);
			response.setContentType(contentType.toString());
			FileCopyUtils.copy(buf, response.getOutputStream());
		}
	}

	private static class UriServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write(req.getRequestURI());
		}
	}

	private static class MultipartServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

			// TODO: determine how to test this

//			assertTrue(ServletFileUpload.isMultipartContent(req));
//			FileItemFactory factory = new DiskFileItemFactory();
//			ServletFileUpload upload = new ServletFileUpload(factory);
//			try {
//				List<?> items = upload.parseRequest(req);
//				assertEquals(4, items.size());
//				FileItem item = (FileItem) items.get(0);
//				assertTrue(item.isFormField());
//				assertEquals("name 1", item.getFieldName());
//				assertEquals("value 1", item.getString());
//
//				item = (FileItem) items.get(1);
//				assertTrue(item.isFormField());
//				assertEquals("name 2", item.getFieldName());
//				assertEquals("value 2+1", item.getString());
//
//				item = (FileItem) items.get(2);
//				assertTrue(item.isFormField());
//				assertEquals("name 2", item.getFieldName());
//				assertEquals("value 2+2", item.getString());
//
//				item = (FileItem) items.get(3);
//				assertFalse(item.isFormField());
//				assertEquals("logo", item.getFieldName());
//				assertEquals("logo.jpg", item.getName());
//				assertEquals("image/jpeg", item.getContentType());
//			} catch (FileUploadException ex) {
//				throw new ServletException(ex);
//			}
		}

	}

}