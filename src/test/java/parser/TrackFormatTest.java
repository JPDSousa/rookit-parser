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
package parser;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.rookit.dm.track.TypeTrack;
import org.rookit.parser.exceptions.AmbiguousFormatException;
import org.rookit.parser.exceptions.InvalidFieldException;
import org.rookit.parser.parser.Field;
import org.rookit.parser.parser.TrackFormat;


@SuppressWarnings("javadoc")
public class TrackFormatTest {

	private static TrackFormat guineaPig;
	private static final String[] FORMATS = {"<ARTIST> - <TITLES> X <EXTRA> <VERSION> [<IGNORE>]",
			"<ARTIST> - <TITLES> (<GENRE>)",
			"<ARTIST> feat. <FEAT> (<EXTRA> <VERSION>) [<IGNORE>] - <TITLES>",
			"feat. <FEAT> (<IGNORE>) - <TITLES>"
	};
	
	@Before
	public void setUp() {
		guineaPig = getRandomTrackFormat();
	}

	private TrackFormat getRandomTrackFormat() {
		final int length = FORMATS.length;
		final Random random = new Random();
		return TrackFormat.create(FORMATS[random.nextInt(length)]);
	}

	@Test
	public final void testTrackFormatString() {
		assertNotNull(guineaPig);
	}

	@Test
	public final void testToString() {
		final TrackFormat clone = TrackFormat.create(guineaPig.toString());
		assertEquals(guineaPig.toString(), clone.toString());
	}

	@Test
	public final void testGetFormats() {
		final List<Field> fields = Arrays.asList(Field.ARTIST, Field.TITLES, Field.GENRE);
		guineaPig = TrackFormat.create(FORMATS[1]);
		assertEquals(guineaPig.getFields(), fields);
	}

	@Test
	public final void testGetSeparators() {
		final List<String> seps = Arrays.asList(TrackFormat.SEP_START, " - ", " (", ")");
		guineaPig = TrackFormat.create(FORMATS[1]);
		assertEquals(seps, guineaPig.getSeparators());
	}

	@Test
	public final void testStartsWithField() {
		List<String> seps;
		guineaPig = TrackFormat.create(FORMATS[0]);
		seps = guineaPig.getSeparators();
		assertEquals(TrackFormat.SEP_START, seps.get(0));
		guineaPig = TrackFormat.create(FORMATS[3]);
		seps = guineaPig.getSeparators();
		assertEquals("feat. ", seps.get(0));
	}

	@Test
	public final void testEndsWithField() {
		List<String> seps;
		guineaPig = TrackFormat.create(FORMATS[0]);
		seps = guineaPig.getSeparators();
		assertEquals("]", seps.get(seps.size()-1));
		guineaPig = TrackFormat.create(FORMATS[3]);
		seps = guineaPig.getSeparators();
		assertEquals(TrackFormat.SEP_END, seps.get(seps.size()-1));
	}

	@Test
	public final void testGetTrackClass() {
		guineaPig = TrackFormat.create(FORMATS[0]);
		assertEquals(TypeTrack.VERSION, guineaPig.getTrackClass());
		guineaPig = TrackFormat.create(FORMATS[1]);
		assertEquals(TypeTrack.ORIGINAL, guineaPig.getTrackClass());
	}

	@Test
	public final void testAppendSepToLastField() {
		final String sep = "sep";
		final int len;
		guineaPig = TrackFormat.create(FORMATS[2]);
		len = guineaPig.getSeparators().size()-1;
		guineaPig.appendSep(sep);
		assertEquals(sep, guineaPig.getSeparators().get(len));
	}
	
	@Test
	public final void testAppendSepToLastSep() {
		final String sep = "sep";
		final String lastSep;
		final int len;
		guineaPig = TrackFormat.create(FORMATS[0]);
		len = guineaPig.getSeparators().size();
		lastSep = guineaPig.getSeparators().get(len-1);
		guineaPig.appendSep(sep);
		assertEquals(lastSep+sep, guineaPig.getSeparators().get(len-1));
	}

	@Test
	public final void testAppendFieldToLastSep() {
		final Field field = Field.ALBUM;
		final int len;
		guineaPig = TrackFormat.create(FORMATS[0]);
		len = guineaPig.getFields().size();
		guineaPig.appendField(field);
		assertEquals(field, guineaPig.getFields().get(len));
	}
	
	@Test(expected = AmbiguousFormatException.class)
	public final void testAppendFieldToLastField() {
		guineaPig = TrackFormat.create(FORMATS[2]);
		guineaPig.appendField(Field.ARTIST);
	}

	@Test
	public final void testFits() {
		final String ex = "a - b (c)";
		guineaPig = TrackFormat.create(FORMATS[1]);
		assertTrue(guineaPig.fits(ex));
		assertFalse(guineaPig.fits(ex.substring(3)));
	}

	@Test
	public final void testGetMissingRequiredFields() {
		final Field[] required = {Field.ARTIST, Field.IGNORE};
		guineaPig = TrackFormat.create(FORMATS[1]);
		assertEquals(Arrays.asList(Field.IGNORE), guineaPig.getMissingRequiredFields(required));
	}

	@Test
	public final void testEqualsObject() {
		final TrackFormat f1 = TrackFormat.create(FORMATS[2]);
		final TrackFormat f2 = TrackFormat.create(FORMATS[2]);
		assertEquals(f2, f1);
	}

	@Test
	public final void testCompareTo() {
		final TrackFormat f1 = TrackFormat.create(FORMATS[0]);
		final TrackFormat f2 = TrackFormat.create(FORMATS[1]);
		assertTrue(f2.compareTo(f1) < 0);
		assertTrue(f2.compareTo(f2) == 0);
		assertTrue(f1.compareTo(f2) > 0);
	}
	
	@Test(expected = InvalidFieldException.class)
	public void testContructorInvalidField() {
		TrackFormat.create("<This Field should never exist>");
	}

	@Test(expected = InvalidFieldException.class)
	public void testConstructorEmptyField(){
		TrackFormat.create("<>");
	}

	@Test(expected = AmbiguousFormatException.class)
	public void testConstructorAmbiguousFormat(){
		TrackFormat.create(String.format("---<%s><%s>eolksdlfkds", Field.EXTRA.name(), Field.ARTIST.name()));
	}

	@Test
	public void testConstructor() {
		for(Field f : Field.values()){
			TrackFormat.create(String.format("<%s>", f.name()));
		}
	}

	@Test
	public void testToStringFull(){
		final String[] testStrings = {Field.ALBUM.toString(),
				String.format("-a<%s>-r<%s>-ghj", Field.ARTIST.name(), Field.NUMBER.name())};
		final String field = Field.ARTIST.toString();
		assertEquals(field, TrackFormat.create(field).toString());
		for(String testString : testStrings){
			assertEquals("To string should return initial format string", testString, TrackFormat.create(testString).toString());
		}
	}
	
	@Test
	public void testAppend(){
		final String sepI = " -";
		final String sepII = " ";
		final String field = Field.ARTIST.toString();
		assertEquals(Arrays.asList(TrackFormat.SEP_START, sepI+sepII), TrackFormat.create(field).appendSep(sepI).appendSep(sepII).getSeparators());
		assertEquals(Arrays.asList(TrackFormat.SEP_START, sepI+sepII), TrackFormat.create(field+sepI).appendSep(sepII).getSeparators());
	}

}
