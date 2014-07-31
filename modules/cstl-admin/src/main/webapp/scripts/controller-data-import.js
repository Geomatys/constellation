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
            layer: null,
            db: {}
        };
        $scope.enableSelectEPSGCode = false;

        $scope.sensor = {
            mode : "existing",
            checked : false
        };

        $scope.close = function() {
            if ($scope.import.currentStep === 'step4Netcdf') {
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



        $scope.importDb = function() {
            var providerId = $scope.import.identifier;
            provider.create({
                id: providerId
            }, {
                type: "feature-store",
                subType: "postgresql",
                parameters: {
                    host: $scope.import.db.url,
                    port: $scope.import.db.port,
                    user: $scope.import.db.user,
                    password: $scope.import.db.password,
                    database: $scope.import.db.name
                }
            }, function() {
                if ($scope.import.metadata) {
                    dataListing.setUpMetadata({values: {'providerId': providerId, 'mdPath': $scope.import.metadata}});
                }

                $growl('success','Success','Postgis database successfully added');
                $modalInstance.close({type: "vector", file: $scope.import.identifier, missing: $scope.import.metadata == null});
            });
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
                upFiles.file = $scope.import.dataName;
                upFiles.mdFile = upMdFile;

                var justFile = upFile.substring(upFile.lastIndexOf("/")+1);
                var fileName = justFile;
                var fileExtension;
                if (fileName.indexOf(".") !== -1) {
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    fileExtension = justFile.substring(justFile.lastIndexOf(".")+1);
                }
                dataListing.importData({values: {'dataPath': $scope.import.dataPath, 'metadataFilePath': $scope.import.mdPath, dataType: $scope.import.uploadType, dataName: $scope.import.dataName}}, function(response) {

                    var importedData = response.dataFile;
                    var importedMetaData = response.metadataFile;
                    $scope.import.metadata = importedMetaData;

                    dataListing.findDataType({values: {'filePath':importedData, 'extension': fileExtension, dataType: $scope.import.uploadType}}, function(selectedExtension) {


                        // Store the providerId for further calls
                        $scope.import.providerId = $scope.import.dataName;
                        $scope.import.uploadType = selectedExtension.dataType;

                        if ($scope.import.uploadType === "vector") {
                            provider.create({
                                id: $scope.import.providerId
                            }, {
                                type: "feature-store",
                                subType: "shapefile",
                                parameters: {
                                    path: importedData
                                }
                            }, function() {
                                provider.verifyCRS({ id: $scope.import.providerId},
                                    function() {
                                        //success
                                        if (importedMetaData || $scope.import.dataName) {
                                            dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId, 'mdPath': importedMetaData, dataName: $scope.import.dataName}});
                                        }
                                        //update data & metadata files reminder for further use
                                        $uploadFiles.files = {file: $scope.import.providerId, mdFile: importedMetaData, providerId: $scope.import.providerId};

                                        $growl('success','Success','Shapefile data '+ $scope.import.providerId +' successfully added');
                                        if ($scope.sensor.checked) {
                                            $scope.showAssociate();
                                        } else {
                                            $modalInstance.close({type: $scope.import.uploadType, file: $scope.import.providerId, missing: $scope.import.metadata == null});
                                        }

                                    }, function() {
                                        //failure
                                        $growl('error','Error','Data '+ $scope.import.providerId +' without Projection');
                                        $scope.import.allowSubmit = false;
                                        //TODO div select EPSG
                                        $scope.import.enableSelectEPSGCode = true;
                                        provider.getAllCodeEPSG({ id: $scope.import.providerId},
                                            function(response){
                                                //success
                                                $scope.epsgList = response.list;
                                                $scope.import.fileName = $scope.import.providerId;

                                            },
                                            function(){
                                                //error
                                                $growl('error','Error','Impossible to get all EPSG code');
                                            });

                                    }
                                );

                            }, function(){
                                $growl('error','Error','Impossible to create dataSet');
                            });
                        } else if ($scope.import.uploadType === "raster") {
                            provider.create({
                                id: $scope.import.providerId
                            }, {
                                type: "coverage-store",
                                subType: "coverage-file",
                                parameters: {
                                    path: importedData
                                }
                            }, function() {
                                provider.verifyCRS({ id: $scope.import.providerId},
                                    function() {
                                        //success
                                        if (importedMetaData) {
                                            dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId, 'mdPath': importedMetaData}});
                                        }
                                        //update data & metadata files reminder for further use
                                        $uploadFiles.files = {file: $scope.import.providerId, mdFile: importedMetaData, providerId: $scope.import.providerId};

                                        if (!fileExtension || fileExtension !== "nc") {
                                            $growl('success','Success','Coverage data '+ $scope.import.providerId +' successfully added');
                                            $modalInstance.close({type: $scope.import.uploadType, file: $scope.import.providerId, missing: $scope.import.metadata == null});
                                        } else {
                                            $scope.showAssociate();
                                            // todo: displayNetCDF(fileName);
                                        }

                                    }, function() {
                                        //failure
                                        $growl('error','Error','Data '+ $scope.import.providerId +' without Projection');
                                        $scope.import.allowSubmit = false;
                                        //TODO div select EPSG
                                        $scope.import.enableSelectEPSGCode = true;
                                        provider.getAllCodeEPSG({ id: $scope.import.providerId},
                                            function(response){
                                                //success
                                                $scope.epsgList = response.list;
                                                $scope.import.fileName = $scope.import.providerId;

                                            },
                                            function(){
                                                //error
                                                $growl('error','Error','Impossible to get all EPSG code');
                                            });


                                        //provider.delete({id: fileName});
                                    }
                                );


                            } , function(){
                                $growl('error','Error','Impossible to create dataSet');
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

                            }, function(){
                                $growl('error','Error','Impossible to create dataSet');
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
                            }, function(){
                                $growl('error','Error','Impossible to create dataSet');
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

        $scope.addProjection = function (){
            console.log($scope.import.epsgSelected);
            var codeEpsg = $scope.import.epsgSelected.substr(0,$scope.import.epsgSelected.indexOf(" "));
            provider.createPRJ({ id: $scope.import.fileName },{codeEpsg: codeEpsg},
                function(){
                    //success
                    $uploadFiles.files = {file: $scope.import.providerId, mdFile: $scope.import.metadata, providerId: $scope.import.providerId};
                    $modalInstance.close({type: $scope.import.uploadType, file: $scope.import.providerId, missing: $scope.import.metadata == null});
                },
                function(){
                    // error
                    $growl('error','Error','Impossible to set projection for data '+ $scope.import.fileName );
                }
            );

        }
    }]);

cstlAdminApp.controller('ModalImportDataStep1LocalController', ['$scope', 'dataListing', '$cookies', '$growl',
    function($scope, dataListing, $cookies, $growl) {
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
                url: $cookies.cstlUrl + "api/1/domain/"+ $cookies.cstlActiveDomainId + "/data/upload/data;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    $scope.$apply(function() {
                        $scope.import.dataPath = data.dataPath;
                        $scope.loader.upload = false;
                        $scope.import.currentStep = 'step2Metadata';
                        $scope.import.allowNext = true;
                    });
                },
                error: function(){
                    $growl('error', 'Error', 'error while uploading data');
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
        $scope.currentPath = 'root';
        $scope.hasSelectedSomething = false;

        $scope.load = function(path){
            $scope.currentPath = path;
            $scope.columns.push(dataListing.dataFolder({}, path, function(files) {
                if ($scope.currentPath === 'root') {
                    // When initializing popup, get the default directory
                    $scope.currentPath = files[0].parentPath;
                }
            }));
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
            $scope.hasSelectedSomething = true;
            if (item.folder) {
                $scope.open(item.path, depth);
            } else {
                $scope.chooseFile(item.path, depth);
            }
        };

        $scope.startWith = function(path) {
            return $scope.currentPath.indexOf(path) === 0;
        };

        $scope.load($scope.currentPath);

        $scope.navServer = function() {
            if($scope.columns.length>3) {
                $(".block-folders").slice(0,$scope.columns.length-3).hide();
                $(".block-folders").slice($scope.columns.length-3,$scope.columns.length-1).show();
            } else {
                $(".block-folders").show();
            }
        };

        $scope.import.next = function() {
            var lastPointIndex = $scope.currentPath.lastIndexOf(".");
            var extension = $scope.currentPath.substring(lastPointIndex+1, $scope.currentPath.length);
            dataListing.extension({}, {value: extension},
                function(response) {
                    if (response.dataType!="") {
                        $scope.import.uploadType = response.dataType;
                    }
                });

            // Use selected data
            $scope.import.dataPath = $scope.currentPath;
            $scope.import.currentStep = 'step2Metadata';
            $scope.import.allowNext = true;
        };
    }]);

