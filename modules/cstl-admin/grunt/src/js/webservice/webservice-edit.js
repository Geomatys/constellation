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

angular.module('cstl-webservice-edit', ['cstl-restapi', 'cstl-services', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .controller('WebServiceEditController', function($rootScope, $scope, $routeParams , webService, dataListing, provider,
                                                     csw, sos, $modal, textService, Dashboard, Growl, $filter,
                                                     DomainResource,StyleSharedService, style, $cookieStore, $translate,
                                                     $window, cfpLoadingBar) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.tagText = '';
        $scope.type = $routeParams.type;
        $scope.cstlUrl = $cookieStore.get('cstlUrl');
        $scope.url = $scope.cstlUrl + "WS/" + $routeParams.type + "/" + $routeParams.id;
        $scope.urlBoxSize = Math.min($scope.url.length,100);
        $scope.domainId = $cookieStore.get('cstlActiveDomainId');
        $scope.writeOperationAvailable = $scope.type === 'csw' || $scope.type === 'sos' || $scope.type === 'wfs';
        $scope.hideScroll = true;


        angular.element($window).bind("scroll", function() {
            if (this.pageYOffset < 220) {
                $scope.hideScroll = true;
            } else {
                $scope.hideScroll = false;
            }
            $scope.$apply();
        });

        $scope.getCurrentLang = function() {
            var lang = $translate.use();
            if(!lang){
                lang = 'en';
            }
            return lang;
        };

        webService.get({type: $scope.type, id: $routeParams.id, lang: $scope.getCurrentLang()}, function (service) {
            $scope.service = service;
            webService.permissionByDomainRole(function (domainroles) {
                $scope.domainroles = domainroles;
            });
            webService.domains({id: service.id}, function (domains) {
                $scope.domains = domains;
            });

            $scope.toggleDomain = function (i) {
                var pathParams = {domainId: $scope.domains[i].id, serviceId: service.id};
                if ($scope.domains[i].linked) {
                    webService.unlinkFromDomain(pathParams, function () {
                        $scope.domains[i].linked = !$scope.domains[i].linked;
                        $scope.domains[i].linked = false;
                    }, function (response) {
                        Growl('error', 'error', response.data.message);
                        webService.domains({id: service.id}, function (domains) {
                            $scope.domains = domains;
                        });
                    });
                } else {
                    webService.linkToDomain(pathParams, {}, function () {
                        $scope.domains[i].linked = true;
                    }, function () {

                    });
                }
            };
        });

        $scope.getVersionsForType = function() {
            if ($scope.type === 'wms') {
                return [{ 'id': '1.1.1','checked':false}, { 'id': '1.3.0','checked':false}];
            }
            if ($scope.type === 'wfs') {
                return [{ 'id': '1.1.0','checked':false}, { 'id': '2.0.0','checked':false}];
            }
            if ($scope.type === 'wcs') {
                return [{ 'id': '1.0.0','checked':false}];
            }
            if ($scope.type === 'wmts') {
                return [{ 'id': '1.0.0','checked':false}];
            }
            if ($scope.type === 'csw') {
                return [{ 'id': '2.0.0','checked':false}, { 'id': '2.0.2','checked':false}];
            }
            if ($scope.type === 'sos') {
                return [{ 'id': '1.0.0','checked':false}, { 'id': '2.0.0','checked':false}];
            }
            if ($scope.type === 'wps') {
                return [{ 'id': '1.0.0','checked':false}];
            }
            return [];
        };

        $scope.metadata = webService.metadata({type: $scope.type,
                                               id:$routeParams.id,
                                               lang:$scope.getCurrentLang()},
            function(response){//on success
                $scope.versions = $scope.getVersionsForType();
                if($scope.versions.length>0){
                    for(var i=0;i<$scope.versions.length;i++){
                        var version = $scope.versions[i];
                        version.checked = ($scope.metadata.versions.indexOf(version.id)!==-1);
                    }
                }
            },function(response){//on error
                Growl('error','Error','Unable to get service metadata');
            }
        );

        $scope.tabdata = true;
        $scope.tabdesc = false;
        $scope.tabmetadata = false;
        $scope.tabrights = false;

        $scope.selectTab = function(item) {
            if (item === 'tabdata') {
                $scope.tabdata = true;
                $scope.tabdesc = false;
                $scope.tabmetadata = false;
                $scope.tabrights = false;
            } else if (item === 'tabdesc') {
                $scope.tabdata = false;
                $scope.tabdesc = true;
                $scope.tabmetadata = false;
                $scope.tabrights = false;
            } else if (item === 'tabmetadata') {
                $scope.tabdata = false;
                $scope.tabdesc = false;
                $scope.tabmetadata = true;
                $scope.tabrights = false;
            } else {
                $scope.tabdata = false;
                $scope.tabdesc = false;
                $scope.tabmetadata = false;
                $scope.tabrights = true;
            }
        };

        $scope.initScope = function() {
            if ($scope.type === 'csw') {
                csw.count({id: $routeParams.id}, {}, function(max) {
                    csw.getRecords({id: $routeParams.id, count: max.value, startIndex: 0}, {}, function(response) {
                        Dashboard($scope, response.BriefNode, false);
                        $scope.wrap.filtertype = "";

                        var mdIds = [];
                        for (var i=0; i<response.BriefNode.length; i++) {
                            mdIds.push(response.BriefNode[i].identifier);
                        }
                        dataListing.dataForMetadata({}, mdIds,
                            function(response) { $scope.relatedDatas = response; },
                            function() { Growl('error','Error','Unable to get related data for metadata'); }
                        );
                    });
                });
            } else if ($scope.type === 'sos') {
                sos.sensorsTree({id: $routeParams.id}, function(sensors) {
                    Dashboard($scope, sensors.children, false);
                    $scope.layers = sensors.children;

                }, function() { Growl('error','Error','Unable to list sensors'); });
            } else if ($scope.type === 'wps') {
                $scope.config = webService.config({type: $scope.type, id:$routeParams.id});
                //@TODO get process list
            } else {
                $scope.config = webService.config({type: $scope.type, id:$routeParams.id});
                $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                    Dashboard($scope, response, true);
                    $scope.wrap.filtertype = "";
                    setTimeout(function(){
                        $scope.showLayerDashboardMap();
                    },300);
                });
            }
        };

        /**
         * Reset filters for dashboard
         */
        $scope.resetFilters = function(){
            $scope.wrap.ordertype= ($scope.service && $scope.service.type && $scope.service.type.toLowerCase()==='sos') ? 'id' : ($scope.service && $scope.service.type && $scope.service.type.toLowerCase==='csw') ? 'title' : 'Name';
            $scope.wrap.orderreverse=false;
            $scope.wrap.filtertext='';
            $scope.selected=null;
            if($scope.type !== 'csw' && $scope.type !== 'sos' && $scope.type !== 'wps') {
                $scope.showLayerDashboardMap();
            }
        };


        // define which version to set
        $scope.selectedVersion = function (){
            var selVersions = $filter('filter')($scope.versions, {checked: true});
            $scope.metadata.versions = [];
            for(var i=0; i < selVersions.length; i++) {
                $scope.metadata.versions.push(selVersions[i].id);
            }
        };

        $scope.addTag = function() {
            if (!$scope.tagText || $scope.tagText === '' || $scope.tagText.length === 0) {
                return;
            }
            if ($scope.metadata.keywords ===null){
                $scope.metadata.keywords = [];
            }
            $scope.metadata.keywords.push($scope.tagText);
            $scope.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.tagText.length === 0 && !key) {
                $scope.metadata.keywords.pop();
            } else if (key) {
                $scope.metadata.keywords.splice(key, 1);
            }
        };

        $scope.selectedMetadataChild = null;
        $scope.selectedSensorsChild = null;

        $scope.selectMetadataChild = function(item) {
            if ($scope.selectedMetadataChild === item) {
                $scope.selectedMetadataChild = null;
            } else {
                $scope.selectedMetadataChild = item;
            }
        };
        $scope.selectSensorsChild = function(item) {
            if ($scope.selectedSensorsChild === item) {
                $scope.selectedSensorsChild = null;
            } else {
                $scope.selectedSensorsChild = item;
            }
        };

        $scope.saveServiceMetadata = function() {
            webService.updateMd({type: $scope.service.type, id: $scope.service.identifier},$scope.metadata,
                function(response) {
                    if (response.status==="Success") {
                        Growl('success','Success','Service description successfully updated');
                    }else{
                        Growl('error','Error','Service description update failed due to :'+response.status);
                    }
                },
                function() {
                    Growl('error','Error','Service description update failed');
                }
            );
        };

        // Show Capa methods
        $scope.showCapa = function(service) {
            if (service.versions.length > 1) {
                var modal = $modal.open({
                    templateUrl: 'views/webservice/modalChooseVersion.html',
                    controller: 'WebServiceVersionsController',
                    resolve: {
                        service: function() { return service; }
                    }
                });
                modal.result.then(function(result) {
                    showModalCapa(service, result);
                });
            } else {
                showModalCapa(service, service.versions[0]);
            }
        };

        function showModalCapa(service, version) {
            $modal.open({
                templateUrl: 'views/webservice/modalCapa.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.capa(service.type.toLowerCase(), service.identifier, version);
                    }
                }
            });
        }

        // Show Logs methods
        $scope.showLogs = function(service) {
            $modal.open({
                templateUrl: 'views/webservice/modalLogs.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.logs(service.type.toLowerCase(), service.identifier);
                    }
                }
            });
        };

        $scope.reload = function(service){
            webService.restart({type: service.type, id: service.identifier}, {value: true},
                function() { Growl('success','Success','Service '+ service.name +' successfully reloaded'); },
                function() { Growl('error','Error','Service '+ service.name +' reload failed'); }
            );
        };

        $scope.startOrStop = function(service){
            if(service.status==='STARTED'){
                webService.stop({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.service.status = "NOT_STARTED";
                        Growl('success','Success','Service '+ service.name +' successfully stopped');
                        $scope.showLayerDashboardMap();
                    }
                }, function() { Growl('error','Error','Service '+ service.name +' stop failed'); });
            }else{
                webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.service.status = "STARTED";
                        Growl('success','Success','Service '+ service.name +' successfully started');
                        $scope.showLayerDashboardMap();
                    }
                }, function() { Growl('error','Error','Service '+ service.name +' start failed'); });
            }
        };

        // Allow to choose data to add for this service
        $scope.showDataToAdd = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalDataChoose.html',
                controller: 'DataModalController',
                resolve: {
                    exclude: function() { return $scope.layers; },
                    service: function() { return $scope.service; }
                }
            });
            modal.result.then(function() {
                if ($scope.type.toLowerCase() !== 'sos') {
                    $scope.layers = webService.layers({type: $scope.type, id: $routeParams.id}, {}, function (response) {
                        Dashboard($scope, response, true);
                        $scope.selected = null;
                        $scope.showLayerDashboardMap();
                    });
                } else {
                    $scope.initScope();
                }
            });
        };

        $scope.showDataToAddWMTS = function() {
            var modal = $modal.open({
                templateUrl: 'views/webservice/wmts/modalAddLayer.html',
                controller: 'WMTSAddLayerModalController',
                resolve: {
                    service: function() { return $scope.service; }
                }
            });
            modal.result.then(function() {
                $scope.layers = webService.layers({type: $scope.type,
                                                   id: $routeParams.id},
                                                   {},
                    function (response) {//success
                        Dashboard($scope, response, true);
                        $scope.selected = null;
                        $scope.showLayerDashboardMap();
                    }
                );
            });
        };

        $scope.deleteLayer = function() {
            var keymsg = ($scope.service.type.toLowerCase() === 'wmts') ? "dialog.message.confirm.delete.tiledLayer" : "dialog.message.confirm.delete.layer";
            if ($scope.selected) {
                var dlg = $modal.open({
                    templateUrl: 'views/modal-confirm.html',
                    controller: 'ModalConfirmController',
                    resolve: {
                        'keyMsg':function(){return keymsg;}
                    }
                });
                dlg.result.then(function(cfrm){
                    if(cfrm){
                        if ($scope.service.type.toLowerCase() === 'sos') {
                            var idToDel = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                            sos.removeSensor({id: $scope.service.identifier, sensor: idToDel},
                              function() {
                                Growl('success', 'Success', 'Sensor ' + idToDel + ' successfully removed from service ' + $scope.service.name);
                                $scope.initScope();
                            },function () {
                                Growl('error', 'Error', 'Unable to remove sensor ' + idToDel + ' from service ' + $scope.service.name);
                            });
                        } else {
                            webService.deleteLayer({type: $scope.service.type,
                                                    id: $scope.service.identifier,
                                                    layerid: $scope.selected.Name},
                                                   {value: $scope.selected.Namespace},
                                function () {//on success
                                    if ($scope.service.type.toLowerCase() === 'wmts') {
                                        $scope.deleteTiledData($scope.service, $scope.selected.Name, $scope.selected.Provider);
                                    }
                                    Growl('success', 'Success', 'Layer ' + $scope.selected.Name + ' successfully deleted from service ' + $scope.service.name);
                                    $scope.layers = webService.layers({type: $scope.type, id: $routeParams.id}, {},
                                    function (response) {
                                        Dashboard($scope, response, true);
                                        $scope.selected=null;
                                        $scope.showLayerDashboardMap();
                                    });
                                },
                                function () {
                                    Growl('error', 'Error', 'Layer ' + $scope.selected.Name + ' failed to be deleted from service ' + $scope.service.name);
                                }
                            );
                        }
                    }
                });
            }
        };

        $scope.deleteTiledData = function(service, layerName, providerId) {
            dataListing.deletePyramidFolder({providerId: providerId}, function(response) {
                if(response.isPyramid){
                    provider.delete({id: providerId}, function() {}, function() {
                        Growl('error','Error','Unable to delete data for layer '+ layerName);
                    });
                }
            });
        };

        /**
         * Open metadata viewer popup and display metadata
         * in appropriate template depending on data type property.
         * this function is called from metadata dashboard.
         */
        $scope.displayMetadataFromCSW = function() {
            var type = 'import';
            if($scope.selectedMetadataChild){
                type = $scope.selectedMetadataChild.Type.toLowerCase();
            }
            if(type.toLowerCase() === 'coverage'){
                type = 'raster';
            }
            $modal.open({
                templateUrl: 'views/data/modalViewMetadata.html',
                controller: 'ViewMetadataModalController',
                resolve: {
                    'dashboardName':function(){return 'dataset';},
                    'metadataValues':function(textService){
                        return textService.cswMetadataJson($scope.service.identifier,$scope.selected.identifier,type,true);
                    }
                }
            });
        };

        /**
         * Open metadata editor in modal popup.
         */
        $scope.displayMetadataEditor = function() {
            var typeToSend;
            if($scope.selectedMetadataChild){
                typeToSend = $scope.selectedMetadataChild.Type.toLowerCase();
            }else {
                typeToSend = 'import';
            }
            if(typeToSend.toLowerCase() === 'coverage'){
                typeToSend = 'raster';
            }
            openModalEditor($scope.service.identifier,$scope.selected.identifier,typeToSend,typeToSend);
        };

        /**
         * Open modal for metadata editor
         * for given provider id, data type and template.
         * @param serviceId
         * @param recordId
         * @param type
         * @param template
         */
        function openModalEditor(serviceId,recordId,type,template){
            $modal.open({
                templateUrl: 'views/data/modalEditMetadata.html',
                controller: 'EditCSWMetadataModalController',
                resolve: {
                    'serviceId':function(){return serviceId;},
                    'recordId':function(){return recordId;},
                    'type':function(){return type;},
                    'template':function(){return template;}
                }
            });
        }

        /**
         * Open modal to edit layer title.
         * in future this modal can be used to edit other attributes.
         */
        $scope.editLayerInfo = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/layerInfo.html',
                controller: 'LayerInfoModalController',
                resolve: {
                    'serviceType':function(){return $scope.service.type;},
                    'serviceIdentifier':function(){return $scope.service.identifier;},
                    'selectedLayer':function(){return $scope.selected;}
                }
            });
            modal.result.then(function() {
                $scope.layers = webService.layers({type: $scope.type, id: $routeParams.id},{},function (response) {
                    Dashboard($scope, response, true);
                    $scope.wrap.ordertype='Name';
                    $scope.showLayerDashboardMap();
                });
            });
        };

        /**
         * binding action to delete metadata from csw service dashboard page.
         */
        $scope.deleteMetadata = function() {
            if ($scope.selected) {
                var dlg = $modal.open({
                    templateUrl: 'views/modal-confirm.html',
                    controller: 'ModalConfirmController',
                    resolve: {
                        'keyMsg':function(){return "dialog.message.confirm.delete.metadata";}
                    }
                });
                dlg.result.then(function(cfrm){
                    if(cfrm){
                        csw.delete({id: $scope.service.identifier, metaId: $scope.selected.identifier}, {},
                            function() {
                                Growl('success','Success','Metadata deleted');
                                csw.count({id: $routeParams.id}, {}, function(max) {
                                    csw.getRecords({id: $routeParams.id, count: max.value, startIndex: 0}, {}, function(response) {
                                        Dashboard($scope, response.BriefNode, false);
                                        $scope.wrap.filtertype = "";
                                    });
                                });
                            }, function() { Growl('error','Error','Failed to delete metadata'); }
                        );
                    }
                });
            }
        };

        $scope.showLayerDashboardMap = function() {
            if($scope.type !== 'sos' && $scope.type !== 'csw' && $scope.type !== 'wps') {
                if($scope.type === 'wmts') {
                    if (WmtsLayerDashboardViewer.map) {
                        WmtsLayerDashboardViewer.map.setTarget(undefined);
                    }
                    WmtsLayerDashboardViewer.initConfig();
                    WmtsLayerDashboardViewer.fullScreenControl = true;
                    if($scope.service.status !== "STARTED"){
                        WmtsLayerDashboardViewer.initMap('wmtsPreviewMap');
                        return;
                    }
                    if($scope.selected) {
                        var wmtslayerName = $scope.selected.Name;
                        // Get wmts values: resolutions, extent, matrixSet and matrixIds
                        textService.capa($scope.service.type.toLowerCase(),
                                $scope.service.identifier,
                                $scope.service.versions[0])
                            .success(function(data, status, headers, config) {
                                webService.extractWMTSLayerInfo({"type":$scope.service.type,
                                        "id":$scope.service.identifier,
                                        "layerName":wmtslayerName,
                                        "crs":WmtsLayerDashboardViewer.projection},
                                    data,
                                    function(response){//success
                                        var wmtsValues = {
                                            "url":$scope.cstlUrl +'WS/wmts/'+ $scope.service.identifier,
                                            "resolutions": response.resolutions,
                                            "matrixSet":response.matrixSet,
                                            "matrixIds":response.matrixIds,
                                            "style":response.style,
                                            "dataExtent":response.dataExtent
                                        };
                                        var layerwmts = WmtsLayerDashboardViewer.createLayer(wmtslayerName, $scope.service.identifier, wmtsValues);
                                        WmtsLayerDashboardViewer.layers = [layerwmts];
                                        WmtsLayerDashboardViewer.initMap('wmtsPreviewMap');
                                        WmtsLayerDashboardViewer.map.getView().fitExtent(wmtsValues.dataExtent,WmtsLayerDashboardViewer.map.getSize());
                                    });
                            });
                    }else {
                        WmtsLayerDashboardViewer.initMap('wmtsPreviewMap');
                    }
                }else {
                    if (LayerDashboardViewer.map) {
                        LayerDashboardViewer.map.setTarget(undefined);
                    }
                    LayerDashboardViewer.initConfig();
                    LayerDashboardViewer.fullScreenControl = true;
                    if($scope.service.status !== "STARTED"){
                        LayerDashboardViewer.initMap('layerPreviewMap');
                        return;
                    }
                    if($scope.selected) {
                        var layerName = $scope.selected.Name;
                        var layerData;
                        var providerId = $scope.selected.Provider;
                        if ($scope.selected.TargetStyle && $scope.selected.TargetStyle.length > 0) {
                            if($scope.service.type.toLowerCase() === 'wms') {
                                layerData = LayerDashboardViewer.createLayerWMSWithStyle($scope.cstlUrl, layerName,$scope.service.identifier,$scope.selected.TargetStyle[0].Name);
                            }else {
                                layerData = LayerDashboardViewer.createLayerWithStyle($scope.cstlUrl, layerName, providerId,
                                    $scope.selected.TargetStyle[0].Name,null,null,true);
                            }
                        } else {
                            if($scope.service.type.toLowerCase() === 'wms') {
                                layerData = LayerDashboardViewer.createLayerWMS($scope.cstlUrl, layerName, $scope.service.identifier);
                            }else {
                                layerData = LayerDashboardViewer.createLayer($scope.cstlUrl, layerName, providerId,null,true);
                            }
                        }
                        LayerDashboardViewer.layers = [layerData];
                        provider.dataGeoExtent({},{values: {'providerId':providerId,'dataId':layerName}},
                            function(response) {//success
                                var bbox = response.boundingBox;
                                if (bbox) {
                                    var extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
                                    LayerDashboardViewer.extent = extent;
                                }
                                LayerDashboardViewer.initMap('layerPreviewMap');
                            }, function() {//error
                                // failed to find a metadata, just load the full map
                                LayerDashboardViewer.initMap('layerPreviewMap');
                            }
                        );
                    }else {
                        LayerDashboardViewer.initMap('layerPreviewMap');
                    }
                }
            }
        };

        $scope.showLayer = function() {
            //clear the map
            if (DataViewer.map) {
                DataViewer.map.setTarget(undefined);
            }
            DataViewer.initConfig();
            if (WmtsViewer.map) {
                WmtsViewer.map.setTarget(undefined);
            }
            var viewerData = $('#viewerData');
            viewerData.modal("show");
            viewerData.off('shown.bs.modal');
            viewerData.on('shown.bs.modal', function (e) {
                var layerName = $scope.selected.Name;
                if ($scope.service.type.toLowerCase() === 'wmts') {
                    // Get wmts values: resolutions, extent, matrixSet and matrixIds
                    textService.capa($scope.service.type.toLowerCase(),
                                     $scope.service.identifier,
                                     $scope.service.versions[0])
                        .success(function(data, status, headers, config) {
                            webService.extractWMTSLayerInfo({"type":$scope.service.type,
                                                             "id":$scope.service.identifier,
                                                             "layerName":layerName,
                                                             "crs":WmtsViewer.projection},
                                                             data,
                                function(response){//success
                                    var wmtsValues = {
                                        "url":$scope.cstlUrl +'WS/wmts/'+ $scope.service.identifier,
                                        "resolutions": response.resolutions,
                                        "matrixSet":response.matrixSet,
                                        "matrixIds":response.matrixIds,
                                        "style":response.style,
                                        "dataExtent":response.dataExtent
                                    };
                                    var layerwmts = WmtsViewer.createLayer(layerName, $scope.service.identifier, wmtsValues);
                                    WmtsViewer.layers = [layerwmts];
                                    WmtsViewer.initMap('dataMap');
                                    WmtsViewer.map.getView().fitExtent(wmtsValues.dataExtent,WmtsViewer.map.getSize());
                            });
                        });
                } else {
                    var layerData;
                    var providerId = $scope.selected.Provider;
                    if ($scope.selected.TargetStyle && $scope.selected.TargetStyle.length > 0) {
                        if($scope.service.type.toLowerCase() === 'wms') {
                            //create wms layer
                            layerData = DataViewer.createLayerWMSWithStyle($scope.cstlUrl, layerName,$scope.service.identifier,$scope.selected.TargetStyle[0].Name);
                        }else {
                            //create portrayal layer
                            layerData = DataViewer.createLayerWithStyle($scope.cstlUrl, layerName, providerId,
                                $scope.selected.TargetStyle[0].Name,null,null,true);
                        }
                    } else {
                        if($scope.service.type.toLowerCase() === 'wms') {
                            //create wms layer
                            layerData = DataViewer.createLayerWMS($scope.cstlUrl, layerName, $scope.service.identifier);
                        }else {
                            //create portrayal layer
                            layerData = DataViewer.createLayer($scope.cstlUrl, layerName, providerId,null,true);
                        }
                    }

                    //attach event loader in modal map viewer
                    layerData.on('precompose',function(){
                        $scope.$apply(function() {
                            window.cfpLoadingBar_parentSelector = '#dataMap';
                            cfpLoadingBar.start();
                            cfpLoadingBar.inc();
                        });
                    });
                    layerData.on('postcompose',function(){
                        cfpLoadingBar.complete();
                        window.cfpLoadingBar_parentSelector = null;
                    });

                    DataViewer.layers = [layerData];
                    provider.dataGeoExtent({},{values: {'providerId':providerId,'dataId':layerName}},
                        function(response) {//success
                            var bbox = response.boundingBox;
                            if (bbox) {
                                var extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
                                DataViewer.extent = extent;
                            }
                            DataViewer.initMap('dataMap');
                        }, function() {//error
                            // failed to find a metadata, just load the full map
                            DataViewer.initMap('dataMap');
                        }
                    );
                }
            });
        };

        $scope.showSensor = function() {
            var sensorId = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
            $modal.open({
                templateUrl: 'views/sensor/modalSensorView.html',
                controller: 'SensorModalController',
                resolve: {
                    service: function() { return $scope.service; },
                    sensorId: function() { return sensorId; }
                }
            });
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#serviceDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };

        // Style methods
        $scope.showStyleList = function() {
            StyleSharedService.showStyleList($scope,$scope.selected);
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style, $scope.selected);
        };

        $scope.truncate = function(small, text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (small === true && text.length > 22) {
                        return text.substr(0, 22) + "...";
                    } else if (small === false && text.length > 65) {
                        return text.substr(0, 65) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small === true && text.length > 15) {
                        return text.substr(0, 15) + "...";
                    } else if (small === false && text.length > 55) {
                        return text.substr(0, 55) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 35) {
                        return text.substr(0, 35) + "...";
                    } else {return text;}
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 25) {
                        return text.substr(0, 25) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 17) {
                        return text.substr(0, 17) + "...";
                    } else {return text;}
                }
            }
        };
    })
    .controller('LayerInfoModalController', function($scope, $modalInstance,webService,Growl,
                                                     serviceType,serviceIdentifier,selectedLayer){
        $scope.serviceType = serviceType;
        $scope.serviceIdentifier = serviceIdentifier;
        $scope.selectedLayer = selectedLayer;
        $scope.layerForm = {
            "title": $scope.selectedLayer.Title
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.save = function() {
            $scope.selectedLayer.Title = $scope.layerForm.title;
            webService.updateLayerTitle({type: $scope.serviceType, id: $scope.serviceIdentifier}, $scope.selectedLayer,
                function(response) {//success
                    Growl('success','Success','Layer information saved with success!');
                    $modalInstance.close();
                },
                function(response) {//error
                    Growl('error','Error','Layer name already exists!');
                }
            );
        };
    })
    .controller('EditCSWMetadataModalController', function($scope, $modalInstance, $controller,Growl,csw,serviceId,recordId,type,template) {
        //$scope.provider = id;
        $scope.serviceId = serviceId;
        $scope.recordId = recordId;
        $scope.type = type;
        $scope.template = template;
        $scope.theme = 'csw';

        $scope.dismiss = function () {
            $modalInstance.dismiss('close');
        };

        $scope.close = function () {
            $modalInstance.close();
        };

        $scope.loadMetadataValues = function(){
            csw.getJsonMetadata({'id':$scope.serviceId,
                                 'metaId':$scope.recordId,
                                 'type':$scope.type,
                                 'prune':false},
                function(response){//success
                    if (response && response.root) {
                        $scope.metadataValues.push({"root":response.root});
                    }
                },
                function(response){//error
                    Growl('error','Error','The server returned an error!');
                }
            );
        };

        /**
         * Save for metadata in modal editor mode.
         */
        $scope.save2 = function() {
            if($scope.metadataValues && $scope.metadataValues.length>0){
                csw.saveMetadata({'id':$scope.serviceId,'metaId':$scope.recordId,'type':$scope.template},
                    $scope.metadataValues[0],
                    function(response) {//success
                        $scope.close();
                        Growl('success','Success','Metadata saved with success!');
                    },
                    function(response) {//error
                        Growl('error','Error','Failed to save metadata because the server returned an error!');
                    }
                );
            }
        };

        $controller('EditMetadataController', {$scope: $scope});

    })

    .controller('WMTSAddLayerModalController', function($scope,dataListing,webService,Dashboard,$modalInstance,service,Growl) {

        //the wmts service object
        $scope.service = service;

        // handle display mode for this modal popup
        $scope.mode = {
            display: 'sourceSelection',
            previous: undefined
        };
        // for SDI this params are hardcoded
        $scope.tileFormat = 'PNG'; //PNG will be used as default
        $scope.crs = "EPSG:3857";
        $scope.values = {
            userLayerName : '',
            listSelect : [],
            listWMTSLayers : [],
            selectedContext : null
        };

        $scope.dismiss = function() {
            $modalInstance.dismiss('close');
        };

        $scope.close = function() {
            $modalInstance.close();
        };

        $scope.isValidWMTSLayerName = function(){
            var letters = /^[A-Za-zàèìòùáéíóúäëïöüñãõåæøâêîôû0-9\-_]+$/;
            var name = $scope.values.userLayerName;
            var passRegEx = false;
            if(name && name.match(letters)) {
                passRegEx = true;
            }
            return passRegEx;
        };

        $scope.isLayerNameExists = function() {
            return checkLayerName($scope.values.userLayerName);
        };

        function checkLayerName(name) {
            if(name && name.length>0 &&
                $scope.values.listWMTSLayers && $scope.values.listWMTSLayers.length>0){
                for(var i=0;i<$scope.values.listWMTSLayers.length;i++){
                    var lay=$scope.values.listWMTSLayers[i];
                    if(lay.Name === name || lay.Alias === name) {
                        return true;
                    }
                }
            }
            return false;
        }

        $scope.goToLastStep = function() {
            //get all layers in this wmts service to compare for existing layer names.
            webService.layers({type: $scope.service.type,
                    id: $scope.service.identifier},
                {},
                function (response) {//success
                    $scope.values.listWMTSLayers = response;
                    if($scope.mode.display==='internal' && $scope.values.userLayerName === '' && $scope.values.listSelect.length===1){
                        //set the layerName for singleton list
                        var name = $scope.values.listSelect[0].Name;
                        if(!checkLayerName(name+'_pyramid')){
                            $scope.values.userLayerName = name+'_pyramid';
                        }else {
                            $scope.values.userLayerName = '';
                        }
                    }else {
                        $scope.values.userLayerName = '';
                    }
                    $scope.mode.previous=$scope.mode.display;
                    $scope.mode.display='lastStep';
                },function(){//error
                    Growl('warning', 'Warning', 'An error occurred!');
                }
            );
        };

        $scope.submitWMTSLayer = function() {
            if($scope.mode.previous==='internal') {
                if ($scope.values.listSelect.length === 0) {
                    Growl('warning', 'Warning', 'No data selected!');
                    return;
                }
                dataListing.pyramidData({"crs": $scope.crs, "layerName":$scope.values.userLayerName},$scope.values.listSelect,
                    function(response){//success
                        if(response.dataId && response.providerId) {
                            webService.addLayer({type: $scope.service.type, id: $scope.service.identifier},
                                {layerAlias: response.dataId,
                                    layerId: response.dataId,
                                    serviceType: $scope.service.type,
                                    serviceId: $scope.service.identifier,
                                    providerId: response.providerId},
                                function () {//success
                                    Growl('success','Success','Layer successfully added to service '+$scope.service.name);
                                    $scope.close();
                                },
                                function () {
                                    Growl('error','Error','Layer failed to be added to service '+$scope.service.name);
                                    $scope.dismiss();
                                }
                            );
                        }
                    },function(response){//error
                        Growl('error', 'Error', 'Failed to generate pyramid data');
                    }
                );
            } else if($scope.mode.previous==='mapcontext') {
                if($scope.values.selectedContext === null) {
                    Growl('warning', 'Warning', 'No map context selected!');
                    return;
                }
                dataListing.pyramidMapContext({"contextId":$scope.values.selectedContext.id,
                                               "crs":$scope.crs,
                                               "layerName":$scope.values.userLayerName},{},
                    function(response){//on success
                        if(response.dataId && response.providerId) {
                            webService.addLayer({type: $scope.service.type, id: $scope.service.identifier},
                                {layerAlias: response.dataId,
                                    layerId: response.dataId,
                                    serviceType: $scope.service.type,
                                    serviceId: $scope.service.identifier,
                                    providerId: response.providerId},
                                function () {//success
                                    Growl('success','Success','Layer successfully added to service '+$scope.service.name);
                                    $scope.close();
                                },
                                function () {
                                    Growl('error','Error','Layer failed to be added to service '+$scope.service.name);
                                    $scope.dismiss();
                                }
                            );
                        }
                    },
                    function(response){//on error
                        Growl('error', 'Error', 'Failed to generate pyramid data');
                    }
                );
            }
        };

    })

    .controller('Step1WMTSInternalDataController', function($scope, dataListing, webService, Dashboard,Growl,
                                                            provider, $cookieStore) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.wrap.nbbypage = 5;

        $scope.dataSelect={all:false};

        $scope.clickFilter = function(ordType){
            $scope.wrap.ordertype = ordType;
            $scope.wrap.orderreverse = !$scope.wrap.orderreverse;
        };

        $scope.initInternalDataWMTS = function() {
            dataListing.listAll({}, function (response) {
                Dashboard($scope, response, true);
            });
            if($scope.mode.previous!=='lastStep') {
                $scope.values.listSelect.splice(0, $scope.values.listSelect.length);//clear array
            }
            setTimeout(function(){
                $scope.previewData();
            },200);
        };

        $scope.previewData = function() {
            //clear the map
            if (DataViewer.map) {
                DataViewer.map.setTarget(undefined);
            }
            var cstlUrl = $cookieStore.get('cstlUrl');
            DataViewer.initConfig();
            if($scope.values.listSelect.length >0){
                var layerName,providerId;
                for(var i=0;i<$scope.values.listSelect.length;i++){
                    var dataItem = $scope.values.listSelect[i];
                    if (dataItem.Namespace) {
                        layerName = '{' + dataItem.Namespace + '}' + dataItem.Name;
                    } else {
                        layerName = dataItem.Name;
                    }
                    providerId = dataItem.Provider;
                    var layerData;
                    var type = dataItem.Type?dataItem.Type.toLowerCase():null;
                    if (dataItem.TargetStyle && dataItem.TargetStyle.length > 0) {
                        layerData = DataViewer.createLayerWithStyle(cstlUrl,layerName,providerId,
                                                                    dataItem.TargetStyle[0].Name,null,null,type!=='vector');
                    } else {
                        layerData = DataViewer.createLayer(cstlUrl, layerName, providerId,null,type!=='vector');
                    }
                    //to force the browser cache reloading styled layer.
                    layerData.get('params').ts=new Date().getTime();
                    DataViewer.layers.push(layerData);
                }
                provider.mergedDataExtent({},$scope.values.listSelect,
                    function(response) {// on success
                        DataViewer.initMap('styledMapPreviewForWMTS');
                        if(response && response.boundingBox) {
                            var bbox = response.boundingBox;
                            var extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
                            DataViewer.zoomToExtent(extent,DataViewer.map.getSize(),false);
                        }
                    }, function() {//on error
                        // failed to calculate an extent, just load the full map
                        DataViewer.initMap('styledMapPreviewForWMTS');
                    }
                );
            }else {
                DataViewer.initMap('styledMapPreviewForWMTS');
                DataViewer.map.getView().setZoom(DataViewer.map.getView().getZoom()+1);
            }
        };

        /**
         * Proceed to select all items of dashboard
         * depending on the property binded to checkbox.
         */
        $scope.selectAllData = function() {
            $scope.values.listSelect = ($scope.dataSelect.all) ? $scope.wrap.fullList.slice(0) : [];
            $scope.previewData();
        };
        /**
         * binding call when clicking on each row item.
         */
        $scope.toggleDataInArray = function(item){
            var itemExists = false;
            for (var i = 0; i < $scope.values.listSelect.length; i++) {
                if ($scope.values.listSelect[i].Id === item.Id) {
                    itemExists = true;
                    $scope.values.listSelect.splice(i, 1);//remove item
                    break;
                }
            }
            if(!itemExists){
                $scope.values.listSelect.push(item);
            }
            $scope.dataSelect.all=($scope.values.listSelect.length === $scope.wrap.fullList.length);
            $scope.previewData();

        };
        /**
         * Returns true if item is in the selected items list.
         * binding function for css purposes.
         * @param item
         * @returns {boolean}
         */
        $scope.isInSelected = function(item){
            for(var i=0; i < $scope.values.listSelect.length; i++){
                if($scope.values.listSelect[i].Id === item.Id){
                    return true;
                }
            }
            return false;
        };

        $scope.setTargetStyle = function(data,index) {
            var tmp = data.TargetStyle.splice(index,1);
            data.TargetStyle.unshift(tmp[0]);
            $scope.previewData();
        };

        /**
         * truncate text with JS.
         * Why not use CSS for this?
         *
         * css rule is
         * {
         *  width: 100px
         *  white-space: nowrap
         *  overflow: hidden
         *  text-overflow: ellipsis // This is where the magic happens
         *  }
         *
         * @param text
         * @param length
         * @returns {string}
         */
        $scope.truncate = function(text,length){
            if(text) {
                return (text.length > length) ? text.substr(0, length) + "..." : text;
            }
        };

        $scope.initInternalDataWMTS();
    })

    .controller('Step1WMTSMapContextController', function($scope, dataListing, webService, Dashboard,Growl,
                                                            provider, $cookieStore,mapcontext) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.wrap.nbbypage = 5;

        $scope.clickFilter = function(ordType){
            $scope.wrap.ordertype = ordType;
            $scope.wrap.orderreverse = !$scope.wrap.orderreverse;
        };

        $scope.initMapContextWMTS = function() {
            mapcontext.listLayers({},{},
                function(response) {//success
                    Dashboard($scope, response, true);
                    $scope.wrap.ordertype='name';
                    $scope.wrap.filtertext='';
                }, function() {//error
                    Growl('error','Error','Unable to show layers list!');
            });

            if($scope.mode.previous!=='lastStep') {
                $scope.values.selectedContext = null;
            }
            setTimeout(function(){
                $scope.previewMapContext();
            },200);
        };

        $scope.previewMapContext = function() {
            //clear the map
            if (DataViewer.map) {
                DataViewer.map.setTarget(undefined);
            }
            DataViewer.initConfig();
            if($scope.values.selectedContext !== null){
                var minX,minY,maxX,maxY;
                minX = $scope.values.selectedContext.west;
                minY = $scope.values.selectedContext.south;
                maxX = $scope.values.selectedContext.east;
                maxY = $scope.values.selectedContext.north;
                var crsCode = $scope.values.selectedContext.crs;
                DataViewer.projection = crsCode;
                DataViewer.addBackground= crsCode==='EPSG:3857';
                if(crsCode === 'EPSG:4326' || crsCode === 'CRS:84') {
                    DataViewer.extent=[-180, -90, 180, 90];
                }
                var cstlUrl = $cookieStore.get('cstlUrl');
                if($scope.values.selectedContext.layers && $scope.values.selectedContext.layers.length>0){
                    var layersToView = [];
                    for (var i=0; i<$scope.values.selectedContext.layers.length; i++) {
                        var layer = $scope.values.selectedContext.layers[i];
                        if (layer.visible) {
                            var layerData;
                            if(layer.iswms){
                                if (layer.externalServiceUrl) {//external wms layer
                                    layerData = (layer.externalStyle) ?
                                        DataViewer.createLayerExternalWMSWithStyle(layer.externalServiceUrl, layer.externalLayer, layer.externalStyle.split(',')[0]) :
                                        DataViewer.createLayerExternalWMS(layer.externalServiceUrl, layer.externalLayer);
                                } else {//internal wms layer
                                    layerData = (layer.styleName) ?
                                        DataViewer.createLayerWMSWithStyle(cstlUrl, layer.Name, layer.serviceIdentifier, layer.styleName) :
                                        DataViewer.createLayerWMS(cstlUrl, layer.Name, layer.serviceIdentifier);
                                }
                            }else {
                                var layerName,providerId;
                                if (layer.Namespace) {
                                    layerName = '{' + layer.Namespace + '}' + layer.Name;
                                } else {
                                    layerName = layer.Name;
                                }
                                providerId = layer.Provider;
                                if (layer.externalStyle || layer.styleName) {
                                    layerData = DataViewer.createLayerWithStyle(cstlUrl,layerName,providerId,
                                        layer.externalStyle?layer.externalStyle:layer.styleName,null,null,true);
                                } else {
                                    layerData = DataViewer.createLayer(cstlUrl, layerName, providerId,null,true);
                                }
                            }
                            layerData.setOpacity(layer.opacity / 100);
                            layersToView.push(layerData);
                        }
                    }
                    DataViewer.layers = layersToView;
                    DataViewer.initMap('mapPreviewMapContextForWMTS');
                    var extent = [minX,minY,maxX,maxY];
                    if(crsCode !== 'EPSG:4326' && crsCode !=='CRS:84'){
                        var projection = ol.proj.get(crsCode);
                        extent = ol.proj.transform(extent, projection,'EPSG:4326');
                    }
                    DataViewer.zoomToExtent(extent,DataViewer.map.getSize(),true);
                } else {
                    DataViewer.initMap('mapPreviewMapContextForWMTS');
                    DataViewer.map.getView().setZoom(DataViewer.map.getView().getZoom()+1);
                }
            }else {
                DataViewer.initMap('mapPreviewMapContextForWMTS');
                DataViewer.map.getView().setZoom(DataViewer.map.getView().getZoom()+1);
            }
        };

        /**
         * binding call when clicking on each row item.
         */
        $scope.toggleContextSelection = function(item){
            if (item && $scope.values.selectedContext && $scope.values.selectedContext.id === item.id) {
                $scope.values.selectedContext = null;
            } else {
                $scope.values.selectedContext = item;
            }
            $scope.previewMapContext();
        };

        /**
         * truncate text with JS.
         * Why not use CSS for this?
         *
         * css rule is
         * {
         *  width: 100px
         *  white-space: nowrap
         *  overflow: hidden
         *  text-overflow: ellipsis // This is where the magic happens
         *  }
         *
         * @param text
         * @param length
         * @returns {string}
         */
        $scope.truncate = function(text,length){
            if(text) {
                return (text.length > length) ? text.substr(0, length) + "..." : text;
            }
        };

        $scope.initMapContextWMTS();
    });
