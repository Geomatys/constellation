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

cstlAdminApp.controller('AdminController', ['$scope', '$location', '$dashboard', 'webService',
    'dataListing', 'provider', 'style', 'textService', '$modal', '$growl', 'StyleSharedService', '$cookies',
    function ($scope, $location, $dashboard, webService, dataListing, provider, style, textService, $modal, $growl, StyleSharedService, $cookies) {

        $scope.viewUrls = {
            'system_state':         {name:'system_state',       url:'views/admin/system_state.html'},
            'system_settings' :     {name:'system_settings',    url:'views/admin/system_settings.html'},
            'system_class_image' :  {name:'system_class_image', url:'views/admin/classimage.html'},
            'system_stats' :        {name:'system_stats',       url:'views/admin/statistics.html'},
            'system_logs' :         {name:'system_logs',        url:'views/admin/logs.html'},
            'system_contact' :      {name:'system_contact',     url:'views/admin/contact.html'},
            'system_about' :        {name:'system_about',       url:'views/admin/about.html'},
            'process' :             {name:'process',            url:'views/admin/process.html'},
            'tasks' :               {name:'tasks',              url:'views/admin/tasks.html'},
            'planification' :       {name:'planification',      url:'views/admin/planification.html'},
            'security_settings' :   {name:'security_settings',  url:'views/admin/security_settings.html'},
            'users' :               {name:'users',              url:'views/admin/users.html'},
            'groups' :              {name:'groups',             url:'views/admin/groups.html'}};

        $scope.currentView = $scope.viewUrls['system_state'];

        $scope.changeView = function(page) {
            $scope.currentView = $scope.viewUrls[page];
        };
    }
]);

cstlAdminApp.controller('MetricsController', ['$scope','Metrics','$window', '$http',
    function ($scope,Metrics, $window, $http) {

        $scope.init = function(){
            $scope.metrics= Metrics.get();
        };
        $scope.rungc = function(){
            $http.get("@cstl/spring/admin/jvm/rungc;jsessionid=").then(function(){
                $scope.metrics= Metrics.get();
            });
        };
    }
]);

cstlAdminApp.controller('ContactController', ['$scope', 'Contact',
    function ($scope, Contact) {
        $scope.data = Contact.get();
        $scope.save = function () {
            Contact.save($scope.data,
                function (value, responseHeaders) {
                    $scope.error = null;
                    $scope.success = 'OK';
                    $scope.data = Contact.get();
                },
                function (httpResponse) {
                    $scope.success = null;
                    $scope.error = "ERROR";
                });
        };
    }
]);


cstlAdminApp.controller('TaskController', ['$scope', 'TaskService','$timeout','StompService',
    function ($scope, TaskService, $timeout, StompService) {

        $scope.tasks = TaskService.list();

        var topic = StompService.subscribe('/topic/taskevents', function(data){
            var event = JSON.parse(data.body)
            var task = $scope.tasks[event.id]
            if(task!=null){
                task.percent = event.percent
                if(task.percent > 99)
                    delete $scope.tasks[event.id]
                $scope.$digest();
            }else{
                //new task
                $scope.tasks[event.id] = {
                    id: event.id,
                    status: event.status,
                    message: event.message,
                    percent: event.percent
                }
                $scope.$digest();
            }
        })
        //connect();
        $scope.$on('$destroy', function () {
            topic.unsubscribe();
        });

    }
]);

cstlAdminApp.controller('UserController', ['$scope', 'UserResource', '$modal', '$growl', '$translate',
    function ($scope, UserResource, $modal, $growl, $translate) {
        $scope.list = UserResource.query({"withDomainAndRoles": true});
        $scope.details = function(i) {
            $modal.open({
                templateUrl: 'views/admin/user/details.html',
                controller: 'UserDetailsController',
                resolve: {
                    'isUpdate': function() {return true},
                    'user': function(){
                        return angular.copy($scope.list[i]);
                    }
                }
            }).result.then(function(user){
                    if(user != null)
                        $scope.list[i] = user;
                });
        };
        $scope.add = function(i) {
            $modal.open({
                templateUrl: 'views/admin/user/add.html',
                controller: 'UserDetailsController',
                resolve: {
                    'isUpdate': function() {return false},
                    'user': function(){
                        return {roles:[]};
                    }
                }
            }).result.then(function(user){
                    if(user != null)
                        $scope.list[$scope.list.length] = user;
                });
        };
        $scope.deleteUser = function(i){
            UserResource.delete({id: $scope.list[i].id}, {} , function(resp){
                $scope.list.splice(i, 1);
            }, function(err){
                var errorCode = err.data
                $translate(['Error',errorCode]).then(function (translations) {
                    $growl('error', translations.Error,  translations[errorCode]);
                });
            });
        };
    }
]);

