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
import java.util.stream.Stream;

import org.rookit.parser.utils.DirectoryFilters;
import org.rookit.parser.utils.ParserValidator;
import org.rookit.utils.builder.StreamGenerator;

@SuppressWarnings("javadoc")
public class GenAlbumGenerator implements StreamGenerator<Path>, AutoCloseable {

	private static final ParserValidator VALIDATOR = ParserValidator.getDefault();

	private final String artistRegex;
	private final String albumRegex;
	private Stream<Path> stream;
	private final Path source;

	public GenAlbumGenerator(Path source, String artistRegex, String albumRegex) {
		super();
		this.artistRegex = artistRegex;
		this.albumRegex = albumRegex;
		this.source = source;
	}

	@Override
	public Stream<Path> generate() {
		stream = list(source)
				.filter(p -> matchesRegex(p, artistRegex))
				.filter(DirectoryFilters.newRegularDirectoryStreamFilter())
				.flatMap(p -> list(p))
				.filter(p -> p != null)
				.filter(p -> matchesRegex(p, albumRegex))
				.filter(DirectoryFilters.newGeneratedDirectoryStreamFilter());
		return stream;
	}
	
	@Override
	public void close() {
		stream.close();
	}

	private boolean matchesRegex(Path p, String regex) {
		return regex == null || p.getFileName().toString().matches(regex);
	}

	private Stream<Path> list(Path p) {
		try {
			return Files.list(p);
		} catch (IOException e) {
			VALIDATOR.handleIOException(e);
			return null;
		}
	}

}
