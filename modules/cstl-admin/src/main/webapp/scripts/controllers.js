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

ZeroClipboard.config({
    moviePath: "scripts/zeroclipboard/ZeroClipboard.swf"
});

/* Controllers */


cstlAdminApp.controller('HeaderController', ['$scope','$http',
    function ($scope, $http) {
        $http.get("app/conf").success(function(data){
	       	  $scope.cstlLoginUrl = data.cstl + "spring/auth/form";
	          $scope.cstlLogoutUrl = data.cstl + "logout";
	      });
}]);

cstlAdminApp.controller('MainController', ['$scope','$location','webService','dataListing','ProcessService','$growl', 'UserResource', 'TaskService',
    function ($scope, $location, webService, dataListing, Process, $growl, UserResource, task) {
        $scope.countStats = function() {
            webService.listAll({}, function(response) {
                var count = 0;
                for (var i=0; i<response.instance.length; i++) {
                    if (response.instance[i].status === 'WORKING' && response.instance[i].type != 'WEBDAV') {
                        count++;
                    }
                }
                $scope.nbservices = count;
            }, function() {
                $scope.nbservices = 0;
                $growl('error', 'Error', 'Unable to count services');
            });


            dataListing.listAll({}, function(response) {
                $scope.nbdata = response.length;
            }, function() {
                $scope.nbdata = 0;
                $growl('error', 'Error', 'Unable to count data');
            });

            task.list({}, function(taskList) {
                $scope.nbprocess = Object.keys(taskList).length;
            }, function() {
                $scope.nbprocess = 0;
                $growl('error', 'Error', 'Unable to count process');
            });

            UserResource.query(function(response) {
                $scope.nbusers = response.length;
            }, function() {
                $scope.nbusers = 1;
                $growl('error', 'Error', 'Unable to count users');
            });
        };
    }]);

cstlAdminApp.controller('LanguageController', ['$scope', '$translate',
    function ($scope, $translate) {
        $scope.changeLanguage = function (languageKey) {
            $translate.uses(languageKey);
        };
    }]);

cstlAdminApp.controller('MenuController', ['$scope',
    function ($scope) {


}]);

cstlAdminApp.controller('LoginController', ['$scope', '$location', 'AuthenticationSharedService',
    function ($scope, $location, AuthenticationSharedService) {
        $scope.rememberMe = true;
        $scope.login = function () {
            AuthenticationSharedService.login({
                username: $scope.username,
                password: $scope.password,
                rememberMe: $scope.rememberMe,
                success: function () {
                    $location.path('');
                }
            });
        };
    }]);

cstlAdminApp.controller('LogoutController', ['$location', 'AuthenticationSharedService',
    function ($location, AuthenticationSharedService) {
        AuthenticationSharedService.logout({
            success: function () {
                $location.path('');
            }
        });
    }]);

cstlAdminApp.controller('ModalInstanceCtrl', ['$scope', '$modalInstance', function($scope, $modalInstance){
    $scope.ok = function () {
    $modalInstance.close();
  };

  $scope.cancel = function () {
    $modalInstance.dismiss();
  };
}]);


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
}]);




cstlAdminApp.controller('SettingsController', ['$scope', 'resolvedAccount', 'Account',
    function ($scope, resolvedAccount, Account) {
        $scope.success = null;
        $scope.error = null;
        $scope.settingsAccount = resolvedAccount;

        $scope.save = function () {
            Account.save($scope.settingsAccount,
                function (value, responseHeaders) {
                    $scope.error = null;
                    $scope.success = 'OK';
                    $scope.settingsAccount = Account.get();
                },
                function (httpResponse) {
                    $scope.success = null;
                    $scope.error = "ERROR";
                });
        };
    }]);

