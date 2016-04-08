/**
 * Copyright (C) 2016 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.otm.forum2016;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the retrieval of mock content messages from the local file system.
 * 
 * @author S. Livezey
 */
public class MockContentProvider {
	
	public static final String JSON_FILE_EXT    = ".json";
	public static final String XML_FILE_EXT     = ".xml";
	public static final String TEXT_FILE_EXT    = ".txt";
	public static final String DEFAULT_FILE_EXT = JSON_FILE_EXT;
	
	private Map<String,Map<String,List<File>>> fileTypeDirectoryCache = new HashMap<>();
	private LRUCache<MockContentTemplate> templateCache = new LRUCache<>();
	
	/**
	 * Returns a <code>MockContentTemplate</code> from the specified folder with the
	 * indicated content type.  If no matching files exist, this method will return
	 * null.
	 * 
	 * @param mockFolder  the mock folder from which to return a template
	 * @param requestMethod  the HTTP request method for which to return a template
	 * @param contentType  the content type of the template to return
	 * @return MockContentTemplate
	 * @throws IOException  thrown if the content of the template file cannot be loaded
	 */
	public synchronized MockContentTemplate getNextTemplate(File mockFolder, String requestMethod,
			String contentType) throws IOException {
		MockContentTemplate template = null;
		
		if (mockFolder != null) {
			List<File> templateFiles = getFolderContents( mockFolder, requestMethod, contentType );
			
			if ((templateFiles != null) && !templateFiles.isEmpty()) {
				File templateFile = templateFiles.remove( 0 );
				
				templateFiles.add( templateFile ); // move this file to the end of the list
				template = getTemplate( templateFile, contentType );
			}
		}
		return template;
	}
	
	/**
	 * Clears all cached data from the provider.
	 */
	public synchronized void clearCache() {
		fileTypeDirectoryCache.clear();
		templateCache.clear();
	}
	
	/**
	 * Returns the list of files from the specified mock folder that match the content
	 * type.
	 * 
	 * @param mockFolder  the mock folder from which to return a list of matching files
	 * @param requestMethod  the HTTP request method to which the list of files should be associated
	 * @param contentType  the content type of the files to return
	 * @return List<File>
	 * @throws IOException  thrown if the folder contents cannot be retrieved
	 */
	private List<File> getFolderContents(File mockFolder, String requestMethod, String contentType) throws IOException {
		String _contentType = (contentType == null) ? "application/json" : contentType.toLowerCase();
		Map<String,List<File>> directoryCache = fileTypeDirectoryCache.get( _contentType );
		String fileExtension = getFileExtension( _contentType, requestMethod ).toLowerCase();
		String folderPath = mockFolder.getCanonicalPath();
		List<File> folderContents = null;
		
		if (directoryCache == null) {
			directoryCache = new HashMap<>();
			fileTypeDirectoryCache.put( _contentType, directoryCache );
		}
		folderContents = directoryCache.get( folderPath );
		
		if (folderContents == null) {
			folderContents = new ArrayList<>();
			
			for (File folderMember : mockFolder.listFiles()) {
				if (folderMember.isFile() &&
						folderMember.getName().toLowerCase().endsWith( fileExtension )) {
					folderContents.add( folderMember );
				}
			}
			directoryCache.put( folderPath, folderContents );
		}
		return folderContents;
	}
	
	/**
	 * Returns a <code>MockContentTemplate</code> that is contstructed from the
	 * content of the given file.
	 * 
	 * @param templateFile  the file from which to initialize the template
	 * @param contentType  the MIME type of the template contents
	 * @return MockContentTemplate
	 * @throws IOException  thrown if the content of the template file cannot be loaded
	 */
	private MockContentTemplate getTemplate(File templateFile, String contentType) throws IOException {
		String cacheKey = templateFile.getCanonicalPath();
		MockContentTemplate template = templateCache.get( cacheKey );
		
		if (template == null) {
			template = new MockContentTemplate( templateFile, contentType );
			templateCache.add( template );
		}
		return template;
	}
	
	/**
	 * Returns the file extension associated with the given MIME type.
	 * 
	 * @param mimeType  the MIME type for which to return a file extension
	 * @param requestMethod  the HTTP request method that should be included with the file extension
	 * @return String
	 */
	private String getFileExtension(String mimeType, String requestMethod) {
		StringBuilder fileExt = new StringBuilder();
		
		if (requestMethod != null) {
			fileExt.append(".").append( requestMethod );
		}
		
		if (mimeType == null) {
			fileExt.append( DEFAULT_FILE_EXT );
			
		} else if (mimeType.equalsIgnoreCase("application/xml") || mimeType.equalsIgnoreCase("text/xml")) {
			fileExt.append( XML_FILE_EXT );
			
		} else if (mimeType.equalsIgnoreCase("application/json") || mimeType.equalsIgnoreCase("text/json")) {
			fileExt.append( JSON_FILE_EXT );
			
		} else if (mimeType.equalsIgnoreCase("text/plain")) {
			fileExt.append( TEXT_FILE_EXT );
			
		} else {
			fileExt.append( "???" ); // guarantee no file match for this MIME type
		}
		return fileExt.toString();
	}
	
}
