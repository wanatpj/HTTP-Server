package pawelwanat.net.common.http.contentproviders;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

import pawelwanat.net.common.http.Content;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.name.Named;
/**
 * TODO: A lot of code is repeated. Rewrite it.
 * @author pawelwanat
 *
 */
public class FileContentProvider implements ContentProvider {
	
	private final String absolutePath;
	private final boolean canModifyOrCreateFiles;
	private final String defaultFileName;  // TODO: List

	@Inject
	public FileContentProvider(
			@Named("AbsolutePath") String absolutePath,
			@Named("CanModifyOrCreateFiles") boolean canModifyOrCreateFiles,
			@Named("DefauleFileName") String defaultFileName){
		this.absolutePath = absolutePath;
		this.canModifyOrCreateFiles = canModifyOrCreateFiles;
		this.defaultFileName = defaultFileName;
	}
	
	@Override
	public Content get(String contentLocation) {
		if (properRelative(contentLocation)) {
			contentLocation = getNormalized(contentLocation);
			if(contentLocation.endsWith("/")){
				contentLocation += defaultFileName;
			}
			File file;
			if(Files.isFile().apply(file = new File(contentLocation))){
				try {
					byte [] content = Files.asByteSource(file).read();
					return new Content(
							content,
							URLConnection.guessContentTypeFromName(contentLocation));
				} catch (IOException e) {
					// TODO: Add Logger
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public boolean put(String contentLocation, Content newContent) {
		if(canModifyOrCreateFiles && properRelative(contentLocation)){
			contentLocation = getNormalized(contentLocation);
			if(contentLocation.endsWith("/")){
				contentLocation += defaultFileName;
				File file = new File(contentLocation);
				if(!file.exists() || Files.isFile().apply(file)){
					try {
						Files.write(newContent.getProperContent(), file);
					} catch (IOException e) {
						// TODO: Add Logger
						return false;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean has(String contentLocation) {
		if (properRelative(contentLocation)) {
			contentLocation = getNormalized(contentLocation);
			if(contentLocation.endsWith("/")){
				contentLocation += defaultFileName;
			}
			if(Files.isFile().apply(new File(contentLocation))){
				return true;
			}
		}
		return false;
	}

	private boolean properRelative(String contentLocation) {
		return contentLocation.matches("\\A/([a-zA-Z_0-9-\\.]+/)*\\z")  // directory match
				|| contentLocation.matches("\\A(/[a-zA-Z_0-9-\\.]+)+\\z");  // file match
	}
	
	private String getNormalized(String path) {
		if (!path.endsWith("/") && Files.isDirectory().apply(new File(path))) {
			return path + "/";
		}
		return absolutePath + path;
	}
}
