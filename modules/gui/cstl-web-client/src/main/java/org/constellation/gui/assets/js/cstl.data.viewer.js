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
CSTL.dataViewer = {
    map : undefined,

    layers : undefined,

    initMap : function(){
        CSTL.dataViewer.map = new OpenLayers.Map('dataMap', {
            controls: [new OpenLayers.Control.Navigation()],
            projection: new OpenLayers.Projection('EPSG:4326'),
            maxExtent: new OpenLayers.Bounds(-180, -90, 180, 90),
            fractionalZoom: true,
            allOverlays:true
        });
        CSTL.dataViewer.map.addLayers(CSTL.dataViewer.layers);
        CSTL.dataViewer.map.zoomToMaxExtent();
        CSTL.dataViewer.map.updateSize();

    },

    createLayer : function(layerName, providerId, portrayUrl){
        var layer = new OpenLayers.Layer.WMS(layerName,
            '/cstl-web-client/mvc/overview',
            {
                request:     'Portray',
                method:      portrayUrl,
                layers:      layerName,
                provider:    providerId,
                version:     '1.3.0',
                sld_version: '1.1.0'
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
