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

angular.module('cstl-sensor-view', ['ngCookies', 'cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('SensorModalController', function($scope, $modalInstance, $modal, $cookies, sos, service, sensorId, Growl, $http) {
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
                    if (oldMeasures) {
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
            var jquerySel = '.sos_edit_graph';
            var margin = {top: 10, right: 70, bottom: 30, left: 50},
                width = $(jquerySel).width() - margin.left - margin.right,
                height = $(jquerySel).height() - margin.top - margin.bottom;

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
                    .x(function (d) { return x(d.time); })
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
                console.debug(d);
                if (measures.length === 1) {
                    d.time = parseDate(d.time);
                    d[measures[0]] = +d[measures[0]];
                } else {
                    d[measures[0]] = +d[measures[0]];
                    d[measures[1]] = +d[measures[1]];
                }
            });

            if (measures.length === 1) {
                x.domain(d3.extent(data, function (d) {
                    return d.time;
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
    });