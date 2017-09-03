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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.rookit.parser.exceptions.InvalidSongFormatException;
import org.rookit.parser.exceptions.MissingRequiredFieldException;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.FormatSong;
import org.rookit.parser.utils.PathUtils;
import org.rookit.parser.utils.TrackPath;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

@SuppressWarnings("javadoc")
public class FormatParser extends AbstractParser<TrackPath, SingleTrackAlbumBuilder> implements Multiparser<TrackPath, SingleTrackAlbumBuilder> {

	private static final String[][] ENHANCEMENTS = {/*{"_", " "},*/
			{"  ", " "},
			{"�", "-"}};

	public static final String[] FEAT_TOKENS = {"featuring", "feat", "ft"};

	private static enum Score {
		SEVERE(10, new String[]{"[", "]", " - ", "_"}),
		LOW(2, FEAT_TOKENS);

		private final int points;
		private final String[] tokens;

		private Score(final int points, final String[] tokens){
			this.points = points;
			this.tokens = tokens;
		}

		public int getPoints() {
			return points;
		}

		public String[] getTokens() {
			return tokens;
		}
	}

	static FormatParser create(ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> configuration) {
		return new FormatParser(configuration);
	}

	private FormatParser(ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> config){
		super(config);
	}

	private void validateRequiredFields(TrackFormat format) {
		final Field[] requiredFields = getConfig().getRequiredFields();
		final List<Field> missingFields = requiredFields != null ? format.getMissingRequiredFields(requiredFields) : Lists.newArrayList();

		if(!missingFields.isEmpty()){
			VALIDATOR.handleTestException(new MissingRequiredFieldException(missingFields));
		}
	}

	private String enhanceFileName(String originalName){
		String name = originalName;
		for(String[] enhancement : ENHANCEMENTS){
			name = name.replace(enhancement[0], enhancement[1]);
		}
		return name.trim();
	}

	@Override
	protected SingleTrackAlbumBuilder parse(TrackPath token, SingleTrackAlbumBuilder baseResult) {
		return multiparse(token, baseResult).get(0);
	}

	@Override
	public List<SingleTrackAlbumBuilder> multiparse(TrackPath path){
		final SingleTrackAlbumBuilder builder = getResults(path);
		final List<SingleTrackAlbumBuilder> results =  multiparse(path, builder);
		final int limit = getConfig().getLimit();
		if(limit > 0 && limit < results.size()) {
			return results.subList(0, limit);
		}
		return results;
	}

	private List<SingleTrackAlbumBuilder> multiparse(TrackPath path, SingleTrackAlbumBuilder baseResult) {
		final String fileName = enhanceFileName(PathUtils.getFileName(path));
		if(baseResult == null) {
			return Lists.newArrayList();
		}

		return multiparse(fileName, baseResult);
	}

	private List<SingleTrackAlbumBuilder> multiparse(String pathName, SingleTrackAlbumBuilder baseResult) {
		final List<SingleTrackAlbumBuilder> results = Lists.newArrayList();
		final List<TrackFormat> formats = getConfig().getFormats();
		formats.forEach(f -> validateRequiredFields(f));

		for(TrackFormat format : getConfig().getFormats()) {
			final SingleTrackAlbumBuilder clone = SingleTrackAlbumBuilder.create(baseResult);
			final SingleTrackAlbumBuilder result = parse(pathName, format, clone); 
			if(result != null) {
				results.add(result);
			}
			log(pathName, format, result != null);
		}
		Collections.sort(results);
		return results;
	}

	private void log(String pathName, TrackFormat format, boolean success) {
		StringBuilder builder = new StringBuilder("[")
				.append(success ? "ok" : "not ok")
				.append("] Parsing '")
				.append(pathName)
				.append("' with: ")
				.append(format);
		VALIDATOR.info(builder.toString());
	}

	public SingleTrackAlbumBuilder parse(String pathName, SingleTrackAlbumBuilder baseResult) {
		return multiparse(pathName, baseResult).get(0);
	}

	private SingleTrackAlbumBuilder getResults(TrackPath path) {
		final Parser<TrackPath, SingleTrackAlbumBuilder> baseParser = getConfig().getBaseParser();
		if(baseParser == null) {
			return SingleTrackAlbumBuilder.create();
		}
		return baseParser.parse(path);
	}

	private SingleTrackAlbumBuilder parse(String pathName, TrackFormat format, SingleTrackAlbumBuilder track) {
		final Tokenizer tokenizer;
		if(!format.fits(pathName)) {
			return null;
		}
		try {
			track.attachFormat(format);
			tokenizer = new Tokenizer();
			tokenize(pathName, tokenizer, format, format.getDenormalizedSeparators(), format.getFields());
			for(Field f : tokenizer.keySet()){
				final List<String> tokens = tokenizer.get(f).stream()
						.map(p -> p.getRight())
						.collect(Collectors.toList());
				f.setField(track, tokens, getConfig());
			}
			track.setScore(tokenizer.getScore());
			return track;
		} catch (NumberFormatException e) {
			VALIDATOR.handleParseException(e);
			return null;
		}
	}

