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

ZeroClipboard.config({
	  moviePath: "scripts/zeroclipboard/ZeroClipboard.swf"
	} );

/* Controllers */

cstlAdminApp.controller('HeaderController', ['$scope','$http',
                                           function ($scope, $http) {
	                                         $http.get("app/conf").success(function(data){
	                                        	 $scope.cstlLoginUrl = data.cstl + "spring/auth/form";
	                                        	 $scope.cstlLogoutUrl = data.cstl + "logout";
	                                        });
                                           }]);

cstlAdminApp.controller('MainController', ['$scope','$location',
    function ($scope, $location) {
    }]);

cstlAdminApp.controller('LanguageController', ['$scope', '$translate',
    function ($scope, $translate) {
        $scope.changeLanguage = function (languageKey) {
            $translate.uses(languageKey);
        };
    }]);

cstlAdminApp.controller('MenuController', ['$scope',
    function ($scope) {


}]);

cstlAdminApp.controller('LoginController', ['$scope', '$location', 'AuthenticationSharedService',
    function ($scope, $location, AuthenticationSharedService) {
        $scope.rememberMe = true;
        $scope.login = function () {
            AuthenticationSharedService.login({
                username: $scope.username,
                password: $scope.password,
                rememberMe: $scope.rememberMe,
                success: function () {
                    $location.path('');
                }
            });
        };
    }]);

cstlAdminApp.controller('LogoutController', ['$location', 'AuthenticationSharedService',
    function ($location, AuthenticationSharedService) {
        AuthenticationSharedService.logout({
            success: function () {
                $location.path('');
            }
        });
    }]);


cstlAdminApp.controller('ContactController', ['$scope', 'Contact',
                                               function ($scope, Contact) {
	$scope.data = Contact.get();
    $scope.save = function () {
       Contact.save($scope.data,
           function (value, responseHeaders) {
              $scope.error = null;
              $scope.success = 'OK';
              $scope.data = Contact.get();
           },
       function (httpResponse) {
           $scope.success = null;
           $scope.error = "ERROR";
       });
    };
}]);

cstlAdminApp.controller('SettingsController', ['$scope', 'resolvedAccount', 'Account',
    function ($scope, resolvedAccount, Account) {
        $scope.success = null;
        $scope.error = null;
        $scope.settingsAccount = resolvedAccount;

        $scope.save = function () {
            Account.save($scope.settingsAccount,
                function (value, responseHeaders) {
                    $scope.error = null;
                    $scope.success = 'OK';
                    $scope.settingsAccount = Account.get();
                },
                function (httpResponse) {
                    $scope.success = null;
                    $scope.error = "ERROR";
                });
        };
    }]);

cstlAdminApp.controller('UserController', ['$scope', 'UserResource', '$modal',
  function ($scope, UserResource, $modal) {
    $scope.list = UserResource.query();
    $scope.details = function(i) {
        $modal.open({
            templateUrl: 'views/user/details.html',
            controller: 'UserDetailsController',
            resolve: {
                'user': function(){
                    return angular.copy($scope.list[i]);
                }
            }
        }).result.then(function(user){
        	if(user != null)
        	$scope.list[i] = user;
        });
    };
    $scope.add = function(i) {
        $modal.open({
            templateUrl: 'views/user/add.html',
            controller: 'UserDetailsController',
            resolve: {
                'user': function(){
                    return {roles:[]};
                }
            }
        }).result.then(function(user){
        	if(user != null)
        	$scope.list[$scope.list.length] = user;
        });
    };
    $scope.delete = function(i){
    	UserResource.delete({id: $scope.list[i].login});
    	$scope.list.splice(i, 1);
    };
}]);

cstlAdminApp.controller('UserDetailsController', ['$scope', '$modalInstance', 'user', 'UserResource',
  function ($scope, $modalInstance, user, UserResource) {
    $scope.user = user;

    $scope.close = function() {
        $modalInstance.dismiss('close');
    };
    $scope.deleteTag = function(role){
        var newRoles = [];
        for(var i=0; i<user.roles.length; i++)
           if(user.roles[i] != role)
               newRoles[newRoles.length] = user.roles[i];
        user.roles = newRoles;
    };

    $scope.addRole = function(role){
    	for(var i=0; i < $scope.user.roles.length; i++)
    	   if(role === $scope.user.roles[i])
    		   return

    	$scope.user.roles[$scope.user.roles.length]=role
    };

    $scope.save = function(){
    	var userResource = new UserResource($scope.user);
    	userResource.$save();
    	$modalInstance.close($scope.user);
    }
}]);


cstlAdminApp.controller('ProcessController', ['$scope', 'ProcessService',
                                              function ($scope, Process) {
                                         	   $scope.oneAtATime=true;
                                                $scope.registries = Process.get();
                                                $scope.groups = [
                                                                 {
                                                                   title: "Dynamic Group Header - 1",
                                                                   content: "Dynamic Group Body - 1"
                                                                 },
                                                                 {
                                                                   title: "Dynamic Group Header - 2",
                                                                   content: "Dynamic Group Body - 2"
                                                                 }
                                                               ];

                                                               $scope.items = ['Item 1', 'Item 2', 'Item 3'];

                                                               $scope.addItem = function() {
                                                                 var newItemNo = $scope.items.length + 1;
                                                                 $scope.items.push('Item ' + newItemNo);
                                                               };
                                         }]);

cstlAdminApp.controller('SessionsController', ['$scope', 'resolvedSessions', 'Sessions',
    function ($scope, resolvedSessions, Sessions) {
        $scope.success = null;
        $scope.error = null;
        $scope.sessions = resolvedSessions;
        $scope.invalidate = function (series) {
            Sessions.delete({series: encodeURIComponent(series)},
                function (value, responseHeaders) {
                    $scope.error = null;
                    $scope.success = "OK";
                    $scope.sessions = Sessions.get();
                },
                function (httpResponse) {
                    $scope.success = null;
                    $scope.error = "ERROR";
                });
        };
    }]);

