package pawelwanat.net.common.http.contentproviders;

import pawelwanat.net.common.http.Content;

public interface ContentProvider {

	Content get(String contentLocation);
	
	/**
	 * Overwrites old content.
	 * @return true if success.
	 */
	boolean put(String contentLocation, Content newContent);

	/**
	 * @return true if some content available under {@param contentLocation}
	 */
	boolean has(String contentLocation);
}
