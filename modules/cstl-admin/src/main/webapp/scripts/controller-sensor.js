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

cstlAdminApp.controller('SensorsController', ['$scope', '$dashboard', 'webService', 'dataListing', '$modal', '$growl',
    function ($scope, $dashboard, webService, dataListing, $modal, $growl){
    	var modalLoader = $modal.open({
          templateUrl: 'views/modalLoader.html',
          controller: 'ModalInstanceCtrl'
        });
        dataListing.listAll({}, function(response) {
            $dashboard($scope, response, true);
            $scope.filtertype = "sensor";
            modalLoader.close();
        });

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