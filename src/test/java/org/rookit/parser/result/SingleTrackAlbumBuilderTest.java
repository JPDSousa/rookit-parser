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
package org.rookit.parser.result;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.rookit.dm.album.Album;
import org.rookit.dm.artist.Artist;
import org.rookit.dm.genre.Genre;
import org.rookit.dm.track.Track;
import org.rookit.dm.track.TypeTrack;
import org.rookit.dm.track.TypeVersion;
import org.rookit.dm.utils.DMTestFactory;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TestUtils;
import org.rookit.parser.utils.TrackPath;

import com.google.common.collect.Iterables;

@SuppressWarnings("javadoc")
public class SingleTrackAlbumBuilderTest {

	private static final DMTestFactory FACTORY = DMTestFactory.getDefault();
	private static SingleTrackAlbumBuilder guineaPig; 
	
	@Before
	public void setUp() {
		guineaPig = SingleTrackAlbumBuilder.create()
				.withType(FACTORY.getRandomTrackType())
				.withTitle(FACTORY.randomString())
				.withTypeVersion(FACTORY.getRandomVersionType())
				.withExtraArtists(FACTORY.getRandomSetOfArtists())
				.withMainArtists(FACTORY.getRandomSetOfArtists())
				.withFeatures(FACTORY.getRandomSetOfArtists())
				.withProducers(FACTORY.getRandomSetOfArtists())
				.withPath(TestUtils.getRandomTrackPath())
				.withDisc(FACTORY.randomString())
				.withNumber(1)
				.withCover(FACTORY.randomString().getBytes())
				.withAlbum(FACTORY.getRandomAlbum())
				.withDate(LocalDate.now())
				.withAlbumTitle(FACTORY.randomString())
				.withGenres(FACTORY.getRandomSetOfGenres())
				.withHiddenTrack(FACTORY.randomString());
		
	}

	@Test
	public final void testCreate() {
		assertNotNull(SingleTrackAlbumBuilderTest.class);
	}
	
	@Test
	public final void testCreateClone() {
		assertEquals(guineaPig, SingleTrackAlbumBuilder.create(guineaPig));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public final void testCreateCloneFromNull() {
		SingleTrackAlbumBuilder.create((SingleTrackAlbumBuilder) null);
	}
	
	@Test
	public final void testType() {
		final TypeTrack type = FACTORY.getRandomTrackType();
		guineaPig.withType(type);
		assertEquals(type, guineaPig.getType());
	}
	
	@Test
	public final void testVersionType() {
		final SingleTrackAlbumBuilder builder = SingleTrackAlbumBuilder.create();
		final TypeVersion version = FACTORY.getRandomVersionType();
		builder.withTypeVersion(version);
		assertEquals(version, builder.getTypeVersion());
	}
	
	@Test
	public final void testMultipleVersions() {
		guineaPig.withTypeVersion(FACTORY.getRandomVersionType());
	}
	
	@Test
	public final void testEmptyBuilderValidDisc() {
		final Album album = FACTORY.getRandomAlbum();
		final Track track = FACTORY.getRandomTrack();
		final String disc = guineaPig.getDisc();
		assertNotNull(guineaPig.getDisc());
		album.addTrack(track, 1, disc);
		
	}

	@Test
	public final void testBuild() throws IOException {
		final TypeTrack trackType = FACTORY.getRandomTrackType();
		final String hiddenTrack = FACTORY.randomString();
		final String albumTitle = FACTORY.randomString();
		final String disc = FACTORY.randomString();
		final String title = FACTORY.randomString();
		final TypeVersion versionType = FACTORY.getRandomVersionType();
		final Set<Artist> producers = FACTORY.getRandomSetOfArtists();
		final Set<Artist> features = FACTORY.getRandomSetOfArtists();
		final Set<Artist> mainArtists = FACTORY.getRandomSetOfArtists();
		final Set<Artist> extras = FACTORY.getRandomSetOfArtists();
		final byte[] cover = title.getBytes();
		final LocalDate date = LocalDate.now();
		final Set<Genre> genres = FACTORY.getRandomSetOfGenres();
		final int number = 1;
		final TrackPath path = TestUtils.getRandomTrackPath();
		final SingleTrackAlbumBuilder builder = SingleTrackAlbumBuilder.create()
				.withType(trackType)
				.withTitle(title)
				.withTypeVersion(versionType)
				.withExtraArtists(extras)
				.withMainArtists(mainArtists)
				.withFeatures(features)
				.withProducers(producers)
				.withPath(path)
				.withDisc(disc)
				.withNumber(number)
				.withCover(cover)
				.withDate(date)
				.withAlbumTitle(albumTitle)
				.withGenres(genres)
				.withHiddenTrack(hiddenTrack);
		final Album album = builder.build();
		final Track track = Iterables.get(album.getTracks(), 0).getTrack();
		final byte[] actualCover = new byte[title.length()];
		album.getCover().getAttachedByteArray().read(actualCover);
		assertEquals(trackType, track.getType());
		assertEquals(title, track.getTitle().getTitle());
		assertEquals(mainArtists, track.getMainArtists());
		assertEquals(features, track.getFeatures());
		assertEquals(producers, track.getProducers());
		assertEquals(path.getPath(), track.getPath().getAttachedFile());
		assertNotNull(album.getTrack(disc, number));
		assertArrayEquals(cover, actualCover);
		assertEquals(date, album.getReleaseDate());
		assertEquals(albumTitle, album.getTitle());
		assertEquals(genres, album.getAllGenres());
		assertEquals(hiddenTrack, track.getHiddenTrack());
		if(trackType == TypeTrack.VERSION) {
			assertEquals(versionType, track.getAsVersionTrack().getVersionType());
			assertEquals(extras, track.getAsVersionTrack().getVersionArtists());
		}
	}
	
}
