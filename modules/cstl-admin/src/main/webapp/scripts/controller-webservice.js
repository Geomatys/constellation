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

cstlAdminApp.controller('WebServiceController', ['$scope', 'webService', 'provider', 'csw', 'sos', '$modal', 'textService', '$growl',
    function ($scope, webService, provider, csw, sos, $modal, textService, $growl) {


        $scope.typeFilter = {type: '!WEBDAV'};


        var modalLoader = $modal.open({
          templateUrl: 'views/modalLoader.html',
          controller: 'ModalInstanceCtrl'
        });
        webService.listAll(function(response){
            $scope.services = response;
            modalLoader.close();
        });


        // Show Capa methods
        $scope.showCapa = function(service) {
            if (service.versions.length > 1) {
                var modal = $modal.open({
                    templateUrl: 'views/modalChooseVersion.html',
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
                templateUrl: 'views/modalCapa.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.capa(service.type.toLowerCase(), service.identifier, version);
                    }
                }
            });
        };

        // Show Logs methods
        $scope.showLogs = function(service) {

            $modal.open({
                templateUrl: 'views/modalLogs.html',
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
                function() { $growl('success','Success','Service '+ service.name +' successfully reloaded'); },
                function() { $growl('error','Error','Service '+ service.name +' reload failed'); }
            );
        };
        $scope.startOrStop = function(service){
            if(service.status==='WORKING'){
                webService.stop({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.services = webService.listAll();
                        $growl('success','Success','Service '+ service.name +' successfully stopped');
                    }
                }, function() { $growl('error','Error','Service '+ service.name +' stop failed'); });
            }else{
                webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.services = webService.listAll();
                        $growl('success','Success','Service '+ service.name +' successfully started');
                    }
                }, function() { $growl('error','Error','Service '+ service.name +' start failed'); });
            }
        };

        $scope.deleteService = function(service) {
            if (confirm("Are you sure?")) {
                if (service.type.toLowerCase() === 'sos') {
                    // A provider has been created for this SOS service, so remove it
                    provider.delete({id: service.identifier +"-om2"});
                }
                webService.delete({type: service.type, id: service.identifier}, {} ,
                    function() { $growl('success','Success','Service '+ service.name +' successfully deleted');
                        $scope.services = webService.listAll(); },
                    function() { $growl('error','Error','Service '+ service.name +' deletion failed'); }
                );
            }
        };

        $scope.refreshIndex = function(service) {
            csw.refresh({id: service.identifier}, {},
                function() { $growl('success','Success','Search index for the service '+ service.name +' successfully refreshed'); },
                function() { $growl('error','Error','Search index for the service '+ service.name +' failed to be updated'); }
            );
        };

        $scope.getLayersCount = function(service) {
            if (service.layersNumber !== null) {
                return service.layersNumber;
            }
            return 0;
        };
    }]);

cstlAdminApp.controller('WebServiceUtilsController', ['$scope', 'webService', '$modalInstance', 'details',
    function ($scope, webService, $modalInstance, details) {
        $scope.details = details.data;
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

    }]);

cstlAdminApp.controller('WebServiceVersionsController', ['$scope', 'webService', '$modalInstance', 'service',
    function ($scope, webService, $modalInstance, service) {
        $scope.service = service;
        $scope.versions = service.versions;
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.chooseVersion = function(version) {
            $modalInstance.close(version);
        };
    }]);

