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

cstlAdminApp.factory('AuthInterceptor', function($rootScope, $cookies, CookiesStorageService) {
    return {
        'request': function(config) {
            var url = config.url+'';
            if(url.indexOf(cstlUrlPrefix) == 0){
                if($cookies.cstlUrl==null){
                    $rootScope.$broadcast('event:auth-loginRequired', null);
                    return
                }
                url = $cookies.cstlUrl + url.substring(cstlUrlPrefix.length);
                if($cookies.cstlActiveDomainId)
                    url = url.replace('$domainId', $cookies.cstlActiveDomainId);
                if($cookies.cstlUserId)
                    url = url.replace('$userId', $cookies.cstlUserId);

                var jsessionIdIndex = url.indexOf(";jsessionid=");
                if(jsessionIdIndex == -1){
                    var cstlSessionId=$cookies.cstlSessionId;
                    if (cstlSessionId) {
                        var imi = url.indexOf("?");
                        if(imi==-1)
                            config.url = url + ";jsessionid=" + cstlSessionId;
                        else
                            config.url = url.substring(0, imi) + ";jsessionid=" + cstlSessionId + url.substring(imi)
                    }else{
                        config.url = url;
                    }
                }else{
                    var cstlSessionId=$cookies.cstlSessionId;
                    if (cstlSessionId) {
                        config.url = url.replace(";jsessionid=", ";jsessionid=" + cstlSessionId);
                    }else{
                        var i = url.indexOf(';jsessionid=');
                        var l = ';jsessionid='.length;
                        config.url = url.substring(0, i) + url.substring(i+l)
                    }

                }
            }
            return config || $q.when(config);
        }
    }

});


var context = findWebappContext();

function Topic(stompClient, path){
    var self = this;
    this.path = path;
    this.unsubscribe = function(){
        stompClient.unsubscribe(self.id);
        console.log('Unsubscribed from ' + path + ' (' + self.id + ')');
    }
}

function Stomper(url){
    var self = this;
    var socket = new SockJS(url);
    var stompClient = Stomp.over(socket);

    this.subscribe = function(path, cb){
        var topic = new Topic(stompClient, path)
        if(stompClient.connected){
            topic.id = stompClient.subscribe(topic.path, cb)
            console.log('Subscribed to ' + topic.path + ' (' + topic.id  + ').')
        }else {
            stompClient.connect('','', function(frame) {
                console.log('Connected to ' + url)
                topic.id = stompClient.subscribe(topic.path, cb)
                console.log('Subscribed to ' + topic.path + ' (' + topic.id  + ').')
            });
        }
        return topic;
    };


}

cstlAdminApp.factory('CookiesStorageService', ['$cookies', function($cookies){
    var cstlUrl = $cookies.cstlUrl;
    return {
        getDomainId: function(){return $cookies.cstlDomainId},
        setDomainId: function(domainId){$cookies.cstlDomainId=domainId}
    }

}]);

cstlAdminApp.factory('StompService', ['$cookies', function($cookies){
    var cstlUrl = $cookies.cstlUrl;

    return new Stomper(cstlUrl + 'spring/ws/adminmessages')

}]);

cstlAdminApp.factory('Account', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/session/account', {}, {
        });
    }]);

