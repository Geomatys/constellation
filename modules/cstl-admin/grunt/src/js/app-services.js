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

angular.module('cstl-services', [])

    // -------------------------------------------------------------------------
    //  Constellation Configuration
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
             * be defined using the 'Config' service using following properties :
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

    .factory('AuthInterceptor', function($rootScope, $cookies, CstlConfig, CstlUtils) {
        return {
            'request': function(config) {
                // Intercept request to Constellation REST API.
                if (isCstlRequest(config.url)) {

                    // Broadcast 'event:auth-loginRequired' event and cancel request
                    // if no authentication.
                    if (!isUserLogged()) {
                        $rootScope.$broadcast('event:auth-loginRequired', null);
                        return; // auth required cancel request
                    }

                    // Inject contextual values into request url.
                    config.url = CstlUtils.compileUrl(config.url);
                }
                return config;
            }
        };

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
    })

    // -------------------------------------------------------------------------
    //  Authentication Service
    // -------------------------------------------------------------------------

    .factory('AuthenticationSharedService', function ($rootScope, $http, $cookieStore, Account, CstlUtils, authService) {
        return {
            authenticate: function() {
                Account.get(function(account) {
                    $rootScope.account = account;

                    $rootScope.hasRole = function(role) {
                        return account.roles.indexOf(role) != -1;
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
                        authService.loginCancelled();
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
        return new Stomper(CstlUtils.compileUrl('@cstl/spring/ws/adminmessages', false));

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
                    stompClient.connect('', '', function() {
                        console.log('Connected to ' + url);
                        topic.id = stompClient.subscribe(topic.path, cb);
                        console.log('Subscribed to ' + topic.path + ' (' + topic.id  + ').');
                    });
                }
                return topic;
            };
        }
    });