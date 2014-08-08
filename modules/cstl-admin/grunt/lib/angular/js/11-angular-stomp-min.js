/*
 * Copyright: 2012, V. Glenn Tarcea
 * MIT License Applies
 */
angular.module("AngularStomp",[]).factory("ngstomp",["rootScope",function(e){function n(e){this.stompClient=Stomp.client(e)}var t={};n.prototype.subscribe=function(t,n){this.stompClient.subscribe(t,function(){var t=arguments;e.$apply(function(){n(t[0])})})};n.prototype.send=function(e,t,n){this.stompClient.send(e,t,n)};n.prototype.connect=function(n,r,i,s,o){this.stompClient.connect(n,r,function(n){e.$apply(function(){i.apply(t,n)})},function(n){e.$apply(function(){s.apply(t,n)})},o)};n.prototype.disconnect=function(t){this.stompClient.disconnect(function(){var n=arguments;e.$apply(function(){t.apply(n)})})};return function(e){return new n(e)}}])
