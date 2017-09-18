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
package parser;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rookit.database.DBManager;
import org.rookit.dm.track.Track;
import org.rookit.dm.utils.PrintUtils;
import org.rookit.parser.exceptions.InvalidSongFormatException;
import org.rookit.parser.parser.Field;
import org.rookit.parser.parser.FormatParser;
import org.rookit.parser.parser.Parser;
import org.rookit.parser.parser.ParserConfiguration;
import org.rookit.parser.parser.ParserFactory;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TrackPath;

import com.google.common.collect.Iterables;

import utils.TestUtils;

@SuppressWarnings("javadoc")
public class FormatParserTest {

	private static ParserFactory parserFactory;
	private static FormatParser parser;
	
	private static final String HOST = "localhost";
	private static final int PORT = 27020;
	private static final String DB_NAME = "rookit_parser_test";
	
	private static DBManager db;
	
	@BeforeClass
	public static void setUp() {
		parserFactory = ParserFactory.create();
		db = DBManager.open(HOST, PORT, DB_NAME);
	}
	
	@Before
	public void before() {
		final ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> config = Parser.createConfiguration(SingleTrackAlbumBuilder.class);
		config.withDBConnection(db)
		//.withBaseParser(PARSER_FACTORY.newTagParserWithDefaultConfiguration())
		.withDbStorage(false)
		.withRequiredFields(Field.getRequiredFields())
		.withSetDate(true)
		.withTrackFormats(TestUtils.getTestFormats());
		parser = parserFactory.newFormatParser(config);
	}
	
	@Test
	public void testDBStorage() {
		final ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> config = Parser.createConfiguration(SingleTrackAlbumBuilder.class);
		config.withDbStorage(true);
		parser = parserFactory.newFormatParser(config);
		assertEquals(true, parser.getDBStorage());
	}
	
	@Test
	public final void testEquals() {
		final FormatParser p1 = parserFactory.newFormatParserWithDefaultConfiguration();
		final FormatParser p2 = parserFactory.newFormatParserWithDefaultConfiguration();
		assertEquals(p2, p1);
	}

	@Test
	public final void testMultiparse() {
		final TrackPath trackPath = TestUtils.getRandomTrackPath();
		final List<SingleTrackAlbumBuilder> results = parser.multiparse(trackPath);
		assertNotNull(results);
		System.out.println(trackPath);
		for(SingleTrackAlbumBuilder result : results) {
			System.out.println(result.getFormat());
			System.out.println("Score: " + result.getScore());
			System.out.println(PrintUtils.track(result.getTrack()));
		} 
	}

	@Test
	public final void testParseString() {
		final TrackFormat format = TrackFormat.create("<ARTIST> - <TITLE> (<IGNORE>)");
		final String artist = "Zabreguelles";
		final String title = "This Title Is Awesome";
		final String ignore = "Ignore me";
		final String str = String.format("%s - %s (%s)", artist, title, ignore);
		parser = parserFactory.newFormatParserWithTrackFormats(Arrays.asList(format));
		final SingleTrackAlbumBuilder result = parser.parse(str, SingleTrackAlbumBuilder.create());
		final Track track = result.getTrack();
		assertEquals(artist, Iterables.get(track.getMainArtists(), 0).getName());
		assertEquals(title, track.getTitle().getTitle());
		assertEquals(ignore, result.getIgnored().get(0));
	}

	@Test
	public final void testCheckSong() throws InvalidSongFormatException {
		parser.checkSong(Paths.get("dir", "valid.mp3"));
	}
	
	@Test(expected = InvalidSongFormatException.class)
	public final void testCheckSongInvalidSongFormatException() throws InvalidSongFormatException {
		parser.checkSong(Paths.get("dir", "invalid.wrongformat"));
	}
	
	
	@Test
	public final void testAmbiguousFormat() {
		final TrackFormat format = TrackFormat.create("[<GENRE>] - <ARTIST> ft. <FEAT> - <TITLES> (<EXTRA> <VERSION>)");
		final String ex = "[Progressive] -  Ale Q & Avedon Ft. Jonathan Mendelsohn -  Open My Eyes (Tom Swoon Edit)";
		parser = parserFactory.newFormatParserWithTrackFormats(Arrays.asList(format));
		final SingleTrackAlbumBuilder result = parser.parse(ex, SingleTrackAlbumBuilder.create());
		System.out.println(PrintUtils.track(result.getTrack()));
	}
	
}
