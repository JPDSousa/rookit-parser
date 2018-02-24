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
import org.rookit.parser.utils.ParserValidator;

import com.google.common.base.Function;

abstract class AbstractParserPipeline<I, CI, R extends Result<?>> implements ParserPipeline<I, CI, R> {
	
	protected static final ParserValidator VALIDATOR = ParserValidator.getDefault();

	@Override
	public ParserPipeline<I, CI, R> insert(Parser<CI, R> parser) {
		return new ParserPipelineImpl<I, CI, R>(parser, this);
	}

	@Override
	public <T> ParserPipeline<I, T, R> mapInput(Function<I, T> mapper) {
		return new MapInputPipeline<>(mapper, this);
	}

	@Override
	public <R1 extends Result<?>> ParserPipeline<I, CI, R1> mapResult(Function<R, R1> mapper) {
		return new MapResultPipeline<>(mapper, this);
	}

}
