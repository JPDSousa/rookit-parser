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
import org.rookit.parser.utils.ParserValidator;

@SuppressWarnings("javadoc")
public interface ParserConfiguration {

	ParserValidator VALIDATOR = ParserValidator.getDefault();
	
	static ParserConfiguration create(Class<? extends Result<?>> resultClass, ParsingConfig mainConfig) {
		VALIDATOR.checkArgumentNotNull(resultClass, "Must provide a result class");
		return new ParserConfigurationImpl(resultClass)
				.withLimit(mainConfig.getParserLimit())
				.withFormatParserConfig(mainConfig.getFormatParser());
	}

	int getLimit();

	List<TrackFormat> getFormats();

	Field[] getRequiredFields();

	boolean isSetDate();

	boolean isStoreDB();

	DBManager getDBConnection();

	Class<? extends Result<?>> getResultClass();
	
	float getTrackFormatPercentage();
	float getTokenizerPercentage();

	ParserConfiguration withLimit(int limit);

	ParserConfiguration withTrackFormats(List<TrackFormat> formats);

	ParserConfiguration withDbStorage(boolean storeDB);

	ParserConfiguration withRequiredFields(Field[] fields);

	ParserConfiguration withSetDate(boolean setDate);

	ParserConfiguration withDBConnection(DBManager connection);
	
	ParserConfiguration withFormatParserConfig(FormatParserConfig config);

}
