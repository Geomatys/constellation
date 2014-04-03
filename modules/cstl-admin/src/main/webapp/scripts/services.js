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

/* Services */
function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}
/*
 * URL starting with this prefix will be rewriten to constellation backend.
 */
var cstlUrlPrefix = "@cstl/";

/*
 * Injection of jessionid for csltSessionId cookie.
 */
cstlAdminApp.factory('AuthInterceptor', function($cookies) {
    return {
	    'request': function(config) {
	    	var url = config.url+'';
	    	if(url.indexOf(cstlUrlPrefix) == 0){
	    	  url = $cookies.cstlUrl + url.substring(cstlUrlPrefix.length);
	    	  var jsessionIdIndex = url.indexOf(";jsessionid=");
	    	  if(jsessionIdIndex != -1){
	    	    var cstlSessionId=$cookies.cstlSessionId;
    	    	if (cstlSessionId) {
    	    		config.url = url.replace(";jsessionid=", ";jsessionid=" + cstlSessionId);
	        	}else{
	        	  var i = url.indexOf(';jsessionid=');
	        	  var l = ';jsessionid='.length;
	        		config.url = url.substring(0, i) + url.substring(i+l)
	        	}
   	    	}else{
   	    	 config.url = url;
   	    	}
	    	}
	        return config || $q.when(config);
	    }
	};
});


var context = findWebappContext();




cstlAdminApp.factory('Account', ['$resource',
    function ($resource) {
        return $resource('@cstl/spring/account;jsessionid=', {}, {
        });
    }]);

cstlAdminApp.factory('Contact', ['$resource',
         function ($resource) {
             return $resource( '@cstl/spring/admin/contact;jsessionid=', {}, {
            	 save: {method:'PUT'}
         });
}]);

cstlAdminApp.factory('ProcessService', ['$resource', '$cookies',
   function($resource, $cookies) {
       	return $resource('@cstl/spring/admin/process;jsessionid=', {}, {
       		'get' : {method : 'GET',isArray : true}
       	});
} ]);


cstlAdminApp.factory('Sessions', ['$resource',
    function ($resource) {
        return $resource('app/rest/account/sessions/:series', {}, {
            'get': { method: 'GET', isArray: true}
        });
    }]);

cstlAdminApp.factory('Metrics', ['$resource',
    function ($resource) {
        return $resource('@cstl/metrics/metrics;jsessionid=', {}, {
            'get': { method: 'GET'}
        });
    }]);

cstlAdminApp.factory('LogsService', ['$resource',
    function ($resource) {
        return $resource('@cstl/spring/rest/logs;jsessionid=', {}, {
            'findAll': { method: 'GET', isArray: true},
            'changeLevel':  { method: 'PUT'}
        });
    }]);


cstlAdminApp.factory('UserResource', ['$resource', '$cookies',
   function ($resource, $cookies) {
        return $resource('@cstl/api/1/user/:id;jsessionid=');
}]);

cstlAdminApp.factory('webService', ['$resource',
                                     function ($resource) {
                                         return $resource('@cstl/api/1/admin/instances;jsessionid=', {}, {
                                             'listAll':      {method: 'GET', isArray: false},
                                             'get':          {method: 'GET', url: '@cstl/api/1/OGC/:type/:id;jsessionid='},
                                             'create':       {method: 'PUT', url: '@cstl/api/1/OGC/:type;jsessionid='},
                                             'delete':       {method: 'DELETE', url: '@cstl/api/1/OGC/:type/:id;jsessionid='},
                                             'restart':      {method: 'POST', url: '@cstl/api/1/OGC/:type/:id/restart;jsessionid='},
                                             'start':        {method: 'POST', url: '@cstl/api/1/OGC/:type/:id/start;jsessionid='},
                                             'stop':         {method: 'POST', url: '@cstl/api/1/OGC/:type/:id/stop;jsessionid='},
                                             'metadata':     {method: 'GET', url: '@cstl/api/1/OGC/:type/:id/metadata;jsessionid='},
                                             'updateMd':     {method: 'POST', url: '@cstl/api/1/OGC/:type/:id/metadata;jsessionid='},
                                             'config':       {method: 'GET', url: '@cstl/api/1/OGC/:type/:id/config;jsessionid='},
                                             'setConfig':    {method: 'POST', url: '@cstl/api/1/OGC/:type/:id/config;jsessionid='},
                                             'logs':         {method: 'GET', url: '@cstl/api/1/log/:type/:id;jsessionid='},
                                             'capabilities': {method: 'GET', url: '@cstl/WS/:type/:id;jsessionid=?REQUEST=GetCapabilities&SERVICE=:typeUpper&VERSION=:version'},
                                             'layers' :      {method: 'GET', url: '@cstl/api/1/MAP/:type/:id/layersummary/all;jsessionid=', isArray: true},
                                             'addLayer':     {method: 'PUT', url: '@cstl/api/1/MAP/:type/:id/layer;jsessionid='},
                                             'deleteLayer':  {method: 'DELETE', url: '@cstl/api/1/MAP/:type/:id/:layerid;jsessionid='},
                                             'updateLayerStyle': {method: 'POST', url: '@cstl/api/1/MAP/:type/:id/updatestyle;jsessionid='},
                                             'removeLayerStyle': {method: 'POST', url: '@cstl/api/1/MAP/:type/:id/removestyle;jsessionid='}
                                         });
                                     }]);

