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

cstlAdminApp.controller('DataController', ['$scope', '$location', '$dashboard', 'webService', 'dataListing', 'provider', 'style', '$modal', '$growl', 'StyleSharedService', '$cookies',
    function ($scope, $location, $dashboard, webService, dataListing, provider, style, $modal, $growl, StyleSharedService, $cookies) {
        var modalLoader = $modal.open({
          templateUrl: 'views/modalLoader.html',
          controller: 'ModalInstanceCtrl'
        });
        dataListing.listAll({}, function(response) {
            $dashboard($scope, response, true);
            $scope.filtertype = "";
            modalLoader.close();
        });

        // Map methods
        $scope.showData = function() {
            $('#viewerData').modal("show");
            var layerName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            var layerData;
            if ($scope.selected.TargetStyle && $scope.selected.TargetStyle.length > 0) {
                layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, layerName, providerId, $scope.selected.TargetStyle[0].Name);
            } else {
                layerData = DataViewer.createLayer($cookies.cstlUrl, layerName, providerId);
            }
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];

            dataListing.metadata({providerId: providerId, dataId: layerName}, {}, function(response) {
                // Success getting the metadata, try to find the data extent
                DataViewer.initMap('dataMap');
                var ident = response['gmd.MD_Metadata']['gmd.identificationInfo'];
                if (ident) {
                    var extentMD = ident['gmd.MD_DataIdentification']['gmd.extent'];
                    if (extentMD) {
                        var bbox = extentMD['gmd.EX_Extent']['gmd.geographicElement']['gmd.EX_GeographicBoundingBox'];
                        var extent = new OpenLayers.Bounds(bbox['gmd.westBoundLongitude']['gco.Decimal'], bbox['gmd.southBoundLatitude']['gco.Decimal'],
                            bbox['gmd.eastBoundLongitude']['gco.Decimal'], bbox['gmd.northBoundLatitude']['gco.Decimal']);
                        DataViewer.map.zoomToExtent(extent, true);
                    }
                }
            }, function() {
                // failed to find a metadata, just load the full map
                DataViewer.initMap('dataMap');
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

                dataListing.hideData({providerid: providerId, dataid: layerName}, {},
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




        // Style methods
        $scope.showStyleList = function() {
            StyleSharedService.showStyleList($scope);
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style);
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#dataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        // Data loading
        $scope.showLocalFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalLocalFile.html',
                controller: 'LocalFileModalController'
            });

            modal.result.then(function(result) {
                dataListing.setMetadata({}, {values: {'providerId': result.file, 'dataType': result.type}}, function() {
                    $location.path('/description/'+ result.type +"/"+ result.file +"/"+ result.missing);
                }, function() { $growl('error','Error','Unable to save metadata'); });
            });
        };

        $scope.showServerFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalServerFile.html',
                controller: 'ServerFileModalController'
            });
        };
    }]);

