package pawelwanat.net.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractSelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Universal Server. It providers single way conversation, according to rule: I listen one of
 * sentence from client, then I talk and don't listen to client, until I'm finished. I can
 * converse with many clients at the same time.
 * 
 * @author pawelwanat
 */
@Singleton
public class Server<INCOMING, OUTCOMING, USR extends UserData> implements Runnable {

	//private final ServerSocketChannel channel;
	private final AbstractSelector keySelector;
	private final JobExecutor<INCOMING, OUTCOMING, USR> jobExecutor;
	private final Provider<ConnectionInformation<INCOMING, OUTCOMING, USR>> connectionInformationProvider;

	@Inject
	public Server(
			@Named("AcceptSelector") AbstractSelector acceptSelector,
			JobExecutor<INCOMING, OUTCOMING, USR> jobExecutor,
			Provider<ConnectionInformation<INCOMING, OUTCOMING, USR>> connectionInformationProvider) {
		this.keySelector = acceptSelector;
		this.jobExecutor = jobExecutor;
		this.connectionInformationProvider = connectionInformationProvider;
	}

	public void run() {
		System.out.println("Running server...");
		System.out.println(String.format("Localhost LAN address: %s", Arrays.toString(getIPAdresses())));
		while (true) {
			System.out.println("Num keys: " + keySelector.keys().size());
			int x = 0;
			System.out.println(String.format("timestamp: %s", System.currentTimeMillis()));
			try {
				x = this.keySelector.select();
				System.out.println(String.format("Selected %d keys.", x));
			} catch (Exception ex) {
				Logger.getLogger(Server.class.getName()).log(Level.SEVERE,
						"Couldn't select SelectionKeys", ex);
				break;
			}
			if (x > 0) {
				Iterator<SelectionKey> selectedKeys = this.keySelector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();
					if (key.isValid()) {
						if (key.isWritable())
							writeKey(key);
						else if (key.isReadable())
							readKey(key);
						else if (key.isAcceptable())
							acceptKey(key);
					}
				}
			} else if (Thread.currentThread().isInterrupted()) {
				return;  // TO BE TESTED
			}
		}
	}

	private void acceptKey(SelectionKey serverChannelKey) {
		try {
			SocketChannel channel = ((ServerSocketChannel) serverChannelKey.channel()).accept();
			System.out.println(String.format(
					"Accept key for address: %s:%d",
					channel.socket().getInetAddress(),
					channel.socket().getPort()));
			channel.configureBlocking(false);
			SelectionKey clientChannelKey = channel
					.register(this.keySelector, SelectionKey.OP_READ);
			clientChannelKey.attach(connectionInformationProvider.get());
		} catch (Exception e) {
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	private void readKey(SelectionKey selectedKey) {
		SocketChannel channel = (SocketChannel) selectedKey.channel();
		System.out.println(String.format(
				"Read key for address: %s:%d",
				channel.socket().getInetAddress(),
				channel.socket().getPort()));
		@SuppressWarnings("unchecked")
		ConnectionInformation<INCOMING, OUTCOMING, USR> connectionInformation =
				(ConnectionInformation<INCOMING, OUTCOMING, USR>) selectedKey.attachment();
		try {
			int newBytes = connectionInformation.getByteProcessor().updateInput(channel);
			if (newBytes == -1) {  // EOS
				finKey(selectedKey);
				return;
			}
			if (connectionInformation.getByteProcessor().hasNextOnInput()) {
				selectedKey.interestOps(SelectionKey.OP_WRITE);
			}
		} catch (Exception e) {
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
			finKey(selectedKey);
		}
	}

	private void writeKey(SelectionKey selectedKey) {
		SocketChannel channel = (SocketChannel) selectedKey.channel();
		System.out.println(String.format(
				"Write key for address: %s:%d",
				channel.socket().getInetAddress(),
				channel.socket().getPort()));
		@SuppressWarnings("unchecked")
		ConnectionInformation<INCOMING, OUTCOMING, USR> connectionInformation =
				(ConnectionInformation<INCOMING, OUTCOMING, USR>) selectedKey.attachment();
		
		try {
			// Checking, if we have some job to do.
			if(jobExecutor.hasSuspendedJob(connectionInformation.getUserData(), selectedKey)) {
				jobExecutor.continueJob();
			} else if (connectionInformation.getByteProcessor().hasNextOnInput()) {
				OUTCOMING instance = jobExecutor.execute(
						connectionInformation.getByteProcessor().nextFromInput(),
						connectionInformation.getUserData(),
						selectedKey
						);
				if (instance != null) {
					connectionInformation.getByteProcessor().updateOutput(instance);
				}
				flushOutput(selectedKey, channel, connectionInformation);
			}
			// Flushing output.
			if (connectionInformation.getByteProcessor().hasBytesToSend()) {
				flushOutput(selectedKey, channel, connectionInformation);
			}
			// Check, if we should switch key to read mode.
			if (!connectionInformation.getByteProcessor().hasBytesToSend() &&
					!connectionInformation.getByteProcessor().hasNextOnInput() &&
					!jobExecutor.hasSuspendedJob(
							connectionInformation.getUserData(),
							selectedKey)) {
				// In fact this should be executed, when !jobExecutor.hasSuspendedJob() &&
				// getByteProcessor().hasBytesToSend(), but it is also good place.
				if(jobExecutor.toBeClosed(
						connectionInformation.getUserData(),
						selectedKey)){
					finKey(selectedKey);
					return;
				}
				selectedKey.interestOps(SelectionKey.OP_READ);
			}
		} catch(Exception e){
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
			finKey(selectedKey);
		}
	}

	private void flushOutput(
			SelectionKey selectedKey,
			WritableByteChannel channel,
			ConnectionInformation<INCOMING, OUTCOMING, USR> connectionInformation) {
		try {
			connectionInformation.getByteProcessor().nextToOutput(channel);
		} catch (IOException ex) {
			Logger.getLogger(Server.class.getName()).log(Level.SEVERE,
					null, ex);
			finKey(selectedKey);
		}
	}

	// TODO
	private void finKey(SelectionKey key) {
		// ((UserData)key.attachment()).clean();
		SocketChannel channel = (SocketChannel) key.channel();
		System.out.println(
				String.format(
						"finalize %s:%d",
						channel.socket().getInetAddress(),
						channel.socket().getPort()));
		System.out.println("Num keys: " + keySelector.keys().size());
		key.cancel();
		try {
			channel.close();
		} catch (IOException ex) {
		}
	}

	private Object[] getIPAdresses() {
	    try {
			List<String> list = new ArrayList<String>();
			Enumeration<NetworkInterface> net = null;
	        net = NetworkInterface.getNetworkInterfaces();
	        while(net.hasMoreElements()){
		        Enumeration<InetAddress> addresses = net.nextElement().getInetAddresses();
		        while (addresses.hasMoreElements()){
		            InetAddress inetAddress = addresses.nextElement();
		                if (inetAddress.isSiteLocalAddress()){
		                	list.add(inetAddress.getHostAddress());
		            }

		        }
		    }
	        return list.toArray();
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }

	    
	}
}
