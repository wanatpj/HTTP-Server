package pawelwanat.net.common;

/**
 * Predicted exception.
 */
@SuppressWarnings("serial")
public class ServerException extends Exception {

	public ServerException() {
		super();
	}
	
	public ServerException(String message) {
		super(message);
	}
	
	public ServerException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ServerException(Throwable cause) {
		super(cause);
	}
}
