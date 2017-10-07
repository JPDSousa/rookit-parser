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
/**
 * 
 */
package parser;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.rookit.dm.utils.PrintUtils;
import org.rookit.parser.parser.Field;
import org.rookit.parser.parser.Parser;
import org.rookit.parser.parser.ParserConfiguration;
import org.rookit.parser.parser.ParserFactory;
import org.rookit.parser.parser.TagParser;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TrackPath;

import utils.TestUtils;

@SuppressWarnings("javadoc")
public class TagParserTest {

	private static final ParserFactory PARSER_FACTORY = ParserFactory.create();
	private static TagParser parser;

	@Before
	public void setUp() {
		final ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> config = Parser.createConfiguration(SingleTrackAlbumBuilder.class);
		config.withDbStorage(false)
		.withRequiredFields(Field.getRequiredFields())
		.withSetDate(true);
		parser = PARSER_FACTORY.newTagParser(config);
	}

	/**
	 * Test method for {@link parsers.TagParser#parse(TrackPath)}.
	 */
	@Test
	public final void testParse() {
		final TrackPath trackPath = TestUtils.getRandomTrackPath();
		final SingleTrackAlbumBuilder result = parser.parse(trackPath);
		assertNotNull(result);
		assertEquals(trackPath.getDurationMiliSec(), result.getDuration());
	}

	/**
	 * Test method for {@link parsers.AbstractParser#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
		final TagParser p1 = PARSER_FACTORY.newTagParserWithDefaultConfiguration();
		final TagParser p2 = PARSER_FACTORY.newTagParserWithDefaultConfiguration();
		assertEquals(p2, p1);
	}

}
