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

package org.constellation.map.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geotoolkit.lang.Immutable;
import org.geotoolkit.wms.xml.v111.DescribeLayer;
import org.geotoolkit.wms.xml.v111.GetCapabilities;
import org.geotoolkit.wms.xml.v111.GetFeatureInfo;
import org.geotoolkit.wms.xml.v111.GetLegendGraphic;
import org.geotoolkit.wms.xml.v111.GetMap;
import org.geotoolkit.wms.xml.v130.DCPType;
import org.geotoolkit.wms.xml.v130.Get;
import org.geotoolkit.wms.xml.v130.HTTP;
import org.geotoolkit.wms.xml.v130.ObjectFactory;
import org.geotoolkit.wms.xml.v130.OnlineResource;
import org.geotoolkit.wms.xml.v130.OperationType;
import org.geotoolkit.wms.xml.v130.Post;
import org.geotoolkit.wms.xml.v130.Request;

/**
 *  WMS Constants
 *
 * @author Guilhem Legal (Geomatys)
 */
@Immutable
public class WMSConstant {

    public static final Request REQUEST_130;
    static {
        final DCPType dcp = new DCPType(new HTTP(new Get(new OnlineResource("someurl")), new Post(new OnlineResource("someurl"))));

        final OperationType getCapabilities = new OperationType(Arrays.asList("text/xml", "application/vnd.ogc.wms_xml"), dcp);
        final OperationType getMap          = new OperationType(Arrays.asList("image/gif","image/png","image/jpeg","image/bmp","image/tiff","image/x-portable-pixmap"), dcp);
        final OperationType getFeatureInfo  = new OperationType(Arrays.asList("text/xml","text/plain","text/html"), dcp);

        REQUEST_130 = new Request(getCapabilities, getMap, getFeatureInfo);
        
        /*
         * Extended Operation
         */
        ObjectFactory factory = new ObjectFactory();
        
        final OperationType describeLayer    = new OperationType(Arrays.asList("text/xml"), dcp);
        final OperationType getLegendGraphic = new OperationType(Arrays.asList("image/png","image/jpeg","image/gif","image/tiff"), dcp);

        REQUEST_130.getExtendedOperation().add(factory.createDescribeLayer(describeLayer));

        REQUEST_130.getExtendedOperation().add(factory.createGetLegendGraphic(getLegendGraphic));

    }

    public static final org.geotoolkit.wms.xml.v111.Request REQUEST_111;
    static {
        final org.geotoolkit.wms.xml.v111.Post post   = new org.geotoolkit.wms.xml.v111.Post(new org.geotoolkit.wms.xml.v111.OnlineResource("someurl"));
        final org.geotoolkit.wms.xml.v111.Get get     = new org.geotoolkit.wms.xml.v111.Get(new org.geotoolkit.wms.xml.v111.OnlineResource("someurl"));
        final org.geotoolkit.wms.xml.v111.HTTP http   = new org.geotoolkit.wms.xml.v111.HTTP(get, post);
        final org.geotoolkit.wms.xml.v111.DCPType dcp = new org.geotoolkit.wms.xml.v111.DCPType(http);

        final GetCapabilities getCapabilities = new GetCapabilities(Arrays.asList("text/xml", "application/vnd.ogc.wms_xml"), dcp);
        final GetMap getMap                   = new GetMap(Arrays.asList("image/gif","image/png","image/jpeg","image/bmp","image/tiff","image/x-portable-pixmap"), dcp);
        final GetFeatureInfo getFeatureInfo   = new GetFeatureInfo(Arrays.asList("text/xml","text/plain","text/html"), dcp);

         /*
         * Extended Operation
         */
        final DescribeLayer describeLayer       = new DescribeLayer(Arrays.asList("text/xml"), dcp);
        final GetLegendGraphic getLegendGraphic = new GetLegendGraphic(Arrays.asList("image/png","image/jpeg","image/gif","image/tiff"), dcp);

        REQUEST_111 = new org.geotoolkit.wms.xml.v111.Request(getCapabilities, getMap, getFeatureInfo, describeLayer, getLegendGraphic, null, null);
    }

    public static final List<String> EXCEPTION_111 = new ArrayList<String>();
    static {
        EXCEPTION_111.add("application/vnd.ogc.se_xml");
        EXCEPTION_111.add("application/vnd.ogc.se_inimage");
        EXCEPTION_111.add("application/vnd.ogc.se_blank");
    }

    public static final List<String> EXCEPTION_130 = new ArrayList<String>();
    static {
        EXCEPTION_130.add("XML");
        EXCEPTION_130.add("INIMAGE");
        EXCEPTION_130.add("BLANK");
    }
}
