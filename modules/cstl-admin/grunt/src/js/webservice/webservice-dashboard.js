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

angular.module('cstl-webservice-dashboard', ['cstl-restapi', 'cstl-services', 'pascalprecht.translate', 'ui.bootstrap.modal'])

    .controller('WebServiceController', function($scope, webService, provider, csw, sos, $modal, textService, Growl, $translate, $window) {

        $scope.typeFilter = {type: '!WEBDAV'};
        $scope.hideScroll = true;
        $scope.hideScrollServices = true;

        $scope.getCurrentLang = function() {
            return $translate.use();
        };

        angular.element($window).bind("scroll", function() {
            if (this.pageYOffset < 220) {
                $scope.hideScrollServices = true;
            } else {
                $scope.hideScrollServices = false;
            }
            $scope.$apply();
        });
        webService.listAll({lang: $scope.getCurrentLang()}, function(response){//success
            $scope.services = response;
        }, function() {//error
            Growl('error','Error','Unable to show services list!');
        });


        // Show Capa methods
        $scope.showCapa = function(service) {
            if (service.versions.length > 1) {
                var modal = $modal.open({
                    templateUrl: 'views/webservice/modalChooseVersion.html',
                    controller: 'WebServiceVersionsController',
                    resolve: {
                        service: function() { return service; }
                    }
                });
                modal.result.then(function(result) {
                    showModalCapa(service, result);
                });
            } else {
                showModalCapa(service, service.versions[0]);
            }
        };

        function showModalCapa(service, version) {
            $modal.open({
                templateUrl: 'views/webservice/modalCapa.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.capa(service.type.toLowerCase(), service.identifier, version);
                    }
                }
            });
        }

        // Show Logs methods
        $scope.showLogs = function(service) {

            $modal.open({
                templateUrl: 'views/webservice/modalLogs.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.logs(service.type.toLowerCase(), service.identifier);
                    }
                }
            });
        };

        $scope.reload = function(service){
            webService.restart({type: service.type, id: service.identifier}, {value: true},
                function() { Growl('success','Success','Service '+ service.name +' successfully reloaded'); },
                function() { Growl('error','Error','Service '+ service.name +' reload failed'); }
            );
        };
        $scope.startOrStop = function(service){
            if(service.status==='STARTED'){
                webService.stop({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.services = webService.listAll({lang: $scope.getCurrentLang()});
                        Growl('success','Success','Service '+ service.name +' successfully stopped');
                    }
                }, function() { Growl('error','Error','Service '+ service.name +' stop failed'); });
            }else{
                webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.services = webService.listAll({lang: $scope.getCurrentLang()});
                        Growl('success','Success','Service '+ service.name +' successfully started');
                    }
                }, function() { Growl('error','Error','Service '+ service.name +' start failed'); });
            }
        };

        $scope.deleteService = function(service) {
            var dlg = $modal.open({
                templateUrl: 'views/modal-confirm.html',
                controller: 'ModalConfirmController',
                resolve: {
                    'keyMsg':function(){return "dialog.message.confirm.delete.service";}
                }
            });
            dlg.result.then(function(cfrm){
                if(cfrm){
                    if (service.type.toLowerCase() === 'sos') {
                        // A provider has been created for this SOS service, so remove it
                        provider.delete({id: service.identifier +"-om2"});
                    }
                    webService.delete({type: service.type, id: service.identifier}, {} ,
                        function() { Growl('success','Success','Service '+ service.name +' successfully deleted');
                            $scope.services = webService.listAll({lang: $scope.getCurrentLang()}); },
                        function() { Growl('error','Error','Service '+ service.name +' deletion failed'); }
                    );
                }
            });
        };

        $scope.refreshIndex = function(service) {
            csw.refresh({id: service.identifier}, {},
                function() { Growl('success','Success','Search index for the service '+ service.name +' successfully refreshed'); },
                function() { Growl('error','Error','Search index for the service '+ service.name +' failed to be updated'); }
            );
        };
    });