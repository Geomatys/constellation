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

angular.module('cstl-admin-security', [])

    .controller('UserController', function($scope, UserResource, $modal, Growl, $translate) {
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
                    Growl('error', translations.Error,  translations[errorCode]);
                });
            });
        };
    })
    
    .controller('UserDetailsController', function($scope, $modalInstance, GeneralService, user, isUpdate, UserResource, Growl, $translate) {
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
                        Growl('error', translations.Error,  translations['admin.user.save.error']);
                    })});
            else
                userResource.$save(function(saved){
                    $modalInstance.close(saved);
                }, function(){
                    $translate(['Error','admin.user.save.error']).then(function (translations) {
                        Growl('error', translations.Error,  translations['admin.user.save.error']);
                    });
                });
        }
    })
    
    .controller('DomainSwitcherController', function(Account, $scope, $cookies, $window) {
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
    })
    
    .controller('DomainController', function($scope, $modal, DomainResource) {
    
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
                        Growl('error', translations.Error,  translations[errorCode]);
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
    })
    
    .controller('DomainDetailsController', function($scope, $modalInstance, domain, DomainResource) {
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
                        Growl('error', translations.Error,  translations['admin.domain.save.error']);
                    })});
            else
                domainResource.$save(function(saved){
                    $modalInstance.close(saved);
                }, function(){
                    $translate(['Error','admin.domain.save.error']).then(function (translations) {
                        Growl('error', translations.Error,  translations['admin.domain.save.error']);
                    });
                });
        }
    })
    
    .controller('DomainMembersController', function($scope, $routeParams, DomainResource, DomainRoleResource, $modal) {
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
    })
    
    .controller('DomainAddMembersController', function($scope, $modalInstance, domain, users, DomainResource) {
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
    })
    
    .controller('DomainChangeUserRolesController', function($scope, $modalInstance, domain, user, allDomainRoles, DomainRoleResource, DomainResource) {
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
    })
    
    .controller('DomainRoleController', function($scope, $modal, Growl, $translate, DomainRoleResource, PermissionService) {

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
                        Growl('error', translations.Error,  translations[errorCode]);
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
    })
    
    .controller('DomainRoleDetailsController', function($scope, $translate, $modalInstance, domainrole, allPermissions, DomainResource) {

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
                        Growl('error', translations.Error,  translations['admin.domain.save.error']);
                    });
                });
            } else {
                domainResource.$update({id: $scope.domainrole.id }, function(updated){
                    $modalInstance.close(updated);
                }, function(){
                    $translate(['Error','admin.domain.save.error']).then(function (translations) {
                        Growl('error', translations.Error,  translations['admin.domain.save.error']);
                    })});
            }
        }
    });