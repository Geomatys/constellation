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

angular.module('cstl-process-dashboard', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('TasksController', function ($scope, Dashboard, Growl, $modal, TaskService, $window){
        var lastOpened = null;

        $scope.nbbypage = 5; // Default value at 5
        $scope.hideScroll = true;

        $scope.init = function() {
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            TaskService.listParamsTask({}).$promise
                .then(function(response){
                    // On success
                    Dashboard($scope, response, false);
                }).catch(function(){
                    // On error
                    Growl('error', 'Error', 'Unable to get tasks list');
                })['finally'](function(){
                // On all case
                modalLoader.close()
            });

            angular.element($window).bind("scroll", function() {
                if (this.pageYOffset < 220) {
                    $scope.hideScroll = true;
                } else {
                    $scope.hideScroll = false;
                }
                $scope.$apply();
            });
        };

        $scope.getUserName = function(id) {

        };

        $scope.deleteTask = function(idTask) {
            TaskService.deleteParamsTask({id:idTask}).$promise.then(function(){
                $scope.init();
            });
        };

        $scope.executeTask = function(idTask) {
            TaskService.executeParamsTask({id:idTask}).$promise.then(function(){
                Growl('success', 'Success', 'The task is currently execute');
            }).catch(function(){
                Growl('error', 'Error', "Can't execute this task");
            });
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#ProcessDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        // Open the add task modal
        $scope.showAddTaskPopup = function(idTask) {
            var id = idTask || 0;

            var modal = $modal.open({
                templateUrl: 'views/tasks/modalAddTask.html',
                controller: 'ModalAddTaskController',
                resolve : {
                    'processes' : function(){return TaskService.listProcess().$promise},
                    'task'      : function(){
                        if (idTask){
                            return TaskService.getParamsTask({id:idTask}).$promise;
                        } else {
                            return {
                                'id' : 0,
                                'name' : ""
                            }
                        }
                    }
                }
            });
            modal.result.then(function(){
                $scope.init();
            });
        };

        $scope.truncate = function(small, text){
            if(text != null) {
                if (window.innerWidth >= 1200) {
                    if (small == true && text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else if (small == false && text.length > 65) {
                        return text.substr(0, 65) + "...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small == true && text.length > 22) {
                        return text.substr(0, 22) + "...";
                    } else if (small == false && text.length > 50) {
                        return text.substr(0, 50) + "...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else return text;
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text != null) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if (text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else return text;
                }
            }
        };
    });