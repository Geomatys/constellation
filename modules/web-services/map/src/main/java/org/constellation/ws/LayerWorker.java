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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.constellation.configuration.Language;
import org.constellation.ws.security.SimplePDP;
import org.constellation.ServiceDef.Specification;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.Languages;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.map.factory.MapFactory;
import org.constellation.map.security.LayerSecurityFilter;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.opengis.feature.type.Name;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.style.MutableStyle;

/**
 * A super class for all the web service worker dealing with layers (WMS, WCS, WMTS, WFS, ...)
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class LayerWorker extends AbstractWorker {

    private final LayerContext layerContext;

    protected final List<String> supportedLanguages = new ArrayList<String>();

    protected final String defaultLanguage;

    private LayerSecurityFilter securityFilter;

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
                        // look for shiro accessibility
                        final String sa = candidate.getCustomParameters().get("shiroAccessible");
                        if (sa != null && !sa.isEmpty()) {
                            shiroAccessible = Boolean.parseBoolean(sa);
                        }
                        // look for capabilities cache flag
                        final String cc = candidate.getCustomParameters().get("cacheCapabilities");
                        if (cc != null && !cc.isEmpty()) {
                            cacheCapabilities = Boolean.parseBoolean(cc);
                        }
                        final MapFactory mapfactory = getMapFactory(candidate.getImplementation());
                        securityFilter = mapfactory.getSecurityFilter();
                    } else {
                        startError = "The layer context File does not contain a layerContext object";
                        isStarted  = false;
                        LOGGER.log(Level.WARNING, startError);
                    }
                } catch (JAXBException ex) {
                    startError = "JAXBExeception while unmarshalling the layer context File";
                    isStarted  = false;
                    LOGGER.log(Level.WARNING, startError, ex);
                } catch (FactoryNotFoundException ex) {
                    startError = ex.getMessage();
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
        
        //listen to changes on the providers to clear the getcapabilities cache
        LayerProviderProxy.getInstance().addPropertyListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refreshUpdateSequence();
                clearCapabilitiesCache();
            }
        });
    }

    /**
     * Select the good CSW factory in the available ones in function of the dataSource type.
     *
     * @param type
     * @return
     */
    private MapFactory getMapFactory(final DataSourceType type) {
        final Iterator<MapFactory> ite = ServiceRegistry.lookupProviders(MapFactory.class);
        while (ite.hasNext()) {
            MapFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(type)) {
                return currentFactory;
            }
        }
        throw new FactoryNotFoundException("No Map factory has been found for type:" + type);
    }
    
    /**
     *
     * @return map of additional informations for each layer declared in the layer context.
     */
    protected Map<Name, Layer> getLayers(final String login){
        if (layerContext == null) {
            return new HashMap<Name, Layer>();
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
                        if (securityFilter.allowed(login, layerName)) {
                            final Layer layer = source.isIncludedLayer(qn);
                            if (layer == null) {
                                layers.put(layerName, new Layer(qn));
                            } else {
                                layers.put(layerName, layer);
                            }
                        }
                    }
                /*
                 * second case : we include only the layer in the balise include
                 */
                } else {
                    Layer layer = source.isIncludedLayer(qn);
                    if (layer != null && securityFilter.allowed(login, layerName)) {
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
    protected List<LayerDetails> getLayerReferences(final String login, final Collection<Name> layerNames) throws CstlServiceException {
        final List<LayerDetails> layerRefs = new ArrayList<LayerDetails>();
        for (Name layerName : layerNames) {
            layerRefs.add(getLayerReference(login, layerName));
        }
        return layerRefs;
    }

    /**
     * Search layer real name and return the LayerDetails from LayerProvider.
     * @param layerName
     * @return a LayerDetails
     * @throws CstlServiceException
     */
    protected LayerDetails getLayerReference(final String login, final Name layerName) throws CstlServiceException {
        final LayerDetails layerRef;
        final LayerProviderProxy namedProxy = LayerProviderProxy.getInstance();
        final Name fullName = layersContainsKey(login, layerName);
        if (fullName != null) {
            layerRef = namedProxy.get(fullName);
            if (layerRef == null) {throw new CstlServiceException("Unable to find  the Layer named:" + layerName + " in the provider proxy", NO_APPLICABLE_CODE);}
        } else {
            throw new CstlServiceException("Unknow Layer name:" + layerName, LAYER_NOT_DEFINED);
        }
        return layerRef;
    }

    /**
     * We can't use directly layers.containsKey because it may miss the namespace or the alias.
     * @param name
     */
    protected Name layersContainsKey(final String login, final Name name) {
        if (name == null) {
            return null;
        }

        final Map<Name,Layer> layers = getLayers(login);
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
                if (layer.getAlias() != null && !layer.getAlias().isEmpty()) {
                    final String alias = layer.getAlias().trim().replaceAll(" ", "_");
                    if (alias.equals(name.getLocalPart())) {
                        return l.getKey();
                    }
                }
            }

            return null;
        }
        return name;
    }
    
    protected static MutableStyle getStyle(final String styleName) throws CstlServiceException {
        final MutableStyle style;
        if (styleName != null && !styleName.isEmpty()) {
            //try to grab the style if provided
            //a style has been given for this layer, try to use it
            style = StyleProviderProxy.getInstance().get(styleName);
            if (style == null) {
                throw new CstlServiceException("Style provided not found.", STYLE_NOT_DEFINED);
            }
        } else {
            //no defined styles, use the favorite one, let the layer get it himself.
            style = null;
        }
        return style;
    }
    
    protected static MutableStyle getStyleByIdentifier(final String styleName) throws CstlServiceException {
        final MutableStyle style;
        if (styleName != null && !styleName.isEmpty()) {
            //try to grab the style if provided
            //a style has been given for this layer, try to use it
            style = StyleProviderProxy.getInstance().getByIdentifier(styleName);
            if (style == null) {
                throw new CstlServiceException("Style provided not found.", STYLE_NOT_DEFINED);
            }
        } else {
            //no defined styles, use the favorite one, let the layer get it himself.
            style = null;
        }
        return style;
    }

    
    protected Layer getMainLayer() {
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

    @Override
    protected String getProperty(final String key) {
        if (layerContext != null && layerContext.getCustomParameters() != null) {
            return layerContext.getCustomParameters().get(key);
        }
        return null;
    }
}
