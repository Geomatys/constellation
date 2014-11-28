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
window.DataViewer = {
    map : undefined,
    target : undefined,
    layers : [], //array of layers to display
    extent: [-180, -85, 180, 85], //extent array of coordinates always in 4326
    projection: 'EPSG:3857', //given projection used to display layers
    maxExtent : undefined, //the maximum extent for the given projection
    addBackground : true,

    initConfig : function() {
        DataViewer.layers = [];
        DataViewer.extent = [-180, -85, 180, 85];
        DataViewer.projection = 'EPSG:3857';
        DataViewer.addBackground = true;
    },

    initMap : function(mapId){
        //unbind the old map
        if (DataViewer.map) {
            DataViewer.map.setTarget(undefined);
        }

        //get projection extent
        var projection = ol.proj.get(DataViewer.projection);
        DataViewer.maxExtent = projection.getExtent();
        //calculate reprojected extent for given projection
        var reprojExtent = ol.proj.transform(DataViewer.extent, 'EPSG:4326', DataViewer.projection);
        //if the projected extent contains Infinity then the extent will be the projection extent.
        if(Array.isArray(reprojExtent)){
            for(var i=0;i<reprojExtent.length;i++){
                var coord = reprojExtent[i];
                if(DataViewer.isNotNumber(coord)){
                    reprojExtent = projection.getExtent();
                    break;
                }
            }
        }

        if(DataViewer.addBackground) {
            //adding background layer by default OSM
            var sourceOSM = new ol.source.OSM({
                attributions:[]
            });
            var backgroundLayer = new ol.layer.Tile({
                source: sourceOSM
            });
            DataViewer.layers.unshift(backgroundLayer);
        }

        DataViewer.map = new ol.Map({
            controls: ol.control.defaults().extend([
                new ol.control.ScaleLine({
                    units: 'metric'
                })
            ]),
            layers: DataViewer.layers,
            target: mapId,
            view: new ol.View({
                projection: DataViewer.projection,
                extent: reprojExtent
            }),
            logo: false
        });

        // Zoom on specified extent
        DataViewer.map.updateSize();
        var size = DataViewer.map.getSize();
        DataViewer.map.getView().fitExtent(reprojExtent, size);

    },

    isNotNumber : function(n) {
        return (n === Number.POSITIVE_INFINITY || n === Number.NEGATIVE_INFINITY || isNaN(n));
    },

    zoomToExtent : function(extent,size,postZoom){
        var projection = ol.proj.get(DataViewer.projection);
        var reprojExtent = ol.proj.transform(extent, 'EPSG:4326', DataViewer.projection);
        if(Array.isArray(reprojExtent)){
            for(var i=0;i<reprojExtent.length;i++){
                var coord = reprojExtent[i];
                if(DataViewer.isNotNumber(coord)){
                    reprojExtent = projection.getExtent();
                    break;
                }
            }
        }
        DataViewer.map.getView().fitExtent(reprojExtent, size);
        if(postZoom) {
            DataViewer.map.getView().setZoom(DataViewer.map.getView().getZoom()+1);
        }
    },

    createLayer : function(cstlUrlPrefix, layerName, providerId, filter, tiled){
        var params = {
            'LAYERS':      layerName,
            'PROVIDER':    providerId,
            'VERSION':     '1.3.0',
            'SLD_VERSION': '1.1.0',
            'FORMAT':      'image/png'
        };
        if(filter){
            params.CQLFILTER=filter;
        }
        var layer;
        if(tiled){
            layer = new ol.layer.Tile({
                source: new ol.source.TileWMS({
                    url: cstlUrlPrefix +'api/1/portrayal/portray',
                    params: params
                })
            });
        }else {
            layer = new ol.layer.Image({
                source: new ol.source.ImageWMS({
                    url: cstlUrlPrefix +'api/1/portrayal/portray',
                    params: params
                })
            });
        }
        layer.set('params',params);
        layer.set('name', layerName);
        return layer;
    },

    createLayerWithStyle : function(cstlUrlPrefix, layerName, providerId, style, sldProvider, filter, tiled){
        var sldProvName = (sldProvider) ? sldProvider : "sld";
        var params = {
            'LAYERS':      layerName,
            'PROVIDER':    providerId,
            'VERSION':     '1.3.0',
            'SLD_VERSION': '1.1.0',
            'FORMAT':      'image/png',
            'SLDID':       (style) ? style : '',
            'SLDPROVIDER': sldProvName
        };
        if(filter){
            params.CQLFILTER=filter;
        }
        var layer;
        if(tiled){
            layer = new ol.layer.Tile({
                source: new ol.source.TileWMS({
                    url: cstlUrlPrefix +'api/1/portrayal/portray/style',
                    params: params
                })
            });
        }else {
            layer = new ol.layer.Image({
                source: new ol.source.ImageWMS({
                    url: cstlUrlPrefix +'api/1/portrayal/portray/style',
                    params: params
                })
            });
        }
        layer.set('params',params);
        layer.set('name', layerName);
        return layer;
    },

    createLayerWMS : function(cstlUrlPrefix, layerName, instance){
        var params = {
            'LAYERS':      layerName,
            'VERSION':     '1.3.0',
            'SLD_VERSION': '1.1.0',
            'FORMAT':      'image/png',
            'TRANSPARENT': 'true'
        };
        var layer = new ol.layer.Tile({
            source: new ol.source.TileWMS({
                url: cstlUrlPrefix +'WS/wms/'+ instance,
                params: params
            })
        });
        layer.set('params',params);
        layer.set('name', layerName);
        return layer;
    },

    createLayerWMSWithStyle : function(cstlUrlPrefix, layerName, instance, style){
        var params = {
            'LAYERS':      layerName,
            'VERSION':     '1.3.0',
            'SLD_VERSION': '1.1.0',
            'FORMAT':      'image/png',
            'STYLES':      (style) ? style : '',
            'TRANSPARENT': 'true'
        };
        var layer = new ol.layer.Tile({
            source: new ol.source.TileWMS({
                url: cstlUrlPrefix +'WS/wms/'+ instance,
                params: params
            })
        });
        layer.set('params',params);
        layer.set('name', layerName);
        return layer;
    },

    createLayerExternalWMS : function(url, layerName){
        var params = {
            'LAYERS':      layerName,
            'VERSION':     '1.3.0',
            'SLD_VERSION': '1.1.0',
            'FORMAT':      'image/png',
            'TRANSPARENT': 'true'
        };
        var layer = new ol.layer.Tile({
            source: new ol.source.TileWMS({
                url: url,
                params: params
            })
        });
        layer.set('params',params);
        layer.set('name', layerName);
        return layer;
    },

    createLayerExternalWMSWithStyle : function(url, layerName, style){
        var params = {
            'LAYERS':      layerName,
            'VERSION':     '1.3.0',
            'SLD_VERSION': '1.1.0',
            'FORMAT':      'image/png',
            'STYLES':      (style) ? style : '',
            'TRANSPARENT': 'true'
        };
        var layer = new ol.layer.Tile({
            source: new ol.source.TileWMS({
                url: url,
                params: params
            })
        });
        layer.set('params',params);
        layer.set('name', layerName);
        return layer;
    },

    createSensorsLayer : function(layerName) {
        var stylesMap = {
            'default':[new ol.style.Style({
                    image: new ol.style.Icon(({
                                src: 'img/marker_normal.png'
                         }))
                    })],
            'select':[new ol.style.Style({
                    image: new ol.style.Icon(({
                                src: 'img/marker_selected.png'
                        }))
                    })]
        };

        var layer = new ol.layer.Vector({
            source: new ol.source.Vector({
                features: []
            }),
            style: function(feature, resolution) {
                if(window.selectClick){
                    var selectedFeatures = window.selectClick.getFeatures();
                    if(selectedFeatures && feature === selectedFeatures[0]){
                        return stylesMap.select;
                    }
                }
                return stylesMap.default;
            }
        });
        layer.set('name', layerName);
        return layer;
    },

    setSensorStyle : function(type, layer) {
        var stylesMap={};
        if (type && type === 'polygon') {
            stylesMap = {
                'default':[new ol.style.Style({
                    fill: new ol.style.Fill({
                        color: 'rgba(57, 179, 215, 0.25)'
                    }),
                    stroke: new ol.style.Stroke({
                        color: '#000000',
                        width: 1
                    })
                })],
                'select':[new ol.style.Style({
                    fill: new ol.style.Fill({
                        color: 'rgba(145, 0, 0, 0.25)'
                    }),
                    stroke: new ol.style.Stroke({
                        color: '#000000',
                        width: 1
                    })
                })]
            };
        } else if (type && type === 'line') {
            stylesMap = {
                'default':[new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: '#39B3D7',
                        width: 4
                    })
                })],
                'select':[new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: '#BE1522',
                        width: 6
                    })
                })]
            };
        } else {
            stylesMap = {
                'default':[new ol.style.Style({
                    image: new ol.style.Icon(({
                        anchor: [0.5, 46],
                        anchorXUnits: 'fraction',
                        anchorYUnits: 'pixels',
                        opacity: 0.75,
                        src: 'img/marker_normal.png'
                    }))
                })],
                'select':[new ol.style.Style({
                    image: new ol.style.Icon(({
                        anchor: [0.5, 46],
                        anchorXUnits: 'fraction',
                        anchorYUnits: 'pixels',
                        opacity: 0.75,
                        src: 'img/marker_selected.png'
                    }))
                })]
            };
        }

        layer.setStyle(function(feature, resolution) {
            var selectedFeatures = window.selectClick.getFeatures();
            if(selectedFeatures && feature === selectedFeatures[0]){
                return stylesMap.select;
            } else {
                return stylesMap.default;
            }
        });
    }
};

