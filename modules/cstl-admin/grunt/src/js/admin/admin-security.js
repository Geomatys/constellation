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

angular.module('cstl-admin-security', ['cstl-restapi', 'cstl-services', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .filter('roleLabel', function($filter) {
        return function(key) {
            switch (key) {
                case 'cstl-admin':
                    return $filter('translate')('settings.form.role.admin');
                case 'cstl-publish':
                    return $filter('translate')('settings.form.role.publish');
                case 'cstl-data':
                    return $filter('translate')('settings.form.role.data');
                default:
                    return key;
            }
        };
    })

    .controller('UserController', function($scope, UserResource, $modal, Growl, $translate) {
        $scope.list = UserResource.query({"withRoles": true});
        $scope.details = function(i) {
            $modal.open({
                templateUrl: 'views/admin/user/details.html',
                controller: 'UserDetailsController',
                resolve: {
                    'isUpdate': function() {return true;},
                    'user': function(){
                        return angular.copy($scope.list[i]);
                    }
                }
            }).result.then(function(user){
                    if(user){
                        $scope.list[i] = user;
                    }
                });
        };
        $scope.add = function(i) {
            $modal.open({
                templateUrl: 'views/admin/user/add.html',
                controller: 'UserDetailsController',
                resolve: {
                    'isUpdate': function() {return false;},
                    'user': function(){
                        return { roles: ['cstl-data'] };
                    }
                }
            }).result.then(function(user){
                    if(user){
                        $scope.list[$scope.list.length] = user;
                    }
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

        var timeout=null;
        $scope.checkLogin = function(login){
            if(timeout){
                window.clearTimeout(timeout);
                timeout = null;
            }
            timeout = setTimeout(function(){
                if($scope.user.login && $scope.user.login.length > 2){
                    GeneralService.checkLogin($scope.user.login).success(function(res){
                        $scope.loginInUse=res.available==="false";
                    }).error(function(){
                    });
                }
            }, 400);
        };

        $scope.save = function(){
            var userResource = new UserResource($scope.user);
            if(isUpdate){
                userResource.$update(function(updated){
                    $modalInstance.close(updated);
                }, function(){
                    $translate(['Error','admin.user.save.error']).then(function (translations) {
                        Growl('error', translations.Error,  translations['admin.user.save.error']);
                    });});
            }
            else{
                userResource.$save(function(saved){
                    $modalInstance.close(saved);
                }, function(){
                    $translate(['Error','admin.user.save.error']).then(function (translations) {
                        Growl('error', translations.Error,  translations['admin.user.save.error']);
                    });
                });
            }
        };
    });