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
angular.module('CstlAdminDep', [
    // Angular official modules.
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngAnimate',
    // Libraries modules.
    'base64',
    'hljs',
    'pascalprecht.translate',
    'rzModule',
    'angular-loading-bar',
    'ui.ace',
    'ui.bootstrap.modal',
    'ui.bootstrap.buttons',
    'ui.bootstrap.tpls-accordion',
    'ui.bootstrap.transition',
    'ui.bootstrap.collapse',
    'ui.bootstrap.accordion',
    'ui.bootstrap.tpls-popover',
    'ui.bootstrap.position',
    'ui.bootstrap.bindHtml',
    'ui.bootstrap.tooltip',
    'ui.bootstrap.popover',
    'ui.bootstrap.tpls-typeahead',
    'ui.bootstrap.typeahead',
    // Constellation modules.
    'cstl-directives',
    'cstl-restapi',
    'cstl-services',
    'cstl-admin',
    'cstl-data',
    'cstl-main',
    'cstl-mapcontext',
    'cstl-process',
    'cstl-sensor',
    'cstl-style',
    'cstl-webservice'])
    .config(['cfpLoadingBarProvider', function(cfpLoadingBarProvider) {
        cfpLoadingBarProvider.latencyThreshold = 250;
    }]);