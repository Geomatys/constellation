/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.map.configuration;

/**
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class QueryConstants {
    
    public static final String REQUEST_ADD_SOURCE       = "addSource"; 
    public static final String REQUEST_MODIFY_SOURCE    = "modifySource"; 
    public static final String REQUEST_GET_SOURCE       = "getSource"; 
    public static final String REQUEST_REMOVE_SOURCE    = "removeSource"; 
    public static final String REQUEST_ADD_LAYER        = "addLayer"; 
    public static final String REQUEST_REMOVE_LAYER     = "removeLayer"; 
    public static final String REQUEST_MODIFY_LAYER     = "modifyLayer"; 
    public static final String REQUEST_GET_SERVICE_DESCRIPTOR   = "getServiceDescriptor"; 
    public static final String REQUEST_GET_SOURCE_DESCRIPTOR   = "getSourceDescriptor"; 
    public static final String REQUEST_LIST_SERVICES    = "listServices"; 
    public static final String REQUEST_REFRESH_INDEX    = "refreshIndex"; 
    
    private QueryConstants(){}
    
}
