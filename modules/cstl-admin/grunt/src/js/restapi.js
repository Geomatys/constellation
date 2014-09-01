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

/**
 * @require angular.js
 * @require angular-resource.js
 * @require app-services.js
 */
angular.module('cstl-restapi', ['ngResource', 'cstl-services'])
    
    .factory('Account', function($resource) {
        return $resource('@cstl/api/1/session/account', {}, {});
    })

    .factory('Contact', function($resource) {
        return $resource( '@cstl/spring/admin/contact;jsessionid=', {}, {
            save: {method:'PUT'}
        });
    })

    .factory('Sessions', function($resource) {
        return $resource('app/rest/account/sessions/:series', {}, {
            'get': { method: 'GET', isArray: true}
        });
    })

    .factory('Metrics', function($resource) {
        return $resource('@cstl/metrics/metrics;jsessionid=', {}, {
            'get': { method: 'GET'}
        });
    })

    .factory('LogsService', function($resource) {
        return $resource('@cstl/spring/rest/logs;jsessionid=', {}, {
            'findAll': { method: 'GET', isArray: true},
            'changeLevel':  { method: 'PUT'}
        });
    })

    .factory('GeneralService', function($http) {
        return {
            "counts": function(){
                return $http({ method: 'GET', url:'@cstl/api/1/general/counts;jsessionid='})
            },
            "checkLogin":function(login){
                return $http({method: 'POST', url: "@cstl/api/1/general/logincheck;jsessionid=", data: login})
            }
        }
    })

    .factory('UserResource', function($resource) {
        return $resource('@cstl/api/1/user/:id', null, {
            'update': { method:'PUT' },
            'domainRoles': {url: '@cstl/api/1/user/$userId/domainroles/:domainId', isArray:true}
        });
    })

    .factory('DomainResource', function($resource) {
        return $resource('@cstl/api/1/domain/:id', null, {
            'update': { method:'PUT' },
            'members' : {url: '@cstl/api/1/domain/members/:id', isArray:true},
            'nonmembers' : {url: '@cstl/api/1/domain/nonmembers/:id', isArray:true},
            'addMemberToDomain' : {method: 'POST', url: '@cstl/api/1/userXdomain/:domainId/user/:userId'},
            'updateMemberInDomain' : {method: 'PUT', url: '@cstl/api/1/userXdomain/:domainId/user/:userId'},
            'removeMemberFromDomain' : {method: 'DELETE', url: '@cstl/api/1/userXdomain/:domainId/user/:userId'}
        });
    })

    .factory('DomainRoleResource', function($resource) {
        return $resource('@cstl/api/1/domainrole/:id;jsessionid=', null, {
            'update': { method:'PUT' },
            'get' : {isArray:false}
        });
    })

    .factory('PermissionService', function($http) {
        return {
            "all": function(){
                return $http({ method: 'GET', url:'@cstl/api/1/permission/;jsessionid='});
            }
        }
    })

    .factory('webService', function($resource) {
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
    })

    .factory('dataListing', function($resource) {
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
            'mergeMetadata':        {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata/merge/:providerId/:type;jsessionid='},
            'metadata':             {method: 'GET',     url: '@cstl/api/1/domain/$domainId/data/metadata/iso/:providerId/:dataId;jsessionid='},
            'dataForMetadata':      {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata/associated;jsessionid='},
            'getDatasetMetadata':   {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata/dataset;jsessionid='},
            'codeLists':            {method: 'GET',     url: '@cstl/api/1/domain/$domainId/data/metadataCodeLists;jsessionid='},
            'findDataType':         {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/findDataType;jsessionid='},
            'linkToSensor':         {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/link/sensor/:providerId/:dataId/:sensorId;jsessionid='},
            'unlinkSensor':         {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/unlink/sensor/:providerId/:dataId/:sensorId;jsessionid='},
            'generateSML':          {method: 'PUT',     url: '@cstl/api/1/sensor/generate;jsessionid='},
            'linkToDomain':         {method: 'POST',    url: '@cstl/api/1/dataXdomain/:dataId/domain/:domainId'},
            'unlinkFromDomain':     {method: 'DELETE',  url: '@cstl/api/1/dataXdomain/:dataId/domain/:domainId'},
            'domains':              {method: 'GET',     url: '@cstl/api/1/dataXdomain/:dataId/user/$userId/domain', isArray: true},
            'findData':             {method: 'POST',    url: '@cstl/api/1/domain/$domainId/data/metadata/find;jsessionid=', isArray: true}
        });
    })

    .factory('metadataListing', function($resource) {
        return $resource('@cstl/api/1/domain/$domainId/metadata/all/:includeService;jsessionid=', {}, {
            'listAll':              {method: 'GET',    isArray: true }
        });
    })

    .factory('style', function($resource) {
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
    })

    .factory('provider', function($resource) {
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
    })

    .factory('csw', function($resource) {
        return $resource('@cstl/api/1/CSW', {}, {
            'count':      {method: 'GET',       url: '@cstl/api/1/CSW/:id/records/count;jsessionid='},
            'getRecords': {method: 'GET',       url: '@cstl/api/1/CSW/:id/records/:count-:startIndex;jsessionid='},
            'getRecord':  {method: 'GET',       url: '@cstl/api/1/CSW/:id/record/:metaId;jsessionid='},
            'downloadMd': {method: 'GET',       url: '@cstl/api/1/CSW/:id/record/download/:metaId;jsessionid='},
            'refresh':    {method: 'POST',      url: '@cstl/api/1/CSW/:id/index/refresh;jsessionid='},
            'delete':     {method: 'DELETE',    url: '@cstl/api/1/CSW/:id/record/:metaId;jsessionid='}
        });
    })

    .factory('sos', function($resource) {
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
    })

    .factory('sensor', function($resource) {
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
    })

    .factory('mapcontext', function($resource) {
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
    })

    .factory('ProcessService', function($resource) {
        return $resource('@cstl/spring/admin/process;jsessionid=', {}, {
            'get' : {method : 'GET',isArray : true}
        });
    })

    .factory('TaskService', function($resource) {
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
    })

    .factory('textService', function($http, Growl) {
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
                    Growl('warning', 'Warning', 'No metadata found for data '+ data);
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
                    Growl('warning', 'Warning', 'Error while retrieving json metadata for data '+ data);
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
                    Growl('warning', 'Warning', 'No sensorML found for sensor '+ sensor);
                });
                return promise;
            }
        };
    });