cstlAdminApp.factory('Contact', ['$resource',
    function ($resource) {
        return $resource( '@cstl/spring/admin/contact;jsessionid=', {}, {
            save: {method:'PUT'}
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

cstlAdminApp.factory('GeneralService', ['$http',
    function ($http) {
        return {
            "counts": function(){
                return $http({ method: 'GET', url:'@cstl/api/1/general/counts;jsessionid='})
            },
            "checkLogin":function(login){
                return $http({method: 'POST', url: "@cstl/api/1/general/logincheck;jsessionid=", data: login})
            }
        }
    }]);

cstlAdminApp.factory('UserResource', ['$resource', '$cookies',
    function ($resource, $cookies) {
        return $resource('@cstl/api/1/user/:id', null,
            {
                'update': { method:'PUT' },
                'domainRoles': {url: '@cstl/api/1/user/$userId/domainroles/:domainId', isArray:true}
            });
    }]);

cstlAdminApp.factory('DomainResource', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/domain/:id', null, {
            'update': { method:'PUT' },
            'members' : {url: '@cstl/api/1/domain/members/:id', isArray:true},
            'nonmembers' : {url: '@cstl/api/1/domain/nonmembers/:id', isArray:true},
            'addMemberToDomain' : {method: 'POST', url: '@cstl/api/1/userXdomain/:domainId/user/:userId'},
            'updateMemberInDomain' : {method: 'PUT', url: '@cstl/api/1/userXdomain/:domainId/user/:userId'},
            'removeMemberFromDomain' : {method: 'DELETE', url: '@cstl/api/1/userXdomain/:domainId/user/:userId'}
        });
    }]);

cstlAdminApp.factory('DomainRoleResource', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/domainrole/:id;jsessionid=', null, {
            'update': { method:'PUT' },
            'get' : {isArray:false}
        });
    }]);

cstlAdminApp.factory('PermissionService', ['$http',
    function ($http) {
        return {
            "all": function(){
                return $http({ method: 'GET', url:'@cstl/api/1/permission/;jsessionid='});
            }
        }
    }]);

cstlAdminApp.factory('webService', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/admin/domain/$domainId/instances/:lang', {}, {
            'permissionByDomainRole' : {method: 'GET',     url: '@cstl/api/1/servicepermission/access', isArray: true},
            'domains':                 {method: 'GET',     url: '@cstl/api/1/servicepermission/user/$userId/service/:id', isArray: true},
            'linkToDomain':            {method: 'POST',    url: '@cstl/api/1/serviceXdomain/:domainId/service/:serviceId'},
            'unlinkFromDomain':        {method: 'DELETE',  url: '@cstl/api/1/serviceXdomain/:domainId/service/:serviceId'},
            'listAll':                 {method: 'GET',     isArray: false},
            'listAllByType':           {method: 'GET',     url: '@cstl/api/1/admin/domain/$domainId/instances/:lang/:type', isArray: false},
            'listServiceLayers':       {method: 'GET',     url: '@cstl/api/1/admin/domain/$domainId/service/layers/:lang', isArray: true},
            'get':                     {method: 'GET',     url: '@cstl/api/1/OGC/:type/:id/:lang;jsessionid='},
            'create':                  {method: 'PUT',     url: '@cstl/api/1/OGC/:type/domain/$domainId'},
            'delete':                  {method: 'DELETE',  url: '@cstl/api/1/OGC/:type/:id;jsessionid='},
            'restart':                 {method: 'POST',    url: '@cstl/api/1/OGC/:type/:id/restart;jsessionid='},
            'start':                   {method: 'POST',    url: '@cstl/api/1/OGC/:type/:id/start;jsessionid='},
            'stop':                    {method: 'POST',    url: '@cstl/api/1/OGC/:type/:id/stop;jsessionid='},
            'metadata':                {method: 'GET',     url: '@cstl/api/1/OGC/:type/:id/metadata/:lang;jsessionid='},
            'updateMd':                {method: 'POST',    url: '@cstl/api/1/OGC/:type/:id/metadata;jsessionid='},
            'config':                  {method: 'GET',     url: '@cstl/api/1/OGC/:type/:id/config;jsessionid='},
            'setConfig':               {method: 'POST',    url: '@cstl/api/1/OGC/:type/:id/config;jsessionid='},
            'logs':                    {method: 'GET',     url: '@cstl/api/1/log/:type/:id;jsessionid='},
            'capabilities':            {method: 'GET',     url: '@cstl/WS/:type/:id;jsessionid=?REQUEST=GetCapabilities&SERVICE=:typeUpper&VERSION=:version'},
            'layers' :                 {method: 'GET',     url: '@cstl/api/1/MAP/:type/:id/layersummary/all;jsessionid=', isArray: true},
            'addLayer':                {method: 'PUT',     url: '@cstl/api/1/MAP/:type/:id/layer;jsessionid='},
            'deleteLayer':             {method: 'POST',    url: '@cstl/api/1/MAP/:type/:id/delete/:layerid;jsessionid='},
            'updateLayerStyle':        {method: 'POST',    url: '@cstl/api/1/MAP/:type/:id/updatestyle;jsessionid='},
            'removeLayerStyle':        {method: 'POST',    url: '@cstl/api/1/MAP/:type/:id/removestyle;jsessionid='}
        });
    }]);

