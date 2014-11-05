$(document).ready(function() {
  $('#username').focus();
});


var cstlLoginApp = angular.module("cstlLoginApp", []);


cstlLoginApp.controller("login", function($scope, $http){  
  $http.get('app/conf', {isArray: false}).success(function(conf){
    var cstlUrl = conf.cstl;
    $.cookie('cstlUrl', cstlUrl );
    $scope.login = function(){
      $http.post(cstlUrl + 'api/user/authenticate', {username: $scope.username, password: $scope.password}).success(function(resp){
        if (resp.token) {
          $.cookie('authToken', resp.token, { path : '/' });
          $.cookie('cstlActiveDomainId', resp.domainId, { path : '/' });
          $.cookie('cstlUserId', resp.userId, { path : '/' });
          window.location.href="admin.html";
        }
      });
    };
  });
});


// setTimeout(
// function() {
// $
// .ajax({
// url : 'http://localhost:8180/constellation/api/user/extendToken'
// + "?token="
// + resp.token,
// type : 'GET',
// crossDomain : true,
// success : function(
// resp) {
// if (resp.token)
// $
// .cookie(
// 'authToken',
// resp.token,
// {
// path : '/'
// });
// }
// });

// }, 5000);
