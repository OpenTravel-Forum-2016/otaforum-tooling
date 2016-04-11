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

package org.opentravel.otm.forum2016.am;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.opentravel.otm.forum2016.util.JsonUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Container for the meta-data and content of an API.
 * 
 * @author S. Livezey
 */
public class SwaggerDocument {
	
	private String apiName;
	private String apiVersion;
	private String description;
	private JsonObject content;
	
	/**
	 * Constructor that loads the Swagger document content from a file.
	 * 
	 * @param swaggerFile  the Swagger document to load
	 * @throws IOException  thrown if the Swagger document cannot be loaded
	 */
	public SwaggerDocument(File swaggerFile) throws IOException {
		try (Reader reader = new FileReader( swaggerFile )) {
			this.content = new Gson().fromJson( reader, JsonObject.class );
		}
		JsonObject swaggerInfo = content.getAsJsonObject( "info" );
		
		this.apiName = JsonUtils.getProperty( swaggerInfo, "title" );
		this.apiVersion = JsonUtils.getProperty( swaggerInfo, "version" );
		this.description = JsonUtils.getProperty( swaggerInfo, "description" );
	}

	/**
	 * Returns the name of the API.
	 *
	 * @return String
	 */
	public String getApiName() {
		return apiName;
	}

	/**
	 * Returns the version of the API.
	 *
	 * @return String
	 */
	public String getApiVersion() {
		return apiVersion;
	}

	/**
	 * Returns the API description.
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the JSON content of the Swagger document.
	 *
	 * @return JsonObject
	 */
	public JsonObject getContent() {
		return content;
	}
	
}
