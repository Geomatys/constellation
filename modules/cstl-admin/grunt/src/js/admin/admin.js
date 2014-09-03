/*
 * Constellation - An open source and standard compliant SDI
 *
 *     http://www.constellation-sdi.org
 *
 *     Copyright 2014 Geomatys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

angular.module('cstl-admin', ['cstl-admin-system',
                              'cstl-admin-processing',
                              'cstl-admin-security'])

    .controller('AdminController', function($scope, $location) {

        var viewUrls = {
            'system_state':    { name: 'system_state',    url: 'views/admin/system_state.html'    },
            'system_settings': { name: 'system_settings', url: 'views/admin/system_settings.html' },
            'system_logs':     { name: 'system_logs',     url: 'views/admin/logs.html'            },
            'system_contact':  { name: 'system_contact',  url: 'views/admin/contact.html'         },
            'system_about':    { name: 'system_about',    url: 'views/admin/about.html'           },
            'tasks_manager':   { name: 'tasks_manager',   url: 'views/admin/tasks_manager.html'   },
            'planning':        { name: 'planning',        url: 'views/admin/planning.html'        },
            'users':           { name: 'users',           url: 'views/admin/users.html'           },
            'groups':          { name: 'groups',          url: 'views/admin/groups.html'          },
            'domains':         { name: 'domains',         url: 'views/admin/domains.html'         },
            'domainmembers':   { name: 'domainmembers',   url: 'views/admin/domain/members.html'  }
        };

        $scope.currentView = viewUrls.system_state;

        $scope.changeView = function(page) {
            $scope.currentView = viewUrls[page];
        };

        if ($location.url() === '/admin/system_state') {
            $scope.changeView('system_state');
        } else if ($location.url() === '/admin/system_settings') {
            $scope.changeView('system_settings');
        } else if ($location.url() === '/admin/system_logs') {
            $scope.changeView('system_logs');
        } else if ($location.url() === '/admin/system_contact') {
            $scope.changeView('system_contact');
        } else if ($location.url() === '/admin/system_about') {
            $scope.changeView('system_about');
        } else if ($location.url() === '/admin/tasks_manager') {
            $scope.changeView('tasks_manager');
        } else if ($location.url() === '/admin/planning') {
            $scope.changeView('planning');
        } else if ($location.url() === '/admin/users') {
            $scope.changeView('users');
        } else if ($location.url() === '/admin/groups') {
            $scope.changeView('groups');
        } else if ($location.url() === '/admin/domains') {
            $scope.changeView('domains');
        } else if ($location.url().indexOf('/admin/domainmembers/') !== -1) {
            $scope.changeView('domainmembers');
        }
    });