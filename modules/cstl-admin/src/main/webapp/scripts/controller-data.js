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

cstlAdminApp.controller('DataController', ['$scope', '$location', '$dashboard', 'webService', 'dataListing', 'provider', 'style', 'textService', '$modal', '$growl', 'StyleSharedService', '$cookies',
    function ($scope, $location, $dashboard, webService, dataListing, provider, style, textService, $modal, $growl, StyleSharedService, $cookies) {
        $scope.cstlUrl = $cookies.cstlUrl;
        $scope.cstlSessionId = $cookies.cstlSessionId;

        $scope.init = function() {
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            dataListing.listAll({}, function(response) {
                $dashboard($scope, response, true);
                $scope.filtertype = "";
                modalLoader.close();
            });
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
                    function() { $growl('success','Success','Data '+ layerName +' successfully deleted');
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
                    function() { $growl('error','Error','Data '+ layerName +' deletion failed'); }
                );
            }
        };

        $scope.displayMetadata = function() {
            $modal.open({
                templateUrl: 'views/modalViewMetadata.html',
                controller: 'ViewMetadataModalController',
                resolve: {
                    'details': function(textService){
                        return textService.metadata($scope.selected.Provider, $scope.selected.Name);
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
                templateUrl: 'views/modalSensorChoose.html',
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
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
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
                dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function() {
                    $location.path('/description/'+ result.type +"/"+ result.file +"/"+ result.missing);
                }, function() { $growl('error','Error','Unable to save metadata'); });
            });
        };

        $scope.showServerFilePopup = function() {
            $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1DataServer'; }
                }
            });
        };
         $scope.showDatabasePopup = function() {
            $modal.open({
                templateUrl: 'views/data/modalImportData.html',
                controller: 'ModalImportDataController',
                resolve: {
                    'firstStep': function() { return 'step1Database'; }
                }
            });
        };
    }]);

