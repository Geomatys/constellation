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
angular.module('cstl-services', ['cstl-restapi'])

    // -------------------------------------------------------------------------
    //  'ngCookies' Substitution (using jQuery Cookie)
    // -------------------------------------------------------------------------

    .factory('$cookieStore', function() {
        return {
            /**
             * @name $cookieStore#get
             *
             * @description
             * Returns the value of given cookie key
             *
             * @param {string} key Id to use for lookup.
             * @returns {string} Cookie value.
             */
            get: function(key) {
                return $.cookie(key);
            },

            /**
             * @name $cookieStore#put
             *
             * @description
             * Sets a value for given cookie key
             *
             * @param {string} key Id for the `value`.
             * @param {string} value Value to be stored.
             * @param {Object} attributes Cookie attributes.
             */
            put: function(key, value, attributes) {
                $.cookie(key, value, attributes);
            },

            /**
             * @name $cookieStore#remove
             *
             * @description
             * Remove given cookie
             *
             * @param {string} key Id of the key-value pair to delete.
             * @param {Object} attributes Cookie attributes.
             */
            remove: function(key, attributes) {
                $.removeCookie(key, attributes);
            }
        };
    })

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
        'cookie.auth.token': 'access_token',

        //cstl version.
        'cstl.version': (new Date()).getFullYear(),

        // Navigation additional buttons
        'cstl.navigation' : [] //keep empty since cstl declare its own links directly in the header, this array is overrided by subprojects.

    })

    // -------------------------------------------------------------------------
    //  Enum raster sld type
    // -------------------------------------------------------------------------
    .constant('rasterstyletype', {
        'none':'none',
        'palette':'palette',
        'cells':'cell',
        'dynamic':'dynamic'
    })

    // -------------------------------------------------------------------------
    //  Constellation Utilities
    // -------------------------------------------------------------------------

    .factory('CstlUtils', function($cookieStore, CstlConfig) {
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
            compileUrl: function(config, url, fillSessionId) {
                // Acquire cookie values.
                var cstlUrl   = $cookieStore.get(CstlConfig['cookie.cstl.url']),
                    domainId  = $cookieStore.get(CstlConfig['cookie.domain.id']),
                    userId    = $cookieStore.get(CstlConfig['cookie.user.id']);

                // Inject cstl-service webapp url.
                if (angular.isDefined(cstlUrl)) {
                    url = url.replace(CstlConfig['inject.expr.ctrl.url'], cstlUrl);
                    if(config){
                      config.headers.access_token = $.cookie(CstlConfig['cookie.auth.token']);
                    }
                }else if (/@cstl/.test(url)){
                  window.location.href="index.html";
                }

                // Inject domain id value.
                if (angular.isDefined(domainId)) {
                    url = url.replace(CstlConfig['inject.expr.domain.id'], domainId);
                }

                // Inject user id value.
                if (angular.isDefined(userId)) {
                    url = url.replace(CstlConfig['inject.expr.user.id'], userId);
                }

                url = url.replace(';jsessionid=', ''); // remove it if no session
    
                return url;
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Authentication HTTP Interceptor
    // -------------------------------------------------------------------------

    .factory('AuthInterceptor', function($rootScope, $q, CstlConfig, CstlUtils) {
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
                    $rootScope.$broadcast('event:auth-cstl-request');
                    
                    // Inject contextual values into request url.
                    config.url = CstlUtils.compileUrl(config, config.url);
                }
                return config;
            },
            'responseError': function(response) {
                if (response.status === 401) {
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

    .factory('TokenService', function ($rootScope, $http, CstlConfig, Account) {
        var lastCall = new Date().getTime();
        var tokenHalfLife = 30 * 60 * 1000;
        return {
            setTokenLife : function(l){
              tokenHalfLife = 500 * 60 * l;
              console.log("Token life set to " + l + " minutes.");
            },
            get : function(){
              console.log("TokenService.get: " + $.cookie(CstlConfig['cookie.auth.token']));
              return $.cookie(CstlConfig['cookie.auth.token']);
            },
            renew: function() {
                $http.get('@cstl/spring/auth/extendToken').success(function(token){
                  $.cookie(CstlConfig['cookie.auth.token'], token, { path : '/' });
                  $rootScope.access_token = token;
                  console.log("Token extended: " + token);
                });
            },
            clear: function(){
              $.removeCookie(CstlConfig['cookie.auth.token'], { path: '/' });
              $.removeCookie(CstlConfig['cookie.user.id'], { path: '/' });
              $.removeCookie(CstlConfig['cookie.domain.id'], { path: '/' });
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
                self.id.unsubscribe();
                //stompClient.unsubscribe(self.id);
                console.log('Unsubscribed from ' + path + ' (' + self.id + ')');
            };
        }

        function Stomper(url){
            var stompClient = null;

            this.connect = function (callback) {
                if (stompClient == null) {
                    var socket = new SockJS(url);
                    stompClient = Stomp.over(socket);
                }
                stompClient.connect({}, function() {
                    console.log('Connected to ' + url);

                    if (callback && typeof(callback) === "function") {
                        callback();
                    }
                });
            };

            this.disconnect = function () {
                if (stompClient != null) {
                    stompClient.disconnect(function () {
                        console.log('Disconnected from ' + url);
                    });
                    stompClient = null;
                }
            };

            this.subscribe = function(path, cb){
                var topic = new Topic(stompClient, path);
                if (stompClient.connected) {
                    topic.id = stompClient.subscribe(topic.path, cb);
                    console.log('Subscribed to ' + topic.path + ' (' + topic.id  + ').');
                } else {
                    this.connect(function() {
                        topic.id = stompClient.subscribe(topic.path, cb);
                        console.log('Subscribed to ' + topic.path + ' (' + topic.id  + ').');
                    });
                }
                return topic;
            };
        }

        return new Stomper(CstlUtils.compileUrl(null, '@cstl/spring/ws/adminmessages', false));
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
            scope.wrap.matchExactly = scope.wrap.matchExactly || false;
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
                    var match = false;
                    if(scope.wrap.filtertext){
                        match=scope.wrap.matchExactly;
                    }
                    array = $filter('filter')(scope.wrap.fullList, {'Type':scope.wrap.filtertype, '$': scope.wrap.filtertext},match);
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

            scope.$watch('wrap.nbbypage+wrap.filtertext+wrap.filtertype+wrap.fullList', function() {
                scope.displayPage(1);
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
            showStyleList : function($scope,selected) {
                var modal = $modal.open({
                    templateUrl: 'views/style/modalStyleChoose.html',
                    controller: 'StyleModalController',
                    resolve: {
                        exclude: function() { return selected.TargetStyle; },
                        selectedLayer: function() { return selected; },
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
                                {values: {layerId: selected.Name, spId: 'sld', styleName: item.Name}},
                                function() {
                                    selected.TargetStyle.push(item);
                                    $scope.showLayerDashboardMap();
                                    Growl('success','Success','Style updated for layer '+ selected.Name);
                                }, function() { Growl('error','Error','Unable to update style for layer '+ selected.Name); }
                            );
                        } else {
                            style.link({
                                provider: item.Provider,
                                name: item.Name
                            }, {
                                values: {
                                    dataProvider: selected.Provider,
                                    dataNamespace: selected.Namespace,
                                    dataId: selected.Name
                                }
                            }, function () {
                                selected.TargetStyle.push(item);
                                $scope.showDataDashboardMap();
                            });
                        }
                    }
                });
            },

            unlinkStyle : function($scope,providerName, styleName, dataProvider, dataId, style, selected) {
                if ($scope.service) {
                    webService.removeLayerStyle({type: $scope.service.type, id: $scope.service.identifier},
                        {values: {layerId: selected.Name, spId: 'sld', styleName: styleName}},
                        function() {
                            for (var i=0; i<selected.TargetStyle.length; i++) {
                                var s = selected.TargetStyle[i];
                                if (s.Name === styleName) {
                                    selected.TargetStyle.splice(i, 1);
                                    break;
                                }
                            }
                            $scope.showLayerDashboardMap();
                        }, function() { Growl('error','Error','Unable to update style for layer '+ selected.Name); }
                    );
                } else {
                    var res = style.unlink({provider: providerName, name: styleName},
                        {values: {dataProvider: dataProvider, dataNamespace: selected.Namespace, dataId: dataId}});
                    if (res) {
                        var index = -1;
                        for (var i = 0; i < selected.TargetStyle.length; i++) {
                            var item = selected.TargetStyle[i];
                            if (item.Provider === providerName && item.Name === styleName) {
                                index = i;
                                break;
                            }
                        }
                        if (index >= 0) {
                            selected.TargetStyle.splice(index, 1);
                        }
                        $scope.showDataDashboardMap();
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
                            scope.wrap.fullList = response;
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
                            scope.wrap.fullList = response;
                            scope.previewStyledData(null,false);
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
                modal.result.then(function(item) {
                    if(typeof scope.showDataDashboardMap === 'function'){
                        scope.showDataDashboardMap();
                    }
                    if(typeof scope.previewStyledData === 'function'){
                        scope.previewStyledData(null,false);
                    }
                });
            }
        };
    })
    
    .factory('interval', function() {
        /**
         * An helper service to call an action on demand with a fixed time
         * interval between two calls.
         *
         * @param fn {function} the action to call
         * @param interval {number} the interval in milliseconds
         */
        return function(fn, interval) {

            // The last execution timestamp.
            var lastTime = null;

            // Calls the specified function if the configured interval is passed.
            return function() {
                var time = new Date().getTime();
                if (!lastTime || (time - lastTime) > interval) {
                    fn.apply(null, arguments);
                    lastTime = time;
                }
            };
        };
    })

    // -------------------------------------------------------------------------
    //  Upload File
    // -------------------------------------------------------------------------

    .service('UploadFiles', function() {
        return {
            files : {file: null, mdFile: null}
        };
    })

    // -------------------------------------------------------------------------
    //  DashboardHelper
    // -------------------------------------------------------------------------

    .factory('DashboardHelper', function($timeout) {
        /**
         * An helper service to manage dashboard features.
         *
         * Provides methods and variables to manage :
         *  - text filter
         *  - column sorting
         *  - request status
         *  - pagination
         *  - item selection
         *
         * Query structure : { page: 1, size: 20, text: 'mytext', sort: { order: 'ASC', field: 'myfield' } }
         *
         * @constructor
         * @param {Function} searchMethod The search method to call.
         * @param {Function} selectMethod The search method to call.
         * @param {Object} [defaultQuery] The initial query for search (default is { page: 1, size: 20 }).
         */
        function DashboardHelper(searchMethod, selectMethod, defaultQuery) {

            var self = this;

            var timeout = null;


            // States on the search request status (-1 pending, 0 not sent, 1 success, 2 error).
            self.searchStatus = 0;

            // Search request criteria.
            self.query = angular.extend({ page: 1, size: 20 }, angular.copy(defaultQuery));

            // Search result page.
            self.page = null;

            // States on the selection request status (-1 pending, 0 not sent, 1 success, 2 error).
            self.selectionStatus = 0;

            // Selection result object.
            self.selection = null;


            // Sends the search query and gets the results.
            self.search = function() {
                self.searchStatus = -1;
                searchMethod(self.query,
                    function success(data) {
                        self.searchStatus = 1;
                        self.page = data;
                    },
                    function error() {
                        self.searchStatus = 2;
                        self.page = null;
                    });
            };

            // Avoids too much HTTP requests on 'keyup' event for text filter.
            self.searchTimeout = function(ms) {
                self.searchStatus = -1;
                self.query.page = 1;
                $timeout.cancel(timeout);
                timeout = $timeout(self.search, ms || 300);
            };

            // Selects an item requesting its details.
            self.select = function(item, idField) {
                self.selectionStatus = -1;
                selectMethod({ id: item[idField || 'id'] },
                    function success(data) {
                        self.selectionStatus = 1;
                        self.selection = data;
                    },
                    function error() {
                        self.selectionStatus = 2;
                        self.selection = null;
                    });
            };

            // Modify the sort order for result items.
            self.sortBy = function(field) {
                if (self.isSortedBy(field)) {
                    switch (self.query.sort.order) {
                        case 'ASC':
                            self.query.sort.order = 'DESC';
                            break;
                        case 'DESC':
                            self.query.sort.order = 'ASC';
                            break;
                        default:
                            self.query.sort.order = 'DESC';
                            break;
                    }
                } else {
                    self.query.sort = { field: field, order: 'DESC' };
                }
                self.search();
            };

            // Checks if the result items are sorted on the specified field.
            self.isSortedBy = function(field) {
                return self.query.sort && (self.query.sort.field === field);
            };

            // Returns the icon class to apply according the current sort order.
            self.getOrderIcon = function(field) {
                if (self.isSortedBy(field)) {
                    switch (self.query.sort.order) {
                        case 'ASC':
                            return 'fa-caret-up';
                        case 'DESC':
                            return 'fa-caret-down';
                    }
                }
                return null;
            };

            // Changes the page index for results.
            self.setPage = function(page) {
                self.query.page = page;
                self.search();
            };

            // Reset search criteria.
            self.resetCriteria = function() {
                self.query = angular.extend({ page: 1, size: 20 }, angular.copy(defaultQuery));
            };
        }

        return DashboardHelper;
    })

    // -------------------------------------------------------------------------
    //  View resolver
    // -------------------------------------------------------------------------

    .factory('viewResolve', function($q, $injector, $http, $templateCache, $sce) {
        /**
         * Based on "ngRoute" angular module source code.
         *
         * @param {Object} config the view configuration
         * @param {Object} [resolveLocals] variables to be injected in "resolve" functions
         * @see https://code.angularjs.org/1.2.17/angular-route.js
         */
        function viewResolve(config, resolveLocals) {
            var locals = angular.extend({}, config.resolve),
                template, templateUrl;

            angular.forEach(locals, function(value, key) {
                if (angular.isString(value)) {
                    locals[key] = $injector.get(value);
                } else {
                    locals[key] = $injector.invoke(value, null, resolveLocals);
                }
            });

            if (angular.isDefined(template = config.template)) {
                if (angular.isFunction(template)) {
                    template = $injector.invoke(template, null, resolveLocals);
                }
            } else if (angular.isDefined(templateUrl = config.templateUrl)) {
                if (angular.isFunction(templateUrl)) {
                    templateUrl = $injector.invoke(templateUrl, null, resolveLocals);
                }
                templateUrl = $sce.getTrustedResourceUrl(templateUrl);
                if (angular.isDefined(templateUrl)) {
                    template = $http.get(templateUrl, {cache: $templateCache}).
                        then(function(response) { return response.data; });
                }
            }
            if (angular.isDefined(template)) {
                locals.$template = template;
            }

            return $q.all(locals);
        }

        return viewResolve;
    })


    // -------------------------------------------------------------------------
    //  Style service : get available styles with a cache support
    // -------------------------------------------------------------------------
    .service('StyleService', function(TaskService) {

        var self = this;
        self.styles = null;

        function init() {
            self.styles = TaskService.listStyle();
        }

        self.getStyles = function() {
            if (self.styles === null) {
                init();
            }
            return self.styles;
        };

        self.refresh = function() {
            init();
        };

    })

    // -------------------------------------------------------------------------
    //  CRS service
    // -------------------------------------------------------------------------
    .service('EPSGService', function(crs) {

        var self = this;
        self.epsgCodes = null;

        function init() {
            self.epsgCodes = crs.listAll();
        }

        self.getEPSGCodes = function() {
            if (self.epsgCodes === null) {
                init();
            }
            return self.epsgCodes;
        };

        self.refresh = function() {
            init();
        };
    })

    // -------------------------------------------------------------------------
    //  OGC WS service
    // -------------------------------------------------------------------------
    .service('OGCWSService', function(TaskService) {

        var self = this;
        self.services = null;

        function init() {
            self.services = TaskService.listService();
        }

        self.getAllServices = function() {
            if (self.services === null) {
                init();
            }
            return self.services;
        };

        self.refresh = function() {
            init();
        };
    })

    // -------------------------------------------------------------------------
    //  Data service
    // -------------------------------------------------------------------------
    .service('DataService', function(TaskService) {

        var self = this;
        self.dataset = null;

        function initDatasets() {
            self.dataset = TaskService.listDataset();
        }

        self.getAllDatasets = function() {
            if (self.dataset === null) {
                initDatasets();
            }
            return self.dataset;
        };

        self.refresh = function() {
            initDatasets();
        };
    })
;
