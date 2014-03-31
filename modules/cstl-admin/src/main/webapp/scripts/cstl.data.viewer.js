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

    initMap : function(mapId){
        if (DataViewer.map) {
            DataViewer.map.destroy();
        }
        DataViewer.map = new OpenLayers.Map(mapId, {
            controls: [new OpenLayers.Control.Navigation()],
            projection: new OpenLayers.Projection('CRS:84'),
            maxExtent: new OpenLayers.Bounds(-180, -90, 180, 90),
            fractionalZoom: true,
            allOverlays:true
        });
        DataViewer.map.addLayers(DataViewer.layers);
        DataViewer.map.zoomToMaxExtent();
        DataViewer.map.updateSize();

    },

    createLayer : function(cstlUrlPrefix, layerName, providerId){
        var layer = new OpenLayers.Layer.WMS(layerName,
           cstlUrlPrefix +'api/1/portrayal/portray',
            {
                layers:      layerName,
                provider:    providerId,
                version:     '1.3.0',
                sld_version: '1.1.0',
                format:      'image/png'
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

    createLayerWithStyle : function(cstlUrlPrefix, layerName, providerId, style){
        var layer = new OpenLayers.Layer.WMS(layerName,
                cstlUrlPrefix +'api/1/portrayal/portray/style',
            {
                layers:      layerName,
                provider:    providerId,
                version:     '1.3.0',
                sld_version: '1.1.0',
                format:      'image/png',
                SLDID:        style,
                SLDPROVIDER:  "sld"
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
    }

}
