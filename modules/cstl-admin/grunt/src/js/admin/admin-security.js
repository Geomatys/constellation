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

    .controller('UserController', function($scope, UserResource, RoleResource, Growl, $modal) {
        $scope.search = {
            page: 1,
            size: 5,
            text: '',
            sort: {
                field: 'cstl_user.login',
                order: 'ASC'
            }
        };

        $scope.loadPage = function(page) {
            $scope.search.page = page;
            UserResource.search($scope.search, function(data){
                $scope.response = data;
            });
        };

        $scope.sortOn = function(column) {
            if ($scope.search.sort && $scope.search.sort.field === column) {
                switch ($scope.search.sort.order) {
                    case 'ASC':
                        $scope.search.sort = null; // reset
                        break;
                    case 'DESC':
                        $scope.search.sort.order = 'ASC';
                        break;
                    default:
                        $scope.search.sort.order = 'DESC';
                        break;
                }
            } else {
                $scope.search.sort = { field: column, order: 'DESC' };
            }
            $scope.loadPage($scope.response.number);
        };

        $scope.edit = function(id){
            $modal.open({
                templateUrl: 'views/admin/user/edit.html',
                controller: 'UserDetailsController',
                resolve: {
                    'currentAccount' : function(Permission) {
                        return Permission.getAccount();
                    },
                    'user': function(UserResource){
                        return UserResource.getWithRole({id: id}).$promise;
                    },
                    'roles': function(RoleResource){
                        return RoleResource.getAll().$promise;
                    }
                }
            }).result.then(function(){
                $scope.loadPage($scope.response.number);
            });
        };

        $scope.add = function(id){
            $modal.open({
                templateUrl: 'views/admin/user/add.html',
                controller: 'UserDetailsController',
                resolve: {
                    'currentAccount' : function(Permission) {
                        return Permission.getAccount();
                    },
                    'user': function(){
                        return {roles: [""]};
                    },
                    'roles': function(RoleResource){
                        return RoleResource.getAll().$promise;
                    }
                }
            }).result.then(function(){
                $scope.loadPage($scope.response.number);
            });
        };

        $scope.updateValidation = function(id){
            UserResource.updateValidation({id: id}, {}, function(){
                $scope.loadPage($scope.response.number);
            });
        };

        //init response
        $scope.loadPage(1);
    })

    .controller('UserDetailsController', function($rootScope, $scope, $modalInstance, $cookieStore, Growl, UserResource,
                                                  cfpLoadingBar, currentAccount, user, roles) {
        $scope.user = user;
        $scope.roles = roles;

        $scope.disableEditLogin = (currentAccount.login === user.login);

        $scope.password = "";
        $scope.password2 = "";

        //set password required : true case create user, false otherwise
        $scope.passwordRequired = Boolean(!$scope.user.id);

        //enable role
        $scope.enableRole = true;

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.save = function(){
            var formData = new FormData(document.getElementById('userForm'));
            if($scope.user.id){
                //edit
                $.ajax({
                    headers: {
                        'access_token': $rootScope.access_token
                    },
                    url: $cookieStore.get('cstlUrl') + 'api/1/user/edit',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    beforeSend: function(){
                        cfpLoadingBar.start();
                        cfpLoadingBar.inc();
                    },
                    success: function(result) {
                        cfpLoadingBar.complete();
                        $modalInstance.close();
                    },
                    error: function(result){
                        Growl('error', 'Error', 'Unable to edit user!');
                        cfpLoadingBar.complete();
                    }
                });
            } else {
                //add
                $.ajax({
                    headers: {
                        'access_token': $rootScope.access_token
                    },
                    url: $cookieStore.get('cstlUrl') + 'api/1/user/add',
                    type: 'POST',
                    data: formData,
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    beforeSend: function(){
                        cfpLoadingBar.start();
                        cfpLoadingBar.inc();
                    },
                    success: function(result) {
                        cfpLoadingBar.complete();
                        $modalInstance.close();
                    },
                    error: function(result){
                        Growl('error', 'Error', 'Unable to add user!');
                        cfpLoadingBar.complete();
                    }
                });
            }
        };
    });