cstlAdminApp.factory('dataListing', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/domain/$domainId/data/list/top/:filter;jsessionid=', {}, {
            'listAll':              {method: 'GET',     isArray: true},
            'listData':             {method: 'GET',     url: '@cstl/api/1/domain/$domainId/data/list/provider;jsessionid='},
            'listDataForProv':      {method: 'GET',     url: '@cstl/api/1/domain/$domainId/data/list/provider/:providerId;jsessionid=', isArray: true},
            'listCoverage':         {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/coverage/list/;jsessionid='},
            'pyramidData':          {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/pyramid/create/:providerId/:dataId;jsessionid='},
            'pyramidConform':       {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/pyramid/createconform/:providerId/:dataId;jsessionid='},
            'pyramidScales':        {method: 'GET',     url: '@cstl/api/1/domain/$domainId/data/pyramid/bestscales/:providerId/:dataId;jsessionid='},
            'deletePyramidFolder':  {method: 'DELETE',  url: '@cstl/api/1/domain/$domainId/data/pyramid/folder/:providerId;jsessionid='},
            'dataFolder':           {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/datapath/true;jsessionid=', isArray: true},
            'metadataFolder':       {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadatapath/true;jsessionid=', isArray: true},
            'importData':           {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/import;jsessionid='},
            'loadData':             {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/load;jsessionid='},
            'extension':            {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/testextension;jsessionid='},
            'hideData':             {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/:providerid/:dataid/hidden;jsessionid='},
            'setMetadata':          {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata;jsessionid='},
            'setUpMetadata':        {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata/upload;jsessionid='},
            'mergeMetadata':        {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata/merge;jsessionid='},
            'metadata':             {method: 'GET',     url: '@cstl/api/1/domain/$domainId/data/metadata/iso/:providerId/:dataId;jsessionid='},
            'dataForMetadata':      {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata/associated;jsessionid='},
            'getDataMetadata':      {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata/data;jsessionid='},
            'codeLists':            {method: 'GET',     url: '@cstl/api/1/domain/$domainId/data/metadataCodeLists/:lang;jsessionid='},
            'findDataType':         {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/findDataType;jsessionid='},
            'linkToSensor':         {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/link/sensor/:providerId/:dataId/:sensorId;jsessionid='},
            'unlinkSensor':         {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/unlink/sensor/:providerId/:dataId/:sensorId;jsessionid='},
            'generateSML':          {method: 'PUT',     url: '@cstl/api/1/sensor/generate;jsessionid='},
            'linkToDomain':         {method: 'POST',    url: '@cstl/api/1/dataXdomain/:dataId/domain/:domainId'},
            'unlinkFromDomain':     {method: 'DELETE',  url: '@cstl/api/1/dataXdomain/:dataId/domain/:domainId'},
            'domains':              {method: 'GET',     url: '@cstl/api/1/dataXdomain/:dataId/user/$userId/domain', isArray: true},
            'findData':             {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata/find;jsessionid=', isArray: true}
        });
    }]);

cstlAdminApp.factory('style', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/SP/:provider/style/available;jsessionid=', {}, {

            'listAll':                  {method: 'GET',    isArray: false },
            'get':                      {method: 'GET',    url: '@cstl/api/1/SP/:provider/style/:name;jsessionid='},
            'createjson':               {method: 'PUT',    url: '@cstl/api/1/SP/:provider/style/create;jsessionid='},
            'updatejson':               {method: 'PUT',    url: '@cstl/api/1/SP/:provider/style/:name/update;jsessionid='},
            'delete':                   {method: 'DELETE', url: '@cstl/api/1/SP/:provider/style/:name;jsessionid='},
            'link':                     {method: 'POST',   url: '@cstl/api/1/SP/:provider/style/:name/linkData;jsessionid='},
            'unlink':                   {method: 'POST',   url: '@cstl/api/1/SP/:provider/style/:name/unlinkData;jsessionid='},
            'generateAutoIntervalStyle':{method: 'POST',   url: '@cstl/api/1/SP/:provider/style/generateAutoInterval;jsessionid='},
            'generateAutoUniqueStyle':  {method: 'POST',   url: '@cstl/api/1/SP/:provider/style/generateAutoUnique;jsessionid='},
            'getChartDataJson':         {method: 'POST',   url: '@cstl/api/1/SP/getChartDataJson;jsessionid='},
            'paletteStyle':             {method: 'GET',    url: '@cstl/api/1/SP/:provider/style/:name/:ruleName;jsessionid='},
            'statistics':               {method: 'POST',   url: '@cstl/api/1/SP/statistics;jsessionid='}
        });
    }]);

cstlAdminApp.factory('provider', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/provider', {}, {
            'test':             {method: "POST",    url: '@cstl/api/1/domain/$domainId/provider/:id/test;jsessionid='},
            'create':           {method: 'POST',    url: '@cstl/api/1/domain/$domainId/provider/:id;jsessionid='},
            'delete':           {method: 'DELETE',  url: '@cstl/api/1/domain/$domainId/provider/:id;jsessionid='},
            'metadata':         {method: 'GET',     url: '@cstl/api/1/domain/$domainId/provider/metadata/:providerId;jsessionid='},
            'dataDesc':         {method: 'GET',     url: '@cstl/api/1/domain/$domainId/provider/:providerId/:dataId/dataDescription;jsessionid='},
            'isGeophysic':      {method: 'GET',     url: '@cstl/api/1/domain/$domainId/provider/:providerId/:dataId/isGeophysic;jsessionid='},
            'verifyCRS':        {method: 'GET',     url: '@cstl/api/1/domain/$domainId/provider/:id/crs'},
            'createPRJ':        {method: 'POST',    url: '@cstl/api/1/domain/$domainId/provider/:id/createprj'},
            'getAllCodeEPSG':   {method: 'GET',     url: '@cstl/api/1/domain/$domainId/provider/:id/epsgCode', transformResponse: function (data) {return {list: angular.fromJson(data)} }}
        });
    }]);

cstlAdminApp.factory('csw', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/CSW', {}, {
            'count':      {method: 'GET',       url: '@cstl/api/1/CSW/:id/records/count;jsessionid='},
            'getRecords': {method: 'GET',       url: '@cstl/api/1/CSW/:id/records/:count-:startIndex;jsessionid='},
            'getRecord':  {method: 'GET',       url: '@cstl/api/1/CSW/:id/record/:metaId;jsessionid='},
            'downloadMd': {method: 'GET',       url: '@cstl/api/1/CSW/:id/record/download/:metaId;jsessionid='},
            'refresh':    {method: 'POST',      url: '@cstl/api/1/CSW/:id/index/refresh;jsessionid='},
            'delete':     {method: 'DELETE',    url: '@cstl/api/1/CSW/:id/record/:metaId;jsessionid='}
        });
    }]);

cstlAdminApp.factory('sos', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/SOS', {}, {
            'count':             {method: 'GET',    url: '@cstl/api/1/SOS/:id/sensors/count;jsessionid='},
            'add':               {method: 'PUT',    url: '@cstl/api/1/SOS/:id/observations;jsessionid='},
            'removeSensor':      {method: 'DELETE', url: '@cstl/api/1/SOS/:id/sensor/:sensor;jsessionid='},
            'getFeatures':       {method: 'GET',    url: '@cstl/api/1/SOS/:id/sensor/location/:sensor;jsessionid='},
            'listSensors':       {method: 'GET',    url: '@cstl/api/1/SOS/:id/sensors/identifiers;jsessionid='},
            'sensorsTree':       {method: 'GET',    url: '@cstl/api/1/SOS/:id/sensors;jsessionid='},
            'listMeasures':      {method: 'GET',    url: '@cstl/api/1/SOS/:id/observedProperties/identifiers;jsessionid='},
            'measuresForSensor': {method: 'GET',    url: '@cstl/api/1/SOS/:id/observedProperty/identifiers/:sensorID;jsessionid='},
            'sensorsForMeasure': {method: 'GET',    url: '@cstl/api/1/SOS/:id/sensors/identifiers/:measure;jsessionid='},
            'importSensor':      {method: 'PUT',    url: '@cstl/api/1/SOS/:id/sensor/import;jsessionid='},
            'getCsvObs' :        {method: 'POST',   url: '@cstl/api/1/SOS/:id/observations;jsessionid='},
            'build' :            {method: 'GET',    url: '@cstl/api/1/SOS/:id/build;jsessionid='}
        });
    }]);

