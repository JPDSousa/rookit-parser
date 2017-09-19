package org.rookit.parser.parser;

import java.util.function.Function;

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
	public O parse(I token) {
		return topParser.build().parse(token);
	}

	@Override
	public <R extends Result<?>> O parse(I token, R baseResult) {
		return topParser.build().parse(token, baseResult);
	}

	@Override
	public CI input2CurrentInput(I input) {
		return mapper.apply(input);
	}

}
