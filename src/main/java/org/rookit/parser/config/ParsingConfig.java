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
package org.rookit.parser.config;

import static org.rookit.utils.config.ConfigUtils.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.rookit.utils.resource.Resources;

@SuppressWarnings("javadoc")
public class ParsingConfig {
	
	public static final int DEFAULT_LIMIT = 5;
	public static final Path DEFAULT_FORMATS = Resources.RESOURCES_MAIN
			.resolve("parser")
			.resolve("formats.txt");
	
	private int resultsLimit;
	private String formatsPath;
	private boolean filterNegatives;
	private OnSuccess onSuccess;
	private FormatParserConfig formatParser;
	
	public ParsingConfig() {
		filterNegatives = true;
	}
	
	public int getParserLimit() {
		return resultsLimit > 0 ? resultsLimit : DEFAULT_LIMIT;
	}

	public void setParserLimit(int parserLimit) {
		this.resultsLimit = parserLimit;
	}

	public Path getFormatsPath() {
		return getOrDefault(formatsPath, DEFAULT_FORMATS, formatsPath -> Paths.get(formatsPath));
	}

	public void setFormatsPath(Path formatsPath) {
		this.formatsPath = formatsPath.toString();
	}
	
	public boolean isFilterNegatives() {
		return filterNegatives;
	}

	public void setFilterNegatives(boolean filterNegatives) {
		this.filterNegatives = filterNegatives;
	}

	public OnSuccess getOnSuccess() {
		return getOrDefault(onSuccess, new OnSuccess());
	}

	public void setOnSuccess(OnSuccess onSuccess) {
		this.onSuccess = onSuccess;
	}
	
	public FormatParserConfig getFormatParser() {
		return getOrDefault(formatParser, new FormatParserConfig());
	}

	public void setFormatParser(FormatParserConfig formatParser) {
		this.formatParser = formatParser;
	}

	public static class OnSuccess {
		
		public enum Remove {
			ALWAYS, ASK, NEVER
		}
		
		private Remove remove;

		public Remove getRemove() {
			return getOrDefault(remove, Remove.ASK);
		}

		public void setRemove(Remove remove) {
			this.remove = remove;
		}
		
		
	}
	
}
