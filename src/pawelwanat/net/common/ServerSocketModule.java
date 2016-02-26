package pawelwanat.net.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

public class ServerSocketModule extends AbstractModule {

	@Override
	protected void configure() {
	}

	@Provides
	@Named("AcceptSelector") AbstractSelector getAbstractSelector(
			@Named("port") int port) throws IOException {
		AbstractSelector selector = SelectorProvider.provider().openSelector();
		ServerSocketChannel channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(port));
		channel.register(selector, SelectionKey.OP_ACCEPT);
		return selector;
	}
}
