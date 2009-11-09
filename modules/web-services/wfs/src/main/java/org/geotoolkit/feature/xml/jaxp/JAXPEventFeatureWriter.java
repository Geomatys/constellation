/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2005-2009, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotoolkit.feature.xml.jaxp;

import com.sun.xml.internal.stream.events.CharacterEvent;
import com.sun.xml.internal.stream.events.EndElementEvent;
import com.sun.xml.internal.stream.events.StartDocumentEvent;
import com.sun.xml.internal.stream.events.StartElementEvent;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.geotoolkit.data.collection.FeatureCollection;
import org.geotoolkit.data.collection.FeatureIterator;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.simple.DefaultSimpleFeature;
import org.geotoolkit.feature.simple.DefaultSimpleFeatureType;
import org.geotoolkit.feature.type.DefaultGeometryDescriptor;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.geometry.isoonjts.JTSUtils;
import org.geotoolkit.internal.jaxb.ObjectFactory;
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.Geometry;

/**
 *
 * @module pending
 *
 * @author Guilhem Legal (Geomatys)
 */
public class JAXPEventFeatureWriter implements XmlFeatureWriter {

    private static final Logger LOGGER = Logger.getLogger("org.geotoolkit.feature.xml.jaxp");

    private static MarshallerPool pool;

    private static ObjectFactory factory = new ObjectFactory();
    
    public JAXPEventFeatureWriter() throws JAXBException {
         // for GML geometries unmarshall
        pool = new MarshallerPool(ObjectFactory.class);
    }

    @Override
    public String write(SimpleFeature feature) {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            StringWriter sw = new StringWriter();
            XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(sw);

            // the XML header
            eventWriter.add(new StartDocumentEvent("UTF-8"));

            write(feature, eventWriter);

            // we close the stream
            eventWriter.flush();
            eventWriter.close();
            
            return sw.toString();
            
        } catch (XMLStreamException ex) {
            LOGGER.severe("XMl stream exception while writing the feature: " + ex.getMessage());
        }
        return null;
    }

    public void write(SimpleFeature feature, XMLEventWriter eventWriter) {

        try {
        //the root element of the xml document (type of the feature)
            FeatureType type = feature.getType();
            String namespace = type.getName().getNamespaceURI();
            String localPart = type.getName().getLocalPart();
            QName root = new QName(namespace, localPart);
            eventWriter.add(new StartElementEvent(root));


            //the simple nodes (attributes of the feature)
            for (Property a : feature.getProperties()) {
                if (!"the_geom".equals(a.getName().getLocalPart())) {
                    QName property = new QName(a.getName().getNamespaceURI(), a.getName().getLocalPart());
                    eventWriter.add(new StartElementEvent(property));
                    eventWriter.add(new CharacterEvent(getStringValue(a.getValue())));
                    eventWriter.add(new EndElementEvent(property));
                }
            }

            // we add the geometry
            QName geomQname = new QName("isoGeometry");
            eventWriter.add(new StartElementEvent(geomQname));
            Geometry isoGeometry = JTSUtils.toISO((com.vividsolutions.jts.geom.Geometry) feature.getDefaultGeometry(), feature.getFeatureType().getCoordinateReferenceSystem());

            Marshaller m = null;
            try {
                m= pool.acquireMarshaller();
                m.setProperty(m.JAXB_FRAGMENT, true);
                m.setProperty(m.JAXB_FORMATTED_OUTPUT, true);
                m.marshal(factory.buildAnyGeometry(isoGeometry), eventWriter);
            } catch (JAXBException ex) {
                LOGGER.severe("JAXB Exception while marshalling the iso geometry: " + ex.getMessage());
            } finally {
                if (m != null)
                    pool.release(m);
            }
            eventWriter.add(new EndElementEvent(geomQname));


            eventWriter.add(new EndElementEvent(root));
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "XMl stream exception while writing the feature: " + ex.getMessage(), ex);
        }
    }


    @Override
    public String write(FeatureCollection featureCollection) {
        try {
            
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            StringWriter sw                = new StringWriter();
            XMLEventWriter eventWriter     = outputFactory.createXMLEventWriter(sw);

            // the XML header
            eventWriter.add(new StartDocumentEvent("UTF-8"));
            
            // the root Element
            QName root = new QName("http://www.opengis.net/gml", "FeatureCollection");
            eventWriter.add(new StartElementEvent(root));


            // we write each feature member of the collection
            QName memberName = new QName("http://www.opengis.net/gml", "featureMember");
            FeatureIterator iterator =featureCollection.features();
            while (iterator.hasNext()) {
                final SimpleFeature f = (SimpleFeature) iterator.next();

                eventWriter.add(new StartElementEvent(memberName));
                write(f, eventWriter);
                eventWriter.add(new EndElementEvent(memberName));
            }

            // we close the stream
            iterator.close();
            eventWriter.flush();
            eventWriter.close();
            
            return sw.toString();
        
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "XMl stream exception while writing the feature: " + ex.getMessage(), ex);
        }
        return null;
    }



    
    public SimpleFeature read(String xml) throws XMLStreamException, JAXBException {

        XMLInputFactory XMLfactory = XMLInputFactory.newInstance();
        XMLfactory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", Boolean.TRUE);

        XMLEventReader eventReader = XMLfactory.createXMLEventReader(new StringReader(xml));

        boolean searchRoot      = true;
        Name name               = null;
        int nbAttribute         = 0;
        List<Object> values     = new ArrayList<Object>();
        GeometryDescriptor geom = null;

        while(eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            LOGGER.info(event + "");
            
            //we are looking for the root mark
            if (event.isStartElement() && searchRoot) {
                StartElement startEvent = event.asStartElement();
                QName q                 = startEvent.getName();
                name                    = new DefaultName(q);
                searchRoot              = false;
            
            // then we extract each attribute
            } else if (event.isStartElement()) {
                nbAttribute++;
                StartElement startEvent = event.asStartElement();
                QName q                 = startEvent.getName();

                if (!q.getLocalPart().equals("isoGeometry")) {
                    XMLEvent content = eventReader.nextEvent();
                    LOGGER.info("find value:" + content.toString() + " for attribute :" + q.getLocalPart());
                } else {
                    eventReader.next();
                    Unmarshaller un  = pool.acquireUnmarshaller();
                    Geometry isoGeom = (Geometry) ((JAXBElement)un.unmarshal(eventReader)).getValue();
                    // TODO iso => JTS
                    geom = new DefaultGeometryDescriptor(null, name, nbAttribute, nbAttribute, searchRoot, isoGeom);
                }

            }
        }

        
        SimpleFeatureType type = new DefaultSimpleFeatureType(name, null, null, false, null, null, null);
        FeatureId id           = new DefaultFeatureId("");
        DefaultSimpleFeature SimpleFeature = new DefaultSimpleFeature(values, type, id);

        return null;
    }


    /**
     * Return a String representation of an Object.
     * Accepted types are : - Integer, Long, String
     * Else it return null.
     * 
     * @param obj A primitive object
     * @return A String representation of the Object.
     */
    public static String getStringValue(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Integer || obj instanceof Long) {
            return obj + "";

        } else if (obj != null) {
            LOGGER.warning("unexpected type:" + obj.getClass());
        } 
        return null;
    }

}