cstlAdminApp.controller('DescriptionController', ['$scope', '$routeParams','dataListing','$location', '$translate', '$uploadFiles', '$modal',
    function ($scope, $routeParams, dataListing, $location, $translate, $uploadFiles, $modal) {
        $scope.provider = $routeParams.id;
        $scope.missing = $routeParams.missing === 'true';
        $scope.type = $routeParams.type;

        $scope.tabiso = $scope.missing;
        $scope.tabcrs = false;
        $scope.tabdesc = $scope.type==='vector' && !$scope.missing;
        $scope.tabimageinfo = $scope.type==='raster' && !$scope.missing;
        $scope.tabsensorinfo = $scope.type==='observation' && !$scope.missing;

        $scope.metadata = {};
        $scope.metadata.keywords = [];

        dataListing.getDataMetadata({}, {values: {'providerId': $scope.provider}}, function(response) {
            if (response) {
                $scope.metadata.title = response.title;
                $scope.metadata.anAbstract = response.anAbstract;
                $scope.metadata.keywords = response.keywords;
                $scope.metadata.username = response.username;
                $scope.metadata.organisationName = response.organisationName;
                $scope.metadata.role = response.role;
                $scope.metadata.localeData = response.localeMetadata;
                $scope.metadata.topicCategory = response.topicCategory;
                $scope.metadata.date = response.date;
                $scope.metadata.dateType = response.dateType;
            }
        });

        $scope.selectTab = function(item) {
            if (item === 'tabiso') {
                $scope.tabiso = true;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
                $scope.tabimageinfo = false;
                $scope.tabsensorinfo = false;
            } else if (item === 'tabcrs') {
                $scope.tabiso = false;
                $scope.tabcrs = true;
                $scope.tabdesc = false;
                $scope.tabimageinfo = false;
                $scope.tabsensorinfo = false;
            } else if (item === 'tabdesc') {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = true;
                $scope.tabimageinfo = false;
                $scope.tabsensorinfo = false;
            } else if (item === 'tabimageinfo') {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
                $scope.tabimageinfo = true;
                $scope.tabsensorinfo = false;
            } else {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
                $scope.tabimageinfo = false;
                $scope.tabsensorinfo = true;
            }
        };

        $scope.addTag = function() {
            if (!$scope.tagText || $scope.tagText == '' || $scope.tagText.length == 0) {
                return;
            }

            $scope.metadata.keywords.push($scope.tagText);
            $scope.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.tagText.length == 0 &&
                key === undefined) {
                $scope.metadata.keywords.pop();
            } else if (key != undefined) {
                $scope.metadata.keywords.splice(key, 1);
            }
        };

        $scope.save = function() {
            $scope.metadata.dataName = $scope.provider;
//            $scope.metadata.dataPath = $uploadFiles.files.file;
            $scope.metadata.type = $scope.type;

            dataListing.mergeMetadata({}, $scope.metadata,
                function() {
                    $location.path('/data');
                }
            );
        };

        $scope.getCurrentLang = function() {
            return $translate.use();
        };

        $scope.createMetadataTree = function(parentDivId, isCoverageMetadata){
            var upFile = $uploadFiles.files.file;
            var upMdFile;
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            if (upFile) {
                upFile = upFile.substring(upFile.lastIndexOf("/")+1);
                upMdFile = $uploadFiles.files.mdFile;
                if (upMdFile != null) {
                    upMdFile = upMdFile.substring(upMdFile.lastIndexOf("/")+1);
                }
            }
            dataListing.loadData({}, {values: {'filePath': upFile, 'metadataFilePath': upMdFile, dataType: $scope.type}}, function(response) {
                if (isCoverageMetadata) {
                    for (var key in response.coveragesMetadata) {
                        var metadataList = response.coveragesMetadata[key].coverageMetadataTree;
                        generateMetadataTags(metadataList, parentDivId);
                    }
                } else {
                    var metadataList = response.fileMetadata;
                    generateMetadataTags(metadataList, parentDivId);
                }

                $("#"+ parentDivId +" .collapse").collapse('show');
                modalLoader.close();
            });

            function generateMetadataTags(metadataList, parentDivId) {
                if (metadataList == null) {
                    return;
                }
                for(var i=0; i<metadataList.length; i++){
                    var key = metadataList[i];
                    var name = key.name;
                    var nameWithoutWhiteSpace = key.nameNoWhiteSpace;
                    var value = key.value;
                    var childrenExist = key.childrenExist;
                    var parentNode = key.parentName;
                    var depthSpan = key.depthSpan;

                    if(childrenExist){
                        //root node
                        if(parentNode === null || parentNode == ''){
                            var htmlElement =   "<a data-toggle='collapse' data-target='#"+nameWithoutWhiteSpace+"Div' class='col-sm-"+depthSpan+"'>"+name+"</a>" +
                                "<div class='collapse col-sm-"+depthSpan+"' id='"+nameWithoutWhiteSpace+"Div'><table id='"+nameWithoutWhiteSpace+"' class='table table-striped'></table></div>";
                            jQuery("#"+ parentDivId).append(htmlElement);
                        }else{
                            var htmlElement =   "<a data-toggle='collapse' data-target='#"+nameWithoutWhiteSpace+"Div' class='col-sm-"+depthSpan+"'>"+name+"</a>" +
                                "<div class='collapse col-sm-"+depthSpan+"' id='"+nameWithoutWhiteSpace+"Div'><table id='"+nameWithoutWhiteSpace+"' class='table table-striped'></table></div>";
                            jQuery("#"+parentNode+"Div").append(htmlElement);
                        }
                    }else{
                        var htmlElement = "<tr><td>"+name+"</td><td>"+value+"</td></tr>";
                        jQuery("#"+parentNode).append(htmlElement);
                    }
                }
            };
        };

        $scope.codeLists = dataListing.codeLists({lang: $scope.getCurrentLang()});
    }]);

