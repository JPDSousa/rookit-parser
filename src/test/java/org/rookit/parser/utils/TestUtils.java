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
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.rookit.parser.formatlist.FormatList;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.utils.TrackPath;
import org.rookit.utils.resource.Resources;

@SuppressWarnings("javadoc")
public class TestUtils {
	
	public static final Path RESOURCES_TRACKS = Resources.RESOURCES_TEST.resolve("tracks");
	public static final Path RESOURCES_TRACKS_UNPARSED = RESOURCES_TRACKS.resolve("unparsed");
	public static final Path RESOURCES_TRACKS_UNPARSED_FORMATS = RESOURCES_TRACKS_UNPARSED.resolve("formats.txt");
	
	public static final TrackPath[] TRACK_PATHS = {
			TrackPath.create(RESOURCES_TRACKS_UNPARSED.resolve("Afterlight.mp3")),
			TrackPath.create(RESOURCES_TRACKS_UNPARSED.resolve("Helium.mp3")),
			TrackPath.create(RESOURCES_TRACKS_UNPARSED.resolve("Help.mp3"))
	};
	
	public static final List<TrackFormat> getTestFormats() throws IOException {
		final FormatList list = FormatList.readFromPath(RESOURCES_TRACKS_UNPARSED_FORMATS);
		return list.getAll().collect(Collectors.toList());
	}
	
	public static final TrackPath getRandomTrackPath() {
		final Random random = new Random();
		final TrackPath[] trackPaths = TRACK_PATHS;
		final int index = random.nextInt(trackPaths.length);
		return trackPaths[index];
	}

}
