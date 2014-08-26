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

var cstlIndexApp = angular.module('cstlIndexApp', [
    // Angular official modules.
    'ngCookies',
    'ngResource',
    'ngRoute',
    // Libraries modules.
    'pascalprecht.translate',
    // Constellation modules.
    'cstl-directives',
    'http-auth-interceptor']);

cstlIndexApp
    .config(['$routeProvider', '$httpProvider', '$translateProvider',
        function ($routeProvider, $httpProvider, $translateProvider) {
            $routeProvider
                .when('/registration', {
                    templateUrl: 'views/registration.html',
                    controller: 'MainController'
                })
                .otherwise({
                templateUrl: 'views/main.html',
                controller: 'MainController'
            });

            $httpProvider.defaults.useXDomain = true;
            $httpProvider.interceptors.push('AuthInterceptor');
            // Initialize angular-translate
            $translateProvider.useStaticFilesLoader({
                prefix: findWebappContext() + '/i18n/',
                suffix: '.json'
            });
            $translateProvider.preferredLanguage('en');
            // remember language
            $translateProvider.useCookieStorage();
        }])
    .run(['$rootScope', '$location',
        function($rootScope, $location){
        }]);
