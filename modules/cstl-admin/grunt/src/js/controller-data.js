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

cstlAdminApp.controller('DataController', ['$scope', '$location', 'Dashboard', 'webService',
    'dataListing', 'DomainResource', 'provider', '$window', 'style', 'textService', '$modal',
    'Growl', 'StyleSharedService', '$cookies',
    function ($scope, $location, Dashboard, webService, dataListing, DomainResource,
              provider, $window, style, textService, $modal, Growl, StyleSharedService, $cookies) {
        $scope.cstlUrl = $cookies.cstlUrl;
        $scope.cstlSessionId = $cookies.cstlSessionId;
        $scope.domainId = $cookies.cstlActiveDomainId;
        $scope.advancedSearch = false;
        $scope.search = {};
        $scope.hideScroll = true;
        $scope.currentTab = 'tabdata'; //possible values are 'tabdata' and 'tabmetadata'

        /**
         * Select appropriate tab 'tabdata' or 'tabmetadata'.
         * @param item
         */
        $scope.selectTab = function(item) {
            $scope.currentTab = item;
        };

        /**
         * Toggle advanced search view panel.
         */
        $scope.toggleAdvancedSearch = function(){
          if ($scope.advancedSearch){
              $scope.advancedSearch = false;
          }  else {
              $scope.advancedSearch = true;
              $scope.searchTerm ="";
          }
        };

        /**
         * Clean advanced search inputs.
         */
        $scope.resetSearch = function(){
            $scope.search = {};
        };

        /**
         * pattern for inputs validity in advanced search
         * @type {RegExp}
         */
        $scope.alphaPattern = /^([0-9A-Za-z\u00C0-\u017F\*\?]+|\s)*$/;

        /**
         * Check the validity against the pattern and display growl error for given validity.
         * @param isInvalid
         */
        $scope.checkIsValid = function(isInvalid){
          if (isInvalid){
              Growl('error','Error','Invalid Chars');
          }
        };

        /**
         * Binding action for search button in data dashboard.
         * the result is stored with Dashboard service.
         */
        $scope.callSearch = function(){
            if ($scope.searchTerm){
                dataListing.findData({values: {'search': $scope.searchTerm}},
                    function(response) {
                        Dashboard($scope, response, true);
                    },
                    function(response){
                        console.error(response);
                        Growl('error','Error','Search failed:'+ response.data);
                    }
                );
            }else{
                if (!$.isEmptyObject($scope.search)){
                    var searchString = "";
                    if ($scope.search.title){
                        searchString += " title:"+$scope.search.title;
                    }
                    if ($scope.search.abstract){
                        searchString += " abstract:"+$scope.search.abstract;
                    }
                    if ($scope.search.keywords){
                        searchString += " keywords:"+$scope.search.keywords;
                    }
                    if ($scope.search.topic){
                        searchString += " topic:"+$scope.search.topic;
                    }
                    if ($scope.search.data){
                        searchString += " data:"+$scope.search.data;
                    }
                    if ($scope.search.level){
                        searchString += " level:"+$scope.search.level;
                    }
                    if ($scope.search.area){
                        searchString += " area:"+$scope.search.area;
                    }
                    dataListing.findData({values: {'search': searchString}},function(response) {
                        Dashboard($scope, response, true);
                    }, function(response){
                        console.error(response);
                        Growl('error','Error','Search failed:'+ response.data);
                    });
                } else {
                    dataListing.listAll({}, function(response) {
                        Dashboard($scope, response, true);
                    });
                }
            }
        };

        /**
         * main function of dashboard that loads the list of objects from server.
         */
        $scope.init = function() {
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            if($scope.currentTab === 'tabdata'){
                dataListing.listAll({}, function(response) {
                    Dashboard($scope, response, true);
                    $scope.filtertype = "";
                    modalLoader.close();
                }, function() {
                    modalLoader.close();
                });
            }else if($scope.currentTab === 'tabmetadata') {
                //@TODO call dataset rest api incoming works, do not modify this part!
                modalLoader.close();
            }
            //display button that allow to scroll to top of the page from a certain height.
            angular.element($window).bind("scroll", function() {
                $scope.hideScroll = this.pageYOffset < 220;
                $scope.$apply();
            });
        };

        /**
         * Apply filter to show only published data in service depending on given flag.
         * ie: data is linked to services.
         * @param published if true then proceed to show only published data.
         */
        $scope.showPublished = function(published){
            $scope.published=published;
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            dataListing.listPublished({published:published}, function(response) {
                Dashboard($scope, response, true);
                modalLoader.close();
            }, function() {
                modalLoader.close();
            });
        };

        /**
         * Apply filter to show only sensorable data depending on given flag.
         * ie: data is linked to sensors
         * @param observation if true then proceed to show only sensorable data.
         */
        $scope.showSensorable = function(observation){
            $scope.observation=observation;
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            dataListing.listSensorable({observation:observation},
               function(response) {//success
                Dashboard($scope, response, true);
                modalLoader.close();
            }, function() {//error
                modalLoader.close();
            });
        };

        /**
         * Returns formatted name of data for given data's provider and data's name.
         * @param providerName given provider name.
         * @param dataName given data name.
         * @returns {*}
         */
        $scope.getDisplayName = function(providerName, dataName) {
            if (providerName === dataName){
                return dataName;
            } else {
                return dataName + ' ( ' + providerName + ' ) ';
            }
        };

        // Map methods
        $scope.showData = function() {
            $('#viewerData').modal("show");
            var layerName;
            if ($scope.selected.Namespace) {
                layerName = '{' + $scope.selected.Namespace + '}' + $scope.selected.Name;
            } else {
                layerName = $scope.selected.Name;
            }

            var providerId = $scope.selected.Provider;
            var layerData;
            var modalLoader = $modal.open({
              templateUrl: 'views/modalLoader.html',
              controller: 'ModalInstanceCtrl'
            });
            if ($scope.selected.TargetStyle && $scope.selected.TargetStyle.length > 0) {
                layerData = DataViewer.createLayerWithStyle($scope.cstlUrl, layerName, providerId, $scope.selected.TargetStyle[0].Name);
            } else {
                layerData = DataViewer.createLayer($scope.cstlUrl, layerName, providerId);
            }

            //to force the browser cache reloading styled layer.
            layerData.mergeNewParams({ts:new Date().getTime()});

            var layerBackground = DataViewer.createLayer($scope.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];

            dataListing.metadata({providerId: providerId, dataId: layerName}, {}, function(response) {
                // Success getting the metadata, try to find the data extent
                DataViewer.initMap('dataMap');
                var md = response['gmd.MD_Metadata'];
                if (md) {
                    var ident = md['gmd.identificationInfo'];
                    if (ident) {
                        var extentMD = ident['gmd.MD_DataIdentification']['gmd.extent'];
                        if (extentMD) {
                            var bbox = extentMD['gmd.EX_Extent']['gmd.geographicElement']['gmd.EX_GeographicBoundingBox'];
                            var extent = new OpenLayers.Bounds(bbox['gmd.westBoundLongitude']['gco.Decimal'], bbox['gmd.southBoundLatitude']['gco.Decimal'],
                                bbox['gmd.eastBoundLongitude']['gco.Decimal'], bbox['gmd.northBoundLatitude']['gco.Decimal']);
                            DataViewer.map.zoomToExtent(extent, true);
                        }
                    }
                }
                modalLoader.close();
            }, function() {
                // failed to find a metadata, just load the full map
                DataViewer.initMap('dataMap');
                modalLoader.close();
            });
        };

        $scope.deleteData = function() {
            if (confirm("Are you sure?")) {
                var layerName = $scope.selected.Name;
                var providerId = $scope.selected.Provider;

                // Remove layer on that data before
                if ($scope.selected.TargetService && $scope.selected.TargetService.length > 0) {
                    for (var i = 0; i < $scope.selected.TargetService.length; i++) {
                        var servId = $scope.selected.TargetService[i].name;
                        var servType = $scope.selected.TargetService[i].protocol[0];
                        webService.deleteLayer({type : servType, id: servId, layerid : layerName});
                    }
                }

                dataListing.hideData({providerid: providerId, dataid: layerName}, {value : $scope.selected.Namespace},
                    function() { Growl('success','Success','Data '+ layerName +' successfully deleted');
                        dataListing.listDataForProv({providerId: providerId}, function(response) {
                            if (response.length === 0) {
                                provider.delete({id: providerId}, function() {
                                    dataListing.listAll({}, function(response) {
                                        $scope.fullList = response;
                                    });
                                });
                            } else {
                                dataListing.listAll({}, function(response) {
                                    $scope.fullList = response;
                                });
                            }
                        });
                    },
                    function() { Growl('error','Error','Data '+ layerName +' deletion failed'); }
                );
            }
        };

        $scope.displayMetadata = function() {
            $modal.open({
                templateUrl: 'views/data/modalViewMetadata.html',
                controller: 'ViewMetadataModalController',
                resolve: {
                    'selected':function(){return $scope.selected;},
                    'metadataValues':function(textService){
                        return textService.metadataJson($scope.selected.Provider,
                            $scope.selected.Name, $scope.selected.Type.toLowerCase(), true);
                    }
                }
            });
        };

        // Style methods
        $scope.showStyleList = function() {
            StyleSharedService.showStyleList($scope);
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style);
        };

        $scope.editLinkedStyle = function(styleProvider, styleName, selectedData) {
            style.get({provider: styleProvider, name: styleName}, function(response) {
                StyleSharedService.editLinkedStyle($scope, response,selectedData);
            });
        };

        $scope.showSensorsList = function() {
            $modal.open({
                templateUrl: 'views/sensor/modalSensorChoose.html',
                controller: 'SensorModalChooseController',
                resolve: {
                    'selectedData': function() { return $scope.selected; }
                }
            });
        };

        $scope.unlinkSensor = function(sensorId) {
            dataListing.unlinkSensor({providerId: $scope.selected.Provider, dataId: $scope.selected.Name, sensorId: sensorId}, {value: $scope.selected.Namespace},
                function() {
                    $scope.selected.TargetSensor.splice(0, 1);
                });
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#dataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };
        $scope.toggleUpDownSelectedMD = function() {
            var $header = $('#metadataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };

        // Data loading
        $scope.showLocalFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1DataLocal'; }
                }
            });
            modal.result.then(function(result) {
                if(!result.file){
                    return;
                }else {
                    dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function () {
                        $location.path('/description/' + result.type + "/" + result.file);
                    }, function () {
                        Growl('error', 'Error', 'Unable to save metadata');
                    });
                }
            });
        };

        $scope.showServerFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1DataServer'; }
                }
            });
            modal.result.then(function(result) {
                if(!result.file){
                    return;
                }else {
                    dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function () {
                        $location.path('/description/' + result.type + "/" + result.file);
                    }, function () {
                        Growl('error', 'Error', 'Unable to save metadata');
                    });
                }
            });
        };

        $scope.showDatabasePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1Database'; }
                }
            });
            modal.result.then(function(result) {
                if(!result.file){
                    return;
                }else {
                    dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function () {
                        $location.path('/description/' + result.type + "/" + result.file);
                    }, function () {
                        Growl('error', 'Error', 'Unable to save metadata');
                    });
                }
            });
        };
        
        $scope.showDomains = function(){
        	var modal = $modal.open({
                templateUrl: 'views/data/linkedDomains.html',
                controller: 'ModalDataLinkedDomainsController',
                resolve: {
                    'domains': function() {return dataListing.domains({dataId: $scope.selected.Id}).$promise;},
                    'dataId': function(){return $scope.selected.Id;}
                }
            });
        };

        $scope.truncate = function(small, text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (small === true && text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else if (small === false && text.length > 65) {
                        return text.substr(0, 65) + "...";
                    } else { return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small === true && text.length > 12) {
                        return text.substr(0, 12) + "...";
                    } else if (small === false && text.length > 50) {
                        return text.substr(0, 50) + "...";
                    } else { return text ;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else { return text;}
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else { return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else { return text; }
                }
            }
        };

    }]);


