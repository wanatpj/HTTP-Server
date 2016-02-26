package www.platformanauczyciela;

import java.net.Socket;
import java.nio.channels.SocketChannel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import pawelwanat.net.common.http.HTTPObjects.HTTPRequest;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse.HttpResponseBuilder;
import pawelwanat.net.common.http.auth.SessionProvider;

@Singleton
public class PupilAuthentificationService extends AuthentificationService {

	private final SessionProvider sessionProvider;
	private final HttpResponseBuilder builder;

	@Inject
	public PupilAuthentificationService(
			SessionProvider sessionProvider,
			HttpResponseBuilder builder){
		this.sessionProvider = sessionProvider;
		this.builder = builder;
	}
	
	//@Override
	public HTTPResponse check(HTTPRequest request, SocketChannel socketChannel) {
		String sidCookie = request.getCookie("SID");
		if(sidCookie != null &&
				sessionProvider.sessionExists(sidCookie) &&
				sessionProvider.getVariable(sidCookie, SessionNames.PUPIL) != null){
			return null; 
		}
		String reqUrl = request.path;
		int idx = reqUrl.indexOf("?");
		if(idx != -1) {
			reqUrl = reqUrl.substring(0, idx);
		}
		return builder.redirect(String.format("scribe.html?reqUrl=%s", reqUrl));
	}

}
