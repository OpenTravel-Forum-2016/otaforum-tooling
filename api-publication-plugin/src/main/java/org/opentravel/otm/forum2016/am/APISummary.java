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
 * Summary of an API that has been registered with the API Manager application.
 * 
 * @author S. Livezey
 */
public class APISummary {
	
	private String id;
	private String name;
	private String version;
	private String context;
	private String provider;
	private String description;
	private String status;
	
	/**
	 * Default constructor.
	 */
	public APISummary() {}
	
	/**
	 * Constructor that obtains all information from the API Manger JSON response.
	 * 
	 * @param jsonValues  the JSON field values for the API
	 */
	public APISummary(Map<String,Object> jsonValues) {
		this.id = (String) jsonValues.get( "id" );
		this.name = (String) jsonValues.get( "name" );
		this.version = (String) jsonValues.get( "version" );
		this.context = (String) jsonValues.get( "context" );
		this.provider = (String) jsonValues.get( "provider" );
		this.description = (String) jsonValues.get( "description" );
		this.status = (String) jsonValues.get( "status" );
	}
	
	/**
	 * Returns this instance as a JSON object.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		if (id != null) {
			json.addProperty( "id", id );
		}
		if (name != null) {
			json.addProperty( "name", name );
		}
		if (version != null) {
			json.addProperty( "version", version );
		}
		if (context != null) {
			json.addProperty( "context", context );
		}
		if (provider != null) {
			json.addProperty( "provider", provider );
		}
		if (description != null) {
			json.addProperty( "description", description );
		}
		if (status != null) {
			json.addProperty( "status", status );
		}
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
	 * Returns the value of the 'version' field.
	 *
	 * @return String
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * Assigns the value of the 'version' field.
	 *
	 * @param version  the field value to assign
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * Returns the value of the 'context' field.
	 *
	 * @return String
	 */
	public String getContext() {
		return context;
	}
	
	/**
	 * Assigns the value of the 'context' field.
	 *
	 * @param context  the field value to assign
	 */
	public void setContext(String context) {
		this.context = context;
	}
	
	/**
	 * Returns the value of the 'provider' field.
	 *
	 * @return String
	 */
	public String getProvider() {
		return provider;
	}
	
	/**
	 * Assigns the value of the 'provider' field.
	 *
	 * @param provider  the field value to assign
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	/**
	 * Returns the value of the 'description' field.
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Assigns the value of the 'description' field.
	 *
	 * @param description  the field value to assign
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the value of the 'status' field.
	 *
	 * @return String
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Assigns the value of the 'status' field.
	 *
	 * @param status  the field value to assign
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
}
