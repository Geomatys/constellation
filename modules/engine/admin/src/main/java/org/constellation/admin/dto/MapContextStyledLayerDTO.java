package org.constellation.admin.dto;

import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerSummary;
import org.constellation.engine.register.MapcontextStyledLayer;

public class MapContextStyledLayerDTO extends LayerSummary implements Comparable<MapContextStyledLayerDTO> {
    private Integer mapcontextId;
    private Integer layerId;
    private String serviceIdentifier;
    private String serviceVersions;
    private Integer styleId;
    private String styleName;
    private int order;
    private int opacity;
    private boolean visible;
    private String externalStyle;

    public MapContextStyledLayerDTO(final MapcontextStyledLayer mapContextStyledLayer, final Layer layer, final DataBrief db) {
        super(layer, db);
        this.mapcontextId = mapContextStyledLayer.getMapcontextId();
        this.layerId = mapContextStyledLayer.getLayerId();
        this.styleId = mapContextStyledLayer.getStyleId();
        this.order = mapContextStyledLayer.getLayerOrder();
        this.opacity = mapContextStyledLayer.getLayerOpacity();
        this.visible = mapContextStyledLayer.isLayerVisible();
        this.externalStyle = mapContextStyledLayer.getExternalStyle();
    }

    public Integer getMapcontextId() {
        return mapcontextId;
    }

    public void setMapcontextId(Integer mapcontextId) {
        this.mapcontextId = mapcontextId;
    }

    public Integer getLayerId() {
        return layerId;
    }

    public void setLayerId(Integer layerId) {
        this.layerId = layerId;
    }

    public Integer getStyleId() {
        return styleId;
    }

    public void setStyleId(Integer styleId) {
        this.styleId = styleId;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getExternalStyle() {
        return externalStyle;
    }

    public void setExternalStyle(String externalStyle) {
        this.externalStyle = externalStyle;
    }

    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    public String getServiceVersions() {
        return serviceVersions;
    }

    public void setServiceVersions(String serviceVersions) {
        this.serviceVersions = serviceVersions;
    }
    @Override
    public int compareTo(MapContextStyledLayerDTO o) {
        return getOrder() - o.getOrder();
    }
}
