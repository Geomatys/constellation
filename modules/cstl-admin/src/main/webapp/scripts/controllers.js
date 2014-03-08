'use strict';

/* Controllers */

cstlAdminApp.controller('HeaderController', ['$scope','$http',
                                           function ($scope, $http) {
	                                         $http.get("app/conf").success(function(data){
	                                        	 $scope.cstlLoginUrl = data.cstl + "spring/auth/form";
	                                        	 $scope.cstlLogoutUrl = data.cstl + "logout";
	                                        });
                                           }]);

cstlAdminApp.controller('MainController', ['$scope','$location',
    function ($scope, $location) {
    }]);

cstlAdminApp.controller('LanguageController', ['$scope', '$translate',
    function ($scope, $translate) {
        $scope.changeLanguage = function (languageKey) {
            $translate.uses(languageKey);
        };
    }]);

cstlAdminApp.controller('MenuController', ['$scope',
    function ($scope) {


}]);

cstlAdminApp.controller('LoginController', ['$scope', '$location', 'AuthenticationSharedService',
    function ($scope, $location, AuthenticationSharedService) {
        $scope.rememberMe = true;
        $scope.login = function () {
            AuthenticationSharedService.login({
                username: $scope.username,
                password: $scope.password,
                rememberMe: $scope.rememberMe,
                success: function () {
                    $location.path('');
                }
            });
        };
    }]);

cstlAdminApp.controller('LogoutController', ['$location', 'AuthenticationSharedService',
    function ($location, AuthenticationSharedService) {
        AuthenticationSharedService.logout({
            success: function () {
                $location.path('');
            }
        });
    }]);


cstlAdminApp.controller('ContactController', ['$scope', 'Contact',
                                               function ($scope, Contact) {
	$scope.data = Contact.get();
    $scope.save = function () {
       Contact.save($scope.data,
           function (value, responseHeaders) {
              $scope.error = null;
              $scope.success = 'OK';
              $scope.data = Contact.get();
           },
       function (httpResponse) {
           $scope.success = null;
           $scope.error = "ERROR";
       });
    };
}]);

cstlAdminApp.controller('SettingsController', ['$scope', 'resolvedAccount', 'Account',
    function ($scope, resolvedAccount, Account) {
        $scope.success = null;
        $scope.error = null;
        $scope.settingsAccount = resolvedAccount;

        $scope.save = function () {
            Account.save($scope.settingsAccount,
                function (value, responseHeaders) {
                    $scope.error = null;
                    $scope.success = 'OK';
                    $scope.settingsAccount = Account.get();
                },
                function (httpResponse) {
                    $scope.success = null;
                    $scope.error = "ERROR";
                });
        };
    }]);

cstlAdminApp.controller('UserController', ['$scope', 'UserResource', '$modal',
  function ($scope, UserResource, $modal) {
    $scope.list = UserResource.query();
    $scope.details = function(i) {
        $modal.open({
            templateUrl: 'views/user/details.html',
            controller: 'UserDetailsController',
            resolve: {
                'user': function(){
                    return angular.copy($scope.list[i]);
                }
            }
        }).result.then(function(user){
        	if(user != null)
        	$scope.list[i] = user;
        });
    };
    $scope.add = function(i) {
        $modal.open({
            templateUrl: 'views/user/add.html',
            controller: 'UserDetailsController',
            resolve: {
                'user': function(){
                    return {roles:[]};
                }
            }
        }).result.then(function(user){
        	if(user != null)
        	$scope.list[$scope.list.length] = user;
        });
    };
    $scope.delete = function(i){
    	UserResource.delete({id: $scope.list[i].login});
    	$scope.list.splice(i, 1);
    };
}]);

cstlAdminApp.controller('UserDetailsController', ['$scope', '$modalInstance', 'user', 'UserResource',
  function ($scope, $modalInstance, user, UserResource) {
    $scope.user = user;

    $scope.close = function() {
        $modalInstance.dismiss('close');
    };
    $scope.deleteTag = function(role){
        var newRoles = [];
        for(var i=0; i<user.roles.length; i++)
           if(user.roles[i] != role)
               newRoles[newRoles.length] = user.roles[i];
        user.roles = newRoles;
    };

    $scope.addRole = function(role){
    	for(var i=0; i < $scope.user.roles.length; i++)
    	   if(role === $scope.user.roles[i])
    		   return

    	$scope.user.roles[$scope.user.roles.length]=role
    };

    $scope.save = function(){
    	var userResource = new UserResource($scope.user);
    	userResource.$save();
    	$modalInstance.close($scope.user);
    }
}]);


