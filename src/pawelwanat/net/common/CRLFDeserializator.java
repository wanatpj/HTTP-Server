package pawelwanat.net.common;

public abstract class CRLFDeserializator<T> implements Deserializator<T> {

	boolean carriageReturnPresent = false;

	@Override
	public boolean process(byte b) {
		if (b == '\r') {
			carriageReturnPresent = true;
		} else if (carriageReturnPresent && b == '\n') {
			carriageReturnPresent = false;
			return true;
		} else {
			carriageReturnPresent = false;
		}
		return false;
	}
}
