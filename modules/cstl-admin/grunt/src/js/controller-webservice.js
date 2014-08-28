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

cstlAdminApp.controller('WebServiceController', ['$scope', 'webService', 'provider', 'csw', 'sos', '$modal', 'textService', 'Growl','$translate', '$window',
    function ($scope, webService, provider, csw, sos, $modal, textService, Growl, $translate, $window) {

        $scope.typeFilter = {type: '!WEBDAV'};
        $scope.hideScroll = true;
        $scope.hideScrollServices = true;

        var modalLoader = $modal.open({
          templateUrl: 'views/modalLoader.html',
          controller: 'ModalInstanceCtrl'
        });

        $scope.getCurrentLang = function() {
            return $translate.use();
        };

        angular.element($window).bind("scroll", function() {
            if (this.pageYOffset < 220) {
                $scope.hideScrollServices = true;
            } else {
                $scope.hideScrollServices = false;
            }
            $scope.$apply();
        });
        webService.listAll({lang: $scope.getCurrentLang()}, function(response){
            $scope.services = response;
            modalLoader.close();
        }, function() {
            modalLoader.close();
        });


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
        };

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
                        $scope.services = webService.listAll({lang: $scope.getCurrentLang()});
                        Growl('success','Success','Service '+ service.name +' successfully stopped');
                    }
                }, function() { Growl('error','Error','Service '+ service.name +' stop failed'); });
            }else{
                webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.services = webService.listAll({lang: $scope.getCurrentLang()});
                        Growl('success','Success','Service '+ service.name +' successfully started');
                    }
                }, function() { Growl('error','Error','Service '+ service.name +' start failed'); });
            }
        };

        $scope.deleteService = function(service) {
            if (confirm("Are you sure?")) {
                if (service.type.toLowerCase() === 'sos') {
                    // A provider has been created for this SOS service, so remove it
                    provider.delete({id: service.identifier +"-om2"});
                }
                webService.delete({type: service.type, id: service.identifier}, {} ,
                    function() { Growl('success','Success','Service '+ service.name +' successfully deleted');
                        $scope.services = webService.listAll({lang: $scope.getCurrentLang()}); },
                    function() { Growl('error','Error','Service '+ service.name +' deletion failed'); }
                );
            }
        };

        $scope.refreshIndex = function(service) {
            csw.refresh({id: service.identifier}, {},
                function() { Growl('success','Success','Search index for the service '+ service.name +' successfully refreshed'); },
                function() { Growl('error','Error','Search index for the service '+ service.name +' failed to be updated'); }
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

cstlAdminApp.controller('WebServiceCreateController', ['$scope','$routeParams', 'webService', '$filter', '$location', 'Growl', '$translate',
    function ($scope, $routeParams, webService, $filter, $location, Growl, $translate) {
        $scope.type = $routeParams.type;
        $scope.tonext = true;
        $scope.serviceInfo = true;
        $scope.serviceContact = false;
        $scope.serviceRights = false;
        $scope.metadata = {};
        $scope.metadata.keywords = [];
        $scope.newService = {};
        $scope.newService.tagText = '';

        $scope.getCurrentLang = function() {
            return $translate.use();
        };


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
                $scope.serviceContact = true;
                $scope.serviceInfo = false;
                $scope.serviceRights = false;
            } else {
                $scope.invalideName=true;
            }
        };
        $scope.goToServiceRights = function() {
            if($scope.metadata.name!= null || $scope.metadata.identifier!=null){
                $scope.serviceContact = false;
                $scope.serviceRights = true;
                $scope.serviceInfo = false;
            } else {
                $scope.invalideName=true;
            }
        };

        $scope.goToServiceInfo = function() {
            $scope.serviceContact = false;
            $scope.serviceRights = false;
            $scope.serviceInfo = true;
        };

        $scope.addTag = function() {
            if (!$scope.newService.tagText || $scope.newService.tagText == '' || $scope.newService.tagText.length == 0) {
                return;
            }

            $scope.metadata.keywords.push($scope.newService.tagText);
            $scope.newService.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.newService.tagText.length == 0 &&
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

            $scope.metadata.lang = $scope.getCurrentLang();

            webService.create({type: $scope.type}, $scope.metadata,
                function() {
                    Growl('success', 'Success', 'Service ' + $scope.metadata.name + ' successfully created');
                    if ($scope.type == 'csw' || $scope.type == 'sos') {
                        $location.path('/webservice/'+ $scope.type +'/'+ $scope.metadata.identifier +'/source');
                    } else {
                        $location.path('/webservice');
                    }
                },

                function() { Growl('error','Error','Service '+ $scope.metadata.name +' creation failed'); }
            );
        };
    }]);

