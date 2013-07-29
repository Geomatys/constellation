/**
 * Add action on nbLayerselect element
 * @param size : max layer number
 */
function addLayerSelectAction(size){
    $("#nbLayersselect").on('change', function(){
        var counter = $(this).val();
        generateList(0, counter, "", "");
        var nbPage = size/counter;
        generatePagination(nbPage);
        $("#previous").addClass("active");
    });
}


/**
 * action call on pagination
 * @param pageSearched : int to know in which page we go
 * @param pageActivated : DOM element we need to activate
 */
function changePage(pageSearched, pageActivated){

    //get number element on page selected
    var nbByPage = $("#nbLayersselect").val();

    //compute first element on next page
    var startElement = (pageSearched-1)*nbByPage;

    //call ajax
    generateList(startElement, nbByPage, "", "");

    //remove all style active on pagination
    $("#paging > .active").removeClass("active");

    //activate selected page
    $(pageActivated).addClass("active")

    //if it's last page, next paging is disabled
    if(pageSearched==$("#paging > .page").length){
        $("#next").addClass("active");
    }

    //if it's first page, previous paging is disabled
    if(pageSearched==1){
        $("#previous").addClass("active");
    }
}

/**
 * Generate pagination part when element number is change
 * @param nbPage : element number by page
 */
function generatePagination(nbPage){
    //clear paging
    $('#paging').empty();

    //if they have only one page, we don't show paging
    if(nbPage>1){

        //add previous button
        $('#paging').append('<li id="previous"><a href="#" class="text-info">&laquo;</a></li>');

        //iterate to add require pages. Activate first page by default
        for(var i=0; i<nbPage; i++){
            var page = i+1;
            if(page==1){
                $('#paging').append('<li class="active page"><a href="#" class="text-info">'+page+'</a></li>');
            }
            else{
                $('#paging').append('<li class="page"><a href="#" class="text-info">'+page+'</a></li>');
            }
        }

        //add next button
        $('#paging').append('<li id="next"><a href="#" class="text-info" >&raquo;</a></li>');

        //add action on previous button
        $("#previous").on('click', function(){
            if(!$(this).hasClass("active")){
                var currentPage = $("#paging > .page.active").text();
                var pageSearched = new Number(currentPage)-1;
                changePage(pageSearched, $("#paging > .page").get(pageSearched-1));
            }
        });

        //add action on next button
        $("#next").on('click', function(){
            if(!$(this).hasClass("active")){
                var currentPage = $("#paging > .page.active").text();
                var pageSearched = new Number(currentPage)+1;
                changePage(pageSearched, $("#paging > .page").get(pageSearched-1));
            }
        });

        //add action on pages
        $("#paging > .page").on('click', function(){
            if(!$(this).hasClass("active")){
                var pageSearched = $(this).text();
                changePage(pageSearched, this);
            }
        });
    }
}