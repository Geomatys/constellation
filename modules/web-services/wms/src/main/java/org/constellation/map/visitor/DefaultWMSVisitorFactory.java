/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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
package org.constellation.map.visitor;

import java.util.List;
import org.constellation.provider.LayerDetails;
import org.constellation.query.Query;
import org.constellation.ws.MimeType;
import org.geotoolkit.wms.xml.GetFeatureInfo;


/**
 * Default getFeatureInfo visitors.
 * Handle :
 * - CSV
 * - HTML
 * - GML
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultWMSVisitorFactory implements WMSVisitorFactory {

    @Override
    public String[] getSupportedMimeTypes() {
        return new String[]{
            "text/xml",
            "text/plain",
            "text/html",
            "application/vnd.ogc.gml",
            "application/vnd.ogc.xml",
            "xml",
            "gml",
            "gml3"
        };
    }

    @Override
    public GetFeatureInfoVisitor createVisitor(final GetFeatureInfo gfi,
            final List<LayerDetails> layerDetails, final String mimeType) {
        if (MimeType.TEXT_PLAIN.equalsIgnoreCase(mimeType)) {
            return new CSVGraphicVisitor(gfi);
        } else if (MimeType.TEXT_HTML.equalsIgnoreCase(mimeType)) {
            return new HTMLGraphicVisitor(gfi, layerDetails);
        } else if (MimeType.APP_GML.equalsIgnoreCase(mimeType)
                || MimeType.TEXT_XML.equalsIgnoreCase(mimeType)
                || MimeType.APP_XML.equalsIgnoreCase(mimeType)
                || Query.XML.equalsIgnoreCase(mimeType)
                || Query.GML.equalsIgnoreCase(mimeType)) {
            // GML
           return new GMLGraphicVisitor(gfi, 0,mimeType);
        } else if (Query.GML3.equalsIgnoreCase(mimeType)) {
            // GML 3
            return new GMLGraphicVisitor(gfi, 1, mimeType);
        }

        return null;
    }


}
