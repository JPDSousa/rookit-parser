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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.rookit.mongodb.DBManager;
import org.rookit.mongodb.queries.AlbumQuery;
import org.rookit.mongodb.queries.ArtistQuery;
import org.rookit.mongodb.queries.GenreQuery;
import org.rookit.mongodb.queries.TrackQuery;
import org.rookit.dm.album.Album;
import org.rookit.dm.artist.Artist;
import org.rookit.dm.genre.Genre;
import org.rookit.dm.parser.IgnoreField;
import org.rookit.dm.track.Track;
import org.rookit.parser.parser.Field;
import org.rookit.parser.parser.Parser;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.smof.gridfs.SmofGridRef;

@SuppressWarnings("javadoc")
public class ParserConfigurationTest {

	private static ParserConfiguration config;
	
	@Before
	public void setUp() {
		config = Parser.createConfiguration(SingleTrackAlbumBuilder.class);
	}
	
	@Test
	public final void testDBConnection() {
		final DBManager db = new DumbDB();
		config.withDBConnection(db);
		assertEquals(db, config.getDBConnection());
		//test null db (allowed)
		config.withDBConnection(null);
	}
	
	@Test
	public final void testSetDate() {
		final boolean date = true;
		config.withSetDate(date);
		assertEquals(date, config.isSetDate());
	}
	
	@Test
	public final void testRequiredFields() {
		final Field[] fields = Field.getRequiredFields();
		config.withRequiredFields(fields);
		assertArrayEquals(fields, config.getRequiredFields());
		//test null fields (allowed)
		config.withRequiredFields(null);
	}
	
	@Test
	public final void testDbStorage() {
		final boolean store = true;
		config.withDbStorage(store)
		.withDBConnection(new DumbDB());
		assertEquals(store, config.isStoreDB());
	}
	
	@Test
	public final void testTrackFormats() {
		final List<TrackFormat> formats = new ArrayList<>();
		config.withTrackFormats(formats);
		assertEquals(formats, config.getFormats());
		//test null formats (allowed)
		config.withTrackFormats(null);
	}
	
	private static class DumbDB implements DBManager {

		@Override
		public void reset() {
			//intentionally empty
		}

		@Override
		public void init() {
			//intentionally empty
		}

		@Override
		public void clear() {
			//intentionally empty
		}

		@Override
		public int getIgnoredOccurrences(String value) {
			//intentionally empty
			return 0;
		}

		@Override
		public void close() throws IOException {
			//intentionally empty
		}

		@Override
		public void addAlbum(Album arg0) {
			//intentionally empty
		}

		@Override
		public void addArtist(Artist arg0) {
			//intentionally empty
		}

		@Override
		public void addGenre(Genre arg0) {
			//intentionally empty
		}

		@Override
		public void addTrack(Track arg0) {
			//intentionally empty
		}

		@Override
		public AlbumQuery getAlbums() {
			//intentionally empty
			return null;
		}

		@Override
		public ArtistQuery getArtists() {
			//intentionally empty
			return null;
		}

		@Override
		public GenreQuery getGenres() {
			//intentionally empty
			return null;
		}

		@Override
		public TrackQuery getTracks() {
			//intentionally empty
			return null;
		}

		@Override
		public void updateAlbum(Album arg0) {
			//intentionally empty
		}

		@Override
		public void updateArtist(Artist arg0) {
			//intentionally empty
		}

		@Override
		public void updateGenre(Genre arg0) {
			//intentionally empty
		}

		@Override
		public void updateIgnored(IgnoreField arg0) {
			//intentionally empty
		}

		@Override
		public void updateTrack(Track arg0) {
			//intentionally empty
		}

		@Override
		public void loadBucket(String arg0) {
			//intentionally empty
		}

		@Override
		public byte[] download(SmofGridRef arg0) {
			//intentionally empty
			return null;
		}

		@Override
		public InputStream stream(SmofGridRef arg0) {
			//intentionally empty
			return null;
		}

		@Override
		public int getTrackFormatOccurrences(String arg0) {
			//intentionally empty
			return 0;
		}

		@Override
		public void updateTrackFormat(org.rookit.dm.parser.TrackFormat arg0) {
			//intentionally empty
		}

		@Override
		public Stream<String> streamTrackFormats() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
