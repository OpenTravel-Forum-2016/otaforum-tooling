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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Details of an API that has been registered with the API Manager application.
 * 
 * @author S. Livezey
 */
public class APIDetails extends APISummary {
	
	private List<String> tiers = new ArrayList<>();
	private String thumbnailUrl;
	private APIVisibility visibility;
	private List<String> visibleRoles = new ArrayList<>();
	private List<String> visibleTenants = new ArrayList<>();
	private Integer cacheTimeout;
	private String productionEndpointType;
	private String productionEndpointUrl;
	private boolean destinationStatsEnabled;
	private String apiDefinition;
	private boolean responseCachingEnabled;
	private boolean defaultVersion;
	private String technicalOwner;
	private String technicalOwnerEmail;
	private String businessOwner;
	private String businessOwnerEmail;
	private List<String> transports = new ArrayList<>();
	private List<String> tags = new ArrayList<>();
	
	/**
	 * Default constructor.
	 */
	public APIDetails() {}
	
	/**
	 * Constructor that obtains all information from the API Manger JSON response.
	 * 
	 * @param jsonValues  the JSON field values for the API
	 */
	@SuppressWarnings("unchecked")
	public APIDetails(Map<String,Object> jsonValues) {
		super( jsonValues );
		Map<String,Object> jsonBizInfo = (Map<String,Object>) jsonValues.get( "businessInformation" );
		Double jsonCacheTimeout = (Double) jsonValues.get( "cacheTimeout" );
		String jsonEndpointConfig = (String) jsonValues.get( "endpointConfig" );
		String jsonVisibilty = (String) jsonValues.get( "visibility" );
		Boolean jsonDefaultVersion = (Boolean) jsonValues.get( "defaultVersion" );
		
		this.tiers = (List<String>) jsonValues.get( "tiers" );
		this.thumbnailUrl = (String) jsonValues.get( "thumbnailUrl" );
		
		if (jsonVisibilty != null) {
			this.visibility = APIVisibility.valueOf( jsonVisibilty );
		}
		this.visibleRoles = (List<String>) jsonValues.get( "visibleRoles" );
		this.visibleTenants = (List<String>) jsonValues.get( "visibleTenants" );
		if (jsonCacheTimeout != null) {
			this.cacheTimeout = jsonCacheTimeout.intValue();
		}
		if (jsonEndpointConfig != null) {
			JsonObject ecValue = new Gson().fromJson( jsonEndpointConfig, JsonObject.class );
			JsonObject jsonProdEndpoint = ecValue.getAsJsonObject( "production_endpoints" );
			
			this.productionEndpointUrl = jsonProdEndpoint.get( "url" ).getAsString();
			this.productionEndpointType = ecValue.get( "endpoint_type" ).getAsString();
		}
		this.destinationStatsEnabled = "Enabled".equals( (String) jsonValues.get( "destinationStatsEnabled" ) );
		this.apiDefinition = (String) jsonValues.get( "apiDefinition" );
		this.responseCachingEnabled = "Enabled".equals( (String) jsonValues.get( "responseCaching" ) );
		this.defaultVersion = (jsonDefaultVersion != null) && jsonDefaultVersion.booleanValue();
		
		if (jsonBizInfo != null) {
			this.technicalOwner = (String) jsonBizInfo.get( "technicalOwner" );
			this.technicalOwnerEmail = (String) jsonBizInfo.get( "technicalOwnerEmail" );
			this.businessOwner = (String) jsonBizInfo.get( "businessOwner" );
			this.businessOwnerEmail = (String) jsonBizInfo.get( "businessOwnerEmail" );
		}
		this.transports = (List<String>) jsonValues.get( "transport" );
		this.tags = (List<String>) jsonValues.get( "tags" );
	}
	
