package www.platformanauczyciela;

import java.net.Socket;
import java.nio.channels.SocketChannel;

import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import pawelwanat.net.common.http.Content;
import pawelwanat.net.common.http.HTTPObjects;
import pawelwanat.net.common.http.HTTPObjects.HTTPRequest;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponseStatus;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse.HttpResponseBuilder;
import pawelwanat.net.common.http.auth.SessionProvider;

@Singleton
public class TeacherAuthentificationService extends AuthentificationService {

	@Inject
	public TeacherAuthentificationService(
			SessionProvider sessionProvider,
			@Named("cookieName") String cookieName,
			HttpResponseBuilder httpResponseBuilder){
		super(sessionProvider, cookieName, httpResponseBuilder);
	}

	@Override
	public HTTPResponse check(HTTPRequest request, SocketChannel socketChannel) {
		String credential = request.getCookie(cookieName);
		if(credential != null &&
				sessionProvider.sessionExists(credential) &&
				sessionProvider.getVariable(credential, SessionNames.TEACHER) != null){
			return null; 
		}
		String reqUrl = request.path;
		int idx = reqUrl.indexOf("?");
		if(idx != -1) {
			reqUrl = reqUrl.substring(0, idx);
		}
		return httpResponseBuilder.redirect(String.format("signup.html?reqUrl=%s", reqUrl));
	}
}
