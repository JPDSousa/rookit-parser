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

import org.rookit.database.DBManager;
import org.rookit.parser.result.Result;
import org.rookit.parser.utils.ParserValidator;

@SuppressWarnings("javadoc")
public class ParserConfiguration<T, R extends Result<?>> {

	private static final ParserValidator VALIDATOR = ParserValidator.getDefault();
	
	static <T, R extends Result<?>> ParserConfiguration<T, R> create(Class<R> resultClass) {
		VALIDATOR.checkArgumentNotNull(resultClass, "Must provide a result class");
		return new ParserConfiguration<>(resultClass);
	}
	
	private final Class<R> resultClass;
	private Parser<T, R> baseParser;
	private boolean setDate;
	private Field[] requiredFields;
	private List<TrackFormat> formats;
	private int limit;
	
	private DBManager database;
	private boolean storeDB;
	
	private ParserConfiguration(Class<R> resultClass) {
		this.resultClass = resultClass;
		this.setDate = true;
		this.storeDB = true;
	}
	
	public ParserConfiguration<T, R> withBaseParser(Parser<T, R> parser) {
		this.baseParser = parser;
		return this;
	}
	
	public ParserConfiguration<T, R> withDBConnection(DBManager connection) {
		this.database = connection;
		return this;
	}
	
	public ParserConfiguration<T, R> withSetDate(boolean setDate) {
		this.setDate = setDate;
		return this;
	}
	
	public ParserConfiguration<T, R> withRequiredFields(Field[] fields) {
		requiredFields = fields;
		return this;
	}
	
	public ParserConfiguration<T, R> withDbStorage(boolean storeDB) {
		this.storeDB = storeDB;
		return this;
	}
	
	public ParserConfiguration<T, R> withTrackFormats(List<TrackFormat> formats) {
		this.formats = formats;
		return this;
	}
	
	public ParserConfiguration<T, R> withLimit(int limit) {
		this.limit = limit;
		return this;
	}

	public Parser<T, R> getBaseParser() {
		return baseParser;
	}

	public Class<R> getResultClass() {
		return resultClass;
	}
	
	public DBManager getDBConnection() {
		return database;
	}
	
	public boolean isStoreDB() {
		return database != null && storeDB;
	}

	public boolean isSetDate() {
		return setDate;
	}
	
	public Field[] getRequiredFields() {
		return requiredFields;
	}

	public List<TrackFormat> getFormats() {
		return formats;
	}
	
	public int getLimit() {
		return limit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseParser == null) ? 0 : baseParser.hashCode());
		result = prime * result + ((database == null) ? 0 : database.hashCode());
		result = prime * result + ((formats == null) ? 0 : formats.hashCode());
		result = prime * result + Arrays.hashCode(requiredFields);
		result = prime * result + (setDate ? 1231 : 1237);
		result = prime * result + (storeDB ? 1231 : 1237);
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
		ParserConfiguration<?, ?> other = (ParserConfiguration<?, ?>) obj;
		if (baseParser == null) {
			if (other.baseParser != null) {
				return false;
			}
		} else if (!baseParser.equals(other.baseParser)) {
			return false;
		}
		if (database == null) {
			if (other.database != null) {
				return false;
			}
		} else if (!database.equals(other.database)) {
			return false;
		}
		if (formats == null) {
			if (other.formats != null) {
				return false;
			}
		} else if (!formats.equals(other.formats)) {
			return false;
		}
		if (!Arrays.equals(requiredFields, other.requiredFields)) {
			return false;
		}
		if (setDate != other.setDate) {
			return false;
		}
		if (storeDB != other.storeDB) {
			return false;
		}
		return true;
	}
	
}
