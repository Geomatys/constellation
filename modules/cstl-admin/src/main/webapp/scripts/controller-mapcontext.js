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
                controller: 'MapContextAddModalController',
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
                controller: 'MapContextAddModalController',
                resolve: {
                    ctxtToEdit: function () { return angular.copy($scope.selected); },
                    layersForCtxt: function () {
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
                            return a.layer.order - b.layer.order;
                        });
                        return lays;
                    }
                }
            });

            modal.result.then(function() {
                $scope.init();
            });
        };
    }]);

cstlAdminApp.controller('MapContextAddModalController', ['$scope', '$modalInstance', 'mapcontext', 'webService', 'style', '$growl', '$translate', 'ctxtToEdit', 'layersForCtxt',
    function ($scope, $modalInstance, mapcontext, webService, style, $growl, $translate, ctxtToEdit, layersForCtxt) {
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
            source: 'interne'
        };

        $scope.layers = {
            toAdd: layersForCtxt || [], // Stores temp layers, selected to be added at the saving time
            toSend: [], // List of layers really sent
            toStyle: null // Layer on which to apply the selected style
        };

        $scope.styles = {
            existing: [],
            selected: null
        };

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

        $scope.initStyles = function() {
            style.listAll({provider: 'sld'}, function(response) {
                $scope.styles.existing = response.styles;
            });
        };

        $scope.select = function(layer,service) {
            $scope.selected = {
                layer: layer,
                service: service
            };
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
            } else if ($scope.mode.display==='addChooseStyle') {
                $scope.layers.toStyle.layer.styleId = $scope.styles.selected.Id;
                $scope.layers.toStyle.layer.styleName = $scope.styles.selected.Name;
                $scope.mode.display = 'general';
            } else if ($scope.mode.display==='chooseLayer') {
                // Add the selected layer to the current map context
                if ($scope.selected.layer) {
                    $scope.layers.toAdd.push(
                        {layer: $scope.selected.layer,
                         service: $scope.selected.service,
                         visible: true
                        });
                }
                // Go back to first screen
                $scope.mode.display = 'general';
            }
        };

        function handleLayersForContext(ctxt) {
            // Prepare layers to be added
            for (var i = 0; i < $scope.layers.toAdd.length; i++) {
                var l = $scope.layers.toAdd[i];
                $scope.layers.toSend.push({
                    mapcontextId: ctxt.id, layerId: l.layer.Id,
                    styleId: l.layer.styleId,
                    order: i, visible: l.visible
                });
            }

            mapcontext.setLayers({id: ctxt.id}, $scope.layers.toSend, function () {
                $growl('success', 'Success', 'Map context created');
                $modalInstance.close();
            }, function () {
                $growl('error', 'Error', 'Unable to add layers to map context');
                $modalInstance.dismiss('close');
            });
        }

        $scope.addLayerToContext = function() {
            $scope.mode.display = 'addChooseSource';
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#selection').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        $scope.orderUp = function(i) {
            if (i > 0) {
                var previous = $scope.layers.toAdd[i - 1];
                $scope.layers.toAdd[i - 1] = $scope.layers.toAdd[i];
                $scope.layers.toAdd[i] = previous;
            }
        };
        $scope.orderDown = function(i) {
            if (i < $scope.layers.toAdd.length - 1) {
                var next = $scope.layers.toAdd[i + 1];
                $scope.layers.toAdd[i + 1] = $scope.layers.toAdd[i];
                $scope.layers.toAdd[i] = next;
            }
        };

        $scope.editMapItem = function(layer) {

        };

        $scope.styleMapItem = function(layer) {
            $scope.mode.display = 'addChooseStyle';
            $scope.layers.toStyle = layer;
        };

        $scope.deleteMapItem = function(layer) {
            var index = $scope.layers.toAdd.indexOf(layer);
            if (index != -1) {
                $scope.layers.toAdd.splice(index, 1);
            }
        };
    }]);
                                     