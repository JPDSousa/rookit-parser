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
package org.rookit.parser.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.rookit.database.DBManager;
import org.rookit.parser.exceptions.InvalidSongFormatException;
import org.rookit.parser.formatlist.FormatList;
import org.rookit.parser.parser.FormatParser;
import org.rookit.parser.parser.Parser;
import org.rookit.parser.parser.ParserConfiguration;
import org.rookit.parser.parser.ParserFactory;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.DirectoryFilters;
import org.rookit.parser.utils.ParserValidator;
import org.rookit.parser.utils.TrackPath;
import org.rookit.utils.builder.StreamGenerator;

@SuppressWarnings("javadoc")
public class TrackParserGenerator implements StreamGenerator<ParseResult>, AutoCloseable {

	private static final ParserValidator VALIDATOR = ParserValidator.getDefault();
	
	private final FormatList list;
	private final ParserFactory parserFactory;
	private Stream<ParseResult> stream;
	private final Path source;
	private final DBManager db;

	public TrackParserGenerator(FormatList list, Path source, DBManager db) {
		super();
		this.list = list;
		this.parserFactory = ParserFactory.create();
		this.source = source;
		this.db = db;
	}

	@Override
	public Stream<ParseResult> generate() {
		final FormatParser parser = createFormatParser();

		stream = list(source)
				//filters tracks
				.filter(DirectoryFilters.newTrackStreamFilter())
				//filters parseable tracks
				.filter(p -> {
					try {
						return parser.checkSong(p);
					} catch (InvalidSongFormatException e) {
//						try {
//							PMSpecialFolders.moveTo(PMSpecialFolders.FORMAT, source, p);
//						} catch (IOException e1) {
//							handleException(e1, VALIDATOR);
//							return false;
//						}
						return false;
					}})
				.map(p -> TrackPath.create(p))
				.map(p -> new ParseResult(p, parser.multiparse(p)));
		return stream;
	}

	private FormatParser createFormatParser() {
		final List<TrackFormat> formats = list.getAll()
				.collect(Collectors.toList());
		final ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> config = Parser.createConfiguration(SingleTrackAlbumBuilder.class);
		config.withDBConnection(db)
		.withBaseParser(createTagParser())
		.withTrackFormats(formats)
		.withLimit(6);
		return parserFactory.newFormatParser(config);
	}

	private Parser<TrackPath, SingleTrackAlbumBuilder> createTagParser() {
		return parserFactory.newTagParserWithDbConnection(db);
	}

	private Stream<Path> list(Path source) {
		try {
			return Files.list(source);
		} catch (IOException e) {
			VALIDATOR.handleIOException(e);
			return null;
		}
	}

	@Override
	public void close() {
		stream.close();
	}

}
