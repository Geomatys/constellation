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

import java.awt.geom.AffineTransform;
import java.util.List;
import javax.measure.unit.Unit;
import org.constellation.ws.MimeType;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.memory.GenericExtendFeatureIterator.FeatureExtend;
import org.geotoolkit.process.vector.sort.SortBy;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.Geometry;

/**
 * 
 * @author Quentin Boileau
 */
public class WPSIO {



     public final static List<InputClass> USEDCLASS = UnmodifiableArrayList.wrap(
             
             new InputClass(Feature.class, MimeType.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE), //XML
             new InputClass(Feature.class, "application/octec-stream", Encoding.NULL, Schema.NULL),         //SHP

             new InputClass(FeatureCollection.class, MimeType.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE),  //XML
             new InputClass(FeatureCollection.class, "application/octec-stream",Encoding.NULL, Schema.NULL),           //SHP

             new InputClass(Geometry.class, MimeType.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE),

             new InputClass(FeatureType.class, MimeType.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE)
             
             //new InputClass(Unit.class, Mime.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE),

             //new InputClass(FeatureExtend.class, Mime.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE),
             //new InputClass(AffineTransform.class, Mime.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE),
             //new InputClass(Filter.class, Mime.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE),
             //new InputClass(SortBy.class, Mime.TEXT_XML, Encoding.UTF8, Schema.OGC_FEATURE)
             );


    private WPSIO (){
    }


    public static enum Encoding{

        NULL(null),
        UTF8("utf-8");
        public final String encoding;

        private Encoding(String encoding) {
            this.encoding = encoding;
        }
        public String getValue(){
            return encoding;
        }
        
    }

    public static enum Schema{

        NULL(null),
        OGC_FEATURE("http://schemas.opengis.net/gml/3.1.1/base/feature.xsd");
        public final String schema;

        private Schema(String schema) {
            this.schema = schema;
        }
        public String getValue(){
            return schema;
        }
    }


    public static class InputClass{
        private Class clazz;
        private String mime;
        private Encoding encoding;
        private Schema schema;

        public InputClass(Class claz, String mime,Encoding enco, Schema sch) {
            this.clazz = claz;
            this.mime = mime;
            this.encoding = enco;
            this.schema = sch;
        }
        public Class getClazz(){
            return clazz;
        }
        public String getEncoding(){
            return encoding.getValue();
        }
        public String getMime(){
            return mime;
        }
        public String getSchema(){
            return schema.getValue();
        }
    }

}
