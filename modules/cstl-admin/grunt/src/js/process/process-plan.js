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

angular.module('cstl-process-plan', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('ModalPlanTaskController', function($scope, $modalInstance, Growl, textService, TaskService, task){

        // Private function
        function generateCronExpression() {
            var cron = ['0', '0', '0', '0', '0', '0']; //seconds, minutes, hours, dayOfMonth, month, dayOfWeek

            if ($scope.plan.time !== null && $scope.plan.date !== null) {
                var timeParts = $scope.plan.time.split(':');
                cron[1] = trimZero(timeParts[1]); //minutes
                cron[2] = trimZero(timeParts[0]); //hours

                var date = new Date($scope.plan.date);
                cron[3] = date.getDate(); //day of month
                cron[4] = date.getMonth() + 1; //month
                cron[5] = date.getDay() + 1; //day of week
            }

            //repeat
            var repeatIdx = $scope.repeatValues.indexOf($scope.plan.repeat);
            //exclude once
            if (repeatIdx !== 0) {
                cron[repeatIdx] = '*';
            }
            
            return cron.join(' ');
        }

        function parseCronExpression(cronExpression) {
            var cronParts = cronExpression.split(' ');

            var repeatIdx = cronParts.indexOf('*');
            if (repeatIdx !== -1) {
                $scope.plan.repeat = $scope.repeatValues[repeatIdx];
            }

            var now = new Date();
            var year = now.getFullYear();
            var month = cronParts[4] === '*' ? now.getMonth() : parseInt(cronParts[4]);
            var day = cronParts[3] === '*' ? now.getDate() : parseInt(cronParts[3]);
            var hour = cronParts[2] === '*' ? '--' : addZero(parseInt(cronParts[2]));
            var minute = cronParts[1] === '*' ? '--' : addZero(parseInt(cronParts[1]));

            $scope.plan.date = year+"-"+addZero(month+1)+'-'+addZero(day+1);
            $scope.plan.time = hour+':'+minute;

        }

        function trimZero(candidate) {
            if (candidate.length === 2 && candidate[0] === '0') {
                return candidate.slice(1);
            }

            if (candidate === '--') {
                return '0';
            }
            return candidate;
        }

        function addZero(candidate) {
            if (candidate < 10) {
                return '0'+candidate;
            }
            return candidate;
        }

        // Scope variables
        $scope.task = task;

        $scope.repeatValues = ['once', 'everyMinute', 'everyHour', 'everyDay', 'everyMonth', 'everyWeek'];
        $scope.plan = {
                date : null,
                time : null,
                repeat : $scope.repeatValues[0],
                folder : null,
                triggerType : null
            };

        // scope functions
        $scope.close = $scope.cancel = $modalInstance.close;

        $scope.validate = function() {

            if (!$scope.isValid("formModalAddTask")) {
                Growl('error', 'Error', 'Form is invalid');
                return false;
            }

            console.log($scope.plan);
            //$scope.task.triggerType = $scope.plan.triggerType;
            if ($scope.plan.triggerType === 'cron') {
                $scope.task.trigger = generateCronExpression();
            } else if ($scope.plan.triggerType === 'folder'){
                $scope.task.trigger = $scope.plan.folder;
            } else {
                $scope.task.trigger = null;
            }

            //Update task
            TaskService.updateParamsTask($scope.task).$promise
            .then(function(response) {
                Growl('success', 'Success', 'The task is correctly save');
                $modalInstance.close();
            }).catch(function(){
                Growl('error', 'Error', 'Error to save the task');
            });
        };

        $scope.isValid = function(elementName) {
            return !jQuery("#"+elementName).hasClass("ng-invalid");
        };

    });