cstlAdminApp.factory('sensor', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/sensor', {}, {
            'list':        {method: 'GET',  url: '@cstl/api/1/sensor/list;jsessionid='},
            'add':         {method: 'PUT',  url: '@cstl/api/1/sensor/add;jsessionid=', isArray: true},
            'delete':      {method: 'DELETE',  url: '@cstl/api/1/sensor/:sensor;jsessionid='},
            'getMetadata': {method: 'GET',  url: '@cstl/api/1/sensor/:sensor;jsessionid='},
            'listMeasures': {method: 'GET',  url: '@cstl/api/1/sensor/observedProperties/identifiers;jsessionid='},
            'measuresForSensor':    {method: 'GET', url: '@cstl/api/1/sensor/observedProperty/identifiers/:sensorID;jsessionid='},
            'sensorsForMeasure':    {method: 'GET', url: '@cstl/api/1/sensor/sensors/identifiers/:measure;jsessionid='},
            'getFeatures':  {method: 'GET',  url: '@cstl/api/1/sensor/location/:sensor;jsessionid='}
        });
    }]);

cstlAdminApp.factory('mapcontext', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/context', {}, {
            'list':            {method: 'GET',    url: '@cstl/api/1/context/list;jsessionid=', isArray: true},
            'listLayers':      {method: 'GET',    url: '@cstl/api/1/context/list/layers;jsessionid=', isArray: true},
            'listExtLayers':   {method: 'POST',   url: '@cstl/api/1/context/external/capabilities/layers/:version;jsessionid=', isArray: true},
            'add':             {method: 'PUT',    url: '@cstl/api/1/context;jsessionid='},
            'update':          {method: 'POST',   url: '@cstl/api/1/context;jsessionid='},
            'delete':          {method: 'DELETE', url: '@cstl/api/1/context/:id;jsessionid='},
            'setLayers':       {method: 'POST',   url: '@cstl/api/1/context/layers/:id;jsessionid='},
            'extent':          {method: 'GET',    url: '@cstl/api/1/context/:id/extent;jsessionid='},
            'extentForLayers': {method: 'POST',   url: '@cstl/api/1/context/extent/layers;jsessionid='}
        });
    }]);

