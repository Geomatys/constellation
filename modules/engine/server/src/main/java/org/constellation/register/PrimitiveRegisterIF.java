/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.register;

import java.util.List;

import org.constellation.ServiceDef;
import org.constellation.provider.Data;
import org.opengis.feature.type.Name;

/**
 * This interface does *not* aim to be a general abstraction of the register but 
 * only to document the calls made from the web service layer into the engine as 
 * a way of isolating the engine and documenting the functionality which will be 
 * needed.
 * <p>
 * <b>THIS IS A TEMPORARY INTERFACE!<b><br/>
 * This interface is in the right place and our code should work through such 
 * an interface but we have not actually designed anything yet. These methods
 * exist only because we need them---we have no larger view of how to proceed.
 * </p>
 * 
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 *
 */
public interface PrimitiveRegisterIF {

	List<Data> getAllLayerReferences(ServiceDef serviceDef) throws RegisterException ;
	
	List<Data> getLayerReferences(ServiceDef serviceDef, List<Name> layerNames) throws RegisterException ;
	
	Data getLayerReference(ServiceDef serviceDef, Name layerName) throws RegisterException ;

        List<String> getRootDirectory() throws RegisterException;

}
