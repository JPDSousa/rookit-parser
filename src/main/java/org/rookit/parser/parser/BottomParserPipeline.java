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

import java.util.Arrays;
import java.util.Collections;

import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.result.Result;

import com.google.common.base.Optional;

class BottomParserPipeline<I, O extends Result<?>> extends AbstractParserPipeline<I, I, O> {
	
	private final O baseResult;
	private final ParserConfiguration defaultConfig;
	
	@SuppressWarnings("unchecked")
	BottomParserPipeline(O baseResult) {
		this.baseResult = baseResult;
		defaultConfig = Parser.createConfiguration((Class<? extends Result<?>>) baseResult.getClass());
	}

	@Override
	public Optional<O> parse(I token) {
		VALIDATOR.checkArgumentNotNull(token, "Cannot parse null");
		return Optional.of(baseResult);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends Result<?>> Optional<O> parse(I token, R baseResult) {
		VALIDATOR.checkArgumentClass(this.baseResult.getClass(), baseResult.getClass(), "The base result class is not valid.");
		return Optional.of((O) baseResult);
	}

	@Override
	public Iterable<O> parseAll(I token) {
		VALIDATOR.checkArgumentNotNull(token, "Cannot parse null");
		final Optional<O> result = parse(token);
		if(result.isPresent()) {
			return Arrays.asList(result.get());
		}
		return Collections.emptyList();
	}

	@Override
	public <R extends Result<?>> Iterable<O> parseAll(I token, R baseResult) {
		final Optional<O> result = parse(token, baseResult);
		if(result.isPresent()) {
			return Arrays.asList(result.get());
		}
		return Collections.emptyList();
	}

	@Override
	public I input2CurrentInput(I input) {
		VALIDATOR.checkArgumentNotNull(input, "Cannot parse null");
		return input;
	}

	@Override
	public ParserConfiguration getConfig() {
		return defaultConfig;
	}

}
