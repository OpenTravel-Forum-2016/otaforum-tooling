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
import org.apache.http.client.methods.HttpGet;
import org.opentravel.otm.forum2016.APIPublisherConfig;

/**
 * Returns the details of an API.
 * 
 * @author S. Livezey
 */
public class GetAPIDetailsOperation extends RESTClientOperation<APIDetails> {
	
	private String id;
	
	/**
	 * Constructor that assigns the factory that created this operation.
	 * 
	 * @param factory  the factory that created this operation
	 */
	public GetAPIDetailsOperation(APIOperationFactory factory) {
		super(factory);
	}

	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#execute()
	 */
	@Override
	public APIDetails execute() throws IOException {
		return execute( new HttpGet( APIPublisherConfig.getWSO2PublisherApiBaseUrl() + "/" + id ) );
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
		return "apim:api_view";
	}

	/**
	 * Returns the ID of the API for which to retrieve details.
	 *
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Assigns the ID of the API for which to retrieve details.
	 *
	 * @param id  the ID of the API to retrieve
	 */
	public void setId(String id) {
		this.id = id;
	}
	
}
