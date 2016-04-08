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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cacheable template for a single mock content file.
 * 
 * @author S. Livezey
 */
public class MockContentTemplate implements Cacheable {
	
	private static Pattern paramPattern = Pattern.compile( "\\{(.*?)\\}" );
	
	private String filePath;
	private String template;
	private String contentType;
	
	/**
	 * Constructor that initializes the template from the contents of the specified file.
	 * 
	 * @param templateFile  the file system location of the mock content template
	 * @param contentType  the MIME type of the template contents
	 * @throws IOException  thrown if the content of the template file cannot be loaded
	 */
	public MockContentTemplate(File templateFile, String contentType) throws IOException {
		try (Reader fileReader = new FileReader( templateFile )) {
			StringWriter writer = new StringWriter( (int) templateFile.length() );
			char[] buffer = new char[1024];
			int charsRead;
			
			while ((charsRead = fileReader.read( buffer, 0, buffer.length)) >= 0) {
				writer.write( buffer, 0, charsRead );
			}
			this.filePath = templateFile.getCanonicalPath();
			this.template = writer.toString();
			this.contentType = contentType;
		}
	}
	
	/**
	 * Returns true if the template for this response is blank, indicating an empty
	 * response payload.
	 * 
	 * @return boolean
	 */
	public boolean isBlank() {
		return (template == null) || (template.length() == 0);
	}
	
	/**
	 * Processes the given template by substituting any parameter occurrances.  Output
	 * is directed to the writer provided.
	 * 
	 * @param template  the template string to be processed
	 * @param parameters  the name/value pairs to use for parameter substitution
	 * @param writer  the writer to which processed output will be directed
	 * @throws IOException  thrown if an error occurs during template processing
	 */
	public void processMockContent(Map<String,String> parameters, Writer writer) throws IOException {
		Matcher m = paramPattern.matcher( template );
		int lastMatch = 0;
		
		while (m.find()) {
			String paramValue = parameters.get( m.group( 1 ) );
			
			writer.write( template.substring( lastMatch, m.start() ) );
			if (paramValue != null) writer.write( paramValue );
			lastMatch = m.end();
		}
		writer.write( template.substring( lastMatch ) );
	}

	/**
	 * Returns the MIME type of the template contents.
	 *
	 * @return String
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @see org.opentravel.otm.forum2016.Cacheable#getCacheKey()
	 */
	@Override
	public String getCacheKey() {
		return filePath;
	}
	
}
