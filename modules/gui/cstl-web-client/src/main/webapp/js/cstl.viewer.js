/*
 * Constellation - An open source and standard compliant SDI
 *      http://www.constellation-sdi.org
 *   (C) 2009-2013, Geomatys
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

/**
 * Created by bgarcia on 03/12/2013.
 */

CSTL.Viewer = {
    map: undefined,

    layers: undefined,

    panel: undefined,

    init: function () {
        //add OSM default Layer on background
        CSTL.Viewer.layers = [new OpenLayers.Layer.OSM("Simple OSM Map")];

        //build controllers
        var controls = this.buildController();
        CSTL.Viewer.buildToolPanel(controls);
        CSTL.Viewer.buildMap();
    },

    callGetFeatureInfo: function () {
        alert("getFeature");
    },

    saveMapContext: function () {
        alert("SaveMapContext");
    },

    toggleFullScreen: function () {
        $("#geomap").toggleFullScreen();
    },

    buildController: function () {
        var navigation = new OpenLayers.Control.Navigation({
            title: "Navigate",
            text: "3"
        });

        var getFeatureBtn = new OpenLayers.Control.Button({
            title: "getFeature",
            text: "7",
            trigger: CSTL.Viewer.callGetFeatureInfo
        });

        var saveMapContextBtn = new OpenLayers.Control.Button({
            title: "saveMapContext",
            text: "e",
            trigger: CSTL.Viewer.saveMapContext
        });

        var toggleFullScreenbtn = new OpenLayers.Control.Button({
            title: "toggleFullScreen",
            text: "Z",
            trigger: CSTL.Viewer.toggleFullScreen
        });

        var controls = [navigation,
            new OpenLayers.Control.ZoomIn({
                title: "Zoom In",
                text: "1"
            }),
            new OpenLayers.Control.ZoomOut({
                title: "Zoom Out",
                text: "2"
            }),
            new OpenLayers.Control.ZoomToMaxExtent({
                title: "Zoom to the max extent",
                text: "4"
            }),
            getFeatureBtn,
            saveMapContextBtn,
            toggleFullScreenbtn];
        return controls;

    },

    buildToolPanel: function (controls) {
        CSTL.Viewer.panel = new OpenLayers.Control.Panel({
            defaultControl: controls[0],
            displayClass: "toolPanel",
            createControlMarkup: function (control) {
                var button = document.createElement('button'),
                    iconSpan = document.createElement('span');
                $(button).addClass("mapButton");
                $(iconSpan).addClass("font-icon");
                iconSpan.innerHTML = control.text;
                button.appendChild(iconSpan);
                return button;
            }
        });

        CSTL.Viewer.panel.addControls(controls);
    },

    buildMap: function () {
        $("html").css("width", "100%");
        $("html").css("height", "100%");
        $("body").css("width", "100%");
        $("body").css("height", "100%");
        $(".jz").css("width", "100%");
        $(".jz").css("height", "100%");
        $(".jz > .container").css("width", "100%");
        $(".jz > .container").css("height", "100%");

        CSTL.Viewer.map = new OpenLayers.Map('geomap', {
            controls: [
                new OpenLayers.Control.ScaleLine(),
                CSTL.Viewer.panel
            ]
        });
        CSTL.Viewer.map.addLayers(CSTL.Viewer.layers);
        //Center on Montpellier
        CSTL.Viewer.map.setCenter(
            new OpenLayers.LonLat(3.877222, 43.611944).transform(
                new OpenLayers.Projection("EPSG:4326"),
                CSTL.Viewer.map.getProjectionObject()
            ), 12
        );
        CSTL.Viewer.map.zoomToMaxExtent();
        CSTL.Viewer.map.updateSize()
    }
};