/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.map.featureinfo;

import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.GFIParam;
import org.constellation.configuration.GetFeatureInfoCfg;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.coveragesgroup.CoveragesGroupProvider;
import org.constellation.provider.coveragesgroup.util.ConvertersJaxbToGeotk;
import org.geotoolkit.display2d.GraphicVisitor;
import org.geotoolkit.display.canvas.RenderingContext;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display.SearchArea;
import org.geotoolkit.display2d.primitive.ProjectedObject;
import org.geotoolkit.display2d.service.*;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.ows.xml.GetFeatureInfo;

import org.apache.sis.util.logging.Logging;
import org.opengis.display.primitive.Graphic;
import org.opengis.feature.type.Name;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FeatureInfoFormat designed to be used with a CoverageGroup provider.
 * Configuration must be contain the {@link #PROVIDER_ID_PARAM} parameter filled with
 * layer provider id.
 * AggregateFeatureInfoFormat will check the CoverageGroup provider configuration to see if
 * layers have some custom GetFeatureInfo configured. In that case the {@link #getSupportedMimeTypes()}
 * method will return all configured mimeTypes.
 *
 * @author Quentin Boileau (Geomatys)
 */
public class AggregateFeatureInfoFormat extends AbstractFeatureInfoFormat {

    public static final Logger LOGGER = Logging.getLogger(AggregateFeatureInfoFormat.class);
    public static final String PROVIDER_ID_PARAM = "providerID";

    private Map<String, List<GetFeatureInfoCfg>> layersConfig = null;
    private FeatureInfoFormat subFormat = null;

    @Override
    public List<String> getSupportedMimeTypes() {
        try {
            if (layersConfig == null) {
                if (!ckeckConfiguration()) return new ArrayList<String>();
            }

            final Set<String> supportedMimes = new HashSet<String>();
            for (List<GetFeatureInfoCfg> infoCfgs : layersConfig.values()) {
                for (GetFeatureInfoCfg gfiCfg : infoCfgs) {
                    if (gfiCfg.getMimeType() != null && !(gfiCfg.getMimeType().isEmpty())) {
                        supportedMimes.add(gfiCfg.getMimeType());
                    }
                }
            }

            return new ArrayList<String>(supportedMimes);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return new ArrayList<String>();
    }

    @Override
    public Object getFeatureInfo(final SceneDef sdef, final ViewDef vdef, final CanvasDef cdef,
                                 final Rectangle searchArea, final GetFeatureInfo getFI) throws PortrayalException {

        try {
            if (!ckeckConfiguration()) return null;
        } catch (ConfigurationException e) {
            throw new PortrayalException(e.getMessage(), e);
        }

        final String requestMime = getFI.getInfoFormat();

        final VisitDef visitDef = new VisitDef();
        visitDef.setArea(searchArea);
        visitDef.setVisitor(new GraphicVisitor() {

            int maxCandidat = getFeatureCount(getFI);
            int idx = 0;
            boolean stop = false;

            @Override
            public void startVisit() {
            }

            @Override
            public void endVisit() {
            }

            @Override
            public void visit(Graphic graphic, RenderingContext context, SearchArea area) {
                if(graphic == null ) return;

                if (graphic instanceof ProjectedObject) {
                    final ProjectedObject obj = (ProjectedObject) graphic;
                    final MapLayer layer = obj.getLayer();

                    final List<GetFeatureInfoCfg> gfiList = layersConfig.get(layer.getName());

                    if (gfiList != null && !(gfiList.isEmpty())) {

                        for (GetFeatureInfoCfg gfiCfg : gfiList) {
                            // mime not null and equals to request
                            if (gfiCfg.getMimeType() != null
                                    && !(gfiCfg.getMimeType().isEmpty())
                                    && gfiCfg.getMimeType().equals(requestMime)) {

                                try {
                                    subFormat = FeatureInfoUtilities.getFeatureInfoFormatFromConf(gfiCfg);
                                } catch (ClassNotFoundException e) {
                                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                                } catch (ConfigurationException e) {
                                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                                }
                                stop = true;
                            }
                        }
                    }
                }
                idx++;
            }

            @Override
            public boolean isStopRequested() {
                return !stop ? (idx == maxCandidat) : true;
            }
        });

        DefaultPortrayalService.visit(cdef,sdef,vdef,visitDef);

        if (subFormat != null) {
            return subFormat.getFeatureInfo(sdef, vdef, cdef, searchArea, getFI);
        }

        return null;
    }

    /**
     * Check the GetFeatureInfo configuration and associated CoverageGroup provider configuration as well.
     *
     * @return true if configuration is ok and false if no configuration is set.
     * @throws ConfigurationException if linked provider is not a CoverageGroup instance or his configuration unmarshalling
     * went wrong.
     */
    private boolean ckeckConfiguration() throws ConfigurationException {
        final GetFeatureInfoCfg conf = getConfiguration();
        if (conf == null) {
            //we should never be in this case.
            //throw new PortrayalException("AggregatedFeatureInfoFormat configuration not found.");
            return false;
        }

        final List<GFIParam> gfiParams = conf.getGfiParameter();
        String providerId = null;
        for (GFIParam gfiParam : gfiParams) {
            if (gfiParam.getKey().equals(PROVIDER_ID_PARAM)) {
                providerId = gfiParam.getValue();
            }
        }

        if (providerId == null || providerId.isEmpty()) {
            //throw new PortrayalException("GetFeatureInfo configuration parameter "+PROVIDER_ID_PARAM+" is not defined.");
            return false;
        }

        final DataProviders namedProxy = DataProviders.getInstance();
        final DataProvider provider = namedProxy.getProvider(providerId);
        if (provider == null || !(provider instanceof CoveragesGroupProvider)) {
            throw new ConfigurationException("GetFeatureInfo configuration parameter "+PROVIDER_ID_PARAM
                    +" doesn't reference a CoverageGroup provider.");
        }

        final CoveragesGroupProvider cgProvider = (CoveragesGroupProvider) provider;
        final Set<Name> layers = cgProvider.getKeys();

        MapContext mapContext = null;
        if (layers.size() > 0) {
            try {

                //only get the fist layer because usually CoverageGroup provider are linked to one MapContext file.
                mapContext = cgProvider.getMapContext(layers.iterator().next(), "", "");
            } catch (JAXBException e) {
                throw new ConfigurationException("Error during MapContext unmarshalling.", e.getMessage());
            }
        }

        if (mapContext == null) {
            throw new ConfigurationException("CoverageGroup provider "+PROVIDER_ID_PARAM
                    +" doesn't reference a MapContext layer.");
        }


        layersConfig = new HashMap<String, List<GetFeatureInfoCfg>>();
        final List<MapLayer> mapLayers = getMapLayers(mapContext);
        for (MapLayer layer : mapLayers) {
            final Object original = layer.getUserProperty(ConvertersJaxbToGeotk.ORIGINAL_CONFIG);
            if (original instanceof org.constellation.provider.coveragesgroup.xml.MapLayer) {
                final org.constellation.provider.coveragesgroup.xml.MapLayer cfg =
                        (org.constellation.provider.coveragesgroup.xml.MapLayer) original;
                layersConfig.put(layer.getName(), cfg.getGetFeatureInfoCfgs());
            }
        }
        return true;
    }

    /**
     * Recursively find leaf MapItem (know as MapLayer).
     * @param mapContext
     * @return
     */
    private List<MapLayer> getMapLayers(MapContext mapContext) {
        final List<MapLayer> layers = new ArrayList<MapLayer>();
        getMapLayers(mapContext, layers);
        return layers;
    }

    /**
     * Recursively find leaf MapItem (know as MapLayer).
     * @param mapItem
     * @param layers
     */
    private void getMapLayers(MapItem mapItem, List<MapLayer> layers) {

        if (mapItem instanceof MapLayer) {
            layers.add((MapLayer)mapItem);
        } else {
            for (MapItem item : mapItem.items()) {
                getMapLayers(item, layers);
            }
        }
    }
}
