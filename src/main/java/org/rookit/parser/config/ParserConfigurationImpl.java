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

import java.util.List;

import org.rookit.mongodb.DBManager;
import org.rookit.parser.parser.Field;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.result.Result;

class ParserConfigurationImpl implements ParserConfiguration {
	
	private final Class<? extends Result<?>> resultClass;
	private boolean setDate;
	private Field[] requiredFields;
	private List<TrackFormat> formats;
	private int limit;
	
	private DBManager database;
	private boolean storeDB;
	
	ParserConfigurationImpl(Class<? extends Result<?>> resultClass) {
		this.resultClass = resultClass;
		this.setDate = true;
		this.storeDB = true;
	}
	
	@Override
	public ParserConfiguration withDBConnection(DBManager connection) {
		this.database = connection;
		return this;
	}
	
	@Override
	public ParserConfiguration withSetDate(boolean setDate) {
		this.setDate = setDate;
		return this;
	}
	
	
	@Override
	public ParserConfiguration withRequiredFields(Field[] fields) {
		requiredFields = fields;
		return this;
	}
	
	
	@Override
	public ParserConfiguration withDbStorage(boolean storeDB) {
		this.storeDB = storeDB;
		return this;
	}
	
	@Override
	public ParserConfiguration withTrackFormats(List<TrackFormat> formats) {
		this.formats = formats;
		return this;
	}
	
	@Override
	public ParserConfiguration withLimit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public Class<? extends Result<?>> getResultClass() {
		return resultClass;
	}
	
	@Override
	public DBManager getDBConnection() {
		return database;
	}

	@Override
	public boolean isStoreDB() {
		return database != null && storeDB;
	}

	@Override
	public boolean isSetDate() {
		return setDate;
	}
	
	@Override
	public Field[] getRequiredFields() {
		return requiredFields;
	}

	@Override
	public List<TrackFormat> getFormats() {
		return formats;
	}
	
	@Override
	public int getLimit() {
		return limit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resultClass == null) ? 0 : resultClass.hashCode());
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
		ParserConfigurationImpl other = (ParserConfigurationImpl) obj;
		if (resultClass == null) {
			if (other.resultClass != null) {
				return false;
			}
		} else if (!resultClass.equals(other.resultClass)) {
			return false;
		}
		return true;
	}
	
}
