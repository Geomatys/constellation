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

angular.module('cstl-process-edit', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal', 'processParamEditorEngine'])

    .config(function(processParamEditorProvider) {
        processParamEditorProvider
            .put('java.lang.Boolean', {
                templateUrl: 'views/tasks/editor/boolean.html'
            })
            .put('java.lang.Double', {
                templateUrl: 'views/tasks/editor/number.html'
            })
            .put('java.lang.Integer', {
                templateUrl: 'views/tasks/editor/number.html'
            })
            .put('java.lang.Long', {
                templateUrl: 'views/tasks/editor/number.html'
            })
            .put('java.lang.String', {
                templateUrl: 'views/tasks/editor/string.html'
            })
            .put('java.net.URL', {
                templateUrl: 'views/tasks/editor/url.html'
            })
            .put('java.io.File', {
                templateUrl: 'views/tasks/editor/file.html'
            })
            .put('org.constellation.process.StyleProcessReference', {
                templateUrl: 'views/tasks/editor/style.html',
                controller:'StyleEditorController',
                controllerAs: 'ec',
                resolve : {
                    'styles': ['StyleService', function(StyleService) {
                        return StyleService.getStyles().$promise;
                    }]
                }
            })
            .put('org.constellation.process.UserProcessReference', {
                templateUrl: 'views/tasks/editor/user.html',
                controller:'UserEditorController',
                controllerAs: 'ec',
                resolve : {
                    'users': ['UserService', function(UserService) {
                        return UserService.getUsers().$promise;
                    }]
                }
            })
            .put('org.constellation.process.ServiceProcessReference', {
                templateUrl: 'views/tasks/editor/service.html',
                controller:'ServiceEditorController',
                controllerAs: 'ec',
                resolve : {
                    'services': ['OGCWSService', function(OGCWSService) {
                        return OGCWSService.getAllServices().$promise;
                    }]
                }
            })
            .put('org.constellation.process.CRSProcessReference', {
                templateUrl: 'views/tasks/editor/crs.html',
                controller:'CRSEditorController',
                controllerAs: 'ec',
                resolve : {
                    'epsgCodes': ['EPSGService', function(EPSGService) {
                        return EPSGService.getEPSGCodes().$promise;
                    }]
                }
            })
            .put('org.constellation.process.DatasetProcessReference', {
                templateUrl: 'views/tasks/editor/dataset.html',
                controller:'DatasetEditorController',
                controllerAs: 'ec',
                resolve : {
                    'datasets': ['DataService', function(DataService) {
                        return DataService.getAllDatasets().$promise;
                    }]
                }
            });
    })

    .controller('StyleEditorController', function(parameter, valueIndex, styles, $filter) {

        var self = this;

        //full list
        self.styles = styles;

        //apply filter
        if (parameter.ext && parameter.ext.filter) {
            self.styles = $filter('filter')(self.styles, parameter.ext.filter);
        }

        // add undefined if parameter optional
        self.styles = (parameter.mandatory ? [] : [undefined]).concat(self.styles);

        //initialize parameter saved value
        if (parameter.save[valueIndex] === undefined) {
            parameter.save[valueIndex] = parameter.mandatory ? styles[0] : undefined;
        }
    })
    
    .controller('UserEditorController', function(parameter, valueIndex, users, $filter) {

        var self = this;

        //full list
        self.users = users;

        //apply filter
        if (parameter.ext && parameter.ext.filter) {
            self.users = $filter('filter')(self.users, parameter.ext.filter);
        }

        // add undefined if parameter optional
        self.users = (parameter.mandatory ? [] : [undefined]).concat(self.users);

        //initialize parameter saved value
        if (parameter.save[valueIndex] === undefined) {
            parameter.save[valueIndex] = parameter.mandatory ? users[0] : undefined;
        }
    })

    .controller('ServiceEditorController', function(parameter, valueIndex, services, $filter) {

        var self = this;
        //full list
        self.services = services;

        //apply filter
        if (parameter.ext && parameter.ext.filter) {
            self.services = $filter('filter')(self.services, parameter.ext.filter);
        }

        // add undefined if parameter optional
        self.services = (parameter.mandatory ? [] : [undefined]).concat(self.services);

        //initialize parameter saved value
        if (parameter.save[valueIndex] === undefined) {
            parameter.save[valueIndex] = parameter.mandatory ? services[0] : undefined;
        }
    })

    .controller('CRSEditorController', function(parameter, valueIndex, epsgCodes) {

        var self = this;

        self.epsgCodes = (parameter.mandatory ? [] : [undefined]).concat(epsgCodes);

        if (parameter.save[valueIndex] === undefined) {
            parameter.save[valueIndex] = parameter.mandatory ? epsgCodes[0] : undefined;
        }
    })

    .controller('DatasetEditorController', function(parameter, valueIndex, datasets) {

        var self = this;

        self.datasets = (parameter.mandatory ? [] : [undefined]).concat(datasets);

        if (parameter.save[valueIndex] === undefined) {
            parameter.save[valueIndex] = parameter.mandatory ? datasets[0] : undefined;
        }
    })

    .controller('ModalAddTaskController', function($scope, $modalInstance, Growl, TaskService, processes, task,
                                                   StyleService, OGCWSService, EPSGService, DataService, processParamEditor){

        //init services
        StyleService.refresh();
        OGCWSService.refresh();
        DataService.refresh();
        //EPSGService.refresh(); //no need to refresh crs list.

        // Private function
        function parseProcessDefaultName(processName) {
            var authBegin = processName.indexOf('{')+1;
            var authEnd = processName.indexOf('}', authBegin);

            return [processName.substr(authBegin, authEnd-authBegin), processName.substr(authEnd+1)];
        }

        function createProcesses(processesList) {

            var tree = {};
            for (var p in processesList) {
                if(processesList.hasOwnProperty(p)){
                    var process = parseProcessDefaultName(processesList[p]);
                    tree[process[0]] = tree[process[0]] || [];
                    var codeInd = tree[process[0]].push(process[1])-1;
                }
            }

            var procTree = [];
            for (var auth in tree) {
                if(tree.hasOwnProperty(auth)){
                    var indAuth = procTree.push({
                            'auth' : auth,
                            'processes' : tree[auth]
                        })-1;
                }
            }
            return procTree;
        }

        function getDescribeProcess() {
            var auth = null;
            var code = null;

            if ($scope.task.processAuthority && $scope.task.processCode) {
                //edit mode
                auth = $scope.task.processAuthority;
                code = $scope.task.processCode;
            } else {
                //add mode
                var authority = $scope.processes[$scope.option.authIndex];
                auth = authority.auth;
                code = authority.processes[$scope.option.processIndex];
            }

            TaskService.describeProcess({'authority': auth, 'code': code}).$promise
                .then(function (data) { // On success
                    $scope.describeProcess = data;
                }).catch(function (data) { // On error
                    Growl('error', 'Error', 'Unable to get the process description');
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

                //test emptyness
                if (parameter.mandatory && (parameter.save === null || parameter.save.length === 0)) {
                    Growl('error', 'Error', 'Parameter '+parameter.name+' is mandatory');
                    return false;
                }
                var length = parameter.save.length;

                for (var i = 0; i < length; i++) {
                    //test cast
                    switch (parameter.binding) {
                        case "java.lang.Integer" : //fall trough
                        case "java.lang.Long" : //fall trough
                        case "java.lang.Double" :
                            if (!angular.isNumber(parameter.save[i])) {
                                Growl('error', 'Error', 'Parameter ' + parameter.name + ' is not a Number');
                                return false;
                            }
                            break;
                    }

                    //test restrictions
                    if (parameter.restriction) {
                        var enumeration = parameter.restriction.enumeration;
                        if (enumeration && enumeration.length > 0) {
                            //only test primitive enumeration
                            if (!angular.isObject(enumeration[0])) {
                                if (enumeration.indexOf(parameter.save[i]) === -1) {
                                    Growl('error', 'Error', 'Value of parameter ' + parameter.name + ' not valid.');
                                    return false;
                                }
                            }
                        }

                        var range = parameter.restriction.range;
                        if (range) {
                            if (parameter.save[i] < range[0] || parameter.save[i] > range[1]) {
                                Growl('error', 'Error', 'Value of parameter ' + parameter.name + ' not valid. ' +
                                'Should be within range [' + range[0] + ',' + range[1] + ']');
                                return false;
                            }
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
            copy.description = param.description;
            copy.type = param.type;
            copy.isArray = param.isArray;

            if (param.type === 'simple') {
                copy.default = param.default;
                copy.binding = param.binding;
                copy.unit = param.unit;
                copy.restriction = param.restriction;
                copy.ext = param.ext;
                copy.save = [];
                for (var i = 0; i < copy.minOccurs; i++) {
                    copy.save.push(copy.default);
                }
            } else {
                copy.inputs = [];
                var paramInputs = param.inputs;
                //only duplicate minOccurs number of group occurrences.
                for (var j = 0; j < copy.minOccurs; j++) {
                    var grpInputs = [];
                    var params = paramInputs[j];
                    var nbParams = params.length;
                    for (var k = 0; k <nbParams; k++) {
                        grpInputs.push(copyParam(params[k]));
                    }
                    copy.inputs.push(grpInputs);
                }
            }
            return copy;
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
            if ("java.lang.Integer" === binding || "java.lang.Long" === binding) {
                return parseInt(value);
            }

            if ("java.lang.Double" === binding) {
                return parseFloat(value);
            }

            if ("java.lang.Boolean" === binding) {
                return Boolean(value);
            }
            return value;
        }

        // Scope variables
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

            //if (!$scope.isValid("formModalAddTask")) {
            //    Growl('error', 'Error', 'Form is invalid');
            //    return false;
            //}

            if (!$scope.task.processAuthority && !$scope.task.processCode) {
                //add mode
                $scope.task.processAuthority = $scope.processes[$scope.option.authIndex].auth;
                $scope.task.processCode = $scope.processes[$scope.option.authIndex].processes[$scope.option.processIndex];
            }
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

                var isArray = function(string) {
                    return string.indexOf('[]', string.length - 2) !== -1;
                };

                var parseParameterDescriptor = function(elem, idPrefix) {
                    var parameter = {};
                    parameter.name = elem.name;
                    parameter.id = idPrefix != null ? idPrefix+'_'+elem.name : elem.name;
                    parameter.minOccurs = elem.minOccurs || 1;
                    parameter.maxOccurs = elem.maxOccurs || 1;
                    parameter.mandatory = parameter.minOccurs > 0;
                    parameter.description = elem.description;

                    //Simple parameter
                    var javaClass = elem.class;
                    var simple = javaClass !== undefined;
                    if (simple) {
                        parameter.type = "simple";
                        parameter.isArray = false;
                        parameter.binding = javaClass;
                        if (isArray(javaClass)) {
                            parameter.isArray = true;
                            parameter.binding = javaClass.substring(0, javaClass.length-2);
                        }
                        parameter.default = convertValue(elem.defaultValue, parameter.binding);
                        parameter.unit = simple.unit;

                        //default values
                        parameter.save = [];
                        for (var j = 0; j < parameter.minOccurs; j++) {
                            parameter.save.push(parameter.default);
                        }

                        //check if parameter is handled
                        if (parameter.mandatory && !processParamEditor.hasEditor(parameter.binding)) {
                            $scope.canManage = false;
                        }

                        if (elem.restriction) {
                            var restriction = elem.restriction;
                            //inputElement.base = restriction.base;

                            //extract valid value range
                            parameter.restriction = {};
                            var minValue = restriction.minValue;
                            var maxValue = restriction.maxValue;
                            if (minValue !== null && maxValue !== null) {
                                parameter.restriction.range = [minValue, maxValue];
                            }

                            //extract valid values
                            parameter.restriction.enumeration = extractEnumeration(restriction.validValues, parameter.binding);
                        } else {
                            parameter.restriction = {};
                            parameter.restriction.enumeration = [];
                        }

                        if (elem.ext) {
                            parameter.ext = elem.ext;
                        }

                    } else {
                        //Group parameters
                        parameter.type = "group";
                        parameter.inputs = [[]];
                        parseElements(elem.descriptors, parameter.inputs[0], parameter.id);
                    }
                    return parameter;
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

                parseElements(newValue.descriptors, inputs, null);
                $scope.parameters = inputs;
                restoreInputs();
            }
        }, true);
    });