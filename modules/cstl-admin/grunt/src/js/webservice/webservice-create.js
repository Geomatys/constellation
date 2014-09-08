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

angular.module('cstl-webservice-create', ['cstl-restapi', 'cstl-services', 'pascalprecht.translate'])

    .controller('WebServiceCreateController', function($scope, $routeParams, webService, $filter, $location, Growl, $translate) {
        $scope.type = $routeParams.type;
        $scope.tonext = true;
        $scope.serviceInfo = true;
        $scope.serviceContact = false;
        $scope.serviceRights = false;
        $scope.metadata = {
            keywords: []
        };
        $scope.newService = {
            tagText: ''
        };

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
            if($scope.metadata.name!== null || $scope.metadata.identifier!==null){
                $scope.serviceContact = true;
                $scope.serviceInfo = false;
                $scope.serviceRights = false;
            } else {
                $scope.invalideName=true;
            }
        };
        $scope.goToServiceRights = function() {
            if($scope.metadata.name!== null || $scope.metadata.identifier!==null){
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
            if (!$scope.newService.tagText || $scope.newService.tagText === '' || $scope.newService.tagText.length === 0) {
                return;
            }

            $scope.metadata.keywords.push($scope.newService.tagText);
            $scope.newService.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.newService.tagText.length === 0 &&
                key === undefined) {
                $scope.metadata.keywords.pop();
            } else if (key !== undefined) {
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
            return $.inArray(currentVersion, $scope.metadata.versions) > -1;
        };

        $scope.saveServiceMetadata = function() {
            // Ensures both name and identifier are filled
            if ((!$scope.metadata.identifier || $scope.metadata.identifier === '') && $scope.metadata.name && $scope.metadata.name !== '') {
                $scope.metadata.identifier = $scope.metadata.name;
            }
            if ((!$scope.metadata.name || $scope.metadata.name === '') && $scope.metadata.identifier && $scope.metadata.identifier !== '') {
                $scope.metadata.name = $scope.metadata.identifier;
            }

            $scope.metadata.lang = $scope.getCurrentLang();

            webService.create({type: $scope.type}, $scope.metadata,
                function() {
                    Growl('success', 'Success', 'Service ' + $scope.metadata.name + ' successfully created');
                    if ($scope.type === 'csw' || $scope.type === 'sos') {
                        $location.path('/webservice/'+ $scope.type +'/'+ $scope.metadata.identifier +'/source');
                    } else {
                        $location.path('/webservice');
                    }
                },

                function() { Growl('error','Error','Service '+ $scope.metadata.name +' creation failed'); }
            );
        };
    })

    .controller('WebServiceChooseSourceController', function($scope, $routeParams , webService, provider, sos, Growl, $location) {
        $scope.type = $routeParams.type;
        $scope.id = $routeParams.id;
        $scope.transactional = {
            choice: null
        };
        $scope.db = {
//            'url': 'localhost',
//            'port': '5432',
//            'className': 'org.postgresql.Driver',
//            'name': ''
        };

        $scope.initSource = function() {
            if ($scope.type === 'csw') {
                $scope.source = {'automatic': {'@format': null}};
            } else if ($scope.type === 'sos') {
                $scope.source = {'constellation-config.SOSConfiguration': {
                    'profile': 'discovery',
                    'constellation-config.SMLConfiguration': {
                        '@format': 'filesystem',
                        'dataDirectory': ''
                    },
                    'constellation-config.observationFilterType': 'om2',
                    'constellation-config.observationReaderType': 'om2',
                    'constellation-config.observationWriterType': 'om2',
                    'constellation-config.SMLType': 'filesystem',
                    'constellation-config.OMConfiguration': {
                        '@format': 'OM2',
                        'bdd': {}
                    }
                }
                };
            }
        };

        $scope.saveServiceSource = function() {
            if ($scope.transactional.choice) {
                $scope.source['constellation-config.SOSConfiguration'].profile = 'transactional';
            }

            var fullDbUrl = ($scope.db.className === 'org.postgresql.Driver') ? 'jdbc:postgresql' : 'jdbc:mysql';
            fullDbUrl += '://'+ $scope.db.url +':'+ $scope.db.port +'/'+ $scope.db.name;
            if ($scope.type !== 'csw') {
                $scope.source['constellation-config.SOSConfiguration']['constellation-config.OMConfiguration'].bdd.className = $scope.db.className;
                $scope.source['constellation-config.SOSConfiguration']['constellation-config.OMConfiguration'].bdd.connectURL = fullDbUrl;
            }

            webService.setConfig({type: $scope.type, id: $scope.id}, $scope.source, function() {
                Growl('success','Success','Service '+ $scope.id +' successfully updated');
                if ($scope.type.toLowerCase() === 'sos') {
                    //@TODO confirm if we need to create OM provider here?
                    //createOmProvider();
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
    });