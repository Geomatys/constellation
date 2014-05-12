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

cstlAdminApp.controller('SensorsController', ['$scope', '$dashboard', 'webService', 'sensor', '$modal', '$growl',
    function ($scope, $dashboard, webService, sensor, $modal, $growl){
        $scope.init = function() {
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            sensor.list({}, function(response) {
                $dashboard($scope, response.children, false);
                modalLoader.close();
            }, function() {
                modalLoader.close();
            });
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#dataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        // Data loading
        $scope.addSensor = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalAddSensor.html',
                controller: 'SensorAddModalController'
            });

            modal.result.then(function() {
                sensor.list({}, function(sensors) {
                    $dashboard($scope, sensors.children, false);
                    $scope.init();
                });
            });
        };

        $scope.selectedSensorsChild = null;

        $scope.selectSensorsChild = function(item) {
            if ($scope.selectedSensorsChild === item) {
                $scope.selectedSensorsChild = null;
            } else {
                $scope.selectedSensorsChild = item;
            }
        };

        $scope.deleteSensor = function() {
            if (confirm("Are you sure?")) {
                var idToDel = ($scope.selectedSensorsChild !== null) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                sensor.delete({sensor: idToDel}, function () {
                    $growl('success', 'Success', 'Sensor ' + idToDel + ' successfully removed');
                    $scope.init();
                }, function () {
                    $growl('error', 'Error', 'Unable to remove sensor ' + idToDel);
                });
            }
        };
    }]);

cstlAdminApp.controller('SensorAddModalController', ['$scope', '$modalInstance', 'sensor', '$growl', '$cookies',
    function ($scope, $modalInstance, sensor, $growl, $cookies) {
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.uploadData = function() {
            var $form = $('#uploadSensor');

            var formData = new FormData($form[0]);

            $.ajax({
                url: $cookies.cstlUrl + "api/1/data/upload/data;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function (path) {
                    importSensor(path);
                }
            });
        };

        function importSensor(path) {
            sensor.add({}, {values: {'path' : path}}, function() {
                $growl('success','Success','Sensor correctly imported');
                $modalInstance.close();
            }, function() {
                $growl('error','Error','Unable to import sensor');
                $modalInstance.dismiss('close');
            });
        }
    }]);