cstlAdminApp.controller('WebServiceCreateController', ['$scope','$routeParams', 'webService', '$filter', '$location', '$growl',
    function ($scope, $routeParams, webService, $filter, $location, $growl) {
        $scope.type = $routeParams.type;
        $scope.tonext = true;
        $scope.metadata = {};
        $scope.metadata.keywords = [];

        $scope.getVersionsForType = function() {
            if ($scope.type === 'wms') {
                return [{ 'id': '1.1.1'}, { 'id': '1.3.0', 'checked': true }];
            }
            if ($scope.type === 'wfs') {
                return [{ 'id': '1.1.0', 'checked': true}, { 'id': '2.0.0' }];
            }
            if ($scope.type === 'wcs') {
                return [{ 'id': '1.0.0', 'checked': true}];
            }
            if ($scope.type === 'wmts') {
                return [{ 'id': '1.0.0', 'checked': true}];
            }
            if ($scope.type === 'csw') {
                return [{ 'id': '2.0.0'}, { 'id': '2.0.2', 'checked': true}];
            }
            if ($scope.type === 'sos') {
                return [{ 'id': '1.0.0'}, { 'id': '2.0.0', 'checked': true}];
            }
            if ($scope.type === 'wps') {
                return [{ 'id': '1.0.0', 'checked': true}];
            }
            return [];
        };
        $scope.versions = $scope.getVersionsForType();


        $scope.goToServiceContact = function() {
            if($scope.metadata.name!= null || $scope.metadata.identifier!=null){
                $scope.tonext = false;
            } else {
                $scope.invalideName=true;
            }
        };

        $scope.goToServiceInfo = function() {
            $scope.tonext = true;
        };

        $scope.addTag = function() {
            if (!$scope.tagText || $scope.tagText == '' || $scope.tagText.length == 0) {
                return;
            }

            $scope.metadata.keywords.push($scope.tagText);
            $scope.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.tagText.length == 0 &&
                key === undefined) {
                $scope.metadata.keywords.pop();
            } else if (key != undefined) {
                $scope.metadata.keywords.splice(key, 1);
            }
        };

        // define which version to set
        $scope.selectedVersion = function (){
            var selVersions = $filter('filter')($scope.versions, {checked: true});
            var strVersions = [];
            for(var i=0; i < selVersions.length; i++) {
                strVersions.push(selVersions[i].id);
            }
            $scope.metadata.versions = strVersions;
        };

        // define which version is Selected
        $scope.versionIsSelected = function(currentVersion){
            return $.inArray(currentVersion, $scope.metadata.versions) > -1
        };

        $scope.saveServiceMetadata = function() {
            // Ensures both name and identifier are filled
            if (($scope.metadata.identifier == null || $scope.metadata.identifier == '') && $scope.metadata.name != null && $scope.metadata.name != '') {
                $scope.metadata.identifier = $scope.metadata.name;
            }
            if (($scope.metadata.name == null || $scope.metadata.name == '') && $scope.metadata.identifier != null && $scope.metadata.identifier != '') {
                $scope.metadata.name = $scope.metadata.identifier;
            }

            webService.create({type: $scope.type}, $scope.metadata,
                function() {
                    $growl('success', 'Success', 'Service ' + $scope.metadata.name + ' successfully created');
                    if ($scope.type == 'csw' || $scope.type == 'sos') {
                        $location.path('/webservice/'+ $scope.type +'/'+ $scope.metadata.identifier +'/source');
                    } else {
                        $location.path('/webservice');
                    }
                },

                function() { $growl('error','Error','Service '+ $scope.metadata.name +' creation failed'); }
            );
        };
    }]);

