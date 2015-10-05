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
package org.constellation.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.extent.DefaultExtent;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.dto.MapContextLayersDTO;
import org.constellation.admin.dto.MapContextStyledLayerDTO;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IMapContextBusiness;
import org.constellation.configuration.DataBrief;
import org.constellation.dto.ParameterValues;
import org.constellation.database.api.jooq.tables.pojos.CstlUser;
import org.constellation.database.api.jooq.tables.pojos.Data;
import org.constellation.database.api.jooq.tables.pojos.Dataset;
import org.constellation.database.api.jooq.tables.pojos.Layer;
import org.constellation.database.api.jooq.tables.pojos.Mapcontext;
import org.constellation.database.api.jooq.tables.pojos.MapcontextStyledLayer;
import org.constellation.database.api.jooq.tables.pojos.Provider;
import org.constellation.database.api.jooq.tables.pojos.Service;
import org.constellation.database.api.jooq.tables.pojos.Style;
import org.constellation.database.api.jooq.tables.pojos.StyledLayer;
import org.constellation.database.api.repository.DataRepository;
import org.constellation.database.api.repository.LayerRepository;
import org.constellation.database.api.repository.MapContextRepository;
import org.constellation.database.api.repository.ProviderRepository;
import org.constellation.database.api.repository.ServiceRepository;
import org.constellation.database.api.repository.StyleRepository;
import org.constellation.database.api.repository.StyledLayerRepository;
import org.constellation.database.api.repository.UserRepository;
import org.constellation.util.DataReference;
import org.geotoolkit.referencing.CRS;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.Identification;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import org.constellation.business.IMetadataBusiness;

@Component
@Primary
public class MapContextBusiness implements IMapContextBusiness {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.admin");

    @Inject
    private MapContextRepository mapContextRepository;

    @Inject
    private LayerRepository layerRepository;

    @Inject
    private DataRepository dataRepository;

    @Inject
    private IMetadataBusiness metadataBusiness;

    @Inject
    private IDataBusiness dataBusiness;

    @Inject
    private IDatasetBusiness datasetBusiness;

    @Inject
    private ProviderRepository providerRepository;

    @Inject
    private StyleRepository styleRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private StyledLayerRepository styledLayerRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    @Transactional
    public void setMapItems(final int contextId, final List<MapcontextStyledLayer> layers) {
        mapContextRepository.setLinkedLayers(contextId, layers);
    }

    @Override
    @Transactional
    public Mapcontext create(final MapContextLayersDTO mapContext) {
        return mapContextRepository.create(mapContext);
    }

    @Override
    public List<MapContextLayersDTO> findAllMapContextLayers() {
        final List<MapContextLayersDTO> ctxtLayers = new ArrayList<>();
        final List<Mapcontext> ctxts = mapContextRepository.findAll();
        for (final Mapcontext ctxt : ctxts) {
            final List<MapcontextStyledLayer> styledLayers = mapContextRepository.getLinkedLayers(ctxt.getId());
            final List<MapContextStyledLayerDTO> styledLayersDto = generateLayerDto(styledLayers);
            final MapContextLayersDTO mapcontext = new MapContextLayersDTO(ctxt, styledLayersDto);

            //getOwner and set userName to pojo.
            final Optional<CstlUser> user = userRepository.findById(ctxt.getOwner());
            if (user != null && user.isPresent()) {
                final CstlUser cstlUser = user.get();
                if(cstlUser!=null){
                    mapcontext.setUserOwner(cstlUser.getLogin());
                }
            }
            ctxtLayers.add(mapcontext);
        }
        return ctxtLayers;
    }

    @Override
    public MapContextLayersDTO findMapContextLayers(int contextId) {
        final Mapcontext ctxt = mapContextRepository.findById(contextId);
        final List<MapcontextStyledLayer> styledLayers = mapContextRepository.getLinkedLayers(contextId);
        final List<MapContextStyledLayerDTO> styledLayersDto = generateLayerDto(styledLayers);
        return new MapContextLayersDTO(ctxt, styledLayersDto);
    }

    @Override
    public String findStyleName(Integer styleId) {
        return styleRepository.findById(styleId).getName();
    }

