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

import java.util.*;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.Languages;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.map.factory.MapFactory;
import org.constellation.map.security.LayerSecurityFilter;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.util.DataReference;
import org.constellation.ws.rs.MapUtilities;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.feature.DefaultName;
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

    protected final List<String> supportedLanguages = new ArrayList<>();

    protected final String defaultLanguage;

    private LayerSecurityFilter securityFilter;

    public LayerWorker(final String id, final Specification specification) {
        super(id, specification);
        isStarted = true;

        String defaultLanguageCandidate = null;
        LayerContext candidate          = null;
        
        try {
            final Object obj = ConfigurationEngine.getConfiguration(specification.name(), id, "layerContext.xml");
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
        } catch (FileNotFoundException ex) {
            startError = "The configuration file layerContext.xml has not been found";
            isStarted = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: " + ex.getMessage(), id);
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
    
    protected Layer getConfigurationLayer(final Name layerName, final String login) {
        if (layerName != null && layerName.getLocalPart() != null) {
            final QName qname = new QName(layerName.getNamespaceURI(), layerName.getLocalPart());
            return getConfigurationLayer(qname, login);
        }
        return null;
    }

    protected Layer getConfigurationLayer(final QName layerName, final String login) {
        if (layerName != null) {
            final List<Layer> layers = getConfigurationLayers(login);
            for (Layer layer : layers) {
                if (layer.getName().equals(layerName) || (layer.getAlias() != null && layer.getAlias().equals(layerName.getLocalPart()))) {
                    return layer;
                }
            }
        }
        return null;
    }

    protected List<QName> getConfigurationLayerNames(final String login) {
        final List<QName> result = new ArrayList<>();
        final List<Layer> layers = getConfigurationLayers(login);
        for (Layer layer : layers) {
            result.add(layer.getName());
        }
        return result;
    }

    /**
     *
     * @return map of additional informations for each layer declared in the
     * layer context.
     */
    public List<Layer> getConfigurationLayers(final String login) {
        return MapUtilities.getConfigurationLayers(layerContext, securityFilter, login);
    }

    
    /**
     * Return all layers details in LayerProviders from there names.
     * @param layerNames
     * @return a list of LayerDetails
     * @throws CstlServiceException
     */
    protected List<LayerDetails> getLayerReferences(final String login, final Collection<Name> layerNames) throws CstlServiceException {
        final List<LayerDetails> layerRefs = new ArrayList<>();
        for (Name layerName : layerNames) {
            layerRefs.add(getLayerReference(login, layerName));
        }
        return layerRefs;
    }

    protected LayerDetails getLayerReference(final String login, final QName layerName) throws CstlServiceException {
        return getLayerReference(login, new DefaultName(layerName));
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
        final NameInProvider fullName = layersContainsKey(login, layerName);
        if (fullName != null) {
            if (fullName.dataVersion != null) {
                layerRef = namedProxy.get(fullName.name, fullName.providerID, fullName.dataVersion);
            } else {
                layerRef = namedProxy.get(fullName.name, fullName.providerID);
            }
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
    protected NameInProvider layersContainsKey(final String login, final Name name) {
        if (name == null) {
            return null;
        }

        final List<QName> layerNames = getConfigurationLayerNames(login);
        if (layerNames == null) {
            return null;
        }

        final Layer directLayer = getConfigurationLayer(name, login);
        if (directLayer == null) {
            //search with only localpart
            for (QName layerName : layerNames) {
                if (layerName.getLocalPart().equals(name.getLocalPart())) {
                    final Layer layerConfig = getConfigurationLayer(layerName, login);
                    Date version = null;
                    if (layerConfig.getVersion() != null) {
                        version = new Date(layerConfig.getVersion());
                    }
                    return new NameInProvider(layerName, layerConfig.getProviderID(), version);
                }
            }

            //search in alias if any
            for (QName l : layerNames) {
                final Layer layer = getConfigurationLayer(l, login);
                if (layer.getAlias() != null && !layer.getAlias().isEmpty()) {
                    final String alias = layer.getAlias().trim().replaceAll(" ", "_");
                    if (alias.equals(name.getLocalPart())) {
                        Date version = null;
                        if (layer.getVersion() != null) {
                            version = new Date(layer.getVersion());
                        }
                        return new NameInProvider(l, layer.getProviderID(), version);
                    }
                }
            }

            return null;
        }

        Date version = null;
        if (directLayer.getVersion() != null) {
            version = new Date(directLayer.getVersion());
        }

        return new NameInProvider(directLayer.getName(), directLayer.getProviderID(), version);
    }
    
    protected static MutableStyle getStyle(final DataReference styleName) throws CstlServiceException {
        final MutableStyle style;
        if (styleName != null) {
            //try to grab the style if provided
            //a style has been given for this layer, try to use it
            style = StyleProviderProxy.getInstance().get(styleName.getLayerId().getLocalPart(), styleName.getProviderId());
            if (style == null) {
                throw new CstlServiceException("Style provided: " + styleName.getReference() + " not found.", STYLE_NOT_DEFINED);
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

    @Override
    public LayerContext getConfiguration() {
        return layerContext;
    }

    /**
     * Parse a Name from a string.
     * @param layerName
     * @return
     */
    protected Name parseCoverageName(final String layerName) {
        final Name namedLayerName;
        if (layerName != null && layerName.lastIndexOf(':') != -1) {
            final String namespace = layerName.substring(0, layerName.lastIndexOf(':'));
            final String localPart = layerName.substring(layerName.lastIndexOf(':') + 1);
            namedLayerName = new DefaultName(namespace, localPart);
        } else {
            namedLayerName = new DefaultName(layerName);
        }
        return namedLayerName;
    }
    
    public static class NameInProvider {
        public Name name;
        public String providerID;
        public Date dataVersion;
     
        public NameInProvider(final Name name, final String providerID) {
            this(name, providerID, null);
        }
        
        public NameInProvider(final QName name, final String providerID) {
            this(new DefaultName(name), providerID, null);
        }

        public NameInProvider(final QName name, final String providerID, final Date dataVersion) {
            this(new DefaultName(name), providerID, dataVersion);
        }

        public NameInProvider(final Name name, final String providerID, final Date dataVersion) {
            this.name = name;
            this.providerID = providerID;
            this.dataVersion= dataVersion;
        }
    }
}
