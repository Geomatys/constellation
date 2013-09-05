function selectElement(element){
    var dataState = $(element).attr("data-state");
    var pictureType = $(element).data("providertype");

    if(dataState == "selected"){
        switch (pictureType){
            case "raster":
                $(element).css("background-image", CSTL.URL_RASTER_PICTURE);
                break;
            case "vector":
                $(element).css("background-image", CSTL.URL_VECTOR_PICTURE);
                break;
            case "sensor":
                $(element).css("background-image", CSTL.URL_SENSOR_PICTURE);
                break;
            case "pyramid":
                $(element).css("background-image", CSTL.URL_PYRAMID_PICTURE);
                break;
            case "style":
                $(element).css("background-image", CSTL.URL_STYLE_PICTURE);
                break;
        }

        $(element).css("color", "rgb(51, 51, 51)");
        $(element).attr("data-state", "");
    }else{
        switch (pictureType){
            case "raster":
                $(element).css("background-image", CSTL.URL_RASTER_SELECTED_PICTURE);
                break;
            case "vector":
                $(element).css("background-image", CSTL.URL_VECTOR_SELECTED_PICTURE);
                break;
            case "sensor":
                $(element).css("background-image", CSTL.URL_SENSOR_SELECTED_PICTURE);
                break;
            case "pyramid":
                $(element).css("background-image", CSTL.URL_PYRAMID_SELECTED_PICTURE);
                break;
            case "style":
                $(element).css("background-image", CSTL.URL_STYLE_SELECTED_PICTURE);
                break;
        }

        $(element).css("color", "white");
        $(element).attr("data-state", "selected");
    }
}