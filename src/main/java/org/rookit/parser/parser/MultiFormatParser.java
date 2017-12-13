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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.exceptions.InvalidSongFormatException;
import org.rookit.parser.exceptions.MissingRequiredFieldException;
import org.rookit.parser.result.Result;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.FormatSong;
import org.rookit.parser.utils.PathUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

class MultiFormatParser extends AbstractParser<String, SingleTrackAlbumBuilder> {
	
	private static final String[][] ENHANCEMENTS = {/*{"_", " "},*/
			{"  ", " "},
			{"�", "-"}};

	static MultiFormatParser create(ParserConfiguration configuration) {
		return new MultiFormatParser(configuration);
	}

	private MultiFormatParser(ParserConfiguration config){
		super(config);
	}

	private void validateRequiredFields(TrackFormat format) {
		final Field[] requiredFields = getConfig().getRequiredFields();
		final List<Field> missingFields = requiredFields != null ? format.getMissingRequiredFields(requiredFields) : Lists.newArrayList();

		if(!missingFields.isEmpty()){
			VALIDATOR.handleTestException(new MissingRequiredFieldException(missingFields));
		}
	}

	private String enhanceInput(String originalName){
		String name = originalName;
		for(String[] enhancement : ENHANCEMENTS){
			name = name.replace(enhancement[0], enhancement[1]);
		}
		return name.trim();
	}

	@Override
	protected Optional<SingleTrackAlbumBuilder> parseFromBaseResult(String token, SingleTrackAlbumBuilder baseResult) {
		final Iterable<SingleTrackAlbumBuilder> results = parseAllLocal(token, baseResult);
		return Optional.ofNullable(Iterables.getFirst(results, null));
	}

	@Override
	public Iterable<SingleTrackAlbumBuilder> parseAll(String path){
		final Iterable<SingleTrackAlbumBuilder> results =  parseAllLocal(path, getDefaultBaseResult());
		final int limit = getConfig().getLimit();
		if(limit > 0) {
			return Iterables.limit(results, limit);
		}
		return results;
	}

	@Override
	public <O extends Result<?>> Iterable<SingleTrackAlbumBuilder> parseAll(String token, O baseResult) {
		VALIDATOR.checkArgumentClass(getConfig().getResultClass(), baseResult.getClass(), "The base result class is not valid.");
		return parseAllLocal(token, (SingleTrackAlbumBuilder) baseResult);
	}

	private Iterable<SingleTrackAlbumBuilder> parseAllLocal(String input, SingleTrackAlbumBuilder baseResult) {
		final String enhancedInput = enhanceInput(input);
		final List<TrackFormat> formats = getConfig().getFormats();
		formats.forEach(f -> validateRequiredFields(f));
		final int nThreads = formats.size()/2;
		final ExecutorService executor;
		if (nThreads > 1) {
			executor = Executors.newFixedThreadPool(nThreads);
		}
		else {
			executor = Executors.newSingleThreadExecutor();
		}
		final Scheduler scheduler = Schedulers.from(executor);

		return Observable.fromIterable(formats)
		.flatMapSingle(format -> Single.fromCallable(new SingleFormatParser(format, enhancedInput, baseResult, this))
				.observeOn(scheduler))
		.filter(Optional::isPresent)
		.map(Optional::get)
		.blockingIterable();
	}

	public boolean checkSong(Path f) throws InvalidSongFormatException {
		final String format = PathUtils.getFormat(f);

		if(!FormatSong.isAllowed(format)){
			throw new InvalidSongFormatException(f.getFileName().toString());
		}

		return true;
	}

	@Override
	protected SingleTrackAlbumBuilder getDefaultBaseResult() {
		return SingleTrackAlbumBuilder.create();
	}
}
