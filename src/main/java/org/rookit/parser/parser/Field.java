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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;
import org.rookit.mongodb.DBManager;
import org.rookit.dm.artist.Artist;
import org.rookit.dm.artist.ArtistFactory;
import org.rookit.dm.genre.GenreFactory;
import org.rookit.dm.track.Track;
import org.rookit.dm.track.TypeVersion;
import org.rookit.parser.result.SingleTrackAlbumBuilder;

interface InitialScores{
	//No value
	static final int NO_VALUE = 0;
	//Level 1 value
	static final int L1_VALUE = 1;
	//Level 2 value
	static final int L2_VALUE = 3;
}

/**
 * Enumeration that manages the types of possible fields
 * defined in the {@link org.rookit.parser.parser.TrackFormat} class.
 *
 */
public enum Field {
	/**
	 * Track number
	 */
	NUMBER(InitialScores.L2_VALUE){
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			track.withNumber(Integer.parseInt(values.get(0)));
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			int isNumber = 1;

			for(int i = 0; i < value.length() && isNumber>0; i++){
				if(!Character.isDigit(value.charAt(i))){
					isNumber = -2;
				}
			}

			return isNumber*getScore();
		}
	},
	/**
	 * Track title
	 */
	TITLE(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			track.withTitle(WordUtils.capitalizeFully(values.get(0)));
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			int isValid = 1;

			for(String token : Track.SUSPICIOUS_TITLE_CHARSEQS){
				if(value.contains(token)){
					isValid = -1;
					break;
				}
			}

			return isValid*getScore();
		}
	},

	/**
	 * Main artist(s)
	 * <p>Note: will be automatically separated, so no need for manual
	 * separation.
	 */
	ARTIST(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {			
			track.withMainArtists(values.stream()
					.flatMap(v -> ArtistFactory.getDefault().getArtistsFromFormat(v).stream())
					.collect(Collectors.toSet()));
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			final DBManager db = context.getDBConnection();
			int isValid = 1;
			int dbScore = 0;

			for(String token : Artist.SUSPICIOUS_NAME_CHARSEQS){
				if(value.contains(token)){
					isValid = -1;
					break;
				}
			}
			if(isValid > 0 && context.isStoreDB()) {
				dbScore = db.getArtists()
						.withName(value)
						.first() != null ? 10 : -1;
			}

			return isValid*(getScore()+dbScore);
		}
	},
	/**
	 * Featuring artist(s)
	 * <p>Note: like {@link Field#ARTIST}, artists will be automatically
	 * separated.
	 */
	FEAT(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			track.withFeatures(values.stream()
					.flatMap(v -> ArtistFactory.getDefault().getArtistsFromFormat(v).stream())
					.collect(Collectors.toSet()));
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			return ARTIST.getScore(value, context);
		}
	},
	/**
	 * Song producers
	 * <p>Note: bare in mind that artists and features have their own fields ({@link Field#ARTIST}
	 * and {@link Field#FEAT} respectively) which should always be used instead of this field. This
	 * field only applies to additional artists who are only credited by the production of the track.
	 */
	PRODUCER(InitialScores.L2_VALUE) {
		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			return Field.ARTIST.getScore(value, context);
		}

		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			track.withProducers(values.stream()
					.flatMap(v -> ArtistFactory.getDefault().getArtistsFromFormat(v).stream())
					.collect(Collectors.toSet()));
		}

	},
	/**
	 * Album title
	 */
	ALBUM(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			final String title = WordUtils.capitalizeFully(values.get(0));
			track.withAlbumTitle(title);
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			return getScore();
		}
	},	
	/**
	 * Track title with the correspondent
	 * album being a single with the same
	 * title
	 */
	TITLES(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context)  {
			TITLE.setField(track, values, context);
			ALBUM.setField(track, values, context);
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			return TITLE.getScore(value, context)+ALBUM.getScore(value, context);
		}
	},

	/**
	 * Track Genre
	 */
	GENRE(InitialScores.L1_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			track.withGenres(values.stream()
					.map(v -> GenreFactory.getDefault().createGenre(v))
					.collect(Collectors.toSet()));
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			final DBManager db = context.getDBConnection();
			final int dbScore;
			if(context.isStoreDB()) {
				dbScore = db.getGenres()
						.withName(value)
						.first() != null ? 10 : -1;
			}
			else {
				dbScore = 0;
			}
			return getScore()+dbScore;
		}
	},
	VERSION(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			track.withTypeVersion(TypeVersion.parse(values.get(0)));
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			return Arrays.stream(TypeVersion.values())
					.flatMap(version -> Arrays.stream(version.getTokens()))
					.filter(token -> value.equalsIgnoreCase(token))
					.findFirst()
					.isPresent() ? getScore() : -20;
		}
	},
	/**
	 * Extra artist(s)
	 * <p> this field refers to the remix/cover artists.
	 * Note: like {@link Field#ARTIST}, artists will be automatically
	 * separated. 
	 */
	EXTRA(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			track.withExtraArtists(values.stream()
					.flatMap(v -> ArtistFactory.getDefault().getArtistsFromFormat(v).stream())
					.collect(Collectors.toSet()));
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			return ARTIST.getScore(value, context);
		}
	},
	/**
	 * Main artist and extra
	 * <p>This field is useful for VIP remixes and artist that
	 * somehow self-cover their own songs.
	 */
	ARTIST_EXTRA(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			ARTIST.setField(track, values, context);
			EXTRA.setField(track, values, context);
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			return ARTIST.getScore(value, context);
		}
	},
	/**
	 * Hidden track
	 * <p> Use this field to refer to a hidden track in another track
	 * the field is associated with the title of the hidden track, as all the
	 * other tags will be associated with the main track.
	 * Note: use {@link TrackFactory#extractHiddenTrack(track.Track)} to create
	 * an hidden track out of the title string.
	 */
	HIDDEN(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			track.withHiddenTrack(values.get(0));
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			return TITLE.getScore(value, context);
		}
	},
	/**
	 * Field that represents ignored information.
	 * <p> Use this field for dynamic information that is not relevant to the
	 * format. Attention: do not abuse from this field, since the more specific
	 * the format is, the more accurate is the result.
	 */
	IGNORE(InitialScores.NO_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context) {
			values.forEach(i -> track.withIgnore(i));
		}

		@Override
		public int getScore(String value, ParserConfiguration<?, ?> context) {
			final DBManager db = context.getDBConnection();
			final int occurrences = db != null ? db.getIgnoredOccurrences(value) : 0;
			return getScore() + occurrences;
		}
	};

	private static final Field[] REQUIRED_FIELDS = new Field[]{IGNORE, ARTIST, FEAT, GENRE};
	private static final Field[][] ALTERNATIVES = new Field[][]{{TITLES, TITLE}, {ARTIST, ARTIST_EXTRA}};

	private final int score;

	private Field(int score){
		this.score = score;
	}

	@Override
	public String toString(){
		return "<"+name()+">";
	}

	int getScore() {
		return score;
	}

	/**
	 * Calculates the score of the field based on the value passed as parameter.
	 * The score returned can be negative, meaning that the value might not be suitable
	 * for the field. E.g. Passing a non-numeric value to the {@link Field#NUMBER} field
	 * will result in a negative score, since this field expects numeric values.
	 * 
	 * <p>Note: when a field has multiple values, this method is invoked independently
	 * for each value.
	 * 
	 * @param value value of the field. Use the field's value only.
	 * @return An integer with the score calculated based on the value.
	 */
	public abstract int getScore(String value, ParserConfiguration<?, ?> context);

	/**
	 * Applies the field's values to the track passed as parameter.
	 * 
	 * @param track track object which will be affected with the values
	 * passed.
	 * @param values field values. These values will affect the track passed
	 * as parameter.
	 * @throws NumberFormatException thrown by {@link Field#NUMBER}. The value is not numeric.
	 */
	public abstract void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration<?, ?> context);

	/**
	 * Returns a sub-set of fields with the required fields that all formats
	 * require when ready for formatting.
	 * 
	 * @see exceptions.MissingRequiredFieldException
	 * 
	 * @return a sub-set of the required fields
	 */
	public static Field[] getRequiredFields(){
		return new Field[]{TITLE, ARTIST};
	}

	/**
	 * Returns a subset of the fields that can happen more than once in a format
	 * 
	 * @return subset of duplicable fields
	 */
	public static Field[] getDuplicableFields(){
		return REQUIRED_FIELDS;
	}

	/**
	 * Returns the alternative field to the field passed as parameter.
	 * <p> This method is useful when checking for the required field in
	 * a format. So, for instance, if field B is an alternative to field A,
	 * in case field A is missing but field B is not, the parser won't
	 * consider the missing field.
	 * 
	 * @param field the field to find the correspondent alternative in the table
	 * of alternatives
	 * @return the alternative field to the field passed as parameter
	 */
	public static Field getAlternative(Field field){
		Field alternative = null;

		for(int i=0; i< ALTERNATIVES.length && alternative == null; i++){
			if(ALTERNATIVES[i][0] == field){
				alternative = ALTERNATIVES[i][1];
			}
			else if(ALTERNATIVES[i][1] == field){
				alternative = ALTERNATIVES[i][0];
			}
		}

		return alternative;
	}
}
