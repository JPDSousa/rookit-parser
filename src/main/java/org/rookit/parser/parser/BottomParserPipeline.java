package org.rookit.parser.parser;

import org.rookit.parser.result.Result;

class BottomParserPipeline<I, O extends Result<?>> extends AbstractParserPipeline<I, I, O> {
	
	private final O baseResult;
	
	BottomParserPipeline(O baseResult) {
		this.baseResult = baseResult;
	}

	@Override
	public O parse(I token) {
		return baseResult;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends Result<?>> O parse(I token, R baseResult) {
		VALIDATOR.checkArgumentClass(this.baseResult.getClass(), baseResult.getClass(), "The base result class is not valid.");
		return (O) baseResult;
	}

	@Override
	public I input2CurrentInput(I input) {
		return input;
	}

}
