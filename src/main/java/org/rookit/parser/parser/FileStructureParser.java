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

import org.rookit.dm.album.Album;
import org.rookit.dm.artist.ArtistFactory;
import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.exceptions.UnknownFileSctrutureException;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TrackPath;

class FileStructureParser extends AbstractParser<TrackPath, SingleTrackAlbumBuilder> {

	private static enum StructureLevel{
		STANDART(3, "\\ARTIST_NAME\\ALBUM_NAME\\TRACK_NAME"),
		MULTIPLE_DISC(4, "\\ARTIST_NAME\\ALBUM_NAME\\DISC_NAME\\TRACK_NAME");

		private final int level;
		private final String pathDisposition;

		private StructureLevel(final int level, final String pathDisposition) {
			this.level = level;
			this.pathDisposition = pathDisposition;
		}

		private String getPathDisposition() {
			return pathDisposition;
		}

		private int getLevel() {
			return level;
		}

		private static StructureLevel getByLevel(final int level) throws UnknownFileSctrutureException{
			StructureLevel value = null;

			for(StructureLevel s : values()){
				if(s.getLevel() == level){
					value = s;
					break;
				}
			}

			if(value == null){
				throw new UnknownFileSctrutureException(allToString());
			}
			return value;
		}

		private static String allToString(){
			final StringBuilder builder = new StringBuilder();
			for(StructureLevel s : values()){
				builder.append(s.name()).append("\n\t\t")
				.append(s.getPathDisposition()).append("\n");
			}

			return builder.toString();
		}
	}
	
	static FileStructureParser create(ParserConfiguration config) {
		return new FileStructureParser(config);
	}

	private FileStructureParser(ParserConfiguration config){
		super(config);
	}


	private int getHopsTillMainFolder(TrackPath trackPath) {
		return trackPath.getPath().getNameCount();
	}

	@Override
	protected SingleTrackAlbumBuilder parseFromBaseResult(TrackPath trackPath, SingleTrackAlbumBuilder baseResult) {
		final ArtistFactory artistFactory = ArtistFactory.getDefault();
		final String albumName;
		final String artistName;
		final String discName;
		final Path currentPath = trackPath.getPath();
		
		try {
			switch(StructureLevel.getByLevel(getHopsTillMainFolder(trackPath))){
			case MULTIPLE_DISC:
				discName = currentPath.getParent().getFileName().toString();
				albumName = currentPath.getParent().getParent().getFileName().toString();
				artistName = currentPath.getParent().getParent().getParent().getFileName().toString();
				break;
			case STANDART:
				discName = Album.DEFAULT_DISC;
				albumName = currentPath.getParent().getFileName().toString();
				artistName = currentPath.getParent().getParent().getFileName().toString();
				break;
			default:
				discName = null;
				albumName = null;
				artistName = null;
				break;
			}
			baseResult.withMainArtists(artistFactory.getArtistsFromFormat(artistName));
			baseResult.withDisc(discName);
			baseResult.withAlbumTitle(albumName);
		} catch (NumberFormatException | UnknownFileSctrutureException e) {
			VALIDATOR.handleParseException(e);
			return null;
		}

		return baseResult;
	}

	@Override
	protected SingleTrackAlbumBuilder getDefaultBaseResult() {
		return SingleTrackAlbumBuilder.create();
	}
	
}