cstlAdminApp.controller('MetricsController', ['$scope', 'resolvedMetrics','Metrics','$window', '$http',
    function ($scope, resolvedMetrics,Metrics, $window, $http) {
        $scope.metrics = resolvedMetrics;
        $scope.init = function(){
        	$scope.metrics= Metrics.get()
        };
        $scope.rungc = function(){
        	$http.get("@cstl/spring/admin/jvm/rungc;jsessionid=").then(function(){
        		$scope.metrics= Metrics.get()
        	});
        };
    }]);

cstlAdminApp.controller('LogsController', ['$scope', 'resolvedLogs', 'LogsService',
    function ($scope, resolvedLogs, LogsService) {
        $scope.loggers = resolvedLogs;

        $scope.changeLevel = function (name, level) {
            LogsService.changeLevel({name: name, level: level}, function () {
                $scope.loggers = LogsService.findAll();
            });
        };
    }]);

cstlAdminApp.controller('DataController', ['$scope', '$location', '$dashboard', 'webService', 'dataListing', 'provider', 'style', '$modal', '$growl', 'StyleSharedService', '$cookies',
    function ($scope, $location, $dashboard, webService, dataListing, provider, style, $modal, $growl, StyleSharedService, $cookies) {

        $scope.filtertype = "";

        dataListing.listAll({}, function(response) {
            $dashboard($scope, response, true);
        });

        // Map methods
        $scope.showData = function() {
            $('#viewerData').modal("show");
            var layerName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            var layerData;
            if ($scope.selected.TargetStyle && $scope.selected.TargetStyle.length > 0) {
                layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, layerName, providerId, $scope.selected.TargetStyle[0].Name);
            } else {
                layerData = DataViewer.createLayer($cookies.cstlUrl, layerName, providerId);
            }
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];

            dataListing.metadata({providerId: providerId, dataId: layerName}, {}, function(response) {
                // Success getting the metadata, try to find the data extent
                DataViewer.initMap('dataMap');
                var ident = response['gmd.MD_Metadata']['gmd.identificationInfo'];
                if (ident) {
                    var extentMD = ident['gmd.MD_DataIdentification']['gmd.extent'];
                    if (extentMD) {
                        var bbox = extentMD['gmd.EX_Extent']['gmd.geographicElement']['gmd.EX_GeographicBoundingBox'];
                        var extent = new OpenLayers.Bounds(bbox['gmd.westBoundLongitude']['gco.Decimal'], bbox['gmd.southBoundLatitude']['gco.Decimal'],
                                                           bbox['gmd.eastBoundLongitude']['gco.Decimal'], bbox['gmd.northBoundLatitude']['gco.Decimal']);
                        DataViewer.map.zoomToExtent(extent, true);
                    }
                }
            }, function() {
                // failed to find a metadata, just load the full map
                DataViewer.initMap('dataMap');
            });
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

                dataListing.deleteData({providerid: providerId, dataid: layerName}, {},
                    function() { $growl('success','Success','Data '+ layerName +' successfully deleted');
                        dataListing.listDataForProv({providerId: providerId}, function(response) {
                            if (response.length == 0) {
                                provider.delete({id: providerId}, function() {
                                    dataListing.listAll({}, function(response) {
                                        $scope.fullList = response;
                                    });
                                });
                            } else {
                                dataListing.listAll({}, function(response) {
                                    $scope.fullList = response;
                                });
                            }
                        });
                    },
                    function() { $growl('error','Error','Data '+ layerName +' deletion failed'); }
                );
            }
        };




        // Style methods
        $scope.showStyleList = function() {
            StyleSharedService.showStyleList($scope);
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style);
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#dataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        // Data loading
        $scope.showLocalFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalLocalFile.html',
                controller: 'LocalFileModalController'
            });

            modal.result.then(function(result) {
                $location.path('/description/'+ result.type +"/"+ result.file +"/"+ result.missing);
            });
        };

        $scope.showServerFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalServerFile.html',
                controller: 'ServerFileModalController'
            });
        };
    }]);