cstlAdminApp.controller('DataModalController', ['$scope', 'dataListing', 'webService', 'sos', 'sensor', '$dashboard', '$modalInstance', 'service', 'exclude', '$growl', '$modal',
    function ($scope, dataListing, webService, sos, sensor, $dashboard, $modalInstance, service, exclude, $growl, $modal) {
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

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.choose = function() {
            if ($scope.selected == null) {
                $growl('warning','Warning','No data selected');
                $modalInstance.dismiss('close');
                return;
            }

            if ($scope.service.type.toLowerCase() === 'sos') {
                var sensorId = ($scope.selectedSensorsChild !== null) ? $scope.selectedSensorsChild.id : $scope.selected.id;
                sos.importSensor({id: service.identifier}, {values: {"sensorId" : sensorId}}, function() {
                    $growl('success', 'Success', 'Sensor '+ sensorId +' imported in service '+ service.name);
                    $modalInstance.close();
                }, function() {
                    $growl('error', 'Error', 'Unable to import sensor '+ sensorId +' in service '+ service.name);
                    $modalInstance.dismiss('close');
                });
                return;
            }

            if ($scope.wmtsParams === false) {
                // just add the data if we are not in the case of the wmts service
                if (service.type.toLowerCase() !== 'wmts') {
                    if (service.type.toLowerCase() === 'wms' && $scope.conformPyramid) {
                        // In the case of a wms service and user asked to pyramid the data
                        dataListing.pyramidConform({providerId: $scope.selected.Provider, dataId: $scope.selected.Name}, {}, function(tiledProvider) {
                            webService.addLayer({type: service.type, id: service.identifier},
                                {layerAlias: tiledProvider.dataId, layerId: tiledProvider.dataId, serviceType: service.type, serviceId: service.identifier, providerId: tiledProvider.providerId},
                                function () {
                                    $growl('success', 'Success', 'Layer ' + tiledProvider.dataId + ' successfully added to service ' + service.name);
                                    $modalInstance.close();
                                },
                                function () {
                                    $growl('error', 'Error', 'Layer ' + tiledProvider.dataId + ' failed to be added to service ' + service.name);
                                    $modalInstance.dismiss('close');
                                }
                            );
                        } , function() {
                            $growl('error', 'Error', 'Failed to generate conform pyramid for ' + $scope.selected.Name);
                            $modalInstance.dismiss('close');
                        });
                    } else {
                        // Not in WMTS and no pyramid requested
                        var modalLoader = $modal.open({
                          templateUrl: 'views/modalLoader.html',
                          controller: 'ModalInstanceCtrl'
                        });
                        webService.addLayer({type: service.type, id: service.identifier},
                            {layerAlias: $scope.selected.Name, layerId: $scope.selected.Name, serviceType: service.type, serviceId: service.identifier, providerId: $scope.selected.Provider, layerNamespace: $scope.selected.Namespace},
                            function () {
                                $growl('success', 'Success', 'Layer ' + $scope.selected.Name + ' successfully added to service ' + service.name);
                                modalLoader.close();
                                $modalInstance.close();
                            },
                            function () {
                                $growl('error', 'Error', 'Layer ' + $scope.selected.Name + ' failed to be added to service ' + service.name);
                                $modalInstance.dismiss('close');
                            }
                        );
                    }
                    return;
                }

                // WMTS here, prepare form
                dataListing.pyramidScales({providerId: $scope.selected.Provider, dataId: $scope.selected.Name}, function(response) {
                    $scope.scales = response.Entry[0].split(',');
                }, function () {
                    $growl('error', 'Error', 'Unable to pyramid data ' + $scope.selected.Name);
                });

                $scope.wmtsParams = true;
            } else {
                // Finish the WMTS publish process
                // Pyramid the data to get the new provider to add
                dataListing.pyramidData({providerId: $scope.selected.Provider, dataId: $scope.selected.Name},
                    {tileFormat: $scope.tileFormat, crs: $scope.crs, scales: $scope.scales, upperCornerX: $scope.upperCornerX, upperCornerY: $scope.upperCornerY},
                    function(respProvider) {
                        // Add the tiled provider to the service
                        webService.addLayer({type: service.type, id: service.identifier},
                            {layerAlias: respProvider.dataId, layerId: respProvider.dataId, serviceType: service.type, serviceId: service.identifier, providerId: respProvider.providerId},
                            function () {
                                $growl('success', 'Success', 'Layer ' + respProvider.dataId + ' successfully added to service ' + service.name);
                                $modalInstance.close();
                            },
                            function () {
                                $growl('error', 'Error', 'Layer ' + respProvider.dataId + ' failed to be added to service ' + service.name);
                                $modalInstance.dismiss('close');
                            }
                        );
                    }, function() { $growl('error', 'Error', 'Pyramid process failed for ' + $scope.selected.Name); });
            }
        };
    }]);

cstlAdminApp.controller('ViewMetadataModalController', ['$scope', '$modalInstance', 'details',
    function ($scope, $modalInstance, details) {
        $scope.details = details.data;
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

    }]);
