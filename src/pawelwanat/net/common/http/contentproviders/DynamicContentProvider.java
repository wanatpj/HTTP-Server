package pawelwanat.net.common.http.contentproviders;

import java.nio.channels.SocketChannel;
import java.util.Map;

import com.google.inject.Inject;

import pawelwanat.net.common.http.HTTPObjects.HTTPRequest;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse;

public class DynamicContentProvider {

	private final Map<String, SiteContentProvider> sites;

	@Inject
	public DynamicContentProvider(Map<String, SiteContentProvider> sites) {
		this.sites = sites;
	}
	
	public boolean applicable(HTTPRequest request, SocketChannel channel) {
		if(sites.containsKey(request.path)){
			return true;
		}
		return false;
	}

	public HTTPResponse execute(HTTPRequest request, SocketChannel channel) {
		return sites.get(request.path).execute(request, channel);
	}
}
