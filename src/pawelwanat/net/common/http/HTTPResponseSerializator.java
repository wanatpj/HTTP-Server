package pawelwanat.net.common.http;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.google.inject.Singleton;

import pawelwanat.net.common.Serializator;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse;

@Singleton
public class HTTPResponseSerializator implements Serializator<HTTPObjects.HTTPResponse> {

	@Override
	public ByteBuffer toByteBuffer(HTTPResponse response) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(String.format(
				"%s %d %s\r\n",
				response.version,
				response.responseStatus.getCode(),
				response.responseStatus.getText()));
		Iterator<String> headerNames = response.headerNameIterator();
		while(headerNames.hasNext()){
			String name = headerNames.next();
			String value = response.getHeader(name);
			stringBuffer.append(String.format("%s: %s\r\n", name, value));
		}
		stringBuffer.append("\r\n");
		byte [] head;
		try {
			head = stringBuffer.toString().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(String.format("%s is unsupported encoding", "US-ASCII"), e);
		}
		byte [] tail = response.content.getProperContent();
		ByteBuffer serialized = ByteBuffer.allocate(head.length + tail.length);
		serialized.put(head);
		serialized.put(tail);
		serialized.flip();
		byte [] tmp = new byte [head.length];
		serialized.get(tmp);
		serialized.rewind();
		return serialized;
	}

}