cstlAdminApp.controller('DescriptionController', ['$scope', '$routeParams','dataListing','$location', '$translate', '$uploadFiles',
    function ($scope, $routeParams, dataListing, $location, $translate, $uploadFiles) {
        $scope.provider = $routeParams.id;
        $scope.missing = $routeParams.missing === 'true';
        $scope.type = $routeParams.type;

        $scope.tabiso = $scope.type==='vector' && $scope.missing;
        $scope.tabcrs = false;
        $scope.tabdesc = $scope.type==='vector' && !$scope.missing;
        $scope.tabimageinfo = $scope.type==='raster';

        $scope.metadata = {};
        $scope.metadata.keywords = [];

        $scope.selectTab = function(item) {
            if (item === 'tabiso') {
                $scope.tabiso = true;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
                $scope.tabimageinfo = false;
            } else if (item === 'tabcrs') {
                $scope.tabiso = false;
                $scope.tabcrs = true;
                $scope.tabdesc = false;
                $scope.tabimageinfo = false;
            } else if (item === 'tabdesc') {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = true;
                $scope.tabimageinfo = false;
            } else {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
                $scope.tabimageinfo = true;
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
            return $translate.uses();
        };

        $scope.createMetadataTree = function(parentDivId, isCoverageMetadata){
            var upFile = $uploadFiles.files.file;
            var upMdFile;
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

cstlAdminApp.controller('LocalFileModalController', ['$scope', '$dashboard', '$modalInstance', '$growl', 'provider', 'dataListing', '$uploadFiles', '$cookies',
    function ($scope, $dashboard, $modalInstance, $growl, provider, dataListing, $uploadFiles, $cookies) {
        $scope.layer = null;
        $scope.data = null;
        $scope.providerId = null;
        $scope.metadata = null;
        $scope.uploadType = null;

        // Handle upload workflow
        $scope.step1A = true;
        $scope.step1B = false;
        $scope.step2 = false;
        $scope.step3 = false;
        $scope.allowSubmit = false;
        $scope.allowNext = false;

        $scope.dataPath = null;
        $scope.mdPath = null;

        $scope.next = function() {
            if ($scope.step1A === true) {
                $scope.uploadData();
                $scope.step1B = true;
                $scope.allowNext = true;
                $scope.step1A = false;
            } else {
                if ($scope.metadata) {
                    $scope.uploadMetadata();
                }
                $scope.step1B = false;
                $scope.allowNext = false;

                if ($scope.uploadType == null) {
                    $scope.step2 = true;
                    $scope.allowSubmit = true;
                } else {
                    $scope.uploaded();
                }
            }
        };

        $scope.close = function() {
            if ($scope.step3) {
                $modalInstance.close({type: $scope.uploadType, file: $scope.providerId, missing: $scope.metadata == null});
            } else {
                $modalInstance.dismiss('close');
            }
        };

        $scope.verifyExtension = function() {
            var lastPointIndex = $scope.data.lastIndexOf(".");
            var extension = $scope.data.substring(lastPointIndex+1, $scope.data.length);
            dataListing.extension({}, {value: extension},
                function(response) {
                    if (response.dataType!="") {
                        $scope.uploadType = response.dataType;
                    }
                    $scope.allowNext = true;
                });
        };

        $scope.uploadData = function() {
            var $form = $('#uploadDataForm');

            var formData = new FormData($form[0]);

            $.ajax({
                url: $cookies.cstlUrl + "api/1/data/upload/data;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function (returndata) {
                    $scope.dataPath = returndata;
                    $scope.allowNext = true;
                }
            });
        };

        $scope.uploadMetadata = function() {
            var $form = $('#uploadMetadataForm');

            var formData = new FormData($form[0]);

            $.ajax({
                url: $cookies.cstlUrl + "api/1/data/upload/metadata;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function(mdPath) {
                    $scope.mdPath = mdPath;
                }
            });
        };

        $scope.uploaded = function() {
            if ($scope.dataPath && $scope.dataPath.indexOf('failed') === -1) {
                var upFile = $scope.dataPath;
                var upMdFile = null;
                if ($scope.mdPath && $scope.mdPath.indexOf('failed') === -1) {
                    upMdFile = $scope.mdPath;
                }

                // Stores uploaded files in session for further use
                var upFiles = $uploadFiles.files;
                upFiles.file = upFile;
                upFiles.mdFile = upMdFile;

                var justFile = upFile.substring(upFile.lastIndexOf("/")+1);
                var fileName = justFile;
                var fileExtension;
                if (fileName.indexOf(".") !== -1) {
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    fileExtension = justFile.substring(justFile.lastIndexOf(".")+1);
                }

                dataListing.importData({values: {'filePath': upFiles.file, 'metadataFilePath': upFiles.mdFile, dataType: $scope.type}}, function(response) {

                    var importedData = response.dataFile;
                    var importedMetaData = response.metadataFile;

                    // Store the providerId for further calls
                    $scope.providerId = fileName;
                    if ($scope.uploadType === "vector") {
                        provider.create({
                            id: fileName
                        }, {
                            type: "feature-store",
                            subType: "shapefile",
                            parameters: {
                                path: importedData
                            }
                        }, function() {
                            if (importedMetaData) {
                                dataListing.setUpMetadata({values: {'providerId': $scope.providerId, 'mdPath': importedMetaData}});
                            }

                            $growl('success','Success','Shapefile data '+ fileName +' successfully added');
                            $modalInstance.close({type: "vector", file: fileName, missing: $scope.metadata == null});
                        });
                    } else if ($scope.uploadType === "raster") {
                        provider.create({
                            id: fileName
                        }, {
                            type: "coverage-store",
                            subType: "coverage-file",
                            parameters: {
                                path: importedData
                            }
                        }, function() {
                            if (importedMetaData) {
                                dataListing.setUpMetadata({values: {'providerId': $scope.providerId, 'mdPath': importedMetaData}});
                            }

                            if (!fileExtension || fileExtension !== "nc") {
                                $growl('success','Success','Coverage data '+ fileName +' successfully added');
                                $modalInstance.close({type: "raster", file: fileName, missing: $scope.metadata == null});
                            } else {
                                displayNetCDF(fileName);
                            }
                        });
                    } else {
                        $growl('warning','Warning','Not implemented choice');
                        $modalInstance.close();
                    }
                });

            } else {
                $growl('error','Error','Data import failed');
                $modalInstance.close();
            }
        };

        function displayNetCDF(providerId) {
            $scope.step1A = false;
            $scope.step1B = false;
            $scope.step2 = false;
            $scope.step3 = true;
            $scope.allowNext = false;
            $scope.allowSubmit = false;

            $scope.coveragesData = dataListing.listCoverage({}, {value: providerId}, function(response) {
                for (var key in response.values) {
                    $scope.displayLayer(response.values[key]);
                    break;
                }
            });
        };

        $scope.displayLayer = function(layer) {
            $scope.layer = layer;
            var layerData = DataViewer.createLayer($cookies.cstlUrl, layer, $scope.providerId);
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataPreviewMap');
        };
    }]);

cstlAdminApp.controller('ServerFileModalController', ['$scope', '$dashboard', '$modalInstance', '$growl', 'dataListing', 'provider', '$cookies',
    function ($scope, $dashboard, $modalInstance, $growl, dataListing, provider, $cookies) {
        $scope.columns = [];
        // current path chosen in server data dir
        $scope.currentPath = '/';
        // path of the server data dir
        $scope.prefixPath = '';
        $scope.finished = false;
        $scope.hasSelectedSomething = false;
        $scope.layer = '';
        $scope.chooseType = false;
        $scope.dataType = 'vector';

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.load = function(path){
            $scope.currentPath = path;
            if (path === '/') {
                path = "root";
            }
            $scope.columns.push(dataListing.dataFolder({}, path));
        };

        $scope.open = function(path, depth) {
            if (depth < $scope.columns.length) {
                $scope.columns.splice(depth + 1, $scope.columns.length - depth);
            }
            $scope.load(path);
        };

        $scope.chooseFile = function(path, depth) {
            if (depth < $scope.columns.length) {
                $scope.columns.splice(depth + 1, $scope.columns.length - depth);
            }
            $scope.currentPath = path;
        };

        $scope.select = function(item,depth) {
            $scope.prefixPath = item.prefixPath;
            $scope.hasSelectedSomething = true;
            if (item.folder) {
                $scope.open(item.subPath, depth);
            } else {
                $scope.chooseFile(item.subPath, depth);
            }
        };

        $scope.startWith = function(path) {
            return $scope.currentPath.indexOf(path) === 0;
        };

        $scope.ok = function() {
            $scope.finished = true;
            $scope.loadData();
        };

        $scope.userChooseType = function() {
            $scope.finished = true;
            $scope.loadDataWithKnownExtension($scope.providerId, $scope.dataType);
        };

        $scope.loadData = function() {
            var file = $scope.currentPath.substring($scope.currentPath.lastIndexOf("/")+1);
            var fileName = file;
            var fileExtension;
            if (file.indexOf(".") !== -1) {
                fileName = file.substring(0, file.lastIndexOf("."));
                fileExtension = file.substring(file.lastIndexOf(".")+1);
            }
            $scope.providerId = fileName;

            // test extension type
            dataListing.extension({}, {value: fileExtension},
                function(response) {
                    $scope.dataType = response.dataType;
                    if ($scope.dataType === "") {
                        $scope.chooseType = true;
                    } else {
                        $scope.loadDataWithKnownExtension(fileName, fileExtension);
                    }
                }, function() {
                    // failure here, impossible to know the extension
                    $scope.chooseType = true;
                }
            );
        };

        $scope.loadDataWithKnownExtension = function(fileName, fileExtension) {
            if ($scope.dataType === "vector") {
                provider.create({
                    id: fileName
                }, {
                    type: "feature-store",
                    subType: "shapefile",
                    parameters: {
                        path: $scope.prefixPath + $scope.currentPath
                    }
                });
                $growl('success','Success','Shapefile data '+ fileName +' successfully added');
                $modalInstance.close();
            } else if ($scope.dataType === "raster") {
                provider.create({
                    id: fileName
                }, {
                    type: "coverage-store",
                    subType: "coverage-file",
                    parameters: {
                        path: $scope.prefixPath + $scope.currentPath
                    }
                }, function() {
                    if (!fileExtension || fileExtension !== "nc") {
                        dataListing.pyramidData({id: fileName}, {value: $scope.prefixPath + $scope.currentPath}, function() {
                            $growl('success','Success','Coverage data '+ fileName +' successfully added');
                            $modalInstance.dismiss('close');
                        });
                    } else {
                        $scope.displayNetCDF(fileName);
                    }
                });
            } else {
                $growl('warning','Warning','Not implemented choice');
                $modalInstance.close();
            }
        };

        $scope.displayNetCDF = function(providerId) {
            $scope.coveragesData = dataListing.listCoverage({}, {value: providerId}, function(response) {
                for (var key in response.values) {
                    $scope.displayLayer(response.values[key]);
                    break;
                }
            });
        };

        $scope.displayLayer = function(layer) {
            $scope.layer = layer;
            var layerData = DataViewer.createLayer($cookies.cstlUrl, layer, $scope.providerId);
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataServerMap');
        };

        $scope.load($scope.currentPath);
    }]);

cstlAdminApp.controller('DataModalController', ['$scope', 'dataListing', 'webService', '$dashboard', '$modalInstance', 'service', 'exclude', '$growl',
    function ($scope, dataListing, webService, $dashboard, $modalInstance, service, exclude, $growl) {
        $scope.service = service;

        $scope.getDefaultFilter = function() {
            if (service.type.toLowerCase() === 'wms') {
                return '';
            }
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
        $scope.data = undefined;
        $scope.tileFormat = undefined;
        $scope.crs = undefined;
        $scope.scales = [];
        $scope.upperCornerX = undefined;
        $scope.upperCornerY = undefined;
        $scope.conformPyramid = undefined;

        dataListing.listAll({}, function(response) {
            $dashboard($scope, response, true);
            $scope.filtertype = $scope.getDefaultFilter();
        });

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.choose = function(data) {
            if (data == null) {
                $growl('warning','Warning','No layer selected');
                $modalInstance.dismiss('close');
            } else {
                if ($scope.wmtsParams === false) {
                    // just add the data if we are not in the case of the wmts service
                    if (service.type.toLowerCase() !== 'wmts') {
                        if (service.type.toLowerCase() === 'wms' && $scope.conformPyramid) {
                            // In the case of a wms service and user asked to pyramid the data
                            dataListing.pyramidConform({providerId: data.Provider, dataId: data.Name}, {}, function(tiledProvider) {
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
                                $growl('error', 'Error', 'Failed to generate conform pyramid for ' + data.Name);
                                $modalInstance.dismiss('close');
                            });
                        } else {
                            // Not in WMTS and no pyramid requested
                            webService.addLayer({type: service.type, id: service.identifier},
                                {layerAlias: data.Name, layerId: data.Name, serviceType: service.type, serviceId: service.identifier, providerId: data.Provider},
                                function () {
                                    $growl('success', 'Success', 'Layer ' + data.Name + ' successfully added to service ' + service.name);
                                    $modalInstance.close();
                                },
                                function () {
                                    $growl('error', 'Error', 'Layer ' + data.Name + ' failed to be added to service ' + service.name);
                                    $modalInstance.dismiss('close');
                                }
                            );
                        }
                        return;
                    }

                    // WMTS here, prepare form
                    dataListing.pyramidScales({providerId: data.Provider, dataId: data.Name}, function(response) {
                        $scope.scales = response.Entry[0].split(',');
                    }, function () {
                        $growl('error', 'Error', 'Unable to pyramid data ' + data.Name);
                    });

                    $scope.wmtsParams = true;
                    // Stores the data for further click on the same choose button in the next form
                    $scope.data = data;
                } else {
                    // Finish the WMTS publish process
                    // Pyramid the data to get the new provider to add
                    dataListing.pyramidData({providerId: $scope.data.Provider, dataId: $scope.data.Name},
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
                        }, function() { $growl('error', 'Error', 'Pyramid process failed for ' + $scope.data.Name); });
                }
            }
        };
    }]);
