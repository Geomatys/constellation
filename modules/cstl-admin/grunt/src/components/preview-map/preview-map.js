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
function previewMapDirective(previewMapOptions) {

    function previewMapLink(scope, element, attrs) {

        // Create the map instance .
        var map = new ol.Map(angular.extend(previewMapOptions(), { target: element[0] }));

        // Observe "layer" attribute reference changes.
        scope.$watch(attrs.layer, layerChanged, false);

        // Observe "extent" attribute value changes.
        scope.$watch(attrs.extent, extentChanged, false);

        // Properly destroy the map when the directive is unmounted.
        element.on('$destroy', map.setTarget.bind(map, null));


        /**
         * Displays the new layer.
         *
         * @param newLayer {ol.layer.Base} The new layer.
         * @param oldLayer {ol.layer.Base} The previous layer.
         */
        function layerChanged(newLayer, oldLayer) {
            if (oldLayer) {
                map.removeLayer(oldLayer);
            }
            if (newLayer) {
                map.addLayer(newLayer);
            }
        }

        /**
         * Zooms to the new extent.
         *
         * @param newExtent {ol.Extent} The new extent.
         */
        function extentChanged(newExtent) {
            var mapProj = map.getView().getProjection();
            if (angular.isArray(newExtent) && newExtent.length === 4) {
                newExtent = ol.proj.transformExtent(newExtent, 'CRS:84', mapProj);
                newExtent = ol.extent.getIntersection(newExtent, mapProj.getExtent());
            } else {
                newExtent = mapProj.getExtent();
            }
            map.getView().fit(newExtent, map.getSize());
        }
    }

    return {
        restrict: 'E',
        link: previewMapLink
    };
}

/**
 * Configure the preview map module.
 *
 * Use the {@link PreviewMapOptionsProvider#provide} method in your own
 * application module to override the following default configuration.
 *
 * @param previewMapOptionsProvider {PreviewMapOptionsProvider}
 */
function previewMapConfig(previewMapOptionsProvider) {

    /**
     * Creates and returns the overview map options.
     *
     * TODO → Use some configuration variables to modify some options.
     *
     * @return {Object} The overview map options.
     */
    function defaultOverviewMapOptionsFactory() {
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
                    source: new ol.source.OSM({
                        url: 'http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png',
                        attributions: [
                            new ol.Attribution({
                                html: 'Tiles courtesy of <a href="http://www.mapquest.com" target="_blank">MapQuest</a>'
                            })
                        ]
                    })
                })
            ],
            logo: false,
            view: new ol.View({
                projection: 'EPSG:3857'
            })
        };
    }

    // Configure preview map options.
    previewMapOptionsProvider.provide(defaultOverviewMapOptionsFactory);
}