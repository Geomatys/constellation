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

package org.constellation.wmts.ws;

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

/**
 *  WMTS Constants
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class WMTSConstant {

    private WMTSConstant() {}
    
    public static final OperationsMetadata OPERATIONS_METADATA;
    static {
        final List<DCP> getAndPost = new ArrayList<DCP>();
        getAndPost.add(new DCP(new HTTP(new RequestMethodType("somURL"), new RequestMethodType("someURL"))));

        final List<DCP> onlyPost = new ArrayList<DCP>();
        onlyPost.add(new DCP(new HTTP(null, new RequestMethodType("someURL"))));

        final List<Operation> operations = new ArrayList<Operation>();

        final Operation getCapabilities = new Operation(getAndPost, null, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final Operation getTile = new Operation(getAndPost, null, null, null, "GetTile");
        operations.add(getTile);

        final Operation getFeatureInfo = new Operation(getAndPost, null, null, null, "GetFeatureInfo");
        operations.add(getFeatureInfo);

        final List<DomainType> constraints = new ArrayList<DomainType>();
        constraints.add(new DomainType("PostEncoding", new AllowedValues(Arrays.asList("XML"))));

        OPERATIONS_METADATA = new OperationsMetadata(operations, constraints, null, null);
    }
}
