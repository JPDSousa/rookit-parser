package org.rookit.parser.storage;

import java.util.List;
import java.util.Map.Entry;

import org.rookit.parser.result.SingleTrackAlbumBuilder;
import org.rookit.parser.utils.TrackPath;

import com.google.common.collect.Lists;

@SuppressWarnings("javadoc")
public class ParseResult implements Entry<TrackPath, List<SingleTrackAlbumBuilder>>{

	private final TrackPath path;
	private List<SingleTrackAlbumBuilder> results;
	
	public ParseResult(TrackPath path) {
		this(path, Lists.newArrayList());
	}
	
	public ParseResult(TrackPath path, List<SingleTrackAlbumBuilder> results) {
		this.path = path;
		this.results = results;
	}

	@Override
	public TrackPath getKey() {
		return path;
	}

	@Override
	public List<SingleTrackAlbumBuilder> getValue() {
		return results;
	}

	@Override
	public List<SingleTrackAlbumBuilder> setValue(List<SingleTrackAlbumBuilder> value) {
		final List<SingleTrackAlbumBuilder> lastResults = this.results;
		this.results = value;
		return lastResults;
	}
	
	public void appendValue(SingleTrackAlbumBuilder value) {
		this.results.add(value);
	}
	
}
