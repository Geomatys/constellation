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

angular.module('cstl-data-import', ['ngCookies', 'cstl-restapi', 'cstl-services', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .controller('ModalImportDataController', function($scope, $modalInstance, dataListing, provider, firstStep, UploadFiles, Growl) {
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
            $modalInstance.close({type: $scope.import.uploadType,
                                  file: $scope.import.providerId});
        };

        $scope.showAssociate = function() {
            $scope.import.currentStep = 'step4Sensor';
            $scope.import.allowSensorChoose = false;
            $scope.import.allowSubmit = true;
        };

        $scope.importDb = function() {
            var providerId = $scope.import.identifier;
            provider.create({id: providerId}, {
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
                //success
                if ($scope.import.metadata) {
                    dataListing.setUpMetadata({values: {'providerId': providerId,
                                                        'mdPath': $scope.import.metadata}});
                }
                Growl('success','Success','Postgis database successfully added');
                $modalInstance.close({type: "vector",
                                      file: $scope.import.identifier});
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
                var upFiles = UploadFiles.files;
                upFiles.file = $scope.import.dataName;
                upFiles.mdFile = upMdFile;

                var justFile = upFile.substring(upFile.lastIndexOf("/")+1);
                var fileName = justFile;
                var fileExtension;
                if (fileName.indexOf(".") !== -1) {
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    fileExtension = justFile.substring(justFile.lastIndexOf(".")+1);
                }
                dataListing.importData({values: {'dataPath': $scope.import.dataPath,
                                                 'metadataFilePath': $scope.import.mdPath,
                                                 dataType: $scope.import.uploadType,
                                                 dataName: $scope.import.dataName,
                                                 fsServer: $scope.import.fsserver }},
                    function(response) {//success
                    var importedData = response.dataFile;
                    var importedMetaData = response.metadataFile;
                    $scope.import.metadata = importedMetaData;

                    dataListing.findDataType({values: {'filePath':importedData,
                                                       'extension': fileExtension,
                                                        dataType: $scope.import.uploadType}},
                        function(selectedExtension) {//success
                        // Store the providerId for further calls
                        $scope.import.providerId = $scope.import.dataName;
                        $scope.import.uploadType = selectedExtension.dataType;
                        if ($scope.import.uploadType === "vector") {
                            var subType;
                            if (fileExtension==="shp" || fileExtension==="SHP") {
                                subType = "";
                            } else {
                                subType = "shapefile";
                            }
                            provider.create({id: $scope.import.providerId},
                                {type: "feature-store",
                                 subType: subType,
                                 parameters: {
                                    path: importedData
                                 }
                                },
                                function() {//success
                                provider.verifyCRS({ id: $scope.import.providerId},
                                    function() {//success
                                        if (importedMetaData || $scope.import.dataName) {
                                            dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId,
                                                                                'mdPath': importedMetaData,
                                                                                dataName: $scope.import.dataName}});
                                        }
                                        //update data & metadata files reminder for further use
                                        UploadFiles.files = {
                                            file: $scope.import.providerId,
                                            mdFile: importedMetaData,
                                            providerId: $scope.import.providerId
                                        };
                                        Growl('success','Success','Shapefile data '+ $scope.import.providerId +' successfully added');
                                        if ($scope.sensor.checked) {
                                            $scope.showAssociate();
                                        } else {
                                            $modalInstance.close({type: $scope.import.uploadType,
                                                                  file: $scope.import.providerId});
                                        }
                                    }, function() {//failure
                                        Growl('error','Error','Data '+ $scope.import.providerId +' without Projection');
                                        $scope.import.allowSubmit = false;
                                        //TODO div select EPSG
                                        $scope.import.enableSelectEPSGCode = true;
                                        provider.getAllCodeEPSG({ id: $scope.import.providerId},
                                            function(response){//success
                                                $scope.epsgList = response.list;
                                                $scope.import.fileName = $scope.import.providerId;

                                            },
                                            function(){//error
                                                Growl('error','Error','Impossible to get all EPSG code');
                                            }
                                        );
                                    }
                                );
                            }, function(){//failure for provider.create
                                Growl('error','Error','Impossible to create dataSet');
                            });
                        } else if ($scope.import.uploadType === "raster") {
                            provider.create({id: $scope.import.providerId}, {
                                type: "coverage-store",
                                subType: "coverage-file",
                                parameters: {
                                    path: importedData
                                }
                            }, function() {//success
                                provider.verifyCRS({ id: $scope.import.providerId},
                                    function() {//success
                                        if (importedMetaData) {
                                            dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId,
                                                                                'mdPath': importedMetaData}});
                                        }
                                        //update data & metadata files reminder for further use
                                        UploadFiles.files = {file: $scope.import.providerId,
                                                             mdFile: importedMetaData,
                                                             providerId: $scope.import.providerId};
                                        if (!fileExtension || fileExtension !== "nc") {
                                            Growl('success','Success','Coverage data '+ $scope.import.providerId +' successfully added');
                                            $modalInstance.close({type: $scope.import.uploadType,
                                                                  file: $scope.import.providerId});
                                        } else {
                                            $scope.showAssociate();
                                            // todo: displayNetCDF(fileName);
                                        }
                                    }, function() {//failure
                                        Growl('error','Error','Data '+ $scope.import.providerId +' without Projection');
                                        $scope.import.allowSubmit = false;
                                        //TODO div select EPSG
                                        $scope.import.enableSelectEPSGCode = true;
                                        provider.getAllCodeEPSG({id: $scope.import.providerId},
                                            function(response){//success
                                                $scope.epsgList = response.list;
                                                $scope.import.fileName = $scope.import.providerId;
                                            },
                                            function(){//error
                                                Growl('error','Error','Impossible to get all EPSG code');
                                            });
                                        //provider.delete({id: fileName});
                                    }
                                );
                            } , function(){//failure of provider.create
                                Growl('error','Error','Impossible to create dataSet');
                            });
                        } else if ($scope.import.uploadType === "observation" && fileExtension === "xml") {
                            provider.create({id: $scope.import.providerId},{
                                type: "observation-store",
                                subType: "observation-xml",
                                parameters: {
                                    path: importedData
                                }
                            }, function() {//success
                                if (importedMetaData) {
                                    dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId,
                                                                        'mdPath': importedMetaData}});
                                }
                                Growl('success','Success','Observation data '+ fileName +' successfully added');
                                $scope.showAssociate();
                            }, function(){//failure
                                Growl('error','Error','Impossible to create dataSet');
                            });
                        } else if ($scope.import.uploadType === "observation") {
                            provider.create({
                                id: $scope.import.providerId
                            }, {
                                type: "observation-store",
                                subType: "observation-file",
                                parameters: {
                                    path: importedData
                                }
                            }, function() {//success
                                if (importedMetaData) {
                                    dataListing.setUpMetadata({values: {'providerId': $scope.import.providerId,
                                                                        'mdPath': importedMetaData}});
                                }
                                Growl('success','Success','Observation data '+ fileName +' successfully added');
                                $scope.showAssociate();
                            }, function(){//failure
                                Growl('error','Error','Impossible to create dataSet');
                            });
                        } else {
                            Growl('warning','Warning','Not implemented choice');
                            $modalInstance.close();
                        }
                    });
                });
            } else {
                Growl('error','Error','Data import failed');
                $modalInstance.close();
            }
        };

        $scope.import.finish = function() {
            if ($scope.import.uploadType !== null ) {
                $scope.uploaded();
            } else {
                Growl('error','Error','Select Data Type');
            }
        };

        $scope.addProjection = function (){
            console.log($scope.import.epsgSelected);
            var codeEpsg = $scope.import.epsgSelected.substr(0,$scope.import.epsgSelected.indexOf(" "));
            provider.createPRJ({id: $scope.import.fileName },{codeEpsg: codeEpsg},
                function(){//success
                    UploadFiles.files = {
                        file: $scope.import.providerId,
                        mdFile: $scope.import.metadata,
                        providerId: $scope.import.providerId
                    };
                    $modalInstance.close({type: $scope.import.uploadType,
                                          file: $scope.import.providerId});
                },
                function(){//error
                    Growl('error','Error','Impossible to set projection for data '+ $scope.import.fileName );
                }
            );
        };
    })

    .controller('ModalImportDataStep1LocalController', function($scope, dataListing, $cookies, Growl) {
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
                success: function (response) {
                    $scope.$apply(function() {
                        $scope.import.dataPath = response.dataPath;
                        $scope.loader.upload = false;
                        $scope.import.currentStep = 'step2Metadata';
                        $scope.import.allowNext = true;
                    });
                },
                error: function(){
                    Growl('error', 'Error', 'error while uploading data');
                }
            });
        };

        $scope.verifyExtension = function(path) {
            var lastPointIndex = path.lastIndexOf(".");
            var extension = path.substring(lastPointIndex+1, path.length);
            dataListing.extension({}, {value: extension},
                function(response) {//success
                    if (response.dataType!=="") {
                        $scope.import.uploadType = response.dataType;
                    }
                    $scope.import.allowNext = true;
                }
            );
        };
    })

    .controller('ModalImportDataStep1ServerController', function($scope, dataListing) {
        $scope.import.allowNext = false;
        $scope.import.fsserver = true;

        $scope.columns = [];

        $scope.load = function(){
            $scope.import.allowNext = false;
            var path = $scope.currentPath;
            dataListing.dataFolder({}, path, function(files) {
                $scope.currentPath = files[0].parentPath;
                $scope.columns = files;
            });
        };

        $scope.open = function(path, depth) {
            $scope.load(path);
        };

        $scope.select = function(item) {
            $scope.currentPath = item.path;
            if (!item.folder) {
                $scope.import.allowNext = true;
            }else{
                $scope.load();
            }

        };

        $scope.startWith = function(path) {
            return $scope.currentPath.indexOf(path) === 0;
        };


        $scope.import.next = function() {
            var lastPointIndex = $scope.currentPath.lastIndexOf(".");
            var extension = $scope.currentPath.substring(lastPointIndex+1, $scope.currentPath.length);
            dataListing.extension({}, {value: extension},
                function(response) {//success
                    if (response.dataType!=="") {
                        $scope.import.uploadType = response.dataType;
                    }
                });

            // Use selected data
            $scope.import.dataPath = $scope.currentPath;
            $scope.import.currentStep = 'step2Metadata';
            $scope.import.allowNext = true;
        };
    })

    .controller('ModalImportDataStep1DatabaseController', function($scope, provider, Growl) {
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
            }, function(response) {//success
                Growl('success', 'Success', 'Connected to database');
                $scope.import.testConnected = true;
                $scope.import.allowNext = true;

            },function(response){//error
                Growl('error','Error','Unable to connect database. Verify parrameters');
            });
        };

})

    .controller('ModalImportDataStep2MetadataController', function($scope, $cookies, Growl, dataListing) {

        $scope.columns = [];

        $scope.load = function(){
            $scope.import.allowNext = false;
            var path = $scope.currentPath;
            dataListing.metadataFolder({}, path, function(files) {//success
                $scope.currentPath = files[0].parentPath;
                $scope.columns = files;
            });
        };

        $scope.open = function(path, depth) {
            $scope.load(path);
        };

        $scope.select = function(item) {

            if (!item.folder) {
                $scope.import.metadata = item.path;
                $scope.currentPath = item.path;
                $scope.import.identifier = null;
                $scope.verifyAllowNext();
            }else{
                $scope.currentPath = item.path;
                $scope.load();
            }

        };

        $scope.startWith = function(path) {
            return $scope.currentPath.indexOf(path) === 0;
        };

        $scope.import.allowNext = false;
        if ($scope.import.dataPath !== null && $scope.import.dataPath.length > 0){
            $scope.import.identifier = $scope.import.dataPath.replace(/^.*(\\|\/|\:)/, '').substr(0,$scope.import.dataPath.replace(/^.*(\\|\/|\:)/, '').lastIndexOf('.'));
        }
        if ($scope.import.identifier !== null && $scope.import.identifier.length > 0) {
            $scope.import.allowNext = true;
        }

        $scope.verifyAllowNext = function(){
                if (($scope.import.identifier !== null && $scope.import.identifier.length > 0) ) {
                    var letters = /^[A-Za-zàèìòùáéíóúäëïöüñãõåæøâêîôû0-9\-_]+$/;
                    var id = $scope.import.identifier;
                    if(!id.match(letters)) {
                        $scope.import.allowNext = false;
                        Growl('error','Error','fill identifier without special chars like space');
                    }else {
                        $scope.import.allowNext = true;
                    }
                }else if ($scope.import.metadata  !== null && $scope.import.metadata.length > 0) {
                    $scope.import.allowNext = true;
                } else {
                    $scope.import.allowNext = false;
                }
        };

        $scope.import.next = function() {
            if ($scope.import.metadata !== null || $scope.import.identifier !== null) {
                $scope.uploadMetadata();
            } else {
                $scope.selectType();
            }

        };

        $scope.selectType = function(){
            $scope.import.allowNext = false;
            if ($scope.import.db.url) {
                $scope.importDb();
            } else if ($scope.import.uploadType === null) {
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
                    Growl('error','Error',result.responseJSON.msg);
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
                if ($scope.import.metadata !== null && $scope.import.metadata.length > 0){
                    $scope.import.allowNext = true;
                }
            });
        };

    })

    .controller('ModalImportDataStep3TypeController', function($scope) {
        $scope.changeAssociationState = function() {
            if ($scope.sensor.checked) {
                $scope.import.allowSensorChoose = true;
                $scope.import.allowSubmit = false;
            } else {
                $scope.import.allowSensorChoose = false;
                $scope.import.allowSubmit = true;
            }
        };
    })

    .controller('ModalImportDataStep4SensorController', function($scope, sensor, dataListing, Dashboard, Growl, $cookies) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

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
                Dashboard($scope, response.children, false);
                $scope.wrap.nbbypage = 5;
            });
        };

        $scope.import.finish = function() {
            if ($scope.sensor.mode === 'existing') {
                var sensorId = ($scope.selectedSensorsChild) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                dataListing.listDataForProv({providerId: $scope.import.providerId},function(response){//success
                    for (var i=0; i<response.length; i++) {
                        dataListing.linkToSensor({providerId: response[i].Provider,
                                                  dataId: response[i].Name,
                                                  sensorId: sensorId},
                                                 {value: response[i].Namespace});
                    }
                });
            } else if ($scope.sensor.mode === 'automatic') {
                dataListing.listDataForProv({providerId: $scope.import.providerId}, function(response){//success
                    for (var i=0; i<response.length; i++) {
                        dataListing.generateSML({}, {values: {
                                                        'providerId': response[i].Provider,
                                                        'dataId': '{' + response[i].Namespace + '}' + response[i].Name}});
                    }
                });
            } else {
                // Import sensorML
                $scope.uploadImportAndLinkSensor();
            }

            $scope.close();
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
                    importAndLinkSensor(path.dataPath);
                }
            });
        };

        //success
        function linkSensorImported(sensorId, response) {
            for (var i=0; i<response.length; i++) {
                dataListing.linkToSensor({providerId: response[i].Provider,
                    dataId: response[i].Name,
                    sensorId: sensorId}, {value: response[i].Namespace});
            }
        }

        function importAndLinkSensor(path) {
            sensor.add({}, {values: {'path' : path}}, function(sensors) {
                Growl('success','Success','Sensor correctly imported');
                for (var s=0; s<sensors.length; s++) {
                    var sensorId = sensors[s].identifier;
                    dataListing.listDataForProv({providerId: $scope.import.providerId},linkSensorImported(sensorId));
                }
            }, function() {
                Growl('error','Error','Unable to import sensor');
            });
        }
    })

    .controller('ModalImportDataStep4SNetcdfController', function($scope, dataListing, Growl, $cookies) {
        function displayNetCDF(providerId) {
            $scope.import.currentStep = 'step4Netcdf';
            $scope.import.allowNext = false;
            $scope.import.allowSensorChoose = false;
            $scope.import.allowSubmit = false;

            $scope.coveragesData = dataListing.listCoverage({}, {value: providerId},
                function(response) {//success
                    for (var key in response.values) {
                        if(response.values.hasOwnProperty(key)){
                            displayLayer(response.values[key]);
                            break;
                        }
                    }
            });
        }

        function displayLayer(layer) {
            $scope.import.layer = layer;
            var layerData = DataViewer.createLayer($cookies.cstlUrl, layer, $scope.import.providerId);
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataPreviewMap');
        }
    });