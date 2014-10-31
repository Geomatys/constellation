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

angular.module('cstl-process-edit', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('ModalAddTaskController', function($scope, $modalInstance, Growl, TaskService, processes, task, style){

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
                if(processesList.hasOwnProperty(p)){
                    var process = parseProcessDefaultName(processesList[p]);
                    tree[process[0]] = tree[process[0]] || [];
                    var codeInd = tree[process[0]].push(process[1])-1;

                    if (process[0] === processAuthority && process[1] === processCode){
                        $scope.option.processIndex = ""+codeInd;
                    }
                }
            }

            var procTree = [];
            for (var auth in tree) {
                if(tree.hasOwnProperty(auth)){
                    var indAuth = procTree.push({
                            'auth' : auth,
                            'processes' : tree[auth]
                        })-1;
                    if (auth === processAuthority) {
                        $scope.option.authIndex = ""+indAuth;
                    }
                }
            }
            return procTree;
        }

        function getDescribeProcess() {
            var authority = $scope.processes[$scope.option.authIndex];
            TaskService.describeProcess({'authority':authority.auth, 'code':authority.processes[$scope.option.processIndex]}).$promise
                .then(function(data){ // On success
                    $scope.describeProcess = data;
                }).catch(function(data){ // On error
                    Growl('error', 'Error', 'Unable to get the describe process');
                });
        }

        function restoreInputs(){
            if ($scope.task.inputs) {
                //convert to object
                if (angular.isString($scope.task.inputs)) {
                    $scope.task.inputs = angular.fromJson($scope.task.inputs);
                }

                for (var param in $scope.task.inputs) {
                    if($scope.task.inputs.hasOwnProperty(param)) {
                        var scopeInput = getInputByName(param);
                        scopeInput.save = $scope.task.inputs[param];
                    }
                }
            }
        }

        function getInputByName(param) {
            var filter = $scope.inputs.filter(function (elem) {
                return elem.name === param;
            });
            return filter[0];
        }

        // Scope variables

        $scope.manageField = [
            "valueClass:java.lang.String",
            "valueClass:java.lang.Boolean",
            "valueClass:java.lang.Integer",
            "valueClass:java.lang.Double",
            "valueClass:java.net.URL",
            "valueClass:java.io.File",
            "valueClass:org.constellation.util.StyleReference"
        ];
        $scope.canManage = false;

        $scope.option = {
            authIndex : 0,
            processIndex : 0
        };

        $scope.describeProcess = undefined;
        $scope.inputs = [];
        $scope.task = task;
        $scope.styles = [];

        $scope.processes = createProcesses(processes.Entry);

        // scope functions
        $scope.close = $scope.cancel = $modalInstance.close;

        $scope.listAvailableStyles = function() {
            style.listAll({provider: 'sld'}, function (response) {
                $scope.styles = [];
                jQuery.each(response.styles, function(i,style) {
                    var styleName = style.Name;
                    var styleRef = '${providerStyleType|sld|'+styleName+'}';
                    $scope.styles.push({name:styleName, ref:styleRef});
                });
            });
        };

        $scope.validate = function() {

            if (!$scope.isValid("formModalAddTask")) {
                Growl('error', 'Error', 'Form is invalid');
                return false;
            }

            $scope.task.processAuthority = $scope.processes[$scope.option.authIndex].auth;
            $scope.task.processCode = $scope.processes[$scope.option.authIndex].processes[$scope.option.processIndex];
            $scope.task.inputs = {};

            for (var i in $scope.inputs){
                if($scope.inputs.hasOwnProperty(i)){
                    var element = $scope.inputs[i];

                    if (!$scope.isValid(element.name)) {
                        Growl('error', 'Error', 'Form is invalid');
                        return false;
                    }

                    if (element.type === 'simple') {
                        $scope.task.inputs[element.name] = element.save;
                    } else {
                        $scope.task.inputs[element.name] = [];

                        //TODO handle group
                    }
                }
            }

            //convert to JSON
            $scope.task.inputs = angular.toJson($scope.task.inputs);

            if ($scope.task.id != null){
                TaskService.updateParamsTask($scope.task).$promise
                    .then(function(response) {
                        Growl('success', 'Success', 'The task is correctly save');
                        $modalInstance.close();
                    }).catch(function(){
                        Growl('error', 'Error', 'Error to save the task');
                    });
            } else {
                TaskService.createParamsTask($scope.task).$promise
                    .then(function(response) {
                        Growl('success', 'Success', 'New task is correctly register');
                        $modalInstance.close();
                    }).catch(function(){
                        Growl('error', 'Error', 'Error to save the new task');
                    });
            }
        };

        $scope.isValid = function(elementName) {
            return !jQuery("#"+elementName).hasClass("ng-invalid");
        };

        // scope watcher
        $scope.$watch('option.authIndex', function(newValue, oldValue){
            if (newValue !== oldValue) {
                if ($scope.option.processIndex === 0) {
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
            if (newValue !== oldValue) {
                $scope.canManage = true;

                var inputs = [];
                var initializationFlags = {};

                var prepareFields = function(input) {

                    //initialize style list
                    if ("valueClass:org.constellation.util.StyleReference" === input.binding) {
                        //initialize only once
                        if (!initializationFlags[input.binding]) {
                            $scope.listAvailableStyles();
                        }
                        initializationFlags[input.binding] = true;
                        input.restriction.enumeration = $scope.styles;
                    }
                };

                var extractEnumeration = function(enumList) {
                    var enumerationList = [];
                    if (enumList && Array.isArray(enumList)) {
                        enumList.forEach(function (val) {
                            enumerationList.push(val.value);
                        });
                    }
                    return enumerationList;
                };

                var parseParameterDescriptor = function(elem) {
                    var inputElement = {};
                    inputElement.name = elem.name;
                    inputElement.minOccurs = elem.minOccurs || 1;
                    inputElement.maxOccurs = elem.maxOccurs || 1;
                    inputElement.mandatory = inputElement.minOccurs > 0;

                    //Simple parameter
                    var simple = elem.simpleType;
                    if (simple) {
                        inputElement.type = "simple";
                        inputElement.default = elem.default;
                        inputElement.binding = simple.annotation.appinfo;
                        inputElement.documentation = simple.annotation.documentation;
                        inputElement.save = null;

                        //check if parameter is handled
                        if (inputElement.mandatory && jQuery.inArray(inputElement.binding, $scope.manageField) === -1) {
                            $scope.canManage = false;
                        }

                        if (simple.restriction) {
                            var restriction = simple.restriction;
                            inputElement.base = restriction.base;

                            //extract valid value range
                            inputElement.restriction = {};
                            var minValue = restriction.minInclusive !== undefined ? restriction.minInclusive.value : null;
                            var maxValue = restriction.maxInclusive !== undefined ? restriction.maxInclusive.value : null;
                            if (minValue !== null && maxValue !== null) {
                                inputElement.restriction.range = [minValue, maxValue];
                            }

                            //extract valid values
                            inputElement.restriction.enumeration = extractEnumeration(restriction.enumeration);
                        }

                        prepareFields(inputElement);

                    } else {
                        //Group parameters
                        inputElement.type = "group";
                        inputElement.inputs = [];
                        inputElement.save = [];

                        var complex = elem.complexType;
                        parseElements(complex.sequence.element, inputElement.inputs);

                        if (complex.sequence.annotation) {
                            inputElement.documentation = complex.sequence.annotation.documentation;
                        }
                    }
                    return inputElement;
                };

                var parseElements = function(elements, inputsDesc) {

                    if (elements) {
                        if (Array.isArray(elements)) {
                            elements.forEach(function (elem) {
                                inputsDesc.push(parseParameterDescriptor(elem));
                            });
                        } else {
                            inputsDesc.push(parseParameterDescriptor(elements));
                        }
                    }
                };

                parseElements(newValue.schema.element.complexType.sequence.element, inputs);
                $scope.inputs = inputs;
                restoreInputs();
            }
        }, true);
    });