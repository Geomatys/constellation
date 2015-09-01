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

    .constant('defaultDatasetQuery', {
        page: 1,
        size: 10,
        sort: { field: 'creation_date', order: 'DESC' },
        excludeEmpty: true,
        hasVectorData: null,
        hasCoverageData: null,
        hasLayerData: null,
        hasSensorData: null
    })

    .controller('DatasetListingController', function($scope, DashboardHelper, Dataset, defaultDatasetQuery) {

        var self = this;

        // Apply DashboardHelper features on the controller instance.
        DashboardHelper.call(self, Dataset.search, angular.copy(defaultDatasetQuery));

        // Immediate content loading.
        self.search();

        // Method used to modify a query filter and launch the search.
        self.setFilter = function(key, value) {
            self.query[key] = value;
            self.search();
        };

        // Method used to filter dataset on their data types.
        self.setTypeFilter = function(type) {
            switch (type) {
                case 'VECTOR':
                    self.query.hasVectorData = true;
                    self.query.hasCoverageData = null;
                    break;
                case 'COVERAGE':
                    self.query.hasVectorData = null;
                    self.query.hasCoverageData = true;
                    break;
                default:
                    self.query.hasVectorData = null;
                    self.query.hasCoverageData = null;
            }
            self.search();
        };

        // Method used to reset the search criteria.
        self.resetCriteria = function() {
            self.query = angular.copy(defaultDatasetQuery);
            self.search();
        };

        // Observe the 'reloadDatasets' event to re-launch the search.
        $scope.$on('reloadDatasets', self.search);
    })

    .controller('DatasetDashboardController', function($scope, $q, $routeParams, $http, $cookieStore, $modal, CstlConfig, Growl, Dataset, Data, dataListing, style, provider) {

        var self = this;

        // Indicates if "singleton" datasets must be displayed.
        self.showSingleton = CstlConfig['dataset.listing.show_singleton'];

        // Active tab ('dataset' or 'metadata').
        self.tab = $routeParams.tab || 'dataset';

        // Variable to store dataset or/and data selection.
        var selection = self.selection = { dataset: null, data: null };

        // Array of available ways to add a data.
        self.addDataWays = [
            {
                idHTML: 'uploadchoice',
                name: 'localFile',
                translateKey: 'label.file.local',
                defaultTranslateValue: 'Local file',
                bindFunction: function() { startDataImport('local', 'step1DataLocal', true); }
            },
            {
                idHTML: 'filesystemchoice',
                name: 'serverFile',
                translateKey: 'label.file.server',
                defaultTranslateValue: 'Server file',
                bindFunction: function() { startDataImport('server', 'step1DataServer', true); }
            },
            {
                idHTML: 'dbchoice',
                name: 'database',
                translateKey: 'label.file.db',
                defaultTranslateValue: 'Database',
                bindFunction: function() { startDataImport('database', 'step1Database', true); }
            }
        ];


        // Select/unselect a dataset/data.
        self.toggleSelect = function(dataset, data) {
            // Update dataset selection.
            if (!self.isSelected(dataset)) {
                selection.dataset = dataset;
                setupDatasetLazyInfo(dataset);
                if (dataset.dataCount === 1 && !data) {
                    data = dataset.data[0]; // auto-select the single data in singleton dataset
                }
            } else if (!data) {
                selection.dataset = null;
            }

            // Update data selection.
            if (data && !self.isSelected(data)) {
                selection.data = data;
                setupDataLazyInfo(data);
            } else {
                selection.data = null;
                if (!self.showSingleton && selection.dataset && selection.dataset.dataCount === 1) {
                    selection.dataset = null; // reset singleton dataset selection if not shown
                }
            }

            // Update data preview.
            self.updatePreview();
        };

        // Determines if a dataset/data is selected.
        self.isSelected = function(object) {
            if (angular.isNumber(object.datasetId)) {
                return selection.data && selection.data.id === object.id; // object is a data
            } else {
                return selection.dataset && selection.dataset.id === object.id; // object is a dataset
            }
        };

        // Determines if a dataset/data should be visible.
        self.shouldDisplayDataset = function(dataset) {
            return self.showSingleton || dataset.dataCount !== 1;
        };

        // Returns the data to display for a dataset.
        self.getDataToDisplay = function(dataset) {
            // If the specified dataset is selected, return its data.
            if (self.isSelected(dataset)) {
                setupDatasetLazyInfo(dataset); // ensure that data are well loaded
                return dataset.data;
            }
            // If the dataset contains a single data and if the 'dataset.listing.show_singleton'
            // configuration variable is set to false, return the single data in order to display
            // it instead of the dataset.
            return (!self.showSingleton && dataset.dataCount === 1) ? dataset.data : [];
        };

        // Display the data in the preview map.
        self.updatePreview = function() {
            // Reset map state.
            if (DataDashboardViewer.map) {
                DataDashboardViewer.map.setTarget(undefined);
            }
            DataDashboardViewer.initConfig();
            DataDashboardViewer.fullScreenControl = true;

            if (selection.data) {
                // Generate layer name.
                var layerName = selection.data.name;
                if (selection.data.namespace) {
                    layerName = '{' + selection.data.namespace + '}' + layerName;
                }

                // Use the pyramid provider identifier for better performances (if expected).
                var providerId = selection.data.providerIdentifier;
                if (CstlConfig['data.overview.use_pyramid'] === true && selection.data.pyramidProviderIdentifier) {
                    providerId = selection.data.pyramidProviderIdentifier;
                }

                // Wait for lazy loading completion.
                var lastSelection = selection.data;
                lastSelection.$infoPromise.then(function() {
                    if (lastSelection !== selection.data) {
                        return; // the selection has changed before the promise is resolved
                    }

                    // Create layer instance.
                    var layer;
                    if (lastSelection.styles.length) {
                        layer = DataDashboardViewer.createLayerWithStyle(
                            $cookieStore.get('cstlUrl'),
                            layerName,
                            providerId,
                            lastSelection.styles[0].name,
                            null,
                            null,
                            lastSelection.type !== 'VECTOR');
                    } else {
                        layer = DataDashboardViewer.createLayer(
                            $cookieStore.get('cstlUrl'),
                            layerName,
                            providerId,
                            null,
                            lastSelection.type !== 'VECTOR');
                    }
                    layer.get('params').ts = new Date().getTime();

                    // Display the layer and zoom on its extent.
                    DataDashboardViewer.layers = [layer];
                    DataDashboardViewer.extent = lastSelection.extent;
                    DataDashboardViewer.initMap('dataPreviewMap');
                });
            } else {
                DataDashboardViewer.initMap('dataPreviewMap');
            }
        };

        // Delete the selected dataset.
        self.deleteDataset = function() {
            if (!selection.dataset) {
                return;
            }
            $modal.open({
                templateUrl: 'views/modal-confirm.html',
                controller: 'ModalConfirmController',
                resolve: {
                    keyMsg: function() { return 'dialog.message.confirm.delete.dataset'; }
                }
            }).result.then(function(confirmation) {
                if (confirmation) {
                    Dataset.delete({ id: selection.dataset.id }, onDatasetDeleteSuccess, onDatasetDeleteError);
                }
            });
        };

        // Delete the selected data.
        self.deleteData = function() {
            if (!selection.data) {
                return;
            }
            $modal.open({
                templateUrl: 'views/modal-confirm.html',
                controller: 'ModalConfirmController',
                resolve: {
                    'keyMsg': function() { return 'dialog.message.confirm.delete.data'; }
                }
            }).result.then(function(confirmation){
                if (confirmation) {
                    Data.delete({ dataId: selection.data.id }, onDataDeleteSuccess, onDataDeleteError);
                }
            });
        };

        // Displays the metadata of the selected data.
        self.openMetadata = function() {
            if (!selection.data) {
                return;
            }
            $modal.open({
                templateUrl: 'views/data/modalViewMetadata.html',
                controller: 'ViewMetadataModalController',
                resolve: {
                    dashboardName: function() { return 'data'; },
                    metadataValues: function(textService) {
                        return textService.metadataJson(
                            selection.data.providerIdentifier,
                            selection.data.name,
                            selection.data.type === 'COVERAGE' ? 'raster' : selection.data.type.toLowerCase(), // TODO - harmonize 'coverage' and 'raster' types...
                            true);
                    }
                }
            });
        };

        // Open the modal allowing to choose new style associations.
        self.associateStyle = function() {
            if (!selection.data) {
                return;
            }
            $modal.open({
                templateUrl: 'views/style/modalStyleChoose.html',
                controller: 'StyleModalController',
                resolve: {
                    exclude: function() {
                        // TODO - harmonize style POJOs structure
                        return selection.data.styles.map(function(style) {
                            return {
                                Name: style.name,
                                Provider: style.providerIdentifier,
                                Type: style.type
                            };
                        });
                    },
                    selectedLayer: function() {
                        // TODO - harmonize data POJOs structure
                        return {
                            Name: selection.data.name,
                            Namespace: selection.data.namespace,
                            Provider: selection.data.providerIdentifier,
                            Type: selection.data.type
                        };
                    },
                    selectedStyle: function() { return null; },
                    serviceName: function() { return null; },
                    newStyle: function() { return null; },
                    stylechooser: function() { return null; }
                }
            }).result.then(function(item) {
                if (angular.isObject(item)) {
                    style.link({ provider: item.Provider, name: item.Name }, {
                        values: {
                            dataProvider: selection.data.providerIdentifier,
                            dataNamespace: selection.data.namespace,
                            dataId: selection.data.name
                        }
                    }, function() {
                        selection.data.styles.push({
                            id: item.Id,
                            name: item.Name,
                            providerIdentifier: item.Provider
                        });
                        selection.data.styleCount++;
                        self.updatePreview();
                    });
                }
            });
        };

        // Display the data in the preview map with style.
        self.previewDataWithStyle = function(style) {
            // Reset map state.
            if (DataDashboardViewer.map) {
                DataDashboardViewer.map.setTarget(undefined);
            }
            DataDashboardViewer.initConfig();
            DataDashboardViewer.fullScreenControl = true;

            if (selection.data) {
                // Generate layer name.
                var layerName = selection.data.name;
                if (selection.data.namespace) {
                    layerName = '{' + selection.data.namespace + '}' + layerName;
                }

                // Use the pyramid provider identifier for better performances (if expected).
                var providerId = selection.data.providerIdentifier;
                if (CstlConfig['data.overview.use_pyramid'] === true && selection.data.pyramidProviderIdentifier) {
                    providerId = selection.data.pyramidProviderIdentifier;
                }

                // Wait for lazy loading completion.
                var lastSelection = selection.data;
                lastSelection.$infoPromise.then(function() {
                    if (lastSelection !== selection.data) {
                        return; // the selection has changed before the promise is resolved
                    }

                    // Create layer instance.
                    var layer;
                    if (style) {
                        layer = DataDashboardViewer.createLayerWithStyle(
                            $cookieStore.get('cstlUrl'),
                            layerName,
                            providerId,
                            style.name,
                            null,
                            null,
                            lastSelection.type !== 'VECTOR');
                    } else {
                        layer = DataDashboardViewer.createLayer(
                            $cookieStore.get('cstlUrl'),
                            layerName,
                            providerId,
                            null,
                            lastSelection.type !== 'VECTOR');
                    }
                    layer.get('params').ts = new Date().getTime();

                    // Display the layer and zoom on its extent.
                    DataDashboardViewer.layers = [layer];
                    DataDashboardViewer.extent = lastSelection.extent;
                    DataDashboardViewer.initMap('dataPreviewMap');
                });
            } else {
                DataDashboardViewer.initMap('dataPreviewMap');
            }
        };

        // Break the association between the selected data and a style.
        self.dissociateStyle = function(style) {
            if (!selection.data) {
                return;
            }
            Data.dissociateStyle({ dataId: selection.data.id, styleId: style.id }, function() {
                selection.data.styles.splice(selection.data.styles.indexOf(style), 1);
                selection.data.styleCount--;
                self.updatePreview();
            });
        };

        // Open the model allowing the edit the specified style.
        self.editStyle = function(reference) {
            if (!selection.data) {
                return;
            }
            style.get({ provider: reference.providerIdentifier, name: reference.name }, function(response) {
                $modal.open({
                    templateUrl: 'views/style/modalStyleEdit.html',
                    controller: 'StyleModalController',
                    resolve: {
                        newStyle: function() { return response; },
                        selectedLayer: function() {
                            // TODO - harmonize data POJOs structure
                            return {
                                Name: selection.data.name,
                                Namespace: selection.data.namespace,
                                Provider: selection.data.providerIdentifier,
                                Type: selection.data.type
                            };
                        },
                        selectedStyle: function() { return null; },
                        serviceName: function() { return null; },
                        exclude: function() {  return null; },
                        stylechooser: function() { return 'edit'; }
                    }
                }).result.then(self.updatePreview);
            });
        };

        // Open the modal allowing to choose new sensor associations.
        self.associateSensor = function() {
            if (!selection.data) {
                return;
            }
            $modal.open({
                templateUrl: 'views/sensor/modalSensorChoose.html',
                controller: 'SensorModalChooseController',
                resolve: {
                    'selectedData': function() { return selection.data; }
                }
            });
        };

        // Break the association between the selected data and a sensor.
        self.dissociateSensor = function(sensor) {
            if (!selection.data) {
                return;
            }
            Data.dissociateSensor({ dataId: selection.data.id, sensorIdentifier: sensor.identifier }, function() {
                selection.data.sensors.splice(selection.data.sensors.indexOf(sensor), 1);
                selection.data.sensorCount--;
            });
        };


        // Starts the data import workflow.
        function startDataImport(type, step, editMetadata) {
            $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    firstStep: function() { return step; },
                    importType: function() { return type; }
                }
            }).result.then(function(result) {
                $scope.$broadcast('reloadDatasets');

                if (!editMetadata || !result || !result.file) {
                    return;
                }

                dataListing.initMetadata({}, {
                    values: {
                        providerId: result.file,
                        dataType: result.type,
                        mergeWithUploadedMD: result.completeMetadata
                    }
                }, function onMetadataInitializationSuccess() {
                    startMetadataEdition(null, result.file, result.type, 'import', 'data');
                }, function onMetadataInitializationError() {
                    Growl('error', 'Error', 'Unable to prepare metadata for next step.');
                });
            });
        }

        // Starts the metadata edition for imported data.
        function startMetadataEdition(provider, identifier, type, template, theme) {
            $modal.open({
                templateUrl: 'views/data/modalEditMetadata.html',
                controller: 'EditMetadataModalController',
                resolve: {
                    'provider': function() { return provider; },
                    'identifier': function() { return identifier; },
                    'type': function() { return type; },
                    'template': function() { return template; },
                    'theme': function() { return theme; }
                }
            });
        }

        // Loads asynchronously dataset advanced information.
        function setupDatasetLazyInfo(dataset, forceReload) {
            if (forceReload === true || !dataset.$infoPromise) {
                // Load the data list (if should be reloaded or if not returned by the server).
                var data = dataset.data;
                if (forceReload || !data) {
                    data = Dataset.getData({ id: dataset.id }).$promise;
                }

                // Combines multiple promises into a single promise.
                dataset.$infoPromise = $q.all([data]);

                // Handle promise results.
                dataset.$infoPromise.then(function(results) {
                    dataset.data = results[0];
                    dataset.dataCount = results[0].length;
                });
            }
        }

        // Loads asynchronously data advanced information.
        function setupDataLazyInfo(data, forceReload) {
            if (forceReload === true || !data.$infoPromise) {
                // Load the geographical extent.
                var description = provider.dataGeoExtent({
                    values: { providerId: data.providerIdentifier, dataId: data.name }
                }).$promise;

                // Load the associations (styles, services, sensors).
                var associations = Data.getAssociations({
                    dataId: data.id
                }).$promise;

                // Combines multiple promises into a single promise.
                data.$infoPromise = $q.all([description, associations]);

                // Handle promise results.
                data.$infoPromise.then(function(results) {
                    data.extent = results[0].boundingBox;
                    data.styles = results[1].styles;
                    data.services = results[1].services;
                    data.sensors = results[1].sensors;
                });
            }
        }

        // Dataset deletion success callback.
        function onDatasetDeleteSuccess() {
            Growl('success', 'Success', 'Data set '+ selection.dataset.name + ' successfully deleted');
            selection.dataset = selection.data = null;
            $scope.$broadcast('reloadDatasets');
        }

        // Dataset deletion error callback.
        function onDatasetDeleteError() {
            Growl('error', 'Error', 'Dataset '+ selection.dataset.name + ' deletion failed');
        }

        // Data deletion success callback.
        function onDataDeleteSuccess() {
            Growl('success', 'Success', 'Data ' + selection.data.name + ' successfully deleted');
            if (selection.dataset.dataCount === 1) {
                selection.dataset = null;
                $scope.$broadcast('reloadDatasets');
            } else {
                setupDatasetLazyInfo(selection.dataset, true);
            }
            selection.data = null;
            self.updatePreview();
        }

        // Data deletion error callback.
        function onDataDeleteError() {
            Growl('error', 'Error', 'Data ' + selection.data.name + ' deletion failed');
        }


        // Load data import configuration.
        $http.get("app/conf").success(function(config) {
            if (config['cstl.import.empty']) {
                self.addDataWays.push({
                    name: 'emptyDataset',
                    idHTML: 'emptychoice',
                    translateKey: 'label.file.empty',
                    defaultTranslateValue: 'Empty dataset',
                    bindFunction: function() { startDataImport('empty', 'step2Metadata', true); }
                });
            }
            if (config['cstl.import.custom']) {
                self.addDataWays.push({
                    name: 'customDataset',
                    idHTML: 'customchoice',
                    translateKey: 'label.file.custom',
                    defaultTranslateValue: 'Other',
                    bindFunction: function() { startDataImport('custom', 'step1Custom', false); }
                });
            }
        });
    })


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
            advancedMetadataSearch : false,
            searchTerm : "",
            searchMetadataTerm : "",
            alphaPattern : /^([0-9A-Za-z\u00C0-\u017F\*\?]+|\s)*$/,
            published : null,
            observation : null,
            smallMode : false,
            selectedDataSetChild : null
        };
        $scope.search = {};
        $scope.searchMD = {};

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
            //display dashboard map
            setTimeout(function(){
                $scope.showDataDashboardMap();
            },300);
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

        // Reload datasets when needed.
        $scope.$on('reloadDatasets', $scope.initAfterImport);

        /**
         * Reset filters for dashboard
         */
        $scope.resetFilters = function(){
            $scope.wrap.filtertext='';
            $scope.wrap.filtertype = undefined;
            $scope.dataCtrl.searchMetadataTerm="";
            $scope.dataCtrl.selectedDataSetChild = null;
            $scope.selectedDS = null;
            datasetListing.listAll({}, function(response){//success
                Dashboard($scope, response, true);
                $scope.wrap.ordertype = "Name";
            }, function(response){//error
                Growl('error','Error','Unable to load list of dataset!');
            });
            $scope.showDataDashboardMap();
        };

        $scope.showDataDashboardMap = function() {
            if (DataDashboardViewer.map) {
                DataDashboardViewer.map.setTarget(undefined);
            }
            DataDashboardViewer.initConfig();
            DataDashboardViewer.fullScreenControl = true;
            var selectedData = $scope.dataCtrl.selectedDataSetChild;
            if(selectedData) {
                var layerName;
                if (selectedData.Namespace) {
                    layerName = '{' + selectedData.Namespace + '}' + selectedData.Name;
                } else {
                    layerName = selectedData.Name;
                }
                var providerId = selectedData.Provider;

                provider.dataGeoExtent({},{values: {'providerId':providerId,'dataId':layerName}},
                    function(response) {//success
                        var bbox = response.boundingBox;

                        var overlay= null;
                        if (bbox) {
                            DataDashboardViewer.extent = [bbox[0],bbox[1],bbox[2],bbox[3]];
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
                        DataDashboardViewer.initMap('metadataPreviewMap');
                        if(overlay) {
                            DataDashboardViewer.map.addOverlay(overlay);
                        }
                    }, function() {//error
                        // failed to find a metadata, just load the full map
                        DataDashboardViewer.initMap('metadataPreviewMap');
                    }
                );
            }else {
                DataDashboardViewer.initMap('metadataPreviewMap');
            }
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

        $scope.toggleUpDownSelectedMD = function() {
            var $header = $('#metadataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
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

