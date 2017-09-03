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
