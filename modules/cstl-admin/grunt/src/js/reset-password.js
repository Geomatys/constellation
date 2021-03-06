/**
 * On document ready set focus to username input
 */
jQuery(document).ready(function() {
    jQuery('#password1').focus();
});

/**
 * Angular login app.
 */
var cstlResetPasswordApp = angular.module("cstlResetPasswordApp",
    ['ngResource',
     'ngRoute',
     'pascalprecht.translate',
     'cstl-directives',
     'cstl-services']);


cstlResetPasswordApp.config(['$translateProvider', '$translatePartialLoaderProvider',
    function ($translateProvider, $translatePartialLoaderProvider) {
        // Initialize angular-translate
        $translateProvider.useLoader('$translatePartialLoader', {
            urlTemplate: 'i18n/{lang}/{part}.json'
        });
        $translatePartialLoaderProvider.addPart('ui-menu');
        $translatePartialLoaderProvider.addPart('ui');
        $translateProvider.preferredLanguage('en');

        // remember language
        $translateProvider.useCookieStorage();
    }
]);

cstlResetPasswordApp.config(['$locationProvider', function ($locationProvider) {
        //Configure $locationProvider to html5 (allow $location.search() to get URL params)
        $locationProvider.html5Mode(true);
    }
]);

/**
 * Login controller.
 */
cstlResetPasswordApp.controller("resetPasswordController", function($scope, $http, $location, $translate, Growl, AppConfigService){

    var self = this;

    var cstlUrl;

    self.params = $location.search();
    self.password1 = '';
    self.password2 = '';

    self.reset = function(){
        $http.post($.cookie('cstlUrl') + 'spring/resetPassword', {password: self.password1, uuid: self.params.uuid})
            .success(function(resp){
                $translate(['Success', 'password.reset.success']).then(function (translations) {
                    Growl('success', translations.Success, translations['password.reset.success']);
                });
                window.location = $.cookie('cstlUrl');
            });
    };

    AppConfigService.getConfigProperty('cstl', function (val) {
        cstlUrl = val;
        $.cookie('cstlUrl', val);
    });
});
