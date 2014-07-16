package org.constellation.admin.dto;

import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerSummary;
import org.constellation.engine.register.MapcontextStyledLayer;

public class MapContextStyledLayerDTO extends LayerSummary implements Comparable<MapContextStyledLayerDTO> {
    private MapcontextStyledLayer mapContextStyledLayer;

    public MapContextStyledLayerDTO(final MapcontextStyledLayer mapContextStyledLayer, final Layer layer, final DataBrief db) {
        super(layer, db);
        this.mapContextStyledLayer = mapContextStyledLayer;
    }

    public MapcontextStyledLayer getMapContextStyledLayer() {
        return mapContextStyledLayer;
    }

    public void setMapContextStyledLayer(MapcontextStyledLayer mapContextStyledLayer) {
        this.mapContextStyledLayer = mapContextStyledLayer;
    }

    @Override
    public int compareTo(MapContextStyledLayerDTO o) {
        return this.getMapContextStyledLayer().getOrder() - o.getMapContextStyledLayer().getOrder();
    }
}
