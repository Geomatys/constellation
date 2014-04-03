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


cstlAdminApp.controller('StylesController', ['$scope', '$dashboard', 'style', '$growl', 'StyleSharedService',
    function ($scope, $dashboard, style, $growl, StyleSharedService) {
        $scope.filtertype = "";

        style.listAll({}, function(response) {
            $dashboard($scope, response.styles, false);
        });

        $scope.deleteStyle = function() {
            if (confirm("Are you sure?")) {
                var styleName = $scope.selected.Name;
                var providerId = $scope.selected.Provider;
                style.delete({providerid: providerId, name: styleName}, {},
                    function() { $growl('success','Success','Style '+ styleName +' successfully deleted');
                        style.listAll({}, function(response) {
                            $scope.fullList = response.styles;
                        });
                    },
                    function() { $growl('error','Error','Style '+ styleName +' deletion failed'); });
            }
        };

        $scope.editStyle = function() {
            var styleName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;

        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#stylesDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        // Style methods
        $scope.showStyleCreate = function() {
            StyleSharedService.showStyleCreate($scope);
        };
    }]);

cstlAdminApp.controller('StyleModalController', ['$scope', '$dashboard', '$modalInstance', 'style', 'exclude', 'layerName', 'providerId', 'serviceName', 'dataType', '$cookies', 'dataListing',
    function ($scope, $dashboard, $modalInstance, style, exclude, layerName, providerId, serviceName, dataType, $cookies, dataListing) {
        $scope.exclude = exclude;
        $scope.layerName = layerName;
        $scope.providerId = providerId;
        $scope.serviceName = serviceName;
        $scope.dataType = dataType;

        $scope.filtertype = "";
        // Page to include
        $scope.pageSld = ($scope.dataType === 'VECTOR' || $scope.dataType === 'feature-store') ? "views/style/vectors.html" : "views/style/raster.html";

        $scope.stylechooser = 'new';

        $scope.setStyleChooser = function(choice){
            $scope.stylechooser = choice;
        };

        $scope.isSelected= function(choice) {
            return choice === $scope.stylechooser;
        };

        $scope.initScopeStyle = function() {
            style.listAll({}, function(response) {
                $dashboard($scope, response.styles, false);
            });
        };

        $scope.initScopeData = function() {
            dataListing.listAll({}, function(response) {
                $dashboard($scope, response, true);
            });
        };

        $scope.ok = function() {
            $modalInstance.close($scope.selected);
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.chooseDataClicked = function(data) {
            $scope.layerName = data.Name;
            $scope.providerId = data.Provider;
            $scope.dataType = data.Type;
            $scope.pageSld = ($scope.dataType === 'VECTOR' || $scope.dataType === 'feature-store') ? "views/style/vectors.html" : "views/style/raster.html";
        };

        $scope.showLayerWithStyle = function(style) {

            var layerName = $scope.layerName;
            var layerData;
            if (serviceName) {
                layerData = DataViewer.createLayerWMSWithStyle($cookies.cstlUrl, layerName, $scope.serviceName, style);
            } else {
                layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, layerName, providerId, style);
            }
            var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('styledMapOL');
        };

        $scope.StyleisSelected = function(){
            if ($scope.selected != null){
                $scope.showLayerWithStyle($scope.selected.Name);
                return true
            } else {
                return false
            }

        };

    }]);
