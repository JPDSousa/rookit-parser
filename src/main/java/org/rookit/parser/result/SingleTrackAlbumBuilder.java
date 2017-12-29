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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.bson.types.ObjectId;
import org.rookit.dm.album.Album;
import org.rookit.dm.album.AlbumFactory;
import org.rookit.dm.album.TypeRelease;
import org.rookit.dm.artist.Artist;
import org.rookit.dm.genre.Genre;
import org.rookit.dm.genre.Genreable;
import org.rookit.dm.play.able.Playable;
import org.rookit.dm.track.Track;
import org.rookit.dm.track.TrackFactory;
import org.rookit.dm.track.TypeTrack;
import org.rookit.dm.track.TypeVersion;
import org.rookit.dm.track.VersionTrack;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.utils.ParserValidator;
import org.rookit.parser.utils.TrackPath;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
				.withId(builder.id)
				.withType(builder.type)
				.withTitle(builder.title)
				.withTypeVersion(builder.versionType)
				.withExtraArtists(builder.extraArtists)
				.withMainArtists(builder.mainArtists)
				.withFeatures(builder.features)
				.withProducers(builder.producers)
				.withPath(builder.path)
				.withExplicit(builder.explicit)
				.withDisc(builder.disc)
				.withNumber(builder.number)
				.withCover(builder.cover)
				.withAlbum(builder.album)
				.withDate(builder.date)
				.withPlays(builder.plays)
				.withSkipped(builder.skipped)
				.withLastPlayed(builder.lastPlayed)
				.withLastSkipped(builder.lastSkipped)
				.withAlbumTitle(builder.albumTitle)
				.withGenres(builder.genres)
				.withDuration(builder.duration)
				.withVersionToken(builder.versionToken)
				.withHiddenTrack(builder.hiddenTrack);
		clone.externalMeta.putAll(builder.getExternalMetadata());
		builder.getIgnored().forEach(i -> clone.withIgnore(i));
		clone.setScore(builder.getScore());
		return clone;
	}
	
	private ObjectId id;
	private TypeTrack type;
	private String title;
	private TypeVersion versionType;
	private Set<Artist> extraArtists;
	private Set<Artist> mainArtists;
	private Set<Artist> features;
	private Set<Artist> producers;
	private TrackPath path;
	private String disc;
	private int number;
	private byte[] cover;
	private Album album;
	private LocalDate date;
	private String albumTitle;
	private Set<Genre> genres;
	private String hiddenTrack;
	private final List<String> ignored;
	private Duration duration;
	private String versionToken;
	private long plays;
	private long skipped;
	private LocalDate lastPlayed;
	private LocalDate lastSkipped;
	private Boolean explicit;
	private final Map<String, Map<String, Object>> externalMeta;
	
	private Album builtAlbum;
	private Track builtTrack;
	
	private final TrackFactory trackFactory;
	private final AlbumFactory albumFactory;
	private TrackFormat format;
	
	private SingleTrackAlbumBuilder(AlbumFactory albumFactory, TrackFactory trackFactory) {
		super();
		this.trackFactory = trackFactory;
		this.albumFactory = albumFactory;
		this.ignored = Lists.newArrayList();
		this.externalMeta = Maps.newHashMap();
		this.disc = Album.DEFAULT_DISC;
	}
	
	public SingleTrackAlbumBuilder withExternalMetadata(String key, Map<String, Object> value) {
		externalMeta.put(key, value);
		return this;
	}
	
	public SingleTrackAlbumBuilder withExplicit(Boolean isExplicit) {
		this.explicit = isExplicit;
		return this;
	}
	
	public SingleTrackAlbumBuilder withId(ObjectId id) {
		this.id = id;
		return this;
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
	
	public SingleTrackAlbumBuilder withPlays(long plays) {
		this.plays = plays;
		return this;
	}
	
	public SingleTrackAlbumBuilder withSkipped(long skipped) {
		this.skipped = skipped;
		return this;
	}
	
	public SingleTrackAlbumBuilder withLastSkipped(LocalDate lastSkipped) {
		this.lastSkipped = lastSkipped;
		return this;
	}
	
	public SingleTrackAlbumBuilder withLastPlayed(LocalDate lastPlayed) {
		this.lastPlayed = lastPlayed;
		return this;
	}
	
	public String getTitle() {
		return title;
	}

	public SingleTrackAlbumBuilder withExtraArtists(Set<Artist> extraArtists) {
		this.extraArtists = extraArtists;
		return this;
	}
	
	public SingleTrackAlbumBuilder withVersionToken(String versionToken) {
		this.versionToken = versionToken;
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
	
	public SingleTrackAlbumBuilder withDuration(Duration duration) {
		this.duration = duration;
		return this;
	}
	
	@Override
	public Duration getDuration() {
		return duration;
	}

	public SingleTrackAlbumBuilder withGenres(Set<Genre> genres) {
		setGenres(genres);
		return this;
	}
	
	@Override
	public Collection<Genre> getAllGenres() {
		return getGenres();
	}

	@Override
	public Collection<Genre> getGenres() {
		return genres;
	}

	@Override
	public Void addGenre(Genre genre) {
		if(genres == null) {
			genres = Sets.newLinkedHashSet();
		}
		genres.add(genre);
		return null;
	}

	@Override
	public Void setGenres(Set<Genre> genres) {
		this.genres = genres;
		return null;
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
	public synchronized Album build() {
		if(builtAlbum == null) {
			final Track track = buildTrack();
			final TypeRelease release = fromTrack(track);
			final Album album = getAlbum(release);
			if(number <= 0) {
				album.addTrackLast(track, disc);
			}
			else {
				album.addTrack(track, number, disc);
			}
			if(cover != null) {
				album.setCover(cover);	
			}
			album.setReleaseDate(date);
			builtAlbum = album;
		}
		return builtAlbum;
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

	public synchronized Track buildTrack() {
		if(builtTrack == null) {
			final Track original = trackFactory.createOriginalTrack(title);
			if(isVersion()) {
				builtTrack = createVersionOf(original);
			}
			else {
				fill(original);
				builtTrack = original;
			}
		}
		return builtTrack;
	}

	private VersionTrack createVersionOf(Track original) {
		final VersionTrack version = trackFactory.createVersionTrack(versionType, original);
		fill(extraArtists, a -> version.addVersionArtist(a));
		fill(producers, a -> version.addProducer(a));
		fill(version);
		return version;
	}

	private void fill(Track track) {
		fill(mainArtists, a -> track.addMainArtist(a));
		fill(features, a -> track.addFeature(a));
		if(explicit != null) {
			track.setExplicit(explicit);
		}
		fill(producers, a -> track.addProducer(a));
		fillPaths(track);
		fillPlayable(track);
		track.getExternalMetadata().putAll(externalMeta);
		fillGenres(track);
		setHiddenTrack(track);
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
			try {
				final OutputStream output = track.getPath().toOutput();
				output.write(Files.readAllBytes(path.getPath()));
				output.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void fillPlayable(Playable playable) {
		if(duration != null) {
			playable.setDuration(duration);
		}
		if(lastPlayed != null) {
			playable.setLastPlayed(lastPlayed);
		}
		if(lastSkipped != null) {
			playable.setLastSkipped(lastSkipped);
		}
		playable.setPlays(plays);
		playable.setSkipped(skipped);
	}

	private void fill(Set<Artist> artists, Consumer<? super Artist> action) {
		if(artists != null) {
			artists.forEach(action);
		}
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
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((explicit == null) ? 0 : explicit.hashCode());
		result = prime * result + ((externalMeta == null) ? 0 : externalMeta.hashCode());
		result = prime * result + ((extraArtists == null) ? 0 : extraArtists.hashCode());
		result = prime * result + ((features == null) ? 0 : features.hashCode());
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((genres == null) ? 0 : genres.hashCode());
		result = prime * result + ((hiddenTrack == null) ? 0 : hiddenTrack.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((ignored == null) ? 0 : ignored.hashCode());
		result = prime * result + ((lastPlayed == null) ? 0 : lastPlayed.hashCode());
		result = prime * result + ((lastSkipped == null) ? 0 : lastSkipped.hashCode());
		result = prime * result + ((mainArtists == null) ? 0 : mainArtists.hashCode());
		result = prime * result + number;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (int) (plays ^ (plays >>> 32));
		result = prime * result + ((producers == null) ? 0 : producers.hashCode());
		result = prime * result + (int) (skipped ^ (skipped >>> 32));
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((versionToken == null) ? 0 : versionToken.hashCode());
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
		if (duration == null) {
			if (other.duration != null) {
				return false;
			}
		} else if (!duration.equals(other.duration)) {
			return false;
		}
		if (explicit == null) {
			if (other.explicit != null) {
				return false;
			}
		} else if (!explicit.equals(other.explicit)) {
			return false;
		}
		if (externalMeta == null) {
			if (other.externalMeta != null) {
				return false;
			}
		} else if (!externalMeta.equals(other.externalMeta)) {
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
		if (format == null) {
			if (other.format != null) {
				return false;
			}
		} else if (!format.equals(other.format)) {
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
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (ignored == null) {
			if (other.ignored != null) {
				return false;
			}
		} else if (!ignored.equals(other.ignored)) {
			return false;
		}
		if (lastPlayed == null) {
			if (other.lastPlayed != null) {
				return false;
			}
		} else if (!lastPlayed.equals(other.lastPlayed)) {
			return false;
		}
		if (lastSkipped == null) {
			if (other.lastSkipped != null) {
				return false;
			}
		} else if (!lastSkipped.equals(other.lastSkipped)) {
			return false;
		}
		if (mainArtists == null) {
			if (other.mainArtists != null) {
				return false;
			}
		} else if (!mainArtists.equals(other.mainArtists)) {
			return false;
		}
		if (number != other.number) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (plays != other.plays) {
			return false;
		}
		if (producers == null) {
			if (other.producers != null) {
				return false;
			}
		} else if (!producers.equals(other.producers)) {
			return false;
		}
		if (skipped != other.skipped) {
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
		if (versionToken == null) {
			if (other.versionToken != null) {
				return false;
			}
		} else if (!versionToken.equals(other.versionToken)) {
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

	@Override
	public String toString() {
		return "SingleTrackAlbumBuilder [id=" + id + ", type=" + type + ", title=" + title + ", versionType="
				+ versionType + ", extraArtists=" + extraArtists + ", mainArtists=" + mainArtists + ", features="
				+ features + ", producers=" + producers + ", path=" + path + ", disc=" + disc + ", number=" + number
				+ ", cover=" + Arrays.toString(cover) + ", album=" + album + ", date=" + date + ", albumTitle="
				+ albumTitle + ", genres=" + genres + ", hiddenTrack=" + hiddenTrack + ", ignored=" + ignored
				+ ", duration=" + duration + ", versionToken=" + versionToken + ", plays=" + plays + ", skipped="
				+ skipped + ", lastPlayed=" + lastPlayed + ", lastSkipped=" + lastSkipped + ", explicit=" + explicit
				+ ", externalMeta=" + externalMeta + ", format=" + format + "]";
	}
	
	public String gerVersionToken() {
		return versionToken;
	}

	@Override
	public LocalDate getLastPlayed() {
		return lastPlayed;
	}

	@Override
	public LocalDate getLastSkipped() {
		return lastSkipped;
	}

	@Override
	public long getPlays() {
		return plays;
	}

	@Override
	public long getSkipped() {
		return skipped;
	}

	@Override
	public Void play() {
		plays++;
		return null;
	}

	@Override
	public Void setDuration(Duration duration) {
		this.duration = duration;
		return null;
	}

	@Override
	public Void setLastPlayed(LocalDate lastPlayed) {
		withLastPlayed(lastPlayed);
		return null;
	}

	@Override
	public Void setLastSkipped(LocalDate lastSkipped) {
		withLastSkipped(lastSkipped);
		return null;
	}

	@Override
	public Void setPlays(long arg0) {
		withPlays(arg0);
		return null;
	}

	@Override
	public Void setSkipped(long arg0) {
		withSkipped(arg0);
		return null;
	}

	@Override
	public Void skip() {
		skipped++;
		return null;
	}

	@Override
	public ObjectId getId() {
		return id;
	}

	@Override
	public String getIdAsString() {
		return id.toHexString();
	}

	@Override
	public LocalDateTime getStorageTime() {
		return ZonedDateTime.ofInstant(id.getDate().toInstant(), ZoneId.systemDefault()).toLocalDateTime();
	}

	@Override
	public void setId(ObjectId arg0) {
		withId(arg0);
	}

	@Override
	public Map<String, Map<String, Object>> getExternalMetadata() {
		return externalMeta;
	}

	@Override
	public Map<String, Object> getExternalMetadata(String arg0) {
		return externalMeta.get(arg0);
	}

	@Override
	public void putExternalMetadata(String arg0, Map<String, Object> arg1) {
		withExternalMetadata(arg0, arg1);
	}
	
}
