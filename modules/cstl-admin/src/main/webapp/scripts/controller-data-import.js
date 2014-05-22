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

cstlAdminApp.controller('ModalImportDataController', ['$scope', '$modalInstance', 'dataListing', 'provider', 'firstStep', '$uploadFiles', '$growl',
    function($scope, $modalInstance, dataListing, provider, firstStep, $uploadFiles, $growl) {
        $scope.import = {
            currentStep: firstStep,
            dataPath: null,
            mdPath: null,
            uploadType: null,
            allowNext: true,
            allowSubmit: false,
            allowSensorChoose: false,
            next: angular.noop,
            finish: angular.noop,
            metadata: null,
            providerId: null,
            layer: null
        };

        $scope.sensor = {
            mode : "existing",
            checked : false
        };

        $scope.close = function() {
            if ($scope.import.currentStep = 'step4Netcdf') {
                $modalInstance.close({type: $scope.import.uploadType, file: $scope.import.providerId, missing: $scope.import.metadata == null});
            } else {
                $modalInstance.dismiss('close');
            }
        };

        $scope.showAssociate = function() {
            $scope.import.currentStep = 'step4Sensor';
            $scope.import.allowSensorChoose = false;
            $scope.import.allowSubmit = true;
        };

        $scope.uploaded = function() {
            if ($scope.import.dataPath && $scope.import.dataPath.indexOf('failed') === -1) {
                var upFile = $scope.import.dataPath;
                var upMdFile = null;
                if ($scope.import.mdPath && $scope.import.mdPath.indexOf('failed') === -1) {
                    upMdFile = $scope.import.mdPath;
                }

                // Stores uploaded files in session for further use
                var upFiles = $uploadFiles.files;
                upFiles.file = upFile;
                upFiles.mdFile = upMdFile;

                var justFile = upFile.substring(upFile.lastIndexOf("/")+1);
                var fileName = justFile;
                var fileExtension;
                if (fileName.indexOf(".") !== -1) {
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    fileExtension = justFile.substring(justFile.lastIndexOf(".")+1);
                }
                dataListing.importData({values: {'filePath': upFiles.file, 'metadataFilePath': upFiles.mdFile, dataType: $scope.import.uploadType}}, function(response) {

                    var importedData = response.dataFile;
                    var importedMetaData = response.metadataFile;

                    dataListing.findDataType({values: {'filePath':importedData, 'extension': fileExtension, dataType: $scope.import.uploadType}}, function(selectedExtension) {


                        // Store the providerId for further calls
                        $scope.import.providerId = fileName;
                        $scope.import.uploadType = selectedExtension.dataType;

                        if ($scope.import.uploadType === "vector") {
                            provider.create({
                                id: fileName
                            }, {
                                type: "feature-store",
                                subType: "shapefile",
                                parameters: {
                                    path: importedData
                                }
                            }, function() {
                                if (importedMetaData) {
                                    dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId, 'mdPath': importedMetaData}});
                                }

                                $growl('success','Success','Shapefile data '+ fileName +' successfully added');
                                if ($scope.sensor.checked) {
                                    $scope.showAssociate();
                                } else {
                                    $modalInstance.close({type: "vector", file: fileName, missing: $scope.import.metadata == null});
                                }
                            });
                        } else if ($scope.import.uploadType === "raster") {
                            provider.create({
                                id: fileName
                            }, {
                                type: "coverage-store",
                                subType: "coverage-file",
                                parameters: {
                                    path: importedData
                                }
                            }, function() {
                                if (importedMetaData) {
                                    dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId, 'mdPath': importedMetaData}});
                                }

                                if (!fileExtension || fileExtension !== "nc") {
                                    $growl('success','Success','Coverage data '+ fileName +' successfully added');
                                    $modalInstance.close({type: "raster", file: fileName, missing: $scope.import.metadata == null});
                                } else {
                                    $scope.showAssociate();
                                    // todo: displayNetCDF(fileName);
                                }
                            });
                        } else if ($scope.import.uploadType === "observation" && fileExtension === "xml") {
                            provider.create({
                                id: fileName
                            }, {
                                type: "observation-store",
                                subType: "observation-xml",
                                parameters: {
                                    path: importedData
                                }
                            }, function() {
                                if (importedMetaData) {
                                    dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId, 'mdPath': importedMetaData}});
                                }

                                $growl('success','Success','Observation data '+ fileName +' successfully added');

                                $scope.showAssociate();
                                //$modalInstance.close({type: "observation", file: fileName, missing: $scope.metadata == null});

                            });

                        } else if ($scope.import.uploadType === "observation") {
                            provider.create({
                                id: fileName
                            }, {
                                type: "observation-store",
                                subType: "observation-file",
                                parameters: {
                                    path: importedData
                                }
                            }, function() {
                                if (importedMetaData) {
                                    dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId, 'mdPath': importedMetaData}});
                                }

                                $growl('success','Success','Observation data '+ fileName +' successfully added');

                                $scope.showAssociate();
