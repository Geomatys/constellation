/*
 * Constellation - An open source and standard compliant SDI
 *      http://www.constellation-sdi.org
 *   (C) 2009-2014, Geomatys
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details..
 */
DataViewer = {
    map : undefined,

    format : new OpenLayers.Format.WMSCapabilities(),

    layers : undefined,

    sensorClicked : undefined,

    initMap : function(mapId){
        if (DataViewer.map) {
            DataViewer.map.destroy();
            DataViewer.sensorClicked = undefined;
        }
        DataViewer.map = new OpenLayers.Map(mapId, {
            controls: [new OpenLayers.Control.Navigation()],
            projection: new OpenLayers.Projection('CRS:84'),
            maxExtent: new OpenLayers.Bounds(-180, -90, 180, 90),
            fractionalZoom: true,
            allOverlays:true,
            eventListeners: {
                featureover: function(e) {
                    e.feature.renderIntent = "select";
                    e.feature.layer.drawFeature(e.feature);
                },
                featureout: function(e) {
                    if (DataViewer.sensorClicked && DataViewer.sensorClicked.sensorName === e.feature.sensorName) {
                        return;
                    }

                    e.feature.renderIntent = "default";
                    e.feature.layer.drawFeature(e.feature);
                },
                featureclick: function(e) {
                    if (DataViewer.sensorClicked) {
                        // Unselect last sensor
                        DataViewer.sensorClicked.renderIntent = "default";
                        DataViewer.sensorClicked.layer.drawFeature(DataViewer.sensorClicked);
                    }
                    DataViewer.sensorClicked = e.feature;
                }
            }
        });
        DataViewer.map.addLayers(DataViewer.layers);
        DataViewer.map.zoomToMaxExtent();
        DataViewer.map.updateSize();
    },

    createLayer : function(cstlUrlPrefix, layerName, providerId, filter){
        var layer = new OpenLayers.Layer.WMS(layerName,
           cstlUrlPrefix +'api/1/portrayal/portray',
            {
                layers:      layerName,
                provider:    providerId,
                version:     '1.3.0',
                sld_version: '1.1.0',
                format:      'image/png',
                CQLFILTER:   filter
            },
            {
                ratio: 1,
                isBaseLayer: true,
                singleTile: true,
                transitionEffect: 'resize'
            }
        );
        return layer;
    },

    createLayerWithStyle : function(cstlUrlPrefix, layerName, providerId, style, filter){
        var layer = new OpenLayers.Layer.WMS(layerName,
                cstlUrlPrefix +'api/1/portrayal/portray/style',
            {
                layers:      layerName,
                provider:    providerId,
                version:     '1.3.0',
                sld_version: '1.1.0',
                format:      'image/png',
                SLDID:        style,
                SLDPROVIDER:  "sld",
                CQLFILTER:    filter
            },
            {
                ratio: 1,
                isBaseLayer: true,
                singleTile: true,
                transitionEffect: 'resize'
            }
        );
        return layer;
    },

    createLayerWMS : function(cstlUrlPrefix, layerName, instance){
        var layer = new OpenLayers.Layer.WMS(layerName,
            cstlUrlPrefix +'WS/wms/'+ instance,
            {
                request:     'GetMap',
                layers:      layerName,
                version:     '1.3.0',
                sld_version: '1.1.0',
                format:      'image/png'
            },
            {
                ratio: 1,
                isBaseLayer: true,
                singleTile: true,
                transitionEffect: 'resize',
                tileOptions: {
                    maxGetUrlLength: 2048
                }
            }
        );
        return layer;
    },

    createLayerWMSWithStyle : function(cstlUrlPrefix, layerName, instance, style){
        var layer = new OpenLayers.Layer.WMS(layerName,
            cstlUrlPrefix +'WS/wms/'+ instance,
            {
                request:     'GetMap',
                layers:      layerName,
                version:     '1.3.0',
                sld_version: '1.1.0',
                format:      'image/png',
                Styles: style
            },
            {
                ratio: 1,
                isBaseLayer: true,
                singleTile: true,
                transitionEffect: 'resize',
                tileOptions: {
                    maxGetUrlLength: 2048
                }
            }
        );
        return layer;
    },

    createSensorsLayer : function(layerName) {
        var style = new OpenLayers.StyleMap({
            'default': new OpenLayers.Style({
                    pointRadius: 12,
                    'externalGraphic': 'images/marker_normal.png'
                }
            ),
            'select': new OpenLayers.Style({
                    pointRadius: 12,
                    'externalGraphic': 'images/marker_selected.png'
                }
            )
        });

        var layer = new OpenLayers.Layer.Vector(layerName,
            {
                styleMap: style,
                ratio: 1,
                isBaseLayer: true,
                singleTile: true,
                transitionEffect: 'resize',
                tileOptions: {
                    maxGetUrlLength: 2048
                }
            }
        );
        return layer;
    },

    setSensorStyle : function(type, layer) {
        var style;
        if (type && type === 'polygon') {
            style = new OpenLayers.StyleMap({
                'default': new OpenLayers.Style({
                        strokeColor: '#000000',
                        strokeWidth: 1,
                        fillColor: '#39B3D7',
                        fillOpacity: 0.25
                    }
                ),
                'select': new OpenLayers.Style({
                        strokeColor: '#000000',
                        strokeWidth: 1,
                        fillColor: '#BE1522',
                        fillOpacity: 0.25
                    }
                )
            });
        } else if (type && type === 'line') {
            style = new OpenLayers.StyleMap({
                'default': new OpenLayers.Style({
                        strokeColor: '#39B3D7',
                        strokeWidth: 4
                    }
                ),
                'select': new OpenLayers.Style({
                        strokeColor: '#BE1522',
                        strokeWidth: 6
                    }
                )
            });
        } else {
            style = new OpenLayers.StyleMap({
                'default': new OpenLayers.Style({
                        pointRadius: 12,
                        'externalGraphic': 'images/marker_normal.png'
                    }
                ),
                'select': new OpenLayers.Style({
                        pointRadius: 12,
                        'externalGraphic': 'images/marker_selected.png'
                    }
                )
            });
        }

        layer.styleMap = style;
    }
};
