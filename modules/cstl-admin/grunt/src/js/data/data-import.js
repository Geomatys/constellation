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

angular.module('cstl-data-import', ['cstl-restapi', 'cstl-services', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .controller('ModalImportDataController', function($scope, $modalInstance, dataListing, datasetListing, provider, firstStep, importType, UploadFiles, Growl) {
        $scope.import = {
            importType: importType,
            currentStep: firstStep,
            dataPath: null,
            mdPath: null,
            fillMetadata:false,
            uploadType: null,
            allowNext: true,
            allowSubmit: false,
            allowSensorChoose: false,
            next: angular.noop,
            finish: angular.noop,
            metadata: null,
            providerId: null,
            layer: null,
            db: {},
            currentPath:null,
            currentMDPath:null
        };
        $scope.enableSelectEPSGCode = false;

        $scope.sensor = {
            mode : "existing",
            checked : false
        };

        $scope.close = function() {
            $modalInstance.close({type: $scope.import.uploadType,
                                  file: $scope.import.providerId,
                                  metadataFile:$scope.import.metadata,
                                  completeMetadata:$scope.import.fillMetadata});
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
                                      file: $scope.import.identifier,
                                      metadataFile:$scope.import.metadata,
                                      completeMetadata:$scope.import.fillMetadata});
            });
        };

        $scope.uploaded = function() {
            if ($scope.import.importType === 'empty' && $scope.import.dataName) {
                //empty dataset case
                datasetListing.createDataset({values: {"datasetIdentifier":$scope.import.dataName}},function(response){//success
                    Growl('success','Success','Data set '+ $scope.import.dataName +' successfully created');
                    $modalInstance.close({file: $scope.import.dataName,
                                          type: "import"});
                },function(response){//error
                    Growl('error','Error','Fail to create dataset '+ $scope.import.dataName);
                    $modalInstance.close();
                });

            } else if ($scope.import.dataPath && $scope.import.dataPath.indexOf('failed') === -1) {
                // dataset creation with data
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
                dataListing.importDataFull({values: {"dataPath": $scope.import.dataPath,
                                                 "metadataFilePath": $scope.import.mdPath,
                                                 "dataType": $scope.import.uploadType,
                                                 "dataName": $scope.import.dataName,
                                                 "extension": fileExtension,
                                                 "fsServer": $scope.import.fsserver }},
                    function(response) {//success
                        if(response){
                            var importedMetaData = response.metadataFile;
                            $scope.import.metadata = importedMetaData;
                            $scope.import.providerId = $scope.import.dataName;
                            $scope.import.uploadType = response.dataType;
                            if ($scope.import.uploadType === "vector") {
                                if('success' === response.verifyCRS){
                                    UploadFiles.files = {
                                        file: $scope.import.providerId,
                                        mdFile: importedMetaData,
                                        providerId: $scope.import.providerId
                                    };
                                    Growl('success','Success','Vector data '+ $scope.import.providerId +' successfully added');
                                    if ($scope.sensor.checked) {
                                        $scope.showAssociate();
                                    } else {
                                        $modalInstance.close({type: $scope.import.uploadType,
                                                              file: $scope.import.providerId,
                                                              metadataFile:$scope.import.metadata,
                                                              completeMetadata:$scope.import.fillMetadata});
                                    }
                                }else if('error' === response.verifyCRS) {
                                    Growl('warning','CRS','Data '+ $scope.import.providerId +' without Projection');
                                    $scope.import.allowSubmit = false;
                                    $scope.import.enableSelectEPSGCode = true;
                                    if(response.codes){
                                        $scope.epsgList = response.codes;
                                        $scope.import.fileName = $scope.import.providerId;
                                    }else {
                                        Growl('error','Error','Impossible to get all EPSG codes');
                                    }
                                }
                            }else if ($scope.import.uploadType === "raster") {
                                if('success' === response.verifyCRS){
                                    UploadFiles.files = {
                                        file: $scope.import.providerId,
                                        mdFile: importedMetaData,
                                        providerId: $scope.import.providerId
                                    };
                                    if (!fileExtension || fileExtension !== "nc") {
                                        Growl('success','Success','Raster data '+ $scope.import.providerId +' successfully added');
                                        $modalInstance.close({type: $scope.import.uploadType,
                                                              file: $scope.import.providerId,
                                                              metadataFile:$scope.import.metadata,
                                                              completeMetadata:$scope.import.fillMetadata});
                                    } else {
                                        $scope.showAssociate();
                                    }
                                }else if('error' === response.verifyCRS) {
                                    Growl('warning','CRS','Data '+ $scope.import.providerId +' without Projection');
                                    $scope.import.allowSubmit = false;
                                    $scope.import.enableSelectEPSGCode = true;
                                    if(response.codes){
                                        $scope.epsgList = response.codes;
                                        $scope.import.fileName = $scope.import.providerId;
                                    } else {
                                        Growl('error','Error','Impossible to get all EPSG codes');
                                    }
                                }
                            }else if ($scope.import.uploadType === "observation" && fileExtension === "xml") {
                                Growl('success','Success','Observation data '+ fileName +' successfully added');
                                $scope.showAssociate();
                            } else if ($scope.import.uploadType === "observation") {
                                Growl('success','Success','Observation data '+ fileName +' successfully added');
                                $scope.showAssociate();
                            }
                        }
                    },function(response){//error
                        Growl('error','Error','Data import failed');
                        $modalInstance.close();
                });
            } else {
                Growl('error','Error','Data import failed');
                $modalInstance.close();
            }
        };

        $scope.import.finish = function() {
            if ($scope.import.uploadType || $scope.import.importType === 'empty') {
                $scope.uploaded();
            } else {
                Growl('error','Error','Select Data Type');
            }
        };

        $scope.addProjection = function (){
            var codeEpsg = $scope.import.epsgSelected.trim();
            if(codeEpsg.indexOf(' ')!== -1){
                codeEpsg = codeEpsg.substring(0,codeEpsg.indexOf(' '));
            }
            provider.createPRJ({id: $scope.import.fileName },{codeEpsg: codeEpsg},
                function(){//success
                    UploadFiles.files = {
                        file: $scope.import.providerId,
                        mdFile: $scope.import.metadata,
                        providerId: $scope.import.providerId
                    };
                    $modalInstance.close({type: $scope.import.uploadType,
                                          file: $scope.import.providerId,
                                          metadataFile:$scope.import.metadata,
                                          completeMetadata:$scope.import.fillMetadata});
                },
                function(response){//error
                    var msgError = '';
                    if(response && response.data && response.data.message) {
                        msgError = response.data.message;
                    }
                    Growl('error','Error','Impossible to set projection : '+msgError);
                }
            );
        };
    })

    .controller('ModalImportDataStep1LocalController', function($rootScope, $scope, dataListing, $cookieStore, Growl, cfpLoadingBar) {
        $scope.loader = {
            upload: false
        };

        $scope.import.allowNext = false;
        $scope.import.next = function() {
            $scope.uploadData();
        };

        $scope.uploadData = function() {
            var $form = $('#uploadDataForm');
            var formData = new FormData($form[0]);
            $scope.loader.upload = true;
            $.ajax({
                headers: {
                  'X-Auth-Token': $rootScope.authToken
                },
                url: $cookieStore.get('cstlUrl') + "api/1/domain/"+ $cookieStore.get('cstlActiveDomainId') + "/data/upload/data",
                type: 'POST',
                data: formData,
                cache: false,
                contentType: false,
                processData: false,
                beforeSend: function(){
                    cfpLoadingBar.start();
                    cfpLoadingBar.inc();
                },
                success: function (response) {
                    $scope.$apply(function() {
                        $scope.import.dataPath = response.dataPath;
                        $scope.loader.upload = false;
                        $scope.import.currentStep = 'step2Metadata';
                        $scope.import.allowNext = true;
                        cfpLoadingBar.complete();
                    });
                },
                error: function(){
                    Growl('error', 'Error', 'error while uploading data');
                    cfpLoadingBar.complete();
                }
            });
        };

        $scope.verifyExtension = function(path) {
            var lastPointIndex = path.lastIndexOf(".");
            var extension = path.substring(lastPointIndex+1, path.length);
            dataListing.extension({}, {value: extension},
                function(response) {//success
                    if (response.dataType) {
                        $scope.import.uploadType = response.dataType;
                    }
                    $scope.import.allowNext = true;
                }
            );
        };
    })

    .controller('ModalImportDataStep1ServerController', function($scope, dataListing, Growl) {
        $scope.import.allowNext = false;
        $scope.import.fsserver = true;

        $scope.columns = [];

        $scope.load = function(){
            $scope.import.allowNext = false;
            var path = $scope.import.currentPath;
            dataListing.dataFolder({}, path,
                function(files) {
                    if(files.length>0) {
                        $scope.import.currentPath = files[0].parentPath;
                    }
                    $scope.columns = files;
                },
                function(resp){//error
                    var msg = 'The file path is invalid';
                    if(resp.data && resp.data.msg){
                        msg = resp.data.msg;
                    }
                    Growl('error','Error',msg);
                }
            );
        };

        $scope.open = function(path, depth) {
            $scope.load(path);
        };

        $scope.select = function(item) {
            $scope.import.currentPath = item.path;
            if (!item.folder) {
                $scope.import.allowNext = true;
            }else{
                $scope.load();
            }

        };

        $scope.startWith = function(path) {
            return $scope.import.currentPath.indexOf(path) === 0;
        };


        $scope.import.next = function() {
            var lastPointIndex = $scope.import.currentPath.lastIndexOf(".");
            var extension = $scope.import.currentPath.substring(lastPointIndex+1, $scope.import.currentPath.length);
            dataListing.extension({}, {value: extension},
                function(response) {//success
                    if (response.dataType) {
                        $scope.import.uploadType = response.dataType;
                    }
                });

            // Use selected data
            $scope.import.dataPath = $scope.import.currentPath;
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
                Growl('error','Error','Unable to connect database. Verify parameters');
            });
        };

})

    .controller('ModalImportDataStep2MetadataController', function($rootScope, $scope, $cookieStore, Growl, dataListing, cfpLoadingBar) {

        $scope.columns = [];

        $scope.load = function(){
            $scope.import.allowNext = false;
            $scope.import.allowSubmit = false;
            var path = $scope.import.currentMDPath;
            dataListing.metadataFolder({}, path,
                function(files) {//success
                    if(files.length>0) {
                        $scope.import.currentMDPath = files[0].parentPath;
                    }
                    $scope.columns = files;
                },
                function(resp){//error
                    var msg = 'The file path is invalid';
                    if(resp.data && resp.data.msg){
                        msg = resp.data.msg;
                    }
                    Growl('error','Error',msg);
                });
        };

        $scope.open = function(path, depth) {
            $scope.load(path);
        };

        $scope.select = function(item) {

            if (!item.folder) {
                $scope.import.metadata = item.path;
                $scope.import.currentMDPath = item.path;
                $scope.import.identifier = null;
                $scope.verifyAllowNext();
            }else{
                $scope.import.currentMDPath = item.path;
                $scope.load();
            }

        };

        $scope.startWith = function(path) {
            return $scope.import.currentMDPath.indexOf(path) === 0;
        };

        $scope.import.allowNext = false;
        if ($scope.import.dataPath && $scope.import.dataPath.length > 0){
            $scope.import.identifier = $scope.import.dataPath.replace(/^.*(\\|\/|\:)/, '').substr(0,$scope.import.dataPath.replace(/^.*(\\|\/|\:)/, '').lastIndexOf('.'));
        }
        if ($scope.import.identifier && $scope.import.identifier.length > 0) {

            //final step if empty dataset creation is selected
            if ($scope.import.importType === 'empty') {
                $scope.import.allowSubmit = true;
            } else {
                $scope.import.allowNext = true;
            }
        }

        $scope.verifyAllowNext = function(){
                $scope.import.allowNext = false;
                $scope.import.allowSubmit = false;
                if (($scope.import.identifier && $scope.import.identifier.length > 0) ) {
                    var letters = /^[A-Za-zàèìòùáéíóúäëïöüñãõåæøâêîôû0-9\-_]+$/;
                    var id = $scope.import.identifier;
                    if(!id.match(letters)) {
                        Growl('error','Error','fill identifier without special chars like space');
                    }else {
                        if ($scope.import.importType === 'empty') {
                            $scope.import.allowSubmit = true;
                        } else {
                            $scope.import.allowNext = true;
                        }
                    }
                }else if ($scope.import.metadata && $scope.import.metadata.length > 0) {
                    $scope.import.allowSubmit = true;
                }
        };

        $scope.import.next = function() {
            if ($scope.import.metadata || $scope.import.identifier) {
                $scope.uploadMetadata();
            } else {
                $scope.selectType();
            }
        };

        $scope.import.finish = function() {

            var finishUpload = false;
            if ($scope.import.importType === 'empty') {
                if ($scope.import.metadata || $scope.import.identifier) {
                    $scope.uploadMetadata();
                } else {
                    finishUpload = true;
                }
            } else {
                if ($scope.import.uploadType) {
                    finishUpload = true;
                } else {
                    Growl('error', 'Error', 'Select Data Type');
                }
            }

            if (finishUpload) {
                $scope.uploaded();
            }
        };

        $scope.selectType = function(){
            $scope.import.allowNext = false;
            if ($scope.import.db.url) {
                $scope.importDb();
            } else if ($scope.import.importType === 'empty') {
                //skip datatype fragment when we're on empty dataset creation.
                $scope.uploaded();
            } else if (!$scope.import.uploadType) {
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
                headers: {
                  'X-Auth-Token': $rootScope.authToken
                },
                url: $cookieStore.get('cstlUrl') + "api/1/domain/"+ $cookieStore.get('cstlActiveDomainId') + '/data/upload/metadata',
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                beforeSend: function(){
                    cfpLoadingBar.start();
                    cfpLoadingBar.inc();
                },
                success: function(result) {
                    $scope.import.mdPath = result.metadataPath;
                    $scope.import.dataName = result.dataName;
                    $scope.import.dataTitle = result.metatitle;
                    $scope.import.metaIdentifier = result.metaIdentifier;
                    $scope.selectType();
                    cfpLoadingBar.complete();
                },
                error: function(result){
                    Growl('error','Error',result.responseJSON.msg);
                    cfpLoadingBar.complete();
                }
            });
        };

        $scope.metadataChosen = function(md) {
            $scope.$apply(function() {
                $scope.import.metadata = md.value;
                $scope.import.identifier = null;
                if ($scope.import.metadata && $scope.import.metadata.length > 0){
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

    .controller('ModalImportDataStep4SensorController', function($rootScope, $scope, sensor, dataListing, Dashboard, Growl, $cookieStore, cfpLoadingBar) {
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
                headers: {
                  'X-Auth-Token': $rootScope.authToken
                },
                url: $cookieStore.get('cstlUrl') + "api/1/domain/"+ $cookieStore.get('cstlActiveDomainId') + '/data/upload/data',
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                beforeSend: function(){
                    cfpLoadingBar.start();
                    cfpLoadingBar.inc();
                },
                success: function (path) {
                    importAndLinkSensor(path.dataPath);
                    cfpLoadingBar.complete();
                },
                error: function (data){
                    Growl('error','Error','Unable to upload sensor');
                    cfpLoadingBar.complete();
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
    });