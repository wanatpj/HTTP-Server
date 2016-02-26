package pawelwanat.net.echo;

import java.nio.ByteBuffer;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import pawelwanat.net.common.Server;
import pawelwanat.net.common.UserData;

public class Main {

	public static void main(String [] args){
		System.out.println("Starting echo server...");
		Server<ByteBuffer, ByteBuffer, UserData> echoServer =
				Guice.createInjector(new EchoServerModule()).getInstance(Key.get(
						new TypeLiteral<Server<ByteBuffer, ByteBuffer, UserData>>() {}));
		System.out.println("Server created. Running...");
		echoServer.run();
	}
}
