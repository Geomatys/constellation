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

/* Controllers */

cstlIndexApp.controller('HeaderController', [ '$scope', '$http',
    '$cookies', function($scope, $http, $cookies) {
      $http.get("app/conf").success(function(data) {
        $cookies.cstlUrl=data.cstl;
        $scope.cstlLoginUrl = data.cstl + "spring/auth/form";
        $scope.cstlLogoutUrl = data.cstl + "logout";
      });
    } ]);

cstlIndexApp.controller('MainController', [ '$scope', '$location',
    function($scope, $location) {
    } ]);

cstlIndexApp.controller('LanguageController', [ '$scope', '$translate',
    function($scope, $translate) {
      $scope.changeLanguage = function(languageKey) {
        $translate.use(languageKey);
      };
    } ]);

cstlIndexApp.controller('MenuController', [ '$scope', function($scope) {

} ]);
