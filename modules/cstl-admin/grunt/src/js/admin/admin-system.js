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

angular.module('cstl-admin-system', ['cstl-restapi', 'cstl-services'])

    .controller('MetricsController', function($scope, $window, $http, Metrics) {

        $scope.init = function() {
            $scope.metrics = Metrics.get();
        };

        $scope.rungc = function() {
            $http.get("@cstl/spring/admin/jvm/rungc;jsessionid=").then($scope.init);
        };
    })

    .controller('ContactController', function($scope, Contact) {

        $scope.data = Contact.get();

        $scope.save = function() {
            Contact.save($scope.data, onSuccess, onError);
        };

        function onSuccess() {
            $scope.error = null;
            $scope.success = 'OK';
            $scope.data = Contact.get();
        }

        function onError() {
            $scope.success = null;
            $scope.error = "ERROR";
        }
    })

    .controller('LogsController', function($scope, LogsService) {

        $scope.init = function() {
            $scope.loggers = LogsService.findAll();
        };

        $scope.changeLevel = function(name, level) {
            LogsService.changeLevel({ name: name, level: level }, $scope.init);
        };
    });
