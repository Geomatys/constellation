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

angular.module('cstl-webservice-edit', ['ngCookies', 'cstl-restapi', 'cstl-services', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .controller('WebServiceEditController', function($rootScope, $scope, $routeParams , webService, dataListing, provider, csw, sos, $modal, textService, Dashboard, Growl, $filter, DomainResource,StyleSharedService, style, $cookies, $translate, $window) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.tagText = '';
        $scope.type = $routeParams.type;
        $scope.url = $cookies.cstlUrl + "WS/" + $routeParams.type + "/" + $routeParams.id;
        $scope.cstlUrl = $cookies.cstlUrl;
        $scope.cstlSessionId = $cookies.cstlSessionId;
        $scope.urlBoxSize = Math.min($scope.url.length,100);
        $scope.domainId = $cookies.cstlActiveDomainId;
        $scope.writeOperationAvailable = $scope.type === 'csw' || $scope.type === 'sos' || $scope.type === 'wfs';
        $scope.hideScroll = true;


        angular.element($window).bind("scroll", function() {
            if (this.pageYOffset < 220) {
                $scope.hideScroll = true;
            } else {
                $scope.hideScroll = false;
            }
            $scope.$apply();
        });

        $scope.getCurrentLang = function() {
            return $translate.use();
        };

        webService.get({type: $scope.type, id: $routeParams.id, lang: $scope.getCurrentLang()}, function (service) {
            $scope.service = service;
            webService.permissionByDomainRole(function (domainroles) {
                $scope.domainroles = domainroles;
            });
            webService.domains({id: service.id}, function (domains) {
                $scope.domains = domains;
            });

            $scope.toggleDomain = function (i) {
                var pathParams = {domainId: $scope.domains[i].id, serviceId: service.id};
                if ($scope.domains[i].linked) {
                    webService.unlinkFromDomain(pathParams, function () {
                        $scope.domains[i].linked = !$scope.domains[i].linked;
                        $scope.domains[i].linked = false;
                    }, function (response) {
                        Growl('error', 'error', response.data.message);
                        webService.domains({id: service.id}, function (domains) {
                            $scope.domains = domains;
                        });
                    });
                } else {
                    webService.linkToDomain(pathParams, {}, function () {
                        $scope.domains[i].linked = true;
                    }, function () {

                    });
                }
            };
        });

        $scope.metadata = webService.metadata({type: $scope.type, id:$routeParams.id, lang:$scope.getCurrentLang()});

        $scope.tabdata = true;
        $scope.tabdesc = false;
        $scope.tabmetadata = false;
        $scope.tabrights = false;

        $scope.selectTab = function(item) {
            if (item === 'tabdata') {
                $scope.tabdata = true;
                $scope.tabdesc = false;
                $scope.tabmetadata = false;
                $scope.tabrights = false;
            } else if (item === 'tabdesc') {
                $scope.tabdata = false;
                $scope.tabdesc = true;
                $scope.tabmetadata = false;
                $scope.tabrights = false;
            } else if (item === 'tabmetadata') {
                $scope.tabdata = false;
                $scope.tabdesc = false;
                $scope.tabmetadata = true;
                $scope.tabrights = false;
            } else {
                $scope.tabdata = false;
                $scope.tabdesc = false;
                $scope.tabmetadata = false;
                $scope.tabrights = true;
            }
        };

        $scope.initScope = function() {
            if ($scope.type === 'csw') {
                csw.count({id: $routeParams.id}, {}, function(max) {
                    csw.getRecords({id: $routeParams.id, count: max.value, startIndex: 0}, {}, function(response) {
                        Dashboard($scope, response.BriefNode, false);
                        $scope.wrap.filtertype = "";

                        var mdIds = [];
                        for (var i=0; i<response.BriefNode.length; i++) {
                            mdIds.push(response.BriefNode[i].identifier);
                        }

                        dataListing.dataForMetadata({}, mdIds,
                            function(response) { $scope.relatedDatas = response; },
                            function() { Growl('error','Error','Unable to get related data for metadata'); }
                        );
                    });
                });
            } else if ($scope.type === 'sos') {
                sos.sensorsTree({id: $routeParams.id}, function(sensors) {
                    Dashboard($scope, sensors.children, false);
                    $scope.layers = sensors.children;

                }, function() { Growl('error','Error','Unable to list sensors'); });
            } else {
                $scope.config = webService.config({type: $scope.type, id:$routeParams.id});
                $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                    Dashboard($scope, response, true);
                    $scope.wrap.filtertype = "";
                });
            }
        };

        $scope.getVersionsForType = function() {
            if ($scope.type === 'wms') {
                return [{ 'id': '1.1.1'}, { 'id': '1.3.0' }];
            }
            if ($scope.type === 'wfs') {
                return [{ 'id': '1.1.0'}, { 'id': '2.0.0' }];
            }
            if ($scope.type === 'wcs') {
                return [{ 'id': '1.0.0'}];
            }
            if ($scope.type === 'wmts') {
                return [{ 'id': '1.0.0'}];
            }
            if ($scope.type === 'csw') {
                return [{ 'id': '2.0.0'}, { 'id': '2.0.2'}];
            }
            if ($scope.type === 'sos') {
                return [{ 'id': '1.0.0'}, { 'id': '2.0.0'}];
            }
            if ($scope.type === 'wps') {
                return [{ 'id': '1.0.0'}];
            }
            return [];
        };
        $scope.versions = $scope.getVersionsForType();

        // define which version to set
        $scope.selectedVersion = function (){
            var selVersions = $filter('filter')($scope.versions, {checked: true});
            var strVersions = [];
            for(var i=0; i < selVersions.length; i++) {
                $scope.metadata.versions.push(selVersions[i].id);
            }
        };

        // define which version is Selected
        $scope.versionIsSelected = function(currentVersion){
            return $.inArray(currentVersion, $scope.metadata.versions) > -1;
        };

        $scope.addTag = function() {
            if (!$scope.tagText || $scope.tagText === '' || $scope.tagText.length === 0) {
                return;
            }
            if ($scope.metadata.keywords ===null){
                $scope.metadata.keywords = [];
            }
            $scope.metadata.keywords.push($scope.tagText);
            $scope.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.tagText.length === 0 && !key) {
                $scope.metadata.keywords.pop();
            } else if (key) {
                $scope.metadata.keywords.splice(key, 1);
            }
        };

        $scope.selectedMetadataChild = null;
        $scope.selectedSensorsChild = null;

        $scope.selectMetadataChild = function(item) {
            if ($scope.selectedMetadataChild === item) {
                $scope.selectedMetadataChild = null;
            } else {
                $scope.selectedMetadataChild = item;
            }
        };
        $scope.selectSensorsChild = function(item) {
            if ($scope.selectedSensorsChild === item) {
                $scope.selectedSensorsChild = null;
            } else {
                $scope.selectedSensorsChild = item;
            }
        };

        $scope.saveServiceMetadata = function() {
            webService.updateMd({type: $scope.service.type, id: $scope.service.identifier},$scope.metadata,
                function(response) {
                    if (response.status==="Success") {
                        Growl('success','Success','Service description successfully updated');
                    }else{
                        Growl('error','Error','Service description update failed due to :'+response.status);
                    }
                },
                function() {
                    Growl('error','Error','Service description update failed');
                }
            );
        };

        // Show Capa methods
        $scope.showCapa = function(service) {
            if (service.versions.length > 1) {
                var modal = $modal.open({
                    templateUrl: 'views/webservice/modalChooseVersion.html',
                    controller: 'WebServiceVersionsController',
                    resolve: {
                        service: function() { return service; }
                    }
                });
                modal.result.then(function(result) {
                    showModalCapa(service, result);
                });
            } else {
                showModalCapa(service, service.versions[0]);
            }
        };

        function showModalCapa(service, version) {
            $modal.open({
                templateUrl: 'views/webservice/modalCapa.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.capa(service.type.toLowerCase(), service.identifier, version);
                    }
                }
            });
        }

        // Show Logs methods
        $scope.showLogs = function(service) {

            $modal.open({
                templateUrl: 'views/webservice/modalLogs.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.logs(service.type.toLowerCase(), service.identifier);
                    }
                }
            });
        };


        $scope.reload = function(service){
            webService.restart({type: service.type, id: service.identifier}, {value: true},
                function() { Growl('success','Success','Service '+ service.name +' successfully reloaded'); },
                function() { Growl('error','Error','Service '+ service.name +' reload failed'); }
            );
        };


        $scope.startOrStop = function(service){
            if(service.status==='STARTED'){
                webService.stop({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.service.status = "NOT_STARTED";
                        Growl('success','Success','Service '+ service.name +' successfully stopped');
                    }
                }, function() { Growl('error','Error','Service '+ service.name +' stop failed'); });
            }else{
                webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.service.status = "STARTED";
                        Growl('success','Success','Service '+ service.name +' successfully started');
                    }
                }, function() { Growl('error','Error','Service '+ service.name +' start failed'); });
            }
        };

        // Allow to choose data to add for this service
        $scope.showDataToAdd = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalDataChoose.html',
                controller: 'DataModalController',
                resolve: {
                    exclude: function() { return $scope.layers; },
                    service: function() { return $scope.service; }
                }
            });

            modal.result.then(function() {
                if ($scope.type.toLowerCase() !== 'sos') {
                    $scope.layers = webService.layers({type: $scope.type, id: $routeParams.id}, {}, function (response) {
                        $scope.wrap.fullList = response;
                    });
                } else {
                    $scope.initScope();
                }
            });
        };

        $scope.deleteLayer = function() {
            var txt = ($scope.service.type.toLowerCase() === 'wmts') ? 'Are you sure? This will also delete the generated tiles for this layer.' : 'Are you sure?';
            if ($scope.selected && confirm(txt)) {
                if ($scope.service.type.toLowerCase() === 'sos') {
                    var idToDel = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                    sos.removeSensor({id: $scope.service.identifier, sensor: idToDel}, function() {
                        Growl('success', 'Success', 'Sensor ' + idToDel + ' successfully removed from service ' + $scope.service.name);
                        $scope.initScope();
                    },function () {
                        Growl('error', 'Error', 'Unable to remove sensor ' + idToDel + ' from service ' + $scope.service.name);
                    });
                } else {
                    webService.deleteLayer({type: $scope.service.type, id: $scope.service.identifier, layerid: $scope.selected.Name}, {value: $scope.selected.Namespace},
                        function () {
                            if ($scope.service.type.toLowerCase() === 'wmts' || $scope.service.type.toLowerCase() === 'wms') {
                                $scope.deleteTiledData($scope.service, $scope.selected.Name, $scope.selected.Provider);
                            }

                            Growl('success', 'Success', 'Layer ' + $scope.selected.Name + ' successfully deleted from service ' + $scope.service.name);
                            $scope.layers = webService.layers({type: $scope.type, id: $routeParams.id}, {}, function (response) {
                                $scope.wrap.fullList = response;
                            });
                        },
                        function () {
                            Growl('error', 'Error', 'Layer ' + $scope.selected.Name + ' failed to be deleted from service ' + $scope.service.name);
                        }
                    );
                }
            }
        };

        $scope.deleteTiledData = function(service, layerName, providerId) {
            dataListing.deletePyramidFolder({providerId: providerId}, function() {
                provider.delete({id: providerId}, function() {}, function() {
                    Growl('error','Error','Unable to delete data for layer '+ layerName);
                });
            });
        };

        $scope.deleteMetadata = function() {
            if ($scope.selected && confirm("Are you sure?")) {
                csw.delete({id: $scope.service.identifier, metaId: $scope.selected.identifier}, {},
                    function() {
                        Growl('success','Success','Metadata deleted');
                        csw.count({id: $routeParams.id}, {}, function(max) {
                            csw.getRecords({id: $routeParams.id, count: max.value, startIndex: 0}, {}, function(response) {
                                Dashboard($scope, response.BriefNode, false);
                                $scope.wrap.filtertype = "";
                            });
                        });
                    }, function() { Growl('error','Error','Failed to delete metadata'); }
                );
            }
        };

        $scope.showLayer = function() {
            $('#viewerData').modal("show");
            var layerName = $scope.selected.Name;
            if ($scope.service.type === 'WMTS') {
                // GetCaps
                textService.capa($scope.service.type.toLowerCase(), $scope.service.identifier, $scope.service.versions[0])
                    .success(function (data, status, headers, config) {
                        // Build map
                        var capabilities = WmtsViewer.format.read(data);
                        WmtsViewer.initMap('dataMap');
                        var layerData = WmtsViewer.createLayer(layerName, $scope.service.identifier, capabilities);
                        WmtsViewer.map.addLayer(layerData);
                        WmtsViewer.map.zoomToMaxExtent();
                    });
            } else {
                var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
                var layerData;
                var providerId = $scope.selected.Provider;
                if ($scope.selected.TargetStyle && $scope.selected.TargetStyle.length > 0) {
                    layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, layerName, providerId, $scope.selected.TargetStyle[0].Name);
                } else {
                    layerData = DataViewer.createLayer($cookies.cstlUrl, layerName, providerId);
                }
                DataViewer.layers = [layerData, layerBackground];
                provider.dataDesc({},{values: {'providerId':providerId,'dataId':layerName}},
                    function(response) {//success
                        DataViewer.initMap('dataMap');
                        var bbox = response.boundingBox;
                        if (bbox) {
                            var extent = new OpenLayers.Bounds(bbox[0],bbox[1],bbox[2],bbox[3]);
                            DataViewer.map.zoomToExtent(extent, true);
                        }
                    }, function() {//error
                        // failed to find a metadata, just load the full map
                        DataViewer.initMap('dataMap');
                    }
                );
            }
        };

        $scope.showSensor = function() {
            var sensorId = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
            $modal.open({
                templateUrl: 'views/sensor/modalSensorView.html',
                controller: 'SensorModalController',
                resolve: {
                    service: function() { return $scope.service; },
                    sensorId: function() { return sensorId; }
                }
            });
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#serviceDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };

        // Style methods
        $scope.showStyleList = function() {
            StyleSharedService.showStyleList($scope);
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style);
        };

        $scope.truncate = function(small, text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (small === true && text.length > 22) {
                        return text.substr(0, 22) + "...";
                    } else if (small === false && text.length > 65) {
                        return text.substr(0, 65) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small === true && text.length > 15) {
                        return text.substr(0, 15) + "...";
                    } else if (small === false && text.length > 55) {
                        return text.substr(0, 55) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 35) {
                        return text.substr(0, 35) + "...";
                    } else {return text;}
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 25) {
                        return text.substr(0, 25) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 17) {
                        return text.substr(0, 17) + "...";
                    } else {return text;}
                }
            }
        };
    });