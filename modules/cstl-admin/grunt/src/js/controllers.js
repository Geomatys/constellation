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

/* Controllers */

var dataNotReady = function(){alert("data not ready")};

cstlAdminApp.controller('HeaderController', ['$scope','$http',
    function ($scope, $http) {
        $http.get("app/conf").success(function(data){
	       	  $scope.cstlLoginUrl = data.cstl + "spring/auth/form";
	          $scope.cstlLogoutUrl = data.cstl + "logout";
	      });
}]);

cstlAdminApp.controller('MainController', ['$scope','$location','webService','dataListing','ProcessService','Growl', 'UserResource', 'GeneralService', 'TaskService',
    function ($scope, $location, webService, dataListing, Process, Growl, UserResource, GeneralService, task) {
        $scope.countStats = function() {
            webService.listAll({}, function(response) {
                var count = 0;
                for (var i=0; i<response.instance.length; i++) {
                    if (response.instance[i].status === 'STARTED' && response.instance[i].type != 'WEBDAV') {
                        count++;
                    }
                }
                $scope.nbservices = count;
            }, function() {
                $scope.nbservices = 0;
                Growl('error', 'Error', 'Unable to count services');
            });


            dataListing.listAll({}, function(response) {
                $scope.nbdata = response.length;
            }, function() {
                $scope.nbdata = 0;
                Growl('error', 'Error', 'Unable to count data');
            });

            task.listTasks({}, function(taskList) {
                $scope.nbprocess = Object.keys(taskList).length;
            }, function() {
                $scope.nbprocess = 0;
                Growl('error', 'Error', 'Unable to count process');
            });

            GeneralService.counts().success(function(response) {
                $scope.nbusers = response.nbuser;
            }).error(function() {
                $scope.nbusers = 1;
                Growl('error', 'Error', 'Unable to count users');
            });
        };
    }]);

cstlAdminApp.controller('LanguageController', ['$scope', '$translate',
    function ($scope, $translate) {

        $scope.currentLang = 'en';

        $scope.changeLanguage = function () {
            $translate.use($scope.currentLang);
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

cstlAdminApp.controller('ModalInstanceCtrl', ['$scope', '$modalInstance', function($scope, $modalInstance){
    $scope.ok = function () {
    $modalInstance.close();
  };

  $scope.cancel = function () {
    $modalInstance.dismiss();
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



cstlAdminApp.controller('navCtrl', ['$scope', '$location', function ($scope, $location) {
    $scope.navClass = function (page) {
        var currentRoute = $location.path().split('/')[1] || 'home';
        return page === currentRoute ? 'menu-selected' : '';
    };
}]);
