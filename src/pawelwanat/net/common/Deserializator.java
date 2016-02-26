package pawelwanat.net.common;

import java.nio.ByteBuffer;
import java.text.ParseException;

public interface Deserializator<INPUT> {

	/**
	 * Tests if byte @p b is the last byte of the incoming entry. This method must be called on
	 * every byte from incoming input.
	 * @throws ParseException 
	 */
	boolean process(byte b) throws ParseException;
	
	/**
	 * Parses instance from ByteBuffer. This array must contain exactly one entry and no more
	 * additional bytes.
	 * @param readOnlyRawInstance aggregated bytes from last calls of
	 *     {@link Deserializator}<?>.process(...)
	 */
	INPUT getInstance(ByteBuffer readOnlyRawInstance) throws ParseException;
}