    private List<MapContextStyledLayerDTO> generateLayerDto(final List<MapcontextStyledLayer> styledLayers) {
        final List<MapContextStyledLayerDTO> styledLayersDto = new ArrayList<>();
        for (final MapcontextStyledLayer styledLayer : styledLayers) {
            final MapContextStyledLayerDTO dto;
            final Integer layerID = styledLayer.getLayerId();
            final Integer dataID = styledLayer.getDataId();
            if (layerID != null) {
                final Layer layer = layerRepository.findById(layerID);
                final Data data = dataRepository.findById(layer.getData());
                final Provider provider = providerRepository.findOne(data.getProvider());
                final QName name = new QName(layer.getNamespace(), layer.getName());

                final org.constellation.configuration.Layer layerConfig = new org.constellation.configuration.Layer(layer.getId(), name);
                layerConfig.setAlias(layer.getAlias());
                layerConfig.setDate(new Date(layer.getDate()));
                layerConfig.setOwner(layer.getOwner());
                layerConfig.setProviderID(provider.getIdentifier());
                layerConfig.setProviderType(provider.getType());

                final List<StyledLayer> styledLays = styledLayerRepository.findByLayer(layer.getId());
                final List<DataReference> drs = new ArrayList<>();
                for (final StyledLayer styledLay : styledLays) {
                    final Style s = styleRepository.findById(styledLay.getStyle());
                    if (s == null) {
                        continue;
                    }
                    final DataReference dr = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "sld", s.getName());
                    drs.add(dr);
                }
                layerConfig.setStyles(drs);

                final QName dataName = new QName(data.getNamespace(), data.getName());
                final DataBrief db = dataBusiness.getDataBrief(dataName, provider.getId());
                dto = new MapContextStyledLayerDTO(styledLayer, layerConfig, db);

                if (styledLayer.getStyleId() != null) {
                    // Extract style information for this layer
                    final Style style = styleRepository.findById(styledLayer.getStyleId());
                    if (style != null) {
                        dto.setStyleName(style.getName());
                    }
                }

                // Extract service information for this layer
                final Layer layerRecord = layerRepository.findById(styledLayer.getLayerId());
                final Service serviceRecord = serviceRepository.findById(layerRecord.getService());
                dto.setServiceIdentifier(serviceRecord.getIdentifier());
                dto.setServiceVersions(serviceRecord.getVersions());
            } else if (dataID != null) {
                final Data data = dataRepository.findById(dataID);
                final Provider provider = providerRepository.findOne(data.getProvider());
                final QName dataName = new QName(data.getNamespace(), data.getName());
                final DataBrief db = dataBusiness.getDataBrief(dataName, provider.getId());
                final org.constellation.configuration.Layer layerConfig = new org.constellation.configuration.Layer(styledLayer.getLayerId(), dataName);
                layerConfig.setAlias(data.getName());
                layerConfig.setDate(new Date(data.getDate()));
                layerConfig.setOwner(data.getOwner());
                layerConfig.setProviderID(provider.getIdentifier());
                layerConfig.setProviderType(provider.getType());

                // Fill styles
                final List<Integer> stylesIds = styleRepository.getStyleIdsForData(dataID);
                final List<DataReference> drs = new ArrayList<>();
                for (final Integer styleId : stylesIds) {
                    final Style s = styleRepository.findById(styleId);
                    if (s == null) {
                        continue;
                    }
                    final DataReference dr = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "sld", s.getName());
                    drs.add(dr);
                }
                layerConfig.setStyles(drs);

                dto = new MapContextStyledLayerDTO(styledLayer, layerConfig, db);

                if (styledLayer.getStyleId() != null) {
                    // Extract style information for this layer
                    final Style style = styleRepository.findById(styledLayer.getStyleId());
                    if (style != null) {
                        dto.setStyleName(style.getName());
                    }
                }

            } else{
                dto = new MapContextStyledLayerDTO(styledLayer);
            }