cstlAdminApp.factory('dataListing', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/data/list/top/:filter;jsessionid=', {}, {
            'listAll':      {method: 'GET', isArray: true},
            'listData': {method: 'GET', url: '@cstl/api/1/data/list/provider;jsessionid='},
            'listCoverage': {method: 'POST', url: '@cstl/api/1/data/coverage/list/;jsessionid='},
            'pyramidData':  {method: 'POST', url: '@cstl/api/1/data/pyramid/create/:providerId/:dataId;jsessionid='},
            'pyramidConform': {method: 'POST', url: '@cstl/api/1/data/pyramid/createconform/:providerId/:dataId;jsessionid='},
            'pyramidScales':  {method: 'GET', url: '@cstl/api/1/data/pyramid/bestscales/:providerId/:dataId;jsessionid='},
            'dataFolder':   {method: 'POST', url: '@cstl/api/1/data/datapath;jsessionid=', isArray: true},
    		'importData':     {method: 'POST', url: '@cstl/api/1/data/import;jsessionid='},
            'loadData':     {method: 'POST', url: '@cstl/api/1/data/load;jsessionid='},
            'extension':    {method: 'POST', url: '@cstl/api/1/data/testextension;jsessionid='},
            'deleteData':   {method: 'DELETE', url: '@cstl/api/1/data/:providerid/:dataid;jsessionid='},
            'setMetadata':  {method: 'POST', url: '@cstl/api/1/data/metadata;jsessionid='},
            'codeLists':    {method: 'GET', url: '@cstl/api/1/data/metadataCodeLists/:lang;jsessionid='}
        });
    }]);

cstlAdminApp.factory('style', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/SP/all/style/available;jsessionid=', {}, {
            'listAll': { method: 'GET', isArray: false },
            'delete':  { method: 'DELETE', url: '@cstl/api/1/SP/:provider/style/:name;jsessionid='},
            'link':    { method: 'POST',   url: '@cstl/api/1/SP/:provider/style/:name/linkData;jsessionid='},
            'unlink':  { method: 'POST',   url: '@cstl/api/1/SP/:provider/style/:name/unlinkData;jsessionid='}
        });
    }]);

cstlAdminApp.factory('provider', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/provider', {}, {
            'create':   {method: 'PUT', url: '@cstl/api/1/provider/:id;jsessionid='},
            'delete':   {method: 'DELETE', url: '@cstl/api/1/provider/:id;jsessionid='},
            'metadata': {method: 'GET', url: '@cstl/api/1/provider/metadata/:providerId;jsessionid='}
        });
    }]);

cstlAdminApp.factory('csw', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/CSW', {}, {
            'count':      {method: 'GET',  url: '@cstl/api/1/CSW/:id/records/count;jsessionid='},
            'getRecords': {method: 'GET',  url: '@cstl/api/1/CSW/:id/records/:count-:startIndex;jsessionid='},
            'getRecord':  {method: 'GET',  url: '@cstl/api/1/CSW/:id/record/:metaId;jsessionid='},
            'downloadMd': {method: 'GET',  url: '@cstl/api/1/CSW/:id/record/download/:metaId;jsessionid='},
            'refresh':    {method: 'POST', url: '@cstl/api/1/CSW/:id/index/refresh;jsessionid='},
            'delete':     {method: 'DELETE', url: '@cstl/api/1/CSW/:id/record/:metaId;jsessionid='}
        });
    }]);

