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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.opentravel.otm.forum2016.am.APIDetails;
import org.opentravel.otm.forum2016.am.APIOperationFactory;
import org.opentravel.otm.forum2016.am.APIStatusAction;
import org.opentravel.otm.forum2016.am.APISummary;
import org.opentravel.otm.forum2016.am.APIVisibility;
import org.opentravel.otm.forum2016.am.ChangeAPIStatusOperation;
import org.opentravel.otm.forum2016.am.CreateAPIOperation;
import org.opentravel.otm.forum2016.am.GetAllAPIsOperation;
import org.opentravel.otm.forum2016.am.OAuth2ClientConfig;
import org.opentravel.otm.forum2016.am.SwaggerDocument;
import org.opentravel.otm.forum2016.am.UpdateAPIOperation;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.task.SwaggerCompilerTask;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Maven plugin that scans all top-level project directories for OTM models.  API
 * specifications are compiled for any models that are discovered and all of the
 * resulting Swagger documents are published to the WSO2 API Management server.
 * 
 * @author S. Livezey
 */
@Mojo( name = "publish-api", defaultPhase = LifecyclePhase.INSTALL, threadSafe=true )
@Execute( goal = "publish-api", phase = LifecyclePhase.INSTALL )
public class APIPublisherMojo extends AbstractMojo {
	
	private APIOperationFactory opFactory = new APIOperationFactory();
	private Log log = getLog();
	
	/**
	 * Root folder of the project for which the build is running.
	 */
	@Parameter( defaultValue = "${project.basedir}", readonly = true )
	private File projectFolder;
	
    /**
     * The binding style for generated schemas and services (default is 'OTA2').
     */
	@Parameter
    protected String bindingStyle;

	/**
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		List<File> contextFolders = getContextFolders();
		
		// Initialize the binding style before compiling any models
		if (bindingStyle != null) {
			CompilerExtensionRegistry.setActiveExtension( bindingStyle );
		}
		
		for (File contextFolder : contextFolders) {
			String context = contextFolder.getName().replaceAll( "\\s+", "_" );
			List<File> otpFiles;
			
			findOTMProjects( contextFolder, (otpFiles = new ArrayList<>() ) );
			
			if (!otpFiles.isEmpty()) {
				log.info("Processing OTM Models for Context: " + context);
				
				for (File otpFile : otpFiles) {
					try {
						List<File> swaggerFiles = compileSwaggerDocuments( otpFile, context );
						
						for (File swaggerFile : swaggerFiles) {
							log.info("  Publishing Swagger Document to WSO2: " + swaggerFile.getName());
							publishSwaggerAPI( swaggerFile, context );
						}
						
					} catch (SchemaCompilerException e) {
						log.error("  Unknown error while generating OTM Project", e);
						
					} catch (Exception e) {
						log.error("  Unexpected error during API publication", e);
					}
				}
			}
		}
	}
	
	/**
	 * Returns the list of top-level context folders for the project.
	 * 
	 * @return List<File>
	 */
	private List<File> getContextFolders() {
		List<File> contextFolders = new ArrayList<>();
		
		for (File item : projectFolder.listFiles()) {
			if (item.isDirectory() && !item.getName().startsWith(".")) {
				contextFolders.add( item );
			}
		}
		return contextFolders;
	}
	
	/**
	 * Returns the list of OTM project files in the specified context folder.
	 * 
	 * @param contextFolder  the context folder to search
	 * @param otmProjectFiles  the list to which all OTP files will be appended
	 */
	private void findOTMProjects(File contextFolder, List<File> otmProjectFiles) {
		if (contextFolder.isFile()) {
			if (contextFolder.getName().toLowerCase().endsWith( ".otp" )) {
				otmProjectFiles.add( contextFolder );
			}
		} else {
			for (File item : contextFolder.listFiles()) {
				findOTMProjects( item, otmProjectFiles );
			}
		}
	}
	
	/**
	 * Loads the specified OTM project file and generates Swagger documents in the project build
	 * directory.  All of the generated swagger documents are returned by this method.
	 * 
	 * @param otpFile  the OTM project file to compile
	 * @param context  the context to which the OTM project belongs
	 * @return List<File>
	 * @throws SchemaCompilerException  thrown if an error occurs during Swagger document generation
	 */
	private List<File> compileSwaggerDocuments(File otpFile, String context) throws SchemaCompilerException {
		File outputFolder = new File( projectFolder, "/.target/" + context + "/" + otpFile.getName() );
		SwaggerCompilerTask swaggerTask = new SwaggerCompilerTask();
		List<File> swaggerFiles = new ArrayList<>();
		ValidationFindings findings;
		
		log.info("  Compiling OTM Project: " + otpFile.getName());
		cleanOutputFolder( outputFolder );
		outputFolder.mkdirs();
		
		swaggerTask.setResourceBaseUrl( APIPublisherConfig.getMockServerUrl() );
		swaggerTask.setOutputFolder( outputFolder.getAbsolutePath() );
		swaggerTask.setGenerateExamples( false );
		findings = swaggerTask.compileOutput( otpFile );
		
		if (findings.hasFinding()) {
			log.info("  Errors/Warnings:");
			
			for (String message : findings.getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT )) {
				log.info("    " + message);
			}
		}
		
