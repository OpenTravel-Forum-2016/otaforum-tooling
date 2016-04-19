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

/**
 * Application class that launches a simple Swing frame that allows the user
 * to choose their context folder.
 * 
 * @author S. Livezey
 */
public class UpdateContextApp {
	
	/**
	 * Main method invoked from the command-line shell.
	 * 
	 * @param args  the command line arguments
	 */
	public static void main(String[] args) {
		try {
			File projectFolder = new File( System.getProperty( "user.dir" ) );
			String context = null;
			
			if ((args != null) && (args.length >= 1)) {
				context = args[0];
			}
			
			if (context == null) {
				ContextChooser chooser = new ContextChooser( projectFolder );
				
				synchronized (chooser) {
					try {
						chooser.wait();
						
					} catch (InterruptedException e) {
						// Ignore and exit
					}
				}
				if (!chooser.isOkPressed()) {
					System.out.println( "Update cancelled by user." );
				}
				context = chooser.getSelectedContext();
			}
			
			if (context != null) {
				new UpdateContextJob( projectFolder, context ).execute();
			}
			
		} catch (Throwable t) {
			t.printStackTrace( System.out );
		}
	}
	
}
