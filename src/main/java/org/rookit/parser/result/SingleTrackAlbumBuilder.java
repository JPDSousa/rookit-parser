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
package org.rookit.parser.result;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.rookit.dm.album.Album;
import org.rookit.dm.album.AlbumFactory;
import org.rookit.dm.album.TypeRelease;
import org.rookit.dm.artist.Artist;
import org.rookit.dm.genre.Genre;
import org.rookit.dm.genre.Genreable;
import org.rookit.dm.track.Track;
import org.rookit.dm.track.TrackFactory;
import org.rookit.dm.track.TypeTrack;
import org.rookit.dm.track.TypeVersion;
import org.rookit.dm.track.VersionTrack;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.utils.ParserValidator;
import org.rookit.parser.utils.TrackPath;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("javadoc")
public class SingleTrackAlbumBuilder extends AbstractResult<Album> implements Genreable {
	
	private static final ParserValidator VALIDATOR = ParserValidator.getDefault();
	
	public static SingleTrackAlbumBuilder create() {
		return new SingleTrackAlbumBuilder(AlbumFactory.getDefault(), TrackFactory.getDefault());
	}
	
	public static SingleTrackAlbumBuilder create(SingleTrackAlbumBuilder builder) {
		VALIDATOR.checkArgumentNotNull(builder, "Cannot clone null");
		final SingleTrackAlbumBuilder clone = create()
				.withType(builder.type)
				.withTitle(builder.title)
				.withTypeVersion(builder.versionType)
				.withExtraArtists(builder.extraArtists)
				.withMainArtists(builder.mainArtists)
				.withFeatures(builder.features)
				.withProducers(builder.producers)
				.withPath(builder.path)
				.withDisc(builder.disc)
				.withNumber(builder.number)
				.withCover(builder.cover)
				.withAlbum(builder.album)
				.withDate(builder.date)
				.withAlbumTitle(builder.albumTitle)
				.withGenres(builder.genres)
				.withHiddenTrack(builder.hiddenTrack);
		builder.getIgnored().forEach(i -> clone.withIgnore(i));
		clone.setScore(builder.getScore());
		return clone;
	}
	
	private TypeTrack type;
	private String title;
	private TypeVersion versionType;
	private Set<Artist> extraArtists;
	private Set<Artist> mainArtists;
	private Set<Artist> features;
	private Set<Artist> producers;
	private TrackPath path;
	private String disc;
	private Integer number;
	private byte[] cover;
	private Album album;
	private LocalDate date;
	private String albumTitle;
	private Set<Genre> genres;
	private String hiddenTrack;
	private final List<String> ignored;
	private long duration;
	
	private final TrackFactory trackFactory;
	private final AlbumFactory albumFactory;
	private TrackFormat format;
	
	private SingleTrackAlbumBuilder(AlbumFactory albumFactory, TrackFactory trackFactory) {
		super();
		this.duration = Track.UNDEF_DURATION;
		this.trackFactory = trackFactory;
		this.albumFactory = albumFactory;
		this.ignored = Lists.newArrayList();
	}

	public SingleTrackAlbumBuilder withType(TypeTrack type) {
		this.type = type;
		return this;
	}

	public TypeTrack getType() {
		return type;
	}

	public SingleTrackAlbumBuilder withTypeVersion(TypeVersion versionType) {
		this.versionType = versionType;
		return this;
	}
	
	public TypeVersion getTypeVersion() {
		return versionType;
	}

	public SingleTrackAlbumBuilder withMainArtists(Set<Artist> artists) {
		this.mainArtists = artists;
		return this;
	}
	
	public SingleTrackAlbumBuilder withFeatures(Set<Artist> artists) {
		this.features = artists;
		return this;
	}
	
	public SingleTrackAlbumBuilder withProducers(Set<Artist> artists) {
		this.producers = artists;
		return this;
	}
	
	public SingleTrackAlbumBuilder withTitle(String title) {
		this.title = title;
		return this;
	}
	
