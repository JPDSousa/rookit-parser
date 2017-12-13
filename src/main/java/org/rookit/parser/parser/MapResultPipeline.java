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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.result.Result;

import com.google.common.collect.Lists;

class MapResultPipeline<I, CI, NO extends Result<?>, O extends Result<?>> extends AbstractParserPipeline<I, CI, NO> {

	private final Function<O, NO> mapper;
	private final ParserPipeline<I, CI, O> topParser;

	MapResultPipeline(Function<O, NO> mapper, ParserPipeline<I, CI, O> topParser) {
		super();
		this.mapper = mapper;
		this.topParser = topParser;
	}

	@Override
	public CI input2CurrentInput(I input) {
		return topParser.input2CurrentInput(input);
	}

	@Override
	public Optional<NO> parse(I token) {
		final Optional<O> baseResult = topParser.parse(token);
		return Optional.ofNullable(mapper.apply(baseResult.orElse(null)));
	}

	@Override
	public <Z extends Result<?>> Optional<NO> parse(I token, Z baseResult) {
		final Optional<O> baseResult1 = topParser.parse(token, baseResult);
		return Optional.ofNullable(mapper.apply(baseResult1.orElse(null)));
	}

	@Override
	public Iterable<NO> parseAll(I token) {
		final List<NO> baseResult = Lists.newArrayList();
		topParser.parseAll(token).forEach(o -> baseResult.add(mapper.apply(o)));
		return baseResult;
	}

	@Override
	public <R extends Result<?>> Iterable<NO> parseAll(I token, R baseResult) {
		final List<NO> baseResults = Lists.newArrayList();
		topParser.parseAll(token, baseResult)
		.forEach(o -> baseResults.add(mapper.apply(o)));
		return baseResults;
	}

	@Override
	public ParserConfiguration getConfig() {
		return topParser.getConfig();
	}
	
}
