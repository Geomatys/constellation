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

angular.module('cstl-webservice-common', ['cstl-restapi', 'ui.bootstrap.modal'])

    .controller('WebServiceUtilsController', function($scope, webService, $modalInstance, details) {

        $scope.details = details.data;

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };
    })

    .controller('WebServiceVersionsController', function($scope, webService, $modalInstance, service) {

        $scope.service = service;

        $scope.versions = service.versions;

        $scope.chooseVersion = function(version) {
            $modalInstance.close(version);
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };
    });