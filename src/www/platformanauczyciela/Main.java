package www.platformanauczyciela;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import pawelwanat.net.common.Server;
import pawelwanat.net.common.http.HTTPObjects;
import pawelwanat.net.common.http.HTTPUserData;

public class Main {

	public static void main(String args[]){
		Server<HTTPObjects.HTTPRequest, HTTPObjects.HTTPResponse, HTTPUserData> server =
				Guice.createInjector(new ServerModule()).getInstance(Key.get(
						new TypeLiteral<Server<HTTPObjects.HTTPRequest, HTTPObjects.HTTPResponse, HTTPUserData>>(){}));
		server.run();
		System.out.println(String.format("%02x", -12));
	}
}
