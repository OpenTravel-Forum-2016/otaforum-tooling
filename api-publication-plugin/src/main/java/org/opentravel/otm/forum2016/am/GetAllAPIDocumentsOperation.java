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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.opentravel.otm.forum2016.APIPublisherConfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Retrieves the list of all documents associated with an API.
 * 
 * @author S. Livezey
 */
public class GetAllAPIDocumentsOperation extends RESTClientOperation<List<APIDocument>> {
	
	private String apiId;
	
	/**
	 * Constructor that assigns the factory that created this operation.
	 * 
	 * @param factory  the factory that created this operation
	 */
	public GetAllAPIDocumentsOperation(APIOperationFactory factory) {
		super(factory);
	}

	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#execute()
	 */
	@Override
	public List<APIDocument> execute() throws IOException {
		return execute( new HttpGet( APIPublisherConfig.getWSO2PublisherApiBaseUrl()
				+ "/" + apiId + "/documents?limit=1000" ) );
	}

	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#unmarshallResponse(org.apache.http.HttpResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected List<APIDocument> unmarshallResponse(HttpResponse response) throws IOException {
		JsonObject responseJson = gson.fromJson( readPayload( response ), JsonObject.class );
		JsonArray jsonApiList = responseJson.getAsJsonArray( "list" );
		List<APIDocument> docList = new ArrayList<>();
		
		for (JsonElement jsonApi : jsonApiList) {
			Map<String,Object> apiValues = gson.fromJson( jsonApi, Map.class );
			docList.add( new APIDocument( apiValues ) );
		}
		return docList;
	}

	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#getSecurityScope()
	 */
	@Override
	protected String getSecurityScope() {
		return "apim:api_view";
	}

	/**
	 * Returns the value of the 'apiId' field.
	 *
	 * @return String
	 */
	public String getApiId() {
		return apiId;
	}

	/**
	 * Assigns the value of the 'apiId' field.
	 *
	 * @param apiId  the field value to assign
	 */
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
	
}
