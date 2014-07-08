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

/**
 * Controller for dashboard of styles.
 */
cstlAdminApp.controller('StylesController', ['$scope', '$dashboard', 'style', '$growl', 'StyleSharedService', '$modal',
    function($scope, $dashboard, style, $growl, StyleSharedService, $modal) {
        $scope.init = function() {
            var modalLoader = $modal.open({
                templateUrl: 'views/modalLoader.html',
                controller: 'ModalInstanceCtrl'
            });
            style.listAll({provider: 'sld'},
                function(response) {
                    $dashboard($scope, response.styles, true);
                    $scope.filtertype = "";
                    $scope.ordertype = "Name";
                    modalLoader.close();
                },
                function() {
                    modalLoader.close();
                }
            );
        };

        /**
         * Proceed to remove the selected styles from dashboard.
         */
        $scope.deleteStyle = function() {
            if (confirm("Are you sure?")) {
                var styleName = $scope.selected.Name;
                var providerId = $scope.selected.Provider;
                style.delete({provider: providerId, name: styleName}, {},
                        function() {
                            $growl('success', 'Success', 'Style ' + styleName + ' successfully deleted');
                            style.listAll({provider: 'sld'}, function(response) {
                                $scope.fullList = response.styles;
                            });
                        },
                        function() {
                            $growl('error', 'Error', 'Style ' + styleName + ' deletion failed');
                        });
            }
        };

        /**
         * Proceed to open modal SLD editor to edit the selected style
         */
        $scope.editStyle = function() {
            var styleName = $scope.selected.Name;
            var providerId = $scope.selected.Provider;
            style.get({provider: providerId, name: styleName}, function(response) {
                StyleSharedService.showStyleEdit($scope, response);
            });
        };

        /**
         * Toggle up and down the selected item
         */
        $scope.toggleUpDownSelected = function() {
            var $header = $('#stylesDashboard').find('.selected-item').find('.block-header');
            $header.next().slideToggle(200);
            $header.find('i').toggleClass('icon-chevron-down icon-chevron-up');
        };

        /**
         * Open sld editor modal to create a new style.
         */
        $scope.showStyleCreate = function() {
            StyleSharedService.showStyleCreate($scope);
        };
    }]);

/**
 * Controller for modal popup SLD-Editor used in dashboards styles, services and data layers.
 */
