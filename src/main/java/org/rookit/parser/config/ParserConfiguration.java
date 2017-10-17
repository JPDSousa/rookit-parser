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
