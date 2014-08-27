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

var cstlSession = {};


/* App Module */

var cstlAdminApp = angular.module('cstlAdminApp', [
    // Angular official modules.
    'ngCookies',
    'ngResource',
    'ngRoute',
    // Libraries modules.
    'base64',
    'hljs',
    'pascalprecht.translate',
    'rzModule',
    'ui.ace',
    'ui.bootstrap.modal',
    'ui.bootstrap.buttons',
    'ui.bootstrap.tpls-accordion',
    'ui.bootstrap.transition',
    'ui.bootstrap.collapse',
    'ui.bootstrap.accordion',
    'ui.bootstrap.tpls-popover',
    'ui.bootstrap.position',
    'ui.bootstrap.bindHtml',
    'ui.bootstrap.tooltip',
    'ui.bootstrap.popover',
    'ui.bootstrap.tpls-typeahead',
    'ui.bootstrap.typeahead',
    // Constellation modules.
    'cstl-directives',
    'cstl-services',
    'http-auth-interceptor']);



cstlAdminApp
    .config(['$routeProvider', '$httpProvider', '$translateProvider',
        function ($routeProvider, $httpProvider, $translateProvider) {
    	 $httpProvider.defaults.useXDomain = true;

    	 $httpProvider.interceptors.push('AuthInterceptor');


            $routeProvider
                .when('/admin', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/system_state', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/system_settings', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/system_logs', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/system_contact', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/system_about', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/tasks_manager', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/planning', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/users', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/groups', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/domains', {
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
                .when('/admin/domainmembers/:domainId', {
                    //templateUrl: 'views/admin/domain/members.html',
                    //controller: 'DomainMembersController'
                    templateUrl: 'views/admin/main.html',
                    controller: 'AdminController'
                })
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
                .when('/logout', {
                    templateUrl: 'views/main.html',
                    controller: 'LogoutController'
                })
                .when('/webservice', {
                    templateUrl: 'views/webservice/webservice.html',
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
                    templateUrl: 'views/data/description.html',
                    controller: 'DescriptionController'
                })
                .when('/data', {
                    templateUrl: 'views/data/data.html',
                    controller: 'DataController'
                })
                .when('/sensors', {
                    templateUrl: 'views/sensor/sensors.html',
                    controller: 'SensorsController'
                })
                .when('/styles', {
                    templateUrl: 'views/style/styles.html',
                    controller: 'StylesController'
                })
                .when('/mapcontext', {
                    templateUrl: 'views/mapcontext/mapcontext.html',
                    controller: 'MapcontextController'
                })
                .when('/tasks', {
                    templateUrl: 'views/tasks/tasks.html',
                    controller: 'TasksController'
                })
                .when('/profile', {
                    templateUrl: 'views/profile.html',
                    controller: 'MainController'
                })

                .otherwise({
                    templateUrl: 'views/main.html',
                    controller: 'MainController'
                });

            // Initialize angular-translate
            $translateProvider.useStaticFilesLoader({
                prefix: 'i18n/',
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

           $rootScope.hasRole = function(){return false}
            
            // Call when the user is authenticated
           $rootScope.$on('event:auth-authConfirmed', function() {
               $rootScope.authenticated = true;
               
               Account.get(function(sessionData){
                $rootScope.cstlSession=sessionData;
                cstlSession = sessionData;
               })

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
