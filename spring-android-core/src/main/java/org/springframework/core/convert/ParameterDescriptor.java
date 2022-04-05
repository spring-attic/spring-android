/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.convert;

import java.lang.annotation.Annotation;

import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.MethodParameter;

/**
 * @author Keith Donald
 * @since 2.0
 */
class ParameterDescriptor extends AbstractDescriptor {

	private final MethodParameter methodParameter;


	public ParameterDescriptor(MethodParameter methodParameter) {
		super(methodParameter.getParameterType());
		if (methodParameter.getNestingLevel() != 1) {
			throw new IllegalArgumentException("MethodParameter argument must have its nestingLevel set to 1");
		}
		this.methodParameter = methodParameter;
	}

	private ParameterDescriptor(Class<?> type, MethodParameter methodParameter) {
		super(type);
		this.methodParameter = methodParameter;
	}


	@Override
	public Annotation[] getAnnotations() {
		if (this.methodParameter.getParameterIndex() == -1) {
			return TypeDescriptor.nullSafeAnnotations(this.methodParameter.getMethodAnnotations());
		}
		else {
			return TypeDescriptor.nullSafeAnnotations(this.methodParameter.getParameterAnnotations());
		}
	}

	@Override
	protected Class<?> resolveCollectionElementType() {
		return GenericCollectionTypeResolver.getCollectionParameterType(this.methodParameter);
	}

	@Override
	protected Class<?> resolveMapKeyType() {
		return GenericCollectionTypeResolver.getMapKeyParameterType(this.methodParameter);
	}

	@Override
	protected Class<?> resolveMapValueType() {
		return GenericCollectionTypeResolver.getMapValueParameterType(this.methodParameter);
	}

	@Override
	protected AbstractDescriptor nested(Class<?> type, int typeIndex) {
		MethodParameter methodParameter = new MethodParameter(this.methodParameter);
		methodParameter.increaseNestingLevel();
		methodParameter.setTypeIndexForCurrentLevel(typeIndex);
		return new ParameterDescriptor(type, methodParameter);
	}

}
