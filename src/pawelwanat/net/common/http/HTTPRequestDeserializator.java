package pawelwanat.net.common.http;

import static pawelwanat.net.common.http.StringUtils.getString;
import static pawelwanat.net.common.http.StringUtils.secure;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Vector;

import com.google.common.net.HttpHeaders;
import com.google.common.primitives.Bytes;

import pawelwanat.net.common.Deserializator;
import pawelwanat.net.common.http.HTTPObjects.HTTPRequest;

public class HTTPRequestDeserializator implements Deserializator<HTTPObjects.HTTPRequest>{
	
	private HTTPObjects.HTTPRequest request = null;
	private final Vector<Byte> vector = new Vector<Byte>();
	private boolean carriageReturn = false;
	private boolean firstLineParsed = false;
	private boolean headersDone = false;
	
	int cnt = 0;
	int contentLength = 0;

	@Override
	public boolean process(byte b) throws ParseException {
		System.out.print((char)b);

		++cnt;
		vector.add(b);
		boolean newLine = false;
		if (b == '\r') {
			carriageReturn = true;
		} else if (carriageReturn && b == '\n'){
			newLine = true;
			carriageReturn = false;
		} else {
			carriageReturn = false;
		}
		if(headersDone){
			if(cnt == contentLength){
				String mime = (request.hasHeader(HttpHeaders.CONTENT_TYPE) ?
						request.getHeader(HttpHeaders.CONTENT_TYPE) : null);
				request.content = new Content(Bytes.toArray(vector), mime);
				clear();
				return true;
			}
		} else if (newLine) {
			vector.remove(vector.size() - 1);
			vector.remove(vector.size() - 1);
			if (!firstLineParsed) {
				firstLineParsed = true;
				int firstSpace = vector.indexOf((byte)' ');
				int secondSpace = vector.indexOf((byte)' ', firstSpace + 1);
				if (firstSpace == -1 || secondSpace == -1) {
					throw new ParseException(String.format(
							"Improper first line: %s [firstSpace=%d, secondSpace=%d]",
							secure(getString(vector, 0, vector.size())),
							firstSpace,
							secondSpace), 0);
				}
				String method = getString(vector, 0, firstSpace);
				String path = getString(vector, firstSpace + 1, secondSpace);
				String version = getString(vector, secondSpace + 1, vector.size());
				request = new HTTPObjects.HTTPRequest(method, path, version);
			} else /*if(!headersDone)*/ {
				int colon = vector.indexOf((byte)':');
				if (colon != -1) {
					String name = getString(vector, 0, colon);
					String value = getString(vector, colon + 1, vector.size()).trim();
					request.setHeader(name, value);
				} else if(vector.isEmpty()) {
					if(!request.hasHeader(HttpHeaders.CONTENT_LENGTH)){
						clear();
						return true;
					}
					contentLength = Integer.parseInt(request.getHeader(HttpHeaders.CONTENT_LENGTH));
					if (contentLength == 0) {
						clear();
						return true;
					}
					headersDone = true;
					cnt = 0;
				} else {
					throw new ParseException(String.format(
							"Improper header: %s",
							secure(getString(vector, 0, vector.size()))), 0);
				}
			}
			vector.clear();
		}
		return false;
	}

	@Override
	public HTTPRequest getInstance(ByteBuffer rawInstance) {
		HTTPRequest result = request;
		request = null;
		return result;
	}

	private void clear(){
		vector.clear();
		carriageReturn = false;
		firstLineParsed = false;
		headersDone = false;
		cnt = 0;
		contentLength = 0;
	}
}
