/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
package org.constellation.provider.coveragesgroup.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

import org.apache.sis.internal.jaxb.geometry.ObjectFactory;
import org.apache.sis.xml.MarshallerPool;

import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.style.MutableStyle;

/**
 * Utility class to read/write geotk MapContext into/from file.
 *
 * @author Quentin Boileau (Geomatys)
 */
public final class MapContextIO {

    private MapContextIO() {
    }

    /**
     * Write a Geotk {@link MapContext} in an xml file.
     *
     * @param destination map context file
     * @param mapContext geotk MapContext
     * @throws JAXBException if marshalling fail.
     * @throws IOException if file can"t be created.
     */
    public static void writeGeotkMapContext(File destination, MapContext mapContext) throws JAXBException, IOException {

        final org.geotoolkit.providers.xml.MapItem finalMapItem = new org.geotoolkit.providers.xml.MapItem(null);
        final org.geotoolkit.providers.xml.MapContext finalMapContext = new org.geotoolkit.providers.xml.MapContext(finalMapItem, mapContext.getName());
        if (destination != null) {
            for (final MapItem mapItem : mapContext.items()) {
                if (mapItem instanceof FeatureMapLayer) {
                    final FeatureMapLayer fml = (FeatureMapLayer) mapItem;
                    final String id = fml.getCollection().getID();
                    final MutableStyle ms = fml.getStyle();
                    final org.geotoolkit.providers.xml.StyleReference styleRef = (ms == null) ? null :
                            new org.geotoolkit.providers.xml.StyleReference(ms.getName());
                    final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                            new org.geotoolkit.providers.xml.DataReference(id), styleRef);
                    ml.setOpacity(fml.getOpacity());
                    finalMapItem.getMapItems().add(ml);
                } else if (mapItem instanceof CoverageMapLayer) {
                    final CoverageMapLayer cml = (CoverageMapLayer) mapItem;
                    final String id = cml.getCoverageReference().getName().getLocalPart();
                    final MutableStyle ms = cml.getStyle();
                    final org.geotoolkit.providers.xml.StyleReference styleRef = (ms == null) ? null :
                            new org.geotoolkit.providers.xml.StyleReference(ms.getName());
                    final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                            new org.geotoolkit.providers.xml.DataReference(id), styleRef);
                    ml.setOpacity(cml.getOpacity());
                    finalMapItem.getMapItems().add(ml);
                } else {
                    visitMapItem(mapItem, finalMapItem);
                }
            }
        }

        writeMapContext(destination, finalMapContext);

    }

    /**
     * Write xml MapContext into a File.
     *
     * @param destination
     * @param mapContext
     * @throws JAXBException if marshalling fail.
     * @throws IOException if file can"t be created.
     */
    public static void writeMapContext(File destination, org.geotoolkit.providers.xml.MapContext mapContext) throws JAXBException, IOException {
        // write finalMapContext
        if (destination != null) {
            final MarshallerPool pool = new MarshallerPool(JAXBContext.newInstance(org.geotoolkit.providers.xml.MapContext.class, ObjectFactory.class), null);
            final Marshaller marshaller = pool.acquireMarshaller();
            if (!destination.exists()) {
                destination.createNewFile();
            }
            marshaller.marshal(mapContext, destination);
            pool.recycle(marshaller);
        }
    }

    /**
     * Convert recursively geotk MapItem into xml MapItem.
     *
     * @param mapItemOrig
     * @param finalMapItem
     */
    private static void visitMapItem(final MapItem mapItemOrig, final org.geotoolkit.providers.xml.MapItem finalMapItem) {
        for (final MapItem mapItem : mapItemOrig.items()) {
            if (mapItem instanceof FeatureMapLayer) {
                final FeatureMapLayer fml = (FeatureMapLayer) mapItem;
                final String id = fml.getCollection().getID();
                final MutableStyle ms = fml.getStyle();
                final org.geotoolkit.providers.xml.StyleReference styleRef = (ms == null) ? null :
                        new org.geotoolkit.providers.xml.StyleReference(ms.getName());
                final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                        new org.geotoolkit.providers.xml.DataReference(id), styleRef);
                finalMapItem.getMapItems().add(ml);
            } else if (mapItem instanceof CoverageMapLayer) {
                final CoverageMapLayer cml = (CoverageMapLayer) mapItem;
                final String id = cml.getCoverageReference().getName().getLocalPart();
                final MutableStyle ms = cml.getStyle();
                final org.geotoolkit.providers.xml.StyleReference styleRef = (ms == null) ? null :
                        new org.geotoolkit.providers.xml.StyleReference(ms.getName());
                final org.geotoolkit.providers.xml.MapLayer ml = new org.geotoolkit.providers.xml.MapLayer(
                        new org.geotoolkit.providers.xml.DataReference(id), styleRef);
                finalMapItem.getMapItems().add(ml);
            } else {
                final org.geotoolkit.providers.xml.MapItem finalMapItemChild = new org.geotoolkit.providers.xml.MapItem(null);
                finalMapItem.getMapItems().add(finalMapItemChild);
                visitMapItem(mapItem, finalMapItemChild);
            }
        }
    }


    /**
     * Read a MapContext file and convert it into geotk MapContext object.
     *
     * @param mapContextFile
     * @param login
     * @param password
     * @return geotk MapContext or null
     * @throws JAXBException
     */
    public static MapContext readMapContextFile(final File mapContextFile, final String login, final String password) throws JAXBException {
        if (mapContextFile != null) {
            final MarshallerPool pool = new MarshallerPool(JAXBContext.newInstance(org.geotoolkit.providers.xml.MapContext.class, ObjectFactory.class), null);
            final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
            Object result = unmarshaller.unmarshal(mapContextFile);
            if (result != null && result instanceof org.geotoolkit.providers.xml.MapContext) {
                final org.geotoolkit.providers.xml.MapContext xmlMapCtx = (org.geotoolkit.providers.xml.MapContext) result;
                return ConvertersJaxbToGeotk.convertsMapContext(xmlMapCtx,login, password);
            }
        }
        return null;
    }
}
