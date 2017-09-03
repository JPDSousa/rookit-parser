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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;
import org.rookit.dm.album.Album;
import org.rookit.dm.album.AlbumFactory;
import org.rookit.dm.artist.Artist;
import org.rookit.dm.artist.ArtistFactory;
import org.rookit.parser.exceptions.SuspiciousCharSeqException;
import org.rookit.parser.exceptions.SuspiciousDuplicateException;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TrackPath;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

@SuppressWarnings("javadoc")
public class TagParser extends AbstractParser<TrackPath, SingleTrackAlbumBuilder> {

	private static final String[] SUSPICIOUS_STRINGS = {"   "};
	private static final String[] SUSPICIOUS_DUPLICATE = {"-1"};
	
	static TagParser create(ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> config) {
		return new TagParser(config);
	}

	private TagParser(ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> config){
		super(config);
	}

	@Override
	protected SingleTrackAlbumBuilder parse(TrackPath path, SingleTrackAlbumBuilder track) {
		final Mp3File mp3;
		try {
			isSuspicious(path.getPath());
			mp3 = getMp3(path);
			track.withPath(path.getAbsolutePath());
			
			if(mp3.hasId3v1Tag()){
				setV1Tags(track, mp3.getId3v1Tag());
			}
			if(mp3.hasId3v2Tag()){
				setV2Tags(track, mp3.getId3v2Tag());
			}

			return track;
		} catch (UnsupportedTagException | SuspiciousCharSeqException | SuspiciousDuplicateException  e) {
			VALIDATOR.handleParseException(e);
			return null;
		}
	}

	private Mp3File getMp3(TrackPath path) throws UnsupportedTagException {
		final Mp3File mp3 = path.getMp3();
		if(!mp3.hasId3v2Tag() && !mp3.hasId3v1Tag()){
			throw new UnsupportedTagException();
		}
		return mp3;
	}

	private void setV2Tags(SingleTrackAlbumBuilder track, ID3v2 tags) {
		final String album = tags.getAlbum();
		final String albumArtists = tags.getAlbumArtist();
		final String artists = tags.getArtist();

		track.withDisc(Album.DEFAULT_DISC)
		.withCover(tags.getAlbumImage())
		.withAlbum(getAlbum(album, albumArtists))
		.withDate(getDate(tags.getYear()))
		.withMainArtists(getMainArtists(albumArtists))
		.withFeatures(getFeatures(albumArtists, artists))
		.withNumber(getTrackNumber(tags.getTrack()))
		.withTitle(getTrackTitle(tags.getTitle()));
	}

	private void setV1Tags(SingleTrackAlbumBuilder track, ID3v1 tags) {
		final String artists = tags.getArtist();

		track.withDisc(Album.DEFAULT_DISC)
		.withMainArtists(getMainArtists(artists))
		.withAlbum(getAlbum(tags.getAlbum(), artists))
		.withDate(getDate(tags.getYear()))
		.withNumber(getTrackNumber(tags.getTrack()))
		.withTitle(getTrackTitle(tags.getTitle()));
	}

	private Set<Artist> getFeatures(final String albumArtists, final String artists) {
		if(artists != null && albumArtists != null){
			return ArtistFactory.getDefault().createFeatArtists(artists, albumArtists);
		}
		return null;
	}

	private Set<Artist> getMainArtists(String albumArtists) {
		if(albumArtists != null){
			return ArtistFactory.getDefault().getArtistsFromTag(albumArtists);
		}
		return null;
	}

	private Album getAlbum(String album, String albumArtists) {
		if(album != null && albumArtists != null && album.length() > 0){
			return AlbumFactory.getDefault().createSingleArtistAlbum(album, albumArtists);
		}
		return null;
	}

	private LocalDate getDate(String date) {
		if(getConfig().isSetDate() && date != null){
			try{
				return LocalDate.parse(date, DateTimeFormatter.ofPattern(Album.DATE_FORMAT));
			} catch (IllegalArgumentException e){
				return getAlternativeDate(date);
			}
		}
		return null;
	}

	private LocalDate getAlternativeDate(String date) {
		for(String format : Album.DATE_FORMAT_ALT){
			try{
				return LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
			} catch (IllegalArgumentException e1){
				continue;
			}
		}
		VALIDATOR.info("Date format not recognized: " + date);
		return LocalDate.now();
	}

	private String getTrackTitle(String title) {
		if(title != null){
			return WordUtils.capitalizeFully(title);
		}
		return null;
	}

	private Integer getTrackNumber(String trackNumber) {
		final int index;
		String number;

		if(trackNumber != null && trackNumber.length()>0){
			if((index = trackNumber.indexOf('/')) > 0){
				number = trackNumber.substring(0, index);
			}
			else{
				number = trackNumber;
			}
			return Integer.parseInt(number);
		}
		return Album.NUMBERLESS;
	}

	private static void isSuspicious(Path song) throws SuspiciousCharSeqException, SuspiciousDuplicateException{
		final String name = song.getFileName().toString();

		for(String suspicious : SUSPICIOUS_STRINGS){
			if(name.contains(suspicious)){
				throw new SuspiciousCharSeqException(suspicious, name);
			}
		}

		for(String suspicious : SUSPICIOUS_DUPLICATE){
			if(name.contains(suspicious)){
				throw new SuspiciousDuplicateException(name);
			}
		}
	}
}