	/**
	 * Returns this instance as a JSON object.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject jsonEndpointConfig = new JsonObject();
		JsonObject jsonProdEndpoint = new JsonObject();
		JsonObject jsonBizInfo = new JsonObject();
		JsonObject json = super.toJson();
		
		json.add( "sequences", new JsonArray() );
		json.add( "tiers", toJsonArray( tiers ) );
		json.addProperty( "thumbnailUrl", thumbnailUrl );
		json.addProperty( "visibility", (visibility == null) ? null : visibility.toString() );
		json.add( "visibleRoles", toJsonArray( visibleRoles ) );
		json.add( "visibleTenants", toJsonArray( visibleTenants ) );
		json.addProperty( "cacheTimeout", cacheTimeout );
		jsonEndpointConfig.add( "production_endpoints", jsonProdEndpoint );
		jsonEndpointConfig.addProperty( "endpoint_type", productionEndpointType );
		jsonProdEndpoint.addProperty( "url", productionEndpointUrl );
		jsonProdEndpoint.add( "config", null );
		json.addProperty( "endpointConfig", jsonEndpointConfig.toString() );
		json.add( "subscriptionAvailability", null );
		json.add( "subscriptionAvailableTenants", new JsonArray() );
		json.addProperty( "destinationStatsEnabled", destinationStatsEnabled ? "Enabled" : "Disabled" );
		json.addProperty( "apiDefinition", apiDefinition );
		json.addProperty( "responseCaching", responseCachingEnabled ? "Enabled" : "Disabled" );
		json.addProperty( "isDefaultVersion", defaultVersion );
		json.addProperty( "gatewayEnvironments", "Production and Sandbox" );
		jsonBizInfo.addProperty( "technicalOwner", technicalOwner );
		jsonBizInfo.addProperty( "technicalOwnerEmail", technicalOwnerEmail );
		jsonBizInfo.addProperty( "businessOwner", businessOwner );
		jsonBizInfo.addProperty( "businessOwnerEmail", businessOwnerEmail );
		json.add( "businessInformation", jsonBizInfo );
		json.add( "transport", toJsonArray( transports ) );
		json.add( "tags", toJsonArray( tags ) );
		return json;
	}
	
	/**
	 * Returns the given list of strings as an array of JSON string values.
	 * 
	 * @param list  the list of strings to convert
	 * @return JsonArray
	 */
	private JsonArray toJsonArray(List<String> list) {
		JsonArray jsonArray = new JsonArray();
		
		for (String value : list) {
			jsonArray.add( value );
		}
		return jsonArray;
	}

	/**
	 * Returns the value of the 'tiers' field.
	 *
	 * @return List<String>
	 */
	public List<String> getTiers() {
		return tiers;
	}

	/**
	 * Returns the value of the 'thumbnailUrl' field.
	 *
	 * @return String
	 */
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	/**
	 * Assigns the value of the 'thumbnailUrl' field.
	 *
	 * @param thumbnailUrl  the field value to assign
	 */
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	/**
	 * Returns the value of the 'visibility' field.
	 *
	 * @return APIVisibility
	 */
	public APIVisibility getVisibility() {
		return visibility;
	}

	/**
	 * Assigns the value of the 'visibility' field.
	 *
	 * @param visibility  the field value to assign
	 */
	public void setVisibility(APIVisibility visibility) {
		this.visibility = visibility;
	}

	/**
	 * Returns the value of the 'visibleRoles' field.
	 *
	 * @return List<String>
	 */
	public List<String> getVisibleRoles() {
		return visibleRoles;
	}

	/**
	 * Returns the value of the 'visibleTenants' field.
	 *
	 * @return List<String>
	 */
	public List<String> getVisibleTenants() {
		return visibleTenants;
	}

	/**
	 * Returns the value of the 'cacheTimeout' field.
	 *
	 * @return Integer
	 */
	public Integer getCacheTimeout() {
		return cacheTimeout;
	}

	/**
	 * Assigns the value of the 'cacheTimeout' field.
	 *
	 * @param cacheTimeout  the field value to assign
	 */
	public void setCacheTimeout(Integer cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}

	/**
	 * Returns the value of the 'productionEndpointType' field.
	 *
	 * @return String
	 */
	public String getProductionEndpointType() {
		return productionEndpointType;
	}

	/**
	 * Assigns the value of the 'productionEndpointType' field.
	 *
	 * @param productionEndpointType  the field value to assign
	 */
	public void setProductionEndpointType(String productionEndpointType) {
		this.productionEndpointType = productionEndpointType;
	}

	/**
	 * Returns the value of the 'productionEndpointUrl' field.
	 *
	 * @return String
	 */
	public String getProductionEndpointUrl() {
		return productionEndpointUrl;
	}

