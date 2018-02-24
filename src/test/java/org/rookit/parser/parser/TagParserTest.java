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
package org.rookit.parser.parser;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rookit.api.storage.DBManager;
import org.rookit.dm.inject.DMFactoriesModule;
import org.rookit.dm.test.DMTestFactory;
import org.rookit.mongodb.inject.morphia.MorphiaModule;
import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.parser.Field;
import org.rookit.parser.parser.Parser;
import org.rookit.parser.parser.ParserFactory;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TestUtils;
import org.rookit.parser.utils.TrackPath;

import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mpatric.mp3agic.Mp3File;

@SuppressWarnings("javadoc")
public class TagParserTest {

	private static DMTestFactory factory;
	private static final ParserFactory PARSER_FACTORY = ParserFactory.create();
	private static DBManager database;
	
	private Parser<TrackPath, SingleTrackAlbumBuilder> parser;

	@BeforeClass
	public static final void setUpBeforeClass() {
		final Injector injector = Guice.createInjector(DMTestFactory.getModule(), 
				new DMFactoriesModule(), new MorphiaModule());
		factory = injector.getInstance(DMTestFactory.class);
		database = injector.getInstance(DBManager.class);
	}
	
	@Before
	public void setUp() {
		final ParserConfiguration config = Parser.createConfiguration(
				SingleTrackAlbumBuilder.class);
		config.withDbStorage(true)
		.withRequiredFields(Field.getRequiredFields())
		.withDBConnection(database)
		.withSetDate(true);
		parser = PARSER_FACTORY.newTagParser(config);
	}

	/**
	 * Test method for {@link parsers.TagParser#parse(TrackPath)}.
	 */
	@Test
	public final void testParse() {
		final TrackPath trackPath = TestUtils.getRandomTrackPath();
		final Optional<SingleTrackAlbumBuilder> result = parser.parse(trackPath);
		assertThat(result.isPresent()).isTrue();
		assertThat(result.get().getDuration().get().toMillis())
		.isEqualTo(trackPath.getDurationMiliSec());
	}
	
	@Test
	public final void testParseNoMetadata() throws IOException {
		final SingleTrackAlbumBuilder expected = SingleTrackAlbumBuilder.create(
				factory.getFactories());
		final Path sourcePath = TestUtils.getRandomTrackPath().getPath();
		final Path targetPath = sourcePath.getParent().resolve("test.mp3");
		Files.deleteIfExists(targetPath);
		Files.copy(sourcePath, targetPath);
		final TrackPath trackPath = TrackPath.create(targetPath);
		final Mp3File mp3 = trackPath.getMp3();
		mp3.removeCustomTag();
		mp3.removeId3v1Tag();
		mp3.removeId3v2Tag();
		trackPath.updateMP3(mp3);
		final Optional<SingleTrackAlbumBuilder> result = parser.parse(trackPath, expected);
		assertThat(result.isPresent()).isTrue();
		assertThat(result.get()).isEqualTo(expected);
		Files.delete(targetPath);
	}

	/**
	 * Test method for {@link parsers.AbstractParser#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
		final Parser<TrackPath, SingleTrackAlbumBuilder> p1 = PARSER_FACTORY.newTagParserWithDefaultConfiguration();
		final Parser<TrackPath, SingleTrackAlbumBuilder> p2 = PARSER_FACTORY.newTagParserWithDefaultConfiguration();
		assertThat(p1).isEqualTo(p2);
	}

}
