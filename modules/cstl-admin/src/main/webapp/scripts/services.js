'use strict';

/* Services */




var context = findWebappContext();

var cstlContext = context=="/cstl-admin"?"constellation/":"";

cstlAdminApp.factory('Account', ['$resource',
    function ($resource) {
        return $resource('app/rest/account', {}, {
        });
    }]);

cstlAdminApp.factory('Password', ['$resource',
    function ($resource) {
        return $resource('app/rest/account/change_password', {}, {
        });
    }]);

cstlAdminApp.factory('Sessions', ['$resource',
    function ($resource) {
        return $resource('app/rest/account/sessions/:series', {}, {
            'get': { method: 'GET', isArray: true}
        });
    }]);

cstlAdminApp.factory('Metrics', ['$resource',
    function ($resource) {
        return $resource(context + '/metrics/metrics', {}, {
            'get': { method: 'GET'}
        });
    }]);

cstlAdminApp.factory('LogsService', ['$resource',
    function ($resource) {
        return $resource('app/rest/logs', {}, {
            'findAll': { method: 'GET', isArray: true},
            'changeLevel':  { method: 'PUT'}
        });
    }]);

cstlAdminApp.factory('webService', ['$resource',
                                     function ($resource) {
                                         return $resource(cstlContext+'api/1/admin/instances', {}, {
                                             'listAll': { method: 'GET', isArray: false},
                                             'get':  { method: 'GET', url: cstlContext+'api/1/OGC/:type/:id'},
                                             'metadata':  { method: 'GET', url: cstlContext+'api/1/OGC/:type/:id/metadata'},
                                             'config':  { method: 'GET', url: cstlContext+'api/1/OGC/:type/:id/config'}

                                         });
                                     }]);

cstlAdminApp.factory('dataListing', ['$resource',
    function ($resource) {
        return $resource(cstlContext+'api/1/data/list/:filter', {}, {
            'listAll': { method: 'GET', isArray: true}
        });
    }]);

cstlAdminApp.factory('AuthenticationSharedService', ['$rootScope', '$http', 'authService',
    function ($rootScope, $http, authService) {
        return {
            authenticate: function() {
                $http.get(context + '/app/rest/authenticate')
                    .success(function (data, status, headers, config) {
                        $rootScope.$broadcast('event:auth-authConfirmed');
                        $http.defaults.headers.common.Authorization = 'Basic YWRtaW46YWRtaW4=';
                    });
            },
            login: function (param) {
                var data ="j_username=" + param.username +"&j_password=" + param.password +"&_spring_security_remember_me=" + param.rememberMe +"&submit=Login";
                $http.post(context + '/app/authentication', data, {
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    ignoreAuthModule: 'ignoreAuthModule'
                }).success(function (data, status, headers, config) {
                    $rootScope.authenticationError = false;
                    authService.loginConfirmed();
                    if(param.success){
                        param.success(data, status, headers, config);
                    }
                    $http.defaults.headers.common.Authorization = 'Basic YWRtaW46YWRtaW4=';
                }).error(function (data, status, headers, config) {
                    console.log("auth error");
                    $rootScope.authenticationError = true;
                    if(param.error){
                        param.error(data, status, headers, config);
                    }
                });
            },
            logout: function () {
                $rootScope.authenticationError = false;
                $http.get(context + '/app/logout')
                    .success(function (data, status, headers, config) {
                        authService.loginCancelled();
                    });
            }
        };
    }]);


cstlAdminApp.directive('pageSwitcher', function() {
    return {
        // Applicable as element/attribute (prefer attribute for IE8 support).
        restrict: 'AE',

        // Declare a new child scope for this directive. Without this line
        // the directive scope will be the current scope, this may cause conflict
        // between multiple directive instances.
        scope: {
            onSelectPage: '&'
        },

        // Directive initialization. Note that there is no controller for this
        // directive because we don't need to expose an API to other directives.
        link: function(scope, element, attrs) {

            // Observe 'page-switcher' attribute changes.
            scope.$parent.$watch(attrs['pageSwitcher'], function(newVal) {
                watchAction(newVal);
            }, true);

            // Handle attributes changes.
            function watchAction(newVal) {
                var page  = newVal.page  || 1,
                    size  = newVal.size  || 10,
                    count = newVal.count || 0;

                // Compute pagination.
                var totalPages = Math.ceil(count / size) || 1,
                    prevCount  = page - 1,
                    nextCount  = totalPages - page,
                    minPage    = page - Math.min(4 - Math.min(2, nextCount), prevCount),
                    maxPage    = page + Math.min(4 - Math.min(2, prevCount), nextCount);

                // Update scope.
                scope.totalPages = totalPages;
                scope.page       = page;
                scope.indexes    = [];
                for (var i = minPage; i <= maxPage; i++) {
                    scope.indexes.push(i);
                }
            }

            // Page select action.
            scope.selectPage = function(page) {
                if (scope.page !== page && page > 0 && page <= scope.totalPages) {
                    scope.onSelectPage({page: page});
                }
            };
        },

        // Replace the element with the following template.
        replace: true,

        // Component HTML template.
        template:
            '<ul class="pagination">' +
                '<li><a ng-click="selectPage(page - 1)">&laquo;</a></li>' +
                '<li ng-repeat="index in indexes" ng-class="{active: index == page}"><a ng-click="selectPage(index)">{{index}}</a></li>' +
                '<li><a ng-click="selectPage(page + 1)">&raquo;</a></li>' +
                '</ul>'
    };
});
