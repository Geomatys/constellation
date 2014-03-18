/*
 * Constellation - An open source and standard compliant SDI
 *      http://www.constellation-sdi.org
 *   (C) 2014, Geomatys
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details..
 */
'use strict';

/* Services */
function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}


/*
 * Injection of jessionid for csltSessionId cookie.
 */
cstlIndexApp.factory('AuthInterceptor', function($cookies) {
    return {
	    'request': function(config) {
	    	var url = config.url+'';
	    	var jsessionIdIndex = url.indexOf(";jsessionid=");
	    	if(jsessionIdIndex != -1)
    	    	if ($cookies.cstlSessionId) {
    	    		config.url = url.replace(";jsessionid=", ";jsessionid=" + $cookies.cstlSessionId);
	        	}else{
	        		config.url = url.substring(0, url.indexOf(';jsessionid='))
	        	}
	        return config || $q.when(config);
	    }
	};
});


var context = findWebappContext();




cstlIndexApp.factory('Account', ['$resource',
    function ($resource) {
        return $resource(cstlContext + 'spring/account;jsessionid=', {}, {
        });
    }]);

cstlIndexApp.factory('AuthenticationSharedService', ['$rootScope', '$http', 'authService', '$base64','$cookieStore',
    function ($rootScope, $http, authService, $base64, $cookieStore) {
        return {
            authenticate: function() {
                $http.get(cstlContext + 'spring/session/status;jsessionid=')
                    .success(function (data, status, headers, config) {
                        $rootScope.$broadcast('event:auth-authConfirmed');
                    })
            },
            logout: function () {
                $rootScope.authenticationError = false;
                $http.get(cstlContext + "spring/session/logout;jsessionid=").then(function(){
                	$cookieStore.remove('cstlSessionId');
                	$http.get(context + '/app/logout')
                	.success(function (data, status, headers, config) {
                		authService.loginCancelled();
                	});

                });
            }
        };
    }]);




cstlIndexApp.service('$growl', function() {
    /**
     * Displays a notification with the specified title and text.
     *
     * @param type  - {string} the notification type (info|error|success|warning)
     * @param title - {string} the notification title
     * @param msg   - {string} the notification message
     */
    return function(type, title, msg) {
        if (type === 'info') {
            $.growl({title: title, message: msg});
        } else if (type === 'error') {
            $.growl.error({title: title, message: msg});
        } else if (type === 'success') {
            $.growl.notice({title: title, message: msg});
        } else if (type === 'warning') {
            $.growl.warning({title: title, message: msg});
        }
    };
});

