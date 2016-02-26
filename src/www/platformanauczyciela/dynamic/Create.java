package www.platformanauczyciela.dynamic;

import java.nio.channels.SocketChannel;

import com.google.inject.Inject;

import pawelwanat.net.common.http.HTTPObjects.HTTPRequest;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponseStatus;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse.HttpResponseBuilder;
import pawelwanat.net.common.http.contentproviders.SiteContentProvider;
import www.platformanauczyciela.TeacherAuthentificationService;

public class Create implements SiteContentProvider {

	private final TeacherAuthentificationService authService;
	private final HttpResponseBuilder builder;

	@Inject
	public Create(TeacherAuthentificationService authService, HttpResponseBuilder builder) {
		this.authService = authService;
		this.builder = builder;
	}
	
	@Override
	public HTTPResponse execute(HTTPRequest request, SocketChannel channel) {
		HTTPResponse response = authService.check(request, channel);
		if(response != null){
			return response;
		}
		return builder.getDefaultResponse(HTTPResponseStatus.NOT_FOUND);
	}
}