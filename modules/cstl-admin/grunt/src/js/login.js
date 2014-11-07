$(document).ready(function() {
  $('#username').focus();
});


var cstlLoginApp = angular.module("cstlLoginApp", []);

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

cstlLoginApp.controller("login", function($scope, $http){

    var cstlUrl;

    $scope.formInputs = {
        username:undefined,
        password:undefined
    };

    $scope.login = function(){
        $http.post(cstlUrl + 'api/user/authenticate', {username: $scope.formInputs.username,
                                                       password: $scope.formInputs.password})
            .success(function(resp){
                if (resp.token) {
                    $.cookie('authToken', resp.token, { path : '/' });
                    $.cookie('cstlActiveDomainId', resp.domainId, { path : '/' });
                    $.cookie('cstlUserId', resp.userId, { path : '/' });
                    window.location.href="admin.html";
                }
            });
    };

    $http.get('app/conf', {isArray: false}).success(function(conf){
        cstlUrl = conf.cstl;
        $.cookie('cstlUrl', cstlUrl );
    });
});
