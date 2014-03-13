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
WmtsViewer = {
    map : undefined,

    format : new OpenLayers.Format.WMTSCapabilities({
        yx: {
            "urn:ogc:def:crs:EPSG::4326": true
        }
    }),

    initMap : function(mapId, capabilities){
        if (WmtsViewer.map) {
            WmtsViewer.map.destroy();
        }

//        var extent = "-5.740083333333334, 47.96008333333334, -4.332083333333334, 48.60008333333334";
//        var maxExtent = new OpenLayers.Bounds.fromString(extent, false);
        var maxExtent = capabilities.contents.layers[0].bounds;

        WmtsViewer.map = new OpenLayers.Map(mapId, {
            controls: [new OpenLayers.Control.Navigation()],
            projection: new OpenLayers.Projection('EPSG:4326'),
            maxExtent: maxExtent,
            allOverlays: true,
            restrictedExtent: maxExtent
        });
    },

    createLayer : function(layerName, instance, capabilities){
        return WmtsViewer.format.createLayer(capabilities, {
            layer: layerName,
            matrixSet: capabilities.contents.layers[0].tileMatrixSetLinks[0].tileMatrixSet,
            format: "image/png",
            style: "default"
        });
    }
}
