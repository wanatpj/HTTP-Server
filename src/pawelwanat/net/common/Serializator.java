package pawelwanat.net.common;

import java.nio.ByteBuffer;

public interface Serializator<OUTPUT> {
	/**
	 * @return serialized object.
	 */
	ByteBuffer toByteBuffer(OUTPUT instance);
}
