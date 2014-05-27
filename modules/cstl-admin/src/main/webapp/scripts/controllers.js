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



cstlAdminApp.controller('navCtrl', ['$scope', '$location', function ($scope, $location) {
    $scope.navClass = function (page) {
        var currentRoute = $location.path().split('/')[1] || 'home';
        return page === currentRoute ? 'menu-selected' : '';
    };
}]);
