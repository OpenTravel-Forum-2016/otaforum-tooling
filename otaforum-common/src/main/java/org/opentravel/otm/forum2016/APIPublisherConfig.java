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

package org.opentravel.otm.forum2016;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides server configuration settings for the <code>MockContentServlet</code>
 * application.
 * 
 * @author S. Livezey
 */
public class APIPublisherConfig {
	
    public static final String CONFIGURATION_FILENAME = "api-publisher.properties";
    public static final String CONFIGURATION_SYSPROP  = "org.opentravel.apiPublisher.config";
    
    private static Properties configProps;
    
    /**
     * Returns the base URL of the WSO2 publisher API's.
     * 
     * @return String
     */
    public static String getWSO2PublisherApiBaseUrl() {
    	return configProps.getProperty( "org.opentravel.apiPublisher.wso2PublisherAPI.baseUrl" );
    }
    
    /**
     * Returns the URL of the WSO2 API publisher application.
     * 
     * @return String
     */
    public static String getWSO2PublisherUrl() {
    	return configProps.getProperty( "org.opentravel.apiPublisher.wso2PublisherUrl" );
    }
    
    /**
     * Returns the URL of the WSO2 API store application.
     * 
     * @return String
     */
    public static String getWSO2StoreUrl() {
    	return configProps.getProperty( "org.opentravel.apiPublisher.wso2StoreUrl" );
    }
    
    /**
     * Returns the base URL endpoint location of the API gateway.
     * 
     * @return String
     */
    public static String getApiGatewayUrl() {
    	return configProps.getProperty( "org.opentravel.apiGateway.baseUrl" );
    }
    
    /**
     * Returns the base URL endpoint location of the mock server.
     * 
     * @return String
     */
    public static String getMockServerUrl() {
    	return configProps.getProperty( "org.opentravel.mockServer.baseUrl" );
    }
    
    /**
     * Returns the full list of configuration properties.
     * 
     * @return Properties
     */
    public static Properties getConfigProperties() {
    	return configProps;
    }
    
    /**
     * Returns an input stream that can be used to access the contents of the
     * configuration file.
     * 
     * @return InputStream
     */
    private static InputStream findConfiguration() throws IOException {
    	InputStream configStream;
        File configFile = null;
        
        // If the system property is specified, it is the first choice in selecting
        // the configuration file location.
        if ((configFile == null) && System.getProperties().containsKey( CONFIGURATION_SYSPROP )) {
            configFile = new File( System.getProperty( CONFIGURATION_SYSPROP ) );
            
            if (!configFile.exists()) {
                configFile = null;
            }
        }
        
        // If the system property was not successful, look in the /.config directory of
        // the root project folder.
        if (configFile == null) {
        	configFile = new File( System.getProperty( "user.dir" ), "/.config/" + CONFIGURATION_FILENAME );
            
            if (!configFile.exists()) {
                configFile = null;
            }
        }
        
        if (configFile != null) {
        	configStream = new FileInputStream( configFile );
        	
        } else {
        	// If all else failed, look in the /config package of the JVM classpath;
        	// typically used for testing.
            configStream = APIPublisherConfig.class.getResourceAsStream( "/config/" + CONFIGURATION_FILENAME );
        }
        return configStream;
    }
    
    /**
     * Static initializer that loads the configuration properties for the application.
     */
    static {
    	try {
    		InputStream configStream = findConfiguration();
    		
    		if (configStream == null) {
    			throw new Error("Mock server configuration properties not found.");
    		}
    		configProps = new Properties();
    		configProps.load( configStream );
    		
    	} catch (Throwable t) {
    		throw new ExceptionInInitializerError( t );
    	}
    }
    
}
