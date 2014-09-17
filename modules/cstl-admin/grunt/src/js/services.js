/*
 * Constellation - An open source and standard compliant SDI
 *
 *     http://www.constellation-sdi.org
 *
 *     Copyright 2014 Geomatys
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @require angular.js
 * @require angular-cookies.js
 * @require app-restapi.js
 */
angular.module('cstl-services', ['ngCookies', 'cstl-restapi'])

    // -------------------------------------------------------------------------
    //  Constellation Configuration Properties
    // -------------------------------------------------------------------------

    .constant('CstlConfig', {
        // Injection expressions.
        'inject.expr.ctrl.url':  '@cstl/',
        'inject.expr.domain.id': '$domainId',
        'inject.expr.user.id':   '$userId',

        // Cookies.
        'cookie.cstl.url':   'cstlUrl',
        'cookie.domain.id':  'cstlActiveDomainId',
        'cookie.user.id':    'cstlUserId',
        'cookie.session.id': 'cstlSessionId'
    })

    // -------------------------------------------------------------------------
    //  Constellation Utilities
    // -------------------------------------------------------------------------

    .factory('CstlUtils', function($cookies, CstlConfig) {
        return {
            /**
             * Return the webapp context path.
             *
             * @return {String} the webapp context path
             */
            getContextPath: function() {
                var path = window.location.pathname;
                if (path === '/') {
                    return path;
                }
                return path.substring(0, path.indexOf('/', 1));
            },

            /**
             * Injects contextual values into the specified url template.
             *
             * Available value expression for contextual values injection could
             * be defined using the 'CstlConfig' service using following properties :
             *  - inject.expr.ctrl.url
             *  - inject.expr.domain.id
             *  - inject.expr.user.id
             *
             * @param url {String} the url template to compile
             * @param fillSessionId {Boolean} indicates if should fill the jsessionid
             * @returns {String} the complied url
             */
            compileUrl: function(url, fillSessionId) {
                // Acquire cookie values.
                var cstlUrl   = $cookies[CstlConfig['cookie.cstl.url']],
                    domainId  = $cookies[CstlConfig['cookie.domain.id']],
                    userId    = $cookies[CstlConfig['cookie.user.id']],
                    sessionId = $cookies[CstlConfig['cookie.session.id']];

                // Inject cstl-service webapp url.
                if (angular.isDefined(cstlUrl)) {
                    url = url.replace(CstlConfig['inject.expr.ctrl.url'], cstlUrl);
                }

                // Inject domain id value.
                if (angular.isDefined(domainId)) {
                    url = url.replace(CstlConfig['inject.expr.domain.id'], domainId);
                }

                // Inject user id value.
                if (angular.isDefined(userId)) {
                    url = url.replace(CstlConfig['inject.expr.user.id'], userId);
                }

                // Inject jsessionid value.
                if (angular.isDefined(sessionId)) {
                    if (url.indexOf(';jsessionid=') !== -1) {
                        url = url.replace(';jsessionid=', ';jsessionid=' + sessionId);
                    } else if (fillSessionId !== false) {
                        var reqIndex = url.indexOf('?');
                        if (reqIndex !== -1) {
                            url = url.replace('?', ';jsessionid=' + sessionId + '?');
                        } else {
                            url = url + ';jsessionid=' + sessionId;
                        }
                    }
                } else {
                    url = url.replace(';jsessionid=', ''); // remove it if no session
                }

                return url;
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Authentication HTTP Interceptor
    // -------------------------------------------------------------------------

    .factory('AuthInterceptor', function($rootScope, $q, $cookies, $cookieStore, CstlConfig, CstlUtils) {
        /**
         * Checks if an user is/was logged. This method can return true even
         * the user session has expired.
         *
         * @return {Boolean} true if an user is/was logged, otherwise false
         */
        function isUserLogged() {
            return angular.isDefined($cookies[CstlConfig['cookie.cstl.url']]);
        }

        /**
         * Checks if the request url destination is the Constellation REST API.
         *
         * @return {Boolean}
         */
        function isCstlRequest(url) {
            return url.indexOf(CstlConfig['inject.expr.ctrl.url']) === 0;
        }

        return {
            'request': function(config) {
                // Intercept request to Constellation REST API.
                if (isCstlRequest(config.url)) {

                    // Broadcast 'event:auth-loginRequired' event and cancel request
                    // if no authentication.
                    if (!isUserLogged()) {
                        $rootScope.$broadcast('event:auth-loginRequired');
                        return; // auth required cancel request
                    }

                    // Inject contextual values into request url.
                    config.url = CstlUtils.compileUrl(config.url);
                }
                return config;
            },
            'responseError': function(response) {
                if (response.status === 401) {
                    // Remove constellation session id cookie.
                    $cookieStore.remove($cookies[CstlConfig['cookie.session.id']]);

                    // Broadcast 'event:auth-loginRequired' event.
                    $rootScope.$broadcast('event:auth-loginRequired');
                }
                return $q.reject(response);
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Authentication Service
    // -------------------------------------------------------------------------

    .factory('AuthService', function ($rootScope, $http, $cookieStore, Account) {
        return {
            authenticate: function() {
                Account.get(function(account) {
                    $rootScope.account = account;

                    $rootScope.hasRole = function(role) {
                        return account.roles.indexOf(role) !== -1;
                    };

                    $rootScope.hasMultipleDomains = function() {
                        return account.domains.length > 1;
                    };

                    $rootScope.$broadcast('event:auth-authConfirmed');
                });
            },

            logout: function () {
                $http.get('@cstl/spring/session/logout;jsessionid=').then(function() {
                    $cookieStore.remove('cstlSessionId');
                    $http.get('/app/logout').success(function() {
                        $rootScope.$broadcast('event:auth-loginCancelled');
                    });
                });
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Growl Service
    // -------------------------------------------------------------------------

    .factory('Growl', function() {
        /**
         * Displays a notification with the specified title and text.
         *
         * @param type  - {string} the notification type (info|error|success|warning)
         * @param title - {string} the notification title
         * @param msg   - {string} the notification message
         */
        return function(type, title, msg) {
            if (type === 'info') {
                $.growl({title: title, message: msg});
            } else if (type === 'error') {
                $.growl.error({title: title, message: msg});
            } else if (type === 'success') {
                $.growl.notice({title: title, message: msg});
            } else if (type === 'warning') {
                $.growl.warning({title: title, message: msg});
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Stomp WebSocket Service
    // -------------------------------------------------------------------------
    
    .factory('StompService', function(CstlUtils) {


        function Topic(stompClient, path) {
            var self = this;

            this.path = path;

            this.unsubscribe = function() {
                stompClient.unsubscribe(self.id);
                console.log('Unsubscribed from ' + path + ' (' + self.id + ')');
            };
        }

        function Stomper(url){
            var socket = new SockJS(url),
                stompClient = Stomp.over(socket);

            this.subscribe = function(path, cb){
                var topic = new Topic(stompClient, path);
                if (stompClient.connected) {
                    topic.id = stompClient.subscribe(topic.path, cb);
                    console.log('Subscribed to ' + topic.path + ' (' + topic.id  + ').');
                } else {
                    stompClient.connect({}, function() {
                        console.log('Connected to ' + url);
                        topic.id = stompClient.subscribe(topic.path, cb);
                        console.log('Subscribed to ' + topic.path + ' (' + topic.id  + ').');
                    });
                }
                return topic;
            };
        }

        return new Stomper(CstlUtils.compileUrl('@cstl/spring/ws/adminmessages', false));
    })

    // -------------------------------------------------------------------------
    //  Dashboard Helper
    // -------------------------------------------------------------------------

    .factory('Dashboard', function($filter) {
        return function(scope, fullList, filterOnType) {
            scope.wrap = scope.wrap || {};
            scope.service = scope.service || null;
            scope.wrap.fullList = fullList || [];
            scope.wrap.dataList = scope.wrap.dataList || [];
            scope.wrap.filtertext = scope.wrap.filtertext || "";
            scope.wrap.filtertype = scope.wrap.filtertype || undefined;
            scope.wrap.ordertype = scope.wrap.ordertype || (scope.service && scope.service.type && scope.service.type.toLowerCase()==='sos') ? "id" : (scope.service && scope.service.type && scope.service.type.toLowerCase==='csw') ? "title" : "Name";
            scope.wrap.orderreverse = scope.wrap.orderreverse || false;
            scope.wrap.countdata = scope.wrap.countdata || 0;
            scope.wrap.nbbypage = scope.wrap.nbbypage || 10;
            scope.wrap.currentpage = scope.wrap.currentpage || 1;
            scope.selected = scope.selected || null;
            scope.selectedDS = scope.selectedDS || null;
            scope.exclude = scope.exclude || [];

            // Dashboard methods
            scope.displayPage = function(page) {
                var array;
                if (filterOnType) {
                    array = $filter('filter')(scope.wrap.fullList, {'Type':scope.wrap.filtertype, '$': scope.wrap.filtertext});
                } else {
                    array = $filter('filter')(scope.wrap.fullList, {'$': scope.wrap.filtertext});
                }
                array = $filter('orderBy')(array, scope.wrap.ordertype, scope.wrap.orderreverse);

                var list = [];
                for (var i = 0; i < array.length; i++) {
                    var found = false;
                    for (var j = 0; j < scope.exclude.length; j++) {
                        if (scope.service && scope.service.type.toLowerCase() === 'sos') {
                            if (scope.exclude[j].id === array[i].Name) {
                                found = true;
                                break;
                            }
                        } else {
                            if (scope.exclude[j].Name === array[i].Name) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        list.push(array[i]);
                    }
                }

                var start = (page - 1) * scope.wrap.nbbypage;

                scope.wrap.currentpage = page;
                scope.wrap.countdata = list.length;
                scope.wrap.dataList = list.splice(start, scope.wrap.nbbypage);
            };

            scope.select = scope.select || function(item) {
                if (scope.selected === item) {
                    scope.selected = null;
                } else {
                    scope.selected = item;
                }
            };

            scope.selectDS = function(item) {
                if (item && scope.selectedDS && scope.selectedDS.id === item.id) {
                    scope.selectedDS = null;
                } else {
                    scope.selectedDS = item;
                }
            };

            scope.$watch('wrap.nbbypage+wrap.filtertext+wrap.filtertype', function() {
                scope.displayPage(1);
            },true);

            scope.$watch('wrap.fullList', function() {
                scope.displayPage(1);
                scope.selected = null;
            },true);

            scope.$watch('wrap.ordertype+wrap.orderreverse', function() {
                scope.displayPage(scope.wrap.currentpage);
            },true);
        };
    })

    // -------------------------------------------------------------------------
    //  Style Service
    // -------------------------------------------------------------------------

    .factory('StyleSharedService', function($modal, style, webService, Growl) {
        return {
            showStyleList : function($scope) {
                var modal = $modal.open({
                    templateUrl: 'views/style/modalStyleChoose.html',
                    controller: 'StyleModalController',
                    resolve: {
                        exclude: function() { return $scope.selected.TargetStyle; },
                        selectedLayer: function() { return $scope.selected; },
                        selectedStyle: function() { return null; },
                        serviceName: function() {
                            if ($scope.service) {
                                // In WMS mode
                                return $scope.service.name;
                            }
                            // For portraying
                            return null;
                        },
                        newStyle: function() { return null; },
                        stylechooser: function(){return null;}
                    }
                });

                modal.result.then(function(item) {
                    if (item) {
                        if ($scope.service) {
                            webService.updateLayerStyle({type: $scope.service.type, id: $scope.service.identifier},
                                {values: {layerId: $scope.selected.Name, spId: 'sld', styleName: item.Name}},
                                function() {
                                    $scope.selected.TargetStyle.push(item);
                                    Growl('success','Success','Style updated for layer '+ $scope.selected.Name);
                                }, function() { Growl('error','Error','Unable to update style for layer '+ $scope.selected.Name); }
                            );
                        } else {
                            style.link({
                                provider: item.Provider,
                                name: item.Name
                            }, {
                                values: {
                                    dataProvider: $scope.selected.Provider,
                                    dataNamespace: $scope.selected.Namespace,
                                    dataId: $scope.selected.Name
                                }
                            }, function () {
                                $scope.selected.TargetStyle.push(item);
                            });
                        }
                    }
                });
            },

            unlinkStyle : function($scope,providerName, styleName, dataProvider, dataId, style) {
                if ($scope.service) {
                    webService.removeLayerStyle({type: $scope.service.type, id: $scope.service.identifier},
                        {values: {layerId: $scope.selected.Name, spId: 'sld', styleName: styleName}},
                        function() {
                            for (var i=0; i<$scope.selected.TargetStyle.length; i++) {
                                var s = $scope.selected.TargetStyle[i];
                                if (s.Name === styleName) {
                                    $scope.selected.TargetStyle.splice(i, 1);
                                    break;
                                }
                            }
                        }, function() { Growl('error','Error','Unable to update style for layer '+ $scope.selected.Name); }
                    );
                } else {
                    var res = style.unlink({provider: providerName, name: styleName},
                        {values: {dataProvider: dataProvider, dataNamespace: $scope.selected.Namespace, dataId: dataId}});
                    if (res) {
                        var index = -1;
                        for (var i = 0; i < $scope.selected.TargetStyle.length; i++) {
                            var item = $scope.selected.TargetStyle[i];
                            if (item.Provider === providerName && item.Name === styleName) {
                                index = i;
                                break;
                            }
                        }
                        if (index >= 0) {
                            $scope.selected.TargetStyle.splice(index, 1);
                        }
                    }
                }
            },

            showStyleCreate : function(scope) {
                var modal = $modal.open({
                    templateUrl: 'views/style/modalStyleCreate.html',
                    controller: 'StyleModalController',
                    resolve: {
                        newStyle: function() { return null; },
                        pageSld: function() {  return 'views/style/chooseType.html'; },
                        selectedLayer: function() {  return null; },
                        selectedStyle: function() { return null; },
                        serviceName: function() {  return null; },
                        exclude: function() {  return null; },
                        stylechooser: function(){return null;}
                    }
                });
                modal.result.then(function(item) {
                    if (scope) {
                        style.listAll({provider: 'sld'}, function(response) {
                            scope.wrap.fullList = response.styles;
                        });
                    }
                });
            },

            showStyleEdit : function(scope, response) {
                var modal = $modal.open({
                    templateUrl: 'views/style/modalStyleEdit.html',
                    controller: 'StyleModalController',
                    resolve: {
                        newStyle: function() { return response;},
                        selectedLayer: function() {  return null; },
                        selectedStyle: function() { return scope.selected; },
                        serviceName: function() {  return null; },
                        exclude: function() {  return null; },
                        stylechooser: function(){return null;}
                    }
                });
                modal.result.then(function(item) {
                    if (scope) {
                        style.listAll({provider: 'sld'}, function(response) {
                            scope.wrap.fullList = response.styles;
                        });
                    }
                });
            },

            editLinkedStyle : function(scope, response, selectedData) {
                var modal = $modal.open({
                    templateUrl: 'views/style/modalStyleEdit.html',
                    controller: 'StyleModalController',
                    resolve: {
                        newStyle: function() { return response;},
                        selectedLayer: function() {  return selectedData; },
                        selectedStyle: function() { return null; },
                        serviceName: function() {  return null; },
                        exclude: function() {  return null; },
                        stylechooser: function(){return 'edit';}
                    }
                });
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Upload File
    // -------------------------------------------------------------------------

    .service('UploadFiles', function() {
        return {
            files : {file: null, mdFile: null}
        };
    });
