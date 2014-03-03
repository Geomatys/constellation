'use strict';

/* Services */
function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}
cstlAdminApp.factory('AuthInterceptor', function($cookies) {
    return {
	    'request': function(config) {
	    	if ($cookies.cstlSessionId) {
	    		if(endsWith(config.url+'', ';jsessionid='))
	    	    config.url += $cookies.cstlSessionId;
	    	}
	        return config || $q.when(config);
	    }
	};
});


var context = findWebappContext();




cstlAdminApp.factory('Account', ['$resource',
    function ($resource) {
        return $resource(cstlContext + 'spring/account;jsessionid=', {}, {
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


cstlAdminApp.factory('UserResource', ['$resource', '$cookies',
   function ($resource, $cookies) {
        return $resource(cstlContext+'api/1/user/:id;jsessionid=');
}]);

cstlAdminApp.factory('webService', ['$resource',
                                     function ($resource) {
                                         return $resource(cstlContext+'api/1/admin/instances;jsessionid=', {}, {
                                             'listAll':      {method: 'GET', isArray: false},
                                             'get':          {method: 'GET', url: cstlContext+'api/1/OGC/:type/:id;jsessionid='},
                                             'create':       {method: 'PUT', url: cstlContext+'api/1/OGC/:type;jsessionid='},
                                             'delete':       {method: 'DELETE', url: cstlContext+'api/1/OGC/:type/:id;jsessionid='},
                                             'restart':      {method: 'POST', url: cstlContext+'api/1/OGC/:type/:id/restart;jsessionid='},
                                             'start':        {method: 'POST', url: cstlContext+'api/1/OGC/:type/:id/start;jsessionid='},
                                             'stop':         {method: 'POST', url: cstlContext+'api/1/OGC/:type/:id/stop;jsessionid='},
                                             'metadata':     {method: 'GET', url: cstlContext+'api/1/OGC/:type/:id/metadata;jsessionid='},
                                             'updateMd':     {method: 'POST', url: cstlContext+'api/1/OGC/:type/:id/metadata;jsessionid='},
                                             'config':       {method: 'GET', url: cstlContext+'api/1/OGC/:type/:id/config;jsessionid='},
                                             'logs':         {method: 'GET', url: cstlContext+'api/1/log/:type/:id;jsessionid='},
                                             'capabilities': {method: 'GET', url: cstlContext+'WS/:type/:id;jsessionid=?REQUEST=GetCapabilities&SERVICE=:type'},
                                             'layers' :      {method: 'GET', url: cstlContext+'api/1/MAP/:type/:id/layersummary/all;jsessionid=', isArray: true},
                                             'addLayer':     {method: 'PUT', url: cstlContext+'api/1/MAP/:type/:id/layer;jsessionid='},
                                             'deleteLayer':  {method: 'DELETE', url: cstlContext+'api/1/MAP/:type/:id/:layerid;jsessionid='}
                                         });
                                     }]);

cstlAdminApp.factory('dataListing', ['$resource',
    function ($resource) {
        return $resource(cstlContext+'api/1/data/list/:filter;jsessionid=', {}, {
            'listAll':      {method: 'GET', isArray: true},
            'listCoverage': {method: 'POST', url: cstlContext+'api/1/data/coverage/list/;jsessionid='},
            'pyramidData':  {method: 'POST', url: cstlContext+'api/1/data/pyramid/:id;jsessionid='},
            'dataFolder':   {method: 'POST', url: cstlContext+'api/1/data/datapath;jsessionid=', isArray: true},
            'loadData':     {method: 'POST', url: cstlContext+'api/1/data/load;jsessionid='},
            'extension':    {method: 'POST', url: cstlContext+'api/1/data/testextension;jsessionid='},
            'deleteData':   {method: 'DELETE', url: cstlContext+'api/1/data/:providerid/:dataid;jsessionid='},
            'setMetadata':  {method: 'POST', url: cstlContext+'api/1/data/metadata;jsessionid='},
            'codeLists':    {method: 'GET', url: cstlContext+'api/1/data/metadataCodeLists/:lang;jsessionid='}
        });
    }]);

cstlAdminApp.factory('style', ['$resource',
    function ($resource) {
        return $resource(cstlContext+'api/1/SP/all/style/available;jsessionid=', {}, {
            'listAll': { method: 'GET', isArray: false },
            'delete':  { method: 'DELETE', url: cstlContext+'api/1/SP/:provider/style/:name;jsessionid='},
            'link':    { method: 'POST',   url: cstlContext+'api/1/SP/:provider/style/:name/linkData;jsessionid='},
            'unlink':  { method: 'POST',   url: cstlContext+'api/1/SP/:provider/style/:name/unlinkData;jsessionid='}
        });
    }]);

cstlAdminApp.factory('provider', ['$resource',
    function ($resource) {
        return $resource(cstlContext+'api/1/provider', {}, {
            'create': {method: 'PUT', url: cstlContext+'api/1/provider/:id;jsessionid='},
            'delete': {method: 'DELETE', url: cstlContext+'api/1/provider/:id;jsessionid='}
        });
    }]);


cstlAdminApp.factory('textService', ['$http',
    function ($http){
        return {
            logs : function(type, id){
                return $http.get(cstlContext+'api/1/log/'+type+'/'+id+';jsessionid=');

            },
            capa : function(type, id){
                return $http.get(cstlContext+'WS/'+type+'/'+id+';jsessionid=?REQUEST=GetCapabilities&SERVICE='+type);

            }
        };
    }]);

cstlAdminApp.factory('AuthenticationSharedService', ['$rootScope', '$http', 'authService', '$base64',
    function ($rootScope, $http, authService, $base64) {
        return {
            authenticate: function() {
                $http.get(cstlContext + 'spring/session/status;jsessionid=')
                    .success(function (data, status, headers, config) {
                        $rootScope.$broadcast('event:auth-authConfirmed');
                      //  $http.defaults.headers.common.Authorization = 'Basic ' + $base64.encode(param.username+':'+param.password);
                    });
            },
            logout: function () {
                $rootScope.authenticationError = false;
                $http.get(cstlContext + "spring/session/logout;jsessionid=");
                $http.defaults.headers.common.Authorization = undefined;
                $http.get(context + '/app/logout')
                    .success(function (data, status, headers, config) {
                        authService.loginCancelled();
                    });
            }
        };
    }]);

cstlAdminApp.factory('StyleSharedService', ['$modal', 'style' ,function ($modal, style) {
        return {
            showStyleList : function($scope) {
                var modal = $modal.open({
                    templateUrl: 'views/modalStyleChoose.html',
                    controller: 'StyleModalController',
                    resolve: {
                        exclude: function() { return $scope.selected.TargetStyle },
                        layerName: function() { return $scope.selected.Name },
                        serviceName: function() { return $scope.service.name }

                    }
                });

                modal.result.then(function(item) {
                    if (item) {
                        style.link({
                            provider: item.Provider,
                            name: item.Name
                        }, {
                            values: {
                                dataProvider: $scope.selected.Provider,
                                dataNamespace: "",
                                dataId: $scope.selected.Name
                            }
                        }, function() {
                            $scope.selected.TargetStyle.push(item);
                        });
                    }
                });
            },

            unlinkStyle : function($scope,providerName, styleName, dataProvider, dataId, style) {
                var res = style.unlink({provider: providerName, name: styleName},
                    {values: {dataProvider: dataProvider, dataNamespace: "", dataId: dataId}});
                if (res) {
                    var index = -1;
                    for (var i=0; i < $scope.selected.TargetStyle.length; i++) {
                        var item = $scope.selected.TargetStyle[i];
                        if (item.Provider === providerName && item.Name === styleName) {
                            index = i;
                            break;
                        }
                    }
                    if (index >= 0) {
                        $scope.selected.TargetStyle.splice(index,1);
                    }
                }
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

cstlAdminApp.service('$dashboard', function($filter) {
    return function($scope, fullList) {

        $scope.fullList = fullList || [];
        $scope.dataList = $scope.dataList || [];
        $scope.filtertext = $scope.filtertext || "";
        $scope.filtertype = $scope.filtertype || "";
        $scope.ordertype = $scope.ordertype || "Name";
        $scope.orderreverse = $scope.orderreverse || false;
        $scope.countdata = $scope.countdata || 0;
        $scope.nbbypage = $scope.nbbypage || 10;
        $scope.currentpage = $scope.currentpage || 1;
        $scope.selected = $scope.selected || null;
        $scope.exclude = $scope.exclude || [];

        // Dashboard methods
        $scope.displayPage = function(page) {
            var array = $filter('filter')($scope.fullList, {'Type':$scope.filtertype, '$': $scope.filtertext});
            array = $filter('orderBy')(array, $scope.ordertype, $scope.orderreverse);

            var list = [];
            for (var i = 0; i < array.length; i++) {
                var found = false;
                for (var j = 0; j < $scope.exclude.length; j++) {
                    if ($scope.exclude[j].Name === array[i].Name) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    list.push(array[i]);
                }
            }

            var start = (page - 1) * $scope.nbbypage;

            $scope.currentpage = page;
            $scope.countdata = list.length;
            $scope.dataList = list.splice(start, $scope.nbbypage);
            $scope.selected = null;
        };

        $scope.select = function(item) {
            $scope.selected = item;
        };

        $scope.$watch('nbbypage', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('filtertext', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('filtertype', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('fullList', function() {
            $scope.displayPage(1);
        });
        $scope.$watch('ordertype', function() {
            $scope.displayPage($scope.currentpage);
        });
        $scope.$watch('orderreverse', function() {
            $scope.displayPage($scope.currentpage);
        });
    };
});

cstlAdminApp.service('$growl', function() {
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
});

cstlAdminApp.service('$uploadFiles', function() {
    return {
        files : {file: null, mdFile: null}
    };
});
