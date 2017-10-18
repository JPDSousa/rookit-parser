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

@SuppressWarnings("javadoc")
public enum FormatSong{
	MP3("mp3", true),
	M4A("m4a", false),
	FLAC("flac", false),
	WAV("wav", false);

	private final String name;
	private final boolean allowed;

	private FormatSong(final String name, final boolean allowed){
		this.name = name;
		this.allowed = allowed;
	}

	public String getName(){
		return name;
	}

	public boolean isAllowed(){
		return allowed;
	}

	public static boolean isAllowed(String format){
		boolean allowed = false;

		for(FormatSong formatType : values()){
			if(formatType.getName().equalsIgnoreCase(format)){
				allowed = formatType.isAllowed();
				break;
			}
		}

		return allowed;
	}

	public static boolean isValid(String format){
		boolean valid = false;

		for(FormatSong formatType : values()){
			if(formatType.getName().equalsIgnoreCase(format)){
				valid = true;
				break;
			}
		}

		return valid;
	}
}
