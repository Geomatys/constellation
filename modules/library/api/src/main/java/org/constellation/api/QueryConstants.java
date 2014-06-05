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
package org.constellation.api;

/**
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class QueryConstants {
    
    //Constellation configuration
    public static final String REQUEST_LIST_SERVICE    = "ListAvailableService";
    
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
    public static final String REQUEST_CREATE_LAYER            = "createLayer";
    public static final String REQUEST_UPDATE_LAYER            = "updateLayer";
    public static final String REQUEST_DELETE_LAYER            = "deleteLayer";
    
    //Style operations
    public static final String REQUEST_DOWNLOAD_STYLE   = "downloadStyle";
    public static final String REQUEST_CREATE_STYLE     = "createStyle"; 
    public static final String REQUEST_UPDATE_STYLE     = "updateStyle"; 
    public static final String REQUEST_DELETE_STYLE     = "deleteStyle";
    
    //Tasks operations
    public static final String REQUEST_LIST_PROCESS            = "listProcess";
    public static final String REQUEST_LIST_PROCESS_FOR_FACTO  = "listProcessForFactory";
    public static final String REQUEST_LIST_PROCESS_FACTORIES  = "listProcessFactories";
    public static final String REQUEST_LIST_TASKS              = "listTasks";
    public static final String REQUEST_GET_PROCESS_DESC        = "getProcessDescriptor";
    public static final String REQUEST_GET_TASK_PARAMS         = "getTaskConfiguration";
    public static final String REQUEST_CREATE_TASK            = "createTask";
    public static final String REQUEST_UPDATE_TASK            = "updateTask";
    public static final String REQUEST_DELETE_TASK            = "deleteTask";
    
    //CSW operations
    public static final String REQUEST_REFRESH_INDEX         = "refreshIndex"; 
    public static final String REQUEST_ADD_TO_INDEX          = "AddToIndex";
    public static final String REQUEST_REMOVE_FROM_INDEX     = "RemoveFromIndex";
    public static final String REQUEST_IMPORT_RECORDS        = "importRecords"; 
    public static final String REQUEST_DELETE_RECORDS        = "deleteRecords";
    public static final String REQUEST_DELETE_ALL_RECORDS    = "deleteAllRecords";
    public static final String REQUEST_METADATA_EXIST        = "metadataExist"; 
    public static final String REQUEST_AVAILABLE_SOURCE_TYPE = "getCSWDatasourceType"; 
    public static final String REQUEST_CLEAR_CACHE           = "clearCache"; 
    
    //Service operations
    public static final String REQUEST_UPDATE_CAPABILITIES   = "updateCapabilities";
    public static final String REQUEST_LIST_INSTANCE         = "listInstance";
    
    //basic parameters
    public static final String REQUEST_PARAMETER = "REQUEST";
    public static final String SERVICE_PARAMETER = "SERVICE";
    public static final String VERSION_PARAMETER = "VERSION";
    public static final String UPDATESEQUENCE_PARAMETER = "UPDATESEQUENCE";
    public static final String ACCEPT_VERSIONS_PARAMETER = "ACCEPTVERSIONS";
    public static final String ACCEPT_FORMATS_PARAMETER = "AcceptFormats";
    public static final String SECTIONS_PARAMETER = "Sections";
    public static final String SERVICE_PARAMETER_LC = "service";
    
    private QueryConstants(){}
    
}
