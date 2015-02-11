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

        /**
         * @param candidate
         * @return {boolean} true if candidate is NOT null and NOT empty, false otherwise.
         */
        function checkNullEmpty(candidate) {
            if (candidate !== null) {
                if (angular.isString(candidate)) {
                    return candidate.length > 0;
                }
                return true;
            }
            return false;
        }

        function init() {
            if (checkNullEmpty($scope.taskStatus.output)) {
                $scope.taskStatus.output = angular.fromJson($scope.taskStatus.output);
            }

            if (angular.isArray($scope.taskStatus.output) && $scope.taskStatus.output.length > 0) {
                $scope.taskStatus.output = $scope.taskStatus.output[0];
                $scope.showOutputs = true;
            } else {
                $scope.showOutputs = checkNullEmpty($scope.taskStatus.output);
            }

            $scope.showMessage = checkNullEmpty($scope.taskStatus.message);
        }

        $scope.taskStatus = taskStatus;
        $scope.close = $scope.cancel = $modalInstance.close;

        init();
    });

