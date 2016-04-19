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
import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static utility methods that assist with the processing of OTM projects and models.
 * 
 * @author S. Livezey
 */
public class OTMProjectUtils {
	
    private static final Logger log = LoggerFactory.getLogger( OTMProjectUtils.class );
    
	/**
	 * Returns the list of top-level context folders for the project.
	 * 
	 * @param projectFolder  the root folder of the mock content workspace
	 * @return List<File>
	 */
	public static List<File> getContextFolders(File projectFolder) {
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
	public static void findOTMProjects(File contextFolder, List<File> otmProjectFiles) {
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
	 * Loads the model from the specified OTM project file.  If errors exist in the
	 * model, this method will display the errors and return null.
	 * 
	 * @param otpFile  the OTM project file to load
	 * @return TLModel
	 * @throws SchemaCompilerException
	 */
	public static TLModel loadModel(File otpFile) throws SchemaCompilerException {
        ValidationFindings findings = new ValidationFindings();
        ProjectManager projectManager = new ProjectManager(false);
        Project project = projectManager.loadProject( otpFile, findings);
		TLModel model = null;
		
		if (findings.hasFinding( FindingType.ERROR )) {
			log.info("  Errors/Warnings:");
			
			for (String message : findings.getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT )) {
				log.info("    " + message);
			}
			
		} else {
			model = project.getModel();
		}
		return model;
	}
	
}