cstlAdminApp.controller('PasswordController', ['$scope', 'Password',
    function ($scope, Password) {
        $scope.success = null;
        $scope.error = null;
        $scope.doNotMatch = null;
        $scope.changePassword = function () {
            if ($scope.password != $scope.confirmPassword) {
                $scope.doNotMatch = "ERROR";
            } else {
                $scope.doNotMatch = null;
                Password.save($scope.password,
                    function (value, responseHeaders) {
                        $scope.error = null;
                        $scope.success = 'OK';
                    },
                    function (httpResponse) {
                        $scope.success = null;
                        $scope.error = "ERROR";
                    });
            }
        };
    }]);

cstlAdminApp.controller('SessionsController', ['$scope', 'resolvedSessions', 'Sessions',
    function ($scope, resolvedSessions, Sessions) {
        $scope.success = null;
        $scope.error = null;
        $scope.sessions = resolvedSessions;
        $scope.invalidate = function (series) {
            Sessions.delete({series: encodeURIComponent(series)},
                function (value, responseHeaders) {
                    $scope.error = null;
                    $scope.success = "OK";
                    $scope.sessions = Sessions.get();
                },
                function (httpResponse) {
                    $scope.success = null;
                    $scope.error = "ERROR";
                });
        };
    }]);

cstlAdminApp.controller('MetricsController', ['$scope', 'resolvedMetrics',
    function ($scope, resolvedMetrics) {
        $scope.metrics = resolvedMetrics;
    }]);

cstlAdminApp.controller('LogsController', ['$scope', 'resolvedLogs', 'LogsService',
    function ($scope, resolvedLogs, LogsService) {
        $scope.loggers = resolvedLogs;

        $scope.changeLevel = function (name, level) {
            LogsService.changeLevel({name: name, level: level}, function () {
                $scope.loggers = LogsService.findAll();
            });
        };
    }]);

cstlAdminApp.controller('DataController', ['$scope', '$location', '$dashboard', 'dataListing', 'style', '$modal', '$growl', 'StyleSharedService',
    function ($scope, $location, $dashboard, dataListing, style, $modal, $growl, StyleSharedService) {

        $scope.filtertype = "VECTOR";

        dataListing.listAll({}, function(response) {
            $dashboard($scope, response);
        });

        // Map methods
        $scope.showData = function() {
            $('#viewerData').modal("show");
            var layerName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            var layerData = DataViewer.createLayer(layerName, providerId);
            var layerBackground = DataViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataMap');
        };

        $scope.deleteData = function() {
            if (confirm("Are you sure?")) {
                var layerName = $scope.selected.Name;
                var providerId = $scope.selected.Provider;
                dataListing.deleteData({providerid: providerId, dataid: layerName}, {},
                    function() { $growl('success','Success','Data '+ layerName +' successfully deleted');
                        dataListing.listAll({}, function(response) {
                            $scope.fullList = response;
                        });
                    },
                    function() { $growl('error','Error','Data '+ layerName +' deletion failed'); }
                );
            }
        };




        // Style methods
        $scope.showStyleList = function() {
            StyleSharedService.showStyleList($scope);
        };

        $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
            StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style);
        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#dataDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        // Data loading
        $scope.showLocalFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalLocalFile.html',
                controller: 'LocalFileModalController'
            });

            modal.result.then(function(result) {
                $location.path('/description/'+ result.type +"/"+ result.file +"/"+ result.missing);
            });
        };

        $scope.showServerFilePopup = function() {
            var modal = $modal.open({
                templateUrl: 'views/modalServerFile.html',
                controller: 'ServerFileModalController'
            });
        };
    }]);

