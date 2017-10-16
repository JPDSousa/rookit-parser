package org.rookit.parser.parser;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rookit.parser.parser.ParserFactory;
import org.rookit.parser.parser.ParserPipeline;
import org.rookit.parser.result.SingleTrackAlbumBuilder;

@SuppressWarnings("javadoc")
public class ParserPipelineTest {
	
	private static ParserFactory factory;
	
	@BeforeClass
	public static final void setup() {
		factory = ParserFactory.create();
	}
	
	@Test 
	public final void testInputMapping() {
		final SingleTrackAlbumBuilder baseResult = SingleTrackAlbumBuilder.create();
		final ParserPipeline<Integer, String, SingleTrackAlbumBuilder> pipeline = factory.newParserPipeline(Integer.class, baseResult)
				.mapInput(i -> Integer.toString(i));
		final Integer testInt = 5;
		assertEquals(Integer.toString(5), pipeline.input2CurrentInput(testInt));
	}
	
	@Test
	public final void testBottom() {
		final SingleTrackAlbumBuilder baseResult = SingleTrackAlbumBuilder.create();
		final ParserPipeline<String, String, SingleTrackAlbumBuilder> pipeline = factory.newParserPipeline(String.class, baseResult);
		final String testStr = "someRandomString";
		assertNotNull(pipeline);
		assertEquals(testStr, pipeline.input2CurrentInput(testStr));
		assertEquals(baseResult, pipeline.parse(testStr));
	}
	
	

}
