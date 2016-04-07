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

package org.opentravel.git;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.opentravel.otm.forum2016.MockContentServlet;

/**
 * Launches an in-process Jetty server to handle live mock service requests.
 */
public class JettyTestServer {
	
	private static final int SERVER_PORT = 8080;
	
    private Server jettyServer;
    
    /**
     * Launches the Jetty server.
     * 
     * @throws Exception  thrown if the server cannot be started
     */
    public synchronized void start() throws Exception {
        if (jettyServer != null) {
            throw new IllegalStateException( "The Jetty server is already running." );
        }
        ServletContextHandler context = new ServletContextHandler( ServletContextHandler.SESSIONS );
		
		context.setContextPath( "/mock-content" );
        context.addServlet(new ServletHolder( new MockContentServlet() ), "/*" );
        
        jettyServer = new Server( SERVER_PORT );

        ErrorHandler errH = new ErrorHandler();
        errH.setShowStacks( true );

        jettyServer.setHandler( context );
        context.setErrorHandler( errH );
        jettyServer.start();
    }

    /**
     * Shuts down the Jetty server.
     * 
     * @throws Exception  thrown if the server cannot be shut down
     */
    public synchronized void stop() throws Exception {
        if (jettyServer == null) {
            throw new IllegalStateException( "The Jetty server is not running." );
        }
        jettyServer.stop();
        jettyServer = null;
    }
    
    /**
     * Returns the base URL to be used when accessing the Jetty server.
     * 
     * @return String
     */
    public String getBaseUrl() {
    	return "http://localhost:" + SERVER_PORT + "/jaxrs-otm-prototype/service";
    }
    
    /**
     * Main method that launches the Jetty test server from the command line.
     * 
     * @param args  the command-line arguments
     */
    public static void main(String[] args) {
    	try {
    		new JettyTestServer().start();
    		System.out.println("Jetty Server running on port " + SERVER_PORT);
    		
    		while (true) {
    			Thread.sleep( 100000L );
    		}
    		
    	} catch (Throwable t) {
    		t.printStackTrace( System.out );
    	}
    }
}
