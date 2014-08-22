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

cstlAdminApp.controller('TasksController', ['$scope', '$dashboard', '$growl', '$modal', 'TaskService',
        function ($scope, $dashboard, $growl, $modal, TaskService){
            var lastOpened = null;

            $scope.nbbypage = 5; // Default value at 5

            $scope.init = function() {
                var modalLoader = $modal.open({
                    templateUrl: 'views/modalLoader.html',
                    controller: 'ModalInstanceCtrl'
                });
                TaskService.listParamsTask({}).$promise
                    .then(function(response){
                        // On success
                        $dashboard($scope, response, false);
                    }).catch(function(){
                        // On error
                        $growl('error', 'Error', 'Unable to get tasks list');
                    })['finally'](function(){
                    // On all case
                    modalLoader.close()
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
                    $growl('success', 'Success', 'The task is currently execute');
                }).catch(function(){
                    $growl('error', 'Error', "Can't execute this task");
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
                if (window.innerWidth >= 1200){
                    if(small==true && text.length > 30){
                        return text.substr(0,30)+"...";
                    } else if(small==false && text.length > 65){
                        return text.substr(0,65)+"...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992){
                    if(small==true && text.length > 22){
                        return text.substr(0,22)+"...";
                    } else if(small==false && text.length > 50){
                        return text.substr(0,50)+"...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if(text.length > 40){
                        return text.substr(0,40)+"...";
                    } else return text;
                }
            };
            $scope.truncateTitleBlock = function(text){
                if (window.innerWidth >= 1200){
                    if(text.length > 40){
                        return text.substr(0,40)+"...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992){
                    if(text.length > 30){
                        return text.substr(0,30)+"...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if(text.length > 20){
                        return text.substr(0,20)+"...";
                    } else return text;
                }
            };
        }]
);

// The controller for the add task modal
cstlAdminApp.controller('ModalAddTaskController', ['$scope', '$modalInstance', '$growl', 'textService', 'TaskService', 'processes', 'task',
        function($scope, $modalInstance, $growl, textService, TaskService, processes, task){

            // Private function
            function parseProcessDefaultName(processName) {
                var authBegin = processName.indexOf('{')+1;
                var authEnd = processName.indexOf('}', authBegin);

                return [processName.substr(authBegin, authEnd-authBegin), processName.substr(authEnd+1)];
            }

            function createProcesses(processesList) {

                var processAuthority = $scope.task.processAuthority || "";
                var processCode = $scope.task.processCode || "";

                var tree = {};
                for (var p in processesList) {
                    var process = parseProcessDefaultName(processesList[p]);
                    tree[process[0]] = tree[process[0]] || [];
                    var codeInd = tree[process[0]].push(process[1])-1;

                    if (process[0] == processAuthority && process[1] == processCode){
                        $scope.option.processIndex = ""+codeInd;
                    }
                }

                var procTree = [];
                for (var auth in tree) {
                    var indAuth = procTree.push({
                            'auth' : auth,
                            'processes' : tree[auth]
                        })-1;
                    if (auth == processAuthority) {
                        $scope.option.authIndex = ""+indAuth;
                    }
                }
                return procTree;
            }

            function getDescribeProcess() {
                var authority = $scope.processes[$scope.option.authIndex];
                textService.getProcessDescriptor(authority.auth, authority.processes[$scope.option.processIndex])
                    .success(function(data){ // On success
                        $scope.describeProcess = data;
                    }).error(function(data){ // On error
                        $growl('error', 'Error', 'Unable to get the describe process');
                    });
            }

            function restoreInputs(){
                if ($scope.task.inputs) {
                    var dom = jQuery(jQuery.parseXML($scope.task.inputs));

                    for (var iter in $scope.inputs){
                        var name = $scope.inputs[iter].name;
                        dom.find(name).each(function(ind, _el){
                            switch($scope.inputs[iter].annotation.info){
                                case "valueClass:java.lang.Double":
                                    $scope.inputs[iter].save[ind] = parseFloat(jQuery(_el)[0].innerHTML);
                                    break;
                                case "valueClass:java.lang.Integer":
                                    $scope.inputs[iter].save[ind] = parseInt(jQuery(_el)[0].innerHTML);
                                    break;
                                case "valueClass:java.lang.Boolean":
                                    $scope.inputs[iter].save[ind] = jQuery(_el)[0].innerHTML == "true";
                                    break;
                                default:
                                    $scope.inputs[iter].save[ind] = jQuery(_el)[0].innerHTML;
                            }

                        });
                    }
                }
            }

            // Scope variables

            $scope.manageField = [
                "valueClass:java.lang.String",
                "valueClass:java.lang.Boolean",
                "valueClass:java.lang.Integer",
                "valueClass:java.lang.Double",
                "valueClass:java.net.URL",
                "valueClass:java.io.File"
            ];
            $scope.canManage = false;

            $scope.option = {
                authIndex : 0,
                processIndex : 0
            };

            $scope.describeProcess = undefined;
            $scope.inputs = [];
            $scope.task = task;

            $scope.processes = createProcesses(processes['Entry']);

            // scope functions
            $scope.close = $scope.cancel = $modalInstance.close;

            $scope.validate = function() {

                if (!$scope.isValid("formModalAddTask")) {
                    $growl('error', 'Error', 'Form is invalid');
                    return false;
                }

                $scope.task.processAuthority = $scope.processes[$scope.option.authIndex].auth;
                $scope.task.processCode = $scope.processes[$scope.option.authIndex].processes[$scope.option.processIndex];
                $scope.task.inputs = '<input xmlns="http://www.geotoolkit.org/parameter">';

                for (var i in $scope.inputs){
                    var element = $scope.inputs[i];

                    if (!$scope.isValid(element.name)) {
                        $growl('error', 'Error', 'Form is invalid');
                        return false;
                    }

                    switch(element.annotation.info) {

                        case "valueClass:java.lang.Boolean" :
                            if (element.save && element.save.length > 0) {
                                for (var s in element.save) {
                                    $scope.task.inputs += '<' + element.name + '>' + element.save[s] + '</' + element.name + '>';
                                }
                            } else {
                                $scope.task.inputs += '<' + element.name + '>' + (element.default||false) + '</' + element.name + '>';
                            }
                            break;

                        case "valueClass:java.lang.String" :
                        default:
                            if (element.save && element.save.length > 0) {
                                for (var s in element.save) {
                                    $scope.task.inputs += '<' + element.name + '>' + element.save[s] + '</' + element.name + '>';
                                }
                            } else if (element.default) {
                                $scope.task.inputs += '<' + element.name + '>' + element.default + '</' + element.name + '>';
                            }
                            break;
                    }
                }
                $scope.task.inputs += '</input>';

                if ($scope.task.id != 0){
                    TaskService.updateParamsTask($scope.task).$promise
                        .then(function(response) {
                            $growl('success', 'Success', 'The task is correctly save');
                            $modalInstance.close();
                        }).catch(function(){
                            $growl('error', 'Error', 'Error to save the task');
                        });
                } else {
                    TaskService.createParamsTask($scope.task).$promise
                        .then(function(response) {
                            $growl('success', 'Success', 'New task is correctly register');
                            $modalInstance.close();
                        }).catch(function(){
                            $growl('error', 'Error', 'Error to save the new task');
                        });
                }
            };

            $scope.isValid = function(elementName) {
                return !jQuery("#"+elementName).hasClass("ng-invalid");
            };

            // scope watcher
            $scope.$watch('option.authIndex', function(newValue, oldValue){
                if (newValue != oldValue) {
                    if ($scope.option.processIndex == 0) {
                        getDescribeProcess();
                    } else {
                        $scope.option.processIndex = 0;
                    }
                }
            }, true);

            $scope.$watch('option.processIndex', function(newValue, oldValue){
                getDescribeProcess();
            }, true);

            $scope.$watch('describeProcess', function(newValue, oldValue){
                if (newValue != oldValue) {
                    $scope.canManage = true;

                    var inputs = [];
                    var save = {};
                    var dom = jQuery(jQuery.parseXML(newValue));

                    var getAnnotationFor = function(_el) {
                        var element = jQuery(_el);
                        var annotation = {};
                        annotation.info = element.find("appinfo").get(0).innerHTML;
                        annotation.documentation = element.find("documentation").get(0).innerHTML;

                        return annotation;
                    };

                    var getRestrictionFor = function(_el) {
                        var element = jQuery(_el);

                        var enumerationList = [];
                        element.find("restriction").find("enumeration").each(function (a, _enum) {
                            enumerationList.push(jQuery(_enum).attr("value"));
                        });
                        return {
                            enumeration : enumerationList
                        }
                    };

                    dom.find('element[name=input]').find('element').each(function(a, el) {
                        var element = jQuery(el);

                        var index = inputs.push({
                                'mandatory' : true,
                                'name': element.attr("name"),
                                'default' : element.attr("default") || "",
                                'minOccurs' : element.attr("minOccurs") || "1",
                                'maxOccurs' : element.attr("maxOccurs") || "1",
                                'annotation' : getAnnotationFor(element),
                                'restriction' : getRestrictionFor(element),
                                'save' : []
                            })-1; // Push add to the end of the array and return length... so length-1 => last element insert

                        // Check mandatory once
                        if (inputs[index].minOccurs == 0) {
                            inputs[index].mandatory = false;
                        }

                        if (inputs[index].mandatory === true && jQuery.inArray(inputs[index].annotation.info, $scope.manageField)===-1){
                            $scope.canManage = false;
                        }
                    });
                    $scope.inputs = inputs;
                    restoreInputs();
                }
            }, true);
        }]
);

                                     