cstlAdminApp.factory('textService', ['$http',
    function ($http){
        return {
            logs : function(type, id){
                return $http.get('@cstl/api/1/log/'+type+'/'+id+';jsessionid=');

            },
            capa : function(type, id, version){
                return $http.get('@cstl/WS/'+type+'/'+id+';jsessionid=?REQUEST=GetCapabilities&SERVICE='+type.toUpperCase()+'&VERSION='+version);

            }
        };
    }]);

cstlAdminApp.factory('AuthenticationSharedService', ['$rootScope', '$http', 'authService', '$base64','$cookieStore',
    function ($rootScope, $http, authService, $base64, $cookieStore) {
        return {
            authenticate: function() {
                $http.get('@cstl/spring/session/status;jsessionid=')
                    .success(function (data, status, headers, config) {
                        $rootScope.$broadcast('event:auth-authConfirmed');
                    })
            },
            logout: function () {
                $rootScope.authenticationError = false;
                $http.get("@cstl/spring/session/logout;jsessionid=").then(function(){
                	$cookieStore.remove('cstlSessionId');
                	$http.get(context + '/app/logout')
                	.success(function (data, status, headers, config) {
                		authService.loginCancelled();
                	});

                });
            }
        };
    }]);

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

cstlAdminApp.factory('StyleSharedService', ['$modal', 'style', 'webService', '$growl',
    function ($modal, style, webService, $growl) {
        return {
            showStyleList : function($scope) {
                var modal = $modal.open({
                    templateUrl: 'views/modalStyleChoose.html',
                    controller: 'StyleModalController',
                    resolve: {
                        exclude: function() { return $scope.selected.TargetStyle },
                        layerName: function() { return $scope.selected.Name },
                        providerId: function() { return $scope.selected.Provider },
                        dataType: function() { return $scope.selected.Type},
                        serviceName: function() {
                            if ($scope.service) {
                                // In WMS mode
                                return $scope.service.name;
                            }
                            // For portraying
                            return null;
                        }
                    }
                });

                modal.result.then(function(item) {
                    if (item) {
                        if ($scope.service) {
                            webService.updateLayerStyle({type: $scope.service.type, id: $scope.service.identifier},
                                {values: {layerId: $scope.selected.Name, spId: 'sld', styleName: item.Name}},
                                function() {
                                    $scope.selected.TargetStyle.push(item);
                                    $growl('success','Success','Style updated for layer '+ $scope.selected.Name);
                                }, function() { $growl('error','Error','Unable to update style for layer '+ $scope.selected.Name); }
                            );
                        } else {
                            style.link({
                                provider: item.Provider,
                                name: item.Name
                            }, {
                                values: {
                                    dataProvider: $scope.selected.Provider,
                                    dataNamespace: "",
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
                        }, function() { $growl('error','Error','Unable to update style for layer '+ $scope.selected.Name); }
                    );
                } else {
                    var res = style.unlink({provider: providerName, name: styleName},
                        {values: {dataProvider: dataProvider, dataNamespace: "", dataId: dataId}});
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

            showStyleCreate : function() {
                var modal = $modal.open({
                    templateUrl: 'views/modalStyleCreate.html',
                    controller: 'StyleModalController',
                    resolve: {
                        exclude: function() { return null },
                        layerName: function() { return null },
                        providerId: function() { return null },
                        dataType: function() { return null},
                        serviceName: function() { return null}
                    }
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

cstlAdminApp.service('$dashboard', function($filter) {
    return function($scope, fullList, filterOnType) {

        $scope.fullList = fullList || [];
        $scope.dataList = $scope.dataList || [];
        $scope.filtertext = $scope.filtertext || "";
        $scope.filtertype = $scope.filtertype || undefined;
        $scope.ordertype = $scope.ordertype || "Name";
        $scope.orderreverse = $scope.orderreverse || false;
        $scope.countdata = $scope.countdata || 0;
        $scope.nbbypage = $scope.nbbypage || 10;
        $scope.currentpage = $scope.currentpage || 1;
        $scope.selected = $scope.selected || null;
        $scope.exclude = $scope.exclude || [];

        // Dashboard methods
        $scope.displayPage = function(page) {
            var array;
            if (filterOnType) {
                array = $filter('filter')($scope.fullList, {'Type':$scope.filtertype, '$': $scope.filtertext});
            } else {
                array = $filter('filter')($scope.fullList, {'$': $scope.filtertext});
            }
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

cstlAdminApp.service('$uploadFiles', function() {
    return {
        files : {file: null, mdFile: null}
    };
});
