package pawelwanat.net.common.http;

import static pawelwanat.net.common.http.StringUtils.secure;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public final class HTTPObjects {

	public final static Set<String> headerNames;

	static {
		try {
			headerNames = Sets.newHashSet();
			for (Field header : HttpHeaders.class.getFields()) {
				if(Modifier.isStatic(header.getModifiers())){
					headerNames.add((String) header.get(null));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public final class CommonHeaderValue {
		private CommonHeaderValue() {}

		public static final String KEEP_ALIVE = "Keep-Alive";
		public static final String CONTENT_TYPE_TEXT_UTF8 = "text/html; charset=utf-8";  // UTF-8, for sure?
	}

	public static enum HTTPResponseStatus {
		OK(200, "OK"),
		CREATED(201, "Created"),
		FOUND(302, "Found"),
		NOT_FOUND(404, "Not Found"),
		METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
		NOT_IMPLEMENTED(501, "Not Implemented"),
		;
		private final int responseCode;
		private final String text;

		private HTTPResponseStatus(int responseCode, String text){
			this.responseCode = responseCode;
			this.text = text;
		}
		
		public int getCode(){
			return responseCode;
		}
		
		public String getText(){
			return text;
		}
		
		public boolean isErrorMessage(){
			return 400 <= responseCode && responseCode <= 599;
		}
	}
	
	// public static ImmutableSet<String> supportedMethods = ImmutableSet.of("GET", "POST", "PUT");

	public final static class HTTPRequest {
		public final String method;
		public final String path;
		public final String version;
		private final Map<String, String> headers = Maps.newHashMap();
		private final Map<String, String> post = Maps.newHashMap();
		public Content content;
		
		// Parsing POST
		/*String postContent = getString(vector, 0, vector.size());
		for(String keyValue : postContent.split("&")){
			int index = keyValue.indexOf('=');
			request.setPost(
					keyValue.substring(0, index),
					keyValue.substring(index + 1));
		}*/

		public HTTPRequest(String method, String path, String version) throws ParseException{
			this.method = method;
			this.path = path;
			this.version = version;
		}
		
		void setHeader(String name, String value) throws ParseException {
			if(headerNames.contains(name)){
				headers.put(name, value);
				return;
			}
			throw new ParseException(String.format("No such header: %s", secure(name)), 0);
		}

		public String getHeader(String name) {
			return headers.get(name);
		}

		public boolean hasHeader(String string) {
			return headers.containsKey(string);
		}

		public void setPost(String key, String value) {
			post.put(key, value);
		}

		public String getCookie(String cookieName) {
			// TODO: optimize
			if(hasHeader(HttpHeaders.COOKIE)){
				for(String pair : getHeader(HttpHeaders.COOKIE).split(";")) {
					pair = pair.trim();
					int idx = pair.indexOf("=");
					if(cookieName.equals(pair.subSequence(0, idx))){
						return pair.substring(idx + 1);
					}
				}
			}
			return null;
		}
	}
	
	public final static class HTTPResponse {
		
		@Singleton
		public static class HttpResponseBuilder {
			
			private final String version;

			@Inject
			public HttpResponseBuilder(@Named("version") String version) {
				this.version = version;
			}

			public HTTPResponse redirect(String location) {
				HTTPResponse response = new HTTPResponse(
						version,
						HTTPResponseStatus.FOUND,
						Content.empty());
				response.setHeader(HttpHeaders.LOCATION, location);
				return response;
			}
			
			public HTTPResponse getDefaultResponse(HTTPResponseStatus status){
				return getResponse(
						status,
						new Content(
								String.format("<h1>%d %s</h1>", status.getCode(), status.getText()),
								HTTPObjects.CommonHeaderValue.CONTENT_TYPE_TEXT_UTF8));
			}
			
			public HTTPResponse getResponse(HTTPResponseStatus status, Content content){
				// ignoring request version
				HTTPResponse httpResponse = new HTTPResponse(version, status, content);
				if(content.hasContentType()){
					httpResponse.setHeader(HttpHeaders.CONTENT_TYPE, content.getContentType());
				}
				httpResponse.setHeader(
						HttpHeaders.CONTENT_LENGTH,
						String.format("%d", content.getProperContent().length));
				return httpResponse;
			}
		}
		public final String version;
		public final HTTPResponseStatus responseStatus;
		private final Map<String, String> headers = Maps.newHashMap();
		public final Content content;

		public void setHeader(String name, String value) {
			if(headerNames.contains(name)){
				headers.put(name, value);
				return;
			}
			throw new RuntimeException(String.format("No such header: %s", secure(name)));
		}

		public String getHeader(String name) {
			return headers.get(name);
		}

		public boolean hasHeader(String string) {
			return headers.containsKey(string);
		}
		
		public Iterator<String> headerNameIterator(){
			return headers.keySet().iterator();
		}
		
		public HTTPResponse(String version, HTTPResponseStatus responseStatus, Content content) {
			this.version = version;
			this.responseStatus = responseStatus;
			this.content = content;
		}
	}
}