cstlAdminApp.controller('ModalDataLinkedDomainsController', ['$scope', '$modalInstance', 'Growl',
    'dataListing', 'domains', 'dataId',
  function($scope, $modalInstance, Growl, dataListing, domains, dataId){
	$scope.domains = domains;
    $scope.close = function() {
      $modalInstance.dismiss('close');
    };
    
    $scope.toggleDomain = function(i){
        var pathParams = {domainId: $scope.domains[i].id, dataId:dataId};
        if($scope.domains[i].linked){
        	dataListing.unlinkFromDomain(pathParams, function(){
            $scope.domains[i].linked = !$scope.domains[i].linked;
            $scope.domains[i].linked = false;
          }, function(response){
            Growl('error','error', response.data.message );
            dataListing.domains({dataId:dataId}, function(domains){
               $scope.domains = domains;          
             });
          }); 
        }else{
        	dataListing.linkToDomain(pathParams, {}, function(){
            $scope.domains[i].linked = true;
          }, function(){
            
          }); 
        }
      };
	
}]);

cstlAdminApp.controller('DescriptionController', ['$scope', '$routeParams',
    'dataListing','$location', '$translate', 'UploadFiles', '$modal','textService',
    function ($scope, $routeParams,dataListing, $location, $translate, UploadFiles, $modal,textService) {
        $scope.provider = $routeParams.id;
        $scope.type = $routeParams.type; //type is one of 'vector' or 'raster' or 'observation'.
        $scope.typeLabelKey = "metadata.edition.dataset."+$scope.type;

        /**
         * Get all codelists for metadata editor
         */
        $scope.codeLists = {};
        dataListing.codeLists({},{},function(response){
            $scope.codeLists = response;
        });

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
        dataListing.getDatasetMetadata({}, {values: {'providerId': $scope.provider,
                                                     'type':$scope.type.toLowerCase(),
                                                     'prune':false}},
            function(response) {
                if (response && response.root) {
                    $scope.metadataValues.push({"root":response.root});
                }
            },
            function(response) {
                console.error(response);
                Growl('error','Error','The server returned an error!');
            }
        );

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
            validationPopup.on('hidden.bs.modal', function(e){
                if(form && form.$invalid){
                    var firstInvalid = $('.highlight-invalid').get(0);
                    if(firstInvalid){
                        $('html, body').animate(
                            {scrollTop: $(firstInvalid).offset().top-200}, 1000);
                        $(firstInvalid).focus();
                    }
                }
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
            window.collapseEditionEventsRegistered = true;
        }

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

        $scope.initMetadataEditorEvents = function() {
            initCollapseEvents();
        };

        $scope.delete = function(data) {
            data.children = [];
        };
        $scope.add = function(data) {
            var post = data.children.length + 1;
            var newName = data.name + '-' + post;
            data.children.push({"block":{name: newName,children: []}});
        };

        /**
         * Save the metadata in server.
         */
        $scope.save = function() {
            //@TODO save metadata
            if($scope.metadataValues && $scope.metadataValues.length>0){
                dataListing.mergeMetadata({'providerId':$scope.provider,'type':$scope.type.toLowerCase()},
                    //JSON.stringify($scope.metadataValues[0],null,1), //uncomment for debugging purposes
                    $scope.metadataValues[0],
                    function(response) {
                        $location.path('/data'); //redirect to data dashboard page
                    },
                    function(response) {
                        console.error(response);
                        Growl('error','Error','The server returned an error!');
                    }
                );
            }
        };

        /**
         * Returns the current lang used.
         * For datepicker as locale.
         */
        $scope.getCurrentLang = function() {
            return $translate.use();
        };

        /**
         * Function to set values for ISO INSPIRE selectOneMenu
         * the INSPIRE predefinedValues are defined in sql.
         * the corresponding code is the topicCategory codes
         * @param value
         * @param parentBlock
         */
        $scope.updateIsoInspireSelectOneMenu = function(value,parentBlock) {
            if(value !== null) {
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

}]);

cstlAdminApp.controller('DataModalController', ['$scope', 'dataListing', 'webService', 'sos',
    'sensor', 'Dashboard', '$modalInstance', 'service', 'exclude', 'Growl', '$modal',
    function ($scope, dataListing, webService, sos, sensor, Dashboard, $modalInstance,
              service, exclude, Growl, $modal) {
        $scope.service = service;

        $scope.getDefaultFilter = function() {
            if (service.type.toLowerCase() === 'wcs') {
                return 'coverage';
            }
            if (service.type.toLowerCase() === 'wfs') {
                return 'vector';
            }
            return '';
        };
        $scope.nbbypage = 5;
        $scope.exclude = exclude;

        // WMTS params in the last form before closing the popup
        $scope.wmtsParams = false;
        $scope.tileFormat = undefined;
        $scope.crs = undefined;
        $scope.scales = [];
        $scope.upperCornerX = undefined;
        $scope.upperCornerY = undefined;
        $scope.conformPyramid = undefined;

        $scope.init = function() {
            if (service.type.toLowerCase() === 'sos') {
                sensor.list({}, function(response) {
                    Dashboard($scope, response.children, false);
                });
            } else {
                dataListing.listAll({}, function (response) {
                    Dashboard($scope, response, true);
                    $scope.filtertype = $scope.getDefaultFilter();
                });
            }
        };

        $scope.selectedSensorsChild = null;

        $scope.selectSensorsChild = function(item) {
            if ($scope.selectedSensorsChild === item) {
                $scope.selectedSensorsChild = null;
            } else {
                $scope.selectedSensorsChild = item;
            }
        };

        $scope.dataSelect={all:false};
        $scope.listSelect=[];

        $scope.selectAllData = function() {
            if ($scope.dataSelect.all) {
                $scope.listSelect = $scope.dataList.slice();
            }else{
                $scope.listSelect=[];
            }
        };
        $scope.dataInArray = function(item){
            if($scope.listSelect.length>0) {
                for (var i = 0; i < $scope.listSelect.length; i++) {
                    if ($scope.listSelect[i].Name === item.Name && $scope.listSelect[i].Provider === item.Provider) {
                        $scope.listSelect.splice(i, 1);
                        break;
                    }
                    if(i===$scope.listSelect.length-1){
                        if ($scope.listSelect[i].Name !== item.Name || $scope.listSelect[i].Provider !== item.Provider){
                            $scope.listSelect.push(item);
                            break;
                        }
                    }
                }
            } else { $scope.listSelect.push(item);}

            if($scope.listSelect.length < $scope.dataList.length){
                $scope.dataSelect.all=false;
            } else { $scope.dataSelect.all=true; }
        };
        $scope.isInSelected = function(item){
            for(var i=0; i < $scope.listSelect.length; i++){
                if($scope.listSelect[i].Name === item.Name && $scope.listSelect[i].Provider === item.Provider){
                    return true;
                }
            }
            return false;
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        function addLayer(tiledProvider) {
            webService.addLayer({type: service.type, id: service.identifier},
                {layerAlias: tiledProvider.dataId, layerId: tiledProvider.dataId, serviceType: service.type, serviceId: service.identifier, providerId: tiledProvider.providerId},
                function () {
                    Growl('success', 'Success', 'Layer ' + tiledProvider.dataId + ' successfully added to service ' + service.name);
                    $modalInstance.close();
                },
                function () {
                    Growl('error', 'Error', 'Layer ' + tiledProvider.dataId + ' failed to be added to service ' + service.name);
                    $modalInstance.dismiss('close');
                }
            );
        }

        function pyramidGenerationError(i) {
            Growl('error', 'Error', 'Failed to generate conform pyramid for ' + $scope.selected[i].Name);
            $modalInstance.dismiss('close');
        }

        function success(response) {
            Growl('success', 'Success', response.message);
            // Not in WMTS and no pyramid requested
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            modalLoader.close();
            $modalInstance.close();
        }

        function error(response) {
            Growl('error', 'Error', response.message);
            $modalInstance.dismiss('close');
        }

        function setScale(response) {
            $scope.scales = response.Entry[0].split(',');
        }

        function errorOnPyramid(j) {
            Growl('error', 'Error', 'Unable to pyramid data ' + $scope.selected[j].Name);
        }

        $scope.choose = function() {
            if ($scope.listSelect.length !== 0) {
                $scope.selected = $scope.listSelect;
            }
            if ($scope.selected === null) {
                Growl('warning', 'Warning', 'No data selected');
                $modalInstance.dismiss('close');
                return;
            }
            else{
                if ($scope.service.type.toLowerCase() === 'sos') {
                    var sensorId = ($scope.selectedSensorsChild !== null) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                    sos.importSensor({id: service.identifier}, {values: {"sensorId": sensorId}}, function () {
                        Growl('success', 'Success', 'Sensor ' + sensorId + ' imported in service ' + service.name);
                        $modalInstance.close();
                    }, function () {
                        Growl('error', 'Error', 'Unable to import sensor ' + sensorId + ' in service ' + service.name);
                        $modalInstance.dismiss('close');
                    });
                    return;
                }

                if ($scope.wmtsParams === false) {
                    // just add the data if we are not in the case of the wmts service
                    if (service.type.toLowerCase() !== 'wmts') {
                        for(var i=0; i<$scope.selected.length; i++) {
                            if (service.type.toLowerCase() === 'wms' && $scope.conformPyramid) {
                                // In the case of a wms service and user asked to pyramid the data
                                dataListing.pyramidConform({providerId: $scope.selected[i].Provider, dataId: $scope.selected[i].Name}, {}, addLayer, pyramidGenerationError(i));
                            } else {
                                webService.addLayer({type: service.type, id: service.identifier},
                                    {layerAlias: $scope.selected[i].Name, layerId: $scope.selected[i].Name, serviceType: service.type, serviceId: service.identifier, providerId: $scope.selected[i].Provider, layerNamespace: $scope.selected[i].Namespace},
                                    success,error);
                            }
                        }
                        return;
                    }
                    for(var j=0; j<$scope.selected.length; j++) {
                        // WMTS here, prepare form
                        dataListing.pyramidScales({providerId: $scope.selected[j].Provider, dataId: $scope.selected[j].Name}, setScale, errorOnPyramid(j));
                        $scope.wmtsParams = true;
                    }
                } else {
                    // Finish the WMTS publish process
                    // Pyramid the data to get the new provider to add
                    for(var k=0; k<$scope.selected.length; k++) {
                        dataListing.pyramidData({providerId: $scope.selected[k].Provider, dataId: $scope.selected[k].Name},
                            {tileFormat: $scope.tileFormat, crs: $scope.crs, scales: $scope.scales, upperCornerX: $scope.upperCornerX, upperCornerY: $scope.upperCornerY}, addLayer, pyramidGenerationError(k));
                    }
                }
            }
        };

        $scope.truncate = function(text){
            if(text !== null) {
                if (text.length > 40) {
                    return text.substr(0, 40) + "...";
                } else { return text; }
            }
        };
    }]);


cstlAdminApp.controller('ViewMetadataModalController', ['$scope', '$modalInstance','$http',
    'selected', 'metadataValues',
    function ($scope, $modalInstance, $http, selected, metadataValues) {
        $scope.metadataValues = [];
        $scope.metadataValues.push(metadataValues.data);

        $scope.selectedData = selected;

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
            return (render.toLowerCase().indexOf('date') !== -1);
        };
        $scope.isCodelistField = function(render){
            return (render.toLowerCase().indexOf('codelist') !== -1);
        };

        $scope.initMetadataViewer = function() {
            initCollapseEvents();
        };

        $scope.delete = function(data) {
            data.children = [];
        };
        $scope.add = function(data) {
            var post = data.children.length + 1;
            var newName = data.name + '-' + post;
            data.children.push({"block":{name: newName,children: []}});
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

    }]);
