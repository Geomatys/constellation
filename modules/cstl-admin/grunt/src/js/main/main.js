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

/* Controllers */
/*jshint -W079 */
var dataNotReady = function(){alert("data not ready");};

angular.module('cstl-main', ['cstl-restapi', 'cstl-services', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .controller('HeaderController', function ($rootScope, $scope, $http, TokenService, Account) {
        $http.get("app/conf").success(function(data){
            $scope.logout = function(){
              $http.delete('@cstl/api/user/logout').then(function() {
                TokenService.clear();
                window.location.href="index.html";
                });
            };
            if(data["token.life"]){
              TokenService.setTokenLife(data["token.life"]);
            }
            Account.get(function(account){
              $scope.firstname = account.firstname;
              $scope.lastname = account.lastname;  
              $rootScope.account = account;

              $rootScope.hasRole = function(role) {
                 return account.roles.indexOf(role) !== -1;
              };

              $rootScope.hasMultipleDomains = function() {
                 return account.domains.length > 1;
              };             
            });
            
        });
    })

    .controller('MainController', function($scope, $location, webService, dataListing, ProcessService, Growl, UserResource, GeneralService, TaskService) {
        $scope.countStats = function() {
            webService.listAll({}, function(response) {
                var count = 0;
                for (var i=0; i<response.instance.length; i++) {
                    if (response.instance[i].status === 'STARTED' && response.instance[i].type !== 'WEBDAV') {
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

            TaskService.countProcess({}, function(response) {
                $scope.nbprocess = response.map.value;
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
    })

    .controller('LanguageController', function($scope, $translate) {

        $scope.currentLang = 'en';

        $scope.changeLanguage = function () {
            $translate.use($scope.currentLang);
        };
    })

    .controller('MenuController', function($scope) {

    })

    .controller('ModalConfirmController', function($scope,keyMsg) {
        $scope.keyMsg = keyMsg;
    })

    .controller('LoginController', function($scope, $location, AuthService) {
        $scope.rememberMe = true;
        $scope.login = function () {
            AuthService.login({
                username: $scope.username,
                password: $scope.password,
                rememberMe: $scope.rememberMe,
                success: function () {
                    $location.path('');
                }
            });
        };
    })

    .controller('ModalInstanceCtrl', function($scope, $modalInstance){
        $scope.ok = function () {
            $modalInstance.close();
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };
    })

    .controller('SettingsController', function($scope, resolvedAccount, Account) {
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
    })

    .controller('SessionsController', function($scope, resolvedSessions, Sessions) {
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
    })

    .controller('navCtrl', function($scope, $location) {
        $scope.navClass = function (page) {
            var currentRoute = $location.path().split('/')[1] || 'home';
            return page === currentRoute ? 'menu-selected' : '';
        };
    })

    .controller('DomainSwitcherController', function(Account, $scope, $cookieStore, $window) {
        Account.get(function(account){
            $scope.domains = account.domains;
            for(var d in account.domains){
                if(account.domains[d].id === $cookieStore.get('cstlActiveDomainId')){
                    $scope.activeDomain = account.domains[d].name;
                    break;
                }
            }
            $scope.changeDomain = function(i){
                if($cookieStore.get('cstlActiveDomainId') !== account.domains[i].id){
                    $scope.activeDomain = account.domains[i].name;
                    $cookieStore.put('cstlActiveDomainId', '' + account.domains[i].id);
                    $window.location.href="admin.html";
                }
            };
        });
    });