            styledLayersDto.add(dto);
        }
        Collections.sort(styledLayersDto);
        return styledLayersDto;
    }

    /**
     * Get the extent of all included layers in this map context.
     *
     * @param contextId Context identifier
     * @return
     * @throws FactoryException
     */
    @Override
    public ParameterValues getExtent(int contextId) throws FactoryException {
        final ParameterValues values = new ParameterValues();
        final Mapcontext context = mapContextRepository.findById(contextId);
        GeneralEnvelope env = null;
        if (context.getWest() != null && context.getSouth() != null && context.getEast() != null && context.getNorth() != null && context.getCrs() != null) {
            final CoordinateReferenceSystem crs = CRS.decode(context.getCrs(), true);
            env = new GeneralEnvelope(crs);
            env.setRange(0, context.getWest(), context.getEast());
            env.setRange(1, context.getSouth(), context.getNorth());
        }

        final List<MapcontextStyledLayer> styledLayers = mapContextRepository.getLinkedLayers(contextId);
        env = getEnvelopeForLayers(styledLayers, env);

        if (env == null) {
            return null;
        }

        final HashMap<String,String> vals = new HashMap<>();
        vals.put("crs", (context.getCrs() != null && !context.getCrs().isEmpty()) ? context.getCrs() : "CRS:84");
        vals.put("west", String.valueOf(env.getLower(0)));
        vals.put("east", String.valueOf(env.getUpper(0)));
        vals.put("south", String.valueOf(env.getLower(1)));
        vals.put("north", String.valueOf(env.getUpper(1)));
        values.setValues(vals);
        return values;
    }

    /**
     * Get the extent for the given layers.
     *
     * @param styledLayers Layers to consider.
     * @return
     * @throws FactoryException
     */
    @Override
    public ParameterValues getExtentForLayers(final List<MapcontextStyledLayer> styledLayers) throws FactoryException {
        final GeneralEnvelope env = getEnvelopeForLayers(styledLayers, null);

        if (env == null) {
            return null;
        }

        final ParameterValues values = new ParameterValues();
        final HashMap<String,String> vals = new HashMap<>();
        vals.put("crs", "CRS:84");
        vals.put("west", String.valueOf(env.getLower(0)));
        vals.put("east", String.valueOf(env.getUpper(0)));
        vals.put("south", String.valueOf(env.getLower(1)));
        vals.put("north", String.valueOf(env.getUpper(1)));
        values.setValues(vals);
        return values;
    }

    private GeneralEnvelope getEnvelopeForLayers(final List<MapcontextStyledLayer> styledLayers,
                                                 final GeneralEnvelope ctxtEnv) throws FactoryException {
        GeneralEnvelope env = ctxtEnv;
        for (final MapcontextStyledLayer styledLayer : styledLayers) {
            if (!styledLayer.getLayerVisible()) {
                continue;
            }
            Integer layerID = styledLayer.getLayerId();
            Integer dataID = styledLayer.getDataId();
            if (layerID != null || dataID != null) {
                if(dataID == null) {
                    final Layer layerRecord = layerRepository.findById(layerID);
                    dataID = layerRecord.getData();
                }
                DefaultMetadata metadata = null;
                if(dataID != null) {
                    try {
                        metadata = metadataBusiness.getIsoMetadataForData(dataID);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    }
                }
                if(metadata == null && dataID != null) {
                    //try to get dataset metadata.
                    final Dataset dataset = dataBusiness.getDatasetForData(dataID);
                    if (dataset != null) {
                        try{
                            metadata = datasetBusiness.getMetadata(dataset.getIdentifier());
                        }catch(Exception ex){
                            //skip for this layer
                            continue;
                        }
                    }
                }
                if (metadata == null || metadata.getIdentificationInfo() == null || metadata.getIdentificationInfo().isEmpty()) {
                    continue;
                }
                final Identification identification = metadata.getIdentificationInfo().iterator().next();
                if (!(identification instanceof DataIdentification)) {
                    continue;
                }
                final Collection<? extends Extent> extents = ((DataIdentification) identification).getExtents();
                if (extents == null || extents.isEmpty()) {
                    continue;
                }
                final DefaultExtent extent = (DefaultExtent) extents.iterator().next();
                if (extent.getGeographicElements() == null || extent.getGeographicElements().isEmpty()) {
                    continue;
                }
                final GeographicBoundingBox geoBBox = (GeographicBoundingBox) extent.getGeographicElements().iterator().next();

                final GeneralEnvelope tempEnv = new GeneralEnvelope(CRS.decode("CRS:84"));
                tempEnv.setRange(0, geoBBox.getWestBoundLongitude(), geoBBox.getEastBoundLongitude());
                tempEnv.setRange(1, geoBBox.getSouthBoundLatitude(), geoBBox.getNorthBoundLatitude());
                if (env == null) {
                    env = tempEnv;
                } else {
                    env.add(tempEnv);
                }
            } else {
                final String extLayerExtent = styledLayer.getExternalLayerExtent();
                if (extLayerExtent != null && !extLayerExtent.isEmpty()) {
                    final String[] layExtent = extLayerExtent.split(",");
                    final GeneralEnvelope tempEnv = new GeneralEnvelope(CRS.decode("CRS:84"));
                    tempEnv.setRange(0, Double.parseDouble(layExtent[0]), Double.parseDouble(layExtent[2]));
                    tempEnv.setRange(1, Double.parseDouble(layExtent[1]), Double.parseDouble(layExtent[3]));
                    if (env == null) {
                        env = tempEnv;
                    } else {
                        env.add(tempEnv);
                    }
                }
            }
        }
        return env;
    }
}
