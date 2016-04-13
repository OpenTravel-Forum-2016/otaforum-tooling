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

import java.util.Map;

import com.google.gson.JsonObject;

/**
 * Describes a document that is associated with a published API.
 * 
 * @author S. Livezey
 */
public class APIDocument {
	
	private String id;
	private String name;
	private String summary;
	private String type;
	private String otherTypeName;
	private String visibility;
	private String sourceType;
	private String sourceUrl;
	
	/**
	 * Default constructor.
	 */
	public APIDocument() {}
	
	/**
	 * Constructor that obtains all information from the API Manger JSON response.
	 * 
	 * @param jsonValues  the JSON field values for the API
	 */
	public APIDocument(Map<String,Object> jsonValues) {
		this.id = (String) jsonValues.get( "documentId" );
		this.name = (String) jsonValues.get( "name" );
		this.summary = (String) jsonValues.get( "summary" );
		this.type = (String) jsonValues.get( "type" );
		this.otherTypeName = (String) jsonValues.get( "otherTypeName" );
		this.visibility = (String) jsonValues.get( "visibility" );
		this.sourceType = (String) jsonValues.get( "sourceType" );
		this.sourceUrl = (String) jsonValues.get( "sourceUrl" );
	}
	
	/**
	 * Returns this instance as a JSON object.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		json.addProperty( "documentId", id );
		json.addProperty( "name", name );
		json.addProperty( "summary", summary );
		json.addProperty( "type", type );
		json.addProperty( "otherTypeName", otherTypeName );
		json.addProperty( "visibility", visibility );
		json.addProperty( "sourceType", sourceType );
		json.addProperty( "sourceUrl", sourceUrl );
		return json;
	}

	/**
	 * Returns the value of the 'id' field.
	 *
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Assigns the value of the 'id' field.
	 *
	 * @param id  the field value to assign
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the value of the 'name' field.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Assigns the value of the 'name' field.
	 *
	 * @param name  the field value to assign
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the value of the 'summary' field.
	 *
	 * @return String
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * Assigns the value of the 'summary' field.
	 *
	 * @param summary  the field value to assign
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * Returns the value of the 'type' field.
	 *
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Assigns the value of the 'type' field.
	 *
	 * @param type  the field value to assign
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the value of the 'otherTypeName' field.
	 *
	 * @return String
	 */
	public String getOtherTypeName() {
		return otherTypeName;
	}

	/**
	 * Assigns the value of the 'otherTypeName' field.
	 *
	 * @param otherTypeName  the field value to assign
	 */
	public void setOtherTypeName(String otherTypeName) {
		this.otherTypeName = otherTypeName;
	}

	/**
	 * Returns the value of the 'visibility' field.
	 *
	 * @return String
	 */
	public String getVisibility() {
		return visibility;
	}

	/**
	 * Assigns the value of the 'visibility' field.
	 *
	 * @param visibility  the field value to assign
	 */
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	/**
	 * Returns the value of the 'sourceType' field.
	 *
	 * @return String
	 */
	public String getSourceType() {
		return sourceType;
	}

	/**
	 * Assigns the value of the 'sourceType' field.
	 *
	 * @param sourceType  the field value to assign
	 */
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * Returns the value of the 'sourceUrl' field.
	 *
	 * @return String
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}

	/**
	 * Assigns the value of the 'sourceUrl' field.
	 *
	 * @param sourceUrl  the field value to assign
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
}
