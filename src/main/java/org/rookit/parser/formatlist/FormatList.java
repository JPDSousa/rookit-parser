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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.rookit.dm.track.TypeTrack;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.utils.ParserValidator;

@SuppressWarnings("javadoc")
public class FormatList {

	private static final String PREFIX_COMMENT = "//";
	private static final ParserValidator VALIDATOR = ParserValidator.getDefault();

	private final Path listFile;

	FormatList(final Path file){
		listFile = file;
	}

	public void put(TrackFormat format) {
		try {
			final BufferedWriter writter = Files.newBufferedWriter(listFile, StandardOpenOption.APPEND);
			writter.write(format.toString()+"\n");
			writter.close();
		} catch(IOException e) {
			VALIDATOR.handleIOException(e);
		}
	}

	public Stream<TrackFormat> getAll() {
		try {
			return Files.lines(listFile)
					.filter(l -> !l.startsWith(PREFIX_COMMENT))
					.map(l -> TrackFormat.create(l))
					.filter(t -> t != null);
		} catch (IOException e) {
			VALIDATOR.handleIOException(e);
			return null;
		}
	}

	public void reset() {
		try {
			final FileWriter writter = new FileWriter(listFile.toFile(), false);
			StringBuilder content = new StringBuilder(32);

			for(TypeTrack t : TypeTrack.values()){
				content.append(PREFIX_COMMENT).append(t.ordinal()).append(": ").append(t.name()).append('\n');
			}

			writter.write(content.toString());
			writter.close();
		} catch (IOException e) {
			VALIDATOR.handleIOException(e);
		}
	}

	public void sort() {
		SortedSet<TrackFormat> formats = new TreeSet<>(getAll().collect(Collectors.toSet()));
		reset();
		formats.forEach(f -> put(f));
	}

}
