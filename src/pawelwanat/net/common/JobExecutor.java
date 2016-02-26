package pawelwanat.net.common;

import java.nio.channels.SelectionKey;

public interface JobExecutor<INPUT, OUTPUT, USR extends UserData> {
	/**
	 * TODO: change Exception to e.g. ServerException in order to distinguish between predicted
	 * and an unpredicted exception.
	 * */
	OUTPUT execute(INPUT instance, USR userData, SelectionKey selectedKey) throws ServerException;

	/**
	 * No new job should be executed, while there exists job, which is suspended.
	 * @return true if there is suspended job to do for certain connection.
	 */
	boolean hasSuspendedJob(USR userData, SelectionKey selectedKey);
	
	/**
	 * Continues suspended job.
	 */
	void continueJob();
	
	/**
	 * @return true if related channel should be closed after all data is sent to client.
	 */
	boolean toBeClosed(USR userData, SelectionKey selectedKey);
}
