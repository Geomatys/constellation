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

/**
 * This interface does *not* aim to be a general abstraction of the register but 
 * only to document the calls made from the web service layer into the engine as 
 * a way of isolating the engine and documenting the functionality which will be 
 * needed.
 * <p>
 * <b>THIS IS A TEMPORARY INTERFACE!<b>
 * </p>
 * 
 * @author acuster
 * @since 0.3
 *
 */
public interface PrimitiveRegisterIF {

	public List<LayerDetails> getAllLayerReferences(ServiceDef serviceDef) throws RegisterException ;
	
	public List<LayerDetails> getLayerReferences(ServiceDef serviceDef, List<String> layerNames) throws RegisterException ;
	
	public LayerDetails getLayerReference(ServiceDef serviceDef, String layerName) throws RegisterException ;
	

}
