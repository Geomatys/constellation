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

angular.module('cstl-data-dashboard', ['ngCookies', 'cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('DataController', function($scope, $location, Dashboard, webService, dataListing, datasetListing, DomainResource,
                                            provider, $window, style, textService, $modal, Growl, StyleSharedService, $cookies, cfpLoadingBar) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.dataCtrl = {
            cstlUrl : $cookies.cstlUrl,
            cstlSessionId : $cookies.cstlSessionId,
            domainId : $cookies.cstlActiveDomainId,
            advancedDataSearch : false,
            advancedMetadataSearch : false,
            searchTerm : "",
            searchMetadataTerm : "",
            hideScroll : true,
            currentTab : 'tabdata',
            alphaPattern : /^([0-9A-Za-z\u00C0-\u017F\*\?]+|\s)*$/,
            published : null,
            observation : null,
            smallMode : false,
            selectedDataSetChild : null
        };
        $scope.search = {};
        $scope.searchMD = {};

        /**
         * Select appropriate tab 'tabdata' or 'tabmetadata'.
         * @param item
         */
        $scope.selectTab = function(item) {
            $scope.dataCtrl.currentTab = item;
        };

        /**
         * Select dataSet child item.
         * @param item
         */
        $scope.selectDataSetChild = function(item) {
            if ($scope.dataCtrl.selectedDataSetChild && item && $scope.dataCtrl.selectedDataSetChild.Id === item.Id) {
                $scope.dataCtrl.selectedDataSetChild = null;
            } else {
                $scope.dataCtrl.selectedDataSetChild = item;
            }
        };

        /**
         * Override the select function to togle item selection from data dashboard.
         * @param item
         */
        $scope.select = function(item) {
            if (item && $scope.selected &&$scope.selected.Id === item.Id) {
                $scope.selected = null;
            } else {
                $scope.selected = item;
            }
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
            if ($scope.dataCtrl.searchTerm){
                dataListing.findData({values: {'search': $scope.dataCtrl.searchTerm}},
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
                        searchString += " title:"+$scope.search.title;
                    }
                    if ($scope.search.abstract){
                        searchString += " abstract:"+$scope.search.abstract;
                    }
                    if ($scope.search.keywords){
                        searchString += " keywords:"+$scope.search.keywords;
                    }
                    if ($scope.search.topic){
                        searchString += " topic:"+$scope.search.topic;
                    }
                    if ($scope.search.data){
                        searchString += " data:"+$scope.search.data;
                    }
                    if ($scope.search.level){
                        searchString += " level:"+$scope.search.level;
                    }
                    if ($scope.search.area){
                        searchString += " area:"+$scope.search.area;
                    }
                    dataListing.findData({values: {'search': searchString}},function(response) {
                        Dashboard($scope, response, true);
                    }, function(response){
                        console.error(response);
                        Growl('error','Error','Search failed:'+ response.data);
                    });
                } else {
                    dataListing.listAll({}, function(response) {
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
            if (term){
                datasetListing.findDataset({values: {'search': term}},
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
                        searchString += " title:"+$scope.searchMD.title;
                    }
                    if ($scope.searchMD.abstract){
                        searchString += " abstract:"+$scope.searchMD.abstract;
                    }
                    if ($scope.searchMD.keywords){
                        searchString += " keywords:"+$scope.searchMD.keywords;
                    }
                    if ($scope.searchMD.topic){
                        searchString += " topic:"+$scope.searchMD.topic;
                    }
                    if ($scope.searchMD.data){
                        searchString += " data:"+$scope.searchMD.data;
                    }
                    if ($scope.searchMD.level){
                        searchString += " level:"+$scope.searchMD.level;
                    }
                    if ($scope.searchMD.area){
                        searchString += " area:"+$scope.searchMD.area;
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
            if($scope.dataCtrl.currentTab === 'tabdata'){
                dataListing.listAll({}, function(response) {//success
                    Dashboard($scope, response, true);
                    $scope.wrap.filtertype = "";
                    $scope.wrap.ordertype = "Name";
                    $scope.dataCtrl.searchTerm="";
                }, function() {//error
                    Growl('error','Error','Unable to load list of data!');
                });
            }else if($scope.dataCtrl.currentTab === 'tabmetadata') {
                datasetListing.listAll({}, function(response){//success
                    Dashboard($scope, response, true);
                    $scope.wrap.filtertype = "";
                    $scope.wrap.ordertype = "Name";

                    if($scope.selected){//data is selected in data Dashboard
                        //then we need to highlight the metadata associated in MD dashboard
                        $scope.dataCtrl.selectedDataSetChild = $scope.selected;
                        var term = "";
                        for(var i =0;i<response.length;i++){
                            if($scope.containsRefData(response[i])){
                                $scope.selectedDS = response[i];
                                term = response[i].Name;
                                break;
                            }
                        }
                        $scope.callSearchMDForTerm(term);
                    }else {
                        //otherwise reset selection
                        $scope.dataCtrl.selectedDataSetChild = null;
                        $scope.selectedDS = null;
                    }

                }, function(response){//error
                    Growl('error','Error','Unable to load list of dataset!');
                });
            }
            //display button that allow to scroll to top of the page from a certain height.
            angular.element($window).bind("scroll", function() {
                $scope.dataCtrl.hideScroll = this.pageYOffset < 220;
                $scope.$apply();
            });
        };

        /**
         * Reset filters for dashboard
         */
        $scope.resetFilters = function(){
            if($scope.dataCtrl.currentTab === 'tabdata'){
                dataListing.listAll({}, function(response) {//success
                    Dashboard($scope, response, true);
                    $scope.wrap.filtertype = "";
                    $scope.wrap.ordertype = "Name";
                    $scope.dataCtrl.searchTerm="";
                }, function() {//error
                    Growl('error','Error','Unable to load list of data!');
                });
            }else if($scope.dataCtrl.currentTab === 'tabmetadata') {
                datasetListing.listAll({}, function(response){//success
                    Dashboard($scope, response, true);
                    $scope.wrap.filtertype = "";
                    $scope.wrap.ordertype = "Name";
                    $scope.dataCtrl.selectedDataSetChild = null;
                    $scope.selectedDS = null;
                }, function(response){//error
                    Growl('error','Error','Unable to load list of dataset!');
                });
            }
        };

        /**
         * Apply filter to show only published data in service depending on given flag.
         * ie: data is linked to services.
         * @param published if true then proceed to show only published data.
         */
        $scope.showPublished = function(published){
            $scope.dataCtrl.published=published;
            dataListing.listPublished({published:published}, function(response) {//success
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
            dataListing.listSensorable({observation:observation},
                function(response) {//success
                    Dashboard($scope, response, true);
                }, function() {//error
                    Growl('error','Error','Unable to show sensorable data!');
                });
        };

        /**
         * Returns formatted name of data for given data's provider and data's name.
         * @param providerName given provider name.
         * @param dataName given data name.
         * @returns {*}
         */
        $scope.getDisplayName = function(providerName, dataName) {
            if (providerName === dataName){
                return dataName;
            } else {
                return dataName + ' ( ' + providerName + ' ) ';
            }
        };

        // Map methods
        $scope.showData = function() {
            $('#viewerData').modal("show");
            var layerName;
            if ($scope.selected.Namespace) {
                layerName = '{' + $scope.selected.Namespace + '}' + $scope.selected.Name;
            } else {
                layerName = $scope.selected.Name;
            }

            var providerId = $scope.selected.Provider;
            var layerData;
            if ($scope.selected.TargetStyle && $scope.selected.TargetStyle.length > 0) {
                layerData = DataViewer.createLayerWithStyle($scope.dataCtrl.cstlUrl,
                    layerName,
                    providerId,
                    $scope.selected.TargetStyle[0].Name);
            } else {
                layerData = DataViewer.createLayer($scope.dataCtrl.cstlUrl, layerName, providerId);
            }

            //to force the browser cache reloading styled layer.
            layerData.mergeNewParams({ts:new Date().getTime()});

            var layerBackground = DataViewer.createLayer($scope.dataCtrl.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");

            //attach event loader in modal map viewer
            layerData.events.register("loadstart", layerData, function() {
                $scope.$apply(function() {
                    window.cfpLoadingBar_parentSelector = '#dataMap';
                    cfpLoadingBar.start();
                    cfpLoadingBar.inc();
                });
            });
//            layerData.events.register("tileloaded", layerData, function() {
//                console.debug("Tile loaded. " + this.numLoadingTiles + " left.");
//            });
            layerData.events.register("loadend", layerData, function() {
                cfpLoadingBar.complete();
                window.cfpLoadingBar_parentSelector = null;
            });

            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataMap');
            provider.dataDesc({},{values: {'providerId':providerId,'dataId':layerName}},
                function(response) {//success
                    var bbox = response.boundingBox;
                    if (bbox) {
                        var extent = new OpenLayers.Bounds(bbox[0],bbox[1],bbox[2],bbox[3]);
                        DataViewer.map.zoomToExtent(extent, true);
                    }
                }, function() {//error
                    // failed to find a metadata, just load the full map
                    //do nothing.
                }
            );
        };

        $scope.deleteData = function() {
            if (confirm("Are you sure?")) {
                var layerName = $scope.selected.Name;
                var providerId = $scope.selected.Provider;

                // Remove layer on that data before
                if ($scope.selected.TargetService && $scope.selected.TargetService.length > 0) {
                    for (var i = 0; i < $scope.selected.TargetService.length; i++) {
                        var servId = $scope.selected.TargetService[i].name;
                        var servType = $scope.selected.TargetService[i].protocol[0];
                        webService.deleteLayer({type : servType, id: servId, layerid : layerName});
                    }
                }

                dataListing.hideData({providerid: providerId, dataid: layerName}, {value : $scope.selected.Namespace},
                    function() {//success
                        Growl('success','Success','Data '+ layerName +' successfully deleted');
                        dataListing.listDataForProv({providerId: providerId}, function(response) {
                            if (response.length === 0) {
                                provider.delete({id: providerId}, function() {
                                    dataListing.listAll({}, function(response) {
                                        Dashboard($scope, response, true);
                                        $scope.selected=null;
                                    });
                                });
                            } else {
                                dataListing.listAll({}, function(response) {
                                    Dashboard($scope, response, true);
                                    $scope.selected=null;
                                });
                            }
                        });
                    },
                    function() {//error
                        Growl('error','Error','Data '+ layerName +' deletion failed');
                    }
                );
            }
        };

        /**
         * Open metadata viewer popup and display metadata
         * in appropriate template depending on data type property.
         * this function is called from data dashboard.
         */
        $scope.displayMetadataFromDD = function() {
            var type = $scope.selected.Type.toLowerCase();
            if(type.toLowerCase() === 'coverage'){
                type = 'raster';
            }
            $modal.open({
                templateUrl: 'views/data/modalViewMetadata.html',
                controller: 'ViewMetadataModalController',
                resolve: {
                    'dashboardName':function(){return 'data';},
                    'metadataValues':function(textService){
                        return textService.metadataJson($scope.selected.Provider,
                            $scope.selected.Name, type, true);
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
            if($scope.selectedDS.Children && $scope.selectedDS.Children.length >0){
                type = $scope.selectedDS.Children[0].Type.toLowerCase();
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
                        return textService.metadataJson($scope.selectedDS.Name,
                            $scope.selectedDS.Name, type, true);
                    }
                }
            });
        };

        /**
         * Open metadata editor in modal popup.
         */
        $scope.displayMetadataEditor = function() {
            var type = 'import';
            if($scope.selectedDS.Children && $scope.selectedDS.Children.length >0){
                type = $scope.selectedDS.Children[0].Type.toLowerCase();
            }
            if(type.toLowerCase() === 'coverage'){
                type = 'raster';
            }
            var template = type;
            openModalEditor($scope.selectedDS.Name,type,template);
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

        /**
         * Open metadata page for dataset metadata.
         * use $scope.displayMetadataEditor for Constellation SDI.
         * this function will be used in Constellation Enterprise.
         */
        $scope.editMetadata = function() {
            var type = 'import';
            if($scope.selectedDS.Children && $scope.selectedDS.Children.length >0){
                type = $scope.selectedDS.Children[0].Type.toLowerCase();
            }
            var template = type;
            $location.path('/editmetadata/'+template+'/'+type+'/'+$scope.selectedDS.Name);
        };

        /**
         * Returns true if the given dataset have children which was selected in data dashboard.
         * @param dataset
         * @returns {boolean}
         */
        $scope.containsRefData = function(dataset) {
            if($scope.selected){
                var idToMatch = $scope.selected.Id;
                if(dataset && dataset.Children){
                    for(var i=0;i<dataset.Children.length;i++){
                        if(idToMatch === dataset.Children[i].Id){
                            return true;
                        }
                    }
                }
            }
            return false;
        };

        // Style methods
        $scope.showStyleList = function() {
            StyleSharedService.showStyleList($scope);
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style);
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
                    'selectedData': function() { return $scope.selected; }
                }
            });
        };

        $scope.unlinkSensor = function(sensorId) {
            dataListing.unlinkSensor({providerId: $scope.selected.Provider,
                    dataId: $scope.selected.Name,
                    sensorId: sensorId},
                {value: $scope.selected.Namespace},
                function(response) {//success
                    $scope.selected.TargetSensor.splice(0, 1);
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
                    'firstStep': function() { return 'step1DataLocal'; }
                }
            });
            modal.result.then(function(result) {
                if(!result.file){
                    return;
                }else {
                    dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function () {
                        $scope.init(); //needed after import
                        openModalEditor(result.file,result.type,"import");

                    }, function () {
                        Growl('error', 'Error', 'Unable to prepare metadata for next step!');
                    });
                }
            });
        };

        $scope.showServerFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1DataServer'; }
                }
            });
            modal.result.then(function(result) {
                if(!result.file){
                    return;
                }else {
                    dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function () {
                        $scope.init(); //needed after import
                        openModalEditor(result.file,result.type,"import");
                    }, function () {
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
                    'firstStep': function() { return 'step1Database'; }
                }
            });
            modal.result.then(function(result) {
                if(!result.file){
                    return;
                }else {
                    dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function () {
                        $scope.init(); //needed after import
                        openModalEditor(result.file,result.type,"import");
                    }, function () {
                        Growl('error', 'Error', 'Unable to save metadata');
                    });
                }
            });
        };

        $scope.showDomains = function(){
            var modal = $modal.open({
                templateUrl: 'views/data/linkedDomains.html',
                controller: 'ModalDataLinkedDomainsController',
                resolve: {
                    'domains': function() {return dataListing.domains({dataId: $scope.selected.Id}).$promise;},
                    'dataId': function(){return $scope.selected.Id;}
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
                                                 service, exclude, Growl, $modal) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.service = service;

        $scope.getDefaultFilter = function() {
            if (service.type.toLowerCase() === 'wcs') {
                return 'coverage';
            }
            if (service.type.toLowerCase() === 'wfs') {
                return 'vector';
            }
            return '';
        };
        $scope.wrap.nbbypage = 5;
        $scope.exclude = exclude;

        // WMTS params in the last form before closing the popup
        $scope.wmtsParams = false;
        $scope.tileFormat = undefined;
        $scope.crs = undefined;
        $scope.scales = [];
        $scope.upperCornerX = undefined;
        $scope.upperCornerY = undefined;
        $scope.conformPyramid = undefined;

        $scope.init = function() {
            if (service.type.toLowerCase() === 'sos') {
                sensor.list({}, function(response) {
                    Dashboard($scope, response.children, false);
                });
            } else {
                dataListing.listAll({}, function (response) {
                    Dashboard($scope, response, true);
                    $scope.wrap.filtertype = $scope.getDefaultFilter();
                });
            }
        };

        $scope.selectedSensorsChild = null;

        $scope.selectSensorsChild = function(item) {
            if ($scope.selectedSensorsChild === item) {
                $scope.selectedSensorsChild = null;
            } else {
                $scope.selectedSensorsChild = item;
            }
        };

        $scope.dataSelect={all:false};
        $scope.listSelect=[];

        $scope.selectAllData = function() {
            if ($scope.dataSelect.all) {
                $scope.listSelect = $scope.wrap.dataList.slice();
            }else{
                $scope.listSelect=[];
            }
        };
        $scope.dataInArray = function(item){
            if($scope.listSelect.length>0) {
                for (var i = 0; i < $scope.listSelect.length; i++) {
                    if ($scope.listSelect[i].Name === item.Name && $scope.listSelect[i].Provider === item.Provider) {
                        $scope.listSelect.splice(i, 1);
                        break;
                    }
                    if(i===$scope.listSelect.length-1){
                        if ($scope.listSelect[i].Name !== item.Name || $scope.listSelect[i].Provider !== item.Provider){
                            $scope.listSelect.push(item);
                            break;
                        }
                    }
                }
            } else { $scope.listSelect.push(item);}

            if($scope.listSelect.length < $scope.wrap.dataList.length){
                $scope.dataSelect.all=false;
            } else { $scope.dataSelect.all=true; }
        };
        $scope.isInSelected = function(item){
            for(var i=0; i < $scope.listSelect.length; i++){
                if($scope.listSelect[i].Name === item.Name && $scope.listSelect[i].Provider === item.Provider){
                    return true;
                }
            }
            return false;
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        function addLayer(tiledProvider) {
            webService.addLayer({type: service.type, id: service.identifier},
                {layerAlias: tiledProvider.dataId, layerId: tiledProvider.dataId, serviceType: service.type, serviceId: service.identifier, providerId: tiledProvider.providerId},
                function () {
                    Growl('success', 'Success', 'Layer ' + tiledProvider.dataId + ' successfully added to service ' + service.name);
                    $modalInstance.close();
                },
                function () {
                    Growl('error', 'Error', 'Layer ' + tiledProvider.dataId + ' failed to be added to service ' + service.name);
                    $modalInstance.dismiss('close');
                }
            );
        }

        function pyramidGenerationError() {
            Growl('error', 'Error', 'Failed to generate pyramid');
            $modalInstance.dismiss('close');
        }

        function setScale(response) {
            $scope.scales = response.Entry[0].split(',');
        }

        function errorOnPyramid() {
               Growl('error', 'Error', 'No scale can automatically be set');
        }

        /**
         * @FIXME rewrite this function to call rest api outside loop
         * the server side must provide method to treat pyramid with an array instead of treating for each data item.
         * @TODO ugly code, the client side should never call rest api inside a loop.
         */
        $scope.choose = function() {
            if ($scope.listSelect.length !== 0) {
                $scope.selected = $scope.listSelect;
            }
            if (!$scope.selected) {
                Growl('warning', 'Warning', 'No data selected');
                $modalInstance.dismiss('close');
                return;
            }
            else{
                if ($scope.service.type.toLowerCase() === 'sos') {
                    var sensorId = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                    sos.importSensor({id: service.identifier}, {values: {"sensorId": sensorId}}, function () {
                        Growl('success', 'Success', 'Sensor ' + sensorId + ' imported in service ' + service.name);
                        $modalInstance.close();
                    }, function () {
                        Growl('error', 'Error', 'Unable to import sensor ' + sensorId + ' in service ' + service.name);
                        $modalInstance.dismiss('close');
                    });
                    return;
                }

                if ($scope.wmtsParams === false) {
                    // just add the data if we are not in the case of the wmts service
                    if (service.type.toLowerCase() !== 'wmts') {
                        angular.forEach($scope.selected, function(value, key){
                            if (service.type.toLowerCase() === 'wms' && $scope.conformPyramid) {
                                // In the case of a wms service and user asked to pyramid the data
                                dataListing.pyramidConform({providerId: value.Provider, dataId: value.Name}, {}, addLayer, pyramidGenerationError);
                            } else {
                                webService.addLayer({type: service.type, id: service.identifier},
                                    {layerAlias: value.Name, layerId: value.Name, serviceType: service.type, serviceId: service.identifier, providerId: value.Provider, layerNamespace: value.Namespace},
                                function(response) {
                                    Growl('success', 'Success', response.message);
                                    $modalInstance.close();
                                },
                                function(response) {
                                    Growl('error', 'Error', response.message);
                                    $modalInstance.dismiss('close');
                                });
                            }
                        });
                        return;
                    }

                    for(var j=0; j<$scope.selected.length; j++) {
                        // WMTS here, prepare form
                        dataListing.pyramidScales({providerId: $scope.selected[j].Provider, dataId: $scope.selected[j].Name}, setScale, errorOnPyramid);
                        $scope.wmtsParams = true;
                    }
                } else {
                    // Finish the WMTS publish process
                    // Pyramid the data to get the new provider to add
                    for(var k=0; k<$scope.selected.length; k++) {
                        dataListing.pyramidData({providerId: $scope.selected[k].Provider, dataId: $scope.selected[k].Name},
                            {tileFormat: $scope.tileFormat, crs: $scope.crs, scales: $scope.scales, upperCornerX: $scope.upperCornerX, upperCornerY: $scope.upperCornerY}, addLayer, pyramidGenerationError);
                    }
                }
            }
        };

        $scope.truncate = function(text){
            if(text) {
                if (text.length > 40) {
                    return text.substr(0, 40) + "...";
                } else { return text; }
            }
        };
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

