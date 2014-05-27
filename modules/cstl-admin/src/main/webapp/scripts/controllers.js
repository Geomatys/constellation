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

var dataNotReady = function(){alert("data not ready")};

cstlAdminApp.controller('HeaderController', ['$scope','$http',
    function ($scope, $http) {
        $http.get("app/conf").success(function(data){
	       	  $scope.cstlLoginUrl = data.cstl + "spring/auth/form";
	          $scope.cstlLogoutUrl = data.cstl + "logout";
	      });
}]);

cstlAdminApp.controller('MainController', ['$scope','$location','webService','dataListing','ProcessService','$growl', 'UserResource', 'GeneralService', 'TaskService',
    function ($scope, $location, webService, dataListing, Process, $growl, UserResource, GeneralService, task) {
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

            GeneralService.counts().success(function(response) {
                $scope.nbusers = response.nbuser;
            }).error(function() {
                $scope.nbusers = 1;
                $growl('error', 'Error', 'Unable to count users');
            });
        };
    }]);

cstlAdminApp.controller('LanguageController', ['$scope', '$translate',
    function ($scope, $translate) {
        $scope.changeLanguage = function (languageKey) {
            $translate.use(languageKey);
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

cstlAdminApp.controller('UserController', ['$scope', 'UserResource', '$modal', '$growl', '$translate', 
  function ($scope, UserResource, $modal, $growl, $translate) {
    $scope.list = UserResource.query({"withDomainAndRoles": true});
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
}]);

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
    	  userResource.$update(function(updated){
          $modalInstance.close(updated);
        }, function(){
          $translate(['Error','admin.user.save.error']).then(function (translations) {
            $growl('error', translations.Error,  translations['admin.user.save.error']);
          })});
    	else
    	  userResource.$save(function(saved){
    	    $modalInstance.close(saved);
    	  }, function(){
    	    $translate(['Error','admin.user.save.error']).then(function (translations) {
            $growl('error', translations.Error,  translations['admin.user.save.error']);
          });
    	  });
    }
}]);

//  BEGIN Domain 
cstlAdminApp.controller('DomainController', ['$scope', '$modal', 'DomainResource',
  function ($scope, $modal, DomainResource) {

  $scope.add = dataNotReady;
  $scope.details = dataNotReady;
  $scope.members = dataNotReady;
  $scope.deleteDomain = dataNotReady;

  DomainResource.query({withMembers:true}, function(domains){
    
    $scope.domains = domains;
    
    $scope.add = function(i) {
      $modal.open({
          templateUrl: 'views/domain/details.html',
          controller: 'DomainDetailsController',
          resolve: {
              'domain': function(){ return {}}
          }
      }).result.then(function(domain){
          if(domain != null)
          $scope.domains[$scope.domains.length] = domain;
      });
  };

  $scope.deleteDomain = function(i){
    DomainResource.delete({id: $scope.domains[i].id}, {} , function(resp){
      $scope.domains.splice(i, 1);
    }, function(err){
      var errorCode = err.data
      $translate(['Error',errorCode]).then(function (translations) {
        $growl('error', translations.Error,  translations[errorCode]);
      });
    });
  }
  
  $scope.details = function(i) {
      $modal.open({
          templateUrl: 'views/domain/details.html',
          controller: 'DomainDetailsController',
          resolve: {
              domain: function(){return DomainResource.get({id: $scope.domains[i].id})}
          }
      }).result.then(function(domain){
          if(domain != null)
          $scope.domains[i] = domain;
      });
  };

  $scope.members = function(i) {
      $modal.open({
          templateUrl: 'views/domain/members.html',
          controller: 'DomainMembersController',
          resolve: {
            domain: function(){return DomainResource.get({id: $scope.domains[i].id}).$promise},
            members: function(){return DomainResource.members({id: $scope.domains[i].id}).$promise}
          }
      }).result.then(function(domain){
          if(domain != null)
          $scope.domains[$scope.domains.length] = domain;
      });
  };
    
    
  });
}]);


cstlAdminApp.controller('DomainDetailsController', ['$scope', '$modalInstance', 'domain', 'DomainResource',
function ($scope, $modalInstance, domain, DomainResource) {
  $scope.domain = domain;

  $scope.close = function() {
      $modalInstance.dismiss('close');
  };
   
  $scope.save = function(){
    var domainResource = new DomainResource($scope.domain);
    if($scope.domain.id)
      domainResource.$update({id: $scope.domain.id }, function(updated){
        $modalInstance.close(updated);
      }, function(){
        $translate(['Error','admin.domain.save.error']).then(function (translations) {
          $growl('error', translations.Error,  translations['admin.domain.save.error']);
        })});
    else
      domainResource.$save(function(saved){
        $modalInstance.close(saved);
      }, function(){
        $translate(['Error','admin.domain.save.error']).then(function (translations) {
          $growl('error', translations.Error,  translations['admin.domain.save.error']);
        });
      });
  }
  
     }]);

