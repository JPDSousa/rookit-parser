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
package org.rookit.parser.formatlist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.rookit.mongodb.DBManager;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.utils.resource.Resources;

@SuppressWarnings("javadoc")
public class FormatList {

	private static final String PREFIX_COMMENT = "//";
	private static final Path DEFAULT_FORMATS_PATH = Resources.RESOURCES_MAIN.resolve("formats.txt");
	
	public static final FormatList readDefaults() throws IOException {
		return readFromPath(DEFAULT_FORMATS_PATH);
	}
	
	public static final FormatList readFromPath(Path path) throws IOException {
		final Stream<String> rawFormats = Files.lines(path)
				.filter(l -> !l.startsWith(PREFIX_COMMENT));
		return new FormatList(rawFormats);
	}
	
	public static final FormatList readFromDB(DBManager db) {
		return new FormatList(db.streamTrackFormats());
	}

	private final List<TrackFormat> formats;

	FormatList(Stream<String> rawFormats){
		formats = rawFormats
				.map(TrackFormat::create)
				.collect(Collectors.toList());
	}

	public Stream<TrackFormat> getAll() {
		return formats.stream();
	}

}
