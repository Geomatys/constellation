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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.constellation.ServiceDef;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;

/**
 * First attempt at a Register, we merely want something functional for now.
 * 
 * 
 * @author Adrian Custer (Geomatys)
 *
 */
public final class PrimitiveRegister implements PrimitiveRegisterIF {
	
	private static PrimitiveRegister instance = null;

	public static PrimitiveRegister getInstance() {
		if ( null == instance ){
			instance = new PrimitiveRegister();
		}
		return instance;
	}
	
	//TODO: only handling providers for now.
	@Override
	public List<LayerDetails> getAllLayerReferences( ServiceDef serviceDef ) throws RegisterException {
		
		if ( serviceDef.getName()=="WMS" && serviceDef.getOrganization()=="OGC"){
			
			List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
			Set<String> layerNames = NamedLayerDP.getInstance().getKeys();
			for (String layerName : layerNames){
				LayerDetails layerRef = NamedLayerDP.getInstance().get(layerName);
				
				if ( null == layerRef) {
		                throw new RegisterException("Unknown layer " + layerName);
				}
				
				layerRefs.add( layerRef );
			}
			
			return layerRefs;
		}
		/* SHOULD NOT REACH HERE */
		throw new RegisterException("Unsupported service type: " + serviceDef.getName() + " " 
				                                                 + serviceDef.getVersion() + " " 
				                                                 + serviceDef.getOrganization() );
	}

	@Override
	public List<LayerDetails> getLayerReferences( ServiceDef serviceDef,
			                                      List<String> layerNames)  throws RegisterException {

		if ( serviceDef.getName() == "WMS" && serviceDef.getOrganization()=="OGC" ){
			
			List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
			for (String layerName : layerNames){
				LayerDetails layerRef = NamedLayerDP.getInstance().get(layerName);
				
				if ( null == layerRef) {
		                throw new RegisterException("Unknown layer " + layerName);
				}
				
				layerRefs.add( layerRef );
			}
			
			return layerRefs;
		}
		/* SHOULD NOT REACH HERE */
		throw new RegisterException("Unsupported service type: " + serviceDef.getName() + " " 
                                                                 + serviceDef.getVersion() + " " 
                                                                 + serviceDef.getOrganization() );
		
	}

	@Override
	public LayerDetails getLayerReference( ServiceDef serviceDef, String layerName) throws RegisterException {

		if ( serviceDef.getName() == "WMS" && serviceDef.getOrganization()=="OGC" ){
			
			LayerDetails layerRef = NamedLayerDP.getInstance().get(layerName);
			
			if ( null == layerRef) {
                throw new RegisterException("Unknown layer " + layerName);
			}
			
			return layerRef;
		}
		/* SHOULD NOT REACH HERE */
		throw new RegisterException("Unsupported service type: " + serviceDef.getName() + " " 
                                                                 + serviceDef.getVersion() + " " 
                                                                 + serviceDef.getOrganization() );
		
	}

	

}
