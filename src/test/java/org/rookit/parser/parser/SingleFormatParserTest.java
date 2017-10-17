package org.rookit.parser.parser;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TestUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

@SuppressWarnings("javadoc")
public class SingleFormatParserTest {

	private static Parser<String, SingleTrackAlbumBuilder> parser;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final ParserConfiguration config = Parser.createConfiguration(SingleTrackAlbumBuilder.class);
		config.withDbStorage(false)
		.withRequiredFields(Field.getRequiredFields())
		.withSetDate(true)
		.withTrackFormats(TestUtils.getTestFormats());
		parser = ParserFactory.create().newFormatParser(config);
	}
	
	@Test
	public final void testResult1() {
		final TrackFormat format = TrackFormat.create("<ARTIST_EXTRA> - <TITLES> <VERSION>");
		final String input = "Dewian Gross - Free To Go";
		parser = createParser(input, format);
		assertNull(parser.parse(input));
	}

	@Test
	public final void testScore1() {
		final String artist1 = "Artist1";
		final String artist2 = "Artist2";
		final String track1 = "Track1";
		final String input = artist1 + " - " + track1 + " (feat. " + artist2 + ")";
		final TrackFormat format = TrackFormat.create("<ARTIST> - <TITLES> (feat. <FEAT>)");
		final Parser<String, SingleTrackAlbumBuilder> parser = createParser(input, format);
		final Map<Field, String> values = Maps.newLinkedHashMap();
		final SingleTrackAlbumBuilder result = parser.parse(input);
		values.put(Field.ARTIST, artist1);
		values.put(Field.TITLES, track1);
		values.put(Field.FEAT, artist2);
		final int expected = values.keySet().stream()
				.mapToInt(field -> field.getScore(values.get(field), parser.getConfig()))
				.sum();
		assertEquals(expected, result.getScore());
	}
	
	private Parser<String, SingleTrackAlbumBuilder> createParser(String input, TrackFormat format) {
		final SingleTrackAlbumBuilder base = SingleTrackAlbumBuilder.create();
		return new SingleFormatParser(format, input, base, parser, Queues.newArrayDeque());
	}

}
