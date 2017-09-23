/*******************************************************************************
 * Copyright (C) 2017 Joao Sousa
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.rookit.parser.parser;

import org.rookit.parser.result.Result;
import org.rookit.parser.result.ResultFactory;
import org.rookit.parser.utils.ParserValidator;

import com.google.common.collect.Lists;

abstract class AbstractParser<T, R extends Result<?>> implements Parser<T, R> {

	protected static final ParserValidator VALIDATOR = ParserValidator.getDefault();
	
	private final ParserConfiguration<T, R> config;
	private final ResultFactory resultFactory;
	
	protected AbstractParser(ParserConfiguration<T, R> config){
		this.config = config;
		this.resultFactory = ResultFactory.getDefault(); 
	}
	
	protected final ParserConfiguration<T, R> getConfig() {
		return config;
	}
	
	protected final R createEmptyResult() {
		final Class<R> resultClass = config.getResultClass();
		return resultFactory.newResult(resultClass);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <R1 extends Result<?>> R parse(T token, R1 baseResult) {
		VALIDATOR.checkArgumentClass(config.getResultClass(), baseResult.getClass(), "Invalid base result class");
		return parseFromBaseResult(token, (R) baseResult);
	}
	
	@Override
	public Iterable<R> parseAll(T token) {
		return Lists.newArrayList(parse(token));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends Result<?>> Iterable<R> parseAll(T token, O baseResult) {
		VALIDATOR.checkArgumentClass(getConfig().getResultClass(), baseResult.getClass(), "The base result class is not valid.");
		return Lists.newArrayList(parse(token, (R) baseResult));
	}

	protected abstract R parseFromBaseResult(T token, R baseResult);
	
	@Override
	public final R parse(T token) {
		return parseFromBaseResult(token, createEmptyResult());
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractParser<?, ?> other = (AbstractParser<?, ?>) obj;
		if (config == null) {
			if (other.config != null) {
				return false;
			}
		} else if (!config.equals(other.config)) {
			return false;
		}
		return true;
	}
}