cstlAdminApp.controller('WebServiceChooseSourceController', ['$scope','$routeParams', 'webService', 'provider', '$growl', '$location',
    function ($scope, $routeParams , webService, provider, $growl, $location) {
        $scope.type = $routeParams.type;
        $scope.id = $routeParams.id;
        $scope.db = {
            'url': 'localhost',
            'port': '5432',
            'className': 'org.postgresql.Driver',
            'name': ''
        };

        $scope.initSource = function() {
            if ($scope.type === 'csw') {
                $scope.source = {'automatic': {'@format': null, 'bdd': {}}};
            } else if ($scope.type === 'sos') {
                $scope.source = {'constellation-config.SOSConfiguration':
                                    {'constellation-config.SMLConfiguration':
                                        {'@format': 'filesystem',
                                         'profile': 'discovery',
                                         'dataDirectory': ''
                                        },
                                     'constellation-config.observationFilterType': 'om2',
                                     'constellation-config.observationReaderType': 'om2',
                                     'constellation-config.observationWriterType': 'om2',
                                     'constellation-config.SMLType': 'filesystem',
                                     'constellation-config.OMConfiguration':
                                        {'@format': 'OM2',
                                         'bdd': {}
                                        }
                                    }
                                };
            }
        };

        $scope.saveServiceSource = function() {
            var fullDbUrl = ($scope.db.className === 'org.postgresql.Driver') ? 'jdbc:postgresql' : 'jdbc:mysql';
            fullDbUrl += '://'+ $scope.db.url +':'+ $scope.db.port +'/'+ $scope.db.name;
            if ($scope.type === 'csw') {
                $scope.source.automatic.bdd.className = $scope.db.className;
                $scope.source.automatic.bdd.connectURL = fullDbUrl;
            } else {
                $scope.source['constellation-config.SOSConfiguration']['constellation-config.OMConfiguration'].bdd.className = $scope.db.className;
                $scope.source['constellation-config.SOSConfiguration']['constellation-config.OMConfiguration'].bdd.connectURL = fullDbUrl;
            }

            webService.setConfig({type: $scope.type, id: $scope.id}, $scope.source, function() {
                $growl('success','Success','Service '+ $scope.id +' successfully updated');
                if ($scope.type.toLowerCase() === 'sos') {
                    createOmProvider();
                }
                $location.path('/webservice');
            }, function() {
                $growl('error','Error','Service configuration update error');
            });
        };

        function createOmProvider() {
            provider.create({
                id: $scope.id +'-om2'
            }, {
                type: "feature-store",
                subType: "om2",
                parameters: {
                    port: $scope.db.port,
                    host: $scope.db.url,
                    database: $scope.db.name,
                    user: $scope.source['constellation-config.SOSConfiguration']['constellation-config.OMConfiguration'].bdd.user,
                    password: $scope.source['constellation-config.SOSConfiguration']['constellation-config.OMConfiguration'].bdd.password,
                    sgbdtype: 'postgres'
                }
            }, function() {}, function() {
                $growl('error','Error','Unable to create OM2 provider');
            });
        }
    }]);

