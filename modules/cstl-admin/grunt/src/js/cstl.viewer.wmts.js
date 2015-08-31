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
window.buildWmtsViewer = function () {
    return {
        map : undefined,
        target : undefined,
        layers : [], //array of layers to display
        extent: [-180, -85, 180, 85], //extent array of coordinates always in 4326
        projection: 'EPSG:3857', //given projection used to display layers
        maxExtent : undefined, //the maximum extent for the given projection
        addBackground : true,
        fullScreenControl : false,

        initConfig : function() {
            this.layers = [];
            this.extent = [-180, -85, 180, 85];
            this.projection = 'EPSG:3857';
            this.addBackground = true;
            this.fullScreenControl = false;
        },

        initMap : function(mapId){
            //unbind the old map
            if (this.map) {
                this.map.setTarget(undefined);
            }

            //get projection extent
            var projection = ol.proj.get(this.projection);
            this.maxExtent = projection.getExtent();
            //calculate reprojected extent for given projection
            var reprojExtent = ol.proj.transformExtent(this.extent, 'EPSG:4326', this.projection);
            //if the projected extent contains Infinity then the extent will be the projection extent.
            if(Array.isArray(reprojExtent)){
                for(var i=0;i<reprojExtent.length;i++){
                    var coord = reprojExtent[i];
                    if(this.isNotNumber(coord)) {
                        reprojExtent = projection.getExtent();
                        break;
                    }
                }
            }
            //clip the projected extent that should never be out of default projection extent
            if(reprojExtent[0]<this.maxExtent[0]){
                reprojExtent[0] = this.maxExtent[0];
            }
            if(reprojExtent[1]<this.maxExtent[1]){
                reprojExtent[1] = this.maxExtent[1];
            }
            if(reprojExtent[2]>this.maxExtent[2]){
                reprojExtent[2] = this.maxExtent[2];
            }
            if(reprojExtent[3]>this.maxExtent[3]){
                reprojExtent[3] = this.maxExtent[3];
            }

            if(this.addBackground) {
                //adding background layer by default OSM
                var sourceOSM = new ol.source.OSM({
                    attributions:[new ol.Attribution({
                        html: 'Tiles courtesy of ' +
                            '<a href="http://www.mapquest.com" target="_blank">MapQuest</a>'
                    })],
                    url:'//otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png'
                });
                var backgroundLayer = new ol.layer.Tile({
                    source: sourceOSM
                });
                this.layers.unshift(backgroundLayer);
            }

            var controlsArray = [
                new ol.control.ScaleLine({
                    units: 'metric'
                }),
                new ol.control.Zoom({
                    zoomInTipLabel:'Zoom in',
                    zoomOutTipLabel:'Zoom out'
                })
            ];
            if(this.fullScreenControl){
                controlsArray.push(new ol.control.FullScreen());
            }

            this.map = new ol.Map({
                controls: ol.control.defaults().extend(controlsArray),
                layers: this.layers,
                target: mapId,
                view: new ol.View({
                    projection: this.projection,
                    extent: reprojExtent
                }),
                logo: false
            });

            // Zoom on specified extent
            this.map.updateSize();
            var size = this.map.getSize();
            this.map.getView().fit(reprojExtent, size);
        },

        isNotNumber : function(n) {
            return (n === Number.POSITIVE_INFINITY || n === Number.NEGATIVE_INFINITY || isNaN(n));
        },

        zoomToExtent : function(extent,size){
            var projection = ol.proj.get(this.projection);
            var reprojExtent = ol.proj.transformExtent(extent, 'EPSG:4326', this.projection);
            if(Array.isArray(reprojExtent)){
                for(var i=0;i<reprojExtent.length;i++){
                    var coord = reprojExtent[i];
                    if(this.isNotNumber(coord)){
                        reprojExtent = projection.getExtent();
                        break;
                    }
                }
            }
            //clip the projected extent that should never be out of default projection extent
            if(reprojExtent[0]<this.maxExtent[0]){
                reprojExtent[0] = this.maxExtent[0];
            }
            if(reprojExtent[1]<this.maxExtent[1]){
                reprojExtent[1] = this.maxExtent[1];
            }
            if(reprojExtent[2]>this.maxExtent[2]){
                reprojExtent[2] = this.maxExtent[2];
            }
            if(reprojExtent[3]>this.maxExtent[3]){
                reprojExtent[3] = this.maxExtent[3];
            }
            this.map.getView().fit(reprojExtent, size);
            this.map.getView().setZoom(this.map.getView().getZoom()+1);
        },

        createLayer : function(layerName, instance, wmtsValues){
            var wmtslayer = new ol.layer.Tile({
                extent: wmtsValues.dataExtent,
                source: new ol.source.WMTS({
                    url: wmtsValues.url,
                    layer: layerName,
                    matrixSet: wmtsValues.matrixSet,
                    format: 'image/png',
                    projection: this.projection,
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
};
window.WmtsViewer = window.buildWmtsViewer();
window.WmtsLayerDashboardViewer = window.buildWmtsViewer();
