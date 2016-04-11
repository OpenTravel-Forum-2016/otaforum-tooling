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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.Gson;

/**
 * Base class that provides common functions related to the invocation of remote
 * REST API's.
 *
 * @param <R>  the type of the API response that will be returned by the operation
 * @author S. Livezey
 */
public abstract class RESTClientOperation<R> {
	
	protected static Gson gson = new Gson();
	
	private APIOperationFactory factory;
	
	/**
	 * Constructor that assigns the factory that created this operation.
	 * 
	 * @param factory  the factory that created this operation
	 */
	protected RESTClientOperation(APIOperationFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * Executes the API Manager request and returns the result.
	 * 
	 * @return R
	 * @throws IOException  thrown if an error occurrs during execution of the request
	 */
	public abstract R execute() throws IOException;
	
	/**
	 * Unmarshalls the response payload stream to the Java return type for the operation.
	 * 
	 * @param response  the HTTP response to unmarshal
	 * @return R
	 * @throws IOException  thrown if an error occurrs during unmarshalling
	 */
	protected abstract R unmarshallResponse(HttpResponse response) throws IOException;
	
	/**
	 * Returns the API Manager security scope for this operation.
	 * 
	 * @return String
	 */
	protected abstract String getSecurityScope();
	
	/**
	 * Invokes the HTTP request using the OAuth2 credentials established by the factory.
	 * 
	 * @param request  the request to be executed
	 * @return R
	 * @throws IOException  thrown if an error occurrs during execution of the request
	 */
	protected R execute(HttpUriRequest request) throws IOException {
		OAuth2AccessToken token = factory.getAccessToken( getSecurityScope() );
		R responseObj = null;
		
		request.addHeader( "Authorization", token.getTokenType() + " " + token.getAccessToken());
		
		try (CloseableHttpClient client = newHttpClient()) {
			HttpResponse response = client.execute( request );
			int statusCode = response.getStatusLine().getStatusCode();
			
			if ((statusCode >= 200) && (statusCode <= 299)) {
				responseObj = unmarshallResponse( response );
				
			} else {
				throw new IOException("Service invocation error [" + statusCode + "]:" + readPayload( response ));
			}
			return responseObj;
		}
	}
	
	/**
	 * Returns a new HTTP client instance for use with API Manager REST API invocations.
	 * 
	 * @return CloseableHttpClient
	 * @throws IOException  thrown if an error occurs while constructing the HTTP client
	 */
	protected CloseableHttpClient newHttpClient() throws IOException {
		return APIOperationFactory.newHttpClient();
	}
	
	/**
	 * Returns the payload from the HTTP response as a string.  If no payload exists, this
	 * method will return null.
	 * 
	 * @param response  the HTTP response to process
	 * @return String
	 * @throws IOException  thrown if an error occurrs while reading the response's entity stream
	 */
	protected static String readPayload(HttpResponse response) throws IOException {
		return APIOperationFactory.readPayload( response );
	}
	
}