cstlAdminApp.controller('LocalFileModalController', ['$scope', '$dashboard', '$modalInstance', '$growl', 'provider', 'dataListing', '$uploadFiles', '$cookies',
    function ($scope, $dashboard, $modalInstance, $growl, provider, dataListing, $uploadFiles, $cookies) {
        $scope.layer = null;
        $scope.data = null;
        $scope.providerId = null;
        $scope.metadata = null;
        $scope.uploadType = null;

        // Handle upload workflow
        $scope.step1A = true;
        $scope.step1B = false;
        $scope.step2 = false;
        $scope.step3 = false;
        $scope.allowSubmit = false;
        $scope.allowNext = false;

        $scope.dataPath = null;

        $scope.next = function() {
            if ($scope.step1A === true) {
                $scope.uploadData();
                $scope.step1B = true;
                $scope.allowNext = true;
                $scope.step1A = false;
            } else {
                if ($scope.metadata) {
                    $scope.uploadMetadata();
                }
                $scope.step1B = false;
                $scope.allowNext = false;

                if ($scope.uploadType == null) {
                    $scope.step2 = true;
                    $scope.allowSubmit = true;
                } else {
                    $scope.uploaded();
                }
            }
        };

        $scope.close = function() {
            if ($scope.step3) {
                $modalInstance.close({type: $scope.uploadType, file: $scope.providerId, missing: $scope.metadata == null});
            } else {
                $modalInstance.dismiss('close');
            }
        };

        $scope.verifyExtension = function() {
            var lastPointIndex = $scope.data.lastIndexOf(".");
            var extension = $scope.data.substring(lastPointIndex+1, $scope.data.length);
            dataListing.extension({}, {value: extension},
                function(response) {
                    if (response.dataType!="") {
                        $scope.uploadType = response.dataType;
                    }
                    $scope.allowNext = true;
                });
        };

        $scope.uploadData = function() {
            var $form = $('#uploadDataForm');

            var formData = new FormData($form[0]);

            $.ajax({
                url: $cookies.cstlUrl + "api/1/data/upload/data;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function (returndata) {
                    $scope.dataPath = returndata;
                    $scope.allowNext = true;
                }
            });
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
                processData: false
            });
        };

        $scope.uploaded = function() {
            var message = $scope.dataPath;
            if (message.indexOf('failed') === -1) {
                var files = message.split(',');
                var upFile = files[0];
                var upMdFile = null;
                if (files.length === 2) {
                    upMdFile = files[1];
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

                dataListing.importData({values: {'filePath': upFiles.file, 'metadataFilePath': upFiles.mdFile, dataType: $scope.type}}, function(response) {

                    var importedData = response.dataFile;
                    var importedMetaData = response.metadataFile;

                    // Store the providerId for further calls
                    $scope.providerId = fileName;
                    if ($scope.uploadType === "vector") {
                        provider.create({
                            id: fileName
                        }, {
                            type: "feature-store",
                            subType: "shapefile",
                            parameters: {
                                path: importedData
                            }
                        });
                        $growl('success','Success','Shapefile data '+ fileName +' successfully added');
                        $modalInstance.close({type: "vector", file: fileName, missing: $scope.metadata == null});
                    } else if ($scope.uploadType === "raster") {
                        provider.create({
                            id: fileName
                        }, {
                            type: "coverage-store",
                            subType: "coverage-file",
                            parameters: {
                                path: importedData
                            }
                        }, function() {
                            if (!fileExtension || fileExtension !== "nc") {
                                //dataListing.pyramidData({id: fileName}, {value: upFile}, function() {
                                    $growl('success','Success','Coverage data '+ fileName +' successfully added');
                                    $modalInstance.close({type: "raster", file: fileName, missing: $scope.metadata == null});
                                //});
                            } else {
                                displayNetCDF(fileName);
                            }
//                            if (fileExtension === "nc") {
//                                displayNetCDF(fileName);
//                            }
//                            if (fileExtention === "tif"){
//                                $growl('success','Success','Geotiff data '+ fileName +' successfully added');
//                                $modalInstance.close({type: "raster", file: fileName, missing: $scope.metadata == null});
//                            }
                        });
                    } else {
                        $growl('warning','Warning','Not implemented choice');
                        $modalInstance.close();
                    }

                });

            } else {
                $growl('error','Error','Data import failed');
                $modalInstance.close();
            }
        };

        function displayNetCDF(providerId) {
            $scope.step1A = false;
            $scope.step1B = false;
            $scope.step2 = false;
            $scope.step3 = true;
            $scope.allowNext = false;
            $scope.allowSubmit = false;

            $scope.coveragesData = dataListing.listCoverage({}, {value: providerId}, function(response) {
                for (var key in response.values) {
                    $scope.displayLayer(response.values[key]);
                    break;
                }
            });
        };

        $scope.displayLayer = function(layer) {
            $scope.layer = layer;
            var layerData = DataViewer.createLayer($cookies.cstlUrl, layer, $scope.providerId);
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataPreviewMap');
        };
    }]);

cstlAdminApp.controller('ServerFileModalController', ['$scope', '$dashboard', '$modalInstance', '$growl', 'dataListing', 'provider', '$cookies',
    function ($scope, $dashboard, $modalInstance, $growl, dataListing, provider, $cookies) {
        $scope.columns = [];
        // current path chosen in server data dir
        $scope.currentPath = '/';
        // path of the server data dir
        $scope.prefixPath = '';
        $scope.finished = false;
        $scope.hasSelectedSomething = false;
        $scope.layer = '';
        $scope.chooseType = false;
        $scope.dataType = 'vector';

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

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

        $scope.ok = function() {
            $scope.finished = true;
            $scope.loadData();
        };

        $scope.userChooseType = function() {
            $scope.finished = true;
            $scope.loadDataWithKnownExtension($scope.providerId, $scope.dataType);
        };

        $scope.loadData = function() {
            var file = $scope.currentPath.substring($scope.currentPath.lastIndexOf("/")+1);
            var fileName = file;
            var fileExtension;
            if (file.indexOf(".") !== -1) {
                fileName = file.substring(0, file.lastIndexOf("."));
                fileExtension = file.substring(file.lastIndexOf(".")+1);
            }
            $scope.providerId = fileName;

            // test extension type
            dataListing.extension({}, {value: fileExtension},
                function(response) {
                    $scope.dataType = response.dataType;
                    if ($scope.dataType === "") {
                        $scope.chooseType = true;
                    } else {
                        $scope.loadDataWithKnownExtension(fileName, fileExtension);
                    }
                }, function() {
                    // failure here, impossible to know the extension
                    $scope.chooseType = true;
                }
            );
        };

        $scope.loadDataWithKnownExtension = function(fileName, fileExtension) {
            if ($scope.dataType === "vector") {
                provider.create({
                    id: fileName
                }, {
                    type: "feature-store",
                    subType: "shapefile",
                    parameters: {
                        path: $scope.prefixPath + $scope.currentPath
                    }
                });
                $growl('success','Success','Shapefile data '+ fileName +' successfully added');
                $modalInstance.close();
            } else if ($scope.dataType === "raster") {
                provider.create({
                    id: fileName
                }, {
                    type: "coverage-store",
                    subType: "coverage-file",
                    parameters: {
                        path: $scope.prefixPath + $scope.currentPath
                    }
                }, function() {
                    if (!fileExtension || fileExtension !== "nc") {
                        dataListing.pyramidData({id: fileName}, {value: $scope.prefixPath + $scope.currentPath}, function() {
                            $growl('success','Success','Coverage data '+ fileName +' successfully added');
                            $modalInstance.dismiss('close');
                        });
                    } else {
                        $scope.displayNetCDF(fileName);
                    }
                });
            } else {
                $growl('warning','Warning','Not implemented choice');
                $modalInstance.close();
            }
        };

        $scope.displayNetCDF = function(providerId) {
            $scope.coveragesData = dataListing.listCoverage({}, {value: providerId}, function(response) {
                for (var key in response.values) {
                    $scope.displayLayer(response.values[key]);
                    break;
                }
            });
        };

        $scope.displayLayer = function(layer) {
            $scope.layer = layer;
            var layerData = DataViewer.createLayer($cookies.cstlUrl, layer, $scope.providerId);
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataServerMap');
        };

        $scope.load($scope.currentPath);
    }]);

