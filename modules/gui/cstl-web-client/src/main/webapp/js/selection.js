function selectElement(element){
    var dataState = $(element).attr("data-state");
    var lastPoint = $(element).css("background-image").lastIndexOf('.');
    var lastSlash = $(element).css("background-image").lastIndexOf('/');
    var pictureType = $(element).css("background-image").substring(lastSlash+1, lastPoint);

    if(dataState == "selected"){
        switch (pictureType){
            case "raster-selected":
                $(element).css("background-image", CSTL.URL_RASTER_PICTURE)
                break;
            case "vecteur-selected":
                $(element).css("background-image", CSTL.URL_VECTEUR_PICTURE)
                break;
            case "sensor-selected":
                $(element).css("background-image", CSTL.URL_SENSOR_PICTURE)
                break;
            case "style-selected":
                $(element).css("background-image", CSTL.URL_STYLE_PICTURE)
                break;
        }

        $(element).css("color", "rgb(51, 51, 51)");
        $(element).attr("data-state", "");
    }else{
        switch (pictureType){
            case "raster":
                $(element).css("background-image", CSTL.URL_RASTER_SELECTED_PICTURE)
                break;
            case "vecteur":
                $(element).css("background-image", CSTL.URL_VECTEUR_SELECTED_PICTURE)
                break;
            case "sensor":
                $(element).css("background-image", CSTL.URL_SENSOR_SELECTED_PICTURE)
                break;
            case "style":
                $(element).css("background-image", CSTL.URL_STYLE_SELECTED_PICTURE)
                break;
        }

        $(element).css("color", "white");
        $(element).attr("data-state", "selected");
    }
}