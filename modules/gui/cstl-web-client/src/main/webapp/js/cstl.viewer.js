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

    navigation:undefined,

    getFeatureBtn:undefined,

        init: function () {
        //add OSM default Layer on background
        CSTL.Viewer.layers = [new OpenLayers.Layer.OSM("Simple OSM Map")];

        //build controllers
        var controls = this.buildController();
        CSTL.Viewer.buildToolPanel(controls);
        CSTL.Viewer.buildMap();
    },

    navigate: function(){
        CSTL.Viewer.getFeatureBtn.deactivate();
    },

    callGetFeatureInfo: function () {
        CSTL.Viewer.navigation.deactivate();
        CSTL.Viewer.getFeatureBtn.activate();
        alert("getFeature");
    },

    saveMapContext: function () {
        alert("SaveMapContext");
    },

    toggleFullScreen: function () {
        $("#geomap").toggleFullScreen();
        var $fullscreenSpan = $("button[name|='fullscreen'] span");
        var current = $fullscreenSpan.html();
        $fullscreenSpan.empty();
        if(current === "Z"){
            $fullscreenSpan.html("/");
        }else{
            $fullscreenSpan.html("Z");
        }
    },

    buildController: function () {
        var toolListeners = {
            "activate": CSTL.Viewer.navigate
        };

        CSTL.Viewer.navigation = new OpenLayers.Control.Navigation({
            title: "Navigate",
            id: "navigate",
            text: "3",
            eventListeners : toolListeners
        });

        CSTL.Viewer.getFeatureBtn = new OpenLayers.Control.Button({
            title: "getFeature",
            text: "7",
            id: "getFeature",
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
            id: "fullscreen",
            trigger: CSTL.Viewer.toggleFullScreen
        });

        var controls = [
            CSTL.Viewer.navigation,
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
            CSTL.Viewer.getFeatureBtn,
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
                $(iconSpan).addClass("font-icon");
                iconSpan.innerHTML = control.text;

                $(button).addClass("mapButton");
                $(button).attr("name", control.id);
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