cstlAdminApp.controller('DescriptionController', ['$scope', '$routeParams','dataListing','$location', '$translate', '$uploadFiles',
    function ($scope, $routeParams, dataListing, $location, $translate, $uploadFiles) {
        $scope.provider = $routeParams.id;
        $scope.missing = $routeParams.missing === 'true';
        $scope.type = $routeParams.type;

        $scope.tabiso = $scope.type==='vector' && $scope.missing;
        $scope.tabcrs = false;
        $scope.tabdesc = $scope.type==='vector' && !$scope.missing;
        $scope.tabimageinfo = $scope.type==='raster';

        $scope.metadata = {};
        $scope.metadata.keywords = [];

        $scope.selectTab = function(item) {
            if (item === 'tabiso') {
                $scope.tabiso = true;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
                $scope.tabimageinfo = false;
            } else if (item === 'tabcrs') {
                $scope.tabiso = false;
                $scope.tabcrs = true;
                $scope.tabdesc = false;
                $scope.tabimageinfo = false;
            } else if (item === 'tabdesc') {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = true;
                $scope.tabimageinfo = false;
            } else {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
                $scope.tabimageinfo = true;
            }
        };

        $scope.addTag = function() {
            if (!$scope.tagText || $scope.tagText == '' || $scope.tagText.length == 0) {
                return;
            }

            $scope.metadata.keywords.push($scope.tagText);
            $scope.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.tagText.length == 0 &&
                key === undefined) {
                $scope.metadata.keywords.pop();
            } else if (key != undefined) {
                $scope.metadata.keywords.splice(key, 1);
            }
        };

        $scope.save = function() {
            $scope.metadata.dataName = $scope.provider;
//            $scope.metadata.dataPath = $uploadFiles.files.file;
            $scope.metadata.type = $scope.type;

            dataListing.setMetadata({}, $scope.metadata,
                function() {
                    $location.path('/data');
                }
            );
        };

        $scope.getCurrentLang = function() {
            return $translate.uses();
        };

        $scope.createMetadataTree = function(parentDivId, isCoverageMetadata){
            var upFile = $uploadFiles.files.file;
            var upMdFile;
            if (upFile) {
                upFile = upFile.substring(upFile.lastIndexOf("/")+1);
                upMdFile = $uploadFiles.files.mdFile;
                if (upMdFile != null) {
                    upMdFile = upMdFile.substring(upMdFile.lastIndexOf("/")+1);
                }
            }
            dataListing.loadData({}, {values: {'filePath': upFile, 'metadataFilePath': upMdFile, dataType: $scope.type}}, function(response) {
                if (isCoverageMetadata) {
                    for (var key in response.coveragesMetadata) {
                        var metadataList = response.coveragesMetadata[key].coverageMetadataTree;
                        generateMetadataTags(metadataList, parentDivId);
                    }
                } else {
                    var metadataList = response.fileMetadata;
                    generateMetadataTags(metadataList, parentDivId);
                }

                $("#"+ parentDivId +" .collapse").collapse('show');
            });

            function generateMetadataTags(metadataList, parentDivId) {
                if (metadataList == null) {
                    return;
                }
                for(var i=0; i<metadataList.length; i++){
                    var key = metadataList[i];
                    var name = key.name;
                    var nameWithoutWhiteSpace = key.nameNoWhiteSpace;
                    var value = key.value;
                    var childrenExist = key.childrenExist;
                    var parentNode = key.parentName;
                    var depthSpan = key.depthSpan;

                    if(childrenExist){
                        //root node
                        if(parentNode === null || parentNode == ''){
                            var htmlElement =   "<a data-toggle='collapse' data-target='#"+nameWithoutWhiteSpace+"Div' class='col-sm-"+depthSpan+"'>"+name+"</a>" +
                                "<div class='collapse col-sm-"+depthSpan+"' id='"+nameWithoutWhiteSpace+"Div'><table id='"+nameWithoutWhiteSpace+"' class='table table-striped'></table></div>";
                            jQuery("#"+ parentDivId).append(htmlElement);
                        }else{
                            var htmlElement =   "<a data-toggle='collapse' data-target='#"+nameWithoutWhiteSpace+"Div' class='col-sm-"+depthSpan+"'>"+name+"</a>" +
                                "<div class='collapse col-sm-"+depthSpan+"' id='"+nameWithoutWhiteSpace+"Div'><table id='"+nameWithoutWhiteSpace+"' class='table table-striped'></table></div>";
                            jQuery("#"+parentNode+"Div").append(htmlElement);
                        }
                    }else{
                        var htmlElement = "<tr><td>"+name+"</td><td>"+value+"</td></tr>";
                        jQuery("#"+parentNode).append(htmlElement);
                    }
                }
            };
        };

        $scope.codeLists = dataListing.codeLists({lang: $scope.getCurrentLang()});
    }]);


