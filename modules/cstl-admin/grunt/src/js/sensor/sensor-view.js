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

angular.module('cstl-sensor-view', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('SensorModalController', function($scope, $modalInstance, $modal, $cookieStore, sos, service, sensorId, Growl, $http, StompService) {
        $scope.service = service;
        $scope.sensorId = sensorId;
        $scope.measures = undefined;
        $scope.var = {
            displayGraph:  false,
            displayRealTimeGraph:  false,
            displayMesureSelector: true,
            needToSelectMeasure: false,
            start: '',
            end: '',
            sosdata: []
        };

        $scope.initSensorView = function() {
            StompService.connect();
            sos.measuresForSensor({id: service.identifier},{value: sensorId}, function(measures){
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
            DataViewer.initConfig();
            sos.getFeatures({id: $scope.service.identifier},{value: $scope.sensorId}, function(wkt) {
                var wktReader = new ol.format.WKT();
                var features = wktReader.readFeatures(wkt.value,{"dataProjection":ol.proj.get('EPSG:4326'),
                                                                 "featureProjection":ol.proj.get(DataViewer.projection)});
                if (features) {
                    var newLayer = DataViewer.createSensorsLayer($scope.sensorId);
                    determineGeomClass(features, newLayer);
                    newLayer.getSource().addFeatures(features);
                    DataViewer.layers = [newLayer];
                    DataViewer.initMap('olSensorMap');
                    // select interaction working on "click"
                    window.selectClick = new ol.interaction.Select({
                        condition: ol.events.condition.click
                    });
                    DataViewer.map.addInteraction(window.selectClick);
                } else {
                    DataViewer.initMap('olSensorMap');
                }
            });
        };

        function determineGeomClass(features, layer) {
            if(features && features.length > 0){
                var feature = features[0];
                var geomType = feature.getGeometry().getType();
                switch (geomType) {
                    case 'Point': DataViewer.setSensorStyle('point', layer); break;
                    case 'MultiPoint': DataViewer.setSensorStyle('point', layer); break;
                    case 'LineString': DataViewer.setSensorStyle('line', layer); break;
                    case 'MultiLineString': DataViewer.setSensorStyle('line', layer); break;
                    case 'Polygon': DataViewer.setSensorStyle('polygon', layer); break;
                    case 'MultiPolygon': DataViewer.setSensorStyle('polygon', layer); break;
                    default: break;
                }
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

        $scope.displaySelector = function(){
            $scope.var.displayMesureSelector=true;
            $scope.var.displayGraph=false;
            $scope.var.displayRealTimeGraph=false;
        };

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
            $scope.var.displayMesureSelector = false;
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



        $scope.showRealTimeGraph = function() {
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
            $scope.var.displayMesureSelector = false;
            $scope.var.displayRealTimeGraph = true;

            var t = new Date();
            $scope.var.sosdata.push([t,0]);


            var g = new Dygraph((jQuery("#sos_realtime_graph")[0]), $scope.var.sosdata,
                {
                    drawPoints: true,
                    showRoller: true,
                    valueRange: [0.0, 12],
                    labels: ['Time', measuresChecked]
                });
            g.resize(jQuery("#sos_realtime_graph").width(), jQuery("#sos_realtime_graph").height());
            

            $scope.var.topic = StompService.subscribe('/topic/sosevents/'+$scope.sensorId, function(data) {
                var event = JSON.parse(data.body);
                console.log(event);
                var arrayLength = event.headers.length;
                for (var i = 0; i < arrayLength; i++) {
                    //console.log(event.headers[i].toLowerCase());
                    //console.log(i);
                    //console.log(measuresChecked.toLowerCase());

                    if (event.headers[i].toLowerCase() === measuresChecked[0].toLowerCase()){
                        console.log(event.values.split(event.tokenSeparator)[i]);
                        console.log(i);
                        var x = new Date();  // current time
                        var y = event.values.split(event.tokenSeparator)[i];
                        var sosdata = $scope.var.sosdata;
                        sosdata.push([x, y]);
                        if (sosdata.length > 20){
                            sosdata.shift();
                        }
                        g.updateOptions( { 'file': sosdata } );
                        var maxy = g.getOption('valueRange')[1];
                        console.log(maxy);
                        console.log(y);
                        if (y > maxy){
                            g.updateOptions( {valueRange:  [0.0, (parseFloat(y)+parseFloat(5))]});
                        }
                    }
                }
                //console.log(event.headers.);
            });
        };



        $scope.$on('$destroy', function() {
            $scope.var.topic.unsubscribe();
        });

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
                    .x(function (d) { return x(d.Time); })
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
                    d.Time = parseDate(d.Time);
                    d[measures[0]] = +d[measures[0]];
                } else {
                    d[measures[0]] = +d[measures[0]];
                    d[measures[1]] = +d[measures[1]];
                }
            });

            if (measures.length === 1) {
                x.domain(d3.extent(data, function (d) {
                    return d.Time;
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