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
