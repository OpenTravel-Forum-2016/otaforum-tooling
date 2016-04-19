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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.codegen.example.ExampleJsonBuilder;
import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates Swagger API specifications from an OTM model and creates the
 * necessary paths in the local mock context directory.  This job also creates
 * sample mock responses and <code>ReadMe.html</code> files for each API operation
 * defined in the OTM model(s).
 * 
 * @author S. Livezey
 */
public class UpdateContextJob {
	
    private static final String TEMPLATE_LOCATION = "/templates/";
    private static final Logger log = LoggerFactory.getLogger( UpdateContextJob.class );
    
    private static VelocityEngine velocityEngine;
    
	private File projectFolder;
	private String context;
	
	/**
	 * Constructor that assigns the name of the context folder in the current user
	 * directory.
	 * 
	 * @param projectFolder  the root directory of the mock content workspace
	 * @param context  the name of the context folder to update
	 */
	public UpdateContextJob(File projectFolder, String context) {
		this.projectFolder = projectFolder;
		this.context = context;
	}
	
	/**
	 * Executes the job to perform the context folder updates.
	 * 
	 * @throws UpdateContextException  thrown if an error occurs during job execution
	 */
	public void execute() throws UpdateContextException {
		File contextFolder = new File( projectFolder, "/" + context );
		List<File> otpFiles = new ArrayList<>();
		
		if (context == null) {
			throw new UpdateContextException("Context folder not specified.");
		}
		if (!contextFolder.exists()) {
			throw new UpdateContextException("Context folder not found: /" + context);
		}
		
		OTMProjectUtils.findOTMProjects( contextFolder, otpFiles );
		
		if (!otpFiles.isEmpty()) {
			log.info("Processing OTM Models for Context: " + context);
			
			for (File otpFile : otpFiles) {
				try {
					log.info("  Loading OTM Project: " + otpFile.getName());
					TLModel model = OTMProjectUtils.loadModel( otpFile );
					
					if (model != null) {
						Map<String,ActionGroup> actionGroups = new HashMap<>();
						List<TLResource> resources = getAllResources( model );
						
						for (TLResource resource : resources) {
							log.info("    Creating workspace artifacts for: " + resource.getName()
									+ " v" + resource.getVersion());
							prepareContextArtifacts( resource, contextFolder, actionGroups );
						}
						
						for (ActionGroup group : actionGroups.values()) {
							generateReadmeFile( group );
						}
					}
					
				} catch (SchemaCompilerException e) {
					log.error("  Unknown error while generating OTM Project", e);
					
				} catch (Exception e) {
					log.error("  Unexpected error while preparing context workspace", e);
				}
			}
		}
	}
	
	/**
	 * Prepares all necessary context workspace files for the resources defined in the OTM model.
	 * 
	 * @param resource  the OTM for which to create workspace artifacts
	 * @param contextFolder  the folder within the mock content workspace that is being processed
	 * @param actionGroups  the registry of actions that all resolve to the same mock content folder
	 */
	private void prepareContextArtifacts(TLResource resource, File contextFolder, Map<String,ActionGroup> actionGroups) {
		for (QualifiedAction action : ResourceCodegenUtils.getQualifiedActions( resource )) {
			File actionFolder = getActionFolder( resource, action, contextFolder );
			TLActionResponse response = getSuccessResponse( action.getAction() );
			TLHttpMethod method = action.getAction().getRequest().getHttpMethod();
			ActionGroup group = actionGroups.get( actionFolder.getAbsolutePath() );
			
			if (response.getPayloadType() != null) {
				for (TLMimeType mimeType : response.getMimeTypes()) {
					generateMockResponse( resource, response, method, mimeType, actionFolder );
				}
			}
			if (group == null) {
				group = new ActionGroup( actionFolder, resource );
				actionGroups.put( actionFolder.getAbsolutePath(), group );
			}
			group.getActions().add( action.getAction() );
		}
	}
	
	/**
	 * Returns the mock folder location for the given actions's path template.
	 * 
	 * @param resource  the resource that declared or inherited the actions
	 * @param actions  the resource actions for which to return a folder location
	 * @param File contextFolder  the root folder of the mock content context
	 * @return File
	 */
	private File getActionFolder(TLResource resource, QualifiedAction action, File contextFolder) {
		TLResource owningResource = action.getAction().getOwner();
		StringBuilder folderPath = new StringBuilder();
		File actionFolder;
		
		folderPath.append("/").append( owningResource.getName() );
		folderPath.append("/").append( owningResource.getVersion() );
		
		for (String pathPart : action.getPathTemplate().split("/")) {
			if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
				String wildcardName = "_" + pathPart.substring( 1, pathPart.length() - 1 ) + "_";
				folderPath.append("/").append( wildcardName );
			} else {
				folderPath.append("/").append( pathPart );
			}
		}
		actionFolder = new File( contextFolder, folderPath.toString() );
		