		if (!findings.hasFinding( FindingType.ERROR )) {
			for (File generatedFile : swaggerTask.getGeneratedFiles()) {
				if (generatedFile.getName().toLowerCase().endsWith(".swagger")) {
					swaggerFiles.add( generatedFile );
				}
			}
		}
		return swaggerFiles;
	}
	
	/**
	 * Recursively deletes the contents of the specified folder.
	 * 
	 * @param outputFolder  the output folder to clean
	 */
	private void cleanOutputFolder(File outputFolder) {
		if (outputFolder.isDirectory()) {
			for (File folderItem : outputFolder.listFiles()) {
				cleanOutputFolder( folderItem );
			}
		}
		outputFolder.delete();
	}
	
	/**
	 * Publishes the Swagger API to the WSO2 API Manager application.
	 * 
	 * @param swaggerFile  the Swagger file containing the API to publish
	 * @param context  the root context under which the API should be published
	 * @throws IOException  thrown if the Swagger document cannot be loaded
	 *						or an error occurs during the publication process
	 */
	private void publishSwaggerAPI(File swaggerFile, String context) throws IOException {
		SwaggerDocument swaggerDoc = new SwaggerDocument( swaggerFile );
		APIDetails api = createAPIDefinition( swaggerDoc, context );
		String existingApiId = getExistingApiID( swaggerDoc );
		
		if (existingApiId == null) { // Create a new API
			CreateAPIOperation createOp = opFactory.newCreateAPIOperation();
			ChangeAPIStatusOperation statusOp = opFactory.newChangeAPIStatusOperation();
			Boolean statusChangeSuccessful;
			
			log.info("    Creating new API definition...");
			createOp.setApi( api );
			api = createOp.execute();
			
			statusOp.setApiId( api.getId() );
			statusOp.setAction( APIStatusAction.PUBLISH );
			statusChangeSuccessful = statusOp.execute();
			log.info("    API published successfully.");
			
			if (!statusChangeSuccessful) {
				throw new IOException("Unknown error while setting API status to 'PUBLISHED'"
						+ " (see server logs for details).");
			}
			
		} else { // Update an existing API
			UpdateAPIOperation updateOp = opFactory.newUpdateAPIOperation();
			
			log.info("    Updating existing API definition...");
			api.setId( existingApiId );
			updateOp.setApi( api );
			updateOp.execute();
			log.info("    API updated successfully.");
		}
	}
	
	/**
	 * If the API has already been published to the WSO2 server, this method will return
	 * its ID.  If the API has not yet been published, null will be returned.
	 * 
	 * @param swaggerDoc  the swagger document defining the API to be published
	 * @return String
	 * @throws IOException  thrown if an error occurs during the call to the WSO2 API Manager
	 */
	private String getExistingApiID(SwaggerDocument swaggerDoc) throws IOException {
		GetAllAPIsOperation getAllOp = opFactory.newGetAllAPIsOperation();
		List<APISummary> existingApis = getAllOp.execute();
		String existingApiID = null;
		
		for (APISummary api : existingApis) {
			if (api.getName().equals( swaggerDoc.getApiName() )
					&& api.getVersion().equals( swaggerDoc.getApiVersion() )) {
				existingApiID = api.getId();
				break;
			}
		}
		return existingApiID;
	}
	
	/**
	 * Constructs a WSO2 API specification based on the given Swagger document.
	 * 
	 * @param swaggerDoc  the Swagger document for which to create an API definition
	 * @param context  the root context under which the API should be published
	 * @return APIDetails
	 */
	private APIDetails createAPIDefinition(SwaggerDocument swaggerDoc, String context) {
		APIDetails newApi = new APIDetails();
		
		newApi.setName( swaggerDoc.getApiName() );
		newApi.setVersion( swaggerDoc.getApiVersion() );
		newApi.setContext( "/" + context + "/" + swaggerDoc.getApiName() );
		newApi.setProvider( OAuth2ClientConfig.getInstance().getUserId() );
		newApi.setDescription( swaggerDoc.getDescription() );
		newApi.getTiers().add( "Unlimited" );
		newApi.setThumbnailUrl( "http://opentravel.org/Images/Logos/logo.png" );
		newApi.setVisibility( APIVisibility.PUBLIC );
		newApi.setCacheTimeout( 300 );
		newApi.setProductionEndpointType( "http" );
		newApi.setProductionEndpointUrl( getProductionEndpointUrl( swaggerDoc ) );
		newApi.setApiDefinition( swaggerDoc.getContent().toString() );
		newApi.setTechnicalOwner( "OpenTravel Alliance" );
		newApi.setTechnicalOwnerEmail( "info@opentravel.org" );
		newApi.setBusinessOwner( "OpenTravel Alliance" );
		newApi.setBusinessOwnerEmail( "info@opentravel.org" );
		newApi.getTransports().add( "https" );
		return newApi;
	}
	
	/**
	 * Returns the production endpoint context for the Swagger document's API.
	 * 
	 * @param swaggerDoc  the Swagger document for which to return an endpoint URL
	 * @return String
	 */
	private String getProductionEndpointUrl(SwaggerDocument swaggerDoc) {
		String mockServerUrl = APIPublisherConfig.getMockServerUrl();
		
		if (mockServerUrl.endsWith("/")) {
			mockServerUrl = mockServerUrl.substring( 0, mockServerUrl.length() - 1 );
		}
		return mockServerUrl + "/" + swaggerDoc.getApiName() + "/" + swaggerDoc.getApiVersion();
	}
	
    /**
     * Initializes the default extension for the schema compiler.
     */
    static {
        try {
            // Force the load of the extension registry class and the initialization of the default
            // compiler extension (as determined by the local configuration file).
            CompilerExtensionRegistry.getActiveExtension();

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
