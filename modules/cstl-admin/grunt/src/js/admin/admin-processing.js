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

angular.module('cstl-admin-processing', ['cstl-restapi', 'cstl-services'])

    .controller('TaskController', function($scope, $timeout, TaskService, StompService) {

        $scope.tasks = TaskService.listTasks();

        var topic = StompService.subscribe('/topic/taskevents', function(data) {
            var event = JSON.parse(data.body);
            var task = $scope.tasks[event.id];
            if (task !== null) {
                task.percent = event.percent;
                if (task.percent > 99) {
                    delete $scope.tasks[event.id];
                }
                $scope.$digest();
            } else {
                // New task
                $scope.tasks[event.id] = {
                    id: event.id,
                    status: event.status,
                    message: event.message,
                    percent: event.percent
                };
                $scope.$digest();
            }
        });

        $scope.$on('$destroy', function() {
            topic.unsubscribe();
        });
    });