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

abstract class AbstractParser<T, R extends Result<?>> implements Parser<T, R> {

	protected static final ParserValidator VALIDATOR = ParserValidator.getDefault();
	
	private boolean dbStorage;
	
	private final ParserConfiguration<T, R> config;
	
	protected AbstractParser(ParserConfiguration<T, R> config){
		this.dbStorage = true;
		this.config = config;
	}

	@Override
	public void setDBStorage(boolean store) {
		this.dbStorage = store;
	}

	@Override
	public boolean getDBStorage() {
		return dbStorage;
	}
	
	protected final ParserConfiguration<T, R> getConfig() {
		return config;
	}
	
	protected abstract R parse(T token, R baseResult);
	
	@Override
	public final R parse(T token) {
		final Parser<T, R> baseParser = config.getBaseParser();
		final ResultFactory factory = ResultFactory.getDefault();
		final Class<R> resultClass = config.getResultClass();
		
		if(baseParser != null) {
			return parseFromBaseParser(token, baseParser);
		}
		return parse(token, factory.newResult(resultClass));
	}

	private R parseFromBaseParser(T token, Parser<T, R> baseParser) {
		final R baseResult = baseParser.parse(token);
		if(baseResult == null) {
			return null;
		}
		return parse(token, baseParser.parse(token));
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
