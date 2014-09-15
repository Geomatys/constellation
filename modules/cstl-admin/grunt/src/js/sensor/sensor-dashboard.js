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

    .controller('SensorsController', function($scope, Dashboard, webService, sensor, $modal, Growl, $window, $cookies) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};
        $scope.wrap.ordertype = 'id';
        $scope.sensorCtrl = {
            cstlUrl : $cookies.cstlUrl,
            cstlSessionId : $cookies.cstlSessionId,
            domainId : $cookies.cstlActiveDomainId,
            selectedSensorsChild : null,
            smallMode : false,
            hideScroll : true
        };

        /**
         * main function of dashboard that loads the list of objects from server.
         */
        $scope.init = function() {
            sensor.list({}, function(response) {//success
                Dashboard($scope, response.children, false);
            }, function() {//error
                Growl('error','Error','Unable to show sensor list!');
            });
            angular.element($window).bind("scroll", function() {
                if (this.pageYOffset < 220) {
                    $scope.sensorCtrl.hideScroll = true;
                } else {
                    $scope.sensorCtrl.hideScroll = false;
                }
                $scope.$apply();
            });
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#sensorDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };

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

        $scope.selectSensorsChild = function(item) {
            if ($scope.sensorCtrl.selectedSensorsChild === item) {
                $scope.sensorCtrl.selectedSensorsChild = null;
            } else {
                $scope.sensorCtrl.selectedSensorsChild = item;
            }
        };

        $scope.deleteSensor = function() {
            if (confirm("Are you sure?")) {
                var idToDel = ($scope.sensorCtrl.selectedSensorsChild) ? $scope.sensorCtrl.selectedSensorsChild.id : $scope.selected.id;
                sensor.delete({sensor: idToDel}, function () {
                    Growl('success', 'Success', 'Sensor ' + idToDel + ' successfully removed');
                    $scope.init();
                }, function () {
                    Growl('error', 'Error', 'Unable to remove sensor ' + idToDel);
                });
            }
        };

        /**
         * Open metadata viewer popup and display metadata sensorML
         * this function is called from sensor dashboard.
         */
        $scope.displayMetadataSensorML = function() {
            var idToView,typeToSend;
            if(($scope.sensorCtrl.selectedSensorsChild)){
                idToView = $scope.sensorCtrl.selectedSensorsChild.id;
                typeToSend = $scope.sensorCtrl.selectedSensorsChild.type;
            }else {
                idToView = $scope.selected.id;
                typeToSend = $scope.selected.type;
            }
            $modal.open({
                templateUrl: 'views/data/modalViewMetadata.html',
                controller: 'ViewMetadataModalController',
                resolve: {
                    'dashboardName':function(){return 'sensor';},
                    'metadataValues':function(textService){
                        //@TODO the server must provide the SensorML as json.
                        return textService.sensorMetadataJson(idToView,typeToSend,true);



                    }
                }
            });
        };

        /**
         * Open metadata editor in modal popup.
         */
        $scope.displayMetadataSensorMLEditor = function() {
            var sensorId,typeToSend;
            if(($scope.sensorCtrl.selectedSensorsChild)){
                sensorId = $scope.sensorCtrl.selectedSensorsChild.id;
                typeToSend = $scope.sensorCtrl.selectedSensorsChild.type;
            }else {
                sensorId = $scope.selected.id;
                typeToSend = $scope.selected.type;
            }
            openModalEditor(sensorId,typeToSend,typeToSend);
        };

        /**
         * Open modal for metadata editor
         * for given provider id, data type and template.
         * @param id
         * @param type
         * @param template
         */
        function openModalEditor(id,type,template){
            $modal.open({
                templateUrl: 'views/data/modalEditMetadata.html',
                controller: 'EditMetadataModalController',
                resolve: {
                    'id':function(){return id;},
                    'type':function(){return type;},
                    'template':function(){return template;}
                }
            });
        }

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

    .controller('SensorModalChooseController', function($scope, $modalInstance, Dashboard, dataListing, sensor, selectedData){
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.sensorModalChooseCtrl = {
            selectedSensorsChild : null,
            hideScroll : true
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        sensor.list({}, function(response) {
            Dashboard($scope, response.children, false);
            $scope.wrap.nbbypage = 5;
        });

        $scope.selectSensorsChild = function(item) {
            if ($scope.sensorModalChooseCtrl.selectedSensorsChild === item) {
                $scope.sensorModalChooseCtrl.selectedSensorsChild = null;
            } else {
                $scope.sensorModalChooseCtrl.selectedSensorsChild = item;
            }
        };

        $scope.choose = function() {
            var sensorId = ($scope.sensorModalChooseCtrl.selectedSensorsChild) ? $scope.sensorModalChooseCtrl.selectedSensorsChild.id : $scope.selected.id;
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