cstlAdminApp.controller('DomainMembersController', ['$scope', '$routeParams', 'DomainResource', 'DomainRoleResource', '$modal',
  function ($scope, $routeParams, DomainResource, DomainRoleResource, $modal) {
    DomainResource.get({id: $routeParams.domainId}, function(domain){
      $scope.domain = domain
    })
    DomainResource.members({id: $routeParams.domainId}, function(members){
      $scope.members = members
    })
    
    $scope.addMembers = function(i) {
        $modal.open({
            templateUrl: 'views/domain/addMembers.html',
            controller: 'DomainAddMembersController',
            resolve: {
                'domain': function(){ return $scope.domain.$promise},
                'users': function(){ return DomainResource.nonmembers({id: $routeParams.domainId}).$promise}
            }
        }).result.then(function(domain){
          DomainResource.members({id: $routeParams.domainId}, function(members){
            $scope.members = members
          })
        });
    } 
    $scope.removeMemberFromDomain = function(i){
      DomainResource.removeMemberFromDomain({domainId: $scope.domain.id, userId: $scope.members[i].id}, function(){
        $scope.members.splice(i, 1);
      })
    };
    
    
}]);


cstlAdminApp.controller('DomainAddMembersController', ['$scope', '$modalInstance', 'domain', 'users', 'DomainResource',
function ($scope, $modalInstance, domain, users, DomainResource) {
  $scope.domain = domain
  $scope.users = users

  $scope.close = function() {
    $modalInstance.dismiss('close');
  }
  
  $scope.addToDomain = function(i, roles){
    var user = $scope.users[i]
    DomainResource.addMemberToDomain({userId: user.id, domainId: $scope.domain.id}, [1], function(){
      $scope.users.splice(i, 1);
      if($scope.users.length==0){
        $modalInstance.close('close');
      }
    });
  };

  
}]);

cstlAdminApp.controller('DomainRoleController', ['$scope', '$modal', 'DomainRoleResource', 'PermissionService',
  function ($scope, $modal, DomainRoleResource, PermissionService) {

    $scope.domainroles = DomainRoleResource.query({withMembers:true});
    
    $scope.add = dataNotReady;
    $scope.details = dataNotReady;
    $scope.members = dataNotReady;
    $scope.deleteDomain = dataNotReady;
    
    PermissionService.all().success(function(data){
      $scope.allPermissions = data;
      
      $scope.add = function(i) {
        $modal.open({
            templateUrl: 'views/group/details.html',
            controller: 'DomainRoleDetailsController',
            resolve: {
                'domainrole': function(){ return {permissions:[]}},
                'allPermissions': function(){ return $scope.allPermissions }
            }
        }).result.then(function(domainrole){
            if(domainrole != null)
            $scope.domainroles[$scope.domainroles.length] = domainrole;
        });
    };

    $scope.deleteDomain = function(i){
      DomainRoleResource.delete({id: $scope.domainroles[i].id}, {} , function(resp){
        $scope.domainroles.splice(i, 1);
      }, function(err){
        var errorCode = err.data
        $translate(['Error',errorCode]).then(function (translations) {
          $growl('error', translations.Error,  translations[errorCode]);
        });
      });
    }
    
    $scope.details = function(i) {
        $modal.open({
            templateUrl: 'views/group/details.html',
            controller: 'DomainRoleDetailsController',
            resolve: {
                'domainrole': function(){return DomainRoleResource.get({id: $scope.domainroles[i].id})},
                'allPermissions': function(){ return $scope.allPermissions }
            }
        }).result.then(function(domainrole){
            if(domainrole != null)
            $scope.domainroles[i] = domainrole;
        });
    };

    $scope.members = function(i) {
        $modal.open({
            templateUrl: 'views/group/members.html',
            controller: 'GroupMembersController',
            resolve: {
                'isUpdate': function() {return false},
                'domainrole': function(){ 
                    return $scope.domain
                }
            }
        }).result.then(function(domain){
            if(domain != null)
            $scope.domains[$scope.domains.length] = domain;
        });
    };
      
      
    });
    
    
  

}]);

cstlAdminApp.controller('DomainRoleDetailsController', ['$scope', '$modalInstance', 'domainrole', 'allPermissions', 'DomainRoleResource',
  function ($scope, $modalInstance, domainrole, allPermissions, DomainResource) {
    $scope.domainrole = domainrole;

    $scope.allPermissions = allPermissions;
    
    $scope.close = function() {
        $modalInstance.dismiss('close');
    };
   
    
    $scope.save = function(){
      var domainResource = new DomainResource($scope.domainrole);
      if($scope.domainrole.id)
        domainResource.$update({id: $scope.domainrole.id }, function(updated){
          $modalInstance.close(updated);
        }, function(){
          $translate(['Error','admin.domain.save.error']).then(function (translations) {
            $growl('error', translations.Error,  translations['admin.domain.save.error']);
          })});
      else
        domainResource.$save(function(saved){
          $modalInstance.close(saved);
        }, function(){
          $translate(['Error','admin.domain.save.error']).then(function (translations) {
            $growl('error', translations.Error,  translations['admin.domain.save.error']);
          });
        });
    }
    
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
