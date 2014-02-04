/*
 * Constellation - An open source and standard compliant SDI
 *      http://www.constellation-sdi.org
 *   (C) 2014, Geomatys
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
DataPreviewViewer = {
    map : undefined,

    layers : undefined,

    initMap : function(){
        if (DataPreviewViewer.map) {
            DataPreviewViewer.map.destroy();
        }
        DataPreviewViewer.map = new OpenLayers.Map('dataPreviewMap', {
            controls: [new OpenLayers.Control.Navigation()],
            projection: new OpenLayers.Projection('CRS:84'),
            maxExtent: new OpenLayers.Bounds(-180, -90, 180, 90),
            fractionalZoom: true,
            allOverlays:true
        });
        DataPreviewViewer.map.addLayers(DataPreviewViewer.layers);
        DataPreviewViewer.map.zoomToMaxExtent();
        DataPreviewViewer.map.updateSize();

    },

    createLayer : function(layerName, providerId){
        var layer = new OpenLayers.Layer.WMS(layerName,
            '/cstl-admin/constellation/api/1/portrayal/portray',
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

    createLayerWMS : function(layerName, instance){
        var layer = new OpenLayers.Layer.WMS(layerName,
            '/constellation/WS/wms/'+ instance,
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
    }
}
