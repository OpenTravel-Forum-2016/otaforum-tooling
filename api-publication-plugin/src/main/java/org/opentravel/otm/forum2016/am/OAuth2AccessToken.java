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

import com.google.gson.Gson;

/**
 * Maintains the contents of an OAuth2 access token.
 */
public class OAuth2AccessToken {
	
	private String accessToken;
	private String tokenType;
	private String scope;
	private String refreshToken;
	private long expirationTimestamp;
	
	/**
	 * Constructor that initializes the access token from the token server's response.
	 * 
	 * @param tokenResponse  the OAuth2 token response in JSON format
	 */
	@SuppressWarnings("unchecked")
	public OAuth2AccessToken(String tokenResponse) {
		Map<String,Object> responseProps = new Gson().fromJson( tokenResponse, Map.class );
		Double expiresIn = (Double) responseProps.get( "expires_in" );
		
		this.accessToken = (String) responseProps.get( "access_token" );
		this.tokenType = (String) responseProps.get( "token_type" );
		this.scope = (String) responseProps.get( "scope" );
		this.refreshToken = (String) responseProps.get( "refresh_token" );
		
		if (expiresIn != null) {
			this.expirationTimestamp = System.currentTimeMillis() + (expiresIn.longValue() * 1000L);
		}
	}
	
	/**
	 * Returns the value of the 'accessToken' field.
	 *
	 * @return String
	 */
	public String getAccessToken() {
		return accessToken;
	}
	
	/**
	 * Returns the value of the 'tokenType' field.
	 *
	 * @return String
	 */
	public String getTokenType() {
		return tokenType;
	}
	
	/**
	 * Returns the value of the 'scope' field.
	 *
	 * @return String
	 */
	public String getScope() {
		return scope;
	}
	
	/**
	 * Returns the value of the 'refreshToken' field.
	 *
	 * @return String
	 */
	public String getRefreshToken() {
		return refreshToken;
	}
	
	/**
	 * Returns the value of the 'expirationTimestamp' field.
	 *
	 * @return long
	 */
	public long getExpirationTimestamp() {
		return expirationTimestamp;
	}
	
}
