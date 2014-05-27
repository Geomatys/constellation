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

cstlAdminApp.controller('StylesController', ['$scope', '$dashboard', 'style', '$growl', 'StyleSharedService', '$modal',
    function($scope, $dashboard, style, $growl, StyleSharedService, $modal) {
        $scope.init = function() {
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            style.listAll({provider: 'sld'}, function(response) {
                $dashboard($scope, response.styles, true);
                $scope.filtertype = "";
                $scope.ordertype = "Name";
                modalLoader.close();
            }, function() {
                modalLoader.close();
            });
        };

        $scope.deleteStyle = function() {
            if (confirm("Are you sure?")) {
                var styleName = $scope.selected.Name;
                var providerId = $scope.selected.Provider;
                style.delete({provider: providerId, name: styleName}, {},
                        function() {
                            $growl('success', 'Success', 'Style ' + styleName + ' successfully deleted');
                            style.listAll({provider: 'sld'}, function(response) {
                                $scope.fullList = response.styles;
                            });
                        },
                        function() {
                            $growl('error', 'Error', 'Style ' + styleName + ' deletion failed');
                        });
            }
        };

        $scope.editStyle = function() {
            var styleName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            style.get({provider: providerId, name: styleName}, function(response) {
                StyleSharedService.showStyleEdit($scope, response);
            });


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

cstlAdminApp.controller('StyleModalController', ['$scope', '$dashboard', '$modalInstance', 'style', '$cookies', 'dataListing', 'provider', '$growl', 'textService', 'newStyle', 'selectedLayer', 'serviceName', 'exclude',
    function($scope, $dashboard, $modalInstance, style, $cookies, dataListing, provider, $growl, textService, newStyle, selectedLayer, serviceName, exclude) {
        $scope.xmlStyle = '<xml></xml>';
        $scope.exclude = exclude;
        $scope.selectedLayer = selectedLayer || null;
        $scope.sldName = '';

        $scope.stylechooser = 'new';
        $scope.page = {
            pageSld: 'views/style/chooseType.html'
        };

        $scope.affectAlpha = function(value, param) {
            //param.color = value.toHexString();
            param.opacity = value.getAlpha();
        };

        $scope.newStyle = newStyle;
        $scope.serviceName = serviceName || null;

        function initSldPage(goToVectors) {
            if ($scope.newStyle && $scope.newStyle.rules[0].symbolizers[0]['@symbol']) {
                $scope.chooseType = true;
                switch ($scope.newStyle.rules[0].symbolizers[0]['@symbol']) {
                    case "point":
                        $scope.page.pageSld = 'views/style/point.html';
                        break;
                    case "polygon":
                        $scope.page.pageSld = 'views/style/polygone.html';
                        break;
                    case "line":
                        $scope.page.pageSld = 'views/style/ligne.html';
                        break;
                    case "text":
                        $scope.page.pageSld = 'views/style/texte.html';
                        break;
                    case "raster":
                        $scope.page.pageSld = 'views/style/raster.html';
                        break;
                    default:
                        $scope.page.pageSld = 'views/style/chooseType.html';
                        $scope.chooseType = false;
                        break;
                }
                return;
            }

            if ($scope.selectedLayer != null) {
                $scope.sldName = $scope.selectedLayer.Name + '-sld';
                if ($scope.selectedLayer.Type && ($scope.selectedLayer.Type.toLowerCase() === 'coverage' || $scope.selectedLayer.Type.toLowerCase() === 'coverage-store')) {
                    $scope.page.pageSld = 'views/style/raster.html';
                } else if ($scope.selectedLayer.Type && ($scope.selectedLayer.Type.toLowerCase() === 'vector' || $scope.selectedLayer.Type.toLowerCase() === 'feature-store')) {
                    if ($scope.selectedLayer.Subtype && !goToVectors) {
                        switch ($scope.selectedLayer.Subtype) {
                            case "Point":
                                $scope.page.pageSld = 'views/style/point.html';
                                break;
                            case "MultiPoint":
                                $scope.page.pageSld = 'views/style/point.html';
                                break;
                            case "Polygon":
                                $scope.page.pageSld = 'views/style/polygone.html';
                                break;
                            case "MultiPolygon":
                                $scope.page.pageSld = 'views/style/polygone.html';
                                break;
                            case "LineString":
                                $scope.page.pageSld = 'views/style/ligne.html';
                                break;
                            case "MultiLineString":
                                $scope.page.pageSld = 'views/style/ligne.html';
                                break;
                            default:
                                $scope.page.pageSld = 'views/style/vectors.html';
                                $scope.chooseType = false;
                                break;
                        }
                    } else {
                        $scope.page.pageSld = 'views/style/vectors.html';
                    }
                } else {
                    $scope.page.pageSld = 'views/style/chooseType.html';
                }
            } else {
                $scope.page.pageSld = 'views/style/chooseType.html';
            }
        }
        initSldPage();

        $scope.goBack = function() {
            $scope.refreshNewStyle();
            if ($scope.selectedLayer && $scope.selectedLayer.Type && ($scope.selectedLayer.Type.toLowerCase() === 'vector' || $scope.selectedLayer.Type.toLowerCase() === 'feature-store')) {
                initSldPage(true);
            } else {
                initSldPage();
            }
        };

        $scope.refreshNewStyle = function() {
            $scope.newStyle = {"name": $scope.sldName,
                "rules": [{
                        "symbolizers": [{}],
                        "filter": null
                    }]
            };
        };

        if ($scope.newStyle === null) {
            $scope.refreshNewStyle();
        }
        else {
            $scope.sldName = $scope.newStyle.name;

            if ($scope.newStyle.rules[0].symbolizers[0]['@symbol'] == 'line') {

                $scope.dataType = 'vector';
                $scope.providerId = 'generic_shp';
                $scope.layerName = 'CNTR_BN_60M_2006';
                if (typeof $scope.newStyle.rules[0].symbolizers[0].stroke !== 'undefined' && $scope.newStyle.rules[0].symbolizers[0].stroke.dashed == false) {
                    $scope.traitType = '';
                } else {
                    $scope.traitType = 'pointille';
                }
            } else if ($scope.newStyle.rules[0].symbolizers[0]['@symbol'] == 'point') {
                $scope.dataType = 'vector';
                $scope.providerId = 'generic_shp';
                $scope.layerName = 'CNTR_LB_2006';
            }
        }

        $scope.initPolygon = function() {
            $scope.dataType = 'vector';
            $scope.providerId = 'generic_shp';
            $scope.layerName = 'CNTR_RG_60M_2006';
            $scope.newStyle.rules[0].symbolizers[0]['@symbol'] = 'polygon';
            if (!$scope.newStyle.rules[0].symbolizers[0].stroke) {
                $scope.newStyle.rules[0].symbolizers[0].stroke = {
                    width: 1
                };
            }
        };

        $scope.initLine = function() {
            $scope.dataType = 'vector';
            $scope.providerId = 'generic_shp';
            $scope.layerName = 'CNTR_BN_60M_2006';
            $scope.newStyle.rules[0].symbolizers[0]['@symbol'] = 'line';
            if (!$scope.newStyle.rules[0].symbolizers[0].stroke) {
                $scope.newStyle.rules[0].symbolizers[0].stroke = {
                    width: 1
                };
            }
        };

        $scope.initPoint = function() {
            $scope.dataType = 'vector';
            $scope.providerId = 'generic_shp';
            $scope.layerName = 'CNTR_LB_2006';
            $scope.newStyle.rules[0].symbolizers[0]['@symbol'] = 'point';
        };

        $scope.initText = function() {
            $scope.dataType = 'vector';
            $scope.providerId = 'generic_shp';
            $scope.layerName = 'CNTR_BN_60M_2006';
            $scope.newStyle.rules[0].symbolizers[0]['@symbol'] = 'text';
            if (!$scope.newStyle.rules[0].symbolizers[0].font) {
                $scope.newStyle.rules[0].symbolizers[0].font = {
                    size: 14
                };
            }
            provider.dataDesc({providerId: $scope.providerId, dataId: $scope.layerName}, function(response) {
                $scope.dataProperties = response.properties;
            }, function() {
                $growl('error', 'Error', 'Unable to get data description');
            });
        };

        $scope.isgeophys = false;
        $scope.initRaster1Band = function() {
            $scope.dataType = 'coverage';
            $scope.providerId = 'generic_world_tif';
            $scope.layerName = 'cloudsgrey';
            $scope.newStyle.rules[0].symbolizers[0]['@symbol'] = 'raster';
            $scope.initDataProperties();

            provider.isGeophysic({providerId: $scope.providerId, dataId: $scope.layerName}, function(response) {
                $scope.isgeophys = (response.value == 'true');
            });
        };

        $scope.initRasterNBands = function() {
            // todo
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
                $growl('error', 'Error', 'Unable to get data description');
            });
        };

        $scope.initScopeStyle = function() {
            style.listAll({provider: 'sld'}, function(response) {
                $dashboard($scope, response.styles, true);
            });
        };

        $scope.aceLoaded = function(_editor) {
            // Options
            _editor.setValue($scope.xmlStyle);
            $scope.aceEditor = _editor;
        };

        $scope.aceChanged = function(e) {

        };

        $scope.saveXml = function() {
            alert("ace=" + $scope.aceEditor.getValue());
            textService.createStyleXml('sld', $scope.aceEditor.getValue());
//                , function() {
//                $growl('success','Success','Style '+ $scope.newStyle.name +' successfully created');
//                $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
//            }, function() {
//                $growl('error','Error','Unable to create style '+ $scope.newStyle.name);
//                $modalInstance.close();
//            });
        };

        $scope.addStrokeDashArray = function(traitType) {
            if (traitType === 'pointille') {
                if ($scope.newStyle.rules[0].symbolizers[0].stroke == undefined) {
                    $scope.newStyle.rules[0].symbolizers[0].stroke = {};
                }
                $scope.newStyle.rules[0].symbolizers[0].stroke.dashArray = [1, 1];
                $scope.newStyle.rules[0].symbolizers[0].stroke.dashed = true;
            } else {
                $scope.newStyle.rules[0].symbolizers[0].stroke.dashArray = null;
                $scope.newStyle.rules[0].symbolizers[0].stroke.dashed = false;
            }
        };

        $scope.initFontFamilies = function() {
            if ($scope.newStyle.rules[0].symbolizers[0].font == undefined) {
                $scope.newStyle.rules[0].symbolizers[0].font = {};
            }
            if ($scope.newStyle.rules[0].symbolizers[0].font.family == undefined) {
                $scope.newStyle.rules[0].symbolizers[0].font.family = [];
            }
        };

        $scope.palette = {
            index: undefined,
            img_palette: 'images/palette0.png',
            rasterMinValue: undefined,
            rasterMaxValue: undefined,
            intervalles: 1,
            channelSelection: undefined,
            nan: false,
            inverse: false
        };

        $scope.choosePalette = function(index) {
            $scope.palette.img_palette = 'images/palette' + index + '.png';
            $scope.palette.index = index;
        };

        $scope.addPalette = function() {
            if ($scope.palette.index == undefined) {
                return;
            }


            if ($scope.newStyle.rules[0].symbolizers[0].colorMap == undefined || $scope.newStyle.rules[0].symbolizers[0].colorMap.function == undefined) {
                $scope.newStyle.rules[0].symbolizers[0].colorMap = {'function': {'@function': 'interpolate'}};
            }
            switch ($scope.palette.index) {
                case 1:
                    var delta = $scope.palette.rasterMaxValue - $scope.palette.rasterMinValue;
                    if (!$scope.palette.inverse) {
                        if ($scope.newStyle.rules[0].symbolizers[0].colorMap.function == undefined) {
                            $scope.newStyle.rules[0].symbolizers[0].colorMap.function = {};
                        }
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
                default:
                    break;
            }
        };

        $scope.setStyleChooser = function(choice) {
            $scope.stylechooser = choice;
        };

        $scope.isSelected = function(choice) {
            return choice === $scope.stylechooser;
        };

        $scope.ok = function() {
            $modalInstance.close($scope.selected);
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.updateStyle = function() {
            // style.updatejson({provider: 'sld', name: $scope.newStyle.name }, $scope.newStyle, function() {
            style.createjson({provider: 'sld'}, $scope.newStyle, function() {
                $growl('success', 'Success', 'Style ' + $scope.newStyle.name + ' successfully created');
                $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
            }, function() {
                $growl('error', 'Error', 'Unable to update style ' + $scope.newStyle.name);
                $modalInstance.close();
            });
        };

        $scope.createStyle = function() {
            if ($scope.newStyle.name === "") {
                $scope.noName = true;
            }
            else {
                if ($scope.dataType.toLowerCase() !== 'coverage' && $scope.dataType.toLowerCase() !== 'raster') {
                    style.createjson({provider: 'sld'}, $scope.newStyle, function() {
                        $growl('success', 'Success', 'Style ' + $scope.newStyle.name + ' successfully updated');
                        $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
                    }, function() {
                        $growl('error', 'Error', 'Unable to create style ' + $scope.newStyle.name);
                        $modalInstance.close();
                    });
                }
                else if ($scope.dataType.toLowerCase() === 'coverage' || $scope.dataType.toLowerCase() === 'raster') {

                    $scope.addPalette();
                    style.createjson({provider: 'sld'}, $scope.newStyle, function() {
                        $growl('success', 'Success', 'Style ' + $scope.newStyle.name + ' successfully created');
                        $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
                    }, function() {
                        $growl('error', 'Error', 'Unable to create style ' + $scope.newStyle.name);
                        $modalInstance.close();
                    });

                }
            }
        };

        $scope.showLayerWithStyle = function(style) {

            var layerData;
            if (serviceName) {
                layerData = DataViewer.createLayerWMSWithStyle($cookies.cstlUrl, $scope.layerName, $scope.serviceName, style);
            } else {
                layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, $scope.layerName, $scope.providerId, style);
            }
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('styledMapOL');
        };

        $scope.displayCurrentStyle = function() {
            if ($scope.dataType.toLowerCase() === 'coverage' || $scope.dataType.toLowerCase() === 'raster') {
                $scope.addPalette();
            }

            if ($scope.newStyle.name === "") {
                $scope.noName = true;
            }
            else {
                $scope.noName = false;
                style.createjson({provider: 'sld_temp'}, $scope.newStyle, function() {
                    var layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, $scope.layerName, $scope.providerId, $scope.newStyle.name, "sld_temp");
                    var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
                    if ($scope.layerName === 'CNTR_BN_60M_2006') {
                        DataViewer.layers = [layerData];
                    } else {
                        DataViewer.layers = [layerData, layerBackground];
                    }
                    DataViewer.initMap('styledMapOL');

                    if ($scope.dataBbox) {
                        var extent = new OpenLayers.Bounds($scope.dataBbox[0], $scope.dataBbox[1], $scope.dataBbox[2], $scope.dataBbox[3]);
                        DataViewer.map.zoomToExtent(extent, true);
                    }
                });
            }
        };

        $scope.StyleisSelected = function() {
            if ($scope.selected !== null) {
                $scope.showLayerWithStyle($scope.selected.Name);
                return true;
            } else {
                return false;
            }

        };

    }]);
