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

import java.util.Collections;

import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.result.Result;

import com.google.common.base.Optional;

class ParserPipelineImpl<I, CI, O extends Result<?>> extends AbstractParserPipeline<I, CI, O> {

	private final Parser<CI, O> parser;
	private final ParserPipeline<I, CI, O> topParser;
	
	ParserPipelineImpl(Parser<CI, O> parser, ParserPipeline<I, CI, O> topParser) {
		this.parser = parser;
		this.topParser = topParser;
	}
	
	private Optional<O> getBaseResult(I token) {
		return topParser.parse(token);
	}
	
	private <R extends Result<?>> Optional<O> getBaseResult(I token, R baseResult) {
		return topParser.parse(token, baseResult);
	}
	
	@Override
	public Optional<O> parse(I token) {
		return parseLocal(token, getBaseResult(token));
	}

	@Override
	public <R extends Result<?>> Optional<O> parse(I token, R baseResult) {
		return parseLocal(token, getBaseResult(token, baseResult));
	}
	
	private Optional<O> parseLocal(I token, Optional<O> baseResult) {
		if(baseResult.isPresent()) {
			return parser.parse(input2CurrentInput(token), baseResult.get());
		}
		return baseResult;
	}

	@Override
	public Iterable<O> parseAll(I token) {
		return parseAllLocal(token, getBaseResult(token));
	}

	@Override
	public <R extends Result<?>> Iterable<O> parseAll(I token, R baseResult) {
		return parseAllLocal(token, getBaseResult(token, baseResult));
	}
	
	private Iterable<O> parseAllLocal(I token, Optional<O> baseResult) {
		if(baseResult.isPresent()) {
			return parser.parseAll(input2CurrentInput(token), baseResult.get());
		}
		return Collections.emptyList();
	}

	@Override
	public CI input2CurrentInput(I input) {
		return topParser.input2CurrentInput(input);
	}

	@Override
	public ParserConfiguration getConfig() {
		return parser.getConfig();
	}

}
