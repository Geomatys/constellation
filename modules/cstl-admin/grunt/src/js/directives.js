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

angular.module('cstlAdminApp')

    .directive('activeMenu', ['$translate', function($translate) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs, controller) {
                var language = attrs.activeMenu;

                scope.$watch(function() {
                    return $translate.use();
                }, function(selectedLanguage) {
                    if (language === selectedLanguage) {
                        element.addClass('active');
                    } else {
                        element.removeClass('active');
                    }
                });
            }
        };
    }])

    .directive('activeLink', ['$location', function(location) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs, controller) {
                var clazz = attrs.activeLink;
                var path = attrs.href;
                path = path.substring(1); //hack because path does bot return including hashbang
                scope.location = location;
                scope.$watch('location.path()', function(newPath) {
                    if (path === newPath) {
                        element.addClass(clazz);
                    } else {
                        element.removeClass(clazz);
                    }
                });
            }
        };
    }])

    /**
     * Datepicker.
     *
     * @see http://eternicode.github.io/bootstrap-datepicker/
     */
    .directive('datepicker', function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {

                scope.$watch(attrs['datepicker'], function(newVal) {
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
                        minViewMode: newVal.minViewMode
                    });
                }
            }
        };
    })

    .directive('tagInput', function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                scope.inputWidth = 20;

                // Watch for changes in text field
                scope.$watch(attrs.ngModel, function(value) {
                    if (value != undefined) {
                        var tempEl = $('<span>' + value + '</span>').appendTo('body');
                        scope.inputWidth = tempEl.width() + 5;
                        tempEl.remove();
                    }
                });

                element.bind('keydown', function(e) {
                    if (e.which == 9) {
                        e.preventDefault();
                    }

                    if (e.which == 8) {
                        scope.$apply(attrs.deleteTag);
                    }
                });

                element.bind('keyup', function(e) {
                    var key = e.which;

                    // Tab or Enter pressed
                    if (key == 9 || key == 13) {
                        e.preventDefault();
                        scope.$apply(attrs.newTag);
                    }
                });
            }
        }
    })

    .directive('spectrum', function() {
        return {
            restrict: 'A',
            link: function(scope, $element, attrs) {
                $element.spectrum({
                    color: scope.$eval(attrs['ngModel']),
                    showInput: true,
                    allowEmpty: true,
                    showAlpha: true,
                    preferredFormat: "hex",
                    showButtons: false,
                    change: function(color) {
                        scope.$eval(attrs['oncolorchanged'] || '', { value: color });
                    }
                });

                $element.on('$destroy', function() {
                    $element.spectrum('destroy');
                });
            }
        };
    })

    .directive('tooltip', function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                scope.$watch(attrs['tooltip'], function(options) {
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
            template: '<div class="multiSelect">' +
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
                            if(newValue !== undefined){
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
                            if(toFilter[i][attrs.displayAttr] == entity[attrs.displayAttr]) {
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
    });
