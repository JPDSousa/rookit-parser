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

import org.rookit.mongodb.DBManager;
import org.rookit.parser.result.Result;
import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TrackPath;

@SuppressWarnings("javadoc")
public class ParserFactory {
	
	public static ParserFactory create() {
		return new ParserFactory();
	}
	
	private ParserFactory() {}

	private ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> getDefaultTrackPathConfig() {
		return Parser.createConfiguration(SingleTrackAlbumBuilder.class);
	}
	
	private ParserConfiguration<String, SingleTrackAlbumBuilder> getDefaultStringConfig() {
		return Parser.createConfiguration(SingleTrackAlbumBuilder.class);
	}
	
	@SuppressWarnings("unused")
	public <I, O extends Result<?>> ParserPipeline<I, I, O> newParserPipeline(Class<I> inputClass, O baseResult) {
		return new BottomParserPipeline<I, O>(baseResult);
	}
	
	public <I, O extends Result<?>> ParserPipeline<I, I, O> newParserPipeline(O baseResult) {
		return new BottomParserPipeline<>(baseResult);
	}

	public TagParser newTagParser(ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> config) {
		return TagParser.create(config);
	}
	
	public TagParser newTagParserWithDefaultConfiguration() {
		return newTagParser(getDefaultTrackPathConfig());
	}
	
	public TagParser newTagParserWithDbConnection(DBManager connection) {
		return newTagParser(getDefaultTrackPathConfig().withDBConnection(connection));
	}
	
	public FormatParser newFormatParser(ParserConfiguration<String, SingleTrackAlbumBuilder> config) {
		return FormatParser.create(config);
	}
	
	public FormatParser newFormatParserWithDefaultConfiguration() {
		return newFormatParser(getDefaultStringConfig());
	}
	
	public FormatParser newFormatParserWithDbConnection(DBManager connection) {
		return newFormatParser(getDefaultStringConfig().withDBConnection(connection));
	}
	
	public FormatParser newFormatParserWithTrackFormats(List<TrackFormat> formats) {
		return newFormatParser(getDefaultStringConfig().withTrackFormats(formats));
	}
	
	public FileStructureParser newFileStructureParser(ParserConfiguration<TrackPath, SingleTrackAlbumBuilder> config) {
		return FileStructureParser.create(config);
	}
	
	public FileStructureParser newFileStructureParserWithDefaultConfiguration() {
		return newFileStructureParser(getDefaultTrackPathConfig());
	}
	
	public FileStructureParser newFileStructureParserWithDbConnection(DBManager connection) {
		return newFileStructureParser(getDefaultTrackPathConfig().withDBConnection(connection));
	}
}