cstlAdminApp.factory('ProcessService', ['$resource', '$cookies',
    function($resource, $cookies) {
        return $resource('@cstl/spring/admin/process;jsessionid=', {}, {
            'get' : {method : 'GET',isArray : true}
        });
    } ]);

cstlAdminApp.factory('TaskService', ['$resource',
    function ($resource) {
        return $resource('@cstl/api/1/task', {}, {
            'listTasks':             {method: 'GET',    url: '@cstl/api/1/task/listTasks;jsessionid=',                           isArray: false },
            'listProcess':           {method: 'GET',    url: '@cstl/api/1/task/listProcesses;jsessionid=',                       isArray: false },
            'listProcessForFactory': {method: 'GET',    url: '@cstl/api/1/task/process/factory/:authorityCode;jsessionid=',      isArray: false },
            'listParamsTask':        {method: 'GET',    url: '@cstl/api/1/task/params/list;jsessionid=',                         isArray: true },
//            Use textService for getProcessDescriptor and get XML response
//            'getProcessDescriptor':  {method: 'GET',    url: '@cstl/api/1/task/process/descriptor/:authority/:code;jsessionid=', isArray: false },
            'getParamsTask':         {method: 'GET',    url: '@cstl/api/1/task/params/get/:id;jsessionid=',                      isArray: false },
            'deleteParamsTask':      {method: 'GET',    url: '@cstl/api/1/task/params/delete/:id;jsessionid=',                   isArray: false },
            'createTask':            {method: 'POST',   url: '@cstl/api/1/task/:id/:authority/:code/:title/:step;jsessionid=',   isArray: false },
            'createParamsTask':      {method: 'POST',   url: '@cstl/api/1/task/params/create;jsessionid=',                       isArray: false },
            'updateParamsTask':      {method: 'POST',   url: '@cstl/api/1/task/params/update;jsessionid=',                       isArray: false },
            'executeParamsTask':     {method: 'GET',    url: '@cstl/api/1/task/params/execute/:id;jsessionid=',                  isArray: false },
            'updateTask':            {method: 'PUT',    url: '@cstl/api/1/task/:id/:authority/:code/:title/:step;jsessionid=',   isArray: false },
            'deleteTask':            {method: 'DELETE', url: '@cstl/api/1/task/:id;jsessionid=',                                 isArray: false }
        });
    }]);

