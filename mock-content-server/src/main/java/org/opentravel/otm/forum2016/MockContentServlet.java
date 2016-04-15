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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet used to process mock content provided from a remote Git repository.
 * 
 * @author S. Livezey
 */
public class MockContentServlet extends HttpServlet {
	
	private static final long serialVersionUID = 5871324880136706476L;
	private static final String refreshContentUri = "/admin/refreshContent";
	
	private static List<String> supportedContentTypes = Arrays.asList( "application/json", "application/xml", "text/plain" );
    private static final Logger log = LoggerFactory.getLogger( MockContentServlet.class );
	
	private File repositoryLocation = new File( MockServerConfig.getLocalRepositoryPath() );
	private MockContentProvider contentProvider = new MockContentProvider();
	
	/**
	 * Processes a request for mock content.
	 * 
	 * @param req  the HTTP servlet request
	 * @param resp  the HTTP servlet response
	 * @throws ServletException  thrown if the servlet request cannot be processed
	 * @throws IOException  thrown if the servlet response cannot be written to
	 */
	@SuppressWarnings("unchecked")
	private void processMockRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MockFolderLocation mockFolder = MockFolderLocation.find( req.getPathInfo(), repositoryLocation );
		List<String> contentTypes = getContentTypes( req );
		MockContentTemplate template = null;
		
		if (mockFolder != null) {
			for (String contentType : contentTypes) {
				String requestMethod = req.getMethod();
				
				if ((requestMethod == null) || requestMethod.equals("HEAD")) {
					requestMethod = "GET";
				}
				template = contentProvider.getNextTemplate( mockFolder.getFolderLocation(), requestMethod, contentType );
				if (template != null) break;
			}
		}
		
		if (template != null) {
			log.info("Processing mock response template: " + template.getCacheKey());
			
			if (template.isBlank()) {
				resp.setStatus( 204 );
				
			} else {
				Map<String,String> requestParams = new HashMap<>( mockFolder.getPathParameters() );
				Map<String,String[]> queryParams = req.getParameterMap();
				
				for (String paramName : queryParams.keySet()) {
					String[] paramValue = queryParams.get( paramName );
					
					if ((paramValue != null) && (paramValue.length > 0)) {
						requestParams.put( paramName, paramValue[0] );
					}
				}
				template.processMockContent( requestParams, resp.getWriter() );
				resp.setContentType( template.getContentType() );
				resp.setStatus( 200 );
			}
			
		} else {
			PrintWriter writer = resp.getWriter();
			
			resp.setStatus( 404 );
			resp.addHeader( "Content-Type", "text/plain" );
			writer.print( "The requested mock resource does not exist." );
			writer.flush();
		}
	}
	
	/**
	 * Returns the list of possible content types that may be used for the response payload.
	 * 
	 * @param req  the HTTP servlet request
	 * @return List<String>
	 */
	private List<String> getContentTypes(HttpServletRequest req) {
		List<String> contentTypes = new ArrayList<>();
		String requestedType = req.getHeader( "Accept" );
		
		if (requestedType != null) {
			String[] ctList = requestedType.split( "\\s*[,|;]\\s*" );
			
			for (String ct : ctList) {
				if (supportedContentTypes.contains( ct )) {
					contentTypes.add( ct );
					
				} else if ("*/*".equals( ct )) {
					contentTypes.addAll( supportedContentTypes );
					break;
				}
			}
			
		} else {
			contentTypes.addAll( supportedContentTypes );
		}
		return contentTypes;
	}
	
	/**
	 * Refreshes the contents of the local Git repository.  If the repository has
	 * not yet been cloned, this method will create a fresh cloned copy on the local
	 * file system.
	 * 
	 * @throws Exception  thrown if the local copy of the Git repository cannot be refreshed
	 */
	private void refreshGitRepository() throws Exception {
		try (GitRepositorySynchronizer synchronizer =
				new GitRepositorySynchronizer(
						MockServerConfig.getRemoteRepositoryUrl(), repositoryLocation )) {
			synchronizer.synchronizeContent();
		}
	}
	
	/**
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			refreshGitRepository();
			
		} catch (Exception e) {
			throw new ServletException("Error while initializing the local Git repository.", e);
		}
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getPathInfo().equals( refreshContentUri )) {
			try {
				refreshGitRepository();
				resp.setStatus( 204 );
				
			} catch (Throwable t) {
				PrintWriter writer = resp.getWriter();
				
				resp.setStatus( 500 );
				resp.addHeader( "Content-Type", "text/plain" );
				writer.print( "An error occurred while refreshing the mock content repository." );
				writer.flush();
			}
		} else {
			processMockRequest( req, resp );
		}
	}
	
	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getPathInfo().equals( refreshContentUri )) {
			try {
				refreshGitRepository();
				resp.setStatus( 204 );
				
			} catch (Throwable t) {
				PrintWriter writer = resp.getWriter();
				
				resp.setStatus( 500 );
				resp.addHeader( "Content-Type", "text/plain" );
				writer.print( "An error occurred while refreshing the mock content repository." );
				writer.flush();
			}
		} else {
			processMockRequest( req, resp );
		}
	}
	
	/**
	 * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processMockRequest( req, resp );
	}
	
	/**
	 * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processMockRequest( req, resp );
	}
	
	/**
	 * @see javax.servlet.http.HttpServlet#doHead(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processMockRequest( req, resp );
	}
	
}
