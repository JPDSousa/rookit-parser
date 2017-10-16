package org.rookit.parser.parser;

import java.util.function.Function;

import org.rookit.parser.result.Result;

@SuppressWarnings("javadoc")
public interface ParserPipeline<I, CI, O extends Result<?>> extends Parser<I, O> {

	ParserPipeline<I, CI, O> insert(Parser<CI, O> parser);
	
	<T> ParserPipeline<I, T, O> mapInput(Function<I, T> mapper);
	
	<R extends Result<?>> ParserPipeline<I, CI, R> mapResult(Function<O, R> mapper);
	
	CI input2CurrentInput(I input);
		
}
