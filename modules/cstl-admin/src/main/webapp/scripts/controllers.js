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

cstlAdminApp.controller('DataController', ['$scope', '$dashboard', 'dataListing', 'style', '$modal',
    function ($scope, $dashboard, dataListing, style, $modal) {

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
            DataViewer.initMap();
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
        $scope.layer = '';

        $scope.init = function() {
            $("#part2").hide();
            $("#part3").hide();
            $("#submitButton").hide();
            $("#nextButton").hide();
        };

        $scope.next = function() {
            $("#part1").hide();
            $("#nextButton").hide();
            $("#part2").show();
            $("#submitButton").show();
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.verifyExtension = function() {
            var selectedFile = $("#file").val();
            var lastPointIndex = selectedFile.lastIndexOf(".");
            var extension = selectedFile.substring(lastPointIndex+1, selectedFile.length);
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
            if(data.dataType!=""){
                $("#part2 [value="+data.dataType+"]").prop("checked", true);
                $("#nextButton").hide();
                $("#submitButton").show();
            } else {
                $("#submitButton").hide();
                $("#nextButton").show();
            }
        };

        $scope.upload = function() {
            var form = $('#uploadForm');
            $ajaxUpload(cstlContext + "api/1/data/upload", form, uploaded);
            $scope.file = form.find('#file').val();
            $scope.uploadType = form.find("input[name='dataType']:checked").val();
        };

        function uploaded(message) {
            $scope.$apply(function() {
                if (message.indexOf('failed') === -1) {
                    var fileName = message.substring(message.lastIndexOf("/")+1);
                    var fileExtension;
                    if (fileName.indexOf(".") !== -1) {
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                        fileExtension = fileName.substring(fileName.lastIndexOf("."));
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
                        $modalInstance.close();
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
                            if (!fileExtension || fileExtension !== ".nc") {
                                dataListing.pyramidData({id: fileName}, {value: message}, function() {
                                    $growl('success','Success','Coverage data '+ fileName +' successfully added');
                                    $modalInstance.dismiss('close');
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
            $("#uploadForm").find(".modal-dialog").addClass("mapview");
            $("#part1").hide();
            $("#part2").hide();
            $("#part3").show();
            $("#submitButton").hide();

            $scope.coveragesData = dataListing.listCoverage({}, {value: providerId}, function(response) {
                for (var key in response.values) {
                    $scope.displayLayer(response.values[key]);
                    break;
                }
            });
        };

        $scope.displayLayer = function(layer) {
            $scope.layer = layer;
            var layerData = DataPreviewViewer.createLayer(layer, $scope.providerId);
            var layerBackground = DataPreviewViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
            DataPreviewViewer.layers = [layerData, layerBackground];
            DataPreviewViewer.initMap();
        };
    }]);

cstlAdminApp.controller('ServerFileModalController', ['$scope', '$dashboard', '$modalInstance', '$growl', 'dataListing',
    function ($scope, $dashboard, $modalInstance, $growl, dataListing) {
        $scope.columns = [];
        $scope.currentPath = '/';

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

        $scope.select = function(item,depth) {
            if (item.folder) {
                $scope.open(item.subPath, depth);
            } else {

            }
        };

        $scope.startWith = function(path) {
            return $scope.currentPath.indexOf(path) === 0;
        };

        $scope.load($scope.currentPath);
    }]);

cstlAdminApp.controller('StylesController', ['$scope', '$dashboard', 'style', '$modal',
    function ($scope, $dashboard, style, $modal) {
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
