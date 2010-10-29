/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.coverage.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geotoolkit.ows.xml.v110.AllowedValues;
import org.geotoolkit.ows.xml.v110.DCP;
import org.geotoolkit.ows.xml.v110.DomainType;
import org.geotoolkit.ows.xml.v110.HTTP;
import org.geotoolkit.ows.xml.v110.Operation;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.RequestMethodType;
import org.geotoolkit.wcs.xml.v100.DCPTypeType;
import org.geotoolkit.wcs.xml.v100.DCPTypeType.HTTP.Get;
import org.geotoolkit.wcs.xml.v100.DCPTypeType.HTTP.Post;
import org.geotoolkit.wcs.xml.v100.OnlineResourceType;
import org.geotoolkit.wcs.xml.v100.WCSCapabilityType.Request;

/**
 *  WCS Constants
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WCSConstant {

    public static final Request REQUEST_100;
    static {
        final Get get         = new DCPTypeType.HTTP.Get(new OnlineResourceType("someurl"));
        final Post post       = new DCPTypeType.HTTP.Post(new OnlineResourceType("someurl"));
        final DCPTypeType dcp = new DCPTypeType(new DCPTypeType.HTTP(get, post));
        REQUEST_100 = new Request();
        final Request.DescribeCoverage describeCoverage = new Request.DescribeCoverage(Arrays.asList(dcp));
        REQUEST_100.setDescribeCoverage(describeCoverage);
        final Request.GetCapabilities getCapabilities = new Request.GetCapabilities(Arrays.asList(dcp));
        REQUEST_100.setGetCapabilities(getCapabilities);
        final Request.GetCoverage getCoverage = new Request.GetCoverage(Arrays.asList(dcp));
        REQUEST_100.setGetCoverage(getCoverage);
    }

    public static final OperationsMetadata OPERATIONS_METADATA_111;
    static {
        final List<DCP> dcps = new ArrayList<DCP>();
        dcps.add(new DCP(new HTTP(new RequestMethodType("somURL"), new RequestMethodType("someURL"))));

        final List<DCP> dcps2 = new ArrayList<DCP>();
        dcps2.add(new DCP(new HTTP(null, new RequestMethodType("someURL"))));

        final List<Operation> operations = new ArrayList<Operation>();

        final List<DomainType> gcParameters = new ArrayList<DomainType>();
        gcParameters.add(new DomainType("AcceptVersions", new AllowedValues(Arrays.asList("1.0.0","1.1.1"))));
        gcParameters.add(new DomainType("AcceptFormats", new AllowedValues(Arrays.asList("text/xml","application/vnd.ogc.wcs_xml"))));
        gcParameters.add(new DomainType("Service", new AllowedValues(Arrays.asList("WCS"))));
        gcParameters.add(new DomainType("Sections", new AllowedValues(Arrays.asList("ServiceIdentification","ServiceProvider","OperationsMetadata","Contents"))));
        Operation getCapabilities = new Operation(dcps, gcParameters, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<DomainType> gcoParameters = new ArrayList<DomainType>();
        gcoParameters.add(new DomainType("Version", new AllowedValues(Arrays.asList("1.0.0","1.1.1"))));
        gcoParameters.add(new DomainType("Service", new AllowedValues(Arrays.asList("WCS"))));
        gcoParameters.add(new DomainType("Format", new AllowedValues(Arrays.asList("image/gif","image/png","image/jpeg","matrix"))));
        gcoParameters.add(new DomainType("Store", new AllowedValues(Arrays.asList("false"))));
        Operation getCoverage = new Operation(dcps, gcoParameters, null, null, "GetCoverage");
        operations.add(getCoverage);

        final List<DomainType> dcParameters = new ArrayList<DomainType>();
        dcParameters.add(new DomainType("Version", new AllowedValues(Arrays.asList("1.0.0","1.1.1"))));
        dcParameters.add(new DomainType("Service", new AllowedValues(Arrays.asList("WCS"))));
        dcParameters.add(new DomainType("Format", new AllowedValues(Arrays.asList("text/xml"))));
        Operation describeCoverage = new Operation(dcps, dcParameters, null, null, "DescribeCoverage");
        operations.add(describeCoverage);

        final List<DomainType> constraints = new ArrayList<DomainType>();
        constraints.add(new DomainType("PostEncoding", new AllowedValues(Arrays.asList("XML"))));
        
        OPERATIONS_METADATA_111 = new OperationsMetadata(operations, null, null, null);
    }

}
