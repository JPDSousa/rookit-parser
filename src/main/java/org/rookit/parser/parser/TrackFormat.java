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


import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.rookit.dm.track.TypeTrack;
import org.rookit.parser.exceptions.AmbiguousFormatException;
import org.rookit.parser.exceptions.InvalidFieldException;
import org.rookit.parser.exceptions.NoFieldsException;
import org.rookit.parser.utils.ParserValidator;

import com.google.common.collect.Lists;

@SuppressWarnings("javadoc")
public class TrackFormat implements Comparable<TrackFormat>{

	private static final ParserValidator VALIDATOR = ParserValidator.getDefault();
	
	public static final String SEP_END = "|E|";
	public static final String SEP_START = "|S|";

	public static final TrackFormat create(String format) {
		return new TrackFormat(format, null);
	}

	public static final TrackFormat create(String format, TypeTrack type) {
		return new TrackFormat(format, type);
	}

	private final List<Field> fieldList;
	private final Separators sepList;
	private final TypeTrack type;
	private final boolean startsWithField;
	private boolean endsWithField;

	private TrackFormat(String format, TypeTrack type) {
		validateFormat(format);
		fieldList = getFormats(format);
		validateFieldsList(format);
		sepList = getSeparators(format);
		startsWithField = sepList.isEmpty() || format.indexOf(sepList.get(0).getKey())>0;
		endsWithField = fieldList.size() > sepList.size() || (!startsWithField && fieldList.size() == sepList.size()); 
		this.type = type == null ? getType() : type;
	}

	private TypeTrack getType() {
		return fieldList.contains(Field.VERSION) ? TypeTrack.VERSION : TypeTrack.ORIGINAL;
	}

	private void validateFieldsList(String format) {
		if(fieldList.isEmpty()){
			VALIDATOR.handleTrackFormatException(new NoFieldsException(format));
		}
	}

	private void validateFormat(String format) {
		if(format.indexOf("><") > 0){
			VALIDATOR.handleTrackFormatException(new AmbiguousFormatException(format));
		}
	}

	@Override
	public String toString(){
		final StringBuilder format = new StringBuilder(32);
		final List<String> sepList = this.sepList.getAllRaw();

		for(int i = 0; i<Math.min(fieldList.size(), sepList.size()); i++){
			if(startsWithField){
				format.append(fieldList.get(i).toString()).append(sepList.get(i));
			}
			else{
				format.append(sepList.get(i)).append(fieldList.get(i).toString());
			}
		}
		if(startsWithField && sepList.size()<fieldList.size()){
			format.append(fieldList.get(fieldList.size()-1));
		}
		else if(!startsWithField && fieldList.size()<sepList.size()){
			format.append(sepList.get(sepList.size()-1));
		}

		return format.toString();
	}

	private List<Field> getFormats(String rawFormat) {
		final List<Field> fields = new ArrayList<>();
		String within;
		String format = rawFormat;

		while((within = getWithin(format, "<", ">")) != null){
			try{
				fields.add(Field.valueOf(within));
				format = format.replace("<" + within + ">", "");
			} catch(IllegalArgumentException e){
				VALIDATOR.handleTrackFormatException(new InvalidFieldException(within));
			}
		}

		return fields;
	}
	
	public static final String getWithin(String str, String init, String end){
		String sub = null;
		final int initIndex;
		if((initIndex = str.indexOf(init)) >= 0){
			if(str.contains(end + "")){
				sub = str.substring(initIndex+init.length(), str.indexOf(end, initIndex));
			}
			else{
				sub = str.substring(initIndex);
			}
		}
		return sub;
	}

	public List<Field> getFields(){
		return fieldList;
	}
	
	public int indexOf(String string, String sep, int startPos) {
		if(sep.equals(SEP_START)) {
			return 0;
		}
		else if(sep.equals(SEP_END)) {
			return string.length();
		}
		else {
			return StringUtils.indexOfIgnoreCase(string, sep, startPos);
		}
		
	}
	
	public int indexOf(String string, String sep) {
		return indexOf(string, sep, 0);
	}
	
	public int indexOf(String string, Pair<String, Integer> sep, int startPos) {
		return indexOf(string, sep.getKey(), startPos);
	}
	
	public int indexOf(String string, Pair<String, Integer> sep) {
		return indexOf(string, sep, 0);
	}
	
	public int length(Pair<String, Integer> sepPair) {
		final String sep = sepPair.getKey();
		if(sep.equals(SEP_START) || sep.equals(SEP_END)) {
			return 0;
		}
		return sep.length();
	}

