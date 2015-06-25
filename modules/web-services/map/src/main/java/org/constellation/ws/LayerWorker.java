/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.ws;

import org.constellation.ServiceDef.Specification;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IStyleBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Language;
import org.constellation.configuration.Languages;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.provider.Data;
import org.constellation.provider.DataProviders;
import org.constellation.util.DataReference;
import org.constellation.ws.security.SimplePDP;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.style.MutableStyle;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.geotoolkit.feature.type.NamesExt;

import static org.geotoolkit.ows.xml.OWSExceptionCode.LAYER_NOT_DEFINED;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.STYLE_NOT_DEFINED;
import org.opengis.util.GenericName;

/**
 * A super class for all the web service worker dealing with layers (WMS, WCS, WMTS, WFS, ...)
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class LayerWorker extends AbstractWorker {

    @Inject
    private ILayerBusiness layerBusiness;

    @Inject
    protected IStyleBusiness styleBusiness;

    private LayerContext layerContext;

    protected final List<String> supportedLanguages = new ArrayList<>();

    protected final String defaultLanguage;

    public LayerWorker(final String id, final Specification specification) {
        super(id, specification);
        isStarted = true;

        String defaultLanguageCandidate = null;
        
        try {
            final Object obj = serviceBusiness.getConfiguration(specification.name().toLowerCase(), id);
            if (obj instanceof LayerContext) {
                layerContext = (LayerContext) obj;
                final String sec = layerContext.getSecurity();
                // Instantiaties the PDP only if a rule has been discovered.
                if (sec != null && !sec.isEmpty()) {
                    pdp = new SimplePDP(sec);
                }
                final Languages languages = layerContext.getSupportedLanguages();
                if (languages != null) {
                    for (Language language : languages.getLanguages()) {
                        supportedLanguages.add(language.getLanguageCode());
                        if (language.getDefault()) {
                            defaultLanguageCandidate = language.getLanguageCode();
                        }
                    }
                }
                // look for shiro accessibility
                final String sa = getProperty("shiroAccessible");
                if (sa != null && !sa.isEmpty()) {
                    shiroAccessible = Boolean.parseBoolean(sa);
                }
                // look for capabilities cache flag
                final String cc = getProperty("cacheCapabilities");
                if (cc != null && !cc.isEmpty()) {
                    cacheCapabilities = Boolean.parseBoolean(cc);
                }

                //Check  FeatureInfo configuration (if exist)
                FeatureInfoUtilities.checkConfiguration(layerContext);

                applySupportedVersion();
            } else {
                startError = "The layer context File does not contain a layerContext object";
                isStarted  = false;
                LOGGER.log(Level.WARNING, startError);
            }
        } catch (FactoryNotFoundException ex) {
            startError = ex.getMessage();
            isStarted  = false;
            LOGGER.log(Level.WARNING, startError, ex);
        } catch (ClassNotFoundException | ConfigurationException ex) {
            startError = "Custom FeatureInfo configuration error : " + ex.getMessage();
            isStarted  = false;
            LOGGER.log(Level.WARNING, startError, ex);
        } catch (CstlServiceException ex) {
            startError = "Error applying supported versions : " + ex.getMessage();
            isStarted  = false;
            LOGGER.log(Level.WARNING, startError, ex);
        }

        defaultLanguage = defaultLanguageCandidate;

        //listen to changes on the providers to clear the getcapabilities cache
        DataProviders.getInstance().addPropertyListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refreshUpdateSequence();
                clearCapabilitiesCache();
            }
        });
    }
    
    @PostConstruct
    public void init(){
        
    }

    protected List<Layer> getConfigurationLayers(final String login, final List<GenericName> layerNames) {
        final List<Layer> layerConfigs = new ArrayList<>();
        for (GenericName layerName : layerNames) {
            Layer l = getConfigurationLayer(layerName, login);
            layerConfigs.add(l);
        }
        return layerConfigs;
    }
    
    protected Layer getConfigurationLayer(final GenericName layerName, final String login) {
        if (layerName != null && layerName.tip().toString()!= null) {
            final QName qname = new QName(NamesExt.getNamespace(layerName), layerName.tip().toString());
            return getConfigurationLayer(qname, login);
        }
        return null;
    }

    protected Layer getConfigurationLayer(final QName layerName, final String login) {

        try {
            return layerBusiness.getLayer(this.specification.name(), getId(), layerName.getLocalPart(), layerName.getNamespaceURI(), login);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.FINE, "No layer is exactly named as queried. Search using alias will start now", e);
        }

        if (layerName != null) {
            final List<Layer> layers = getConfigurationLayers(login);
            for (Layer layer : layers) {
                if (layer.getName().equals(layerName) || (layer.getAlias() != null && layer.getAlias().equals(layerName.getLocalPart()))) {
                    return layer;
                }
            }
            // we do a second round with missing namespace search
            for (Layer layer : layers) {
                if (layer.getName().getLocalPart().equals(layerName.getLocalPart())) {
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
     * @param login
     * 
     * @return map of additional informations for each layer declared in the
     * layer context.
     */
    public List<Layer> getConfigurationLayers(final String login) {
        try {
            return layerBusiness.getLayers(this.specification.name().toLowerCase(), getId(), login);
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Error while getting layers", ex);
        }
        return new ArrayList<>();
    }

    
    /**
     * Return all layers details in LayerProviders from there names.
     * @param login
     * @param layerNames
     * @return a list of LayerDetails
     * @throws CstlServiceException
     */
    protected List<Data> getLayerReferences(final String login, final Collection<GenericName> layerNames) throws CstlServiceException {
        final List<Data> layerRefs = new ArrayList<>();
        for (GenericName layerName : layerNames) {
            layerRefs.add(getLayerReference(login, layerName));
        }
        return layerRefs;
    }

    protected Data getLayerReference(final Layer layer) throws CstlServiceException {
        return DataProviders.getInstance().get(NamesExt.create(layer.getName()), layer.getProviderID(), layer.getDate());
    }

    protected Data getLayerReference(final String login, final QName layerName) throws CstlServiceException {
        return getLayerReference(login, NamesExt.create(layerName));
    }

    /**
     * Search layer real name and return the LayerDetails from LayerProvider.
     * @param login
     * @param layerName
     * @return a LayerDetails
     * @throws CstlServiceException
     */
    protected Data getLayerReference(final String login, final GenericName layerName) throws CstlServiceException {
        final Data layerRef;
        final DataProviders namedProxy = DataProviders.getInstance();
        final NameInProvider fullName = layersContainsKey(login, layerName);
        if (fullName != null) {
            if (fullName.dataVersion != null) {
                layerRef = namedProxy.get(fullName.name, fullName.providerID, fullName.dataVersion);
            } else {
                layerRef = namedProxy.get(fullName.name, fullName.providerID);
            }
            if (layerRef == null) {
                throw new CstlServiceException("Unable to find  the Layer named:{"+NamesExt.getNamespace(layerName) + '}' + layerName.tip().toString()+ " in the provider proxy", NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("Unknown Layer name:" + layerName, LAYER_NOT_DEFINED);
        }
        return layerRef;
    }

    /**
     * We can't use directly layers.containsKey because it may miss the namespace or the alias.
     * @param login
     * @param name
     */
    protected NameInProvider layersContainsKey(final String login, final GenericName name) {
        if (name == null) {
            return null;
        }

        final Layer directLayer = getConfigurationLayer(name, login);
        if (directLayer == null) {

            final List<QName> layerNames = getConfigurationLayerNames(login);
            if (layerNames == null) {
                return null;
            }

            //search with only localpart
            for (QName layerName : layerNames) {
                if (layerName.getLocalPart().equals(name.tip().toString())) {
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
                    final String alias = layer.getAlias();
                    if (alias.equals(name.tip().toString())) {
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
    
    protected MutableStyle getStyle(final DataReference styleReference) throws CstlServiceException {
        MutableStyle style;
        if (styleReference != null) {
            try {
                style = styleBusiness.getStyle(styleReference.getProviderId(), styleReference.getLayerId().tip().toString());
            } catch (TargetNotFoundException e) {
                throw new CstlServiceException("Style provided: " + styleReference.getReference() + " not found.", STYLE_NOT_DEFINED);
            }
        } else {
            //no defined styles, use the favorite one, let the layer get it himself.
            style = null;
        }
//        final MutableStyle style;
//        if (styleName != null) {
//            //try to grab the style if provided
//            //a style has been given for this layer, try to use it
//            style = StyleProviders.getInstance().get(styleName.getLayerId().getLocalPart(), styleName.getProviderId());
//            if (style == null) {
//                throw new CstlServiceException("Style provided: " + styleName.getReference() + " not found.", STYLE_NOT_DEFINED);
//            }
//        } else {
//            //no defined styles, use the favorite one, let the layer get it himself.
//            style = null;
//        }
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
    public boolean isAuthorized(final String ip, final String referer) {
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
    protected final String getProperty(final String key) {
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
    protected GenericName parseCoverageName(final String layerName) {
        final GenericName namedLayerName;
        if (layerName != null && layerName.lastIndexOf(':') != -1) {
            final String namespace = layerName.substring(0, layerName.lastIndexOf(':'));
            final String localPart = layerName.substring(layerName.lastIndexOf(':') + 1);
            namedLayerName = NamesExt.create(namespace, localPart);
        } else {
            namedLayerName = NamesExt.create(layerName);
        }
        return namedLayerName;
    }
    
    public static class NameInProvider {
        public GenericName name;
        public String providerID;
        public Date dataVersion;
     
        public NameInProvider(final GenericName name, final String providerID) {
            this(name, providerID, null);
        }
        
        public NameInProvider(final QName name, final String providerID) {
            this(NamesExt.create(name), providerID, null);
        }

        public NameInProvider(final QName name, final String providerID, final Date dataVersion) {
            this(NamesExt.create(name), providerID, dataVersion);
        }

        public NameInProvider(final GenericName name, final String providerID, final Date dataVersion) {
            this.name = name;
            this.providerID = providerID;
            this.dataVersion= dataVersion;
        }
    }
}
