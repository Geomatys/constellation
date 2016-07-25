/**
 * @namespace cstl.components.previewMap
 * @requires ng
 */
angular.module('cstl.components.previewMap', ['ng'])
    .provider('previewMapOptions', PreviewMapOptionsProvider)
    .directive('previewMap', previewMapDirective)
    .config(previewMapConfig);

/**
 * @ngdoc provider
 * @name PreviewMapOptionsProvider
 *
 * @description
 * Used for configuring preview map options.
 *
 * @this PreviewMapOptionsProvider
 */
function PreviewMapOptionsProvider() {

    var options;

    /**
     * @ngdoc method
     * @ngdoc PreviewMapOptionsProvider#provide
     *
     * @description
     * Set options for preview map.
     *
     * @param {*} value The preview map options.
     */
    this.provide = function(value) {
        options = value;
    };

    this.$get = ['$injector', function($injector) {

        /**
         * @ngdoc service
         * @name previewMapOptions
         *
         * @description
         * Used for acquiring configured preview map options.
         */
        function previewMapOptions() {
            if (angular.isString(options)) {
                return $injector.get(options);
            } else if (angular.isArray(options) || angular.isFunction(options)) {
                return $injector.invoke(options);
            }
            return options || {};
        }

        return previewMapOptions;
    }];
}

/**
 * @ngdoc directive
 *
 * @description
 * The preview map component.
 *
 * @param previewMapOptions {Function}
 * @return {Object} The directive configuration object.
 */
function previewMapDirective($timeout, previewMapOptions) {

    function previewMapLink(scope, element, attrs) {

        // Create the map instance .
        var map = new ol.Map(angular.extend(previewMapOptions(), { target: element[0] }));


        // Observe "layer" attribute reference changes.
        scope.$watch(attrs.layer, layerChanged);

        // Observe "layer-only" attribute reference changes.
        scope.$watch(attrs.layerOnly, layerOnlyChanged);

        // Observe "projection" attribute reference changes.
        scope.$watch(attrs.projection, projectionChanged);

        // Observe "extent" attribute reference changes.
        scope.$watch(attrs.extent, extentChanged);

        // Properly destroy the map when the directive is unmounted.
        element.on('$destroy', map.setTarget.bind(map, null));


        /**
         * Displays the new layer.
         *
         * @param newValue {ol.layer.Base} The new layer.
         * @param oldValue {ol.layer.Base} The previous layer.
         */
        function layerChanged(newValue, oldValue) {
            if (oldValue) {
                map.removeLayer(oldValue);
            }
            if (newValue) {
                map.addLayer(newValue);
            }
        }

        /**
         * Toggles visibility of other layers.
         *
         * @param newValue {Boolean} The new value.
         */
        function layerOnlyChanged(newValue) {
            var currLayer = scope.$eval(attrs.layer);
            map.getLayers().forEach(function(layer) {
                layer.setVisible(!newValue || layer === currLayer);
            });
        }

        /**
         * Updates the map projection.
         *
         * @param newValue {ol.proj.ProjectionLike} The previous layer.
         */
        function projectionChanged(newValue) {
            var currView = map.getView(),
                currProjection = currView.getProjection(),
                newProjection = ol.proj.get(newValue);

            if (newProjection && newProjection !== currProjection) {
                map.setView(new ol.View({
                    projection: newValue,
                    rotation: currView.getRotation()
                }));
                extentChanged(scope.$eval(attrs.extent));
            }
        }

        /**
         * Zooms to the new extent.
         *
         * @param newValue {ol.Extent} The new extent.
         */
        function extentChanged(newValue) {
            var mapProj = map.getView().getProjection();
            if (angular.isArray(newValue) && newValue.length === 4) {
                // TODO → Move this code to an utility service.
                var crs84Proj = ol.proj.get('CRS:84');
                newValue = ol.extent.getIntersection(newValue, crs84Proj.getExtent());
                newValue = ol.proj.transformExtent(newValue, crs84Proj, mapProj);
                newValue = ol.extent.getIntersection(newValue, mapProj.getExtent());
            } else {
                newValue = mapProj.getExtent();
            }
            map.getView().fit(newValue, map.getSize());
        }
    }

    return {
        restrict: 'E',
        link: previewMapLink
    };
}

/**
 * Configures the preview map module.
 *
 * Use the {@link PreviewMapOptionsProvider#provide} method in your own
 * application module to override the following default configuration.
 *
 * @param previewMapOptionsProvider {PreviewMapOptionsProvider}
 */
function previewMapConfig(previewMapOptionsProvider) {

    /**
     * Creates and returns the preview map options.
     *
     * TODO → Use some configuration variables to modify these options.
     *
     * @return {Object} The preview map options.
     */
    function defaultPreviewMapOptionsFactory() {
        return {
            controls: [
                new ol.control.ScaleLine(),
                new ol.control.Zoom(),
                new ol.control.FullScreen(),
                new ol.control.Rotate(),
                new ol.control.Attribution()
            ],
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.OSM()
                })
            ],
            logo: false,
            view: new ol.View({
                projection: 'EPSG:3857'
            })
        };
    }

    // Configure preview map options.
    previewMapOptionsProvider.provide(defaultPreviewMapOptionsFactory);
}