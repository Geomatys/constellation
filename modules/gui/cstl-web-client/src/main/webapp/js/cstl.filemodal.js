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
function gotoNext(){
    $("#nextbutton").hide();

    $("#folderPart").hide(function(){
        $("#typePart").show();
        $("#endbutton").show();
    });
}


var $next;
var $previous;
var depth;
var parentPath;

function addChildOperation() {
    $next.data("depth", depth+1);
    $next.find("a").on("click", {parent : $next}, updateChild);
}

function addParentOperation(){
    $previous.find("a").on("click", {parent : $previous}, updateChild);
    $previous.data("depth", $previous.data("depth")-1);

    var $parent = $previous.find("[data-path='"+parentPath+"']");
    $parent.addClass("text-error");
    var $liParent = $parent.parent()
    var $icon = $liParent.find('i');
    $icon.removeClass("icon-folder-close-alt");
    $icon.addClass("icon-folder-open-alt");
    $icon.css("color", "#b94a48");


}

/**
 * move element from panel right to left.
 * @returns {*|jQuery|HTMLElement}
 */
function moveLeft(){
    var $first = $("[data-panel='1']");
    $first.empty();

    var $second = $("[data-panel='2']");
    $first.append($second.html());
    $first.find("a").on("click", {parent : $first}, updateChild);
    $first.data("depth", $second.data("depth"));
    $second.empty();

    var $third = $("[data-panel='3']");
    $second.append($third.html());
    $second.find("a").on("click", {parent : $second}, updateChild);
    $second.data("depth", $third.data("depth"));
    $third.empty();
    return $third;
}

/**
 * move element from panel left to right.
 */
function moverRight($clickedElement){
    var $third = $("[data-panel='3']");
    $third.empty();

    var $second = $("[data-panel='2']");
    $third.append($second.html());
    $third.find("a").on("click", {parent : $third}, updateChild);
    $third.data("depth", $second.data("depth"));
    $second.empty();

    var $first = $("[data-panel='1']");
    $second.append($first.html());
    $second.find("a").on("click", {parent : $second}, updateChild);
    $second.data("depth", $first.data("depth"));

    $first.empty();
    var endParentPathIndex = $clickedElement.data("path").lastIndexOf("/");
    var path = $clickedElement.data("path").substring(0, endParentPathIndex);
    parentPath = path;
    endParentPathIndex = path.lastIndexOf("/");
    path = path.substring(0, endParentPathIndex);

    $previous = $first;
    $first.jzLoad("DataController.getDataFolders()",{"path":path}, addParentOperation);
}


/**
 * Set elements on panels
 * @param event
 */
function updateChild(event){
    $(this).tooltip("hide");

    var $parent = event.data.parent;
    var $domParent = $parent.parent();

    var panel = $parent.data("panel");
    depth = $parent.data("depth");

    var hasNext = $(this).data("next");
    var nextLevel = panel+1;

    //remove color on old selection
    var $liParent = $(this).parent();
    var $ulParent = $liParent.parent();
    var $oldSelect = $ulParent.find("a.text-error");
    $oldSelect.siblings("i").css("color", "#5bc0de");
    $oldSelect.removeClass("text-error");

    //set new color
    var $icon = $liParent.find('i');
    $icon.css("color", "#b94a48");
    $(this).addClass("text-error");

    if(hasNext==true){
        //folder icons
        $oldSelect.siblings("i").removeClass("icon-folder-open-alt");
        $oldSelect.siblings("i").addClass("icon-folder-close-alt");
        $icon.removeClass("icon-folder-close-alt");
        $icon.addClass("icon-folder-open-alt");

        //add folder on breadcrumb
        updateBreadcrumb($(this).data("path"));

        if(depth>1 && panel==1){
            moverRight($(this));
        }else{
            var $nextChild = $domParent.children(":nth-child("+nextLevel+")");
            //we are less than 3
            if($nextChild.length==0){
                $nextChild = moveLeft()
            }

            hideAll($parent);
            $nextChild.empty();
            $next = $nextChild;
            var path = $(this).data("path");
            $nextChild.jzLoad("DataController.getDataFolders()",{"path":path}, addChildOperation);
            showAll($domParent, nextLevel)
        }
    }else{
        $domParent.children(":nth-child("+nextLevel+")").hide();
        //Update file path to know file to load
        $("#filePath").val($(this).data("path"));
    }
}

/**
 *
 */
function filterFiles($Element){
    var $ulParent = $Element.parent().parent();
    var $liaElements = $ulParent.find("a");
    for (var i = 0; i < $liaElements.length; i++) {
        var $selectedAnchor = $($liaElements.get(i));
        var innerHtml = $selectedAnchor.html();
        if(!innerHtml.contains($Element.val())){
            $selectedAnchor.parent().hide();
        }else{
            $selectedAnchor.parent().show();
        }
    }
}

/**
 *
 */
function updateBreadcrumb(path){
    $breadcrumb = $(".breadcrumb");
    $breadcrumb.empty();
    var pathArray = path.split("/");
    for (var i = 0; i < pathArray.length; i++) {
        var pathPart = pathArray[i];
        $breadcrumb.append("<li class='active'>"+pathPart+"<span class='divider'>/</span></li>");
    }
}