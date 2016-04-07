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
public class MockServerConfig {
	
    public static final String CONFIGURATION_FILENAME = "ota2-mockserver.properties";
    public static final String CONFIGURATION_SYSPROP  = "org.opentravel.mockServer.config";
    
    private static Properties configProps;
    
    /**
     * Returns the URL location of the remote Git repository.
     * 
     * @return String
     */
    public static String getRemoteRepositoryUrl() {
    	return configProps.getProperty( "org.opentravel.mockServer.remoteRepositoryUrl" );
    }
    
    /**
     * Returns the file system location of the local Git repository.
     * 
     * @return String
     */
    public static String getLocalRepositoryPath() {
    	return configProps.getProperty( "org.opentravel.mockServer.localRepositoryPath" );
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

        if (System.getProperties().containsKey( "catalina.base" )) {
            configFile = new File( System.getProperty( "catalina.base" ), "/conf/" + CONFIGURATION_FILENAME );
            
            if (!configFile.exists()) {
                configFile = null;
            }
        }
        
        if ((configFile == null) && System.getProperties().containsKey( CONFIGURATION_SYSPROP )) {
            configFile = new File( System.getProperty( CONFIGURATION_SYSPROP ) );
            
            if (!configFile.exists()) {
                configFile = null;
            }
        }
        
        if (configFile != null) {
        	configStream = new FileInputStream( configFile );
        	
        } else {
            configStream = MockServerConfig.class.getResourceAsStream( "/config/" + CONFIGURATION_FILENAME );
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
