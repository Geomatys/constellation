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
                controller: 'MapContextAddModalController'
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
    }]);

cstlAdminApp.controller('MapContextAddModalController', ['$scope', '$modalInstance', 'mapcontext', 'webService', '$growl', '$translate',
    function ($scope, $modalInstance, mapcontext, webService, $growl, $translate) {
        // item to save in the end
        $scope.ctxt = {};

        // handle display mode for this modal popup
        $scope.mode = {
            display: 'general',
            source: 'interne'
        };

        $scope.layers = {
            toAdd: [], // Stores temp layers, selected to be added at the saving time
            toSend: [] // List of layers really sent
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

        $scope.select = function(layer,service) {
            $scope.selected = {
                layer: layer,
                service: service
            };
        };

        $scope.validate = function () {
            // Verify on which step the user is.
            if ($scope.mode.display==='general') {
                // On the general panel, it means saving the whole context
                mapcontext.add({}, $scope.ctxt, function (ctxtCreated) {
                    // Prepare layers to be added
                    for (var i=0; i<$scope.layers.toAdd.length; i++) {
                        var l = $scope.layers.toAdd[i];
                        $scope.layers.toSend.push({
                            mapcontextId: ctxtCreated.id, layerId: l.layer.Id,
                            styleId: (l.layer.TargetStyle.length > 0)? l.layer.TargetStyle[0].Id : null,
                            order: i, visible: l.visible
                        });
                    }

                    mapcontext.setLayers({id: ctxtCreated.id}, $scope.layers.toSend, function() {
                        $growl('success', 'Success', 'Map context created');
                        $modalInstance.close();
                    }, function() {
                        $growl('error', 'Error', 'Unable to add layers to map context');
                        $modalInstance.dismiss('close');
                    });
                }, function () {
                    $growl('error', 'Error', 'Unable to create map context');
                    $modalInstance.dismiss('close');
                });
            } else if ($scope.mode.display==='addChooseSource') {
                $scope.mode.display = 'chooseLayer';
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

        $scope.addLayerToContext = function() {
            $scope.mode.display = 'addChooseSource';
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#selection').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };
    }]);
                                     