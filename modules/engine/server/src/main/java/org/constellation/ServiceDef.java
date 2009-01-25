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

package org.constellation;

/**
 * All the services known by Constellation, as a Name:Version pair.
 * 
 * @author Adrian Custer (Geomatys)
 *
 */
public enum ServiceDef {
	
	WMS_1_0_0("WMS", "1.0.0", "OGC"),
	WMS_1_1_0_SLD("WMS", "1.1.0", "OGC"),
	WMS_1_1_1_SLD("WMS", "1.1.0", "OGC"),
	WMS_1_3_0("WMS", "1.3.0", "OGC"),
	
	WCS_1_0_0("WCS", "1.0.0", "OGC"),
	WCS_1_1_0("WCS", "1.1.0", "OGC"),
	WCS_1_1_1("WCS", "1.1.2", "OGC"),
	WCS_1_1_2("WCS", "1.1.2", "OGC"),
	
	PEP("PEP", "none", "OASIS"),
	PDP("PDP", "none", "OASIS");
	
	
	//Functional block
	private final String name;
	private final String version;
	private final String organization;
	
	private ServiceDef(String name, String version, String organization){
		this.name=name;
		this.version=version;
		this.organization=organization;
	}
	
	public String getName(){return name;}
	public String getVersion(){return version;}
	public String getOrganization(){return organization;}
	
}
