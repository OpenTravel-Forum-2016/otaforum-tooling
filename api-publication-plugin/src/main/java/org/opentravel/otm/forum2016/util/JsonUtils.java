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

package org.opentravel.otm.forum2016.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Static utility methods for manipulating JSON content.
 * 
 * @author S. Livezey
 */
public class JsonUtils {
	
	/**
	 * Returns the string property with the given name or null if no such property
	 * exists.
	 * 
	 * @param jsonObj  the JSON object from which to obtain the property value
	 * @param propertyName  the name of the property to retrieve
	 * @return String
	 */
	public static String getProperty(JsonObject jsonObj, String propertyName) {
		JsonElement value = jsonObj.get( propertyName );
		return (value == null) ? null : value.getAsString();
	}
	
}