cstlAdminApp.controller('WebServiceEditController', ['$scope','$routeParams', 'webService', 'dataListing', 'provider', 'csw', 'sos', '$modal','textService', '$dashboard', '$growl', '$filter', 'StyleSharedService','style','$cookies',
    function ($scope, $routeParams , webService, dataListing, provider, csw, sos, $modal, textService, $dashboard, $growl, $filter, StyleSharedService, style, $cookies) {
        $scope.tagText = '';
        $scope.type = $routeParams.type;
        $scope.url = $cookies.cstlUrl + "WS/" + $routeParams.type + "/" + $routeParams.id;
        $scope.cstlUrl = $cookies.cstlUrl;
        $scope.cstlSessionId = $cookies.cstlSessionId;
        $scope.urlBoxSize = Math.min($scope.url.length,100);

        var client = new ZeroClipboard( document.getElementById("copy-button") );

        client.on( "load", function(client) {
            // alert( "movie is loaded" );
            client.on( "complete", function(client, args) {
                // `this` is the element that was clicked
                $growl('success','Success',"Copied text to clipboard: " + args.text );
            } );
        } );

        $scope.service = webService.get({type: $scope.type, id:$routeParams.id});
        $scope.metadata = webService.metadata({type: $scope.type, id:$routeParams.id});

        $scope.tabdata = true;
        $scope.tabdesc = false;
        $scope.tabmetadata = false;

        $scope.selectTab = function(item) {
            if (item === 'tabdata') {
                $scope.tabdata = true;
                $scope.tabdesc = false;
                $scope.tabmetadata = false;
            } else if (item === 'tabdesc') {
                $scope.tabdata = false;
                $scope.tabdesc = true;
                $scope.tabmetadata = false;
            } else {
                $scope.tabdata = false;
                $scope.tabdesc = false;
                $scope.tabmetadata = true;
            }
        };

        $scope.initScope = function() {
            if ($scope.type === 'csw') {
                csw.count({id: $routeParams.id}, {}, function(max) {
                    csw.getRecords({id: $routeParams.id, count: max.value, startIndex: 0}, {}, function(response) {
                        $dashboard($scope, response.BriefNode, false);
                        $scope.filtertype = "";

                        var mdIds = [];
                        for (var i=0; i<response.BriefNode.length; i++) {
                            mdIds.push(response.BriefNode[i].identifier);
                        }

                        dataListing.dataForMetadata({}, mdIds,
                            function(response) { $scope.relatedDatas = response; },
                            function() { $growl('error','Error','Unable to get related data for metadata'); }
                        );
                    });
                });
            } else if ($scope.type === 'sos') {
                sos.sensorsTree({id: $routeParams.id}, function(sensors) {
                    $dashboard($scope, sensors.children, false);
                    $scope.layers = sensors.children;

                }, function() { $growl('error','Error','Unable to list sensors'); });
            } else {
                $scope.config = webService.config({type: $scope.type, id:$routeParams.id});
                $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                    $dashboard($scope, response, true);
                    $scope.filtertype = "";
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
                strVersions.push(selVersions[i].id);
            }
            $scope.metadata.versions = strVersions;
        };

        // define which version is Selected
        $scope.versionIsSelected = function(currentVersion){
            return $.inArray(currentVersion, $scope.metadata.versions) > -1
        };

        $scope.addTag = function() {
            if (!$scope.tagText || $scope.tagText == '' || $scope.tagText.length == 0) {
                return;
            }
            if ($scope.metadata.keywords ==null){
                $scope.metadata.keywords = [];
            }
            $scope.metadata.keywords.push($scope.tagText);
            $scope.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.tagText.length == 0 &&
                key === undefined) {
                $scope.metadata.keywords.pop();
            } else if (key != undefined) {
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
                        $growl('success','Success','Service description successfully updated');
                    }else{
                        $growl('error','Error','Service description update failed due to :'+response.status);
                    }
                },
                function() {
                    $growl('error','Error','Service description update failed');
                }
            )
        };

        // Show Capa methods
        $scope.showCapa = function(service) {
            if (service.versions.length > 1) {
                var modal = $modal.open({
                    templateUrl: 'views/modalChooseVersion.html',
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
                templateUrl: 'views/modalCapa.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.capa(service.type.toLowerCase(), service.identifier, version);
                    }
                }
            });
        };

        // Show Logs methods
        $scope.showLogs = function(service) {

            $modal.open({
                templateUrl: 'views/modalLogs.html',
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
                function() { $growl('success','Success','Service '+ service.name +' successfully reloaded'); },
                function() { $growl('error','Error','Service '+ service.name +' reload failed'); }
            );
        };


        $scope.startOrStop = function(service){
            if(service.status==='WORKING'){
                webService.stop({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.service.status = "NOT_STARTED";
                        $growl('success','Success','Service '+ service.name +' successfully stopped');
                    }
                }, function() { $growl('error','Error','Service '+ service.name +' stop failed'); });
            }else{
                webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.service.status = "WORKING";
                        $growl('success','Success','Service '+ service.name +' successfully started');
                    }
                }, function() { $growl('error','Error','Service '+ service.name +' start failed'); });
            }
        };

        // Allow to choose data to add for this service
        $scope.showDataToAdd = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalDataChoose.html',
                controller: 'DataModalController',
                resolve: {
                    exclude: function() { return $scope.layers; },
                    service: function() { return $scope.service; }
                }
            });

            modal.result.then(function() {
                if ($scope.type.toLowerCase() !== 'sos') {
                    $scope.layers = webService.layers({type: $scope.type, id: $routeParams.id}, {}, function (response) {
                        $scope.fullList = response;
                    });
                } else {
                    $scope.initScope();
                }
            });
        };

        $scope.deleteLayer = function() {
            var txt = ($scope.service.type.toLowerCase() === 'wmts') ? 'Are you sure? This will also delete the generated tiles for this layer.' : 'Are you sure?';
            if ($scope.selected != null && confirm(txt)) {
                if ($scope.service.type.toLowerCase() === 'sos') {
                    var idToDel = ($scope.selectedSensorsChild !== null) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                    sos.removeSensor({id: $scope.service.identifier, sensor: idToDel}, function() {
                        $growl('success', 'Success', 'Sensor ' + idToDel + ' successfully removed from service ' + $scope.service.name);
                        $scope.initScope();
                    },function () {
                        $growl('error', 'Error', 'Unable to remove sensor ' + idToDel + ' from service ' + $scope.service.name);
                    });
                } else {
                    webService.deleteLayer({type: $scope.service.type, id: $scope.service.identifier, layerid: $scope.selected.Name}, {value: $scope.selected.Namespace},
                        function () {
                            if ($scope.service.type.toLowerCase() === 'wmts' || $scope.service.type.toLowerCase() === 'wms') {
                                $scope.deleteTiledData($scope.service, $scope.selected.Name, $scope.selected.Provider);
                            }

                            $growl('success', 'Success', 'Layer ' + $scope.selected.Name + ' successfully deleted from service ' + $scope.service.name);
                            $scope.layers = webService.layers({type: $scope.type, id: $routeParams.id}, {}, function (response) {
                                $scope.fullList = response;
                            });
                        },
                        function () {
                            $growl('error', 'Error', 'Layer ' + $scope.selected.Name + ' failed to be deleted from service ' + $scope.service.name);
                        }
                    );
                }
            }
        };

        $scope.deleteTiledData = function(service, layerName, providerId) {
            dataListing.deletePyramidFolder({providerId: providerId}, function() {
                provider.delete({id: providerId}, function() {}, function() {
                    $growl('error','Error','Unable to delete data for layer '+ layerName);
                });
            });
        };

        $scope.deleteMetadata = function() {
            if ($scope.selected != null && confirm("Are you sure?")) {
                csw.delete({id: $scope.service.identifier, metaId: $scope.selected.identifier}, {},
                    function() {
                        $growl('success','Success','Metadata deleted');
                        csw.count({id: $routeParams.id}, {}, function(max) {
                            csw.getRecords({id: $routeParams.id, count: max.asInt, startIndex: 0}, {}, function(response) {
                                $dashboard($scope, response.BriefNode, false);
                                $scope.filtertype = "";
                            });
                        });
                    }, function() { $growl('error','Error','Failed to delete metadata'); }
                );
            }
        };

        $scope.showLayer = function() {
            $('#viewerData').modal("show");
            var layerName = $scope.selected.Name;
            var modalLoader = $modal.open({
              templateUrl: 'views/modalLoader.html',
              controller: 'ModalInstanceCtrl'
            });
            if ($scope.service.type === 'WMTS') {
                // GetCaps
                textService.capa($scope.service.type.toLowerCase(), $scope.service.identifier, $scope.service.versions[0])
                    .success(function (data, status, headers, config) {
                        // Build map
                        var capabilities = WmtsViewer.format.read(data);
                        WmtsViewer.initMap('dataMap', capabilities);
                        var layerData = WmtsViewer.createLayer(layerName, $scope.service.identifier, capabilities);
                        WmtsViewer.map.addLayer(layerData);
                        var maxExtent = capabilities.contents.layers[0].bounds;
                        WmtsViewer.map.zoomToExtent(maxExtent, true);
                        modalLoader.close();
                    });
            } else {
                var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
                var layerData;
                if ($scope.service.type === 'WMS') {
                    textService.capa($scope.service.type.toLowerCase(), $scope.service.identifier, $scope.service.versions[0])
                        .success(function (data, status, headers, config) {
                            var capabilities = DataViewer.format.read(data);
                            var layers = capabilities.capability.layers;
                            var capsLayer;
                            for(var i=0; i < layers.length; i++) {
                                var l = layers[i];
                                if (l.name === layerName) {
                                    capsLayer = l;
                                    break;
                                }
                            }
                            var llbbox = capsLayer.llbbox;
                            var extent = new OpenLayers.Bounds(llbbox[0], llbbox[1], llbbox[2], llbbox[3]);
                            layerData = DataViewer.createLayerWMS($cookies.cstlUrl, layerName, $scope.service.identifier);
                            DataViewer.layers = [layerData, layerBackground];
                            DataViewer.initMap('dataMap');
                            DataViewer.map.zoomToExtent(extent, true);
                            modalLoader.close();
                        });
                } else {
                    var providerId = $scope.selected.Provider;
                    layerData = DataViewer.createLayer($cookies.cstlUrl, layerName, providerId);
                    DataViewer.layers = [layerData, layerBackground];
                    dataListing.metadata({providerId: providerId, dataId: layerName}, {}, function(response) {
                        // Success getting the metadata, try to find the data extent
                        var md = response['gmd.MD_Metadata'];
                        if (md) {
                            var ident = md['gmd.identificationInfo'];
                            if (ident) {
                                var extentMD = ident['gmd.MD_DataIdentification']['gmd.extent'];
                                if (extentMD) {
                                    var bbox = extentMD['gmd.EX_Extent']['gmd.geographicElement']['gmd.EX_GeographicBoundingBox'];
                                    var extent = new OpenLayers.Bounds(bbox['gmd.westBoundLongitude']['gco.Decimal'], bbox['gmd.southBoundLatitude']['gco.Decimal'],
                                        bbox['gmd.eastBoundLongitude']['gco.Decimal'], bbox['gmd.northBoundLatitude']['gco.Decimal']);
                                    DataViewer.initMap('dataMap');
                                    DataViewer.map.zoomToExtent(extent, true);
                                }
                            }
                        } else {
                            DataViewer.initMap('dataMap');
                        }
                        modalLoader.close();
                    }, function() {
                        DataViewer.initMap('dataMap');
                        modalLoader.close();
                    });
                }
            }
        };

        $scope.showSensor = function() {
            var sensorId = ($scope.selectedSensorsChild != null) ? $scope.selectedSensorsChild.id : $scope.selected.id;
            $modal.open({
                templateUrl: 'views/modalSensorView.html',
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
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        // Style methods
        $scope.showStyleList = function() {
            StyleSharedService.showStyleList($scope);
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style);
        };
    }]);

