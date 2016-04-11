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

import java.util.Properties;

import org.opentravel.otm.forum2016.APIPublisherConfig;

/**
 * Contains the configuration settings of an OAuth2 client application.
 * 
 * @author S. Livezey
 */
public class OAuth2ClientConfig {
	
	private static final OAuth2ClientConfig defaultInstance = new OAuth2ClientConfig();
	private static final String OAUTH_CONFIG_PREFIX = "org.opentravel.apiPublisher.oauth2.";
	
	private String tokenUrl;
	private String clientKey;
	private String clientSecret;
	private String grantType;
	private String userId;
	private String password;
	
	/**
	 * Returns the default singleton instance of the OAuth2 client configuration.
	 * 
	 * @return OAuth2ClientConfig
	 */
	public static OAuth2ClientConfig getInstance() {
		return defaultInstance;
	}
	
	/**
	 * Private constructor.
	 */
	private OAuth2ClientConfig() {
		Properties configProps = APIPublisherConfig.getConfigProperties();
		
		this.tokenUrl = configProps.getProperty( OAUTH_CONFIG_PREFIX + "tokenUrl" );
		this.clientKey = configProps.getProperty( OAUTH_CONFIG_PREFIX + "clientKey" );
		this.clientSecret = configProps.getProperty( OAUTH_CONFIG_PREFIX + "clientSecret" );
		this.grantType = configProps.getProperty( OAUTH_CONFIG_PREFIX + "grantType" );
		this.userId = configProps.getProperty( OAUTH_CONFIG_PREFIX + "userId" );
		this.password = configProps.getProperty( OAUTH_CONFIG_PREFIX + "password" );
	}
	
	/**
	 * Returns the value of the 'tokenUrl' field.
	 *
	 * @return String
	 */
	public String getTokenUrl() {
		return tokenUrl;
	}
	
	/**
	 * Returns the value of the 'clientKey' field.
	 *
	 * @return String
	 */
	public String getClientKey() {
		return clientKey;
	}
	
	/**
	 * Returns the value of the 'clientSecret' field.
	 *
	 * @return String
	 */
	public String getClientSecret() {
		return clientSecret;
	}
	
	/**
	 * Returns the value of the 'grantType' field.
	 *
	 * @return String
	 */
	public String getGrantType() {
		return grantType;
	}
	
	/**
	 * Returns the value of the 'userId' field.
	 *
	 * @return String
	 */
	public String getUserId() {
		return userId;
	}
	
	/**
	 * Returns the value of the 'password' field.
	 *
	 * @return String
	 */
	public String getPassword() {
		return password;
	}
	
}
