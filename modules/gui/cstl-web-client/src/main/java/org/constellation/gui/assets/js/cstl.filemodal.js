function hideAll($Element){
    $Element.parent().children().hide();
    $Element.show();
}

/**
 * Show element from first panel to
 * @param $domParent
 * @param counter
 */
function showAll($domParent, counter){
    for(var i=0; i<counter; i++){
        var child = $domParent.children()[i];
        $(child).show();
    }
}

/**
 *
 */
function loadFolder(){
    $("#nextbutton").hide();

    $("#typePart").hide(function(){
        $("#folderPart").show();
        var $first = $("[data-panel='1']");
        hideAll($first);
        $first.jzLoad("DataController.getDataFolders()",{"path":""}, function(){
            $("#first").find("a").on("click", {parent : $first}, updateChild);
        });
    });
}


var $next;
var depth;

function addChildOperation() {
    $next.data("depth", depth+1);
    $next.find("a").on("click", {parent : $next}, updateChild);
}

/**
 * Set elements on panels
 * @param event
 */
function updateChild(event){
    var $parent = event.data.parent;
    var $domParent = $parent.parent();

    var panel = $parent.data("panel");
    depth = $parent.data("depth");

    var hasNext = $(this).data("next");
    var nextLevel = panel+1;

    if(hasNext==true){

        if(depth>1 && panel==1){
            moverRight();
        }else{
            var $nextChild = $domParent.children(":nth-child("+nextLevel+")");
            //we are less than 3
            if($nextChild.length==0){
                $nextChild = moveLeft()
            }

            hideAll($parent);
            $nextChild.empty();
            $next = $nextChild;
            $nextChild.jzLoad("DataController.getDataFolders()",{"path":$(this).data("path")}, addChildOperation);
            showAll($domParent, nextLevel)
        }
    }else{
        $domParent.children(":nth-child("+nextLevel+")").hide();
        //TODO update file path to know file to load
        $("#filePath").val($(this).data("path"));

        //TODO submit form
        $("#serverFileModalForm").submit();
    }
}