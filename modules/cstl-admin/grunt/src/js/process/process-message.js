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

angular.module('cstl-process-message', ['ui.bootstrap.modal'])

    .controller('ModalStatusMessageController', function($scope, $modalInstance, taskStatus) {

        function isNullOrEmpty(candidate) {
            if (candidate !== null) {
                if (angular.isString(candidate)) {
                    return candidate.length === 0;
                }
                return false;
            }
            return true;
        }

        function extractMessage(message) {
            if (angular.isString(message)) {
                return message;
            }
            if (angular.isArray(message)) {
                return message[0];
            }
        }

        function cleanOutput(output) {
            if (!isNullOrEmpty(output) && angular.isArray(output) && output.length === 1) {
                return output[0];
            }
            return output;
        }

        function prepareOutputs(rawOutputs) {
            var outputs = [];

            var i = 0;
            for (var rawOutput in rawOutputs) {
                if (rawOutputs.hasOwnProperty(rawOutput)) {
                    var output = {};
                    output.identifier = rawOutput;
                    output.value = cleanOutput(rawOutputs[rawOutput]);
                    outputs[i] = output;
                    i++;
                }
            }
            return outputs;
        }

        function init() {
            // convert json to object
            if (!isNullOrEmpty($scope.taskStatus.output)) {
                var rawOutputs = angular.fromJson($scope.taskStatus.output);
                $scope.outputs = prepareOutputs(rawOutputs);
            }

            $scope.message = extractMessage($scope.taskStatus.message);
            $scope.showMessage = !isNullOrEmpty($scope.message);

        }

        $scope.messageType = taskStatus.status;
        $scope.message = null;
        $scope.showMessage = true;

        $scope.outputs = [];

        $scope.taskStatus = taskStatus;
        $scope.close = $scope.cancel = $modalInstance.close;

        init();

        $scope.statusLabelKey = function(status) {
            switch(status.status) {
                case 'SUCCEED' : return 'task.status.succeed';
                case 'WARNING' : return 'task.status.warning';
                case 'FAILED' : return 'task.status.failed';
                case 'CANCELLED' : return 'task.status.cancelled';
                default : return status.status;
            }
        };
    });