		if (!actionFolder.exists()) {
			actionFolder.mkdirs();
		}
		return actionFolder;
	}
	
	/**
	 * Generates a brief readme file in the actions folder that links to the online documentation.
	 * 
	 * @param group  the group of all actions that resolve to the same mock content folder
	 */
	private void generateReadmeFile(ActionGroup group) {
		File readmeFile = new File( group.getActionFolder(), "/readme.html" );
		try (Writer writer = new FileWriter( readmeFile )) {
			Template template = velocityEngine.getTemplate( TEMPLATE_LOCATION + "api-readme.vm", "UTF-8" );
			VelocityContext context = new VelocityContext();
			
			context.put( "context", this.context );
			context.put( "resource", group.getResource() );
			context.put( "actionList", group.getActions() );
			context.put( "apiStoreUrl", APIPublisherConfig.getWSO2StoreUrl() );
			context.put( "apiPublisherUrl", APIPublisherConfig.getWSO2PublisherUrl() );
			context.put( "apiGatewayUrl", APIPublisherConfig.getApiGatewayUrl() );
			context.put( "mockServerUrl", APIPublisherConfig.getMockServerUrl() );
			
			template.merge( context, writer );
			
		} catch (Exception e) {
			log.error("Error generating readme.html file", e);
		}
	}
	
	/**
	 * Generates a mock response file based on the default example output for the OTM
	 * compiler.  If an error occurs during file generation, a warning will be logged
	 * and this method will return without re-throwing the exception.
	 * 
	 * @param resource  the resource that declared or inherited the actions
	 * @param response  the actions response for which the mock is to be generated
	 * @param method  the HTTP method of the actions request
	 * @param mimeType  the content type of the mock response file to create
	 * @param targetFolder  the folder where the mock response file should be created
	 * @throws IOException  thrown if an error occurs while generating the mock response file
	 */
	private void generateMockResponse(TLResource resource, TLActionResponse response, TLHttpMethod method,
			TLMimeType mimeType, File targetFolder) {
		boolean jsonFormat = (mimeType == TLMimeType.APPLICATION_JSON) || (mimeType == TLMimeType.TEXT_JSON);
		String filename = resource.getName() + "-01." + method.toString() + (jsonFormat ? ".json" : ".xml");
		File mockFile = new File( targetFolder, filename );
		
		if (!mockFile.exists()) {
			try (Writer out = new FileWriter( mockFile )) {
				NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( response.getPayloadType() );
				String fileContent;
				
				if (jsonFormat) {
					fileContent = new ExampleJsonBuilder( new ExampleGeneratorOptions() )
							.setModelElement( payloadType ).buildString();
					
				} else { // XML output
					fileContent = new ExampleDocumentBuilder( new ExampleGeneratorOptions() )
							.setModelElement( payloadType ).buildString();
				}
				out.write( fileContent );
				out.flush();
				
			} catch (ValidationException | CodeGenerationException | IOException e) {
				log.warn("Error generating mock response file: " + filename, e);
			}
		}
	}
	
	/**
	 * Returns a list of all resources from the given OTM model.
	 * 
	 * @param model  the OTM model containing the resources to be returned
	 * @return List<TLResource>
	 */
	private List<TLResource> getAllResources(TLModel model) {
		List<TLResource> resources = new ArrayList<>();
		
		for (TLLibrary library : model.getUserDefinedLibraries()) {
			resources.addAll( library.getResourceTypes() );
		}
		return resources;
	}
	
	/**
	 * Returns the actions response for a 200 (success) status.
	 * 
	 * @param actions  the actions for which to return the success response
	 * @return
	 */
	private TLActionResponse getSuccessResponse(TLAction action) {
		TLActionResponse firstChoice = null;
		TLActionResponse secondChoice = null;
		
		for (TLActionResponse r : ResourceCodegenUtils.getInheritedResponses( action )) {
			for (Integer status : r.getStatusCodes()) {
				if (status == 200) {
					firstChoice = r;
				} else if ((status >= 200) && (status <= 299)){
					secondChoice = r;
				}
				
			}
		}
		return (firstChoice != null) ? firstChoice : secondChoice;
	}
	
	/**
	 * Returns the root directory location that contains the context folder.
	 *
	 * @return File
	 */
	public File getProjectFolder() {
		return projectFolder;
	}
	
	/**
	 * Assigns the root directory location that contains the context folder.
	 *
	 * @param projectFolder  the folder location to assign
	 */
	public void setProjectFolder(File projectFolder) {
		this.projectFolder = projectFolder;
	}
	
	/**
	 * Returns the name of the context folder to update.
	 *
	 * @return String
	 */
	public String getContext() {
		return context;
	}
	
	/**
	 * Assigns the name of the context folder to update.
	 *
	 * @param context  the context name to assign
	 */
	public void setContext(String context) {
		this.context = context;
	}
	
	/**
	 * Initializes the Velocity template processing engine.
	 */
	static {
		try {
			VelocityEngine ve = new VelocityEngine();
			
			ve.setProperty( RuntimeConstants.RESOURCE_LOADER, "classpath" );
			ve.setProperty( "classpath.resource.loader.class", ClasspathResourceLoader.class.getName() );
			ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
			velocityEngine = ve;
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
	/**
	 * Groups the actions together that resolve to the same mock content folder.
	 */
	private static class ActionGroup {
		
		private File actionFolder;
		private TLResource resource;
		private List<TLAction> actions = new ArrayList<>();
		
		/**
		 * Constructor that specifies the owning resource for all actions in this group.
		 * 
		 * @param actionFolder  the folder location where all actions resolve to
		 * @param resource  the owning resource for all actions in this group
		 */
		public ActionGroup(File actionFolder, TLResource resource) {
			this.actionFolder = actionFolder;
			this.resource = resource;
		}

		/**
		 * Returns the value of the 'actionFolder' field.
		 *
		 * @return File
		 */
		public File getActionFolder() {
			return actionFolder;
		}

		/**
		 * Returns the value of the 'resource' field.
		 *
		 * @return TLResource
		 */
		public TLResource getResource() {
			return resource;
		}

		/**
		 * Returns the value of the 'actions' field.
		 *
		 * @return List<TLAction>
		 */
		public List<TLAction> getActions() {
			return actions;
		}
		
	}
	
}