cstlAdminApp.controller('WebServiceChooseSourceController', ['$scope','$routeParams', 'webService', 'provider', 'sos', 'Growl', '$location',
    function ($scope, $routeParams , webService, provider, sos, Growl, $location) {
        $scope.type = $routeParams.type;
        $scope.id = $routeParams.id;
//        $scope.db = {
//            'url': 'localhost',
//            'port': '5432',
//            'className': 'org.postgresql.Driver',
//            'name': ''
//        };

        $scope.initSource = function() {
            if ($scope.type === 'csw') {
                $scope.source = {'automatic': {'@format': null}};
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
//            var fullDbUrl = ($scope.db.className === 'org.postgresql.Driver') ? 'jdbc:postgresql' : 'jdbc:mysql';
//            fullDbUrl += '://'+ $scope.db.url +':'+ $scope.db.port +'/'+ $scope.db.name;
            if ($scope.type === 'csw') {
//                $scope.source.automatic.bdd.className = $scope.db.className;
//                $scope.source.automatic.bdd.connectURL = fullDbUrl;
            } else {
                $scope.source['constellation-config.SOSConfiguration']['constellation-config.OMConfiguration'].bdd.className = $scope.db.className;
                $scope.source['constellation-config.SOSConfiguration']['constellation-config.OMConfiguration'].bdd.connectURL = fullDbUrl;
            }

            webService.setConfig({type: $scope.type, id: $scope.id}, $scope.source, function() {
                Growl('success','Success','Service '+ $scope.id +' successfully updated');
                if ($scope.type.toLowerCase() === 'sos') {
                    createOmProvider();
                    buildOmDatasource();
                }
                $location.path('/webservice');
            }, function() {
                Growl('error','Error','Service configuration update error');
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
                Growl('error','Error','Unable to create OM2 provider');
            });
        }
        function buildOmDatasource() {
            sos.build({
                id: $scope.id
            }, function() {}, function() {
                Growl('error','Error','Unable to build OM2 datasource');
            });
        }
    }]);

cstlAdminApp.controller('WebServiceEditController', ['$rootScope', '$scope','$routeParams', 'webService', 'dataListing', 'provider', 'csw', 'sos', '$modal','textService', '$dashboard', 'Growl', '$filter', 'DomainResource' ,'StyleSharedService','style','$cookies','$translate','$window',
    function ($rootScope, $scope, $routeParams , webService, dataListing, provider, csw, sos, $modal, textService, $dashboard, Growl, $filter, DomainResource,StyleSharedService, style, $cookies, $translate, $window) {
        $scope.tagText = '';
        $scope.type = $routeParams.type;
        $scope.url = $cookies.cstlUrl + "WS/" + $routeParams.type + "/" + $routeParams.id;
        $scope.cstlUrl = $cookies.cstlUrl;
        $scope.cstlSessionId = $cookies.cstlSessionId;
        $scope.urlBoxSize = Math.min($scope.url.length,100);
        $scope.domainId = $cookies.cstlActiveDomainId;
        $scope.writeOperationAvailable = $scope.type == 'csw' || $scope.type == 'sos' || $scope.type == 'wfs';
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
                        })
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
                        $dashboard($scope, response.BriefNode, false);
                        $scope.filtertype = "";

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
                    $dashboard($scope, sensors.children, false);
                    $scope.layers = sensors.children;

                }, function() { Growl('error','Error','Unable to list sensors'); });
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
                $scope.metadata.versions.push(selVersions[i].id);
            }
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
                        Growl('success','Success','Service description successfully updated');
                    }else{
                        Growl('error','Error','Service description update failed due to :'+response.status);
                    }
                },
                function() {
                    Growl('error','Error','Service description update failed');
                }
            )
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
        };

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
                                $scope.fullList = response;
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
            if ($scope.selected != null && confirm("Are you sure?")) {
                csw.delete({id: $scope.service.identifier, metaId: $scope.selected.identifier}, {},
                    function() {
                        Growl('success','Success','Metadata deleted');
                        csw.count({id: $routeParams.id}, {}, function(max) {
                            csw.getRecords({id: $routeParams.id, count: max.asInt, startIndex: 0}, {}, function(response) {
                                $dashboard($scope, response.BriefNode, false);
                                $scope.filtertype = "";
                            });
                        });
                    }, function() { Growl('error','Error','Failed to delete metadata'); }
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
                        WmtsViewer.initMap('dataMap');
                        var layerData = WmtsViewer.createLayer(layerName, $scope.service.identifier, capabilities);
                        WmtsViewer.map.addLayer(layerData);
                        WmtsViewer.map.zoomToMaxExtent();
                        modalLoader.close();
                    });
            } else {
                var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
                var layerData;
//                if ($scope.service.type === 'WMS') {
//                    textService.capa($scope.service.type.toLowerCase(), $scope.service.identifier, $scope.service.versions[0])
//                        .success(function (data, status, headers, config) {
//                            var capabilities = DataViewer.format.read(data);
//                            var layers = capabilities.capability.layers;
//                            var capsLayer;
//                            for(var i=0; i < layers.length; i++) {
//                                var l = layers[i];
//                                if (l.name === layerName) {
//                                    capsLayer = l;
//                                    break;
//                                }
//                            }
//                            var llbbox = capsLayer.llbbox;
//                            var extent = new OpenLayers.Bounds(llbbox[0], llbbox[1], llbbox[2], llbbox[3]);
//                            layerData = DataViewer.createLayerWMS($cookies.cstlUrl, layerName, $scope.service.identifier);
//
//                            //to force the browser cache reloading styled layer.
//                            layerData.mergeNewParams({ts:new Date().getTime()});
//
//                            DataViewer.layers = [layerData, layerBackground];
//                            DataViewer.initMap('dataMap');
//                            DataViewer.map.zoomToExtent(extent, true);
//                            modalLoader.close();
//                        });
//                } else {
                    var providerId = $scope.selected.Provider;
                    if ($scope.selected.TargetStyle && $scope.selected.TargetStyle.length > 0) {
                        layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, layerName, providerId, $scope.selected.TargetStyle[0].Name);
                    } else {
                        layerData = DataViewer.createLayer($cookies.cstlUrl, layerName, providerId);
                    }
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
//                }
            }
        };

        $scope.showSensor = function() {
            var sensorId = ($scope.selectedSensorsChild != null) ? $scope.selectedSensorsChild.id : $scope.selected.id;
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
            if(text != null) {
                if (window.innerWidth >= 1200) {
                    if (small == true && text.length > 22) {
                        return text.substr(0, 22) + "...";
                    } else if (small == false && text.length > 65) {
                        return text.substr(0, 65) + "...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small == true && text.length > 15) {
                        return text.substr(0, 15) + "...";
                    } else if (small == false && text.length > 55) {
                        return text.substr(0, 55) + "...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if (text.length > 35) {
                        return text.substr(0, 35) + "...";
                    } else return text;
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text != null) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 25) {
                        return text.substr(0, 25) + "...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if (text.length > 17) {
                        return text.substr(0, 17) + "...";
                    } else return text;
                }
            }
        };
    }]);