cstlAdminApp.factory('AuthenticationSharedService', ['$rootScope', '$http', 'Account' ,'authService', '$base64','$cookieStore',
    function ($rootScope, $http, Account, authService, $base64, $cookieStore) {
        return {
            authenticate: function() {
                Account.get(function(account){
                    $rootScope.account=account
                    $rootScope.hasRole = function(role){
                        return account.roles.indexOf(role) != -1
                    }
                    $rootScope.hasMultipleDomains = function(){
                        return account.domains.length > 1
                    }
                    $rootScope.$broadcast('event:auth-authConfirmed');
                });
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

cstlAdminApp.factory('textService', ['$http', '$growl',
    function ($http, $growl){
        return {
            logs : function(type, id){
                return $http.get('@cstl/api/1/log/'+type+'/'+id+';jsessionid=');

            },
            capa : function(type, id, version){
                return $http.get('@cstl/WS/'+type+'/'+id+';jsessionid=?REQUEST=GetCapabilities&SERVICE='+type.toUpperCase()+'&VERSION='+version);

            },
            capaWmsExterne : function(url){
                return $http.get(url +'?REQUEST=GetCapabilities&SERVICE=WMS');
            },
            getProcessDescriptor : function(authority, code){
                return $http.get('@cstl/api/1/task/process/descriptor/'+authority+'/'+code+';jsessionid=');
            },
            createStyleXml : function(provider, styleXml){
                return $http({
                    url: '@cstl/api/1/SP/'+provider+'/style;jsessionid=',
                    method: "PUT",
                    data: styleXml,
                    headers: {'Content-Type': 'application/xml'}
                });
            },
            metadata : function(provider, data){
                var promise = $http({
                    url: '@cstl/api/1/domain/$domainId/data/metadata/iso/'+ provider+'/'+ data +';jsessionid=',
                    method: "GET",
                    headers: {'Accept': 'application/xml'}
                });
                promise.error(function(errorMsg) {
                    $growl('warning', 'Warning', 'No metadata found for data '+ data);
                });
                return promise;
            },
            metadataJson : function(provider, data, type, prune){
                var promise = $http({
                    url: '@cstl/api/1/domain/$domainId/data/metadataJson/iso/'+ provider+'/'+ data +'/'+ type +'/'+ prune +';jsessionid=',
                    method: "GET",
                    headers: {'Accept': 'application/json'}
                });
                promise.error(function(errorMsg) {
                    $growl('warning', 'Warning', 'Error while retrieving json metadata for data '+ data);
                });
                return promise;
            },
            sensorMetadata : function(sensor){
                var promise = $http({
                    url: '@cstl/api/1/sensor/'+ sensor+';jsessionid=',
                    method: "GET",
                    headers: {'Accept': 'application/xml'}
                });
                promise.error(function(errorMsg) {
                    $growl('warning', 'Warning', 'No sensorML found for sensor '+ sensor);
                });
                return promise;
            }
        };
    }]);

cstlAdminApp.factory('StyleSharedService', ['$modal', 'style', 'webService', '$growl',
    function ($modal, style, webService, $growl) {
        return {
            showStyleList : function($scope) {
                var modal = $modal.open({
                    templateUrl: 'views/style/modalStyleChoose.html',
                    controller: 'StyleModalController',
                    resolve: {
                        exclude: function() { return $scope.selected.TargetStyle },
                        selectedLayer: function() { return $scope.selected },
                        selectedStyle: function() { return null },
                        serviceName: function() {
                            if ($scope.service) {
                                // In WMS mode
                                return $scope.service.name;
                            }
                            // For portraying
                            return null;
                        },
                        newStyle: function() { return null }
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

            showStyleCreate : function(scope) {
                var modal = $modal.open({
                    templateUrl: 'views/style/modalStyleCreate.html',
                    controller: 'StyleModalController',
                    resolve: {
                        newStyle: function() { return null; },
                        pageSld: function() {  return 'views/style/chooseType.html'; },
                        selectedLayer: function() {  return null },
                        selectedStyle: function() { return null },
                        serviceName: function() {  return null },
                        exclude: function() {  return null }
                    }
                });
                modal.result.then(function(item) {
                    if (scope) {
                        style.listAll({provider: 'sld'}, function(response) {
                            scope.fullList = response.styles;
                        });
                    }
                });
            },

            showStyleEdit : function(scope, response) {
                var modal = $modal.open({
                    templateUrl: 'views/style/modalStyleEdit.html',
                    controller: 'StyleModalController',
                    resolve: {
                        newStyle: function() { return response},
                        selectedLayer: function() {  return null },
                        selectedStyle: function() { return scope.selected },
                        serviceName: function() {  return null },
                        exclude: function() {  return null }
                    }
                });
                modal.result.then(function(item) {
                    if (scope) {
                        style.listAll({provider: 'sld'}, function(response) {
                            scope.fullList = response.styles;
                        });
                    }
                });
            }
        };

    }]);

cstlAdminApp.service('$dashboard', function($filter) {
    return function($scope, fullList, filterOnType) {

        $scope.service = $scope.service || null;
        $scope.fullList = fullList || [];
        $scope.dataList = $scope.dataList || [];
        $scope.filtertext = $scope.filtertext || "";
        $scope.filtertype = $scope.filtertype || undefined;
        $scope.ordertype = $scope.ordertype || ($scope.service && $scope.service.type && $scope.service.type.toLowerCase()==='sos') ? "id" : ($scope.service && $scope.service.type && $scope.service.type.toLowerCase==='csw') ? "title" : "Name";
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
                    if ($scope.service && $scope.service.type.toLowerCase() === 'sos') {
                        if ($scope.exclude[j].id === array[i].Name) {
                            found = true;
                            break;
                        }
                    } else {
                        if ($scope.exclude[j].Name === array[i].Name) {
                            found = true;
                            break;
                        }
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
            if ($scope.selected === item) {
                $scope.selected = null;
            } else {
                $scope.selected = item;
            }
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