	/**
	 * Assigns the value of the 'productionEndpointUrl' field.
	 *
	 * @param productionEndpointUrl  the field value to assign
	 */
	public void setProductionEndpointUrl(String productionEndpointUrl) {
		this.productionEndpointUrl = productionEndpointUrl;
	}

	/**
	 * Returns the value of the 'destinationStatsEnabled' field.
	 *
	 * @return boolean
	 */
	public boolean isDestinationStatsEnabled() {
		return destinationStatsEnabled;
	}

	/**
	 * Assigns the value of the 'destinationStatsEnabled' field.
	 *
	 * @param destinationStatsEnabled  the field value to assign
	 */
	public void setDestinationStatsEnabled(boolean destinationStatsEnabled) {
		this.destinationStatsEnabled = destinationStatsEnabled;
	}

	/**
	 * Returns the value of the 'apiDefinition' field.
	 *
	 * @return String
	 */
	public String getApiDefinition() {
		return apiDefinition;
	}

	/**
	 * Assigns the value of the 'apiDefinition' field.
	 *
	 * @param apiDefinition  the field value to assign
	 */
	public void setApiDefinition(String apiDefinition) {
		this.apiDefinition = apiDefinition;
	}

	/**
	 * Returns the value of the 'responseCachingEnabled' field.
	 *
	 * @return boolean
	 */
	public boolean isResponseCachingEnabled() {
		return responseCachingEnabled;
	}

	/**
	 * Assigns the value of the 'responseCachingEnabled' field.
	 *
	 * @param responseCachingEnabled  the field value to assign
	 */
	public void setResponseCachingEnabled(boolean responseCachingEnabled) {
		this.responseCachingEnabled = responseCachingEnabled;
	}

	/**
	 * Returns the value of the 'defaultVersion' field.
	 *
	 * @return boolean
	 */
	public boolean isDefaultVersion() {
		return defaultVersion;
	}

	/**
	 * Assigns the value of the 'defaultVersion' field.
	 *
	 * @param defaultVersion  the field value to assign
	 */
	public void setDefaultVersion(boolean defaultVersion) {
		this.defaultVersion = defaultVersion;
	}

	/**
	 * Returns the value of the 'technicalOwner' field.
	 *
	 * @return String
	 */
	public String getTechnicalOwner() {
		return technicalOwner;
	}

	/**
	 * Assigns the value of the 'technicalOwner' field.
	 *
	 * @param technicalOwner  the field value to assign
	 */
	public void setTechnicalOwner(String technicalOwner) {
		this.technicalOwner = technicalOwner;
	}

	/**
	 * Returns the value of the 'technicalOwnerEmail' field.
	 *
	 * @return String
	 */
	public String getTechnicalOwnerEmail() {
		return technicalOwnerEmail;
	}

	/**
	 * Assigns the value of the 'technicalOwnerEmail' field.
	 *
	 * @param technicalOwnerEmail  the field value to assign
	 */
	public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
		this.technicalOwnerEmail = technicalOwnerEmail;
	}

	/**
	 * Returns the value of the 'businessOwner' field.
	 *
	 * @return String
	 */
	public String getBusinessOwner() {
		return businessOwner;
	}

	/**
	 * Assigns the value of the 'businessOwner' field.
	 *
	 * @param businessOwner  the field value to assign
	 */
	public void setBusinessOwner(String businessOwner) {
		this.businessOwner = businessOwner;
	}

	/**
	 * Returns the value of the 'businessOwnerEmail' field.
	 *
	 * @return String
	 */
	public String getBusinessOwnerEmail() {
		return businessOwnerEmail;
	}

	/**
	 * Assigns the value of the 'businessOwnerEmail' field.
	 *
	 * @param businessOwnerEmail  the field value to assign
	 */
	public void setBusinessOwnerEmail(String businessOwnerEmail) {
		this.businessOwnerEmail = businessOwnerEmail;
	}

	/**
	 * Returns the value of the 'transports' field.
	 *
	 * @return List<String>
	 */
	public List<String> getTransports() {
		return transports;
	}

	/**
	 * Returns the value of the 'tags' field.
	 *
	 * @return List<String>
	 */
	public List<String> getTags() {
		return tags;
	}

}
