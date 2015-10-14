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

angular.module('CstlIndexApp', [
    // Angular official modules.
    'ngResource',
    'ngRoute',
    // Libraries modules.
    'pascalprecht.translate',
    // Constellation modules.
    'cstl-directives',
    'cstl-services'])

    // -------------------------------------------------------------------------
    //  Configuration
    // -------------------------------------------------------------------------
    
    .config(function($routeProvider, $httpProvider, $translateProvider, $translatePartialLoaderProvider) {
        
        // Configure routes.
        $routeProvider
            .when('/disclaimer', {
                templateUrl: 'views/disclaimer.html'
            })
            .when('/help', {
                templateUrl: 'views/help.html'
            })

            .otherwise({
                templateUrl: 'views/main.html'
            });

        // Configure $http service.
        $httpProvider.defaults.useXDomain = true;
        $httpProvider.interceptors.push('AuthInterceptor');
    
        // Configure $translate service.
        $translateProvider.useLoader('$translatePartialLoader', {
            urlTemplate: 'i18n/{lang}/{part}.json'
        });
        $translatePartialLoaderProvider.addPart('ui-menu');
        $translatePartialLoaderProvider.addPart('ui');

        $translateProvider.preferredLanguage('en');
        $translateProvider.useCookieStorage();
    })

    // -------------------------------------------------------------------------
    //  Controllers
    // -------------------------------------------------------------------------
    .controller('HeaderController', function($scope, $http, $cookieStore, CstlConfig, AppConfigService, Account) {

        AppConfigService.getConfig(function(config) {
            $scope.cstlURL  = config.cstl;
            $cookieStore.put('cstlUrl', $scope.cstlURL, {});

            $scope.cstlLoginUrl  = config.cstlLoginURL || 'login.html';

            // default domain
            $cookieStore.put(CstlConfig['cookie.domain.id'], '1', {});

            Account.get({},function(response) {
                //already authenticated, redirect to administration page
                window.location.href="admin.html";
            });
        });
    })
    .controller('FooterController', function($scope,CstlConfig,BuildService) {
        var self = this;
        self.cstlVersion=CstlConfig['cstl.version'];
        $scope.buildInfo = BuildService;
    })

    .controller('LanguageController', function($scope, $translate) {
        $scope.changeLanguage = function(languageKey) {
            $translate.use(languageKey);
        };
    })

    .controller('RegisterController', function() { /* TODO */ })
;
