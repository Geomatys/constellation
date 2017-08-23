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

    .controller('EditMetadataController', function ($scope, $routeParams,dataListing, $location, $translate, Growl,$cookieStore,$rootScope,$modal) {
        $scope.provider = $scope.provider || $routeParams.provider;
        $scope.identifier = $scope.identifier || $routeParams.identifier;

        $scope.typeLabelKey = $scope.typeLabelKey || "metadata.edition.dataset.import";
        $scope.type = $scope.type || $routeParams.type; //type is one of 'vector' or 'raster' or 'observation'.
        if ($scope.type) {
            $scope.type = $scope.type.toLowerCase();
            $scope.typeLabelKey = "metadata.edition.dataset." + $scope.type;
        }

        $scope.template = $scope.template || $routeParams.template || 'import';
        $scope.theme = $scope.theme || 'data';

        $scope.uriRegExp=/^([a-z][a-z0-9+.-]*):(?:\/\/((?:(?=((?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*))(\3)@)?(?=(\[[0-9A-F:.]{2,}\]|(?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*))\5(?::(?=(\d*))\6)?)(\/(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*))\8)?|(\/?(?!\/)(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*))\10)?)(?:\?(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/?]|%[0-9A-F]{2})*))\11)?(?:#(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/?]|%[0-9A-F]{2})*))\12)?$/i;

        $scope.contentError = $scope.contentError || false;

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
                            $scope.contentError = false;
                        }else {
                            $scope.contentError = true;
                        }
                    },
                    function(response) {
                        $scope.contentError = true;
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
                            $scope.contentError = false;
                        }else {
                            $scope.contentError = true;
                        }
                    },
                    function(response) {
                        $scope.contentError = true;
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
            var validationPopup = $modal.open({
                templateUrl: 'validation_popup.html',
                controller : ['$scope','metadataform', function($scope,metadataform){
                    $scope.metadataform = metadataform;
                }],
                resolve: {
                    'metadataform':function(){return form;}
                }
            });
            validationPopup.result.then(function(value){
                //nothing to do here
            }, function () {
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
        };

        /**
         * Display bbox modal to enter spatial extent for metadata block with render=BOUNDINGBOX
         * @param blockObj the current block that contains field children with current values.
         */
        $scope.openBboxModal = function(blockObj) {
            var bboxPopup = $modal.open({
                templateUrl: 'bbox_popup.html',
                controller : 'BboxMetadataModalController as bboxCtrl',
                resolve: {
                    'block':function(){return blockObj;}
                }
            });
        };

        /**
         * Add new occurrence of field. the field must have multiplicity gt 1
         * @param blockObj
         * @param fieldObj
         */
        $scope.addFieldOccurrence = function(blockObj,fieldObj) {
            var newField = {"type": "field","field":{}};
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
            var newBlock = {"type": "block","block":{}};
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
            var max = 0;
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
         * a field should be marked with tag=title in json template.
         * @returns {string} the title value located in json model.
         */
        $scope.getMetadataTitle = function() {
            if($scope.metadataValues && $scope.metadataValues.length>0){
                for(var i=0;i<$scope.metadataValues[0].root.children.length;i++){
                    var sb = $scope.metadataValues[0].root.children[i];
                    for(var j=0;j<sb.superblock.children.length;j++){
                        var b = sb.superblock.children[j];
                        for(var k=0;k<b.block.children.length;k++){
                            var f = b.block.children[k];
                            if(f.field.tag === 'title'){
                                return f.field.value;
                            }
                        }
                    }
                }
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
                            Growl('error','Error','Failed to save metadata because the server returned an error!');
                        }
                    );
                }
            }
        };

        $scope.uploadImage = $scope.uploadImage || function(value,field) {
            var cstlUrl = $cookieStore.get('cstlUrl');
            if(value) {
                var $form = $('#metadataform');
                var fileInput = $form.find('.uploadimage');
                if(!fileInput || !fileInput.get(0).files || fileInput.get(0).files.length===0){
                    return;
                }
                var fileSize = fileInput.get(0).files[0].size/1000000;
                if(fileSize > 2){
                    Growl('error', 'Error', 'The image size exceed the limitation of 2Mo.');
                    return;
                }
                var formData = new FormData($form[0]);
                $.ajax({
                    headers: {
                        'access_token': $rootScope.access_token
                    },
                    url: cstlUrl + "api/1/metadata/uploadGraphicOverview2/"+$scope.identifier,
                    type: 'POST',
                    data: formData,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (response) {
                        $scope.$apply(function() {
                            if(response.mdIdentifierSHA1) {
                                field.value = cstlUrl+'spring/resolveImage/'+response.mdIdentifierSHA1+'?t=' + new Date().getTime();
                            }
                        });
                    },
                    error: function(){
                        Growl('error', 'Error', 'error while uploading image');
                    }
                });
            } else {
                field.value = "";
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
                var fieldIndexToChange = -1;
                for(var i=0;i<parentBlock.children.length;i++){
                    var fobj = parentBlock.children[i];
                    if(fobj.field && fobj.field.render === 'ISO_INSPIRE.codelist') {
                        fieldIndexToChange = i;
                        break;
                    }
                }
                if(fieldIndexToChange!==-1){
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
                    parentBlock.children[fieldIndexToChange].field.value=valueToSet;
                }
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

        $scope.dismiss = function () {
            $modalInstance.dismiss('close');
        };

        $scope.close = function () {
            $modalInstance.close();
        };

        $controller('EditMetadataController', {$scope: $scope});

    })

    .controller('BboxMetadataModalController', function($scope, $modalInstance, block) {
        var self = this;
        self.bboxOptions = {
            enableDragBoxControl : true,
            currentBboxBlock : block,
            olVectorLayer : null,
            olStyle : [
                new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: '#66AADD',
                        width: 2.25
                    })
                })
            ],
            north : 90.0,
            south : -90.0,
            east : 180.0,
            west : -180.0
        };

        self.initCtrl = function() {
            //get existing coordinates values if exists.
            self.retrieveBboxValues(self.bboxOptions.currentBboxBlock);
            //show the map
            //note we use here a setTimeout because this version of uibootstrap does not have the rendered promise yet
            //$modalInstance.rendered.then
            MetadataEditorViewer.enableDragBoxControl = self.bboxOptions.enableDragBoxControl;
            setTimeout(function(){
                self.showBboxViewer();
            },400);
        };

        self.toggleControlDragBox = function() {
            self.bboxOptions.enableDragBoxControl = !self.bboxOptions.enableDragBoxControl;
            MetadataEditorViewer.enableDragBoxControl = self.bboxOptions.enableDragBoxControl;
        };

        self.retrieveBboxValues = function(blockObj) {
            if(blockObj && blockObj.block && blockObj.block.children) {
                var fields = blockObj.block.children;
                for(var i = 0; i< fields.length;i++) {
                    if(fields[i].field && fields[i].field.name.indexOf('westBoundLongitude') !==-1 && fields[i].field.value){
                        self.bboxOptions.west = Number(fields[i].field.value);
                    }else if(fields[i].field && fields[i].field.name.indexOf('eastBoundLongitude') !==-1 && fields[i].field.value){
                        self.bboxOptions.east = Number(fields[i].field.value);
                    }else if(fields[i].field && fields[i].field.name.indexOf('northBoundLatitude') !==-1 && fields[i].field.value){
                        self.bboxOptions.north = Number(fields[i].field.value);
                    }else if(fields[i].field && fields[i].field.name.indexOf('southBoundLatitude') !==-1 && fields[i].field.value){
                        self.bboxOptions.south = Number(fields[i].field.value);
                    }
                }
            }
        };

        /**
         * Show the bbox map in the modal for metadata editor.
         */
        self.showBboxViewer = function() {
            if (MetadataEditorViewer.map) {
                MetadataEditorViewer.map.setTarget(undefined);
            }
            MetadataEditorViewer.initConfig();
            MetadataEditorViewer.enableAttributions = false;
            var bboxExists = (self.bboxOptions.north && self.bboxOptions.south && self.bboxOptions.west && self.bboxOptions.east) &&
                (self.bboxOptions.north !== 90.0 || self.bboxOptions.south !== -90.0 || self.bboxOptions.west !== -180.0 || self.bboxOptions.east !== 180.0);
            if (bboxExists) {
                var minX = self.bboxOptions.west;
                var minY = self.bboxOptions.south;
                var maxX = self.bboxOptions.east;
                var maxY = self.bboxOptions.north;
                //For pseudo Mercator we need to check against the validity,
                // the bbox crs is always defined as EPSG:4326
                //if the viewer use pseudo Mercator then fix Latitude to avoid Infinity values
                if(MetadataEditorViewer.projection === 'EPSG:3857'){
                    if(minY < -85){minY=-85;}
                    if(maxY > 85){maxY=85;}
                }
                var coordinates = [[[minX, minY], [minX, maxY], [maxX, maxY], [maxX, minY], [minX, minY]]];
                var polygon = new ol.geom.Polygon(coordinates);
                polygon = polygon.transform('EPSG:4326',MetadataEditorViewer.projection);
                var extentFeature = new ol.Feature(polygon);
                MetadataEditorViewer.initMap('bboxMap');
                self.bboxOptions.olVectorLayer = new ol.layer.Vector({
                    map: MetadataEditorViewer.map,
                    source: new ol.source.Vector({
                        features: new ol.Collection(),
                        useSpatialIndex: false // optional, might improve performance
                    }),
                    style: self.bboxOptions.olStyle,
                    updateWhileAnimating: true, // optional, for instant visual feedback
                    updateWhileInteracting: true // optional, for instant visual feedback
                });
                self.bboxOptions.olVectorLayer.getSource().addFeature(extentFeature);
                MetadataEditorViewer.zoomToExtent([minX,minY,maxX,maxY],MetadataEditorViewer.map.getSize(),false);
            } else {
                MetadataEditorViewer.initMap('bboxMap');
                MetadataEditorViewer.map.getView().setZoom(1);
            }

            //mouse coordinates
            $('#bboxMap').find('div.ol-scale-line')
                .after($('<p style="bottom:0;position:absolute;right:5px;">' +
                    '<span id="mouse4326" class="label" style="background-color:rgba(0,60,136,0.3)"></span>' +
                    '</p>'));
            MetadataEditorViewer.map.on('pointermove', function(event) {
                var eventCoords = event.coordinate;
                var coord4326 = ol.proj.transform(eventCoords, MetadataEditorViewer.projection, 'EPSG:4326');
                var template = 'Coord. {x} / {y}';
                var coordStr = ol.coordinate.format(coord4326, template, 3);
                $('#mouse4326').text(coordStr);
            });

            //drag bbox control
            var dragBox = new ol.interaction.DragBox({
                condition: function(){
                    return MetadataEditorViewer.enableDragBoxControl;
                }
            });
            MetadataEditorViewer.map.addInteraction(dragBox);
            var startX,startY,endX,endY;
            dragBox.on('boxstart',function(ev){
                var eventCoords = ev.coordinate;
                startX =eventCoords[0];
                startY =eventCoords[1];
                $('#bboxMap').css("cursor", "crosshair");
            });
            dragBox.on('boxend',function(ev){
                $scope.$apply(function() {
                    var eventCoords = ev.coordinate;
                    endX = eventCoords[0];
                    endY = eventCoords[1];
                    var minX = Math.min(startX,endX);
                    var minY = Math.min(startY,endY);
                    var maxX = Math.max(startX,endX);
                    var maxY = Math.max(startY,endY);
                    var coordinates = [[[minX, minY], [minX, maxY], [maxX, maxY], [maxX, minY], [minX, minY]]];
                    var polygon = new ol.geom.Polygon(coordinates);
                    var feature = new ol.Feature(polygon);

                    if(!self.bboxOptions.olVectorLayer) {
                        self.bboxOptions.olVectorLayer = new ol.layer.Vector({
                            map: MetadataEditorViewer.map,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                useSpatialIndex: false // optional, might improve performance
                            }),
                            style: self.bboxOptions.olStyle,
                            updateWhileAnimating: true, // optional, for instant visual feedback
                            updateWhileInteracting: true // optional, for instant visual feedback
                        });
                    }
                    self.bboxOptions.olVectorLayer.getSource().clear();
                    self.bboxOptions.olVectorLayer.getSource().addFeature(feature);
                    var extent4326 = ol.proj.transformExtent([minX,minY,maxX,maxY], MetadataEditorViewer.projection,'EPSG:4326');
                    self.bboxOptions.north=Number(extent4326[3].toFixed(4));
                    self.bboxOptions.south=Number(extent4326[1].toFixed(4));
                    self.bboxOptions.east=Number(extent4326[2].toFixed(4));
                    self.bboxOptions.west=Number(extent4326[0].toFixed(4));
                    $('#bboxMap').css("cursor", "");
                });
            });
        };

        /**
         * onchange function for inputs to apply changes of coordinates to viewer.
         */
        self.onChangeBboxCoords = function() {
            if (self.bboxOptions.north && self.bboxOptions.south && self.bboxOptions.east && self.bboxOptions.west) {
                var minX = Number(self.bboxOptions.west);
                var minY = Number(self.bboxOptions.south);
                var maxX = Number(self.bboxOptions.east);
                var maxY = Number(self.bboxOptions.north);
                if(MetadataEditorViewer.projection === 'EPSG:3857'){
                    if(minY < -85){minY=-85;}
                    if(maxY > 85){maxY=85;}
                }
                var coordinates = [[[minX, minY], [minX, maxY], [maxX, maxY], [maxX, minY], [minX, minY]]];
                var polygon = new ol.geom.Polygon(coordinates);
                polygon = polygon.transform('EPSG:4326',MetadataEditorViewer.projection);
                var feature = new ol.Feature(polygon);
                if(!self.bboxOptions.olVectorLayer) {
                    self.bboxOptions.olVectorLayer = new ol.layer.Vector({
                        map: MetadataEditorViewer.map,
                        source: new ol.source.Vector({
                            features: new ol.Collection(),
                            useSpatialIndex: false // optional, might improve performance
                        }),
                        style: self.bboxOptions.olStyle,
                        updateWhileAnimating: true, // optional, for instant visual feedback
                        updateWhileInteracting: true // optional, for instant visual feedback
                    });
                }
                self.bboxOptions.olVectorLayer.getSource().clear();
                self.bboxOptions.olVectorLayer.getSource().addFeature(feature);
            }
        };

        /**
         * Apply new coordinates to the metadata block and close the modal.
         */
        self.saveBboxValues = function() {
            if(self.bboxOptions.currentBboxBlock &&
                self.bboxOptions.currentBboxBlock.block &&
                self.bboxOptions.currentBboxBlock.block.children) {
                var fields = self.bboxOptions.currentBboxBlock.block.children;
                for(var i = 0; i< fields.length;i++) {
                    if(fields[i].field && fields[i].field.name.indexOf('westBoundLongitude') !==-1){
                        fields[i].field.value = self.bboxOptions.west;
                    }else if(fields[i].field && fields[i].field.name.indexOf('eastBoundLongitude') !==-1){
                        fields[i].field.value = self.bboxOptions.east;
                    }else if(fields[i].field && fields[i].field.name.indexOf('northBoundLatitude') !==-1){
                        fields[i].field.value = self.bboxOptions.north;
                    }else if(fields[i].field && fields[i].field.name.indexOf('southBoundLatitude') !==-1){
                        fields[i].field.value = self.bboxOptions.south;
                    }
                }
            }
            $modalInstance.close();
        };
        self.initCtrl();
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