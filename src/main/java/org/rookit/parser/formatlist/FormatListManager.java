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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rookit.parser.parser.TrackFormat;

@SuppressWarnings("javadoc")
public class FormatListManager {

	private static final String DEFAULT_FORMAT_LIST_TRACK = "resources/formatListTrack";
	private static final String DEFAULT_FORMAT_LIST_ALBUM = "resources/formatListAlbum";
	private static final String[] DEFAULT_FORMATS = {"0: [<GENRE>] - <ARTIST> - <TITLE>", 
														"0: <ARTIST> - <TITLE>"};
	private static FormatListManager singleton;

	public static FormatListManager getManager(){
		if(singleton == null){
			singleton = new FormatListManager();
		}
		return singleton;
	}

	public static List<TrackFormat> getDefaultFormats() {
		final List<TrackFormat> defFormats = new ArrayList<>();

		for(String defFormat : DEFAULT_FORMATS){
			defFormats.add(TrackFormat.create(defFormat));
		}

		return defFormats;
	}
	
	private final Map<Path, FormatList> formatLists;
	
	private FormatListManager(){
		final Path defaultPath = Paths.get(DEFAULT_FORMAT_LIST_TRACK);
		final Path defaultAlbum = Paths.get(DEFAULT_FORMAT_LIST_ALBUM);
		formatLists = new HashMap<>();
		formatLists.put(defaultPath, new FormatList(defaultPath));
		formatLists.put(defaultAlbum, new FormatList(defaultAlbum));
	}
	
	public FormatList get(Path formatListPath){
		formatLists.putIfAbsent(formatListPath, new FormatList(formatListPath));
		return formatLists.get(formatListPath);
	}
	
	public FormatList getDefault4Tracks(){
		return get(Paths.get(DEFAULT_FORMAT_LIST_TRACK));
	}
	
	public FormatList getDefault4Albuns(){
		return get(Paths.get(DEFAULT_FORMAT_LIST_ALBUM));
	}
}
