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
package org.rookit.parser.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;


@SuppressWarnings("javadoc")
public final class PathUtils {

	private static final ParserValidator VALIDATOR = ParserValidator.getDefault();

	private PathUtils(){}

	public static String getFormat(Path path){
		VALIDATOR.checkArgumentNotNull(path, "The path cannot be null");
		final String name = path.getFileName().toString();
		final int index = name.lastIndexOf('.');

		return index > 0 ? name.substring(index+1) : "";
	}
	
	public static String getFormat(TrackPath path) {
		return getFormat(path.getPath());
	}

	public static String getFileName(Path path){
		VALIDATOR.checkArgumentNotNull(path, "The path cannot be null");
		final String name = path.getFileName().toString();
		final int index = name.lastIndexOf('.');

		return index > 0 ? name.substring(0, index) : "";
	}
	
	public static String getFileName(TrackPath path) {
		return getFileName(path.getPath());
	}

	public static void deleteFile(Path path) {
		try {
			Files.delete(path);
			VALIDATOR.info("Deleted: " + path);
		} catch (IOException e) {
			VALIDATOR.handleIOException(e);
		}
	}

	public static void backUpFile(final Path path) {
		final StringBuilder builder = new StringBuilder("_");
		final Path newPath;
		builder.append(path.getFileName().toString());

		newPath = path.getParent().resolve(builder.toString());
		try {
			FileUtils.copyDirectory(path.toFile(), newPath.toFile());
		} catch (IOException e) {
			VALIDATOR.handleIOException(e);
		}
	}
	
}
