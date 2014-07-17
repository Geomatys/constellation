package org.constellation.admin;

import org.constellation.admin.dto.MapContextLayersDTO;
import org.constellation.admin.dto.MapContextStyledLayerDTO;
import org.constellation.configuration.DataBrief;
import org.constellation.engine.register.*;
import org.constellation.engine.register.repository.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class MapContextBusiness {

    @Inject
    private MapContextRepository mapContextRepository;

    @Inject
    private LayerRepository layerRepository;

    @Inject
    private DataRepository dataRepository;

    @Inject
    private DataBusiness dataBusiness;

    @Inject
    private ProviderRepository providerRepository;

    @Inject
    private StyleRepository styleRepository;

    public void setMapItems(final int contextId, final List<MapcontextStyledLayer> layers) {
        mapContextRepository.setLinkedLayers(contextId, layers);
    }

    public List<MapContextLayersDTO> findAllMapContextLayers() {
        final List<MapContextLayersDTO> ctxtLayers = new ArrayList<>();
        final List<Mapcontext> ctxts = mapContextRepository.findAll();
        for (final Mapcontext ctxt : ctxts) {
            final List<MapcontextStyledLayer> styledLayers = mapContextRepository.getLinkedLayers(ctxt.getId());
            final List<MapContextStyledLayerDTO> styledLayersDto = new ArrayList<>();

            for (final MapcontextStyledLayer styledLayer : styledLayers) {
                final org.constellation.engine.register.Layer layer = layerRepository.findById(styledLayer.getLayerId());
                final Data data = dataRepository.findById(layer.getData());
                final Provider provider  = providerRepository.findOne(data.getProvider());
                final QName name         = new QName(layer.getNamespace(), layer.getName());

                final org.constellation.configuration.Layer layerConfig = new org.constellation.configuration.Layer(layer.getId(), name);
                layerConfig.setAlias(layer.getAlias());
                layerConfig.setDate(new Date(layer.getDate()));
                layerConfig.setOwner(layer.getOwner());
                layerConfig.setProviderID(provider.getIdentifier());
                layerConfig.setProviderType(provider.getType());

                final QName dataName = new QName(data.getNamespace(), data.getName());
                final DataBrief db = dataBusiness.getDataBrief(dataName, provider.getId());
                final MapContextStyledLayerDTO dto = new MapContextStyledLayerDTO(styledLayer, layerConfig , db);
                if (styledLayer.getStyleId() != null) {
                    final Style style = styleRepository.findById(styledLayer.getStyleId());
                    if (style != null) {
                        dto.setStyleName(style.getName());
                    }
                }

                styledLayersDto.add(dto);
            }
            Collections.sort(styledLayersDto);
            ctxtLayers.add(new MapContextLayersDTO(ctxt, styledLayersDto));
        }
        return ctxtLayers;
    }
}
