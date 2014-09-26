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

angular.module('cstl-data-metadata', ['cstl-restapi', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .controller('EditMetadataController', function ($scope, $routeParams,dataListing, $location, $translate, Growl) {
        $scope.provider = $scope.provider || $routeParams.provider;
        $scope.identifier = $scope.identifier || $routeParams.identifier;
        $scope.type = $scope.type || $routeParams.type; //type is one of 'vector' or 'raster' or 'observation'.
        if($scope.type.toLowerCase() === 'coverage'){
            $scope.type = 'raster';
        }
        $scope.template = $scope.template || $routeParams.template || 'import';
        $scope.typeLabelKey = "metadata.edition.dataset."+$scope.type;
        $scope.theme = $scope.theme || 'data';

        /**
         * Get all codelists for metadata editor
         */
        $scope.codeLists = {};
        dataListing.codeLists({},{},function(response){
            $scope.codeLists = response;
        });

        /**
         * This is the predefined values for some fields.
         */
        $scope.predefinedValues = {};
        $scope.predefinedValues.inspireThemes = [
            "Addresses","Hydrography","Administrative units","Land cover",
            "Agricultural and aquaculture facilities","Land use",
            "Area management/restriction/regulation zones and reporting units",
            "Meteorological geographical features","Atmospheric conditions",
            "Mineral resources","Bio-geographical regions","Natural risk zones",
            "Buildings","Oceanographic geographical features","Cadastral parcels",
            "Orthoimagery","Coordinate reference systems","Population distribution — demography",
            "Elevation","Production and industrial facilities","Energy resources",
            "Protected sites","Environmental monitoring facilities","Sea regions",
            "Geographical grid systems","Soil","Geographical names","Species distribution",
            "Geology","Statistical units","Habitats and biotopes",
            "Transport networks","Human health and safety","Utility and governmental services"];
        $scope.predefinedValues.referenceSystemIdentifier = [
            'WGS84 - EPSG:4326',
            'Fort Desaix / UTM 20 N - EPSG:2973',
            'UTM 20 N - EPSG:32620',
            'CGS 1967 / UTM 21 N - EPSG:3312',
            'UTM 22 N - EPSG:2971',
            'RGFG95 / UTM 22 N - EPSG:2972',
            'UTM 30 N - EPSG:32630',
            'UTM 31 N - EPSG:32631',
            'UTM 32 N - EPSG:32632',
            'RGF93 / Lambert-93 - EPSG:2154',
            'Lambert I - EPSG:27571',
            'Lambert II - EPSG:27572',
            'Lambert III - EPSG:27573',
            'Lambert IV - EPSG:27574',
            'CC42 - EPSG:3942',
            'WGS84 / UTM 20 N - EPSG:4559'];
        $scope.predefinedValues.distributionFormat = ['SHP','TAB','MIF/MID','KML','GML','GeoTIFF','ECW','Autre'];
        $scope.predefinedValues.specifications=[
            'No INSPIRE Data Specification',
            'INSPIRE Data Specification on Administrative Units',
            'INSPIRE Data Specification on Cadastral Parcels',
            'INSPIRE Data Specification on Geographical Names',
            'INSPIRE Data Specification on Hydrography',
            'INSPIRE Data Specification on Protected Sites',
            'INSPIRE Data Specification on Transport Networks',
            'INSPIRE Data Specifications on Addresses',
            'INSPIRE Specification on Coordinate Reference Systems',
            'INSPIRE Specification on Geographical Grid Systems',
            'Data Specification on Agricultural and Aquaculture Facilities',
            'Data Specification on Area management / restriction / regulation zones and reporting units',
            'Data Specification on Atmospheric Conditions- Meteorological geographical features',
            'Data Specification on Bio-geographical regions',
            'Data Specification on Buildings',
            'Data Specification on Elevation',
            'Data Specification on Energy Resources',
            'Data Specification on Environmental monitoring Facilities',
            'Data Specification on Geology',
            'Data Specification on Habitats and biotopes',
            'Data Specification on Human health and safety',
            'Data Specification on Land cover',
            'Data Specification on Land use',
            'Data Specification on Mineral Resources',
            'Data Specification on Natural risk zones',
            'Data Specification on Oceanographic geographical features',
            'Data Specification on Orthoimagery',
            'Data Specification on Population distribution - demography',
            'Data Specification on Production and Industrial Facilities',
            'Data Specification on Sea regions',
            'Data Specification on Soil',
            'Data Specification on Species distribution',
            'Data Specification on Statistical units',
            'Data Specification on Utility and Government Services',
            'RÈGLEMENT (UE) N°1089/2010'];
        $scope.predefinedValues.resultPass=['nilReason:unknown','false','true'];

        /**
         * Get metadata values
         * @type {Array}
         */
        $scope.metadataValues = [];

        $scope.loadMetadataValues = $scope.loadMetadataValues || function(){
            if($scope.provider){//for data
                dataListing.getDataMetadata({}, {values: {'provider':$scope.provider,
                                                          'identifier': $scope.identifier,
                                                          'type':$scope.template,
                                                          'prune':false}},
                    function(response) {
                        if (response && response.root) {
                            $scope.metadataValues.push({"root":response.root});
                        }
                    },
                    function(response) {
                        Growl('error','Error','The server returned an error!');
                    }
                );
            }else {//for dataset
                dataListing.getDatasetMetadata({}, {values: {'identifier': $scope.identifier,
                                                             'type':$scope.template,
                                                             'prune':false}},
                    function(response) {
                        if (response && response.root) {
                            $scope.metadataValues.push({"root":response.root});
                        }
                    },
                    function(response) {
                        Growl('error','Error','The server returned an error!');
                    }
                );
            }
        };

        $scope.loadMetadataValues();

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
                return $scope.metadataValues[0].root.children[0].superblock.children[0].block.children[0].field.value;
            }
        };

        /**
         * Init editor events
         */
        $scope.initMetadataEditorEvents = function() {
            initCollapseEvents();
        };

        /**
         * Save for metadata in page editor mode.
         */
        $scope.save = function() {
            if($scope.metadataValues && $scope.metadataValues.length>0){
                //console.debug($scope.metadataValues[0]);
                //console.debug(JSON.stringify($scope.metadataValues[0],null,1));
                if($scope.provider){//for data
                    dataListing.mergeMetadata({'provider':$scope.provider,'identifier':$scope.identifier,'type':$scope.template},
                        $scope.metadataValues[0],
                        function(response) {
                            $location.path('/data'); //redirect to data dashboard page
                            Growl('success','Success','Metadata saved with success!');
                        },
                        function(response) {
                            Growl('error','Error','Failed to save metadata because the server returned an error!');
                        }
                    );
                }else {//for dataset
                    dataListing.mergeMetadataDS({'identifier':$scope.identifier,'type':$scope.template},
                        $scope.metadataValues[0],
                        function(response) {
                            $location.path('/data'); //redirect to data dashboard page
                            Growl('success','Success','Metadata saved with success!');
                        },
                        function(response) {
                            Growl('error','Error','Failed to save metadata because the server returned an error!');
                        }
                    );
                }
            }
        };
        /**
         * Save for metadata in modal editor mode.
         */
        $scope.save2 = $scope.save2 || function() {
            if($scope.metadataValues && $scope.metadataValues.length>0){
                //console.debug($scope.metadataValues[0]);
                //console.debug(JSON.stringify($scope.metadataValues[0],null,1));
                if($scope.provider){//for data
                    dataListing.mergeMetadata({'provider':$scope.provider,'identifier':$scope.identifier,'type':$scope.template},
                        $scope.metadataValues[0],
                        function(response) {
                            $scope.close();
                            Growl('success','Success','Metadata saved with success!');
                        },
                        function(response) {
                            $scope.close();
                            Growl('error','Error','Failed to save metadata because the server returned an error!');
                        }
                    );
                }else {//for dataset
                    dataListing.mergeMetadataDS({'identifier':$scope.identifier,'type':$scope.template},
                        $scope.metadataValues[0],
                        function(response) {
                            $scope.close();
                            Growl('success','Success','Metadata saved with success!');
                        },
                        function(response) {
                            $scope.close();
                            Growl('error','Error','Failed to save metadata because the server returned an error!');
                        }
                    );
                }
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

        /**
         * Function to set values for ISO INSPIRE selectOneMenu
         * the INSPIRE predefinedValues are defined in sql.
         * the corresponding code is the topicCategory codes
         * @param value
         * @param parentBlock
         */
        $scope.updateIsoInspireSelectOneMenu = function(value,parentBlock) {
            if(value) {
                var INSPIRE_ISO_MAP = {};
                /* jshint ignore:start */
                INSPIRE_ISO_MAP['Elevation'] = 'MD_TopicCategoryCode.elevation';
                INSPIRE_ISO_MAP['Geology'] = 'MD_TopicCategoryCode.geoscientificInformation';
                INSPIRE_ISO_MAP['Habitats and biotopes'] = 'MD_TopicCategoryCode.biota';
                INSPIRE_ISO_MAP['Environmental monitoring facilities'] = 'MD_TopicCategoryCode.structure';
                INSPIRE_ISO_MAP['Land cover'] = 'MD_TopicCategoryCode.imageryBaseMapsEarthCover';
                INSPIRE_ISO_MAP['Species distribution'] = 'MD_TopicCategoryCode.biota';
                INSPIRE_ISO_MAP['Land use'] = 'MD_TopicCategoryCode.planningCadastre';
                INSPIRE_ISO_MAP['Area management/restriction/regulation zones and reporting units'] = 'MD_TopicCategoryCode.planningCadastre';
                INSPIRE_ISO_MAP['Natural risk zones'] = 'MD_TopicCategoryCode.planningCadastre';
                INSPIRE_ISO_MAP['Buildings'] = 'MD_TopicCategoryCode.structure';
                INSPIRE_ISO_MAP['Oceanographic geographical features'] = 'MD_TopicCategoryCode.oceans';
                INSPIRE_ISO_MAP['Bio-geographical regions'] = 'MD_TopicCategoryCode.biota';
                INSPIRE_ISO_MAP['Sea regions'] = 'MD_TopicCategoryCode.oceans';
                INSPIRE_ISO_MAP['Statistical units'] = 'MD_TopicCategoryCode.boundaries';
                INSPIRE_ISO_MAP['Addresses'] = 'MD_TopicCategoryCode.location';
                INSPIRE_ISO_MAP['Geographical names'] = 'MD_TopicCategoryCode.location';
                INSPIRE_ISO_MAP['Hydrography'] = 'MD_TopicCategoryCode.inlandWaters';
                INSPIRE_ISO_MAP['Cadastral parcels'] = 'MD_TopicCategoryCode.planningCadastre';
                INSPIRE_ISO_MAP['Transport networks'] = 'MD_TopicCategoryCode.transportation';
                INSPIRE_ISO_MAP['Protected sites'] = 'MD_TopicCategoryCode.environment';
                INSPIRE_ISO_MAP['Administrative units'] = 'MD_TopicCategoryCode.boundaries';
                INSPIRE_ISO_MAP['Orthoimagery'] = 'MD_TopicCategoryCode.imageryBaseMapsEarthCover';
                INSPIRE_ISO_MAP['Meteorological geographical features'] = 'MD_TopicCategoryCode.climatologyMeteorologyAtmosphere';
                INSPIRE_ISO_MAP['Atmospheric conditions'] = 'MD_TopicCategoryCode.climatologyMeteorologyAtmosphere';
                INSPIRE_ISO_MAP['Agricultural and aquaculture facilities'] = 'MD_TopicCategoryCode.farming';
                INSPIRE_ISO_MAP['Production and industrial facilities'] = 'MD_TopicCategoryCode.structure';
                INSPIRE_ISO_MAP['Population distribution — demography'] = 'MD_TopicCategoryCode.society';
                INSPIRE_ISO_MAP['Mineral resources'] = 'MD_TopicCategoryCode.economy';
                INSPIRE_ISO_MAP['Human health and safety'] = 'MD_TopicCategoryCode.health';
                INSPIRE_ISO_MAP['Utility and governmental services'] = 'MD_TopicCategoryCode.utilitiesCommunication';
                INSPIRE_ISO_MAP['Soil'] = 'MD_TopicCategoryCode.geoscientificInformation';
                INSPIRE_ISO_MAP['Energy resources'] = 'MD_TopicCategoryCode.economy';
                /* jshint ignore:end */
                var valueToSet=INSPIRE_ISO_MAP[value];
                parentBlock.children[4].field.value=valueToSet;
            }
        };

    })

    .controller('EditMetadataModalController', function($scope, $modalInstance, $controller,provider,identifier,type,template,theme) {
        $scope.provider = provider;
        $scope.identifier = identifier;
        $scope.type = type;
        $scope.template = template;
        $scope.current = {};
        $scope.theme = theme;
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $controller('EditMetadataController', {$scope: $scope});

    })

    .controller('ViewMetadataModalController', function($scope, $modalInstance, $http, metadataValues, dashboardName) {
        $scope.metadataValues = [];
        $scope.metadataValues.push(metadataValues.data);

        $scope.existsMetadata=false;
        if(metadataValues.data){
            $scope.existsMetadata=true;
        }

        $scope.theme = {
            "isDataDashboard": dashboardName === 'data' || false,
            "isDatasetDashboard": dashboardName === 'dataset' || false,
            "isSensorDashboard": dashboardName === 'sensor' || false
        };

        /**
         * Enable expand/collapsible events on each element in metadata view.
         */
        function initCollapseEvents () {
            if(window.collapseEventsRegistered) {return;} //to fix a bug with angular
            $(document).on('click','.expand-all-btn',function(){
                var labels = $(this).find('.label');
                var icon = $(this).find('.fa');
                var blockRows = $('#advancedViewMetadata').find('.block-row');
                var smallBlocks = blockRows.find(".small-block");
                if (icon.hasClass('fa-compress')) {
                    icon.removeClass('fa-compress');
                    icon.addClass('fa-expand');
                    blockRows.find('.collapse-block').addClass('hide');
                    smallBlocks.addClass('closed');
                    smallBlocks.find('.data-icon').removeClass('fa-angle-up');
                    smallBlocks.find('.data-icon').addClass('fa-angle-down');
                } else {
                    icon.removeClass('fa-expand');
                    icon.addClass('fa-compress');
                    blockRows.find('.collapse-block').removeClass('hide');
                    smallBlocks.removeClass('closed');
                    smallBlocks.find('.data-icon').removeClass('fa-angle-down');
                    smallBlocks.find('.data-icon').addClass('fa-angle-up');
                }
                labels.toggle();
            });
            $(document).on('click','#advancedViewMetadata .small-block .heading-block',function(){
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
            $(document).on('click','#advancedViewMetadata .collapse-row-heading',function(){
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
            window.collapseEventsRegistered = true;
        }

        $scope.isDateField = function(render){
            return (render.toLowerCase().indexOf('date') !== -1 && render.toLowerCase().indexOf('codelist') === -1);
        };
        $scope.isCodelistField = function(render){
            return (render.toLowerCase().indexOf('codelist') !== -1);
        };

        /**
         * init function called with ng-init directive for metadata viewer page.
         */
        $scope.initMetadataViewer = function() {
            initCollapseEvents();
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

    });