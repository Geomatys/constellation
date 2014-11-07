$(document).ready(function() {
  $('#username').focus();
});


var cstlLoginApp = angular.module("cstlLoginApp", []);

cstlLoginApp.controller("login", function($scope, $http){

    var cstlUrl;

    $scope.formInputs = {
        username:undefined,
        password:undefined
    };

    $scope.login = function(){
        //angular cannot bind the password value when browser fill it automatically.
        //then fixing it with jquery
        $scope.formInputs.password = $('#password').val();

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