cstlAdminApp.controller('UserController', ['$scope', 'UserResource', '$modal',
  function ($scope, UserResource, $modal) {
    $scope.list = UserResource.query();
    $scope.details = function(i) {
        $modal.open({
            templateUrl: 'views/user/details.html',
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
            templateUrl: 'views/user/add.html',
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
    $scope.delete = function(i){
    	UserResource.delete({id: $scope.list[i].login});
    	$scope.list.splice(i, 1);
    };
}]);

cstlAdminApp.controller('UserDetailsController', ['$scope', '$modalInstance', 'user', 'isUpdate', 'UserResource',
  function ($scope, $modalInstance, user, isUpdate, UserResource) {
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

    $scope.save = function(){
    	var userResource = new UserResource($scope.user);
    	if(isUpdate)
    	  userResource.$update();
    	else
    	  userResource.$save();
    	$modalInstance.close($scope.user);
    }
}]);

cstlAdminApp.controller('GroupController', ['$scope', '$modal',
  function ($scope, $modal) {

    $scope.add = function(i) {
        $modal.open({
            templateUrl: 'views/group/details.html',
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
            templateUrl: 'views/group/details.html',
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
            templateUrl: 'views/group/members.html',
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

}]);

cstlAdminApp.controller('GroupDetailsController', ['$scope', '$modalInstance', 'user', 'isUpdate', 'UserResource',
  function ($scope, $modalInstance, user, isUpdate, UserResource) {
    $scope.user = user;

    $scope.close = function() {
        $modalInstance.dismiss('close');
    };
    $scope.user={
        adduser:"normal"
    };
}]);

cstlAdminApp.controller('GroupMembersController', ['$scope', '$modalInstance', 'user', 'isUpdate', 'UserResource',
  function ($scope, $modalInstance, user, isUpdate, UserResource) {
    $scope.user = user;

    $scope.close = function() {
        $modalInstance.dismiss('close');
    };
    $scope.user={
        adduser:"normal"
    };
}]);

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
          
}]);

                                     
cstlAdminApp.controller('SessionsController', ['$scope', 'resolvedSessions', 'Sessions',
    function ($scope, resolvedSessions, Sessions) {
        $scope.success = null;
        $scope.error = null;
        $scope.sessions = resolvedSessions;
        $scope.invalidate = function (series) {
            Sessions.delete({series: encodeURIComponent(series)},
                function (value, responseHeaders) {
                    $scope.error = null;
                    $scope.success = "OK";
                    $scope.sessions = Sessions.get();
                },
                function (httpResponse) {
                    $scope.success = null;
                    $scope.error = "ERROR";
                });
        };
    }]);

cstlAdminApp.controller('MetricsController', ['$scope', 'resolvedMetrics','Metrics','$window', '$http',
    function ($scope, resolvedMetrics,Metrics, $window, $http) {
        $scope.metrics = resolvedMetrics;
        $scope.init = function(){
        	$scope.metrics= Metrics.get()
        };
        $scope.rungc = function(){
        	$http.get("@cstl/spring/admin/jvm/rungc;jsessionid=").then(function(){
        		$scope.metrics= Metrics.get()
        	});
        };
    }]);

cstlAdminApp.controller('LogsController', ['$scope', 'resolvedLogs', 'LogsService',
    function ($scope, resolvedLogs, LogsService) {
        $scope.loggers = resolvedLogs;

        $scope.changeLevel = function (name, level) {
            LogsService.changeLevel({name: name, level: level}, function () {
                $scope.loggers = LogsService.findAll();
            });
        };
    }]);

cstlAdminApp.controller('navCtrl', ['$scope', '$location', function ($scope, $location) {
    $scope.navClass = function (page) {
        var currentRoute = $location.path().split('/')[1] || 'home';
        return page === currentRoute ? 'menu-selected' : '';
    };   
    $scope.navClassAdmin = function () {
        var currentRouteAdmin = $location.path().substring(1) || 'home';
        if(currentRouteAdmin=='user')
            return 'menu-selected';
        else if(currentRouteAdmin=='group')
            return 'menu-selected';
        else if(currentRouteAdmin=='metrics')
            return 'menu-selected';
        else if(currentRouteAdmin=='logs')
            return 'menu-selected';
        else if(currentRouteAdmin=='contact')
            return 'menu-selected';
        else if(currentRouteAdmin=='task')
            return 'menu-selected';
    };       
}]);
