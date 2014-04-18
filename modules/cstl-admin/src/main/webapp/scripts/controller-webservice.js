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

cstlAdminApp.controller('WebServiceController', ['$scope', 'webService', 'csw', '$modal', 'textService', '$growl',
    function ($scope, webService, csw, $modal, textService, $growl) {

        $scope.services = webService.listAll();

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
            return [];
        };
        $scope.versions = $scope.getVersionsForType();


        $scope.goToServiceContact = function() {
            $scope.tonext = false;
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
                        $location.path('/webservice/'+ $scope.type +'/'+ $scope.metadata.name +'/source');
                    } else {
                        $location.path('/webservice');
                    }
                },

                function() { $growl('error','Error','Service '+ $scope.metadata.name +' creation failed'); }
            );
        };
    }]);

cstlAdminApp.controller('WebServiceChooseSourceController', ['$scope','$routeParams', 'webService', '$growl', '$location',
    function ($scope, $routeParams , webService, $growl, $location) {
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
                $location.path('/webservice');
            }, function() {
                $growl('error','Error','Service configuration update error');
            });
        };
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
                    csw.getRecords({id: $routeParams.id, count: max.asInt, startIndex: 0}, {}, function(response) {
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
            } else {
                $scope.config = webService.config({type: $scope.type, id:$routeParams.id});
                $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                    $dashboard($scope, response, true);
                    $scope.filtertype = "";
                });
            }
        };

        $scope.sensors = undefined;
        $scope.measures = undefined;
        $scope.initSensors = function() {
            sos.listSensors({id: $routeParams.id}, function(response) {
                $scope.sensors = response.Entry;
            }, function() { $growl('error','Error','Unable to list sensors'); });

            sos.listMeasures({id: $routeParams.id}, function(response) {
                $scope.measures = response.Entry;
            }, function() { $growl('error','Error','Unable to list measures'); });
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
                $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                    $scope.fullList = response;
                });
            });
        };

        $scope.deleteLayer = function() {
            var txt = ($scope.service.type.toLowerCase() === 'wmts') ? 'Are you sure? This will also delete the generated tiles for this layer.' : 'Are you sure?';
            if ($scope.selected != null && confirm(txt)) {
                webService.deleteLayer({type: $scope.service.type, id: $scope.service.identifier, layerid: $scope.selected.Name}, {layernamespace: ''},
                    function() {
                        if ($scope.service.type.toLowerCase() === 'wmts' || $scope.service.type.toLowerCase() === 'wms') {
                            $scope.deleteTiledData($scope.service, $scope.selected.Name, $scope.selected.Provider);
                        }

                        $growl('success','Success','Layer '+ $scope.selected.Name +' successfully deleted from service '+ $scope.service.name);
                        $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                            $scope.fullList = response;
                        });
                    },
                    function() {$growl('error','Error','Layer '+ $scope.selected.Name +' failed to be deleted from service '+ $scope.service.name);}
                );
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
                        });
                } else {
                    var providerId = $scope.selected.Provider;
                    layerData = DataViewer.createLayer($cookies.cstlUrl, layerName, providerId);
                    DataViewer.layers = [layerData, layerBackground];

                    dataListing.metadata({providerId: providerId, dataId: layerName}, {}, function(response) {
                        // Success getting the metadata, try to find the data extent
                        var ident = response['gmd.MD_Metadata']['gmd.identificationInfo'];
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
                    }, function() {
                        DataViewer.initMap('dataMap');
                    });
                }
            }
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

        $scope.selectedDataMetadata = null;

        $scope.selectDataMetadata = function(item) {
            $scope.selectedDataMetadata = item;
        };
    }]);
