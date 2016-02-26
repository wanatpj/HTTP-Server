package pawelwanat.net.common.http.contentproviders;

import java.util.Map;

import pawelwanat.net.common.http.Content;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CacheContentProvider implements ContentProvider {

	private Map<String, Content> cache;
	private final int cacheCapacity;
	private int cacheSize;
	
	@Inject
	public CacheContentProvider(@Named("CacheCapacity") int cacheCapacity){
		this.cacheCapacity = cacheCapacity;
		clear();
	}
	
	@Override
	public Content get(String contentLocation) {
		return cache.get(contentLocation);
	}

	@Override
	public boolean put(String contentLocation, Content newContent) {
		if(has(contentLocation)){
			cacheSize -= contentLocation.length() + get(contentLocation).length() + 16;
		}
		cacheSize += contentLocation.length() + newContent.length() + 16;
		if (cacheSize > cacheCapacity) {
			clear();
			cacheSize = contentLocation.length() + newContent.length() + 16;
		}
		cache.put(contentLocation, newContent);
		return true;
	}

	@Override
	public boolean has(String contentLocation) {
		return cache.containsKey(contentLocation);
	}
	
	private void clear(){
		cache = Maps.newHashMapWithExpectedSize(50);
		cacheSize = 0;
	}
}
