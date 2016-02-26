package pawelwanat.net.common.http.contentproviders;

import java.nio.channels.SocketChannel;

import pawelwanat.net.common.http.HTTPObjects.HTTPRequest;
import pawelwanat.net.common.http.HTTPObjects.HTTPResponse;

public interface SiteContentProvider {

	public HTTPResponse execute(HTTPRequest request, SocketChannel channel);
}
