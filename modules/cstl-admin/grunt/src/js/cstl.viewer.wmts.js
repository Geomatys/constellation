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
    layers : [], //array of layers to display
    extent: [-180, -85, 180, 85], //extent array of coordinates always in 4326
    projection: 'EPSG:3857', //given projection used to display layers
    maxExtent : undefined, //the maximum extent for the given projection
    addBackground : true,

    initMap : function(mapId){
        //unbind the old map
        if (WmtsViewer.map) {
            WmtsViewer.map.setTarget(undefined);
        }

        //get projection extent
        var projection = ol.proj.get(WmtsViewer.projection);
        WmtsViewer.maxExtent = projection.getExtent();
        //calculate reprojected extent for given projection
        var reprojExtent = ol.proj.transform(WmtsViewer.extent, 'EPSG:4326', WmtsViewer.projection);
        //if the projected extent contains Infinity then the extent will be the projection extent.
        if(Array.isArray(reprojExtent)){
            for(var i=0;i<reprojExtent.length;i++){
                var coord = reprojExtent[i];
                if(WmtsViewer.isNotNumber(coord)) {
                    reprojExtent = projection.getExtent();
                    break;
                }
            }
        }

        if(WmtsViewer.addBackground) {
            //adding background layer by default OSM
            var sourceOSM = new ol.source.OSM({
                attributions:[]
            });
            var backgroundLayer = new ol.layer.Tile({
                source: sourceOSM
            });
            WmtsViewer.layers.unshift(backgroundLayer);
        }

        WmtsViewer.map = new ol.Map({
            controls: ol.control.defaults().extend([
                new ol.control.ScaleLine({
                    units: 'metric'
                })
            ]),
            layers: WmtsViewer.layers,
            target: mapId,
            view: new ol.View({
                projection: WmtsViewer.projection,
                extent: reprojExtent
            }),
            logo: false
        });

        // Zoom on specified extent
        WmtsViewer.map.updateSize();
        var size = WmtsViewer.map.getSize();
        WmtsViewer.map.getView().fitExtent(reprojExtent, size);
    },

    isNotNumber : function(n) {
        return (n === Number.POSITIVE_INFINITY || n === Number.NEGATIVE_INFINITY || isNaN(n));
    },

    zoomToExtent : function(extent,size){
        var projection = ol.proj.get(WmtsViewer.projection);
        var reprojExtent = ol.proj.transform(extent, 'EPSG:4326', WmtsViewer.projection);
        if(Array.isArray(reprojExtent)){
            for(var i=0;i<reprojExtent.length;i++){
                var coord = reprojExtent[i];
                if(WmtsViewer.isNotNumber(coord)){
                    reprojExtent = projection.getExtent();
                    break;
                }
            }
        }
        WmtsViewer.map.getView().fitExtent(reprojExtent, size);
        WmtsViewer.map.getView().setZoom(WmtsViewer.map.getView().getZoom()+1);
    },

    createLayer : function(layerName, instance, wmtsValues){
        var wmtslayer = new ol.layer.Tile({
            extent: wmtsValues.dataExtent,
            source: new ol.source.WMTS({
                url: wmtsValues.url,
                layer: layerName,
                matrixSet: wmtsValues.matrixSet,
                format: 'image/png',
                projection: WmtsViewer.projection,
                tileGrid: new ol.tilegrid.WMTS({
                    origin: ol.extent.getTopLeft(wmtsValues.dataExtent),
                    resolutions: wmtsValues.resolutions,
                    matrixIds: wmtsValues.matrixIds
                }),
                style: wmtsValues.style
            })
        });

        wmtslayer.set('name', layerName);
        return wmtslayer;
    }
};