cstlAdminApp.controller('WebServiceController', ['$scope', 'webService', 'csw', '$modal', 'textService', '$growl',
    function ($scope, webService, csw, $modal, textService, $growl) {

       $scope.services = webService.listAll();

        // Show Capa methods
        $scope.showCapa = function(service) {
            if (service.versions.length > 1) {
                var modal = $modal.open({
                    templateUrl: 'views/modalChooseVersion.html',
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
                templateUrl: 'views/modalCapa.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.capa(service.type.toLowerCase(), service.identifier, version);
                    }
                }
            });
        };

        // Show Logs methods
        $scope.showLogs = function(service) {

            $modal.open({
                templateUrl: 'views/modalLogs.html',
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
                function() { $growl('success','Success','Service '+ service.name +' successfully reloaded'); },
                function() { $growl('error','Error','Service '+ service.name +' reload failed'); }
            );
        };
        $scope.startOrStop = function(service){
            if(service.status==='WORKING'){
                webService.stop({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.services = webService.listAll();
                        $growl('success','Success','Service '+ service.name +' successfully stopped');
                    }
                }, function() { $growl('error','Error','Service '+ service.name +' stop failed'); });
            }else{
                webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.services = webService.listAll();
                        $growl('success','Success','Service '+ service.name +' successfully started');
                    }
                }, function() { $growl('error','Error','Service '+ service.name +' start failed'); });
            }
        };

        $scope.deleteService = function(service) {
            if (confirm("Are you sure?")) {
                webService.delete({type: service.type, id: service.identifier}, {} ,
                    function() { $growl('success','Success','Service '+ service.name +' successfully deleted');
                                 $scope.services = webService.listAll(); },
                    function() { $growl('error','Error','Service '+ service.name +' deletion failed'); }
                );
            }
        };

        $scope.refreshIndex = function(service) {
            csw.refresh({id: service.identifier}, {},
                function() { $growl('success','Success','Search index for the service '+ service.name +' successfully refreshed'); },
                function() { $growl('error','Error','Search index for the service '+ service.name +' failed to be updated'); }
            );
        };
    }]);

cstlAdminApp.controller('WebServiceUtilsController', ['$scope', 'webService', '$modalInstance', 'details',
    function ($scope, webService, $modalInstance, details) {
        $scope.details = details.data;
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

    }]);

cstlAdminApp.controller('WebServiceVersionsController', ['$scope', 'webService', '$modalInstance', 'service',
    function ($scope, webService, $modalInstance, service) {
        $scope.service = service;
        $scope.versions = service.versions;
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.chooseVersion = function(version) {
            $modalInstance.close(version);
        };
    }]);

cstlAdminApp.controller('WebServiceCreateController', ['$scope','$routeParams', 'webService', '$filter', '$location', '$growl',
    function ($scope, $routeParams, webService, $filter, $location, $growl) {
        $scope.type = $routeParams.type;
        $scope.tonext = true;
        $scope.metadata = {};
        $scope.metadata.keywords = [];

        $scope.getVersionsForType = function() {
            if ($scope.type === 'wms') {
                return [{ 'id': '1.1.1'}, { 'id': '1.3.0', 'checked': true }];
            }
            if ($scope.type === 'wfs') {
                return [{ 'id': '1.1.0', 'checked': true}, { 'id': '2.0.0' }];
            }
            if ($scope.type === 'wcs') {
                return [{ 'id': '1.0.0', 'checked': true}];
            }
            if ($scope.type === 'wmts') {
                return [{ 'id': '1.0.0', 'checked': true}];
            }
            if ($scope.type === 'csw') {
                return [{ 'id': '2.0.0'}, { 'id': '2.0.2', 'checked': true}];
            }
            if ($scope.type === 'sos') {
                return [{ 'id': '1.0.0', 'checked': true}];
            }
            return [];
        };
        $scope.versions = $scope.getVersionsForType();


        $scope.goToServiceContact = function() {
            $scope.tonext = false;
        };

        $scope.goToServiceInfo = function() {
            $scope.tonext = true;
        };

        $scope.addTag = function() {
            if (!$scope.tagText || $scope.tagText == '' || $scope.tagText.length == 0) {
                return;
            }

            $scope.metadata.keywords.push($scope.tagText);
            $scope.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.tagText.length == 0 &&
                key === undefined) {
                $scope.metadata.keywords.pop();
            } else if (key != undefined) {
                $scope.metadata.keywords.splice(key, 1);
            }
        };

        // define which version to set
        $scope.selectedVersion = function (){
            var selVersions = $filter('filter')($scope.versions, {checked: true});
            var strVersions = [];
            for(var i=0; i < selVersions.length; i++) {
                strVersions.push(selVersions[i].id);
            }
            $scope.metadata.versions = strVersions;
        };

        // define which version is Selected
        $scope.versionIsSelected = function(currentVersion){
            return $.inArray(currentVersion, $scope.metadata.versions) > -1
        };

        $scope.saveServiceMetadata = function() {
            // Ensures both name and identifier are filled
            if ($scope.metadata.identifier == null && $scope.metadata.name != null) {
                $scope.metadata.identifier = $scope.metadata.name;
            }
            if ($scope.metadata.name == null && $scope.metadata.identifier != null) {
                $scope.metadata.name = $scope.metadata.identifier;
            }

            webService.create({type: $scope.type}, $scope.metadata,
                              function() {
                                  $growl('success', 'Success', 'Service ' + $scope.metadata.name + ' successfully created');
                                  if ($scope.type == 'csw' || $scope.type == 'sos') {
                                      $location.path('/webservice/'+ $scope.type +'/'+ $scope.metadata.name +'/source');
                                  } else {
                                      $location.path('/webservice');
                                  }
                              },

                              function() { $growl('error','Error','Service '+ $scope.metadata.name +' creation failed'); }
            );
        };
    }]);

