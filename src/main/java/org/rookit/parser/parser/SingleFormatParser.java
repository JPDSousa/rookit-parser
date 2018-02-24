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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.rookit.api.storage.DBManager;
import org.rookit.parser.result.SingleTrackAlbumBuilder;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

class SingleFormatParser extends AbstractParser<String, SingleTrackAlbumBuilder> implements Callable<Optional<SingleTrackAlbumBuilder>> {

	private final TrackFormat format;
	private final String input;
	private final SingleTrackAlbumBuilder baseResult;

	SingleFormatParser(TrackFormat format, String input, SingleTrackAlbumBuilder baseResult, 
			Parser<String, SingleTrackAlbumBuilder> parent) {
		super(parent.getConfig());
		this.format = format;
		this.input = input;
		this.baseResult = baseResult;
	}

	@Override
	protected SingleTrackAlbumBuilder getDefaultBaseResult() {
		return SingleTrackAlbumBuilder.create(baseResult);
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

	@Override
	public Optional<SingleTrackAlbumBuilder> call() throws Exception {
		final Optional<SingleTrackAlbumBuilder> result = parse(input);
		log(input, format, result.isPresent());
		return result;
	}

	@Override
	protected Optional<SingleTrackAlbumBuilder> parseFromBaseResult(String token, SingleTrackAlbumBuilder baseResult) {
		final Tokenizer tokenizer;
		if(!format.fits(token)) {
			return Optional.absent();
		}
		try {
			baseResult.attachFormat(format);
			tokenizer = new Tokenizer();
			tokenize(token, tokenizer, format, format.getDenormalizedSeparators(), format.getFields());
			if(tokenizer.getFields().size() != format.getFields().size()) {
				return Optional.absent();
			}
			for(Field f : tokenizer.keySet()){
				final List<String> tokens = tokenizer.get(f).stream()
						.map(p -> p.getRight())
						.collect(Collectors.toList());
				f.setField(baseResult, tokens, getConfig());
			}
			baseResult.setScore(getScore(tokenizer, format));
			return Optional.of(baseResult);
		} catch (NumberFormatException e) {
			VALIDATOR.handleParseException(e);
			return Optional.absent();
		}
	}

	private int getScore(final Tokenizer tokenizer, final TrackFormat format) {
		final DBManager db = getConfig().getDBConnection();
		final int tokenizerScore = tokenizer.getScore();
		
		if(tokenizerScore > 0 && getConfig().isStoreDB()) {
			final int trackFormatScore = db.getTrackFormatOccurrences(format.toString());
			final float finalScore = tokenizerScore*getConfig().getTokenizerPercentage() 
					+ trackFormatScore*getConfig().getTrackFormatPercentage(); 
			return Math.round(finalScore);
		}
		return tokenizerScore;
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

	private class Tokenizer {

		private Map<Field, List<Pair<Integer, String>>> map;
		private boolean valid;

		private Tokenizer() {
			map = Maps.newLinkedHashMap();
			valid = true;
		}

		public Set<Field> getFields() {
			return map.keySet();
		}

		private void merge(Tokenizer tokenizer) {
			if(tokenizer.valid) {
				for(Field field : tokenizer.map.keySet()) {
					get(field).addAll(tokenizer.map.get(field));
				}
			}
		}

		private List<Pair<Integer, String>> get(Field field) {
			List<Pair<Integer, String>> res = map.get(field);
			if(res == null) {
				res = Lists.newArrayList();
				map.put(field, res);
			}
			return res;
		}

		private void put(Field field, String token) {
			final int score = field.getScore(token, getConfig());
			final Pair<Integer, String> entry = Pair.of(score, token);
			get(field).add(entry);
		}

		private void max(Tokenizer tokenizer) {
			if(tokenizer.valid && !tokenizer.map.isEmpty() && (map.isEmpty() || tokenizer.getScore() > getScore())) {
				map = tokenizer.map;
			}
			else {
				map.isEmpty();
			}
		}

		public Set<Field> keySet() {
			return map.keySet().stream()
					.filter(field -> !get(field).isEmpty())
					.collect(Collectors.toSet());
		}

		public Integer getScore(){
			return map.values().stream()
					.flatMap(l -> l.stream())
					.mapToInt(p -> p.getLeft())
					.reduce(0, (left, right) -> (left < 0 || right < 0) ? Math.min(left, right) : left + right);
		}

		@Override
		public String toString() {
			return "[" + valid + "] " + map.toString();
		}
	}

}
