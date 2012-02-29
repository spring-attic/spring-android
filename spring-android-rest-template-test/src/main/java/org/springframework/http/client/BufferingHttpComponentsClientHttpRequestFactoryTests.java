package org.springframework.http.client;

public class BufferingHttpComponentsClientHttpRequestFactoryTests extends BufferingAbstractClientHttpRequestFactoryTests {

	@Override
	protected ClientHttpRequestFactory createRequestFactory() {
		return new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory());
	}
}
