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

angular.module('cstl-style-dashboard', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('StylesController', function($scope, Dashboard, style, Growl, StyleSharedService, $modal, $window) {
        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.hideScroll = true;

        $scope.init = function() {
            style.listAll({provider: 'sld'},function(response) {//success
                    Dashboard($scope, response.styles, true);
                    $scope.wrap.filtertype = "";
                    $scope.wrap.ordertype = "Name";
                    $scope.wrap.filtertext='';
                    $scope.wrap.orderreverse=false;
                },function() {//error
                    Growl('error','Error','Unable to show styles list!');
                }
            );
            angular.element($window).bind("scroll", function() {
                if (this.pageYOffset < 220) {
                    $scope.hideScroll = true;
                } else {
                    $scope.hideScroll = false;
                }
                $scope.$apply();
            });
        };

        /**
         * Reset filters for dashboard
         */
        $scope.resetFilters = function(){
            style.listAll({provider: 'sld'},function(response) {//success
                    Dashboard($scope, response.styles, true);
                    $scope.wrap.filtertype = "";
                    $scope.wrap.ordertype = "Name";
                    $scope.wrap.filtertext='';
                    $scope.wrap.orderreverse=false;
                },function() {//error
                    Growl('error','Error','Unable to show styles list!');
                }
            );
        };

        /**
         * Proceed to remove the selected styles from dashboard.
         */
        $scope.deleteStyle = function() {
            if (confirm("Are you sure?")) {
                var styleName = $scope.selected.Name;
                var providerId = $scope.selected.Provider;
                style.delete({provider: providerId, name: styleName}, {},
                    function() {
                        Growl('success', 'Success', 'Style ' + styleName + ' successfully deleted');
                        style.listAll({provider: 'sld'}, function(response) {
                            Dashboard($scope, response.styles, true);
                            $scope.selected=null;
                        });
                    },
                    function() {
                        Growl('error', 'Error', 'Style ' + styleName + ' deletion failed');
                    });
            }
        };

        /**
         * Proceed to open modal SLD editor to edit the selected style
         */
        $scope.editStyle = function() {
            var styleName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            style.get({provider: providerId, name: styleName}, function(response) {
                StyleSharedService.showStyleEdit($scope, response);
            });
        };

        /**
         * Toggle up and down the selected item
         */
        $scope.toggleUpDownSelected = function() {
            var $header = $('#stylesDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('fa-chevron-down fa-chevron-up');
        };

        /**
         * Open sld editor modal to create a new style.
         */
        $scope.showStyleCreate = function() {
            StyleSharedService.showStyleCreate($scope);
        };

        $scope.truncate = function(small, text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (small === true && text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else if (small === false && text.length > 60) {
                        return text.substr(0, 60) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (small === true && text.length > 15) {
                        return text.substr(0, 15) + "...";
                    } else if (small === false && text.length > 45) {
                        return text.substr(0, 45) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 32) {
                        return text.substr(0, 32) + "...";
                    } else {return text;}
                }
            }
        };
        $scope.truncateTitleBlock = function(text){
            if(text) {
                if (window.innerWidth >= 1200) {
                    if (text.length > 40) {
                        return text.substr(0, 40) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 1200 && window.innerWidth >= 992) {
                    if (text.length > 30) {
                        return text.substr(0, 30) + "...";
                    } else {return text;}
                } else if (window.innerWidth < 992) {
                    if (text.length > 20) {
                        return text.substr(0, 20) + "...";
                    } else {return text;}
                }
            }
        };
    });