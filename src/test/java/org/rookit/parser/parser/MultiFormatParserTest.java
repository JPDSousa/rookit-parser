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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rookit.api.dm.factory.RookitFactories;
import org.rookit.api.dm.track.Track;
import org.rookit.api.storage.DBManager;
import org.rookit.dm.inject.DMFactoriesModule;
import org.rookit.dm.utils.DMPrintUtils;
import org.rookit.mongodb.inject.morphia.MorphiaModule;
import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.exceptions.InvalidSongFormatException;
import org.rookit.parser.parser.Field;
import org.rookit.parser.parser.MultiFormatParser;
import org.rookit.parser.parser.Parser;
import org.rookit.parser.parser.ParserFactory;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TestUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.inject.Guice;
import com.google.inject.Injector;

@SuppressWarnings("javadoc")
public class MultiFormatParserTest {

	private static DBManager database;
	private static RookitFactories factories;
	private static ParserFactory parserFactory;

	private Parser<String, SingleTrackAlbumBuilder> parser;
	private ParserConfiguration config;

	@BeforeClass
	public static void setUp() {
		final Injector injector = Guice.createInjector(new DMFactoriesModule(), 
				new MorphiaModule());
		factories = injector.getInstance(RookitFactories.class);
		parserFactory = ParserFactory.create();
		database = injector.getInstance(DBManager.class);
	}

	@Before
	public void before() throws IOException {
		config = Parser.createConfiguration(SingleTrackAlbumBuilder.class).withDbStorage(true)
				.withDBConnection(database)
				.withRequiredFields(Field.getRequiredFields())
				.withSetDate(true)
				.withTrackFormats(TestUtils.getTestFormats());
		parser = parserFactory.newFormatParser(config);
	}

	@Test
	public final void testEquals() {
		final Parser<String, SingleTrackAlbumBuilder> p1 = parserFactory.newFormatParserWithDefaultConfiguration();
		final Parser<String, SingleTrackAlbumBuilder> p2 = parserFactory.newFormatParserWithDefaultConfiguration();
		assertThat(p1).isEqualTo(p2);
	}

	private final void testMultiparse(String input) {
		final Iterable<SingleTrackAlbumBuilder> results = parser.parseAll(input);
		assertThat(results).isNotNull();
		System.out.println(input);
		for(SingleTrackAlbumBuilder result : results) {
			System.out.println(result.getFormat());
			System.out.println("Score: " + result.getScore());
			System.out.println(DMPrintUtils.track(result.buildTrack()));
		} 
	}

	@Test
	public final void testMultiparse() {
		testMultiparse("Artist1 - Track1 (feat. Artist2)");
	}

	@Test
	public final void testMultiParse7() {
		testMultiparse("A R I Z O N A - Electric Touch (Lyrics _ Lyric Video)");
	}

	@Test
	public final void testParse() {
		final TrackFormat format = TrackFormat.create("<ARTIST> - <TITLE> (<IGNORE>)");
		final String artist = "Zabreguelles";
		final String title = "This Title Is Awesome";
		final String ignore = "Ignore me";
		final String str = String.format("%s - %s (%s)", artist, title, ignore);
		parser = parserFactory.newFormatParser(config.withTrackFormats(Arrays.asList(format)));
		final Optional<SingleTrackAlbumBuilder> result = parser
				.parse(str, SingleTrackAlbumBuilder.create(factories));
		assertThat(result.isPresent()).isTrue();
		final Track track = result.get().buildTrack();
		assertThat(Iterables.get(track.getMainArtists(), 0).getName())
		.isEqualTo(artist);
		assertThat(track.getTitle().getTitle()).isEqualTo(title);
		assertThat(result.get().getIgnored().get(0)).isEqualTo(ignore);
	}

	@Test
	public final void testCheckSong() throws InvalidSongFormatException {
		((MultiFormatParser) parser).checkSong(Paths.get("dir", "valid.mp3"));
		assertThat(true).isTrue();
	}

	@Test(expected = InvalidSongFormatException.class)
	public final void testCheckSongInvalidSongFormatException() throws InvalidSongFormatException {
		((MultiFormatParser) parser).checkSong(Paths.get("dir", "invalid.wrongformat"));
		fail("Should have thrown exception");
	}


	@Test
	public final void testAmbiguousFormat() {
		final TrackFormat format = TrackFormat.create("[<GENRE>] - <ARTIST> ft. <FEAT> - <TITLES> (<EXTRA> <VERSION>)");
		final String ex = "[Progressive] -  Ale Q & Avedon Ft. Jonathan Mendelsohn -  Open My Eyes (Tom Swoon Edit)";
		parser = parserFactory.newFormatParser(config.withTrackFormats(Arrays.asList(format)));
		final Optional<SingleTrackAlbumBuilder> result = parser.parse(ex, 
				SingleTrackAlbumBuilder.create(factories));
		System.out.println(DMPrintUtils.track(result.get().buildTrack()));
	}

}
