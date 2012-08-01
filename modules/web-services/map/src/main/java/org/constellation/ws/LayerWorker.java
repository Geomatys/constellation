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

import org.constellation.configuration.Language;
import org.constellation.ws.security.SimplePDP;
import org.constellation.ServiceDef.Specification;
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
import org.constellation.configuration.Languages;
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

    private final LayerContext layerContext;

    protected final List<String> supportedLanguages = new ArrayList<String>();

    protected final String defaultLanguage;


    public LayerWorker(final String id, final File configurationDirectory, final Specification specification) {
        super(id, configurationDirectory, specification);
        isStarted = true;

        String defaultLanguageCandidate = null;
        LayerContext candidate          = null;
        if (configurationDirectory != null) {
            final File lcFile = new File(configurationDirectory, "layerContext.xml");
            if (lcFile.exists()) {
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    Object obj   = unmarshaller.unmarshal(lcFile);
                    if (obj instanceof LayerContext) {
                        candidate = (LayerContext) obj;
                        final String sec = candidate.getSecurity();
                        // Instantiaties the PDP only if a rule has been discovered.
                        if (sec != null && !sec.isEmpty()) {
                            pdp = new SimplePDP(sec);
                        }
                        final Languages languages = candidate.getSupportedLanguages();
                        if (languages != null) {
                            for (Language language : languages.getLanguages()) {
                                supportedLanguages.add(language.getLanguageCode());
                                if (language.getDefault()) {
                                    defaultLanguageCandidate = language.getLanguageCode();
                                }
                            }
                        }
                        // look for transaction security
                        final String ts = candidate.getCustomParameters().get("transactionSecurized");
                        if (ts != null && !ts.isEmpty()) {
                            LOGGER.log(Level.INFO, "transaction securized:{0}", ts);
                            transactionSecurized = Boolean.parseBoolean(ts);
                        }
                    } else {
                        startError = "The layer context File does not contain a layerContext object";
                        isStarted  = false;
                        LOGGER.log(Level.WARNING, startError);
                    }
                } catch (JAXBException ex) {
                    startError = "JAXBExeception while unmarshalling the layer context File";
                    isStarted  = false;
                    LOGGER.log(Level.WARNING, startError, ex);
                } finally {
                    if (unmarshaller != null) {
                        GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
                    }
                }
            } else {
                startError = "The configuration file layerContext.xml has not been found";
                isStarted = false;
                LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: ", id);
            }
        } else {
            startError = "The configuration directory has not been found";
            isStarted  = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: " + startError, id);
        }

        layerContext    = candidate;
        defaultLanguage = defaultLanguageCandidate;
    }

    /**
     *
     * @return map of additional informations for each layer declared in the layer context.
     */
    protected Map<Name, Layer> getLayers(){
        if (layerContext == null) {
            return null;
        }
        final LayerProviderProxy namedProxy  = LayerProviderProxy.getInstance();
        final Map<Name, Layer> layers = new HashMap<Name, Layer>();
        /*
         * For each source declared in the layer context we search for layers informations.
         */
        for (final Source source : layerContext.getLayers()) {
            final Set<Name> layerNames = namedProxy.getKeys(source.getId());
            for(final Name layerName : layerNames) {
                final QName qn = new QName(layerName.getNamespaceURI(), layerName.getLocalPart());
                /*
                 * first case : source is in load-all mode
                 */
                if (source.getLoadAll()) {
                    // we look if the layer is excluded
                    if (source.isExcludedLayer(qn)) {
                        continue;
                    // we look for detailled informations in the include sections
                    } else {
                        final Layer layer = source.isIncludedLayer(qn);
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
        return layers;
    }

    /**
     * Return all layers details in LayerProviders from there names.
     * @param layerNames
     * @return a list of LayerDetails
     * @throws CstlServiceException
     */
    protected List<LayerDetails> getLayerReferences(final List<Name> layerNames) throws CstlServiceException {
        final List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
        for (Name layerName : layerNames) {
            layerRefs.add(getLayerReference(layerName));
        }
        return layerRefs;
    }

    /**
     * Search layer real name and return the LayerDetails from LayerProvider.
     * @param layerName
     * @return a LayerDetails
     * @throws CstlServiceException
     */
    protected LayerDetails getLayerReference(final Name layerName) throws CstlServiceException {
        final LayerDetails layerRef;
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();
        final Name fullName = layersContainsKey(layerName);
        if (fullName != null) {
            layerRef = namedProxy.get(fullName);
            if (layerRef == null) throw new CstlServiceException("Unable to find  the Layer named:" + layerName + " in the provider proxy", NO_APPLICABLE_CODE);
        } else {
            throw new CstlServiceException("Unknow Layer name:" + layerName, LAYER_NOT_DEFINED);
        }
        return layerRef;
    }

    /**
     * We can't use directly layers.containsKey because it may miss the namespace or the alias.
     * @param name
     */
    protected Name layersContainsKey(Name name) {
        if (name == null) {
            return null;
        }

        final Map<Name,Layer> layers = getLayers();
        if (layers == null) {
            return null;
        }

        if (!layers.containsKey(name)) {
            //search with only localpart
            for (Name layerName: layers.keySet()) {
                if (layerName.getLocalPart().equals(name.getLocalPart())) {
                    return layerName;
                }
            }

            //search in alias if any
            for (Map.Entry<Name, Layer> l: layers.entrySet()) {
                final Layer layer = l.getValue();
                if (layer.getAlias() != null && layer.getAlias().equals(name.getLocalPart())) {
                    return l.getKey();
                }
            }

            return null;
        }
        return name;
    }

    public Layer getMainLayer() {
        if (layerContext == null) {
            return null;
        }
        return layerContext.getMainLayer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(String ip, String referer) {
        return pdp.isAuthorized(ip, referer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSecured() {
        return (pdp != null);
    }

    protected String getProperty(final String key) {
        if (layerContext != null && layerContext.getCustomParameters() != null) {
            return layerContext.getCustomParameters().get(key);
        }
        return null;
    }
}
