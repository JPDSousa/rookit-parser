package org.rookit.parser.parser;

import java.util.List;
import java.util.function.Function;

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
	public NO parse(I token) {
		final O baseResult = topParser.build().parse(token);
		return mapper.apply(baseResult);
	}

	@Override
	public <Z extends Result<?>> NO parse(I token, Z baseResult) {
		final O baseResult1 = topParser.build().parse(token, baseResult);
		return mapper.apply(baseResult1);
	}

	@Override
	public Iterable<NO> parseAll(I token) {
		final List<NO> baseResult = Lists.newArrayList();
		topParser.build().parseAll(token).forEach(o -> baseResult.add(mapper.apply(o)));
		return baseResult;
	}

	@Override
	public <R extends Result<?>> Iterable<NO> parseAll(I token, R baseResult) {
		final List<NO> baseResults = Lists.newArrayList();
		topParser.build().parseAll(token, baseResult)
		.forEach(o -> baseResults.add(mapper.apply(o)));
		return baseResults;
	}

}
