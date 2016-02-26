package pawelwanat.net.echo;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import pawelwanat.net.common.JobExecutor;
import pawelwanat.net.common.UserData;

public class EchoJobExecutor implements JobExecutor<ByteBuffer, ByteBuffer, UserData>{

	@Override
	public ByteBuffer execute(ByteBuffer instance, UserData userData,
			SelectionKey selectedKey) {
		return instance;
	}

	@Override
	public boolean hasSuspendedJob(UserData userData, SelectionKey selectedKey) {
		return false;
	}

	@Override
	public void continueJob() {
		// do nothing
	}

	@Override
	public boolean toBeClosed(UserData userData, SelectionKey selectedKey) {
		return false;
	}

}
