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