cstlAdminApp.controller('ModalImportDataStep1DatabaseController', ['$scope','provider', '$growl',
    function($scope, provider, $growl) {
        $scope.import.allowNext = false;
        $scope.import.testConnected = false;

        $scope.import.next = function() {
            $scope.import.currentStep = 'step2Metadata';

        };
        $scope.testDB = function(){
            var providerId = "postgis-"+ $scope.import.db.name;
            provider.test({
                id: providerId
            }, {
                type: "feature-store",
                subType: "postgresql",
                parameters: {
                    host: $scope.import.db.url,
                    port: $scope.import.db.port,
                    user: $scope.import.db.user,
                    password: $scope.import.db.password,
                    database: $scope.import.db.name
                }
            }, function(response) {
                $growl('success', 'Success', 'Connected to database');
                $scope.import.testConnected = true;
                $scope.import.allowNext = true;

            },function(response){
                $growl('error','Error','Unable to connect database. Verify parrameters');
            });
        }

    }]);


cstlAdminApp.controller('ModalImportDataStep2MetadataController', ['$scope', '$cookies','$growl',
    function($scope, $cookies, $growl) {

        $scope.import.allowNext = false;
        if ($scope.import.dataPath != null && $scope.import.dataPath.length > 0){
            $scope.import.identifier = $scope.import.dataPath.replace(/^.*(\\|\/|\:)/, '').substr(0,$scope.import.dataPath.replace(/^.*(\\|\/|\:)/, '').lastIndexOf('.'));
        }
        if ($scope.import.identifier != null && $scope.import.identifier.length > 0) {
            $scope.import.allowNext = true;
        }



        $scope.verifyAllowNext = function(){
                if (($scope.import.identifier != null && $scope.import.identifier.length > 0) || ($scope.import.metadata  != null && $scope.import.metadata.length > 0)) {
                    var letters = /^[A-Za-zàèìòùáéíóúäëïöüñãõåæøâêîôû0-9\-_]+$/;
                    var id = $scope.import.identifier;
                    if(!id.match(letters)) {
                        $scope.import.allowNext = false;
                        $growl('error','Error','fill identifier without special chars like space');
                    }else {
                        $scope.import.allowNext = true;
                    }
                }else{
                    $scope.import.allowNext = false;
                }

        };

        $scope.import.next = function() {
            if ($scope.import.metadata != null || $scope.import.identifier != null) {
                $scope.uploadMetadata();
            } else {
                $scope.selectType();
            }

        };

        $scope.selectType = function(){
            $scope.import.allowNext = false;
            if ($scope.import.db.url) {
                $scope.importDb();
            } else if ($scope.import.uploadType == null) {
                $scope.import.currentStep = 'step3Type';
                $scope.import.allowSubmit = true;
            } else {
                $scope.uploaded();
            }
        }

        $scope.uploadMetadata = function() {
            var $form = $('#uploadMetadataForm');

            var formData = new FormData($form[0]);

            $.ajax({
                url: $cookies.cstlUrl + "api/1/domain/"+ $cookies.cstlActiveDomainId + "/data/upload/metadata;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function(result) {
                    $scope.import.mdPath = result.metadataPath;
                    $scope.import.dataName = result.dataName;
                    $scope.import.dataTitle = result.metatitle;
                    $scope.import.metaIdentifier = result.metaIdentifier;
                    $scope.selectType();
                },
                error: function(result){

                    $growl('error','Error',result.responseJSON.msg);
//                    $scope.import.currentStep = 'step2Metadata';
//                    $scope.import.allowNext = false;
//                    $scope.import.allowSubmit = false;
                }
            });
        };

        $scope.metadataChosen = function(md) {
            $scope.$apply(function() {
                $scope.import.metadata = md.value;
                $scope.import.identifier = null;
                if ($scope.import.metadata != null && $scope.import.metadata.length > 0){
                    $scope.import.allowNext = true;
                }
            });
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
                url: $cookies.cstlUrl + "api/1/domain/"+ $cookies.cstlActiveDomainId + "/data/upload/data;jsessionid="+ $cookies.cstlSessionId,
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