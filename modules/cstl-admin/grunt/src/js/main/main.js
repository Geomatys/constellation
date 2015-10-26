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

    .controller('HeaderController', function ($rootScope, $scope, $http, TokenService, Account, $idle, $modal, CstlConfig,
                                              AppConfigService, Permission) {

        $scope.navigationArray = CstlConfig['cstl.navigation'];

        $scope.permissionService = Permission;

        function closeModals() {
            if ($scope.warning) {
                $scope.warning.close();
                $scope.warning = null;
            }

            if ($scope.timedout) {
                $scope.timedout.close();
                $scope.timedout = null;
            }
        }

        $scope.$on('$idleStart', function() {
            // the user appears to have gone idle
            closeModals();
            $scope.warning = $modal.open({
                templateUrl: 'views/idle/warning-dialog.html',
                windowClass: 'modal-warning'
            });
        });

        $scope.$on('$idleTimeout', function() {
            // the user has timed out (meaning idleDuration + warningDuration has passed without any activity)
            // this is where you'd log them
            closeModals();
            $scope.timedout = $modal.open({
                templateUrl: 'views/idle/timedout-dialog.html',
                windowClass: 'modal-danger'
            });
        });

        $scope.$on('$idleEnd', function() {
            // the user has come back from AFK and is doing stuff. if you are warning them, you can use this to hide the dialog
            closeModals();
            //renew token
            //TokenService.renew();
        });

        $scope.$on('$keepalive', function() {
            // keep the user's session alive
            //renew token
            TokenService.renew();
        });

        $scope.logout = function(){
            $http.delete($scope.cstlLogoutURL).then(function() {
                TokenService.clear();
                window.location.href="index.html";
            });
        };

        $rootScope.hasMultipleDomains = function() {
            return false; //TODO since error happen due to domain refactoring, we return false temporarily, FIXME implements this function
        };

        AppConfigService.getConfig(function(config) {
            $scope.cstlLogoutURL = config.cstlLogoutURL || '@cstl/spring/auth/logout';
            $scope.cstlProfileURL = config.cstlProfileURL || '#/profile';

            var cstlRefreshURL = config.cstlRefreshURL;
            if (cstlRefreshURL) {
                TokenService.setRefreshURL(cstlRefreshURL);
            }

            if (config["token.life"]) {
                TokenService.setTokenLife(config["token.life"]);
            }

            Account.get(function(account){
                $scope.firstname = account.firstname;
                $scope.lastname = account.lastname;
                $rootScope.account = account;
            });
        });
    })

    .controller('FooterController', function($scope,CstlConfig,BuildService) {
        var self = this;
        self.cstlVersion=CstlConfig['cstl.version'];
        $scope.buildInfo = BuildService;
    })

    .controller('MainController', function($scope, $location, webService, dataListing, ProcessService, Growl, UserResource, GeneralService, TaskService) {
        $scope.countStats = function() {
            webService.listAll({}, function(response) {
                var count = 0;
                for (var i=0; i<response.length; i++) {
                    if (response[i].status === 'STARTED' && response[i].type !== 'WEBDAV') {
                        count++;
                    }
                }
                $scope.nbservices = count;
            }, function() {
                $scope.nbservices = 0;
                Growl('error', 'Error', 'Unable to count services');
            });


            dataListing.countAll({}, function(response) {
                $scope.nbdata = response.count;
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

    .controller('UserAccountController', function($scope, $rootScope, $location, $cookieStore, $translate, Growl, cfpLoadingBar, user, roles) {
        $scope.user = user;
        $scope.roles = roles;

        $scope.password = "";
        $scope.password2 = "";

        //disabled role select
        $scope.enableRole = false;

        //update language when update select tag
        $scope.shouldUpdateLanguage = true;

        $scope.save = function(){
            var formData = new FormData(document.getElementById('userForm'));
            $.ajax({
                headers: {
                    'access_token': $rootScope.access_token
                },
                url: $cookieStore.get('cstlUrl') + 'api/1/user/my_account',
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                beforeSend: function(){
                    cfpLoadingBar.start();
                    cfpLoadingBar.inc();
                },
                success: function(result) {
                    Growl('success', 'Success', 'The changes have been successfully applied!');
                    cfpLoadingBar.complete();
                    $location.url('/');
                },
                error: function(result){
                    Growl('error', 'Error', 'Unable to edit user!');
                    cfpLoadingBar.complete();
                }
            });
        };

        $scope.changeLanguage = function(){
            $translate.use($scope.user.locale);
        };
    })

    .controller('LanguageController', function($scope, $translate) {

        $scope.currentLang = $translate.use();

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

