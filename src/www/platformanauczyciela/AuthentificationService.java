package www.platformanauczyciela;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import pawelwanat.net.common.http.HTTPObjects.HTTPRequest;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse.HttpResponseBuilder;
import pawelwanat.net.common.http.auth.SessionProvider;

public class AuthentificationService {
	
	protected final String cookieName;
	protected final SessionProvider sessionProvider;
	protected final HttpResponseBuilder httpResponseBuilder;

	@Inject
	public AuthentificationService(
			SessionProvider sessionProvider,
			@Named("cookieName") String cookieName,
			HttpResponseBuilder httpResponseBuilder) {
		this.sessionProvider = sessionProvider;
		this.cookieName = cookieName;
		this.httpResponseBuilder = httpResponseBuilder;
	}

	public HTTPResponse check(HTTPRequest request, SocketChannel SocketChannel) {
		String credential = request.getCookie(cookieName);
		if(credential != null && sessionProvider.sessionExists(credential)){
			return null; 
		}
		String reqUrl = request.path;
		int idx = reqUrl.indexOf("?");
		if(idx != -1) {
			reqUrl = reqUrl.substring(0, idx);
		}
		try {
			return httpResponseBuilder.redirect(String.format("signup.html?reqUrl=%s", URLEncoder.encode(reqUrl, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error while encoding redirection URL TODO(URL) LOGGER", e);
		}
	}
}