cstlAdminApp.controller('StyleModalController', ['$scope', '$dashboard', '$modalInstance', 'style', 'exclude','layerName','serviceName',
    function ($scope, $dashboard, $modalInstance, style, exclude, layerName, serviceName) {
        $scope.exclude = exclude;
        $scope.layerName = layerName;
        $scope.serviceName = serviceName;

        $scope.filtertype = "";

        $scope.stylechooser = 'new';

        $scope.setStyleChooser = function(choice){
            $scope.stylechooser = choice;
        };

        $scope.isSelected= function(choice) {
            return choice === $scope.stylechooser;
        };

        style.listAll({}, function(response) {
            $dashboard($scope, response.styles);
        });

        $scope.ok = function() {
            $modalInstance.close($scope.selected);
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.showLayerWithStyle = function(style) {

            var layerName = $scope.layerName;
            var layerData = DataViewer.createLayerWMSWithStyle(layerName, $scope.serviceName,$scope.selected.Name);
            var layerBackground = DataViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('styledMapOL');
        };

        $scope.StyleisSelected =function(){
            if ($scope.selected != null){
                $scope.showLayerWithStyle($scope.selected.Name);
                return true
            } else {
                return false
            }

        };

    }]);

cstlAdminApp.controller('LocalFileModalController', ['$scope', '$dashboard', '$modalInstance', '$growl', 'provider', 'dataListing', '$uploadFiles', '$cookies',
    function ($scope, $dashboard, $modalInstance, $growl, provider, dataListing, $uploadFiles, $cookies) {
        $scope.layer = null;
        $scope.file = null;
        $scope.providerId = null;
        $scope.metadatafile = null;
        $scope.uploadType = '';

        // Handle upload workflow
        $scope.step1 = true;
        $scope.step2 = false;
        $scope.step3 = false;
        $scope.allowSubmit = false;
        $scope.allowNext = false;

        $scope.next = function() {
            $scope.step1 = false;
            $scope.step2 = true;
            $scope.step3 = false;
            $scope.allowNext = false;
            $scope.allowSubmit = true;
        };

        $scope.close = function() {
            if ($scope.step3) {
                $modalInstance.close({type: $scope.uploadType, file: $scope.providerId, missing: $scope.metadatafile == null});
            } else {
                $modalInstance.dismiss('close');
            }
        };

        $scope.verifyExtension = function() {
            var lastPointIndex = $scope.file.lastIndexOf(".");
            var extension = $scope.file.substring(lastPointIndex+1, $scope.file.length);
            dataListing.extension({}, {value: extension},
                function(response) {
                    if(response.dataType!=""){
                        $scope.uploadType = response.dataType;
                        $scope.allowNext = false;
                        $scope.allowSubmit = true;
                    } else {
                        $scope.allowNext = true;
                        $scope.allowSubmit = false;
                    }
                });
        };

        $scope.upload = function() {
            var $form = $('#uploadForm');

            var formData = new FormData($form[0]);

            $.ajax({
                url: cstlContext + "api/1/data/upload;jsessionid="+ $cookies.cstlSessionId,
                type: 'POST',
                data: formData,
                async: false,
                cache: false,
                contentType: false,
                processData: false,
                success: function (returndata) {
                    uploaded(returndata);
                }
            });
        };

        function uploaded(message) {
            if (message.indexOf('failed') === -1) {
                var files = message.split(',');
                var upFile = files[0];
                var upMdFile = null;
                if (files.length === 2) {
                    upMdFile = files[1];
                }

                // Stores uploaded files in session for further use
                var upFiles = $uploadFiles.files;
                upFiles.file = upFile;
                upFiles.mdFile = upMdFile;

                var justFile = upFile.substring(upFile.lastIndexOf("/")+1);
                var fileName = justFile;
                var fileExtension;
                if (fileName.indexOf(".") !== -1) {
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    fileExtension = justFile.substring(justFile.lastIndexOf(".")+1);
                }

                // Store the providerId for further calls
                $scope.providerId = fileName;
                if ($scope.uploadType === "vector") {
                    provider.create({
                        id: fileName
                    }, {
                        type: "feature-store",
                        subType: "shapefile",
                        parameters: {
                            path: upFile
                        }
                    });
                    $growl('success','Success','Shapefile data '+ fileName +' successfully added');
                    $modalInstance.close({type: "vector", file: fileName, missing: $scope.metadatafile == null});
                } else if ($scope.uploadType === "raster") {
                    provider.create({
                        id: fileName
                    }, {
                        type: "coverage-store",
                        subType: "coverage-file",
                        parameters: {
                            path: upFile
                        }
                    }, function() {
                        if (!fileExtension || fileExtension !== "nc") {
                            dataListing.pyramidData({id: fileName}, {value: upFile}, function() {
                                $growl('success','Success','Coverage data '+ fileName +' successfully added');
                                $modalInstance.close({type: "raster", file: fileName, missing: $scope.metadatafile == null});
                            });
                        } else {
                            displayNetCDF(fileName);
                        }
                    });
                } else {
                    $growl('warning','Warning','Not implemented choice');
                    $modalInstance.close();
                }
            } else {
                $growl('error','Error','Data import failed');
                $modalInstance.close();
            }
        };

        function displayNetCDF(providerId) {
            $scope.step1 = false;
            $scope.step2 = false;
            $scope.step3 = true;
            $scope.allowNext = false;
            $scope.allowSubmit = false;

            $scope.coveragesData = dataListing.listCoverage({}, {value: providerId}, function(response) {
                for (var key in response.values) {
                    $scope.displayLayer(response.values[key]);
                    break;
                }
            });
        };

        $scope.displayLayer = function(layer) {
            $scope.layer = layer;
            var layerData = DataViewer.createLayer(layer, $scope.providerId);
            var layerBackground = DataViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataPreviewMap');
        };
    }]);

cstlAdminApp.controller('ServerFileModalController', ['$scope', '$dashboard', '$modalInstance', '$growl', 'dataListing', 'provider',
    function ($scope, $dashboard, $modalInstance, $growl, dataListing, provider) {
        $scope.columns = [];
        // current path chosen in server data dir
        $scope.currentPath = '/';
        // path of the server data dir
        $scope.prefixPath = '';
        $scope.finished = false;
        $scope.hasSelectedSomething = false;
        $scope.layer = '';
        $scope.chooseType = false;
        $scope.dataType = 'vector';

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.load = function(path){
            $scope.currentPath = path;
            if (path === '/') {
                path = "root";
            }
            $scope.columns.push(dataListing.dataFolder({}, path));
        };

        $scope.open = function(path, depth) {
            if (depth < $scope.columns.length) {
                $scope.columns.splice(depth + 1, $scope.columns.length - depth);
            }
            $scope.load(path);
        };

        $scope.chooseFile = function(path, depth) {
            if (depth < $scope.columns.length) {
                $scope.columns.splice(depth + 1, $scope.columns.length - depth);
            }
            $scope.currentPath = path;
        };

        $scope.select = function(item,depth) {
            $scope.prefixPath = item.prefixPath;
            $scope.hasSelectedSomething = true;
            if (item.folder) {
                $scope.open(item.subPath, depth);
            } else {
                $scope.chooseFile(item.subPath, depth);
            }
        };

        $scope.startWith = function(path) {
            return $scope.currentPath.indexOf(path) === 0;
        };

        $scope.ok = function() {
            $scope.finished = true;
            $scope.loadData();
        };

        $scope.userChooseType = function() {
            $scope.finished = true;
            $scope.loadDataWithKnownExtension($scope.providerId, $scope.dataType);
        };

        $scope.loadData = function() {
            var file = $scope.currentPath.substring($scope.currentPath.lastIndexOf("/")+1);
            var fileName = file;
            var fileExtension;
            if (file.indexOf(".") !== -1) {
                fileName = file.substring(0, file.lastIndexOf("."));
                fileExtension = file.substring(file.lastIndexOf(".")+1);
            }
            $scope.providerId = fileName;

            // test extension type
            dataListing.extension({}, {value: fileExtension},
                function(response) {
                    $scope.dataType = response.dataType;
                    if ($scope.dataType === "") {
                        $scope.chooseType = true;
                    } else {
                        $scope.loadDataWithKnownExtension(fileName, fileExtension);
                    }
                }, function() {
                    // failure here, impossible to know the extension
                    $scope.chooseType = true;
                }
            );
        };

        $scope.loadDataWithKnownExtension = function(fileName, fileExtension) {
            if ($scope.dataType === "vector") {
                provider.create({
                    id: fileName
                }, {
                    type: "feature-store",
                    subType: "shapefile",
                    parameters: {
                        path: $scope.prefixPath + $scope.currentPath
                    }
                });
                $growl('success','Success','Shapefile data '+ fileName +' successfully added');
                $modalInstance.close();
            } else if ($scope.dataType === "raster") {
                provider.create({
                    id: fileName
                }, {
                    type: "coverage-store",
                    subType: "coverage-file",
                    parameters: {
                        path: $scope.prefixPath + $scope.currentPath
                    }
                }, function() {
                    if (!fileExtension || fileExtension !== "nc") {
                        dataListing.pyramidData({id: fileName}, {value: $scope.prefixPath + $scope.currentPath}, function() {
                            $growl('success','Success','Coverage data '+ fileName +' successfully added');
                            $modalInstance.dismiss('close');
                        });
                    } else {
                        $scope.displayNetCDF(fileName);
                    }
                });
            } else {
                $growl('warning','Warning','Not implemented choice');
                $modalInstance.close();
            }
        };

        $scope.displayNetCDF = function(providerId) {
            $scope.coveragesData = dataListing.listCoverage({}, {value: providerId}, function(response) {
                for (var key in response.values) {
                    $scope.displayLayer(response.values[key]);
                    break;
                }
            });
        };

        $scope.displayLayer = function(layer) {
            $scope.layer = layer;
            var layerData = DataViewer.createLayer(layer, $scope.providerId);
            var layerBackground = DataViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
            DataViewer.layers = [layerData, layerBackground];
            DataViewer.initMap('dataServerMap');
        };

        $scope.load($scope.currentPath);
    }]);

cstlAdminApp.controller('StylesController', ['$scope', '$dashboard', 'style', '$growl',
    function ($scope, $dashboard, style, $growl) {
        $scope.filtertype = "";

        style.listAll({}, function(response) {
            $dashboard($scope, response.styles);
        });

        $scope.deleteStyle = function() {
            if (confirm("Are you sure?")) {
                var styleName = $scope.selected.Name;
                var providerId = $scope.selected.Provider;
                style.delete({providerid: providerId, name: styleName}, {},
                    function() { $growl('success','Success','Style '+ styleName +' successfully deleted');
                        style.listAll({}, function(response) {
                            $scope.fullList = response.styles;
                        });
                    },
                    function() { $growl('error','Error','Style '+ styleName +' deletion failed'); });
            }
        };

        $scope.editStyle = function() {
            var styleName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;

        };

        $scope.toggleUpDownSelected = function() {
            var $header = $('#stylesDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };
    }]);

cstlAdminApp.controller('DescriptionController', ['$scope', '$routeParams','dataListing','$location', '$translate', '$uploadFiles',
    function ($scope, $routeParams, dataListing, $location, $translate, $uploadFiles) {
        $scope.provider = $routeParams.id;
        $scope.missing = $routeParams.missing === 'true';
        $scope.type = $routeParams.type;

        $scope.tabiso = $scope.type==='vector' && $scope.missing;
        $scope.tabcrs = false;
        $scope.tabdesc = $scope.type==='vector' && !$scope.missing;
        $scope.tabimageinfo = $scope.type==='raster';

        $scope.selectTab = function(item) {
            if (item === 'tabiso') {
                $scope.tabiso = true;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
                $scope.tabimageinfo = false;
            } else if (item === 'tabcrs') {
                $scope.tabiso = false;
                $scope.tabcrs = true;
                $scope.tabdesc = false;
                $scope.tabimageinfo = false;
            } else if (item === 'tabdesc') {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = true;
                $scope.tabimageinfo = false;
            } else {
                $scope.tabiso = false;
                $scope.tabcrs = false;
                $scope.tabdesc = false;
                $scope.tabimageinfo = true;
            }
        };

        $scope.save = function() {
            dataListing.setMetadata({},
                {dataName:$scope.provider, dataPath:$uploadFiles.files.file, anAbstract:$scope.mdabstract, title:$scope.mdtitle, keywords:$scope.mdkeywords,
                 username:$scope.mdusername, organisationName:$scope.mdorganisationName, role:$scope.mdrole, localeMetadata:$scope.mdlocaleData,
                 topicCategory:$scope.mdtopicCategory, date:$scope.mddate,dateType:$scope.mddateType,type:$scope.type
                },
                function() {
                    $location.path('/data');
                }
            );
        };

        $scope.getCurrentLang = function() {
            return $translate.uses();
        };

        $scope.createMetadataTree = function(parentDivId, isCoverageMetadata){
            var upFile = $uploadFiles.files.file;
            upFile = upFile.substring(upFile.lastIndexOf("/")+1);
            var upMdFile = $uploadFiles.files.mdFile;
            if (upMdFile != null) {
                upMdFile = upMdFile.substring(upMdFile.lastIndexOf("/")+1);
            }
            dataListing.loadData({}, {values: {'filePath': upFile, 'metadataFilePath': upMdFile, dataType: $scope.type}}, function(response) {
                if (isCoverageMetadata) {
                    for (var key in response.coveragesMetadata) {
                        var metadataList = response.coveragesMetadata[key].coverageMetadataTree;
                        generateMetadataTags(metadataList, parentDivId);
                    }
                } else {
                    var metadataList = response.fileMetadata;
                    generateMetadataTags(metadataList, parentDivId);
                }

                $("#"+ parentDivId +" .collapse").collapse('show');
            });

            function generateMetadataTags(metadataList, parentDivId) {
                for(var i=0; i<metadataList.length; i++){
                    var key = metadataList[i];
                    var name = key.name;
                    var nameWithoutWhiteSpace = key.nameNoWhiteSpace;
                    var value = key.value;
                    var childrenExist = key.childrenExist;
                    var parentNode = key.parentName;
                    var depthSpan = key.depthSpan;

                    if(childrenExist){
                        //root node
                        if(parentNode === null || parentNode == ''){
                            var htmlElement =   "<a data-toggle='collapse' data-target='#"+nameWithoutWhiteSpace+"Div' class='col-sm-"+depthSpan+"'>"+name+"</a>" +
                                "<div class='collapse col-sm-"+depthSpan+"' id='"+nameWithoutWhiteSpace+"Div'><table id='"+nameWithoutWhiteSpace+"' class='table table-striped'></table></div>";
                            jQuery("#"+ parentDivId).append(htmlElement);
                        }else{
                            var htmlElement =   "<a data-toggle='collapse' data-target='#"+nameWithoutWhiteSpace+"Div' class='col-sm-"+depthSpan+"'>"+name+"</a>" +
                                "<div class='collapse col-sm-"+depthSpan+"' id='"+nameWithoutWhiteSpace+"Div'><table id='"+nameWithoutWhiteSpace+"' class='table table-striped'></table></div>";
                            jQuery("#"+parentNode+"Div").append(htmlElement);
                        }
                    }else{
                        var htmlElement = "<tr><td>"+name+"</td><td>"+value+"</td></tr>";
                        jQuery("#"+parentNode).append(htmlElement);
                    }
                }
            };
        };

        $scope.codeLists = dataListing.codeLists({lang: $scope.getCurrentLang()});
    }]);


cstlAdminApp.controller('WebServiceController', ['$scope', 'webService','$modal', 'textService', '$growl',
    function ($scope, webService, $modal, textService, $growl) {

       $scope.services = webService.listAll();

        // Show Capa methods
        $scope.showCapa = function(service) {
            $modal.open({
                templateUrl: 'views/modalCapa.html',
                controller: 'WebServiceUtilsController',
                resolve: {
                    'details': function(textService){
                        return textService.capa(service.type.toLowerCase(), service.identifier);
                    }
                }
            });
        };

        // Show Logs methods
        $scope.showLogs = function(service) {

            $modal.open({
                templateUrl: 'views/modalLogs.html',
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
                function() { $growl('success','Success','Service '+ service.name +' successfully reloaded'); },
                function() { $growl('error','Error','Service '+ service.name +' reload failed'); }
            );
        };
        $scope.startOrStop = function(service){
            if(service.status==='WORKING'){
                webService.stop({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.services = webService.listAll();
                        $growl('success','Success','Service '+ service.name +' successfully stopped');
                    }
                }, function() { $growl('error','Error','Service '+ service.name +' stop failed'); });
            }else{
                webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                    if (response.status==="Success") {
                        $scope.services = webService.listAll();
                        $growl('success','Success','Service '+ service.name +' successfully started');
                    }
                }, function() { $growl('error','Error','Service '+ service.name +' start failed'); });
            }
        };

        $scope.deleteService = function(service) {
            if (confirm("Are you sure?")) {
                webService.delete({type: service.type, id: service.identifier}, {} ,
                    function() { $growl('success','Success','Service '+ service.name +' successfully deleted');
                                 $scope.services = webService.listAll(); },
                    function() { $growl('error','Error','Service '+ service.name +' deletion failed'); }
                );
            }
        };
    }]);

cstlAdminApp.controller('WebServiceUtilsController', ['$scope', 'webService', '$modalInstance', 'details',
    function ($scope, webService, $modalInstance, details) {
        $scope.details = details.data;
        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

    }]);

cstlAdminApp.controller('WebServiceCreateController', ['$scope','$routeParams', 'webService', '$filter', '$location', '$growl',
    function ($scope, $routeParams, webService, $filter, $location, $growl) {
        $scope.type = $routeParams.type;
        $scope.tonext = true;
        $scope.metadata = {};
        $scope.metadata.keywords = [];

        $scope.getVersionsForType = function() {
            if ($scope.type === 'wms') {
                return [{ 'id': '1.1.1'}, { 'id': '1.3.0' }];
            }
            if ($scope.type === 'wfs') {
                return [{ 'id': '1.1.0'}, { 'id': '2.0.0' }];
            }
            return [];
        };
        $scope.versions = $scope.getVersionsForType();


        $scope.goToServiceContact = function() {
            $scope.tonext = false;
        };

        $scope.goToServiceInfo = function() {
            $scope.tonext = true;
        };

        $scope.addTag = function() {
            if ($scope.tagText.length == 0) {
                return;
            }

            $scope.metadata.keywords.push($scope.tagText);
            $scope.tagText = '';
        };

        $scope.deleteTag = function(key) {
            if ($scope.metadata.keywords.length > 0 &&
                $scope.tagText.length == 0 &&
                key === undefined) {
                $scope.metadata.keywords.pop();
            } else if (key != undefined) {
                $scope.metadata.keywords.splice(key, 1);
            }
        };

        // define which version to set
        $scope.selectedVersion = function (){
            $scope.metadata.versions = $filter('filter')($scope.versions, {checked: true});
        };

        // define which version is Selected
        $scope.versionIsSelected = function(currentVersion){
            return $.inArray(currentVersion, $scope.metadata.versions) > -1
        };

        $scope.addTag = function() {
            if ($scope.tagText.length == 0) {
                return;
            }

            $scope.metadata.keywords.push($scope.tagText);
            $scope.tagText = '';
        };

        $scope.saveServiceMetadata = function() {
            // Ensures both name and identifier are filled
            if ($scope.metadata.identifier == null && $scope.metadata.name != null) {
                $scope.metadata.identifier = $scope.metadata.name;
            }
            if ($scope.metadata.name == null && $scope.metadata.identifier != null) {
                $scope.metadata.name = $scope.metadata.identifier;
            }

            webService.create({type: $scope.type}, $scope.metadata,
                              function() { $growl('success','Success','Service '+ name +' successfully created');
                                           $location.path('/webservice'); },
                              function() { $growl('error','Error','Service '+ name +' creation failed'); }
            );
        };
    }]);

cstlAdminApp.controller('WebServiceEditController', ['$scope','$routeParams', 'webService', '$modal','textService', '$dashboard', '$growl', '$filter', 'StyleSharedService','style',
                                                 function ($scope, $routeParams , webService, $modal, textService, $dashboard, $growl, $filter, StyleSharedService, style) {
    $scope.tagText = '';
    $scope.type = $routeParams.type;

    $scope.service = webService.get({type: $scope.type, id:$routeParams.id});

    $scope.metadata = webService.metadata({type: $scope.type, id:$routeParams.id});
    $scope.config = webService.config({type: $scope.type, id:$routeParams.id});

    $scope.filtertype = "";
    $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
        $dashboard($scope, response);
    });

    $scope.getVersionsForType = function() {
        if ($scope.type === 'wms') {
            return [{ 'id': '1.1.1'}, { 'id': '1.3.0' }];
        }
        if ($scope.type === 'wfs') {
            return [{ 'id': '1.1.0'}, { 'id': '2.0.0' }];
        }
        return [];
    };
    $scope.versions = $scope.getVersionsForType();

    // define which version to set
    $scope.selectedVersion = function (){
        $scope.metadata.versions = $filter('filter')($scope.versions, {checked: true});
    };

    // define which version is Selected
    $scope.versionIsSelected = function(currentVersion){
       return $.inArray(currentVersion, $scope.metadata.versions) > -1
    };

     $scope.addTag = function() {
         if ($scope.tagText.length == 0) {
             return;
         }
         if ($scope.metadata.keywords ==null){
             $scope.metadata.keywords = [];
         }
         $scope.metadata.keywords.push($scope.tagText);
         $scope.tagText = '';
     }

     $scope.deleteTag = function(key) {
         if ($scope.metadata.keywords.length > 0 &&
             $scope.tagText.length == 0 &&
             key === undefined) {
             $scope.metadata.keywords.pop();
         } else if (key != undefined) {
             $scope.metadata.keywords.splice(key, 1);
         }
     }

    $scope.saveServiceMetadata = function() {
      webService.updateMd({type: $scope.service.type, id: $scope.service.identifier},$scope.metadata,
          function(response) {
              if (response.status==="Success") {
                  $growl('success','Success','Service description successfully updated');
              }else{
                  $growl('error','Error','Service description update failed due to :'+response.status);
              }
          },
          function() {
              $growl('error','Error','Service description update failed');
          }
      )
    };

    // Show Capa methods
    $scope.showCapa = function(service) {
        $modal.open({
            templateUrl: 'views/modalCapa.html',
            controller: 'WebServiceUtilsController',
            resolve: {
                'details': function(textService){
                    return textService.capa(service.type.toLowerCase(), service.identifier);
                }
            }
        });
    };

     // Show Logs methods
     $scope.showLogs = function(service) {

         $modal.open({
             templateUrl: 'views/modalLogs.html',
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
            function() { $growl('success','Success','Service '+ service.name +' successfully reloaded'); },
            function() { $growl('error','Error','Service '+ service.name +' reload failed'); }
        );
     };


     $scope.startOrStop = function(service){
        if(service.status==='WORKING'){
            webService.stop({type: service.type, id: service.identifier}, {}, function(response) {
                if (response.status==="Success") {
                    $scope.service.status = "NOT_STARTED";
                    $growl('success','Success','Service '+ service.name +' successfully stopped');
                }
            }, function() { $growl('error','Error','Service '+ service.name +' stop failed'); });
        }else{
            webService.start({type: service.type, id: service.identifier}, {}, function(response) {
                if (response.status==="Success") {
                    $scope.service.status = "WORKING";
                    $growl('success','Success','Service '+ service.name +' successfully started');
                }
            }, function() { $growl('error','Error','Service '+ service.name +' start failed'); });
        }
     };


     $scope.tabdata = true;
     $scope.tabdesc = false;
     $scope.tabmetadata = false;

     $scope.selectTab = function(item) {
         if (item === 'tabdata') {
             $scope.tabdata = true;
             $scope.tabdesc = false;
             $scope.tabmetadata = false;
         } else if (item === 'tabdesc') {
             $scope.tabdata = false;
             $scope.tabdesc = true;
             $scope.tabmetadata = false;
         } else {
             $scope.tabdata = false;
             $scope.tabdesc = false;
             $scope.tabmetadata = true;
         }
     };

     // Allow to choose data to add for this service
     $scope.showDataToAdd = function() {
         var modal = $modal.open({
             templateUrl: 'views/modalDataChoose.html',
             controller: 'DataModalController',
             resolve: {
                 exclude: function() { return $scope.layers; },
                 service: function() { return $scope.service; }
             }
         });

         modal.result.then(function() {
             $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                 $scope.fullList = response;
             });
         });
     };

     $scope.deleteLayer = function() {
         if ($scope.selected != null && confirm("Are you sure?")) {
             webService.deleteLayer({type: $scope.service.type, id: $scope.service.identifier, layerid: $scope.selected.Name}, {layernamespace: ''},
                 function() {$growl('success','Success','Layer '+ $scope.selected.Name +' successfully deleted from service '+ $scope.service.name);
                             $scope.layers = webService.layers({type: $scope.type, id:$routeParams.id}, {}, function(response) {
                                 $scope.fullList = response;
                             });
                 },
                 function() {$growl('error','Error','Layer '+ $scope.selected.Name +' failed to be deleted from service '+ $scope.service.name);}
                );
         }
     };

     $scope.showLayer = function() {
         $('#viewerData').modal("show");
         var layerName = $scope.selected.Name;
         var layerData = DataViewer.createLayerWMS(layerName, $scope.service.identifier);
         var layerBackground = DataViewer.createLayer("CNTR_BN_60M_2006", "generic_shp");
         DataViewer.layers = [layerData, layerBackground];
         DataViewer.initMap('dataMap');
     };

     $scope.toggleUpDownSelected = function() {
         var $header = $('#serviceDashboard').find('.selected-item').find('.block-header');
         $header.next().slideToggle(200);
         $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
     };

     // Style methods
     $scope.showStyleList = function() {
         StyleSharedService.showStyleList($scope);
     };

     $scope.unlinkStyle = function(providerName, styleName, dataProvider, dataId) {
         StyleSharedService.unlinkStyle($scope,providerName, styleName, dataProvider, dataId, style);
     };
    }]);

cstlAdminApp.controller('DataModalController', ['$scope', 'dataListing', 'webService', '$dashboard', '$modalInstance', 'service', 'exclude', '$growl',
    function ($scope, dataListing, webService, $dashboard, $modalInstance, service, exclude, $growl) {
        $scope.filtertype = "";
        $scope.nbbypage = 5;
        $scope.exclude = exclude;

        dataListing.listAll({}, function(response) {
            $dashboard($scope, response);
        });

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        $scope.choose = function(data) {
            if (data == null) {
                $growl('warning','Warning','No layer selected');
                $modalInstance.dismiss('close');
            } else {
                // Add chosen data to this service
                webService.addLayer({type: service.type, id: service.identifier},
                                    {layerAlias: data.Name, layerId: data.Name, serviceType: service.type, serviceId: service.identifier,  providerId: data.Provider},
                                    function() {$growl('success','Success','Layer '+ data.Name +' successfully added to service '+ service.name);
                                                $modalInstance.close();},
                                    function() {$growl('error','Error','Layer '+ data.Name +' failed to be added to service '+ service.name);
                                                $modalInstance.dismiss('close');}
                );

            }
        };
    }]);