	public String getTitle() {
		return title;
	}

	public SingleTrackAlbumBuilder withExtraArtists(Set<Artist> extraArtists) {
		this.extraArtists = extraArtists;
		return this;
	}
	
	public SingleTrackAlbumBuilder withPath(TrackPath path) {
		this.path = path;
		return this;
	}

	public TrackPath getPath() {
		return path;
	}

	public SingleTrackAlbumBuilder withDisc(String disc) {
		this.disc = disc;
		return this;
	}
	
	public String getDisc() {
		return disc;
	}

	public SingleTrackAlbumBuilder withNumber(Integer number) {
		this.number = number;
		return this;
	}
	
	public Integer getNumber() {
		return number;
	}

	public SingleTrackAlbumBuilder withCover(byte[] cover) {
		this.cover = cover;
		return this;
	}
	
	public SingleTrackAlbumBuilder withAlbum(Album album) {
		this.album = album;
		return this;
	}
	
	public SingleTrackAlbumBuilder withDate(LocalDate date) {
		this.date = date;
		return this;
	}
	
	public SingleTrackAlbumBuilder withAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
		return this;
	}
	
	public SingleTrackAlbumBuilder withDuration(long duration) {
		this.duration = duration;
		return this;
	}
	
	public SingleTrackAlbumBuilder withGenres(Set<Genre> genres) {
		setGenres(genres);
		return this;
	}
	
	@Override
	public Iterable<Genre> getAllGenres() {
		return getGenres();
	}

	@Override
	public Iterable<Genre> getGenres() {
		return genres;
	}

	@Override
	public void addGenre(Genre genre) {
		if(genres == null) {
			genres = Sets.newLinkedHashSet();
		}
		genres.add(genre);
	}

	@Override
	public void setGenres(Set<Genre> genres) {
		this.genres = genres;
	}

	public SingleTrackAlbumBuilder withHiddenTrack(String hiddenTrack) {
		this.hiddenTrack = hiddenTrack;
		return this;
	}

	public SingleTrackAlbumBuilder withIgnore(String ignore) {
		this.ignored.add(ignore);
		return this;
	}

	public List<String> getIgnored() {
		return ignored;
	}

	@Override
	public Album build() {
		final Track track = buildTrack();
		final TypeRelease release = fromTrack(track);
		final Album album = getAlbum(release);
		album.addTrack(track, number, disc);
		if(cover != null) {
			album.setCover(cover);	
		}
		album.setReleaseDate(date);
		return album;
	}

	public Track getTrack() {
		return buildTrack();
	}

	private Album getAlbum(final TypeRelease release) {
		if(album != null) {
			return album;
		}
		final String albumTitle = this.albumTitle != null ? this.albumTitle : title;
		return albumFactory.createSingleArtistAlbum(albumTitle, release, mainArtists);
	}

	private TypeRelease fromTrack(Track track) {
		if(track.isVersionTrack()) {
			switch(track.getAsVersionTrack().getVersionType()) {
			case ACOUSTIC:
				break;
			case ALTERNATIVE:
				break;
			case BONUS:
				break;
			case COVER:
				return TypeRelease.COVERS;
			case DEMO:
				break;
			case EXTENDED:
				break;
			case INSTRUMENTAL:
				break;
			case INTERLUDE:
				break;
			case LIVE:
				return TypeRelease.LIVE;
			case PREVIEW:
				return TypeRelease.PROMO;
			case RADIO:
				break;
			case REMIX:
				return TypeRelease.REMIXES;
			case SECOND:
				break;
			case VIDEO:
				break;
			}
		}
		return TypeRelease.SINGLE;
	}

	private Track buildTrack() {
		final Track original = trackFactory.createOriginalTrack(title);
		fill(original);
		if(isVersion()) {
			return createVersionOf(original);
		}
		original.setDuration(duration);
		fill(producers, a -> original.addProducer(a));
		fillPaths(original);
		fillGenres(original);
		setHiddenTrack(original);
		return original;
	}

	private boolean isVersion() {
		return type == TypeTrack.VERSION 
				|| (type == null && versionType != null);
	}

	private void setHiddenTrack(Track track) {
		if(hiddenTrack != null) {
			track.setHiddenTrack(hiddenTrack);
		}
	}

	private void fillGenres(Track track) {
		if(genres != null) {
			track.setGenres(genres);
		}
	}

	private void fillPaths(Track track) {
		if(path != null) {
			track.getPath().attachFile(path.getAbsolutePath());
		}
	}

	private VersionTrack createVersionOf(Track original) {
		final VersionTrack version = trackFactory.createVersionTrack(versionType, original);
		original.setDuration(duration);
		fill(extraArtists, a -> version.addVersionArtist(a));
		fill(producers, a -> version.addProducer(a));
		fillPaths(version);
		fillGenres(version);
		setHiddenTrack(version);
		return version;
	}

	private void fill(Set<Artist> artists, Consumer<? super Artist> action) {
		if(artists != null) {
			artists.forEach(action);
		}
	}

	private void fill(Track track) {
		fill(mainArtists, a -> track.addMainArtist(a));
		fill(features, a -> track.addFeature(a));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((album == null) ? 0 : album.hashCode());
		result = prime * result + ((albumTitle == null) ? 0 : albumTitle.hashCode());
		result = prime * result + Arrays.hashCode(cover);
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((disc == null) ? 0 : disc.hashCode());
		result = prime * result + ((extraArtists == null) ? 0 : extraArtists.hashCode());
		result = prime * result + ((features == null) ? 0 : features.hashCode());
		result = prime * result + ((genres == null) ? 0 : genres.hashCode());
		result = prime * result + ((hiddenTrack == null) ? 0 : hiddenTrack.hashCode());
		result = prime * result + ((mainArtists == null) ? 0 : mainArtists.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((producers == null) ? 0 : producers.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((versionType == null) ? 0 : versionType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SingleTrackAlbumBuilder other = (SingleTrackAlbumBuilder) obj;
		if (album == null) {
			if (other.album != null) {
				return false;
			}
		} else if (!album.equals(other.album)) {
			return false;
		}
		if (albumTitle == null) {
			if (other.albumTitle != null) {
				return false;
			}
		} else if (!albumTitle.equals(other.albumTitle)) {
			return false;
		}
		if (!Arrays.equals(cover, other.cover)) {
			return false;
		}
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		if (disc == null) {
			if (other.disc != null) {
				return false;
			}
		} else if (!disc.equals(other.disc)) {
			return false;
		}
		if (extraArtists == null) {
			if (other.extraArtists != null) {
				return false;
			}
		} else if (!extraArtists.equals(other.extraArtists)) {
			return false;
		}
		if (features == null) {
			if (other.features != null) {
				return false;
			}
		} else if (!features.equals(other.features)) {
			return false;
		}
		if (genres == null) {
			if (other.genres != null) {
				return false;
			}
		} else if (!genres.equals(other.genres)) {
			return false;
		}
		if (hiddenTrack == null) {
			if (other.hiddenTrack != null) {
				return false;
			}
		} else if (!hiddenTrack.equals(other.hiddenTrack)) {
			return false;
		}
		if (mainArtists == null) {
			if (other.mainArtists != null) {
				return false;
			}
		} else if (!mainArtists.equals(other.mainArtists)) {
			return false;
		}
		if (number == null) {
			if (other.number != null) {
				return false;
			}
		} else if (!number.equals(other.number)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (producers == null) {
			if (other.producers != null) {
				return false;
			}
		} else if (!producers.equals(other.producers)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (versionType != other.versionType) {
			return false;
		}
		return true;
	}

	public TrackFormat getFormat() {
		return format;
	}
	
	public void attachFormat(TrackFormat format) {
		this.format = format;
	}
	
	
}
