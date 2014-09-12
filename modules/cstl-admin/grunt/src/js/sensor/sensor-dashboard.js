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

angular.module('cstl-sensor-dashboard', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('SensorsController', function($scope, Dashboard, webService, sensor, $modal, Growl, $window) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.hideScroll = true;

        $scope.init = function() {
            sensor.list({}, function(response) {//success
                Dashboard($scope, response.children, false);
            }, function() {//error
                Growl('error','Error','Unable to show sensor list!');
            });
            angular.element($window).bind("scroll", function() {
                if (this.pageYOffset < 220) {
                    $scope.hideScroll = true;
                } else {
                    $scope.hideScroll = false;
                }
                $scope.$apply();
            });
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#dataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };

        // Data loading
        $scope.addSensor = function() {
            var modal = $modal.open({
                templateUrl: 'views/sensor/modalAddSensor.html',
                controller: 'SensorAddModalController'
            });

            modal.result.then(function() {
                sensor.list({}, function(sensors) {
                    Dashboard($scope, sensors.children, false);
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
                var idToDel = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                sensor.delete({sensor: idToDel}, function () {
                    Growl('success', 'Success', 'Sensor ' + idToDel + ' successfully removed');
                    $scope.init();
                }, function () {
                    Growl('error', 'Error', 'Unable to remove sensor ' + idToDel);
                });
            }
        };

        $scope.showSensor = function() {
            var idToView = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
            $modal.open({
                templateUrl: 'views/sensor/modalViewSensorMetadata.html',
                controller: 'ViewSensorMLModalController',
                resolve: {
                    'details': function(textService){
                        return textService.sensorMetadata(idToView);
                    }
                }
            });
        };

        $scope.truncate = function(small, text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (small === true && text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else if (small === false && text.length > 60) {
                        return text.substr(0, 60) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small === true && text.length > 22) {
                        return text.substr(0, 22) + "...";
                    } else if (small === false && text.length > 42) {
                        return text.substr(0, 42) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 22) {
                        return text.substr(0, 22) + "...";
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
    })

    .controller('ViewSensorMLModalController', function ($scope, $modalInstance, details) {
        $scope.details = details.data;
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };
    })

    .controller('SensorModalChooseController', function($scope, $modalInstance, Dashboard, dataListing, sensor, selectedData){
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        sensor.list({}, function(response) {
            Dashboard($scope, response.children, false);
            $scope.wrap.nbbypage = 5;
        });

        $scope.selectedSensorsChild = null;

        $scope.selectSensorsChild = function(item) {
            if ($scope.selectedSensorsChild === item) {
                $scope.selectedSensorsChild = null;
            } else {
                $scope.selectedSensorsChild = item;
            }
        };

        $scope.choose = function() {
            var sensorId = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
            dataListing.linkToSensor({providerId: selectedData.Provider, dataId: selectedData.Name, sensorId: sensorId}, {value: selectedData.Namespace},
                function() {
                    selectedData.TargetSensor.push(sensorId);
                });

            $modalInstance.dismiss('close');
        };

        $scope.truncate = function(text){
            if(text) {
                if (text.length > 30) {
                    return text.substr(0, 30) + "...";
                } else {return text;}
            }
        };
    });