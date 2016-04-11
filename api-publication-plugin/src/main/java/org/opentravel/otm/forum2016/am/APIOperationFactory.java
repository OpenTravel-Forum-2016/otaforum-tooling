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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;

/**
 * Factory class used to create <code>APIManagerClientOperations</code>.  It also handles
 * the creation of OAuth2 access tokens.
 * 
 * @author S. Livezey
 */
public class APIOperationFactory {
	
	private Map<String,OAuth2AccessToken> tokenRegistry = new HashMap<>();
	
	/**
	 * Returns a new <code>GetAllAPIsOperation</code> instance.
	 * 
	 * @return GetAllAPIsOperation
	 */
	public GetAllAPIsOperation newGetAllAPIsOperation() {
		return new GetAllAPIsOperation( this );
	}
	
	/**
	 * Returns a new <code>GetAPIDetailsOperation</code> instance.
	 * 
	 * @return GetAPIDetailsOperation
	 */
	public GetAPIDetailsOperation newGetAPIDetailsOperation() {
		return new GetAPIDetailsOperation( this );
	}
	
	/**
	 * Returns a new <code>CreateAPIOperation</code> instance.
	 * 
	 * @return CreateAPIOperation
	 */
	public CreateAPIOperation newCreateAPIOperation() {
		return new CreateAPIOperation( this );
	}
	
	/**
	 * Returns a new <code>UpdateAPIOperation</code> instance.
	 * 
	 * @return UpdateAPIOperation
	 */
	public UpdateAPIOperation newUpdateAPIOperation() {
		return new UpdateAPIOperation( this );
	}
	
	/**
	 * Returns a new <code>ChangeAPIStatusOperation</code> instance.
	 * 
	 * @return ChangeAPIStatusOperation
	 */
	public ChangeAPIStatusOperation newChangeAPIStatusOperation() {
		return new ChangeAPIStatusOperation( this );
	}
	
	/**
	 * Returns a new <code>DeleteAPIOperation</code> instance.
	 * 
	 * @return DeleteAPIOperation
	 */
	public DeleteAPIOperation newDeleteAPIOperation() {
		return new DeleteAPIOperation( this );
	}
	
	/**
	 * Returns an API Manager authorization token for the specified scope.
	 * 
	 * @param scope  the security scope for the token to return
	 * @return OAuth2AccessToken
	 */
	protected synchronized OAuth2AccessToken getAccessToken(String scope) {
		OAuth2AccessToken token = tokenRegistry.get( scope );
		
		if (token == null) {
			OAuth2ClientConfig oauth2Config = OAuth2ClientConfig.getInstance();
			HttpPost tokenRequest = new HttpPost( oauth2Config.getTokenUrl() );
			StringBuilder postData = new StringBuilder()
					.append( "grant_type=" ).append( oauth2Config.getGrantType() )
					.append( "&username=" ).append( oauth2Config.getUserId() )
					.append( "&password=" ).append( oauth2Config.getPassword() )
					.append( "&scope=" ).append( scope );
			
			tokenRequest.addHeader( "Content-Type", "application/x-www-form-urlencoded" );
			tokenRequest.addHeader( "Authorization", "Basic " + Base64.encodeBase64String(
					(oauth2Config.getClientKey() + ":" + oauth2Config.getClientSecret()).getBytes() ) );
			tokenRequest.setEntity( new StringEntity( postData.toString(), Charset.defaultCharset() ) );
			
			try (CloseableHttpClient client = newHttpClient()) {
				HttpResponse response = client.execute( tokenRequest );
				String responsePayload = readPayload( response );
				
				token = new OAuth2AccessToken( responsePayload );
				tokenRegistry.put( scope, token );
				
			} catch (IOException e) {
				throw new RuntimeException("Error obtaining access token from the API managment server.", e);
			}
		}
		return token;
	}
	
	/**
	 * Returns a new HTTP client instance for use with API Manager REST API invocations.
	 * 
	 * @return CloseableHttpClient
	 * @throws IOException  thrown if an error occurs while constructing the HTTP client
	 */
	public static CloseableHttpClient newHttpClient() throws IOException {
		try {
			SSLContext sslContext = SSLContexts.custom()
			        .loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
			SSLConnectionSocketFactory connectionFactory =
		            new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
			
			return HttpClientBuilder.create().useSystemProperties()
					.setSSLSocketFactory(connectionFactory).build();
			
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new IOException("Error constructing SSL context for HTTP client.",  e);
		}

	}
	
	/**
	 * Returns the payload from the HTTP response as a string.  If no payload exists, this
	 * method will return null.
	 * 
	 * @param response  the HTTP response to process
	 * @return String
	 * @throws IOException  thrown if an error occurrs while reading the response's entity stream
	 */
	public static String readPayload(HttpResponse response) throws IOException {
		String payload = null;
		
		if (response.getEntity() != null) {
			try (InputStream rStream = response.getEntity().getContent()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int bytesRead;
				
				while ((bytesRead = rStream.read( buffer, 0, buffer.length )) >= 0) {
					out.write( buffer, 0, bytesRead );
				}
				payload = new String( out.toByteArray() );
			}
		}
		return payload;
	}
	
}