cstlAdminApp.controller('StyleModalController', ['$scope', '$dashboard', '$modalInstance', 'style', '$cookies', 'dataListing', 'provider', '$growl', 'textService', 'newStyle', 'selectedLayer','selectedStyle', 'serviceName', 'exclude','$timeout',
    function($scope, $dashboard, $modalInstance, style, $cookies, dataListing, provider, $growl, textService, newStyle, selectedLayer,selectedStyle, serviceName, exclude, $timeout) {
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
         * to display existing styles dashbord or new style creation panel.
         */
        $scope.stylechooser = 'existing';
        /**
         * the path of page to include in sld editor vectors or raster or chooseType wich is default.
         */
        $scope.page = {
            pageSld: 'views/style/chooseType.html'
        };

        $scope.chosenTab = 'description';

        /**
         * SLD model object that store all needed variables to avoid angular bug behaviour in modal.
         */
        $scope.optionsSLD={
            autoPreview:true,
            selectedRule:null,
            enableRuleEditor:false,
            selectedSymbolizerType:"",
            selectedSymbolizer:null,
            filtersEnabled:false,
            filters:[{
                "attribute":"",
                "comparator":"=",
                "value":"",
                "operator":''
            }]
        };

        
        
        $scope.colorModels = [
          {name:'palette', value:'Palette'},
          {name:'grayscale', value:'Grascale /RGB'}];

	      $scope.colorModel= $scope.colorModels[0];
	
	      $scope.symbolPills = 'color';
	      
	      $scope.selectedBand = undefined;
        
	      $scope.test = {
	    		  'selected':undefined
	      };
        
	      $scope.ichanged = function(){
	    	  console.log($scope.test);
	      }
	      
        /**
         * The json model that represents the style sld.
         */
        $scope.newStyle = newStyle;
        /**
         * Adding watcher for newStyle variable to enable the auto preview on the map.
         */
        $scope.$watch('newStyle', function() {
            if($scope.optionsSLD.autoPreview){
                var mapId = null;
                if($scope.selectedLayer !== null && $scope.stylechooser === 'existing'){
                    mapId = 'styledMapWithSelectedStyle';
                }else {
                    mapId = 'styledMapOL'
                }
                //using $timeout to fix Angular bug :
                // with modal to let OpenLayers map initialization when the map div is not rendered yet.
                $timeout(function(){$scope.displayCurrentStyle(mapId);},100);
            }
        },true);

        //There is a bug in angular for uiModal we cannot fix it with a simple call $parent
        //the following is a fix to wrap the variable from the good scope.
        $scope.wrapScope = {
            filterText : $scope.filtertext,
            nbbypage : $scope.nbbypage || 10
        };
        $scope.$watch('wrapScope.filterText', function() {
            $scope.filtertext =$scope.wrapScope.filterText;
        });
        $scope.$watch('wrapScope.nbbypage', function() {
            $scope.nbbypage =$scope.wrapScope.nbbypage;
        });

        /**
         * The layer's attributes json object.
         */
        $scope.dataProperties = null;
        $scope.dataBbox = null;
        $scope.dataBands = null;

        $scope.palette = {
            index: undefined,
            img_palette: 'images/palette0.png',
            rasterMinValue: 0,
            rasterMaxValue: 0,
            intervalles: 1,
            channelSelection: undefined,
            nan: false,
            inverse: false,
            interpolation:'interpolate'
        };

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
            $scope.ordertype = ordType;
            $scope.orderreverse = !$scope.orderreverse;
        };

        /**
         * The main init function called to prepare the sld editor when opening the modal.
         */
        function initSldPage() {
            //if we are in data dashboard
            if($scope.selectedLayer !== null) {
                $scope.sldName = $scope.selectedLayer.Name + '-sld';

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
            }else if($scope.selectedStyle != null) {
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
            if ($scope.newStyle === null) {
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
        };

        /**
         * setter for stylechooser
         */
        $scope.setStyleChooser = function(choice) {
            $scope.stylechooser = choice;
            if(choice ==='existing'){
                setTimeout(function(){$scope.displayCurrentStyle('styledMapWithSelectedStyle');},100);
            }else {
                setTimeout(function(){$scope.displayCurrentStyle('styledMapOL');},100);
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
                    var rule = {
                        "name": 'default',
                        "title":'',
                        "description":'',
                        "maxScale":1.7976931348623157E308,
                        "symbolizers": [],
                        "filter": null
                    };
                    $scope.newStyle.rules.push(rule);
                    $scope.setSelectedRule(rule);
                    $scope.editSelectedRule();
                }else if(mode ==='auto_interval'){

                }else if(mode ==='auto_values'){

                }else if(mode ==='raster_palette'){
                    var rule = {
                        "name": 'palette-rule',
                        "title":'',
                        "description":'',
                        "maxScale":1.7976931348623157E308,
                        "symbolizers": [],
                        "filter": null
                    };
                    $scope.newStyle.rules.push(rule);
                    $scope.setSelectedRule(rule);
                    $scope.editSelectedRule();
                }else if(mode ==='raster_cellule'){
                    var rule = {
                        "name": 'cell-rule',
                        "title":'',
                        "description":'',
                        "maxScale":1.7976931348623157E308,
                        "symbolizers": [],
                        "filter": null
                    };
                    $scope.newStyle.rules.push(rule);
                    $scope.setSelectedRule(rule);
                    $scope.editSelectedRule();
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
            if ($scope.optionsSLD.selectedRule && confirm("Are you sure?")) {
                var indexToRemove = $scope.newStyle.rules.indexOf($scope.optionsSLD.selectedRule);
                if(indexToRemove>-1){
                    $scope.newStyle.rules.splice(indexToRemove, 1);
                    $scope.optionsSLD.selectedRule = null;
                }
            }
        };

        /**
         * Remove all rules from the current style and set selected rule to null.
         */
        $scope.deleteAllRules = function (){
            if ($scope.newStyle.rules.length >0 && confirm("Are you sure?")) {
                $scope.newStyle.rules= [];
                $scope.optionsSLD.selectedRule = null;
            }
        };

        /**
         * Open the rule editor and make sure before that there is a selectedRule object not null into the scope.
         */
        $scope.editSelectedRule = function(){
            if($scope.optionsSLD.selectedRule !== null){
                $scope.optionsSLD.enableRuleEditor = true;
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
            if($scope.optionsSLD.selectedRule !== null && $scope.optionsSLD.selectedSymbolizerType !== ""){
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
                        }
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
                        }
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
            if(confirm("Are you sure?")){
                var indexToRemove = $scope.optionsSLD.selectedRule.symbolizers.indexOf(symbolizer);
                if(indexToRemove>-1){
                    $scope.optionsSLD.selectedRule.symbolizers.splice(indexToRemove, 1);
                }
            }
        };

        /**
         * Set the selected symbolizer into the scope.
         * @param symbol
         */
        $scope.setSelectedSymbolizer = function (symbol){
            $scope.optionsSLD.selectedSymbolizer = symbol;
        }

        /**
         * Function to move item in array with given indexes from and to.
         * @param array
         * @param from index in array
         * @param to index in array
         */
        function move(array, from, to) {
            if( to === from ) return;
            var target = array[from];
            var increment = to < from ? -1 : 1;
            for(var k = from; k != to; k += increment){
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
                            if(filter.value.indexOf(',')!=-1){
                                var arr= filter.value.split(',');
                                if(arr.length==2 && arr[0].trim()!=='' && arr[1].trim()!==''){
                                    strQuery += operator+filter.attribute + ' ' + filter.comparator +' '+ arr[0]+ ' AND '+arr[1];
                                }
                            }
                        }else {
                            strQuery += operator+filter.attribute + ' ' + filter.comparator + ' \''+ filter.value +'\'';
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
            if($scope.optionsSLD.selectedRule.filter !== null){
                var cql = $scope.optionsSLD.selectedRule.filter;
                var format = new OpenLayers.Format.CQL();
                var olfilter;
                try {
                    olfilter = format.read(cql);
                } catch (err) {
                    console.error(err);
                }
                if(olfilter){
                    $scope.optionsSLD.filters = convertOLFilterToArray(olfilter);
                    $scope.optionsSLD.filtersEnabled=true;
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
            if(olfilter.CLASS_NAME ==='OpenLayers.Filter.Comparison'){
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
            }else if(olfilter.CLASS_NAME ==='OpenLayers.Filter.Logical'){
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
            if(obj.CLASS_NAME ==='OpenLayers.Filter.Logical'){
                if(obj.filters && obj.filters.length==2){
                    if(obj.filters[0].CLASS_NAME === 'OpenLayers.Filter.Comparison' &&
                        obj.filters[1].CLASS_NAME === 'OpenLayers.Filter.Comparison'){
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
                    }else if(obj.filters[0].CLASS_NAME === 'OpenLayers.Filter.Logical' &&
                        obj.filters[1].CLASS_NAME === 'OpenLayers.Filter.Comparison'){
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
         OpenLayers.Filter.Comparison.EQUAL_TO = “==”;
         OpenLayers.Filter.Comparison.NOT_EQUAL_TO = “!=”;
         OpenLayers.Filter.Comparison.LESS_THAN = “<”;
         OpenLayers.Filter.Comparison.GREATER_THAN = “>”;
         OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO = “<=”;
         OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO = “>=”;
         OpenLayers.Filter.Comparison.BETWEEN = “..”;
         OpenLayers.Filter.Comparison.LIKE = “~”;
         OpenLayers.Filter.Comparison.IS_NULL = “NULL”;
         */
        var convertOLComparatorToCQL = function(olType){
            var comparator;
            if(olType =='=='){
                comparator = '=';
            }else if(olType =='..'){
                comparator = 'BETWEEN';
            }else if(olType =='~'){
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
            if(olType =='&&'){
                operator = 'AND';
            }else if(olType =='||'){
                operator = 'OR';
            }else if(olType =='!'){
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
            }else if(operator ==''){
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
            $scope.displayCurrentStyle('styledMapOL');
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
            $scope.displayCurrentStyle('styledMapOL');
        };

        /**
         * Proceed to get layer attributes
         */
        $scope.loadDataProperties = function() {
            provider.dataDesc({providerId: $scope.providerId, dataId: $scope.layerName},
                function(response) {
                    $scope.dataProperties = response;
                },
                function() {
                    $growl('error', 'Error', 'Unable to get data properties for layer '+$scope.layerName);
                }
            );
        };

        /**
         * Proceed to load all data layers properties bbox and band.
         */
        $scope.initDataLayerProperties = function(callback) {
            provider.dataDesc({providerId: $scope.providerId, dataId: $scope.layerName},
                function(response) {
                    $scope.dataProperties = response.properties;
                    $scope.dataBbox = response.boundingBox;
                    $scope.dataBands = response.bands;
                    if ($scope.dataBands && $scope.dataBands.length > 0) {
                        $scope.palette.rasterMinValue = $scope.dataBands[0].minValue;
                        $scope.palette.rasterMaxValue = $scope.dataBands[0].maxValue;
                    }
                    if(typeof callback =='function'){
                        callback();
                    }
                }, function() {
                    $growl('error', 'Error', 'Unable to get data description');
                }
            );
        };

        $scope.isgeophys = false;
        $scope.initRaster1Band = function() {
            $scope.dataType = 'coverage';
            $scope.providerId = 'generic_world_tif';
            $scope.layerName = 'cloudsgrey';
            $scope.newStyle.rules[0].symbolizers[0]['@symbol'] = 'raster';
            $scope.initDataLayerProperties();

            provider.isGeophysic({providerId: $scope.providerId, dataId: $scope.layerName}, function(response) {
                $scope.isgeophys = (response.value == 'true');
            });
        };

        $scope.initRasterNBands = function() {
            // todo
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
//                $growl('success','Success','Style '+ $scope.newStyle.name +' successfully created');
//                $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
//            }, function() {
//                $growl('error','Error','Unable to create style '+ $scope.newStyle.name);
//                $modalInstance.close();
//            });
        };

        /**
         * function called for symbolizer line or polygon for stroke type
         * @param traitType
         */
        $scope.addStrokeDashArray = function(symbolizer,traitType) {
            if (traitType === 'pointille') {
                if (symbolizer.stroke == undefined) {
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
         * init the font model in symbolizer text.
         */
        $scope.initFontFamilies = function(symbolizer) {
            if (symbolizer.font == undefined) {
                symbolizer.font = {};
            }
            if (symbolizer.font.family == undefined) {
                symbolizer.font.family = [];
            }
        };

        $scope.choosePalette = function(index) {
            $scope.palette.img_palette = 'images/palette' + index + '.png';
            $scope.palette.index = index;
        };

      
        $scope.addPalette = function() {
            if ($scope.palette.index == undefined) {
                return;
            }

            if ($scope.newStyle.rules[0].symbolizers[0].colorMap == undefined || 
            		$scope.newStyle.rules[0].symbolizers[0].colorMap.function == undefined ||
            		$scope.newStyle.rules[0].symbolizers[0].colorMap.function['@function'] != $scope.palette.interpolation) {
            	$scope.newStyle.rules[0].symbolizers[0].colorMap = {'function': {'@function': $scope.palette.interpolation}};
            }
            switch ($scope.palette.index) {
                case 1:
                    var delta = $scope.palette.rasterMaxValue - $scope.palette.rasterMinValue;
                    if (!$scope.palette.inverse) {
                        if ($scope.newStyle.rules[0].symbolizers[0].colorMap.function == undefined) {
                            $scope.newStyle.rules[0].symbolizers[0].colorMap.function = {};
                        }
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#e52520'},
                                    {data: delta * 0.25 + $scope.palette.rasterMinValue, color: '#ffde00'},
                                    {data: delta * 0.5 + $scope.palette.rasterMinValue, color: '#95c11f'},
                                    {data: delta * 0.75 + $scope.palette.rasterMinValue, color: '#1d71b8'},
                                    {data: $scope.palette.rasterMinValue, color: '#662483'}
                                ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#662483'},
                                    {data: delta * 0.25 + $scope.palette.rasterMinValue, color: '#1d71b8'},
                                    {data: delta * 0.5 + $scope.palette.rasterMinValue, color: '#95c11f'},
                                    {data: delta * 0.75 + $scope.palette.rasterMinValue, color: '#ffde00'},
                                    {data: $scope.palette.rasterMinValue, color: '#e52520'}
                                ];
                    }
                    break;
                case 2:
                    if (!$scope.palette.inverse) {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#3F3460'},
                                    {data: $scope.palette.rasterMaxValue, color: '#EC1876'}
                                ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#EC1876'},
                                    {data: $scope.palette.rasterMaxValue, color: '#3F3460'}
                                ];
                    }
                    break;
                case 3:
                    if (!$scope.palette.inverse) {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#036531'},
                                    {data: $scope.palette.rasterMaxValue, color: '#FDF01A'}
                                ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#FDF01A'},
                                    {data: $scope.palette.rasterMaxValue, color: '#036531'}
                                ];
                    }
                    break;
                case 4:
                    var delta = $scope.palette.rasterMaxValue - $scope.palette.rasterMinValue;
                    if (!$scope.palette.inverse) {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#2d2e83'},
                                    {data: delta * 0.25 + $scope.palette.rasterMinValue, color: '#1d71b8'},
                                    {data: delta * 0.5 + $scope.palette.rasterMinValue, color: '#ffde00'},
                                    {data: $scope.palette.rasterMinValue, color: '#e52520'}
                                ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#e52520'},
                                    {data: delta * 0.5 + $scope.palette.rasterMinValue, color: '#ffde00'},
                                    {data: delta * 0.75 + $scope.palette.rasterMinValue, color: '#1d71b8'},
                                    {data: $scope.palette.rasterMinValue, color: '#2d2e83'}
                                ];
                    }
                    break;
                case 5:
                    if (!$scope.palette.inverse) {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#000000'},
                                    {data: $scope.palette.rasterMaxValue, color: '#FFFFFF'}
                                ];
                    } else {
                        $scope.newStyle.rules[0].symbolizers[0].colorMap.function.points =
                                [
                                    {data: $scope.palette.rasterMinValue, color: '#FFFFFF'},
                                    {data: $scope.palette.rasterMaxValue, color: '#000000'}
                                ];
                    }
                    break;
                default:
                    break;
            }
        };


        $scope.ok = function() {
            $modalInstance.close($scope.selected);
        };

        $scope.close = function() {
            $modalInstance.dismiss('close');
        };

        /**
         * Proceed to update an existing style.
         */
        $scope.updateStyle = function() {
            // style.updatejson({provider: 'sld', name: $scope.newStyle.name }, $scope.newStyle, function() {
            style.createjson({provider: 'sld'}, $scope.newStyle, function() {
                $growl('success', 'Success', 'Style ' + $scope.newStyle.name + ' successfully created');
                $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
            }, function() {
                $growl('error', 'Error', 'Unable to update style ' + $scope.newStyle.name);
                $modalInstance.close();
            });
        };

        /**
         * Creates a new instance of style in server side by calling rest service.
         */
        $scope.createStyle = function() {
            if ($scope.newStyle.name === "") {
                $scope.noName = true;
            } else {
                if ($scope.dataType.toLowerCase() !== 'coverage' && $scope.dataType.toLowerCase() !== 'raster') {
                    style.createjson({provider: 'sld'}, $scope.newStyle, function() {
                        $growl('success', 'Success', 'Style ' + $scope.newStyle.name + ' successfully updated');
                        $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
                    }, function() {
                        $growl('error', 'Error', 'Unable to create style ' + $scope.newStyle.name);
                        $modalInstance.close();
                    });
                }
                else if ($scope.dataType.toLowerCase() === 'coverage' || $scope.dataType.toLowerCase() === 'raster') {

                    $scope.addPalette();
                    style.createjson({provider: 'sld'}, $scope.newStyle, function() {
                        $growl('success', 'Success', 'Style ' + $scope.newStyle.name + ' successfully created');
                        $modalInstance.close({"Provider": "sld", "Name": $scope.newStyle.name});
                    }, function() {
                        $growl('error', 'Error', 'Unable to create style ' + $scope.newStyle.name);
                        $modalInstance.close();
                    });

                }
            }
        };

        /**
         * Performs a preview of current style in map
         */
        $scope.displayCurrentStyle = function(mapId) {
            if(! $scope.layerName){
                return;
            }

            if($scope.selectedLayer !== null && $scope.stylechooser === 'existing'){
                var styleName = null;
                if ($scope.selected) {
                    styleName = $scope.selected.Name;
                }
                var layerData;
                if (serviceName) {
                    if(styleName){
                        layerData = DataViewer.createLayerWMSWithStyle($cookies.cstlUrl, $scope.layerName, $scope.serviceName, styleName);
                    }   else {
                        layerData = DataViewer.createLayerWMS($cookies.cstlUrl, $scope.layerName, $scope.serviceName);
                    }
                } else {
                    if(styleName){
                        layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, $scope.layerName, $scope.providerId, styleName);
                    }else {
                        layerData = DataViewer.createLayer($cookies.cstlUrl, $scope.layerName, $scope.providerId);
                    }
                }
                //to force the browser cache reloading styled layer.
                layerData.mergeNewParams({ts:new Date().getTime()});
                var layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
                DataViewer.layers = [layerData, layerBackground];
                DataViewer.initMap(mapId);
                $scope.initDataLayerProperties(function(){
                    if ($scope.dataBbox) {
                        var extent = new OpenLayers.Bounds($scope.dataBbox[0], $scope.dataBbox[1], $scope.dataBbox[2], $scope.dataBbox[3]);
                        DataViewer.map.zoomToExtent(extent, true);
                    }
                });
            }else {
                if ($scope.dataType.toLowerCase() === 'coverage' || $scope.dataType.toLowerCase() === 'raster') {
                    $scope.addPalette();
                }

                if ($scope.newStyle.name === "") {
                    $scope.newStyle.name = 'default-sld';
                }

                //creates the current style in a temporary provider.
                style.createjson({provider: 'sld_temp'}, $scope.newStyle,
                    function() {
                        var layerData;
                        var layerBackground = null;
                        if($scope.selectedLayer !== null){
                            if($scope.newStyle.rules.length ==0){
                                layerData = DataViewer.createLayer($cookies.cstlUrl, $scope.layerName, $scope.providerId);
                            }else {
                                layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, $scope.layerName, $scope.providerId, $scope.newStyle.name, "sld_temp");
                            }
                            layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
                        }else {
                            //if there is no selectedLayer ie the sld editor in styles dashboard
                            if ($scope.dataType.toLowerCase() === 'coverage' || $scope.dataType.toLowerCase() === 'raster') {
                                //to avoid layer disappear when rules is empty
                                if($scope.newStyle.rules.length ==0){
                                    layerData = DataViewer.createLayer($cookies.cstlUrl, $scope.layerName, $scope.providerId);
                                }else {
                                    layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, $scope.layerName, $scope.providerId, $scope.newStyle.name, "sld_temp");
                                }
                            }else {
                                layerData = DataViewer.createLayerWithStyle($cookies.cstlUrl, $scope.layerName, $scope.providerId, $scope.newStyle.name, "sld_temp");
                            }

                            if(!existsLinePolygonSymbolizers()){
                                layerBackground = DataViewer.createLayer($cookies.cstlUrl, "CNTR_BN_60M_2006", "generic_shp");
                            }
                        }
                        //to force the browser cache reloading styled layer.
                        layerData.mergeNewParams({ts:new Date().getTime()});

                        if(layerBackground ===null){
                            DataViewer.layers = [layerData];
                        }else {
                            DataViewer.layers = [layerData, layerBackground];
                        }
                        DataViewer.initMap(mapId);

                        if ($scope.dataBbox) {
                            var extent = new OpenLayers.Bounds($scope.dataBbox[0], $scope.dataBbox[1], $scope.dataBbox[2], $scope.dataBbox[3]);
                            DataViewer.map.zoomToExtent(extent, true);
                        }
                    }
                );
            }
        };

        $scope.setMinScale = function(){
            if(DataViewer.map) {
                var currentScale=DataViewer.map.getScale();
                if(currentScale < $scope.optionsSLD.selectedRule.maxScale){
                	$scope.optionsSLD.selectedRule.minScale = currentScale;
                }else{
                	console.log("currenscale > maxScale");
                }
            }
        };

        $scope.setMaxScale = function(){
            if(DataViewer.map) {
                var currentScale=DataViewer.map.getScale();
                if(currentScale > $scope.optionsSLD.selectedRule.minScale){
                	$scope.optionsSLD.selectedRule.maxScale = currentScale;                	
                }else{
                	console.log("currenscale < minScale");
                }
            }
        };

        /**
         * Returns true if there is a LINE or POLYGON symbolizer for the current style.
         * this is needed to set the background layer depending on what symbolizer is present.
         * @returns {boolean}
         */
        var existsLinePolygonSymbolizers = function(){
            for(var i=0;i<$scope.newStyle.rules.length;i++){
                var rule = $scope.newStyle.rules[i];
                for(var j=0;j<rule.symbolizers.length;j++){
                    var symb = rule.symbolizers[j];
                    if(symb['@symbol']==='line' || symb['@symbol']==='polygon'){
                        return true;
                    }
                }
            }
        };

        /**
         * init the dashboard styles in modal.
         * called only when opening sld editor from the data dashboard or service dashboard.
         */
        $scope.initScopeStyle = function() {
            style.listAll({provider: 'sld'}, function(response) {
                $dashboard($scope, response.styles, true);
            });
        };

    }]);