//                                if (!fileExtension || fileExtension !== "nc") {
//                                    $growl('success','Success','Observation data '+ fileName +' successfully added');
//                                    $modalInstance.close({type: "observation", file: fileName, missing: $scope.metadata == null});
//                                } else {
//                                    displayNetCDF(fileName);
//                                }
                            });
                        } else {
                            $growl('warning','Warning','Not implemented choice');
                            $modalInstance.close();
                        }
                    });
                });

            } else {
                $growl('error','Error','Data import failed');
                $modalInstance.close();
            }
        };

        $scope.import.finish = function() {
            $scope.uploaded();
        };
    }]);

cstlAdminApp.controller('ModalImportDataStep1LocalController', ['$scope', 'dataListing', '$cookies',
    function($scope, dataListing, $cookies) {
        $scope.loader = {
            upload: false
        };

        $scope.import.next = function() {
            $scope.uploadData();
        };

        $scope.uploadData = function() {
            var $form = $('#uploadDataForm');

            var formData = new FormData($form[0]);
            $scope.loader.upload = true;
            $.ajax({
                url: $cookies.cstlUrl + "api/1/data/upload/data;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                cache: false,
                contentType: false,
                processData: false,
                success: function (dataPath) {
                    $scope.$apply(function() {
                        $scope.import.dataPath = dataPath;
                        $scope.loader.upload = false;
                        $scope.import.currentStep = 'step2Metadata';
                        $scope.import.allowNext = true;
                    });
                }
            });
        };

        $scope.verifyExtension = function(path) {
            var lastPointIndex = path.lastIndexOf(".");
            var extension = path.substring(lastPointIndex+1, path.length);
            dataListing.extension({}, {value: extension},
                function(response) {
                    if (response.dataType!="") {
                        $scope.import.uploadType = response.dataType;
                    }
                    $scope.import.allowNext = true;
                });
        };
    }]);

cstlAdminApp.controller('ModalImportDataStep1ServerController', ['$scope', 'dataListing',
    function($scope, dataListing) {
        $scope.columns = [];
        // current path chosen in server data dir
        $scope.currentPath = '/';
        // path of the server data dir
        $scope.prefixPath = '';
        $scope.hasSelectedSomething = false;

        $scope.load = function(path){
            $scope.currentPath = path;
            if (path === '/') {
                path = "root";
            }
            $scope.columns.push(dataListing.dataFolder({}, path));
        };

        $scope.open = function(path, depth) {
            if (depth < $scope.columns.length) {
                $scope.columns.splice(depth + 1, $scope.columns.length - depth);
            }
            $scope.load(path);
        };

        $scope.chooseFile = function(path, depth) {
            if (depth < $scope.columns.length) {
                $scope.columns.splice(depth + 1, $scope.columns.length - depth);
            }
            $scope.currentPath = path;
        };

        $scope.select = function(item,depth) {
            $scope.prefixPath = item.prefixPath;
            $scope.hasSelectedSomething = true;
            if (item.folder) {
                $scope.open(item.subPath, depth);
            } else {
                $scope.chooseFile(item.subPath, depth);
            }
        };

        $scope.startWith = function(path) {
            return $scope.currentPath.indexOf(path) === 0;
        };

        $scope.load($scope.currentPath);

        $scope.shownColumn=1;

        $scope.navServer = function() {
            var tab= $scope.currentPath.split('/');

            if(tab.length>3) {
                $(".block-folders").slice(0,tab.length-3).hide();
                $(".block-folders").slice(tab.length-3,tab.length-1).show();
            }
            else
                $(".block-folders").show();
        };
    }]);

cstlAdminApp.controller('ModalImportDataStep1DatabaseController', ['$scope',
    function($scope) {

    }]);

