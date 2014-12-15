/*
 * angular-ui-bootstrap
 * http://angular-ui.github.io/bootstrap/

 * Version: 0.12.0 - 2014-11-16
 * License: MIT
 */
angular.module("ui.bootstrap", ["ui.bootstrap.tpls-progressbar","ui.bootstrap.progressbar"]);
angular.module("ui.bootstrap.tpls-progressbar", ["template/progressbar/bar.html","template/progressbar/progress.html","template/progressbar/progressbar.html"]);
angular.module('ui.bootstrap.progressbar', [])

    .constant('progressConfig', {
        animate: true,
        max: 100
    })

    .controller('ProgressController', ['$scope', '$attrs', 'progressConfig', function($scope, $attrs, progressConfig) {
        var self = this,
            animate = angular.isDefined($attrs.animate) ? $scope.$parent.$eval($attrs.animate) : progressConfig.animate;

        this.bars = [];
        $scope.max = angular.isDefined($attrs.max) ? $scope.$parent.$eval($attrs.max) : progressConfig.max;

        this.addBar = function(bar, element) {
            if ( !animate ) {
                element.css({'transition': 'none'});
            }

            this.bars.push(bar);

            bar.$watch('value', function( value ) {
                bar.percent = +(100 * value / $scope.max).toFixed(2);
            });

            bar.$on('$destroy', function() {
                element = null;
                self.removeBar(bar);
            });
        };

        this.removeBar = function(bar) {
            this.bars.splice(this.bars.indexOf(bar), 1);
        };
    }])

    .directive('progress', function() {
        return {
            restrict: 'EA',
            replace: true,
            transclude: true,
            controller: 'ProgressController',
            require: 'progress',
            scope: {},
            templateUrl: 'template/progressbar/progress.html'
        };
    })

    .directive('bar', function() {
        return {
            restrict: 'EA',
            replace: true,
            transclude: true,
            require: '^progress',
            scope: {
                value: '=',
                type: '@'
            },
            templateUrl: 'template/progressbar/bar.html',
            link: function(scope, element, attrs, progressCtrl) {
                progressCtrl.addBar(scope, element);
            }
        };
    })

    .directive('progressbar', function() {
        return {
            restrict: 'EA',
            replace: true,
            transclude: true,
            controller: 'ProgressController',
            scope: {
                value: '=',
                type: '@'
            },
            templateUrl: 'template/progressbar/progressbar.html',
            link: function(scope, element, attrs, progressCtrl) {
                progressCtrl.addBar(scope, angular.element(element.children()[0]));
            }
        };
    });
angular.module("template/progressbar/bar.html", []).run(["$templateCache", function($templateCache) {
    $templateCache.put("template/progressbar/bar.html",
        "<div class=\"progress-bar\" ng-class=\"type && 'progress-bar-' + type\" role=\"progressbar\" aria-valuenow=\"{{value}}\" aria-valuemin=\"0\" aria-valuemax=\"{{max}}\" ng-style=\"{width: percent + '%'}\" aria-valuetext=\"{{percent | number:0}}%\" ng-transclude></div>");
}]);

angular.module("template/progressbar/progress.html", []).run(["$templateCache", function($templateCache) {
    $templateCache.put("template/progressbar/progress.html",
        "<div class=\"progress\" ng-transclude></div>");
}]);

angular.module("template/progressbar/progressbar.html", []).run(["$templateCache", function($templateCache) {
    $templateCache.put("template/progressbar/progressbar.html",
        "<div class=\"progress\">\n" +
            "  <div class=\"progress-bar\" ng-class=\"type && 'progress-bar-' + type\" role=\"progressbar\" aria-valuenow=\"{{value}}\" aria-valuemin=\"0\" aria-valuemax=\"{{max}}\" ng-style=\"{width: percent + '%'}\" aria-valuetext=\"{{percent | number:0}}%\" ng-transclude></div>\n" +
            "</div>");
}]);
