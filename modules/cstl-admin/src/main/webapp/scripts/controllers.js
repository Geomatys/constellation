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

cstlAdminApp.controller('DataController', ['$scope', '$filter', 'dataListing',
    function ($scope, $filter, dataListing) {

        $scope.displayPage = function(page) {
            var array = $filter('filter')(fullList, {'Type':$scope.filtertype, '$': $scope.filtertext});
            array = $filter('orderBy')(array, $scope.ordertype, $scope.orderreverse);
            var start = (page - 1) * $scope.nbbypage;

            $scope.currentpage = page;
            $scope.countdata = array.length;
            $scope.dataList = array.splice(start, $scope.nbbypage);
            $scope.selected = null;
        };

        $scope.dataList = [];
        var fullList = dataListing.listAll({}, function() {
            $scope.displayPage(1);
        });

        $scope.filtertext = "";
        $scope.filtertype = "VECTOR";
        $scope.ordertype = "Name";
        $scope.orderreverse = false;
        $scope.countdata = 0;
        $scope.nbbypage = 10;
        $scope.currentpage = 1;
        $scope.selected = null;

        $scope.select = function(item) {
            $scope.selected = item;
        };

        $scope.$watch('nbbypage', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('filtertext', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('filtertype', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('ordertype', function() {
            $scope.displayPage($scope.currentpage);
        });
        $scope.$watch('orderreverse', function() {
            $scope.displayPage($scope.currentpage);
        });

        $scope.showData = function() {
            $('#viewerData').modal("show");
            var layerName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            var layerData = DataViewer.createLayer(layerName, providerId);
            var layerBackground = DataViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap();
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