cstlAdminApp.controller('ModalImportDataStep2MetadataController', ['$scope', '$cookies',
    function($scope, $cookies) {
        $scope.import.next = function() {
            if ($scope.import.metadata) {
                $scope.uploadMetadata();
            }

            $scope.import.allowNext = false;
            if ($scope.import.uploadType == null) {
                $scope.import.currentStep = 'step3Type';
                $scope.import.allowSubmit = true;
            } else {
                $scope.uploaded();
            }
        };

        $scope.uploadMetadata = function() {
            var $form = $('#uploadMetadataForm');

            var formData = new FormData($form[0]);

            $.ajax({
                url: $cookies.cstlUrl + "api/1/data/upload/metadata;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function(mdPath) {
                    $scope.import.mdPath = mdPath;
                }
            });
        };

        $scope.metadataChosen = function(md) {
            $scope.import.metadata = md.value;
            $scope.$digest();
        };
    }]);

cstlAdminApp.controller('ModalImportDataStep3TypeController', ['$scope',
    function($scope) {
        $scope.changeAssociationState = function() {
            if ($scope.sensor.checked) {
                $scope.import.allowSensorChoose = true;
                $scope.import.allowSubmit = false;
            } else {
                $scope.import.allowSensorChoose = false;
                $scope.import.allowSubmit = true;
            }
        };
    }]);

cstlAdminApp.controller('ModalImportDataStep4SensorController', ['$scope', 'sensor', 'dataListing', '$dashboard', '$growl', '$cookies',
    function($scope, sensor, dataListing, $dashboard, $growl, $cookies) {
        $scope.selectedSensorsChild = null;

        $scope.selectSensorsChild = function(item) {
            if ($scope.selectedSensorsChild === item) {
                $scope.selectedSensorsChild = null;
            } else {
                $scope.selectedSensorsChild = item;
            }
        };

        $scope.initDashboardSensor = function() {
            sensor.list({}, function(response) {
                $dashboard($scope, response.children, false);
                $scope.nbbypage = 5;
            });
        };


        $scope.import.finish = function() {
            if ($scope.sensor.mode === 'existing') {
                var sensorId = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                dataListing.listDataForProv({providerId: $scope.import.providerId}, function(response) {
                    for (var i=0; i<response.length; i++) {
                        dataListing.linkToSensor({providerId: response[i].Provider, dataId: response[i].Name, sensorId: sensorId}, {value: response[i].Namespace});
                    }
                });
            } else if ($scope.sensor.mode === 'automatic') {
                dataListing.listDataForProv({providerId: $scope.import.providerId}, function(response) {
                    for (var i=0; i<response.length; i++) {
                        dataListing.generateSML({}, {values: {'providerId': response[i].Provider, 'dataId': '{' + response[i].Namespace + '}' + response[i].Name}});
                    }
                });
            } else {
                // Import sensorML
                $scope.uploadImportAndLinkSensor();
            }

            $scope.close();
            //$modalInstance.close({type: $scope.import.uploadType, file: $scope.import.providerId, missing: $scope.import.metadata == null});
        };

        $scope.uploadImportAndLinkSensor = function() {
            var $form = $('#uploadSensor');

            var formData = new FormData($form[0]);

            $.ajax({
                url: $cookies.cstlUrl + "api/1/data/upload/data;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function (path) {
                    importAndLinkSensor(path);
                }
            });
        };

        function importAndLinkSensor(path) {
            sensor.add({}, {values: {'path' : path}}, function(sensors) {
                $growl('success','Success','Sensor correctly imported');

                for (var s=0; s<sensors.length; s++) {
                    var sensorId = sensors[s].identifier;
                    dataListing.listDataForProv({providerId: $scope.import.providerId}, function(response) {
                        for (var i=0; i<response.length; i++) {
                            dataListing.linkToSensor({providerId: response[i].Provider, dataId: response[i].Name, sensorId: sensorId}, {value: response[i].Namespace});
                        }
                    });
                }
            }, function() {
                $growl('error','Error','Unable to import sensor');
            });
        }
    }]);

cstlAdminApp.controller('ModalImportDataStep4SNetcdfController', ['$scope', 'dataListing', '$growl', '$cookies',
    function($scope, dataListing, $growl, $cookies) {
        function displayNetCDF(providerId) {
            $scope.import.currentStep = 'step4Netcdf';
            $scope.import.allowNext = false;
            $scope.import.allowSensorChoose = false;
            $scope.import.allowSubmit = false;

            $scope.coveragesData = dataListing.listCoverage({}, {value: providerId}, function(response) {
                for (var key in response.values) {
                    displayLayer(response.values[key]);
                    break;
                }
            });
        }

        function displayLayer(layer) {
            $scope.import.layer = layer;
            var layerData = DataViewer.createLayer($cookies.cstlUrl, layer, $scope.import.providerId);
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataPreviewMap');
        };
    }]);