	private Separators getSeparators(String format){
		final String[] seps = Pattern.compile("<\\w+>").split(format);
		final Separators listSeps = new Separators();

		for(String sep : seps){
			if(sep.length()>0){
				listSeps.add(sep);
			}
		}
		return listSeps;
	}

	public List<String> getSeparators(){
		return sepList.normalize();
	}

	public List<Pair<String, Integer>> getDenormalizedSeparators() {
		return sepList.getAll();
	}

	public TypeTrack getTrackClass(){
		return type;
	}

	private boolean lastAppendedIsField(){
		return (startsWithField && fieldList.size() > sepList.size()) ||
				(!startsWithField && fieldList.size() >= sepList.size());
	}

	public TrackFormat appendSep(String sep){
		if(!lastAppendedIsField() && !sepList.isEmpty()){
			sepList.append2Last(sep);
		}
		else{
			sepList.add(sep);
		}
		endsWithField = false;
		return this;
	}

	public TrackFormat appendField(Field field) {
		if(lastAppendedIsField()){
			VALIDATOR.handleTrackFormatException(new AmbiguousFormatException(this.toString()));
		}
		fieldList.add(field);
		endsWithField = true;
		return this;
	}

	public boolean fits(String fileName) {
		if(sepList.isEmpty()) {
			return true;
		}
		int curIndex;
		int lastIndex = 0;
		boolean fits = true;

		for(int i=0; fits && i<sepList.size()-1 && fits; i++){
			final Pair<String, Integer> separator = sepList.get(i);
			final Pair<String, Integer> nextSeparator = sepList.get(i+1);
			curIndex = indexOf(fileName, separator, lastIndex);
			lastIndex = indexOf(fileName, nextSeparator, curIndex+separator.getKey().length());
			fits = curIndex >= 0 && lastIndex >= 0 && StringUtils.countMatches(fileName.substring(curIndex, lastIndex).toLowerCase(), separator.getLeft().toLowerCase()) >= separator.getValue();
			lastIndex = curIndex;
		}

		return fits;
	}

	public List<Field> getMissingRequiredFields(Field[] fields) {
		List<Field> missing = new ArrayList<>();
		Field alt;

		for(Field field : fields){
			alt = Field.getAlternative(field);
			if(!getFields().contains(field) && (alt == null || !getFields().contains(alt))){
				missing.add(field);
				break;
			}
		}

		return missing;
	}

	@Override
	public int compareTo(TrackFormat o) {
		return Collator.getInstance().compare(toString(), o.toString());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldList == null) ? 0 : fieldList.hashCode());
		result = prime * result + ((sepList == null) ? 0 : sepList.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TrackFormat other = (TrackFormat) obj;
		return other.toString().equals(toString());
	}

	private class Separators {
		
		private final List<Pair<String, Integer>> separators;
		
		private Separators() {
			separators = Lists.newArrayList();
		}
		
		private void add(String separator) {
			final String forSep = formatSep(separator);
			if(!separators.isEmpty()) {
				final Pair<String, Integer> last = getLast();
				if(last.equals(forSep)) {
					last.setValue(last.getValue()+1);
					return;
				}
			}
			separators.add(new MutablePair<>(forSep, 1));
		}

		private Pair<String, Integer> getLast() {
			return separators.get(separators.size()-1);
		}
	
		private String formatSep(String sep) {
			return sep.replaceAll("  ", " ");
		}
		
		private boolean isEmpty() {
			return separators.isEmpty();
		}
		
		private List<Pair<String, Integer>> getAll() {
			final List<Pair<String, Integer>> separators = Lists.newArrayList();
			if(startsWithField) {
				separators.add(Pair.of(SEP_START, 1));
			}
			separators.addAll(this.separators);
			if(endsWithField) {
				separators.add(Pair.of(SEP_END, 1));
			}
			return separators;
		}
		
		private int size() {
			return separators.size();
		}
		
		private Pair<String, Integer> get(int index) {
			return separators.get(index);
		}
		
		private void append2Last(String separator) {
			final Pair<String, Integer> last = getLast();
			last.setValue(last.getValue()-1);
			add(last.getKey()+separator);
		}
		
		private List<String> normalize() {
			final List<String> normalized = Lists.newArrayList();
			for(Pair<String, Integer> separator : getAll()) {
				for(int i = 0; i < separator.getRight(); i++) {
					normalized.add(separator.getLeft());
				}
			}
			return normalized;
		}
		
		private List<String> getAllRaw() {
			final List<String> all = Lists.newArrayList();
			for(Pair<String, Integer> separator : this.separators) {
				for(int i = 0; i < separator.getRight(); i++) {
					all.add(separator.getLeft());
				}
			}
			return all;
		}

		@Override
		public String toString() {
			return separators.toString();
		}
		
		
	}
}
