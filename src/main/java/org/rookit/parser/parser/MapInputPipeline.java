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

import java.util.Optional;
import java.util.function.Function;

import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.result.Result;

class MapInputPipeline<I, CI, O extends Result<?>> extends AbstractParserPipeline<I, CI, O> {

	private final Function<I, CI> mapper;
	private final ParserPipeline<I, ?, O> topParser;
	
	MapInputPipeline(Function<I, CI> mapper, ParserPipeline<I, ?, O> topParser) {
		super();
		this.mapper = mapper;
		this.topParser = topParser;
	}

	@Override
	public Optional<O> parse(I token) {
		return topParser.parse(token);
	}

	@Override
	public <R extends Result<?>> Optional<O> parse(I token, R baseResult) {
		return topParser.parse(token, baseResult);
	}

	@Override
	public CI input2CurrentInput(I input) {
		return mapper.apply(input);
	}

	@Override
	public Iterable<O> parseAll(I token) {
		return topParser.parseAll(token);
	}

	@Override
	public <R extends Result<?>> Iterable<O> parseAll(I token, R baseResult) {
		return topParser.parseAll(token, baseResult);
	}

	@Override
	public ParserConfiguration getConfig() {
		return topParser.getConfig();
	}

}