cstlAdminApp.controller('UserDetailsController', ['$scope', '$modalInstance', 'GeneralService', 'user', 'isUpdate', 'UserResource', '$growl', '$translate',
    function ($scope, $modalInstance, GeneralService, user, isUpdate, UserResource, $growl, $translate) {
        $scope.user = user;

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };
        $scope.deleteTag = function(role){
            var newRoles = [];
            for(var i=0; i<user.roles.length; i++)
                if(user.roles[i] != role)
                    newRoles[newRoles.length] = user.roles[i];
            user.roles = newRoles;
        };

        $scope.addRole = function(role){
            for(var i=0; i < $scope.user.roles.length; i++)
                if(role === $scope.user.roles[i])
                    return

            $scope.user.roles[$scope.user.roles.length]=role
        };
        var timeout=null;
        $scope.checkLogin = function(login){
            if(timeout != null){
                window.clearTimeout(timeout)
                timeout = null;
            }
            timeout = setTimeout(function(){
                if($scope.user.login && $scope.user.login.length > 2)
                    GeneralService.checkLogin($scope.user.login).success(function(res){
                        $scope.loginInUse=res.available=="false"
                    }).error(function(){
                    })
            }, 400)
        };

        $scope.save = function(){
            var userResource = new UserResource($scope.user);
            if(isUpdate)
                userResource.$update(function(){
                    $modalInstance.close($scope.user);
                }, function(){
                    $translate(['Error','admin.user.save.error']).then(function (translations) {
                        $growl('error', translations.Error,  translations['admin.user.save.error']);
                    })});
            else
                userResource.$save(function(){
                    $modalInstance.close($scope.user);
                }, function(){
                    $translate(['Error','admin.user.save.error']).then(function (translations) {
                        $growl('error', translations.Error,  translations['admin.user.save.error']);
                    });
                });
        }
    }
]);

cstlAdminApp.controller('GroupController', ['$scope', '$modal',
    function ($scope, $modal) {

        $scope.add = function(i) {
            $modal.open({
                templateUrl: 'views/admin/group/details.html',
                controller: 'GroupDetailsController',
                resolve: {
                    'isUpdate': function() {return false},
                    'user': function(){
                        return {roles:[]};
                    }
                }
            }).result.then(function(user){
                    if(user != null)
                        $scope.list[$scope.list.length] = user;
                });
        };

        $scope.details = function(i) {
            $modal.open({
                templateUrl: 'views/admin/group/details.html',
                controller: 'GroupDetailsController',
                resolve: {
                    'isUpdate': function() {return false},
                    'user': function(){
                        return {roles:[]};
                    }
                }
            }).result.then(function(user){
                    if(user != null)
                        $scope.list[$scope.list.length] = user;
                });
        };

        $scope.members = function(i) {
            $modal.open({
                templateUrl: 'views/admin/group/members.html',
                controller: 'GroupMembersController',
                resolve: {
                    'isUpdate': function() {return false},
                    'user': function(){
                        return {roles:[]};
                    }
                }
            }).result.then(function(user){
                    if(user != null)
                        $scope.list[$scope.list.length] = user;
                });
        };

    }
]);

cstlAdminApp.controller('GroupDetailsController', ['$scope', '$modalInstance', 'user', 'isUpdate', 'UserResource',
    function ($scope, $modalInstance, user, isUpdate, UserResource) {
        $scope.user = user;

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };
        $scope.user={
            adduser:"normal"
        };
    }
]);

cstlAdminApp.controller('LogsController', ['$scope', 'LogsService',
    function ($scope, LogsService) {

        $scope.init = function(){
            $scope.loggers= LogsService.findAll();
        };

        $scope.changeLevel = function (name, level) {
            LogsService.changeLevel({name: name, level: level}, function () {
                $scope.loggers = LogsService.findAll();
            });
        };
    }
]);

