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

    format : undefined,

    initMap : function(mapId, maxExtent){
        if (WmtsViewer.map) {
            WmtsViewer.map.destroy();
        }

        WmtsViewer.format = new OpenLayers.Format.WMTSCapabilities({
            yx: {
                "urn:ogc:def:crs:EPSG::4326": true
            }
        });

        WmtsViewer.map = new OpenLayers.Map(mapId, {
            controls: [new OpenLayers.Control.Navigation()],
            projection: new OpenLayers.Projection('EPSG:4326'),
            maxExtent: maxExtent,
            allOverlays: true,
            restrictedExtent: maxExtent
        });
    },

    createLayer : function(layerName, instance, xmlCaps){
        var capabilities = WmtsViewer.format.read(xmlCaps);
        return WmtsViewer.format.createLayer(capabilities, {
            layer: layerName,
            matrixSet: capabilities.contents.layers[0].tileMatrixSetLinks[0].tileMatrixSet,
            format: "image/png",
            style: "default"
        });
    }
}
