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

cstlAdminApp.controller('SensorsController', ['$scope', '$dashboard', 'webService', 'sensor', '$modal', 'Growl','$window',
    function ($scope, $dashboard, webService, sensor, $modal, Growl, $window){
        $scope.hideScroll = true;

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
                    Growl('success', 'Success', 'Sensor ' + idToDel + ' successfully removed');
                    $scope.init();
                }, function () {
                    Growl('error', 'Error', 'Unable to remove sensor ' + idToDel);
                });
            }
        };

        $scope.showSensor = function() {
            var idToView = ($scope.selectedSensorsChild !== null) ? $scope.selectedSensorsChild.id : $scope.selected.id;
            $modal.open({
                templateUrl: 'views/sensor/modalViewSensorMetadata.html',
                controller: 'ViewMetadataModalController',
                resolve: {
                    'details': function(textService){
                        return textService.sensorMetadata(idToView);
                    }
                }
            });
        };

        $scope.truncate = function(small, text){
            if(text != null) {
                if (window.innerWidth >= 1200) {
                    if (small == true && text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else if (small == false && text.length > 60) {
                        return text.substr(0, 60) + "...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small == true && text.length > 22) {
                        return text.substr(0, 22) + "...";
                    } else if (small == false && text.length > 42) {
                        return text.substr(0, 42) + "...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if (text.length > 22) {
                        return text.substr(0, 22) + "...";
                    } else return text;
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text != null) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if (text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else return text;
                }
            }
        };
    }]);

cstlAdminApp.controller('SensorAddModalController', ['$scope', '$modalInstance', 'sensor', 'Growl', '$cookies',
    function ($scope, $modalInstance, sensor, Growl, $cookies) {
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.uploadData = function() {
            var $form = $('#uploadSensor');

            var formData = new FormData($form[0]);

            $.ajax({
                url: $cookies.cstlUrl + "api/1/sensor/upload;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    Growl('success','Success','Sensor correctly imported');
                    $modalInstance.close();
                },
                error: function (data){
                    Growl('error','Error','Unable to import sensor');
                    $modalInstance.dismiss('close');
                }
            });
        };
    }]);

cstlAdminApp.controller('SensorModalChooseController', ['$scope', '$modalInstance', '$dashboard', 'dataListing', 'sensor', 'selectedData', 'Growl',
    function ($scope, $modalInstance, $dashboard, dataListing, sensor, selectedData, Growl){
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        sensor.list({}, function(response) {
            $dashboard($scope, response.children, false);
            $scope.nbbypage = 5;
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
            var sensorId = ($scope.selectedSensorsChild !== null) ? $scope.selectedSensorsChild.id : $scope.selected.id;
            dataListing.linkToSensor({providerId: selectedData.Provider, dataId: selectedData.Name, sensorId: sensorId}, {value: selectedData.Namespace},
                function() {
                    selectedData.TargetSensor.push(sensorId);
                });

            $modalInstance.dismiss('close');
        };

        $scope.truncate = function(text){
            if(text != null) {
                if (text.length > 30) {
                    return text.substr(0, 30) + "...";
                } else return text;
            }
        };
    }]);

cstlAdminApp.controller('SensorModalController', ['$scope', '$modalInstance', '$modal', '$cookies', 'sos', 'service', 'sensorId', 'Growl', '$http',
    function ($scope, $modalInstance, $modal, $cookies, sos, service, sensorId, Growl, $http) {
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
            }, function() { Growl('error','Error','Unable to list measures for sensor '+ sensorId); });
        };

        $scope.initMap = function() {
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            sos.getFeatures({id: $scope.service.identifier, sensor: $scope.sensorId}, function(wkt) {
                var wktReader = new OpenLayers.Format.WKT();
                var vector = wktReader.read(wkt.value);
                if (vector.geometry) {
//                if (Array.isArray(vector)) {
//                    DataViewer.layers = [layerBackground];
//                    for (var i=0; i<vector.length; i++) {
//                        var v = vector[i];
//                        v.sensorName = $scope.sensorId;
//                        var newLayer = DataViewer.createSensorsLayer($scope.sensorId);
//                        DataViewer.layers.push(newLayer);
//                    }
//                    DataViewer.initMap('olSensorMap');
//                } else {
                    vector.sensorName = $scope.sensorId;
                    var newLayer = DataViewer.createSensorsLayer($scope.sensorId);
                    determineGeomClass(vector, newLayer);
                    newLayer.addFeatures(vector);
                    DataViewer.layers = [layerBackground, newLayer];
                    DataViewer.initMap('olSensorMap');
//                }
                } else {
                    DataViewer.layers = [layerBackground];
                    DataViewer.initMap('olSensorMap');
                }
            });
        };

        function determineGeomClass(vector, layer) {
            var vectorClass = vector.geometry.CLASS_NAME;
            switch (vectorClass) {
                case 'OpenLayers.Geometry.Point': DataViewer.setSensorStyle('point', layer); break;
                case 'OpenLayers.Geometry.MultiPoint': DataViewer.setSensorStyle('point', layer); break;
                case 'OpenLayers.Geometry.LineString': DataViewer.setSensorStyle('line', layer); break;
                case 'OpenLayers.Geometry.MultiLineString': DataViewer.setSensorStyle('line', layer); break;
                case 'OpenLayers.Geometry.Polygon': DataViewer.setSensorStyle('polygon', layer); break;
                case 'OpenLayers.Geometry.MultiPolygon': DataViewer.setSensorStyle('polygon', layer); break;
                default: break;
            }
        }

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

