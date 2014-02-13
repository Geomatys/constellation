'use strict';

/* Controllers */

cstlAdminApp.controller('MainController', ['$scope',
    function ($scope) {
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

cstlAdminApp.controller('PasswordController', ['$scope', 'Password',
    function ($scope, Password) {
        $scope.success = null;
        $scope.error = null;
        $scope.doNotMatch = null;
        $scope.changePassword = function () {
            if ($scope.password != $scope.confirmPassword) {
                $scope.doNotMatch = "ERROR";
            } else {
                $scope.doNotMatch = null;
                Password.save($scope.password,
                    function (value, responseHeaders) {
                        $scope.error = null;
                        $scope.success = 'OK';
                    },
                    function (httpResponse) {
                        $scope.success = null;
                        $scope.error = "ERROR";
                    });
            }
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

cstlAdminApp.controller('MetricsController', ['$scope', 'resolvedMetrics',
    function ($scope, resolvedMetrics) {
        $scope.metrics = resolvedMetrics;
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

cstlAdminApp.controller('DataController', ['$scope', '$location', '$dashboard', 'dataListing', 'style', '$modal',
    function ($scope, $location, $dashboard, dataListing, style, $modal) {

        $scope.filtertype = "VECTOR";

        dataListing.listAll({}, function(response) {
            $dashboard($scope, response);
        });

        // Map methods
        $scope.showData = function() {
            $('#viewerData').modal("show");
            var layerName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            var layerData = DataViewer.createLayer(layerName, providerId);
            var layerBackground = DataViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataMap');
        };

        $scope.deleteData = function() {
            var layerName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            dataListing.deleteData({providerid: providerId, dataid: layerName});
        };

        // Style methods
        $scope.showStyleList = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalStyleChoose.html',
                controller: 'StyleModalController',
                resolve: {
                    exclude: function() { return $scope.selected.TargetStyle }
                }
            });

            modal.result.then(function(item) {
                if (item) {
                    style.link({
                        provider: item.Provider,
                        name: item.Name
                    }, {
                        values: {
                            dataProvider: $scope.selected.Provider,
                            dataNamespace: "", dataId: $scope.selected.Name
                        }
                    }, function() {
                        $scope.selected.TargetStyle.push(item);
                    });
                }
            });
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            var res = style.unlink({provider: providerName, name: styleName},
                         {values: {dataProvider: dataProvider, dataNamespace: "", dataId: dataId}});
            if (res) {
                var index = -1;
                for (var i=0; i < $scope.selected.TargetStyle.length; i++) {
                    var item = $scope.selected.TargetStyle[i];
                    if (item.Provider === providerName && item.Name === styleName) {
                        index = i;
                        break;
                    }
                }
                if (index >= 0) {
                    $scope.selected.TargetStyle.splice(index,1);
                }
            }
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
                $location.path('/description/'+ result.file +"/"+ result.missing);
            });
        };

        $scope.showServerFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalServerFile.html',
                controller: 'ServerFileModalController'
            });
        };
    }]);

cstlAdminApp.controller('StyleModalController', ['$scope', '$dashboard', '$modalInstance', 'style', 'exclude',
    function ($scope, $dashboard, $modalInstance, style, exclude) {
        $scope.exclude = exclude;
        $scope.filtertype = "";

        style.listAll({}, function(response) {
            $dashboard($scope, response.styles);
        });

        $scope.ok = function() {
            $modalInstance.close($scope.selected);
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };
    }]);

cstlAdminApp.controller('LocalFileModalController', ['$scope', '$dashboard', '$modalInstance', '$ajaxUpload', '$growl', 'provider', 'dataListing',
    function ($scope, $dashboard, $modalInstance, $ajaxUpload, $growl, provider, dataListing) {
        $scope.layer = null;
        $scope.file = null;
        $scope.providerId = null;
        $scope.metadatafile = null;
        $scope.uploadType = '';

        // Handle upload workflow
        $scope.step1 = true;
        $scope.step2 = false;
        $scope.step3 = false;
        $scope.allowSubmit = false;
        $scope.allowNext = false;

        $scope.next = function() {
            $scope.step1 = false;
            $scope.step2 = true;
            $scope.step3 = false;
            $scope.allowNext = false;
            $scope.allowSubmit = true;
        };

        $scope.close = function() {
            if ($scope.step3) {
                $modalInstance.close({file: $scope.providerId, missing: $scope.metadatafile == null});
            } else {
                $modalInstance.dismiss('close');
            }
        };

        $scope.verifyExtension = function() {
            var lastPointIndex = $scope.file.lastIndexOf(".");
            var extension = $scope.file.substring(lastPointIndex+1, $scope.file.length);
            var simplevalue = new SimpleValue(extension);
            $.ajax({
                type  :   "POST",
                url   :   cstlContext + "api/1/data/testextension/",
                success : localFileSuccess,
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                data: JSON.stringify(simplevalue)
            });
        };

        function SimpleValue(extension) {
            this.value = extension;
        };

        function localFileSuccess(data){
            $scope.$apply(function() {
                if(data.dataType!=""){
                    $scope.uploadType = data.dataType;
                    $scope.allowNext = false;
                    $scope.allowSubmit = true;
                } else {
                    $scope.allowNext = true;
                    $scope.allowSubmit = false;
                }
            });
        };

        $scope.upload = function() {
            var form = $('#uploadForm');
            $ajaxUpload(cstlContext + "api/1/data/upload", form, uploaded);
        };

        function uploaded(message) {
            $scope.$apply(function() {
                if (message.indexOf('failed') === -1) {
                    var file = message.substring(message.lastIndexOf("/")+1);
                    var fileName = file;
                    var fileExtension;
                    if (file.indexOf(".") !== -1) {
                        fileName = file.substring(0, file.lastIndexOf("."));
                        fileExtension = file.substring(file.lastIndexOf(".")+1);
                    }

                    // Store the providerId for further calls
                    $scope.providerId = fileName;
                    if ($scope.uploadType === "vector") {
                        provider.create({
                            id: fileName
                        }, {
                            type: "feature-store",
                            subType: "shapefile",
                            parameters: {
                                path: message
                            }
                        });
                        $growl('success','Success','Shapefile data '+ fileName +' successfully added');
                        $modalInstance.close({file: fileName, missing: $scope.metadatafile == null});
                    } else if ($scope.uploadType === "raster") {
                        provider.create({
                            id: fileName
                        }, {
                            type: "coverage-store",
                            subType: "coverage-file",
                            parameters: {
                                path: message
                            }
                        }, function() {
                            if (!fileExtension || fileExtension !== "nc") {
                                dataListing.pyramidData({id: fileName}, {value: message}, function() {
                                    $growl('success','Success','Coverage data '+ fileName +' successfully added');
                                    $modalInstance.close({file: fileName, missing: $scope.metadatafile == null});
                                });
                            } else {
                                displayNetCDF(fileName);
                            }
                        });
                    } else {
                        $growl('warning','Warning','Not implemented choice');
                        $modalInstance.close();
                    }
                } else {
                    $growl('error','Error','Data import failed');
                    $modalInstance.close();
                }
            });
        };

        function displayNetCDF(providerId) {
            $scope.step1 = false;
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
            var layerData = DataViewer.createLayer(layer, $scope.providerId);
            var layerBackground = DataViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataPreviewMap');
        };
    }]);

cstlAdminApp.controller('ServerFileModalController', ['$scope', '$dashboard', '$modalInstance', '$growl', 'dataListing', 'provider',
    function ($scope, $dashboard, $modalInstance, $growl, dataListing, provider) {
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
            var layerData = DataViewer.createLayer(layer, $scope.providerId);
            var layerBackground = DataViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataServerMap');
        };

        $scope.load($scope.currentPath);
    }]);

cstlAdminApp.controller('StylesController', ['$scope', '$dashboard', 'style',
    function ($scope, $dashboard, style) {
        $scope.filtertype = "";

        style.listAll({}, function(response) {
            $dashboard($scope, response.styles);
        });

        $scope.deleteStyle = function() {
            var styleName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            style.delete({providerid: providerId, name: styleName});
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#stylesDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };
    }]);

cstlAdminApp.controller('DescriptionController', ['$scope', '$routeParams',
    function ($scope, $routeParams) {
        $scope.provider = $routeParams.id;
        $scope.missing = $routeParams.missing;
        $scope.tabiso = true;
        $scope.tabcrs = false;
        $scope.tabdesc = false;

        $scope.selectTab = function(item) {
            if (item === 'tabiso') {
                $scope.tabiso = true;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
            } else if (item === 'tabcrs') {
                $scope.tabiso = false;
                $scope.tabcrs = true;
                $scope.tabdesc = false;
            } else {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = true;
            }
        };

        $scope.ok = function() {

        };
    }]);

cstlAdminApp.controller('WebServiceController', ['$scope', 'webService',
    function ($scope, webService) {
       $scope.services = webService.listAll();
    }]);

cstlAdminApp.controller('WebServiceEditController', ['$scope','$routeParams', 'webService',
                                                 function ($scope, $routeParams , webService) {
    $scope.service = webService.get({type: $routeParams.type, id:$routeParams.id});
    $scope.metadata = webService.metadata({type: $routeParams.type, id:$routeParams.id});
    $scope.config = webService.config({type: $routeParams.type, id:$routeParams.id});
                                                 }]);
