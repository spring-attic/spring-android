package org.springframework.http.client;

public class BufferingSimpleClientHttpRequestFactoryTests extends BufferingAbstractClientHttpRequestFactoryTests {

	@Override
	protected ClientHttpRequestFactory createRequestFactory() {
		return new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
	}
}