	/**
	 * <h1><center>Algorithm for format parsing<center></h1>
	 * <p>This method computes the file name in order to create a multiple value map of
	 * {@link Field} and {@link String}.
	 *  
	 * @param unparsedString name of the file to be parsed
	 * @throws EmptyFieldException
	 */
	private void tokenize(String input, Tokenizer tokenizer, TrackFormat format,
			List<Pair<String, Integer>> separators, List<Field> fields) {
		final Pair<String, Integer> curSep = separators.get(0);//left
		final Pair<String, Integer> nextSep = separators.get(1);
		final int beginIndex = format.length(curSep);
		int endIndex = 0;
		int occurrences;
		final int hops;
		String subTokens;

		if(separators.size() == 2) {
			endIndex = format.indexOf(input, nextSep);
			subTokens = input.substring(beginIndex, endIndex);
			handleSep(subTokens, curSep, tokenizer, fields, 0);
		}
		else {
			final Pair<String, Integer> nextNextSep = separators.get(2);
			final int nextIndex = format.indexOf(input, nextSep, beginIndex)+format.length(nextSep);
			endIndex = format.indexOf(input, nextNextSep, nextIndex);
			subTokens = input.substring(beginIndex, endIndex);
			occurrences = StringUtils.countMatches(subTokens.toLowerCase(), nextSep.getKey().toLowerCase());
			if(occurrences == 1 && nextSep.getRight() == 1) {
				endIndex = format.indexOf(input, nextSep, beginIndex);
				subTokens = input.substring(beginIndex, endIndex);
				hops=1;
				occurrences = 0;
			}
			else {
				hops = nextSep.getRight()+1;
			}
			handleSep(subTokens, nextSep, tokenizer, fields.subList(0, hops), occurrences);
			final List<Field> nextFields = subList(fields, hops);
			if(!nextFields.isEmpty()) {
				tokenize(input.substring(endIndex), tokenizer, format, 
						subList(separators, hops), nextFields);	
			}
		}
	}
	
	private <T> List<T> subList(List<T> list, int beginIndex) {
		return list.subList(beginIndex, list.size());
	}

	private void handleSep(String subName, Pair<String, Integer> sep, Tokenizer tokens, List<Field> fields, int occurrences) {
		if(occurrences == sep.getRight()) {
			String[] splitTokens = subName.split(sep.getKey()); 
			for(int i = 0; i < fields.size(); i++) {
				tokens.put(fields.get(i), splitTokens[i]);
			}
		}
		else if (occurrences == 0) {
			tokens.put(fields.get(0), subName);
		}
		else {
			//occurrences are bigger
			tokens.merge(solveAmbiguity(Queues.newArrayDeque(fields), subName, sep.getLeft()));
		}
	}

	private Tokenizer solveAmbiguity(Queue<Field> fields, String str, String sep) {
		Tokenizer tokens = new Tokenizer();
		int index = 0;
		int nextIndex;
		if(!fields.isEmpty() && str.isEmpty()) {
			tokens.valid = false;
		}
		else if(fields.size() == 1) {
			final Tokenizer value = new Tokenizer();
			value.put(fields.poll(), str);
			return value;
		}
		else {
			final Field field = fields.poll();
			do {
				final String subStr;
				final Tokenizer value = new Tokenizer();
				nextIndex = StringUtils.indexOfIgnoreCase(str, sep, index);
				nextIndex = nextIndex < 0 ? nextIndex = str.length() : nextIndex;
				subStr = str.substring(0, nextIndex);
				value.put(field, subStr);
				nextIndex += sep.length();
				if(fields.size() >= 1) {
					final String nextStr = nextIndex >= str.length() ? "" : str.substring(nextIndex);
					value.merge(solveAmbiguity(Queues.newArrayDeque(fields), nextStr, sep));
				}
				tokens.max(value);
				index = nextIndex;
			} while(index < str.length() && index > 0);
		}
		return tokens;
	}

	public boolean checkSong(Path f) throws InvalidSongFormatException {
		final String format = PathUtils.getFormat(f);

		if(!FormatSong.isAllowed(format)){
			throw new InvalidSongFormatException(f.getFileName().toString());
		}

		return true;
	}

	public class Tokenizer {

		private Map<Field, List<Pair<Integer, String>>> map;
		private boolean valid;

		private Tokenizer() {
			map = Maps.newLinkedHashMap();
			Arrays.stream(Field.values()).forEach(f -> map.put(f, Lists.newArrayList()));
			valid = true;
		}

		private void merge(Tokenizer tokenizer) {
			if(!tokenizer.valid) {
				valid = false;
			}
			else {
				for(Field field : tokenizer.map.keySet()) {
					map.get(field).addAll(tokenizer.map.get(field));
				}
			}
		}

		private void put(Field field, String token) {
			final int score = field.getScore(token, getConfig());
			final Pair<Integer, String> entry = Pair.of(score, token);
			map.get(field).add(entry);
		}

		private void max(Tokenizer tokenizer) {
			if(tokenizer.valid && tokenizer.getScore() > getScore()) {
				map = tokenizer.map;
			}
		}

		public Set<Field> keySet() {
			return map.keySet().stream()
					.filter(field -> !map.get(field).isEmpty())
					.collect(Collectors.toSet());
		}

		public List<Pair<Integer, String>> get(Field field) {
			return map.get(field);
		}

		public Integer getScore(){
			return map.values().stream()
					.flatMap(l -> l.stream())
					.mapToInt(p -> p.getLeft())
					.sum();
		}

		@Override
		public String toString() {
			return "[" + valid + "] " + map.toString();
		}
	}
}
