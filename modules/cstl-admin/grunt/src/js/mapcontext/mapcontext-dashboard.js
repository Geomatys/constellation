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
                //display dashboard map
                setTimeout(function(){
                    $scope.showMapContextDashboardMap();
                },300);
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
                        $scope.selected=null;
                        $scope.initMapContextDashboard();
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

        $scope.showMapContextDashboardMap = function() {
            if (MapContextDashboardViewer.map) {
                MapContextDashboardViewer.map.setTarget(undefined);
            }
            MapContextDashboardViewer.initConfig();
            MapContextDashboardViewer.fullScreenControl = true;
            var selectedContext = $scope.selected;
            if(selectedContext) {
                var mapcontextLayers = $scope.resolveLayers();
                if (mapcontextLayers && mapcontextLayers.length>0) {
                    var cstlUrl = $cookieStore.get('cstlUrl');
                    var layersToView = [];
                    for (var i=0; i<mapcontextLayers.length; i++) {
                        var layObj = mapcontextLayers[i];
                        if (layObj.visible) {
                            var layerData;
                            if (layObj.isWms) {//wms layer external and internal
                                if(layObj.layer.externalServiceUrl) {
                                    layerData = (layObj.layer.externalStyle) ?
                                        MapContextDashboardViewer.createLayerExternalWMSWithStyle(layObj.layer.externalServiceUrl,
                                            layObj.layer.externalLayer, layObj.layer.externalStyle.split(',')[0]) :
                                        MapContextDashboardViewer.createLayerExternalWMS(layObj.layer.externalServiceUrl, layObj.layer.externalLayer);
                                }else {
                                    var serviceName = (layObj.layer.serviceIdentifier) ? layObj.layer.serviceIdentifier : layObj.service.identifier;
                                    if(layObj.layer.externalStyle){
                                        layerData = MapContextDashboardViewer.createLayerWMSWithStyle(cstlUrl, layObj.layer.Name, serviceName, layObj.layer.externalStyle.split(',')[0]);
                                    }else {
                                        layerData = MapContextDashboardViewer.createLayerWMS(cstlUrl, layObj.layer.Name, serviceName);
                                    }
                                }
                            } else {//internal data layer
                                var layerName,providerId;
                                var dataItem = layObj.layer;
                                var type = dataItem.Type?dataItem.Type.toLowerCase():null;
                                if (dataItem.Namespace) {
                                    layerName = '{' + dataItem.Namespace + '}' + dataItem.Name;
                                } else {
                                    layerName = dataItem.Name;
                                }
                                providerId = dataItem.Provider;
                                if (layObj.styleObj || dataItem.styleName) {
                                    layerData = MapContextDashboardViewer.createLayerWithStyle(cstlUrl,layerName,providerId,
                                        layObj.styleObj?layObj.styleObj.Name:dataItem.styleName,null,null,type!=='vector');
                                } else {
                                    layerData = MapContextDashboardViewer.createLayer(cstlUrl, layerName, providerId,null,type!=='vector');
                                }
                            }
                            layerData.setOpacity(layObj.opacity / 100);
                            layersToView.push(layerData);
                        }
                    }
                    MapContextDashboardViewer.layers = layersToView;
                }
                if(selectedContext.crs){
                    var crsCode = selectedContext.crs;
                    MapContextDashboardViewer.projection = crsCode;
                    MapContextDashboardViewer.addBackground= crsCode==='EPSG:3857';
                    if(crsCode === 'EPSG:4326' || crsCode === 'CRS:84') {
                        MapContextDashboardViewer.extent=[-180, -90, 180, 90];
                    }
                }
                MapContextDashboardViewer.initMap('mapcontextPreviewMap');
                if(selectedContext.west && selectedContext.south && selectedContext.east && selectedContext.north && selectedContext.crs) {
                    var extent = [selectedContext.west,selectedContext.south,selectedContext.east,selectedContext.north];
                    MapContextDashboardViewer.map.updateSize();
                    //because zoomToExtent take extent in EPSG:4326 we need to reproject the zoom extent
                    if(selectedContext.crs !== 'EPSG:4326' && selectedContext.crs !=='CRS:84'){
                        var projection = ol.proj.get(selectedContext.crs);
                        extent = ol.proj.transformExtent(extent, projection,'EPSG:4326');
                    }
                    MapContextDashboardViewer.zoomToExtent(extent, MapContextDashboardViewer.map.getSize(),true);
                }
            }else {
                MapContextDashboardViewer.initMap('mapcontextPreviewMap');
            }
        };

        $scope.resolveLayers = function() {
            var lays = [];
            for (var i=0; i<$scope.selected.layers.length; i++) {
                var lay = $scope.selected.layers[i];
                var styleObj;
                if(lay.externalServiceUrl){
                    if(lay.externalStyle){
                        styleObj = {"Name":lay.externalStyle.split(',')[0]};
                    }
                }else if(lay.TargetStyle && lay.TargetStyle.length>0 && lay.externalStyle){
                    var styleToMatch = lay.externalStyle.split(',')[0];
                    for(var j=0;j<lay.TargetStyle.length;j++){
                        var candidat =lay.TargetStyle[j];
                        if(styleToMatch === candidat.Name){
                            styleObj = candidat;
                            break;
                        }
                    }
                }
                lays.push({
                    "layer": lay,
                    "visible": lay.visible,
                    "opacity": lay.opacity,
                    "isWms": lay.iswms,
                    "styleObj": styleObj
                });
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