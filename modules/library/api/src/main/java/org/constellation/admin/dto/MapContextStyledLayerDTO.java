package org.constellation.admin.dto;

import java.util.Collections;

import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerSummary;
import org.constellation.configuration.StyleBrief;
import org.constellation.engine.register.jooq.tables.pojos.MapcontextStyledLayer;

public class MapContextStyledLayerDTO extends LayerSummary implements Comparable<MapContextStyledLayerDTO> {
    private Integer id;
    private Integer mapcontextId;
    private Integer layerId;
    private Integer dataId;
    private String serviceIdentifier;
    private String serviceVersions;
    private Integer styleId;
    private String styleName;
    private int order;
    private int opacity;
    private boolean visible;
    private boolean iswms;
    private String externalStyle;
    private String externalServiceUrl;
    private String externalServiceVersion;
    private String externalLayer;
    private String externalLayerExtent;

    /**
     * Default constructor needed by jackson when dealing with json.
     */
    public MapContextStyledLayerDTO(){
        super();
    }

    public MapContextStyledLayerDTO(final MapcontextStyledLayer mapContextStyledLayer) {
        super();
        this.id = mapContextStyledLayer.getId();
        this.mapcontextId = mapContextStyledLayer.getMapcontextId();
        this.layerId = mapContextStyledLayer.getLayerId();
        this.styleId = mapContextStyledLayer.getStyleId();
        this.order = mapContextStyledLayer.getLayerOrder();
        this.opacity = mapContextStyledLayer.getLayerOpacity();
        this.visible = mapContextStyledLayer.getLayerVisible();
        this.externalStyle = mapContextStyledLayer.getExternalStyle();
        this.externalServiceUrl = mapContextStyledLayer.getExternalServiceUrl();
        this.externalServiceVersion = mapContextStyledLayer.getExternalServiceVersion();
        this.externalLayer = mapContextStyledLayer.getExternalLayer();
        this.externalLayerExtent = mapContextStyledLayer.getExternalLayerExtent();
        this.dataId = mapContextStyledLayer.getDataId();
        this.iswms = mapContextStyledLayer.getIswms();

        super.setName(externalLayer);
        super.setAlias(externalLayer);
        final StyleBrief style = new StyleBrief();
        style.setName(externalStyle);
        style.setTitle(externalStyle);
        super.setTargetStyle(Collections.singletonList(style));
    }

    public MapContextStyledLayerDTO(final MapcontextStyledLayer mapContextStyledLayer, final Layer layer, final DataBrief db) {
        super(layer, db);
        this.id = mapContextStyledLayer.getId();
        this.mapcontextId = mapContextStyledLayer.getMapcontextId();
        this.layerId = mapContextStyledLayer.getLayerId();
        this.styleId = mapContextStyledLayer.getStyleId();
        this.order = mapContextStyledLayer.getLayerOrder();
        this.opacity = mapContextStyledLayer.getLayerOpacity();
        this.visible = mapContextStyledLayer.getLayerVisible();
        this.externalStyle = mapContextStyledLayer.getExternalStyle();
        this.externalServiceUrl = mapContextStyledLayer.getExternalServiceUrl();
        this.externalServiceVersion = mapContextStyledLayer.getExternalServiceVersion();
        this.externalLayer = mapContextStyledLayer.getExternalLayer();
        this.externalLayerExtent = mapContextStyledLayer.getExternalLayerExtent();
        this.dataId = mapContextStyledLayer.getDataId();
        this.iswms = mapContextStyledLayer.getIswms();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getExternalServiceUrl() {
        return externalServiceUrl;
    }

    public void setExternalServiceUrl(String externalServiceUrl) {
        this.externalServiceUrl = externalServiceUrl;
    }

    public String getExternalServiceVersion() {
        return externalServiceVersion;
    }

    public void setExternalServiceVersion(String externalServiceVersion) {
        this.externalServiceVersion = externalServiceVersion;
    }

    public String getExternalLayer() {
        return externalLayer;
    }

    public void setExternalLayer(String externalLayer) {
        this.externalLayer = externalLayer;
    }

    public String getExternalLayerExtent() {
        return externalLayerExtent;
    }

    public void setExternalLayerExtent(String externalLayerExtent) {
        this.externalLayerExtent = externalLayerExtent;
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

    public Integer getDataId() {
        return dataId;
    }

    public void setDataId(Integer dataId) {
        this.dataId = dataId;
    }

    public boolean isIswms() {
        return iswms;
    }

    public void setIswms(boolean iswms) {
        this.iswms = iswms;
    }

    public MapcontextStyledLayer getMapcontextStyledLayer() {
        return new MapcontextStyledLayer(this.id, this.mapcontextId, this.layerId,
                this.styleId, this.order, this.opacity, this.visible,this.externalLayer,
                this.externalLayerExtent, this.externalServiceUrl, this.externalServiceVersion,
                this.externalStyle, this.iswms, this.dataId);
    }

    @Override
    public int compareTo(MapContextStyledLayerDTO o) {
        return getOrder() - o.getOrder();
    }
}
