package pawelwanat.net.echo;

import static com.google.inject.name.Names.named;

import java.nio.ByteBuffer;

import pawelwanat.net.common.Deserializator;
import pawelwanat.net.common.JobExecutor;
import pawelwanat.net.common.Serializator;
import pawelwanat.net.common.ServerSocketModule;
import pawelwanat.net.common.UserData;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

public class EchoServerModule extends AbstractModule {

	@Override
	protected void configure() {
		// Server Instantiation
		install(new ServerSocketModule());
		bind(Integer.class).annotatedWith(named("port")).toInstance(7);
		bind(new TypeLiteral<JobExecutor<ByteBuffer, ByteBuffer, UserData>>() {})
				.to(EchoJobExecutor.class);
		
		// ConnectionInformation Instantiation
		bind(Integer.class).annotatedWith(named("MaxAccumulated")).toInstance(1024);
		bind(new TypeLiteral<Serializator<ByteBuffer>>(){}).to(ByteBufferCRLFSerializator.class);
		bind(new TypeLiteral<Deserializator<ByteBuffer>>(){}).to(ByteBufferCRLFDeserializator.class);
	}

	// for ConnectionInformation
	@Provides
	@Named("InputBuffer")
	ByteBuffer getInputByteBuffer(){
		return ByteBuffer.allocate(4096);
	}
}
