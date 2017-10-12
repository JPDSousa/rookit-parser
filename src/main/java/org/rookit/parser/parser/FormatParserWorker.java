package org.rookit.parser.parser;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.rookit.mongodb.DBManager;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.ParserValidator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

class FormatParserWorker implements Runnable {
	
	private final ParserValidator validator;
	private final Queue<SingleTrackAlbumBuilder> results;
	private final SingleTrackAlbumBuilder baseResult;
	private final TrackFormat format;
	private final String input;
	private final ParserConfiguration<String, SingleTrackAlbumBuilder> config;
	
	FormatParserWorker(TrackFormat format, String input, SingleTrackAlbumBuilder baseResult, 
			FormatParser parent, Queue<SingleTrackAlbumBuilder> results) {
		super();
		this.format = format;
		this.input = input;
		this.baseResult = SingleTrackAlbumBuilder.create(baseResult);
		this.validator = FormatParser.VALIDATOR;
		this.results = results;
		this.config = parent.getConfig();
	}

	private void log(String pathName, TrackFormat format, boolean success) {
		StringBuilder builder = new StringBuilder("[")
				.append(success ? "ok" : "not ok")
				.append("] Parsing '")
				.append(pathName)
				.append("' with: ")
				.append(format);
		validator.info(builder.toString());
	}

	@Override
	public void run() {
		final SingleTrackAlbumBuilder result = parse(input, format, baseResult);
		if(result != null) {
			results.add(result);
		}
		log(input, format, result != null);
	}
	
	private int getScore(Tokenizer tokenizer, TrackFormat format) {
		final DBManager db = config.getDBConnection();
		int tfScore = db != null ? db.getTrackFormatOccurrences(format.toString()) : 0;
		return tokenizer.getScore() + tfScore;
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
				f.setField(track, tokens, config);
			}
			track.setScore(getScore(tokenizer, format));
			return track;
		} catch (NumberFormatException e) {
			validator.handleParseException(e);
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
	
	private class Tokenizer {

		private Map<Field, List<Pair<Integer, String>>> map;
		private boolean valid;

		private Tokenizer() {
			map = Maps.newLinkedHashMap();
			valid = true;
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
			final int score = field.getScore(token, config);
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
					.sum();
		}

		@Override
		public String toString() {
			return "[" + valid + "] " + map.toString();
		}
	}

}
