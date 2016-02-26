package pawelwanat.net.common;

import com.google.inject.Inject;

public class ConnectionInformation<S, T, USR extends UserData> {

    private final ByteProcessor<S, T> byteProcessor;
	private USR userData;

	@Inject
    public ConnectionInformation(
    		ByteProcessor<S, T> byteProcessor,
    		USR userData) {
        this.byteProcessor = byteProcessor;
        this.userData = userData;
    }
    
    ByteProcessor<S, T> getByteProcessor(){
    	return byteProcessor;
    }

	public USR getUserData() {
		return userData;
	}
}
