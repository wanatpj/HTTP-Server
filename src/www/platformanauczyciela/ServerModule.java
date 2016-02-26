package www.platformanauczyciela;

import static com.google.inject.name.Names.named;

import java.nio.ByteBuffer;
import java.util.Map;

import pawelwanat.net.common.Deserializator;
import pawelwanat.net.common.JobExecutor;
import pawelwanat.net.common.Serializator;
import pawelwanat.net.common.ServerSocketModule;
import pawelwanat.net.common.http.HTTPJobExecutor;
import pawelwanat.net.common.http.HTTPObjects;
import pawelwanat.net.common.http.HTTPRequestDeserializator;
import pawelwanat.net.common.http.HTTPResponseSerializator;
import pawelwanat.net.common.http.HTTPServerModule;
import pawelwanat.net.common.http.HTTPUserData;
import pawelwanat.net.common.http.contentproviders.ContentProvider;
import pawelwanat.net.common.http.contentproviders.SiteContentProvider;
import www.platformanauczyciela.dynamic.Create;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

public class ServerModule extends AbstractModule {

	@Override
	protected void configure() {
		// Server Instantiation
		install(new ServerSocketModule());
		install(new HTTPServerModule());

		bind(Integer.class).annotatedWith(named("port")).toInstance(12345);
		bind(new TypeLiteral<JobExecutor<
				HTTPObjects.HTTPRequest,
				HTTPObjects.HTTPResponse,
				HTTPUserData>>() {})
				.to(HTTPJobExecutor.class);

		// HTTPJobExecutor
		bind(String.class).annotatedWith(named("version")).toInstance("HTTP/1.1");
		bind(ContentProvider.class)
			.annotatedWith(named("CachableContentProvider"))
			.to(CacheableContentProvider.class);

		// FileContentProvider
		bind(String.class).annotatedWith(named("AbsolutePath")).toInstance("/home/pawelwanat/www");
		bind(Boolean.class).annotatedWith(named("CanModifyOrCreateFiles")).toInstance(true);
		bind(String.class).annotatedWith(named("DefauleFileName")).toInstance("index.html");

		// CacheContentProvider
		bind(Integer.class).annotatedWith(named("CacheCapacity")).toInstance(200000000);

		// ConnectionInformation
		bind(Integer.class).annotatedWith(named("MaxAccumulated")).toInstance(1000000);
		bind(new TypeLiteral<Serializator<HTTPObjects.HTTPResponse>>(){})
			.to(HTTPResponseSerializator.class);
		bind(new TypeLiteral<Deserializator<HTTPObjects.HTTPRequest>>(){})
			.to(HTTPRequestDeserializator.class);
		
		// SessionProvider
		bind(Integer.class).annotatedWith(named("MaxCookiesNumber")).toInstance(10000);
		bind(Integer.class).annotatedWith(named("SessionCredentialsStrength")).toInstance(128);
	}
	
	// ConnectionInformation
	@Provides
	@Named("InputBuffer")
	ByteBuffer getInputByteBuffer(){
		return ByteBuffer.allocate(4096);
	}

	@Provides
	@Named("OutputBuffer")
	ByteBuffer getOutputByteBuffer(){
		return ByteBuffer.allocate(4096);
	}

	@Provides
	@Singleton
	Map<String, SiteContentProvider> getDynamicSites() {
		Map<String, SiteContentProvider> result = Maps.newHashMap();
		result.put("/create.dyn", Guice.createInjector(this).getInstance(Create.class));
		return result;
	}
}
