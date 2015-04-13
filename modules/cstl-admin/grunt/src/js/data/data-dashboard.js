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

angular.module('cstl-data-dashboard', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('DataController', function($scope, $routeParams, $location, Dashboard, webService, dataListing, datasetListing, DomainResource,
                                            provider, $window, style, textService, $modal, Growl, StyleSharedService, $cookieStore,$http) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};
        $scope.wrap.matchExactly = true;

        $scope.dataCtrl = {
            cstlUrl : $cookieStore.get('cstlUrl'),
            domainId : $cookieStore.get('cstlActiveDomainId'),
            advancedDataSearch : false,
            advancedMetadataSearch : false,
            searchTerm : "",
            searchMetadataTerm : "",
            hideScroll : true,
            currentTab : $routeParams.tabindex || 'tabdata',
            alphaPattern : /^([0-9A-Za-z\u00C0-\u017F\*\?]+|\s)*$/,
            published : null,
            observation : null,
            smallMode : false,
            selectedDataSetChild : null
        };
        $scope.search = {};
        $scope.searchMD = {};

        /**
         * Array of available ways to import data in Constellation.
         * This array can be overrided by sub project to add its own data format.
         */
        $scope.dataCtrl.availableWays = [
            {   name:'localFile',
                idHTML:'uploadchoice',
                translateKey:'label.file.local',
                defaultTranslateValue:'Local file',
                bindFunction:function(){$scope.showLocalFilePopup();}
            },
            {   name:'serverFile',
                idHTML:'filesystemchoice',
                translateKey:'label.file.server',
                defaultTranslateValue:'Server file',
                bindFunction:function(){$scope.showServerFilePopup();}
            },
            {   name:'database',
                idHTML:'dbchoice',
                translateKey:'label.file.db',
                defaultTranslateValue:'Database',
                bindFunction:function(){$scope.showDatabasePopup();}
            }
        ];

        /**
         * Get cstl config to check for methods to import data.
         */
        $http.get("app/conf").success(function(data){
            if(data['cstl.import.empty']) {
                $scope.dataCtrl.availableWays.push(
                    {   name:'emptyDataset',
                        idHTML:'emptychoice',
                        translateKey:'label.file.empty',
                        defaultTranslateValue:'Empty dataset',
                        bindFunction:function(){$scope.showEmptyDataSetPopup();}
                    }
                );
            }
            if(data['cstl.import.custom']) {
                $scope.dataCtrl.availableWays.push(
                    {   name:'customDataset',
                        idHTML:'customchoice',
                        translateKey:'label.file.custom',
                        defaultTranslateValue:'Other',
                        bindFunction:function(){$scope.showCustomDataSetPopup();}
                    }
                );
            }
        });

        /**
         * Select appropriate tab 'tabdata' or 'tabmetadata'.
         * @param item
         */
        $scope.selectTab = function(item) {
            $scope.dataCtrl.currentTab = item;
        };

        /**
         * Toggle selection of dataSet child item.
         * @param item
         */
        $scope.selectDataSetChild = function(item,parent) {
            if(parent && $scope.selectedDS && parent.id !== $scope.selectedDS.id){
                $scope.selectedDS = null;
            }
            if ($scope.dataCtrl.selectedDataSetChild && item && $scope.dataCtrl.selectedDataSetChild.Id === item.Id) {
                $scope.dataCtrl.selectedDataSetChild = null;
            } else {
                $scope.dataCtrl.selectedDataSetChild = item;
            }
            $scope.showDataDashboardMap();
        };

        $scope.toggleDataSetFirstChild = function(parent) {
            if(parent && $scope.selectedDS){
                var item = null;
                if(parent.Children.length===1){
                    item = parent.Children[0];
                }
                if(item){
                    $scope.dataCtrl.selectedDataSetChild = item;
                }else {
                    $scope.dataCtrl.selectedDataSetChild = null;
                }
            }else {
                $scope.dataCtrl.selectedDataSetChild = null;
            }
            $scope.showDataDashboardMap();
        };

        /**
         * Toggle advanced data search view panel.
         */
        $scope.toggleAdvancedDataSearch = function(){
            if ($scope.dataCtrl.advancedDataSearch){
                $scope.dataCtrl.advancedDataSearch = false;
            }  else {
                $scope.dataCtrl.advancedDataSearch = true;
                $scope.dataCtrl.searchTerm ="";
            }
        };

        /**
         * Toggle advanced metadata search view panel.
         */
        $scope.toggleAdvancedMDSearch = function(){
            if ($scope.dataCtrl.advancedMetadataSearch){
                $scope.dataCtrl.advancedMetadataSearch = false;
            }  else {
                $scope.dataCtrl.advancedMetadataSearch = true;
                $scope.dataCtrl.searchMetadataTerm ="";
            }
        };

        /**
         * Clean advanced search inputs.
         */
        $scope.resetSearch = function(){
            $scope.search = {};
        };

        /**
         * Clean advanced metadata search inputs.
         */
        $scope.resetSearchMD = function(){
            $scope.searchMD = {};
        };

        /**
         * Check the validity against the pattern and display growl error for given validity.
         * @param isInvalid
         */
        $scope.checkIsValid = function(isInvalid){
            if (isInvalid){
                Growl('error','Error','Invalid Chars');
            }
        };

        /**
         * Binding action for search button in data dashboard.
         * the result is stored with Dashboard service.
         */
        $scope.callSearch = function(){
            $scope.wrap.filtertext='';
            if ($scope.dataCtrl.searchTerm){
                datasetListing.findDataset({values: {'search': $scope.dataCtrl.searchTerm+'*'}},
                    function(response) {
                        Dashboard($scope, response, true);
                    },
                    function(response){
                        console.error(response);
                        Growl('error','Error','Search failed:'+ response.data);
                    }
                );
            }else{
                if (!$.isEmptyObject($scope.search)){
                    var searchString = "";
                    if ($scope.search.title){
                        searchString += " title:"+$scope.search.title+'*';
                    }
                    if ($scope.search.abstract){
                        searchString += " abstract:"+$scope.search.abstract+'*';
                    }
                    if ($scope.search.keywords){
                        searchString += " keywords:"+$scope.search.keywords+'*';
                    }
                    if ($scope.search.topic){
                        searchString += " topic:"+$scope.search.topic+'*';
                    }
                    if ($scope.search.data){
                        searchString += " data:"+$scope.search.data+'*';
                    }
                    if ($scope.search.level){
                        searchString += " level:"+$scope.search.level+'*';
                    }
                    if ($scope.search.area){
                        searchString += " area:"+$scope.search.area+'*';
                    }
                    datasetListing.findDataset({values: {'search': searchString}},function(response) {
                        Dashboard($scope, response, true);
                    }, function(response){
                        console.error(response);
                        Growl('error','Error','Search failed:'+ response.data);
                    });
                } else {
                    datasetListing.listAll({}, function(response) {
                        Dashboard($scope, response, true);
                    });
                }
            }
        };

        /**
         * Binding action for search button in metadata dashboard.
         * the result is stored with Dashboard service.
         */
        $scope.callSearchMDForTerm = function(term){
            $scope.wrap.filtertext='';
            if (term){
                datasetListing.findDataset({values: {'search': term+'*'}},
                    function(response) {//success
                        Dashboard($scope, response, true);
                    },
                    function(response){//error
                        console.error(response);
                        Growl('error','Error','Search failed:'+ response.data);
                    });
            }else{
                if (!$.isEmptyObject($scope.searchMD)){
                    var searchString = "";
                    if ($scope.searchMD.title){
                        searchString += " title:"+$scope.searchMD.title+'*';
                    }
                    if ($scope.searchMD.abstract){
                        searchString += " abstract:"+$scope.searchMD.abstract+'*';
                    }
                    if ($scope.searchMD.keywords){
                        searchString += " keywords:"+$scope.searchMD.keywords+'*';
                    }
                    if ($scope.searchMD.topic){
                        searchString += " topic:"+$scope.searchMD.topic+'*';
                    }
                    if ($scope.searchMD.data){
                        searchString += " data:"+$scope.searchMD.data+'*';
                    }
                    if ($scope.searchMD.level){
                        searchString += " level:"+$scope.searchMD.level+'*';
                    }
                    if ($scope.searchMD.area){
                        searchString += " area:"+$scope.searchMD.area+'*';
                    }
                    datasetListing.findDataset({values: {'search': searchString}},
                        function(response) {//success
                            Dashboard($scope, response, true);
                        }, function(response){ //error
                            console.error(response);
                            Growl('error','Error','Search failed:'+ response.data);
                        });
                } else {
                    datasetListing.listAll({}, function(response) {
                        Dashboard($scope, response, true);
                    });
                }
            }
        };
        $scope.callSearchMD = function(){
            $scope.callSearchMDForTerm($scope.dataCtrl.searchMetadataTerm);
        };

        /**
         * main function of dashboard that loads the list of objects from server.
         */
        $scope.init = function() {
            $scope.wrap.fullList = [];
            $scope.wrap.filtertext='';
            $scope.wrap.filtertype = undefined;
            if($scope.dataCtrl.currentTab === 'tabdata'){
                $scope.dataCtrl.searchTerm="";
                datasetListing.listAll({}, function(response) {//success
                    Dashboard($scope, response, true);
                    $scope.wrap.ordertype = "Name";
                }, function() {//error
                    Growl('error','Error','Unable to load list of data!');
                });
            }else if($scope.dataCtrl.currentTab === 'tabmetadata') {
                $scope.dataCtrl.searchMetadataTerm="";
                datasetListing.listAll({}, function(response){//success
                    Dashboard($scope, response, true);
                    $scope.wrap.ordertype = "Name";
                    if($scope.selectedDS || $scope.dataCtrl.selectedDataSetChild){
                        //then we need to highlight the metadata associated in MD dashboard
                        var term = $scope.selectedDS?$scope.selectedDS.Name:$scope.dataCtrl.selectedDataSetChild.Name;
                        $scope.wrap.filtertext = term;
                    }else {
                        //otherwise reset selection
                        $scope.dataCtrl.selectedDataSetChild = null;
                        $scope.selectedDS = null;
                    }
                }, function(response){//error
                    Growl('error','Error','Unable to load list of dataset!');
                });
            }
            //display dashboard map
            setTimeout(function(){
                $scope.showDataDashboardMap();
            },300);
            //display button that allow to scroll to top of the page from a certain height.
            angular.element($window).bind("scroll", function() {
                $scope.dataCtrl.hideScroll = this.pageYOffset < 220;
                $scope.$apply();
            });
        };

        /**
         * Called only when import wizard finished.
         */
        $scope.initAfterImport = function() {
            $scope.wrap.fullList = [];
            $scope.wrap.filtertext='';
            $scope.wrap.filtertype = undefined;
            $scope.dataCtrl.searchTerm="";
            $scope.dataCtrl.searchMetadataTerm="";
            $scope.dataCtrl.selectedDataSetChild = null;
            $scope.selectedDS = null;
            datasetListing.listAll({}, function(response) {//success
                Dashboard($scope, response, true);
                $scope.wrap.ordertype = "Date";
                $scope.wrap.orderreverse=true;
            }, function() {//error
                Growl('error','Error','Unable to load list of dataset!');
            });
            $scope.showDataDashboardMap();
        };

        /**
         * Reset filters for dashboard
         */
        $scope.resetFilters = function(){
            $scope.wrap.filtertext='';
            $scope.wrap.filtertype = undefined;
            if($scope.dataCtrl.currentTab === 'tabdata'){
                $scope.dataCtrl.searchTerm="";
                $scope.dataCtrl.selectedDataSetChild = null;
                $scope.selectedDS = null;
                datasetListing.listAll({}, function(response) {//success
                    Dashboard($scope, response, true);
                    $scope.wrap.ordertype = "Name";
                }, function() {//error
                    Growl('error','Error','Unable to load list of data!');
                });
            }else if($scope.dataCtrl.currentTab === 'tabmetadata') {
                $scope.dataCtrl.searchMetadataTerm="";
                $scope.dataCtrl.selectedDataSetChild = null;
                $scope.selectedDS = null;
                datasetListing.listAll({}, function(response){//success
                    Dashboard($scope, response, true);
                    $scope.wrap.ordertype = "Name";
                }, function(response){//error
                    Growl('error','Error','Unable to load list of dataset!');
                });
            }
            $scope.showDataDashboardMap();
        };

        /**
         * Apply filter to show only published data in service depending on given flag.
         * ie: data is linked to services.
         * @param published if true then proceed to show only published data.
         */
        $scope.showPublished = function(published){
            $scope.dataCtrl.published=published;
            dataListing.listPublishedDS({published:published}, function(response) {//success
                Dashboard($scope, response, true);
            }, function() { //error
                Growl('error','Error','Unable to show published data!');
            });
        };

        /**
         * Apply filter to show only sensorable data depending on given flag.
         * ie: data is linked to sensors
         * @param observation if true then proceed to show only sensorable data.
         */
        $scope.showSensorable = function(observation){
            $scope.dataCtrl.observation=observation;
            dataListing.listSensorableDS({observation:observation},
                function(response) {//success
                    Dashboard($scope, response, true);
                }, function() {//error
                    Growl('error','Error','Unable to show sensorable data!');
                });
        };

        $scope.showDataDashboardMap = function() {
            if (DataDashboardViewer.map) {
                DataDashboardViewer.map.setTarget(undefined);
            }
            DataDashboardViewer.initConfig();
            DataDashboardViewer.fullScreenControl = true;
            var mapId = $scope.dataCtrl.currentTab === 'tabdata' ? 'dataPreviewMap' : 'metadataPreviewMap';
            var selectedData = $scope.dataCtrl.selectedDataSetChild;
            if(selectedData) {
                var layerName;
                if (selectedData.Namespace) {
                    layerName = '{' + selectedData.Namespace + '}' + selectedData.Name;
                } else {
                    layerName = selectedData.Name;
                }
                var providerId = selectedData.Provider;
                if($scope.dataCtrl.currentTab === 'tabdata') {
                    var layerData;
                    var pyramidProviderId = selectedData.PyramidConformProviderId;
                    var type = selectedData.Type.toLowerCase();
                    if (selectedData.TargetStyle && selectedData.TargetStyle.length > 0) {
                        layerData = DataDashboardViewer.createLayerWithStyle($scope.dataCtrl.cstlUrl,layerName,
                            pyramidProviderId?pyramidProviderId:providerId,
                            selectedData.TargetStyle[0].Name,
                            null,null,type!=='vector');
                    } else {
                        layerData = DataDashboardViewer.createLayer($scope.dataCtrl.cstlUrl, layerName,
                            pyramidProviderId?pyramidProviderId:providerId,null,type!=='vector');
                    }
                    //to force the browser cache reloading styled layer.
                    layerData.get('params').ts=new Date().getTime();
                    DataDashboardViewer.layers = [layerData];
                }

                provider.dataGeoExtent({},{values: {'providerId':providerId,'dataId':layerName}},
                    function(response) {//success
                        var bbox = response.boundingBox;

                        var overlay= null;
                        if (bbox) {
                            DataDashboardViewer.extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
                            if($scope.dataCtrl.currentTab === 'tabmetadata') {
                                var minX = bbox[0];
                                var minY = bbox[1];
                                var maxX = bbox[2];
                                var maxY = bbox[3];
                                //For pseudo Mercator we need to check against the validity,
                                // the bbox crs is always defined as EPSG:4326
                                //if the viewer use pseudo Mercator then fix Latitude to avoid Infinity values
                                if(DataDashboardViewer.projection === 'EPSG:3857'){
                                    if(minY < -85){minY=-85;}
                                    if(maxY > 85){maxY=85;}
                                }
                                var coordinates = [[[minX, minY], [minX, maxY], [maxX, maxY], [maxX, minY], [minX, minY]]];
                                var polygon = new ol.geom.Polygon(coordinates);
                                polygon = polygon.transform('EPSG:4326',DataDashboardViewer.projection);
                                var extentFeature = new ol.Feature(polygon);
                                var stroke = new ol.style.Stroke({
                                    color: '#66AADD',
                                    width: 2.25
                                });
                                var styles = [
                                    new ol.style.Style({
                                        stroke: stroke
                                    })
                                ];
                                overlay = new ol.FeatureOverlay({
                                    features : [extentFeature],
                                    style : styles
                                });
                            }
                        }
                        DataDashboardViewer.initMap(mapId);
                        if(overlay) {
                            DataDashboardViewer.map.addOverlay(overlay);
                        }
                    }, function() {//error
                        // failed to find a metadata, just load the full map
                        DataDashboardViewer.initMap(mapId);
                    }
                );
            }else {
                DataDashboardViewer.initMap(mapId);
            }
        };

        /**
         * Returns if the data is a pyramid (tiled data)
         * this function is overrided by sub-projects.
         * @param data
         * @returns {boolean}
         */
        $scope.checkIsPyramid = function(data){
            return (data && data.PyramidConformProviderId);
        };

        $scope.deleteData = function() {
            var dlg = $modal.open({
                templateUrl: 'views/modal-confirm.html',
                controller: 'ModalConfirmController',
                resolve: {
                    'keyMsg':function(){return "dialog.message.confirm.delete.data";}
                }
            });
            dlg.result.then(function(cfrm){
                if(cfrm){
                    var layerName = $scope.dataCtrl.selectedDataSetChild.Name;
                    var dataId = $scope.dataCtrl.selectedDataSetChild.Id;
                    dataListing.removeData({'dataId':dataId},{},
                        function() {// on success
                            Growl('success','Success','Data '+ layerName +' successfully deleted');
                            datasetListing.listAll({}, function(response) {
                                Dashboard($scope, response, true);
                                $scope.dataCtrl.selectedDataSetChild=null;
                                $scope.showDataDashboardMap();
                            });
                        },
                        function() {//on error
                            Growl('error','Error','Data '+ layerName +' deletion failed');
                        }
                    );
                }
            });
        };

        /**
         * Delete selected dataset.
         */
        $scope.deleteDataset = function() {
            var dlg = $modal.open({
                templateUrl: 'views/modal-confirm.html',
                controller: 'ModalConfirmController',
                resolve: {
                    'keyMsg':function(){return "dialog.message.confirm.delete.dataset";}
                }
            });
            dlg.result.then(function(cfrm){
                if(cfrm){
                    datasetListing.deleteDataset({"datasetIdentifier":$scope.selectedDS.Name},function(response){//success
                        Growl('success','Success','Data set '+ $scope.selectedDS.Name +' successfully deleted');
                        datasetListing.listAll({}, function(response) {
                            Dashboard($scope, response, true);
                            $scope.dataCtrl.selectedDataSetChild=null;
                            $scope.selectedDS = null;
                            $scope.showDataDashboardMap();
                        });
                    },function(response){//error
                        Growl('error','Error','Dataset '+ $scope.selectedDS.Name +' deletion failed');
                    });
                }
            });
        };

        /**
         * Open metadata viewer popup and display metadata
         * in appropriate template depending on data type property.
         * this function is called from data dashboard.
         */
        $scope.displayMetadataFromDD = function() {
            var type = 'import';
            if($scope.dataCtrl.selectedDataSetChild){
                type = $scope.dataCtrl.selectedDataSetChild.Type.toLowerCase();
            }else if($scope.selectedDS && $scope.selectedDS.Type){
                type = $scope.selectedDS.Type.toLowerCase();
            }
            if(type.toLowerCase() === 'coverage'){
                type = 'raster';
            }
            $modal.open({
                templateUrl: 'views/data/modalViewMetadata.html',
                controller: 'ViewMetadataModalController',
                resolve: {
                    'dashboardName':function(){return 'data';},
                    'metadataValues':function(textService){
                        if($scope.dataCtrl.selectedDataSetChild){
                            return textService.metadataJson($scope.dataCtrl.selectedDataSetChild.Provider,
                                                            $scope.dataCtrl.selectedDataSetChild.Name,
                                                            type,true);
                        }else if($scope.selectedDS){
                            return textService.metadataJsonDS($scope.selectedDS.Name,type,true);
                        }
                    }
                }
            });
        };

        /**
         * Open metadata viewer popup and display metadata
         * in appropriate template depending on data type property.
         * this function is called from metadata dashboard.
         */
        $scope.displayMetadataFromMD = function() {
            var type = 'import';
            if($scope.dataCtrl.selectedDataSetChild){
                type = $scope.dataCtrl.selectedDataSetChild.Type.toLowerCase();
            }else if($scope.selectedDS && $scope.selectedDS.Type){
                type = $scope.selectedDS.Type.toLowerCase();
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
                        if($scope.dataCtrl.selectedDataSetChild){
                            return textService.metadataJson($scope.dataCtrl.selectedDataSetChild.Provider,
                                $scope.dataCtrl.selectedDataSetChild.Name,
                                type,true);
                        }else if($scope.selectedDS){
                            return textService.metadataJsonDS($scope.selectedDS.Name,type,true);
                        }
                    }
                }
            });
        };

        /**
         * Open metadata editor in modal popup.
         */
        $scope.displayMetadataEditor = function() {
            var type = 'import';
            if($scope.dataCtrl.selectedDataSetChild){
                type = $scope.dataCtrl.selectedDataSetChild.Type.toLowerCase();
            }else if($scope.selectedDS && $scope.selectedDS.Type){
                type = $scope.selectedDS.Type.toLowerCase();
            }
            if(type.toLowerCase() === 'coverage'){
                type = 'raster';
            }
            var template = type;
            if($scope.dataCtrl.selectedDataSetChild){
                openModalEditor($scope.dataCtrl.selectedDataSetChild.Provider,
                                $scope.dataCtrl.selectedDataSetChild.Name,
                                type,template,'csw');
            }else if($scope.selectedDS){
                openModalEditor(null,$scope.selectedDS.Name,type,template,'csw');
            }
        };

        /**
         * Open modal for metadata editor
         * for given provider id, data type and template.
         * @param provider
         * @param identifier
         * @param type
         * @param template
         * @param theme
         */
        function openModalEditor(provider,identifier,type,template,theme){
            var modalEditor = $modal.open({
                templateUrl: 'views/data/modalEditMetadata.html',
                controller: 'EditMetadataModalController',
                resolve: {
                    'provider':function(){return provider;},
                    'identifier':function(){return identifier;},
                    'type':function(){return type;},
                    'template':function(){return template;},
                    'theme':function(){return theme;}
                }
            });
            modalEditor.result.then(function(){
                datasetListing.listAll({}, function(response) {
                    Dashboard($scope, response, true);
                });
            });
        }

        /**
         * Open metadata page for dataset metadata.
         * use $scope.displayMetadataEditor for Constellation SDI.
         * this function will be used in Constellation Enterprise.
         */
        $scope.editMetadata = function() {
            var type = 'import';
            if($scope.selectedDS && $scope.selectedDS.Children && $scope.selectedDS.Children.length >0){
                type = $scope.selectedDS.Children[0].Type.toLowerCase();
            }
            var template = type;
            $location.path('/editmetadata/'+template+'/'+type+'/'+$scope.selectedDS.Name);
        };

        // Style methods
        $scope.showStyleList = function() {
            StyleSharedService.showStyleList($scope, $scope.dataCtrl.selectedDataSetChild);
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style,$scope.dataCtrl.selectedDataSetChild);
        };

        $scope.editLinkedStyle = function(styleProvider, styleName, selectedData) {
            style.get({provider: styleProvider, name: styleName}, function(response) {
                StyleSharedService.editLinkedStyle($scope, response,selectedData);
            });
        };

        $scope.showSensorsList = function() {
            $modal.open({
                templateUrl: 'views/sensor/modalSensorChoose.html',
                controller: 'SensorModalChooseController',
                resolve: {
                    'selectedData': function() { return $scope.dataCtrl.selectedDataSetChild; }
                }
            });
        };

        $scope.unlinkSensor = function(sensorId) {
            dataListing.unlinkSensor({providerId: $scope.dataCtrl.selectedDataSetChild.Provider,
                    dataId: $scope.dataCtrl.selectedDataSetChild.Name,
                    sensorId: sensorId},
                {value: $scope.dataCtrl.selectedDataSetChild.Namespace},
                function(response) {//success
                    $scope.dataCtrl.selectedDataSetChild.TargetSensor.splice(0, 1);
                });
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#dataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };
        $scope.toggleUpDownSelectedMD = function() {
            var $header = $('#metadataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };

        // Data loading
        $scope.showLocalFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1DataLocal'; },
                    'importType': function() { return 'local'; }
                }
            });
            modal.result.then(function(result) {
                if(!result){
                    $scope.initAfterImport();
                    return;
                }
                if(!result.file){
                    $scope.initAfterImport();
                    return;
                }else {
                    dataListing.initMetadata({}, {values: {"providerId": result.file,
                                                          "dataType": result.type,
                                                          "mergeWithUploadedMD":result.completeMetadata}},
                       function () {//success
                        $scope.initAfterImport();
                        openModalEditor(null,result.file,result.type,"import",'data');
                    }, function () {//error
                        Growl('error', 'Error', 'Unable to prepare metadata for next step!');
                        $scope.initAfterImport();
                    });
                }
            });
        };

        $scope.showServerFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1DataServer'; },
                    'importType': function() { return 'server'; }
                }
            });
            modal.result.then(function(result) {
                if(!result){
                    return;
                }
                if(!result.file){
                    return;
                }else {
                    dataListing.initMetadata({}, {values: {"providerId": result.file,
                                                          "dataType": result.type,
                                                          "mergeWithUploadedMD":result.completeMetadata}},
                       function () {//success
                        $scope.initAfterImport();
                        openModalEditor(null,result.file,result.type,"import",'data');
                    }, function () {//error
                        Growl('error', 'Error', 'Unable to save metadata');
                    });
                }
            });
        };

        $scope.showDatabasePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1Database'; },
                    'importType': function() { return 'database'; }
                }
            });
            modal.result.then(function(result) {
                if(!result){
                    return;
                }
                if(!result.file){
                    return;
                }else {
                    dataListing.initMetadata({}, {values: {"providerId": result.file,
                                                          "dataType": result.type,
                                                          "mergeWithUploadedMD":result.completeMetadata}},
                       function () {//success
                        $scope.initAfterImport();
                        openModalEditor(null,result.file,result.type,"import",'data');
                    }, function () {//error
                        Growl('error', 'Error', 'Unable to save metadata');
                    });
                }
            });
        };

        $scope.showEmptyDataSetPopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step2Metadata'; },
                    'importType': function() { return 'empty'; }
                }
            });
            modal.result.then(function(result) {
                if(!result){
                    return;
                }
                if(!result.file){
                    return;
                }else {
                    dataListing.initMetadata({}, {values: {"providerId": result.file,
                                                          "dataType": result.type}},
                        function () {//success
                            $scope.initAfterImport();
                            openModalEditor(null,result.file,result.type,"import",'data');
                        }, function () {//error
                            Growl('error', 'Error', 'Unable to save metadata');
                        });
                }
            });
        };

        $scope.showCustomDataSetPopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1Custom'; },
                    'importType': function() { return 'custom'; }
                }
            });
            modal.result.then(function() {
                $scope.initAfterImport();
            });
        };

        $scope.showDomains = function(){
            var modal = $modal.open({
                templateUrl: 'views/data/linkedDomains.html',
                controller: 'ModalDataLinkedDomainsController',
                resolve: {
                    'domains': function() {return dataListing.domains({dataId: $scope.dataCtrl.selectedDataSetChild.Id}).$promise;},
                    'dataId': function(){return $scope.dataCtrl.selectedDataSetChild.Id;}
                }
            });
        };

        $scope.truncate = function(small, text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (small && text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else if (!small && text.length > 65) {
                        return text.substr(0, 65) + "...";
                    } else { return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small && text.length > 12) {
                        return text.substr(0, 12) + "...";
                    } else if (!small && text.length > 50) {
                        return text.substr(0, 50) + "...";
                    } else { return text ;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else { return text;}
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else { return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else { return text; }
                }
            }
        };

    })

    .controller('DataModalController', function($scope, dataListing, webService, sos, sensor, Dashboard, $modalInstance,
                                                 service, exclude, Growl,provider,$cookieStore,$filter) {
        /**
         * To fix angular bug caused by nested scope issue in modal.
         */
        $scope.wrap = {};

        $scope.wrap.nbbypage = 5;

        $scope.dataSelect={all:false};

        $scope.service = service;

        $scope.exclude = exclude;

        $scope.values = {
            listSelect : [],
            selectedSensor : null,
            selectedSensorsChild : null
        };

        $scope.dismiss = function() {
            $modalInstance.dismiss('close');
        };

        $scope.close = function() {
            $modalInstance.close();
        };

        $scope.clickFilter = function(ordType){
            $scope.wrap.ordertype = ordType;
            $scope.wrap.orderreverse = !$scope.wrap.orderreverse;
        };

        $scope.getDefaultFilter = function() {
            if ($scope.service.type.toLowerCase() === 'wcs') {
                return 'coverage';
            }
            if ($scope.service.type.toLowerCase() === 'wfs') {
                return 'vector';
            }
            return undefined;
        };

        $scope.initData = function() {
            if ($scope.service.type.toLowerCase() === 'sos') {
                sensor.list({}, function(response) {
                    Dashboard($scope, response.children, false);
                });
            } else {
                dataListing.listAll({}, function (response) {
                    Dashboard($scope, response, true);
                    $scope.wrap.filtertype = $scope.getDefaultFilter();
                });
                setTimeout(function(){
                    $scope.previewData();
                },300);
            }
        };

        $scope.previewData = function() {
            //clear the map
            if (DataViewer.map) {
                DataViewer.map.setTarget(undefined);
            }
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
                        layerData = DataViewer.createLayerWithStyle($cookieStore.get('cstlUrl'),layerName,providerId,
                            dataItem.TargetStyle[0].Name,null,null,type!=='vector');
                    } else {
                        layerData = DataViewer.createLayer($cookieStore.get('cstlUrl'), layerName, providerId,null,type!=='vector');
                    }
                    //to force the browser cache reloading styled layer.
                    layerData.get('params').ts=new Date().getTime();
                    DataViewer.layers.push(layerData);
                }
                provider.mergedDataExtent({},$scope.values.listSelect,
                    function(response) {// on success
                        DataViewer.initMap('dataChooseMapPreview');
                        if(response && response.boundingBox) {
                            var bbox = response.boundingBox;
                            var extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
                            DataViewer.zoomToExtent(extent,DataViewer.map.getSize(),false);
                        }
                    }, function() {//on error
                        // failed to calculate an extent, just load the full map
                        DataViewer.initMap('dataChooseMapPreview');
                    }
                );
            }else {
                DataViewer.initMap('dataChooseMapPreview');
                DataViewer.map.getView().setZoom(DataViewer.map.getView().getZoom()+1);
            }
        };

        $scope.toggleSelectSensor = function(item) {
            if (item && $scope.values.selectedSensor && $scope.values.selectedSensor.id === item.id) {
                $scope.values.selectedSensor = null;
            } else {
                $scope.values.selectedSensor = item;
            }
        };

        $scope.selectSensorsChild = function(item) {
            if (item && $scope.values.selectedSensorsChild && $scope.values.selectedSensorsChild.id === item.id) {
                $scope.values.selectedSensorsChild = null;
            } else {
                $scope.values.selectedSensorsChild = item;
            }
        };

        /**
         * Proceed to select all items of modal dashboard
         * depending on the property of checkbox selectAll.
         */
        $scope.selectAllData = function() {
            var array = $filter('filter')($scope.wrap.fullList, {'Type':$scope.wrap.filtertype, '$': $scope.wrap.filtertext},$scope.wrap.matchExactly);
            $scope.values.listSelect = ($scope.dataSelect.all) ? array.slice(0) : [];
            if ($scope.service.type.toLowerCase() !== 'sos') {
                $scope.previewData();
            }
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
            if ($scope.service.type.toLowerCase() !== 'sos') {
                $scope.previewData();
            }

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
         * function to add data to service
         */
        $scope.choose = function() {
            if ($scope.service.type.toLowerCase() === 'sos') {
                if (!$scope.values.selectedSensor) {
                    Growl('warning', 'Warning', 'No data selected');
                    return;
                }
                var sensorId = ($scope.values.selectedSensorsChild) ? $scope.values.selectedSensorsChild.id : $scope.values.selectedSensor.id;
                sos.importSensor({id: $scope.service.identifier}, {values: {"sensorId": sensorId}},
                    function () {//success
                        Growl('success', 'Success', 'Sensor ' + sensorId + ' imported in service ' + $scope.service.name+' successfully!');
                        $scope.close();
                    }, function () {
                        Growl('error', 'Error', 'Unable to import sensor ' + sensorId + ' in service ' + $scope.service.name);
                        $scope.dismiss();
                    }
                );
            }else {
                if ($scope.values.listSelect.length === 0) {
                    Growl('warning', 'Warning', 'No data selected!');
                    return;
                }
                //using angular.forEach to avoid jsHint warning when declaring function in loop
                angular.forEach($scope.values.listSelect, function(value, key){
                    var providerId = value.Provider;
                    if($scope.service.type.toLowerCase() === 'wms'){
                        providerId = value.PyramidConformProviderId?value.PyramidConformProviderId:value.Provider;
                    }
                    webService.addLayer({type: $scope.service.type, id: $scope.service.identifier},
                                        {layerAlias: value.Name,
                                         layerId: value.Name,
                                         serviceType: $scope.service.type,
                                         serviceId: $scope.service.identifier,
                                         providerId: providerId,
                                         layerNamespace: value.Namespace},
                        function(response) {//on success
                            Growl('success', 'Success', response.message);
                            $scope.close();
                        },
                        function(response) {//on error
                            Growl('error', 'Error', response.message);
                            $scope.dismiss();
                        }
                    );
                });
            }
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

        $scope.initData();
    })

    .controller('ModalDataLinkedDomainsController', function($scope, $modalInstance, Growl, dataListing, domains, dataId) {
        $scope.domains = domains;
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.toggleDomain = function(i){
            var pathParams = {domainId: $scope.domains[i].id, dataId:dataId};
            if($scope.domains[i].linked){
                dataListing.unlinkFromDomain(pathParams, function(){
                    $scope.domains[i].linked = !$scope.domains[i].linked;
                    $scope.domains[i].linked = false;
                }, function(response){
                    Growl('error','error', response.data.message );
                    dataListing.domains({dataId:dataId}, function(domains){
                        $scope.domains = domains;
                    });
                });
            }else{
                dataListing.linkToDomain(pathParams, {}, function(){
                    $scope.domains[i].linked = true;
                }, function(){

                });
            }
        };

    });

