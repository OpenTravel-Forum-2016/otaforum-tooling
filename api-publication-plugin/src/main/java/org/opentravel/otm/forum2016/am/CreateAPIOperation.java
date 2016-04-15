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
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.opentravel.otm.forum2016.APIPublisherConfig;

import com.google.gson.Gson;

/**
 * Creates a new API on the API Manager application.
 * 
 * @author S. Livezey
 */
public class CreateAPIOperation extends RESTClientOperation<APIDetails> {
	
	private APIDetails api;
	
	/**
	 * Constructor that assigns the factory that created this operation.
	 * 
	 * @param factory  the factory that created this operation
	 */
	public CreateAPIOperation(APIOperationFactory factory) {
		super(factory);
	}
	
	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#execute()
	 */
	@Override
	public APIDetails execute() throws IOException {
		HttpPost request = new HttpPost( APIPublisherConfig.getWSO2PublisherApiBaseUrl() );
		
		request.setHeader( "Content-Type", "application/json" );
		request.setEntity( new StringEntity( new Gson().toJson( api.toJson() ) ) );
		return execute( request );
	}
	
	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#unmarshallResponse(org.apache.http.HttpResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected APIDetails unmarshallResponse(HttpResponse response) throws IOException {
		Map<String,Object> jsonApiDetails = gson.fromJson( readPayload( response ), Map.class );
		return new APIDetails( jsonApiDetails );
	}
	
	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#getSecurityScope()
	 */
	@Override
	protected String getSecurityScope() {
		return "apim:api_create";
	}
	
	/**
	 * Returns the API to be created.
	 *
	 * @return APIDetails
	 */
	public APIDetails getApi() {
		return api;
	}
	
	/**
	 * Assigns the API to be created.
	 *
	 * @param api  the API instance to assign
	 */
	public void setApi(APIDetails api) {
		this.api = api;
	}
	
}
