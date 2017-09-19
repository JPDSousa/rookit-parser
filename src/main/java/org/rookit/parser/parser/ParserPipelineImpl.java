package org.rookit.parser.parser;

import org.rookit.parser.result.Result;

class ParserPipelineImpl<I, CI, O extends Result<?>> extends AbstractParserPipeline<I, CI, O> {

	private final Parser<CI, O> parser;
	private final ParserPipeline<I, CI, O> topParser;
	
	ParserPipelineImpl(Parser<CI, O> parser, ParserPipeline<I, CI, O> topParser) {
		this.parser = parser;
		this.topParser = topParser;
	}
	
	@Override
	public O parse(I token) {
		final O baseResult = topParser.build().parse(token);
		return parser.parse(input2CurrentInput(token), baseResult);
	}

	@Override
	public <R extends Result<?>> O parse(I token, R baseResult) {
		final O baseResult1 = topParser.build().parse(token, baseResult);
		return parser.parse(input2CurrentInput(token), baseResult1);
	}

	@Override
	public CI input2CurrentInput(I input) {
		return topParser.input2CurrentInput(input);
	}

}
