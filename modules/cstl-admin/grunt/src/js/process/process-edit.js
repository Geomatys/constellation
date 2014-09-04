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

    .controller('ModalAddTaskController', function($scope, $modalInstance, Growl, textService, TaskService, processes, task, style){

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
            textService.getProcessDescriptor(authority.auth, authority.processes[$scope.option.processIndex])
                .success(function(data){ // On success
                    $scope.describeProcess = data;
                }).error(function(data){ // On error
                    Growl('error', 'Error', 'Unable to get the describe process');
                });
        }

        function setSaved(dom, name, iter){
            dom.find(name).each(function(ind, _el) {
                switch ($scope.inputs[iter].annotation.info) {
                    case "valueClass:java.lang.Double":
                        $scope.inputs[iter].save[ind] = parseFloat(jQuery(_el)[0].textContent);
                        break;
                    case "valueClass:java.lang.Integer":
                        $scope.inputs[iter].save[ind] = parseInt(jQuery(_el)[0].textContent);
                        break;
                    case "valueClass:java.lang.Boolean":
                        $scope.inputs[iter].save[ind] = jQuery(_el)[0].textContent === "true";
                        break;
                    default:
                        $scope.inputs[iter].save[ind] = jQuery(_el)[0].textContent;
                }
            });

        }

        function restoreInputs(){
            if ($scope.task.inputs) {
                var dom = jQuery(jQuery.parseXML($scope.task.inputs));

                for (var iter in $scope.inputs){
                    if($scope.inputs.hasOwnProperty(iter)){
                        var name = $scope.inputs[iter].name;
                        setSaved(dom, name, iter);
                    }
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
            $scope.task.inputs = '<input xmlns="http://www.geotoolkit.org/parameter">';

            for (var i in $scope.inputs){
                if($scope.inputs.hasOwnProperty(i)){
                    var element = $scope.inputs[i];

                    if (!$scope.isValid(element.name)) {
                        Growl('error', 'Error', 'Form is invalid');
                        return false;
                    }

                    switch(element.annotation.info) {

                        case "valueClass:java.lang.Boolean" :
                            if (element.save && element.save.length > 0) {
                                for (var s in element.save) {
                                    if(element.save.hasOwnProperty(s)){
                                        $scope.task.inputs += '<' + element.name + '>' + element.save[s] + '</' + element.name + '>';
                                    }
                                }
                            } else {
                                $scope.task.inputs += '<' + element.name + '>' + (element.default||false) + '</' + element.name + '>';
                            }
                            break;
                        default:
                            if (element.save && element.save.length > 0) {
                                for (var save in element.save) {
                                    if (element.save.hasOwnProperty(save)) {
                                        $scope.task.inputs += '<' + element.name + '>' + element.save[save] + '</' + element.name + '>';
                                    }
                                }
                            } else if (element.default) {
                                $scope.task.inputs += '<' + element.name + '>' + element.default + '</' + element.name + '>';
                            }
                            break;
                    }
                }
            }
            $scope.task.inputs += '</input>';

            if ($scope.task.id !== 0){
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
                var save = {};
                var dom = jQuery(jQuery.parseXML(newValue));

                var getAnnotationFor = function(_el) {
                    var element = jQuery(_el);
                    var annotation = {};
                    annotation.info = element.find("appinfo").get(0).textContent;
                    annotation.documentation = element.find("documentation").get(0).textContent;

                    if ("valueClass:org.constellation.util.StyleReference" === annotation.info) {
                        $scope.listAvailableStyles();
                    }

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
                    };
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
                    if (inputs[index].minOccurs === 0) {
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
    });