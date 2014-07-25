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
'use strict';

cstlAdminApp.controller('MapcontextController', ['$scope', '$dashboard', '$growl', '$modal', '$cookies', 'mapcontext',
    function ($scope, $dashboard, $growl, $modal, $cookies, mapcontext){
        $scope.domainId = $cookies.cstlActiveDomainId;

        $scope.init = function() {
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            mapcontext.listLayers({}, function(response) {
                $dashboard($scope, response, true);
                $scope.ordertype='name';
                modalLoader.close();
            }, function() {
                modalLoader.close();
            });
        };

        $scope.selectedLayer = null;

        $scope.selectContextChild = function(item) {
            if ($scope.selectedLayer === item) {
                $scope.selectedLayer = null;
            } else {
                $scope.selectedLayer = item;
            }
        };


        $scope.toggleUpDownSelected = function() {
            var $header = $('#MapcontextDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        $scope.addMapContext = function() {
            var modal = $modal.open({
                templateUrl: 'views/mapcontext/modalAddContext.html',
                controller: 'MapContextModalController',
                resolve: {
                    ctxtToEdit: function () { return null; },
                    layersForCtxt: function () { return null; }
                }
            });

            modal.result.then(function() {
                $scope.init();
            });
        };

        $scope.deleteMapContext = function() {
            if (confirm("Are you sure?")) {
                var ctxtName = $scope.selected.name;
                mapcontext.delete({id: $scope.selected.id}, function () {
                    $growl('success', 'Success', 'Map context ' + ctxtName + ' successfully removed');
                    $scope.init();
                }, function () {
                    $growl('error', 'Error', 'Unable to remove map context ' + ctxtName);
                });
            }
        };

        $scope.editMapContext = function() {
            var modal = $modal.open({
                templateUrl: 'views/mapcontext/modalAddContext.html',
                controller: 'MapContextModalController',
                resolve: {
                    ctxtToEdit: function () { return angular.copy($scope.selected); },
                    layersForCtxt: function() { return $scope.resolveLayers(); }
                }
            });

            modal.result.then(function() {
                $scope.init();
            });
        };

        $scope.showMapContext = function() {
            $modal.open({
                templateUrl: 'views/mapcontext/modalViewer.html',
                controller: 'MapContextModalController',
                resolve: {
                    ctxtToEdit: function () { return angular.copy($scope.selected); },
                    layersForCtxt: function() { return $scope.resolveLayers(); }
                }
            });
        };

        $scope.resolveLayers = function() {
            var lays = [];
            for (var i=0; i<$scope.selected.layers.length; i++) {
                var lay = $scope.selected.layers[i];
                lays.push(
                    {layer: lay,
                        visible: lay.visible
                    }
                );
            }
            lays.sort(function (a, b) {
                return a.layer.layerOrder - b.layer.layerOrder;
            });
            return lays;
        };
    }]);

cstlAdminApp.controller('MapContextModalController', ['$scope', '$modalInstance', 'mapcontext', 'webService', 'style', '$growl', '$translate', 'ctxtToEdit', 'layersForCtxt', '$cookies', 'textService',
    function ($scope, $modalInstance, mapcontext, webService, style, $growl, $translate, ctxtToEdit, layersForCtxt, $cookies, textService) {
        // item to save in the end
        $scope.ctxt = {};
        // defines if we are in adding or edition mode
        $scope.addMode = true;

        if (ctxtToEdit) {
            $scope.addMode = false;
            $scope.ctxt = ctxtToEdit;
            // remove property layers if it exists for serialization
            delete $scope.ctxt.layers;
        }

        // handle display mode for this modal popup
        $scope.mode = {
            selTab: 'tabInfo',
            display: 'general',
            source: 'interne',
            dispWmsLayers: false
        };

        $scope.layers = {
            toAdd: layersForCtxt || [], // Stores temp layers, selected to be added at the saving time
            toSend: [], // List of layers really sent
            toStyle: null // Layer on which to apply the selected style
        };

        $scope.external = {
            serviceUrl: null
        };

        $scope.selected = {};

        $scope.styles = {
            existing: [],
            selected: null
        };

        $scope.$watch('layers.toAdd.layer.opacity', function() {

        }, true);

        $scope.close = function () {
            $modalInstance.dismiss('close');
        };

        $scope.getCurrentLang = function() {
            return $translate.use();
        };

        $scope.initInternalWmsServices = function() {
            webService.listServiceLayers({lang: $scope.getCurrentLang()}, function(response) {
                $scope.servicesLayers = response;
            });
        };

        $scope.select = function(layer,service) {
            $scope.selected.layer = layer;
            $scope.selected.service = service;
        };

        $scope.selectItem = function(item) {
            $scope.selected.item = item;
        };

        $scope.selectExtLayer = function(extLayer) {
            $scope.selected.extLayer = extLayer;
        };

        $scope.selectStyle = function(item) {
            $scope.styles.selected = item;
        };

        $scope.validate = function () {
            // Verify on which step the user is.
            if ($scope.mode.display==='general') {
                // On the general panel, it means saving the whole context
                if ($scope.addMode) {
                    mapcontext.add({}, $scope.ctxt, function (ctxtCreated) {
                        handleLayersForContext(ctxtCreated);
                    }, function () {
                        $growl('error', 'Error', 'Unable to create map context');
                        $modalInstance.dismiss('close');
                    });
                } else {
                    mapcontext.update({}, $scope.ctxt, function (ctxtUpdated) {
                        handleLayersForContext(ctxtUpdated);
                    }, function () {
                        $growl('error', 'Error', 'Unable to update map context');
                        $modalInstance.dismiss('close');
                    });
                }
            } else if ($scope.mode.display==='addChooseSource') {
                $scope.mode.display = 'chooseLayer';
            } else if ($scope.mode.display==='chooseLayer') {
                // Add the selected layer to the current map context
                if ($scope.selected.extLayer) {
                    var llExtent = '';
                    if ($scope.selected.extLayer.llbbox) {
                        for (var i = 0; i < $scope.selected.extLayer.llbbox.length; i++) {
                            if (i != 0) {
                                llExtent += ',';
                            }
                            llExtent += $scope.selected.extLayer.llbbox[i];
                        }
                    }
                    var layerExt = {
                        externalLayer: $scope.selected.extLayer.name,
                        externalLayerExtent: llExtent,
                        externalServiceUrl: $scope.external.serviceUrl,
                        externalServiceVersion: null,
                        opacity: 100
                    };
                    var layerExtToAdd = {
                        layer: layerExt,
                        visible: true
                    };
                    $scope.layers.toAdd.push(layerExtToAdd);
                } else {
                    if ($scope.selected.layer) {
                        var layerToAdd = {
                            layer: $scope.selected.layer,
                            service: $scope.selected.service,
                            visible: true
                        };
                        layerToAdd.layer.opacity = 100;
                        $scope.layers.toAdd.push(layerToAdd);
                    }
                }
                $scope.viewMap(false);

                // Go back to first screen
                $scope.mode.display = 'general';
            } else if ($scope.mode.display==='addChooseStyle') {
                $scope.layers.toStyle.layer.styleId = $scope.styles.selected.Id;
                $scope.layers.toStyle.layer.styleName = $scope.styles.selected.Name;
                $scope.viewMap(false);
                $scope.mode.display = 'general';
            }
        };

        $scope.cancel = function() {
            if ($scope.mode.display==='general') {
                $scope.close();
            } else {
                $scope.mode.display = 'general';
            }
        };

        function handleLayersForContext(ctxt) {
            // Prepare layers to be added
            fillLayersToSend(ctxt);

            mapcontext.setLayers({id: ctxt.id}, $scope.layers.toSend, function () {
                $growl('success', 'Success', 'Map context created');
                $modalInstance.close();
            }, function () {
                $growl('error', 'Error', 'Unable to add layers to map context');
                $modalInstance.dismiss('close');
            });
        }

        function fillLayersToSend(ctxt) {
            $scope.layers.toSend = [];
            for (var i = 0; i < $scope.layers.toAdd.length; i++) {
                var l = $scope.layers.toAdd[i];
                $scope.layers.toSend.push({
                    mapcontextId: (ctxt) ? ctxt.id : null, layerId: (l.layer.Id) ? l.layer.Id : l.layer.layerId, styleId: l.layer.styleId,
                    layerOrder: i, layerOpacity: l.layer.opacity, layerVisible: l.visible,
                    externalServiceUrl: l.layer.externalServiceUrl, externalServiceVersion: l.layer.externalServiceVersion,
                    externalLayer: l.layer.externalLayer, externalLayerExtent: l.layer.externalLayerExtent, externalStyle: l.layer.externalStyle
                });
            }
        }

        $scope.goToAddLayerToContext = function() {
            $scope.mode.display = 'addChooseSource';
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#selectionLayer').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };
        $scope.toggleUpDownExtSelected = function() {
            var $header = $('#selectionExtLayer').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        $scope.orderUp = function(i) {
            if (i > 0) {
                var previous = $scope.layers.toAdd[i - 1];
                $scope.layers.toAdd[i - 1] = $scope.layers.toAdd[i];
                $scope.layers.toAdd[i] = previous;

                // Now switch layer order for the map
                DataViewer.map.setLayerIndex(DataViewer.map.layers[i], i-1);
            }
        };
        $scope.orderDown = function(i) {
            if (i < $scope.layers.toAdd.length - 1) {
                var next = $scope.layers.toAdd[i + 1];
                $scope.layers.toAdd[i + 1] = $scope.layers.toAdd[i];
                $scope.layers.toAdd[i] = next;

                // Now switch layer order for the map
                DataViewer.map.setLayerIndex(DataViewer.map.layers[i+1], i);
            }
        };

        $scope.viewLayerInfo = function(item) {
            var serviceIdentifier = (item.service) ? item.service.name : item.layer.serviceIdentifier;
            var serviceVersions = (item.service) ? item.service.versions : item.layer.serviceVersions;
            return '<div><b>Name</b></div>' +
                '<div>'+ item.layer.Name +'</div>' +
                '<div><b>Alias</b></div>' +
                '<div>'+ item.layer.Alias +'</div>' +
                '<div><b>Type</b></div>' +
                '<div>'+ item.layer.Type +'</div>' +
                '<div><b>Service name</b></div>' +
                '<div>'+ serviceIdentifier +'</div>' +
                '<div><b>Service version(s)</b></div>' +
                '<div>'+ serviceVersions +'</div>';
        };

        $scope.goToStyleMapItem = function(item) {
            style.listAll({provider: 'sld'}, function(response) {
                $scope.styles.existing = [];
                for (var j=0; j<item.layer.TargetStyle.length; j++) {
                    var tgStyle = item.layer.TargetStyle[j];
                    for (var i=0; i<response.styles.length; i++) {
                        var style = response.styles[i];
                        if (style.Name===tgStyle.Name && style.Provider===tgStyle.Provider) {
                            $scope.styles.existing.push(style);
                            break;
                        }
                    }
                }
            });
            $scope.mode.display = 'addChooseStyle';
            $scope.layers.toStyle = item;
        };

        $scope.deleteMapItem = function(item) {
            var index = $scope.layers.toAdd.indexOf(item);
            if (index != -1) {
                $scope.layers.toAdd.splice(index, 1);
            }

            for (var i=0; i<DataViewer.layers.length; i++) {
                var l = DataViewer.layers[i];
                if (l.name === item.layer.Name) {
                    DataViewer.map.removeLayer(l);
                    return;
                }
            }
        };

        $scope.changeVisibility = function(item) {
            for (var i=0; i<DataViewer.layers.length; i++) {
                var l = DataViewer.layers[i];
                if (l.name === item.layer.Name) {
                    l.setVisibility(item.visible);
                    return;
                }
            }
        };

        $scope.updateOpacity = function(item) {
            for (var i=0; i<DataViewer.layers.length; i++) {
                var l = DataViewer.layers[i];
                if (l.name === item.layer.Name) {
                    l.setOpacity(item.layer.opacity / 100);
                    return;
                }
            }
        };

        $scope.viewMap = function(firstTime) {
            if (!$scope.layers.toAdd || $scope.layers.toAdd.length===0) {
                return;
            }

            var cstlUrl = $cookies.cstlUrl;

            var layersToView = [];
            for (var i=0; i<$scope.layers.toAdd.length; i++) {
                var l = $scope.layers.toAdd[i];
                if (l.visible) {
                    var layerData;
                    if (l.layer.externalServiceUrl) {
                        layerData = (l.layer.externalStyle) ?
                            DataViewer.createLayerExternalWMSWithStyle(l.layer.externalServiceUrl, l.layer.externalLayer, l.layer.externalStyle) :
                            DataViewer.createLayerExternalWMS(l.layer.externalServiceUrl, l.layer.externalLayer);
                    } else {
                        var serviceName = (l.layer.serviceIdentifier) ? l.layer.serviceIdentifier : l.service.identifier;
                        layerData = (l.layer.styleName) ?
                            DataViewer.createLayerWMSWithStyle(cstlUrl, l.layer.Name, serviceName, l.layer.styleName) :
                            DataViewer.createLayerWMS(cstlUrl, l.layer.Name, serviceName);
                    }
                    layerData.setOpacity(l.layer.opacity / 100);
                    layersToView.push(layerData);
                }
            }

            DataViewer.layers = layersToView;
            if (layersToView.length === 0) {
                return;
            }

            DataViewer.initMap('mapContextMap');

            if (firstTime) {
                var extent = new OpenLayers.Bounds($scope.ctxt.west, $scope.ctxt.south, $scope.ctxt.east, $scope.ctxt.north);
                DataViewer.map.zoomToExtent(extent, true);
            } else {
                fillLayersToSend();
                mapcontext.extentForLayers({}, $scope.layers.toSend, function(response) {
                    useExtentForLayers(response.values);
                });
            }
        };

        function useExtentForLayers(values) {
            $scope.ctxt.crs = values['crs'];
            $scope.ctxt.west = values['west'];
            $scope.ctxt.south = values['south'];
            $scope.ctxt.east = values['east'];
            $scope.ctxt.north = values['north'];
            var extent = new OpenLayers.Bounds($scope.ctxt.west, $scope.ctxt.south, $scope.ctxt.east, $scope.ctxt.north);
            DataViewer.map.zoomToExtent(extent, true)
        }

        $scope.applyExtent = function() {
            var currentMapExtent = DataViewer.map.getExtent();
            var extent = currentMapExtent.toArray();
            $scope.ctxt.west = extent[0];
            $scope.ctxt.south = extent[1];
            $scope.ctxt.east = extent[2];
            $scope.ctxt.north = extent[3];
        };

        $scope.searchAndDisplayWmsLayers = function() {
            if ($scope.external.serviceUrl) {
                textService.capaWmsExterne($scope.external.serviceUrl)
                    .success(function (data, status, headers, config) {
                        var capabilities = DataViewer.format.read(data);
                        $scope.external.layers = capabilities.capability.layers;

                        $scope.mode.dispWmsLayers = true;
                    });
            }
        }
    }]);
                                     