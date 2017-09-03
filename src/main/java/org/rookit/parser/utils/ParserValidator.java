package org.rookit.parser.utils;

import org.rookit.parser.exceptions.ParserException;
import org.rookit.parser.exceptions.ParserTestException;
import org.rookit.utils.log.Errors;
import org.rookit.utils.log.Logs;
import org.rookit.utils.log.Validator;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

@SuppressWarnings("javadoc")
public class ParserValidator extends Validator {
	
	private static final ParserValidator SINGLETON = new ParserValidator();
	
	public static ParserValidator getDefault() {
		return SINGLETON;
	}
	
	private ParserValidator() {
		super(Logs.PARSING.getLogger());
	}
	
	public void handleTestException(Throwable cause) {
		Errors.handleException(new ParserTestException(cause), logger);
	}
	
	public void handleParseException(Throwable cause) {
		Errors.handleException(new ParserException(cause), logger);
	}
	
	public void handleMp3Exception(NotSupportedException cause) {
		Errors.handleException(new RuntimeException(cause), logger);
	}
	
	public void handleMp3Exception(UnsupportedTagException cause) {
		Errors.handleException(new RuntimeException(cause), logger);
	}
	
	public void handleMp3Exception(InvalidDataException cause) {
		Errors.handleException(new RuntimeException(cause), logger);
	}
	
	public void handleTrackFormatException(RuntimeException cause) {
		Errors.handleException(cause, logger);
	}

}
