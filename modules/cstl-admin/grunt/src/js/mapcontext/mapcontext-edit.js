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
                                                      $translate, ctxtToEdit, layersForCtxt, $cookieStore,provider) {
        // item to save in the end
        $scope.ctxt = ctxtToEdit || {"crs":'EPSG:3857',"west":-35848354.76952138,
                                                       "south":-35848354.76952139,
                                                       "east":35848354.76952138,
                                                       "north":35848354.76952136};
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
            extLayer:null,
            internalData:null
        };

        $scope.styles = {
            existing: [],
            selected: null
        };

        $scope.dismiss = function () {
            $modalInstance.dismiss('close');
        };

        $scope.close = function () {
            $modalInstance.close();
        };


        $scope.initScopeMapContextEditor = function() {
            setTimeout(function(){
                $scope.viewMap(true,null);
            },500);
        };

        $scope.goToInternalSource = function() {
            $scope.mode.source='interne';
            $scope.mode.display='chooseLayer';
        };

        $scope.selectInternalData = function(data) {
            if (data && $scope.selection.internalData && $scope.selection.internalData.Id === data.Id) {
                $scope.selection.internalData = null;
            } else {
                $scope.selection.internalData = data;
            }
        };

        $scope.selectLayer = function(layer,service) {
            $scope.selection.extLayer = null; //reset selection for extLayer
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
            //reset selection for wms cstl layer
            $scope.selection.layer = null;
            $scope.selection.service = null;
            if (extLayer && $scope.selection.extLayer && $scope.selection.extLayer.Name === extLayer.Name) {
                $scope.selection.extLayer = null;
            } else {
                $scope.selection.extLayer = extLayer;
            }
        };

        $scope.selectStyle = function(item) {
            if (item && $scope.styles.selected && $scope.styles.selected.Name === item.Name) {
                $scope.styles.selected = null;
            } else {
                $scope.styles.selected = item;
            }
            fillLayersToSend(null);
        };

        $scope.showMapWithStyle = function(styleObj) {
            $scope.viewMap(false,{"layer":$scope.layers.toStyle,"style":styleObj});
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
                        updateLayersForContext(ctxtCreated);
                    }, function () {
                        Growl('error', 'Error', 'Unable to create map context');
                    });
                } else {
                    mapcontext.update({}, $scope.ctxt, function (ctxtUpdated) {
                        updateLayersForContext(ctxtUpdated);
                    }, function () {
                        Growl('error', 'Error', 'Unable to update map context');
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
                        externalStyle: extStyle
                    };
                    var styleExtArray = $scope.selection.extLayer.Style;
                    var layerExtToAdd = {
                        layer: layerExt,
                        visible: true,
                        isWms:true,
                        opacity: 100,
                        styleObj:styleExtArray && styleExtArray.length>0?styleExtArray[0]:null
                    };
                    $scope.layers.toAdd.push(layerExtToAdd);
                } else if ($scope.selection.layer) {
                    var styleArray = $scope.selection.layer.TargetStyle;
                    var layerToAdd = {
                        layer: $scope.selection.layer,
                        service: $scope.selection.service,
                        visible: true,
                        isWms:true,
                        opacity:100,
                        styleObj:styleArray && styleArray.length>0?styleArray[0]:null
                    };
                    $scope.layers.toAdd.push(layerToAdd);
                }else if ($scope.selection.internalData) {
                    var styleArr = $scope.selection.internalData.TargetStyle;
                    var layerObjToAdd = {
                        layer: $scope.selection.internalData,
                        visible: true,
                        isWms:false,
                        opacity:100,
                        styleObj:styleArr && styleArr.length>0?styleArr[0]:null
                    };
                    $scope.layers.toAdd.push(layerObjToAdd);
                }
                fillLayersToSend(null);
                $scope.selection = {};
                // Go back to first screen
                $scope.mode.display = 'general';
                setTimeout(function(){
                    $scope.viewMap(false,null);
                },200);

            } else if ($scope.mode.display==='addChooseStyle') {
                $scope.layers.toStyle.styleObj=$scope.styles.selected;
                if ($scope.layers.toStyle.layer.externalServiceUrl && $scope.layers.toStyle.layer.externalStyle) {
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
                }
                $scope.mode.display = 'general';
                setTimeout(function(){
                    $scope.viewMap(false,null);
                },200);
            }
        };

        $scope.cancel = function() {
            if ($scope.mode.display==='general') {
                $scope.dismiss();
            } else {
                $scope.mode.display = 'general';
                fillLayersToSend(null);
                setTimeout(function(){
                    $scope.viewMap(false,null);
                },200);
            }
        };

        function updateLayersForContext(ctxt) {
            // Prepare layers to be added
            fillLayersToSend(ctxt);
            mapcontext.setLayers({id: ctxt.id}, $scope.layers.toSend, function () {
                Growl('success', 'Success', 'Map context created');
                $scope.close();
            }, function () {
                Growl('error', 'Error', 'Unable to add layers to map context');
            });
        }

        function fillLayersToSend(ctxt) {
            $scope.layers.toSend = [];
            for (var i = 0; i < $scope.layers.toAdd.length; i++) {
                var layObj = $scope.layers.toAdd[i];
                $scope.layers.toSend.push({
                    mapcontextId: (ctxt) ? ctxt.id : null,
                    layerId: (layObj.isWms)? (layObj.layer.Id) ? layObj.layer.Id : layObj.layer.layerId : null,
                    dataId: (!layObj.isWms)?(layObj.layer.Id) ? layObj.layer.Id : layObj.layer.dataId:null,
                    iswms: layObj.isWms,
                    styleId: (layObj.styleObj && layObj.styleObj.Id) ? layObj.styleObj.Id : null,
                    layerOrder: i,
                    layerOpacity: layObj.opacity,
                    layerVisible: layObj.visible,
                    externalServiceUrl: layObj.layer.externalServiceUrl,
                    externalServiceVersion: layObj.layer.externalServiceVersion,
                    externalLayer: layObj.layer.externalLayer,
                    externalLayerExtent: layObj.layer.externalLayerExtent,
                    externalStyle: (layObj.layer.externalStyle) ? layObj.layer.externalStyle : (layObj.styleObj)?layObj.styleObj.Name:null
                });
            }
        }

        $scope.goToAddLayerToContext = function() {
            $scope.mode.display = 'addChooseSource';
            $scope.selection = {};
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

        $scope.goToStyleMapItem = function(item) {
            $scope.styles.selected = null;
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
                if($scope.styles.existing && $scope.styles.existing.length>0){
                    for(var s=0;s<$scope.styles.existing.length;s++) {
                        var candidat = $scope.styles.existing[s];
                        if(item.styleObj){
                            if(item.styleObj.Name && candidat.Name === item.styleObj.Name){
                                $scope.styles.selected = candidat;
                                break;
                            }
                        } else if(item.layer.styleName && candidat.Name === item.layer.styleName){
                            $scope.styles.selected = candidat;
                            break;
                        }else if(item.layer.externalStyle && candidat.Name === item.layer.externalStyle.split(',')[0]){
                            $scope.styles.selected = candidat;
                            break;
                        }
                    }
                }
            } else {
                // for Internal data layer and for internal wms layer
                style.listAll({provider: 'sld'}, function (response) {
                    if(response && response.styles) {
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
                        if($scope.styles.existing && $scope.styles.existing.length>0){
                            for(var s=0;s<$scope.styles.existing.length;s++) {
                                var candidat = $scope.styles.existing[s];
                                if(item.styleObj){
                                    if(item.styleObj.Name && candidat.Name === item.styleObj.Name) {
                                        $scope.styles.selected = candidat;
                                        break;
                                    }
                                } else if(item.layer.styleName && candidat.Name === item.layer.styleName){
                                    $scope.styles.selected = candidat;
                                    break;
                                }else if(item.layer.externalStyle && candidat.Name === item.layer.externalStyle){
                                    $scope.styles.selected = candidat;
                                    break;
                                }
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
                var candidat = DataViewer.layers[i];
                var candidatName = candidat.get('name');
                if(item.isWms) {
                    if ((item.layer.Name && candidatName === item.layer.Name) ||
                        (item.layer.externalLayer && candidatName === item.layer.externalLayer)) {
                        DataViewer.map.removeLayer(candidat);
                        return;
                    }
                }else {
                    var layerName;
                    if (item.layer.Namespace) {
                        layerName = '{' + item.layer.Namespace + '}' + item.layer.Name;
                    } else {
                        layerName = item.layer.Name;
                    }
                    if (layerName && candidatName === layerName) {
                        DataViewer.map.removeLayer(candidat);
                        return;
                    }
                }
            }
        };

        $scope.changeVisibility = function(item) {
            item.visible=!(item.visible);
            for (var i=0; i<DataViewer.layers.length; i++) {
                var candidat = DataViewer.layers[i];
                var candidatName = candidat.get('name');
                if(item.isWms) {
                    if ((item.layer.Name && candidatName === item.layer.Name) ||
                        (item.layer.externalLayer && candidatName === item.layer.externalLayer)) {
                        candidat.setVisible(item.visible);
                        return;
                    }
                }else {
                    var layerName;
                    if (item.layer.Namespace) {
                        layerName = '{' + item.layer.Namespace + '}' + item.layer.Name;
                    } else {
                        layerName = item.layer.Name;
                    }
                    if (layerName && candidatName === layerName) {
                        candidat.setVisible(item.visible);
                        return;
                    }
                }
            }
        };

        $scope.updateOpacity = function(item) {
            for (var i=0; i<DataViewer.layers.length; i++) {
                var candidat = DataViewer.layers[i];
                var candidatName = candidat.get('name');
                if(item.isWms) {
                    if ((item.layer.Name && candidatName === item.layer.Name) ||
                        (item.layer.externalLayer && candidatName === item.layer.externalLayer)) {
                        candidat.setOpacity(item.opacity / 100);
                        return;
                    }
                }else {
                    var layerName;
                    if (item.layer.Namespace) {
                        layerName = '{' + item.layer.Namespace + '}' + item.layer.Name;
                    } else {
                        layerName = item.layer.Name;
                    }
                    if (layerName && candidatName === layerName) {
                        candidat.setOpacity(item.opacity / 100);
                        return;
                    }
                }
            }
        };

        $scope.viewMap = function(zoomOnMapContextExtent,layerStyleObj) {
            DataViewer.initConfig();
            if ($scope.layers.toAdd && $scope.layers.toAdd.length>0) {
                var cstlUrl = $cookieStore.get('cstlUrl');
                var layersToView = [];
                for (var i=0; i<$scope.layers.toAdd.length; i++) {
                    var layObj = $scope.layers.toAdd[i];
                    if (layObj.visible) {
                        var layerData;
                        if (layObj.isWms) {//external wms layer
                            if(layObj.layer.externalServiceUrl) {
                                if(layerStyleObj && layerStyleObj.layer.layer.Name === layObj.layer.Name){
                                    if(layerStyleObj.style && layerStyleObj.style.Name){
                                        layerData = DataViewer.createLayerExternalWMSWithStyle(layObj.layer.externalServiceUrl,
                                            layObj.layer.externalLayer,
                                            layerStyleObj.style.Name);
                                    }else {
                                        layerData = DataViewer.createLayerExternalWMS(layObj.layer.externalServiceUrl, layObj.layer.externalLayer);
                                    }
                                }else {
                                    if(layObj.styleObj || layObj.layer.externalStyle){
                                        layerData = DataViewer.createLayerExternalWMSWithStyle(layObj.layer.externalServiceUrl,
                                            layObj.layer.externalLayer,
                                            layObj.styleObj?layObj.styleObj.Name:layObj.layer.externalStyle.split(',')[0]);
                                    }else {
                                        layerData = DataViewer.createLayerExternalWMS(layObj.layer.externalServiceUrl, layObj.layer.externalLayer);
                                    }
                                }
                            }else {//internal wms layer
                                var serviceName = (layObj.layer.serviceIdentifier) ? layObj.layer.serviceIdentifier : layObj.service.identifier;
                                if(layerStyleObj && layerStyleObj.layer.layer.Name === layObj.layer.Name){
                                    if(layerStyleObj.style && layerStyleObj.style.Name){
                                        layerData = DataViewer.createLayerWMSWithStyle(cstlUrl, layObj.layer.Name, serviceName,
                                            layerStyleObj.style.Name);
                                    }else {
                                        layerData = DataViewer.createLayerWMS(cstlUrl, layObj.layer.Name, serviceName);
                                    }
                                }else {
                                    if(layObj.styleObj || layObj.layer.externalStyle){
                                        layerData = DataViewer.createLayerWMSWithStyle(cstlUrl, layObj.layer.Name, serviceName,
                                            layObj.styleObj?layObj.styleObj.Name:layObj.layer.externalStyle.split(',')[0]);
                                    }else {
                                        layerData = DataViewer.createLayerWMS(cstlUrl, layObj.layer.Name, serviceName);
                                    }
                                }
                            }
                        } else {//internal data layer
                            var layerName,providerId;
                            var dataItem = layObj.layer;
                            if (dataItem.Namespace) {
                                layerName = '{' + dataItem.Namespace + '}' + dataItem.Name;
                            } else {
                                layerName = dataItem.Name;
                            }
                            providerId = dataItem.Provider;
                            var type = dataItem.Type?dataItem.Type.toLowerCase():null;
                            if(layerStyleObj && layerStyleObj.layer.layer.Name === dataItem.Name){
                                if(layerStyleObj.style && layerStyleObj.style.Name){
                                    layerData = DataViewer.createLayerWithStyle(cstlUrl,layerName,providerId,
                                        layerStyleObj.style.Name,null,null,type!=='vector');
                                }else {
                                    layerData = DataViewer.createLayer(cstlUrl, layerName, providerId,null,type!=='vector');
                                }
                            }else {
                                if (layObj.styleObj || dataItem.styleName) {
                                    layerData = DataViewer.createLayerWithStyle(cstlUrl,layerName,providerId,
                                        layObj.styleObj?layObj.styleObj.Name:dataItem.styleName,null,null,type!=='vector');
                                } else {
                                    layerData = DataViewer.createLayer(cstlUrl, layerName, providerId,null,type!=='vector');
                                }
                            }
                        }
                        layerData.setOpacity(layObj.opacity / 100);
                        layersToView.push(layerData);
                    }
                }
                DataViewer.layers = layersToView;
            }
            if($scope.ctxt && $scope.ctxt.crs){
                var crsCode = $scope.ctxt.crs;
                DataViewer.projection = crsCode;
                DataViewer.addBackground= crsCode==='EPSG:3857';
                if(crsCode === 'EPSG:4326' || crsCode === 'CRS:84') {
                    DataViewer.extent=[-180, -90, 180, 90];
                }
            }
            DataViewer.initMap('mapContextMap');
            if (zoomOnMapContextExtent) {
                if($scope.ctxt && $scope.ctxt.west && $scope.ctxt.south && $scope.ctxt.east && $scope.ctxt.north && $scope.ctxt.crs) {
                    var extent = [$scope.ctxt.west, $scope.ctxt.south, $scope.ctxt.east, $scope.ctxt.north];
                    DataViewer.map.updateSize();
                    //because zoomToExtent take extent in EPSG:4326 we need to reproject the zoom extent
                    if($scope.ctxt.crs !== 'EPSG:4326' && $scope.ctxt.crs !=='CRS:84'){
                        var projection = ol.proj.get($scope.ctxt.crs);
                        extent = ol.proj.transform(extent, projection,'EPSG:4326');
                    }
                    DataViewer.zoomToExtent(extent, DataViewer.map.getSize(),true);
                }
            } else {
                if($scope.layers.toSend.length===0){
                    fillLayersToSend(null);
                }
                mapcontext.extentForLayers({}, $scope.layers.toSend, function(response) {
                    useExtentForLayers(response.values);
                });
            }
        };

        function useExtentForLayers(values) {
            var west = Number(values.west);
            var south = Number(values.south);
            var east = Number(values.east);
            var north = Number(values.north);
            var extent = [west,south,east,north];
            DataViewer.map.updateSize();
            DataViewer.zoomToExtent(extent, DataViewer.map.getSize(),true);
            $scope.applyExtent();
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

        $scope.zoomToLayerExtent = function(item) {
            if(item.isWms) {
                if(item.layer.externalLayerExtent){
                    var extentArr = item.layer.externalLayerExtent.split(',');
                    if(extentArr.length===4){
                        var extent = [Number(extentArr[0]),Number(extentArr[1]),Number(extentArr[2]),Number(extentArr[3])];
                        DataViewer.zoomToExtent(extent,DataViewer.map.getSize(),false);
                    }
                }else if(item.layer.Provider) {
                    var layerName,providerId;
                    if (item.layer.Namespace) {
                        layerName = '{' + item.layer.Namespace + '}' + item.layer.Name;
                    } else {
                        layerName = item.layer.Name;
                    }
                    providerId = item.layer.Provider;
                    provider.dataGeoExtent({},{values: {'providerId':providerId,'dataId':layerName}},
                        function(response) {//success
                            var bbox = response.boundingBox;
                            if (bbox) {
                                var extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
                                DataViewer.zoomToExtent(extent,DataViewer.map.getSize(),false);
                            }
                        }
                    );
                }
            } else {
                var layerdataName,providerdataId;
                if (item.layer.Namespace) {
                    layerdataName = '{' + item.layer.Namespace + '}' + item.layer.Name;
                } else {
                    layerdataName = item.layer.Name;
                }
                providerdataId = item.layer.Provider;
                provider.dataGeoExtent({},{values: {'providerId':providerdataId,'dataId':layerdataName}},
                    function(response) {//success
                        var bbox = response.boundingBox;
                        if (bbox) {
                            var extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
                            DataViewer.zoomToExtent(extent,DataViewer.map.getSize(),false);
                        }
                    }
                );
            }
        };

        $scope.truncate = function(text,length){
            if(text) {
                return (text.length > length) ? text.substr(0, length) + "..." : text;
            }
        };

        $scope.initScopeMapContextEditor();
    })
    .controller('InternalSourceMapContextController', function($scope, dataListing, Dashboard,Growl,provider, $cookieStore) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};
        $scope.wrap.nbbypage = 5;
        $scope.searchVisible=false;

        $scope.clickFilter = function(ordType){
            $scope.wrap.ordertype = ordType;
            $scope.wrap.orderreverse = !$scope.wrap.orderreverse;
        };

        $scope.initInternalSourceMapContext = function() {
            dataListing.listAll({}, function (response) {
                Dashboard($scope, response, true);
            });
            $scope.selection.internalData = null;
            setTimeout(function(){
                $scope.previewData();
            },200);
        };

        $scope.previewData = function() {
            //clear the map
            if (DataViewer.map) {
                DataViewer.map.setTarget(undefined);
            }
            DataViewer.initConfig();
            if($scope.selection.internalData){
                var cstlUrl = $cookieStore.get('cstlUrl');
                var layerName,providerId;
                var dataItem = $scope.selection.internalData;
                if (dataItem.Namespace) {
                    layerName = '{' + dataItem.Namespace + '}' + dataItem.Name;
                } else {
                    layerName = dataItem.Name;
                }
                providerId = dataItem.Provider;
                var type = dataItem.Type?dataItem.Type.toLowerCase():null;
                var layerData;
                if (dataItem.TargetStyle && dataItem.TargetStyle.length > 0) {
                    layerData = DataViewer.createLayerWithStyle(cstlUrl,layerName,providerId,
                        dataItem.TargetStyle[0].Name,null,null,type!=='vector');
                } else {
                    layerData = DataViewer.createLayer(cstlUrl, layerName, providerId,null,type!=='vector');
                }
                //to force the browser cache reloading styled layer.
                layerData.get('params').ts=new Date().getTime();
                DataViewer.layers.push(layerData);
                provider.dataGeoExtent({},{values: {'providerId':providerId,'dataId':layerName}},
                    function(response) {//success
                        DataViewer.initMap('internalDataSourcePreview');
                        var bbox = response.boundingBox;
                        if (bbox) {
                            var extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
                            DataViewer.zoomToExtent(extent,DataViewer.map.getSize(),false);
                        }
                    }, function() {//error
                        // failed to find an extent, just load the full map
                        DataViewer.initMap('internalDataSourcePreview');
                    }
                );
            }else {
                DataViewer.initMap('internalDataSourcePreview');
                DataViewer.map.getView().setZoom(DataViewer.map.getView().getZoom()+1);
            }
        };

        $scope.setTargetStyle = function(data,index) {
            var tmp = data.TargetStyle.splice(index,1);
            data.TargetStyle.unshift(tmp[0]);
            $scope.previewData();
        };

        $scope.truncate = function(text,length){
            if(text) {
                return (text.length > length) ? text.substr(0, length) + "..." : text;
            }
        };

        $scope.initInternalSourceMapContext();

    })
    .controller('WMSSourceMapContextController', function($scope,webService,Growl,provider,$cookieStore,$translate,mapcontext) {

        $scope.servicesLayers = [];

        $scope.getCurrentLang = function() {
            return $translate.use();
        };

        $scope.initWmsSourceMapContext = function() {
            webService.listServiceLayers({lang: $scope.getCurrentLang()}, function(response) {
                $scope.servicesLayers = response;
            });
            $scope.selection.layer = null;
            $scope.selection.service = null;
            $scope.selection.extLayer = null;
            setTimeout(function(){
                $scope.previewWMSLayer();
            },200);
        };

        $scope.previewWMSLayer = function() {
            //clear the map
            if (DataViewer.map) {
                DataViewer.map.setTarget(undefined);
            }
            DataViewer.initConfig();
            var layerData;
            if($scope.selection.layer && $scope.selection.service){
                var cstlUrl = $cookieStore.get('cstlUrl');
                var serviceName = ($scope.selection.layer.serviceIdentifier) ? $scope.selection.layer.serviceIdentifier : $scope.selection.service.identifier;
                if ($scope.selection.layer.TargetStyle && $scope.selection.layer.TargetStyle.length > 0) {
                    layerData = DataViewer.createLayerWMSWithStyle(cstlUrl, $scope.selection.layer.Name, serviceName, $scope.selection.layer.TargetStyle[0].Name);
                } else {
                    layerData = DataViewer.createLayerWMS(cstlUrl, $scope.selection.layer.Name, serviceName);
                }
                //to force the browser cache reloading styled layer.
                layerData.get('params').ts=new Date().getTime();
                DataViewer.layers.push(layerData);
                DataViewer.initMap('wmsDataSourcePreview');
                var arrayLayer = [];
                arrayLayer.push({
                        mapcontextId:null,
                        layerId: $scope.selection.layer.Id,
                        dataId: null,
                        iswms: true,
                        styleId: $scope.selection.layer.styleId,
                        layerOrder: 0,
                        layerOpacity: 100,
                        layerVisible: true,
                        externalServiceUrl: null,
                        externalServiceVersion: null,
                        externalLayer: null,
                        externalLayerExtent: null,
                        externalStyle: null
                });
                zoomToLayer(arrayLayer);
            } else if($scope.selection.extLayer) {
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
                if ($scope.selection.extLayer.Style && $scope.selection.extLayer.Style.length>0) {
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
                    externalStyle: extStyle
                };
                if($scope.selection.extLayer.Style && $scope.selection.extLayer.Style.length>0){
                    layerData = DataViewer.createLayerExternalWMSWithStyle(layerExt.externalServiceUrl,
                                                                           layerExt.externalLayer,
                                                                           $scope.selection.extLayer.Style[0].Name);
                }else {
                    layerData = DataViewer.createLayerExternalWMS(layerExt.externalServiceUrl,layerExt.externalLayer);
                }
                //to force the browser cache reloading styled layer.
                layerData.get('params').ts=new Date().getTime();
                DataViewer.layers.push(layerData);

                //zoom to layer extent
                DataViewer.initMap('wmsDataSourcePreview');
                var arrayExtLayer = [];
                arrayExtLayer.push({
                    mapcontextId:null,
                    layerId: null,
                    dataId: null,
                    iswms: true,
                    styleId: null,
                    layerOrder: 0,
                    layerOpacity: 100,
                    layerVisible: true,
                    externalServiceUrl: layerExt.externalServiceUrl,
                    externalServiceVersion: layerExt.externalServiceVersion,
                    externalLayer: layerExt.externalLayer,
                    externalLayerExtent: layerExt.externalLayerExtent,
                    externalStyle: layerExt.externalStyle
                });
                zoomToLayer(arrayExtLayer);
            } else {
                DataViewer.initMap('wmsDataSourcePreview');
                DataViewer.map.getView().setZoom(DataViewer.map.getView().getZoom()+1);
            }
        };

        $scope.searchAndDisplayWmsLayers = function() {
            if ($scope.external.serviceUrl) {
                // Try in WMS version 1.3.0
                mapcontext.listExtLayers({version: "1.3.0"}, $scope.external.serviceUrl,
                    function(response) {//on success
                        $scope.external.layers = response;
                        $scope.mode.dispWmsLayers = true;
                    }, function() {//on error
                        // If it fails try it in version 1.1.1
                        mapcontext.listExtLayers({version: "1.1.1"}, $scope.external.serviceUrl,
                            function(response) {//on success
                                $scope.external.layers = response;
                                $scope.mode.dispWmsLayers = true;
                            }, function() {//on error
                                Growl('error', 'Error', 'Unable to find layers for this url');
                            }
                        );
                    }
                );
            }
        };

        function zoomToLayer(layerObj) {
            mapcontext.extentForLayers({},layerObj,
                function(response) {//on success
                    var values = response.values;
                    var crs = values.crs;
                    var west = Number(values.west);
                    var south = Number(values.south);
                    var east = Number(values.east);
                    var north = Number(values.north);
                    var extent = [west,south,east,north];
                    DataViewer.map.updateSize();
                    //because zoomToExtent take extent in EPSG:4326 we need to reproject the zoom extent
                    if(crs !== 'EPSG:4326' && crs !=='CRS:84'){
                        var projection = ol.proj.get(crs);
                        extent = ol.proj.transform(extent, projection,'EPSG:4326');
                    }
                    DataViewer.zoomToExtent(extent, DataViewer.map.getSize(),true);
                }
            );
        }

        $scope.setTargetStyle = function(data,index) {
            var tmp = data.TargetStyle.splice(index,1);
            data.TargetStyle.unshift(tmp[0]);
            $scope.previewWMSLayer();
        };

        $scope.setExtTargetStyle = function(data,index) {
            var tmp = data.Style.splice(index,1);
            data.Style.unshift(tmp[0]);
            $scope.previewWMSLayer();
        };

        $scope.truncate = function(text,length){
            if(text) {
                return (text.length > length) ? text.substr(0, length) + "..." : text;
            }
        };

        $scope.initWmsSourceMapContext();

    })
    .controller('MapContextViewerModalController', function($scope,$modalInstance,ctxtToEdit,layersForCtxt,$cookieStore) {
        // item to save in the end
        $scope.ctxt = ctxtToEdit || {};

        $scope.layers = {
            toAdd: layersForCtxt || [],
            toSend: [], // List of layers really sent
            toStyle: null // Layer on which to apply the selected style
        };

        $scope.dismiss = function () {
            $modalInstance.dismiss('close');
        };

        $scope.close = function () {
            $modalInstance.close();
        };

        $scope.initScope = function() {
            setTimeout(function(){
                $scope.showMap();
            },300);
        };

        $scope.showMap = function() {
            DataViewer.initConfig();
            if ($scope.layers.toAdd && $scope.layers.toAdd.length>0) {
                var cstlUrl = $cookieStore.get('cstlUrl');
                var layersToView = [];
                for (var i=0; i<$scope.layers.toAdd.length; i++) {
                    var layObj = $scope.layers.toAdd[i];
                    if (layObj.visible) {
                        var layerData;
                        if (layObj.isWms) {//wms layer external and internal
                            if(layObj.layer.externalServiceUrl) {
                                layerData = (layObj.layer.externalStyle) ?
                                    DataViewer.createLayerExternalWMSWithStyle(layObj.layer.externalServiceUrl,
                                        layObj.layer.externalLayer, layObj.layer.externalStyle.split(',')[0]) :
                                    DataViewer.createLayerExternalWMS(layObj.layer.externalServiceUrl, layObj.layer.externalLayer);
                            }else {
                                var serviceName = (layObj.layer.serviceIdentifier) ? layObj.layer.serviceIdentifier : layObj.service.identifier;
                                if(layObj.layer.externalStyle){
                                    layerData = DataViewer.createLayerWMSWithStyle(cstlUrl, layObj.layer.Name, serviceName, layObj.layer.externalStyle.split(',')[0]);
                                }else {
                                    layerData = DataViewer.createLayerWMS(cstlUrl, layObj.layer.Name, serviceName);
                                }
                            }
                        } else {//internal data layer
                            var layerName,providerId;
                            var dataItem = layObj.layer;
                            var type = dataItem.Type?dataItem.Type.toLowerCase():null;
                            if (dataItem.Namespace) {
                                layerName = '{' + dataItem.Namespace + '}' + dataItem.Name;
                            } else {
                                layerName = dataItem.Name;
                            }
                            providerId = dataItem.Provider;
                            if (layObj.styleObj || dataItem.styleName) {
                                layerData = DataViewer.createLayerWithStyle(cstlUrl,layerName,providerId,
                                    layObj.styleObj?layObj.styleObj.Name:dataItem.styleName,null,null,type!=='vector');
                            } else {
                                layerData = DataViewer.createLayer(cstlUrl, layerName, providerId,null,type!=='vector');
                            }
                        }
                        layerData.setOpacity(layObj.opacity / 100);
                        layersToView.push(layerData);
                    }
                }
                DataViewer.layers = layersToView;
            }
            if($scope.ctxt && $scope.ctxt.crs){
                var crsCode = $scope.ctxt.crs;
                DataViewer.projection = crsCode;
                DataViewer.addBackground= crsCode==='EPSG:3857';
                if(crsCode === 'EPSG:4326' || crsCode === 'CRS:84') {
                    DataViewer.extent=[-180, -90, 180, 90];
                }
            }
            DataViewer.initMap('mapContextViewerMap');
            if($scope.ctxt && $scope.ctxt.west && $scope.ctxt.south && $scope.ctxt.east && $scope.ctxt.north && $scope.ctxt.crs) {
                var extent = [$scope.ctxt.west, $scope.ctxt.south, $scope.ctxt.east, $scope.ctxt.north];
                DataViewer.map.updateSize();
                //because zoomToExtent take extent in EPSG:4326 we need to reproject the zoom extent
                if($scope.ctxt.crs !== 'EPSG:4326' && $scope.ctxt.crs !=='CRS:84'){
                    var projection = ol.proj.get($scope.ctxt.crs);
                    extent = ol.proj.transform(extent, projection,'EPSG:4326');
                }
                DataViewer.zoomToExtent(extent, DataViewer.map.getSize(),true);
            }
        };
        $scope.initScope();
    });