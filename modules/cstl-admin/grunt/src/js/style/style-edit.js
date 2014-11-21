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

angular.module('cstl-style-edit', ['cstl-restapi', 'cstl-services', 'ui.bootstrap.modal'])

    .controller('StyleModalController', function($scope, Dashboard, $modalInstance, style, $cookieStore, dataListing,
                                                 provider, Growl, textService, newStyle, selectedLayer,selectedStyle,
                                                 serviceName, exclude, $timeout,stylechooser,$modal) {

        /**
         * To fix angular bug with nested scope.
         */
        $scope.wrap = {};

        $scope.xmlStyle = '<xml></xml>';
        $scope.exclude = exclude;

        /**
         * selectedLayer is not null when opening sld editor modal from data dashboard.
         */
        $scope.selectedLayer = selectedLayer || null;

        /**
         * selectedStyle is not null when opening sld editor modal from styles dashboard.
         */
        $scope.selectedStyle = selectedStyle || null;

        /**
         * The service name when opening sld editor modal from Services dashboard.
         */
        $scope.serviceName = serviceName || null;

        /**
         * The sld name
         */
        $scope.sldName = '';

        /**
         * stylechooser is used as a flag to switch between tabs
         * to display existing styles dashbord or new style creation panel or edit state.
         */
        $scope.stylechooser = stylechooser || 'existing';
        /**
         * the path of page to include in sld editor vectors or raster or chooseType wich is default.
         */
        $scope.page = {
            pageSld: 'views/style/chooseType.html'
        };

        /**
         * SLD model object that store all needed variables to avoid angular bug behaviour in modal.
         */
        function initOptionsSLD(){
            $scope.optionsSLD={
                userStyleName:'default-sld',
                temporaryStyleName:'',
                enabledVectorChart:false,
                enabledRasterChart:false,
                autoPreview:true,
                selectedRule:null,
                enableRuleEditor:false,
                autoIntervalValues:{
                    "attr":"",
                    "nbIntervals":5,
                    "method":"equidistant",
                    "symbol":"polygon",
                    "palette": {
                        index: 1,
                        img_palette: 'img/palette1.png',
                        colors:[],
                        reverseColors:false
                    },
                    "customPalette":{
                        "enabled":false,
                        "color1":'#ffffff',
                        "color2":'#0022fc'
                    }
                },
                enableAutoIntervalEditor:false,
                autoUniqueValues:{
                    "attr":"",
                    "symbol":"polygon",
                    "palette": {
                        index: 1,
                        img_palette: 'img/palette1.png',
                        colors:[],
                        reverseColors:false
                    },
                    "customPalette":{
                        "enabled":false,
                        "color1":'#ffffff',
                        "color2":'#0022fc'
                    }
                },
                enableAutoUniqueEditor:false,
                rasterPalette:{
                    "symbolPills":'color',
                    "colorModel":'palette',
                    band: {
                        "selected":undefined
                    },
                    palette: {
                        "index": undefined,
                        "img_palette": 'img/palette0.png',
                        "rasterMinValue": 0,
                        "rasterMaxValue": 0,
                        "intervalsCount": 5,
                        "channelSelection": undefined,
                        nan: {
                            "color":undefined,
                            "selected":false
                        },
                        "inverse": false,
                        "method":'interpolate',
                        "open":false
                    },
                    repartition:undefined,
                    dataXArray:[],
                    rgbChannels : [{name:''},{name:''},{name:''}],
                    greyChannel :{
                        name: ''
                    }
                },
                enableRasterPalette:false,
                rasterCells:{
                    "cellSize":20,
                    "cellType":'point',
                    pointSymbol:{
                        "@symbol": 'point',
                        "name":'',
                        "graphic":{
                            "size":15,
                            "rotation":0,
                            "opacity":1,
                            "mark":{
                                "geometry":'circle',
                                "stroke":{
                                    "color":'#000000',
                                    "opacity":1
                                },
                                "fill":{
                                    "color":'#808080',
                                    "opacity":0.7
                                }
                            }
                        }
                    },
                    textSymbol:{
                        "@symbol": 'text',
                        "name":'',
                        "label":'',
                        "font":{
                            "size":12,
                            "bold":false,
                            "italic":false,
                            "family":['Arial']
                        },
                        "fill":{
                            "color":"#000000",
                            "opacity":1
                        }
                    }
                },
                enableRasterCells:false,
                selectedSymbolizerType:"",
                selectedSymbolizer:null,
                filtersEnabled:false,
                showFilterTextArea:false,
                filters:[{
                    "attribute":"",
                    "comparator":"=",
                    "value":"",
                    "operator":''
                }],
                chart:{
                    "widget":null,
                    "attribute":"",
                    "min":null,
                    "max":null
                }
            };
        }
        initOptionsSLD();

        /**
         * The json model that represents the style sld.
         */
        $scope.newStyle = newStyle;
        /**
         * Adding watcher for newStyle variable to enable the auto preview on the map.
         */
        $scope.$watch('newStyle.rules', function() {
            if($scope.optionsSLD.autoPreview){
                var mapId = null;
                var timeout = 100;
                if($scope.selectedLayer && $scope.stylechooser === 'existing'){
                    mapId = 'styledMapWithSelectedStyle';
                    timeout = 400;
                }else {
                    mapId = 'styledMapOL';
                }
                //using $timeout to fix Angular bug :
                // with modal to let OpenLayers map initialization when the map div is not rendered yet.
                $timeout(function(){$scope.displayCurrentStyle(mapId,null);},timeout);
            }
        },true);

        //There is a bug in angular for uiModal we cannot fix it with a simple call $parent
        //the following is a fix to wrap the variable from the good scope.
        $scope.wrapScope = {
            filterText : $scope.wrap.filtertext,
            nbbypage : $scope.wrap.nbbypage || 10
        };
        $scope.$watch('wrapScope.filterText', function() {
            $scope.wrap.filtertext =$scope.wrapScope.filterText;
        });
        $scope.$watch('wrapScope.nbbypage', function() {
            $scope.wrap.nbbypage =$scope.wrapScope.nbbypage;
        });

        /**
         * The layer's metadata properties.
         */
        $scope.dataProperties = null;
        $scope.attributesTypeNumber = [];
        $scope.attributesExcludeGeometry = [];
        $scope.dataBbox = null;
        $scope.dataBands = null;
        /**
         * This is the distribution for all bands from current raster layer.
         */
        $scope.dataBandsRepartition = null;

        /**
         * This is the mapping code->css class for awesome font in symbolizer point selection.
         */
        $scope.fontsMapping={
            '#0xf072':'fa-plane',
            '#0xf0e7':'fa-bolt',
            '#0xf06e':'fa-eye',
            '#0xf024':'fa-flag',
            '#0xf112':'fa-reply',
            '#0xf0e9':'fa-umbrella',
            '#0xf041':'fa-map-marker',
            '#0xf06d':'fa-fire',
            '#0xf002':'fa-search',
            '#0xf007':'fa-user',
            '#0xf071':'fa-warning',
            '#0xf0ad':'fa-wrench',
            '#0xf09e':'fa-rss',
            '#0xf13d':'fa-anchor',
            '#0xf06c':'fa-leaf',
            '#0xf0c2':'fa-cloud'
        };
        $scope.fontsCodes = [];
        for(var code in $scope.fontsMapping){
            if($scope.fontsMapping.hasOwnProperty(code)){
                $scope.fontsCodes.push(code);
            }
        }

        /**
         * Affect alpha from colorpicker into param.opacity
         * @param value
         * @param param
         */
        $scope.affectAlpha = function(value, param) {
            param.opacity = value.getAlpha();
        };

        /**
         * Used to fix a bug with angular into modal popup for dashboard (existing styles) to sort items.
         * @param ordType
         */
        $scope.clickFilter = function(ordType){
            $scope.wrap.ordertype = ordType;
            $scope.wrap.orderreverse = !$scope.wrap.orderreverse;
        };

        /**
         * The main init function called to prepare the sld editor when opening the modal.
         */
        function initSldPage() {
            //if we are in data dashboard
            if($scope.selectedLayer) {
                var timestamp=new Date().getTime();
                $scope.sldName = $scope.selectedLayer.Name + '-sld' + timestamp;
                $scope.optionsSLD.userStyleName = $scope.selectedLayer.Name + '-sld';
                $scope.optionsSLD.temporaryStyleName = $scope.sldName;

                var layerName;
                if ($scope.selectedLayer.Namespace) {
                    layerName = '{' + $scope.selectedLayer.Namespace + '}' + $scope.selectedLayer.Name;
                } else {
                    layerName = $scope.selectedLayer.Name;
                }

                if ($scope.selectedLayer.Type &&
                    ($scope.selectedLayer.Type.toLowerCase() === 'coverage' ||
                        $scope.selectedLayer.Type.toLowerCase() === 'coverage-store')) {
                    //going to raster page
                    $scope.chooseType = true;
                    $scope.page.pageSld = 'views/style/raster.html';
                    $scope.dataType = 'raster';
                    $scope.providerId = $scope.selectedLayer.Provider;
                    $scope.layerName = layerName;
                }else if($scope.selectedLayer.Type &&
                    ($scope.selectedLayer.Type.toLowerCase() === 'vector' ||
                        $scope.selectedLayer.Type.toLowerCase() === 'feature-store')){
                    //going to vector page
                    $scope.chooseType = true;
                    $scope.page.pageSld = 'views/style/vectors.html';
                    $scope.dataType = 'vector';
                    $scope.providerId = $scope.selectedLayer.Provider;
                    $scope.layerName = layerName;
                }else {
                    //going to chooseType page
                    $scope.chooseType = false;
                    $scope.page.pageSld = 'views/style/chooseType.html';
                }
            }else if($scope.selectedStyle) {
                //we are in styles dashboard
                if ($scope.selectedStyle.Type &&
                    ($scope.selectedStyle.Type.toLowerCase() === 'coverage' ||
                        $scope.selectedStyle.Type.toLowerCase() === 'coverage-store')) {
                    //going to raster page
                    $scope.chooseType = true;
                    $scope.page.pageSld = 'views/style/raster.html';
                    $scope.dataType = 'raster';
                    $scope.providerId = 'generic_world_tif';
                    $scope.layerName = 'cloudsgrey';
                }else if($scope.selectedStyle.Type &&
                    ($scope.selectedStyle.Type.toLowerCase() === 'vector' ||
                        $scope.selectedStyle.Type.toLowerCase() === 'feature-store')){
                    //going to vector page
                    $scope.chooseType = true;
                    $scope.page.pageSld = 'views/style/vectors.html';
                    $scope.dataType = 'vector';
                    $scope.providerId = 'generic_shp';
                    $scope.layerName = 'CNTR_RG_60M_2006';
                }
            }else {
                //going to chooseType page
                $scope.chooseType = false;
                $scope.page.pageSld = 'views/style/chooseType.html';
            }
            //prevent the sld object is never null
            if (!$scope.newStyle) {
                $scope.newStyle = {
                    "name": $scope.sldName,
                    "rules": []
                };
            }
        }
        //Call the initSldPage() function to determine which page we are going to open.
        initSldPage();

        /**
         * Function to allow the user to go back to rules list using the breadcrumb after opening a rule.
         *
         */
        $scope.goBack = function() {
            $scope.optionsSLD.enableRuleEditor = false;
            $scope.optionsSLD.enableAutoIntervalEditor = false;
            $scope.optionsSLD.enableAutoUniqueEditor = false;
            $scope.optionsSLD.enableRasterPalette = false;
            $scope.optionsSLD.enableRasterCells = false;
        };

        /**
         * Binding function to control ng-if for displaying mode buttons to switch between carto or chart.
         * the view mode must be activated only if the layer data exists.
         * @returns {boolean}
         */
        $scope.shouldDisplayVectorChart = function() {
            if($scope.selectedLayer){
                return true;
            }else {
                return false;
            }
        };

        /**
         * Binding action to show or display the raster's histogram chart
         */
        $scope.toggleRasterChart = function(){
            $scope.optionsSLD.enabledRasterChart= !$scope.optionsSLD.enabledRasterChart;
            if($scope.optionsSLD.enabledRasterChart){
                //fix bug for graph resize.
                setTimeout(function(){window.c3chart.resize();},200);
            }
        };

        /**
         * Configure sld editor with given style object to edit them.
         * @param styleObj
         */
        $scope.editChooseStyle = function(styleObj) {
            //init all necessary objects for given style
            $scope.setStyleChooser('edit');
            var styleName = styleObj.Name;
            var providerId = styleObj.Provider;
            if(styleObj.Type === 'VECTOR'){
                style.get({provider: providerId, name: styleName}, function(response) {
                    $scope.newStyle = response;
                    $scope.selectedStyle = styleObj;
                    initOptionsSLD();
                    $scope.loadDataProperties();
                    $scope.initVectorPlot();
                });
            }else {
                style.get({provider: providerId, name: styleName}, function(response) {
                    $scope.newStyle = response;
                    $scope.selectedStyle = styleObj;
                    initOptionsSLD();
                    $scope.loadDataProperties();
                });
            }
        };

        /**
         * Reset sld editor for new style creation.
         */
        $scope.editNewStyle = function() {
            //init all necessary objects for new style
            $scope.setStyleChooser('new');
            $scope.newStyle = null;
            $scope.selectedStyle = null;
            initOptionsSLD();
            initSldPage();
            $scope.loadDataProperties();
        };

        /**
         * Configure the sld editor with a copy of the given style object
         * to create a new style based.
         * @param styleObj
         */
        $scope.duplicateChooseStyle = function(styleObj) {
            //@TODO make a duplication of given style to create a new style based on it.
            //$scope.setStyleChooser('duplicate');
        };

        /**
         * setter for stylechooser
         */
        $scope.setStyleChooser = function(choice) {
            $scope.stylechooser = choice;
            if(choice ==='existing'){
                setTimeout(function(){$scope.displayCurrentStyle('styledMapWithSelectedStyle',null);},100);
            }else {
                setTimeout(function(){$scope.displayCurrentStyle('styledMapOL',null);},100);
            }
        };

        /**
         * Returns true if the given choice matches the stylechooser.
         * @param choice
         * @returns {boolean}
         */
        $scope.isSelectedChooser = function(choice) {
            return choice === $scope.stylechooser;
        };

        /**
         * Creates a new rule for given mode :
         * available values are :
         * for vector 'manual', 'auto_interval', 'auto_values'
         * for raster 'raster_palette', 'raster_cellule'
         * @param mode
         */
        $scope.createRules = function(mode){
            if ($scope.newStyle.name === "") {
                $scope.noName = true;
                return; //invalid style the name is required
            }else {
                $scope.noName = false;
                if(mode ==='manual'){
                    var manualRule = {
                        "name": 'default',
                        "title":'',
                        "description":'',
                        "maxScale":500000000,
                        "symbolizers": [],
                        "filter": null
                    };
                    $scope.newStyle.rules.push(manualRule);
                    $scope.setSelectedRule(manualRule);
                    $scope.editSelectedRule();
                }else if(mode ==='auto_interval'){
                    $scope.optionsSLD.autoIntervalValues = {
                        "attr":"",
                        "nbIntervals":5,
                        "method":"equidistant",
                        "symbol":"polygon",
                        "palette": {
                            index: 1,
                            img_palette: 'img/palette1.png',
                            colors:[],
                            reverseColors:false
                        },
                        "customPalette":{
                            "enabled":false,
                            "color1":'#ffffff',
                            "color2":'#0022fc'
                        }
                    };
                    $scope.editAutoIntervalPanel();
                }else if(mode ==='auto_values'){
                    $scope.optionsSLD.autoUniqueValues = {
                        "attr":"",
                        "symbol":"polygon",
                        "palette": {
                            index: 1,
                            img_palette: 'img/palette1.png',
                            colors:[],
                            reverseColors:false
                        },
                        "customPalette":{
                            "enabled":false,
                            "color1":'#ffffff',
                            "color2":'#0022fc'
                        }
                    };
                    $scope.editAutoUniquePanel();
                }else if(mode ==='raster_palette'){
                    $scope.optionsSLD.rasterPalette = {
                        "symbolPills":'color',
                        "colorModel":'palette',
                        band: {
                            "selected":undefined
                        },
                        palette: {
                            "index": undefined,
                            "img_palette": 'img/palette0.png',
                            "rasterMinValue": 0,
                            "rasterMaxValue": 0,
                            "intervalsCount": 5,
                            "channelSelection": undefined,
                            nan: {
                                "color":undefined,
                                "selected":false
                            },
                            "inverse": false,
                            "method":'interpolate',
                            "open":false
                        },
                        repartition:undefined,
                        dataXArray:[],
                        rgbChannels : [{name:''},{name:''},{name:''}],
                        greyChannel :{
                            name: ''
                        }
                    };
                    var paletteRule = {
                        "name": 'palette-rule-'+new Date().getTime(),
                        "title":'',
                        "description":'',
                        "maxScale":500000000,
                        "symbolizers": [{'@symbol':'raster'}],
                        "filter": null
                    };
                    $scope.newStyle.rules.push(paletteRule );
                    $scope.setSelectedRule(paletteRule );
                    $scope.editRasterPalette();
                }else if(mode ==='raster_cellule'){
                    $scope.optionsSLD.rasterCells = {
                        "cellSize":20,
                        "cellType":'point',
                        pointSymbol:{
                            "@symbol": 'point',
                            "name":'',
                            "graphic":{
                                "size":15,
                                "rotation":0,
                                "opacity":1,
                                "mark":{
                                    "geometry":'circle',
                                    "stroke":{
                                        "color":'#000000',
                                        "opacity":1
                                    },
                                    "fill":{
                                        "color":'#808080',
                                        "opacity":0.7
                                    }
                                }
                            }
                        },
                        textSymbol:{
                            "@symbol": 'text',
                            "name":'',
                            "label":'',
                            "font":{
                                "size":12,
                                "bold":false,
                                "italic":false,
                                "family":['Arial']
                            },
                            "fill":{
                                "color":"#000000",
                                "opacity":1
                            }
                        }
                    };
                    var cellRule = {
                        "name": 'cell-rule-'+new Date().getTime(),
                        "title":'',
                        "description":'',
                        "maxScale":500000000,
                        "symbolizers": [{'@symbol':'raster'},{
                            '@symbol':'cell',
                            "cellSize":20,
                            rule:{
                                "name": 'default',
                                "title":'',
                                "description":'',
                                "maxScale":500000000,
                                "symbolizers": [],
                                "filter": null
                            }
                        }],
                        "filter": null
                    };
                    $scope.newStyle.rules.push(cellRule);
                    $scope.setSelectedRule(cellRule);
                    $scope.editRasterCells();
                }
                $scope.optionsSLD.filtersEnabled=false;
                $scope.optionsSLD.filters=[{
                    "attribute":"",
                    "comparator":"=",
                    "value":"",
                    "operator":''
                }];
            }
        };

        /**
         * Set the selected rule object into the scope.
         * @param rule
         */
        $scope.setSelectedRule = function(rule){
            $scope.optionsSLD.selectedRule = rule;
        };

        /**
         * Remove the selected rule from the current style's rules array.
         */
        $scope.deleteSelectedRule = function (){
            if ($scope.optionsSLD.selectedRule) {
                var dlg = $modal.open({
                    templateUrl: 'views/modal-confirm.html',
                    controller: 'ModalConfirmController',
                    resolve: {
                        'keyMsg':function(){return "dialog.message.confirm.delete.rule";}
                    }
                });
                dlg.result.then(function(cfrm){
                    if(cfrm){
                        var indexToRemove = $scope.newStyle.rules.indexOf($scope.optionsSLD.selectedRule);
                        if(indexToRemove>-1){
                            $scope.newStyle.rules.splice(indexToRemove, 1);
                            $scope.optionsSLD.selectedRule = null;
                        }
                    }
                });
            }
        };

        /**
         * Remove all rules from the current style and set selected rule to null.
         */
        $scope.deleteAllRules = function (){
            if ($scope.newStyle.rules.length >0) {
                var dlg = $modal.open({
                    templateUrl: 'views/modal-confirm.html',
                    controller: 'ModalConfirmController',
                    resolve: {
                        'keyMsg':function(){return "dialog.message.confirm.delete.allrules";}
                    }
                });
                dlg.result.then(function(cfrm){
                    if(cfrm){
                        $scope.newStyle.rules= [];
                        $scope.optionsSLD.selectedRule = null;
                    }
                });
            }
        };

        /**
         * For Vector : Open the rule editor and make sure before that there is a selectedRule object not null into the scope.
         * make the rule editor panel visible.
         */
        $scope.editSelectedRule = function(){
            if($scope.optionsSLD.selectedRule){
                $scope.optionsSLD.enableRuleEditor = true;
                $scope.optionsSLD.enableAutoIntervalEditor = false;
                $scope.optionsSLD.enableAutoUniqueEditor = false;
            }
        };
        /**
         * Edit rule for raster.
         * and restore $scope.optionsSLD.rasterPalette with the selected rule properties.
         */
        $scope.editSelectedRasterRule = function(){
            //restore scope for channel selections (rgb / grayscale).
            var symbolizers=$scope.optionsSLD.selectedRule.symbolizers;
            if(symbolizers && symbolizers.length>0 && symbolizers[0].channelSelection){
                if(symbolizers[0].channelSelection.greyChannel){
                    $scope.optionsSLD.rasterPalette.greyChannel = symbolizers[0].channelSelection.greyChannel;
                    if(symbolizers[0].colorMap){
                        $scope.optionsSLD.rasterPalette.colorModel = 'palette';
                    }else{
                        $scope.optionsSLD.rasterPalette.colorModel = 'grayscale';
                    }
                }else if(symbolizers[0].channelSelection.rgbChannels){
                    $scope.optionsSLD.rasterPalette.rgbChannels = symbolizers[0].channelSelection.rgbChannels;
                    $scope.optionsSLD.rasterPalette.colorModel = 'rgb';
                }
            }
            if(! existsCellSymbolizer(symbolizers)){
                $scope.optionsSLD.enableRasterPalette = true;
                $scope.optionsSLD.enableRasterCells = false;

                //init sld editor values with selected rule.
                var channelSelection = symbolizers[0].channelSelection;
                if(channelSelection && channelSelection.greyChannel && $scope.optionsSLD.rasterPalette.band && $scope.dataBands) {
                    var bandIdentified = null;
                    for(var i=0;i<$scope.dataBands.length;i++){
                        if($scope.dataBands[i].name === channelSelection.greyChannel.name){
                            bandIdentified =$scope.dataBands[i];
                            break;
                        }
                    }
                    if(!bandIdentified){
                        bandIdentified = $scope.dataBands[0];
                    }
                    $scope.optionsSLD.rasterPalette.band.selected = bandIdentified;
                    $scope.optionsSLD.rasterPalette.palette.rasterMinValue = $scope.optionsSLD.rasterPalette.band.selected.minValue;
                    $scope.optionsSLD.rasterPalette.palette.rasterMaxValue = $scope.optionsSLD.rasterPalette.band.selected.maxValue;
                }
                var colorMap = symbolizers[0].colorMap;
                if(colorMap){
                    $scope.optionsSLD.rasterPalette.palette.method = colorMap.function['@function'];
                    $scope.optionsSLD.rasterPalette.palette.intervalsCount = colorMap.function.interval;

                    if(colorMap.function.nanColor && colorMap.function.nanColor){
                        $scope.optionsSLD.rasterPalette.palette.nan.selected = true;
                        $scope.optionsSLD.rasterPalette.palette.nan.color = colorMap.function.nanColor;
                    }
                    $scope.optionsSLD.rasterPalette.repartition = colorMap.function.points;
                }
                //Load the selected band on the graph, the repartition of statistics is already present.
                if($scope.dataBandsRepartition && $scope.optionsSLD.rasterPalette.band.selected){
                    var selectedBand = $scope.optionsSLD.rasterPalette.band.selected.name;
                    if(!selectedBand){
                        selectedBand = 0;
                    }
                    var xArray=[],yArray=[];
                    if($scope.dataBandsRepartition[selectedBand]){
                        var repartitionBand = $scope.dataBandsRepartition[selectedBand].fullDistribution;
                        for(var key in repartitionBand){
                            if(repartitionBand.hasOwnProperty(key)){
                                xArray.push(key);
                                yArray.push(repartitionBand[key]);
                            }
                        }
                    }
                    $scope.optionsSLD.rasterPalette.dataXArray = xArray;
                    var dataRes = {
                        json:{
                            x: xArray,
                            data1: yArray
                        }
                    };
                    //load data on graph
                    $scope.loadPlot(dataRes,'Band '+selectedBand,
                        true,460,205,'#chartRaster',{}
                    );
                }
                //Add on graph the vertical thresholds
                $scope.drawThresholds();
                $scope.optionsSLD.rasterPalette.palette.open = true;
            }else {
                //open raster cells panel
                $scope.optionsSLD.enableRasterPalette = false;
                $scope.optionsSLD.enableRasterCells = true;
                if(symbolizers.length>1){
                    var symb = symbolizers[1];
                    $scope.optionsSLD.rasterCells.cellSize = symb.cellSize;
                    if(symb.rule && symb.rule.symbolizers && symb.rule.symbolizers.length>0){
                        var cellType = symb.rule.symbolizers[0]['@symbol'];
                        $scope.optionsSLD.rasterCells.cellType = cellType;
                        if(cellType === 'point'){
                            $scope.optionsSLD.rasterCells.pointSymbol = symb.rule.symbolizers[0];
                        }else if(cellType === 'text'){
                            $scope.optionsSLD.rasterCells.textSymbol = symb.rule.symbolizers[0];
                        }
                    }
                }
            }
        };

        /**
         * For Vector : make autoInterval Panel visible.
         */
        $scope.editAutoIntervalPanel = function(){
            if($scope.optionsSLD.autoIntervalValues){
                $scope.optionsSLD.enableAutoIntervalEditor=true;
                $scope.optionsSLD.enableRuleEditor = false;
                $scope.optionsSLD.enableAutoUniqueEditor = false;
            }
        };
        /**
         * For Vector : make autoUnique values panel to visible.
         */
        $scope.editAutoUniquePanel = function(){
            if($scope.optionsSLD.autoUniqueValues){
                $scope.optionsSLD.enableAutoUniqueEditor = true;
                $scope.optionsSLD.enableAutoIntervalEditor=false;
                $scope.optionsSLD.enableRuleEditor = false;
            }
        };
        /**
         * For Raster : make raster palette panel to visible
         */
        $scope.editRasterPalette = function() {
            if($scope.optionsSLD.rasterPalette) {
                $scope.optionsSLD.enableRasterPalette = true;
                $scope.optionsSLD.enableRasterCells = false;
            }
        };
        /**
         * For Raster : make raster cells panel to visible
         */
        $scope.editRasterCells = function() {
            if($scope.optionsSLD.rasterPalette) {
                $scope.optionsSLD.enableRasterPalette = false;
                $scope.optionsSLD.enableRasterCells = true;
            }
        };

        /**
         * Move rule position to previous index in rules array
         */
        $scope.moveUpRule = function(){
            if ($scope.optionsSLD.selectedRule){
                var indexPos = $scope.newStyle.rules.indexOf($scope.optionsSLD.selectedRule);
                if(indexPos>0) {
                    move($scope.newStyle.rules,indexPos,indexPos-1);
                }
            }
        };

        /**
         * Move rule position to next index in rules array
         */
        $scope.moveDownRule = function(){
            if ($scope.optionsSLD.selectedRule){
                var indexPos = $scope.newStyle.rules.indexOf($scope.optionsSLD.selectedRule);
                if(indexPos<$scope.newStyle.rules.length-1) {
                    move($scope.newStyle.rules,indexPos,indexPos+1);
                }
            }
        };

        /**
         * Add new symbolizer for current rule.
         * the geometry type is given from the select element.
         */
        $scope.addSymbolizer = function() {
            if($scope.optionsSLD.selectedRule && $scope.optionsSLD.selectedSymbolizerType){
                var symbol;
                if($scope.optionsSLD.selectedSymbolizerType === 'point'){
                    symbol={
                        "@symbol": 'point',
                        "name":$scope.optionsSLD.selectedSymbolizerType,
                        "graphic":{
                            "size":15,
                            "rotation":0,
                            "opacity":1,
                            "mark":{
                                "geometry":'circle',
                                "stroke":{
                                    "color":'#000000',
                                    "opacity":1
                                },
                                "fill":{
                                    "color":'#808080',
                                    "opacity":0.7
                                }
                            }
                        }
                    };
                }else if($scope.optionsSLD.selectedSymbolizerType === 'line'){
                    symbol={
                        "@symbol": 'line',
                        "name":$scope.optionsSLD.selectedSymbolizerType,
                        "stroke":{
                            "color":"#000000",
                            "dashArray":null,
                            "dashOffset":0,
                            "dashed":false,
                            "lineCap":"square",
                            "lineJoin":"bevel",
                            "opacity":1,
                            "width":1
                        },
                        "perpendicularOffset":0
                    };
                }else if($scope.optionsSLD.selectedSymbolizerType === 'polygon'){
                    symbol={
                        "@symbol": 'polygon',
                        "name":$scope.optionsSLD.selectedSymbolizerType,
                        "fill":{
                            "color":"#c1c1c1",
                            "opacity":1
                        },
                        "stroke":{
                            "color":"#000000",
                            "dashArray":null,
                            "dashOffset":0,
                            "dashed":false,
                            "lineCap":"square",
                            "lineJoin":"bevel",
                            "opacity":1,
                            "width":1
                        },
                        "perpendicularOffset":0
                    };
                }else if($scope.optionsSLD.selectedSymbolizerType === 'text'){
                    symbol={
                        "@symbol": 'text',
                        "name":$scope.optionsSLD.selectedSymbolizerType,
                        "label":'',
                        "font":{
                            "size":12,
                            "bold":false,
                            "italic":false,
                            "family":['Arial']
                        },
                        "fill":{
                            "color":"#000000",
                            "opacity":1
                        },
                        "halo":{
                            "radius":1,
                            "fill":{
                                "color":"#FFFFFF",
                                "opacity":1.0
                            }
                        }
                    };
                }
                $scope.optionsSLD.selectedRule.symbolizers.push(symbol);
                $scope.setSelectedSymbolizer(symbol);
                //@TODO scrollTo the new symbolizer panel and open it.
            }
        };

        /**
         * Remove given symbolizer from the current rule.
         * @param symbolizer
         */
        $scope.removeSymbolizer = function(symbolizer) {
            var dlg = $modal.open({
                templateUrl: 'views/modal-confirm.html',
                controller: 'ModalConfirmController',
                resolve: {
                    'keyMsg':function(){return "dialog.message.confirm.delete.symbolizer";}
                }
            });
            dlg.result.then(function(cfrm){
                if(cfrm){
                    var indexToRemove = $scope.optionsSLD.selectedRule.symbolizers.indexOf(symbolizer);
                    if(indexToRemove>-1){
                        $scope.optionsSLD.selectedRule.symbolizers.splice(indexToRemove, 1);
                    }
                }
            });
        };

        /**
         * Set the selected symbolizer into the scope.
         * @param symbol
         */
        $scope.setSelectedSymbolizer = function (symbol){
            $scope.optionsSLD.selectedSymbolizer = symbol;
        };

        /**
         * Function to move item in array with given indexes from and to.
         * @param array
         * @param from index in array
         * @param to index in array
         */
        function move(array, from, to) {
            if( to === from ) {return;}
            var target = array[from];
            var increment = to < from ? -1 : 1;
            for(var k = from; k !== to; k += increment){
                array[k] = array[k + increment];
            }
            array[to] = target;
        }

        /**
         * Binding action for checkbox to enable or disable filter in current rule.
         */
        $scope.applyFilter = function() {
            if($scope.optionsSLD.filtersEnabled){
                //apply current filter to the model
                var strQuery = '';
                var operator = '';
                for(var i=0;i<$scope.optionsSLD.filters.length;i++){
                    var filter = $scope.optionsSLD.filters[i];
                    if(filter.attribute !==''){
                        if(filter.comparator === 'BETWEEN'){
                            if(filter.value.indexOf(',')!==-1){
                                var arr= filter.value.split(',');
                                if(arr.length===2 && arr[0].trim()!=='' && arr[1].trim()!==''){
                                    strQuery += operator+filter.attribute + ' ' + filter.comparator +' '+ arr[0]+ ' AND '+arr[1];
                                }
                            }
                        }else {
                            var strFilter = filter.value;
                            //escape CQL quote from the ui value before apply
                            if(strFilter.indexOf("'") !== -1){
                                var find = "'";
                                var re = new RegExp(find, 'g');
                                strFilter = strFilter.replace(re, "\\'");
                            }
                            strQuery += operator+filter.attribute + ' ' + filter.comparator + ' \''+ strFilter +'\'';
                        }
                        if(filter.operator !== ''){
                            operator = ' '+filter.operator+' ';
                        }
                    }
                }
                if(strQuery !== ''){
                    $scope.optionsSLD.selectedRule.filter = strQuery;
                }
            }else {
                //remove filter for current model
                $scope.optionsSLD.selectedRule.filter = null;
            }
        };

        /**
         * Called at init the ng-repeat for filters to read the current rule's filter and affect the local variable.
         */
        $scope.restoreFilters = function() {
            if($scope.optionsSLD.selectedRule.filter){
                var cql = $scope.optionsSLD.selectedRule.filter;
                if(cql.indexOf('\\\'') !== -1){
                    var find = "\\\\\'";
                    var re = new RegExp(find, 'g');
                    cql = cql.replace(re, "''");
                }

                //@TODO ol3 does not have any cql formatter, so needs to write one. good luck.
                var format = new olext.Format.CQL();
                var olfilter;
                var readfailed = false;
                try {
                    olfilter = format.read(cql);
                } catch (err) {
                    console.error(err);
                    readfailed=true;
                }
                if(olfilter){
                    $scope.optionsSLD.filters = convertOLFilterToArray(olfilter);
                    $scope.optionsSLD.filtersEnabled=true;
                }else {
                    $scope.optionsSLD.filtersEnabled=false;
                    $scope.optionsSLD.filters=[{
                        "attribute":"",
                        "comparator":"=",
                        "value":"",
                        "operator":''
                    }];
                    //show textarea instead of auto form inputs in case of read fail
                    $scope.optionsSLD.showFilterTextArea = readfailed;
                }
            }else {
                $scope.optionsSLD.filtersEnabled=false;
                $scope.optionsSLD.filters=[{
                    "attribute":"",
                    "comparator":"=",
                    "value":"",
                    "operator":''
                }];
            }
        };

        /**
         * build an array of query filters for given OpenLayers Filter object.
         * @param olfilter
         * @returns {Array}
         */
        var convertOLFilterToArray = function(olfilter){
            var resultArray = [];
            if(olfilter.CLASS_NAME ==='olext.Filter.Comparison'){
                var comparator = convertOLComparatorToCQL(olfilter.type);
                var value;
                if(comparator === 'BETWEEN'){
                    value = olfilter.lowerBoundary+','+olfilter.upperBoundary;
                }else {
                    value = olfilter.value;
                }
                var q = {
                    "attribute":olfilter.property,
                    "comparator":comparator,
                    "value":value,
                    "operator":''
                };
                resultArray.push(q);
            }else if(olfilter.CLASS_NAME ==='olext.Filter.Logical'){
                recursiveResolveFilter(olfilter,resultArray);
            }
            return resultArray;
        };

        /**
         * recursive function to resolve OpenLayers filter to current model.
         * @param obj
         * @param arrayRes
         */
        var recursiveResolveFilter = function(obj,arrayRes){
            if(obj.CLASS_NAME ==='olext.Filter.Logical'){
                if(obj.filters && obj.filters.length===2){
                    if(obj.filters[0].CLASS_NAME === 'olext.Filter.Comparison' &&
                        obj.filters[1].CLASS_NAME === 'olext.Filter.Comparison'){
                        var comparator1 = convertOLComparatorToCQL(obj.filters[0].type);
                        var value1;
                        if(comparator1 === 'BETWEEN'){
                            value1 = obj.filters[0].lowerBoundary+','+obj.filters[0].upperBoundary;
                        }else {
                            value1 = obj.filters[0].value;
                        }
                        var comparator2 = convertOLComparatorToCQL(obj.filters[1].type);
                        var value2;
                        if(comparator2 === 'BETWEEN'){
                            value2 = obj.filters[1].lowerBoundary+','+obj.filters[1].upperBoundary;
                        }else {
                            value2 = obj.filters[1].value;
                        }
                        var operator = convertOLOperatorToCQL(obj.type);
                        arrayRes.push({
                            "attribute":obj.filters[0].property,
                            "comparator":comparator1,
                            "value":value1,
                            "operator":operator
                        });
                        arrayRes.push({
                            "attribute":obj.filters[1].property,
                            "comparator":comparator2,
                            "value":value2,
                            "operator":''
                        });
                    }else if(obj.filters[0].CLASS_NAME === 'olext.Filter.Logical' &&
                        obj.filters[1].CLASS_NAME === 'olext.Filter.Comparison'){
                        recursiveResolveFilter(obj.filters[0],arrayRes);
                        var op = convertOLOperatorToCQL(obj.type);
                        arrayRes[arrayRes.length-1].operator = op;
                        var comparator = convertOLComparatorToCQL(obj.filters[1].type);
                        var value;
                        if(comparator === 'BETWEEN'){
                            value = obj.filters[1].lowerBoundary+','+obj.filters[1].upperBoundary;
                        }else {
                            value = obj.filters[1].value;
                        }
                        arrayRes.push({
                            "attribute":obj.filters[1].property,
                            "comparator":comparator,
                            "value":value,
                            "operator":''
                        });
                    }
                }
            }
        };

        /**
         * Utility function to convert OpenLayers comparison type to CQL comparator.
         *
         * This is the list of type of the comparison in OpenLayers
         *
         olext.Filter.Comparison.EQUAL_TO = “==”;
         olext.Filter.Comparison.NOT_EQUAL_TO = “!=”;
         olext.Filter.Comparison.LESS_THAN = “<”;
         olext.Filter.Comparison.GREATER_THAN = “>”;
         olext.Filter.Comparison.LESS_THAN_OR_EQUAL_TO = “<=”;
         olext.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO = “>=”;
         olext.Filter.Comparison.BETWEEN = “..”;
         olext.Filter.Comparison.LIKE = “~”;
         olext.Filter.Comparison.ILIKE = “ILIKE”;
         olext.Filter.Comparison.IS_NULL = “NULL”;
         */
        var convertOLComparatorToCQL = function(olType){
            var comparator;
            if(olType ==='=='){
                comparator = '=';
            }else if(olType ==='..'){
                comparator = 'BETWEEN';
            }else if(olType ==='~'){
                comparator = 'LIKE';
            }else {
                comparator = olType;
            }
            return comparator;
        };

        /**
         * Utility function to convert OpenLayers operator
         * @param olType
         * @returns {*}
         */
        var convertOLOperatorToCQL = function(olType){
            var operator;
            if(olType ==='&&'){
                operator = 'AND';
            }else if(olType ==='||'){
                operator = 'OR';
            }else if(olType ==='!'){
                operator = 'NOT';
            }
            return operator;
        };

        /**
         * Binding action for select in filter expression to add a new filter object.
         * @param operator
         */
        $scope.addNewFilter = function(operator,index) {
            if(operator !=='' && (index+1) === $scope.optionsSLD.filters.length){
                var filter = {
                    "attribute":"",
                    "comparator":"=",
                    "value":"",
                    "operator":''
                };
                $scope.optionsSLD.filters.push(filter);
            }else if(operator ===''){
                $scope.optionsSLD.filters = $scope.optionsSLD.filters.slice(0,index+1);
            }
        };

        /**
         * Called in chooseType.html and performs vector init default values.
         */
        $scope.initVectorType = function() {
            $scope.chooseType = true;
            $scope.page.pageSld = 'views/style/vectors.html';
            $scope.dataType = 'vector';
            $scope.providerId = 'generic_shp';
            $scope.layerName = 'CNTR_RG_60M_2006';
            $scope.displayCurrentStyle('styledMapOL',null);
        };

        /**
         * Called in chooseType.html and performs raster init default values.
         */
        $scope.initRasterType = function() {
            $scope.chooseType = true;
            $scope.page.pageSld = 'views/style/raster.html';
            $scope.dataType = 'raster';
            $scope.providerId = 'generic_world_tif';
            $scope.layerName = 'cloudsgrey';
            $scope.displayCurrentStyle('styledMapOL',null);
        };

        /**
         * Proceed to get layer attributes
         */
        $scope.loadDataProperties = function() {
            if($scope.providerId && $scope.layerName) {
                provider.dataDesc({},{values: {'providerId':$scope.providerId, 'dataId':$scope.layerName}},
                    function(response) {//success
                        $scope.dataProperties = response;
                        $scope.attributesTypeNumber = getOnlyNumbersFields(response.properties);
                        $scope.attributesExcludeGeometry = getFieldsExcludeGeometry(response.properties);
                        if($scope.attributesTypeNumber.length>0){
                            $scope.optionsSLD.autoIntervalValues.attr=$scope.attributesTypeNumber[0].name;
                        }
                        if($scope.attributesExcludeGeometry.length>0){
                            $scope.optionsSLD.autoUniqueValues.attr=$scope.attributesExcludeGeometry[0].name;
                        }
                        //for raster only
                        if($scope.dataType === 'raster'){
                            $scope.dataBands = response.bands;
                            if($scope.dataBands && $scope.dataBands.length > 0){
                                $scope.optionsSLD.rasterPalette.band.selected = $scope.dataBands[0];
                                $scope.optionsSLD.rasterPalette.palette.rasterMinValue = $scope.optionsSLD.rasterPalette.band.selected.minValue;
                                $scope.optionsSLD.rasterPalette.palette.rasterMaxValue = $scope.optionsSLD.rasterPalette.band.selected.maxValue;
                            }
                        }
                    },
                    function() {//error
                        Growl('error', 'Error', 'Unable to get data properties for layer '+$scope.layerName);
                    }
                );
            }
        };
        $scope.loadDataProperties();

        /**
         * Fix rzslider bug with angular on value changed for band selector.
         */
        $scope.fixRZSlider = function(){
            $scope.optionsSLD.rasterPalette.palette.rasterMinValue = $scope.optionsSLD.rasterPalette.band.selected.minValue;
            $scope.optionsSLD.rasterPalette.palette.rasterMaxValue = $scope.optionsSLD.rasterPalette.band.selected.maxValue;
        };

        /**
         * Extract and returns all numeric fields from data properties.
         * @param properties
         * @returns {Array}
         */
        var getOnlyNumbersFields = function(properties){
            var arrayRes = [];
            if(properties && properties.length>0){
                for(var i=0;i<properties.length;i++){
                    if(properties[i].type ==='java.lang.Double' ||
                        properties[i].type ==='java.lang.Integer' ||
                        properties[i].type ==='java.lang.Float' ||
                        properties[i].type ==='java.lang.Number' ||
                        properties[i].type ==='java.lang.Long' ||
                        properties[i].type ==='java.lang.Short' ){
                        arrayRes.push(properties[i]);
                    }
                }
            }
            return arrayRes;
        };

        /**
         * Returns all fields excepts the geometry properties.
         * @param properties
         * @returns {Array}
         */
        var getFieldsExcludeGeometry = function(properties){
            var arrayRes = [];
            if(properties && properties.length>0){
                for(var i=0;i<properties.length;i++){
                    //skip geometry field
                    if(properties[i].type.indexOf('com.vividsolutions') === -1){
                        arrayRes.push(properties[i]);
                    }
                }
            }
            return arrayRes;
        };

        /**
         * Proceed to load all data layers properties bbox.
         */
        $scope.initDataLayerProperties = function(callback) {
            if($scope.providerId && $scope.layerName) {
                provider.dataGeoExtent({},{values: {'providerId':$scope.providerId, 'dataId':$scope.layerName}},
                    function(response) {//success
                        $scope.dataBbox = response.boundingBox;
                        if(typeof callback ==='function'){
                            callback();
                        }
                    }, function() {//error
                        Growl('error', 'Error', 'Unable to get data description');
                    }
                );
            }
        };

        $scope.aceLoaded = function(_editor) {
            // Options
            _editor.setValue($scope.xmlStyle);
            $scope.aceEditor = _editor;
        };

        $scope.aceChanged = function(e) {

        };

        $scope.saveXml = function() {
            alert("ace=" + $scope.aceEditor.getValue());
            textService.createStyleXml('sld', $scope.aceEditor.getValue());
//                , function() {
//                Growl('success','Success','Style '+ $scope.newStyle.name +' successfully created');
//                $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
//            }, function() {
//                Growl('error','Error','Unable to create style '+ $scope.newStyle.name);
//                $modalInstance.close();
//            });
        };

        /**
         * function called for symbolizer line or polygon for stroke type
         * @param traitType
         */
        $scope.addStrokeDashArray = function(symbolizer,traitType) {
            if (traitType === 'pointille') {
                if (!symbolizer.stroke) {
                    symbolizer.stroke = {};
                }
                symbolizer.stroke.dashArray = [1, 1];
                symbolizer.stroke.dashed = true;
            } else {
                symbolizer.stroke.dashArray = null;
                symbolizer.stroke.dashed = false;
            }
        };

        /**
         * init the font model for symbolizer text.
         */
        $scope.initFontFamilies = function(symbolizer) {
            if (!symbolizer.font) {
                symbolizer.font = {};
            }
            if (!symbolizer.font.family) {
                symbolizer.font.family = [];
            }
        };

        /**
         * Returns true if the given string value is ttf://fontName#code.
         * @param value
         * @returns {*|boolean}
         */
        $scope.isTTFValue = function(value) {
            return (value && value.indexOf('ttf://') !== -1);
        };

        /**
         * Returns FontAwesome css class for code.
         * @param value
         * @returns {*}
         */
        $scope.resolveClassForCode = function(value) {
            if($scope.isTTFValue(value)){
                return $scope.fontsMapping[value.substring(value.indexOf('#'))];
            }
            return '';
        };

        $scope.choosePalette = function(index) {
            $scope.optionsSLD.rasterPalette.palette.img_palette = 'img/palette' + index + '.png';
            $scope.optionsSLD.rasterPalette.palette.index = index;
        };

        $scope.choosePaletteVectorInterval = function(index) {
            $scope.choosePaletteVector(index,$scope.optionsSLD.autoIntervalValues.palette);
        };

        $scope.choosePaletteVectorUnique = function(index) {
            $scope.choosePaletteVector(index,$scope.optionsSLD.autoUniqueValues.palette);
        };

        $scope.choosePaletteVector = function(index, paletteObj) {
            paletteObj.img_palette = 'img/palette' + index + '.png';
            paletteObj.index = index;

            paletteObj.colors = [];
            switch (index) {
                case 1:
                    paletteObj.colors.push('#e52520','#ffde00','#95c11f','#1d71b8','#662483');
                    break;
                case 2:
                    paletteObj.colors.push('#3F3460','#EC1876');
                    break;
                case 3:
                    paletteObj.colors.push('#036531','#FDF01A');
                    break;
                case 4:
                    paletteObj.colors.push('#2d2e83','#1d71b8','#ffde00','#e52520');
                    break;
                case 5:
                    paletteObj.colors.push('#000000','#FFFFFF');
                    break;
                default:
                    break;
            }
        };

        /**
         * Restore the default palette value in select component in case of custom palette.
         */
        $scope.affectDefaultPalette = function() {
            if($scope.optionsSLD.autoIntervalValues.customPalette.enabled){
                $scope.choosePaletteVectorInterval(0);
            }
            if($scope.optionsSLD.autoUniqueValues.customPalette.enabled){
                $scope.choosePaletteVectorUnique(0);
            }
        };

        /**
         * proceed to generate rules automatically for intervals and apply on current style.
         */
        $scope.generateAutoInterval = function() {
            if(! $scope.layerName){
                return;
            }
            if(! $scope.selectedLayer){
                return;
            }

            //get parameters
            //current layer name and namespace
            var layerName = $scope.layerName;
            var namespace = $scope.selectedLayer.Namespace;
            var dataProvider = $scope.providerId;
            //selected numeric field
            var fieldName = $scope.optionsSLD.autoIntervalValues.attr;
            //intervals count
            var nbIntervals = $scope.optionsSLD.autoIntervalValues.nbIntervals;
            //method
            var method = $scope.optionsSLD.autoIntervalValues.method;
            //symbol
            var symbol = $scope.optionsSLD.autoIntervalValues.symbol;
            //palette colors
            var customPalette =$scope.optionsSLD.autoIntervalValues.customPalette.enabled;
            var colors = [];
            if(customPalette){
                colors.push($scope.optionsSLD.autoIntervalValues.customPalette.color1,$scope.optionsSLD.autoIntervalValues.customPalette.color2);
            }else {
                colors =$scope.optionsSLD.autoIntervalValues.palette.colors;
            }
            if(colors.length===0){
                colors.push('#e52520','#ffde00','#95c11f','#1d71b8','#662483');
            }
            var reverseColors =$scope.optionsSLD.autoIntervalValues.palette.reverseColors;
            if(reverseColors){
                colors = colors.reverse();
            }

            var autoInterval = {
                "attr": fieldName,
                "nbIntervals": nbIntervals,
                "method": method,
                "symbol": symbol,
                "colors":colors
            };

            var wrapper = {
                "layerName": layerName,
                "namespace": namespace,
                "dataProvider":dataProvider,
                "style": $scope.newStyle,
                "intervalValues": autoInterval
            };

            //Now send all params to server and it will create the temporary style and returns the full style as json object.
            style.generateAutoIntervalStyle({provider: 'sld_temp'}, wrapper,
                function(response) {
                    //push rules array in current newStyle object to trigger the changes on the map.
                    if(response.rules && response.rules.length >0){
                        $scope.newStyle.rules = response.rules;
                        $scope.goBack();
                    }
                }
            );
        };

        /**
         * proceed to generate rules automatically for unique values and apply on current style.
         */
        $scope.generateAutoUnique = function() {
            if(! $scope.layerName){
                return;
            }
            if(! $scope.selectedLayer){
                return;
            }

            //get parameters
            //current layer name and namespace
            var layerName = $scope.layerName;
            var namespace = $scope.selectedLayer.Namespace;
            var dataProvider = $scope.providerId;
            //selected field
            var fieldName = $scope.optionsSLD.autoUniqueValues.attr;
            //symbol
            var symbol = $scope.optionsSLD.autoUniqueValues.symbol;
            //palette colors
            var customPalette =$scope.optionsSLD.autoUniqueValues.customPalette.enabled;
            var colors = [];
            if(customPalette){
                colors.push($scope.optionsSLD.autoUniqueValues.customPalette.color1,$scope.optionsSLD.autoUniqueValues.customPalette.color2);
            }else {
                colors =$scope.optionsSLD.autoUniqueValues.palette.colors;
            }
            if(colors.length===0){
                colors.push('#e52520','#ffde00','#95c11f','#1d71b8','#662483');
            }
            var reverseColors =$scope.optionsSLD.autoUniqueValues.palette.reverseColors;
            if(reverseColors){
                colors = colors.reverse();
            }

            var autoUnique = {
                "attr": fieldName,
                "symbol": symbol,
                "colors":colors
            };

            var wrapper = {
                "layerName": layerName,
                "namespace": namespace,
                "dataProvider":dataProvider,
                "style": $scope.newStyle,
                "uniqueValues": autoUnique
            };

            //Now send all params to server and it will create the temporary style and returns the full style as json object.
            style.generateAutoUniqueStyle({provider: 'sld_temp'}, wrapper,
                function(response) {
                    //push rules array in current newStyle object to trigger the changes on the map.
                    if(response.rules && response.rules.length >0){
                        $scope.newStyle.rules = response.rules;
                        $scope.goBack();
                    }
                }
            );
        };

        /**
         * Binding action to generate raster palette.
         */
        $scope.generateRasterPalette = function() {
            if($scope.optionsSLD.selectedRule){
                //first of all, add Palette and ensure that the temporary style exists in server.
                $scope.addPalette($scope.optionsSLD.selectedRule);
                $scope.displayCurrentStyle('styledMapOL', function(createdTmpStyle){
                    //get interpolation points for ui
                    if ($scope.optionsSLD.rasterPalette.palette.index) {
                        //show palette
                        style.paletteStyle({provider: 'sld_temp',
                                            name : $scope.newStyle.name,
                                            ruleName : $scope.optionsSLD.selectedRule.name},
                            function(response) {
                                if(response.points){
                                    $scope.optionsSLD.selectedRule.symbolizers[0].colorMap.function.points = response.points;
                                    $scope.optionsSLD.rasterPalette.repartition = $scope.optionsSLD.selectedRule.symbolizers[0].colorMap.function.points;

                                    //Load the selected band on the graph, the repartition of statistics is already present.
                                    if($scope.dataBandsRepartition){
                                        var loader = $('#chart_ajax_loader');
                                        loader.show();
                                        var selectedBand = $scope.optionsSLD.rasterPalette.band.selected.name;
                                        var xArray=[],yArray=[];
                                        if($scope.dataBandsRepartition[selectedBand]){
                                            var repartition = $scope.dataBandsRepartition[selectedBand].fullDistribution;
                                            for(var key in repartition){
                                                if(repartition.hasOwnProperty(key)){
                                                    xArray.push(key);
                                                    yArray.push(repartition[key]);
                                                }
                                            }
                                        }
                                        $scope.optionsSLD.rasterPalette.dataXArray = xArray;
                                        var dataRes = {
                                            json:{
                                                x: xArray,
                                                data1: yArray
                                            }
                                        };
                                        //load data on graph
                                        $scope.loadPlot(dataRes,'Band '+selectedBand,
                                            true,460,205,'#chartRaster',{}
                                        );
                                        loader.hide();
                                    }

                                    //Add on graph the vertical thresholds
                                    $scope.drawThresholds();
                                }
                            },
                            function() {
                                Growl('error', 'Error', 'Unable to get palette for '+$scope.layerName);
                            }
                        );


                        $scope.optionsSLD.rasterPalette.palette.open = true;
                    }
                });
            }
        };

        /**
         * Remove repartition entry and apply this change on the histogram.
         * @param point
         */
        $scope.removeRepartitionEntry = function(point){
            if ($scope.optionsSLD.rasterPalette.repartition) {
                var dlg = $modal.open({
                    templateUrl: 'views/modal-confirm.html',
                    controller: 'ModalConfirmController',
                    resolve: {
                        'keyMsg':function(){return "dialog.message.confirm.delete.repartitionEntry";}
                    }
                });
                dlg.result.then(function(cfrm){
                    if(cfrm){
                        var indexToRemove = $scope.optionsSLD.rasterPalette.repartition.indexOf(point);
                        if(indexToRemove>-1){
                            $scope.optionsSLD.rasterPalette.repartition.splice(indexToRemove, 1);
                        }
                        //remove threshold vertical line on graph.
                        if(point.data){
                            for(var j=0;j<$scope.optionsSLD.rasterPalette.dataXArray.length;j++){
                                if($scope.optionsSLD.rasterPalette.dataXArray[j] >= point.data){
                                    window.c3chart.xgrids.remove({value:j});
                                    break;
                                }
                            }
                        }else {
                            $scope.optionsSLD.selectedRule.symbolizers[0].colorMap.function.nanColor = null;
                        }
                    }
                });
            }
        };

        /**
         * Action to add new value in colorMap
         */
        $scope.addColorMapEntry = function() {
            $scope.optionsSLD.rasterPalette.repartition.push({data:255,color:'#000000'});
        };

        /**
         * Draw xgrids thresholds.
         */
        $scope.drawThresholds = function(){
            if($scope.optionsSLD.rasterPalette.dataXArray && $scope.optionsSLD.rasterPalette.dataXArray.length>0){
                var gridsArray = [];
                var paletteRepartition = $scope.optionsSLD.rasterPalette.repartition;
                if(paletteRepartition){
                    for(var i=0;i<paletteRepartition.length;i++){
                        var threshold = paletteRepartition[i].data;
                        for(var j=0;j<$scope.optionsSLD.rasterPalette.dataXArray.length;j++){
                            if($scope.optionsSLD.rasterPalette.dataXArray[j] >= threshold){
                                gridsArray.push(
                                    {value:j,
                                        text:threshold});
                                break;
                            }
                        }
                    }
                    window.c3chart.xgrids(gridsArray);
                }
            }
        };

        /**
         * Apply RGB composition for current style and clean up colormap and rasterPalette.repartition.
         */
        $scope.applyRGBComposition = function() {
            var rgbChannels = $scope.optionsSLD.rasterPalette.rgbChannels;
            var isValid = true;
            //@TODO confirm with sld conformance, is it necessary to check channel's band not empty?
            for(var i=0;i<rgbChannels.length;i++){
                if(rgbChannels[i].name ===''){
                    isValid = false;
                    break;
                }
            }
            if(!isValid){
                alert('Please select a band for all channels!');
                return;
            }else {
                //Apply rgb channels to selected rule
                $scope.optionsSLD.selectedRule.symbolizers[0].channelSelection = {
                    greyChannel :null,
                    rgbChannels : $scope.optionsSLD.rasterPalette.rgbChannels
                };
                //clean colorMap for selected rule
                $scope.optionsSLD.rasterPalette.repartition = undefined;
                $scope.optionsSLD.selectedRule.symbolizers[0].colorMap = undefined;
            }
        };

        /**
         * Apply grayscale channel for current style and clean up colormap and rasterPalette.repartition.
         */
        $scope.applyGrayscaleComposition = function() {
            $scope.optionsSLD.selectedRule.symbolizers[0].channelSelection = {
                greyChannel :$scope.optionsSLD.rasterPalette.greyChannel,
                rgbChannels : null
            };
            //clean colorMap for selected rule
            $scope.optionsSLD.rasterPalette.repartition = undefined;
            $scope.optionsSLD.selectedRule.symbolizers[0].colorMap = undefined;
        };

        /**
         * Apply and bind cell point symbolizer for current style
         */
        $scope.applyCellPointSymbolizer = function(){
            $scope.optionsSLD.selectedRule.symbolizers[1].rule.symbolizers[0] = $scope.optionsSLD.rasterCells.pointSymbol;
        };
        /**
         * Apply and bind cell text symbolizer for current style
         */
        $scope.applyCellTextSymbolizer = function(){
            $scope.optionsSLD.selectedRule.symbolizers[1].rule.symbolizers[0] = $scope.optionsSLD.rasterCells.textSymbol;
        };

        /**
         * Apply colorMap on rule for selected palette.
         */
        $scope.addPalette = function(rule) {
            var palette = $scope.optionsSLD.rasterPalette.palette;
            if (!palette.index) {
                return;
            }

            //set channel selection
            rule.symbolizers[0].channelSelection = {
                greyChannel :{
                    name: $scope.optionsSLD.rasterPalette.band.selected.name
                },
                rgbChannels : null
            };

            var colorMap = rule.symbolizers[0].colorMap;

            if (!colorMap|| !colorMap.function ||
                colorMap.function['@function'] !== palette.method) {
                colorMap = {'function': {'@function': palette.method}};
            }

            colorMap.function.interval = palette.intervalsCount;
            if(palette.nan.selected){
                if(palette.nan.color){
                    colorMap.function.nanColor = palette.nan.color;
                }else {
                    colorMap.function.nanColor = '#ffffff';
                }
            }else {
                colorMap.function.nanColor = null;
            }

            switch (palette.index) {
                case 1:
                    var delta1 = palette.rasterMaxValue - palette.rasterMinValue;
                    if (!palette.inverse) {
                        if (!colorMap.function) {
                            colorMap.function = {};
                        }
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#e52520'},
                                {data: delta1 * 0.25 + palette.rasterMinValue, color: '#ffde00'},
                                {data: delta1 * 0.5 + palette.rasterMinValue, color: '#95c11f'},
                                {data: delta1 * 0.75 + palette.rasterMinValue, color: '#1d71b8'},
                                {data: palette.rasterMaxValue, color: '#662483'}
                            ];
                    } else {
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#662483'},
                                {data: delta1 * 0.25 + palette.rasterMinValue, color: '#1d71b8'},
                                {data: delta1 * 0.5 + palette.rasterMinValue, color: '#95c11f'},
                                {data: delta1 * 0.75 + palette.rasterMinValue, color: '#ffde00'},
                                {data: palette.rasterMaxValue, color: '#e52520'}
                            ];
                    }
                    break;
                case 2:
                    if (!palette.inverse) {
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#3F3460'},
                                {data: palette.rasterMaxValue, color: '#EC1876'}
                            ];
                    } else {
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#EC1876'},
                                {data: palette.rasterMaxValue, color: '#3F3460'}
                            ];
                    }
                    break;
                case 3:
                    if (!palette.inverse) {
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#036531'},
                                {data: palette.rasterMaxValue, color: '#FDF01A'}
                            ];
                    } else {
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#FDF01A'},
                                {data: palette.rasterMaxValue, color: '#036531'}
                            ];
                    }
                    break;
                case 4:
                    var delta4 = palette.rasterMaxValue - palette.rasterMinValue;
                    if (!palette.inverse) {
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#2d2e83'},
                                {data: delta4 * 0.25 + palette.rasterMinValue, color: '#1d71b8'},
                                {data: delta4 * 0.5 + palette.rasterMinValue, color: '#ffde00'},
                                {data: palette.rasterMinValue, color: '#e52520'}
                            ];
                    } else {
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#e52520'},
                                {data: delta4 * 0.5 + palette.rasterMinValue, color: '#ffde00'},
                                {data: delta4 * 0.75 + palette.rasterMinValue, color: '#1d71b8'},
                                {data: palette.rasterMinValue, color: '#2d2e83'}
                            ];
                    }
                    break;
                case 5:
                    if (!palette.inverse) {
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#000000'},
                                {data: palette.rasterMaxValue, color: '#FFFFFF'}
                            ];
                    } else {
                        colorMap.function.points =
                            [
                                {data: palette.rasterMinValue, color: '#FFFFFF'},
                                {data: palette.rasterMaxValue, color: '#000000'}
                            ];
                    }
                    break;
                default:
                    break;
            }

            rule.symbolizers[0].colorMap = colorMap;
        };

        /**
         * Delete temporary style created with sld_temp provider.
         */
        $scope.deleteTempStyle = function(){
            if($scope.optionsSLD.temporaryStyleName){
                style.delete({provider: 'sld_temp', name: $scope.optionsSLD.temporaryStyleName});
            }
            if($scope.newStyle.name){
                style.delete({provider: 'sld_temp', name: $scope.newStyle.name});
            }
        };


        $scope.ok = function() {
            //delete temporary style
            $scope.deleteTempStyle();
            $modalInstance.close($scope.selected);
        };

        $scope.close = function() {
            //delete temporary style
            $scope.deleteTempStyle();
            $modalInstance.dismiss('close');
        };

        /**
         * Proceed to update an existing style.
         */
        $scope.updateStyle = function() {
            style.updatejson({provider: 'sld', name: $scope.newStyle.name}, $scope.newStyle, function() {
                Growl('success', 'Success', 'Style ' + $scope.newStyle.name + ' successfully updated');
                $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
            }, function() {
                Growl('error', 'Error', 'Unable to update style ' + $scope.newStyle.name);
                $modalInstance.close();
            });
            //delete temporary style
            $scope.deleteTempStyle();
        };

        /**
         * Creates a new instance of style in server side by calling rest service.
         */
        $scope.createStyle = function() {
            if ($scope.optionsSLD.userStyleName === "") {
                $scope.noName = true;
            } else {
                $scope.newStyle.name = $scope.optionsSLD.userStyleName;
                //write style in server side.
                style.createjson({provider: 'sld'}, $scope.newStyle, function(response) {
                    Growl('success', 'Success', 'Style ' + $scope.newStyle.name + ' successfully created');
                    $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
                }, function() {
                    Growl('error', 'Error', 'Unable to create style ' + $scope.newStyle.name);
                    $modalInstance.close();
                });
                //delete temporary style
                $scope.deleteTempStyle();
            }
        };

        /**
         * Performs a preview of current style in map
         */
        $scope.displayCurrentStyle = function(mapId, callbackAfterCreate) {
            //skip if layerName is undefined
            if(! $scope.layerName){
                return;
            }

            if($scope.selectedLayer && $scope.stylechooser === 'existing'){
                var styleName = null;
                if ($scope.selected) {
                    styleName = $scope.selected.Name;
                }
                var layerData;
                if(styleName){
                    layerData = DataViewer.createLayerWithStyle($cookieStore.get('cstlUrl'), $scope.layerName,
                        $scope.providerId, styleName,null,null,false);
                }else {
                    layerData = DataViewer.createLayer($cookieStore.get('cstlUrl'), $scope.layerName, $scope.providerId,null,true);
                }
                //to force the browser cache reloading styled layer.
                layerData.get('params').ts=new Date().getTime();
                DataViewer.layers = [layerData];
                $scope.initDataLayerProperties(function(){
                    if ($scope.dataBbox) {
                        var extent = [$scope.dataBbox[0], $scope.dataBbox[1], $scope.dataBbox[2], $scope.dataBbox[3]];
                        DataViewer.extent = extent;
                    }
                    DataViewer.initMap(mapId);
                });
            }else {
                if ($scope.newStyle.name === "") {
                    var timestamp=new Date().getTime();
                    $scope.newStyle.name = 'default-sld-'+timestamp;
                    $scope.optionsSLD.temporaryStyleName = $scope.newStyle.name;
                }

                //creates the current style in a temporary provider.
                style.createjson({provider: 'sld_temp'}, $scope.newStyle,
                    function(response) {
                        //$scope.newStyle.rules = response.rules;
                        var layerData;
                        if($scope.selectedLayer){
                            if($scope.newStyle.rules.length ===0){
                                layerData = DataViewer.createLayer($cookieStore.get('cstlUrl'), $scope.layerName,
                                    $scope.providerId,null,true);
                            }else {
                                layerData = DataViewer.createLayerWithStyle($cookieStore.get('cstlUrl'), $scope.layerName,
                                    $scope.providerId, $scope.newStyle.name, "sld_temp",null,false);
                            }
                        }else {
                            //if there is no selectedLayer ie the sld editor in styles dashboard
                            if ($scope.dataType.toLowerCase() === 'raster') {
                                //to avoid layer disappear when rules is empty
                                if($scope.newStyle.rules.length ===0){
                                    layerData = DataViewer.createLayer($cookieStore.get('cstlUrl'), $scope.layerName,
                                        $scope.providerId,null,true);
                                }else {
                                    layerData = DataViewer.createLayerWithStyle($cookieStore.get('cstlUrl'), $scope.layerName,
                                        $scope.providerId, $scope.newStyle.name, "sld_temp",null,false);
                                }
                            }else {
                                layerData = DataViewer.createLayerWithStyle($cookieStore.get('cstlUrl'), $scope.layerName,
                                    $scope.providerId, $scope.newStyle.name, "sld_temp",null,false);
                            }
                        }
                        //to force the browser cache reloading styled layer.
                        layerData.get('params').ts=new Date().getTime();

                        DataViewer.layers = [layerData];
                        setTimeout(function(){
                            DataViewer.initMap(mapId);
                            if ($scope.dataBbox) {
                                var extent = [$scope.dataBbox[0], $scope.dataBbox[1], $scope.dataBbox[2], $scope.dataBbox[3]];
                                DataViewer.zoomToExtent(extent,DataViewer.map.getSize(),true);
                            }
                            else {
                                $scope.initDataLayerProperties(function(){
                                    if ($scope.dataBbox) {
                                        var extent = [$scope.dataBbox[0], $scope.dataBbox[1], $scope.dataBbox[2], $scope.dataBbox[3]];
                                        DataViewer.zoomToExtent(extent,DataViewer.map.getSize(),true);
                                    }
                                });
                            }
                            DataViewer.map.on('moveend',setCurrentScale,DataViewer.map);
                            setCurrentScale();
                            if(typeof callbackAfterCreate ==='function'){
                                callbackAfterCreate(response);
                            }
                        },200);
                    }
                );
            }
        };

        /**
         * Calculate and returns the map scale.
         * OL3 does not have any getScale() function.
         * @returns {number}
         */
        var calcCurrentScale = function () {
            var map = DataViewer.map;
            var view = map.getView();
            var resolution = view.getResolution();
            var mpu =view.getProjection().getMetersPerUnit();
            var dpi = 25.4 / 0.28;
            var scale = resolution * mpu * 39.37 * dpi;
            return scale;
        };

        /**
         * Utility function to set the current scale of OL map into page element.
         */
        var setCurrentScale = function(){
            if(DataViewer.map) {
                var currentScale=calcCurrentScale();
                currentScale = Math.round(currentScale);
                jQuery('.currentScale').html("1 : "+currentScale);
            }
        };

        /**
         * Binding action to set the map's scale as filter min scale.
         */
        $scope.setMinScale = function(){
            if(DataViewer.map) {
                var currentScale=calcCurrentScale();
                currentScale = Math.round(currentScale);
                $scope.optionsSLD.selectedRule.minScale = currentScale;
            }
        };

        /**
         * Binding action to set the map's scale as filter max scale.
         */
        $scope.setMaxScale = function(){
            if(DataViewer.map) {
                var currentScale=calcCurrentScale();
                currentScale = Math.round(currentScale);
                $scope.optionsSLD.selectedRule.maxScale = currentScale;
            }
        };

        /**
         * Returns true if there is a cell symbolizer in given array.
         * Used to identify cellSymbolizers rule against Palette/colors rule
         * @param symbolizers
         * @returns {boolean}
         */
        var existsCellSymbolizer = function(symbolizers){
            if(symbolizers) {
                for(var i=0;i<symbolizers.length;i++){
                    var symb = symbolizers[i];
                    if(symb['@symbol']==='cell'){
                        return true;
                    }
                }
            }
            return false;
        };

        /**
         * init the dashboard styles in modal.
         * called only when opening sld editor from the data dashboard or service dashboard.
         */
        $scope.initScopeStyle = function() {
            style.listAll({provider: 'sld'}, function(response) {
                Dashboard($scope, response.styles, true);
            });
        };

        /**
         * Additional utility functions on Array
         * @param from
         * @param until
         * @returns {Array}
         */
        if (typeof (Array.generate) === "undefined") {
            Array.generate = function (length, generator) {
                var list = new Array(length);
                for (var i = 0; i < length; i++) {
                    list[i] = generator(i);
                }
                return list;
            };
        }
        if (typeof (Math.randomInt) === "undefined") {
            Math.randomInt = function (min, max) {
                return Math.floor(Math.random() * (max - min + 1)) + min;
            };
        }
        if (typeof (Array.generateNumbers) === "undefined") {
            Array.generateNumbers = function (from, until) {
                if (arguments.length === 1) {
                    until = from;
                    from = 0;
                }
                var length = until - from;
                var list = new Array(length);
                for (var i = 0; i < length; i++) {
                    list[i] = i + from;
                }
                return list;
            };
        }

        /**
         * binding for ng-init to display zero data chart.
         */
        $scope.initVectorPlot = function() {
            $scope.loadPlot({
                json: {
                    x: [],
                    data1: []
                }
            },'',true,460,250,'#chart',{top: 20,right: 10,bottom: 6,left: 50});
        };

        $scope.initRasterPlot = function() {
            $scope.loadPlot({
                    json: {
                        x: [],
                        data1: []
                    }
                },'',true,460,205,
                '#chartRaster',{}
            );

            if($scope.layerName && $scope.providerId){
                $('#chart_ajax_loader').show();
                ///show histogram
                var values = {
                    "values":{
                        "dataProvider":$scope.providerId,
                        "dataId":$scope.layerName
                    }
                };
                style.statistics({}, values,
                    function(response){
                        if(response.bands && response.bands.length>0){
                            $scope.dataBandsRepartition = response.bands;
                            var repartition = response.bands[0].fullDistribution;
                            var xArray=[],yArray=[];
                            for(var key in repartition){
                                if(repartition.hasOwnProperty(key)){
                                    xArray.push(key);
                                    yArray.push(repartition[key]);
                                }
                            }
                            $scope.optionsSLD.rasterPalette.dataXArray = xArray;
                            var dataRes = {
                                json:{
                                    x: xArray,
                                    data1: yArray
                                }
                            };
                            var bandName='Band 0';
                            if($scope.dataBands && $scope.dataBands.length>0){
                                bandName =$scope.dataBands[0].name;
                            }
                            $scope.loadPlot(dataRes,bandName,
                                true,460,205,'#chartRaster',{}
                            );
                            $('#chart_ajax_loader').hide();
                        }
                    },
                    function(){
                        Growl('error', 'Error', 'Unable to get statistics '+$scope.layerName);
                    }
                );
            }
        };

        /**
         * load histogram c3 chart for given data and attribute.
         * @param data
         * @param attr
         * @param useCategories
         * @param width
         * @param height
         * @param bindTo
         * @param padding
         */
        $scope.loadPlot = function(data, attr,useCategories,width,height,bindTo,padding) {
            window.c3chart = c3.generate({
                bindto: bindTo,
                size: {
                    height: height,
                    width: width
                },
                padding: padding,
                data: {
                    x: 'x',
                    json: data.json,
                    types: {
                        data1: 'bar'
                    },
                    names: {
                        data1: attr
                    }
                },
                color: {
                    pattern: ['#9edae5']
                },
                zoom: {
                    enabled: true
                },
                bar: {
                    width: {
                        ratio: 0.8
                    }
                },
                axis: {
                    x: {
                        type: useCategories?'category':null
                    },
                    y: {
                        label: {
                            text: "Count",
                            position: 'outer-middle'
                        }
                    }
                }
            });
            $(window).resize(function() {
                if(window.c3chart){
                    window.c3chart.resize();
                }
            });
        };

        $scope.loadPlotForAttribute = function(){
            if(! $scope.layerName){
                return;
            }
            if(! $scope.selectedLayer){
                return;
            }
            if($scope.optionsSLD.chart.attribute ===''){
                $scope.initVectorPlot();
                return;
            }

            //get parameters
            //current layer name and namespace
            var layerName = $scope.layerName;
            var parameters = {
                "values":{
                    "layerName": layerName,
                    "dataProvider":$scope.providerId,
                    "attribute": $scope.optionsSLD.chart.attribute,
                    "intervals":20
                }
            };

            //Now send all params to server and it will create the temporary style and returns the full style as json object.
            style.getChartDataJson({}, parameters,
                function(response) {
                    $scope.optionsSLD.chart.min=response.minimum;
                    $scope.optionsSLD.chart.max=response.maximum;
                    if(response.mapping){
                        var xarray = [];
                        var yarray = [];
                        for(var key in response.mapping){
                            if(response.mapping.hasOwnProperty(key)){
                                xarray.push(key === '' ? 'empty':key);
                                yarray.push(response.mapping[key]);
                            }
                        }
                        var dataRes = {
                            json:{
                                x: xarray,
                                data1: yarray
                            }
                        };
                        $scope.loadPlot(dataRes,$scope.optionsSLD.chart.attribute, true,460,250,'#chart',{top: 20,right: 10,bottom: 6,left: 50});
                    }
                }
            );

        };


        /**
         * utility function that returns true if the expression is a number.
         * otherwise return false.
         * @param expr
         * @returns {boolean}
         */
        $scope.isExpressionNumber = function(expr) {
            var n = Number(expr);
            return isFinite(n);
        };

        $scope.styleBtnSelected = {"color":'#ffffff',"background-color":'#c1c1c1'};
        $scope.styleBtnDefault = {"color":'#333333',"background-color":'#ffffff'};

        $scope.setAttrToInputSize = function(attrName,symbolizerGraphicOrFont) {
            symbolizerGraphicOrFont.size = '"'+attrName+'"';
        };
        $scope.setAttrToInputRotation = function(attrName,symbolizerGraphic) {
            symbolizerGraphic.rotation = '"'+attrName+'"';
        };
        $scope.setAttrToInputOpacity = function(attrName,symbolizerGraphic) {
            symbolizerGraphic.opacity = '"'+attrName+'"';
        };
        $scope.setAttrToInputWidth = function(attrName,symbolizerStroke) {
            symbolizerStroke.width = '"'+attrName+'"';
        };

        /**
         * truncate text with JS.
         * Why not use CSS for this?
         *
         * css rule is
         * {
         *  width: 100px
         *  white-space: nowrap
         *  overflow: hidden
         *  text-overflow: ellipsis // This is where the magic happens
         *  }
         *
         * @param text
         * @returns {string}
         */
        $scope.truncate = function(text){
            if(text) {
                return (text.length > 30) ? text.substr(0, 30) + "..." : text;
            }
        };

    });