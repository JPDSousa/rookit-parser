package org.rookit.parser.parser;

import org.rookit.parser.result.Result;

class ParserPipelineImpl<I, CI, O extends Result<?>> extends AbstractParserPipeline<I, CI, O> {

	private final Parser<CI, O> parser;
	private final ParserPipeline<I, CI, O> topParser;
	
	ParserPipelineImpl(Parser<CI, O> parser, ParserPipeline<I, CI, O> topParser) {
		this.parser = parser;
		this.topParser = topParser;
	}
	
	private O getBaseResult(I token) {
		return topParser.parse(token);
	}
	
	private <R extends Result<?>> O getBaseResult(I token, R baseResult) {
		return topParser.parse(token, baseResult);
	}
	
	@Override
	public O parse(I token) {
		return parseLocal(token, getBaseResult(token));
	}

	@Override
	public <R extends Result<?>> O parse(I token, R baseResult) {
		return parseLocal(token, getBaseResult(token, baseResult));
	}
	
	private O parseLocal(I token, O baseResult) {
		return parser.parse(input2CurrentInput(token), baseResult);
	}

	@Override
	public Iterable<O> parseAll(I token) {
		return parseAllLocal(token, getBaseResult(token));
	}

	@Override
	public <R extends Result<?>> Iterable<O> parseAll(I token, R baseResult) {
		return parseAllLocal(token, getBaseResult(token, baseResult));
	}
	
	private Iterable<O> parseAllLocal(I token, O baseResult) {
		return parser.parseAll(input2CurrentInput(token), baseResult);
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
