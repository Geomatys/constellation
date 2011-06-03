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
    
    //Provider services operations
    public static final String REQUEST_LIST_SERVICES            = "listServices"; 
    public static final String REQUEST_GET_SERVICE_DESCRIPTOR   = "getServiceDescriptor";
    public static final String REQUEST_GET_SOURCE_DESCRIPTOR    = "getSourceDescriptor";
    
    //Provider operations
    public static final String REQUEST_CREATE_PROVIDER      = "createProvider"; 
    public static final String REQUEST_UPDATE_PROVIDER      = "updateProvider"; 
    public static final String REQUEST_GET_PROVIDER_CONFIG  = "getProviderConfiguration"; 
    public static final String REQUEST_DELETE_PROVIDER      = "deleteProvider"; 
    public static final String REQUEST_RESTART_PROVIDER     = "restartProvider"; 
    
    //Layer operations
    public static final String REQUEST_CREATE_LAYER = "createLayer"; 
    public static final String REQUEST_UPDATE_LAYER = "updateLayer"; 
    public static final String REQUEST_DELETE_LAYER = "deleteLayer";
    
    //Other
    public static final String REQUEST_REFRESH_INDEX    = "refreshIndex"; 
    
    private QueryConstants(){}
    
}
