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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.WordUtils;
import org.rookit.api.dm.artist.factory.ArtistFactory;
import org.rookit.api.dm.genre.factory.GenreFactory;
import org.rookit.api.dm.track.TypeVersion;
import org.rookit.api.storage.DBManager;
import org.rookit.parser.config.ParserConfiguration;
import org.rookit.parser.result.SingleTrackAlbumBuilder;

interface InitialScores{
	//No value
	static final int NO_VALUE = 0;
	//Level 1 value
	static final int L1_VALUE = 1;
	//Level 2 value
	static final int L2_VALUE = 3;
}

enum Score {
	SEVERE(20, new String[]{"[", "]", " - ", "_"}),
	LOW(10, new String[]{"featuring", "feat.", "feat ", "ft.", "ft "});

	private final int points;
	private final String[] tokens;

	private Score(final int points, final String[] tokens){
		this.points = points;
		this.tokens = tokens;
	}

	public int getPoints() {
		return -points;
	}

	public String[] getTokens() {
		return tokens;
	}
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
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration context) {
			track.withNumber(Integer.parseInt(values.get(0)));
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
			boolean isNumber = true;
			for(int i = 0; i < value.length() && isNumber; i++){
				if(!Character.isDigit(value.charAt(i))){
					isNumber = false;
				}
			}

			return super.getScore(value, context) + (isNumber ? 0 : Score.SEVERE.getPoints());
		}
	},
	/**
	 * Track title
	 */
	TITLE(InitialScores.L2_VALUE) {
		@Override
		public void setField(final SingleTrackAlbumBuilder track, final List<String> values, 
				final ParserConfiguration context) {
			track.withTitle(WordUtils.capitalizeFully(values.get(0)));
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
			boolean isValid = !Arrays.stream(SUSPICIOUS_TITLE_CHARSEQS)
					.anyMatch(token -> value.contains(token));
			final int score = super.getScore(value, context);
			
			return isValid ? score : score + Score.LOW.getPoints();
		}
	},

	/**
	 * Main artist(s)
	 * <p>Note: will be automatically separated, so no need for manual
	 * separation.
	 */
	ARTIST(InitialScores.L2_VALUE) {
		@Override
		public void setField(final SingleTrackAlbumBuilder track, final List<String> values, 
				final ParserConfiguration context) {
			final ArtistFactory artistFactory = context.getDBConnection()
					.getFactories()
					.getArtistFactory();
			track.withMainArtists(values.stream()
					.map(artistFactory::getArtistsFromFormat)
					.flatMap(Collection::stream)
					.collect(Collectors.toSet()));
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
			final DBManager db = context.getDBConnection();
			int dbScore = 0;
			final boolean isValid = !Arrays.stream(MultiFormatParser.SUSPICIOUS_NAME_CHARSEQS)
					.anyMatch(token -> value.contains(token));

			if(isValid && context.isStoreDB()) {
				dbScore = db.getArtists()
						.withName(value)
						.first() != null ? 10 : -1;
			}

			return super.getScore(value, context)+dbScore + (isValid ? 0 : Score.SEVERE.getPoints());
		}
	},
	/**
	 * Featuring artist(s)
	 * <p>Note: like {@link Field#ARTIST}, artists will be automatically
	 * separated.
	 */
	FEAT(InitialScores.L2_VALUE) {
		@Override
		public void setField(final SingleTrackAlbumBuilder track, final List<String> values, 
				final ParserConfiguration context) {
			final ArtistFactory artistFactory = context.getDBConnection()
					.getFactories()
					.getArtistFactory();
			track.withFeatures(values.stream()
					.map(artistFactory::getArtistsFromFormat)
					.flatMap(Collection::stream)
					.collect(Collectors.toSet()));
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
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
		public int getScore(String value, ParserConfiguration context) {
			return Field.ARTIST.getScore(value, context);
		}

		@Override
		public void setField(final SingleTrackAlbumBuilder track, final List<String> values, 
				final ParserConfiguration context) {
			final ArtistFactory artistFactory = context.getDBConnection()
					.getFactories()
					.getArtistFactory();
			track.withProducers(values.stream()
					.map(artistFactory::getArtistsFromFormat)
					.flatMap(Collection::stream)
					.collect(Collectors.toSet()));
		}

	},
	/**
	 * Album title
	 */
	ALBUM(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration context) {
			final String title = WordUtils.capitalizeFully(values.get(0));
			track.withAlbumTitle(title);
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
			return super.getScore(value, context);
		}
	},	
	/**
	 * Track title with the correspondent
	 * album being a single with the same
	 * title
	 */
	TITLES(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration context)  {
			TITLE.setField(track, values, context);
			ALBUM.setField(track, values, context);
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
			return TITLE.getScore(value, context)+ALBUM.getScore(value, context);
		}
	},

	/**
	 * Track Genre
	 */
	GENRE(InitialScores.L1_VALUE) {
		@Override
		public void setField(final SingleTrackAlbumBuilder track, final List<String> values, 
				final ParserConfiguration context) {
			final GenreFactory genreFactory = context.getDBConnection()
					.getFactories()
					.getGenreFactory();
			track.withGenres(values.stream()
					.map(genreFactory::createGenre)
					.collect(Collectors.toSet()));
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
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
			return super.getScore(value, context)+dbScore;
		}
	},
	VERSION(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration context) {
			track.withTypeVersion(TypeVersion.parse(values.get(0)));
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
			return Arrays.stream(TypeVersion.values())
					.flatMap(version -> Arrays.stream(version.getTokens()))
					.anyMatch(token -> value.equalsIgnoreCase(token)) ? super.getScore(value, context) : Score.SEVERE.getPoints();
		}
	},
	VTOKEN(InitialScores.NO_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration context) {
			track.withVersionToken(values.get(0));
		}
		
		@Override
		public int getScore(String value, ParserConfiguration config) {
			final DBManager db = config.getDBConnection();
			final int dbScore;
			if(config.isStoreDB()) {
				dbScore = db.getTracks()
						.withVersionToken(value)
						.first() != null ? 10 : Score.LOW.getPoints();
			}
			else {
				dbScore = 0;
			}
			return super.getScore(value, config) + dbScore;
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
		public void setField(final SingleTrackAlbumBuilder track, final List<String> values, 
				final ParserConfiguration context) {
			final ArtistFactory artistFactory = context.getDBConnection()
					.getFactories()
					.getArtistFactory();
			track.withVersionArtists(values.stream()
					.map(artistFactory::getArtistsFromFormat)
					.flatMap(Collection::stream)
					.collect(Collectors.toSet()));
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
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
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration context) {
			ARTIST.setField(track, values, context);
			EXTRA.setField(track, values, context);
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
			return ARTIST.getScore(value, context);
		}
	},
	/**
	 * Hidden track
	 * <p> Use this field to refer to a hidden track in another track
	 * the field is associated with the title of the hidden track, as all the
	 * other tags will be associated with the main track.
	 * Note: use {@link TrackFactory#extractHiddenTrack(Track)} to create
	 * an hidden track out of the title string.
	 */
	HIDDEN(InitialScores.L2_VALUE) {
		@Override
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration context) {
			track.withHiddenTrack(values.get(0));
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
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
		public void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration context) {
			values.forEach(i -> track.withIgnore(i));
		}

		@Override
		public int getScore(String value, ParserConfiguration context) {
			final DBManager db = context.getDBConnection();
			final int occurrences = db != null ? db.getIgnoredOccurrences(value) : 0;
			return super.getScore(value, context) + occurrences;
		}
	};

	private static final Field[] REQUIRED_FIELDS = new Field[]{IGNORE, ARTIST, FEAT, GENRE};
	private static final Field[][] ALTERNATIVES = new Field[][]{{TITLES, TITLE}, {ARTIST, ARTIST_EXTRA}};

	public static final String[] SUSPICIOUS_TITLE_CHARSEQS = new String[]{"- ", " -",  "[", "]", "_", "{", "}", "~", "|", "�", " vs ", " vs. "};

	private final int score;

	private Field(int score){
		this.score = score;
	}

	@Override
	public String toString(){
		return "<"+name()+">";
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
	public int getScore(String value, ParserConfiguration context) {
		if(value.isEmpty()) {
			return Score.SEVERE.getPoints();
		}
		int score = this.score;
		return score + Arrays.stream(Score.values())
				.flatMap(this::enpair)
				.mapToInt(pair -> pair.getValue()*countMatchesIgnoreCase(value, pair.getKey()))
				.sum();
	}
	
	private int countMatchesIgnoreCase(String str, String sub) {
		return StringUtils.countMatches(str.toLowerCase(), sub.toLowerCase());
	}
	
	private Stream<Pair<String, Integer>> enpair(Score score) {
		return Arrays.stream(score.getTokens()).map(token -> Pair.of(token, score.getPoints()));
	}

	/**
	 * Applies the field's values to the track passed as parameter.
	 * 
	 * @param track track object which will be affected with the values
	 * passed.
	 * @param values field values. These values will affect the track passed
	 * as parameter.
	 * @throws NumberFormatException thrown by {@link Field#NUMBER}. The value is not numeric.
	 */
	public abstract void setField(SingleTrackAlbumBuilder track, List<String> values, ParserConfiguration context);

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
