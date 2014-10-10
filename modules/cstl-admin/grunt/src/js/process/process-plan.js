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

            if ($scope.plan.time !== null) {
                var timeParts = $scope.plan.time.split(':');
                cron[1] = trimZero(timeParts[1]); //minutes
                cron[2] = trimZero(timeParts[0]); //hours
            }

            if ($scope.plan.date !== null) {
                var date = new Date($scope.plan.date);
                cron[3] = date.getDate();       //day of month
                cron[4] = date.getMonth() + 1;  //month
                cron[5] = date.getDay();        //day of week
            }

            //repeat
            var repeatIdx = $scope.repeatValues.indexOf($scope.plan.repeat);

            //exclude once
            if (repeatIdx !== 0) {

                //special case for weekly periodicity
                if (repeatIdx === 5) {
                    cron[3] = cron[4] = '*';
                } else {
                    //standard case (minutely, hourly, daily, monthly)
                    for (var i = repeatIdx; i < 6; i++) {
                        cron[i] = '*';
                    }
                }
            }
            
            return cron.join(' ');
        }

        /**
         * This parser doesn't support range (/) or L, W and # characters
         * in cron expression.
         * @param cronExpression
         */
        function parseCronExpression(cronExpression) {

            var minutely = isEveryMinutes(cronExpression);
            var hourly  = isEveryHour(cronExpression);
            var daily   = isEveryDay(cronExpression);
            var monthly = isEveryMonth(cronExpression);
            var weekly  = isEveryWeek(cronExpression);

            if (minutely){ $scope.plan.repeat = $scope.repeatValues[1]; }
            if (hourly)  { $scope.plan.repeat = $scope.repeatValues[2]; }
            if (daily)   { $scope.plan.repeat = $scope.repeatValues[3]; }
            if (monthly) { $scope.plan.repeat = $scope.repeatValues[4]; }
            if (weekly)  { $scope.plan.repeat = $scope.repeatValues[5]; }

            var cronParts = cronExpression.split(' ');

            //parse time
            var minute = null;
            var hour = null;

            if (!minutely) {
                minute = addZero(parseInt(cronParts[1]));
                if (!hourly) {
                    hour = addZero(parseInt(cronParts[2]));
                }
            }

            if (hour === null && minute === null) {
                $scope.plan.time = null;
            } else {
                if (hour === null) {
                    hour = '00';
                }
                if (minute === null) {
                    minute = '00';
                }
                $scope.plan.time = hour + ':' + minute;
            }

            //no need to parse date if periodicity is minutely or hourly or daily
            if (!minutely && !hourly && !daily) {
                //parse date
                var now = new Date();
                var year = now.getFullYear();
                var month = null;
                var day = null;

                if (weekly) {
                    var dayOfWeek = parseInt(cronParts[5]);
                    month = '01';

                    //find in first month the first day with the same dayOfWeek
                    var d = 1;
                    var tmpDate = new Date();
                    do  {
                        tmpDate = new Date(year, 0, d, 0, 0, 0, 0);
                        d++;
                    } while (tmpDate.getDay() !== dayOfWeek);

                    day = addZero(tmpDate.getDate());

                } else {
                    month = cronParts[4] === '*' ? null : addZero(parseInt(cronParts[4]));
                    day = cronParts[3] === '*' ? null : addZero(parseInt(cronParts[3]));
                }

                if (month === null && day === null) {
                    $scope.plan.date = null;
                } else {
                    if (month === null) {
                        month = '01';
                    }
                    if (day === null) {
                        day = '01';
                    }
                    $scope.plan.date = year + "-" + month + '-' +day;
                }
            }
        }

        function isEveryMinutes(exp) {
            return exp.search(/[0-9]\d{0,1} \* \* \* \* \*/i) === 0;
        }

        function isEveryHour(exp) {
            return exp.search(/[[0-9]\d{0,1} [0-9]\d{0,1} \* \* \* \*/i) === 0;
        }

        function isEveryDay(exp) {
            return exp.search(/[0-9]\d{0,1} [0-9]\d{0,1} [0-9]\d{0,1} \* \* \*/i) === 0;
        }

        function isEveryWeek(exp) {
            return exp.search(/[0-9]\d{0,1} [0-9]\d{0,1} [0-9]\d{0,1} \* \* [0-6]/i) === 0;
        }

        function isEveryMonth(exp) {
            return exp.search(/[0-9]\d{0,1} [0-9]\d{0,1} [0-9]\d{0,1} [0-9]\d{0,1} \* \*/i) === 0;
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

        function init() {
            $scope.plan.triggerType = $scope.task.triggerType;
            if ($scope.task.triggerType === $scope.cronTrigger) {
                parseCronExpression($scope.task.trigger);
            } else if ($scope.task.triggerType === $scope.folderTrigger) {
                $scope.plan.folder = $scope.task.trigger;
            }
        }

        // Scope variables
        $scope.task = task;

        $scope.repeatValues = [
            'tasks.plan.label.once',
            'tasks.plan.label.everyMinute',
            'tasks.plan.label.everyHour',
            'tasks.plan.label.everyDay',
            'tasks.plan.label.everyMonth',
            'tasks.plan.label.everyWeek'];

        $scope.cronTrigger = 'CRON';
        $scope.folderTrigger = 'FOLDER';

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
            var oldTriggerType = $scope.task.triggerType;
            var oldTrigger =  $scope.task.trigger;

            $scope.task.triggerType = $scope.plan.triggerType;
            if ($scope.plan.triggerType === $scope.cronTrigger) {
                $scope.task.trigger = generateCronExpression();
            } else if ($scope.plan.triggerType === $scope.folderTrigger){
                $scope.task.trigger = $scope.plan.folder;
            } else {
                $scope.task.trigger = null;
            }

            // no need to re-schedule if trigger unchanged
            if ($scope.task.triggerType !== oldTriggerType || $scope.task.trigger !== oldTrigger) {
                //Update task
                TaskService.updateParamsTask($scope.task).$promise
                    .then(function(response) {
                        Growl('success', 'Success', 'The task is correctly saved');

                        if ($scope.task.triggerType !== null) {
                            TaskService.startScheduleParamsTask($scope.task).$promise
                                .then(function (response) {
                                    Growl('success', 'Success', 'The task successfully scheduled');
                                }).catch(function () {
                                    Growl('error', 'Error', 'Error to schedule the task');
                                });
                        } else {
                            //no triggerType -> stop task scheduling
                            TaskService.stopScheduleParamsTask($scope.task).$promise
                                .then(function (response) {
                                    Growl('success', 'Success', 'The task successfully removed from scheduler');
                                }).catch(function () {
                                    Growl('error', 'Error', 'Error to un-schedule the task');
                                });
                        }
                    }).catch(function(){
                        Growl('error', 'Error', 'Error to save the task');
                    });
            }

            $modalInstance.close();
        };

        $scope.reset = function() {
            $scope.plan = {
                date : null,
                time : null,
                repeat : $scope.repeatValues[0],
                folder : null,
                triggerType : null
            };
        };

        $scope.isValid = function(elementName) {
            return !jQuery("#"+elementName).hasClass("ng-invalid");
        };

        $scope.$watch('describeProcess', function(newValue, oldValue) {
            init();
        }, true);

    });