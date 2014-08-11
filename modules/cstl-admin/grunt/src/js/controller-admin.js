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
            'system_logs' :         {name:'system_logs',        url:'views/admin/logs.html'},
            'system_contact' :      {name:'system_contact',     url:'views/admin/contact.html'},
            'system_about' :        {name:'system_about',       url:'views/admin/about.html'},
            'tasks_manager' :       {name:'tasks_manager',      url:'views/admin/tasks_manager.html'},
            'planning' :            {name:'planning',           url:'views/admin/planning.html'},
            'users' :               {name:'users',              url:'views/admin/users.html'},
            'groups' :              {name:'groups',             url:'views/admin/groups.html'},
            'domains' :             {name:'domains',            url:'views/admin/domains.html'},
            'domainmembers' :       {name:'domainmembers',      url:'views/admin/domain/members.html'}};

        $scope.currentView = $scope.viewUrls['system_state'];

        $scope.changeView = function(page) {
            $scope.currentView = $scope.viewUrls[page];
        };

        if($location.url()==='/admin/system_state'){
            $scope.changeView('system_state');
        }else if($location.url()==='/admin/system_settings'){
            $scope.changeView('system_settings');
        }else if($location.url()==='/admin/system_logs'){
            $scope.changeView('system_logs');
        }else if($location.url()==='/admin/system_contact'){
            $scope.changeView('system_contact');
        }else if($location.url()==='/admin/system_about'){
            $scope.changeView('system_about');
        }else if($location.url()==='/admin/tasks_manager'){
            $scope.changeView('tasks_manager');
        }else if($location.url()==='/admin/planning'){
            $scope.changeView('planning');
        }else if($location.url()==='/admin/users'){
            $scope.changeView('users');
        }else if($location.url()==='/admin/groups'){
            $scope.changeView('groups');
        }else if($location.url()==='/admin/domains'){
            $scope.changeView('domains');
        }else if($location.url().indexOf('/admin/domainmembers/') != -1){
            $scope.changeView('domainmembers');
        }
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

        $scope.tasks = TaskService.listTasks();

        var topic = StompService.subscribe('/topic/taskevents', function(data){
            var event = JSON.parse(data.body);
            var task = $scope.tasks[event.id];
            if(task!=null){
                task.percent = event.percent;
                if(task.percent > 99)
                    delete $scope.tasks[event.id];
                $scope.$digest();
            }else{
                //new task
                $scope.tasks[event.id] = {
                    id: event.id,
                    status: event.status,
                    message: event.message,
                    percent: event.percent
                };
                $scope.$digest();
            }
        });
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
                var errorCode = err.data;
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
                    return;

            $scope.user.roles[$scope.user.roles.length]=role
        };
        var timeout=null;
        $scope.checkLogin = function(login){
            if(timeout != null){
                window.clearTimeout(timeout);
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

cstlAdminApp.controller('DomainSwitcherController', ['Account', '$scope', '$cookies', '$window',
  function(Account, $scope, $cookies, $window){
    Account.get(function(account){
      $scope.domains = account.domains;
      for(var d in account.domains){
        if(account.domains[d].id == $cookies.cstlActiveDomainId){
          $scope.activeDomain=account.domains[d].name;
          break;
        }
      }
      $scope.changeDomain = function(i){
       if($cookies.cstlActiveDomainId != account.domains[i].id){
         $scope.activeDomain=account.domains[i].name;
         $cookies.cstlActiveDomainId= ""+account.domains[i].id;
         $window.location.href="admin.html";
       }
      };
    }) 
}]);

cstlAdminApp.controller('DomainController', ['$scope', '$modal', 'DomainResource',
    function ($scope, $modal, DomainResource) {

        $scope.add = dataNotReady;
        $scope.details = dataNotReady;
        $scope.members = dataNotReady;
        $scope.deleteDomain = dataNotReady;

        DomainResource.query(function(domains){

            $scope.domains = domains;

            $scope.add = function(i) {
                $modal.open({
                    templateUrl: 'views/admin/domain/details.html',
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
                    var errorCode = err.data;
                    $translate(['Error',errorCode]).then(function (translations) {
                        $growl('error', translations.Error,  translations[errorCode]);
                    });
                });
            };

            $scope.details = function(i) {
                $modal.open({
                    templateUrl: 'views/admin/domain/details.html',
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
                    templateUrl: 'views/admin/domain/members.html',
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
    }
]);


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

    }
]);


cstlAdminApp.controller('DomainMembersController', ['$scope', '$routeParams', 'DomainResource', 'DomainRoleResource', '$modal',
    function ($scope, $routeParams, DomainResource, DomainRoleResource, $modal) {
        DomainResource.get({id: $routeParams.domainId}, function(domain){
            $scope.domain = domain
        });
        DomainResource.members({id: $routeParams.domainId}, function(members){
            $scope.members = members
        });
        
        $scope.changeDomainRoles = function(user) {
            $modal.open({
                templateUrl: 'views/admin/domain/changeUserRoles.html',
                controller: 'DomainChangeUserRolesController',
                resolve: {
                    'domain': function(){ return $scope.domain},
                    'user': function(){ return angular.copy(user)},
                    'allDomainRoles': function(){return DomainRoleResource.query().$promise}
                }
            }).result.then(function(domain){
                    DomainResource.members({id: $routeParams.domainId}, function(members){
                        $scope.members = members
                    })
                });
        };

        $scope.addMembers = function(i) {
            $modal.open({
                templateUrl: 'views/admin/domain/addMembers.html',
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
        };
        $scope.removeMemberFromDomain = function(i){
            DomainResource.removeMemberFromDomain({domainId: $scope.domain.id, userId: $scope.members[i].id}, function(){
                $scope.members.splice(i, 1);
            })
        };


    }
]);

cstlAdminApp.controller('DomainAddMembersController', ['$scope', '$modalInstance', 'domain', 'users', 'DomainResource',
    function ($scope, $modalInstance, domain, users, DomainResource) {
        $scope.domain = domain;
        $scope.users = users;

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.addToDomain = function(i, roles){
            var user = $scope.users[i];
            DomainResource.addMemberToDomain({userId: user.id, domainId: $scope.domain.id}, [1], function(){
                $scope.users.splice(i, 1);
                if($scope.users.length==0){
                    $modalInstance.close('close');
                }
            });
        };


    }
]);

//
// User domainrole in a given domain management.
//
cstlAdminApp.controller('DomainChangeUserRolesController', ['$scope', '$modalInstance', 'domain', 'user', 'allDomainRoles', 'DomainRoleResource', 'DomainResource',
  function ($scope, $modalInstance, domain, user, allDomainRoles, DomainRoleResource, DomainResource) {
    $scope.domain = domain;
    $scope.user = user;
    $scope.allDomainRoles = allDomainRoles;
    
    $scope.close = function() {
      $modalInstance.dismiss('close');
    };

    $scope.save = function(){
      var roleIds = [];
      for(var i in $scope.user.domainRoles){
        roleIds[roleIds.length] = $scope.user.domainRoles[i].id;
      }
      DomainResource.updateMemberInDomain({userId: user.id, domainId: $scope.domain.id}, roleIds, function(){
         $modalInstance.close(user);

      });
    };
}]);

cstlAdminApp.controller('DomainRoleController', ['$scope', '$modal', '$growl', '$translate', 'DomainRoleResource', 'PermissionService',
    function ($scope, $modal, $growl, $translate, DomainRoleResource, PermissionService) {

        $scope.domainroles = DomainRoleResource.query({withMembers:true});

        $scope.add = dataNotReady;
        $scope.details = dataNotReady;
        $scope.members = dataNotReady;
        $scope.deleteDomain = dataNotReady;

        PermissionService.all().success(function(data){
            $scope.allPermissions = data;

            $scope.add = function(i) {
                $modal.open({
                    templateUrl: 'views/admin/group/details.html',
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
            };

            $scope.details = function(i) {
                $modal.open({
                    templateUrl: 'views/admin/group/details.html',
                    controller: 'DomainRoleDetailsController',
                    resolve: {
                        'domainrole': function(){return DomainRoleResource.get({id: $scope.domainroles[i].id})},
                        'allPermissions': function(){ return $scope.allPermissions }
                    }
                }).result.then(function(domainrole){
                        if(domainrole != null){
                          domainrole.memberList = $scope.domainroles[i].memberList             
                          $scope.domainroles[i] = domainrole;
                        }
                    });
            };

        });
    }
]);

cstlAdminApp.controller('DomainRoleDetailsController', ['$scope', '$translate', '$modalInstance', 'domainrole', 'allPermissions', 'DomainRoleResource',
    function ($scope, $translate, $modalInstance, domainrole, allPermissions, DomainResource) {
        $scope.domainrole = domainrole;

        $scope.allPermissions = allPermissions;

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };


        $scope.save = function(){
            var domainResource = new DomainResource($scope.domainrole);
            if($scope.domainrole.id == undefined){
              domainResource.$save(function(saved){
                $modalInstance.close(saved);
              }, function(){
                $translate(['Error','admin.domain.save.error']).then(function (translations) {
                  $growl('error', translations.Error,  translations['admin.domain.save.error']);
                });
              });
            } else {
              domainResource.$update({id: $scope.domainrole.id }, function(updated){
                $modalInstance.close(updated);
              }, function(){
                $translate(['Error','admin.domain.save.error']).then(function (translations) {
                  $growl('error', translations.Error,  translations['admin.domain.save.error']);
                })});
            }
        }

    }
]);
