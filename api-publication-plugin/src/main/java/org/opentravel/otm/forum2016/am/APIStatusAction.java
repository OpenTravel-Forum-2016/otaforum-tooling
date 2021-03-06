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

/**
 * Enumeration of the possible actions that can be used to modify an API's status.
 * 
 * @author S. Livezey
 */
public enum APIStatusAction {
	
	PUBLISH( "Publish" ),
	DEPLOY_AS_PROTOTYPE( "Deploy as a Prototype" ),
	DEMOTE_TO_CREATED( "Demote to Created" ),
	DEMOTE_TO_PROTOTYPED( "Demote to Prototyped" ),
	BLOCK( "Block" ),
	DEPRECATE( "Deprecate" ),
	REPUBLISH( "Re-Publish" ),
	RETIRE( "Retire" );
	
	private String actionText;
	
	/**
	 * Constructor that defines the action text of the enumeration value.
	 * 
	 * @param actionText  the text equivalent of the action status value
	 */
	private APIStatusAction(String actionText) {
		this.actionText = actionText;
	}

	/**
	 * Returns the text equivalent of the action status value.
	 *
	 * @return String
	 */
	public String getActionText() {
		return actionText;
	}
	
}
