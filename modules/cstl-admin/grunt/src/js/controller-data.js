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
'use strict';


cstlAdminApp.controller('DataController', ['$scope', '$location', '$dashboard', 'webService', 'dataListing', 'DomainResource', 'provider', '$window',
    'style', 'textService', '$modal', 'Growl', 'StyleSharedService', '$cookies',
    function ($scope, $location, $dashboard, webService, dataListing, DomainResource,  provider, $window, style, textService, $modal, Growl,
              StyleSharedService, $cookies) {
        $scope.cstlUrl = $cookies.cstlUrl;
        $scope.cstlSessionId = $cookies.cstlSessionId;
        $scope.domainId = $cookies.cstlActiveDomainId;
        $scope.advancedSearch = false;
        $scope.search = {};
        $scope.hideScroll = true;

        $scope.toggleAdvancedSearch = function(){
          if ($scope.advancedSearch){
              $scope.advancedSearch = false;
          }  else {
              $scope.advancedSearch = true;
              $scope.searchTerm ="";
          }
        };

        $scope.resetSearch = function(){
            $scope.search = {};
        };

        $scope.checkIsValid = function(isInvalid){
          if (isInvalid){
              Growl('error','Error','Invalid Chars');
          }
        };

        $scope.alphaPattern = /^([0-9A-Za-z\u00C0-\u017F\*\?]+|\s)*$/;

        $scope.callSearch = function(){
            if ($scope.searchTerm){
                dataListing.findData({values: {'search': $scope.searchTerm}},
                    function(response) {
                        $dashboard($scope, response, true);
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
                        $dashboard($scope, response, true);
                    }, function(response){
                        console.error(response);
                        Growl('error','Error','Search failed:'+ response.data);
                    });
                } else {
                    dataListing.listAll({}, function(response) {
                        $dashboard($scope, response, true);
                    });
                }
            }
        };

        $scope.init = function() {
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            dataListing.listAll({}, function(response) {
                $dashboard($scope, response, true);
                $scope.filtertype = "";
                modalLoader.close();
            }, function() {
                modalLoader.close();
            });

            angular.element($window).bind("scroll", function() {
                if (this.pageYOffset < 220) {
                    $scope.hideScroll = true;
                } else {
                    $scope.hideScroll = false;
                }
                $scope.$apply();
            });
        };

        $scope.getDisplayName = function(providerName, dataName) {
                if (providerName == dataName){
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
                            if (response.length == 0) {
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
                    'selected':function(){return $scope.selected},
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
                if (result.missing) {
                    dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function () {
                        $location.path('/description/' + result.type + "/" + result.file + "/" + result.missing);
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
                if (result.missing) {
                    dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function () {
                        $location.path('/description/' + result.type + "/" + result.file + "/" + result.missing);
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
                if (result.missing) {
                    dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function () {
                        $location.path('/description/' + result.type + "/" + result.file + "/" + result.missing);
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
                    'domains': function() {return dataListing.domains({dataId: $scope.selected.Id}).$promise},
                    'dataId': function(){return $scope.selected.Id}
                }
            });
        };

        $scope.tabdata = true;
        $scope.tabmetadata = false;
        $scope.selectTab = function(item) {
            if (item === 'tabdata') {
                $scope.tabdata = true;
                $scope.tabmetadata = false;
            } else if (item === 'tabmetadata') {
                $scope.tabdata = false;
                $scope.tabmetadata = true;
            }
        };

        $scope.truncate = function(small, text){
            if(text != null) {
                if (window.innerWidth >= 1200) {
                    if (small == true && text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else if (small == false && text.length > 65) {
                        return text.substr(0, 65) + "...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small == true && text.length > 12) {
                        return text.substr(0, 12) + "...";
                    } else if (small == false && text.length > 50) {
                        return text.substr(0, 50) + "...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else return text;
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text != null) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else return text;
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else return text;
                } else if (window.innerWidth < 992) {
                    if (text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else return text;
                }
            }
        };

    }]);


cstlAdminApp.controller('ModalDataLinkedDomainsController', ['$scope', '$modalInstance', 'Growl', 'dataListing', 'domains', 'dataId',
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
             })
          }); 
        }else{
        	dataListing.linkToDomain(pathParams, {}, function(){
            $scope.domains[i].linked = true;
          }, function(){
            
          }); 
        }
      }
	
}]);

