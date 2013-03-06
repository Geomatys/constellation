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
package org.constellation.api;

/**
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class QueryConstants {
    
    //Constellation configuration
    public static final String REQUEST_GET_CONFIG_PATH = "getConfigurationPath"; 
    public static final String REQUEST_SET_CONFIG_PATH = "setConfigurationPath"; 
    public static final String REQUEST_FULL_RESTART    = "Restart";
    public static final String REQUEST_DOWNLOAD        = "Download";
    public static final String REQUEST_LIST_SERVICE    = "ListAvailableService";
    public static final String REQUEST_UPDATE_USER     = "updateUser";
    public static final String REQUEST_DELETE_USER     = "deleteUser";
    public static final String REQUEST_GET_USER_NAME   = "getUserName";
    public static final String REQUEST_ACCESS          = "access";
    
    
    //Provider services operations
    public static final String REQUEST_LIST_SERVICES                = "listServices"; 
    public static final String REQUEST_GET_SERVICE_DESCRIPTOR       = "getServiceDescriptor";
    public static final String REQUEST_GET_SOURCE_DESCRIPTOR        = "getSourceDescriptor";
    
    //Provider operations
    public static final String REQUEST_RESTART_ALL_LAYER_PROVIDERS  = "restartLayerProviders";
    public static final String REQUEST_RESTART_ALL_STYLE_PROVIDERS  = "restartStyleProviders";
    public static final String REQUEST_CREATE_PROVIDER              = "createProvider"; 
    public static final String REQUEST_UPDATE_PROVIDER              = "updateProvider"; 
    public static final String REQUEST_GET_PROVIDER_CONFIG          = "getProviderConfiguration"; 
    public static final String REQUEST_DELETE_PROVIDER              = "deleteProvider"; 
    public static final String REQUEST_RESTART_PROVIDER             = "restartProvider"; 
    
    //Layer operations
    public static final String REQUEST_CREATE_LAYER     = "createLayer"; 
    public static final String REQUEST_UPDATE_LAYER     = "updateLayer"; 
    public static final String REQUEST_DELETE_LAYER     = "deleteLayer";
    
    //Style operations
    public static final String REQUEST_DOWNLOAD_STYLE   = "downloadStyle";
    public static final String REQUEST_CREATE_STYLE     = "createStyle"; 
    public static final String REQUEST_UPDATE_STYLE     = "updateStyle"; 
    public static final String REQUEST_DELETE_STYLE     = "deleteStyle";
    
    //Tasks operations
    public static final String REQUEST_LIST_PROCESS     = "listProcess";
    public static final String REQUEST_LIST_TASKS       = "listTasks"; 
    public static final String REQUEST_GET_PROCESS_DESC = "getProcessDescriptor"; 
    public static final String REQUEST_GET_TASK_PARAMS  = "getTaskConfiguration";
    public static final String REQUEST_CREATE_TASK      = "createTask";
    public static final String REQUEST_UPDATE_TASK      = "updateTask";
    public static final String REQUEST_DELETE_TASK      = "deleteTask";
    
    //CSW operations
    public static final String REQUEST_REFRESH_INDEX         = "refreshIndex"; 
    public static final String REQUEST_ADD_TO_INDEX          = "AddToIndex";
    public static final String REQUEST_REMOVE_FROM_INDEX     = "RemoveFromIndex";
    public static final String REQUEST_IMPORT_RECORDS        = "importRecords"; 
    public static final String REQUEST_DELETE_RECORDS        = "deleteRecords";
    public static final String REQUEST_METADATA_EXIST        = "metadataExist"; 
    public static final String REQUEST_AVAILABLE_SOURCE_TYPE = "getCSWDatasourceType"; 
    
    //Service operations
    public static final String REQUEST_UPDATE_CAPABILITIES   = "updateCapabilities"; 
    
    //basic parameters
    public static final String REQUEST_PARAMETER = "REQUEST";
    public static final String SERVICE_PARAMETER = "SERVICE";
    public static final String VERSION_PARAMETER = "VERSION";
    public static final String UPDATESEQUENCE_PARAMETER = "UPDATESEQUENCE";
    public static final String ACCEPT_VERSIONS_PARAMETER = "ACCEPTVERSIONS";
    public static final String ACCEPT_FORMATS_PARAMETER = "AcceptFormats";
    public static final String SECTIONS_PARAMETER = "Sections";
    
    private QueryConstants(){}
    
}
