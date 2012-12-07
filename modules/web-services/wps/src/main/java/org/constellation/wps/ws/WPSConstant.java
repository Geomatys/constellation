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
package org.constellation.wps.ws;

import java.util.ArrayList;
import java.util.List;
import org.geotoolkit.ows.xml.v110.DCP;
import org.geotoolkit.ows.xml.v110.DomainType;
import org.geotoolkit.ows.xml.v110.HTTP;
import org.geotoolkit.ows.xml.v110.Operation;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.RequestMethodType;


/**
 *  WPS Constants
 *
 * @author Quentin Boileau (Geomatys)
 */
public final class WPSConstant {

    private WPSConstant() {}

    /**
     * WPS Query service
     */
    public static final String WPS_SERVICE = "WPS";

    /**
     * Version
     */
     public static final String WPS_1_0_0 = "1.0.0";

    /**
     * Lang
     */
     public static final String WPS_LANG = "en-EN";

    /**
     * Request parameters.
     */
    public static final String GETCAPABILITIES = "GetCapabilities";
    public static final String DESCRIBEPROCESS = "DescribeProcess";
    public static final String EXECUTE = "Execute";
    
    
    public static final String IDENTIFER_PARAMETER = "IDENTIFIER";
    public static final String LANGUAGE_PARAMETER = "LANGUAGE";

    /* Maximum size in megabytes for a complex input */
    public static final int MAX_MB_INPUT_COMPLEX = 100;
    
   
    /** 
     * Process identifier prefix to uniquely identifies process using OGC URN code.
     */
    public static final String PROCESS_PREFIX = "urn:ogc:cstl:wps:";
    
    /**
     * Temprary directory name used for store responses.
     */
    public static final String TEMP_FOLDER = "/wps/output" ;
    
    public static final OperationsMetadata OPERATIONS_METADATA;
    static {
        final List<DCP> getAndPost = new ArrayList<DCP>();
        getAndPost.add(new DCP(new HTTP(new RequestMethodType("somURL"), new RequestMethodType("someURL"))));

        final List<DCP> onlyPost = new ArrayList<DCP>();
        onlyPost.add(new DCP(new HTTP(null, new RequestMethodType("someURL"))));

        final List<Operation> operations = new ArrayList<Operation>();

        final List<DomainType> gcParameters = new ArrayList<DomainType>();
        gcParameters.add(new DomainType("service", "WPS"));
        gcParameters.add(new DomainType("Acceptversions", "1.0.0"));
        gcParameters.add(new DomainType("AcceptFormats", "text/xml"));
        final Operation getCapabilities = new Operation(getAndPost, gcParameters, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<DomainType> dpParameters = new ArrayList<DomainType>();
        dpParameters.add(new DomainType("service", "WPS"));
        dpParameters.add(new DomainType("version", "1.0.0"));
        final Operation describeProcess = new Operation(getAndPost, dpParameters, null, null, "DescribeProcess");
        operations.add(describeProcess);

        final List<DomainType> eParameters = new ArrayList<DomainType>();
        eParameters.add(new DomainType("service", "WPS"));
        eParameters.add(new DomainType("version", "1.0.0"));
        final Operation execute = new Operation(onlyPost, eParameters, null, null, "Execute");
        operations.add(execute);

        final List<DomainType> constraints = new ArrayList<DomainType>();
        constraints.add(new DomainType("PostEncoding", "XML"));

        OPERATIONS_METADATA = new OperationsMetadata(operations, null, constraints, null);
    }
}
