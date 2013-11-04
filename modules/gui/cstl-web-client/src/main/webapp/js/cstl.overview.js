/**
 * Change layer saw
 * @param $caller
 */
function changeLayer($caller, providerId){
    for (var i = 0; i < map.layers.length; i++) {
        var layer = map.layers[i];
        map.removeLayer(layer);
    }

    var layer = createLayer($caller.data("value"), providerId);
    $("#coverageName").empty();
    $("#coverageName").html($caller.data("value"));

    map.addLayer(layer);
    map.zoomToExtent();
}

/**
 *
 * @param layerName
 * @param providerId
 */
function createLayer(layerName, providerId){
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
}
