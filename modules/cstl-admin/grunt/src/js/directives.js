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

/**
 * @require angular.js
 * @require angular-translate.js
 */
angular.module('cstl-directives', ['pascalprecht.translate'])

    // -------------------------------------------------------------------------
    //  Page Switcher
    // -------------------------------------------------------------------------

    /**
     * @see http://getbootstrap.com/components/#pagination
     */
    .directive('pageSwitcher', function() {
        return {
            // Applicable as element/attribute (prefer attribute for IE8 support).
            restrict: 'AE',

            // Declare a new child scope for this directive. Without this line
            // the directive scope will be the current scope, this may cause conflict
            // between multiple directive instances.
            scope: {
                onSelectPage: '&'
            },

            // Directive initialization. Note that there is no controller for this
            // directive because we don't need to expose an API to other directives.
            link: function(scope, element, attrs) {

                // Observe 'page-switcher' attribute changes.
                scope.$parent.$watch(attrs.pageSwitcher, function(newVal) {
                    watchAction(newVal);
                }, true);

                // Handle attributes changes.
                function watchAction(newVal) {
                    var page  = newVal.page  || 1,
                        size  = newVal.size  || 10,
                        count = newVal.count || 0;

                    // Compute pagination.
                    var totalPages = Math.ceil(count / size) || 1,
                        prevCount  = page - 1,
                        nextCount  = totalPages - page,
                        minPage    = page - Math.min(4 - Math.min(2, nextCount), prevCount),
                        maxPage    = page + Math.min(4 - Math.min(2, prevCount), nextCount);

                    // Update scope.
                    scope.totalPages = totalPages;
                    scope.page       = page;
                    scope.indexes    = [];
                    for (var i = minPage; i <= maxPage; i++) {
                        scope.indexes.push(i);
                    }
                }

                // Page select action.
                scope.selectPage = function(page) {
                    if (scope.page !== page && page > 0 && page <= scope.totalPages) {
                        scope.onSelectPage({page: page});
                    }
                };
            },

            // Replace the element with the following template.
            replace: true,

            // Component HTML template.
            template:
            '<ul class="pagination">' +
                '<li><a ng-click="selectPage(page - 1)">&laquo;</a></li>' +
                '<li ng-repeat="index in indexes" ng-class="{active: index == page}"><a ng-click="selectPage(index)">{{index}}</a></li>' +
                '<li><a ng-click="selectPage(page + 1)">&raquo;</a></li>' +
            '</ul>'
        };
    })

    // -------------------------------------------------------------------------
    //  Date Picker
    // -------------------------------------------------------------------------

    /**
     * @see http://eternicode.github.io/bootstrap-datepicker/
     */
    .directive('datepicker', function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {

                scope.$watch(attrs.datepicker, function(newVal) {
                    watchAction(newVal || {});
                }, true);

                function watchAction(newVal) {
                    element.datepicker('remove');
                    element.datepicker({
                        todayBtn:    newVal.todayBtn,
                        language:    newVal.language,
                        orientation: newVal.orientation,
                        format:      newVal.format,
                        weekStart:   newVal.weekStart,
                        viewMode:    newVal.viewMode,
                        minViewMode: newVal.minViewMode,
                        autoclose: true
                    });
                }
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Tooltip
    // -------------------------------------------------------------------------

    /**
     * @see http://getbootstrap.com/javascript/#tooltips
     */
    .directive('tooltip', function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                scope.$watch(attrs.tooltip, function(options) {
                    options = options || {};
                    element.tooltip({
                        animation: options.animation,
                        html:      options.html,
                        placement: options.placement,
                        selector:  options.selector,
                        title:     options.title,
                        trigger:   options.trigger,
                        delay:     options.delay,
                        container: options.container
                    });
                }, true);

                element.on('$destroy', function() {
                    element.tooltip('destroy');
                });
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Tag Input
    // -------------------------------------------------------------------------

    .directive('tagInput', function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                scope.inputWidth = 20;

                // Watch for changes in text field
                scope.$watch(attrs.ngModel, function(value) {
                    if (value) {
                        var tempEl = $('<span>' + value + '</span>').appendTo('body');
                        scope.inputWidth = tempEl.width() + 5;
                        tempEl.remove();
                    }
                });

                element.bind('keydown', function(e) {
                    if (e.which === 9) {
                        e.preventDefault();
                    }
                    if (e.which === 8) {
                        scope.$apply(attrs.deleteTag);
                    }
                });

                element.bind('keyup', function(e) {
                    var key = e.which;

                    // Tab or Enter pressed
                    if (key === 9 || key === 13) {
                        e.preventDefault();
                        scope.$apply(attrs.newTag);
                    }
                });
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Active Menu
    // -------------------------------------------------------------------------

    .directive('activeMenu', function($rootScope, $translate) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {

                // Language changed callback.
                function onLanguageChanged() {
                    if (attrs.activeMenu === $translate.use()) {
                        element.addClass('active');
                    } else {
                        element.removeClass('active');
                    }
                }

                // Observe language changes.
                $rootScope.$on('$translateChangeSuccess', onLanguageChanged);
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Active Link
    // -------------------------------------------------------------------------

    .directive('activeLink', function($rootScope, $location) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {

                // Location changed callback.
                function onLocationChanged() {
                    if (attrs.href.substring(1) === $location.path()) {
                        element.addClass(attrs.activeLink);
                    } else {
                        element.removeClass(attrs.activeLink);
                    }
                }

                // Observe location changes.
                $rootScope.$on('$locationChangeSuccess', onLocationChanged);
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Spectrum
    // -------------------------------------------------------------------------

    .directive('spectrum', function() {
        return {
            restrict: 'A',
            link: function(scope, $element, attrs) {
                $element.spectrum({
                    color: scope.$eval(attrs.ngModel),
                    showInput: true,
                    allowEmpty: true,
                    showAlpha: true,
                    preferredFormat: "hex8",
                    showButtons: false,
                    change: function(color) {
                        scope.$eval(attrs.oncolorchanged || '', { value: color });
                    }
                });

                $element.on('$destroy', function() {
                    $element.spectrum('destroy');
                });
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Date Time Picker
    // -------------------------------------------------------------------------

    .directive('datetimepicker', function() {
        return {
            restrict: 'A',
            link: function(scope, $element, attrs) {
                $element.datetimepicker({
                    format: 'yyyy-mm-dd hh:ii:ss',
                    autoclose: true
                });

                $element.on('$destroy', function() {
                    $element.datetimepicker('destroy');
                });
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Directive to set input name dynamically
    // -------------------------------------------------------------------------

    .directive('dynamicName', function() {
        return {
            restrict:"A",
            require: ['ngModel', '^form'],
            link:function(scope,element,attrs,ctrls){
                ctrls[0].$name = scope.$eval(attrs.dynamicName) || attrs.dynamicName;
                ctrls[1].$addControl(ctrls[0]);
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Multi Select
    // -------------------------------------------------------------------------

    .directive('multiSelect', function($q) {
        return {
            restrict: 'E',
            require: 'ngModel',
            scope: {
                selectedLabel: "@",
                availableLabel: "@",
                displayAttr: "@",
                available: "=",
                model: "=ngModel"
            },
            template:
            '<div class="multiSelect">' +
            '<div class="select">' +
            '<label class="control-label" for="multiSelectSelected">{{ selectedLabel }} ' +
            '({{ model.length }})</label>' +
            '<select id="currentRoles" ng-model="selected.current" multiple ' +
            'class="pull-left" ng-options="e as e[displayAttr] for e in model">' +
            '</select>' +
            '</div>' +
            '<div class="select buttons">' +
            '<button class="btn mover left" ng-click="add()" title="Add selected" ' +
            'ng-disabled="selected.available.length == 0">' +
            '<i class="glyphicon glyphicon-arrow-left"></i>' +
            '</button>' +
            '<button class="btn mover right" ng-click="remove()" title="Remove selected" ' +
            'ng-disabled="selected.current.length == 0">' +
            '<i class="glyphicon glyphicon-arrow-right"></i>' +
            '</button>' +
            '</div>' +
            '<div class="select">' +
            '<label class="control-label" for="multiSelectAvailable">{{ availableLabel }} ' +
            '({{ available.length }})</label>' +
            '<select id="multiSelectAvailable" ng-model="selected.available" multiple ' +
            'ng-options="e as e[displayAttr] for e in available"></select>' +
            '</div>' +
            '</div>',
            link: function(scope, elm, attrs) {
                scope.selected = {
                    available: [],
                    current: []
                };

                /* Handles cases where scope data hasn't been initialized yet */
                var dataLoading = function(scopeAttr) {
                    var loading = $q.defer();
                    if(scope[scopeAttr]) {
                        loading.resolve(scope[scopeAttr]);
                    } else {
                        scope.$watch(scopeAttr, function(newValue, oldValue) {
                            if(newValue){
                                loading.resolve(newValue);
                            }
                        });
                    }
                    return loading.promise;
                };

                /* Filters out items in original that are also in toFilter. Compares by reference. */
                var filterOut = function(original, toFilter) {
                    var filtered = [];
                    angular.forEach(original, function(entity) {
                        var match = false;
                        for(var i = 0; i < toFilter.length; i++) {
                            if(toFilter[i][attrs.displayAttr] === entity[attrs.displayAttr]) {
                                match = true;
                                break;
                            }
                        }
                        if(!match) {
                            filtered.push(entity);
                        }
                    });
                    return filtered;
                };

                scope.refreshAvailable = function() {
                    scope.available = filterOut(scope.available, scope.model);
                    scope.selected.available = [];
                    scope.selected.current = [];
                };

                scope.add = function() {
                    scope.model = scope.model.concat(scope.selected.available);
                    scope.refreshAvailable();
                };
                scope.remove = function() {
                    scope.available = scope.available.concat(scope.selected.current);
                    scope.model = filterOut(scope.model, scope.selected.current);
                    scope.refreshAvailable();
                };

                $q.all([dataLoading("model"), dataLoading("available")]).then(function(results) {
                    scope.refreshAvailable();
                });
            }
        };
    })

    // -------------------------------------------------------------------------
    //  Back to top button
    // -------------------------------------------------------------------------

    .directive("scrollTo", function($window){
        return {
            restrict : "AC",
            compile : function(){

                var document = $window.document;

                function scrollInto(idOrName) {//find element with the give id of name and scroll to the first element it finds
                    if(!idOrName){$window.scrollTo(0, 0);}
                    //check if an element can be found with id attribute
                    var el = document.getElementById(idOrName);
                    if(!el) {//check if an element can be found with name attribute if there is no such id
                        el = document.getElementsByName(idOrName);

                        if(el && el.length){
                            el = el[0];

                        }else{
                            el = null;
                        }
                    }

                    if(el) //if an element is found, scroll to the element
                    {el.scrollIntoView();}
                    //otherwise, ignore
                }

                return function(scope, element, attr) {
                    element.bind("click", function(event){
                        scrollInto(attr.scrollTo);
                    });
                };
            }
        };
    })

    /**
     * AngularJS fixed header scrollable table directive
     * Git : https://github.com/cornflourblue/angu-fixed-header-table/blob/master/angu-fixed-header-table.js
     * @author Jason Watmore <jason@pointblankdevelopment.com.au> (http://jasonwatmore.com)
     * @version 1.0.0
     */
    .directive('fixedHeader', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            scope: {
                tableHeight: '@',
                model: '=ngModel'
            },
            link: function ($scope, $elem, $attrs, $ctrl) {
                function isVisible(el) {
                    var style = window.getComputedStyle(el);
                    return (style.display !== 'none' && el.offsetWidth !== 0 );
                }

                function isTableReady() {
                    return isVisible(elem.querySelector("tbody")) && elem.querySelector('tbody tr:first-child') != null && $scope.model;
                }

                var elem = $elem[0];
                // wait for content to load into table and to have at least one row,
                // tdElems could be empty at the time of execution if td are created asynchronously (eg ng-repeat with promise)
                var unbindWatch = $scope.$watch(isTableReady,
                    function (newValue, oldValue) {
                        if (newValue !== oldValue) {
                            // reset display styles so column widths are correct when measured below
                            angular.element(elem.querySelectorAll('thead, tbody, tfoot')).css('display', '');

                            // wrap in $timeout to give table a chance to finish rendering
                            $timeout(function () {
                                // set widths of columns
                                angular.forEach(elem.querySelectorAll('tr:first-child th'), function (thElem, i) {

                                    var tdElems = elem.querySelector('tbody tr:first-child td:nth-child(' + (i + 1) + ')');
                                    var tfElems = elem.querySelector('tfoot tr:first-child td:nth-child(' + (i + 1) + ')');

                                    var columnWidth = tdElems ? tdElems.offsetWidth : thElem.offsetWidth;
                                    if(tdElems) {
                                        tdElems.style.width = columnWidth + 'px';
                                    }
                                    if(thElem) {
                                        thElem.style.width = columnWidth + 'px';
                                    }
                                    if (tfElems) {
                                        tfElems.style.width = columnWidth + 'px';
                                    }
                                });

                                // set css styles on thead and tbody
                                angular.element(elem.querySelectorAll('thead, tfoot')).css('display', 'block');

                                angular.element(elem.querySelectorAll('tbody')).css({
                                    'display': 'block',
                                    'max-height': $scope.tableHeight || 'inherit',
                                    'overflow': 'auto'
                                });

                                // reduce width of last column by width of scrollbar
                                var tbody = elem.querySelector('tbody');
                                var scrollBarWidth = tbody.offsetWidth - tbody.clientWidth;
                                if (scrollBarWidth > 0) {
                                    // for some reason trimming the width by 2px lines everything up better
                                    scrollBarWidth -= 2;
                                    var lastColumn = elem.querySelector('tbody tr:first-child td:last-child');
                                    lastColumn.style.width = (lastColumn.offsetWidth - scrollBarWidth) + 'px';
                                }
                            });

                            //we only need to watch once
                            //unbindWatch();
                        }
                    });
            }
        };
    }])


/**
 * Custom directive for hightlight js that fix conflicts with source directive of angular-highlightjs library.
 * Code inspired from http://stackoverflow.com/a/25583090/1284121
 */
    .directive('highlight', function(){
        return {
            restrict: 'EA',
            link: function (scope, tElem, tAttrs) {
                tElem.html('<pre><code class="hljs"></code></pre>');
                var codeElem = tElem.find('code');

                scope.$watch(tAttrs.source, function(value) {
                    var highlightAuto = hljs.highlightAuto(hljs.fixMarkup(value));
                    codeElem.html(highlightAuto.value);
                    codeElem.addClass(highlightAuto.language);
                });
            }
        };
    });
