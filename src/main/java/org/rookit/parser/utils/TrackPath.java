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

import org.apache.commons.text.RandomStringGenerator;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

@SuppressWarnings("javadoc")
public class TrackPath {
	
	public static final String PATHS = "paths";
	
	public static final long MAX_DURATION = 20*60*1000;
	
	private static final RandomStringGenerator RANDOM_STRING_GENERATOR =
			new RandomStringGenerator.Builder().build();
	
	private static final ParserValidator VALIDATOR = ParserValidator.getDefault();

	public static TrackPath create(Path path) {
		return new TrackPath(path);
	}
	
	private final Path path;

	private TrackPath(Path path) {
		validateTrackPath(path.toAbsolutePath());
		this.path = path;
	}

	private void validateTrackPath(Path path) {
		if(!Files.isRegularFile(path) || !FormatSong.isValid(PathUtils.getFormat(path))) {
			throw new IllegalArgumentException(path + " is not a valid track file");
		}
	}
	
	public Mp3File getMp3() {
		try {
			return new Mp3File(getAbsolutePath().toFile());
		} catch (IOException e) {
			VALIDATOR.handleIOException(e);
		} catch (UnsupportedTagException e) {
			VALIDATOR.handleMp3Exception(e);
		} catch (InvalidDataException e) {
			VALIDATOR.handleMp3Exception(e);
		}
		return null;
	}
	
	public Path getAbsolutePath() {
		return path.toAbsolutePath();
	}
	
	public Path getPath() {
		return path;
	}
	
	public String getFileName() {
		return path.getFileName().toString();
	}
	
	public void updateMP3(Mp3File metadata) {
		final Path absPath = getAbsolutePath();
		final Path parentPath = absPath.getParent();
		final Path randomChildPath = parentPath.resolve(RANDOM_STRING_GENERATOR.generate(20));
		try {
			metadata.save(randomChildPath.toString());
			Files.delete(absPath);
			Files.move(randomChildPath, randomChildPath.resolveSibling(getPath().getFileName()));
		} catch (IOException e) {
			VALIDATOR.handleIOException(e);
		} catch (NotSupportedException e) {
			VALIDATOR.handleMp3Exception(e);
		}
	}
	
	public long getDurationSec() {
		return getDurationMiliSec()/1000;
	}
	
	private void validateDurationMiliSec(long duration) {
		if(duration <= 0) {
			throw new IllegalArgumentException(getPath() + ": duration must be greater than 0 seconds");
		}
		else if(duration > MAX_DURATION) {
			throw new IllegalArgumentException(getPath() + ": duration cannot be greater than " + MAX_DURATION + " seconds");
		}
	}

	public long getDurationMiliSec() {
		final long duration = getMp3().getLengthInMilliseconds();
		validateDurationMiliSec(duration);
		return duration;
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public String toString() {
		return path.toString();
	}
}
