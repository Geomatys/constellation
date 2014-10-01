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

angular.module('cstl-sensor-metadata', ['cstl-restapi', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .controller('EditSensorMLController', function ($scope,$modalInstance, $routeParams,sensor, $location, $translate,
                                                    Growl,id,type,template) {

        $scope.sensorId = id;
        $scope.sensorType = type;
        $scope.template = template;

        $scope.typeLabelKey = "metadata.edition.sensor."+$scope.sensorType.toLowerCase();

        $scope.theme = 'sensor';

        $scope.uriRegExp=/^([a-z][a-z0-9+.-]*):(?:\/\/((?:(?=((?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*))(\3)@)?(?=(\[[0-9A-F:.]{2,}\]|(?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*))\5(?::(?=(\d*))\6)?)(\/(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*))\8)?|(\/?(?!\/)(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*))\10)?)(?:\?(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/?]|%[0-9A-F]{2})*))\11)?(?:#(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/?]|%[0-9A-F]{2})*))\12)?$/i;

        /**
         * Get metadata values
         * @type {Array}
         */
        $scope.metadataValues = [];
        $scope.loadMetadataValues = function(){
            sensor.getJsonMetadata({'sensorId':$scope.sensorId,'type':$scope.sensorType,'prune':false},
                function(response){//success
                    if (response && response.root) {
                        $scope.metadataValues.push({"root":response.root});
                    }
                },
                function(response){//error
                    Growl('error','Error','The server returned an error!');
                }
            );
        };
        $scope.loadMetadataValues();

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.isValidField = function(input){
            if(input){
                return (input.$valid || input.$pristine);
            }
            return true;
        };

        $scope.isValidRequired = function(input){
            if(input){
                return ! input.$error.required;
            }
            return true;
        };

        $scope.isValidEmail = function(input){
            if(input){
                return ! input.$error.email;
            }
            return true;
        };

        $scope.isValidUrl = function(input){
            if(input){
                return ! input.$error.url;
            }
            return true;
        };

        $scope.isValidUri = function(input){
            if(input){
                return ! input.$error.pattern;
            }
            return true;
        };

        $scope.isValidNumber = function(input){
            if(input){
                return ! input.$error.number;
            }
            return true;
        };

        /**
         * Proceed to check validation form
         * @param form
         */
        $scope.checkValidation = function(form) {
            if(form.$error.required){
                for(var i=0;i<form.$error.required.length;i++){
                    form.$error.required[i].$pristine = false;
                }
            }
            $scope.showValidationPopup(form);
        };

        /**
         * Display validation modal popup that show the form state
         * and when popup is closed then animate scroll to next invalid input.
         * @param form
         */
        $scope.showValidationPopup = function(form) {
            var validationPopup = $('#validationPopup');
            validationPopup.modal("show");
            validationPopup.on('shown.bs.modal', function (e) {
                validationPopup.blur(); //fix bug for safari
            });
        };

        /**
         * Add new occurrence of field. the field must have multiplicity gt 1
         * @param blockObj
         * @param fieldObj
         */
        $scope.addFieldOccurrence = function(blockObj,fieldObj) {
            var newField = {"field":{}};
            // Shallow copy
            newField.field = jQuery.extend({}, fieldObj.field);
            newField.field.value=fieldObj.field.defaultValue;
            if(newField.field.path.indexOf('+')===-1){
                newField.field.path = newField.field.path+'+';
            }
            var indexOfField = blockObj.block.children.indexOf(fieldObj);
            blockObj.block.children.splice(indexOfField+1,0,newField);
        };

        /**
         * Remove occurrence of given field for given block.
         * @param blockObj
         * @param fieldObj
         */
        $scope.removeFieldOccurrence = function(blockObj,fieldObj) {
            var indexToRemove = blockObj.block.children.indexOf(fieldObj);
            blockObj.block.children.splice(indexToRemove,1);
        };

        /**
         * Returns true if the given field is an occurrence that can be removed from the form.
         * @param fieldObj
         * @returns {boolean}
         */
        $scope.isFieldOccurrence = function(fieldObj){
            var strPath = fieldObj.field.path;
            if(endsWith(strPath,'+')){
                return true;
            }
            var number = getNumeroForPath(strPath);
            if(number>1){
                return true;
            }
            return false;
        };

        /**
         * Utility function that returns if the string ends with given suffix.
         * @param str given string to check.
         * @param suffix given suffix.
         * @returns {boolean} returns true if str ends with suffix.
         */
        function endsWith(str,suffix) {
            return str.indexOf(suffix, str.length - suffix.length) !== -1;
        }

        /**
         * Add new occurrence of given block.
         * @param superBlockObj the parent of given block.
         * @param blockObj the given block to create a new occurrence based on it.
         */
        $scope.addBlockOccurrence = function(superBlockObj, blockObj){
            var newBlock = {"block":{}};
            // Deep copy
            newBlock.block = jQuery.extend(true,{}, blockObj.block);
            var blockPath = newBlock.block.path;
            var commonStr = blockPath.substring(0,blockPath.lastIndexOf('['));
            var max = getMaxNumeroForBlock(superBlockObj,commonStr);
            for(var i=0;i<newBlock.block.children.length;i++){
                var fieldObj = newBlock.block.children[i];
                fieldObj.field.value=fieldObj.field.defaultValue;
                var fieldPath = fieldObj.field.path;
                fieldPath = fieldPath.replace(commonStr,'');
                if(fieldPath.indexOf('[')===0){
                    fieldPath = '['+max+fieldPath.substring(fieldPath.indexOf(']'));
                    fieldPath = commonStr+fieldPath;
                    fieldObj.field.path = fieldPath;
                }
            }
            newBlock.block.path=commonStr+'['+max+']';
            var indexOfBlock = superBlockObj.superblock.children.indexOf(blockObj);
            superBlockObj.superblock.children.splice(indexOfBlock+1,0,newBlock);
        };

        /**
         * Proceed to remove the given block occurrence from its parent superblock.
         * @param superBlockObj the given block's parent.
         * @param blockObj the given block to remove.
         */
        $scope.removeBlockOccurrence = function(superBlockObj,blockObj) {
            var indexToRemove = superBlockObj.superblock.children.indexOf(blockObj);
            superBlockObj.superblock.children.splice(indexToRemove,1);
        };

        /**
         * Returns true if given block is an occurrence of another block.
         * @param blockObj given block json to check.
         * @returns {boolean} return true if block's path is not null and the occurrence number is >1.
         */
        $scope.isBlockOccurrence = function(blockObj){
            var strPath = blockObj.block.path;
            if(!strPath){
                return false;
            }
            if(endsWith(strPath,'+')){
                return true;
            }
            var number = getNumeroForPath(strPath);
            if(number>1){
                return true;
            }
            return false;
        };

        /**
         * Returns the maximum number used in each block which is child of given superBlock
         * and when block's path starts with given prefix.
         * @param superBlockObj the given superblock json object.
         * @param prefix the given string that each path must matches with.
         * @returns {number} the result incremented number.
         */
        function getMaxNumeroForBlock(superBlockObj,prefix){
            var max = 1;
            for(var i=0;i<superBlockObj.superblock.children.length;i++){
                var childObj = superBlockObj.superblock.children[i];
                var childPath = childObj.block.path;
                if(childPath && childPath.indexOf(prefix)!==-1){
                    childPath = childPath.replace(prefix,'');
                    if(childPath.indexOf('[')===0){
                        var numero = childPath.substring(1,childPath.indexOf(']'));
                        max = Math.max(max,parseInt(numero));
                    }
                }
            }
            max++;
            return max;
        }

        /**
         * Return the occurrence number of given path.
         * @param path given path to read.
         * @returns {null} if path does not contains occurrence number.
         */
        function getNumeroForPath(path){
            if(path && path.indexOf('[')!==-1){
                var number = path.substring(path.lastIndexOf('[')+1,path.length-1);
                return number;
            }
            return null;
        }

        /**
         * attach events click for editor blocks elements
         * to allow collapsible/expandable panels.
         */
        function initCollapseEvents () {
            if(window.collapseEditionEventsRegistered){return;} //to fix a bug with angular
            $(document).on('click','#editorMetadata .small-block .heading-block',function(){
                var blockRow = $(this).parents('.block-row');
                var parent = $(this).parent('.small-block');
                parent.toggleClass('closed');
                blockRow.find('.collapse-block').toggleClass('hide');
                var icon=parent.find('.data-icon');
                if(icon.hasClass('fa-angle-up')){
                    icon.removeClass('fa-angle-up');
                    icon.addClass('fa-angle-down');
                }else {
                    icon.removeClass('fa-angle-down');
                    icon.addClass('fa-angle-up');
                }
            });
            $(document).on('click','#editorMetadata .collapse-row-heading',function(){
                $(this).parent().toggleClass('open');
                $(this).next().toggleClass('hide');
                var icon=$(this).find('.data-icon');
                if(icon.hasClass('fa-angle-up')){
                    icon.removeClass('fa-angle-up');
                    icon.addClass('fa-angle-down');
                }else {
                    icon.removeClass('fa-angle-down');
                    icon.addClass('fa-angle-up');
                }
            });
            $(document).on('hidden.bs.modal','#validationPopup', function(e){
                if($('#metadataform').hasClass('ng-invalid')){
                    var selClass = '.highlight-invalid';
                    var firstInvalid = $(selClass).get(0);
                    if(firstInvalid){
                        var modalBody = $(selClass).parents('.modal-body');
                        if(modalBody && modalBody.get(0)){
                            modalBody.animate(
                                {scrollTop: $(firstInvalid).offset().top-200}, 1000);
                        }else {
                            $('html, body').animate(
                                {scrollTop: $(firstInvalid).offset().top-200}, 1000);
                        }
                        $(firstInvalid).focus();
                    }
                }
            });
            window.collapseEditionEventsRegistered = true;
        }

        /**
         * Scrolling to top with animation effect.
         */
        $scope.scrollToTop = function(){
            var elem = $('.scrolltotop');
            var modalBody = elem.parents('.modal-body');
            if(modalBody && modalBody.get(0)){
                modalBody.animate({scrollTop: '0px'}, 1000);
            }else {
                jQuery('html, body').animate({scrollTop: '0px'}, 1000);
            }
        };

        /**
         * Returns the title value of metadata.
         * Note that this way is not generic,
         * we need to find another way to retrieve the title for
         * more generic metadata to support sensorML and ISO and others.
         * //@FIXME do it more generic
         * @returns {string} the title value located in json model.
         */
        $scope.getMetadataTitle = function() {
            if($scope.metadataValues && $scope.metadataValues.length>0){
                //@FIXME get field with jsonPath
                return $scope.metadataValues[0].root.children[0].superblock.children[0].block.children[2].field.value;
            }
            return null;
        };

        /**
         * Init editor events
         */
        $scope.initMetadataEditorEvents = function() {
            initCollapseEvents();
        };

        /**
         * Save for metadata in modal editor mode.
         */
        $scope.save2 = function() {
            if($scope.metadataValues && $scope.metadataValues.length>0){
                //console.debug($scope.metadataValues[0]);
                //console.debug(JSON.stringify($scope.metadataValues[0],null,1));
                sensor.saveSensorMLMetadata({'sensorId':$scope.sensorId,
                                             'type':$scope.template},
                                             $scope.metadataValues[0],
                    function(response) {//success
                        $scope.close();
                        Growl('success','Success','SensorML saved with success!');
                    },
                    function(response) {//error
                        $scope.close();
                        Growl('error','Error','Failed to save sensorML because the server returned an error!');
                    }
                );
            }
        };

        /**
         * Returns the current lang used.
         * For datepicker as locale.
         */
        $scope.getCurrentLang = function() {
            var lang = $translate.use();
            if(!lang){
                lang = 'en';
            }
            return lang;
        };

    });