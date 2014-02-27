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

import org.constellation.provider.coveragesgroup.xml.DataReference;
import org.constellation.provider.coveragesgroup.xml.MapLayer;
import org.constellation.provider.coveragesgroup.xml.StyleReference;

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

        final org.constellation.provider.coveragesgroup.xml.MapItem finalMapItem = new org.constellation.provider.coveragesgroup.xml.MapItem(null);
        final org.constellation.provider.coveragesgroup.xml.MapContext finalMapContext = new org.constellation.provider.coveragesgroup.xml.MapContext(finalMapItem, mapContext.getName());
        if (destination != null) {
            for (final MapItem mapItem : mapContext.items()) {
                if (mapItem instanceof FeatureMapLayer) {
                    final FeatureMapLayer fml = (FeatureMapLayer) mapItem;
                    final String id = fml.getCollection().getID();
                    final MutableStyle ms = fml.getStyle();
                    final StyleReference styleRef = (ms == null) ? null :
                            new StyleReference(ms.getName());
                    final MapLayer ml = new MapLayer(
                            new DataReference(id), styleRef);
                    ml.setOpacity(fml.getOpacity());
                    finalMapItem.getMapItems().add(ml);
                } else if (mapItem instanceof CoverageMapLayer) {
                    final CoverageMapLayer cml = (CoverageMapLayer) mapItem;
                    final String id = cml.getCoverageReference().getName().getLocalPart();
                    final MutableStyle ms = cml.getStyle();
                    final StyleReference styleRef = (ms == null) ? null :
                            new StyleReference(ms.getName());
                    final MapLayer ml = new MapLayer(
                            new DataReference(id), styleRef);
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
    public static void writeMapContext(File destination, org.constellation.provider.coveragesgroup.xml.MapContext mapContext) throws JAXBException, IOException {
        // write finalMapContext
        if (destination != null) {
            final MarshallerPool pool = new MarshallerPool(JAXBContext.newInstance(org.constellation.provider.coveragesgroup.xml.MapContext.class, ObjectFactory.class), null);
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
    private static void visitMapItem(final MapItem mapItemOrig, final org.constellation.provider.coveragesgroup.xml.MapItem finalMapItem) {
        for (final MapItem mapItem : mapItemOrig.items()) {
            if (mapItem instanceof FeatureMapLayer) {
                final FeatureMapLayer fml = (FeatureMapLayer) mapItem;
                final String id = fml.getCollection().getID();
                final MutableStyle ms = fml.getStyle();
                final org.constellation.provider.coveragesgroup.xml.StyleReference styleRef = (ms == null) ? null :
                        new org.constellation.provider.coveragesgroup.xml.StyleReference(ms.getName());
                final org.constellation.provider.coveragesgroup.xml.MapLayer ml = new org.constellation.provider.coveragesgroup.xml.MapLayer(
                        new org.constellation.provider.coveragesgroup.xml.DataReference(id), styleRef);
                finalMapItem.getMapItems().add(ml);
            } else if (mapItem instanceof CoverageMapLayer) {
                final CoverageMapLayer cml = (CoverageMapLayer) mapItem;
                final String id = cml.getCoverageReference().getName().getLocalPart();
                final MutableStyle ms = cml.getStyle();
                final org.constellation.provider.coveragesgroup.xml.StyleReference styleRef = (ms == null) ? null :
                        new org.constellation.provider.coveragesgroup.xml.StyleReference(ms.getName());
                final org.constellation.provider.coveragesgroup.xml.MapLayer ml = new org.constellation.provider.coveragesgroup.xml.MapLayer(
                        new org.constellation.provider.coveragesgroup.xml.DataReference(id), styleRef);
                finalMapItem.getMapItems().add(ml);
            } else {
                final org.constellation.provider.coveragesgroup.xml.MapItem finalMapItemChild = new org.constellation.provider.coveragesgroup.xml.MapItem(null);
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

        final org.constellation.provider.coveragesgroup.xml.MapContext xmlMapCtx = readRawMapContextFile(mapContextFile, login, password);
        if (xmlMapCtx != null) {
            return ConvertersJaxbToGeotk.convertsMapContext(xmlMapCtx,login, password);
        }
        return null;
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
    public static org.constellation.provider.coveragesgroup.xml.MapContext readRawMapContextFile(final File mapContextFile, final String login, final String password) throws JAXBException {
        if (mapContextFile != null) {
            final MarshallerPool pool = new MarshallerPool(JAXBContext.newInstance(org.constellation.provider.coveragesgroup.xml.MapContext.class, ObjectFactory.class), null);
            final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
            Object result = unmarshaller.unmarshal(mapContextFile);
            if (result != null && result instanceof org.constellation.provider.coveragesgroup.xml.MapContext) {
                return (org.constellation.provider.coveragesgroup.xml.MapContext) result;
            }
        }
        return null;
    }
}
