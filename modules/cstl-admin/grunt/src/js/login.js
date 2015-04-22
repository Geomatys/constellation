/**
 * On document ready set focus to username input
 */
jQuery(document).ready(function() {
  jQuery('#username').focus();
});

/**
 * Angular login app.
 */
var cstlLoginApp = angular.module("cstlLoginApp", ['pascalprecht.translate']);
cstlLoginApp.config(['$translateProvider', '$translatePartialLoaderProvider',
    function ($translateProvider, $translatePartialLoaderProvider) {
        // Initialize angular-translate
        $translateProvider.useLoader('$translatePartialLoader', {
            urlTemplate: 'i18n/{lang}/{part}.json'
        });
        $translatePartialLoaderProvider.addPart('ui-menu');
        $translatePartialLoaderProvider.addPart('ui');
        $translateProvider.preferredLanguage('en');
    }
]);

/**
 * Directive to fix a bug with form auto filling by navigator and angular.
 */
cstlLoginApp.directive('formAutofillFix', function() {
    return function(scope, elem, attrs) {
        elem.prop('method', 'POST');
        // Fix autofill issues where Angular doesn't know about autofilled inputs
        if(attrs.ngSubmit) {
            setTimeout(function() {
                elem.unbind('submit').bind('submit',function(e) {
                    e.preventDefault();
                    elem.find('input, textarea, select').trigger('input').trigger('change').trigger('keydown');
                    scope.$apply(attrs.ngSubmit);
                });
            }, 0);
        }
    };
});

/**
 * Login controller.
 */
cstlLoginApp.controller("login", function($scope, $http){

    var cstlUrl;
    $scope.formInputs = {
        username:undefined,
        password:undefined
    };

    $scope.login = function(target){
        $http.post(cstlUrl + 'spring/login', {username: $scope.formInputs.username,
                                                       password: $scope.formInputs.password})
            .success(function(resp){
                jQuery('#msg-error').hide();
                if (resp.token) {
                    $.cookie('access_token', resp.token, { path : '/' });
                    $.cookie('cstlActiveDomainId', 1, { path : '/' });
                    $.cookie('cstlUserId', resp.userId, { path : '/' });
                    window.location.href= target ? target : "admin.html";
                }
            }).error(function(resp){
                jQuery('#msg-error').show('fade');
            });
    };

    $http.get('app/conf', {isArray: false}).success(function(conf){
        cstlUrl = conf.cstl;
        if(cstlUrl.indexOf('http://')===-1){
            var currentUrl = window.location.href;
            cstlUrl = currentUrl.substring(0,currentUrl.indexOf('/',7))+cstlUrl;
        }
        $.cookie('cstlUrl', cstlUrl );
    });
});
