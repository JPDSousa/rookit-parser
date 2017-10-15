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
		return topParser.parse(token);
	}

	@Override
	public <R extends Result<?>> O parse(I token, R baseResult) {
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
