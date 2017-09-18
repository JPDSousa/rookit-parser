package utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.rookit.parser.formatlist.FormatList;
import org.rookit.parser.formatlist.FormatListManager;
import org.rookit.parser.parser.TrackFormat;
import org.rookit.parser.utils.TrackPath;

@SuppressWarnings("javadoc")
public class TestUtils {
	
	public static final Path RESOURCES = Paths.get("src", "test", "resources");
	public static final Path RESOURCES_TRACKS = RESOURCES.resolve("tracks");
	public static final Path RESOURCES_TRACKS_UNPARSED = RESOURCES_TRACKS.resolve("unparsed");
	public static final Path RESOURCES_TRACKS_UNPARSED_FORMATS = RESOURCES_TRACKS_UNPARSED.resolve("formats.txt");
	
	public static final TrackPath[] TRACK_PATHS = {};
	
	public static final List<TrackFormat> getTestFormats() {
		final FormatList list = FormatListManager.getManager().get(RESOURCES_TRACKS_UNPARSED_FORMATS);
		return list.getAll().collect(Collectors.toList());
	}
	
	public static final TrackPath getRandomTrackPath() {
		final Random random = new Random();
		final TrackPath[] trackPaths = TRACK_PATHS;
		final int index = random.nextInt(trackPaths.length);
		return trackPaths[index];
	}

}
