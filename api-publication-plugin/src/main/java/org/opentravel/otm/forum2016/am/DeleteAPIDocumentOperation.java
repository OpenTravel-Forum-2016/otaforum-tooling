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
import org.apache.http.client.methods.HttpDelete;
import org.opentravel.otm.forum2016.APIPublisherConfig;

/**
 * Deletes an API document from the API Manager application.
 * 
 * @author S. Livezey
 */
public class DeleteAPIDocumentOperation extends RESTClientOperation<Boolean> {
	
	private String apiId;
	private String documentId;
	
	/**
	 * Constructor that assigns the factory that created this operation.
	 * 
	 * @param factory  the factory that created this operation
	 */
	public DeleteAPIDocumentOperation(APIOperationFactory factory) {
		super(factory);
	}

	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#execute()
	 */
	@Override
	public Boolean execute() throws IOException {
		return execute( new HttpDelete( APIPublisherConfig.getWSO2PublisherUrl() + "/" + apiId + "/documents/" + documentId ) );
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
		return "apim:api_create";
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

	/**
	 * Returns the value of the 'documentId' field.
	 *
	 * @return String
	 */
	public String getDocumentId() {
		return documentId;
	}

	/**
	 * Assigns the value of the 'documentId' field.
	 *
	 * @param documentId  the field value to assign
	 */
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

}
