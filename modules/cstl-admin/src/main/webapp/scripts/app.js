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


function findWebappContext(){
    var path = window.location.pathname;
    if(path == '/')
        return "/";

    return path.substring(0, path.indexOf("/", 1));
}


/* App Module */

var cstlAdminApp = angular.module('cstlAdminApp', ['http-auth-interceptor', 'ngResource', 'ngRoute', 'ngCookies', 'pascalprecht.translate', 'uiModal', 'hljs', 'base64','ui.ace']);

cstlAdminApp
    .config(['$routeProvider', '$httpProvider', '$translateProvider',
        function ($routeProvider, $httpProvider, $translateProvider) {
    	 $httpProvider.defaults.useXDomain = true;

    	 $httpProvider.interceptors.push('AuthInterceptor');


            $routeProvider
                .when('/settings', {
                    templateUrl: 'views/settings.html',
                    controller: 'SettingsController',
                    resolve:{
                        resolvedAccount:['Account', function (Account) {
                            return Account.get();
                        }]
                    }
                })
                .when('/password', {
                    templateUrl: 'views/password.html',
                    controller: 'PasswordController'
                })
                .when('/sessions', {
                    templateUrl: 'views/sessions.html',
                    controller: 'SessionsController',
                    resolve:{
                        resolvedSessions:['Sessions', function (Sessions) {
                            return Sessions.get();
                        }]
                    }
                })
                .when('/metrics', {
                    templateUrl: 'views/metrics.html',
                    controller: 'MetricsController',
                    resolve:{
                        resolvedMetrics:['Metrics', function (Metrics) {
                            return Metrics.get();
                        }]
                    }
                })
                .when('/logs', {
                    templateUrl: 'views/logs.html',
                    controller: 'LogsController',
                    resolve:{
                        resolvedLogs:['LogsService', function (LogsService) {
                            return LogsService.findAll();
                        }]
                    }
                })
                .when('/logout', {
                    templateUrl: 'views/main.html',
                    controller: 'LogoutController'
                })
                .when('/webservice', {
                    templateUrl: 'views/webservice.html',
                    controller: 'WebServiceController'
                })
                .when('/webservice/:type/:id', {
                    templateUrl: 'views/webservice/edit.html',
                    controller: 'WebServiceEditController'
                })
                .when('/webservice/:type/:id/source', {
                    templateUrl: 'views/webservice/source.html',
                    controller: 'WebServiceChooseSourceController'
                })
                .when('/webservice/:type', {
                    templateUrl: 'views/webservice/create.html',
                    controller: 'WebServiceCreateController'
                })
                .when('/description/:type/:id/:missing', {
                    templateUrl: 'views/description.html',
                    controller: 'DescriptionController'
                })
                .when('/data', {
                    templateUrl: 'views/data.html',
                    controller: 'DataController'
                })
                .when('/sensors', {
                    templateUrl: 'views/sensors.html',
                    controller: 'SensorsController'
                })
                .when('/styles', {
                    templateUrl: 'views/styles.html',
                    controller: 'StylesController'
                })
                .when('/mapcontext', {
                    templateUrl: 'views/mapcontext.html',
                    controller: 'MapcontextController'
                })
                .when('/tasks', {
                    templateUrl: 'views/tasks.html',
                    controller: 'ProcessController'
                })
                .otherwise({
                    templateUrl: 'views/main.html',
                    controller: 'MainController'
                });

            $routeProvider
            .when('/user', {
                templateUrl: 'views/user/list.html',
                controller: 'UserController'
            });
            $routeProvider
            .when('/group', {
                templateUrl: 'views/group/list.html',
                controller: 'GroupController'
            });
            $routeProvider
            .when('/contact', {
                templateUrl: 'views/contact.html',
                controller: 'ContactController'
            });

            //Process routes
            $routeProvider
            .when('/task', {
                templateUrl: 'views/task/list.html',
                controller: 'TaskController'
            });

            // Initialize angular-translate
            $translateProvider.useStaticFilesLoader({
                prefix: findWebappContext() + '/i18n/',
                suffix: '.json'
            });

            $translateProvider.preferredLanguage('en');

            // remember language
            $translateProvider.useCookieStorage();
        }])
        .run(['$rootScope', '$location', 'AuthenticationSharedService', 'Account',
            function($rootScope, $location, AuthenticationSharedService, Account) {
            $rootScope.$on("$routeChangeStart", function(event, next, current) {
                // Check if the status of the user. Is it authenticated or not?
                if($rootScope.authenticated)
                    return;
                AuthenticationSharedService.authenticate({}, function() {
                    $rootScope.authenticated = true;
                });
            });

            // Call when the 401 response is returned by the client
            $rootScope.$on('event:auth-loginRequired', function(rejection) {
              window.location.href="index.html";
            });

            // Call when the user is authenticated
           $rootScope.$on('event:auth-authConfirmed', function() {
               $rootScope.authenticated = true;
               $rootScope.account = Account.get();

               // If the login page has been requested and the user is already logged in
               // the user is redirected to the home page
               if ($location.path() === "/login") {
                   $location.path('/').replace();
               }
            });

            // Call when the user logs in
            $rootScope.$on('event:auth-loginConfirmed', function() {
                $rootScope.authenticated = true;
                $rootScope.account = Account.get();
                $location.path('').replace();
            });

            // Call when the user logs out
            $rootScope.$on('event:auth-loginCancelled', function() {
                $rootScope.authenticated = false;
                $location.path('');
            });
        }]);
