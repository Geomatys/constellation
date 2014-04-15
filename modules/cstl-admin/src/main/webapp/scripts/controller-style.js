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


cstlAdminApp.controller('StylesController', ['$scope', '$dashboard', 'style', '$growl', 'StyleSharedService',
    function ($scope, $dashboard, style, $growl, StyleSharedService) {

        style.listAll({}, function(response) {
            $dashboard($scope, response.styles, false);
            $scope.filtertype = "";
        });

        $scope.deleteStyle = function() {
            if (confirm("Are you sure?")) {
                var styleName = $scope.selected.Name;
                var providerId = $scope.selected.Provider;
                style.delete({providerid: providerId, name: styleName}, {},
                    function() { $growl('success','Success','Style '+ styleName +' successfully deleted');
                        style.listAll({}, function(response) {
                            $scope.fullList = response.styles;
                        });
                    },
                    function() { $growl('error','Error','Style '+ styleName +' deletion failed'); });
            }
        };

        $scope.editStyle = function() {
            var styleName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;

        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#stylesDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        // Style methods
        $scope.showStyleCreate = function() {
            StyleSharedService.showStyleCreate($scope);
        };
    }]);

cstlAdminApp.controller('StyleModalController', ['$scope', '$dashboard', '$modalInstance', 'style', 'exclude', 'layerName', 'providerId', 'serviceName', 'dataType', '$cookies', 'dataListing', 'provider', '$growl',
    function ($scope, $dashboard, $modalInstance, style, exclude, layerName, providerId, serviceName, dataType, $cookies, dataListing, provider, $growl) {
        $scope.exclude = exclude;
        $scope.layerName = layerName;
        $scope.providerId = providerId;
        $scope.serviceName = serviceName;
        $scope.dataType = dataType;

        $scope.initFilterType = function() {
            if (dataType.toLowerCase() === 'vector' || dataType.toLowerCase() === 'feature-store') {
                return 'vector';
            }
            if (dataType.toLowerCase() === 'coverage' || dataType.toLowerCase() === 'raster') {
                return 'coverage';
            }
            return '';
        };

        $scope.stylechooser = 'new';

        // TODO: add field to handle style name
        $scope.newStyle = { "name": $scope.layerName +"-sld",
                            "rules": [{
                                "symbolizers": [{}],
                                "filter": null
                            }]
                          };

        $scope.addStrokeDashArray = function(traitType) {
            if (traitType === 'pointille') {
                if ($scope.newStyle.rules[0].symbolizers[0].stroke == undefined) {
                    $scope.newStyle.rules[0].symbolizers[0].stroke = {};
                }
                $scope.newStyle.rules[0].symbolizers[0].stroke.dashArray = [1, 1];
            } else {
                $scope.newStyle.rules[0].symbolizers[0].stroke.dashArray = null;
            }
        };

        $scope.addFontFamilies = function(font) {
            if ($scope.newStyle.rules[0].symbolizers[0].font == undefined) {
                $scope.newStyle.rules[0].symbolizers[0].font = {};
            }
            $scope.newStyle.rules[0].symbolizers[0].font.family = [];
            $scope.newStyle.rules[0].symbolizers[0].font.family[0] = font;
        };

        $scope.palette = {
            index : undefined,
            img_palette : undefined,
            rasterMinValue : undefined,
            rasterMaxValue : undefined,
            intervalles : 1,
            channelSelection : undefined,
            nan : false,
            inverse : false
        };

        $scope.choosePalette = function(index) {
            $scope.palette.img_palette = 'images/palette'+ index +'.png';
            $scope.palette.index = index;
        };

        $scope.addPalette = function() {
            if ($scope.palette.index == undefined) {
                return;
            }

            if ($scope.newStyle.rules[0].symbolizers[0].colorMap == undefined) {
                $scope.newStyle.rules[0].symbolizers[0].colorMap = {'function': {'@function' : 'interpolate'}};
            }
            switch ($scope.palette.index) {
                case 1:
                    var delta = $scope.palette.rasterMaxValue - $scope.palette.rasterMinValue;
                    if (!$scope.palette.inverse) {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#e52520'},
                                {data: delta * 0.25 + $scope.palette.rasterMinValue, color: '#ffde00'},
                                {data: delta * 0.5 + $scope.palette.rasterMinValue, color: '#95c11f'},
                                {data: delta * 0.75 + $scope.palette.rasterMinValue, color: '#1d71b8'},
                                {data: $scope.palette.rasterMinValue, color: '#662483'}
                            ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#662483'},
                                {data: delta * 0.25 + $scope.palette.rasterMinValue, color: '#1d71b8'},
                                {data: delta * 0.5 + $scope.palette.rasterMinValue, color: '#95c11f'},
                                {data: delta * 0.75 + $scope.palette.rasterMinValue, color: '#ffde00'},
                                {data: $scope.palette.rasterMinValue, color: '#e52520'}
                            ];
                    }
                    break;
                case 2:
                    if (!$scope.palette.inverse) {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#3F3460'},
                                {data: $scope.palette.rasterMaxValue, color: '#EC1876'}
                            ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#EC1876'},
                                {data: $scope.palette.rasterMaxValue, color: '#3F3460'}
                            ];
                    }
                    break;
                case 3:
                    if (!$scope.palette.inverse) {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#036531'},
                                {data: $scope.palette.rasterMaxValue, color: '#FDF01A'}
                            ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#FDF01A'},
                                {data: $scope.palette.rasterMaxValue, color: '#036531'}
                            ];
                    }
                    break;
                case 4:
                    var delta = $scope.palette.rasterMaxValue - $scope.palette.rasterMinValue;
                    if (!$scope.palette.inverse) {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#2d2e83'},
                                {data: delta * 0.25 + $scope.palette.rasterMinValue, color: '#1d71b8'},
                                {data: delta * 0.5 + $scope.palette.rasterMinValue, color: '#ffde00'},
                                {data: $scope.palette.rasterMinValue, color: '#e52520'}
                            ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#e52520'},
                                {data: delta * 0.5 + $scope.palette.rasterMinValue, color: '#ffde00'},
                                {data: delta * 0.75 + $scope.palette.rasterMinValue, color: '#1d71b8'},
                                {data: $scope.palette.rasterMinValue, color: '#2d2e83'}
                            ];
                    }
                    break;
                case 5:
                    if (!$scope.palette.inverse) {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#000000'},
                                {data: $scope.palette.rasterMaxValue, color: '#FFFFFF'}
                            ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                            [
                                {data: $scope.palette.rasterMinValue, color: '#FFFFFF'},
                                {data: $scope.palette.rasterMaxValue, color: '#000000'}
                            ];
                    }
                    break;
                default: break;
            }
        };

        $scope.dataProperties = null;
        $scope.dataBbox = null;
        $scope.dataBands = null;

        $scope.initDataProperties = function() {
            provider.dataDesc({providerId: $scope.providerId, dataId: $scope.layerName}, function(response) {
                $scope.dataProperties = response.properties;
                $scope.dataBbox = response.boundingBox;
                $scope.dataBands = response.bands;
                if ($scope.dataBands && $scope.dataBands.length > 0) {
                    $scope.palette.rasterMinValue = $scope.dataBands[0].minValue;
                    $scope.palette.rasterMaxValue = $scope.dataBands[0].maxValue;
                }
            }, function() {
                $growl('error','Error','Unable to get data description');
            });
        };

        $scope.setStyleChooser = function(choice){
            $scope.stylechooser = choice;
        };

        $scope.isSelected= function(choice) {
            return choice === $scope.stylechooser;
        };

        $scope.initScopeStyle = function() {
            // Page to include
            $scope.pageSld = ($scope.dataType.toLowerCase() === 'vector' || $scope.dataType === 'feature-store') ? "views/style/vectors.html" : "views/style/raster.html";

            style.listAll({}, function(response) {
                $dashboard($scope, response.styles, true);
                $scope.filtertype = $scope.initFilterType();
            });
        };

        $scope.initScopeData = function() {
            dataListing.listAll({}, function(response) {
                $dashboard($scope, response, true);
                $scope.filtertype = $scope.initFilterType();
            });
        };

        $scope.ok = function() {
            $modalInstance.close($scope.selected);
        };

        $scope.createStyle = function() {
            if ($scope.dataType.toLowerCase() === 'coverage' || $scope.dataType.toLowerCase() === 'raster') {
                $scope.addPalette();
            }
            style.create({provider: 'sld'}, $scope.newStyle, function() {
                $growl('success','Success','Style '+ $scope.newStyle.name +' successfully created');
                $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
            }, function() {
                $growl('error','Error','Unable to create style '+ $scope.newStyle.name);
                $modalInstance.close();
            });
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.chooseDataClicked = function(data) {
            $scope.layerName = data.Name;
            $scope.providerId = data.Provider;
            $scope.dataType = data.Type;
            $scope.pageSld = ($scope.dataType.toLowerCase() === 'vector' || $scope.dataType === 'feature-store') ? "views/style/vectors.html" : "views/style/raster.html";
        };

        $scope.showLayerWithStyle = function(style) {

            var layerName = $scope.layerName;
            var layerData;
            if (serviceName) {
                layerData = DataViewer.createLayerWMSWithStyle($cookies.cstlUrl, layerName, $scope.serviceName, style);
            } else {
                layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, layerName, providerId, style);
            }
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('styledMapOL');
        };

        $scope.displayCurrentStyle = function() {
            if ($scope.dataType.toLowerCase() === 'coverage' || $scope.dataType.toLowerCase() === 'raster') {
                $scope.addPalette();
            }
            style.create({provider: 'sld'}, $scope.newStyle, function() {
                var layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, $scope.layerName, $scope.providerId, $scope.newStyle.name);
                //var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
                DataViewer.layers = [layerData];
                DataViewer.initMap('styledMapOL');

                if ($scope.dataBbox) {
                    var extent = new OpenLayers.Bounds($scope.dataBbox[0], $scope.dataBbox[1], $scope.dataBbox[2], $scope.dataBbox[3]);
                    DataViewer.map.zoomToExtent(extent, true);
                }
            });
        };

        $scope.StyleisSelected = function(){
            if ($scope.selected != null){
                $scope.showLayerWithStyle($scope.selected.Name);
                return true
            } else {
                return false
            }

        };

    }]);
