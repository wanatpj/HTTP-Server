package pawelwanat.net.common.http;

import java.io.UnsupportedEncodingException;

public class Content {
	private final String content;
	private byte[] binaryContent;
	private final String mimeContentType;

	/**
	 * @param mime might NOT be null
	 */
	public Content(String content, String mimeContentType){
		this.content = content;
		this.binaryContent = null;
		this.mimeContentType = mimeContentType;
	}

	/**
	 * @param mime might NOT be null
	 */
	public Content(byte [] binaryContent, String mimeContentType){
		this.content = null;
		this.binaryContent = binaryContent;
		this.mimeContentType = mimeContentType;
	}
	
	private Content(){
		content = null;
		binaryContent = new  byte[0];
		mimeContentType = null;
	}
	
	public static Content empty(){
		return new Content();
	}
	
	public String getContent(){
		return content;
	}
	
	public byte [] getBinaryContent(){
		return binaryContent;
	}
	
	public String getContentType(){
		return mimeContentType;
	}
	
	public boolean isTextual(){
		return content != null;
	}
	
	public byte [] getProperContent(){
		if(binaryContent == null){  // it's not isTextual()
			try {
				String charset = extractCharset(mimeContentType);
				binaryContent = content.getBytes(charset);
				return binaryContent;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(String.format(
						"%s is unsupported encoding",
						"US-ASCII"), e);
			}
		}
		return binaryContent;
	}
	
	public int length() {
		return getProperContent().length;
	}

	/**
	 * Fake size. Just for cache.
	 * @return
	 */
	/*public int size(){
		int size = 24;
		if(content != null){
			size += content.length();
		}
		if(binaryContent != null){
			size += binaryContent.length;
		}
		if(mimeContentType != null){
			size += mimeContentType.length();
		}
		return size;
	}*/

	private String extractCharset(String contentType) {
		String result = contentType;
		result = result.substring(result.indexOf("charset"));
		result = result.substring(result.indexOf('=') + 1);
		int idx = result.indexOf(";");
		result = result.substring(0, (idx == -1 ? result.length() : idx)).trim();
		return result;
	}

	public boolean hasContentType() {
		return mimeContentType != null;
	}
}