cstlAdminApp.controller('WebServiceChooseSourceController', ['$scope','$routeParams', 'webService', '$growl', '$location',
    function ($scope, $routeParams , webService, $growl, $location) {
        $scope.type = $routeParams.type;
        $scope.id = $routeParams.id;

        $scope.source = {'automatic' : {'@format': null, 'bdd': {}}};
        $scope.mdsource = {'automatic' : {'@format': null, 'bdd': {}}};
        $scope.end = 'false';
        $scope.setEnd=function(value){
            $scope.end=value;
        };
        $scope.saveServiceSource = function() {
            webService.setConfig({type: $scope.type, id: $scope.id}, $scope.source, function() {
                $growl('success','Success','Service '+ $scope.id +' successfully updated');
                $location.path('/webservice');
            }, function() {
                $growl('error','Error','Service configuration update error');
            });
        };
        $scope.saveSOSSource = function() {
            webService.setConfig({type: $scope.type, id: $scope.id}, $scope.source, function() {
                $growl('success','Success','Service '+ $scope.id +' successfully updated');
                $location.path('/webservice');
            }, function() {
                $growl('error','Error','Service configuration update error');
            });
        };
    }]);

cstlAdminApp.controller('WebServiceEditController', ['$scope','$routeParams', 'webService', 'dataListing', 'provider', 'csw', '$modal','textService', '$dashboard', '$growl', '$filter', 'StyleSharedService','style','$cookies',
                                                 function ($scope, $routeParams , webService, dataListing, provider, csw, $modal, textService, $dashboard, $growl, $filter, StyleSharedService, style, $cookies) {
    $scope.tagText = '';
    $scope.type = $routeParams.type;
    $scope.url = $cookies.cstlUrl + "WS/" + $routeParams.type + "/" + $routeParams.id;
    $scope.cstlUrl = $cookies.cstlUrl;
    $scope.cstlSessionId = $cookies.cstlSessionId;
    $scope.urlBoxSize = Math.min($scope.url.length,100);

    var client = new ZeroClipboard( document.getElementById("copy-button") );

  	client.on( "load", function(client) {
  	  // alert( "movie is loaded" );
  	  client.on( "complete", function(client, args) {
  	    // `this` is the element that was clicked
  		$growl('success','Success',"Copied text to clipboard: " + args.text );
  	  } );
  	} );

    $scope.service = webService.get({type: $scope.type, id:$routeParams.id});
    $scope.metadata = webService.metadata({type: $scope.type, id:$routeParams.id});

    $scope.filtertype = "";

    $scope.tabdata = true;
    $scope.tabdesc = false;
    $scope.tabmetadata = false;

    $scope.selectTab = function(item) {
        if (item === 'tabdata') {
            $scope.tabdata = true;
            $scope.tabdesc = false;
            $scope.tabmetadata = false;
        } else if (item === 'tabdesc') {
            $scope.tabdata = false;
            $scope.tabdesc = true;
            $scope.tabmetadata = false;
        } else {
            $scope.tabdata = false;
            $scope.tabdesc = false;
            $scope.tabmetadata = true;
        }
    };

    $scope.initScope = function() {
        if ($scope.type === 'csw') {
            csw.count({id: $routeParams.id}, {}, function(max) {
                csw.getRecords({id: $routeParams.id, count: max.asInt, startIndex: 0}, {}, function(response) {
                    $dashboard($scope, response.BriefNode, false);
                    dataListing.listData({},
                        function(response) { $scope.relatedDatas = response; },
                        function() { $growl('error','Error','Unable to get related data for providers'); }
                    );
                });
            });
        } else {
            $scope.config = webService.config({type: $scope.type, id:$routeParams.id});
            $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                $dashboard($scope, response, true);
            });
        }
    };

    $scope.getVersionsForType = function() {
        if ($scope.type === 'wms') {
            return [{ 'id': '1.1.1'}, { 'id': '1.3.0' }];
        }
        if ($scope.type === 'wfs') {
            return [{ 'id': '1.1.0'}, { 'id': '2.0.0' }];
        }
        if ($scope.type === 'wcs') {
            return [{ 'id': '1.0.0'}];
        }
        if ($scope.type === 'wmts') {
            return [{ 'id': '1.0.0'}];
        }
        if ($scope.type === 'csw') {
            return [{ 'id': '2.0.0'}, { 'id': '2.0.2'}];
        }
        if ($scope.type === 'sos') {
            return [{ 'id': '1.0.0'}];
        }
        return [];
    };
    $scope.versions = $scope.getVersionsForType();

    // define which version to set
    $scope.selectedVersion = function (){
        var selVersions = $filter('filter')($scope.versions, {checked: true});
        var strVersions = [];
        for(var i=0; i < selVersions.length; i++) {
            strVersions.push(selVersions[i].id);
        }
        $scope.metadata.versions = strVersions;
    };

    // define which version is Selected
    $scope.versionIsSelected = function(currentVersion){
       return $.inArray(currentVersion, $scope.metadata.versions) > -1
    };

     $scope.addTag = function() {
         if (!$scope.tagText || $scope.tagText == '' || $scope.tagText.length == 0) {
             return;
         }
         if ($scope.metadata.keywords ==null){
             $scope.metadata.keywords = [];
         }
         $scope.metadata.keywords.push($scope.tagText);
         $scope.tagText = '';
     };

     $scope.deleteTag = function(key) {
         if ($scope.metadata.keywords.length > 0 &&
             $scope.tagText.length == 0 &&
             key === undefined) {
             $scope.metadata.keywords.pop();
         } else if (key != undefined) {
             $scope.metadata.keywords.splice(key, 1);
         }
     };

    $scope.saveServiceMetadata = function() {
      webService.updateMd({type: $scope.service.type, id: $scope.service.identifier},$scope.metadata,
          function(response) {
              if (response.status==="Success") {
                  $growl('success','Success','Service description successfully updated');
              }else{
                  $growl('error','Error','Service description update failed due to :'+response.status);
              }
          },
          function() {
              $growl('error','Error','Service description update failed');
          }
      )
    };

    // Show Capa methods
    $scope.showCapa = function(service) {
        if (service.versions.length > 1) {
            var modal = $modal.open({
                templateUrl: 'views/modalChooseVersion.html',
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
            templateUrl: 'views/modalCapa.html',
            controller: 'WebServiceUtilsController',
            resolve: {
                'details': function(textService){
                    return textService.capa(service.type.toLowerCase(), service.identifier, version);
                }
            }
        });
    };

     // Show Logs methods
     $scope.showLogs = function(service) {

         $modal.open({
             templateUrl: 'views/modalLogs.html',
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
            function() { $growl('success','Success','Service '+ service.name +' successfully reloaded'); },
            function() { $growl('error','Error','Service '+ service.name +' reload failed'); }
        );
     };


     $scope.startOrStop = function(service){
        if(service.status==='WORKING'){
            webService.stop({type: service.type, id: service.identifier}, {}, function(response) {
                if (response.status==="Success") {
                    $scope.service.status = "NOT_STARTED";
                    $growl('success','Success','Service '+ service.name +' successfully stopped');
                }
            }, function() { $growl('error','Error','Service '+ service.name +' stop failed'); });
        }else{
            webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                if (response.status==="Success") {
                    $scope.service.status = "WORKING";
                    $growl('success','Success','Service '+ service.name +' successfully started');
                }
            }, function() { $growl('error','Error','Service '+ service.name +' start failed'); });
        }
     };

     // Allow to choose data to add for this service
     $scope.showDataToAdd = function() {
         var modal = $modal.open({
             templateUrl: 'views/modalDataChoose.html',
             controller: 'DataModalController',
             resolve: {
                 exclude: function() { return $scope.layers; },
                 service: function() { return $scope.service; }
             }
         });

         modal.result.then(function() {
             $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                 $scope.fullList = response;
             });
         });
     };

     $scope.deleteLayer = function() {
         if ($scope.selected != null && confirm("Are you sure?")) {
             webService.deleteLayer({type: $scope.service.type, id: $scope.service.identifier, layerid: $scope.selected.Name}, {layernamespace: ''},
                 function() {$growl('success','Success','Layer '+ $scope.selected.Name +' successfully deleted from service '+ $scope.service.name);
                             $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                                 $scope.fullList = response;
                             });
                 },
                 function() {$growl('error','Error','Layer '+ $scope.selected.Name +' failed to be deleted from service '+ $scope.service.name);}
                );
         }
     };

     $scope.deleteMetadata = function() {
         if ($scope.selected != null && confirm("Are you sure?")) {
             csw.delete({id: $scope.service.identifier, metaId: $scope.selected.identifier}, {},
                 function() {
                     $growl('success','Success','Metadata deleted');
                     csw.count({id: $routeParams.id}, {}, function(max) {
                         csw.getRecords({id: $routeParams.id, count: max.asInt, startIndex: 0}, {}, function(response) {
                             $dashboard($scope, response.BriefNode, false);
                         });
                     });
                 }, function() { $growl('error','Error','Failed to delete metadata'); }
             );
         }
     };

     $scope.showLayer = function() {
         $('#viewerData').modal("show");
         var layerName = $scope.selected.Name;
         if ($scope.service.type === 'WMTS') {
             // GetCaps
             textService.capa($scope.service.type.toLowerCase(), $scope.service.identifier, $scope.service.versions[0])
                 .success(function (data, status, headers, config) {
                     // Build map
                     var capabilities = WmtsViewer.format.read(data);
                     WmtsViewer.initMap('dataMap', capabilities);
                     var layerData = WmtsViewer.createLayer(layerName, $scope.service.identifier, capabilities);
                     WmtsViewer.map.addLayer(layerData);
                     var maxExtent = capabilities.contents.layers[0].bounds;
                     WmtsViewer.map.zoomToExtent(maxExtent, true);
                 });
         } else {
             var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
             var layerData;
             if ($scope.service.type === 'WMS') {
                 textService.capa($scope.service.type.toLowerCase(), $scope.service.identifier, $scope.service.versions[0])
                     .success(function (data, status, headers, config) {
                         var capabilities = DataViewer.format.read(data);
                         var layers = capabilities.capability.layers;
                         var capsLayer;
                         for(var i=0; i < layers.length; i++) {
                             var l = layers[i];
                             if (l.name === layerName) {
                                 capsLayer = l;
                                 break;
                             }
                         }
                         var llbbox = capsLayer.llbbox;
                         var extent = new OpenLayers.Bounds(llbbox[0], llbbox[1], llbbox[2], llbbox[3]);
                         layerData = DataViewer.createLayerWMS($cookies.cstlUrl, layerName, $scope.service.identifier);
                         DataViewer.layers = [layerData, layerBackground];
                         DataViewer.initMap('dataMap');
                         DataViewer.map.zoomToExtent(extent, true);
                     });
             } else {
                 var providerId = $scope.selected.Provider;
                 layerData = DataViewer.createLayer($cookies.cstlUrl, layerName, providerId);
                 DataViewer.layers = [layerData, layerBackground];

                 dataListing.metadata({providerId: providerId, dataId: layerName}, {}, function(response) {
                     // Success getting the metadata, try to find the data extent
                     var ident = response['gmd.MD_Metadata']['gmd.identificationInfo'];
                     if (ident) {
                         var extentMD = ident['gmd.MD_DataIdentification']['gmd.extent'];
                         if (extentMD) {
                             var bbox = extentMD['gmd.EX_Extent']['gmd.geographicElement']['gmd.EX_GeographicBoundingBox'];
                             var extent = new OpenLayers.Bounds(bbox['gmd.westBoundLongitude']['gco.Decimal'], bbox['gmd.southBoundLatitude']['gco.Decimal'],
                                 bbox['gmd.eastBoundLongitude']['gco.Decimal'], bbox['gmd.northBoundLatitude']['gco.Decimal']);
                             DataViewer.initMap('dataMap');
                             DataViewer.map.zoomToExtent(extent, true);
                         }
                     }
                 }, function() {
                     DataViewer.initMap('dataMap');
                 });
             }
         }
     };

     $scope.toggleUpDownSelected = function() {
         var $header = $('#serviceDashboard').find('.selected-item').find('.block-header');
         $header.next().slideToggle(200);
         $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
     };

     // Style methods
     $scope.showStyleList = function() {
         StyleSharedService.showStyleList($scope);
     };

     $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
         StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style);
     };
    }]);