cstlAdminApp.controller('SensorModalController', ['$scope', '$modalInstance', '$modal', '$cookies', 'sos', 'service', 'sensorId', '$growl', '$http',
    function ($scope, $modalInstance, $modal, $cookies, sos, service, sensorId, $growl, $http) {
        $scope.service = service;
        $scope.sensorId = sensorId;
        $scope.measures = undefined;
        $scope.var = {
            displayGraph:  false,
            needToSelectMeasure: false,
            start: '',
            end: ''
        };

        $scope.init = function() {
            sos.measuresForSensor({id: service.identifier, 'sensorID': sensorId}, function(measures){
                var oldMeasures = $scope.measures;

                $scope.measures = [];
                for (var i=0; i<measures.Entry.length; i++) {
                    var newMeasureId = measures.Entry[i];
                    var check = false;
                    if (oldMeasures != null) {
                        for (var j = 0; j < oldMeasures.length; j++) {
                            // Get back old values checked or not for new measures that match the chosen sensor
                            var oldMeasure = oldMeasures[j];
                            if (oldMeasure.id === newMeasureId) {
                                check = oldMeasure.checked;
                                break;
                            }
                        }
                    }
                    $scope.measures[i] = {id: newMeasureId, checked:check};
                }
            }, function() { $growl('error','Error','Unable to list measures for sensor '+ sensorId); });
        };

        $scope.initMap = function() {
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            var newLayer = DataViewer.createSensorsLayer("sensors");
            sos.getFeatures({id: $scope.service.identifier, sensor: $scope.sensorId}, function(wkt) {
                var wktReader = new OpenLayers.Format.WKT();
                var vector = wktReader.read(wkt.value);
                vector.sensorName = $scope.sensorId;
                var vectorClass = vector.geometry.CLASS_NAME;
                switch (vectorClass) {
                    case 'OpenLayers.Geometry.Point': DataViewer.setSensorStyle('point', newLayer); break;
                    case 'OpenLayers.Geometry.MultiPoint': DataViewer.setSensorStyle('point', newLayer); break;
                    case 'OpenLayers.Geometry.LineString': DataViewer.setSensorStyle('line', newLayer); break;
                    case 'OpenLayers.Geometry.MultiLineString': DataViewer.setSensorStyle('line', newLayer); break;
                    case 'OpenLayers.Geometry.Polygon': DataViewer.setSensorStyle('polygon', newLayer); break;
                    case 'OpenLayers.Geometry.MultiPolygon': DataViewer.setSensorStyle('polygon', newLayer); break;
                    default: break;
                }

                newLayer.addFeatures(vector);
            });

            DataViewer.layers = [layerBackground,newLayer];
            DataViewer.initMap('olSensorMap');
        };

        function getMeasuresChecked() {
            var checked = [];
            for (var i=0; i<$scope.measures.length; i++) {
                var measure = $scope.measures[i];
                if (measure.checked) {
                    checked.push(measure.id);
                }
            }
            return checked;
        }

        function getAllMeasures() {
            var allMeasures = [];
            for (var i=0; i<$scope.measures.length; i++) {
                var measure = $scope.measures[i];
                allMeasures.push(measure.id);
            }
            return allMeasures;
        }

        $scope.showGraph = function() {
            var measuresChecked = getMeasuresChecked();
            if (measuresChecked.length === 0) {
                var allMeasures = getAllMeasures();
                if (allMeasures.length === 1) {
                    measuresChecked = allMeasures;
                } else {
                    // Please select one or more measure(s) in the list
                    $scope.var.needToSelectMeasure = true;
                    return;
                }
            }

            $scope.var.displayGraph = true;

            var obsFilter = {
                'sensorID': $scope.sensorId,
                'observedProperty': measuresChecked
            };
            if ($scope.var.start !== '' && $scope.var.end !== '') {
                obsFilter.start = $scope.var.start;
                obsFilter.end = $scope.var.end;
            }
            $http.post('@cstl/api/1/SOS/'+ $scope.service.identifier +'/observations;jsessionid=', obsFilter)
                .success(function(response){
                    generateD3Graph(response, measuresChecked);
                });
        };

        $scope.clickMeasure = function(measure) {
            $scope.var.needToSelectMeasure = false;
        };

        function generateD3Graph(csv, measures) {

            var margin = {top: 10, right: 70, bottom: 30, left: 50},
                width = $('.sos_edit_graph').width() - margin.left - margin.right,
                height = $('.sos_edit_graph').height() - margin.top - margin.bottom;

            var parseDate = d3.time.format("%Y-%m-%dT%H:%M:%S").parse;

            var x;
            if (measures.length === 1) {
                x = d3.time.scale().range([0, width]);
            } else {
                x = d3.scale.linear().range([0, width]);
            }
            var y = d3.scale.linear().range([height, 0]);

            var xAxis = d3.svg.axis().scale(x).orient("bottom");
            var yAxis = d3.svg.axis().scale(y).orient("left");


            var line;
            if (measures.length === 1) {
                line = d3.svg.line()
                    .x(function (d) { return x(d.date); })
                    .y(function (d) { return y(d[measures[0]]); });
            } else {
                line = d3.svg.line()
                    .x(function (d) { return x(d[measures[0]]); })
                    .y(function (d) { return y(d[measures[1]]); });
            }

            var svg = d3.select("#graph").append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

            var data = d3.csv.parse(csv);

            // Hack to limit number of values
            var originalLength = data.length;
            if (originalLength > 10000) {
                data.splice(10000, originalLength - 10000);
            }

            data.forEach(function(d) {
                if (measures.length === 1) {
                    d.date = parseDate(d.date);
                    d[measures[0]] = +d[measures[0]];
                } else {
                    d[measures[0]] = +d[measures[0]];
                    d[measures[1]] = +d[measures[1]];
                }
            });

            if (measures.length === 1) {
                x.domain(d3.extent(data, function (d) {
                    return d.date;
                }));
                y.domain(d3.extent(data, function (d) {
                    return d[measures[0]];
                }));
            } else {
                x.domain(d3.extent(data, function (d) {
                    return d[measures[0]];
                }));
                y.domain(d3.extent(data, function (d) {
                    return d[measures[1]];
                }));
            }

            svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);

            svg.append("g")
                .attr("class", "y axis")
                .call(yAxis)
                .append("text")
                .attr("transform", "rotate(-90)")
                .attr("y", 6)
                .attr("dy", ".71em")
                .style("text-anchor", "end")
                .text(measures[0]);

            svg.append("path")
                .datum(data)
                .attr("class", "line")
                .attr("d", line);
        }

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };
    }]);
