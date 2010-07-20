/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.register;

import java.util.List;

import org.constellation.ServiceDef;
import org.constellation.provider.LayerDetails;
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

	List<LayerDetails> getAllLayerReferences(ServiceDef serviceDef) throws RegisterException ;
	
	List<LayerDetails> getLayerReferences(ServiceDef serviceDef, List<Name> layerNames) throws RegisterException ;
	
	LayerDetails getLayerReference(ServiceDef serviceDef, Name layerName) throws RegisterException ;

        List<String> getRootDirectory() throws RegisterException;

}
