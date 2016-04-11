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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.opentravel.otm.forum2016.APIPublisherConfig;

/**
 * Modifies the lifecycle status of an API.
 * 
 * @author S. Livezey
 */
public class ChangeAPIStatusOperation extends RESTClientOperation<Boolean> {
	
	private String apiId;
	private APIStatusAction action;
	
	/**
	 * Constructor that assigns the factory that created this operation.
	 * 
	 * @param factory  the factory that created this operation
	 */
	public ChangeAPIStatusOperation(APIOperationFactory factory) {
		super(factory);
	}
	
	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#execute()
	 */
	@Override
	public Boolean execute() throws IOException {
		StringBuilder url = new StringBuilder( APIPublisherConfig.getWSO2PublisherUrl() );
		HttpPost request;
		
		url.append( "/change-lifecycle?apiId=" ).append( apiId );
		url.append( "&action=" ).append( action.getActionText() );
		request = new HttpPost( url.toString() );
		return execute( request );
	}
	
	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#unmarshallResponse(org.apache.http.HttpResponse)
	 */
	@Override
	protected Boolean unmarshallResponse(HttpResponse response) throws IOException {
		int statusCode = response.getStatusLine().getStatusCode();
		return (statusCode >= 200) && (statusCode <= 299);
	}
	
	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#getSecurityScope()
	 */
	@Override
	protected String getSecurityScope() {
		return "apim:api_publish";
	}

	/**
	 * Returns the ID of the API whose status is to be updated.
	 *
	 * @return String
	 */
	public String getApiId() {
		return apiId;
	}

	/**
	 * Assigns the ID of the API whose status is to be updated.
	 *
	 * @param apiId  the API ID to assign
	 */
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	/**
	 * Returns the API status action for the operation.
	 *
	 * @return APIStatusAction
	 */
	public APIStatusAction getAction() {
		return action;
	}

	/**
	 * Assigns the API status action for the operation.
	 *
	 * @param action  the status action value assign
	 */
	public void setAction(APIStatusAction action) {
		this.action = action;
	}
	
}
