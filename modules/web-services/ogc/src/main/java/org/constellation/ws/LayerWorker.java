/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
package org.constellation.ws;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.opengis.feature.type.Name;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * A super class for all the web service worker dealing with layers (WMS, WCS, WMTS, WFS, ...)
 * 
 * @author Guilhem Legal (Geomatys)
 */
public abstract class LayerWorker extends AbstractWorker {

    protected LayerContext layerContext;

    protected Map<Name, Layer> layers;

    public LayerWorker(String id, File configurationDirectory) {
        super(id, configurationDirectory);
        isStarted = true;
        if (configurationDirectory != null) {
            File lcFile = new File(configurationDirectory, "layerContext.xml");
            if (lcFile.exists()) {
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    Object obj   = unmarshaller.unmarshal(lcFile);
                    if (obj instanceof LayerContext) {
                        layerContext = (LayerContext) obj;
                        initLayerContext();
                    } else {
                        isStarted = false;
                        LOGGER.log(Level.WARNING, "The layer context File does not contain a layerContext object");
                    }
                } catch (JAXBException ex) {
                    isStarted = false;
                    LOGGER.log(Level.WARNING, "JAXBExeception while unmarshalling the layer context File", ex);
                } finally {
                    if (unmarshaller != null) {
                        GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
                    }
                }
            } else {
                isStarted = false;
                LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: The configuration file layerContext.xml has not been found", id);
            }
        } else {
            isStarted = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: The configuration directory has not been found", id);
        }
    }

    /**
     * Fill the layers Map.
     */
    private void initLayerContext() {
        final LayerProviderProxy namedProxy  = LayerProviderProxy.getInstance();
        layers = new HashMap<Name, Layer>();
        /*
         * For each source declared in the layer context we search for layers informations.
         */
        for (Source source : layerContext.getLayers()) {
            Set<Name> layerNames = namedProxy.getKeys(source.getId());
            for(Name layerName : layerNames) {
                QName qn = new QName(layerName.getNamespaceURI(), layerName.getLocalPart());
                /*
                 * first case : source is in load-all mode
                 */
                if (source.getLoadAll()) {
                    // we look if the layer is excluded
                    if (source.isExcludedLayer(qn)) {
                        continue;
                    // we look for detailled informations in the include sections
                    } else {
                        Layer layer = source.isIncludedLayer(qn);
                        if (layer == null) {
                            layers.put(layerName, new Layer(qn));
                        } else {
                            layers.put(layerName, layer);
                        }
                    }
                /*
                 * second case : we include only the layer in the balise include
                 */
                } else {
                    Layer layer = source.isIncludedLayer(qn);
                    if (layer != null) {
                        layers.put(layerName, layer);
                    }
                }
            }
        }
    }

    protected List<LayerDetails> getLayerReferences(final List<Name> layerNames) throws CstlServiceException {
        final List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
        for (Name layerName : layerNames) {
            layerRefs.add(getLayerReference(layerName));
        }
        return layerRefs;
    }

    protected LayerDetails getLayerReference(final Name layerName) throws CstlServiceException {
        final LayerDetails layerRef;
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();
        if (layersContainsKey(layerName)) {
            layerRef = namedProxy.get(layerName);
        } else {
            throw new CstlServiceException("Unknow Layer name:" + layerName, LAYER_NOT_DEFINED);
        }
        return layerRef;
    }

    /**
     * We can use directly layers.containsKey because it may miss the namespace
     * @param name
     */
    protected boolean layersContainsKey(Name name) {
        if (name == null) {
            return false;
        }
        if (!layers.containsKey(name)) {
            for (Name layerName: layers.keySet()) {
                if (layerName.getLocalPart().equals(name.getLocalPart())) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }


}
