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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rookit.api.dm.album.Album;
import org.rookit.api.dm.factory.RookitFactories;
import org.rookit.dm.inject.DMFactoriesModule;
import org.rookit.dm.test.DMTestFactory;
import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.parser.ParserFactory;
import org.rookit.parser.parser.ParserPipeline;
import org.rookit.parser.result.Result;
import org.rookit.parser.result.SingleTrackAlbumBuilder;

import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;

@SuppressWarnings("javadoc")
public class ParserPipelineTest {
	
	private static DMTestFactory dataFactory;
	private static ParserFactory factory;
	private static RookitFactories factories;
	
	@BeforeClass
	public static final void setup() {
		final Injector injector = Guice.createInjector(DMTestFactory.getModule(), 
				new DMFactoriesModule());
		factory = ParserFactory.create();
		factories = injector.getInstance(RookitFactories.class);
		dataFactory = injector.getInstance(DMTestFactory.class);
	}
	
	private SingleTrackAlbumBuilder getBasicSingleTrackAlbumBuilder() {
		return SingleTrackAlbumBuilder.create(factories);
	}
	
	@Test 
	public final void testInputMapping() {
		final SingleTrackAlbumBuilder baseResult = getBasicSingleTrackAlbumBuilder();
		final ParserPipeline<Integer, String, SingleTrackAlbumBuilder> pipeline = factory
				.newParserPipeline(Integer.class, baseResult)
				.mapInput(i -> Integer.toString(i));
		final Integer testInt = 5;
		assertThat(pipeline.input2CurrentInput(testInt)).isEqualTo(Integer.toString(5));
	}
	
	@Test
	public final void testBottom() {
		final ParserConfiguration config = Parser.createConfiguration((Class<? extends Result<?>>) SingleTrackAlbumBuilder.class);
		final SingleTrackAlbumBuilder baseResult = getBasicSingleTrackAlbumBuilder();
		final ParserPipeline<String, String, SingleTrackAlbumBuilder> pipeline = factory.newParserPipeline(String.class, baseResult);
		final String testStr = "someRandomString";
		assertThat(pipeline).isNotNull();
		assertThat(pipeline.getConfig()).isEqualTo(config);
		assertThat(pipeline.parse(testStr)).isEqualTo(Optional.of(baseResult));
		assertThat(pipeline.parse(testStr, baseResult))
		.isEqualTo(Optional.of(baseResult));
		assertThat(pipeline.input2CurrentInput(testStr)).isEqualTo(testStr);
		assertThat(pipeline.parse(testStr).get()).isEqualTo(baseResult);
	}
	
	@Test
	public final void testParser() {
		final Album album = dataFactory.getRandomAlbum();
		final ParserConfiguration config = Parser.createConfiguration((Class<? extends Result<?>>) SingleTrackAlbumBuilder.class);
		final Result<Album> baseResult = getBasicSingleTrackAlbumBuilder();
		final ParserPipeline<Album, Album, Result<Album>> pipeline = factory
				.newParserPipeline(Album.class, baseResult)
				.insert(createParser(album, config));
		
		assertThat(pipeline).isNotNull();
		assertThat(pipeline.getConfig()).isEqualTo(config);
		assertThat(pipeline.parse(album).get().build()).isEqualTo(album);
		assertThat(pipeline.parse(album, baseResult).get().build())
		.isEqualTo(album);
	}
	
	@SuppressWarnings("unchecked")
	private final <T> Parser<T, Result<T>> createParser(T item, ParserConfiguration config) {
		final Result<T> result = mock(Result.class);
		final Optional<Result<T>> oResult = Optional.of(result);
		final Parser<T, Result<T>> parser = mock(Parser.class);
		when(result.build()).thenReturn(item);
		when(parser.parse(any())).thenReturn(oResult);
		when(parser.parse(any(), any())).thenReturn(oResult);
		when(parser.parseAll(any())).thenReturn(Arrays.asList(result));
		when(parser.parseAll(any(), any())).thenReturn(Arrays.asList(result));
		when(parser.getConfig()).thenReturn(config);
		
		return parser;
	}
	
	

}
