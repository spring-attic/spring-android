package org.springframework.http.client;

public class BufferingCommonsClientHttpRequestFactoryTests extends BufferingAbstractClientHttpRequestFactoryTests {

	@SuppressWarnings("deprecation")
	@Override
	protected ClientHttpRequestFactory createRequestFactory() {
		return new BufferingClientHttpRequestFactory(new CommonsClientHttpRequestFactory());
	}
}
