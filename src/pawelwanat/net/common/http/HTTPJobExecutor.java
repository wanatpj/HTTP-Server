package pawelwanat.net.common.http;

import static pawelwanat.net.common.http.StringUtils.secure;

import java.net.URLConnection;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import pawelwanat.net.common.JobExecutor;
import pawelwanat.net.common.Server;
import pawelwanat.net.common.http.HTTPObjects.CommonHeaderValue;
import pawelwanat.net.common.http.HTTPObjects.HTTPRequest;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse.HttpResponseBuilder;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponseStatus;
import pawelwanat.net.common.http.contentproviders.ContentProvider;
import pawelwanat.net.common.http.contentproviders.DynamicContentProvider;

@Singleton
public final class HTTPJobExecutor implements
		JobExecutor<HTTPObjects.HTTPRequest, HTTPObjects.HTTPResponse, HTTPUserData> {

	private final ContentProvider cacheContentProvider;
	private final ContentProvider cachableContentProvider; // Content to be cache.
	private final DynamicContentProvider dynamicContentProvider;
	private final ContentProvider fileContentProvider;
	// private final String defaultOnlyHost;  // for host recognition, to be implemented later
	private final HttpResponseBuilder httpResponseBuilder;

	@Inject
	public HTTPJobExecutor(
			@Named("CacheContentProvider") ContentProvider cacheContentProvider,
			@Named("CachableContentProvider") ContentProvider cachableContentProvider,
			DynamicContentProvider dynamicContentProvider,
			@Named("FileContentProvider") ContentProvider fileContentProvider,
			HttpResponseBuilder httpResponseBuilder){
		this.cacheContentProvider = cacheContentProvider;
		this.cachableContentProvider = cachableContentProvider;
		this.dynamicContentProvider = dynamicContentProvider;
		this.fileContentProvider = fileContentProvider;
		this.httpResponseBuilder = httpResponseBuilder;
	}

	@Override
	public HTTPResponse execute(HTTPRequest request, HTTPUserData userData, SelectionKey selectedKey) {
		// Generating response.
		HTTPResponse response;
		// Checking host
		/* Checking host omitted, due to it's not necessary in current impl (only one host) */
		// Getting requested path
		int questionMark = request.path.indexOf('?');
		String path = (questionMark != -1 ? request.path.substring(0, questionMark) : request.path);
		// Method switch
		if("GET".equals(request.method)){
			if (cacheContentProvider.has(path)) {
				Content content = cacheContentProvider.get(path);
				response = httpResponseBuilder.getResponse(HTTPResponseStatus.OK, content);
			} else if (cachableContentProvider.has(path)) {
				// Maybe it could be done in future that here will be cached content, which you
				// need to have privilege to view.
				Content content = cachableContentProvider.get(path);
				cacheContentProvider.put(path, content);
				response = httpResponseBuilder.getResponse(HTTPResponseStatus.OK, content);
			} else if (dynamicContentProvider.applicable(
					request,
					(SocketChannel) selectedKey.channel())) {
				response = dynamicContentProvider.execute(
						request,
						(SocketChannel) selectedKey.channel());
			} else if (fileContentProvider.has(path)) {
				Content content = fileContentProvider.get(path);
				cacheContentProvider.put(path, content);
				response = httpResponseBuilder.getResponse(HTTPResponseStatus.OK, content);
			} else {
				response = httpResponseBuilder.getDefaultResponse(HTTPResponseStatus.NOT_FOUND);
			}
		} else if("POST".equals(request.method)) {
			if (dynamicContentProvider.applicable(
					request,
					(SocketChannel) selectedKey.channel())) {
				response = dynamicContentProvider.execute(
						request,
						(SocketChannel) selectedKey.channel());
			} else {
				response = httpResponseBuilder.getDefaultResponse(HTTPResponseStatus.METHOD_NOT_ALLOWED);
			}
		} else if("PUT".equals(request.method)) {
			if(fileContentProvider.put(path, request.content)){
				response = httpResponseBuilder.getResponse(HTTPResponseStatus.CREATED, new Content("", null));
				cacheContentProvider.put(path, request.content);
			} else {
				response = httpResponseBuilder.getDefaultResponse(HTTPResponseStatus.METHOD_NOT_ALLOWED);
			}
		} else {
			Logger.getLogger(Server.class.getName()).log(Level.INFO, String.format(
					"Requested method (%s) not supported",
					secure(request.method)));
			response = httpResponseBuilder.getDefaultResponse(HTTPResponseStatus.NOT_IMPLEMENTED);
		}
		// Response adornment
		if (request.getHeader(HttpHeaders.CONNECTION)
				.equalsIgnoreCase(CommonHeaderValue.KEEP_ALIVE)) {
			userData.KeepAlive = true;
			response.setHeader(HttpHeaders.CONNECTION, CommonHeaderValue.KEEP_ALIVE);
		}
		return response;
	}

	// Likely to be changed in future.
	@Override
	public boolean hasSuspendedJob(HTTPUserData userData, SelectionKey selectedKey) {
		return false;
	}

	@Override
	public void continueJob() { }

	@Override
	public boolean toBeClosed(HTTPUserData userData, SelectionKey selectedKey) {
		return !userData.KeepAlive;
	}
}