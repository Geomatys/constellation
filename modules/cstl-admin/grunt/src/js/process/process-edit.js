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
                fillParametersValues($scope.task.inputs, $scope.parameters);
            }
        }

        /**
         * Fill form parameters save attributes from task inputs
         * @param inputs
         * @param parameters
         */
        function fillParametersValues(inputs, parameters) {
            for (var param in inputs) {
                if(inputs.hasOwnProperty(param)) {
                    var value = inputs[param];
                    var scopeParam = getParameterByName(parameters, param);

                    if (scopeParam.type === 'simple') {
                        scopeParam.save = value;
                    } else {
                        if (angular.isArray(value)) {
                            var nbOccurs = value.length;

                            //create occurs
                            for (var i = 0; i < nbOccurs-1; i++) {
                                $scope.addGroupOccurrence(scopeParam);
                            }

                            //fill occurences parameters
                            for (i = 0; i < nbOccurs; i++) {
                                fillParametersValues(value[i], scopeParam.inputs[i]);
                            }
                        }
                    }
                }
            }
        }

        function getParameterByName(inputArray, param) {
            var filter = inputArray.filter(function (elem) {
                return elem.name === param;
            });
            return filter[0];
        }

        /**
         * Rebuild task input from form parameter save attributes
         * @param inputs
         * @param parameters
         */
        function fillInputsValues(inputs, parameters) {
            var valid = true;
            var nbParam = parameters.length;

            for (var i = 0; i < nbParam; i++) {
                var param = parameters[i];
                if (param.type === 'simple') {
                    valid = isValid(param);
                    inputs[param.name] = param.save;
                } else {
                    inputs[param.name] = [];
                    var nbOccurs = param.inputs.length;

                    for (var j = 0; j < nbOccurs; j++) {
                        var supInputs = {};
                        fillInputsValues(supInputs, param.inputs[j]);
                        inputs[param.name].push(supInputs);
                    }
                }
            }
            return valid;
        }

        /**
         * Check if a simple parameter value is valid
         * @param parameter
         */
        function isValid(parameter) {
            if (parameter.type === 'simple') {

                //test cast
                switch(parameter.binding) {
                    case "valueClass:java.lang.Integer" : //fall trough
                    case "valueClass:java.lang.Double" :
                        if (!angular.isNumber(parameter.save)) {
                            Growl('error', 'Error', 'Parameter '+parameter.name+' is not a Number');
                            return false;
                        }
                        break;
                }

                //test restrictions
                if (parameter.restriction) {

                    var enumeration = parameter.restriction.enumeration;
                    if (enumeration && enumeration.length > 0) {
                        if (enumeration.indexOf(parameter.save) === -1) {
                            Growl('error', 'Error', 'Value of parameter '+parameter.name+' not valid.');
                            return false;
                        }
                    }

                    var range = parameter.restriction.range;
                    if (range) {
                        if (parameter.save < range[0] || parameter.save > range[1]) {
                            Growl('error', 'Error', 'Value of parameter '+parameter.name+' not valid. ' +
                                'Should be within range ['+range[0]+','+ range[1]+']');
                            return false;
                        }
                    }
                }

                return true;
            }
            return false;
        }

        /**
         * Recursively copy a parameter without save value
         * @param param
         */
        function copyParam(param) {
            var copy = {};
            copy.name = param.name;
            copy.id = param.id;
            copy.minOccurs = param.minOccurs;
            copy.maxOccurs = param.maxOccurs;
            copy.mandatory = param.mandatory;
            copy.documentation = param.documentation;
            copy.type = param.type;

            if (param.type === 'simple') {
                copy.default = param.default;
                copy.binding = param.binding;
                copy.documentation = param.documentation;
                copy.save = copy.default;
                copy.base = param.base;
                copy.restriction = param.restriction;
            } else {
                copy.inputs = [[]];
                var paramInputs = param.inputs;
                var nbGrp = paramInputs.length;
                for (var i = 0; i <nbGrp; i++) {
                    var grpInputs = [];
                    var params = paramInputs[i];
                    var nbParams = params.length;
                    for (var j = 0; j <nbParams; j++) {
                        grpInputs.push(copyParam(params[j]));
                    }
                    copy.inputs.push(grpInputs);
                }
            }
            return copy;
        }

        function listAvailableStyles() {
            style.listAll({provider: 'sld'}, function (response) {
                $scope.styles.splice(0,$scope.styles.length);
                jQuery.each(response.styles, function(i,style) {
                    var styleName = style.Name;
                    var styleRef = '${providerStyleType|sld|'+styleName+'}';
                    $scope.styles.push({name:styleName, ref:styleRef});
                });
            });
        }

        function extractEnumeration(enumList, binding) {
            var enumerationList = [];
            if (enumList && Array.isArray(enumList)) {
                enumList.forEach(function (val) {
                    enumerationList.push(convertValue(val.value, binding));
                });
            }
            return enumerationList;
        }


        function convertValue(value, binding) {
            if ("valueClass:java.lang.Integer" === binding) {
                return parseInt(value);
            }

            if ("valueClass:java.lang.Double" === binding) {
                return parseFloat(value);
            }

            if ("valueClass:java.lang.Boolean" === binding) {
                return Boolean(value);
            }
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
        $scope.parameters = [];
        $scope.task = task;
        $scope.styles = [];

        $scope.processes = createProcesses(processes.Entry);

        // scope functions
        $scope.close = $scope.cancel = $modalInstance.close;

        $scope.addGroupOccurrence = function (groupParam) {
            if (groupParam.type === 'group') {
                var firstOccur = groupParam.inputs[0];

                var newOccur = [];
                var nbParam = firstOccur.length;
                for (var i = 0; i < nbParam; i++) {
                    newOccur.push(copyParam(firstOccur[i]));
                }
                groupParam.inputs.push(newOccur);
            }
        };

        $scope.removeGroupOccurrence = function (groupParam, index) {
            if (groupParam.type === 'group' && groupParam.inputs.length > index) {
                groupParam.inputs.splice(index, 1);
            }
        };

        $scope.validate = function() {

            if (!$scope.isValid("formModalAddTask")) {
                Growl('error', 'Error', 'Form is invalid');
                return false;
            }

            $scope.task.processAuthority = $scope.processes[$scope.option.authIndex].auth;
            $scope.task.processCode = $scope.processes[$scope.option.authIndex].processes[$scope.option.processIndex];
            $scope.task.inputs = {};

            fillInputsValues($scope.task.inputs, $scope.parameters);

            //convert to JSON
            $scope.task.inputs = angular.toJson($scope.task.inputs);

            if ($scope.task.id != null){
                TaskService.updateParamsTask($scope.task).$promise
                    .then(function(response) {
                        Growl('success', 'Success', 'The task is correctly save');
                        $modalInstance.close();
                    }).catch(function(response){
                        var message = 'Error to save the task';
                        if (response.data && response.data.message) {
                            message = response.data.message;
                        }
                        Growl('error', 'Error', message);
                    });
            } else {
                TaskService.createParamsTask($scope.task).$promise
                    .then(function(response) {
                        Growl('success', 'Success', 'New task is correctly register');
                        $modalInstance.close();
                    }).catch(function(response){
                        var message = 'Error to save the new task';
                        if (response.data && response.data.message) {
                            message = response.data.message;
                        }
                        Growl('error', 'Error', message);
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
                            listAvailableStyles();
                        }
                        initializationFlags[input.binding] = true;
                        input.restriction.enumeration = $scope.styles;
                    }
                };


                var parseParameterDescriptor = function(elem, idPrefix) {
                    var inputElement = {};
                    inputElement.name = elem.name;
                    inputElement.id = idPrefix != null ? idPrefix+'_'+elem.name : elem.name;
                    inputElement.minOccurs = elem.minOccurs || 1;
                    inputElement.maxOccurs = elem.maxOccurs || 1;
                    inputElement.mandatory = inputElement.minOccurs > 0;

                    //Simple parameter
                    var simple = elem.simpleType;
                    if (simple) {
                        inputElement.type = "simple";
                        inputElement.binding = simple.annotation.appinfo;
                        inputElement.default = convertValue(elem.default, inputElement.binding);
                        inputElement.documentation = simple.annotation.documentation;
                        inputElement.save = inputElement.default;

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
                            inputElement.restriction.enumeration = extractEnumeration(restriction.enumeration, inputElement.binding);
                        }

                        prepareFields(inputElement);

                    } else {
                        //Group parameters
                        inputElement.type = "group";
                        inputElement.inputs = [[]];
                        var complex = elem.complexType;

                        parseElements(complex.sequence.element, inputElement.inputs[0], inputElement.id);

                        if (complex.sequence.annotation) {
                            inputElement.documentation = complex.sequence.annotation.documentation;
                        }
                    }
                    return inputElement;
                };

                var parseElements = function(elements, inputsDesc, idPrefix) {

                    if (elements) {
                        if (Array.isArray(elements)) {
                            var i = 0;
                            elements.forEach(function (elem) {
                                var pref = idPrefix != null ? idPrefix+'_'+i : null;
                                inputsDesc.push(parseParameterDescriptor(elem, pref));
                                i++;
                            });
                        } else {
                            inputsDesc.push(parseParameterDescriptor(elements, idPrefix));
                        }
                    }
                };

                parseElements(newValue.schema.element.complexType.sequence.element, inputs, null);
                $scope.parameters = inputs;
                restoreInputs();
            }
        }, true);
    });