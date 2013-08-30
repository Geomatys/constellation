function goToDataListing(){
    $("#dataList").fadeIn("slow");
    $("#dataName").fadeOut("slow");
    $("#styleList").fadeOut("slow");
    $("#continue").off("click");
    $("#continue").on("click", function(){
        //clear css
        $selectedElement = $("#dataList").find("[data-state=selected]");

        selectElement($selectedElement)

        //set form
        var providerId = $selectedElement.attr("data-provider");
        var layerName =$selectedElement.attr("data-layername");
        $("#providerId").val(providerId);
        $('#layerProviderId').val(layerName);

        //copy selected div
        var htmlSelectedElement = $selectedElement.parent().html()
        $selectedElement.attr("data-state", "");
        $selectedData = $("[data-part=selectedData]");
        $selectedData.empty();
        $selectedData.append(htmlSelectedElement);

        $appendedSelectedElement = $("[data-part=selectedData] [data-state=selected]");
        $appendedSelectedElement.off("click");
        $appendedSelectedElement.attr("data-state", "");
        goToAlias();
    });
}

function goToAlias(){
    $("#dataList").fadeToggle(function(){
        $("#dataName").fadeToggle();
        $("#continue").off("click");
        $("#continue").on("click", function(){
            loadStyle(0, 10, "", "", "");
            goToStyleList();
        });
    });
}

function goToStyleList(){
    $("#dataName").fadeToggle(function(){
        $("#styleList").fadeToggle();
        $("#continue").off("click");
        $("#continue").on("click", function(){
            $("#addLayerForm").submit();
        });
    });
}