package org.rookit.parser.config;

import static org.rookit.utils.config.ConfigUtils.getOrDefault;

@SuppressWarnings("javadoc")
public class FormatParserConfig {
	
	private String scoreRatio;

	public String getScoreRatio() {
		return getOrDefault(scoreRatio, "1:1");
	}

	public void setScoreRatio(String scoreRatio) {
		this.scoreRatio = scoreRatio;
	}
	
	
}