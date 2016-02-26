package pawelwanat.net.common.http.auth;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Provides session.
 * TODO: change deletion cookie policy
 */
public class SessionProvider {
	
	private SecureRandom secureRandom = new SecureRandom();

	private final int maxCookiesNumber;
	private Map<String, Map<String, Object>> sessions = Maps.newHashMap();
	private final int sessionCredentialsStrength;

	@Inject
	public SessionProvider(
			@Named("MaxCookiesNumber") int maxCookiesNumber,
			@Named("SessionCredentialsStrength") int sessionCredentialsStrength) {
		this.maxCookiesNumber = maxCookiesNumber;
		this.sessionCredentialsStrength = sessionCredentialsStrength;
	}
	
	public boolean sessionExists(String credential){
		return sessions.containsKey(credential);
	}
	
	/**
	 * Returns hex-string credential that identifies newly created session.
	 */
	public String createSession(){
		if(sessions.size() >= maxCookiesNumber){
			clear();
		}
		byte [] randomBytes = new byte[sessionCredentialsStrength];
		secureRandom.nextBytes(randomBytes);
		String result = new String(randomBytes, Charset.forName("UTF-8"));
		sessions.put(result, Maps.<String, Object>newHashMap());
		return result;
	}
	
	public void unregister(String credential){
		sessions.remove(credential);
	}
	
	public Object getVariable(String credential, String variableName){
		return sessions.get(credential).get(variableName);
	}
	
	public void setVariable(String credential, String variableName, Object variableValue){
		sessions.get(credential).put(variableName, variableValue);
	}

	private void clear() {
		sessions = Maps.newHashMap();
	}
	
	private String getHex(byte[] a) {
	    StringBuilder bob = new StringBuilder(a.length * 2);
		for(byte b : a) {
			bob.append(String.format("%02x", b & 0xff));
		}
		return bob.toString();
	}
}