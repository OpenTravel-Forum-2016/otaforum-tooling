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
import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.opentravel.otm.forum2016.APIPublisherConfig;

/**
 * Uploads the content of an API document.
 * 
 * @author S. Livezey
 */
public class UploadAPIDocumentOperation extends RESTClientOperation<APIDocument> {
	
	private String apiId;
	private String documentId;
	private File contentFile;
	private String contentType;
	
	/**
	 * Constructor that assigns the factory that created this operation.
	 * 
	 * @param factory  the factory that created this operation
	 */
	public UploadAPIDocumentOperation(APIOperationFactory factory) {
		super(factory);
	}

	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#execute()
	 */
	@Override
	public APIDocument execute() throws IOException {
		HttpPost request = new HttpPost( APIPublisherConfig.getWSO2PublisherApiBaseUrl()
				+ "/" + apiId + "/documents/" + documentId + "/content" );
		
		request.setEntity( MultipartEntityBuilder.create().addBinaryBody( "file", contentFile,
				ContentType.parse( contentType ), contentFile.getName() ).build() );
		return execute( request );
	}

	/**
	 * @see org.opentravel.otm.forum2016.am.RESTClientOperation#unmarshallResponse(org.apache.http.HttpResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected APIDocument unmarshallResponse(HttpResponse response) throws IOException {
		Map<String,Object> jsonDocDetails = gson.fromJson( readPayload( response ), Map.class );
		return new APIDocument( jsonDocDetails );
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

	/**
	 * Returns the value of the 'contentFile' field.
	 *
	 * @return File
	 */
	public File getContentFile() {
		return contentFile;
	}

	/**
	 * Assigns the value of the 'contentFile' field.
	 *
	 * @param contentFile  the field value to assign
	 */
	public void setContentFile(File contentFile) {
		this.contentFile = contentFile;
	}

	/**
	 * Returns the value of the 'contentType' field.
	 *
	 * @return String
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Assigns the value of the 'contentType' field.
	 *
	 * @param contentType  the field value to assign
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
