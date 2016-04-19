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

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.opentravel.otm.forum2016.am.APIDetails;
import org.opentravel.otm.forum2016.am.APIDocument;
import org.opentravel.otm.forum2016.am.APIOperationFactory;
import org.opentravel.otm.forum2016.am.APIStatusAction;
import org.opentravel.otm.forum2016.am.APISummary;
import org.opentravel.otm.forum2016.am.APIVisibility;
import org.opentravel.otm.forum2016.am.ChangeAPIStatusOperation;
import org.opentravel.otm.forum2016.am.CreateAPIDocumentOperation;
import org.opentravel.otm.forum2016.am.CreateAPIOperation;
import org.opentravel.otm.forum2016.am.DeleteAPIDocumentOperation;
import org.opentravel.otm.forum2016.am.GetAllAPIDocumentsOperation;
import org.opentravel.otm.forum2016.am.GetAllAPIsOperation;
import org.opentravel.otm.forum2016.am.OAuth2ClientConfig;
import org.opentravel.otm.forum2016.am.SwaggerDocument;
import org.opentravel.otm.forum2016.am.UpdateAPIOperation;
import org.opentravel.otm.forum2016.am.UploadAPIDocumentOperation;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.task.SwaggerCompilerTask;
import org.opentravel.schemacompiler.util.SchemaCompilerException;

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
	
	private static final String SWAGGER_DOCUMENT_FORMAT = "html";
	
	private APIOperationFactory opFactory = new APIOperationFactory();
	protected Log log = getLog();
	
	/**
	 * Root folder of the project for which the build is running.
	 */
	@Parameter( defaultValue = "${project.basedir}", readonly = true )
	protected File projectFolder;
	
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
		List<File> contextFolders = OTMProjectUtils.getContextFolders( projectFolder );
		
		// Initialize the binding style before compiling any models
		if (bindingStyle != null) {
			CompilerExtensionRegistry.setActiveExtension( bindingStyle );
		}
		
		for (File contextFolder : contextFolders) {
			String context = contextFolder.getName().replaceAll( "\\s+", "_" );
			List<File> otpFiles;
			
			OTMProjectUtils.findOTMProjects( contextFolder, (otpFiles = new ArrayList<>() ) );
			
			if (!otpFiles.isEmpty()) {
				log.info("Processing OTM Models for Context: " + context);
				
				for (File otpFile : otpFiles) {
					try {
						log.info("  Compiling OTM Project: " + otpFile.getName());
						TLModel model = OTMProjectUtils.loadModel( otpFile );
						
						if (model != null) {
							File outputFolder = new File( projectFolder, "/.target/" + context + "/" + otpFile.getName() );
							List<File> swaggerFiles = compileSwaggerDocuments( model, context, outputFolder );
							
							for (File swaggerFile : swaggerFiles) {
								log.info("  Publishing Swagger Document to WSO2: " + swaggerFile.getName());
								publishSwaggerAPI( swaggerFile, context );
							}
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
	 * Recursively deletes the contents of the specified folder.
	 * 
	 * @param outputFolder  the output folder to clean
	 */
	protected void cleanOutputFolder(File outputFolder) {
		if (outputFolder.isDirectory()) {
			for (File folderItem : outputFolder.listFiles()) {
				cleanOutputFolder( folderItem );
			}
		}
		outputFolder.delete();
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
	private List<File> compileSwaggerDocuments(TLModel model, String context, File outputFolder)
			throws SchemaCompilerException {
		SwaggerCompilerTask swaggerTask = new SwaggerCompilerTask();
		List<File> swaggerFiles = new ArrayList<>();
		
		cleanOutputFolder( outputFolder );
		outputFolder.mkdirs();
		
		swaggerTask.setResourceBaseUrl( APIPublisherConfig.getMockServerUrl() );
		swaggerTask.setOutputFolder( outputFolder.getAbsolutePath() );
		swaggerTask.setGenerateExamples( false );
		swaggerTask.compileOutput( model );
		
		for (File generatedFile : swaggerTask.getGeneratedFiles()) {
			if (generatedFile.getName().toLowerCase().endsWith(".swagger")) {
				swaggerFiles.add( generatedFile );
			}
		}
		return swaggerFiles;
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
		
		// Publish the API specification
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
		
		// Generate HTML documentation for the Swagger API
		Swagger swagger = new SwaggerParser().read( swaggerFile.getAbsolutePath() );
		ClientOptInput docgenInput = new ClientOptInput().opts( new ClientOpts() ).swagger( swagger );
		CodegenConfig swaggerDocConfig = getSwaggerDocumentConfig();
		File docFolder = new File( swaggerFile.getParentFile(), "/html" );
		File docFile = new File( docFolder, "/index.html" );
		
		swaggerDocConfig.additionalProperties().put( "appName", swaggerDoc.getApiName() + " API Specification" );
		swaggerDocConfig.additionalProperties().put( "version", swaggerDoc.getApiVersion() );
		swaggerDocConfig.additionalProperties().put( "appDescription", swaggerDoc.getDescription() );
		swaggerDocConfig.additionalProperties().put( "infoUrl", "http://www.opentravel.org" );
		swaggerDocConfig.additionalProperties().put( "infoEmail", "info@opentravel.org" );
		swaggerDocConfig.setOutputDir( docFolder.getAbsolutePath() );
		docgenInput.setConfig( swaggerDocConfig );
		new DefaultGenerator().opts( docgenInput ).generate();
		
		// Publish all documents associated with the API
		GetAllAPIDocumentsOperation getDocsOp = opFactory.newGetAllAPIDocumentsOperation();
		getDocsOp.setApiId( api.getId() );
		
		List<APIDocument> existingDocs = getDocsOp.execute();
		List<String> publishedFilenames = new ArrayList<>();
		File swaggerFolder = swaggerFile.getParentFile();
		
		publishAPIDocument( "API Documentation", docFile, api, existingDocs ); // publish the HTML documentation
		
		for (File file : swaggerFolder.listFiles()) {
			if (file.isFile() && !file.getName().toLowerCase().endsWith(".swagger")) {
				publishAPIDocument( file.getName(), file, api, existingDocs );
				publishedFilenames.add( file.getName() );
			}
		}
		
		// Delete API documents (only of type OTHER) that no longer exist
		for (APIDocument doc : existingDocs) {
			if (doc.getType().equals("OTHER") && !publishedFilenames.contains( doc.getName() )) {
				try {
					DeleteAPIDocumentOperation deleteDocOp = opFactory.newDeleteAPIDocumentOperation();
					
					deleteDocOp.setApiId( api.getApiDefinition() );
					deleteDocOp.setDocumentId( doc.getId() );
					deleteDocOp.execute();
					
				} catch (IOException e) {
					log.warn("Unable to delete obsolete API document: " + doc.getName());
				}
			}
		}
		log.info("    API documentation published.");
	}
	
	/**
	 * Returns the Swagger code generation configuration for generating HTML documentation.
	 * 
	 * @return CodegenConfig
	 */
    private CodegenConfig getSwaggerDocumentConfig() {
        ServiceLoader<CodegenConfig> loader = ServiceLoader.load(CodegenConfig.class);
        CodegenConfig config = null;
        
        for (CodegenConfig c : loader) {
            if (SWAGGER_DOCUMENT_FORMAT.equals( c.getName() )) {
                config = c;
                break;
            }
        }
        return config;
    }
    
	/**
	 * Publishes the given file as a document of the API.
	 * 
	 * @param docName  the name of the document as it will appear in the API documentation listing
	 * @param docFile  the document file to publish
	 * @param api  the API with which the document should be associated
	 * @param existingDocs  the list of existing documents associated with the API
	 */
	private void publishAPIDocument(String docName, File docFile, APIDetails api, List<APIDocument> existingDocs) {
		try {
			UploadAPIDocumentOperation uploadOp = opFactory.newUploadAPIDocumentOperation();
			String filename = docFile.getName();
			String contentType = "text/html";
			String otherTypeName = null;
			APIDocument apiDoc = null;
			
			// Check to see if the document has already been published
			for (APIDocument doc : existingDocs) {
				if (docName.equals( doc.getName() )) {
					apiDoc = doc;
					break;
				}
			}
			
			if (filename.endsWith( ".schema.json" )) {
				otherTypeName = "JSON Schema";
				contentType = "application/json";
				
			} else if (filename.endsWith( ".xsd" )) {
				otherTypeName = "XML Schema";
				contentType = "application/xml";
			}
			
			if (apiDoc == null) { // need to publish a new document record
				CreateAPIDocumentOperation createDocOp = opFactory.newCreateAPIDocumentOperation();
				
				apiDoc = new APIDocument();
				apiDoc.setName( docName );
				apiDoc.setSourceType( "FILE" );
				apiDoc.setType( (otherTypeName == null) ? "HOWTO" : "OTHER" );
				apiDoc.setOtherTypeName( otherTypeName );
				apiDoc.setVisibility( "API_LEVEL" );
				
				createDocOp.setApiId( api.getId() );
				createDocOp.setDocument( apiDoc );
				apiDoc = createDocOp.execute();
			}
			
			uploadOp.setApiId( api.getId() );
			uploadOp.setDocumentId( apiDoc.getId() );
			uploadOp.setContentFile( docFile );
			uploadOp.setContentType( contentType );
			uploadOp.execute();
			
		} catch (IOException e) {
			log.warn("Error publishing API document: " + docFile.getName());
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
		newApi.setProductionEndpointUrl( getProductionEndpointUrl( swaggerDoc, context ) );
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
	 * @param context  the root context under which the API should be published
	 * @return String
	 */
	private String getProductionEndpointUrl(SwaggerDocument swaggerDoc, String context) {
		String mockServerUrl = APIPublisherConfig.getMockServerUrl();
		
		if (mockServerUrl.endsWith("/")) {
			mockServerUrl = mockServerUrl.substring( 0, mockServerUrl.length() - 1 );
		}
		return mockServerUrl + "/" + context + "/" + swaggerDoc.getApiName() + "/" + swaggerDoc.getApiVersion();
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
