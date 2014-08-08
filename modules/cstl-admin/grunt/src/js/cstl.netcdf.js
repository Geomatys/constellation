Netcdf = {
    $caller : null,

    index: 0,

    changedCRS: new Array(),
    /**
     * Change layer saw
     * @param $caller
     */


    updateCRS : function (data) {
        var crs = data.Entry;

        var val = Netcdf.searchOnChanged();
        if(val === undefined){
            $("#horizontal").val(crs[0]);
        }else{
            $("#horizontal").val(val.crsName);
            $("#horizontal").data("value", val.crsCode);
        }

        if(crs[1]===undefined){
            $("#vertical").parent().parent().hide();
        }else{
            $("#vertical").parent().parent().show();
            $("#vertical").val(crs[1]);
        }
        if(crs[2]===undefined){
            $("#temporal").parent().parent().hide();
        }else{
            $("#temporal").parent().parent().show();
            $("#temporal").val(crs[2]);
        }
    },



    /**
     *
     * @param layerName
     * @param providerId
     */
    createLayer : function (layerName, providerId){
        var layer = new OpenLayers.Layer.WMS('portray',
            '@cstl/api/1/portrayal/portray',
            {
                request:    'Portray',
                layers:     layerName,
                provider:   providerId
            },
            {
                ratio: 1,
                isBaseLayer: true,
                singleTile: true,
                transitionEffect: 'resize',
                tileOptions: {
                    maxGetUrlLength: 2048
                }
            }
        );
        return layer;
    },

    chooseHorizontal : function(filter, state){
        var filterSelected = "none";
        if(filter != ""){
            filterSelected=filter;
        }


        if(state === "previous"){
            Netcdf.index = Netcdf.index-10;
        }else if(state === "next"){
            Netcdf.index = Netcdf.index+10;
        }

        var url = window.location.protocol + "//" + window.location.host +"/constellation/api/1/crs/all/"+Netcdf.index+"/10/"+filterSelected;
        console.warn("URL : "+url);


        //Get Json EPSG list
        $.getJSON(url, Netcdf.buildCRSListing);
    },

    buildCRSListing : function(data){
        var max = data.length;
        var epsgs = data.selectedEPSGCode.entry;

        var $epsgTable = $("#epsgTable");
        $epsgTable.empty();

        //build Inner HTML on modal
        for (var i = 0; i < epsgs.length; i++) {
            var epsg = epsgs[i];
            var line = '<tr><td><a href="#" data-value="'+epsg.value+'"> '+epsg.key+'</a></td></tr>';
            $epsgTable.append(line);
        }

        $("#epsgTable a").on("click", function(event){
            console.log("clicked on "+$(this).data("value"));
            event.stopPropagation();

            $("#horizontal").val($(this).html());
            $("#horizontal").data("value", $(this).data("value"));
            var layer = Netcdf.$caller.data("value");
            Netcdf.updateChangedCRS(layer, $(this).data("value"), $(this).html());

            $('#chooseHorizontal').modal("hide");
        })

        var $nbElement = $("#nbElements");
        $nbElement.empty();
        $nbElement.append(max+ "elements");

        $("#previous").parent().removeClass("disabled");
        $("#next").parent().removeClass("disabled");

        if(Netcdf.index==0){
            $("#previous").parent().addClass("disabled");
        }

        if((Netcdf.index+10)>max){
            $("#next").parent().addClass("disabled");
        }

        //open modal
        $('#chooseHorizontal').modal();
    },

    updateChangedCRS: function(layer, value, name){
        var toSaved = {"crsCode":value, "crsName":name};
        for (var i = 0; i < Netcdf.changedCRS.length; i++) {
            var alreadyChanged = Netcdf.changedCRS[i];
            if(layer == alreadyChanged[0]){
                alreadyChanged[1] = toSaved;
                var layerId = "#"+layer.replace(/[^\w\s]/gi, '');
                $(layerId).val(value);
                return;
            }
        }
        Netcdf.changedCRS[Netcdf.changedCRS.length] = [layer, toSaved];
        var idparsed = layer.replace(/[^\w\s]/gi, '');
        var newInput = '<input type="hidden" id="'+idparsed+'" name="'+layer+'" value="'+value+'"/>';
        $("#crsSubmition").append(newInput);
    },

    searchOnChanged : function(){
        var search = Netcdf.$caller.data("value");
        for (var i = 0; i < Netcdf.changedCRS.length; i++) {
            var changed = Netcdf.changedCRS[i];
            if(search == changed[0]){
                return changed[1];
            }
        }
        return undefined;
    }
}





