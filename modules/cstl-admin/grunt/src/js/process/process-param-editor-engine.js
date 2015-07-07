angular.module('processParamEditorEngine', ['ng', 'cstl-services'])

    // -------------------------------------------------------------------------
    //  Provider
    // -------------------------------------------------------------------------

    .provider('processParamEditor', function ProcessParamEditorProvider() {

        var editors = {};

        var defaults = this.defaults = {
            fallback: {
                template: '<p class="form-control-static" translate="task.field.not.supported">This parameter is not supported by the web application</p>'
            }
        };

        this.put = function(type, config) {
            editors[type] = config;
            return this;
        };

        this.$get = function() {
            return {
                getConfig: function(type) {
                    return editors[type] || defaults.fallback;
                },
                hasEditor: function(type) {
                    return editors[type] ? true : false;
                }
            };
        };
    })

    // -------------------------------------------------------------------------
    //  Directive
    // -------------------------------------------------------------------------

    .directive('processParamEditor', function processParamEditorDirective($controller, $compile, processParamEditor, viewResolve) {
        return {
            restrict: 'ECA',
            link: function (scope, $element, attr) {

                // Process input parameter value changed callback.
                function parameterWatchAction(parameter) {
                    var config = processParamEditor.getConfig(parameter.binding);

                    // Use the 'viewResolve' service to analyse and resolve all promises
                    // required to display the editor content (including the HTML template
                    // itself). The process input editor configuration is strictly the same
                    // that a route configuration using the 'ngRoute' module.
                    viewResolve(config, { parameter: parameter }).then(function(locals) {

                        // Cleanup directive template. Unlike the 'ngView' directive, animations
                        // are not supported, so the directive template can be cleared immediately.
                        $element.empty();

                        if (angular.isDefined(locals.$template)) {

                            // Set the directive raw/non-compiled template. Controller must be
                            // instantiated before applying a scope on the template.
                            $element.html(locals.$template);

                            // Instantiate (and publish) controller. Same code as the 'ngView'
                            // directive, in addition variables named 'parameter' and 'valueIndex'
                            // are automatically injected in the controller instance.
                            if (angular.isDefined(config.controller)) {
                                locals.$scope = scope;
                                locals.parameter = parameter;
                                locals.valueIndex = scope.$index;
                                var controller = $controller(config.controller, locals);
                                if (angular.isString(config.controllerAs)) {
                                    scope[config.controllerAs] = controller;
                                }
                                $element.data('$ngControllerController', controller);
                                $element.children().data('$ngControllerController', controller);
                            }

                            // Compile the new template.
                            $compile($element.contents())(scope);
                        }
                    });
                }

                // Watch for process input parameter changes.
                scope.$watch(attr.processParamEditor, parameterWatchAction);
            }
        };
    });