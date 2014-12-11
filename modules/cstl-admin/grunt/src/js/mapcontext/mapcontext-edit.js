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

angular.module('cstl-mapcontext-edit', ['cstl-restapi', 'cstl-services', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .controller('MapContextModalController', function($scope, $modalInstance, mapcontext, webService, style, Growl,
                                                      $translate, ctxtToEdit, layersForCtxt, $cookieStore) {
        // item to save in the end
        $scope.ctxt = ctxtToEdit || {};
        // defines if we are in adding or edition mode
        $scope.addMode = ctxtToEdit ? false:true;

        $scope.tag = {
            text: '',
            keywords: (ctxtToEdit && ctxtToEdit.keywords) ? ctxtToEdit.keywords.split(','): []
        };

        // handle display mode for this modal popup
        $scope.mode = {
            selTab: 'tabInfo',
            display: 'general',
            dispWmsLayers: false,
            errorNoGivenName: false
        };

        $scope.layers = {
            toAdd: layersForCtxt || [], // Stores temp layers, selected to be added at the saving time
            toSend: [], // List of layers really sent
            toStyle: null // Layer on which to apply the selected style
        };

        $scope.external = {
            serviceUrl: null
        };

        $scope.selection = {
            layer:null,
            service:null,
            item:null,
            extLayer:null
        };

        $scope.styles = {
            existing: [],
            selected: null
        };

        $scope.servicesLayers = [];

        $scope.dismiss = function () {
            $modalInstance.dismiss('close');
        };

        $scope.close = function () {
            $modalInstance.close();
        };

        $scope.getCurrentLang = function() {
            return $translate.use();
        };

        $scope.initInternalWmsServices = function() {
            webService.listServiceLayers({lang: $scope.getCurrentLang()}, function(response) {
                $scope.servicesLayers = response;
            });
        };

        $scope.selectLayer = function(layer,service) {
            if (layer && $scope.selection.layer && $scope.selection.layer.Id === layer.Id) {
                $scope.selection.layer = null;
                $scope.selection.service = null;
            } else {
                $scope.selection.layer = layer;
                $scope.selection.service = service;
            }
        };

        $scope.selectItem = function(item) {
            if ($scope.selection.item === item) {
                $scope.selection.item = null;
            } else {
                $scope.selection.item = item;
            }
        };

        $scope.selectExtLayer = function(extLayer) {
            if (extLayer && $scope.selection.extLayer && $scope.selection.extLayer.Name === extLayer.Name) {
                $scope.selection.extLayer = null;
            } else {
                $scope.selection.extLayer = extLayer;
            }
        };

        $scope.selectStyle = function(item) {
            if (item && $scope.styles.selected && $scope.styles.selected.Id === item.Id) {
                $scope.styles.selected = null;
            } else {
                $scope.styles.selected = item;
            }
        };

        $scope.addTag = function() {
            if (!$scope.tag.text || $scope.tag.text === '' || $scope.tag.text.length === 0) {
                return;
            }
            $scope.tag.keywords.push($scope.tag.text);
            $scope.tag.text = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.tag.keywords.length > 0 &&
                $scope.tag.text.length === 0 &&
                !key) {
                $scope.tag.keywords.pop();
            } else if (key) {
                $scope.tag.keywords.splice(key, 1);
            }
        };

        $scope.updateNamePresent = function() {
            $scope.mode.errorNoGivenName = (!$scope.ctxt.name || $scope.ctxt.name === null);
        };

        $scope.validate = function () {
            // Verify on which step the user is.
            if ($scope.mode.display==='general') {
                if (!$scope.ctxt.name) {
                    $scope.mode.errorNoGivenName = true;
                    Growl('warning', 'Warning', 'You must specify a name');
                    return;
                }

                if ($scope.tag.keywords) {
                    var str = '';
                    for (var i = 0; i < $scope.tag.keywords.length; i++) {
                        if (i > 0) {
                            str += ',';
                        }
                        str += $scope.tag.keywords[i];
                    }
                    $scope.ctxt.keywords = str;
                }
                // On the general panel, it means saving the whole context
                if ($scope.addMode) {
                    mapcontext.add({}, $scope.ctxt, function (ctxtCreated) {
                        handleLayersForContext(ctxtCreated);
                    }, function () {
                        Growl('error', 'Error', 'Unable to create map context');
                        $scope.dismiss();
                    });
                } else {
                    mapcontext.update({}, $scope.ctxt, function (ctxtUpdated) {
                        handleLayersForContext(ctxtUpdated);
                    }, function () {
                        Growl('error', 'Error', 'Unable to update map context');
                        $scope.dismiss();
                    });
                }
            } else if ($scope.mode.display==='chooseLayer') {
                // Add the selected layer to the current map context
                if ($scope.selection.extLayer) {
                    var llExtent = '';
                    if ($scope.selection.extLayer.EX_GeographicBoundingBox) {
                        var exCaps130 = $scope.selection.extLayer.EX_GeographicBoundingBox;
                        llExtent = exCaps130.westBoundLongitude +','+
                                   exCaps130.southBoundLatitude +','+
                                   exCaps130.eastBoundLongitude +','+
                                   exCaps130.northBoundLatitude;
                    } else if ($scope.selection.extLayer.LatLonBoundingBox) {
                        var exCaps111 = $scope.selection.extLayer.LatLonBoundingBox;
                        llExtent = exCaps111.minx +','+ exCaps111.miny +','+ exCaps111.maxx +','+ exCaps111.maxy;
                    }

                    var extStyle = '';
                    if ($scope.selection.extLayer.Style) {
                        for (var j=0; j < $scope.selection.extLayer.Style.length; j++) {
                            if (j > 0) {
                                extStyle += ',';
                            }
                            var capsStyle = $scope.selection.extLayer.Style[j];
                            extStyle += capsStyle.Name;
                        }
                    }

                    var layerExt = {
                        externalLayer: $scope.selection.extLayer.Name,
                        externalLayerExtent: llExtent,
                        externalServiceUrl: $scope.external.serviceUrl,
                        externalServiceVersion: ($scope.selection.extLayer.EX_GeographicBoundingBox) ? '1.3.0' : '1.1.1',
                        externalStyle: extStyle,
                        opacity: 100
                    };
                    var layerExtToAdd = {
                        layer: layerExt,
                        visible: true
                    };
                    $scope.layers.toAdd.push(layerExtToAdd);
                    $scope.selection = {};
                } else if ($scope.selection.layer) {
                    var layerToAdd = {
                        layer: $scope.selection.layer,
                        service: $scope.selection.service,
                        visible: true
                    };
                    layerToAdd.layer.opacity = 100;
                    $scope.layers.toAdd.push(layerToAdd);
                    $scope.selection = {};
                }
                // Go back to first screen
                $scope.mode.display = 'general';

                $scope.viewMap(false);

            } else if ($scope.mode.display==='addChooseStyle') {
                if ($scope.layers.toStyle.layer.externalStyle) {
                    // It's an external WMS style, put the one chosen in first, as the default one
                    var possibleStyles = $scope.layers.toStyle.layer.externalStyle.split(',');
                    if (possibleStyles[0] !== $scope.styles.selected.Name) {
                        var indexForStyle;
                        for (var k=0; k<possibleStyles.length; k++) {
                            var s = possibleStyles[k];
                            if (s === $scope.styles.selected.Name) {
                                indexForStyle = k;
                            }
                        }

                        if (indexForStyle) {
                            // Remove it from its old place
                            possibleStyles.splice(indexForStyle, 1);
                            // Put it in first
                            possibleStyles.splice(0, 0, $scope.styles.selected.Name);
                        }

                        var finalStyles = '';
                        for (var l=0; l<possibleStyles.length; l++) {
                            if (l > 0) {
                                finalStyles += ',';
                            }
                            finalStyles += possibleStyles[l];
                        }
                        $scope.layers.toStyle.layer.externalStyle = finalStyles;
                    }
                } else {
                    // Internal style
                    $scope.layers.toStyle.layer.styleId = $scope.styles.selected.Id;
                    $scope.layers.toStyle.layer.styleName = $scope.styles.selected.Name;
                }
                $scope.mode.display = 'general';
                $scope.viewMap(false);
            }
        };

        $scope.cancel = function() {
            if ($scope.mode.display==='general') {
                $scope.dismiss();
            } else {
                $scope.mode.display = 'general';
            }
        };

        function handleLayersForContext(ctxt) {
            // Prepare layers to be added
            fillLayersToSend(ctxt);

            mapcontext.setLayers({id: ctxt.id}, $scope.layers.toSend, function () {
                Growl('success', 'Success', 'Map context created');
                $scope.close();
            }, function () {
                Growl('error', 'Error', 'Unable to add layers to map context');
                $scope.dismiss();
            });
        }

        function fillLayersToSend(ctxt) {
            $scope.layers.toSend = [];
            for (var i = 0; i < $scope.layers.toAdd.length; i++) {
                var l = $scope.layers.toAdd[i];
                $scope.layers.toSend.push({
                    mapcontextId: (ctxt) ? ctxt.id : null, layerId: (l.layer.Id) ? l.layer.Id : l.layer.layerId, styleId: l.layer.styleId,
                    layerOrder: i, layerOpacity: l.layer.opacity, layerVisible: l.visible,
                    externalServiceUrl: l.layer.externalServiceUrl, externalServiceVersion: l.layer.externalServiceVersion,
                    externalLayer: l.layer.externalLayer, externalLayerExtent: l.layer.externalLayerExtent, externalStyle: l.layer.externalStyle
                });
            }
        }

        $scope.goToAddLayerToContext = function() {
            $scope.mode.display = 'addChooseSource';
            $scope.selection = {};
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#selectionLayer').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };
        $scope.toggleUpDownExtSelected = function() {
            var $header = $('#selectionExtLayer').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };

        $scope.orderUp = function(i) {
            var skipBg = (DataViewer.addBackground)?i+1:i;
            if (i > 0) {
                var previous = $scope.layers.toAdd[i - 1];
                $scope.layers.toAdd[i - 1] = $scope.layers.toAdd[i];
                $scope.layers.toAdd[i] = previous;

                // Now switch layer order for the map
                var item0 = DataViewer.map.getLayers().item(skipBg-1);
                var item1 = DataViewer.map.getLayers().item(skipBg);
                DataViewer.map.getLayers().setAt(skipBg-1, item1);
                DataViewer.map.getLayers().setAt(skipBg,   item0);
            }
        };
        $scope.orderDown = function(i) {
            var skipBg = (DataViewer.addBackground)?i+1:i;
            if (i < $scope.layers.toAdd.length - 1) {
                var next = $scope.layers.toAdd[i + 1];
                $scope.layers.toAdd[i + 1] = $scope.layers.toAdd[i];
                $scope.layers.toAdd[i] = next;

                // Now switch layer order for the map
                var item0 = DataViewer.map.getLayers().item(skipBg);
                var item1 = DataViewer.map.getLayers().item(skipBg+1);
                DataViewer.map.getLayers().setAt(skipBg,   item1);
                DataViewer.map.getLayers().setAt(skipBg+1, item0);
            }
        };

        $scope.viewLayerInfo = function(item) {
            var serviceIdentifier = (item.service) ? item.service.name : (item.layer.serviceIdentifier) ? item.layer.serviceIdentifier : item.layer.externalServiceUrl;
            var serviceVersions = (item.service) ? item.service.versions : (item.layer.serviceVersions) ? (item.layer.serviceVersions) : item.layer.externalServiceVersion;
            var str = '<div style="word-wrap: break-word;"> </div><div><b>Name</b></div>' +
                '<div style="word-wrap: break-word;">'+ item.layer.Name +'</div>' +
                '<div><b>Alias</b></div>' +
                '<div style="word-wrap: break-word;">'+ item.layer.Alias +'</div>';
            if (item.layer.Type) {
                str += '<div><b>Type</b></div>' +
                '<div style="word-wrap: break-word;">'+ item.layer.Type +'</div>';
            }
            str += '<div><b>Service</b></div>' +
            '<div style="word-wrap: break-word;">'+ serviceIdentifier +'</div>' +
            '<div><b>Service version(s)</b></div>' +
            '<div>'+ serviceVersions +'</div></div>';
            return str;
        };

        $scope.goToStyleMapItem = function(item) {
            $scope.styles.existing = [];
            if (item.layer.externalLayer) {
                var styleItems = [];
                if (item.layer.externalStyle) {
                    var styleNames = item.layer.externalStyle.split(',');
                    for (var i = 0; i < styleNames.length; i++) {
                        styleItems.push({Name: styleNames[i]});
                    }
                }
                $scope.styles.existing = styleItems;
            } else {
                // Internal layer
                style.listAll({provider: 'sld'}, function (response) {
                    for (var j = 0; j < item.layer.TargetStyle.length; j++) {
                        var tgStyle = item.layer.TargetStyle[j];
                        for (var i = 0; i < response.styles.length; i++) {
                            var style = response.styles[i];
                            if (style.Name === tgStyle.Name && style.Provider === tgStyle.Provider) {
                                $scope.styles.existing.push(style);
                                break;
                            }
                        }
                    }
                });
            }
            $scope.layers.toStyle = item;
            $scope.mode.display = 'addChooseStyle';
        };

        $scope.deleteMapItem = function(item) {
            var index = $scope.layers.toAdd.indexOf(item);
            if (index !== -1) {
                $scope.layers.toAdd.splice(index, 1);
            }

            for (var i=0; i<DataViewer.layers.length; i++) {
                var l = DataViewer.layers[i];
                if (l.get('name') === item.layer.Name ||
                    l.get('name') === item.layer.externalLayer) {
                    DataViewer.map.removeLayer(l);
                    return;
                }
            }
        };

        $scope.changeVisibility = function(item) {
            item.visible=!(item.visible);
            for (var i=0; i<DataViewer.layers.length; i++) {
                var l = DataViewer.layers[i];
                if (l.get('name') === item.layer.Name ||
                    l.get('name') === item.layer.externalLayer) {
                    l.setVisible(item.visible);
                    return;
                }
            }
        };

        $scope.updateOpacity = function(item) {
            for (var i=0; i<DataViewer.layers.length; i++) {
                var l = DataViewer.layers[i];
                if (l.get('name') === item.layer.Name ||
                    l.get('name') === item.layer.externalLayer) {
                    l.setOpacity(item.layer.opacity / 100);
                    return;
                }
            }
        };

        $scope.viewMap = function(firstTime) {
            if (!$scope.layers.toAdd || $scope.layers.toAdd.length===0) {
                return;
            }
            DataViewer.initConfig();
            var cstlUrl = $cookieStore.get('cstlUrl');

            var layersToView = [];
            for (var i=0; i<$scope.layers.toAdd.length; i++) {
                var l = $scope.layers.toAdd[i];
                if (l.visible) {
                    var layerData;
                    if (l.layer.externalServiceUrl) {
                        layerData = (l.layer.externalStyle) ?
                            DataViewer.createLayerExternalWMSWithStyle(l.layer.externalServiceUrl, l.layer.externalLayer, l.layer.externalStyle.split(',')[0]) :
                            DataViewer.createLayerExternalWMS(l.layer.externalServiceUrl, l.layer.externalLayer);
                    } else {
                        var serviceName = (l.layer.serviceIdentifier) ? l.layer.serviceIdentifier : l.service.identifier;
                        layerData = (l.layer.styleName) ?
                            DataViewer.createLayerWMSWithStyle(cstlUrl, l.layer.Name, serviceName, l.layer.styleName) :
                            DataViewer.createLayerWMS(cstlUrl, l.layer.Name, serviceName);
                    }
                    layerData.setOpacity(l.layer.opacity / 100);
                    layersToView.push(layerData);
                }
            }

            DataViewer.layers = layersToView;
            if (layersToView.length === 0) {
                return;
            }

            if($scope.ctxt && $scope.ctxt.crs){
                var crsCode = $scope.ctxt.crs;
                DataViewer.projection = crsCode;
                DataViewer.addBackground= crsCode==='EPSG:3857';
                if(crsCode === 'EPSG:4326' || crsCode === 'CRS:84') {
                    DataViewer.extent=[-180, -90, 180, 90];
                }
            }

            //DataViewer.addBackground = false;
            DataViewer.initMap('mapContextMap');

            if (firstTime) {
                if($scope.ctxt && $scope.ctxt.west && $scope.ctxt.south && $scope.ctxt.east && $scope.ctxt.north && $scope.ctxt.crs) {
                    var extent = [$scope.ctxt.west, $scope.ctxt.south, $scope.ctxt.east, $scope.ctxt.north];
                    setTimeout(function(){
                        DataViewer.map.updateSize();
                        //because zoomToExtent take extent in EPSG:4326 we need to reproject the zoom extent
                        if($scope.ctxt.crs !== 'EPSG:4326' && $scope.ctxt.crs !=='CRS:84'){
                            var projection = ol.proj.get($scope.ctxt.crs);
                            extent = ol.proj.transform(extent, projection,'EPSG:4326');
                        }
                        DataViewer.zoomToExtent(extent, DataViewer.map.getSize(),true);
                    },300);
                }
            } else {
                fillLayersToSend();
                mapcontext.extentForLayers({}, $scope.layers.toSend, function(response) {
                    useExtentForLayers(response.values);
                });
            }
        };

        function useExtentForLayers(values) {
            $scope.ctxt.crs = values.crs;
            $scope.ctxt.west = Number(values.west);
            $scope.ctxt.south = Number(values.south);
            $scope.ctxt.east = Number(values.east);
            $scope.ctxt.north = Number(values.north);
            var extent = [$scope.ctxt.west, $scope.ctxt.south, $scope.ctxt.east, $scope.ctxt.north];
            setTimeout(function(){
                DataViewer.map.updateSize();
                //because zoomToExtent take extent in EPSG:4326 we need to reproject the zoom extent
                if($scope.ctxt.crs !== 'EPSG:4326' && $scope.ctxt.crs !=='CRS:84'){
                    var projection = ol.proj.get($scope.ctxt.crs);
                    extent = ol.proj.transform(extent, projection,'EPSG:4326');
                }
                DataViewer.zoomToExtent(extent, DataViewer.map.getSize(),true);
            },300);
        }

        $scope.applyExtent = function() {
            var extent = DataViewer.map.getView().calculateExtent(DataViewer.map.getSize());
            var crsCode = DataViewer.map.getView().getProjection().getCode();
            if(crsCode) {
                $scope.ctxt.crs = crsCode;
            }
            $scope.ctxt.west = extent[0];
            $scope.ctxt.south = extent[1];
            $scope.ctxt.east = extent[2];
            $scope.ctxt.north = extent[3];
        };

        $scope.searchAndDisplayWmsLayers = function() {
            if ($scope.external.serviceUrl) {
                // Try in WMS version 1.3.0
                mapcontext.listExtLayers({version: "1.3.0"}, $scope.external.serviceUrl, function(response) {
                    $scope.external.layers = response;
                    $scope.mode.dispWmsLayers = true;
                }, function() {
                    // If it fails tries it in version 1.1.1
                    mapcontext.listExtLayers({version: "1.1.1"}, $scope.external.serviceUrl, function(response) {
                        $scope.external.layers = response;
                        $scope.mode.dispWmsLayers = true;
                    }, function() {
                        Growl('error', 'Error', 'Unable to find layers for this url');
                    });
                });
            }
        };

        $scope.truncate = function(test,text){
            if(text) {
                if (test !== 'chooseLayer' && text.length > 30) {
                    return text.substr(0, 30) + "...";
                } else if (test === 'chooseLayer' && text.length > 60) {
                    return text.substr(0, 60) + "...";
                } else {return text;}
            }
        };
    });