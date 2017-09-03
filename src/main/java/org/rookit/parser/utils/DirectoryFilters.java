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
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("javadoc")
public final class DirectoryFilters {

	private enum DirectoryTokens {
		DOWNLOADED(new String[]{" - ", " � "}),
		DISCOGRAPHY(new String[]{"discography"});

		private final String[] tokens;

		private DirectoryTokens(final String[] tokens) {
			this.tokens = tokens;
		}

		private String[] getTokens() {
			return tokens;
		}
	}

	private static Filter<Path> toFilter(final Predicate<Path> predicate) {
		return new Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException {
				return predicate.test(entry);
			}
		};
	}

	public static Filter<Path> newRegularDirectoryFilter(){
		return toFilter(newRegularDirectoryStreamFilter());
	}

	public static Predicate<Path> newRegularDirectoryStreamFilter() {
		return new RegularDirectoryFilter();
	}

	public static Filter<Path> newDiscographyFilter() {
		return toFilter(newDiscographyStreamFilter());
	}

	public static Predicate<Path> newDiscographyStreamFilter() {
		return new DiscographyFilter();
	}

	public static Filter<Path> newNonDiscographyFilter() {
		return toFilter(newNonDiscographyStreamFilter());
	}

	public static Predicate<Path> newNonDiscographyStreamFilter() {
		return new NonDiscographyFilter();
	}

	public static Filter<Path> newGeneratedDirectoryFilter() {
		return toFilter(newGeneratedDirectoryStreamFilter());
	}

	public static Predicate<Path> newGeneratedDirectoryStreamFilter() {
		return new GeneratedDirectoryFilter();
	}

	public static Filter<Path> newNonGeneratedDirectoryFilter() {
		return toFilter(newNonGeneratedDirectoryStreamFilter());
	}

	public static Predicate<Path> newNonGeneratedDirectoryStreamFilter() {
		return new NonGeneratedDirectoryFilter();
	}

	public static Filter<Path> newTrackFilter(){
		return toFilter(newTrackStreamFilter());
	}

	public static Predicate<Path> newTrackStreamFilter() {
		return new TrackFilter();
	}

	public static Filter<Path> newCustomSearchFilter(final String query){
		return toFilter(newCustomSearchStreamFilter(query));
	}

	public static Predicate<Path> newCustomSearchStreamFilter(final String query) {
		return new CustomSearchFilter(query);
	}

	private static class RegularDirectoryFilter implements Predicate<Path>{

		private RegularDirectoryFilter() {}

		@Override
		public boolean test(final Path entry) {
			return Files.isDirectory(entry);
		}

	}

	private static class GeneratedDirectoryFilter implements Predicate<Path> {

		private final Predicate<Path> discographyFilter;
		private final Predicate<Path> nonDiscographyFilter;

		private GeneratedDirectoryFilter() {
			discographyFilter = new DiscographyFilter();
			nonDiscographyFilter = new NonDiscographyFilter();
		}

		@Override
		public boolean test(Path entry) {
			return !discographyFilter.test(entry) && !nonDiscographyFilter.test(entry);
		}
	}

	private static class NonGeneratedDirectoryFilter implements Predicate<Path> {

		private final Predicate<Path> discographyFilter;
		private final Predicate<Path> nonDiscographyFilter;

		private NonGeneratedDirectoryFilter() {
			discographyFilter = new DiscographyFilter();
			nonDiscographyFilter = new NonDiscographyFilter();
		}

		@Override
		public boolean test(Path entry) {
			return discographyFilter.test(entry) || nonDiscographyFilter.test(entry);
		}
	}

	private static class DiscographyFilter extends RegularDirectoryFilter {

		private DiscographyFilter() {}

		@Override
		public boolean test(Path entry) {
			return super.test(entry) 
					&& Arrays.stream(DirectoryTokens.DISCOGRAPHY.getTokens())
					.filter(token -> StringUtils.containsIgnoreCase(entry.getFileName().toString(), token))
					.findFirst().isPresent(); 
		}
	}

	private static class NonDiscographyFilter extends RegularDirectoryFilter {

		private NonDiscographyFilter() {}

		@Override
		public boolean test(Path entry) {
			return super.test(entry) 
					&& Arrays.stream(DirectoryTokens.DOWNLOADED.getTokens())
					.filter(t -> StringUtils.containsIgnoreCase(entry.getFileName().toString(), t))
					.findFirst().isPresent();
		}
	}

	private static class TrackFilter implements Predicate<Path>{

		private TrackFilter() {}

		@Override
		public boolean test(final Path entry) {
			return Files.isRegularFile(entry) && FormatSong.isValid(PathUtils.getFormat(entry));
		}
	}

	private static class CustomSearchFilter implements Predicate<Path>{

		private final String query;

		private CustomSearchFilter(final String query) {
			super();
			this.query = query;
		}

		@Override
		public boolean test(final Path entry) {
			return entry.getFileName().toString().equals(query);
		}
	}

	private DirectoryFilters() {}

}
