package org.rookit.parser.parser;

import java.util.function.Function;

import org.rookit.parser.result.Result;
import org.rookit.parser.utils.ParserValidator;

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
