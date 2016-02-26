package pawelwanat.net.echo;

import java.nio.ByteBuffer;

import com.google.inject.Singleton;

import pawelwanat.net.common.Serializator;

@Singleton
public class ByteBufferCRLFSerializator implements Serializator<ByteBuffer>{

	@Override
	public ByteBuffer toByteBuffer(ByteBuffer instance) {
		ByteBuffer result = ByteBuffer.allocate(instance.limit() + 2);
		result.put(instance);
		result.put((byte)'\r').put((byte)'\n').flip();
		return result;
	}

}
