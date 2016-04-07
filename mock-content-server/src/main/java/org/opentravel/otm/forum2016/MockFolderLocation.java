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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns a mock folder location that is based on a relative URI path.  Any path
 * components that resolve to substitutable path variables will included in the path
 * parameter map.
 * 
 * @author S. Livezey
 */
public class MockFolderLocation {
	
	private File folderLocation;
	private Map<String,String> pathParameters;
	
	/**
	 * Private constructor
	 */
	private MockFolderLocation(File folderLocation, Map<String,String> pathParameters) {
		this.folderLocation = folderLocation;
		this.pathParameters = pathParameters;
	}
	
	/**
	 * Returns a mock folder location that is based upon the contents of the relative
	 * URI path provided.  If a folder location cannot be resolved, this method will
	 * return null.
	 * 
	 * @param uriPath  the relative URI path
	 * @param rootFolder  the root folder location for the mock content repository
	 * @return MockFolderLocation
	 */
	public static MockFolderLocation find(String uriPath, File rootFolder) {
		MockFolderLocation mockFolder = null;
		
		if (uriPath != null) {
			List<String> pathParts = new ArrayList<>();
			
			for (String pathPart : uriPath.split("/")) {
				if ((pathPart = pathPart.trim()).length() > 0) {
					pathParts.add( pathPart );
				}
			}
			if (pathParts.size() > 0) {
				mockFolder = _find( pathParts.toArray( new String[ pathParts.size() ]),
						rootFolder, new HashMap<String,String>() );
			}
		}
		return mockFolder;
	}
	
	/**
	 * Recursive search that attempts to map a folder location on the local file system
	 * to the path parts derived from the relative URI path.
	 * 
	 * @param pathParts  the parts of the relative URI path to traverse
	 * @param folder  the current folder location being searched
	 * @param pathParams  the path parameters collected so far during the search
	 * @return MockFolderLocation
	 */
	private static MockFolderLocation _find(String[] pathParts, File folder, Map<String,String> pathParams) {
		MockFolderLocation result = null;
		
		if (pathParts.length == 0) {
			result = new MockFolderLocation( folder, new HashMap<>( pathParams ) );
			
		} else {
			String firstPart = pathParts[0];
			String[] remainingParts = Arrays.copyOfRange( pathParts, 1, pathParts.length );
			
			for (File subFolder : getSubFolders( folder )) {
				String folderName = subFolder.getName();
				
				if (isWildcardFolder( folderName )) {
					// This is a wildcard sub-folder
					String paramName = folderName.substring( 1, folderName.length() - 1 );
					
					pathParams.put( paramName, firstPart );
					result = _find( remainingParts, subFolder, pathParams );
					pathParams.remove( paramName );
					
				} else if (firstPart.equalsIgnoreCase( folderName )) {
					result = _find( remainingParts, subFolder, pathParams );
				}
				if (result != null) break;
			}
		}
		return result;
	}
	
	/**
	 * Returns the list of sub-folders.  The order of the resulting list is such that
	 * "wildcard" folders (that start and end with a '_' underscore) are sorted to the
	 * end of the list.
	 * 
	 * @param folder  the folder for which to return sub-folders
	 * @return List<File>
	 */
	private static List<File> getSubFolders(File folder) {
		List<File> literalFolders = new ArrayList<>();
		List<File> wildcardFolders = new ArrayList<>();
		
		for (File subFolder : folder.listFiles()) {
			if (!subFolder.isDirectory()) continue;
			
			if (isWildcardFolder( subFolder.getName() )) {
				wildcardFolders.add( subFolder );
			} else {
				literalFolders.add( subFolder );
			}
		}
		literalFolders.addAll( wildcardFolders );
		return literalFolders;
	}
	
	/**
	 * Returns true if the given folder name represents a "wildcard" folder (that starts
	 * and ends with a '_' underscore).
	 * 
	 * @param folderName  the folder name to analyze
	 * @return boolean
	 */
	private static boolean isWildcardFolder(String folderName) {
		return (folderName != null) && folderName.startsWith("_")
				&& folderName.endsWith("_") && (folderName.length() > 2);
	}
	
	/**
	 * Returns the folder location that was resolved from the relative URI path.  The
	 * folder that is returned is guaranteed to exist on the local file system.
	 *
	 * @return File
	 */
	public File getFolderLocation() {
		return folderLocation;
	}
	
	/**
	 * Returns the name/value pairs that were identified as path parameters in the
	 * original URI path.
	 *
	 * @return Map<String,String>
	 */
	public Map<String, String> getPathParameters() {
		return Collections.unmodifiableMap( pathParameters );
	}
	
}
