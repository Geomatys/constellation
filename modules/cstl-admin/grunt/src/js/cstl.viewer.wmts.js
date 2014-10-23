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
window.WmtsViewer = {
    map : undefined,
    target : undefined,
    layers : [],
    extent: [-180, -90, 180, 90],
    projection: 'EPSG:4326',
    maxExtent : undefined,

    initMap : function(mapId){
        if (WmtsViewer.map) {
            WmtsViewer.map.setTarget(undefined);
        }
        WmtsViewer.map = new ol.Map({
            layers: WmtsViewer.layers,
            target: mapId,
            view: new ol.View({
                projection: WmtsViewer.projection,
                extent: WmtsViewer.extent
            }),
            logo: false
        });
        var projection = ol.proj.get(WmtsViewer.projection);
        WmtsViewer.maxExtent = projection.getExtent();

        // Zoom on specified extent
        WmtsViewer.map.updateSize();
        WmtsViewer.map.getView().fitExtent(WmtsViewer.extent, WmtsViewer.map.getSize());
    },

    createLayer : function(layerName, instance, capabilities){
        var layers = capabilities.contents.layers;
        var maxExtent = null;
        var matrixSet = null;
        for (var i=0; i<layers.length; i++) {
            var layer = layers[i];
            if (layer.title === layerName) {
                maxExtent = layer.bounds;
                matrixSet = layer.tileMatrixSetLinks[0].tileMatrixSet;
                break;
            }
        }

        var wmtslayer = new ol.layer.Tile({
            extent: WmtsViewer.extent,
            source: new ol.source.WMTS({
                url: capabilities.url,
                layer: layerName,
                matrixSet: matrixSet, //'EPSG:3857'
                format: 'image/png',
                projection: WmtsViewer.projection,
                tileGrid: new ol.tilegrid.WMTS({
                    origin: ol.extent.getTopLeft(WmtsViewer.extent),
                    resolutions: capabilities.resolutions,
                    matrixIds: capabilities.matrixIds
                }),
                style: 'default'
            })
        });

        wmtslayer.set('name', layerName);
        return wmtslayer;
    }
};
