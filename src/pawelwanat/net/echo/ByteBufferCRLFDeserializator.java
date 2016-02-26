package pawelwanat.net.echo;

import java.nio.ByteBuffer;

import pawelwanat.net.common.CRLFDeserializator;

public class ByteBufferCRLFDeserializator extends CRLFDeserializator<ByteBuffer> {

	@Override
	public ByteBuffer getInstance(ByteBuffer readOnlyRawInstance) {
		readOnlyRawInstance.limit(readOnlyRawInstance.limit() - 2);
		ByteBuffer result = ByteBuffer.allocate(readOnlyRawInstance.limit());
		result.put(readOnlyRawInstance).flip();
		return result;
	}

}
