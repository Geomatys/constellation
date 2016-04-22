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
angular.module('cstl-data-dashboard', [
    'cstl-restapi',
    'cstl-services',
    'cstl.components.previewMap',
    'ui.bootstrap.modal'])

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

    .controller('DatasetDashboardController', function($scope, $q, $routeParams, $http, $cookieStore, $modal, CstlConfig,
                                                       Growl, Dataset, Data, dataListing, style, provider, AppConfigService) {

        var self = this;

        // Indicates if "singleton" datasets must be displayed.
        self.showSingleton = CstlConfig['dataset.listing.show_singleton'];

        // Overview layer instance.
        self.preview = { layer: undefined, extent: undefined };

        // Variable to store dataset or/and data selection.
        var selection = self.selection = { dataset: null, data: null, style: null };

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
            }/*,
            {
                idHTML: 'dbchoice',
                name: 'database',
                translateKey: 'label.file.db',
                defaultTranslateValue: 'Database',
                bindFunction: function() { startDataImport('database', 'step1Database', true); }
            }*/
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
            selection.style = null;
            self.updatePreview();
        };

        // Select the style for data preview and.
        self.selectStyle = function(style) {
            selection.style = style;
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
                var targetData = selection.data;
                targetData.$infoPromise.then(function() {
                    if (targetData !== selection.data) {
                        return; // the selection has changed before the promise is resolved
                    }

                    // Create layer instance.
                    var layer;
                    if (targetData.styles.length) {
                        selection.style = selection.style || targetData.styles[0]; // get or set the style selection
                        layer = DataDashboardViewer.createLayerWithStyle(
                            $cookieStore.get('cstlUrl'),
                            layerName,
                            providerId,
                            selection.style.name,
                            null,
                            null,
                            targetData.type !== 'VECTOR');
                    } else {
                        layer = DataDashboardViewer.createLayer(
                            $cookieStore.get('cstlUrl'),
                            layerName,
                            providerId,
                            null,
                            targetData.type !== 'VECTOR');
                    }
                    layer.get('params').ts = new Date().getTime();

                    // Display the layer and zoom on its extent.
                    self.preview.extent = targetData.extent;
                    self.preview.layer = layer;
                });
            } else {
                self.preview.extent = self.preview.layer = undefined;
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
                    var targetData = selection.data;
                    style.link({ provider: item.Provider, name: item.Name }, {
                        values: {
                            dataProvider: targetData.providerIdentifier,
                            dataNamespace: targetData.namespace,
                            dataId: targetData.name
                        }
                    }, function() {
                        var style = { id: item.Id, name: item.Name, providerIdentifier: item.Provider };
                        targetData.styles.push(style);
                        targetData.styleCount++;
                        if (targetData === selection.data) {
                            // If the selection hasn't changed, use the new style in preview.
                            selection.style = style;
                            self.updatePreview();
                        }
                    });
                }
            });
        };

        // Break the association between the selected data and a style.
        self.dissociateStyle = function(style) {
            if (!selection.data) {
                return;
            }
            var targetData = selection.data;
            Data.dissociateStyle({ dataId: targetData.id, styleId: style.id }, function() {
                targetData.styles.splice(targetData.styles.indexOf(style), 1);
                targetData.styleCount--;
                if (targetData === selection.data && style === selection.style) {
                    // If the selection hasn't changed and the removed style was in use, no longer
                    // use this style in preview.
                    selection.style = null;
                    self.updatePreview();
                }
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
            var targetData = selection.data;
            Data.dissociateSensor({ dataId: targetData.id, sensorIdentifier: sensor.identifier }, function() {
                targetData.sensors.splice(targetData.sensors.indexOf(sensor), 1);
                targetData.sensorCount--;
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
            Growl('success', 'Success', 'Dataset '+ selection.dataset.name + ' successfully deleted');
            if (selection.data) {
                selection.data = null;
                // If a data was selected we need to clear the preview map.
                self.updatePreview();
            }
            selection.dataset = null;
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

        AppConfigService.getConfig(function(config) {

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

    /**
     * This controller is used by webservice-edit.js to open modal to add data into services
     */
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
    });