cstlAdminApp.controller('DataModalController', ['$scope', 'dataListing', 'webService', '$dashboard', '$modalInstance', 'service', 'exclude', '$growl',
    function ($scope, dataListing, webService, $dashboard, $modalInstance, service, exclude, $growl) {
        $scope.service = service;

        $scope.getDefaultFilter = function() {
            if (service.type.toLowerCase() === 'wms') {
                return '';
            }
            if (service.type.toLowerCase() === 'wcs') {
                return 'coverage';
            }
            if (service.type.toLowerCase() === 'wfs') {
                return 'vector';
            }
            return '';
        };
        $scope.filtertype = $scope.getDefaultFilter();
        $scope.nbbypage = 5;
        $scope.exclude = exclude;

        // WMTS params in the last form before closing the popup
        $scope.wmtsParams = false;
        $scope.data = undefined;
        $scope.tileFormat = undefined;
        $scope.crs = undefined;
        $scope.scales = [];
        $scope.upperCornerX = undefined;
        $scope.upperCornerY = undefined;
        $scope.conformPyramid = undefined;

        dataListing.listAll({}, function(response) {
            $dashboard($scope, response, true);
        });

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.choose = function(data) {
            if (data == null) {
                $growl('warning','Warning','No layer selected');
                $modalInstance.dismiss('close');
            } else {
                if ($scope.wmtsParams === false) {
                    // just add the data if we are not in the case of the wmts service
                    if (service.type.toLowerCase() !== 'wmts') {
                        if (service.type.toLowerCase() === 'wms' && $scope.conformPyramid) {
                            // In the case of a wms service and user asked to pyramid the data
                            dataListing.pyramidConform({providerId: data.Provider, dataId: data.Name}, {}, function(tiledProvider) {
                                webService.addLayer({type: service.type, id: service.identifier},
                                    {layerAlias: tiledProvider.dataId, layerId: tiledProvider.dataId, serviceType: service.type, serviceId: service.identifier, providerId: tiledProvider.providerId},
                                    function () {
                                        $growl('success', 'Success', 'Layer ' + tiledProvider.dataId + ' successfully added to service ' + service.name);
                                        $modalInstance.close();
                                    },
                                    function () {
                                        $growl('error', 'Error', 'Layer ' + tiledProvider.dataId + ' failed to be added to service ' + service.name);
                                        $modalInstance.dismiss('close');
                                    }
                                );
                            } , function() {
                                $growl('error', 'Error', 'Failed to generate conform pyramid for ' + data.Name);
                                $modalInstance.dismiss('close');
                            });
                        } else {
                            // Not in WMTS and no pyramid requested
                            webService.addLayer({type: service.type, id: service.identifier},
                                {layerAlias: data.Name, layerId: data.Name, serviceType: service.type, serviceId: service.identifier, providerId: data.Provider},
                                function () {
                                    $growl('success', 'Success', 'Layer ' + data.Name + ' successfully added to service ' + service.name);
                                    $modalInstance.close();
                                },
                                function () {
                                    $growl('error', 'Error', 'Layer ' + data.Name + ' failed to be added to service ' + service.name);
                                    $modalInstance.dismiss('close');
                                }
                            );
                        }
                        return;
                    }

                    // WMTS here, prepare form
                    dataListing.pyramidScales({providerId: data.Provider, dataId: data.Name}, function(response) {
                        $scope.scales = response.Entry[0].split(',');
                    }, function () {
                        $growl('error', 'Error', 'Unable to pyramid data ' + data.Name);
                    });

                    $scope.wmtsParams = true;
                    // Stores the data for further click on the same choose button in the next form
                    $scope.data = data;
                } else {
                    // Finish the WMTS publish process
                    // Pyramid the data to get the new provider to add
                    dataListing.pyramidData({providerId: $scope.data.Provider, dataId: $scope.data.Name},
                                            {tileFormat: $scope.tileFormat, crs: $scope.crs, scales: $scope.scales, upperCornerX: $scope.upperCornerX, upperCornerY: $scope.upperCornerY},
                                            function(respProvider) {
                                                // Add the tiled provider to the service
                                                webService.addLayer({type: service.type, id: service.identifier},
                                                    {layerAlias: respProvider.dataId, layerId: respProvider.dataId, serviceType: service.type, serviceId: service.identifier, providerId: respProvider.providerId},
                                                    function () {
                                                        $growl('success', 'Success', 'Layer ' + respProvider.dataId + ' successfully added to service ' + service.name);
                                                        $modalInstance.close();
                                                    },
                                                    function () {
                                                        $growl('error', 'Error', 'Layer ' + respProvider.dataId + ' failed to be added to service ' + service.name);
                                                        $modalInstance.dismiss('close');
                                                    }
                                                );
                                            }, function() { $growl('error', 'Error', 'Pyramid process failed for ' + $scope.data.Name); });
                }
            }
        };
    }]);
