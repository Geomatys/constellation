CSTL.Netcdf = {
    $caller : null,

    /**
     * Change layer saw
     * @param $caller
     */
    changeLayer : function ($caller, providerId){
        CSTL.Netcdf.$caller = $caller;
        CSTL.Netcdf.loadCRS(providerId);

        for (var i = 0; i < map.layers.length; i++) {
            var layer = map.layers[i];
            map.removeLayer(layer);
        }

        var layer = CSTL.Netcdf.createLayer($caller.data("value"), providerId);
        $("#coverageName").empty();
        $("#coverageName").html($caller.data("value"));

        map.addLayer(layer);
        map.zoomToExtent();
    },

    updateCRS : function (data) {
        var crs = data.Entry;
        $("#horizontal").val(crs[0]);

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

    loadCRS : function (providerId){
        var url = window.location.protocol + "//" + window.location.host +"/constellation/api/1/crs/";
        url = url + providerId+"/"+CSTL.Netcdf.$caller.data("value");
        $.getJSON(url, CSTL.Netcdf.updateCRS);
    },

    /**
     *
     * @param layerName
     * @param providerId
     */
    createLayer : function (layerName, providerId){
        var layer = new OpenLayers.Layer.WMS('portray',
            '/constellation/api/1/portrayal/portray',
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

    chooseHorizontal : function(){
        //TODO get Json EPSG list
        var url = window.location.protocol + "//" + window.location.host +"/constellation/api/1/crs/all";
        $.getJSON(url, function(data){
            alert(data);

            //TODO build Inner HTML on modal


            //TODO open modal
        });
    }
}





