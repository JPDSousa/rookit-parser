package utils;

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
