/*
 * Constellation - An open source and standard compliant SDI
 *
 *     http://www.constellation-sdi.org
 *
 *     Copyright 2014 Geomatys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

angular.module('cstl-mapcontext-dashboard', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('MapcontextController', function($scope, Dashboard, Growl, $modal, $cookieStore, mapcontext, $window){
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.cstlUrl = $cookieStore.get('cstlUrl');
        $scope.domainId = $cookieStore.get('cstlActiveDomainId');
        $scope.hideScroll = true;

        $scope.values = {
            selectedLayer : null
        };

        $scope.initMapContextDashboard = function() {
            mapcontext.listLayers({}, function(response) {//success
                Dashboard($scope, response, true);
                $scope.wrap.ordertype='name';
                $scope.wrap.filtertext='';
                if($scope.selected) {
                    for(var i=0;i<response.length;i++){
                        if($scope.selected.id === response[i].id){
                            $scope.selected = response[i];
                            break;
                        }
                    }
                }else {
                    $scope.selected = null;
                }
            }, function() {//error
                Growl('error','Error','Unable to get list of map context!');
            });
            angular.element($window).bind("scroll", function() {
                $scope.hideScroll = (this.pageYOffset < 220);
                $scope.$apply();
            });
        };

        /**
         * Reset filters for dashboard
         */
        $scope.resetFilters = function(){
            mapcontext.listLayers({}, function(response) {//success
                Dashboard($scope, response, true);
                $scope.wrap.ordertype='name';
                $scope.wrap.orderreverse=false;
                $scope.wrap.filtertext='';
            }, function() {//error
                Growl('error','Error','Unable to restore list of map context!');
            });
        };

        $scope.selectContextChild = function(item) {
            if (item && $scope.values.selectedLayer && $scope.values.selectedLayer.id === item.id) {
                $scope.values.selectedLayer = null;
            } else {
                $scope.values.selectedLayer = item;
            }
        };


        $scope.toggleUpDownSelected = function() {
            var $header = $('#MapcontextDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
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
                $scope.initMapContextDashboard();
            });
        };

        $scope.deleteMapContext = function() {
            var dlg = $modal.open({
                templateUrl: 'views/modal-confirm.html',
                controller: 'ModalConfirmController',
                resolve: {
                    'keyMsg':function(){return "dialog.message.confirm.delete.mapcontext";}
                }
            });
            dlg.result.then(function(cfrm){
                if(cfrm){
                    var ctxtName = $scope.selected.name;
                    mapcontext.delete({id: $scope.selected.id}, function () {
                        Growl('success', 'Success', 'Map context ' + ctxtName + ' successfully removed');
                        $scope.initMapContextDashboard();
                        $scope.selected=null;
                    }, function () {
                        Growl('error', 'Error', 'Unable to remove map context ' + ctxtName);
                    });
                }
            });
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
                $scope.initMapContextDashboard();
            });
        };

        $scope.showMapContext = function() {
            $modal.open({
                templateUrl: 'views/mapcontext/modalViewer.html',
                controller: 'MapContextViewerModalController',
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

        $scope.truncate = function(small, text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (small === true && text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else if (small === false && text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small === true && text.length > 22) {
                        return text.substr(0, 22) + "...";
                    } else if (small === false && text.length > 29) {
                        return text.substr(0, 29) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else {return text;}
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else {return text;}
                }
            }
        };

        $scope.initMapContextDashboard();
    });