cstlAdminApp.controller('DescriptionController', ['$scope', '$routeParams',
    'dataListing','$location', '$translate', '$uploadFiles', '$modal','textService',
    function ($scope, $routeParams,dataListing, $location, $translate, $uploadFiles, $modal,textService) {
        $scope.provider = $routeParams.id;
        $scope.missing = $routeParams.missing === 'true';
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
        //$scope.predefinedValues.inspireThemes = $scope.predefinedValues.inspireThemes.sort();
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
        //$scope.predefinedValues.referenceSystemIdentifier = $scope.predefinedValues.referenceSystemIdentifier.sort();
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
        dataListing.getDatasetMetadata({}, {values: {'providerId': $scope.provider, 'type':$scope.type.toLowerCase(),'prune':false}},
            function(response) {
                if (response && response.root) {
                    $scope.metadataValues.push({"root":response.root});
                    //$scope.codeLists =dataListing.codeLists({});
                }
            }
        );

        $scope.isInValidRequiredField = function(input){
            if(input){
                /*if(input.$invalid && input.$dirty){
                    console.debug(input.$error);
                }*/
                return input.$invalid && input.$dirty;
            }
            return false;
        };

        function initCollapseEvents () {
            if(window.collapseEditionEventsRegistered)return; //to fix a bug with angular
            $(document).on('click','.small-block .heading-block',function(){
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
            $(document).on('click','.collapse-row-heading',function(){
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

        $scope.getMetadataTitle = function() {
            if($scope.metadataValues && $scope.metadataValues.length>0){
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
            //$scope.metadata.dataName = $scope.provider;
            //$scope.metadata.type = $scope.type;
            if($scope.metadataValues && $scope.metadataValues.length>0){
                console.debug($scope.metadataValues[0]);
                $location.path('/data'); //redirect to data dashboard page
            }
            //@TODO save metadata
            /*dataListing.mergeMetadata({}, $scope.metadata,
                function() {
                    $location.path('/data');
                }
            );*/
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
            if(value != null) {
                var INSPIRE_ISO_MAP = {};
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
                var valueToSet=INSPIRE_ISO_MAP[value];
                parentBlock.children[4].field.value=valueToSet;
            }
        };

}]);

cstlAdminApp.controller('DataModalController', ['$scope', 'dataListing', 'webService', 'sos', 'sensor', '$dashboard', '$modalInstance', 'service', 'exclude', 'Growl', '$modal',
    function ($scope, dataListing, webService, sos, sensor, $dashboard, $modalInstance, service, exclude, Growl, $modal) {
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
                    $dashboard($scope, response.children, false);
                });
            } else {
                dataListing.listAll({}, function (response) {
                    $dashboard($scope, response, true);
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
        }
        $scope.dataInArray = function(item){
            if($scope.listSelect.length>0) {
                for (var i = 0; i < $scope.listSelect.length; i++) {
                    if ($scope.listSelect[i].Name == item.Name && $scope.listSelect[i].Provider == item.Provider) {
                        $scope.listSelect.splice(i, 1);
                        break;
                    }
                    if(i==$scope.listSelect.length-1){
                        if ($scope.listSelect[i].Name != item.Name || $scope.listSelect[i].Provider != item.Provider){
                            $scope.listSelect.push(item);
                            break;
                        }
                    }
                }
            } else $scope.listSelect.push(item);

            if($scope.listSelect.length < $scope.dataList.length){
                $scope.dataSelect.all=false;
            } else $scope.dataSelect.all=true;
        }
        $scope.isInSelected = function(item){
            for(var i=0; i < $scope.listSelect.length; i++){
                if($scope.listSelect[i].Name == item.Name && $scope.listSelect[i].Provider == item.Provider){
                    return true;
                }
            }
            return false;
        }

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.choose = function() {
            if ($scope.listSelect.length != 0) {
                $scope.selected = $scope.listSelect;
            }
            if ($scope.selected == null) {
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
                                dataListing.pyramidConform({providerId: $scope.selected[i].Provider, dataId: $scope.selected[i].Name}, {}, function (tiledProvider) {
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
                                }, function () {
                                    Growl('error', 'Error', 'Failed to generate conform pyramid for ' + $scope.selected[i].Name);
                                    $modalInstance.dismiss('close');
                                });
                            } else {
                                webService.addLayer({type: service.type, id: service.identifier},
                                    {layerAlias: $scope.selected[i].Name, layerId: $scope.selected[i].Name, serviceType: service.type, serviceId: service.identifier, providerId: $scope.selected[i].Provider, layerNamespace: $scope.selected[i].Namespace},
                                    function (response) {
                                        Growl('success', 'Success', response.message);
                                        modalLoader.close();
                                        $modalInstance.close();
                                    },
                                    function (response) {
                                        Growl('error', 'Error', response.message);
                                        $modalInstance.dismiss('close');
                                    }
                                );
                            }
                        }
                        // Not in WMTS and no pyramid requested
                        var modalLoader = $modal.open({
                            templateUrl: 'views/modalLoader.html',
                            controller: 'ModalInstanceCtrl'
                        });
                        return;
                    }
                    for(var i=0; i<$scope.selected.length; i++) {
                        // WMTS here, prepare form
                        dataListing.pyramidScales({providerId: $scope.selected[i].Provider, dataId: $scope.selected[i].Name}, function (response) {
                            $scope.scales = response.Entry[0].split(',');
                        }, function () {
                            Growl('error', 'Error', 'Unable to pyramid data ' + $scope.selected[i].Name);
                        });

                        $scope.wmtsParams = true;
                    }
                } else {
                    // Finish the WMTS publish process
                    // Pyramid the data to get the new provider to add
                    for(var i=0; i<$scope.selected.length; i++) {
                        dataListing.pyramidData({providerId: $scope.selected[i].Provider, dataId: $scope.selected[i].Name},
                            {tileFormat: $scope.tileFormat, crs: $scope.crs, scales: $scope.scales, upperCornerX: $scope.upperCornerX, upperCornerY: $scope.upperCornerY},
                            function (respProvider) {
                                // Add the tiled provider to the service
                                webService.addLayer({type: service.type, id: service.identifier},
                                    {layerAlias: respProvider.dataId, layerId: respProvider.dataId, serviceType: service.type, serviceId: service.identifier, providerId: respProvider.providerId},
                                    function () {
                                        Growl('success', 'Success', 'Layer ' + respProvider.dataId + ' successfully added to service ' + service.name);
                                        $modalInstance.close();
                                    },
                                    function () {
                                        Growl('error', 'Error', 'Layer ' + respProvider.dataId + ' failed to be added to service ' + service.name);
                                        $modalInstance.dismiss('close');
                                    }
                                );
                            }, function () {
                                Growl('error', 'Error', 'Pyramid process failed for ' + $scope.selected[i].Name);
                            });
                    }
                }
            }
        };

        $scope.truncate = function(text){
            if(text != null) {
                if (text.length > 40) {
                    return text.substr(0, 40) + "...";
                } else return text;
            }
        };
    }]);


cstlAdminApp.controller('ViewMetadataModalController', ['$scope', '$modalInstance','$http', 'selected', 'metadataValues',
    function ($scope, $modalInstance, $http, selected, metadataValues) {
        $scope.metadataValues = [];
        $scope.metadataValues.push(metadataValues.data);

        $scope.selectedData = selected;

        function initCollapseEvents () {
            if(window.collapseEventsRegistered)return; //to fix a bug with angular
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
            $(document).on('click','.small-block .heading-block',function(){
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
            $(document).on('click','.collapse-row-heading',function(){
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
            return (render.toLowerCase().indexOf('date') != -1);
        };
        $scope.isCodelistField = function(render){
            return (render.toLowerCase().indexOf('codelist') != -1);
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
