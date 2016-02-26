package pawelwanat.net.common.http;

import static com.google.inject.name.Names.named;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pawelwanat.net.common.http.contentproviders.CacheContentProvider;
import pawelwanat.net.common.http.contentproviders.ContentProvider;
import pawelwanat.net.common.http.contentproviders.FileContentProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

public class HTTPServerModule extends AbstractModule {

	@Override
	protected void configure() {
		// HTTPJobExecutor
		bind(ContentProvider.class)
			.annotatedWith(named("CacheContentProvider"))
			.to(CacheContentProvider.class);
		bind(ContentProvider.class)
			.annotatedWith(named("FileContentProvider"))
			.to(FileContentProvider.class);
	}

	// HTTPJobExecutor
	@Provides
	@Named("DefaultOnlyHost")
	String getDefaultOnlyHost(){
		try {
			// TOCHECK: This protect to connect outside of LAN.
			// TODO: possible to connect outside of LAN
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException("Couldn't get localhost IP address.", e);
		}
	}
}
